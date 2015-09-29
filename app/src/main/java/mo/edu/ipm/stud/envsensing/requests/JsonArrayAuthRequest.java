package mo.edu.ipm.stud.envsensing.requests;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import mo.edu.ipm.stud.envsensing.R;

/**
 * A JSON request with user token.
 */
public class JsonArrayAuthRequest extends JsonArrayRequest {
    private String token;

    public JsonArrayAuthRequest(Context context, int method, String url, JSONArray jsonRequest,
                                Response.Listener<JSONArray> listener,
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
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode == 204)
            return Response.success(new JSONArray(), HttpHeaderParser.parseCacheHeaders(response));
        else
            return super.parseNetworkResponse(response);
    }
}
