package com.janyo.janyoshare.fragment

import android.os.Bundle
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.janyo.janyoshare.R
import com.janyo.janyoshare.activity.MainActivity

import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter
import com.janyo.janyoshare.`interface`.ExportListener
import com.janyo.janyoshare.classes.InstallApp
import com.janyo.janyoshare.handler.ExportHandler
import com.janyo.janyoshare.handler.LoadHandler
import com.janyo.janyoshare.handler.SearchHandler
import com.janyo.janyoshare.util.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_custom_name_format.*
import vip.mystery0.tools.logs.Logs
import java.io.File

import java.util.ArrayList
import java.util.concurrent.Executors

class AppFragment : Fragment() {

    companion object {
        private val TAG = "AppFragment"
        fun newInstance(type: Int): AppFragment {
            val bundle = Bundle()
            bundle.putInt("type", type)
            val fragment = AppFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var loadHandler: LoadHandler
    private lateinit var searchHandler: SearchHandler
    //    private val singleThreadPool = Executors.newSingleThreadExecutor()
    private var type = -1
    private var index = 0
    private var isReadyTag = false
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var appRecyclerViewAdapter: AppRecyclerViewAdapter
    lateinit var exportHandler: ExportHandler
    private val installAppList = ArrayList<InstallApp>()
    private val showList = ArrayList<InstallApp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments.getInt("type")
        Logs.i(TAG, "onCreate: 创建app的fragment" + type)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Logs.i(TAG, "onCreateView: 创建视图")
        coordinatorLayout = activity.findViewById(R.id.coordinatorLayout)
        val view = inflater.inflate(R.layout.fragment_app, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

        appRecyclerViewAdapter = AppRecyclerViewAdapter(activity, showList)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = appRecyclerViewAdapter
        loadHandler = LoadHandler(showList, installAppList, appRecyclerViewAdapter, swipeRefreshLayout)
        searchHandler = SearchHandler(appRecyclerViewAdapter)
        exportHandler = ExportHandler(appRecyclerViewAdapter, activity)
        swipeRefreshLayout.setOnRefreshListener { refresh() }
        isReadyTag = true
        return view
    }

    fun clearSelected(activity: MainActivity) {
        singleThreadPool.execute {
            while (true) {
                if (isReadyTag)
                    break
                Logs.i(TAG, "clearSelected: 等待初始化")
                Thread.sleep(200)
            }
            if (appRecyclerViewAdapter.multiChoiceList.size != 0) {
                appRecyclerViewAdapter.multiChoiceList.clear()
                activity.invalidateOptionsMenu()
            }
        }
    }

    fun search(query: String) {
        singleThreadPool.execute {
            while (true) {
                if (isReadyTag)
                    break
                Logs.i(TAG, "clearSelected: 等待初始化")
                Thread.sleep(200)
            }
            showList.clear()
            if (query.isNotEmpty()) {
                val searchList = AppManager.searchApps(installAppList, query)
                showList.addAll(searchList)
            } else {
                showList.addAll(installAppList)
            }
            searchHandler.sendEmptyMessage(0)
        }
    }

    fun exportAPK(list: List<InstallApp>, listener: ExportListener) {
        Logs.i(TAG, "exportAPK: " + list.size)
        Thread(Runnable {
            val fileList = ArrayList<File>()
            val cacheThreadPool = Executors.newCachedThreadPool()
            var finish = 0
            var error = 0
            list.forEach {
                cacheThreadPool.submit {
                    val code: Int = if (Settings.customFileName.format == "")
                        JYFileUtil.fileToSD(it.sourceDir!!, it, getString(R.string.app_name), "apk")
                    else
                        JYFileUtil.fileToSD(it.sourceDir!!, Settings.customFileName, it, getString(R.string.app_name), "apk")
                    if (code == -1) {
                        Toast.makeText(activity, getString(R.string.hint_copy_error_with_name, it.name), Toast.LENGTH_SHORT)
                                .show()
                        error++
                    } else {
                        if (Settings.customFileName.format == "")
                            fileList.add(JYFileUtil.getFilePath(it, context.getString(R.string.app_name), "apk"))
                        else
                            fileList.add(JYFileUtil.getFilePath(Settings.customFileName, it, context.getString(R.string.app_name), "apk"))
                        finish++
                    }
                }
            }
            cacheThreadPool.shutdown()
            while (true) {
                if (cacheThreadPool.isTerminated) {
                    listener.done(finish, error, fileList)
                    break
                }
                Thread.sleep(100)
            }
        }).start()
    }

    fun refreshList() {
        Observable.create<Boolean> { subscriber ->
            while (true) {
                if (isReadyTag)
                    break
                Logs.i(TAG, "refreshList: 等待初始化")
                Thread.sleep(200)
            }
            index = Settings.sort
            subscriber.onComplete()
        }
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :Observer<Boolean>{
                    override fun onSubscribe(d: Disposable) {
                        swipeRefreshLayout.isRefreshing=true
                    }

                    override fun onComplete() {
                        if (Settings.savedSort == Settings.sort && JYFileUtil.isCacheAvailable(activity)) {
                            getCatchList()
                        } else {
                            refresh()
                        }
                    }

                    override fun onNext(t: Boolean) {
                    }

                    override fun onError(e: Throwable) {
                    }
                })
    }

    private fun getCatchList() {
        Observable.create<Boolean> {
            var fileName = ""
            when (type) {
                AppManager.SYSTEM -> fileName = "system.list"
                AppManager.USER -> fileName = "user.list"
            }
            val installAppList = JYFileUtil.getList(context, fileName)
            if (installAppList != null) {
                val message = Message()
                message.obj = installAppList
                message.what = LoadHandler.REFRESH_DONE
                loadHandler.sendMessage(message)
            } else {
                refresh()
            }
        }
    }

    private fun refresh() {
        Observable.create<Boolean> { subscriber ->
            index = Settings.sort
            val list = AppManager.getInstallAppList(activity, type, index, true)
            showList.clear()
            showList.addAll(list)
            installAppList.clear()
            installAppList.addAll(list)
            when (type) {
                AppManager.SYSTEM -> JYFileUtil.saveList(activity, list, "system.list")
                AppManager.USER -> JYFileUtil.saveList(activity, list, "user.list")
            }
            Settings.savedSort = index
            subscriber.onComplete()
        }
                .subscribeOn(Schedulers.newThread())
                .unsubscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onError(e: Throwable) {
                    }

                    override fun onNext(t: Boolean) {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {
                        appRecyclerViewAdapter.notifyDataSetChanged()
                        swipeRefreshLayout.isRefreshing = false
                    }
                })
    }
}
