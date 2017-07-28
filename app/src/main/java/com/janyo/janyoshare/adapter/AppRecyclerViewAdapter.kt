@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.adapter

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.janyo.janyoshare.activity.FileTransferConfigureActivity

import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.handler.RenameHandler
import com.janyo.janyoshare.handler.SendHandler
import com.janyo.janyoshare.util.JYFileUtil
import com.mystery0.tools.FileUtil.FileUtil
import java.io.File

class AppRecyclerViewAdapter(private val context: Context,
							 private val installAppList: List<InstallApp>) : RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>()
{
	private val coordinatorLayout: CoordinatorLayout = (context as Activity).findViewById(R.id.coordinatorLayout)
	private val renameHandler = RenameHandler(context as Activity)
	var progressDialog = ProgressDialog(context)

	val sendHandler = SendHandler()

	init
	{
		sendHandler.progressDialog = progressDialog
		sendHandler.context = context


	}

	override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
		return ViewHolder(view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int)
	{
		progressDialog.setCancelable(false)
		progressDialog.setOnCancelListener(null)
		progressDialog.setMessage(context.getString(R.string.copy_file_loading))
		val installApp = installAppList[position]
		holder.textView_name.text = installApp.name
		holder.textView_packageName.text = installApp.packageName
		holder.textView_versionName.text = installApp.versionName
		if (installApp.iconPath != null)
			Glide.with(context).load(installApp.iconPath).into(holder.imageView)
		else
			holder.imageView.setImageDrawable(installApp.icon)
		holder.textView_size.text = FileUtil.FormatFileSize(installApp.size)
		holder.fullView.setOnClickListener {
			AlertDialog.Builder(context)
					.setTitle(R.string.copy_file_selection)
					.setItems(R.array.copy_do, { _, choose ->
						when (choose)
						{
							0 ->
							{
								progressDialog.show()
								if (JYFileUtil.isDirExist(context.getString(R.string.app_name)))
								{
									Thread(Runnable {
										val code = JYFileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
										progressDialog.dismiss()
										when (code)
										{
											-1 ->
											{
												Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
														.show()
											}
											0 ->
											{
												Snackbar.make(coordinatorLayout, R.string.hint_copy_exist, Snackbar.LENGTH_SHORT)
														.setAction(R.string.hint_recopy, {
															progressDialog.show()
															Thread(Runnable {
																JYFileUtil.deleteFile(File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)), installApp.name + "_" + installApp.versionName + ".apk"))
																val temp = JYFileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																progressDialog.dismiss()
																if (temp == 1)
																{
																	Snackbar.make(coordinatorLayout, String.format(context.getString(R.string.hint_copy_done), context.getString(R.string.app_name)), Snackbar.LENGTH_SHORT)
																			.show()
																}
																else
																{
																	Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
																			.show()
																}
															}).start()
														})
														.show()
											}
											1 ->
											{
												Snackbar.make(coordinatorLayout, String.format(context.getString(R.string.hint_copy_done), context.getString(R.string.app_name)), Snackbar.LENGTH_SHORT)
														.show()
											}
										}
									}).start()
								}
								else
								{
									progressDialog.dismiss()
									Snackbar.make(coordinatorLayout, context.getString(R.string.hint_copy_not_exist), Snackbar.LENGTH_SHORT)
											.show()
								}
							}
							1 ->
							{
								progressDialog.show()
								if (JYFileUtil.isDirExist(context.getString(R.string.app_name)))
								{
									Thread(Runnable {
										val code = JYFileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
										progressDialog.dismiss()
										when (code)
										{
											-1 ->
											{
												Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
														.show()
											}
											0 ->
											{
												Snackbar.make(coordinatorLayout, R.string.hint_copy_exist, Snackbar.LENGTH_SHORT)
														.setAction(R.string.hint_recopy, {
															progressDialog.show()
															Thread(Runnable {
																JYFileUtil.deleteFile(File(Environment.getExternalStoragePublicDirectory(context.getString(R.string.app_name)), installApp.name + "_" + installApp.versionName + ".apk"))
																val temp = JYFileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																progressDialog.dismiss()
																if (temp == 1)
																{
																	JYFileUtil.doShare(context, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																}
																else
																{
																	Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
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
																	JYFileUtil.doShare(context, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
																}
															}
														})
														.show()
											}
											1 ->
											{
												JYFileUtil.doShare(context, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
											}
										}
									}).start()
								}
								else
								{
									progressDialog.dismiss()
									Snackbar.make(coordinatorLayout, context.getString(R.string.hint_copy_not_exist), Snackbar.LENGTH_SHORT)
											.show()
								}
							}
							2 ->
							{
								progressDialog.show()
								if (JYFileUtil.isDirExist(context.getString(R.string.app_name)))
								{
									Thread(Runnable {
										val code = JYFileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
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
											Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
													.show()
										}
									}).start()
								}
								else
								{
									progressDialog.dismiss()
									Snackbar.make(coordinatorLayout, context.getString(R.string.hint_copy_not_exist), Snackbar.LENGTH_SHORT)
											.show()
								}
							}
							3 ->
							{
								progressDialog.show()
								if (JYFileUtil.isDirExist(context.getString(R.string.app_name)))
								{
									Thread(Runnable {
										val code = JYFileUtil.fileToSD(installApp.sourceDir!!, installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
										progressDialog.dismiss()
										if (code != -1)
										{
											val transferFile = TransferFile()
											transferFile.fileName = installApp.name + ".apk"
											transferFile.filePath = JYFileUtil.getFilePath(installApp.name!!, installApp.versionName!!, context.getString(R.string.app_name))
											transferFile.fileIconPath = installApp.iconPath
											transferFile.fileSize = installApp.size
											val intent = Intent(context, FileTransferConfigureActivity::class.java)
											val bundle = Bundle()
											bundle.putSerializable("app", transferFile)
											intent.putExtra("action", 1)
											intent.putExtra("app", bundle)
											context.startActivity(intent)
										}
										else
										{
											Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
													.show()
										}
									}).start()
								}
								else
								{
									progressDialog.dismiss()
									Snackbar.make(coordinatorLayout, context.getString(R.string.hint_copy_not_exist), Snackbar.LENGTH_SHORT)
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
