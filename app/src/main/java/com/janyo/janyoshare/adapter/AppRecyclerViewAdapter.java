package com.janyo.janyoshare.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.janyo.janyoshare.R;
import com.janyo.janyoshare.classes.InstallApp;
import com.janyo.janyoshare.util.FileUtil;

import java.util.List;

public class AppRecyclerViewAdapter extends RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>
{
	private Context context;
	private List<InstallApp> installAppList;

	public AppRecyclerViewAdapter(Context context, List<InstallApp> installAppList)
	{
		this.context = context;
		this.installAppList = installAppList;
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
				if (FileUtil.isDirExist("JanYoShare"))
				{
					FileUtil.doShare(context, installApp.getSourceDir(), installApp.getName(), installApp.getVersionName());
				} else
				{
					Toast.makeText(context, "文件夹不存在！", Toast.LENGTH_SHORT)
							.show();
				}
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
