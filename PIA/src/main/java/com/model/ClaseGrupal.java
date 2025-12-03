package com.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClaseGrupal implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String description;
    private LocalDate date;
    private LocalTime hour;

    public ClaseGrupal(String id, String description, LocalDate date, LocalTime hour) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.hour = hour;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHour() { return hour; }
    public void setHour(LocalTime hour) { this.hour = hour; }

    @Override
    public String toString() {
        return description + " | " + date + " | " + hour;
    }
}