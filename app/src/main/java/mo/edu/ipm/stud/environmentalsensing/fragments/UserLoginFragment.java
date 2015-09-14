package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import mo.edu.ipm.stud.environmentalsensing.R;
import mo.edu.ipm.stud.environmentalsensing.requests.MyRequestQueue;
import mo.edu.ipm.stud.environmentalsensing.requests.RetryPolicy;
import mo.edu.ipm.stud.environmentalsensing.requests.UserTokenRequest;

/**
 * A {@link Fragment} allow user type their username & password to login.
 */
public class UserLoginFragment extends Fragment {
    private static final String TAG = "UserLoginFragment";

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
        final String username = textUsername.getText().toString();
        final String password = textPassword.getText().toString();
        if (username.isEmpty())
            textUsername.setError(getString(R.string.cannot_be_blank));
        if (password.isEmpty())
            textPassword.setError(getString(R.string.cannot_be_blank));
        if (username.isEmpty() || password.isEmpty())
            return;

        buttonLogin.setEnabled(false);
        UserTokenRequest request = new UserTokenRequest(username, password,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User token got: " + response.substring(0, 8) + "...");
                onLoggedIn(username, "", response);
                // TODO: get user's email.
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    Toast.makeText(getActivity(), R.string.auth_fail_message,
                            Toast.LENGTH_SHORT).show();
                } else if (error instanceof NetworkError || error instanceof TimeoutError) {
                    Toast.makeText(getActivity(), R.string.network_fail_message,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.unknown_fail_message,
                            Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Unknown Volley error", error);
                }
                buttonLogin.setEnabled(true);
            }
        });
        request.setRetryPolicy(new RetryPolicy());
        MyRequestQueue.getInstance(getActivity()).add(request);

    }

    private void onLoggedIn(String username, String email, String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_user_name), username);
        editor.putString(getString(R.string.pref_user_email), email);
        editor.putString(getString(R.string.pref_user_token), token);
        editor.apply();
        if (callback != null)
            callback.onUserLoggedIn(username, email);
    }

    public interface OnUserLoginListener {
        public void onUserLoggedIn(String username, String email);
    }

}
