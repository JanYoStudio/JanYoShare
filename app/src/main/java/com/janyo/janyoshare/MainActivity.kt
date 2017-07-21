package com.janyo.janyoshare

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import com.janyo.janyoshare.adapter.ViewPagerAdapter
import com.janyo.janyoshare.util.AppManager
import com.janyo.janyoshare.util.FileUtil
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
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.AppType.USER), "User Apps")
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.AppType.SYSTEM), "System Apps")
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
				list[position].onResume()
			}

			override fun onPageScrollStateChanged(state: Int)
			{
			}
		})

		FileUtil.isDirExist(getString(R.string.app_name))
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && settings!!.isAutoClean)
		{
			Snackbar.make(coordinatorLayout, "文件清除" + (if (FileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败") + "！", Snackbar.LENGTH_SHORT)
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
			Snackbar.make(coordinatorLayout, "再按一次返回键退出", Snackbar.LENGTH_SHORT)
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
				Snackbar.make(coordinatorLayout, "请授予存储权限来拷贝apk文件到SD卡", Snackbar.LENGTH_LONG)
						.setAction("确定") { checkPermission() }
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
					Snackbar.make(coordinatorLayout, "文件清除" + (if (FileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败") + "！", Snackbar.LENGTH_SHORT)
							.show()
				}
			}
		}
	}
}
