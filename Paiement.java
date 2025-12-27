package com.gestion.parking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Paiement {
    private int id;
    private Integer ticketId;   
    private double montant;
    private LocalDateTime date;  

    private Ticket ticket;
    
    // Formateur pour l'affichage
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // Constructeurs
    public Paiement() {}
    
    public Paiement(int id, Integer ticketId, double montant, LocalDateTime date) {
        this.id = id;
        this.ticketId = ticketId;
        this.montant = montant;
        this.date = date;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public Integer getTicketId() {
        return ticketId;
    }
    
    public double getMontant() {
        return montant;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public Ticket getTicket() {
        return ticket;
    }
    
    public boolean estPourAbonnement() {
        return ticketId == null;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }
    
    public void setMontant(double montant) {
        this.montant = montant;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        if (ticket != null) {
            this.ticketId = ticket.getId();
        } else {
            this.ticketId = null;
        }
    }
    
    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", montant=" + montant + "DH" +
                ", date=" + (date != null ? date.format(formatter) : "null") +
                ", estPourAbonnement=" + estPourAbonnement() +
                '}';
    }
    
    
}