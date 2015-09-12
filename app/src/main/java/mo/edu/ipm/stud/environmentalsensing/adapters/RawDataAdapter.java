package mo.edu.ipm.stud.environmentalsensing.adapters;

import android.content.Context;
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
import mo.edu.ipm.stud.environmentalsensing.entities.Temperature;

/**
 * Used by the RecyclerView on RawDataViewFragment.
 */
public class RawDataAdapter extends RecyclerView.Adapter<RawDataAdapter.ViewHolder> {
    private Context context;
    private SimpleDateFormat dateFormat;

    // TODO: Change to Iterator to avoid load full list.
    private List<Measurement> measurements;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textDate;
        public TextView textTemperature;

        public ViewHolder(View view) {
            super(view);
            textDate = (TextView) view.findViewById(R.id.text_date);
            textTemperature = (TextView) view.findViewById(R.id.text_temperature);
        }
    }

    public RawDataAdapter(Context context, List<Measurement> measurements) {
        this.context = context;
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
        Measurement measurement = measurements.get(position);
        Temperature temperature = measurement.getData(Temperature.class);
        holder.textDate.setText(dateFormat.format(measurements.get(position).getDate()));

        holder.textTemperature.setVisibility(temperature == null ? View.GONE : View.VISIBLE);
        if (temperature != null)
            holder.textTemperature.setText(
                    context.getString(R.string.certain_celsius, temperature.getCelsius()));
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

}
