package com.tckr.currencyconverter;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tckr.currencyconverter.data.CurrencyData;
import com.tckr.currencyconverter.data.DatabaseHelper;
import com.tckr.currencyconverter.view.DraggableGridView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	
	private static final int ADD_TO_DASHBOARD_DIALOG = 10;
	private static final int REMOVE_FROM_DASHBOARD_DIALOG = 11;
	private static final int ABOUT_DIALOG = 12;
	
	public static String LOG_TAG = "com.tckr.currencyconverter.MainActivity";
	
	// Get all the currencies that we support for the application
	private static final CurrencyData[] CURRENCY_DATA = CurrencyData.populateData();
	
	ActionBar mActionBar;
	ViewPager mPager;
	Spinner addToDashboard;
	Integer[] dashCurrency;
	ListView removeToDashboard;
	Dialog aboutDialog;
	Dialog addDialog;
	Dialog removeDialog;
	UpdateDashboardAsync uda;
	Context context;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		/*
		 * findViewById(R.id.currencyFragment and only be found if you are in layout-sw600dp mode, therefore
		 * If you are null, then you are in phone mode and we need to populate the page view.
		 */
		if(findViewById(R.id.currencyFragment) == null) {
		
			// Getting a reference to action bar of this activity
			mActionBar = getSupportActionBar();
 
			// Set tab navigation mode
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
			// Getting a reference to ViewPager from the layout
			mPager = (ViewPager) findViewById(R.id.pager);
 
			// Getting a reference to FragmentManager
			FragmentManager fm = getSupportFragmentManager();
 
			// Defining a listener for pageChange
			ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
				@Override
				public void onPageSelected(int position) {
					super.onPageSelected(position);
					mActionBar.setSelectedNavigationItem(position);
				}
			};
 
			// Setting the pageChange listner to the viewPager
			mPager.setOnPageChangeListener(pageChangeListener);
 
			// Creating an instance of FragmentPagerAdapter
			TheFragmentPagerAdapter fragmentPagerAdapter = new TheFragmentPagerAdapter(fm);
 
			// Setting the FragmentPagerAdapter object to the viewPager object
			mPager.setAdapter(fragmentPagerAdapter);
 
			mActionBar.setDisplayShowTitleEnabled(true);
 
			// Defining tab listener
			ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 
				@Override
				public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}
 
				@Override
				public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
					mPager.setCurrentItem(tab.getPosition());
				}
 
				@Override
				public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
			};
 
			// Creating Android Tab
			ActionBar.Tab tab = mActionBar.newTab()
					.setText("Converter")
					.setTabListener(tabListener);
 
			mActionBar.addTab(tab);
 
			// Creating Apple Tab
			tab = mActionBar.newTab()
					.setText("Dashboard")
					.setTabListener(tabListener);
 
			mActionBar.addTab(tab);
		}
	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/**
	 * This will handle when user clicks on the menu item.
	 * (non-Javadoc)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Create the object for the database
		DatabaseHelper dh = new DatabaseHelper(this);
		
		if (item.getItemId() == R.id.menu_refresh) {
			
			// Check Internet Connection
			if(!isNetworkAvailable(this)) {
				Toast.makeText(this, R.string.toast6, Toast.LENGTH_SHORT).show();
				return true;
			}
			
			// Get the currency that we need to convert to
			Integer[] dashCurr = dh.getDashboardCurrency();
			
			// Check if there are any currencies to update. If nothing then show message to user and exit.
			if(dashCurr.length == 0 || dashCurr == null) {
				Toast.makeText(this, R.string.toast7, Toast.LENGTH_SHORT).show();
				return true;
			}
			
			// Get the Dashboard Fragment so we can do something in the AsyncTask
			DraggableGridView dgv = (DraggableGridView) findViewById(R.id.vgv);
			TextView tv = (TextView) findViewById(R.id.no_currency_dashboard);
			
			// Call UpdateDashboardAsync and try to update the dashboard.
			uda = new UpdateDashboardAsync(this, tv, dgv);
			uda.execute();
			
			// To handle connection timeout after 45 seconds
			// Store the view so we can display a toast
			context = this;
			
			// Create a handler and the a postDelay which will do something after a period of time
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				
				/**
				 * If you are running more than 30 seconds then timeout
				 */
				@Override
				public void run() {
					if (uda.getStatus() == AsyncTask.Status.RUNNING ) {
						// Cancel the thread and remove the dialog.
						uda.cancel(true);
						uda.onCancelled();

						// Toast. Yummy
						Toast.makeText(context, getString(R.string.toast2), Toast.LENGTH_SHORT).show();
						context = null;
					}
				}
			}, 45000);
			
			return true;
			
		} else if (item.getItemId() == R.id.menu_add) {
			
			// Check to make sure that we not not have more than 15 currencies on the dashboard.
			if(!dh.checkDashboardLessThanFifteen()) {
				Toast.makeText(this, R.string.toast1, Toast.LENGTH_SHORT).show();
				return true;
			} else {
				showDialog(ADD_TO_DASHBOARD_DIALOG);
			}
			
			return true;
			
		} else if (item.getItemId() == R.id.menu_remove) {
			
			// Make sure dashCurrency is empty just in case we get some inconsistencies.
			dashCurrency = null;
			
			// Get the currencies that is stored on the database and assign it
			dashCurrency = dh.getDashboardCurrency();
			
			if(dashCurrency.length == 0) {
				Toast.makeText(this, R.string.toast3, Toast.LENGTH_SHORT).show();
			} else {
				showDialog(REMOVE_FROM_DASHBOARD_DIALOG);
			}
			
			return true;
			
		} else if (item.getItemId() == R.id.menu_about) {
			
			try {
				dismissDialog(ABOUT_DIALOG);
			} catch (IllegalArgumentException e) {
				Log.e(LOG_TAG, "Cannot Dismiss Dialog - Menu_About: " + e);
			}
			
			showDialog(ABOUT_DIALOG);
			
			return true;
			
		}
		return true;
	}
	
	/*
	 * This method will get called before the dialog is presented to the end user. We need to override this because our dialog is dynamic and
	 * we will need to make sure it has the up to date values it needs.
	 * (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog (int id, Dialog dialog) {
		switch (id) {
			
			// Do nothing for this
			case ABOUT_DIALOG:
				return;
				
			// Do nothing for this
			case ADD_TO_DASHBOARD_DIALOG:
				return;
			
			// To populate the latest items from the database
			case REMOVE_FROM_DASHBOARD_DIALOG:
				
				// Make sure that dashCurrency has values. Usually happens when screen is rotated.
				if(dashCurrency == null) {
					DatabaseHelper dh = new DatabaseHelper(this);
					dashCurrency = dh.getDashboardCurrency();
				}
				
				// Get the list of currencies and populate it.
				String[] currToPopulate = new String[dashCurrency.length];
				for(int i = 0; i < dashCurrency.length; i++) {
					currToPopulate[i] = CURRENCY_DATA[dashCurrency[i].intValue()].getCurrencyDisplay();
				}
				
				/** Creating array adapter to set data in listview */
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(removeDialog.getContext(), 
						android.R.layout.simple_list_item_multiple_choice, currToPopulate);
				
				/** Setting the array adapter to the listview */
				removeToDashboard.setAdapter(adapter);
				
				return;
		}
	}
	
	/**
	 * 
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		
			case ABOUT_DIALOG:

				aboutDialog = new Dialog(this);
				aboutDialog.setContentView(R.layout.about_dialog);
				aboutDialog.setTitle(R.string.about);
				TextView tv = (TextView) aboutDialog.findViewById(R.id.aboutTextView);
				tv.setText(Html.fromHtml("<b>Currency Converter</b><br /><br /><b>Version 1.1.5</b><br /><br />This application is a simple currency converter. The exchange rates comes live from data provided by Google Finance. The device needs to be connected to the Internet in order to work.<br /><br /><b>Permissions</b><br /><br />The application requires the following permission<br /><br /><u>Full Network Access</u><br />In order to provide the latest exchange rates, the application needs to connect to the Internet.<br /><br /><u>View Network Connection</u><br />To allow the application to show a message that the device is not connected to the Internet.<br /><br /><b>Contact</b><br /><a href=\"mailto:currencyconverter@justexperiment.com\">Email Us</a><br /><a href=\"http://justexperiment.com/currencyconverter.php\">Visit our Webpage</a><br /><br /><b>Buy us a Beer!<br /></b><a href=\"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=52MBSJUUWA562\">Donate via PayPal</a><br /><br /><b>Github</b><br /><a href=\"https://github.com/richardtan/CurrencyConverter/\">Find/Fork the code at Github</a>"));
				tv.setMovementMethod(LinkMovementMethod.getInstance());
				return aboutDialog;
		
			case ADD_TO_DASHBOARD_DIALOG:
				
				addDialog = new Dialog(this);
				addDialog.setContentView(R.layout.add_dashboard_dialog);
				addDialog.setTitle(R.string.add_dashboard);

				// find the spinner and then create an ArrayAdaptor with all the currencies to be inserted
				addToDashboard = (Spinner) addDialog.findViewById(R.id.add_dashboard_spinner);
				ArrayAdapter<CurrencyData> arrayAdapter = new ArrayAdapter<CurrencyData>(addDialog.getContext(),
						  android.R.layout.simple_spinner_item, CURRENCY_DATA);
				
				// Specify the layout to use when the list of choices appears
				arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				
				// Tell the spinner about our adapter
				addToDashboard.setAdapter(arrayAdapter);
				
				// Define the action for the Cancel Button
				Button buttonCancel = (Button) addDialog.findViewById(R.id.button_cancel_dashboard);
				buttonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						addDialog.dismiss();
					}
				});
				
				// Define the action for the Add Button
				Button buttonAdd = (Button) addDialog.findViewById(R.id.button_add_dashboard);
				buttonAdd.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						// Create database object to query the internal storage
						DatabaseHelper dh = new DatabaseHelper(v.getContext());
					
						/*
					 	 * The follow will try to add the currency position to the database. First it will check if the item exists in the database. If it
					 	 * does then it will not allow to add. If it does not exist then add it to the dashboard.
					 	 */
						if(dh.checkAddCurrencyNotExist(addToDashboard.getSelectedItemPosition() + "")) {
							
							DraggableGridView dgv = (DraggableGridView) findViewById(R.id.vgv);
							TextView tv = (TextView) findViewById(R.id.no_currency_dashboard);
							dh.addNewCurrToDashboard(addToDashboard.getSelectedItemPosition() + "", tv, dgv, v.getContext());
							Toast.makeText(v.getContext(), addToDashboard.getSelectedItem().toString() + " " + getString(R.string.toast8), Toast.LENGTH_SHORT).show();
							addDialog.dismiss();
						} else {
							Toast.makeText(v.getContext(), addToDashboard.getSelectedItem().toString() + " " + getString(R.string.toast9), Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				return addDialog;
				
			case REMOVE_FROM_DASHBOARD_DIALOG:
				
				// Initialise the dialog
				removeDialog = new Dialog(this);
				removeDialog.setContentView(R.layout.remove_dashboard_dialog);
				removeDialog.setTitle(R.string.remove_dashboard);
				
				// Make the ListView known
				removeToDashboard = (ListView) removeDialog.findViewById(R.id.remove_dashboard_list);
		 
				// Enable mutli selection on the list view
				removeToDashboard.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				
				/* ** IMPORTANT **
				 * Refer to onPrepareDialog method where it will populate the ListView with items from an Adaptor.
				 */
				
				// Define the action for the Cancel Button
				Button buttonCancel2 = (Button) removeDialog.findViewById(R.id.button_cancel2_dashboard);
				buttonCancel2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						removeDialog.dismiss();
					}
				});
				
				// Define the action for the Cancel Button
				Button buttonRemove = (Button) removeDialog.findViewById(R.id.button_remove_dashboard);
				buttonRemove.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						/*
						 * To revisit in the future. The getChecekdItemIds() is the right way to do this, however my adaptor returns false when hasStableIds() is called.
						 * Reason I believe is that when I created the ListView I am not implementing the proper methods was I am doing it through a hack
						 * on the Dialog class.
						 */
						//long[] a = removeToDashboard.getCheckedItemIds();						
						
						
						// Get the items that are to be removed and stick them in an array
						ArrayList<Integer> currToRemoveList = new ArrayList<Integer>();
						for(int i = 0; i < removeToDashboard.getCount(); i++) {
							
							// If the item is checked, we have to make sure the system is aware of this
							if(removeToDashboard.isItemChecked(i)) {
								
								/*
								 *  Due to the stupid way that I have coded this, we need to iterate through the array to find out
								 *  what is the index of the currency that we need to remove. Once we find that we add the item
								 *  to the array that we need to use to remove the currency from the dashboard.
								 */
								for(CurrencyData c: CURRENCY_DATA) {
									if(c.getCurrencyDisplay().equals(removeToDashboard.getItemAtPosition(i))) {
										currToRemoveList.add(c.getCurrencyIndex());
									}
								}
							}
						}
						
						// Check to see if the end user has selected anything
						if(currToRemoveList.size() == 0) {
							Toast.makeText(v.getContext(), getString(R.string.toast10), Toast.LENGTH_SHORT).show();
						} else {
							
							// Create the object for the database
							DatabaseHelper dh = new DatabaseHelper(v.getContext());
							
							DraggableGridView dgv = (DraggableGridView) findViewById(R.id.vgv);
							
							// Get the items and then start removing them from the database
							for(Integer i: currToRemoveList) {
								// Remove from the database
								dh.removeCurrFromDashboard(i.intValue() + "", dgv);
							}
							
							// Show some Toast and remove the dialog.
							Toast.makeText(v.getContext(), getString(R.string.toast11), Toast.LENGTH_SHORT).show();
							removeDialog.dismiss();
						}
					}
				});
	
				return removeDialog;
		}		
		return null;
	}
	
	// Find out if there is Internet
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
}