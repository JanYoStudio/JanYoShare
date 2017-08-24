package com.janyo.janyoshare.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.janyo.janyoshare.activity.FileTransferConfigureActivity

import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.handler.RenameHandler
import com.janyo.janyoshare.handler.SendHandler
import com.janyo.janyoshare.util.FileTransferHelper
import com.janyo.janyoshare.util.JYFileUtil
import com.janyo.janyoshare.util.Settings
import com.mystery0.tools.FileUtil.FileUtil
import com.mystery0.tools.Logs.Logs
import dmax.dialog.SpotsDialog
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppRecyclerViewAdapter(private val context: Context,
							 private val installAppList: List<InstallApp>) : RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>()
{
	private val TAG = "AppRecyclerViewAdapter"
	private val coordinatorLayout: CoordinatorLayout = (context as Activity).findViewById(R.id.coordinatorLayout)
	private val renameHandler = RenameHandler(context as Activity)
	private val shareList = ArrayList<File>()
	private val settings = Settings.getInstance(context)
	private var progressDialog = SpotsDialog(context, R.style.SpotsDialog)
	private val sendHandler = SendHandler()
	var menu: Menu? = null
	val multiChoiceList = ArrayList<InstallApp>()

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

		holder.checkBox.tag = installApp
		if (multiChoiceList.contains(installApp))
		{
			holder.imageView.visibility = View.GONE
			holder.checkBox.visibility = View.VISIBLE
			holder.checkBox.isChecked = true
		}
		else
		{
			holder.checkBox.visibility = View.GONE
			holder.imageView.visibility = View.VISIBLE
			holder.checkBox.isChecked = false
		}

		val singleThreadPool = Executors.newSingleThreadExecutor()
		holder.imageView.setOnClickListener {
			holder.imageView.visibility = View.GONE
			holder.checkBox.visibility = View.VISIBLE
			holder.checkBox.isChecked = true
		}
		holder.checkBox.setOnCheckedChangeListener { _, checked ->
			if (checked)
			{
				if (multiChoiceList.size == 0)
				{
					val action_search = menu!!.findItem(R.id.action_search)
					val action_sort = menu!!.findItem(R.id.action_sort)
					val action_clear = menu!!.findItem(R.id.action_clear)
					val action_export = menu!!.findItem(R.id.action_export)
					val action_send = menu!!.findItem(R.id.action_send)
					action_search.isVisible = false
					action_sort.isVisible = false
					action_clear.isVisible = false
					action_export.isVisible = true
					action_send.isVisible = true
				}
				if (!multiChoiceList.contains(holder.checkBox.tag as InstallApp))
					multiChoiceList.add(installApp)
			}
			else
			{
				holder.checkBox.visibility = View.GONE
				holder.imageView.visibility = View.VISIBLE
				if (multiChoiceList.contains(holder.checkBox.tag as InstallApp))
					multiChoiceList.remove(installApp)
				if (multiChoiceList.size == 0)
				{
					(context as Activity).invalidateOptionsMenu()
				}
			}
		}
		holder.fullView.setOnClickListener {
			AlertDialog.Builder(context)
					.setTitle(R.string.copy_file_selection)
					.setItems(R.array.copy_do, { _, choose ->
						if (choose == 6)
						{
							copyInfo(installApp)
						}
						else
						{
							progressDialog.show()
							if (JYFileUtil.isDirExist(context.getString(R.string.app_name)))
							{
								doSelect(singleThreadPool, installApp, choose)
							}
							else
							{
								progressDialog.dismiss()
								Snackbar.make(coordinatorLayout, context.getString(R.string.hint_copy_not_exist), Snackbar.LENGTH_SHORT)
										.show()
							}
						}
					})
					.show()
		}
		holder.fullView.setOnLongClickListener {
			if (settings.longClickDo != 0)
			{
				progressDialog.show()
				if (JYFileUtil.isDirExist(context.getString(R.string.app_name)))
				{
					doSelect(singleThreadPool, installApp, settings.longClickDo)
				}
				else
				{
					progressDialog.dismiss()
					Snackbar.make(coordinatorLayout, context.getString(R.string.hint_copy_not_exist), Snackbar.LENGTH_SHORT)
							.show()
				}
			}
			true
		}
	}

	private fun doSelect(singleThreadPool: ExecutorService, installApp: InstallApp, choose: Int)
	{
		singleThreadPool.execute {
			val code: Int = if (settings.customFileName.format == "")
				JYFileUtil.fileToSD(installApp.sourceDir!!, installApp, context.getString(R.string.app_name), "apk")
			else
				JYFileUtil.fileToSD(installApp.sourceDir!!, settings.customFileName, installApp, context.getString(R.string.app_name), "apk")
			progressDialog.dismiss()
			when
			{
				code == -1 ->
				{
					Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
							.show()
				}
				code == 1 || choose != 0 ->
					doChoose(choose, installApp)
				code == 0 ->
				{
					Snackbar.make(coordinatorLayout, R.string.hint_copy_exist, Snackbar.LENGTH_SHORT)
							.setAction(R.string.hint_recopy, {
								progressDialog.show()
								singleThreadPool.execute {
									if (settings.customFileName.format == "")
										JYFileUtil.deleteFile(JYFileUtil.getFilePath(installApp, context.getString(R.string.app_name), "apk"))
									else
										JYFileUtil.deleteFile(JYFileUtil.getFilePath(settings.customFileName, installApp, context.getString(R.string.app_name), "apk"))
									val temp: Int = if (settings.customFileName.format == "")
										JYFileUtil.fileToSD(installApp.sourceDir!!, installApp, context.getString(R.string.app_name), "apk")
									else
										JYFileUtil.fileToSD(installApp.sourceDir!!, settings.customFileName, installApp, context.getString(R.string.app_name), "apk")
									progressDialog.dismiss()
									if (temp == 1)
									{
										doChoose(choose, installApp)
									}
									else
									{
										Snackbar.make(coordinatorLayout, R.string.hint_copy_error, Snackbar.LENGTH_SHORT)
												.show()
									}
								}
							})
							.addCallback(object : Snackbar.Callback()
							{
								override fun onDismissed(
										transientBottomBar: Snackbar?,
										event: Int)
								{
									if (event != DISMISS_EVENT_ACTION)
									{
										doChoose(choose, installApp)
									}
								}
							})
							.show()
				}
			}
		}
	}

	private fun copyInfo(installApp: InstallApp)
	{
		AlertDialog.Builder(context)
				.setTitle(R.string.copy_file_selection)
				.setItems(R.array.copy_info, { _, choose ->
					when (choose)
					{
						0 -> copyToClipboard(installApp.name, installApp.name)
						1 -> copyToClipboard(installApp.name, installApp.packageName)
						2 -> copyToClipboard(installApp.name, installApp.versionName)
						3 -> copyToClipboard(installApp.name, installApp.versionCode.toString())
					}
				})
				.show()
	}

	private fun copyToClipboard(label: String?, text: String?)
	{
		val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		clipboardManager.primaryClip = ClipData.newPlainText(label, text)
		Snackbar.make(coordinatorLayout, R.string.hint_copy_info, Snackbar.LENGTH_SHORT)
				.show()
	}

	private fun doChoose(choose: Int, installApp: InstallApp)
	{
		when (choose)
		{
			0 -> Snackbar.make(coordinatorLayout, String.format(context.getString(R.string.hint_copy_done), context.getString(R.string.app_name)), Snackbar.LENGTH_SHORT)
					.show()
			1 ->
				if (settings.customFileName.format == "")
					JYFileUtil.doShare(context, installApp, context.getString(R.string.app_name), "apk")
				else
					JYFileUtil.doShare(context, settings.customFileName, installApp, context.getString(R.string.app_name), "apk")
			2 ->
			{
				val message = Message()
				message.what = 1
				message.obj = installApp
				if (settings.customFileName.format == "")
					message.obj = installApp.name + "_" + installApp.versionName
				else
					message.obj = settings.customFileName.toFormat(installApp)
				renameHandler.sendMessage(message)
			}
			3 ->
			{
				val message = Message()
				message.what = 2
				message.obj = installApp
				if (settings.customFileName.format == "")
					message.obj = installApp.name + "_" + installApp.versionName
				else
					message.obj = settings.customFileName.toFormat(installApp)
				renameHandler.sendMessage(message)
			}
			4 ->
			{
				if (settings.customFileName.format == "")
					shareList.add(JYFileUtil.getFilePath(installApp, context.getString(R.string.app_name), "apk"))
				else
					shareList.add(JYFileUtil.getFilePath(settings.customFileName, installApp, context.getString(R.string.app_name), "apk"))
				val files = JYFileUtil.checkObb(installApp.packageName!!)
				if (files != null)
				{
					Snackbar.make(coordinatorLayout, context.getString(R.string.hint_check_obb_number_warning, files.size), Snackbar.LENGTH_LONG)
							.setAction(R.string.action_done, {
								shareList.addAll(files)
							})
							.addCallback(object : Snackbar.Callback()
							{
								override fun onDismissed(
										transientBottomBar: Snackbar?,
										event: Int)
								{
									Logs.i(TAG, "onDismissed: " + shareList.size)
									if (shareList.size > 1)
									{
										JYFileUtil.doShare(context, shareList)
									}
									else
									{
										if (settings.customFileName.format == "")
											JYFileUtil.doShare(context, installApp, context.getString(R.string.app_name), "apk")
										else
											JYFileUtil.doShare(context, settings.customFileName, installApp, context.getString(R.string.app_name), "apk")
									}
									shareList.clear()
								}
							})
							.show()
				}
				else
				{
					Snackbar.make(coordinatorLayout, context.getString(R.string.hint_check_obb_not_exists), Snackbar.LENGTH_SHORT)
							.addCallback(object : Snackbar.Callback()
							{
								override fun onDismissed(
										transientBottomBar: Snackbar?,
										event: Int)
								{
									if (settings.customFileName.format == "")
										JYFileUtil.doShare(context, installApp, context.getString(R.string.app_name), "apk")
									else
										JYFileUtil.doShare(context, settings.customFileName, installApp, context.getString(R.string.app_name), "apk")
									shareList.clear()
								}
							})
							.show()
				}
			}
			5 ->
			{
				val transferFile = TransferFile()
				if (settings.customFileName.format == "")
					transferFile.fileName = installApp.name + ".apk"
				else
					transferFile.fileName = settings.customFileName.toFormat(installApp) + ".apk"
				val file = if (settings.customFileName.format == "")
					JYFileUtil.getFilePath(installApp, context.getString(R.string.app_name), "apk")
				else
					JYFileUtil.getFilePath(settings.customFileName, installApp, context.getString(R.string.app_name), "apk")
				transferFile.fileUri = FileProvider.getUriForFile(context,
						context.getString(R.string.authorities),
						file).toString()
				transferFile.filePath = file.absolutePath
				transferFile.fileSize = installApp.size
				FileTransferHelper.getInstance().fileList.add(transferFile)
				val intent = Intent(context, FileTransferConfigureActivity::class.java)
				intent.putExtra("action", 1)
				context.startActivity(intent)
			}
		}
	}

	override fun getItemCount(): Int
	{
		return installAppList.size
	}

	class ViewHolder(var fullView: View) : RecyclerView.ViewHolder(fullView)
	{
		var checkBox: CheckBox = fullView.findViewById(R.id.checkBox)
		var imageView: ImageView = fullView.findViewById(R.id.app_icon)
		var textView_name: TextView = fullView.findViewById(R.id.app_name)
		var textView_packageName: TextView = fullView.findViewById(R.id.app_package_name)
		var textView_versionName: TextView = fullView.findViewById(R.id.app_version_name)
		var textView_size: TextView = fullView.findViewById(R.id.app_size)
	}
}
