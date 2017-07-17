@file:Suppress("DEPRECATION")

package com.janyo.janyoshare.adapter

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView

import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.util.FileUtil

class AppRecyclerViewAdapter(private val context: Context,
							 private val installAppList: List<InstallApp>) : RecyclerView.Adapter<AppRecyclerViewAdapter.ViewHolder>()
{
	var installApp: InstallApp? = null

	override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
		return ViewHolder(context as Activity, view)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int)
	{
		val installApp = installAppList[position]
		holder.textView_name.text = installApp.name
		holder.textView_packageName.text = installApp.packageName
		holder.textView_versionName.text = installApp.versionName
		holder.imageView.setImageDrawable(installApp.icon)
		holder.textView_size.text = FileUtil.FormatFileSize(installApp.size)
		holder.fullView.setOnLongClickListener {
			this.installApp = installApp
			false
		}
	}

	override fun getItemCount(): Int
	{
		return installAppList.size
	}

	class ViewHolder(var fullView: View) : RecyclerView.ViewHolder(fullView)
	{
		var imageView: ImageView = fullView.findViewById<ImageView>(R.id.app_icon)
		var textView_name: TextView = fullView.findViewById<TextView>(R.id.app_name)
		var textView_packageName: TextView = fullView.findViewById<TextView>(R.id.app_package_name)
		var textView_versionName: TextView = fullView.findViewById<TextView>(R.id.app_version_name)
		var textView_size: TextView = fullView.findViewById<TextView>(R.id.app_size)

		constructor(activity: Activity, fullView: View) : this(fullView)
		{
			fullView.setOnCreateContextMenuListener { contextMenu, _, _ ->
				activity.menuInflater.inflate(R.menu.menu_context, contextMenu)
			}
		}
	}
}
