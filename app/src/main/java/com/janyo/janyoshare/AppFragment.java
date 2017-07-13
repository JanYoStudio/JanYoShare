package com.janyo.janyoshare;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.janyo.janyoshare.adapter.AppRecyclerViewAdapter;
import com.janyo.janyoshare.classes.InstallApp;
import com.janyo.janyoshare.util.AppManager;
import com.janyo.janyoshare.util.FileUtil;
import com.janyo.janyoshare.util.Settings;

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends Fragment
{
	private static final String TAG = "AppFragment";
	private SwipeRefreshLayout swipeRefreshLayout;
	private AppRecyclerViewAdapter appRecyclerViewAdapter;
	private List<InstallApp> installAppList = new ArrayList<>();
	private List<InstallApp> showList = new ArrayList<>();
	private AppManager appManager;
	private AppManager.AppType type;
	private Settings settings;
	private int index = 0;
	private MyHandler handler;

	public AppFragment()
	{
	}

	public static AppFragment newInstance(AppManager.AppType type)
	{
		Bundle bundle = new Bundle();
		bundle.putSerializable("type", type);
		AppFragment fragment = new AppFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		type = (AppManager.AppType) getArguments().getSerializable("type");
		settings = new Settings(getActivity());
		appManager = new AppManager(getActivity());
		index = settings.getSort();
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_main, menu);
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean b)
			{
				MenuItem action_clear = menu.findItem(R.id.action_clear);
				MenuItem action_settings = menu.findItem(R.id.action_settings);
				MenuItem action_sort = menu.findItem(R.id.action_sort);
				if (b)
				{
					action_clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
					action_settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
					action_sort.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				} else
				{
					action_clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					action_settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
					action_sort.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				}
			}
		});
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				showList.clear();
				if (query.length() > 0)
				{
					List<InstallApp> searchList = appManager.searchApps(installAppList, query);
					showList.addAll(searchList);
				} else
				{
					showList.addAll(installAppList);
				}
				appRecyclerViewAdapter.notifyDataSetChanged();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				showList.clear();
				if (newText.length() > 0)
				{
					List<InstallApp> searchList = appManager.searchApps(installAppList, newText);
					showList.addAll(searchList);
				} else
				{
					showList.addAll(installAppList);
				}
				appRecyclerViewAdapter.notifyDataSetChanged();
				return false;
			}
		});
	}

	@SuppressLint("InflateParams")
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_clear:
				Snackbar.make(getActivity().findViewById(R.id.coordinatorLayout), "文件清除" + (FileUtil.cleanFileDir(getString(R.string.app_name)) ? "成功" : "失败") + "！", Snackbar.LENGTH_SHORT)
						.show();
				break;
			case R.id.action_sort:
				index = settings.getSort();
				new AlertDialog.Builder(getActivity())
						.setTitle("请选择排序方式")
						.setSingleChoiceItems(R.array.sort, index, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								index = i;
							}
						})
						.setPositiveButton("确定", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								settings.setSort(index);
								swipeRefreshLayout.setRefreshing(true);
								refresh();
							}
						})
						.show();
				break;
			case R.id.action_settings:
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_app, container, false);
		RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
		swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		appRecyclerViewAdapter = new AppRecyclerViewAdapter(getActivity(), showList);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.setAdapter(appRecyclerViewAdapter);
		handler = new MyHandler(showList, installAppList, appRecyclerViewAdapter, swipeRefreshLayout);

		swipeRefreshLayout.setRefreshing(true);
		refresh();

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				refresh();
			}
		});
		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		index = settings.getSort();
		swipeRefreshLayout.setRefreshing(true);
		refresh();
	}

	public void refresh()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				List<InstallApp> installAppList = appManager.getInstallAppList(type, index);
				Message message = new Message();
				message.obj = installAppList;
				message.what = 1;
				handler.sendMessage(message);
			}
		}).start();
	}
}

class MyHandler extends Handler
{
	private List<InstallApp> showList;
	private List<InstallApp> installAppList;
	private AppRecyclerViewAdapter appRecyclerViewAdapter;
	private SwipeRefreshLayout swipeRefreshLayout;

	MyHandler(List<InstallApp> showList, List<InstallApp> installAppList, AppRecyclerViewAdapter appRecyclerViewAdapter, SwipeRefreshLayout swipeRefreshLayout)
	{
		this.showList = showList;
		this.installAppList = installAppList;
		this.appRecyclerViewAdapter = appRecyclerViewAdapter;
		this.swipeRefreshLayout = swipeRefreshLayout;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message message)
	{
		switch (message.what)
		{
			case 1:
				List<InstallApp> installApps = (List<InstallApp>) message.obj;
				showList.clear();
				showList.addAll(installApps);
				installAppList.clear();
				installAppList.addAll(installApps);
				appRecyclerViewAdapter.notifyDataSetChanged();
				swipeRefreshLayout.setRefreshing(false);
				break;
		}
	}
}