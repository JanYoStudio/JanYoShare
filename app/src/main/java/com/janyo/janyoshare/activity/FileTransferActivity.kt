package com.janyo.janyoshare.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.janyo.janyoshare.adapter.FileTransferAdapter
import com.janyo.janyoshare.classes.TransferFile

import kotlinx.android.synthetic.main.activity_file_transfer.*
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import com.janyo.janyoshare.R
import com.janyo.janyoshare.service.ReceiveFileService
import com.janyo.janyoshare.service.SendFileService
import com.janyo.janyoshare.util.FileTransferHandler
import com.mystery0.tools.FileUtil.FileUtil
import com.mystery0.tools.Logs.Logs
import java.io.File

class FileTransferActivity : AppCompatActivity()
{
	private val TAG = "FileTransferActivity"
	private val CHOOSE_FILE = 233
	private var adapter: FileTransferAdapter? = null
	private val list = FileTransferHandler.getInstance().fileList

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_transfer)
		setSupportActionBar(toolbar)
		adapter = FileTransferAdapter(this, list)

		recycler_view.layoutManager = LinearLayoutManager(this)
		recycler_view.adapter = adapter

		fab.setOnClickListener {
			val intent = Intent(Intent.ACTION_GET_CONTENT)
			intent.type = "*/*"
			intent.addCategory(Intent.CATEGORY_OPENABLE)
			startActivityForResult(intent, CHOOSE_FILE)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		if (requestCode == CHOOSE_FILE && data != null)
		{
			val file = File(FileUtil.getPath(this, data.data))
			val transferFile = TransferFile()
			transferFile.fileName = file.name
			transferFile.filePath = file.absolutePath
			transferFile.fileSize = file.length()
			list.add(transferFile)
			adapter!!.notifyDataSetChanged()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean
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
				when (FileTransferHandler.getInstance().tag)
				{
					0 ->
					{
						Logs.e(TAG, "onOptionsItemSelected: 未知标志，取消传输")
					}
					1 ->
					{
						val intent = Intent(this, SendFileService::class.java)
						intent.putExtra("action", "start")
						startService(intent)
					}
					2 ->
					{
						val intent = Intent(this, ReceiveFileService::class.java)
						intent.putExtra("action", "start")
						startService(intent)
					}
				}
			}
		}
		return super.onOptionsItemSelected(item)
	}

}
