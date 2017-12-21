package com.janyo.janyoshare.util

import android.content.Context
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.Response
import com.janyo.janyoshare.handler.UploadLogHandler
import vip.mystery0.tools.hTTPok.HTTPok
import vip.mystery0.tools.hTTPok.HTTPokResponse
import vip.mystery0.tools.hTTPok.HTTPokResponseListener
import vip.mystery0.tools.logs.Logs

/**
 * Created by myste.
 */
object ExceptionUtil
{
	private val TAG = "ExceptionUtil"

	fun sendException(map: Map<String, Any>, url: String, listener: HTTPokResponseListener)
	{
		HTTPok().setURL(url)
				.setRequestMethod(HTTPok.POST)
				.setParams(map)
				.setListener(listener)
				.open()
	}

	fun tryOther(map: Map<String, Any>, uploadLogHandler: UploadLogHandler)
	{
		ExceptionUtil.sendException(map, "http://123.206.186.70/php/uploadLog/upload_file.php", object : HTTPokResponseListener
		{
			override fun onError(message: String?)
			{
				Logs.e(TAG, "onError: " + message)
				uploadLogHandler.sendEmptyMessage(-1)
			}

			override fun onResponse(response: HTTPokResponse?)
			{
				try
				{
					val response2 = response?.getJSON(Response::class.java)
					uploadLogHandler.response = response2
				}
				catch (e: Exception)
				{
				}
				uploadLogHandler.sendEmptyMessage(0)
			}
		})
	}

	fun tryOther(context: Context, map: Map<String, Any>)
	{
		ExceptionUtil.sendException(map, "http://123.206.186.70/php/uploadLog/upload_file.php", object : HTTPokResponseListener
		{
			override fun onError(message: String?)
			{
				Logs.e(TAG, "onError: " + message)
				Toast.makeText(context, R.string.hint_upload_log_done, Toast.LENGTH_SHORT)
						.show()
			}

			override fun onResponse(response: HTTPokResponse?)
			{
				try
				{
					val response2 = response?.getJSON(Response::class.java)
					if (response2?.code == 0)
						Toast.makeText(context, R.string.hint_upload_log_done, Toast.LENGTH_SHORT)
								.show()
					else
						Toast.makeText(context, R.string.hint_upload_log_error, Toast.LENGTH_SHORT)
								.show()
				}
				catch (e: Exception)
				{
					Toast.makeText(context, R.string.hint_upload_log_error, Toast.LENGTH_SHORT)
							.show()
				}
			}
		})
	}
}