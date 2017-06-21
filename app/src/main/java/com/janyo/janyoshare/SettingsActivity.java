package com.janyo.janyoshare;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.janyo.janyoshare.util.Settings;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity
{
	private static final String TAG = "SettingsActivity";
	private Settings settings;
	private Toolbar toolbar;
	private SwitchPreference auto_clean;
	private SwitchPreference use_snack_bar;
	private EditTextPreference set_dir;

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
		use_snack_bar = (SwitchPreference) findPreference(getString(R.string.key_use_snack_bar));
		set_dir = (EditTextPreference) findPreference(getString(R.string.key_set_dir));

		auto_clean.setChecked(settings.isAutoClean());
		use_snack_bar.setChecked(settings.isUseSnackBar());
		set_dir.setText(settings.getDir());
	}

	private void monitor()
	{
		auto_clean.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object o)
			{
				settings.setAutoClean(!auto_clean.isChecked());
				return true;
			}
		});

		use_snack_bar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object o)
			{
				settings.setUseSnackBar(!use_snack_bar.isChecked());
				return true;
			}
		});

		set_dir.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object o)
			{
				Log.i(TAG, "onPreferenceChange: " + set_dir.getEditText().getText().toString());
				settings.setDir(set_dir.getEditText().getText().toString());
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
