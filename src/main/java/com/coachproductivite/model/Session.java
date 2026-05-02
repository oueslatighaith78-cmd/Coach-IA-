package com.coachproductivite.model;

import java.time.LocalDateTime;

public class Session {

    private int id;
    private int idTache;
    private int duree;
    private LocalDateTime dateSession;

    public Session() {}

    public Session(int idTache, int duree) {
        this.idTache = idTache;
        this.duree = duree;
        this.dateSession = LocalDateTime.now();
    }

    public Session(int id, int idTache, int duree, LocalDateTime dateSession) {
        this.id = id;
        this.idTache = idTache;
        this.duree = duree;
        this.dateSession = dateSession;
    }

    public int getId() { return id; }
    public int getIdTache() { return idTache; }
    public int getDuree() { return duree; }
    public LocalDateTime getDateSession() { return dateSession; }

    public void setId(int id) { this.id = id; }
    public void setIdTache(int idTache) { this.idTache = idTache; }
    public void setDuree(int duree) { this.duree = duree; }
    public void setDateSession(LocalDateTime dateSession) { this.dateSession = dateSession; }

    @Override
    public String toString() {
        return "Session{id=" + id + ", idTache=" + idTache +
                ", duree=" + duree + "min}";
    }
}