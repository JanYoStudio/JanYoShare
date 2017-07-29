package com.janyo.janyoshare.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.support.annotation.RequiresApi
import com.janyo.janyoshare.activity.FileTransferConfigureActivity
import com.janyo.janyoshare.activity.MainActivity

@RequiresApi(Build.VERSION_CODES.N)
class FileTransferTileService : TileService()
{
	override fun onStartListening()
	{
		qsTile.state = Tile.STATE_ACTIVE
	}

	override fun onClick()
	{
		startActivity(Intent(applicationContext, FileTransferConfigureActivity::class.java))
	}
}