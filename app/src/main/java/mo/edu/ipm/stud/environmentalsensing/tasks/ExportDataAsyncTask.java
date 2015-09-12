package mo.edu.ipm.stud.environmentalsensing.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import mo.edu.ipm.stud.environmentalsensing.entities.Humidity;
import mo.edu.ipm.stud.environmentalsensing.entities.LocationInfo;
import mo.edu.ipm.stud.environmentalsensing.entities.MeasureValue;
import mo.edu.ipm.stud.environmentalsensing.entities.Measurement;
import mo.edu.ipm.stud.environmentalsensing.entities.Monoxide;
import mo.edu.ipm.stud.environmentalsensing.entities.OxidizingGas;
import mo.edu.ipm.stud.environmentalsensing.entities.Pressure;
import mo.edu.ipm.stud.environmentalsensing.entities.ReducingGas;
import mo.edu.ipm.stud.environmentalsensing.entities.Temperature;

/**
 * Export measure data to a csv file.
 */
public class ExportDataAsyncTask extends AsyncTask<File, Integer, Boolean> {
    private static final String TAG = "ExportDataAsyncTask";
    private static final String CSV_HEADER = "id,timestamp,temperature,humidity,pressure," +
            "monoxide,oxidizing,reducing,latitude,longitude,accuracy\r\n";

    @Override
    protected Boolean doInBackground(File... files) {
        List<Measurement> measurements = Measurement.listAll(Measurement.class);
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(files[0])));
            writer.write(CSV_HEADER);
            writer.flush();

            int totalRows = measurements.size();
            int currentRow = 0;
            for (Measurement measurement : measurements) {
                writer.write(measurement.getId() + ",");
                writer.write(measurement.getTimestamp() + ",");

                MeasureValue[] values = new MeasureValue[6];
                values[0] = measurement.getValue(Temperature.class);
                values[1] = measurement.getValue(Humidity.class);
                values[2] = measurement.getValue(Pressure.class);
                values[3] = measurement.getValue(Monoxide.class);
                values[4] = measurement.getValue(OxidizingGas.class);
                values[5] = measurement.getValue(ReducingGas.class);
                for (MeasureValue value : values) {
                    if (value != null)
                        writer.write(value.getValue() + "");
                    writer.write(",");
                }

                LocationInfo location = measurement.getValue(LocationInfo.class);
                if (location == null)
                    writer.write(",,\r\n");
                else
                    writer.write(location.getLatitude() + "," + location.getLongitude() + ","
                            + location.getAccuracy() + "\r\n");

                writer.flush();
                currentRow ++;
                publishProgress((int) ((double) currentRow) / totalRows * 100);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot create file.", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Cannot writing out.", e);
            return false;
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }
        }

        return true;
    }
}
