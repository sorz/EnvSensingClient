package mo.edu.ipm.stud.envsensing.requests;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.circle.android.api.OkHttpStack;

import okhttp3.OkHttpClient;


/**
 * A singleton to keep RequestQueue.
 */
public class MyRequestQueue {
    private static RequestQueue queue;

    public static RequestQueue getInstance(Context context) {
        if (queue == null)
            queue = Volley.newRequestQueue(context.getApplicationContext(),
                    new OkHttpStack(new OkHttpClient()));
        return queue;
    }

}
