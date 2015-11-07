package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.squareup.timessquare.CalendarPickerView;

import java.util.Date;
import java.util.List;

import mo.edu.ipm.stud.envsensing.R;

/**
 * Show a calendar to select a range of date.
 */
public class DatePickerFragment extends DialogFragment {
    private final static String ARGS_MIN_DATE = "min-date";
    private final static String ARGS_MAX_DATE = "max-date";
    public final static String RESULT_DATE_FROM = "date-from";
    public final static String RESULT_DATE_TO = "date-to";
    public final static int RESULT_SELECTED = 0;

    private CalendarPickerView calendarView;

    static DatePickerFragment newInstance(long minDate, long maxDate) {
        DatePickerFragment fragment = new DatePickerFragment();

        Bundle args = new Bundle();
        args.putLong(ARGS_MIN_DATE, minDate);
        args.putLong(ARGS_MAX_DATE, maxDate);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.choose_date_range));
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_picker, container, false);
        calendarView = (CalendarPickerView) view.findViewById(R.id.calendarView);
        Button button = (Button) view.findViewById(R.id.button);

        Date minDate = new Date(getArguments().getLong(ARGS_MIN_DATE));
        Date maxDate = new Date(getArguments().getLong(ARGS_MAX_DATE));
        calendarView.init(minDate, maxDate).inMode(CalendarPickerView.SelectionMode.RANGE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        return view;
    }

    private void confirm() {
        List<Date> selectDate = calendarView.getSelectedDates();
        if (selectDate.isEmpty())
            return;  // TODO: tell user.
        long dateFrom = selectDate.get(0).getTime();
        long dateTo = selectDate.get(selectDate.size() - 1).getTime() + 24 * 3600 * 1000;

        Intent intent = new Intent();
        intent.putExtra(RESULT_DATE_FROM, dateFrom);
        intent.putExtra(RESULT_DATE_TO, dateTo);
        if (getTargetFragment() != null)
            getTargetFragment().onActivityResult(
                    getTargetRequestCode(), RESULT_SELECTED, intent);
        dismiss();
    }

}
