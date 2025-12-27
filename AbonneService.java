package com.gestion.parking.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.gestion.parking.dao.AbonneDAO;
import com.gestion.parking.dao.VehiculeDAO;
import com.gestion.parking.model.Abonne;
import com.gestion.parking.model.Vehicule;

public class AbonneService {
    private AbonneDAO abonneDAO;
    private VehiculeDAO vehiculeDAO;
    private VehiculeService vehiculeService;
    
    public AbonneService() {
        this.abonneDAO = new AbonneDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.vehiculeService = new VehiculeService();
    }
    
    // Méthode pour calculer le revenu mensuel total des abonnements valides
    public double calculerRevenuMensuel() {
        try {
            List<Abonne> abonnesValides = abonneDAO.findAbonnesValides();
            double revenuTotal = 0.0;
            
            for (Abonne abonne : abonnesValides) {
                revenuTotal += abonne.getMontantPaye();
            }
            
            return revenuTotal;
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du revenu mensuel: " + e.getMessage());
            return 0.0;
        }
    }
    
    // Méthode pour calculer le revenu total attendu
    public double calculerRevenuTotalAttendu() {
        try {
            List<Abonne> abonnes = abonneDAO.findAll();
            double revenuTotal = 0.0;
            
            for (Abonne abonne : abonnes) {
                revenuTotal += abonne.getMontantTotal();
            }
            
            return revenuTotal;
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du revenu total: " + e.getMessage());
            return 0.0;
        }
    }
    // Créer un nouvel abonné
    public Abonne creerAbonne(Vehicule vehicule, LocalDate dateDebut, LocalDate dateFin) {
        try {
            // Vérifier si le véhicule existe
            if (vehicule == null || vehicule.getId() <= 0) {
                throw new IllegalArgumentException("Véhicule invalide");
            }
            
            // Vérifier si le véhicule est déjà abonné
            Abonne abonneExistant = abonneDAO.findByVehiculeId(vehicule.getId());
            if (abonneExistant != null) {
                throw new IllegalArgumentException("Ce véhicule est déjà abonné");
            }
            
            // Créer l'abonné
            Abonne abonne = new Abonne();
            abonne.setVehicule(vehicule);
            abonne.setDateDebut(dateDebut);
            abonne.setDateFin(dateFin);
            
            abonneDAO.create(abonne);
            return abonne;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'abonné: " + e.getMessage());
            return null;
        }
    }
    
    // Créer un abonné avec immatriculation
    public Abonne creerAbonne(String immatriculation, LocalDate dateDebut, LocalDate dateFin) {
        try {
            // Trouver ou créer le véhicule
            Vehicule vehicule = vehiculeService.trouverVehiculeParImmatriculation(immatriculation);
            if (vehicule == null) {
                throw new IllegalArgumentException("Véhicule non trouvé");
            }
            
            return creerAbonne(vehicule, dateDebut, dateFin);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'abonné: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver un abonné par ID
    public Abonne trouverAbonneParId(int id) {
        try {
            return abonneDAO.findById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'abonné: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver un abonné par immatriculation
    public Abonne trouverAbonneParImmatriculation(String immatriculation) {
        try {
            return abonneDAO.findByVehiculeImmatriculation(immatriculation.toUpperCase());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'abonné: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver un abonné par ID du véhicule
    public Abonne trouverAbonneParVehiculeId(int vehiculeId) {
        try {
            return abonneDAO.findByVehiculeId(vehiculeId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'abonné: " + e.getMessage());
            return null;
        }
    }
    
    // Mettre à jour un abonné
    public boolean mettreAJourAbonne(Abonne abonne) {
        try {
            return abonneDAO.update(abonne);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'abonné: " + e.getMessage());
            return false;
        }
    }
    
    // Renouveler l'abonnement
    public boolean renouvelerAbonnement(int abonneId, LocalDate nouvelleDateFin) {
        try {
            Abonne abonne = abonneDAO.findById(abonneId);
            if (abonne == null) {
                return false;
            }
            
            abonne.setDateFin(nouvelleDateFin);
            return abonneDAO.update(abonne);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du renouvellement de l'abonnement: " + e.getMessage());
            return false;
        }
    }
    
    // Supprimer un abonné
    public boolean supprimerAbonne(int id) {
        try {
            return abonneDAO.delete(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'abonné: " + e.getMessage());
            return false;
        }
    }
    
    // Lister tous les abonnés
    public List<Abonne> listerTousAbonnes() {
        try {
            return abonneDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des abonnés: " + e.getMessage());
            return null;
        }
    }
    
    // Lister les abonnés valides
    public List<Abonne> listerAbonnesValides() {
        try {
            return abonneDAO.findAbonnesValides();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des abonnés valides: " + e.getMessage());
            return null;
        }
    }
    
    // Vérifier si un abonné est valide
    public boolean estAbonneValide(String immatriculation) {
        try {
            Abonne abonne = abonneDAO.findByVehiculeImmatriculation(immatriculation.toUpperCase());
            if (abonne == null) {
                return false;
            }
            
            LocalDate aujourdhui = LocalDate.now();
            return !aujourdhui.isBefore(abonne.getDateDebut()) && !aujourdhui.isAfter(abonne.getDateFin());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'abonnement: " + e.getMessage());
            return false;
        }
    }
    
    // Vérifier si un véhicule est abonné (valide ou non)
    public boolean estVehiculeAbonne(String immatriculation) {
        try {
            Abonne abonne = abonneDAO.findByVehiculeImmatriculation(immatriculation.toUpperCase());
            return abonne != null;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'abonnement: " + e.getMessage());
            return false;
        }
    }
    
    // Obtenir l'abonné d'un véhicule
    public Abonne obtenirAbonneParVehicule(String immatriculation) {
        return trouverAbonneParImmatriculation(immatriculation);
    }
    
    // Compter tous les abonnés
    public int compterAbonnes() {
        try {
            return abonneDAO.countAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des abonnés: " + e.getMessage());
            return 0;
        }
    }
    
    // Compter les abonnés valides
    public int compterAbonnesValides() {
        try {
            return abonneDAO.countAbonnesValides();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des abonnés valides: " + e.getMessage());
            return 0;
        }
    }
    
    // Lister les abonnés expirés
    public List<Abonne> listerAbonnesExpires() {
        try {
            return abonneDAO.findAbonnesExpires();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des abonnés expirés: " + e.getMessage());
            return null;
        }
    }
}