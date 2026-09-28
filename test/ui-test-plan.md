# Command-line UI test plan

The `test-ui` skill runs each case in a new temporary working directory. This
keeps task storage isolated, so a case must include every command needed to
set up its own state.

## Exit after startup
Aim: Verify that the application welcomes the user and exits politely.
Input:
```text
bye
```
Expected output:
```text
____________________________________________________________
__   __  ___  ____    _
\ \ / / / _ \|  _ \  / \
 \ V / | | | | | | |/ _ \
  | |  | |_| | |_| / ___ \
  |_|   \___/|____/_/   \_\
____________________________________________________________
Hello! I'm YODA.
What can I do for you?
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```

## Add and list a task
Aim: Verify that a new task is retained and displayed with its task number.
Input:
```text
todo read book
list
bye
```
Expected output:
```text
____________________________________________________________
__   __  ___  ____    _
\ \ / / / _ \|  _ \  / \
 \ V / | | | | | | |/ _ \
  | |  | |_| | |_| / ___ \
  |_|   \___/|____/_/   \_\
____________________________________________________________
Hello! I'm YODA.
What can I do for you?
____________________________________________________________
____________________________________________________________
Got it. I've added this task:
  [T][ ] read book
Now you have 1 tasks in the list.
____________________________________________________________
____________________________________________________________
Here are the tasks in your list:
1.[T][ ] read book
____________________________________________________________
____________________________________________________________
Bye. Hope to see you again soon!
____________________________________________________________
```
