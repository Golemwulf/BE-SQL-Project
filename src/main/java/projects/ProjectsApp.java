package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
	private Scanner scanner = new Scanner(System.in);
	private ProjectService projectService = new ProjectService();
	private Project curProject;

	/**
	 * Shows the available options to user. The methods are created below.
	 */
	// @formatter:off
  private List<String> operations = List.of(
      "1) Add a project",
      "2) List projects",
      "3) Select a project",
      "4) Update project details",
      "5) Delete a project"
  );
  // @formatter:on

	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
	}

	/**
	 * /** Switch statement for menu so user can select operation.
	 */

	private void processUserSelections() {
		boolean done = false;

		while (!done) {
			try {
				int selection = getUserSelection();

				switch (selection) {
				case -1:
					done = exitMenu();
					break;

				case 1:
					createProject();
					break;

				case 2:
					listProjects();
					break;

				case 3:
					selectProject();
					break;

				case 4:
					updateProjectDetails();
					break;

				case 5:
					deleteProject();
					break;

				default:
					System.out.println("\n" + selection + " is not a valid selection. Try again.");
					break;
				}
			} catch (Exception e) {
				System.out.println("\nError: " + e + " Try again.");
			}
		}
	}

	/**
	 * Deletes existing project
	 * 
	 * @return
	 */
	private void deleteProject() {
		listProjects();

		Integer projectId = getIntInput("Enter the ID of the project to delete");

		projectService.deleteProject(projectId);
		System.out.println("Project " + projectId + " was deleted successfully.");

		if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
			curProject = null;
		}
	}

	/**
	 * Modifies existing project
	 * 
	 * @return
	 */
	private void updateProjectDetails() {
		/* If there is no current project selected, return to the menu. */
		if (Objects.isNull(curProject)) {
			System.out.println("\nPlease select a project.");
			return;
		}

		/*
		 * Collect input from the user. If the user presses Enter without entering a
		 * value, the local variable will be null.
		 */
		String projectName = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");

		BigDecimal estimatedHours = getDecimalInput(
				"Enter the estimated hours [" + curProject.getEstimatedHours() + "]");

		BigDecimal actualHours = getDecimalInput("Enter the actual hours + [" + curProject.getActualHours() + "]");

		Integer difficulty = getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");

		String notes = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");

		Project project = new Project();

		project.setProjectId(curProject.getProjectId());
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);

		project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);

		project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);

		/* Call the project service to update the project details. */
		projectService.modifyProjectDetails(project);

		/* Re-read the current project, which will display the new details. */
		curProject = projectService.fetchProjectById(curProject.getProjectId());
	}

	/**
	 * selects and existing project by ID
	 */
	private void selectProject() {
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to select a project");

		curProject = null;

		curProject = projectService.fetchProjectById(projectId);
	}

	/**
	 * Lists everything currently on the Projects table
	 */
	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();

		System.out.println("\nProjects:");

		projects.forEach(
				project -> System.out.println("   " + project.getProjectId() + ": " + project.getProjectName()));
	}

	/**
	 * Gather user input for a project row then call the project service to create
	 * the row.
	 */
	private void createProject() {
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
		String notes = getStringInput("Enter the project notes");

		Project project = new Project();

		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);

		Project dbProject = projectService.addProject(project);
		System.out.println("You have successfully created project: " + dbProject);
	}

	/**
	 * Gets the user's input from the console and converts it to a BigDecimal.
	 * 
	 * @param prompt The prompt to display on the console.
	 * @return A BigDecimal value if successful.
	 * @throws DbException Thrown if an error occurs converting the number to a
	 *                     BigDecimal.
	 */
	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}

	/**
	 * Called when the user wants to exit the application. It prints a message and
	 * returns
	 * 
	 * 
	 * 
	 */
	private boolean exitMenu() {
		System.out.println("Exiting the menu.");
		return true;
	}

	/**
	 * processes the user's selection input to determine which operation to run
	 * next.
	 * 
	 * @return
	 */
	private int getUserSelection() {
		printOperations();

		Integer input = getIntInput("Enter a menu selection");

		return Objects.isNull(input) ? -1 : input;
	}

	/*
	 * This and the next operation collect the input values from the user.
	 */
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return Integer.valueOf(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = scanner.nextLine();

		return input.isBlank() ? null : input.trim();
	}

	/**
	 * Prints the line provided and includes 3 spaces at beginning for formatting.
	 * 
	 * @param line The line to print.
	 */
	private void printWithIndent(String line) {
		System.out.println("   " + line);
	}

	/**
	 * Print the menu selections
	 */
	private void printOperations() {
		System.out.println("\nThese are the available selections. Press the Enter key to quit:");
		operations.forEach(line -> printWithIndent(line));

		if (Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou are working with project: " + curProject);
		}
	}
}
