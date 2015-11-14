package mo.edu.ipm.stud.envsensing.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
 * Bind a measurement to a view which show all information about this measurement.
 *
 * Used on RawDataAdapter.onBindViewHolder() and other places.
 */
public class BindDataViewAsyncTask
        extends AsyncTask<Void, Void, Pair<List<MeasureValue>, LocationInfo>> {
    private Context context;
    private ViewHolder holder;
    private int position;
    private Measurement measurement;

    public BindDataViewAsyncTask(Context context, ViewHolder holder, Measurement measurement) {
        this.context = context;
        this.holder = holder;
        this.measurement = measurement;
    }

    @Override
    protected Pair<List<MeasureValue>, LocationInfo> doInBackground(Void... voids) {
        position = holder.position;

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

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public int position;
        public TextView textDate;
        public TextView textTemperature;
        public TextView textHumidity;
        public TextView textPressure;
        public TextView textMonoxide;
        public TextView textOxidizing;
        public TextView textReducing;
        public TextView textLatitude;
        public TextView textLongitude;

        public ViewHolder(View view) {
            super(view);
            textDate = (TextView) view.findViewById(R.id.text_date);
            textTemperature = (TextView) view.findViewById(R.id.text_temperature);
            textHumidity = (TextView) view.findViewById(R.id.text_humidity);
            textPressure = (TextView) view.findViewById(R.id.text_pressure);
            textMonoxide = (TextView) view.findViewById(R.id.text_monoxide);
            textOxidizing = (TextView) view.findViewById(R.id.text_oxidizing);
            textReducing = (TextView) view.findViewById(R.id.text_reducing);
            textLatitude = (TextView) view.findViewById(R.id.text_latitude);
            textLongitude = (TextView) view.findViewById(R.id.text_longitude);
        }

    }

}
