package mo.edu.ipm.stud.envsensing.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import mo.edu.ipm.stud.envsensing.entities.Humidity;
import mo.edu.ipm.stud.envsensing.entities.LocationInfo;
import mo.edu.ipm.stud.envsensing.entities.MeasureValue;
import mo.edu.ipm.stud.envsensing.entities.Measurement;
import mo.edu.ipm.stud.envsensing.entities.Monoxide;
import mo.edu.ipm.stud.envsensing.entities.OxidizingGas;
import mo.edu.ipm.stud.envsensing.entities.ReducingGas;
import mo.edu.ipm.stud.envsensing.entities.Temperature;

/**
 * Upload measured data to server.
 */
public class UploadAsyncTask extends AsyncTask<Void, Float, Void> {
    private static final String TAG = "UploadAsyncTask";
    private static final int MAX_PER_REQUEST = 20;

    @Override
    protected Void doInBackground(Void... voids) {
        long total = Measurement.count(Measurement.class, "uploaded = false", null);
        Log.d(TAG, "%s measurements need to upload.");
        Iterator<Measurement> measureIterator = Measurement.findAsIterator(
                Measurement.class, "uploaded = false", null, null, "-timestamp", null);


        try {
            while (measureIterator.hasNext()) {
                JSONArray measures = new JSONArray();

                while (measureIterator.hasNext() && measures.length() < MAX_PER_REQUEST) {
                    Measurement measure = measureIterator.next();
                    JSONObject json = packUpMeasure(measure);
                    if (json == null)
                        Log.d(TAG, "Bypass invalid data.");
                    else
                        measures.put(json);
                }

                // TODO: upload measures and wait.
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject packUpMeasure(Measurement measure) throws JSONException {
        LocationInfo location = measure.getValueWithoutCache(LocationInfo.class);
        JSONObject values = packUpMeasureValues(measure);
        if (location == null || values == null)
            return null;
        JSONObject json = new JSONObject();
        json.put("timestamp", measure.getTimestamp());
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
            if (value != null) {
                json.put(value.getClass().getSimpleName(), value.getValue());
                notNull = true;
            }
        }
        if (!notNull)
            return null;
        return json;
    }
}
