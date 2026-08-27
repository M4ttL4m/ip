
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
            String command = scanner.nextLine();
            System.out.println(separator);

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(separator);
                break;
            }
            else if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println((i + 1) + ". " + tasks[i]);
                }
            }
            else if (command.startsWith("mark ")) {

                int taskIndex = Integer.parseInt(command.substring(5)) - 1;

                tasks[taskIndex].markAsDone();

                System.out.println("     Nice! I've marked this task as done:");
                System.out.println("       " + tasks[taskIndex]);
            }
            else if (command.startsWith("unmark ")) {

                int taskIndex = Integer.parseInt(command.substring(7)) - 1;

                tasks[taskIndex].markAsUndone();

                System.out.println("     Nice! I've marked this task as not done yet:");
                System.out.println("       " + tasks[taskIndex]);
            }
            else {
                tasks[taskCount] = new Task(command);
                taskCount++;
                for (int i = 0; i < taskCount; i++) {
                    System.out.println((i + 1) + ". " + tasks[i]);
                }
            }


            System.out.println(separator);
        }
    }
}
