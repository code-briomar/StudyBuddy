# StudyBuddy 📚

StudyBuddy is a command-line application written in Kotlin that helps students organize their academic tasks and build productive study habits.

## Features

### Task Management
- ✅ Create tasks with title, description, subject, priority, and deadline
- 📋 List and filter tasks by status, subject, priority, or deadline
- ✏️ Update task status (TODO, IN_PROGRESS, COMPLETED, CANCELLED)
- 🗑️ Delete tasks
- ⚠️ View overdue tasks

### Study Session Tracking
- 🎯 Start and end study sessions
- ⏱️ Track study time by subject
- 📊 View study statistics and productivity metrics
- 📝 Add notes to completed study sessions

### Task Priorities
- URGENT
- HIGH
- MEDIUM
- LOW

## Building the Application

To build the application, use Gradle:

```bash
./gradlew build
```

## Running the Application

You can run the application using:

```bash
./gradlew run
```

Or use the distribution scripts:

```bash
./app/build/install/app/bin/app
```

## Usage

When you start StudyBuddy, you'll see an interactive menu with the following options:

1. **Create a new task** - Add academic tasks with details like subject, priority, and deadline
2. **List all tasks** - View all tasks with filtering options
3. **Update task status** - Mark tasks as in progress, completed, or cancelled
4. **Delete a task** - Remove tasks from your list
5. **Start study session** - Begin tracking a study session for a subject
6. **End study session** - Complete your study session with notes
7. **View study statistics** - See your total study time and productivity metrics
8. **View overdue tasks** - Check which tasks are past their deadline
9. **Exit** - Close the application

## Example Workflow

1. Start StudyBuddy
2. Create a task: "Complete Math Assignment" with subject "Mathematics" and priority "HIGH"
3. Start a study session for "Mathematics"
4. Study and complete your assignment
5. End the study session with notes
6. Update the task status to "COMPLETED"
7. View your study statistics to track progress

## Testing

Run the test suite:

```bash
./gradlew test
```

## Project Structure

```
app/src/main/kotlin/studybuddy/
├── model/           # Domain models (Task, StudySession)
├── service/         # Business logic (TaskService, StudySessionService)
├── cli/             # Command-line interface
└── App.kt           # Main application entry point
```

## Requirements

- Java 17 or higher
- Gradle 9.2.0 or higher

## License

See LICENSE file for details.

