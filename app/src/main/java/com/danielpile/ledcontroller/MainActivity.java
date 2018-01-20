package com.danielpile.ledcontroller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    public TBlue blu;
    final String MY_HC06 = "20:15:08:10:86:72";
    String DEVICE_ADDRESS;
    Boolean DEVICE_PREF_RESUME_STARTUP;
    String DEVICE_PREF_BRIGHTNESS;
    String DEVICE_PREF_COLOR_DELAY;

    @Override
    protected void onPause() {
        super.onPause();
        try {
            blu.close();
        } catch (Exception e) {
            Log.e("tag", "error", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.pref_content, new DemoFragment())
                    .commit();
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        blu = new TBlue(this);

    }

    public void sendCommmand(String command) {
        try {
            //String command = mode + "," + red + "," + green + "," + blue;
            blu.write(command + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeBlu() {
        try {
            int resume_startup = (DEVICE_PREF_RESUME_STARTUP) ? 1 : 0;
            String command = 25 + "," + resume_startup + "\n";
            Log.d("Derp",command);
            blu.write(command);
            int brightness = Integer.valueOf(DEVICE_PREF_BRIGHTNESS);
            command = 26 + "," + brightness + "\n";
            Log.d("Derp",command);
            blu.write(command);
            int delay = Integer.valueOf(DEVICE_PREF_COLOR_DELAY);
            command = 27 + "," + delay + "\n";
            Log.d("Derp",command);
            blu.write(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        DEVICE_ADDRESS = sharedPref.getString(SettingsActivity.KEY_PREF_BLUETOOTH_ADDRESS, "");
        DEVICE_PREF_RESUME_STARTUP = sharedPref.getBoolean(SettingsActivity.KEY_PREF_RESUME_STARTUP, true);
        DEVICE_PREF_BRIGHTNESS = sharedPref.getString(SettingsActivity.KEY_PREF_BRIGHTNESS, "");
        DEVICE_PREF_COLOR_DELAY = sharedPref.getString(SettingsActivity.KEY_PREF_COLOR_DELAY, "");
        Log.d("Preferences","BRIGHTNESS: " + DEVICE_PREF_BRIGHTNESS);
        Log.d("Preferences","DELAY: " + DEVICE_PREF_COLOR_DELAY);
        new ConnectBlu(this).execute("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static class ConnectBlu extends AsyncTask<String, Void, Boolean> {
        private WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        ConnectBlu(MainActivity context) {
            activityReference = new WeakReference<>(context);
            dialog = new ProgressDialog(activityReference.get());
        }

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            dialog.setMessage("Please wait");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(final String... args) {
            try {
                activityReference.get().blu.close();
            } catch (Exception e) {
                Log.e("tag", "error", e);
            }
            try {
                if (TextUtils.isEmpty(activityReference.get().DEVICE_ADDRESS)) {
                    Snackbar.make(activityReference.get().findViewById(R.id.myCoordinatorLayout), "Please select bluetooth device in settings", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else{
                    if (!activityReference.get().blu.isConnected()) {
                        try {
                            boolean connected = activityReference.get().blu.connect(activityReference.get().DEVICE_ADDRESS);
                            if (connected) {
                                Snackbar.make(activityReference.get().findViewById(R.id.myCoordinatorLayout), "Connected to " + activityReference.get().blu.address, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }else{
                                Snackbar.make(activityReference.get().findViewById(R.id.myCoordinatorLayout), "Failed to connect to " + activityReference.get().blu.address, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Snackbar.make(activityReference.get().findViewById(R.id.myCoordinatorLayout), "Failed to connect", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        Snackbar.make(activityReference.get().findViewById(R.id.myCoordinatorLayout), "Already Connected!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }

                return true;
            } catch (Exception e) {
                Log.e("tag", "error", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            activityReference.get().initializeBlu();

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
