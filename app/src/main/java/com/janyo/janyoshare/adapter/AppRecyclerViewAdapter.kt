@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.adapter

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.util.FileUtil
import com.janyo.janyoshare.util.Settings

class AppRecyclerViewAdapter(private val context: Context,
							 private val installAppList: List<InstallApp>) : RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>()
{
	private val coordinatorLayout: View = (context as Activity).findViewById<View>(R.id.coordinatorLayout)
	private val settings: Settings = Settings(context)

	override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int)
	{
		val progressDialog = ProgressDialog(context)
		progressDialog.setCancelable(false)
		progressDialog.setMessage("提取中……")
		val installApp = installAppList[position]
		holder.textView_name.text = installApp.name
		holder.textView_packageName.text = installApp.packageName
		holder.textView_versionName.text = installApp.versionName
		holder.imageView.setImageDrawable(installApp.icon)
		holder.textView_size.text = FileUtil.FormatFileSize(installApp.size)
		holder.fullView.setOnClickListener {
			progressDialog.show()
			if (FileUtil.isDirExist(context.getString(R.string.app_name)))
			{
				Thread(Runnable {
					val code = FileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
					progressDialog.dismiss()
					if (code == 1)
					{
						Snackbar.make(coordinatorLayout, "文件提取成功，是否进行分享操作？", Snackbar.LENGTH_LONG)
								.setAction("分享") { FileUtil.doShare(context, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name)) }
								.addCallback(object : Snackbar.Callback()
								{
									override fun onDismissed(transientBottomBar: Snackbar?,
															 event: Int)
									{
										if (event != Snackbar.Callback.DISMISS_EVENT_ACTION)
										{
											if (!settings.isAutoClean)
											{
												Snackbar.make(coordinatorLayout, "如果没有额外操作，建议您开启自动清理功能。", Snackbar.LENGTH_SHORT)
														.show()
											}
										}
									}
								})
								.show()
					}
					else
					{
						Snackbar.make(coordinatorLayout, "文件提取失败", Snackbar.LENGTH_SHORT)
								.show()
					}
				}).start()
			}
			else
			{
				progressDialog.dismiss()
				Snackbar.make(coordinatorLayout, "文件夹不存在！", Snackbar.LENGTH_SHORT)
						.show()
			}
		}
		holder.fullView.setOnLongClickListener {
			progressDialog.show()
			if (FileUtil.isDirExist(context.getString(R.string.app_name)))
			{
				Thread(Runnable {
					val code = FileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
					progressDialog.dismiss()
					if (code == 1)
					{
						Snackbar.make(coordinatorLayout, "文件提取成功！存储路径为SD卡根目录下" + context.getString(R.string.app_name) + "文件夹", Snackbar.LENGTH_SHORT)
								.show()
					}
					else
					{
						Snackbar.make(coordinatorLayout, "文件提取失败", Snackbar.LENGTH_SHORT)
								.show()
					}
				}).start()
			}
			else
			{
				progressDialog.dismiss()
				Snackbar.make(coordinatorLayout, "文件夹不存在！", Snackbar.LENGTH_SHORT)
						.show()
			}
			true
		}
	}

	override fun getItemCount(): Int
	{
		return installAppList.size
	}

	class ViewHolder(var fullView: View) : RecyclerView.ViewHolder(fullView)
	{
		var imageView: ImageView = fullView.findViewById<ImageView>(R.id.app_icon)
		var textView_name: TextView = fullView.findViewById<TextView>(R.id.app_name)
		var textView_packageName: TextView = fullView.findViewById<TextView>(R.id.app_package_name)
		var textView_versionName: TextView = fullView.findViewById<TextView>(R.id.app_version_name)
		var textView_size: TextView = fullView.findViewById<TextView>(R.id.app_size)

	}
}
