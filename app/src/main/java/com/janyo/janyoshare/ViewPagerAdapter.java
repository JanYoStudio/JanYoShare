package com.janyo.janyoshare;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.Fragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> mFragments;
	private List<String> mTitles;

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<>();
		mTitles = new ArrayList<>();
	}

	public void addFragment(Fragment fragment, String title) {
		mFragments.add(fragment);
		mTitles.add(title);
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTitles.get(position);
	}
}