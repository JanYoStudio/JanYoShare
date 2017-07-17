@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.adapter

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView

import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.util.FileUtil
import java.io.File

class AppRecyclerViewAdapter(private val context: Context,
							 private val installAppList: List<InstallApp>) : RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>()
{
	private val coordinatorLayout: CoordinatorLayout = (context as Activity).findViewById(R.id.coordinatorLayout)
	private val renameHandler = RenameHandler(context as Activity)

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
			AlertDialog.Builder(context)
					.setTitle("请选择操作")
					.setItems(R.array.copy_do, { _, choose ->
						when (choose)
						{
							0 ->
							{
								progressDialog.show()
								if (FileUtil.isDirExist(context.getString(R.string.app_name)))
								{
									Thread(Runnable {
										val code = FileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
										progressDialog.dismiss()
										when (code)
										{
											-1 ->
											{
												Snackbar.make(coordinatorLayout, "文件提取失败", Snackbar.LENGTH_SHORT)
														.show()
											}
											0 ->
											{
												Snackbar.make(coordinatorLayout, "文件已经存在", Snackbar.LENGTH_SHORT)
														.setAction("重新提取", {
															progressDialog.show()
															Thread(Runnable {
																FileUtil.deleteFile(File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)), installApp.name + "_" + installApp.versionName + ".apk"))
																val temp = FileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																progressDialog.dismiss()
																if (temp == 1)
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
														})
														.show()
											}
											1 ->
											{
												Snackbar.make(coordinatorLayout, "文件提取成功！存储路径为SD卡根目录下" + context.getString(R.string.app_name) + "文件夹", Snackbar.LENGTH_SHORT)
														.show()
											}
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
							1 ->
							{
								progressDialog.show()
								if (FileUtil.isDirExist(context.getString(R.string.app_name)))
								{
									Thread(Runnable {
										val code = FileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
										progressDialog.dismiss()
										when (code)
										{
											-1 ->
											{
												Snackbar.make(coordinatorLayout, "文件提取失败", Snackbar.LENGTH_SHORT)
														.show()
											}
											0 ->
											{
												Snackbar.make(coordinatorLayout, "文件已经存在", Snackbar.LENGTH_SHORT)
														.setAction("重新提取", {
															progressDialog.show()
															Thread(Runnable {
																FileUtil.deleteFile(File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)), installApp.name + "_" + installApp.versionName + ".apk"))
																val temp = FileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																progressDialog.dismiss()
																if (temp == 1)
																{
																	FileUtil.doShare(context, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																}
																else
																{
																	Snackbar.make(coordinatorLayout, "文件提取失败", Snackbar.LENGTH_SHORT)
																			.show()
																}
															}).start()
														})
														.addCallback(object : Snackbar.Callback()
														{
															override fun onDismissed(
																	transientBottomBar: Snackbar?,
																	event: Int)
															{
																if (event != DISMISS_EVENT_ACTION)
																{
																	FileUtil.doShare(context, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																}
															}
														})
														.show()
											}
											1 ->
											{
												FileUtil.doShare(context, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
											}
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
							2 ->
							{
								progressDialog.show()
								if (FileUtil.isDirExist(context.getString(R.string.app_name)))
								{
									Thread(Runnable {
										val code = FileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
										progressDialog.dismiss()
										if (code != -1)
										{
											val message = Message()
											message.what = 1
											message.obj = installApp
											renameHandler.sendMessage(message)
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
						}
					})
					.show()
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

internal class RenameHandler(private val activity: Activity) : Handler()
{
	override fun handleMessage(message: Message)
	{
		when (message.what)
		{
			1 ->
			{
				val installApp = message.obj as InstallApp
				val coordinatorLayout: CoordinatorLayout = activity.findViewById(R.id.coordinatorLayout)
				val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
				val text: TextInputLayout = view.findViewById(R.id.layout)
				text.hint = installApp.name + "_" + installApp.versionName
				android.support.v7.app.AlertDialog.Builder(activity)
						.setTitle("请输入新的文件名(不包含扩展名)")
						.setView(view)
						.setPositiveButton(R.string.action_done, { _, _ ->
							if (FileUtil.fileRename(installApp.name!!, installApp.versionName!!, activity.getString(R.string.app_name), text.editText!!.text.toString()))
							{
								FileUtil.doShare(activity, text.editText!!.text.toString() + ".apk", activity.getString(R.string.app_name))
							}
							else
							{
								Snackbar.make(coordinatorLayout, "重命名失败，已取消分享！", Snackbar.LENGTH_SHORT)
										.show()
							}
						})
						.show()
			}
		}
	}
}
