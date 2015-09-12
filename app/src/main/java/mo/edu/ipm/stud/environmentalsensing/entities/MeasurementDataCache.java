package mo.edu.ipm.stud.environmentalsensing.entities;

import android.util.LruCache;
import android.util.Pair;

import com.orm.SugarRecord;

/**
 * A singleton LruCache to cache measured value from database.
 */
public class MeasurementDataCache extends LruCache<Pair<Long, Class>, SugarRecord> {
    private static final int MAX_SIZE = 1000;
    private static MeasurementDataCache instance;

    private MeasurementDataCache() {
        super(MAX_SIZE);
    }

    static public MeasurementDataCache getInstance() {
        if (instance == null)
            instance = new MeasurementDataCache();
        return instance;
    }

    public <T extends SugarRecord<?>> T getData(long measureId, Class<T> type) {
        return (T) get(new Pair<Long, Class>(measureId, type));
    }

    public <T extends SugarRecord<?>> void putData(long measureId, T data) {
        put(new Pair<Long, Class>(measureId, data.getClass()), data);
    }

    public <T extends SugarRecord<?>> void removeData(long measureId, Class<T> type) {
        remove(new Pair<Long, Class>(measureId, type));
    }

}
