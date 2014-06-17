package org.mortar.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static enum Cmd {
		EXIT
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final MainActivity thiz = MainActivity.this;
		Intent intent = getIntent();
		if (intent.getIntExtra(Cmd.class.getSimpleName(), -1) == Cmd.EXIT.ordinal()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Mortar Client");
			builder.setMessage("Exit?");
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Toast.makeText(getApplicationContext(), "MortarClient: exit", Toast.LENGTH_SHORT).show();
					stopService(new Intent(thiz, ListenerService.class));
					finish();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.create().show();
		} else {
			Toast.makeText(getApplicationContext(), "MortarClient: started", Toast.LENGTH_SHORT).show();
			startService(new Intent(this, ListenerService.class));
			finish();
		}
	}
}
