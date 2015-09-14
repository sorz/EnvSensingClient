package mo.edu.ipm.stud.environmentalsensing.requests;

import com.android.volley.DefaultRetryPolicy;

/**
 * Our retry policy for all general Volley requests.
 */
public class RetryPolicy extends DefaultRetryPolicy {
    static private final int TIMEOUT = 5000;  // 5 seconds.

    public RetryPolicy() {
        super(TIMEOUT, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT);
    }
}
