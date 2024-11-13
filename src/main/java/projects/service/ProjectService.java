/**
 * 
 */
package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
	private ProjectDao projectDao = new ProjectDao();

	/**
	 * Adds project
	 * 
	 * @param project
	 * @return
	 */

	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

	/**
	 * fetches all of the projects
	 * 
	 * @return
	 */
	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}

	/**
	 * fetches the project based on the ID provided by user.
	 * 
	 * @return
	 */
	public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId).orElseThrow(
				() -> new NoSuchElementException("Project with project ID=" + projectId + " does not exist."));
	}

	/**
	 * Modifies existing project
	 * 
	 * @return
	 */
	public void modifyProjectDetails(Project project) {
		if (!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
		}
	}

	/**
	 * Deletes existing project
	 * 
	 * @return
	 */
	public void deleteProject(Integer projectId) {
		if (!projectDao.deleteProject(projectId)) {
			throw new DbException("Project with ID=" + projectId + " does not exist.");
		}
	}
}
