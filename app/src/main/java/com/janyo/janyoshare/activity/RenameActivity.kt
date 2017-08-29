package com.janyo.janyoshare.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.janyo.janyoshare.R
import com.mystery0.tools.Logs.Logs
import java.io.File

class RenameActivity : AppCompatActivity()
{
	private val TAG = "RenameActivity"
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = intent.action
		val type = intent.type
		if (action == Intent.ACTION_VIEW && type != null)
		{
			val uri = intent.data
			val file = File(uri.path)
			if (file.renameTo(File(file.parent + File.separator + file.nameWithoutExtension + ".apk")))
			{
				Logs.i(TAG, "onCreate: 重命名完成")
				val intent = Intent(Intent.ACTION_VIEW)
				val openFile = File(file.parent + File.separator + file.name + ".apk")
				val openUri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
					FileProvider.getUriForFile(this, getString(R.string.authorities), openFile)
				else
					Uri.fromFile(openFile)
				intent.setDataAndType(openUri, "application/vnd.android.package-archive")
				startActivity(intent)
			}
			else
			{
				Toast.makeText(this, R.string.hint_jys_rename_error, Toast.LENGTH_SHORT)
						.show()
			}
		}
		finish()
	}
}