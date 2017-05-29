package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PM
{
	PackageManager mPackageManager;
	Context mContext;

	public PM(Context context)
	{
		this.mContext = context;
	}

	public List<Map<String, Object>> getUserAppList()
	{
		mPackageManager = mContext.getPackageManager();
		List<PackageInfo> applist = mPackageManager.getInstalledPackages(0);
		List<Map<String, Object>> relistv = new ArrayList<Map<String, Object>>();

		//String text = "";
		//int icon = 0 ;
		for (int i = 0; i < applist.size(); i++)
		{
			PackageInfo mPackageInfo = applist.get(i);
			if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)
			{
				//text = text +"\n" + mPackageInfo.applicationInfo.sourceDir + " " + mPackageInfo.applicationInfo.loadLabel(mPackageManager);
				Map<String, Object> listmap = new HashMap<String, Object>();
				listmap.put("Name", mPackageInfo.applicationInfo.loadLabel(mPackageManager));
				listmap.put("VersionName", mPackageInfo.versionName);
				listmap.put("Dir", mPackageInfo.applicationInfo.sourceDir);
				listmap.put("PGN", mPackageInfo.applicationInfo.packageName);
				listmap.put("icon", mPackageInfo.applicationInfo.loadIcon(mPackageManager));
				relistv.add(listmap);
				//icon = mPackageInfo.applicationInfo.icon;
			}
		}
		return relistv;
	}

	public List<Map<String, Object>> getSystemAppList()
	{
		mPackageManager = mContext.getPackageManager();
		List<PackageInfo> applist = mPackageManager.getInstalledPackages(0);
		List<Map<String, Object>> relistv = new ArrayList<Map<String, Object>>();

		//String text = "";
		//int icon = 0 ;
		for (int i = 0; i < applist.size(); i++)
		{
			PackageInfo mPackageInfo = applist.get(i);
			if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
			{
				//text = text +"\n" + mPackageInfo.applicationInfo.sourceDir + " " + mPackageInfo.applicationInfo.loadLabel(mPackageManager);
				Map<String, Object> listmap = new HashMap<String, Object>();
				listmap.put("Name", mPackageInfo.applicationInfo.loadLabel(mPackageManager));
				listmap.put("VersionName", mPackageInfo.versionName);
				listmap.put("Dir", mPackageInfo.applicationInfo.sourceDir);
				listmap.put("PGN", mPackageInfo.applicationInfo.packageName);
				listmap.put("icon", mPackageInfo.applicationInfo.loadIcon(mPackageManager));
				relistv.add(listmap);
				//icon = mPackageInfo.applicationInfo.icon;
			}
		}
		return relistv;
	}
}