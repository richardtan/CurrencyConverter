package com.tckr.currencyconverter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class TheFragmentPagerAdapter extends FragmentPagerAdapter{
 
	final int PAGE_COUNT = 2;
 
	/** Constructor of the class */
	public TheFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}
 
	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int arg0) {
		Bundle data = new Bundle();
		switch(arg0){
			/** Android tab is selected */
			case 0:
				ConverterFragment converterFragment = new ConverterFragment();
				data.putInt("current_page", arg0+1);
				converterFragment.setArguments(data);
				return converterFragment;
 
			/** Apple tab is selected */
			case 1:
				DashboardFragment dashboardFragment = new DashboardFragment();
				data.putInt("current_page", arg0+1);
				dashboardFragment.setArguments(data);
				return dashboardFragment;
		}
		return null;
	}
 
	/** Returns the number of pages */
	@Override
	public int getCount() {
		return PAGE_COUNT;
	}
}