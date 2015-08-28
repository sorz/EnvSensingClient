package mo.edu.ipm.stud.environmentalsensing;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class MainActivity extends AppCompatActivity
        implements Drawer.OnDrawerItemClickListener,
        SensorSelectionFragment.OnSensorSelectedListener,
        SettingsFragment.OnDisplayDialogListener {

    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the drawer.
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.title_section_settings).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.title_section2).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.title_section3).withIdentifier(1)
                )
                .withOnDrawerItemClickListener(this)
                .build();
        drawer.setSelectionAtPosition(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new SettingsFragment();
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
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0)
            fragmentManager.popBackStack();
        else
            super.onBackPressed();
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
