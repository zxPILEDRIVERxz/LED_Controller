package com.danielpile.ledcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.jaredrummler.android.colorpicker.ColorPreference;

public class DemoFragment extends PreferenceFragment {

  private static final String TAG = "DemoFragment";

  private static final String KEY_DEFAULT_COLOR = "default_color";
    private static final String KEY_TRANSITION_MODE = "transition_mode";
    private static final String KEY_SCENE_MODE = "scene_mode";

  long starttime = 0L;
  long elapsedtime = 0L;
  MainActivity mainActivity;

    @Override
    public void onResume() {
        super.onResume();
        mainActivity = ((MainActivity)this.getActivity());
    }

    @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.main);

    final ListPreference listPreference = (ListPreference) findPreference(KEY_SCENE_MODE);
    listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(KEY_SCENE_MODE.equals(preference.getKey())){
                Log.i("Preferences", newValue.toString());
                if (newValue.equals("3")){
                    Log.i("Preferences","Demo Mode");
                    String command = newValue.toString();
                    mainActivity.sendCommmand(command);
                } else if(newValue.equals("4")){
                    Log.i("Preferences","Breathe");
                    String command = newValue.toString();
                    mainActivity.sendCommmand(command);
                }else if(newValue.equals("0")){
                    Log.i("Preferences","Static Mode");
                }
            }
            return true;
        }
    });

    // Example showing how we can get the new color when it is changed:
    final ColorPreference colorPreference = (ColorPreference) findPreference(KEY_DEFAULT_COLOR);
    colorPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_DEFAULT_COLOR.equals(preference.getKey())) {

            Integer color = (int) newValue;
            elapsedtime = SystemClock.uptimeMillis() - starttime;
            Log.i("ColorSeekBar", "color:" + color);
            Log.i("ColorSeekBar", "ElapsedTime:" + elapsedtime);
            if (elapsedtime < 95) {
                Log.i("ColorSeekBar", "RateLimiting:" + elapsedtime);
                return false;
            }
            starttime = SystemClock.uptimeMillis();
            String r = String.valueOf(Color.red(color));
            String g = String.valueOf(Color.green(color));
            String b = String.valueOf(Color.blue(color));
            ListPreference ListPref = (ListPreference) findPreference("transition_mode");
            Log.i("Preferences", ListPref.getValue());
            try {
                String command = ListPref.getValue() + "," + r + "," + g + "," + b;
                //blu.write(command + "\n");
                mainActivity.sendCommmand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ListPreference listPreference = (ListPreference) findPreference(KEY_SCENE_MODE);
            listPreference.setValueIndex(0);
        }
        return true;
      }
    });
  }
}
