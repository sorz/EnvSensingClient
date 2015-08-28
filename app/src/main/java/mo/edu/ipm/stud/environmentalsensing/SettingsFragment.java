package mo.edu.ipm.stud.environmentalsensing;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by xierch on 2015/8/29.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }



}
