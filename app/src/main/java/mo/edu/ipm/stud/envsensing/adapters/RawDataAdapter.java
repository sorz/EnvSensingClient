package mo.edu.ipm.stud.envsensing.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.entities.Measurement;
import mo.edu.ipm.stud.envsensing.tasks.BindDataViewAsyncTask;

/**
 * Used by the RecyclerView on RawDataViewFragment.
 */
public class RawDataAdapter extends RecyclerView.Adapter<RawDataAdapter.ViewHolder> {
    private static final int TYPE_SINGLE_LINE = 0;
    private static final int TYPE_EXTENDED = 1;

    private Context context;
    private SimpleDateFormat dateFormat;

    // TODO: Change to Iterator to avoid load full list.
    private List<Measurement> measurements;
    private List<Boolean> extended;

    public static class ViewHolder extends BindDataViewAsyncTask.ViewHolder
            implements View.OnClickListener {
        private OnViewHolderClick onViewHolderClick;
        public View container;

        public ViewHolder(View view, OnViewHolderClick onClickListener) {
            super(view);
            onViewHolderClick = onClickListener;
            container = view.findViewById(R.id.container);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onViewHolderClick.onContainerClick(position);
        }
    }

    private interface OnViewHolderClick {
        // Reference:
        // https://stackoverflow.com/questions/24885223/why-doesnt-recyclerview-have-
        // onitemclicklistener-and-how-recyclerview-is-dif
        void onContainerClick(int position);
    }

    public RawDataAdapter(Context context, List<Measurement> measurements) {
        this.context = context;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        this.measurements = measurements;

        // Reference:
        // https://stackoverflow.com/questions/20615448/set-all-values-of-
        // arraylistboolean-to-false-on-instatiation
        extended = new ArrayList<>(Arrays.asList(new Boolean[measurements.size()]));
        Collections.fill(extended, Boolean.FALSE);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case TYPE_SINGLE_LINE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_raw_data_singleline, parent, false);
                break;
            case TYPE_EXTENDED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_raw_data_extended, parent, false);
                break;
        }
        ViewHolder holder = new ViewHolder(view, new OnViewHolderClick() {
            @Override
            public void onContainerClick(int position) {
                extended.set(position, !extended.get(position));
                notifyItemChanged(position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Measurement measurement = measurements.get(position);
        holder.position = position;
        holder.textDate.setText(dateFormat.format(measurement.getDate()));

        new BindDataViewAsyncTask(context, holder, measurement).execute();
    }

    @Override
    public int getItemViewType(int position) {
        return extended.get(position) ? TYPE_EXTENDED : TYPE_SINGLE_LINE;
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

}
