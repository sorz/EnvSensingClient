package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.requests.MyErrorListener;
import mo.edu.ipm.stud.envsensing.requests.MyJsonObjectRequest;
import mo.edu.ipm.stud.envsensing.requests.MyRequestQueue;
import mo.edu.ipm.stud.envsensing.requests.ResourcePath;
import mo.edu.ipm.stud.envsensing.requests.RetryPolicy;
import mo.edu.ipm.stud.envsensing.requests.UserTokenRequest;

/**
 * A {@link Fragment} allow new user to create their account.
 */
public class UserRegisterFragment extends Fragment {
    static private final String TAG = "UserRegisterFragment";
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
                checkThenRegister();
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

    private void checkThenRegister() {
        String username = textUsername.getText().toString();
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();
        String password2 = textPassword2.getText().toString();

        if (username.isEmpty())
            textUsername.setError(getString(R.string.cannot_be_blank));
        if (email.isEmpty())
            textEmail.setError(getString(R.string.cannot_be_blank));
        if (password.isEmpty())
            textPassword.setError(getString(R.string.cannot_be_blank));
        if (password2.isEmpty())
            textPassword2.setError(getString(R.string.cannot_be_blank));
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty())
            return;

        if (username.length() < 3) {
            textUsername.setError(getString(R.string.username_too_short));
            return;
        }
        if (!password.equals(password2)) {
            textPassword2.setError(getString(R.string.password_no_match));
            textPassword2.setText("");
            return;
        }

        register(username, email, password);
    }

    private void register(final String username, final String email, final String password) {
        buttonRegister.setEnabled(false);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("email", email);
        userInfo.put("password", password);

        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST,
            ResourcePath.USERS, new JSONObject(userInfo),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    onUserRegistered(username, password);
                }
            }, new MyErrorListener(getActivity()) {
                @Override
                public void onErrorResponse(VolleyError error) {
                    super.onErrorResponse(error);
                    buttonRegister.setEnabled(true);
                }
            });
        request.setRetryPolicy(new RetryPolicy());
        MyRequestQueue.getInstance(getActivity()).add(request);
    }

    private void onUserRegistered(String username, String password) {
        Log.d(TAG, username + " registered successfully.");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_user_name), username);
        editor.apply();

        UserTokenRequest request = new UserTokenRequest(username, password,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String token) {
                    buttonRegister.setEnabled(true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getString(R.string.pref_user_token), token);
                    editor.apply();
                    if (callback != null)
                        callback.onUserRegisterFinish(true);
                }
            }, new MyErrorListener(getActivity()) {
                @Override
                public void onErrorResponse(VolleyError error) {
                    super.onErrorResponse(error);
                    buttonRegister.setEnabled(true);
                    if (callback != null)
                        callback.onUserRegisterFinish(false);
                }
            });
        request.setRetryPolicy(new RetryPolicy());
        MyRequestQueue.getInstance(getActivity()).add(request);
    }

    public interface OnUserRegisterListener {
        public void onUserRegisterFinish(boolean loggedIn);
    }

}
