package mo.edu.ipm.stud.envsensing.entities;

/**
 * This value is illegal or impossible for this sensor.
 */
public class InvalidValueException extends Exception {
    InvalidValueException() {
    }

    InvalidValueException(String message) {
        super(message);
    }
}
