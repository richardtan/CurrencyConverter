package com.tckr.currencyconverter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class ConverterAsync extends AsyncTask<String, Integer, String> {

	private Context context;
	private ProgressDialog dialog;
	private TextView tv;
	
	public static String LOG_TAG = "com.tckr.currencyconverter.ConverterAsync";
	
	public ConverterAsync(Context context, TextView tv) {
		this.context = context;
		this.tv = tv;
	}
	
	@Override
	protected void onPreExecute() {
		
		// Set up a dialog that something is executing.
		dialog = ProgressDialog.show(context, "", "Converting...");
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		
		// This listens to anything that is going to be cancelled, including hitting the back key.
		// This will attempt to kill the thread.
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// actually could set running = false; right here, but I'll
				// stick to contract.
				cancel(true);
			}
		});
	}
	
	/**
	 * This method will connect to the Internet and go to Google to find out the conversion.
	 */
	@Override
	protected String doInBackground(String... params) {
		
		try {

			// Execute the URL which has been passed into the Thread
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(params[0]);
			HttpResponse response = client.execute(request);
			
			// Setup an InputStream to handle the response.
			InputStream ips  = response.getEntity().getContent();
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
			 * {"to": "EUR", "rate": 0.73763999999999996, "from": "USD", "v": 0.73763999999999996}
			 */

            // Limit to 6 decimal places
            String returnValue = new DecimalFormat("#.######").format(jObject.getDouble("v"));

			// Build the object to be returned back to the end user.
			//return jObject.getString("lhs") + " equals <b>" + jObject.getString("rhs") + "</b>";
            return params[1] + " " + jObject.getString("from") + " equals <b>" + returnValue + " " + jObject.getString("to") + "</b>";
		   
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return "Error, something went wrong... Sorry!";
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
	protected void onPostExecute(String result) {
		
		// Set the text back to the text view
		tv.setText(Html.fromHtml(result));
		
		// When finished dismiss the Dialog.
		try {
			dialog.dismiss();
		} catch (Exception e) {
			Log.e(LOG_TAG, e + ": Error in closing the dialog, most like due to screen rotation");
		}
	}
}
