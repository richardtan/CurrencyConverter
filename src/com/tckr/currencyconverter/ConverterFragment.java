package com.tckr.currencyconverter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
 
import com.actionbarsherlock.app.SherlockFragment;
import com.tckr.currencyconverter.data.CurrencyData;

public class ConverterFragment extends SherlockFragment {
	
	View tempViewToast;
	Spinner fromSpinner;
	Spinner toSpinner;
	EditText convertNumber;
	TextView resultText;
	ConverterAsync tc;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Create the view and inflate it to the currency_fragment.
		View view = inflater.inflate(R.layout.currency_fragment, container, false);
		
		// Get all the currencies that we support for the application
		final CurrencyData[] currencyData = CurrencyData.populateData();
		
		/**
		 * Start of populating Spinner for "convert_from"
		 */
		// find the spinner and then create an ArrayAdaptor with all the currencies to be inserted
		fromSpinner = (Spinner) view.findViewById(R.id.convert_from);
		ArrayAdapter<CurrencyData> fromArrayAdapter = new ArrayAdapter<CurrencyData>(view.getContext(),
				  android.R.layout.simple_spinner_item, currencyData);

		// Specify the layout to use when the list of choices appears
		fromArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Tell the spinner about our adapter
		fromSpinner.setAdapter(fromArrayAdapter);
		
		// Select USD as the base
		fromSpinner.setSelection(84);
		
		// Create a listener whenever something is selected.
		fromSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		/**
		 * Finish of "convert_from"
		 */
		
		/**
		 * Start of populating Spinner for "convert_to"
		 */
		// find the spinner and then create an ArrayAdaptor with all the currencies to be inserted
		toSpinner = (Spinner) view.findViewById(R.id.convert_to);
		ArrayAdapter<CurrencyData> toArrayAdapter = new ArrayAdapter<CurrencyData>(view.getContext(),
				  android.R.layout.simple_spinner_item, currencyData);

		// Specify the layout to use when the list of choices appears
		toArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Tell the spinner about our adapter
		toSpinner.setAdapter(toArrayAdapter);
		
		// Select EUR as the base
		toSpinner.setSelection(22);
		
		// Create a listener whenever something is selected.
		toSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		/**
		 * Finish of "convert_to"
		 */
		
		/**
		 * The following is assigned to the flip button. This will flip the currency that is currently selected around.
		 */
		Button flipButton = (Button) view.findViewById(R.id.flip_button);
		flipButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				int iTo = toSpinner.getSelectedItemPosition();
				int iFrom = fromSpinner.getSelectedItemPosition();
				toSpinner.setSelection(iFrom);
				fromSpinner.setSelection(iTo);
			}
		});
		
		// Set the convertNumber and resultText to the class so it can be used by the button class below.
		convertNumber = (EditText) view.findViewById(R.id.convert_number);
		resultText = (TextView) view.findViewById(R.id.result_textview);
		
		/**
		 * Below will convert the values by the end user using Google's API.
		 */
		Button convertButton = (Button) view.findViewById(R.id.convert_button);
		convertButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				// Check Internet Connection
				if(!ConverterFragment.isNetworkAvailable(v)) {
					Toast.makeText(v.getContext(), getString(R.string.toast4), Toast.LENGTH_SHORT).show();
					return;
				}
				
				// Get the number to convert.
				String numberToConvert = convertNumber.getText().toString();
				
				try {
					// Test if the number is numeric. If it is not numeric than error and show Toast
					Double.parseDouble(numberToConvert);
					
					// Get the currency from the end user
					int iTo = toSpinner.getSelectedItemPosition();
					int iFrom = fromSpinner.getSelectedItemPosition();
					String sTo = currencyData[iTo].getCurrency();
					String sFrom = currencyData[iFrom].getCurrency();
					
					// Build the URL to be executed/
					//String builder = "http://www.google.com/ig/calculator?hl=en&q=" + numberToConvert + sFrom + "=?" + sTo; //OLD CODE!
                    //String builder = "http://rate-exchange.appspot.com/currency?from=" + sFrom + "&to=" + sTo + "&q=" + numberToConvert;
                    String builder = "http://just-experiment.appspot.com/currency?from=" + sFrom + "&to=" + sTo + "&q=" + numberToConvert;

					// Execute the result and pass the display to TheConverter on the resultText element.
					tc = new ConverterAsync(v.getContext(), resultText);
					tc.execute(new String[] {builder, numberToConvert});
					
					// To handle connection timeout after 30 seconds
					// Store the view so we can display a toast
					tempViewToast = v;
					
					// Create a handler and the a postDelay which will do something after a period of time
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						
						/**
						 * If you are running more than 30 seconds then timeout
						 */
						@Override
						public void run() {
							if (tc.getStatus() == AsyncTask.Status.RUNNING ) {
								// Cancel the thread and remove the dialog.
								tc.cancel(true);
								tc.onCancelled();
								
								// Toast. Yummy
								Toast.makeText(tempViewToast.getContext(), getString(R.string.toast2), Toast.LENGTH_SHORT).show();
								tempViewToast = null;
							}
						}
					}, 30000);
				   
				} catch (NumberFormatException nfe){
					Toast.makeText(v.getContext(), numberToConvert + " " + getString(R.string.toast5), Toast.LENGTH_SHORT).show();
				} catch (Throwable e) {
					e.printStackTrace();
				}				
			}
		});
		
		return view;
	}
	
	// Find out if there is Internet
	public static boolean isNetworkAvailable(View view) {
		ConnectivityManager connectivityManager 
			  = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
}