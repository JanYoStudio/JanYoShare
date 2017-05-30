package com.janyo.janyoshare;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.janyo.janyoshare.util.FileOperation;
import com.janyo.janyoshare.util.FileUtil;
import com.janyo.janyoshare.util.PM;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;

public class UserFragment extends Fragment
{
	private View mView;
	private ListView appListV;
	private TextView appPath, appName, versionName;
	private LinearLayout ProgressBarLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mView = inflater.inflate(R.layout.fragment_userapp, null);
		appListV = (ListView) mView.findViewById(R.id.fragment_userlistview);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				List<Map<String, Object>> applist = new PM(getActivity()).getUserAppList();
				final SimpleAdapter sa = new SimpleAdapter(getActivity(), applist, R.layout.view_app_listview, new String[]{"Name", "Dir", "icon", "VersionName"}, new int[]{R.id.tv_app_name, R.id.tv_app_package_name, R.id.iv_app_icon, R.id.tv_app_version_name});
				getActivity().runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						ProgressBarLayout = (LinearLayout) mView.findViewById(R.id.ProgressBarLayout);
						ProgressBarLayout.setVisibility(View.GONE);
						appListV.setAdapter(sa);
						sa.setViewBinder(new ViewBinder()
						{

							@Override
							public boolean setViewValue(View p1, Object p2, String p3)
							{
								if (p1 instanceof ImageView && p2 instanceof Drawable)
								{
									ImageView iv = (ImageView) p1;
									iv.setImageDrawable((Drawable) p2);
									return true;
								} else return false;
							}
						});
					}

				});
			}
		}).start();

		appListV.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
			{
				appPath = (TextView) p2.findViewById(R.id.tv_app_package_name);
				appName = (TextView) p2.findViewById(R.id.tv_app_name);
				versionName = (TextView) p2.findViewById(R.id.tv_app_version_name);
				FileUtil.doShare(getActivity(), appPath.getText().toString(), appName.getText().toString(), versionName.getText().toString());
			}
		});

//		appListV.setOnItemLongClickListener(new OnItemLongClickListener()
//		{
//
//			@Override
//			public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
//			{
//				appPath = (TextView) p2.findViewById(R.id.tv_app_package_name);
//				appName = (TextView) p2.findViewById(R.id.tv_app_name);
//				versionName = (TextView) p2.findViewById(R.id.tv_app_version_name);
//				FileUtil.fileToSD(appPath.getText().toString(), appName.getText().toString(), versionName.getText().toString());
//				Toast.makeText(getActivity(), "已提取到手机储存/JYShare/" + appName.getText() + "_" + versionName.getText() + ".apk", Toast.LENGTH_SHORT).show();
//				return true;
//			}
//
//		});
		return mView;
	}
}