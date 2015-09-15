package mo.edu.ipm.stud.envsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing notes.
 */
public class Note extends SugarRecord<Note> {
    private long timestamp;
    private String note;
    // TODO: Add a user-chosen location here?
    // En.. add on another class maybe better.

    public Note() {
        timestamp = System.currentTimeMillis();
    }

    public Note(String note) {
        this();
        this.note = note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }
}
