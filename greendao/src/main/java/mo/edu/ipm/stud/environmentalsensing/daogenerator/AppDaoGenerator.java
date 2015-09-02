package mo.edu.ipm.stud.environmentalsensing.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;

/**
 * Generating GreenDao classes.
 */
public class AppDaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1000, "mo.edu.ipm.stud.environmentalsensing.databases");


        new DaoGenerator().generateAll(schema, "app/src-gen");
    }
}
