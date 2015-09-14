package mo.edu.ipm.stud.environmentalsensing.requests;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * A singleton to keep RequestQueue.
 */
public class MyRequestQueue {
    private static RequestQueue queue;

    public static RequestQueue getInstance(Context context) {
        if (queue == null)
            queue = Volley.newRequestQueue(context.getApplicationContext());
        return queue;
    }

}
