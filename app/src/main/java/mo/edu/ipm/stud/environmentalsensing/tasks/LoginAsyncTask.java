package mo.edu.ipm.stud.environmentalsensing.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

import mo.edu.ipm.stud.environmentalsensing.R;

/**
 * Given the username and password, get user token via API.
 */
public class LoginAsyncTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "LoginAsyncTask";
    private static final String PATH = "/token";

    private Context context;


    public LoginAsyncTask(Context context) {
        this.context = context;
    }

    private String getBasicAuthorization(String username, String password) {
        // Reference:
        // https://stackoverflow.com/questions/14550131/http-basic-authentication-
        // issue-on-android-jelly-bean-4-1-using-httpurlconnectio
        byte[] auth = (username + ":" + password).getBytes();
        return Base64.encodeToString(auth, Base64.NO_WRAP);
    }

    @Override
    protected String doInBackground(String... params) {
        String username = params[0];
        String password = params[1];

        HttpURLConnection conn = null;
        try {
            URL url = new URL(context.getString(R.string.api_url) + PATH);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization",
                    "Basic " + getBasicAuthorization(username, password));
            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_FORBIDDEN) {
                // TODO: username/password error.
            } else if (code == HttpURLConnection.HTTP_OK) {
                // TODO: parsing json.
            } else {
                // TODO: server fail.
            }

        } catch (java.io.IOException e) {
            Log.w(TAG, "Network error occur during login.", e);
            return null;
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return null;
    }
}
