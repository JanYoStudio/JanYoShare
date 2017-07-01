package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.janyo.janyoshare.classes.InstallApp;

import java.util.ArrayList;
import java.util.List;

public class AppManager
{
	private Context context;

	public enum AppType
	{
		SYSTEM, USER
	}

	public AppManager(Context context)
	{
		this.context = context;
	}

	public List<InstallApp> getInstallAppList(AppType appType)
	{
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
		List<InstallApp> installAppList = new ArrayList<>();
		switch (appType)
		{
			case SYSTEM:
				for (int i = 0; i < packageInfoList.size(); i++)
				{
					InstallApp installApp = new InstallApp();
					PackageInfo packageInfo = packageInfoList.get(i);
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
					{
						installApp.setName(packageInfo.applicationInfo.loadLabel(packageManager).toString());
						installApp.setVersionName(packageInfo.versionName);
						installApp.setSourceDir(packageInfo.applicationInfo.sourceDir);
						installApp.setPackageName(packageInfo.applicationInfo.packageName);
						installApp.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
						installAppList.add(installApp);
					}
				}
				break;
			case USER:
				for (int i = 0; i < packageInfoList.size(); i++)
				{
					InstallApp installApp = new InstallApp();
					PackageInfo packageInfo = packageInfoList.get(i);
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)
					{
						installApp.setName(packageInfo.applicationInfo.loadLabel(packageManager).toString());
						installApp.setVersionName(packageInfo.versionName);
						installApp.setSourceDir(packageInfo.applicationInfo.sourceDir);
						installApp.setPackageName(packageInfo.applicationInfo.packageName);
						installApp.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
						installAppList.add(installApp);
					}
				}
				break;
		}
		return installAppList;
	}

	public List<InstallApp> searchApps(List<InstallApp> sourceList, String message)
	{
		List<InstallApp> list = new ArrayList<>();
		for (InstallApp installApp : sourceList)
		{
			if (installApp.getName().toLowerCase().contains(message.toLowerCase()))
			{
				list.add(installApp);
			}
		}
		return list;
	}
}