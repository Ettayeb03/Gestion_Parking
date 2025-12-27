package com.gestion.parking.service;

import com.gestion.parking.dao.PaiementDAO;
import com.gestion.parking.dao.TicketDAO;
import com.gestion.parking.model.Paiement;
import com.gestion.parking.model.Ticket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PaiementService {
    private PaiementDAO paiementDAO;
    private TicketDAO ticketDAO;
    private TicketService ticketService;
    
    public PaiementService() {
        this.paiementDAO = new PaiementDAO();
        this.ticketDAO = new TicketDAO();
        this.ticketService = new TicketService();
    }
    
    // Effectuer un paiement
    public Paiement effectuerPaiement(Ticket ticket, double montant) {
        try {
            // Vérifier si le ticket existe
            if (ticket == null || ticket.getId() <= 0) {
                throw new IllegalArgumentException("Ticket invalide");
            }
            
            // Vérifier si le ticket a déjà une sortie
            if (ticket.getSortie() == null) {
                throw new IllegalStateException("Le véhicule n'est pas encore sorti");
            }
            
            // Vérifier le montant
            double montantDu = ticket.getMontant();
            double totalDejaPaye = paiementDAO.calculerTotalPaiementsTicket(ticket.getId());
            
            if (montant <= 0) {
                throw new IllegalStateException("Le montant doit être supérieur à zéro");
            }
            
            // Créer le paiement
            Paiement paiement = new Paiement();
            paiement.setTicket(ticket);
            paiement.setMontant(montant);
            paiement.setDate(LocalDateTime.now());
            
            paiementDAO.create(paiement);
            return paiement;
            
        } catch (Exception e) {
            System.err.println("Erreur lors du paiement: " + e.getMessage());
            return null;
        }
    }
    
    // Effectuer un paiement complet
    public Paiement effectuerPaiementComplet(Ticket ticket) {
        try {
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket non trouvé");
            }
            
            double montantDu = ticket.getMontant();
            double totalDejaPaye = paiementDAO.calculerTotalPaiementsTicket(ticket.getId());
            double montantAPayer = montantDu - totalDejaPaye;
            
            if (montantAPayer <= 0) {
                throw new IllegalStateException("Le ticket est déjà entièrement payé");
            }
            
            return effectuerPaiement(ticket, montantAPayer);
            
        } catch (Exception e) {
            System.err.println("Erreur lors du paiement complet: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver un paiement par ID
    public Paiement trouverPaiementParId(int id) {
        try {
            return paiementDAO.findById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du paiement: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver les paiements d'un ticket
    public List<Paiement> trouverPaiementsParTicket(Ticket ticket) {
        try {
            if (ticket == null) {
                return null;
            }
            
            return paiementDAO.findByTicketId(ticket.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des paiements: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver les paiements par date
    public List<Paiement> trouverPaiementsParDate(LocalDateTime date) {
        try {
            return paiementDAO.findByDate(date);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des paiements par date: " + e.getMessage());
            return null;
        }
    }
    
   
    
    // Lister tous les paiements
    public List<Paiement> listerTousPaiements() {
        try {
            return paiementDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des paiements: " + e.getMessage());
            return null;
        }
    }
    
    // Calculer le total payé pour un ticket
    public double calculerTotalPaye(Ticket ticket) {
        try {
            if (ticket == null) {
                return 0.0;
            }
            
            return paiementDAO.calculerTotalPaiementsTicket(ticket.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du total payé: " + e.getMessage());
            return 0.0;
        }
    }
    
    // Vérifier si un ticket est entièrement payé
    public boolean estTicketEntierementPaye(Ticket ticket) {
        try {
            if (ticket == null) {
                return false;
            }
            
            return paiementDAO.estTicketPaye(ticket.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du paiement: " + e.getMessage());
            return false;
        }
    }
    
    // Calculer le chiffre d'affaire entre deux dates
    public double calculerChiffreAffairePeriode(LocalDateTime dateDebut, LocalDateTime dateFin) {
        try {
            return paiementDAO.calculerTotalPaiementsDateRange(dateDebut, dateFin);
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du chiffre d'affaire: " + e.getMessage());
            return 0.0;
        }
    }
    
    // Calculer le chiffre d'affaire du jour
    public double calculerChiffreAffaireJour() {
        LocalDateTime aujourdhui = LocalDateTime.now();
        LocalDateTime debutJour = aujourdhui.toLocalDate().atStartOfDay();
        LocalDateTime finJour = debutJour.plusDays(1).minusSeconds(1);
        
        return calculerChiffreAffairePeriode(debutJour, finJour);
    }
    
    // Calculer le chiffre d'affaire du mois
    public double calculerChiffreAffaireMois() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debutMois = maintenant.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMois = debutMois.plusMonths(1).minusSeconds(1);
        
        return calculerChiffreAffairePeriode(debutMois, finMois);
    }
    
    // Compter tous les paiements
    public int compterPaiements() {
        try {
            return paiementDAO.countAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des paiements: " + e.getMessage());
            return 0;
        }
    }
    
    // Générer un reçu
    public String genererReçu(Paiement paiement) {
        if (paiement == null || paiement.getTicket() == null) {
            return "Reçu invalide";
        }
        
        Ticket ticket = paiement.getTicket();
        StringBuilder reçu = new StringBuilder();
        
        reçu.append("========== REÇU DE PAIEMENT ==========\n");
        reçu.append("Numéro de reçu: ").append(paiement.getId()).append("\n");
        reçu.append("Date: ").append(paiement.getDate().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        reçu.append("\n");
        reçu.append("Informations Ticket:\n");
        reçu.append("  Véhicule: ").append(ticket.getVehicule().getImmatriculation()).append("\n");
        reçu.append("  Propriétaire: ").append(ticket.getVehicule().getProprietaire()).append("\n");
        reçu.append("  Place: ").append(ticket.getPlace().getNumero()).append("\n");
        reçu.append("  Entrée: ").append(ticket.getEntree().format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        
        if (ticket.getSortie() != null) {
            reçu.append("  Sortie: ").append(ticket.getSortie().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
            reçu.append("  Durée: ").append(ticketService.obtenirDureeStationnement(ticket)).append("\n");
        }
        
        reçu.append("\n");
        reçu.append("Détails Paiement:\n");
        reçu.append("  Montant payé: ").append(String.format("%.2f", paiement.getMontant())).append(" DH\n");
        
        double totalPaye = calculerTotalPaye(ticket);
        double montantDu = ticket.getMontant();
        
        reçu.append("\n");
        reçu.append("Récapitulatif:\n");
        reçu.append("  Montant total du ticket: ").append(String.format("%.2f", montantDu)).append(" DH\n");
        reçu.append("  Total déjà payé: ").append(String.format("%.2f", totalPaye)).append(" DH\n");
        reçu.append("  Statut: ").append(estTicketEntierementPaye(ticket) ? "PAYÉ" : "PARTIELLEMENT PAYÉ").append("\n");
        reçu.append("\n");
        reçu.append("========== MERCI DE VOTRE VISITE ==========\n");
        
        return reçu.toString();
    }
}