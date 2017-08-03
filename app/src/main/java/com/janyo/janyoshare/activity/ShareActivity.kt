package com.janyo.janyoshare.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.janyo.janyoshare.classes.TransferFile
import com.mystery0.tools.FileUtil.FileUtil
import java.io.File

class ShareActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = intent.action
		val type = intent.type
		if (action == Intent.ACTION_SEND && type != null)
		{
			val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
			val path = FileUtil.getPath(this, uri)
			val file = File(path)
			val transferFile = TransferFile()
			transferFile.fileName = file.name
			transferFile.filePath = path
//			transferFile.fileIconPath = installApp.iconPath
			transferFile.fileSize = file.length()
			val intent = Intent(this, FileTransferConfigureActivity::class.java)
			val bundle = Bundle()
			bundle.putSerializable("app", transferFile)
			intent.putExtra("action", 1)
			intent.putExtra("app", bundle)
			startActivity(intent)
			finish()
		}
		else
		{
			Toast.makeText(this, "无效的文件！", Toast.LENGTH_SHORT)
					.show()
		}
	}
}