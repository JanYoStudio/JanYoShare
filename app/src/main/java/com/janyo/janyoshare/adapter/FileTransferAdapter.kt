package com.janyo.janyoshare.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.mystery0.tools.Logs.Logs


class FileTransferAdapter(
		var list: List<TransferFile>) : RecyclerView.Adapter<FileTransferAdapter.ViewHolder>()
{
	private val TAG = "FileTransferAdapter"

	override fun onBindViewHolder(holder: ViewHolder, position: Int)
	{
		val transferFile = list[position]
		holder.progressBar.max = 100
		holder.progressBar.progress = 20
		holder.fileName.text = transferFile.fileName
		holder.filePath.text = transferFile.filePath
		holder.fileSize.text = transferFile.fileSize

		Thread(Runnable {
			for (i in 0..100)
			{
				try
				{
					Thread.sleep(100)
				}
				catch (e: InterruptedException)
				{
					e.printStackTrace()
				}
				holder.progressBar.progress = i
			}
		}).start()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
	{
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, RelativeLayout(parent.context), false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int
	{
		return list.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
	{
		var fileImg: ImageView = itemView.findViewById(R.id.fileImg)
		var fileName: TextView = itemView.findViewById(R.id.fileName)
		var filePath: TextView = itemView.findViewById(R.id.filePath)
		var fileSize: TextView = itemView.findViewById(R.id.fileSize)
		var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
	}
}