package com.janyo.janyoshare.classes

class CustomFormat(var format: String)
{
	fun toFormat(installApp: InstallApp): String
	{
		var fileName = ""
		var index = 0
		while (index < format.length)
		{
			if (format[index] == '%' && index + 1 < format.length)
			{
				when (format[index + 1])
				{
					'N' ->
					{
						fileName += installApp.name
						index += 2
					}
					'V' ->
					{
						fileName += installApp.versionName
						index += 2
					}
					'W' ->
					{
						fileName += installApp.versionCode
						index += 2
					}
					'P' ->
					{
						fileName += installApp.packageName
						index += 2
					}
					else ->
					{
						fileName += '%'
						index++
					}
				}
			}
			else
			{
				fileName += format[index]
				index++
			}
		}
		return fileName
	}
}