package com.janyo.janyoshare.classes

import java.io.Serializable

data class Error(val time: String, val appVersionName: String,
				 val appVersionCode: Int, val AndroidVersion: String,
				 val sdk: Int, val vendor: String, val model: String,
				 val ex: Throwable) : Serializable