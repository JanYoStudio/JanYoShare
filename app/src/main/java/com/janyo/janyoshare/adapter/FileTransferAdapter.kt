package com.janyo.janyoshare.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.mystery0.tools.FileUtil.FileUtil

class FileTransferAdapter(val context: Context,
						  val list: List<TransferFile>) : RecyclerView.Adapter<FileTransferAdapter.ViewHolder>()
{
	override fun onBindViewHolder(holder: ViewHolder, position: Int)
	{
		val transferFile = list[position]
		holder.fileName.text = transferFile.fileName
		holder.filePath.text = transferFile.fileUri
		holder.fileSize.text = FileUtil.FormatFileSize(transferFile.fileSize)
	}

	override fun getItemCount(): Int
	{
		return list.size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
	{
		val view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false)
		return ViewHolder(view)
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
	{
		var fileImg = itemView.findViewById<ImageView>(R.id.fileImg)
		var fileName = itemView.findViewById<TextView>(R.id.fileName)
		var filePath = itemView.findViewById<TextView>(R.id.filePath)
		var fileSize = itemView.findViewById<TextView>(R.id.fileSize)
		var progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
	}
}