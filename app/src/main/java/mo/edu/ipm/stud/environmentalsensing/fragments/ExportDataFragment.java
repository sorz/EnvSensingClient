package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mo.edu.ipm.stud.environmentalsensing.R;

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

    public interface OnDataExportedListener {
        public void onDataExported();
    }
}
