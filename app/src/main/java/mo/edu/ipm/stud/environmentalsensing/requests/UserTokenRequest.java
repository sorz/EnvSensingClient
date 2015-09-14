package mo.edu.ipm.stud.environmentalsensing.requests;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Get user token with given username and password.
 */
public class UserTokenRequest extends Request<String> {
    private static final String TAG = "UserTokenRequest";

    private Response.Listener<String> listener;
    private String username;
    private String password;

    public UserTokenRequest(String username, String password, Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(Method.GET, ResourcePath.TOKEN, errorListener);
        this.listener = listener;
        this.username = username;
        this.password = password;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        // Reference:
        // https://stackoverflow.com/questions/16817980/
        // how-does-one-use-basic-authentication-with-volley-on-android
        HashMap<String, String> params = new HashMap<>();
        String cred = String.format("%s:%s", username, password);
        String auth = "Basic " + Base64.encodeToString(cred.getBytes(), Base64.NO_WRAP);
        params.put("Authorization", auth);
        return params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            if (response.statusCode == HttpURLConnection.HTTP_OK) {
                JSONObject json = new JSONObject(new String(response.data));
                return Response.success(json.getString("token"),
                        HttpHeaderParser.parseCacheHeaders(response));
            } else {
                Log.w(TAG, "Unexpected status code: " + response.statusCode);
                return Response.error(new ServerError());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parsing response.", e);
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }
}
