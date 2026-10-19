package fr.novadeskt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    public enum Status {
        TODO, IN_PROGRESS, DONE
    }

    private final long id;
    private final long projectId;
    private String title;
    private String description;
    private Status status;
    private LocalDate dueDate;
    private final LocalDateTime createdAt;

    public Task(long id, long projectId, String title,
                String description, Status status,
                LocalDate dueDate, LocalDateTime createdAt) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }
        if (projectId <= 0) {
            throw new IllegalArgumentException("Le projet est invalide.");
        }

        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description == null ? "" : description;
        this.status = status == null ? Status.TODO : status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getProjectId() { return projectId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void rename(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
        this.status = status;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
