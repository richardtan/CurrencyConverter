package com.tckr.currencyconverter;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
 
import com.actionbarsherlock.app.SherlockFragment;

import com.tckr.currencyconverter.data.CurrencyData;
import com.tckr.currencyconverter.data.DatabaseHelper;
import com.tckr.currencyconverter.view.DraggableGridView;
import com.tckr.currencyconverter.view.OnRearrangeListener;
 
public class DashboardFragment extends SherlockFragment{

	private DraggableGridView dgv;
	private TextView tv;
	private View view;
	private Spinner baseCurrency;
	private final static CurrencyData[] CURRENCYDATA = CurrencyData.populateData();
	public static String LOG_TAG = "com.tckr.currencyconverter.DashboardFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Create the view and inflate it to the currency_fragment.
		view = inflater.inflate(R.layout.dashboard_fragment, container, false);
		
		// Checks to make sure the database exists. If not then create it.
		DatabaseHelper dh = new DatabaseHelper(view.getContext());
		try {
			dh.createDataBase();
		} catch(IOException e) {
			throw new Error("Error trying to create database" + e);
		}
		
		/**
		 * Start of populating Spinner for "base_currency_spinner"
		 */
		// find the spinner and then create an ArrayAdaptor with all the currencies to be inserted
		baseCurrency = (Spinner) view.findViewById(R.id.base_currency_spinner);
		ArrayAdapter<CurrencyData> fromArrayAdapter = new ArrayAdapter<CurrencyData>(view.getContext(),
				  android.R.layout.simple_spinner_item, CURRENCYDATA);

		// Specify the layout to use when the list of choices appears
		fromArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Tell the spinner about our adapter
		baseCurrency.setAdapter(fromArrayAdapter);
		
		int curr_index = dh.getBaseCurrency();
		
		// Select USD as the base
		baseCurrency.setSelection(curr_index);
		
		// Create a listener whenever something is selected.
		baseCurrency.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				// Get the position for the baseCurrency from the spinner and saves it to the database.
				DatabaseHelper dh = new DatabaseHelper(view.getContext());
				dh.setBaseCurrency(baseCurrency.getSelectedItemPosition() + "", CURRENCYDATA[baseCurrency.getSelectedItemPosition()].getBaseIndex() + "");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		/**
		 * Finish of "base_currency_spinner"
		 */
		
		// Draw the dashboard to the view
		rebuildGrid(view);

