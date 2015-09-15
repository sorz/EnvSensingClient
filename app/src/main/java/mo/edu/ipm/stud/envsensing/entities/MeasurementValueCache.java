package mo.edu.ipm.stud.envsensing.entities;

import android.util.LruCache;
import android.util.Pair;

import com.orm.SugarRecord;

/**
 * A singleton LruCache to cache measured value from database.
 */
public class MeasurementValueCache extends LruCache<Pair<Long, Class>, SugarRecord> {
    private static final int MAX_SIZE = 1000;
    private static MeasurementValueCache instance;

    private MeasurementValueCache() {
        super(MAX_SIZE);
    }

    static public MeasurementValueCache getInstance() {
        if (instance == null)
            instance = new MeasurementValueCache();
        return instance;
    }

    public <T extends SugarRecord<?>> T getValue(long measureId, Class<T> type) {
        return (T) get(new Pair<Long, Class>(measureId, type));
    }

    public <T extends SugarRecord<?>> void putValue(long measureId, T data) {
        put(new Pair<Long, Class>(measureId, data.getClass()), data);
    }

    public <T extends SugarRecord<?>> void removeValue(long measureId, Class<T> type) {
        remove(new Pair<Long, Class>(measureId, type));
    }

}
