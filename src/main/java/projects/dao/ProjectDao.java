/**
 * 
 */
package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

/**
 * Uses JDBC to perform CRUD operations on the project tables.
 * 
 */
public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	/**
	 * Inserts a project row into the project table.
	 * 
	 * @param project The project object to insert.
	 * @return The Project with the primary key.
	 * @throws DbException Thrown if an error occur.
	 */
	public Project insertProject(Project project) {
	// @formatter:off
    String sql = ""
        + "INSERT INTO " + PROJECT_TABLE + " "
        + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
        + "VALUES "
        + "(?, ?, ?, ?, ?)";
    // @formatter:on

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);

				stmt.executeUpdate();

				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);

				project.setProjectId(projectId);
				return project;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Uses JDBC methods to retrieve all project rows from the project table. It
	 * does not retrieve any materials, steps, or categories. The project rows are
	 * ordered by project name.
	 * 
	 * @return The list of projects.
	 * @throws DbException Thrown if a SQLException is thrown by the driver.
	 */
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				try (ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();

					while (rs.next()) {
						projects.add(extract(rs, Project.class));

					}

					return projects;
				}
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Used JDBC method calls to retrieve a single project row, along with its
	 * associated materials, steps, and categories.
	 * 
	 * @param projectId The ID of the project to retrieve.
	 * @return An Optional with the requested project if successful. If the project
	 *         ID is invalid, an empty Optional is returned.
	 * @throws DbException Thrown if a SQLException is returned by the driver.
	 */
	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try {
				Project project = null;

				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class);

					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							project = extract(rs, Project.class);
						}
					}
				}

				if (Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}

				commitTransaction(conn);

				return Optional.ofNullable(project);
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * This method retrieves all the categories associated with the user provided
	 * project ID.
	 * 
	 * @param conn      The Connection object supplied by the caller.
	 * @param projectId The project ID to use for the categories.
	 * @return A list of categories if successful.
	 * @throws DbException Thrown if an exception is thrown by the driver.
	 */
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) {
	// @formatter:off
    String sql = ""
        + "SELECT c.* FROM " + CATEGORY_TABLE + " c "
        + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
        + "WHERE project_id = ?";
    // @formatter:on

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>();

				while (rs.next()) {
					categories.add(extract(rs, Category.class));
				}

				return categories;
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * Uses JDBC method calls to retrieve project steps for the given project ID.
	 * 
	 * @param conn      The caller-supplied connection.
	 * @param projectId The project ID used to retrieve the steps.
	 * @return A list of steps in step order.
	 * @throws SQLException Thrown if the database driver encounters an error.
	 */
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();

				while (rs.next()) {
					steps.add(extract(rs, Step.class));
				}

				return steps;
			}
		}
	}

	/**
	 * Uses JDBC method calls to retrieve project materials for the given project
	 * ID.
	 * 
	 * @param conn      The caller-supplied connection.
	 * @param projectId The project ID used to retrieve the materials.
	 * @return A list of materials.
	 * @throws SQLException Thrown if the database driver encounters an error.
	 */
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>();

				while (rs.next()) {
					materials.add(extract(rs, Material.class));
				}

				return materials;
			}
		}
	}

	/**
	 * Uses JDBC calls to modify the project details. An UPDATE statement is used
	 * for this.
	 * 
	 * @param project The project object with modified details.
	 * @return {@code true} if the project was modified successfully. {@code false}
	 *         if an invalid project ID is supplied.
	 * @throws DbException Thrown if a SQLException is thrown by the driver.
	 */
	public boolean modifyProjectDetails(Project project) {
	// @formatter:off
    String sql = ""
        + "UPDATE " + PROJECT_TABLE + " SET "
        + "project_name = ?, "
        + "estimated_hours = ?, "
        + "actual_hours = ?, "
        + "difficulty = ?, "
        + "notes = ? "
        + "WHERE project_id = ?";
    // @formatter:on

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);

				boolean modified = stmt.executeUpdate() == 1;
				commitTransaction(conn);

				return modified;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

	/**
	 * deletes the project row from the project table if the project ID is found in
	 * an existing row. All child rows are deleted as well because the foreign keys
	 * in those tables were created using ON DELETE CASCADE.
	 * 
	 * @param projectId The project ID of the project to delete.
	 * @return {@code true} if the project was deleted. {@code false} if an invalid
	 *         project ID is supplied.
	 * @throws DbException Thrown if the driver throws a {@link SQLException}.
	 */
	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);

			
				boolean deleted = stmt.executeUpdate() == 1;

				commitTransaction(conn);
				return deleted;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}
	}

}
