package com.janyo.janyoshare.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.janyo.janyoshare.R;
import com.janyo.janyoshare.classes.InstallApp;
import com.janyo.janyoshare.util.FileUtil;
import com.janyo.janyoshare.util.Settings;

import java.util.List;

public class AppRecyclerViewAdapter extends RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>
{
	private Context context;
	private List<InstallApp> installAppList;
	private View coordinatorLayout;
	private Settings settings;

	public AppRecyclerViewAdapter(Context context, List<InstallApp> installAppList)
	{
		this.context = context;
		this.installAppList = installAppList;
		settings = new Settings(context);
		coordinatorLayout = ((Activity) context).findViewById(R.id.coordinatorLayout);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		final InstallApp installApp = installAppList.get(position);
		holder.textView_name.setText(installApp.getName());
		holder.textView_packageName.setText(installApp.getPackageName());
		holder.textView_versionName.setText(installApp.getVersionName());
		holder.imageView.setImageDrawable(installApp.getIcon());
		holder.fullView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				final ProgressDialog progressDialog = new ProgressDialog(context);
				progressDialog.setCancelable(false);
				progressDialog.setMessage("提取中……");
				progressDialog.show();
				if (FileUtil.isDirExist(context.getString(R.string.app_name)))
				{
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							int code = FileUtil.fileToSD(installApp.getSourceDir(), installApp.getName(), installApp.getVersionName(), context.getString(R.string.app_name));
							progressDialog.dismiss();
							if (code == 1)
							{
								Snackbar.make(coordinatorLayout, "文件提取成功，是否进行分享操作？", Snackbar.LENGTH_LONG)
										.setAction("分享", new View.OnClickListener()
										{
											@Override
											public void onClick(View view)
											{
												FileUtil.doShare(context, installApp.getName(), installApp.getVersionName(), context.getString(R.string.app_name));
											}
										})
										.addCallback(new Snackbar.Callback()
										{
											@Override
											public void onDismissed(Snackbar transientBottomBar, int event)
											{
												if (event != DISMISS_EVENT_ACTION)
												{
													if (!settings.isAutoClean())
													{
														Snackbar.make(coordinatorLayout, "如果没有额外操作，建议您开启自动清理功能。", Snackbar.LENGTH_SHORT)
																.show();
													}
												}
											}
										})
										.show();
							}else
							{
								Snackbar.make(coordinatorLayout,"文件提取失败",Snackbar.LENGTH_SHORT)
										.show();
							}
						}
					}).start();
				} else
				{
					progressDialog.dismiss();
					Snackbar.make(coordinatorLayout, "文件夹不存在！", Snackbar.LENGTH_SHORT)
							.show();
				}
			}
		});
		holder.fullView.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View view)
			{
				final ProgressDialog progressDialog = new ProgressDialog(context);
				progressDialog.setCancelable(false);
				progressDialog.setMessage("提取中……");
				progressDialog.show();
				if (FileUtil.isDirExist(context.getString(R.string.app_name)))
				{
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							int code = FileUtil.fileToSD(installApp.getSourceDir(), installApp.getName(), installApp.getVersionName(), context.getString(R.string.app_name));
							progressDialog.dismiss();
							if (code == 1)
							{
								Snackbar.make(coordinatorLayout, "文件提取成功！存储路径为SD卡根目录下" + context.getString(R.string.app_name) + "文件夹", Snackbar.LENGTH_SHORT)
										.show();
							}else
							{
								Snackbar.make(coordinatorLayout,"文件提取失败",Snackbar.LENGTH_SHORT)
										.show();
							}
						}
					}).start();
				} else
				{
					progressDialog.dismiss();
					Snackbar.make(coordinatorLayout, "文件夹不存在！", Snackbar.LENGTH_SHORT)
							.show();
				}
				return true;
			}
		});
	}

	@Override
	public int getItemCount()
	{
		return installAppList.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder
	{
		View fullView;
		ImageView imageView;
		TextView textView_name;
		TextView textView_packageName;
		TextView textView_versionName;

		public ViewHolder(View itemView)
		{
			super(itemView);
			fullView = itemView;
			imageView = itemView.findViewById(R.id.app_icon);
			textView_name = itemView.findViewById(R.id.app_name);
			textView_packageName = itemView.findViewById(R.id.app_package_name);
			textView_versionName = itemView.findViewById(R.id.app_version_name);
		}
	}
}
