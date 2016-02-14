package mo.edu.ipm.stud.envsensing;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import mo.edu.ipm.stud.envsensing.fragments.ExportDataFragment;
import mo.edu.ipm.stud.envsensing.fragments.MapsFragment;
import mo.edu.ipm.stud.envsensing.fragments.RawDataViewerFragment;
import mo.edu.ipm.stud.envsensing.fragments.SensorConnectFragment;
import mo.edu.ipm.stud.envsensing.fragments.SensorNewTaskFragment;
import mo.edu.ipm.stud.envsensing.fragments.SensorInTaskFragment;
import mo.edu.ipm.stud.envsensing.fragments.SensorSelectionFragment;
import mo.edu.ipm.stud.envsensing.fragments.SensorStatusFragment;
import mo.edu.ipm.stud.envsensing.fragments.SettingsFragment;
import mo.edu.ipm.stud.envsensing.fragments.UserLoginFragment;
import mo.edu.ipm.stud.envsensing.fragments.UserRegisterFragment;
import mo.edu.ipm.stud.envsensing.services.RecordService;
import mo.edu.ipm.stud.envsensing.services.SensorService;

public class MainActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener,
        Drawer.OnDrawerItemClickListener,
        Drawer.OnDrawerNavigationListener,
        SensorSelectionFragment.OnSensorSelectedListener,
        SettingsFragment.OnDisplayDialogListener,
        SensorInTaskFragment.OnRecordingStoppedListener,
        RawDataViewerFragment.OnExportDataListener,
        ExportDataFragment.OnDataExportedListener,
        UserLoginFragment.OnUserLoginListener,
        UserRegisterFragment.OnUserRegisterListener,
        SensorService.OnSensorStateChangedListener {
    public static final String ACTION_SHOW_RECORD_STATUS = MainActivity.class.getName() +
            ".ACTION_SHOW_RECORD_STATUS";
    private static final String TAG = "MainActivity";
    private static final int SECTION_SENSOR_STATUS = 1;
    private static final int SECTION_SETTINGS = 2;
    private static final int SECTION_SENSOR_CONTROL = 3;
    private static final int SECTION_RAW_DATA_VIEWER = 4;
    private static final int SECTION_MAPS = 5;

    private Drawer drawer;
    private int currentSection;
    private SensorService sensorService;
    private boolean sensorIsBound;

    private ServiceConnection sensorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            sensorService = ((SensorService.LocalBinder) service).getService();
            sensorService.registerSensorStateChangedListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            sensorService.unregisterSensorServiceStateChangedListener(MainActivity.this);
            sensorService = null;
        }
    };

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        bindSensorService();

        // Enable RecordService display recording section directly.
        // In this case, do not add the drawer.
        if (ACTION_SHOW_RECORD_STATUS.equals(getIntent().getAction()) &&
                RecordService.isRunning()) {
            switchSection(SECTION_SENSOR_CONTROL);
            return;
        }

        // Set up the drawer.
        // Reference:
        // https://github.com/mikepenz/MaterialDrawer
        AccountHeaderBuilder headerBuilder = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header);
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerBuilder.build())
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.title_sensor_control)
                                .withIcon(R.drawable.ic_hearing_black_24dp)
                                .withIdentifier(SECTION_SENSOR_CONTROL),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_maps)
                                .withIcon(R.drawable.ic_map_black_24dp)
                                .withIdentifier(SECTION_MAPS),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_raw_data)
                                .withIcon(R.drawable.ic_sd_card_black_24dp)
                                .withIdentifier(SECTION_RAW_DATA_VIEWER),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_sensor_status)
                                .withIcon(R.drawable.ic_swap_vert_black_24dp)
                                .withIdentifier(SECTION_SENSOR_STATUS),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_settings)
                                .withIcon(R.drawable.ic_settings_black_24dp)
                                .withIdentifier(SECTION_SETTINGS)
                )
                .withOnDrawerItemClickListener(this)
                .withOnDrawerNavigationListener(this)
                .build();

        if (preferences.getString(getString(R.string.pref_bluetooth_mac), null) == null) {
            drawer.setSelection(SECTION_SETTINGS);
            showSensorSelectionDialog();
        } else {
            drawer.setSelection(SECTION_SENSOR_CONTROL);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindSensorService();
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (ACTION_SHOW_RECORD_STATUS.equals(intent.getAction())) {
            if (drawer == null)
                switchSection(SECTION_SENSOR_CONTROL);
            else
                drawer.setSelection(SECTION_SENSOR_CONTROL);
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
            case SECTION_SENSOR_CONTROL:
                if (sensorService != null) {
                    SensorService.SensorState state = sensorService.getState();
                    if (state == SensorService.SensorState.DISCONNECTED ||
                            state == SensorService.SensorState.CONNECTING)
                        fragment = new SensorConnectFragment();
                    else if (state == SensorService.SensorState.HEATING ||
                            state == SensorService.SensorState.READY)
                        fragment = new SensorNewTaskFragment();
                    else if (state == SensorService.SensorState.TASK_MEASURING ||
                            state == SensorService.SensorState.TASK_REST)
                        fragment = new SensorInTaskFragment();
                    else {
                        Log.wtf(TAG, "Unknown sensor state: " + state);
                        return;
                    }
                } else {
                    Log.d(TAG, "SensorService doesn't start!");
                    fragment = new SensorConnectFragment();
                }
                break;
            case SECTION_RAW_DATA_VIEWER:
                fragment = new RawDataViewerFragment();
                break;
            case SECTION_MAPS:
                fragment = new MapsFragment();
                break;
            default:
                fragment = new Fragment();
                break;
        }
        currentSection = id;
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void switchFragment(Fragment fragment, boolean addToBackStack) {
        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment);
        if (addToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    public SensorService getSensorService() {
        return sensorService;
    }

    private void bindSensorService() {
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, sensorConnection, Context.BIND_AUTO_CREATE);
        sensorIsBound = true;
    }

    private void unbindSensorService() {
        if (sensorIsBound) {
            unbindService(sensorConnection);
            sensorIsBound = false;
        }
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
        switchFragment(new SensorSelectionFragment(), true);
    }

    @Override
    public void onSensorStateChanged(SensorService.SensorState newState) {
        Log.d(TAG, "Sensor state changed to " + newState + ".");
        if (currentSection == SECTION_SENSOR_CONTROL)
           switchSection(currentSection);
    }

    @Override
    public void onDisplaySensorSelectionDialog() {
        showSensorSelectionDialog();
    }

    @Override
    public void onDisplayLoginDialog() {
        switchFragment(new UserLoginFragment(), true);
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
    public void onRecordingStopped() {
        if (drawer == null)
            // This activity start with ACTION_SHOW_RECORD_STATUS by RecordService.
            finish();
        else
            switchFragment(new SensorNewTaskFragment(), false);
    }

    @Override
    public void onExportData() {
        switchFragment(new ExportDataFragment(), true);
    }

    @Override
    public void onDataExported() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onUserLoggedIn() {
       getFragmentManager().popBackStack();
    }

    @Override
    public void onUserRegister() {
        switchFragment(new UserRegisterFragment(), true);
    }

    @Override
    public void onUserRegisterFinish(boolean loggedIn) {
        getFragmentManager().popBackStack();
        if (loggedIn)
            getFragmentManager().popBackStack();
    }
}
