package com.janyo.janyoshare.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import com.janyo.janyoshare.R
import com.janyo.janyoshare.adapter.FileTransferAdapter
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.service.ReceiveFileService
import com.janyo.janyoshare.service.SendFileService
import com.janyo.janyoshare.util.FileTransferHelper
import com.janyo.janyoshare.util.Settings
import vip.mystery0.tools.fileUtil.FileUtil
import vip.mystery0.tools.logs.Logs

import kotlinx.android.synthetic.main.activity_file_transfer.*
import java.io.File

class FileTransferActivity : AppCompatActivity()
{
	private val TAG = "FileTransferActivity"
	private val CHOOSE_FILE = 233
	private lateinit var adapter: FileTransferAdapter

	override fun onCreate(savedInstanceState: Bundle?)
	{
		if (Settings.getInstance(this).dayNight)
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		else
			delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		super.onCreate(savedInstanceState)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
			window.exitTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.slide_right)
			window.enterTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left)
			window.reenterTransition = TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left)
		}
		setContentView(R.layout.activity_file_transfer)
		setSupportActionBar(toolbar)

		adapter = FileTransferAdapter(this, FileTransferHelper.getInstance().transferHelperHandler!!.list)
		recycler_view.layoutManager = LinearLayoutManager(this)
		if (FileTransferHelper.getInstance().tag == 1)
		{
			val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT)
			{
				override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
									target: RecyclerView.ViewHolder): Boolean
				{
					return false
				}

				override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
				{
					val position = viewHolder.adapterPosition
					Logs.i(TAG, "onSwiped: " + position)
					FileTransferHelper.getInstance().fileList.removeAt(position)
					FileTransferHelper.getInstance().transferHelperHandler!!.list.removeAt(position)
					adapter.notifyItemRemoved(position)
				}
			}
			ItemTouchHelper(callback).attachToRecyclerView(recycler_view)
		}
		else
			fab.visibility = View.GONE
		recycler_view.adapter = adapter
		FileTransferHelper.getInstance().transferHelperHandler!!.adapter = adapter

		fab.setOnClickListener {
			val intent = Intent(Intent.ACTION_GET_CONTENT)
			intent.type = "*/*"
			intent.addCategory(Intent.CATEGORY_OPENABLE)
			startActivityForResult(intent, CHOOSE_FILE)
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean
	{
		if (FileTransferHelper.getInstance().tag == 1)
			menuInflater.inflate(R.menu.menu_file_transfer, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		when (item.itemId)
		{
			R.id.action_send_files ->
			{
				if (FileTransferHelper.getInstance().fileList.size == 0)
				{
					Snackbar.make(coordinatorLayout, R.string.hint_transfer_file_not_exists, Snackbar.LENGTH_SHORT)
							.show()
				}
				else
				{
					val intent = Intent(this, SendFileService::class.java)
					intent.putExtra("action", "start")
					startService(intent)
				}
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		if (requestCode == CHOOSE_FILE && data != null)
		{
			val file = File(FileUtil.getPath(this, data.data))
			val transferFile = TransferFile()
			transferFile.fileName = file.name
			transferFile.fileUri = FileProvider.getUriForFile(this, getString(R.string.authorities), file).toString()
			transferFile.fileSize = file.length()
			transferFile.filePath = FileUtil.getPath(this, data.data)
			FileTransferHelper.getInstance().fileList.add(transferFile)
			FileTransferHelper.getInstance().transferHelperHandler!!.list.add(transferFile)
			adapter.notifyDataSetChanged()
		}
	}

	override fun onDestroy()
	{
		super.onDestroy()
		stopService(Intent(this, ReceiveFileService::class.java))
		stopService(Intent(this, SendFileService::class.java))
	}
}
