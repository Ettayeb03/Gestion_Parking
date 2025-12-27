package com.gestion.parking.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.gestion.parking.dao.AbonneDAO;
import com.gestion.parking.dao.DatabaseConnection;
import com.gestion.parking.dao.PlaceDAO;
import com.gestion.parking.dao.TicketDAO;
import com.gestion.parking.dao.VehiculeDAO;
import com.gestion.parking.model.Paiement;
import com.gestion.parking.model.Place;
import com.gestion.parking.model.Ticket;
import com.gestion.parking.model.Vehicule;
import com.google.protobuf.Timestamp;
import com.mysql.cj.xdevapi.Statement;

public class TicketService {
    private TicketDAO ticketDAO;
    private VehiculeDAO vehiculeDAO;
    private PlaceDAO placeDAO;
    private AbonneDAO abonneDAO;
    private VehiculeService vehiculeService;
    private PlaceService placeService;
    private AbonneService abonneService;
    
    // Tarifs (à configurer)
    private static final double TARIF_HORAIRE_VOITURE = 5.0;
    
    public TicketService() {
        this.ticketDAO = new TicketDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.placeDAO = new PlaceDAO();
        this.abonneDAO = new AbonneDAO();
        this.vehiculeService = new VehiculeService();
        this.placeService = new PlaceService();
        this.abonneService = new AbonneService();
    }
    
    // Générer un numéro de ticket unique
    private String genererNumeroTicket() {
        return "TKT-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
               + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Enregistrer une entrée
    public Ticket enregistrerEntree(String immatriculation) {
        try {
            // Vérifier si le véhicule est déjà dans le parking
            if (estDansParking(immatriculation)) {
                throw new IllegalStateException("Ce véhicule est déjà dans le parking");
            }
            
            // Vérifier si une place est disponible
            Place place = placeService.trouverPlaceLibre();
            if (place == null) {
                throw new IllegalStateException("Aucune place disponible");
            }
            
            // Trouver ou créer le véhicule
            Vehicule vehicule = vehiculeService.trouverVehiculeParImmatriculation(immatriculation);
            if (vehicule == null) {
                throw new IllegalArgumentException("Véhicule non enregistré. Veuillez d'abord enregistrer le véhicule");
            }
            
            // Vérifier si le véhicule est abonné
            boolean estAbonne = abonneService.estAbonneValide(immatriculation);
            
            // Occuper la place
            placeService.occuperPlace(place.getId());
            
            // Créer le ticket
            Ticket ticket = new Ticket();
            ticket.setPlace(place);
            ticket.setVehicule(vehicule);
            ticket.setEntree(LocalDateTime.now());
            ticket.setSortie(null);
            ticket.setMontant(0.0); // À calculer à la sortie
            
            // Si c'est un abonné, le montant reste à 0
            if (estAbonne) {
                ticket.setMontant(0.0);
            }
            
            ticketDAO.create(ticket);
            return ticket;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de l'entrée: " + e.getMessage());
            return null;
        }
    }
    
 // Enregistrer une sortie
    public Ticket enregistrerSortie(String immatriculation) {
        try {
            // Vérifier si le véhicule est dans le parking
            if (!estDansParking(immatriculation)) {
                throw new IllegalArgumentException("Ce véhicule n'est pas dans le parking");
            }
            
            // Trouver le véhicule
            Vehicule vehicule = vehiculeService.trouverVehiculeParImmatriculation(immatriculation);
            if (vehicule == null) {
                throw new IllegalArgumentException("Véhicule non trouvé");
            }
            
            // Trouver le ticket en cours
            Ticket ticket = obtenirTicketEnCours(immatriculation);
            if (ticket == null) {
                throw new IllegalStateException("Aucun ticket en cours pour ce véhicule");
            }
            
            // Enregistrer la sortie
            LocalDateTime sortie = LocalDateTime.now();
            ticket.setSortie(sortie);
            
            // Vérifier si c'est un abonné
            boolean estAbonne = abonneService.estAbonneValide(immatriculation);
            
            // Calculer le montant si ce n'est pas un abonné
            if (!estAbonne) {
                double montant = calculerMontant(ticket);
                ticket.setMontant(montant);
            } else {
                ticket.setMontant(0.0); // Gratuit pour abonnés
            }
            
            // Libérer la place
            placeService.libererPlace(ticket.getPlace().getId());
            
            // Mettre à jour le ticket
            ticketDAO.update(ticket);
            
            // Si le ticket a un montant > 0, créer un paiement
            if (ticket.getMontant() > 0) {
                creerPaiement(ticket);
            }
            
            return ticket;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de la sortie: " + e.getMessage());
            return null;
        }
    }

 // Créer un paiement
    private void creerPaiement(Ticket ticket) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            // Vérifier si le ticket existe et a un montant
            if (ticket == null || ticket.getId() <= 0 || ticket.getMontant() <= 0) {
                return; // Pas de paiement à créer
            }
            
            // Insérer dans la base de données
            conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO Paiement (ticket_id, montant, date) VALUES (?, ?, ?)";
            
            // Créer le statement sans RETURN_GENERATED_KEYS
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ticket.getId());
            stmt.setDouble(2, ticket.getMontant());
            
