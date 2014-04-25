package com.clickatell.bullseye;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clickatell.bullseye.library.Clickatell;
import com.clickatell.bullseye.library.User;

public class MainScreen extends Activity {
	Clickatell click;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] options;
	
	private TextView email,balance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		click = new Clickatell();
		if (!User.isLoggedIn(getApplicationContext())) {
	        Intent login = new Intent(getApplicationContext(), LoginScreen.class);
	        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(login);
	        finish();
	        return;
		}
		setContentView(R.layout.activity_main_screen);

		mTitle = mDrawerTitle = getTitle();
		options = getResources().getStringArray(R.array.options);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		
		email = new TextView(getApplication());
		balance = new TextView(getApplication());
		LinearLayout ll = new LinearLayout(getApplication());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(email);
		ll.addView(balance);
		mDrawerList.addHeaderView(ll);
		email.setText(User.get(getApplication(), User.USERNAME));

		// Get the balance
		new Thread() {
			public void run() {
				final String bal = click.getBalance(
						User.get(getApplication(), User.USERNAME),
						User.get(getApplication(), User.PASSWORD),
						User.get(getApplication(), User.API_ID));
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						balance.setText(bal);
					}
				});
			}
		}.start();

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, options));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.action_open,
				R.string.action_close) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_screen, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_logout).setVisible(drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.action_logout:
			User.set(getApplicationContext(), User.USERNAME, "");
			User.set(getApplicationContext(), User.API_ID, "");
			User.set(getApplicationContext(), User.USER_NUMBER, "");
			User.set(getApplicationContext(), User.PASSWORD, "");
	        Intent login = new Intent(getApplicationContext(), LoginScreen.class);
	        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        startActivity(login);
	        finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		String title = options[0];
		Fragment fragment = null;
		switch(position) {
		case 0:
		case 2:
			title = options[1];
			fragment = new AccountScreen();
			break;
		case 3:
			title = options[2];
			Intent i = new Intent();
		    i.setComponent(new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity"));
		    i.setAction("android.intent.action.MAIN");
		    i.addCategory("android.intent.category.LAUNCHER");
		    i.addCategory("android.intent.category.DEFAULT");
		    startActivity(i);
		    return;
		case 4:
			title = options[3];
			new Thread() {
				public void run() {
					final String url = click.getBuyCreditUrl(
							User.get(getApplicationContext(), User.USERNAME),
							User.get(getApplicationContext(), User.PASSWORD));
					if (url.length() > 5) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Intent browserIntent = new Intent(
										Intent.ACTION_VIEW, Uri.parse(url));
								startActivity(browserIntent);
							}
						});
					}
				}
			}.start();
			return;
			default:
				fragment = new MessagingScreen();
		}
		//mDrawerList.setItemChecked(position, true);
		setTitle(title);
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();

		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

}