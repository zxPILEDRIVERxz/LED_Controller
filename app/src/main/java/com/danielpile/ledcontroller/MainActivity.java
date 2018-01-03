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

public class MainActivity extends AppCompatActivity {
    public TBlue blu;

    public TBlue getTBlue(){
        return this.blu;
    }
    String DEVICE_ADDRESS;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.pref_content, new DemoFragment())
                    .commit();
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        DEVICE_ADDRESS = sharedPref.getString(SettingsActivity.KEY_PREF_BLUETOOTH_ADDRESS, "");
        Toast.makeText(this, DEVICE_ADDRESS.toString(), Toast.LENGTH_SHORT).show();

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

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        new ConnectBlu().execute("");
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

    private class ConnectBlu extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(final String... args) {
            try {
                blu.close();
            } catch (Exception e) {
                Log.e("tag", "error", e);
            }
            try {
                if (TextUtils.isEmpty(DEVICE_ADDRESS)) {
                    Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Please select bluetooth device in settings", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else{
                    if (!blu.isConnected()) {
                        try {
                            String mac = "20:15:08:10:86:72";
                            boolean connected = blu.connect(DEVICE_ADDRESS);
                            if (connected) {
                                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Connected to " + mac, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }else{
                                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Failed to connect to " + mac, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Failed to connect", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Already Connected!", Snackbar.LENGTH_LONG)
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

            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
