package com.clickatell.bullseye;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.clickatell.bullseye.library.Clickatell;
import com.clickatell.bullseye.library.CountryItem;
import com.clickatell.bullseye.library.User;

public class RegisterScreen extends Activity {

	Clickatell click = new Clickatell();
	Spinner spinner;
	EditText firstname, surname, mobile, email, password, coupon,
			captcha_input;
	CheckBox terms;
	Button submit;
	ImageView captcha;
	String captcha_id = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register_screen);

		spinner = (Spinner) findViewById(R.id.country);
		firstname = (EditText) findViewById(R.id.firstname);
		surname = (EditText) findViewById(R.id.surname);
		mobile = (EditText) findViewById(R.id.mobile);
		email = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		coupon = (EditText) findViewById(R.id.coupon);
		terms = (CheckBox) findViewById(R.id.terms);
		submit = (Button) findViewById(R.id.button_submit);
		terms.setMovementMethod(LinkMovementMethod.getInstance());
		captcha = (ImageView) findViewById(R.id.captcha_image);
		captcha_input = (EditText) findViewById(R.id.captcha_text);
		redoCaptcha();

		new Thread() {
			@Override
			public void run() {
				final ArrayList<CountryItem> country_list = click
						.getCountries();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						ArrayAdapter<CountryItem> spinnerArrayAdapter = new ArrayAdapter<CountryItem>(
								getApplicationContext(),
								android.R.layout.simple_spinner_dropdown_item,
								country_list);
						spinner.setAdapter(spinnerArrayAdapter);
					}
				});

			}
		}.start();
		TelephonyManager tMgr = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		mobile.setText(tMgr.getLine1Number());

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doRegister();
			}
		});
	}

	static final String HEX_DIGITS = "0123456789ABCDEF";

	protected static byte[] urlDecode(char[] cs)
			throws UnsupportedEncodingException, IllegalArgumentException {
		if (cs == null) {
			return null;
		}

		byte[] decodeBytes = new byte[cs.length];
		int decodedByteCount = 0;

		try {
			for (int count = 0; count < cs.length; count++) {
				switch (cs[count]) {
				case '+':
					decodeBytes[decodedByteCount++] = (byte) ' ';
					break;

				case '%':
					decodeBytes[decodedByteCount++] = (byte) ((HEX_DIGITS
							.indexOf(cs[++count]) << 4) + (HEX_DIGITS
							.indexOf(cs[++count])));

					break;

				default:
					decodeBytes[decodedByteCount++] = (byte) cs[count];
				}
			}

		} catch (IndexOutOfBoundsException ae) {
			throw new IllegalArgumentException("Malformed encoding");
		}

		return decodeBytes;
	}

	private void doRegister() {
		if (!terms.isChecked()) {
			makeZeToast(getString(R.string.error_terms_not_selected));
			return;
		}
		if (mobile.getText().length() < 5) {
			makeZeToast(getString(R.string.error_number_too_short));
			return;
		}
		if (!User.isValidEmail(email.getText().toString())) {
			makeZeToast(getString(R.string.error_email_not_valid));
			return;
		}
		switch (User.testValidPassword(password.getText().toString())) {
		case -1:
			break;
		case 0:
		case 1:
			makeZeToast(getString(R.string.error_password_too_short));
			return;
		case 2:
			makeZeToast(getString(R.string.error_password_needs_number));
			return;
		case 3:
			makeZeToast(getString(R.string.error_password_needs_letter));
			return;
		default:
		}
		if (firstname.getText().length() < 3) {
			makeZeToast(getString(R.string.error_firstname_tooshort));
			return;
		}
		if (surname.getText().length() < 3) {
			makeZeToast(getString(R.string.error_surname_tooshort));
			return;
		}

		new Thread() {
			@Override
			public void run() {
				NameValuePair[] ret = click.register(firstname.getText()
						.toString(), surname.getText().toString(), mobile
						.getText().toString(), email.getText().toString(),
						password.getText().toString(), "",
						((CountryItem) spinner.getSelectedItem()).id, coupon
								.getText().toString(), captcha_id,
						captcha_input.getText().toString());

				for (NameValuePair s : ret) {
					if (s.getName().equalsIgnoreCase("Result")
							&& s.getValue().equalsIgnoreCase("Success")) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Intent login = new Intent(
										getApplicationContext(),
										LoginScreen.class);
								login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								login.putExtra(
										"user_message",
										"Registration Complete.\nAn email has been sent to "
												+ email.getText().toString()
												+ "\nPlease email activate, there after login to your account.");
								startActivity(login);
								finish();
							}
						});
						return;
					} else if (s.getName().equalsIgnoreCase("Description")) {
						final String st = s.getValue();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast t = Toast.makeText(
										getApplicationContext(), st,
										Toast.LENGTH_LONG);
								t.setGravity(Gravity.CENTER, 0, 0);
								t.show();
							}
						});
						redoCaptcha();
					}
				}
			}
		}.start();

	}

	private void redoCaptcha() {

		new Thread() {
			@Override
			public void run() {
				String captcha_image = "";
				NameValuePair[] s = click.getCaptcha();
				for (NameValuePair i : s) {
					if (i.getName().equalsIgnoreCase("captcha_id")) {
						captcha_id = i.getValue();
					} else if (i.getName().equalsIgnoreCase("captcha_image")) {
						captcha_image = i.getValue();
					}
				}
				byte[] image;
				try {
					image = urlDecode(captcha_image.toCharArray());
					final Bitmap bitmap = BitmapFactory.decodeByteArray(image,
							0, image.length);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							captcha.setImageBitmap(bitmap);
						}
					});
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}

			}
		}.start();
	}

	private void makeZeToast(final String s) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast t = Toast.makeText(getApplicationContext(), s,
						Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();

			}
		});

	}
}