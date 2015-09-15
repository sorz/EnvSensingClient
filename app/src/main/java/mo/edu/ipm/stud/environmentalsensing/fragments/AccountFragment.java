package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import mo.edu.ipm.stud.environmentalsensing.R;

/**
 * A {@link Fragment} show user information and allow to logout.
 */
public class AccountFragment extends Fragment {
    private OnUserLogoutListener callback;
    private SharedPreferences preferences;

    private TextView textUsername;
    private Button buttonLogout;

    public AccountFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_account);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        textUsername = (TextView) view.findViewById(R.id.text_username);
        buttonLogout = (Button) view.findViewById(R.id.button_logout);

        if (preferences.getString(getString(R.string.pref_user_token), null) == null) {
            callback.onUserLoggedOut();
            return view;
        }

        textUsername.setText(preferences.getString(getString(R.string.pref_user_name), ""));

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        textUsername = null;
        buttonLogout = null;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OnUserLogoutListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.pref_user_token));
        editor.apply();
        if (callback != null)
            callback.onUserLoggedOut();
    }

    public interface OnUserLogoutListener {
        public void onUserLoggedOut();
    }

}
