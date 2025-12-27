package com.gestion.parking.service;

import com.gestion.parking.dao.VehiculeDAO;
import com.gestion.parking.model.Vehicule;
import java.sql.SQLException;
import java.util.List;

public class VehiculeService {
    private VehiculeDAO vehiculeDAO;
    
    public VehiculeService() {
        this.vehiculeDAO = new VehiculeDAO();
    }
    
    // Enregistrer un nouveau véhicule
    public Vehicule enregistrerVehicule(String immatriculation, String proprietaire) {
        try {
            // Vérifier si le véhicule existe déjà
            Vehicule existant = vehiculeDAO.findByImmatriculation(immatriculation);
            if (existant != null) {
                return existant;
            }
            
            // Créer un nouveau véhicule
            Vehicule vehicule = new Vehicule();
            vehicule.setImmatriculation(immatriculation.toUpperCase());
            vehicule.setProprietaire(proprietaire);
            
            vehiculeDAO.create(vehicule);
            return vehicule;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement du véhicule: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver un véhicule par immatriculation
    public Vehicule trouverVehiculeParImmatriculation(String immatriculation) {
        try {
            return vehiculeDAO.findByImmatriculation(immatriculation.toUpperCase());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du véhicule: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver un véhicule par ID
    public Vehicule trouverVehiculeParId(int id) {
        try {
            return vehiculeDAO.findById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du véhicule: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver des véhicules par propriétaire
    public List<Vehicule> trouverVehiculesParProprietaire(String proprietaire) {
        try {
            return vehiculeDAO.findByProprietaire(proprietaire);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des véhicules: " + e.getMessage());
            return null;
        }
    }
    
    // Mettre à jour un véhicule
    public boolean mettreAJourVehicule(Vehicule vehicule) {
        try {
            return vehiculeDAO.update(vehicule);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du véhicule: " + e.getMessage());
            return false;
        }
    }
    
 // Supprimer un véhicule
    public boolean supprimerVehicule(int id) {
        try {
            // Vérifier si le véhicule existe
            Vehicule vehicule = trouverVehiculeParId(id);
            if (vehicule == null) {
                throw new IllegalStateException("Le véhicule n'existe pas dans la base de données.");
            }
            
            // Vérifier si le véhicule a des tickets en cours
            TicketService ticketService = new TicketService();
            boolean estDansParking = ticketService.estDansParking(vehicule.getImmatriculation());
            
            if (estDansParking) {
                throw new IllegalStateException("Impossible de supprimer un véhicule présent dans le parking.");
            }
            
            // Vérifier si le véhicule a des tickets historiques
            boolean aDesTickets = ticketService.vehiculeATicketsHistoriques(vehicule.getImmatriculation());
            
            if (aDesTickets) {
                // Proposer des alternatives
                throw new IllegalStateException("Ce véhicule a des tickets historiques. " +
                    "Veuillez d'abord supprimer ou archiver tous ses tickets.");
            }
            
            // Tenter la suppression
            return vehiculeDAO.delete(id);
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la suppression du véhicule: " + e.getMessage());
            
            // Analyser l'erreur SQL pour donner un message plus précis
            if (e.getMessage().toLowerCase().contains("foreign key constraint")) {
                throw new IllegalStateException("Ce véhicule ne peut pas être supprimé car il est référencé " +
                    "dans des tickets. Veuillez d'abord supprimer ou archiver tous les tickets associés.");
            }
            
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage(), e);
            
        } catch (IllegalStateException e) {
            // Relancer l'exception avec le message d'erreur approprié
            throw e;
            
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la suppression du véhicule: " + e.getMessage());
            throw new RuntimeException("Erreur inattendue: " + e.getMessage(), e);
        }
    }
    
    // Lister tous les véhicules
    public List<Vehicule> listerTousVehicules() {
        try {
            return vehiculeDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des véhicules: " + e.getMessage());
            return null;
        }
    }
    
    // Vérifier si un véhicule existe
    public boolean vehiculeExiste(String immatriculation) {
        try {
            return vehiculeDAO.exists(immatriculation.toUpperCase());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du véhicule: " + e.getMessage());
            return false;
        }
    }
    
    // Compter tous les véhicules
    public int compterVehicules() {
        try {
            return vehiculeDAO.countAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des véhicules: " + e.getMessage());
            return 0;
        }
    }
}