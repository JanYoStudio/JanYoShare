package com.janyo.janyoshare;

import android.content.DialogInterface;
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

import com.janyo.janyoshare.util.Settings;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity
{
	private Settings settings;
	private Toolbar toolbar;
	private SwitchPreference auto_clean;

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
				}else
				{
					settings.setAutoClean(false);
					auto_clean.setSummary("已关闭自动清理");
				}
				return true;
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
