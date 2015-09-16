package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import mo.edu.ipm.stud.envsensing.R;

/**
 * A {@link Fragment} allow new user to create their account.
 */
public class UserRegisterFragment extends Fragment {
    private OnUserRegisterListener callback;
    private SharedPreferences preferences;

    private TextView textUsername;
    private TextView textEmail;
    private TextView textPassword;
    private TextView textPassword2;
    private Button buttonRegister;


    public UserRegisterFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_user_register);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_register, container, false);
        textUsername = (TextView) view.findViewById(R.id.text_username);
        textEmail = (TextView) view.findViewById(R.id.text_email);
        textPassword = (TextView) view.findViewById(R.id.text_password);
        textPassword2 = (TextView) view.findViewById(R.id.text_password2);
        buttonRegister = (Button) view.findViewById(R.id.button_register);

        textUsername.setText(preferences.getString(getString(R.string.pref_user_name), ""));
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        textUsername = null;
        textEmail = null;
        textPassword = null;
        textPassword2 = null;
        buttonRegister = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OnUserRegisterListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private void register() {
        String username = textUsername.getText().toString();
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();
        String password2 = textPassword2.getText().toString();

        if (username.isEmpty())
            textUsername.setError(getString(R.string.cannot_be_blank));
        if (email.isEmpty())
            textUsername.setError(getString(R.string.cannot_be_blank));
        if (password.isEmpty())
            textUsername.setError(getString(R.string.cannot_be_blank));
        if (password2.isEmpty())
            textUsername.setError(getString(R.string.cannot_be_blank));
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty())
            return;

        if (username.length() < 3) {
            textUsername.setError(getString(R.string.username_too_short));
            return;
        }
        if (!password.equals(password2)) {
            textPassword2.setError(getString(R.string.password_no_match));
            return;
        }

        // TODO: register with server.
    }

    public interface OnUserRegisterListener {
        public void onUserRemasterFinish();
    }

}
