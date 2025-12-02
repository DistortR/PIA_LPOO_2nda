package com.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClaseGrupal implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String description;
    private LocalDate year;
    private LocalDate day;
    private LocalTime hour;

    public ClaseGrupal(String id, String description, LocalDate year, LocalDate day, LocalTime hour) {
        this.id = id;
        this.description = description;
        this.year = year;
        this.day = day;
        this.hour = hour;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getYear() { return year; }
    public void setYear(LocalDate year) { this.year = year; }

    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }

    public LocalTime getHour() { return hour; }
    public void setHour(LocalTime hour) { this.hour = hour; }

    @Override
    public String toString() {
        return description + " | " + day + " | " + hour + " | " + year;
    }
}