package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.List;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.entities.Measurement;
import mo.edu.ipm.stud.envsensing.services.UploadService;

/**
 * A PreferenceFragment which list all settings.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener  {
    private OnDisplayDialogListener callback;
    private SharedPreferences preferences;
    private Preference prefBtMac;
    private Preference prefUsername;
    private Preference prefLogout;
    private Preference prefUploadCategory;
    private Preference prefStartUpload;
    private Preference prefResetUploadMark;

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
        prefBtMac = findPreference(getString(R.string.pref_bluetooth_mac));
        prefUsername = findPreference(getString(R.string.pref_user_name));
        prefLogout = findPreference(getString(R.string.pref_account_logout));
        prefUploadCategory = findPreference(getString(R.string.pref_upload_category));
        prefStartUpload = findPreference(getString(R.string.pref_start_upload));
        prefResetUploadMark = findPreference(getString(R.string.pref_reset_upload_mark));

        prefBtMac.setSummary(preferences.getString(getString(R.string.pref_bluetooth_mac),
                getString(R.string.press_to_select)));
        prefBtMac.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                callback.onDisplaySensorSelectionDialog();
                return true;
            }
        });

        prefUsername.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!isUserLoggedIn()) {
                    callback.onDisplayLoginDialog();
                    return true;
                }
                return false;
            }
        });

        prefLogout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor editor = preference.getEditor();
                editor.remove(getString(R.string.pref_user_token));
                editor.apply();
                return true;
            }
        });

        prefStartUpload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (UploadService.isRunning())
                    return false;
                Intent intent = new Intent(getActivity(), UploadService.class);
                intent.setAction(UploadService.ACTION_START);
                getActivity().startService(intent);
                return true;
            }
        });

        prefResetUploadMark.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                List<Measurement> measures = Measurement.listAll(Measurement.class);
                                for (Measurement measure : measures)
                                    measure.setUploaded(false);
                                Measurement.saveInTx(measures);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {
                                Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                        return true;
                    }
                });

        updateAccountStatus();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(prefBtMac.getKey())) {
            prefBtMac.setSummary(
                    sharedPreferences.getString(key, getString(R.string.press_to_select)));
        }
        if (key.equals(getString(R.string.pref_user_token))) {
            updateAccountStatus();
        }
    }

    private boolean isUserLoggedIn() {
        return preferences.getString(getString(R.string.pref_user_token), null) != null;
    }

    private void updateAccountStatus() {
        boolean loggedIn = isUserLoggedIn();
        prefLogout.setEnabled(loggedIn);
        prefUploadCategory.setEnabled(loggedIn);
        if (loggedIn) {
            prefUsername.setTitle(R.string.username);
            prefUsername.setSummary(preferences.getString(getString(R.string.pref_user_name), ""));
        } else {
            prefUsername.setTitle(R.string.not_logged_in);
            prefUsername.setSummary(R.string.click_to_login);
        }
    }


    public interface OnDisplayDialogListener {
        public void onDisplaySensorSelectionDialog();
        public void onDisplayLoginDialog();
    }

}
