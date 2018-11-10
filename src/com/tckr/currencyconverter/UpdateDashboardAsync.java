package com.tckr.currencyconverter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.tckr.currencyconverter.data.CurrencyData;
import com.tckr.currencyconverter.data.DatabaseHelper;
import com.tckr.currencyconverter.view.DraggableGridView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class UpdateDashboardAsync extends AsyncTask<String, Integer, String[][]> {

	private TextView tv;
	private DraggableGridView dgv;
	private Context context;
	private ProgressDialog dialog;
	
	public static String LOG_TAG = "com.tckr.currencyconverter.UpdateDashboardAsync";
	public static final CurrencyData[] CURRENCYDATA = CurrencyData.populateData();
	
	public UpdateDashboardAsync(Context context, TextView tv, DraggableGridView dgv) {
		this.context = context;
		this.tv = tv;
		this.dgv = dgv;
	}
	
	
	@Override
	protected void onPreExecute() {
		
		// Set up a dialog that something is executing.
		dialog = ProgressDialog.show(context, "", "Updating Dashboard");
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		
		/* 
		 * This listens to anything that is going to be cancelled, including hitting the back key.
		 * This will attempt to kill the thread.
		 */
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				cancel(true);
			}
		});
	}
	
	/**
	 * This method will connect to the Internet and go to Google to find out the conversion.
	 */
	@Override
	protected String[][] doInBackground(String... params) {
		
		try {
			
			// Create a DatabaseHelper object
			DatabaseHelper dh = new DatabaseHelper(context);
			
			// Get the base index from the database
			int baseCurr = dh.getBaseCurrency();
			
			// Get the currency that we need to convert to
			Integer[] dashCurr = dh.getDashboardCurrency();
			
			// Create an Array to store the result into
			String[][] dashboardValue = new String[dashCurr.length][2];
			
			// Iterate through currency we need to convert.
			for(int i = 0; i < dashCurr.length; i++) {
				
				// Build the url that is to be executed to Google // OLD CODE
				//String urlExecute = "http://www.google.com/ig/calculator?hl=en&q=" + CURRENCYDATA[baseCurr].getBaseIndex() +
				//		CURRENCYDATA[baseCurr].getCurrency() + "=?" + CURRENCYDATA[dashCurr[i].intValue()].getCurrency();

                //String urlExecute = "http://rate-exchange.appspot.com/currency?from=" + CURRENCYDATA[baseCurr].getCurrency() + "&to=" +
                //        CURRENCYDATA[dashCurr[i].intValue()].getCurrency() + "&q=" + CURRENCYDATA[baseCurr].getBaseIndex();

                String urlExecute = "https://just-experiment.appspot.com/currency?from=" + CURRENCYDATA[baseCurr].getCurrency() + "&to=" +
                        CURRENCYDATA[dashCurr[i].intValue()].getCurrency() + "&q=" + CURRENCYDATA[baseCurr].getBaseIndex();

				Log.d(LOG_TAG, urlExecute);
				
				// Execute the URL which has been passed into the Thread
				URL url = new URL(urlExecute);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

				try {

					// Setup an InputStream to handle the response.
					InputStream ips = new BufferedInputStream(urlConnection.getInputStream());
					BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"iso-8859-1"), 8);

					// Use a reader to read the data and store it in a String builder
					StringBuilder sb = new StringBuilder();
					String s;
					while(true)
					{
						s = buf.readLine();
						if(s==null || s.length()==0)
							break;
						sb.append(s);
					}
					buf.close();
					ips.close();

					// Pass the end result into a JSONObject
					JSONObject jObject = new JSONObject(sb.toString());

					/*
					 * NOTE
					 * JSON will return in the below format and will be one object deep:
					 * {lhs: "1 U.S. dollar",rhs: "0.756315232 Euros",error: "",icc: true}
					 * {"to": "EUR", "rate": 0.73763999999999996, "from": "USD", "v": 0.73763999999999996}
					 */

					// Limit to 6 decimal places
					String returnValue = new DecimalFormat("#.######").format(jObject.getDouble("v"));

					dashboardValue[i][0] = "" + dashCurr[i].intValue();
					dashboardValue[i][1] = returnValue + " " +
							CURRENCYDATA[dashCurr[i].intValue()].getCurrencyDisplay().substring(0, CURRENCYDATA[dashCurr[i].intValue()].getCurrencyDisplay().length() - 6);

				} finally {
					urlConnection.disconnect();
				}
			}
			
			return dashboardValue;
			
		} catch (Exception e1) {
			Log.e(LOG_TAG, e1 + ": ERROR in UPDATE DASHBOARD!!!");
			e1.printStackTrace();
		}

		return null;
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		
		// When finished dismiss the Dialog.
		try {
			dialog.dismiss();
		} catch (Exception e) {
			Log.e(LOG_TAG, e + ": Error in closing the dialog, most like due to screen rotation");
		}
	}
	
	@Override
	protected void onPostExecute(String result[][]) {
		
		// Only do something if the result is not null
		if(result != null) {
			
			// Create a DatabaseHelper object
			DatabaseHelper dh = new DatabaseHelper(context);
			
			Log.d(LOG_TAG, "LENGTH" + result.length);
			
			for(int i = 0; i < result.length; i++) {
				
				Log.d(LOG_TAG, result[i][0] + " : " + result[i][1]);
				dh.setDashboardValue(result[i][0], result[i][1]);
				
			}
			
			// Remove everything from the grid view and stop when there is nothing let to remove
			boolean removeViewError = true;
			while(removeViewError) {
				try {
					// Remove the current item from the dashboard. We will rebuild this later
					dgv.removeViewAt(0);
				} catch (Exception e) {
					removeViewError = false;
				}
			}
			
			/*
			 * BEWARE
			 * If the below is changed then you have to make sure it is sync to DashboardFragment.rebuildGrid()
			 */
			
			// Get the base currency for the view
			String base = DashboardFragment.getBase(context);
			
			// Get the values that we need to populate on the view
			String[] dashValues = dh.getDashboardResults();
			
			Log.d(LOG_TAG, "HOW MANY HAVE WE TO POPULATE? " + dashValues.length);
			
			// Populate the grid view
			DashboardFragment.addToDashboard(dashValues, base, context, tv, dgv);
			
		}
		
		// When finished dismiss the Dialog.
		try {
			dialog.dismiss();
		} catch (Exception e) {
			Log.e(LOG_TAG, e + ": Error in closing the dialog, most like due to screen rotation");
		}
	}
}
