package fr.novadeskt.service;

import fr.novadeskt.model.Task;
import fr.novadeskt.repository.TaskRepository;

import java.sql.SQLException;
import java.util.List;

public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(long projectId, String title, String description)
            throws SQLException {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }

        Task task = new Task(
                0, projectId, title.trim(), description,
                Task.Status.TODO, null, null);

        return repository.save(task);
    }

    public List<Task> getByProject(long projectId) throws SQLException {
        return repository.findByProjectId(projectId);
    }
}
