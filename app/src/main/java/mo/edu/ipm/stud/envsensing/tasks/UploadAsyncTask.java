package mo.edu.ipm.stud.envsensing.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import mo.edu.ipm.stud.envsensing.entities.Humidity;
import mo.edu.ipm.stud.envsensing.entities.LocationInfo;
import mo.edu.ipm.stud.envsensing.entities.MeasureValue;
import mo.edu.ipm.stud.envsensing.entities.Measurement;
import mo.edu.ipm.stud.envsensing.entities.Monoxide;
import mo.edu.ipm.stud.envsensing.entities.OxidizingGas;
import mo.edu.ipm.stud.envsensing.entities.ReducingGas;
import mo.edu.ipm.stud.envsensing.entities.Temperature;
import mo.edu.ipm.stud.envsensing.requests.JsonArrayAuthRequest;
import mo.edu.ipm.stud.envsensing.requests.MyRequestQueue;
import mo.edu.ipm.stud.envsensing.requests.ResourcePath;
import mo.edu.ipm.stud.envsensing.requests.RetryPolicy;

/**
 * Upload measured data to server.
 *
 * Progress: float between 0 and 1.
 * Result: Pair(IsSuccess, UploadedCount).
 */
public class UploadAsyncTask extends AsyncTask<Void, Float, Pair<Boolean, Long>> {
    private static final String TAG = "UploadAsyncTask";
    private static final int MAX_PER_REQUEST = 20;

    private Context context;
    private String deviceId;

    public UploadAsyncTask(Context context) {
        this.context = context;
        deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    protected Pair<Boolean, Long> doInBackground(Void... voids) {
        long total = Measurement.count(Measurement.class, "UPLOADED = 0", null);
        Log.d(TAG, total + " measurements need to upload.");
        Iterator<Measurement> measureIterator = Measurement.findAsIterator(
                Measurement.class, "UPLOADED = 0", null, null, "-timestamp", null);

        long progress = 0;
        long uploaded = 0;
        try {
            while (!isCancelled() && measureIterator.hasNext()) {
                JSONArray measures = new JSONArray();
                List<Measurement> needToSave = new ArrayList<>(MAX_PER_REQUEST);
                while (!isCancelled() &&
                        measureIterator.hasNext() && measures.length() < MAX_PER_REQUEST) {
                    ++progress;
                    Measurement measure = measureIterator.next();
                    JSONObject json = packUpMeasure(measure);
                    if (json == null) {
                        Log.d(TAG, "Bypass invalid data.");
                    } else {
                        measures.put(json);
                        measure.setUploaded(true);
                        needToSave.add(measure);
                    }
                }
                uploadMeasures(measures);
                Measurement.saveInTx(needToSave);
                uploaded += measures.length();
                publishProgress(((float) progress / total));
            }
            return new Pair<>(true, uploaded);
        } catch (JSONException e) {
            Log.w(TAG, "JSON exception.", e);
        } catch (ExecutionException e) {
            Log.i(TAG, "Execution exception");
            if (e.getCause() instanceof VolleyError) {
                VolleyError error = (VolleyError) e.getCause();
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    String message = tryParseErrorMessageFromResponse(error.networkResponse.data);
                    Log.w(TAG, String.format("Upload failed %s: %s",
                            error.networkResponse.statusCode, message));
                }
            }
        }
        return new Pair<>(false, uploaded);
    }

    private String tryParseErrorMessageFromResponse(byte[] data) {
        try {
            JSONObject json = new JSONObject(new String(data));
            return json.getString("message");
        } catch (JSONException e) {
            return null;
        }
    }

    private void uploadMeasures(JSONArray measures) throws ExecutionException {
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayAuthRequest request = new JsonArrayAuthRequest(context, Request.Method.POST,
                String.format(ResourcePath.MEASURES, deviceId), measures, future, future);
        request.setRetryPolicy(new RetryPolicy());
        MyRequestQueue.getInstance(context).add(request);

        try {
            JSONArray response = future.get();
        } catch (InterruptedException e) {
            Log.d(TAG, "Upload request interrupted.");
        }

    }

    private JSONObject packUpMeasure(Measurement measure) throws JSONException {
        LocationInfo location = measure.getValueWithoutCache(LocationInfo.class);
        JSONObject values = packUpMeasureValues(measure);
        if (location == null || values == null)
            return null;
        JSONObject json = new JSONObject();
        json.put("timestamp", measure.getTimestamp() / 1000);
        json.put("longitude", location.getLongitude());
        json.put("latitude", location.getLatitude());
        json.put("accuracy", location.getAccuracy());
        json.put("values", values);
        return json;
    }

    private JSONObject packUpMeasureValues(Measurement measure) throws JSONException {
        JSONObject json = new JSONObject();
        MeasureValue[] values = new MeasureValue[6];
        int i = 0;
        values[i++] = measure.getValueWithoutCache(Temperature.class);
        values[i++] = measure.getValueWithoutCache(Humidity.class);
        values[i++] = measure.getValueWithoutCache(Monoxide.class);
        values[i++] = measure.getValueWithoutCache(OxidizingGas.class);
        values[i] = measure.getValueWithoutCache(ReducingGas.class);

        boolean notNull = false;
        for (MeasureValue value : values) {
            if (value != null && value.isValid()) {
                json.put(value.getClass().getSimpleName(), value.getValue());
                notNull = true;
            }
        }
        if (!notNull)
            return null;
        return json;
    }
}
