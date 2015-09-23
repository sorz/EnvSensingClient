package mo.edu.ipm.stud.envsensing.requests;

/**
 * Collect URLs of API.
 */
public class ResourcePath {
    private static final String SITE = "https://fyp.sorz.org/api";

    static final String TOKEN = SITE + "/token/";
    static public final String DEVICES = SITE + "/devices/";
    static public final String MEASURES = SITE + "/devices/%s/";
}
