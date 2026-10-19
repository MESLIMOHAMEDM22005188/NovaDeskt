package fr.novadeskt.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {
    @Test
    void shouldRejectBlankProjectName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Project(0, " ", "", null));
    }

    @Test
    void shouldAcceptValidProjectName() {
        Project project = new Project(1, "Portfolio", "", null);
        assertEquals("Portfolio", project.getName());
    }
}
