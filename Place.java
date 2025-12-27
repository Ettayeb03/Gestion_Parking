package com.gestion.parking.model;

public class Place {
    private int id;
    private String numero;
    private String statut; 
    
    // Constructeurs
    public Place() {}
    
    public Place(int id, String numero, String statut) {
        this.id = id;
        this.numero = numero;
        this.statut = statut;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public boolean isLibre() {
        return "LIBRE".equals(statut);
    }
    
    public boolean isOccupee() {
        return "OCCUPEE".equals(statut);
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public void setLibre() {
        this.statut = "LIBRE";
    }
    
    public void setOccupee() {
        this.statut = "OCCUPEE";
    }
    
    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}