package com.janyo.janyoshare.util

import android.content.Context
import vip.mystery0.tools.logs.Logs
import java.net.NetworkInterface
import java.util.concurrent.Executors

class WIFIUtil(var context: Context, private val port: Int)
{
	private val TAG = "WIFIUtil"
	private var localAddress = ""//存储本机ip，例：本地ip ：192.168.1.1

	private fun getLocalAddress()
	{
		try
		{
			val en = NetworkInterface.getNetworkInterfaces()
			// 遍历所用的网络接口
			while (en.hasMoreElements())
			{
				val networks = en.nextElement()
				// 得到每一个网络接口绑定的所有ip
				val address = networks.inetAddresses
				// 遍历每一个接口绑定的所有ip
				while (address.hasMoreElements())
				{
					val ip = address.nextElement()
					if (!ip.isLoopbackAddress && isIpv4(ip.hostAddress))
					{
						localAddress = ip.hostAddress
					}
				}
			}
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	fun scanIP(scanListener: ScanListener)
	{
		getLocalAddress()
		Logs.i(TAG, "scanIP: " + localAddress)
		var isDeviceFind = false
		val localAddressIndex = localAddress.substring(0, localAddress.lastIndexOf(".") + 1)
		if (localAddressIndex == "")
		{
			Logs.e(TAG, "scanIP: 扫描失败")
			return
		}

		val cacheThreadPool = Executors.newCachedThreadPool()//创建线程池
		for (i in 0..255)
		{
			cacheThreadPool.submit {
				val currentIP = localAddressIndex + i
				if (currentIP == localAddress)
				{
					return@submit
				}
				val socketUtil = SocketUtil()
				try
				{
					if (socketUtil.tryCreateSocketConnection(currentIP, port))
					{
						isDeviceFind = true
						scanListener.onScan(currentIP, socketUtil)
					}
				}
				catch (e: Exception)
				{
					scanListener.onError(e)
				}
			}
		}
		cacheThreadPool.shutdown()
		while (true)
		{
			if (cacheThreadPool.isTerminated)
			{
				Logs.i(TAG, "scanIP: 所有线程已结束")
				scanListener.onFinish(isDeviceFind)
				break
			}
			Thread.sleep(100)
		}
	}

	private fun isIpv4(ipv4: String): Boolean
	{
		if (ipv4.isEmpty())
		{
			return false//字符串为空或者空串
		}
		val parts = ipv4.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()//因为java doc里已经说明, split的参数是reg, 即正则表达式, 如果用"|"分割, 则需使用"\\|"
		if (parts.size != 4)
		{
			return false//分割开的数组根本就不是4个数字
		}
		for (i in parts.indices)
		{
			try
			{
				val n = Integer.parseInt(parts[i])
				if (n < 0 || n > 255)
				{
					return false//数字不在正确范围内
				}
			}
			catch (e: NumberFormatException)
			{
				return false//转换数字不正确
			}

		}
		return true
	}

	interface ScanListener
	{
		fun onScan(ipv4: String, socketUtil: SocketUtil)
		fun onFinish(isDeviceFind: Boolean)
		fun onError(e: Exception)
	}
}