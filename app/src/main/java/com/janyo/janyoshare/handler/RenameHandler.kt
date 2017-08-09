package com.janyo.janyoshare.handler

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.janyo.janyoshare.R
import com.janyo.janyoshare.util.JYFileUtil

class RenameHandler(private val activity: Activity) : Handler()
{
	override fun handleMessage(message: Message)
	{
		when (message.what)
		{
			1 ->
			{
				val oldName = message.obj.toString()
				val coordinatorLayout: CoordinatorLayout = activity.findViewById(R.id.coordinatorLayout)
				val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit, TextInputLayout(activity), false)
				val text: TextInputLayout = view.findViewById(R.id.layout)
				AlertDialog.Builder(activity)
						.setTitle(R.string.hint_new_name)
						.setView(view)
						.setPositiveButton(R.string.action_done, { _, _ ->
							if (JYFileUtil.fileRename(oldName, activity.getString(R.string.app_name), text.editText!!.text.toString(), "apk"))
							{
								JYFileUtil.doShare(activity, text.editText!!.text.toString() + ".apk", activity.getString(R.string.app_name))
							}
							else
							{
								Snackbar.make(coordinatorLayout, R.string.hint_rename_error, Snackbar.LENGTH_SHORT)
										.show()
							}
						})
						.show()
			}
		}
	}
}