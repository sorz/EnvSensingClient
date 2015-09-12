package mo.edu.ipm.stud.environmentalsensing;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import mo.edu.ipm.stud.environmentalsensing.fragments.ExportDataFragment;
import mo.edu.ipm.stud.environmentalsensing.fragments.RawDataViewerFragment;
import mo.edu.ipm.stud.environmentalsensing.fragments.RecordConfigFragment;
import mo.edu.ipm.stud.environmentalsensing.fragments.RecordStatusFragment;
import mo.edu.ipm.stud.environmentalsensing.fragments.SensorSelectionFragment;
import mo.edu.ipm.stud.environmentalsensing.fragments.SensorStatusFragment;
import mo.edu.ipm.stud.environmentalsensing.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener,
        Drawer.OnDrawerItemClickListener,
        Drawer.OnDrawerNavigationListener,
        SensorSelectionFragment.OnSensorSelectedListener,
        SettingsFragment.OnDisplayDialogListener,
        RecordConfigFragment.OnRecordingStartedListener,
        RecordStatusFragment.OnRecordingStoppedListener,
        RawDataViewerFragment.OnExportDataListener,
        ExportDataFragment.OnDataExportedListener {
    public static final String ACTION_SHOW_RECORD_STATUS = MainActivity.class.getName() +
            ".ACTION_SHOW_RECORD_STATUS";
    private static final int SECTION_SENSOR_STATUS = 1;
    private static final int SECTION_SETTINGS = 2;
    private static final int SECTION_RECORDING = 3;
    private static final int SECTION_RAWDATA_VIEWER = 4;

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

        // Enable RecordService display recording section directly.
        // In this case, do not add the drawer.
        if (ACTION_SHOW_RECORD_STATUS.equals(getIntent().getAction()) &&
                RecordService.isRunning()) {
            switchSection(SECTION_RECORDING);
            return;
        }

        // Set up the drawer.
        // Reference:
        // https://github.com/mikepenz/MaterialDrawer
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .build();
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(header)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_recording)
                                .withIdentifier(SECTION_RECORDING),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_raw_data)
                                .withIdentifier(SECTION_RAWDATA_VIEWER),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_sensor_status)
                                .withIdentifier(SECTION_SENSOR_STATUS),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_settings)
                                .withIdentifier(SECTION_SETTINGS)
                )
                .withOnDrawerItemClickListener(this)
                .withOnDrawerNavigationListener(this)
                .build();

        if (preferences.getString(getString(R.string.pref_bluetooth_mac), null) == null) {
            drawer.setSelection(SECTION_SETTINGS);
            showSensorSelectionDialog();
        } else {
            drawer.setSelection(SECTION_RECORDING);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (ACTION_SHOW_RECORD_STATUS.equals(intent.getAction())) {
            if (drawer == null)
                switchSection(SECTION_RECORDING);
            else
                drawer.setSelection(SECTION_RECORDING);
        }
    }

    private void switchSection(int id) {
        Fragment fragment;
        switch (id) {
            case SECTION_SENSOR_STATUS:
                fragment = new SensorStatusFragment();
                break;
            case SECTION_SETTINGS:
                fragment = new SettingsFragment();
                break;
            case SECTION_RECORDING:
                if (RecordService.isRunning())
                    fragment = new RecordStatusFragment();
                else
                    fragment = new RecordConfigFragment();
                break;
            case SECTION_RAWDATA_VIEWER:
                fragment = new RawDataViewerFragment();
                break;
            default:
                fragment = new Fragment();
                break;
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switchSection(drawerItem.getIdentifier());
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // When drawer is disabled (i.e. start with ACTION_SHOW_RECORD_STATUS),
                // onClick event of home (up) button will be handle here
                // other than onNavigationClickListener().
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSensorSelectionDialog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SensorSelectionFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDisplaySensorSelectionDialog() {
        showSensorSelectionDialog();
    }

    @Override
    public void onSensorSelected(String mac) {
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(getString(R.string.pref_bluetooth_mac), mac);
        editor.apply();

        getFragmentManager().popBackStack();
    }

    @Override
    public void onRecordingStarted() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new RecordStatusFragment())
                .commit();
    }

    @Override
    public void onRecordingStopped() {
        if (drawer == null)
            // This activity start with ACTION_SHOW_RECORD_STATUS by RecordService.
            finish();
        else
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new RecordConfigFragment())
                    .commit();
    }

    @Override
    public void onExportData() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new ExportDataFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDataExported() {
        getFragmentManager().popBackStack();
    }

}
