
import java.util.Scanner;


/**
 * Starts the YODA command-line application.
 */
public class Yoda {

    public static void main(String[] args) {
        String separator = "____________________________________________________________";
        String banner = "__   __  ___  ____    _\n"
                + "\\ \\ / / / _ \\|  _ \\  / \\\n"
                + " \\ V / | | | | | | |/ _ \\\n"
                + "  | |  | |_| | |_| / ___ \\\n"
                + "  |_|   \\___/|____/_/   \\_\\\n";

        System.out.println(separator);
        System.out.print(banner);
        System.out.println(separator);
        System.out.println("Hello! I'm YODA.");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        Task[] tasks = new Task[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }


            String[] parts = input.split(" ", 2);
            String command = parts[0];
            String arguments = parts.length > 1 ? parts[1] : "";

            System.out.println(separator);

            switch (command) {
                case "bye":
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(separator);
                    return;

                case "list":
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println((i + 1) + "." + tasks[i]);
                    }
                    break;

                case "mark": {
                    int taskIndex = Integer.parseInt(arguments) - 1;
                    tasks[taskIndex].markAsDone();
                    System.out.println("Nice! I've marked this task as done:");
                    System.out.println("  " + tasks[taskIndex]);
                    break;
                }

                case "unmark": {
                    int taskIndex = Integer.parseInt(arguments) - 1;
                    tasks[taskIndex].markAsUndone();
                    System.out.println("OK, I've marked this task as not done yet:");
                    System.out.println("  " + tasks[taskIndex]);
                    break;
                }

                case "todo":
                    tasks[taskCount] = new Todo(arguments);
                    taskCount++;
                    printAddedTask(tasks[taskCount - 1], taskCount);
                    break;

                case "deadline": {
                    String[] deadlineParts = arguments.split(" /by ");
                    tasks[taskCount] = new Deadline(deadlineParts[0], deadlineParts[1]);
                    taskCount++;
                    printAddedTask(tasks[taskCount - 1], taskCount);
                    break;
                }

                case "event": {
                    String[] eventParts = arguments.split(" /from ");
                    String description = eventParts[0];
                    String[] timeParts = eventParts[1].split(" /to ");

                    tasks[taskCount] = new Event(description, timeParts[0], timeParts[1]);
                    taskCount++;
                    printAddedTask(tasks[taskCount - 1], taskCount);
                    break;
                }

                default:
                    System.out.println("Unknown command!");
                    break;
            }

            System.out.println(separator);
        }
    }


    private static void printAddedTask(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }
}