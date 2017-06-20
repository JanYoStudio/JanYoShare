package com.janyo.janyoshare;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
	private List<InstallApp> installAppList = new ArrayList<>();
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
					installAppList.clear();
					installAppList.addAll(installApps);
					AppRecyclerViewAdapter appRecyclerViewAdapter = new AppRecyclerViewAdapter(getActivity(),installAppList);
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				List<InstallApp> installAppList = new AppManager(getActivity()).getInstallAppList(type);
				Message message = new Message();
				message.obj = installAppList;
				message.what = 1;
				handler.sendMessage(message);
			}
		});

		View view = inflater.inflate(R.layout.fragment_app, container, false);
		recyclerView = view.findViewById(R.id.recycler_view);
		swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		swipeRefreshLayout.setRefreshing(true);
		thread.start();

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				thread.start();
			}
		});

		return view;
	}
}