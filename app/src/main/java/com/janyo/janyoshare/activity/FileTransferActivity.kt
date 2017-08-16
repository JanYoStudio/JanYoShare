package com.janyo.janyoshare.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import com.janyo.janyoshare.R
import com.janyo.janyoshare.adapter.FileTransferAdapter
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.handler.TransferHelperHandler
import com.janyo.janyoshare.service.SendFileService
import com.janyo.janyoshare.util.FileTransferHelper
import com.mystery0.tools.FileUtil.FileUtil
import com.mystery0.tools.Logs.Logs

import kotlinx.android.synthetic.main.activity_file_transfer.*
import java.io.File

class FileTransferActivity : AppCompatActivity()
{
	private val TAG = "FileTransferActivity"
	private val CHOOSE_FILE = 233
	private lateinit var adapter: FileTransferAdapter

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_transfer)
		setSupportActionBar(toolbar)

		FileTransferHelper.getInstance().transferHelperHandler = TransferHelperHandler()
		adapter = FileTransferAdapter(this, FileTransferHelper.getInstance().transferHelperHandler!!.list)
		recycler_view.layoutManager = LinearLayoutManager(this)
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
				FileTransferHelper.getInstance().transferHelperHandler!!.list.removeAt(position)
				adapter.notifyItemRemoved(position)
			}
		}
		ItemTouchHelper(callback).attachToRecyclerView(recycler_view)
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
		menuInflater.inflate(R.menu.menu_file_transfer, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean
	{
		when (item.itemId)
		{
			R.id.action_send_files ->
			{
				val intent = Intent(this, SendFileService::class.java)
				intent.putExtra("action", "start")
				startService(intent)
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
			transferFile.filePath = data.data.path
			FileTransferHelper.getInstance().fileList.add(transferFile)
			FileTransferHelper.getInstance().transferHelperHandler!!.list.add(transferFile)
			adapter.notifyDataSetChanged()
		}
	}

	override fun onDestroy()
	{
		super.onDestroy()

	}
}
