package com.clickatell.bullseye;

import java.io.UnsupportedEncodingException;

import org.apache.http.NameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clickatell.bullseye.library.Clickatell;
import com.clickatell.bullseye.library.User;

/**
 * This is the main activity that controls the initial login activity.
 *
 */
public class LoginScreen extends Activity {
	Clickatell click;

	EditText username, password, captcha_input;
	Button login, register, forgot;
	ImageView captcha;
	String captcha_id = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		click = new Clickatell();
		// Log the user out!!!
		User.set(getApplicationContext(), User.USERNAME, "");

		setContentView(R.layout.activity_login_screen);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		login = (Button) findViewById(R.id.submit);
		register = (Button) findViewById(R.id.register);
		forgot = (Button) findViewById(R.id.forgot);
		captcha = (ImageView) findViewById(R.id.captcha_image);
		captcha_input = (EditText) findViewById(R.id.captcha_text);
		redoCaptcha();
		
		// Check for a message...
		Bundle extras = getIntent().getExtras();
		TextView message = (TextView) findViewById(R.id.message);
		if (extras != null) {
			String value = extras.getString("user_message");
			message.setText(value);
		}

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doLogin();
			}
		});
		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent login = new Intent(getApplicationContext(),
						RegisterScreen.class);
				startActivity(login);
			}
		});
		forgot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent login = new Intent(getApplicationContext(),
						ForgotScreen.class);
				startActivity(login);
			}
		});
	}

	/**
	 * Do the initial login call.
	 */
	private void doLogin() {
		username.setEnabled(false);
		password.setEnabled(false);
		new Thread() {
			public void run() {
				String u = username.getText().toString();
				String p = password.getText().toString();
				if (!User.isValidEmail(u)) {
					makeZeToast(getString(R.string.error_email_not_valid));
					return;
				}
				switch (User.testValidPassword(p)) {
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
				final int r = click.connectAuth(u, p, captcha_id, captcha_input.getText().toString());
				if (r > -1) {
					final int a = click.createApi(u, p);
					if (a > -1) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								User.set(getApplicationContext(),
										User.USER_NUMBER, "" + r);
								User.set(getApplicationContext(),
										User.USERNAME, username.getText()
												.toString());
								User.set(getApplicationContext(),
										User.PASSWORD, password.getText()
												.toString());
								User.set(getApplicationContext(), User.API_ID,
										"" + a);
								Intent main = new Intent(
										getApplicationContext(),
										MainScreen.class);
								startActivity(main);
							}
						});
					}
				} else {
					redoCaptcha();
					doEmailRegistration();
				}

			}
		}.start();
	}

	private void doEmailRegistration() {
		String u = username.getText().toString();
		String p = password.getText().toString();
		final int r = click.resendEmailActivation(u, p);
		switch (r) {
		case Clickatell.STATUS_FAILURE:
			makeZeToast(getString(R.string.error_account_not_exist));
			break;
		case Clickatell.STATUS_SUCCESS:
			makeZeToast(getString(R.string.error_account_not_email_activate));
			break;
		}
	}

	private void makeZeToast(final String s) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG)
						.show();
				username.setEnabled(true);
				password.setEnabled(true);

			}
		});

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
					image = Clickatell.urlDecode(captcha_image.toCharArray());
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
}
