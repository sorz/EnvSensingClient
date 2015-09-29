package mo.edu.ipm.stud.envsensing.requests;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import mo.edu.ipm.stud.envsensing.R;

/**
 * Show error message with Toast to user.
 */
public class MyErrorListener implements Response.ErrorListener {
    static private final String TAG = "MyErrorListener";

    private Context context;

    public MyErrorListener(Context context) {
        this.context = context;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        String message = null;
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                JSONObject resp = new JSONObject(new String(error.networkResponse.data));
                message = resp.getString("message");
            } catch (JSONException e) {
                Log.d(TAG, "Cannot parse error message: " + e);
            }
        }
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else if (error instanceof NetworkError || error instanceof TimeoutError) {
            Toast.makeText(context, R.string.network_fail_message, Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "Unknown Volley error", error);
            Toast.makeText(context, R.string.unknown_fail_message, Toast.LENGTH_SHORT).show();
        }
    }
}
