package com.janyo.janyoshare.classes

import java.io.Serializable

class TransferFile : Serializable
{
	var filePath: String? = null
	var fileName: String? = null
	var fileUri: String? = null
	var fileSize: Long = 0
	var transferProgress: Int = 0
}