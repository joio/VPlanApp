package com.inf1315.vertretungsplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.inf1315.vertretungsplan.api.*;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

public class PlanActivity extends FragmentActivity implements ActionBar.TabListener
{

    PlanPagerAdapter planPagerAdapter;
    ViewPager viewPager;
    int ticker = 0;
    List<TickerObject> tickers = new ArrayList<TickerObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_plan);

	// Set up the action bar.
	final ActionBar actionBar = getActionBar();
	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	// Show the Up button in the action bar.
	actionBar.setDisplayHomeAsUpEnabled(true);

	// Create the adapter that will return a fragment for each of the three
	// primary sections of the app.
	planPagerAdapter = new PlanPagerAdapter(getSupportFragmentManager());

	// Set up the ViewPager with the sections adapter.
	viewPager = (ViewPager) findViewById(R.id.plan_pager);
	viewPager.setAdapter(planPagerAdapter);

	// When swiping between different sections, select the corresponding
	// tab. We can also use ActionBar.Tab#select() to do this if we have
	// a reference to the Tab.
	viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
	{
	    @Override
	    public void onPageSelected(int position)
	    {
		actionBar.setSelectedNavigationItem(position);
	    }
	});

	// For each of the sections in the app, add a tab to the action bar.
	for (int i = 0; i < planPagerAdapter.getCount(); i++)
	{
	    // Create a tab with text corresponding to the page title defined by
	    // the adapter. Also specify this Activity object, which implements
	    // the TabListener interface, as the callback (listener) for when
	    // this tab is selected.
	    actionBar.addTab(actionBar.newTab().setText(planPagerAdapter.getPageTitle(i)).setTabListener(this));
	}
	loadData();
    }

    private void loadData()
    {
	final List<AsyncTask<?, ?, ?>> tasks = new ArrayList<AsyncTask<?, ?, ?>>();
	try
	{
	    AsyncTask<Object, Object, TickerObject[]> tickersTask = new AsyncTask<Object, Object, TickerObject[]>()
	    {

		@Override
		protected TickerObject[] doInBackground(Object... params)
		{
		    try
		    {
			ApiResponse resp = API.STANDARD_API.request(ApiAction.TICKER);
			if(!resp.getSuccess())
			{
			    Log.w("Ticker Loader", "Ticker request unsuccesfull");
			    ticker = -1;
			    return new TickerObject[0];
			}
			ApiResultArray result = (ApiResultArray)resp.getResult();
			return (TickerObject[])result.getArray(new TickerObject[0]);
		    } catch (Exception e)
		    {
			e.printStackTrace();
			return new TickerObject[0];
		    }
		}

		@Override
		protected void onPostExecute(TickerObject[] result)
		{
		    tasks.remove(this);
		    if (tasks.isEmpty())
			finishedLoading();
		}
	    };
	    TickerObject[] toa = tickersTask.execute().get();
	    for(TickerObject to : toa)
		tickers.add(to);
	    Collections.sort(tickers);
	} catch (InterruptedException e)
	{
	    e.printStackTrace();
	} catch (ExecutionException e)
	{
	    e.printStackTrace();
	}
    }
    
    private void finishedLoading()
    {
	LoginActivity.loadingDialog.hide();
	showTicker();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	// Inflate the menu; this adds items to the action bar if it is present.
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.plan_activity_actions, menu);
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	switch (item.getItemId())
	{
	    case android.R.id.home:
		NavUtils.navigateUpFromSameTask(this);
		return true;
	    case R.id.action_show_ticker:
		showTicker();
		return true;
	}
	return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
	// When the given tab is selected, switch to the corresponding page in
	// the ViewPager.
	viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {}

    public void showTicker()
    {
	if (!tickers.isEmpty())
	{
	    for(TickerObject to : tickers)
	    {
		Toast toast = Toast.makeText(getApplicationContext(), to.toString(), Toast.LENGTH_LONG);
		toast.show();
	    }
	} else
	{
	    Toast toast = Toast.makeText(getApplicationContext(),
		ticker < 0 ? R.string.ticker_loading_error : R.string.no_ticker,
		ticker < 0 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
	    toast.show();
	}

    }

    public class PlanPagerAdapter extends FragmentPagerAdapter
    {

	public PlanPagerAdapter(FragmentManager fm)
	{
	    super(fm);
	}

	@Override
	public Fragment getItem(int position)
	{
	    Fragment fragment = new PlanFragment();
	    Bundle args = new Bundle();
	    boolean isTabToday = (position == 0) ? true : false;
	    args.putBoolean(PlanFragment.ARG_TODAY, isTabToday);
	    fragment.setArguments(args);
	    return fragment;
	}

	@Override
	public int getCount()
	{
	    // There are 2 Tabs
	    return 2;
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
	    switch (position)
	    {
		case 0:
		    return getResources().getString(R.string.today);
		case 1:
		    return getResources().getString(R.string.tomorrow);
	    }
	    return null;
	}
    }

}