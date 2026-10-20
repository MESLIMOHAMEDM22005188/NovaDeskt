package fr.novadeskt.model;

import java.time.LocalDateTime;

public class Project {
    private final long id;
    private String name;
    private String description;
    private final LocalDateTime createdAt;

    public Project(long id, String name, String description,
                   LocalDateTime createdAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }

        this.id = id;
        this.name = name;
        this.description = description == null ? "" : description;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }
}
