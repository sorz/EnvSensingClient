package mo.edu.ipm.stud.environmentalsensing.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import mo.edu.ipm.stud.environmentalsensing.R;
import mo.edu.ipm.stud.environmentalsensing.entities.Measurement;

/**
 * Used by the RecyclerView on RawDataViewFragment.
 */
public class RawDataAdapter extends RecyclerView.Adapter<RawDataAdapter.ViewHolder> {
    private SimpleDateFormat dateFormat;

    // TODO: Change to Iterator to avoid load full list.
    private List<Measurement> measurements;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.textView);
        }
    }

    public RawDataAdapter(List<Measurement> measurements) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        this.measurements = measurements;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_raw_data, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(dateFormat.format(measurements.get(position).getDate()));
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

}
