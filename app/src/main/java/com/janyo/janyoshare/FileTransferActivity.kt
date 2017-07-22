package com.janyo.janyoshare

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.janyo.janyoshare.adapter.FileTransferAdapter
import com.janyo.janyoshare.classes.TransferFile

import kotlinx.android.synthetic.main.activity_file_transfer.*
import android.content.Intent
import com.mystery0.tools.FileUtil.FileUtil
import java.io.File


class FileTransferActivity : AppCompatActivity()
{
	private val CHOOSE_FILE = 233
	private var list: ArrayList<TransferFile>? = null
	private var adapter: FileTransferAdapter? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_file_transfer)
		setSupportActionBar(toolbar)
		list = ArrayList<TransferFile>()
		adapter = FileTransferAdapter(list!!)

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
			transferFile.fileSize = FileUtil.FormatFileSize(file.length())
			list!!.add(transferFile)
			adapter!!.notifyDataSetChanged()
		}
	}

}