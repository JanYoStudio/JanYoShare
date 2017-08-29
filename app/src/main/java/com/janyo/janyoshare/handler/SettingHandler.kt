package com.janyo.janyoshare.handler

import android.content.Context
import android.os.Handler
import android.os.Message
import android.preference.Preference
import android.support.v7.app.AlertDialog
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.util.Settings
import dmax.dialog.SpotsDialog

class SettingHandler(private val context: Context,
					 private val spotsDialog: SpotsDialog,
					 private val excludeList: Preference) : Handler()
{
	override fun handleMessage(msg: Message)
	{
		@Suppress("UNCHECKED_CAST")
		val list = msg.obj as List<InstallApp>
		val settings = Settings.getInstance(context)
		val arrays = Array(list.size, { i -> list[i].name })
		val checkedItems = BooleanArray(list.size)
		val saved = settings.excludeList
		list.indices
				.filter { saved.contains(list[it].packageName) }
				.forEach { checkedItems[it] = true }
		spotsDialog.dismiss()
		AlertDialog.Builder(context)
				.setTitle(R.string.hint_exclude_list_title)
				.setMultiChoiceItems(arrays, checkedItems, { _, position, checked ->
					checkedItems[position] = checked
				})
				.setPositiveButton(R.string.action_done, { _, _ ->
					val set = HashSet<String>()
					checkedItems.indices
							.filter { checkedItems[it] }
							.forEach { set.add(list[it].packageName!!) }
					settings.excludeList = set
					excludeList.summary = context.getString(R.string.summary_exclude_list, set.size)
				})
				.show()
	}
}