package mo.edu.ipm.stud.environmentalsensing;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class MainActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener,
        Drawer.OnDrawerItemClickListener,
        Drawer.OnDrawerNavigationListener,
        SensorSelectionFragment.OnSensorSelectedListener,
        SettingsFragment.OnDisplayDialogListener {
    static final int SECTION_STATUS = 0;
    static final int SECTION_SETTINGS = 1;
    static final int SECTION_RECORDING = 2;

    private SharedPreferences preferences;
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().addOnBackStackChangedListener(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up the drawer.
        // Reference:
        // https://github.com/mikepenz/MaterialDrawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_status)
                                .withIdentifier(SECTION_STATUS),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_recording)
                                .withIdentifier(SECTION_RECORDING),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_settings)
                                .withIdentifier(SECTION_SETTINGS)
                )
                .withOnDrawerItemClickListener(this)
                .withOnDrawerNavigationListener(this)
                .build();

        if (preferences.getString("pref_bluetooth_mac", null) == null)
            drawer.setSelectionAtPosition(1);
        else
            drawer.setSelectionAtPosition(0);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        Fragment fragment;
        switch (drawerItem.getIdentifier()) {
            case SECTION_STATUS:
                fragment = new SensorStatusFragment();
                break;
            case SECTION_SETTINGS:
                fragment = new SettingsFragment();
                break;
            case SECTION_RECORDING:
                if (RecordService.isRunning())
                    fragment = new Fragment(); // TODO: RecordStatusFragment()
                else
                    fragment = new RecordConfigFragment();
                break;
            default:
                fragment = new Fragment();
                break;
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0)
                fragmentManager.popBackStack();
            else
                super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        // Display the homeAsUpIndicator if back stack is not empty.
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(
                getFragmentManager().getBackStackEntryCount() == 0);
    }

    @Override
    public boolean onNavigationClickListener(View view) {
        // Handle back button click, return last fragment or default operation (exit).
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public void onDisplaySensorSelectionDialog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SensorSelectionFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSensorSelected(String mac) {
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("pref_bluetooth_mac", mac);
        editor.apply();

        getFragmentManager().popBackStack();
    }

}
