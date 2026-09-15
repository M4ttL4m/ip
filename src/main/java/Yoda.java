import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Yoda {

    private static final int MAX_TASKS = 100;
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String BANNER = "__   __  ___  ____    _\n"
            + "\\ \\ / / / _ \\|  _ \\  / \\\n"
            + " \\ V / | | | | | | |/ _ \\\n"
            + "  | |  | |_| | |_| / ___ \\\n"
            + "  |_|   \\___/|____/_/   \\_\\\n";

    // OS-independent relative path definition
    private static final Path FILE_PATH = Paths.get(".", "data", "yoda.txt");

    private static Task[] tasks = new Task[MAX_TASKS];
    private static int taskCount = 0;

    /**
     * Starts the YODA command-line application.
     */
    public static void main(String[] args) {
        printWelcome();
        loadTasksFromDisk();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split(" ", 2);
            String command = parts[0];
            String arguments = parts.length > 1 ? parts[1] : "";

            System.out.println(SEPARATOR);

            try {
                handleCommand(command, arguments);
            } catch (YodaException e) {
                System.out.println("OOPS! " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("OOPS! That's not a valid number.\n"
                        + "  Correct format: mark <number>  OR  unmark <number>\n"
                        + "  Example:        mark 2");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("OOPS! Your task list is full (max " + MAX_TASKS + " tasks). Please remove some tasks first.");
            }

            System.out.println(SEPARATOR);
        }
    }

    private static void printWelcome() {
        System.out.println(SEPARATOR);
        System.out.print(BANNER);
        System.out.println(SEPARATOR);
        System.out.println("Hello! I'm YODA.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);
    }

    private static void handleCommand(String command, String arguments) throws YodaException {
        switch (command) {
            case "bye":
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(SEPARATOR);
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
            default:
                throw new YodaException("Unknown command: \"" + command + "\"\n"
                        + "  Available commands: todo, deadline, event, mark, unmark, list, bye");
        }
    }

    private static void handleList() throws YodaException {
        if (taskCount == 0) {
            throw new YodaException("Your task list is empty. Add a task first!");
        }
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + "." + tasks[i]);
        }
    }

    private static void handleMark(String arguments) throws YodaException {
        int taskIndex = parseTaskIndex(arguments, "mark");
        tasks[taskIndex].markAsDone();
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + tasks[taskIndex]);
    }

    private static void handleUnmark(String arguments) throws YodaException {
        int taskIndex = parseTaskIndex(arguments, "unmark");
        tasks[taskIndex].markAsUndone();
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + tasks[taskIndex]);
    }

    private static void handleTodo(String arguments) throws YodaException {
        if (arguments.isEmpty()) {
            throw new YodaException("Missing description!\n"
                    + "  Correct format: todo <description>\n"
                    + "  Example:        todo Read a book");
        }
        tasks[taskCount] = new Todo(arguments);
        taskCount++;
        printAddedTask(tasks[taskCount - 1], taskCount);
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
        tasks[taskCount] = new Deadline(deadlineParts[0], deadlineParts[1]);
        taskCount++;
        printAddedTask(tasks[taskCount - 1], taskCount);
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
        tasks[taskCount] = new Event(description, timeParts[0], timeParts[1]);
        taskCount++;
        printAddedTask(tasks[taskCount - 1], taskCount);
    }

    private static int parseTaskIndex(String arguments, String command) throws YodaException {
        if (arguments.isEmpty()) {
            throw new YodaException("Missing task number!\n"
                    + "  Correct format: " + command + " <number>\n"
                    + "  Example:        " + command + " 2");
        }
        int taskIndex = Integer.parseInt(arguments) - 1;
        if (taskIndex < 0 || taskIndex >= taskCount) {
            throw new YodaException("Task " + (taskIndex + 1) + " does not exist.\n"
                    + "  You currently have " + taskCount + " task(s). Please enter a number between 1 and " + taskCount + ".");
        }
        return taskIndex;
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
                    if (task != null && taskCount < MAX_TASKS) {
                        tasks[taskCount] = task;
                        taskCount++;
                    }
                } catch (Exception e) {
                    // Corruption stretch goal: safely skip malformed line
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
            for (int i = 0; i < taskCount; i++) {
                sb.append(tasks[i].toFileFormat()).append(System.lineSeparator());
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
        // Line format example: T | 1 | read book
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
                break;            case "D":
                if (parts.length < 4) {
                    throw new YodaException("Corrupted deadline format");
                }
                task = new Deadline(description, parts[3].trim());
                break;            case "E":
                if (parts.length < 5) {
                    throw new YodaException("Corrupted event format");
                }
                task = new Event(description, parts[3].trim(), parts[4].trim());
                break;            default:
                throw new YodaException("Unknown task type");
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }
}