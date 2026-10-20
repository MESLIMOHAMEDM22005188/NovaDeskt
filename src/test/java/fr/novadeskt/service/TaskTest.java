package fr.novadeskt.service;

import fr.novadeskt.model.Task;
import fr.novadeskt.repository.TaskRepository;
import fr.novadeskt.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private TaskService service;
    private InMemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
        service = new TaskService(repository);
    }

    @Test
    void shouldCreateTaskWithTodoStatus() throws SQLException {
        Task task = service.create(1, "Préparer le rapport", "Rapport mensuel");

        assertEquals(1, task.getProjectId());
        assertEquals("Préparer le rapport", task.getTitle());
        assertEquals("Rapport mensuel", task.getDescription());
        assertEquals(Task.Status.TODO, task.getStatus());
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(1, "  ", "Description"));
    }

    @Test
    void shouldRejectInvalidProjectId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(0, "Ma tâche", ""));
    }

    @Test
    void shouldFindTasksByProject() throws SQLException {
        service.create(1, "Tâche A", "");
        service.create(2, "Tâche B", "");

        List<Task> tasks = service.getByProject(1);

        assertEquals(1, tasks.size());
        assertEquals("Tâche A", tasks.get(0).getTitle());
    }

    @Test
    void shouldFindTaskById() throws SQLException {
        Task created = service.create(1, "Ma tâche", "");

        Optional<Task> result = service.getById(created.getId());

        assertTrue(result.isPresent());
        assertEquals("Ma tâche", result.get().getTitle());
    }

    @Test
    void shouldUpdateTask() throws SQLException {
        Task created = service.create(1, "Ancien titre", "");

        Task updated = service.update(
                created.getId(),
                "Nouveau titre",
                "Nouvelle description",
                Task.Status.IN_PROGRESS,
                LocalDate.of(2026, 11, 1)
        );

        assertEquals("Nouveau titre", updated.getTitle());
        assertEquals("Nouvelle description", updated.getDescription());
        assertEquals(Task.Status.IN_PROGRESS, updated.getStatus());
        assertEquals(LocalDate.of(2026, 11, 1), updated.getDueDate());
    }

    @Test
    void shouldDeleteTask() throws SQLException {
        Task created = service.create(1, "À supprimer", "");

        service.delete(created.getId());

        assertTrue(service.getById(created.getId()).isEmpty());
    }

    private static class InMemoryTaskRepository extends TaskRepository {
        private final List<Task> tasks = new ArrayList<>();
        private long nextId = 1;

        @Override
        public Task save(Task task) {
            Task saved = new Task(
                    nextId++,
                    task.getProjectId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getDueDate(),
                    task.getCreatedAt() == null
                            ? LocalDateTime.now()
                            : task.getCreatedAt()
            );
            tasks.add(saved);
            return saved;
        }

        @Override
        public List<Task> findByProjectId(long projectId) {
            return tasks.stream()
                    .filter(task -> task.getProjectId() == projectId)
                    .toList();
        }

        @Override
        public Optional<Task> findById(long id) {
            return tasks.stream()
                    .filter(task -> task.getId() == id)
                    .findFirst();
        }

        @Override
        public boolean update(Task task) {
            Optional<Task> existing = findById(task.getId());

            if (existing.isEmpty()) {
                return false;
            }

            tasks.remove(existing.get());
            tasks.add(task);
            return true;
        }

        @Override
        public boolean deleteById(long id) {
            return tasks.removeIf(task -> task.getId() == id);
        }
    }
}
