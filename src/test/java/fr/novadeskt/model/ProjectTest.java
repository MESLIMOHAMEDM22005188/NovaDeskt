package fr.novadeskt.model;

import fr.novadeskt.model.Project;
import fr.novadeskt.repository.ProjectRepository;
import fr.novadeskt.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    private InMemoryProjectRepository repository;
    private ProjectService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProjectRepository();
        service = new ProjectService(repository);
    }

    @Test
    void shouldCreateProjectWithTrimmedName() throws SQLException {
        Project project = service.create("  NovaDeskt  ", "  Description  ");

        assertEquals("NovaDeskt", project.getName());
        assertEquals("Description", project.getDescription());
        assertTrue(project.getId() > 0);
    }

    @Test
    void shouldRejectBlankProjectName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.create("   ", "Description")
        );
    }

    @Test
    void shouldRejectNullProjectName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(null, "Description")
        );
    }

    @Test
    void shouldRejectProjectNameLongerThan100Characters() {
        String name = "a".repeat(101);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(name, "")
        );
    }

    @Test
    void shouldNormalizeNullDescription() throws SQLException {
        Project project = service.create("Projet", null);

        assertEquals("", project.getDescription());
    }

    @Test
    void shouldRejectDescriptionLongerThan2000Characters() {
        String description = "a".repeat(2001);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create("Projet", description)
        );
    }

    @Test
    void shouldFindCreatedProjectById() throws SQLException {
        Project created = service.create("Projet", "Description");

        Optional<Project> result = service.getById(created.getId());

        assertTrue(result.isPresent());
        assertEquals(created.getName(), result.orElseThrow().getName());
    }

    @Test
    void shouldRejectInvalidProjectId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(0)
        );
    }

    @Test
    void shouldUpdateExistingProject() throws SQLException {
        Project created = service.create("Ancien nom", "Ancienne description");

        Project updated = service.update(
                created.getId(),
                "Nouveau nom",
                "Nouvelle description"
        );

        assertEquals("Nouveau nom", updated.getName());
        assertEquals("Nouvelle description", updated.getDescription());
        assertEquals(created.getId(), updated.getId());
    }

    @Test
    void shouldRejectUpdateOfUnknownProject() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.update(999, "Projet", "")
        );
    }

    @Test
    void shouldDeleteExistingProject() throws SQLException {
        Project created = service.create("Projet", "");

        service.delete(created.getId());

        assertTrue(service.getById(created.getId()).isEmpty());
    }

    @Test
    void shouldRejectDeletionOfUnknownProject() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.delete(999)
        );
    }

    private static class InMemoryProjectRepository
            extends ProjectRepository {

        private final List<Project> projects = new ArrayList<>();
        private long nextId = 1;

        @Override
        public Project save(Project project) {
            Project saved = new Project(
                    nextId++,
                    project.getName(),
                    project.getDescription(),
                    LocalDateTime.now()
            );

            projects.add(saved);
            return saved;
        }

        @Override
        public List<Project> findAll() {
            return new ArrayList<>(projects);
        }

        @Override
        public Optional<Project> findById(long id) {
            return projects.stream()
                    .filter(project -> project.getId() == id)
                    .findFirst();
        }

        @Override
        public boolean update(Project updatedProject) {
            for (int i = 0; i < projects.size(); i++) {
                if (projects.get(i).getId() == updatedProject.getId()) {
                    projects.set(i, updatedProject);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean deleteById(long id) {
            return projects.removeIf(project -> project.getId() == id);
        }
    }
}
