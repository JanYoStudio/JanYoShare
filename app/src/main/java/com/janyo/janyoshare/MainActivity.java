package com.janyo.janyoshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.janyo.janyoshare.util.AppManager;
import com.janyo.janyoshare.util.FileUtil;
import com.janyo.janyoshare.util.Settings;

public class MainActivity extends AppCompatActivity
{
	private View coordinatorLayout;
	private Settings settings;
	private static final int PERMISSION_CODE = 233;
	private long oneClickTime;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		settings = new Settings(MainActivity.this);
		checkPermission();
		initialization();
	}

	private void initialization()
	{
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		TabLayout title_tabs = (TabLayout) findViewById(R.id.title_tabs);
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		coordinatorLayout = findViewById(R.id.coordinatorLayout);

		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.AppType.USER), "User Apps");
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.AppType.SYSTEM), "System Apps");
		viewPager.setAdapter(viewPagerAdapter);
		title_tabs.setupWithViewPager(viewPager);
		title_tabs.setTabMode(TabLayout.MODE_FIXED);

		FileUtil.isDirExist(getString(R.string.app_name));
		if (ContextCompat.checkSelfPermission(MainActivity.this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_GRANTED && settings.isAutoClean())
		{
			Snackbar.make(coordinatorLayout, "文件清除" + (FileUtil.cleanFileDir(getString(R.string.app_name)) ? "成功" : "失败") + "！", Snackbar.LENGTH_SHORT)
					.show();
		}

		setSupportActionBar(toolbar);
	}

	private void checkPermission()
	{
		if (ContextCompat.checkSelfPermission(MainActivity.this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					PERMISSION_CODE);
		}
	}

	@Override
	public void onBackPressed()
	{
		long doubleClickTime = System.currentTimeMillis();
		if (doubleClickTime - oneClickTime > 2000)
		{
			Snackbar.make(coordinatorLayout, "再按一次返回键退出", Snackbar.LENGTH_SHORT)
					.show();
			oneClickTime = doubleClickTime;
		} else
		{
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@SuppressLint("InflateParams")
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_clear:
				Snackbar.make(coordinatorLayout, "文件清除" + (FileUtil.cleanFileDir(getString(R.string.app_name)) ? "成功" : "失败") + "！", Snackbar.LENGTH_SHORT)
						.show();
				break;
			case R.id.action_about:
				new AlertDialog.Builder(MainActivity.this)
						.setTitle(" ")
						.setView(LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_about, null))
						.setPositiveButton("关闭", null)
						.show();
				break;
			case R.id.action_settings:
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
				break;
			case R.id.action_help:
				new AlertDialog.Builder(MainActivity.this)
						.setTitle(" ")
						.setView(LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_help, null))
						.setPositiveButton("确定", null)
						.show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case PERMISSION_CODE:
				if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
				{
					Snackbar.make(coordinatorLayout, "请授予存储权限来拷贝apk文件到SD卡", Snackbar.LENGTH_LONG)
							.setAction("确定", new View.OnClickListener()
							{
								@Override
								public void onClick(View view)
								{
									checkPermission();
								}
							})
							.addCallback(new Snackbar.Callback()
							{
								@Override
								public void onDismissed(Snackbar transientBottomBar, int event)
								{
									if (event != DISMISS_EVENT_ACTION)
									{
										finish();
									}
								}
							})
							.show();
				} else
				{
					if (settings.isAutoClean())
					{
						Snackbar.make(coordinatorLayout, "文件清除" + (FileUtil.cleanFileDir(getString(R.string.app_name)) ? "成功" : "失败") + "！", Snackbar.LENGTH_SHORT)
								.show();
					}
				}
				break;
		}
	}
}
