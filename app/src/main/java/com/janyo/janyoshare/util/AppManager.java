package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.janyo.janyoshare.classes.InstallApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

	public List<InstallApp> getInstallAppList(AppType appType, int type)
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
						installApp.setSize(new File(packageInfo.applicationInfo.publicSourceDir).length());
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
						installApp.setSize(new File(packageInfo.applicationInfo.publicSourceDir).length());
						installAppList.add(installApp);
					}
				}
				break;
		}
		if (type == 0)
			return installAppList;
		return sortList(installAppList, type);
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

	private List<InstallApp> sortList(List<InstallApp> list, int type)
	{
		Object[] objects = list.toArray();
		InstallApp[] installApps = new InstallApp[objects.length];
		for (int i = 0; i < objects.length; i++)
		{
			installApps[i] = (InstallApp) objects[i];
		}
		quickSort(installApps, 0, list.size() - 1, type);
		List<InstallApp> installAppList = new ArrayList<>();
		Collections.addAll(installAppList, installApps);
		return installAppList;
	}

	private void quickSort(InstallApp A[], int begin, int end, int type)
	{
		if (begin < end)
		{
			int q;
			q = partition(A, begin, end, type);
			quickSort(A, begin, q - 1, type);
			quickSort(A, q + 1, end, type);
		}
	}

	private int partition(InstallApp A[], int low, int high, int type)
	{
		InstallApp pivot = A[low];
		switch (type)
		{
			case 1:
				while (low < high)
				{
					while (low < high && A[high].getName().compareToIgnoreCase(pivot.getName()) >= 0)
						--high;
					A[low] = A[high];
					while (low < high && A[low].getName().compareToIgnoreCase(pivot.getName()) <= 0)
						++low;
					A[high] = A[low];
				}
				break;
			case 2:
				while (low < high)
				{
					while (low < high && A[high].getSize() >= pivot.getSize())
						--high;
					A[low] = A[high];
					while (low < high && A[low].getSize() <= pivot.getSize())
						++low;
					A[high] = A[low];
				}
				break;
			case 3:
				while (low < high)
				{
					while (low < high && A[high].getPackageName().compareToIgnoreCase(pivot.getPackageName()) >= 0)
						--high;
					A[low] = A[high];
					while (low < high && A[low].getPackageName().compareToIgnoreCase(pivot.getPackageName()) <= 0)
						++low;
					A[high] = A[low];
				}
				break;
		}
		A[low] = pivot;
		return low;
	}
}