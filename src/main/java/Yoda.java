import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Yoda {

    // OS-independent relative path definition
    private static final Path FILE_PATH = Paths.get(".", "data", "yoda.txt");

    private static TaskList tasks = new TaskList();

    /**
     * Starts the YODA command-line application.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();
        loadTasksFromDisk();

        while (ui.hasNextCommand()) {
            String input = ui.readCommand();
            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split(" ", 2);
            String command = parts[0];
            String arguments = parts.length > 1 ? parts[1] : "";

            ui.showDivider();

            try {
                handleCommand(command, arguments, ui);
            } catch (YodaException e) {
                ui.showError(e.getMessage());
            } catch (NumberFormatException e) {
                ui.showInvalidNumberError();
            }

            ui.showDivider();
        }
    }

    /**
     * Performs the action requested by a parsed command.
     *
     * @param command the command word entered by the user
     * @param arguments the text after the command word
     * @param ui the interface used to display command-specific messages
     * @throws YodaException if the command or its arguments are invalid
     */
    private static void handleCommand(String command, String arguments, Ui ui) throws YodaException {
        switch (command) {
            case "bye":
                ui.showGoodbye();
                System.exit(0);
                break;
            case "list":
                handleList();
                break;
            case "mark":
                handleMark(arguments);
                saveTasksToDisk();
                break;
            case "unmark":
                handleUnmark(arguments);
                saveTasksToDisk();
                break;
            case "todo":
                handleTodo(arguments);
                saveTasksToDisk();
                break;
            case "deadline":
                handleDeadline(arguments);
                saveTasksToDisk();
                break;
            case "event":
                handleEvent(arguments);
                saveTasksToDisk();
                break;
            case "delete":
                handleDelete(arguments);
                saveTasksToDisk();
                break;
            default:
                throw new YodaException("Unknown command: \"" + command + "\"\n"
                        + "  Available commands: todo, deadline, event, mark, unmark, delete, list, bye");
        }
    }

    private static void handleList() throws YodaException {
        if (tasks.isEmpty()) {
            throw new YodaException("Your task list is empty. Add a task first!");
        }
        System.out.println("Here are the tasks in your list:");
        for (int index = 0; index < tasks.size(); index++) {
            System.out.println((index + 1) + "." + tasks.get(index));
        }
    }

    private static void handleMark(String arguments) throws YodaException {
        int taskIndex = tasks.getTaskIndex(arguments, "mark");
        tasks.get(taskIndex).markAsDone();
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + tasks.get(taskIndex));
    }

    private static void handleUnmark(String arguments) throws YodaException {
        int taskIndex = tasks.getTaskIndex(arguments, "unmark");
        tasks.get(taskIndex).markAsUndone();
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + tasks.get(taskIndex));
    }

    private static void handleTodo(String arguments) throws YodaException {
        if (arguments.isEmpty()) {
            throw new YodaException("Missing description!\n"
                    + "  Correct format: todo <description>\n"
                    + "  Example:        todo Read a book");
        }
        tasks.add(new Todo(arguments));
        printAddedTask(tasks.get(tasks.size() - 1), tasks.size());
    }

    private static void handleDeadline(String arguments) throws YodaException {
        if (arguments.isEmpty()) {
            throw new YodaException("Missing description and deadline!\n"
                    + "  Correct format: deadline <description> /by <time>\n"
                    + "  Example:        deadline Submit report /by Monday 6pm");
        }
        if (!arguments.contains(" /by ")) {
            throw new YodaException("Missing '/by' field!\n"
                    + "  Correct format: deadline <description> /by <time>\n"
                    + "  Example:        deadline Submit report /by Monday 6pm");
        }
        String[] deadlineParts = arguments.split(" /by ", 2);
        tasks.add(new Deadline(deadlineParts[0], deadlineParts[1]));
        printAddedTask(tasks.get(tasks.size() - 1), tasks.size());
    }

    private static void handleEvent(String arguments) throws YodaException {
        if (arguments.isEmpty()) {
            throw new YodaException("Missing description and event times!\n"
                    + "  Correct format: event <description> /from <start> /to <end>\n"
                    + "  Example:        event Team meeting /from Mon 2pm /to Mon 4pm");
        }
        if (!arguments.contains(" /from ")) {
            throw new YodaException("Missing '/from' field!\n"
                    + "  Correct format: event <description> /from <start> /to <end>\n"
                    + "  Example:        event Team meeting /from Mon 2pm /to Mon 4pm");
        }
        String[] eventParts = arguments.split(" /from ", 2);
        if (!eventParts[1].contains(" /to ")) {
            throw new YodaException("Missing '/to' field!\n"
                    + "  Correct format: event <description> /from <start> /to <end>\n"
                    + "  Example:        event Team meeting /from Mon 2pm /to Mon 4pm");
        }
        String description = eventParts[0];
        String[] timeParts = eventParts[1].split(" /to ", 2);
        tasks.add(new Event(description, timeParts[0], timeParts[1]));
        printAddedTask(tasks.get(tasks.size() - 1), tasks.size());
    }

    private static void handleDelete(String arguments) throws YodaException {
        if (arguments.isEmpty()) {
            throw new YodaException("Missing task number!\n"
                    + "  Correct format: delete <number>\n"
                    + "  Example:        delete 2");
        }
        int taskIndex = tasks.getTaskIndex(arguments, "delete");
        Task removed = tasks.remove(taskIndex);
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + removed);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    private static void printAddedTask(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }

    // =========================================================================
    // STORAGE LOGIC (LEVEL 7)
    // =========================================================================

    /**
     * Loads tasks from the hard disk file. Creates file/folder if not present.
     * Skips corrupted lines.
     */
    private static void loadTasksFromDisk() {
        try {
            ensureStorageExists();
            List<String> lines = Files.readAllLines(FILE_PATH);
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Task task = parseTaskFromLine(line);
                    if (task != null) {
                        tasks.add(task);
                    }
                } catch (Exception e) {
                    System.out.println("[Warning] Skipped corrupted data line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("[Warning] Failed to load data from " + FILE_PATH);
        }
    }

    /**
     * Saves all current tasks to disk.
     */
    private static void saveTasksToDisk() {
        try {
            ensureStorageExists();
            StringBuilder sb = new StringBuilder();
            for (Task task : tasks.getTasks()) {
                sb.append(task.toFileFormat()).append(System.lineSeparator());
            }
            Files.writeString(FILE_PATH, sb.toString());
        } catch (IOException e) {
            System.out.println("OOPS! Error occurred while saving tasks to file: " + e.getMessage());
        }
    }

    /**
     * Creates parent directory and empty file if they do not exist.
     */
    private static void ensureStorageExists() throws IOException {
        if (FILE_PATH.getParent() != null && !Files.exists(FILE_PATH.getParent())) {
            Files.createDirectories(FILE_PATH.getParent());
        }
        if (!Files.exists(FILE_PATH)) {
            Files.createFile(FILE_PATH);
        }
    }

    /**
     * Parses a single encoded line into a Task object.
     */
    private static Task parseTaskFromLine(String line) throws YodaException {
        String[] parts = line.split(" \\| ");
        if (parts.length < 3) {
            throw new YodaException("Corrupted format");
        }

        String type = parts[0].trim();
        boolean isDone = parts[1].trim().equals("1");
        String description = parts[2].trim();

        Task task;
        switch (type) {
            case "T":
                task = new Todo(description);
                break;
            case "D":
                if (parts.length < 4) {
                    throw new YodaException("Corrupted deadline format");
                }
                task = new Deadline(description, parts[3].trim());
                break;
            case "E":
                if (parts.length < 5) {
                    throw new YodaException("Corrupted event format");
                }
                task = new Event(description, parts[3].trim(), parts[4].trim());
                break;
            default:
                throw new YodaException("Unknown task type");
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }
}
