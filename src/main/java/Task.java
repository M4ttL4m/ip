import java.util.Locale;

/**
 * Represents one task in the user's task list.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public void markAsDone() {
        this.isDone = true;
    }

    public void markAsUndone() {
        this.isDone = false;
    }

    /**
     * Returns whether this task's description contains the supplied keyword.
     *
     * @param keyword the text to search for
     * @return true if the description contains the keyword, ignoring letter case
     */
    public boolean containsKeyword(String keyword) {
        return description.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    /**
     * Formats the task into a string representation for saving to disk.
     */
    public String toFileFormat() {
        return (isDone ? "1" : "0") + " | " + description;
    }

    @Override
    public String toString() {
        return "[" + (isDone ? "X" : " ") + "] " + description;
    }
}
