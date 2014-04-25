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
import android.widget.Toast;

import com.clickatell.bullseye.library.Clickatell;
import com.clickatell.bullseye.library.User;

/**
 * This is the screen that manages the forgot password.
 * 
 */
public class ForgotScreen extends Activity {
	Clickatell click;
	EditText email, captcha_input;
	Button reset;
	ImageView captcha;
	String captcha_id = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		click = new Clickatell();
		User.set(getApplicationContext(), User.USERNAME, "");

		setContentView(R.layout.android_forgot_screen);

		email = (EditText) findViewById(R.id.email);
		reset = (Button) findViewById(R.id.reset);
		captcha = (ImageView) findViewById(R.id.captcha_image);
		captcha_input = (EditText) findViewById(R.id.captcha_code);
		redoCaptcha();

		reset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doReset();
			}
		});
	}

	/**
	 * Make a generic toast message.
	 * 
	 * @param msg
	 *            The message to display.
	 */
	private void makeZeToast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});

	}

	/**
	 * Do a password reset. On the current user.
	 */
	private void doReset() {
		if (User.isValidEmail(email.getText().toString())) {
			new Thread() {
				public void run() {
					NameValuePair[] s = click.resetPassword(email.getText()
							.toString(), captcha_id, captcha_input.getText()
							.toString());
					for (NameValuePair i : s) {
						if (i.getName().equalsIgnoreCase("Result")) {
							if (i.getValue().equalsIgnoreCase("Success")) {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										Intent login = new Intent(
												getApplicationContext(),
												LoginScreen.class);
										login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										login.putExtra("user_message",
												"A new password will be mailed to the email address.");
										startActivity(login);
									}
								});
							} else {
								makeZeToast("Please Try Again");
								redoCaptcha();
							}
						}
					}
				}
			}.start();
		} else {
			makeZeToast(getString(R.string.error_email_not_valid));
		}

	}

	/**
	 * This gets the new captcha, decodes it and displays it.
	 */
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
				} catch (IllegalArgumentException e) {
				}

			}
		}.start();
	}
}
