package com.janyo.janyoshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.janyo.janyoshare.util.AppManager;
import com.janyo.janyoshare.util.FileUtil;

public class MainActivity extends AppCompatActivity
{
	private static final int PERMISSION_CODE = 233;
	private long oneClickTime;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		checkPermission();
		initialization();
	}

	private void initialization()
	{
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		TabLayout title_tabs = (TabLayout) findViewById(R.id.title_tabs);
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

		ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.AppType.USER), "User Apps");
		viewPagerAdapter.addFragment(AppFragment.newInstance(AppManager.AppType.SYSTEM), "System Apps");
		viewPager.setAdapter(viewPagerAdapter);
		title_tabs.setupWithViewPager(viewPager);
		title_tabs.setTabMode(TabLayout.MODE_FIXED);

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
			Toast.makeText(MainActivity.this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
			oneClickTime = doubleClickTime;
		} else
		{
			System.exit(0);
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
				Toast.makeText(MainActivity.this, "文件清除" + (FileUtil.cleanFileDir("JanYoShare") ? "成功" : "失败") + "！", Toast.LENGTH_SHORT)
						.show();
				break;
			case R.id.action_about:
				new AlertDialog.Builder(MainActivity.this)
						.setTitle(" ")
						.setView(LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_about, null))
						.setPositiveButton("关闭", null)
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
					Toast.makeText(MainActivity.this, "请授予存储权限来拷贝apk文件到SD卡", Toast.LENGTH_LONG)
							.show();
					finish();
				}
				break;
		}
	}
}
