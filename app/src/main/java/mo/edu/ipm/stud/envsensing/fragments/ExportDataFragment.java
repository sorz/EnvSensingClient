package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.tasks.ExportDataAsyncTask;

/**
 * A {@link Fragment} to export raw data.
 */
public class ExportDataFragment extends Fragment {
    private static final String TAG = "ExportDataFragment";
    private static final String DEFAULT_FILENAME = "sensors-%s.csv";
    private static final String FILENAME_DATE_FORMAT = "yyMMdd.hhmmss";

    private OnDataExportedListener callback;
    private File destFile;

    private TextView textPath;
    private Button buttonExport;
    private ProgressBar progressBar;

    public ExportDataFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_export_data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export_data, container, false);
        textPath = (TextView) view.findViewById(R.id.text_export_path);
        buttonExport = (Button) view.findViewById(R.id.button_export);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        destFile = getDefaultExportFilePath();

        try {
            textPath.setText(destFile.getCanonicalPath());
        } catch (IOException e) {
            Log.e(TAG, "Cannot get dest file path.", e);
            Toast.makeText(getActivity(),
                    R.string.cannot_export_io_exception, Toast.LENGTH_SHORT).show();
            callback.onDataExported();
            return view;
        }

        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                export();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnDataExportedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDataExportedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private String getDefaultFilename() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FILENAME_DATE_FORMAT, Locale.US);
        return String.format(DEFAULT_FILENAME,
                dateFormat.format(new Date(System.currentTimeMillis())));
    }

    private File getDefaultExportFilePath() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(path, getDefaultFilename());
    }

    private void export() {
        new ExportDataAsyncTask() {
            @Override
            public void onPreExecute () {
                buttonExport.setEnabled(false);
                progressBar.setMax(100);
                progressBar.setProgress(0);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressUpdate (Integer... values) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(values[0]);
            }

            @Override
            public void onPostExecute(Boolean result) {
                progressBar.setVisibility(View.INVISIBLE);
                buttonExport.setEnabled(true);
                if (result)
                    Toast.makeText(getActivity(), R.string.export_ok, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), R.string.export_error, Toast.LENGTH_SHORT).show();
            }
        }.execute(destFile);
    }

    public interface OnDataExportedListener {
        public void onDataExported();
    }
}
