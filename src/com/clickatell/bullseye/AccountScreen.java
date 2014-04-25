package com.clickatell.bullseye;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.clickatell.bullseye.library.Clickatell;
import com.clickatell.bullseye.library.User;

/**
 * This is the screen that shows your account information to the user. Not all
 * of this information is useful for the user but it was useful for us.
 * 
 */
public class AccountScreen extends Fragment {

	/**
	 * Access to the Clickatell Library.
	 */
	Clickatell click;

	/**
	 * UI access to the balance.
	 */
	TextView balance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Get Frame
		View rootView = inflater.inflate(R.layout.android_account_screen,
				container, false);
		click = new Clickatell();

		// Get all the UI components
		Button buy = (Button) rootView.findViewById(R.id.buy);
		balance = (TextView) rootView.findViewById(R.id.balance);
		TextView email_address = (TextView) rootView
				.findViewById(R.id.email_address);
		TextView api_id = (TextView) rootView.findViewById(R.id.api_id);

		// Set all the basics
		email_address.setText(User.get(getActivity(), User.USERNAME));
		api_id.setText(getString(R.string.text_api_id) + ": "
				+ User.get(getActivity(), User.API_ID));

		// Get the balance
		new Thread() {
			public void run() {
				final String bal = click.getBalance(
						User.get(getActivity(), User.USERNAME),
						User.get(getActivity(), User.PASSWORD),
						User.get(getActivity(), User.API_ID));
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						balance.setText(bal);
					}
				});
			}
		}.start();

		// Do the buy now url.
		buy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread() {
					public void run() {
						final String url = click.getBuyCreditUrl(
								User.get(getActivity(), User.USERNAME),
								User.get(getActivity(), User.PASSWORD));
						// If url is long enough
						if (url.length() > 5) {
							getActivity().runOnUiThread(new Runnable() {

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
			}
		});

		CheckBox cb = (CheckBox) rootView.findViewById(R.id.cb_test_mode);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				User.set(getActivity(), User.TEST_MODE, isChecked ? "1" : "0");
			}
		});
		cb.setChecked(User.get(getActivity(), User.TEST_MODE).equals("1"));

		return rootView;
	}
}