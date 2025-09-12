# Development Workflow

## Execution Cycle

1. **Prompt for Feature**

   - Ask the user for a **new feature name** and **feature ID**. after getting them, proceed to next step.

2. **Create Task Files**

   - show the plan and get for approval.
   - Break the feature into required tasks.
   - Save each task as a separate file in:
     ```
     /docs/development/backlog/
     ```
   - **File naming convention**:

     ```
     todo_(feature id)_(task priority number)_(feature-name)_(task-title).md

     ```

   - Example:
     ```
     todo_FT-001_1_User-Management_Create-User-API.md
     ```

3. **Select Next Task**

   - show the user the list of task files in `/docs/development/backlog/` that with filename start with todo.
   - ask user to select which one from the list to implement next.

4. **Implement Task**

   - Implement the feature described in the task file.
   - Write and run **unit/integration tests**.

5. **Mark Task as Done**

   - Update the file name:
     - Replace `todo` with `done`.
   - Example:
     ```
     done_FT-001_1_User-Management_Create-User-API.md
     ```

6. **Git Commit**

   - Commit changes using the task file name as the commit message:
     ```
     git commit -m "done_FT-001_1.User-Management_Create-User-API"
     git push
     ```

7. **Repeat Cycle**
   - Go back to **Step 3** until no more `todo*` files remain.
