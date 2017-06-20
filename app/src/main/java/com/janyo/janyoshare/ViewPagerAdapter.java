package com.janyo.janyoshare;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.Fragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragmentList;
	private List<String> titleList;

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
		fragmentList = new ArrayList<>();
		titleList = new ArrayList<>();
	}

	public void addFragment(Fragment fragment, String title) {
		fragmentList.add(fragment);
		titleList.add(title);
	}

	@Override
	public Fragment getItem(int position) {
		return fragmentList.get(position);
	}

	@Override
	public int getCount() {
		return fragmentList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return titleList.get(position);
	}
}