package com.janyo.janyoshare.adapter

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.janyo.janyoshare.R
import com.janyo.janyoshare.classes.TransferFile
import com.janyo.janyoshare.util.FileTransferHelper
import com.janyo.janyoshare.util.JYFileUtil
import vip.mystery0.tools.fileUtil.FileUtil
import java.io.File
import java.io.Serializable

class FileTransferAdapter(val context: Context,
                          val list: List<TransferFile>) : RecyclerView.Adapter<FileTransferAdapter.ViewHolder>(), Serializable {
    private val option = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transferFile = list[position]
        holder.fileName.text = transferFile.fileName
        holder.fileSize.text = FileUtil.FormatFileSize(transferFile.fileSize)
        holder.progressBar.max = 100
        holder.progressBar.progress = transferFile.transferProgress
        var path = ""
        try {
            when (FileTransferHelper.getInstance().tag) {
                1 -> {
                    path = transferFile.filePath!!
                }
                2 -> {
                    path = JYFileUtil.getSaveFilePath(transferFile.fileName!!, "JY Share")
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, R.string.hint_file_transfer_path_error, Toast.LENGTH_SHORT)
                    .show()
            (context as Activity).finish()
        }
        holder.filePath.text = path
        if (transferFile.transferProgress == 100 || FileTransferHelper.getInstance().tag == 1)
            when (File(path).extension) {
                "png", "jpg", "jpeg" -> {
                    Glide.with(context)
                            .load(path)
                            .apply(option)
                            .into(holder.fileImg)
                }
                "apk" -> {
                    Glide.with(context)
                            .load(JYFileUtil.getApkIconPath(context, path))
                            .apply(option)
                            .into(holder.fileImg)
                }
                else -> {
                    holder.fileImg.setImageResource(R.mipmap.ic_file)
                }
            }
        else
            holder.fileImg.setImageResource(R.mipmap.ic_file)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileImg: ImageView = itemView.findViewById(R.id.fileImg)
        var fileName: TextView = itemView.findViewById(R.id.fileName)
        var filePath: TextView = itemView.findViewById(R.id.filePath)
        var fileSize: TextView = itemView.findViewById(R.id.fileSize)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }
}