		return view;
	}
	
	protected void removeFromView(int count) {}
	
	/**
	 * BEWARE
	 * If the below is changed then you have to make sure it is sync to UpdateDashboardAsync.getBase() and UpdateDashboardAsync.onPostExecute() 
	 * This method will build the view based on the Database values
	 * @param v
	 */
	private void rebuildGrid(View v) {
		
		// Get the grid view to populate.
		dgv = ((DraggableGridView)view.findViewById(R.id.vgv));
		tv = ((TextView)view.findViewById(R.id.no_currency_dashboard));
		
		// Get the database object so we can do some querying.
		DatabaseHelper dh = new DatabaseHelper(view.getContext());
		
		// This text will be use to display what we are trying to convert from. This will get the base index and the name from
		// the database, so it will have a format for example: "1 US Dollar equals"
		String base = getBase(view.getContext());
		
		// Get the values that we need to populate on the view
		String[] dashValues = dh.getDashboardResults();
		
		// Populate the grid view
		addToDashboard(dashValues, base, view.getContext(), tv, dgv);
		
		/*
		 *  Set a listener so when the user moves and arrange the dashboard items around, the position gets
		 *  stored on the database
		 */
		dgv.setOnRearrangeListener(new OnRearrangeListener() {
			public void onRearrange(int oldIndex, int newIndex) {
				
				// Get the database object so we can do some querying.
				DatabaseHelper dh = new DatabaseHelper(view.getContext());
				
				// Increment the oldIndex and newIndex by one, as that is how it is stored in the database
				oldIndex++;
				newIndex++;
				
				if(oldIndex < newIndex) {
					
					// Assign -99 ordering to the item that has been moved
					dh.updateDashboardPositionCurrency(oldIndex + "", "-99");
					Log.d(LOG_TAG, "dh.updateDashboardPositionCurrency(" + oldIndex + ", -99);");
					
					// Move all the other currency on the dashboard by 1 to their new position.
					for(int i = oldIndex + 1; i <= newIndex; i++) {
						dh.updateDashboardPositionCurrency(i + "", (i - 1) + "");
						Log.d(LOG_TAG, "dh.updateDashboardPositionCurrency(" + i + ", " + (i - 1) + ")");
					}
					
					// Update the new position of the dashboard that has been updated via the user
					dh.updateDashboardPositionCurrency("-99" + "", newIndex + "");
					Log.d(LOG_TAG, "dh.updateDashboardPositionCurrency(-99, " + newIndex +");");
					
				} else if(newIndex < oldIndex) {
					
					// Assign -99 ordering to the item that has been moved
					dh.updateDashboardPositionCurrency(oldIndex + "", "-99");
					Log.d(LOG_TAG, "dh.updateDashboardPositionCurrency(" + oldIndex + ", -99);");
					
					// Move all the other currency on the dashboard by 1 to their new position.
					for(int i = oldIndex - 1; i >= newIndex; i--) {
						dh.updateDashboardPositionCurrency(i + "", (i + 1) + "");
						Log.d(LOG_TAG, "dh.updateDashboardPositionCurrency(" + i + ", " + (i + 1) + ")");
					}
					
					// Update the new position of the dashboard that has been updated via the user
					dh.updateDashboardPositionCurrency("-99" + "", newIndex + "");
					Log.d(LOG_TAG, "dh.updateDashboardPositionCurrency(-99, " + newIndex +");");
				}
			}
		});
		
	}
	
	public static Bitmap getThumb(String base, String conversion)
	{
		// Create the Bitmap. Using 1000 to compensate for screen resolutions
		Bitmap bmp = Bitmap.createBitmap(1000, 500, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		TextPaint paint = new TextPaint();
		
		// Set the colour of the rectangle to light grey and then apply it
		paint.setColor(Color.rgb(229, 229, 229));
		canvas.drawRect(new Rect(0, 0, 1000, 500), paint);
		
		// Set some specification for the text. Make it dark grey
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setColor(Color.rgb(112, 112, 112));
		
		// This is for the base currency text
		paint.setTextSize(50);
		StaticLayout mTextLayout = new StaticLayout(base, paint, canvas.getWidth() - 100, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		canvas.translate(50, 50);
		mTextLayout.draw(canvas);
		
		// This is for the conversion currency text.
		paint.setTextSize(100);
		mTextLayout = new StaticLayout(conversion, paint, canvas.getWidth() - 100, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		canvas.translate(0, 70);
		mTextLayout.draw(canvas);

		return bmp;
	}
	
	/**
	 * This will get the base text for the dashboard
	 * @param context
	 * @return
	 */
	public static String getBase(Context context) {
		// Get the database object so we can do some querying.
		DatabaseHelper dh = new DatabaseHelper(context);
						
		// Get the base index from the database
		int curr_index = dh.getBaseCurrency();
						
		// This text will be use to display what we are trying to convert from. This will get the base index and the name from
		// the database, so it will have a format for example: "1 US Dollar equals"
		return CURRENCYDATA[curr_index].getBaseIndex() + " " + 
				CURRENCYDATA[curr_index].getCurrencyDisplay().substring(0, CURRENCYDATA[curr_index].getCurrencyDisplay().length() - 6) + 
				" equals";
	}
	
	/**
	 * This will iterate through and will draw the items to the dashboard.
	 * @param dashValues
	 * @param base
	 * @param context
	 * @param tv
	 * @param dgv
	 */
	public static void addToDashboard(String[] dashValues, String base, Context context, TextView tv, DraggableGridView dgv) {

		// If there are no currencies being populated, then show the message, else show nothing.
		if (dashValues.length > 0) {
			tv.setText("");
		} else {
			tv.setText(R.string.no_currency_dashboard);
		}
		
		// Iterate through and try to draw the values to the dashboard.
		for(int i = 0; i < dashValues.length; i++) {
			
			// Form the image and add it to the grid view.
			ImageView iv = new ImageView(context);
			iv.setImageBitmap(getThumb(base, dashValues[i]));
			dgv.addView(iv);
		}
	}
}