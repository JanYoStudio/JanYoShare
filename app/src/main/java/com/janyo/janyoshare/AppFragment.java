package com.janyo.janyoshare;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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

import java.util.ArrayList;
import java.util.List;

public class AppFragment extends Fragment
{
	private RecyclerView recyclerView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private AppRecyclerViewAdapter appRecyclerViewAdapter;
	private List<InstallApp> installAppList = new ArrayList<>();
	private List<InstallApp> showList = new ArrayList<>();
	private AppManager appManager;
	private AppManager.AppType type;

	public static AppFragment newInstance(AppManager.AppType type)
	{
		Bundle bundle = new Bundle();
		bundle.putSerializable("type", type);
		AppFragment fragment = new AppFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler()
	{
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
					appRecyclerViewAdapter = new AppRecyclerViewAdapter(getActivity(), showList);
					recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
					recyclerView.setAdapter(appRecyclerViewAdapter);
					appRecyclerViewAdapter.notifyDataSetChanged();
					swipeRefreshLayout.setRefreshing(false);
					break;
			}
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		type = (AppManager.AppType) getArguments().getSerializable("type");
		appManager = new AppManager(getActivity());
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
				if (b)
				{
					action_clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
					action_settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
				} else
				{
					action_clear.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					action_settings.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
		recyclerView = view.findViewById(R.id.recycler_view);
		swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
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

	public void refresh()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				List<InstallApp> installAppList = appManager.getInstallAppList(type);
				Message message = new Message();
				message.obj = installAppList;
				message.what = 1;
				handler.sendMessage(message);
			}
		}).start();
	}
}