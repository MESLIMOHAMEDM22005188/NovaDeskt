
package fr.novadeskt.service;

import fr.novadeskt.model.Task;
import fr.novadeskt.repository.TaskRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException(
                    "Le repository ne peut pas être null."
            );
        }
        this.repository = repository;
    }

    public Task create(long projectId, String title, String description)
            throws SQLException {

        validateId(projectId);

        String validatedTitle = validateTitle(title);
        String validatedDescription = normalizeDescription(description);

        Task task = new Task(
                0,
                projectId,
                validatedTitle,
                validatedDescription,
                Task.Status.TODO,
                null,
                null
        );

        return repository.save(task);
    }

    public List<Task> getByProject(long projectId) throws SQLException {
        validateId(projectId);
        return repository.findByProjectId(projectId);
    }

    public Optional<Task> getById(long id) throws SQLException {
        validateId(id);
        return repository.findById(id);
    }

    public Task update(long id, String title, String description,
                       Task.Status status, java.time.LocalDate dueDate)
            throws SQLException {

        validateId(id);

        String validatedTitle = validateTitle(title);
        String validatedDescription = normalizeDescription(description);

        if (status == null) {
            throw new IllegalArgumentException(
                    "Le statut de la tâche est obligatoire."
            );
        }

        Task existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune tâche trouvée avec l'identifiant " + id + "."
                ));

        Task updated = new Task(
                existing.getId(),
                existing.getProjectId(),
                validatedTitle,
                validatedDescription,
                status,
                dueDate,
                existing.getCreatedAt()
        );

        if (!repository.update(updated)) {
            throw new SQLException("La modification de la tâche a échoué.");
        }

        return repository.findById(id)
                .orElseThrow(() -> new SQLException(
                        "La tâche modifiée est introuvable."
                ));
    }

    public void delete(long id) throws SQLException {
        validateId(id);

        if (!repository.deleteById(id)) {
            throw new IllegalArgumentException(
                    "Aucune tâche trouvée avec l'identifiant " + id + "."
            );
        }
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Le titre de la tâche est obligatoire."
            );
        }

        String normalized = title.trim();

        if (normalized.length() > 150) {
            throw new IllegalArgumentException(
                    "Le titre ne peut pas dépasser 150 caractères."
            );
        }

        return normalized;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }

        String normalized = description.trim();

        if (normalized.length() > 2000) {
            throw new IllegalArgumentException(
                    "La description ne peut pas dépasser 2000 caractères."
            );
        }

        return normalized;
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant doit être positif."
            );
        }
    }
}
