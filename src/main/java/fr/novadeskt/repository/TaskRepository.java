
package fr.novadeskt.repository;

import fr.novadeskt.config.Database;
import fr.novadeskt.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {

    public Task save(Task task) throws SQLException {
        String sql = """
                INSERT INTO tasks(project_id, title, description, status, due_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, task.getProjectId());
            statement.setString(2, task.getTitle());
            statement.setString(3, task.getDescription());
            statement.setString(4, task.getStatus().name());
            statement.setString(5, task.getDueDate() == null
                    ? null : task.getDueDate().toString());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(connection, keys.getLong(1))
                            .orElseThrow(() ->
                                    new SQLException("Tâche créée mais introuvable."));
                }
            }
        }

        throw new SQLException("Création de la tâche impossible.");
    }

    public List<Task> findByProjectId(long projectId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE project_id = ? ORDER BY id DESC";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, projectId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    tasks.add(map(result));
                }
            }
        }

        return tasks;
    }

    public Optional<Task> findById(long id) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            return findById(connection, id);
        }
    }

    public boolean update(Task task) throws SQLException {
        String sql = """
                UPDATE tasks
                SET title = ?, description = ?, status = ?, due_date = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getStatus().name());
            statement.setString(4, task.getDueDate() == null
                    ? null : task.getDueDate().toString());
            statement.setLong(5, task.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Optional<Task> findById(Connection connection, long id)
            throws SQLException {

        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(map(result));
                }
            }
        }

        return Optional.empty();
    }

    private Task map(ResultSet result) throws SQLException {
        String created = result.getString("created_at");
        String due = result.getString("due_date");

        LocalDateTime createdAt = created == null
                ? null : LocalDateTime.parse(created.replace(" ", "T"));

        return new Task(
                result.getLong("id"),
                result.getLong("project_id"),
                result.getString("title"),
                result.getString("description"),
                Task.Status.valueOf(result.getString("status")),
                due == null ? null : LocalDate.parse(due),
                createdAt
        );
    }
}
