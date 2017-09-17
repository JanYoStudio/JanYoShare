package com.janyo.janyoshare.util

import android.content.Context
import android.widget.Toast
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Response
import vip.mystery0.tools.Logs.Logs
import vip.mystery0.tools.MysteryNetFrameWork.HttpUtil
import vip.mystery0.tools.MysteryNetFrameWork.ResponseListener
import java.io.File
import java.util.HashMap

/**
 * Created by myste.
 */
object ExceptionUtil
{
	fun sendException(context: Context, map: Map<String, String>, fileMap: HashMap<String, File>,
					  url: String, listener: ResponseListener)
	{
		HttpUtil(context)
				.setRequestQueue(Volley.newRequestQueue(context))
				.setUrl(url)
				.setRequestMethod(HttpUtil.RequestMethod.POST)
				.setFileRequest(HttpUtil.FileRequest.UPLOAD)
				.isFileRequest(true)
				.setMap(map)
				.setFileMap(fileMap)
				.setResponseListener(listener)
				.open()
	}
}