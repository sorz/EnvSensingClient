package mo.edu.ipm.stud.envsensing.requests;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Allow 204 no content response.
 */
public class MyJsonObjectRequest extends JsonObjectRequest {

    public MyJsonObjectRequest(int method, String url, JSONObject jsonRequest,
                               Response.Listener<JSONObject> listener,
                               Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        if (response.statusCode == 204)
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
        else
            return super.parseNetworkResponse(response);
    }
}
