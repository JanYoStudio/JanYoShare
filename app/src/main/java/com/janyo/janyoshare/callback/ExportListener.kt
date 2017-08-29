package com.janyo.janyoshare.callback

import java.io.File
import java.util.ArrayList

interface ExportListener
{
	fun done(finish: Int, error: Int, fileList: ArrayList<File>)
}