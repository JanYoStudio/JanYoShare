package com.janyo.janyoshare.activity

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.janyo.janyoshare.AppFragment
import com.janyo.janyoshare.R
import com.janyo.janyoshare.adapter.ViewPagerAdapter
import com.janyo.janyoshare.util.AppManager
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.CrashHandler.CrashHandler
import com.mystery0.tools.Logs.Logs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity()
{
	private val TAG = "MainActivity"
	private var settings: Settings? = null
	private val PERMISSION_CODE = 233
	private var oneClickTime: Long = 0

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		settings = Settings(this)
		checkPermission()
		initialization()
	}

	private fun initialization()
	{
		setContentView(R.layout.activity_main)

		val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.USER), "User Apps")
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.SYSTEM), "System Apps")
		viewpager.adapter = viewPagerAdapter
		title_tabs.setupWithViewPager(viewpager)
		title_tabs.tabMode = TabLayout.MODE_FIXED

		viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
		{
			override fun onPageScrolled(position: Int, positionOffset: Float,
										positionOffsetPixels: Int)
			{
			}

			override fun onPageSelected(position: Int)
			{
				val list = supportFragmentManager.fragments
				(list[position] as AppFragment).refreshList()
			}

			override fun onPageScrollStateChanged(state: Int)
			{
			}
		})

		JYFileUtil.isDirExist(getString(R.string.app_name))
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && settings!!.isAutoClean)
		{
			Snackbar.make(coordinatorLayout, String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
					.show()
		}
		CrashHandler.getInstance(this)
				.clean(object : CrashHandler.AutoCleanListener
				{
					override fun done()
					{
						Logs.i(TAG, "done: clean crash log")
					}

					override fun error(message: String?)
					{
						Logs.i(TAG, "error: " + message)
					}
				})

		setSupportActionBar(toolbar)

		if (settings!!.isFirst)
		{
			val view_howToUse = LayoutInflater.from(this).inflate(R.layout.dialog_help, NestedScrollView(this), false)
			val textView = view_howToUse.findViewById<TextView>(R.id.autoCleanWarn)
			if (settings!!.isAutoClean)
			{
				textView.visibility = View.VISIBLE
			}
			AlertDialog.Builder(this)
					.setTitle(" ")
					.setView(view_howToUse)
					.setPositiveButton(R.string.action_done, null)
					.setOnDismissListener {
						val view_license = LayoutInflater.from(this).inflate(R.layout.dialog_license, NestedScrollView(this), false)
						val text_license_point1 = view_license.findViewById<TextView>(R.id.license_point1)
						val text_license_point2 = view_license.findViewById<TextView>(R.id.license_point2)
						val text_license_point3 = view_license.findViewById<TextView>(R.id.license_point3)
						val point = VectorDrawableCompat.create(resources, R.drawable.ic_point, null)
						point!!.setBounds(0, 0, point.minimumWidth, point.minimumHeight)
						text_license_point1.setCompoundDrawables(point, null, null, null)
						text_license_point2.setCompoundDrawables(point, null, null, null)
						text_license_point3.setCompoundDrawables(point, null, null, null)
						AlertDialog.Builder(this)
								.setTitle(" ")
								.setView(view_license)
								.setPositiveButton(R.string.action_done, { _, _ ->
									settings!!.isFirst = false
								})
								.show()
					}
					.show()
		}
	}

	private fun checkPermission()
	{
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,
					arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
					PERMISSION_CODE)
		}
	}

	override fun onBackPressed()
	{
		val doubleClickTime = System.currentTimeMillis()
		if (doubleClickTime - oneClickTime > 2000)
		{
			Snackbar.make(coordinatorLayout, R.string.hint_twice_exit, Snackbar.LENGTH_SHORT)
					.show()
			oneClickTime = doubleClickTime
		}
		else
		{
			finish()
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
											grantResults: IntArray)
	{
		when (requestCode)
		{
			PERMISSION_CODE -> if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
			{
				Snackbar.make(coordinatorLayout, R.string.hint_permission, Snackbar.LENGTH_LONG)
						.setAction(R.string.action_done) { checkPermission() }
						.addCallback(object : Snackbar.Callback()
						{
							override fun onDismissed(transientBottomBar: Snackbar?, event: Int)
							{
								if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
								{
									finish()
								}
							}
						})
						.show()
			}
			else
			{
				if (settings!!.isAutoClean)
				{
					Snackbar.make(coordinatorLayout, String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
							.show()
				}
			}
		}
	}
}
