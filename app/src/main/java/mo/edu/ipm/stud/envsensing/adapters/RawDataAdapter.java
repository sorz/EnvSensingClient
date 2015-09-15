package mo.edu.ipm.stud.envsensing.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.entities.Humidity;
import mo.edu.ipm.stud.envsensing.entities.LocationInfo;
import mo.edu.ipm.stud.envsensing.entities.MeasureValue;
import mo.edu.ipm.stud.envsensing.entities.Measurement;
import mo.edu.ipm.stud.envsensing.entities.Monoxide;
import mo.edu.ipm.stud.envsensing.entities.OxidizingGas;
import mo.edu.ipm.stud.envsensing.entities.Pressure;
import mo.edu.ipm.stud.envsensing.entities.ReducingGas;
import mo.edu.ipm.stud.envsensing.entities.Temperature;

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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnViewHolderClick onViewHolderClick;

        public int position;
        public View container;
        public TextView textDate;
        public TextView textTemperature;
        public TextView textHumidity;
        public TextView textPressure;
        public TextView textMonoxide;
        public TextView textOxidizing;
        public TextView textReducing;
        public TextView textLatitude;
        public TextView textLongitude;


        public ViewHolder(View view, OnViewHolderClick onClickListener) {
            super(view);
            onViewHolderClick = onClickListener;
            container = view.findViewById(R.id.container);
            textDate = (TextView) view.findViewById(R.id.text_date);
            textTemperature = (TextView) view.findViewById(R.id.text_temperature);
            textHumidity = (TextView) view.findViewById(R.id.text_humidity);
            textPressure = (TextView) view.findViewById(R.id.text_pressure);
            textMonoxide = (TextView) view.findViewById(R.id.text_monoxide);
            textOxidizing = (TextView) view.findViewById(R.id.text_oxidizing);
            textReducing = (TextView) view.findViewById(R.id.text_reducing);
            textLatitude = (TextView) view.findViewById(R.id.text_latitude);
            textLongitude = (TextView) view.findViewById(R.id.text_longitude);

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
        holder.position = position;
        holder.textDate.setText(dateFormat.format(measurements.get(position).getDate()));

        new AsyncTask<ViewHolder, Void, Pair<List<MeasureValue>, LocationInfo>>() {
            private ViewHolder holder;
            private int position;
            private Measurement measurement;

            @Override
            protected Pair<List<MeasureValue>, LocationInfo> doInBackground(ViewHolder... viewHolders) {
                holder = viewHolders[0];
                position = holder.position;
                measurement = measurements.get(position);

                List<MeasureValue> values = new ArrayList<>(6);
                values.add(measurement.getValue(Temperature.class));
                values.add(measurement.getValue(Humidity.class));
                values.add(measurement.getValue(Pressure.class));
                values.add(measurement.getValue(Monoxide.class));
                values.add(measurement.getValue(OxidizingGas.class));
                values.add(measurement.getValue(ReducingGas.class));
                LocationInfo location = measurement.getValue(LocationInfo.class);
                return new Pair<>(values, location);
            }

            private int getVisibility(Object object) {
                return object == null ? View.GONE : View.VISIBLE;
            }

            @SuppressWarnings("ResourceType")
            @Override
            protected void onPostExecute(Pair<List<MeasureValue>, LocationInfo> result) {
                if (holder.position != position)
                    return;
                List<MeasureValue> values = result.first;
                LocationInfo location = result.second;

                int i = 0;
                holder.textTemperature.setVisibility(getVisibility(values.get(i++)));
                holder.textHumidity.setVisibility(getVisibility(values.get(i++)));
                holder.textPressure.setVisibility(getVisibility(values.get(i++)));
                holder.textMonoxide.setVisibility(getVisibility(values.get(i++)));
                holder.textOxidizing.setVisibility(getVisibility(values.get(i++)));
                holder.textReducing.setVisibility(getVisibility(values.get(i)));

                holder.textLatitude.setVisibility(getVisibility(location));
                holder.textLongitude.setVisibility(getVisibility(location));

                i = 0;
                if (values.get(i) != null)
                    holder.textTemperature.setText(
                            context.getString(R.string.certain_celsius,
                                    ((Temperature) values.get(i++)).getCelsius()));
                if (values.get(i) != null)
                    holder.textHumidity.setText(
                            context.getString(R.string.certain_percentage,
                                    values.get(i++).getValue()));
                if (values.get(i) != null)
                    holder.textPressure.setText(
                            context.getString(R.string.certain_hpa,
                                    values.get(i++).getValue() / 100.0));
                if (values.get(i) != null)
                    holder.textMonoxide.setText(
                            context.getString(R.string.certain_ppm,
                                    values.get(i++).getValue()));
                if (values.get(i) != null)
                    holder.textOxidizing.setText(
                            context.getString(R.string.certain_ohm,
                                    values.get(i++).getValue()));
                if (values.get(i) != null)
                    holder.textReducing.setText(
                            context.getString(R.string.certain_ohm,
                                    values.get(i).getValue()));

                if (location != null) {
                    holder.textLatitude.setText(
                            context.getString(R.string.certain_degree, location.getLatitude()));
                    holder.textLongitude.setText(
                            context.getString(R.string.certain_degree, location.getLongitude()));
                }
            }
        }.execute(holder);
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