            // Utiliser la date actuelle
            long currentTime = System.currentTimeMillis();
            java.sql.Timestamp timestamp = new java.sql.Timestamp(currentTime);
            stmt.setTimestamp(3, timestamp);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Paiement créé avec succès pour le ticket: " + ticket.getId());
            } else {
                System.err.println("Échec de la création du paiement pour le ticket: " + ticket.getId());
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la création du paiement: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du paiement: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer les ressources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
            }
        }
    }
    
    // Calculer le montant pour un ticket
    private double calculerMontant(Ticket ticket) {
        if (ticket.getEntree() == null || ticket.getSortie() == null) {
            return 0.0;
        }
        
        // Calculer la durée en heures
        long minutes = java.time.Duration.between(ticket.getEntree(), ticket.getSortie()).toMinutes();
        double heures = minutes / 60.0;
        
        // Arrondir à l'heure supérieure
        heures = Math.ceil(heures);
        
        // Tarif par défaut (vous pouvez ajuster selon le type de véhicule si nécessaire)
        double tarifHoraire = TARIF_HORAIRE_VOITURE;
        
        return heures * tarifHoraire;
    }
    
    // Trouver un ticket par ID
    public Ticket trouverTicketParId(int id) {
        try {
            return ticketDAO.findById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du ticket: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver les tickets en cours
    public List<Ticket> listerTicketsEnCours() {
        try {
            return ticketDAO.findTicketsEnCours();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des tickets en cours: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver le ticket en cours d'un véhicule
    public Ticket obtenirTicketEnCours(String immatriculation) {
        try {
            Vehicule vehicule = vehiculeService.trouverVehiculeParImmatriculation(immatriculation);
            if (vehicule == null) {
                return null;
            }
            
            return ticketDAO.findTicketEnCoursByVehicule(vehicule.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du ticket en cours: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver les tickets par date d'entrée
    public List<Ticket> listerTicketsParDate(LocalDateTime date) {
        try {
            return ticketDAO.findTicketsByDateEntree(date);
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des tickets par date: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver les tickets par véhicule
    public List<Ticket> listerTicketsParVehicule(String immatriculation) {
        try {
            Vehicule vehicule = vehiculeService.trouverVehiculeParImmatriculation(immatriculation);
            if (vehicule == null) {
                return null;
            }
            
            return ticketDAO.findTicketsByVehicule(vehicule.getId());
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des tickets par véhicule: " + e.getMessage());
            return null;
        }
    }
    
    // Lister tous les tickets
    public List<Ticket> listerTousTickets() {
        try {
            return ticketDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des tickets: " + e.getMessage());
            return null;
        }
    }
    
    // Vérifier si un véhicule est dans le parking
    public boolean estDansParking(String immatriculation) {
        try {
            Vehicule vehicule = vehiculeService.trouverVehiculeParImmatriculation(immatriculation);
            if (vehicule == null) {
                return false;
            }
            
            Ticket ticket = ticketDAO.findTicketEnCoursByVehicule(vehicule.getId());
            return ticket != null;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification: " + e.getMessage());
            return false;
        }
    }
    
    // Obtenir la durée de stationnement pour un ticket
    public String obtenirDureeStationnement(Ticket ticket) {
        if (ticket == null) {
            return "N/A";
        }
        
        if (ticket.getEntree() == null) {
            return "N/A";
        }
        
        LocalDateTime fin = (ticket.getSortie() != null) ? ticket.getSortie() : LocalDateTime.now();
        long minutes = java.time.Duration.between(ticket.getEntree(), fin).toMinutes();
        
        long heures = minutes / 60;
        long mins = minutes % 60;
        
        return String.format("%02d:%02d", heures, mins);
    }
    
    // Calculer le montant dû pour un ticket
    public double calculerMontantDu(Ticket ticket) {
        if (ticket == null) {
            return 0.0;
        }
        
        // Si le ticket a déjà un montant fixé (après sortie)
        if (ticket.getSortie() != null && ticket.getMontant() > 0) {
            return ticket.getMontant();
        }
        
        // Si c'est un ticket en cours, calculer le montant estimé
        if (ticket.getEntree() != null && ticket.getSortie() == null) {
            // Vérifier si c'est un abonné
            boolean estAbonne = abonneService.estAbonneValide(ticket.getVehicule().getImmatriculation());
            if (estAbonne) {
                return 0.0; // Gratuit pour abonnés
            }
            
            // Calculer montant estimé
            long minutes = java.time.Duration.between(ticket.getEntree(), LocalDateTime.now()).toMinutes();
            double heures = Math.ceil(minutes / 60.0);
            return heures * TARIF_HORAIRE_VOITURE;
        }
        
        return 0.0;
    }
    
    // Compter les tickets en cours
    public int compterTicketsEnCours() {
        try {
            return ticketDAO.countTicketsEnCours();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des tickets en cours: " + e.getMessage());
            return 0;
        }
    }
    
    // Calculer le chiffre d'affaires entre deux dates
    public double calculerChiffreAffaires(java.sql.Date dateDebut, java.sql.Date dateFin) {
        try {
            return ticketDAO.calculerChiffreAffaires(dateDebut, dateFin);
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du chiffre d'affaires: " + e.getMessage());
            return 0.0;
        }
    }
    
    // Obtenir les statistiques pour les rapports
    public int obtenirNombreEntrees(java.sql.Date dateDebut, java.sql.Date dateFin) {
        try {
            String sql = "SELECT COUNT(*) FROM Ticket WHERE entree BETWEEN ? AND ?";
            
            try (Connection conn = com.gestion.parking.dao.DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setDate(1, dateDebut);
                stmt.setDate(2, dateFin);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des entrées: " + e.getMessage());
        }
        return 0;
    }
    
 // TicketService.java - Méthode à ajouter
    public boolean vehiculeATicketsHistoriques(String immatriculation) {
        try {
            List<Ticket> tickets = listerTousTickets();
            if (tickets != null) {
                for (Ticket ticket : tickets) {
                    if (ticket.getVehicule() != null && 
                        ticket.getVehicule().getImmatriculation().equalsIgnoreCase(immatriculation)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des tickets historiques: " + e.getMessage());
            return false;
        }
    }
}