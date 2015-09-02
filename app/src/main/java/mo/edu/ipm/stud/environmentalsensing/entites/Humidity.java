package mo.edu.ipm.stud.environmentalsensing.entites;

/**
 * Created by xierch on 2015/9/2.
 */
public class Humidity extends MeasureValue<Humidity> {

    Humidity() {
    }

    public Humidity(Measurement measurement) {
        super(measurement, 2);
    }
}
