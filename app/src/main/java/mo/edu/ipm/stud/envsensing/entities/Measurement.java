package mo.edu.ipm.stud.envsensing.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;

/**
 * Logging the date time of a specific measurement.
 */
public class Measurement extends SugarRecord<Measurement> implements ClusterItem, Parcelable {
    private long timestamp;
    private boolean uploaded;
    private String tag;

    public Measurement() {
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return new Date(getTimestamp());
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Nullable
    public <T extends SugarRecord<?>> T getValue(Class<T> type) {
        MeasurementValueCache cache = MeasurementValueCache.getInstance();
        T value = cache.getValue(getId(), type);
        if (value != null)
            return value;

        List<T> result = Temperature.find(type, "MEASURE_ID = ?", "" + getId());
        if (result.size() == 0)
            return null;

        value = result.get(0);
        cache.putValue(getId(), value);
        return value;
    }

    @Nullable
    public <T extends SugarRecord<?>> T getValueWithoutCache(Class<T> type) {
        // TODO: Remove redundancy.
        List<T> result = Temperature.find(type, "MEASURE_ID = ?", "" + getId());
        if (result.size() == 0)
            return null;
        else
            return result.get(0);
    }

    public String getTag() {
        return tag;
    }

    @Override
    public LatLng getPosition() {
        LocationInfo location = getValue(LocationInfo.class);
        if (location == null)
            return null;
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(getId());
        out.writeLong(timestamp);
        out.writeInt(uploaded ? 1 : 0);
        out.writeString(tag);
    }

    public static final Parcelable.Creator<Measurement> CREATOR
            = new Parcelable.Creator<Measurement>() {
        public Measurement createFromParcel(Parcel in) {
            return new Measurement(in);
        }

        public Measurement[] newArray(int size) {
            return new Measurement[size];
        }
    };

    private Measurement(Parcel in) {
        setId(in.readLong());
        timestamp = in.readLong();
        uploaded = in.readInt() == 1;
        tag = in.readString();
    }

}
