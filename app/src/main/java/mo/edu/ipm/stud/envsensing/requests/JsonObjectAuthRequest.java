package mo.edu.ipm.stud.envsensing.requests;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mo.edu.ipm.stud.envsensing.R;

/**
 * A JSON object request with user token.
 */
public class JsonObjectAuthRequest extends JsonObjectRequest {
    private String token;

    public JsonObjectAuthRequest(Context context, String url, JSONObject jsonRequest,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
        token = getUserToken(context);
    }

    public JsonObjectAuthRequest(Context context, int method, String url, String requestBody,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
        token = getUserToken(context);
    }

    public JsonObjectAuthRequest(Context context, String url,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        token = getUserToken(context);
    }

    public JsonObjectAuthRequest(Context context, int method, String url,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        token = getUserToken(context);
    }

    public JsonObjectAuthRequest(Context context, int method, String url, JSONObject jsonRequest,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        token = getUserToken(context);
    }

    private String getUserToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_user_token), null);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (token == null)
            throw new AuthFailureError("Token not found.");
        HashMap<String, String> params = new HashMap<>();
        String base64 = Base64.encodeToString((token + ":").getBytes(), Base64.NO_WRAP);
        params.put("Authorization", String.format("Basic %s:", base64));
        return params;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode == 204)
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
        else
            return super.parseNetworkResponse(response);
    }
}
