package com.janyo.janyoshare

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.ArrayList
import android.support.v4.app.Fragment

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm)
{

	private val fragmentList: MutableList<Fragment>
	private val titleList: MutableList<String>

	init
	{
		fragmentList = ArrayList<Fragment>()
		titleList = ArrayList<String>()
	}

	fun addFragment(fragment: Fragment, title: String)
	{
		fragmentList.add(fragment)
		titleList.add(title)
	}

	override fun getItem(position: Int): Fragment
	{
		return fragmentList[position]
	}

	override fun getCount(): Int
	{
		return fragmentList.size
	}

	override fun getPageTitle(position: Int): CharSequence
	{
		return titleList[position]
	}
}