package com.janyo.janyoshare.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.janyo.janyoshare.AppFragment
import com.janyo.janyoshare.R
import com.janyo.janyoshare.adapter.ViewPagerAdapter
import com.janyo.janyoshare.util.AppManager
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.CrashHandler.CrashHandler
import com.mystery0.tools.Logs.Logs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File
import com.mystery0.tools.MysteryNetFrameWork.ResponseListener
import com.mystery0.tools.MysteryNetFrameWork.HttpUtil
import com.android.volley.toolbox.Volley
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{
	private val TAG = "MainActivity"
	private lateinit var settings: Settings
	private val PERMISSION_CODE = 233
	private var oneClickTime: Long = 0
	private lateinit var currentFragment: AppFragment
	private lateinit var img_janyo: ImageView

	override fun onCreate(savedInstanceState: Bundle?)
	{
		settings = Settings.getInstance(this)
		if (settings.dayNight)
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		else
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		super.onCreate(savedInstanceState)
		checkPermission()
		initialization()
		monitor()
	}

	private fun initialization()
	{
		setContentView(R.layout.activity_main)

		val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
		drawer_layout.addDrawerListener(toggle)
		toggle.syncState()

		img_janyo = nav_view.getHeaderView(0).findViewById(R.id.imageView)
		nav_view.menu.findItem(R.id.action_night).actionView.findViewById<Switch>(R.id.Switch).isChecked = settings.dayNight
		nav_view.menu.findItem(R.id.action_night).actionView.findViewById<Switch>(R.id.Switch).setOnCheckedChangeListener { _, checked ->
			settings.dayNight = checked
			Snackbar.make(coordinatorLayout, R.string.hint_day_night, Snackbar.LENGTH_SHORT)
					.show()
		}

		nav_view.setNavigationItemSelectedListener(this)

		val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
		val userFragment = AppFragment.newInstance(AppManager.USER)
		val systemFragment = AppFragment.newInstance(AppManager.SYSTEM)
		viewPagerAdapter.addFragment(userFragment, "User Apps")
		viewPagerAdapter.addFragment(systemFragment, "System Apps")
		currentFragment = userFragment
		viewpager.adapter = viewPagerAdapter
		title_tabs.setupWithViewPager(viewpager)

		viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
		{
			override fun onPageScrolled(position: Int, positionOffset: Float,
										positionOffsetPixels: Int)
			{
			}

			override fun onPageSelected(position: Int)
			{
				val fragment = viewPagerAdapter.getItem(position) as AppFragment
				fragment.refreshList()
				currentFragment = fragment
				Logs.i(TAG, "onPageSelected: 当前滚动到" + viewPagerAdapter.getPageTitle(position))
			}

			override fun onPageScrollStateChanged(state: Int)
			{
			}
		})

		JYFileUtil.isDirExist(getString(R.string.app_name))
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && settings.isAutoClean)
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
		CrashHandler.getInstance(this)
				.sendException(object : CrashHandler.CatchExceptionListener
				{
					override fun onException(file: File, appVersionName: String,
											 appVersionCode: Int, AndroidVersion: String,
											 sdk: Int, vendor: String, model: String)
					{
						if (settings.isAutoUploadLog)
						{
							val map = HashMap<String, String>()
							val fileMap = HashMap<String, File>()
							val time: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Calendar.getInstance().time)
							fileMap.put("logFile", file)
							map.put("date", time)
							map.put("appName", getString(R.string.app_name))
							map.put("appVersionName", appVersionName)
							map.put("appVersionCode", appVersionCode.toString())
							map.put("androidVersion", AndroidVersion)
							map.put("sdk", sdk.toString())
							map.put("vendor", vendor)
							map.put("model", model)
							HttpUtil(this@MainActivity)
									.setRequestQueue(Volley.newRequestQueue(applicationContext))
									.setUrl("http://123.206.186.70/php/uploadLog/upload_file.php")
									.setRequestMethod(HttpUtil.RequestMethod.POST)
									.setFileRequest(HttpUtil.FileRequest.UPLOAD)
									.isFileRequest(true)
									.setMap(map)
									.setFileMap(fileMap)
									.setResponseListener(object : ResponseListener
									{
										override fun onResponse(code: Int, message: String?)
										{
											if (code == 0)
											{
												Toast.makeText(applicationContext, R.string.hint_upload_log_done, Toast.LENGTH_SHORT)
														.show()
											}
											else
											{
												Logs.e(TAG, "onResponse: " + message)
											}
										}
									})
									.open()
						}
						else
						{
							val error = Error(appVersionName, appVersionCode, AndroidVersion, sdk, vendor, model)
							val bundle = Bundle()
							bundle.putSerializable("error", error)
							val intent = Intent(this@MainActivity, ErrorActivity::class.java)
							intent.putExtra("error", bundle)
							startActivity(intent)
						}
					}
				})

		setSupportActionBar(toolbar)

		if (settings.isFirst)
		{
			val view_howToUse = LayoutInflater.from(this).inflate(R.layout.dialog_help, NestedScrollView(this), false)
			val textView = view_howToUse.findViewById<TextView>(R.id.autoCleanWarn)
			if (settings.isAutoClean)
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
									settings.isFirst = false
								})
								.show()
					}
					.show()
		}
	}

	private fun monitor()
	{
		img_janyo.setOnClickListener {
			val intent = Intent(Intent.ACTION_VIEW)
			intent.data = Uri.parse(getString(R.string.address_home_page))
			startActivity(intent)
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

	override fun onNavigationItemSelected(item: MenuItem): Boolean
	{
		when (item.itemId)
		{
			R.id.action_file_transfer -> startActivity(Intent(this, FileTransferConfigureActivity::class.java), ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
			R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
			else -> return true
		}
		drawer_layout.closeDrawer(GravityCompat.START)
		return true
	}

	override fun onBackPressed()
	{
		if (drawer_layout.isDrawerOpen(GravityCompat.START))
		{
			drawer_layout.closeDrawer(GravityCompat.START)
		}
		else
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
				System.exit(0)//销毁进程
			}
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
				if (settings.isAutoClean)
				{
					Snackbar.make(coordinatorLayout, String.format(getString(R.string.hint_clear_file), (if (JYFileUtil.cleanFileDir(getString(R.string.app_name))) "成功" else "失败")), Snackbar.LENGTH_SHORT)
							.show()
				}
			}
		}
	}
}
