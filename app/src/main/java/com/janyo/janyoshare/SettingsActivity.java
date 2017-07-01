package com.janyo.janyoshare;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.janyo.janyoshare.util.Settings;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity
{
	private Settings settings;
	private Toolbar toolbar;
	private SwitchPreference auto_clean;
	private Preference about;
	private Preference howToUse;
	private Preference openSourceAddress;
	private Preference checkUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		settings = new Settings(SettingsActivity.this);
		addPreferencesFromResource(R.xml.preferences);
		initialization();
		monitor();
		toolbar.setTitle(getTitle());
	}

	private void initialization()
	{
		auto_clean = (SwitchPreference) findPreference(getString(R.string.key_auto_clean));
		about = findPreference(getString(R.string.key_about));
		howToUse = findPreference(getString(R.string.key_how_to_use));
		openSourceAddress = findPreference(getString(R.string.key_open_source_address));
		checkUpdate = findPreference(getString(R.string.key_check_update));

		auto_clean.setChecked(settings.isAutoClean());
		if (settings.isAutoClean())
		{
			auto_clean.setSummary("已开启自动清理，将在下次启动时清理临时文件");
		} else
		{
			auto_clean.setSummary("已关闭自动清理");
		}
	}

	private void monitor()
	{
		auto_clean.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object o)
			{
				boolean isAutoClean = !auto_clean.isChecked();
				if (isAutoClean)
				{
					new AlertDialog.Builder(SettingsActivity.this)
							.setTitle(" ")
							.setMessage(R.string.autoCleanWarn)
							.setPositiveButton("开启", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialogInterface, int i)
								{
									settings.setAutoClean(true);
								}
							})
							.setNegativeButton("取消", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialogInterface, int i)
								{
									auto_clean.setChecked(false);
									settings.setAutoClean(false);
								}
							})
							.setOnDismissListener(new DialogInterface.OnDismissListener()
							{
								@Override
								public void onDismiss(DialogInterface dialogInterface)
								{
									auto_clean.setChecked(settings.isAutoClean());
									if (settings.isAutoClean())
									{
										auto_clean.setSummary("已开启自动清理，将在下次启动时清理临时文件");
									} else
									{
										auto_clean.setSummary("已关闭自动清理");
									}
								}
							})
							.show();
				} else
				{
					settings.setAutoClean(false);
					auto_clean.setSummary("已关闭自动清理");
				}
				return true;
			}
		});
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				new AlertDialog.Builder(SettingsActivity.this)
						.setTitle(" ")
						.setView(LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_about, new LinearLayout(SettingsActivity.this), false))
						.setPositiveButton("关闭", null)
						.show();
				return false;
			}
		});
		howToUse.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				View view = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.dialog_help, new LinearLayout(SettingsActivity.this), false);
				TextView textView = view.findViewById(R.id.autoCleanWarn);
				if (settings.isAutoClean())
				{
					textView.setVisibility(View.VISIBLE);
				}
				new AlertDialog.Builder(SettingsActivity.this)
						.setTitle(" ")
						.setView(view)
						.setPositiveButton("确定", null)
						.show();
				return false;
			}
		});
		openSourceAddress.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(getString(R.string.address_open_source));
				intent.setData(content_url);
				startActivity(intent);
				return false;
			}
		});
		checkUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(getString(R.string.address_check_update));
				intent.setData(content_url);
				startActivity(intent);
				return false;
			}
		});
	}

	@Override
	public void setContentView(int layoutResID)
	{
		ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_settings, new LinearLayout(this), false);
		toolbar = contentView.findViewById(R.id.toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				finish();
			}
		});

		ViewGroup contentWrapper = contentView.findViewById(R.id.content_wrapper);
		LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

		getWindow().setContentView(contentView);
	}
}
