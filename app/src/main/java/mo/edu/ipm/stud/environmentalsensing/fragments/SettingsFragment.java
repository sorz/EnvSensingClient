package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import mo.edu.ipm.stud.environmentalsensing.R;

/**
 * A PreferenceFragment which list all settings.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener  {
    private OnDisplayDialogListener callback;
    private SharedPreferences preferences;
    private Preference bluetoothMac;

    public interface OnDisplayDialogListener {
        public void onDisplaySensorSelectionDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OnDisplayDialogListener) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_section_settings);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);

        bluetoothMac = findPreference("pref_bluetooth_mac");
        bluetoothMac.setSummary(preferences.getString("pref_bluetooth_mac",
                getString(R.string.press_to_select)));
        bluetoothMac.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                callback.onDisplaySensorSelectionDialog();
                return true;
            }
        });

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(bluetoothMac.getKey())) {
            bluetoothMac.setSummary(
                    sharedPreferences.getString(key, getString(R.string.press_to_select)));
        }
    }

}
