package com.gestion.parking.model;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private int id;
    private int placeId;        
    private int vehiculeId;     
    private LocalDateTime entree;   
    private LocalDateTime sortie;   
    private double montant;
    
    private Place place;
    private Vehicule vehicule;
    
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // Constructeurs
    public Ticket() {}
    
    public Ticket(int id, int placeId, int vehiculeId, LocalDateTime entree, 
                 LocalDateTime sortie, double montant) {
        this.id = id;
        this.placeId = placeId;
        this.vehiculeId = vehiculeId;
        this.entree = entree;
        this.sortie = sortie;
        this.montant = montant;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getPlaceId() {
        return placeId;
    }
    
    public int getVehiculeId() {
        return vehiculeId;
    }
    
    public LocalDateTime getEntree() {
        return entree;
    }
    
    public LocalDateTime getSortie() {
        return sortie;
    }
    
    public double getMontant() {
        return montant;
    }
    
    public Place getPlace() {
        return place;
    }
    
    public Vehicule getVehicule() {
        return vehicule;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }
    
    public void setVehiculeId(int vehiculeId) {
        this.vehiculeId = vehiculeId;
    }
    
    public void setEntree(LocalDateTime entree) {
        this.entree = entree;
    }
    
    public void setSortie(LocalDateTime sortie) {
        this.sortie = sortie;
    }
    
    public void setMontant(double montant) {
        this.montant = montant;
    }
    
    public void setPlace(Place place) {
        this.place = place;
        if (place != null) {
            this.placeId = place.getId();
        }
    }
    
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        if (vehicule != null) {
            this.vehiculeId = vehicule.getId();
        }
    }
    
    // Méthodes métier
    public long getDureeMinutes() {
        LocalDateTime endTime = (sortie != null) ? sortie : LocalDateTime.now();
        if (entree == null) return 0;
        return Duration.between(entree, endTime).toMinutes();
    }
    
    public long getDureeHeures() {
        return getDureeMinutes() / 60;
    }
    
    public boolean estEnCours() {
        return sortie == null;
    }
    
    public boolean estTermine() {
        return sortie != null;
    }
    
    public void calculerMontant(double tarifHoraire) {
        if (montant > 0 || sortie == null) return;
        
        long heures = getDureeHeures();
        if (getDureeMinutes() % 60 > 0) {
            heures++; 
        }
        
        this.montant = heures * tarifHoraire;
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", placeId=" + placeId +
                ", vehiculeId=" + vehiculeId +
                ", entree=" + (entree != null ? entree.format(formatter) : "null") +
                ", sortie=" + (sortie != null ? sortie.format(formatter) : "null") +
                ", montant=" + montant + "€" +
                '}';
    }
    
    public String getDureeFormatee() {
        long minutes = getDureeMinutes();
        long heures = minutes / 60;
        long minutesRestantes = minutes % 60;
        
        if (heures > 0) {
            return heures + "h " + minutesRestantes + "min";
        }
        return minutes + "min";
    }
}