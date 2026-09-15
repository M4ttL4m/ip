import java.util.Scanner;
import java.util.ArrayList;

public class Yoda {

    private static final String SEPARATOR = "____________________________________________________________";
    private static final String BANNER = "__   __  ___  ____    _\n"
            + "\\ \\ / / / _ \\|  _ \\  / \\\n"
            + " \\ V / | | | | | | |/ _ \\\n"
            + "  | |  | |_| | |_| / ___ \\\n"
            + "  |_|   \\___/|____/_/   \\_\\\n";

    private static ArrayList<Task> tasks = new ArrayList<>();

    /**
     * Starts the YODA command-line application.
     */
    public static void main(String[] args) {
        printWelcome();

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
                break;
            case "unmark":
                handleUnmark(arguments);
                break;
            case "todo":
                handleTodo(arguments);
                break;
            case "deadline":
                handleDeadline(arguments);
                break;
            case "event":
                handleEvent(arguments);
                break;
            case "delete":
                handleDelete(arguments);
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
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    private static void handleMark(String arguments) throws YodaException {
        int taskIndex = parseTaskIndex(arguments, "mark");
        tasks.get(taskIndex).markAsDone();
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + tasks.get(taskIndex));
    }

    private static void handleUnmark(String arguments) throws YodaException {
        int taskIndex = parseTaskIndex(arguments, "unmark");
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
        int taskIndex = Integer.parseInt(arguments) - 1;
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new YodaException("Task " + (taskIndex + 1) + " does not exist.\n"
                    + "  You currently have " + tasks.size() + " task(s).");
        }
        Task removed = tasks.remove(taskIndex);
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + removed);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Parses and validates a task index from user input for mark/unmark commands.
     */
    private static int parseTaskIndex(String arguments, String command) throws YodaException {
        if (arguments.isEmpty()) {
            throw new YodaException("Missing task number!\n"
                    + "  Correct format: " + command + " <number>\n"
                    + "  Example:        " + command + " 2");
        }
        int taskIndex = Integer.parseInt(arguments) - 1;
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new YodaException("Task " + (taskIndex + 1) + " does not exist.\n"
                    + "  You currently have " + tasks.size() + " task(s). Please enter a number between 1 and " + tasks.size() + ".");
        }
        return taskIndex;
    }

    private static void printAddedTask(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }
}