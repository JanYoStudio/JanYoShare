package com.janyo.janyoshare.classes

import java.io.Serializable

/**
 * 请求头对象
 */
class TransferHeader:Serializable
{
	var list = ArrayList<TransferFile>()
}