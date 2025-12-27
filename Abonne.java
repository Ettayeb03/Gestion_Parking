package com.gestion.parking.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public class Abonne {
    private int id;
    private int vehiculeId;   
    private LocalDate dateDebut;
    private LocalDate dateFin;
    
    private Vehicule vehicule;
    
    // Constante pour le prix mensuel
    private static final double PRIX_MENSUEL = 700.0;
    
    // Constructeurs
    public Abonne() {}
    
    public Abonne(int id, int vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        this.id = id;
        this.vehiculeId = vehiculeId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getVehiculeId() {
        return vehiculeId;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public LocalDate getDateFin() {
        return dateFin;
    }
    
    public Vehicule getVehicule() {
        return vehicule;
    }
    
    public static double getPrixMensuel() {
        return PRIX_MENSUEL;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setVehiculeId(int vehiculeId) {
        this.vehiculeId = vehiculeId;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        if (vehicule != null) {
            this.vehiculeId = vehicule.getId();
        }
    }
    
    // Méthode pour calculer la durée en mois
    public int getDureeMois() {
        if (dateDebut == null || dateFin == null) {
            return 0;
        }
        Period period = Period.between(dateDebut, dateFin);
        return period.getYears() * 12 + period.getMonths() ; 
    }
    
    // Méthode pour calculer le montant total
    public double getMontantTotal() {
        return getDureeMois() * PRIX_MENSUEL;
    }
    
    // Méthode pour calculer le montant payé
    public double getMontantPaye() {
        if (estExpire()) {
            return getMontantTotal();
        }
        
        if (!estValide()) {
            return 0.0;
        }
        
        LocalDate aujourdhui = LocalDate.now();
        Period periodPaye = Period.between(dateDebut, aujourdhui);
        int moisPayes = periodPaye.getYears() * 12 + periodPaye.getMonths();
        if (aujourdhui.getDayOfMonth() >= dateDebut.getDayOfMonth()) {
            moisPayes += 1;
        }
        
        return Math.min(moisPayes, getDureeMois()) * PRIX_MENSUEL;
    }
    
    // Méthodes métier
    public boolean estValide() {
        if (dateDebut == null || dateFin == null) {
            return false;
        }
        LocalDate aujourdhui = LocalDate.now();
        return !aujourdhui.isBefore(dateDebut) && !aujourdhui.isAfter(dateFin);
    }
    
    public boolean estExpire() {
        if (dateFin == null) {
            return false;
        }
        return LocalDate.now().isAfter(dateFin);
    }
    
    @Override
    public String toString() {
        return "Abonne{" +
                "id=" + id +
                ", vehiculeId=" + vehiculeId +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", dureeMois=" + getDureeMois() +
                ", montantTotal=" + getMontantTotal() +
                ", estValide=" + estValide() +
                '}';
    }
    
    public String getProprietaire() {
        return (vehicule != null) ? vehicule.getProprietaire() : "Inconnu";
    }
    
    public String getImmatriculation() {
        return (vehicule != null) ? vehicule.getImmatriculation() : "Inconnu";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonne abonne = (Abonne) o;
        return vehiculeId == abonne.vehiculeId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(vehiculeId);
    }
}