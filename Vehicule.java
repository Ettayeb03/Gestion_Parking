package com.gestion.parking.model;

import java.util.Objects;

public class Vehicule {
    private int id;
    private String immatriculation;
    private String proprietaire;
    
    // Constructeurs
    public Vehicule() {}
    
    public Vehicule(int id, String immatriculation, String proprietaire) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.proprietaire = proprietaire;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getImmatriculation() {
        return immatriculation;
    }
    
    public String getProprietaire() {
        return proprietaire;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }
    
    public void setProprietaire(String proprietaire) {
        this.proprietaire = proprietaire;
    }
    
    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", immatriculation='" + immatriculation + '\'' +
                ", proprietaire='" + proprietaire + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return Objects.equals(immatriculation, vehicule.immatriculation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(immatriculation);
    }
}