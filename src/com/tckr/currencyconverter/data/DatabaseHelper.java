package com.tckr.currencyconverter.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.TextView;

import com.tckr.currencyconverter.DashboardFragment;
import com.tckr.currencyconverter.view.DraggableGridView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	//The Android's default system path of your application database.
	private static String DB_PATH = ""; //"/data/data/com.tckr.currencyconverter/databases/";
	private static String DB_NAME = "currconvert.db";
	private final Context myContext;
	public static String LOG_TAG = "com.tckr.currencyconverter.data.DatabaseHelper";
	private final static CurrencyData[] CURRENCYDATA = CurrencyData.populateData();
	
	private final String SELECT_VERSION_BASE = "0";
	private final String SELECT_BASE_CURRENCY = "1";
	private final String SELECT_DASHBOARD_CURRENCY = "2";
	
	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application 
	 * assets and resources.
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
		DB_PATH = context.getFilesDir().getPath() + "/";
	}
	
	/**
	 * Creates a empty database on the system and rewrites it with your own database. 
	 */
	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if(!dbExist) {
			/* 
			 * By calling this method and empty database will be created into the default system path
			 * of your application so we are gonna be able to overwrite that database with our database.
			 */
			this.getReadableDatabase();
			try {
				copyDataBase();
				Log.d(LOG_TAG, "createDataBase() - copyDataBase() has been executed");
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
			this.getReadableDatabase().close();
		}
		
		// Make updates to database if it needs to be after an app update
		this.checkVersionBase();
	}
	
	// Checks if the database has been created. If not then create it from the Asset Folder.
	@TargetApi(24)
	private boolean checkDataBase() {
			
		SQLiteDatabase checkDB = null;
		boolean dbExist = false;
			
		try {
			// Try opening the database if it exist
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			checkDB = null;
			Log.d(LOG_TAG, "checkDataBase() - Database needs to be created.");
		}
			
		/*
		 * If the database does exist, then checkDB will not be null. Therefore set the dbExist variable to true
		 * and then do clean up by closing the database.
		 */
		if(checkDB != null){
			dbExist = true;
			checkDB.close();
		}
		Log.d(LOG_TAG, "checkDataBase() - checkDB = " + dbExist);
		return dbExist;
	}
	
	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled. This is done by transferring bytestream. 
	 */
	private void copyDataBase() throws IOException{

		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// Transfer bytes from the input file to the output file
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	/**
	 * This method will update the database components if there is a need to do it after an application update.
	 */
	private void checkVersionBase() {
		SQLiteDatabase db = null;
		Cursor c = null;
		String versionBase = "EMPTY";
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
			
			// Execute the query to get the facts for the widget
			c = db.rawQuery("SELECT value FROM dashboard WHERE type = ?", new String[] {SELECT_VERSION_BASE});
			
			// Insert the value		
			if(c.moveToFirst()) {
				versionBase =  c.getString(0);
			}
			
			// You are coming from version code 1.
			if(versionBase.equals("EMPTY")) {
				
				/*
				 * Create a new row in the Database to identify the VERSION CODE BASE of the table 
				 */
				
				// Add the values
				ContentValues cv = new ContentValues();
				cv.put("type", SELECT_VERSION_BASE);
				cv.put("value", "VERSION_CODE_2");
				
				// Insert the record into the database
				db.insert("dashboard", null, cv);
				
				/*
				 * We now need to update the base currency to the new index
				 */
				int baseCurrency = this.getBaseCurrency();
				int newBaseCurrency = -1;
				for(int i = 0; i < CURRENCYDATA.length; i ++) {
					if(CURRENCYDATA[i].getCurrencyIndexOld() == baseCurrency) {
						newBaseCurrency = CURRENCYDATA[i].getCurrencyIndex();
					}
				}
				
				// Update the new base currency
				cv = new ContentValues();
				cv.put("curr_index", newBaseCurrency);
				db.update("dashboard", cv, "type = ? AND curr_index = ?", new String[] {SELECT_BASE_CURRENCY, baseCurrency + ""});
				
				/*
				 * We now need to update the existing curr_index to the new curr_index for the dashboard.
				 */
				Integer[] curr_index = this.getDashboardCurrency();
				
				for(int i = 0; i < curr_index.length; i++) {
					
					// Find the new curr_index
					int newCurrIndex = -1;
					for(int j = 0; j < CURRENCYDATA.length; j ++) {
						if(CURRENCYDATA[j].getCurrencyIndexOld() == curr_index[i].intValue()) {
							newCurrIndex = CURRENCYDATA[j].getCurrencyIndex();
						}
					}
					
					// Update the new curr_index
					cv = new ContentValues();
					cv.put("curr_index", newCurrIndex);
					db.update("dashboard", cv, "type = ? AND curr_index = ?", new String[] {SELECT_DASHBOARD_CURRENCY, curr_index[i].intValue() + ""});
				}
			}
			
			// Close the database
			c.close();
			db.close();
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "checkVersionBase() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
	}
	
	// Gets the base currency for the view. This will get the position.
	public int getBaseCurrency() {
		SQLiteDatabase db = null;
		Cursor c = null;
		int baseCurrency = 84; // USD by default
		
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
			
			// Execute the query to get the facts for the widget
			c = db.rawQuery("SELECT curr_index FROM dashboard WHERE type = ?", new String[] {SELECT_BASE_CURRENCY});
			
			/*
			 * Find and store the fact in an array string
			 */			
			if(c.moveToFirst()) {
				baseCurrency =  c.getInt(0);
			}
			
			// Close the database
			c.close();
			db.close();
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "getBaseCurrency() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
		
		return baseCurrency;
	}
	
	// Gets the curr_index for the view. This will get the position.
	public Integer[] getDashboardCurrency() {
		SQLiteDatabase db = null;
		Cursor c = null;
		ArrayList<Integer> dashCurrency = new ArrayList<Integer>();
	
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

			// Execute the query to get the facts for the widget
			c = db.rawQuery("SELECT curr_index FROM dashboard WHERE type = ? ORDER BY ordering", new String[] {SELECT_DASHBOARD_CURRENCY});

			while(c.moveToNext()) {
				dashCurrency.add(c.getInt(0));
			}

			// Close the database
			c.close();
			db.close();
				
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "getBaseCurrency() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
			
		return (Integer[]) dashCurrency.toArray(new Integer[dashCurrency.size()]);
	}
	
	// Gets the base currency for the view. This will get the position.
	public String[] getDashboardResults() {
		SQLiteDatabase db = null;
		Cursor c = null;
		ArrayList<String> dashResult = new ArrayList<String>();
		
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

			// Execute the query to get the value for the dashboard
			c = db.rawQuery("SELECT value FROM dashboard WHERE type = ? ORDER BY ordering ASC", new String[] {SELECT_DASHBOARD_CURRENCY});

			/*
			 * Find and add it to the ArrayList
			 */
			while(c.moveToNext()) {
				dashResult.add(c.getString(0));
			}

			// Close the database
			c.close();
			db.close();
					
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "getBaseCurrency() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
				
		return (String[]) dashResult.toArray(new String[dashResult.size()]);
	}
	
	// Update the dashboard value for a given currency
	public void setDashboardValue(String currency, String value) {
		SQLiteDatabase db = null;
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
			
			ContentValues cv = new ContentValues();
			cv.put("value", value);
			
			db.update("dashboard", cv, "type = ? AND curr_index = ?", new String[] {SELECT_DASHBOARD_CURRENCY, currency});
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "setDashboardValue() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
		}
		db.close();
	}
	
	// Set the new Base currency identified by the user.
	public void setBaseCurrency(String curr_index, String base) {
		SQLiteDatabase db = null;
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
			
			ContentValues cv = new ContentValues();
			cv.put("curr_index", curr_index);
			cv.put("base", base);
			
			db.update("dashboard", cv, "type = ?", new String[] {SELECT_BASE_CURRENCY});
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "setBaseCurrency() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
		}
		db.close();
	}
	
	/**
	 * This query will be used to update the position of the currency on the dashboard when the user
	 * moves the dashboard around on the screen.
	 * 
	 * positionFrom will be used to find the items to update
	 * positionTo will be the new value to assign to the currency.
	 * 
	 * @param positionFrom
	 * @param positionTo
	 */
	public void updateDashboardPositionCurrency(String positionFrom, String positionTo) {
		SQLiteDatabase db = null;
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
			
			ContentValues cv = new ContentValues();
			cv.put("ordering", positionTo);
				
			db.update("dashboard", cv, "type = ? AND ordering = ?", new String[] {SELECT_DASHBOARD_CURRENCY, positionFrom});
				
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "updateDashboardPositionCurrency() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
		}
		db.close();
	}
	
	/**
	 * This method will insert a currency to the dashboard table. It will look at the highest value
	 * @param curr_index
	 */
	public void addNewCurrToDashboard(String curr_index, TextView tv, DraggableGridView dgv, Context context) {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
			
			// Set the ordering to 0
			int ordering = 0;
			
			// Find the max ordering so when we add the currency we know what to put it as. 
			c = db.rawQuery("SELECT MAX(ordering) FROM dashboard WHERE type = ?", new String[] {SELECT_DASHBOARD_CURRENCY});
			
			// Find and store the fact in an array string - If empty then do nothing	
			if(c.moveToFirst()) {
				ordering =  c.getInt(0);
			}
			
			// Increment the ordering
			ordering++;
			
			// Add the values
			ContentValues cv = new ContentValues();
			cv.put("type", SELECT_DASHBOARD_CURRENCY);
			cv.put("curr_index", curr_index);
			cv.put("value", "Please Refresh");
			cv.put("base", "1");
			cv.put("ordering", ordering);
			
			// Insert the record into the database
			db.insert("dashboard", null, cv);
			
			// Add to the dashboard
			DashboardFragment.addToDashboard(new String[] {"Please Refresh"}, DashboardFragment.getBase(context), context, tv, dgv);
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "setBaseCurrency() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
		db.close();
		c.close();
	}
	
	/**
	 * This will remove the currency from the dashboard.
	 * @param curr_index
	 */
	public void removeCurrFromDashboard(String curr_index, DraggableGridView dgv) {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
			
			// We need to get the ordering number before we delete so we can update the rest of the values.
			c = db.rawQuery("SELECT ordering FROM dashboard WHERE type = ? AND curr_index = ?", new String[] {SELECT_DASHBOARD_CURRENCY, curr_index});
			
			// Find and store the deleted ordering. Default to 100
			int deletedOrdering = 100;
			if(c.moveToFirst()) {
				deletedOrdering =  c.getInt(0);
			}
			
			// Close the cursor as we need to use it again
			c.close();
			
			// Find the max ordering so we know what we have to update after we delete the item.
			c = db.rawQuery("SELECT MAX(ordering) FROM dashboard WHERE type = ?", new String[] {SELECT_DASHBOARD_CURRENCY});
			
			// Find and store the max ordering in a variable. Default to 99
			int maxOrdering = 99;
			if(c.moveToFirst()) {
				maxOrdering =  c.getInt(0);
			}
			
			// Run the delete query to remove the currency from the table.
			db.delete("dashboard", "type = ? AND curr_index = ?", new String[] {SELECT_DASHBOARD_CURRENCY, curr_index});
			
			// Remove from the Grid View
			dgv.removeViewAt(deletedOrdering - 1);
			
			// Start to update the existing records and reorder
			for(int i = deletedOrdering; i < maxOrdering; i++) {
				
				// Update ordering to what is in i where ordering = i + 1
				ContentValues cv = new ContentValues();
				cv.put("ordering", i);
				db.update("dashboard", cv, "type = ? AND ordering = ?", new String[] {SELECT_DASHBOARD_CURRENCY, (i + 1) + ""});
				
			}
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "setBaseCurrency() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
		db.close();
		c.close();
	}
	
	/**
	 * This will check if the number of items on the dashboard table where type = SELECT_DASHBOARD_CURRENCY is less than 15.
	 * Will will only allow 10 items to be on the dashboard only
	 * @return
	 */
	public boolean checkDashboardLessThanFifteen() {
		SQLiteDatabase db = null;
		Cursor c = null;
		boolean lessThanFifteen = false;
				
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
				
			// Execute the query to get the facts for the widget
			c = db.rawQuery("SELECT curr_index FROM dashboard WHERE type = ?", new String[] {SELECT_DASHBOARD_CURRENCY});
				
			// If the count from the query != 0 then set isExist = true
			if(c.getCount() < 15) {
				lessThanFifteen = true;
			}
			
			Log.d(LOG_TAG, "checkDashboardLessThanTen() = c.getCount() = " + c.getCount());
			
			// Close the database
			c.close();
			db.close();
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "checkDashboardLessThanEleven() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
		
		return lessThanFifteen;
	}
	
	// This will check if the currency does not exist where type = SELECT_DASHBOARD_CURRENCY
	public boolean checkAddCurrencyNotExist(String curr_index) {
		SQLiteDatabase db = null;
		Cursor c = null;
		boolean notExist = false;
			
		try {
			// Open the database
			String path = DB_PATH + DB_NAME;
			db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
				
			// Execute the query to get the facts for the widget
			c = db.rawQuery("SELECT curr_index FROM dashboard WHERE type = ? AND curr_index = ?", new String[] {SELECT_DASHBOARD_CURRENCY, curr_index});
			
			// If the count from the query != 0 then set isExist = true
			if(c.getCount() == 0) {
				notExist = true;
			}

			// Close the database
			c.close();
			db.close();
			
		} catch (SQLiteException e) {
			Log.e(LOG_TAG, "checkAddCurrencyExist() - Error in Executing the Query: " + e);
			if(db != null) {
				db.close();
			}
			if(c != null) {
				c.close();
			}
		}
		
		db.close();
		return notExist;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
