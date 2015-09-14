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
 * A {@link Fragment} allow user type their username & password to login.
 */
public class UserLoginFragment extends Fragment {
    private OnUserLoginListener callback;
    private SharedPreferences preferences;

    private TextView textUsername;
    private TextView textPassword;
    private Button buttonLogin;

    public UserLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_user_login);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_login, container, false);
        textUsername = (TextView) view.findViewById(R.id.text_username);
        textPassword = (TextView) view.findViewById(R.id.text_password);
        buttonLogin = (Button) view.findViewById(R.id.button_login);

        textUsername.setText(preferences.getString(getString(R.string.pref_user_name), ""));
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        textUsername = null;
        textPassword = null;
        buttonLogin = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OnUserLoginListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private void login() {
        String username = textUsername.getText().toString();
        String password = textPassword.getText().toString();
        if (username.isEmpty())
            textUsername.setError(getString(R.string.cannot_be_blank));
        if (password.isEmpty())
            textPassword.setError(getString(R.string.cannot_be_blank));
        if (username.isEmpty() || password.isEmpty())
            return;
    }

    public interface OnUserLoginListener {
        public void onUserLoggedIn(String username, String email);
    }

}
