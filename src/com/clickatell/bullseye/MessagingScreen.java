package com.clickatell.bullseye;

import java.util.HashSet;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.clickatell.bullseye.library.Clickatell;
import com.clickatell.bullseye.library.Contact;
import com.clickatell.bullseye.library.User;

public class MessagingScreen extends Fragment {

	Clickatell click;

	TextView counter, status, count_contacts;
	EditText message;
	Spinner groups;
	ImageButton send;
	int threads_start = 0;
	int threads_finished = 0;
	NumberPicker np_hours, np_minutes;
	HashSet<String> numbers = new HashSet<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		click = new Clickatell();
		View rootView = inflater.inflate(R.layout.activity_message_screen,
				container, false);
		counter = (TextView) rootView.findViewById(R.id.counter);
		count_contacts = (TextView) rootView.findViewById(R.id.count_contacts);
		status = (TextView) rootView.findViewById(R.id.status);
		message = (EditText) rootView.findViewById(R.id.message);
		groups = (Spinner) rootView.findViewById(R.id.contacts_group);
		np_hours = (NumberPicker) rootView.findViewById(R.id.np_hours);
		String hours[] = new String[] { "0 hours", "1 hour", "2 hours",
				"3 hours", "4 hours", "5 hours", "6 hours", "7 hours",
				"8 hours", "9 hours", "10 hours", "11 hours", "12 hours",
				"13 hours", "14 hours", "15 hours", "16 hours", "17 hours",
				"18 hours", "19 hours", "20 hours", "21 hours", "22 hours",
				"23 hours", "24 hours", "25 hours", "26 hours", "27 hours",
				"28 hours", "29 hours", "30 hours", "31 hours", "32 hours",
				"33 hours", "34 hours", "35 hours", "36 hours", "37 hours",
				"38 hours", "39 hours", "40 hours", "41 hours", "42 hours",
				"43 hours", "44 hours", "45 hours", "46 hours", "47 hours" };
		String minutes[] = new String[] { "0 minutes", "5 minutes",
				"10 minutes", "15 minutes", "20 minutes", "25 minutes",
				"30 minutes", "35 minutes", "40 minutes", "45 minutes",
				"50 minutes", "55 minutes" };
		np_hours.setDisplayedValues(hours);
		np_hours.setMinValue(0);
		np_hours.setMaxValue(47);
		np_hours.setWrapSelectorWheel(false);
		np_hours.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		np_minutes = (NumberPicker) rootView.findViewById(R.id.np_minutes);
		np_minutes.setDisplayedValues(minutes);
		np_minutes.setMinValue(0);
		np_minutes.setMaxValue(11);
		np_minutes.setWrapSelectorWheel(false);
		np_minutes
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		send = (ImageButton) rootView.findViewById(R.id.submit);
		counter.setText((160 - message.getText().length()) + " "
				+ getString(R.string.text_character_count));
		message.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				counter.setText((160 - s.length()) + " "
						+ getString(R.string.text_character_count));
				if (s.length() > 160) {
					message.setText(message.getText().subSequence(0, 160));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final String message_text = message.getText().toString();
				if (message_text.length() < 5) {
					status.setText("Your message must at least be more than 5 characters long.");
					return;
				}
				threads_finished = 0;
				threads_start = 0;
				message.setText("");
				status.setText("Sending Started");

				final int time_delay = np_hours.getValue() * 60
						+ np_minutes.getValue() * 5;
				new Thread(new Runnable() {

					@Override
					public void run() {
						String numbers_send = "";
						String numbers3 = "";
						String results = "";
						int count = 0;
						String arr[] = numbers.toArray(new String[0]);
						for (String s : arr) {
							if (count > 0) {
								numbers_send += ",";
							}
							numbers_send += s;
							numbers3 += "\n* " + s;
							count++;
							if (count > 50) {
								results += "\n";
								if (count == 1)
									results += numbers_send + " ";
								results += sendMessage(numbers_send, message_text,
										time_delay);
								numbers_send = "";
								count = 0;
							}
						}
						if (count > 0) {
							results += "\n";
							if (count == 1)
								results += numbers_send + " ";
							results += sendMessage(numbers_send, message_text, time_delay);
						}

						String email = "Numbers to send to: " + numbers3
								+ "\n\nMessage: " + message_text
								+ "\nMinutes Delayed: " + time_delay
								+ "\n\nAPI Result:\n" + results;
						Intent i = new Intent(Intent.ACTION_SEND);
						i.setType("message/rfc822");
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { User.get(getActivity(), User.USERNAME) });
						i.putExtra(Intent.EXTRA_SUBJECT,
								"BullsEye Results");
						i.putExtra(Intent.EXTRA_TEXT, email);
						try {
							startActivity(Intent.createChooser(i,
									"Send mail..."));
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(getActivity(),
									"There are no email clients installed.",
									Toast.LENGTH_SHORT).show();
						}

					}
				}).start();

			}
		});

		new Thread() {
			public void run() {
				final Contact[] contacts = User.getGroups(getActivity());
				if (getActivity() == null) {
					return;
				}
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						ArrayAdapter<Contact> spinnerArrayAdapter = new ArrayAdapter<Contact>(
								getActivity(),
								android.R.layout.simple_spinner_dropdown_item,
								contacts);
						groups.setAdapter(spinnerArrayAdapter);
						String s = User.get(getActivity(), User.DEFAULT_GROUP);
						try {
							int i = Integer.parseInt(s);
							groups.setSelection(i);
						} catch (NumberFormatException e) {

						}
					}
				});
			}
		}.start();

		groups.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Contact contact = (Contact) groups.getSelectedItem();

				User.set(getActivity(), User.DEFAULT_GROUP,
						"" + groups.getSelectedItemId());
				Contact[] contacts = User
						.getContacts(getActivity(), contact.id);

				numbers.clear();
				for (Contact c : contacts) {
					for (Contact n : c.numbers) {
						numbers.add(n.name);
					}
				}
				count_contacts.setText("Sending to " + numbers.size()
						+ " numbers.");
				send.setEnabled(true);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				send.setEnabled(false);
			}
		});
		send.setEnabled(false);

		return rootView;
	}

	private String sendMessage(final String numbers, final String text,
			final int offset) {
		threads_start++;
		String numbers2 = numbers.replaceAll("\\+", "");
		String s = "";
		if (User.get(getActivity(), User.TEST_MODE).equals("1")) {
			s = "I am in test mode";
		} else {
			s = click.sendMsg(User.get(getActivity(), User.API_ID),
					User.get(getActivity(), User.USERNAME),
					User.get(getActivity(), User.PASSWORD), numbers2, text,
					offset);
		}

		// System.out.println(s);
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				threads_finished++;
				Toast.makeText(
						getActivity(),
						"Sets Completed: " + threads_finished + "/"
								+ threads_start, Toast.LENGTH_LONG).show();
				status.setText("Sets Completed: " + threads_finished + "/"
						+ threads_start);
				if (threads_finished == threads_start) {
					status.setText("All Done");
				}
			}
		});
		return s;
	}
}