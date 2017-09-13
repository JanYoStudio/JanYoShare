package com.janyo.janyoshare.`interface`

import java.io.File
import java.util.ArrayList

interface ExportListener
{
	fun done(finish: Int, error: Int, fileList: ArrayList<File>)
}