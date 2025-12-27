package com.gestion.parking.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.gestion.parking.dao.DatabaseConnection;
import com.gestion.parking.dao.PaiementDAO;
import com.gestion.parking.dao.PlaceDAO;
import com.gestion.parking.dao.TicketDAO;
import com.gestion.parking.dao.VehiculeDAO;

public class StatistiqueService {
    private TicketDAO ticketDAO;
    private PaiementDAO paiementDAO;
    private PlaceDAO placeDAO;
    private VehiculeDAO vehiculeDAO;
    private TicketService ticketService;
    private PlaceService placeService;
    
    public StatistiqueService() {
        this.ticketDAO = new TicketDAO();
        this.paiementDAO = new PaiementDAO();
        this.placeDAO = new PlaceDAO();
        this.vehiculeDAO = new VehiculeDAO();
        this.ticketService = new TicketService();
        this.placeService = new PlaceService();
    }
    
    // Obtenir le chiffre d'affaires pour une période
    public double obtenirChiffreAffaires(LocalDate dateDebut, LocalDate dateFin) {
        try {
            return ticketDAO.calculerChiffreAffaires(
                Date.valueOf(dateDebut), 
                Date.valueOf(dateFin)
            );
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du chiffre d'affaires: " + e.getMessage());
            return 0.0;
        }
    }
    
    
    // Obtenir le chiffre d'affaires du jour depuis les tickets
    public double obtenirChiffreAffairesDuJour() {
        try {
            LocalDate aujourdhui = LocalDate.now();
            
            String sql = "SELECT COALESCE(SUM(t.montant), 0) as total " +
                        "FROM Ticket t " +
                        "WHERE t.sortie IS NOT NULL " +
                        "AND DATE(t.sortie) = ? " +
                        "AND t.montant IS NOT NULL";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setDate(1, Date.valueOf(aujourdhui));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur dans obtenirChiffreAffairesDuJour: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    // Obtenir le chiffre d'affaires du mois
    public double obtenirChiffreAffairesDuMois() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return obtenirChiffreAffaires(debutMois, finMois);
    }
    
    // Obtenir le nombre d'entrées pour une période
    public int obtenirNombreEntrees(LocalDate dateDebut, LocalDate dateFin) {
        try {
            return ticketService.obtenirNombreEntrees(Date.valueOf(dateDebut), Date.valueOf(dateFin));
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des entrées: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir le taux d'occupation
    public double obtenirTauxOccupation() {
        try {
            return placeService.calculerTauxOccupation();
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul du taux d'occupation: " + e.getMessage());
            return 0.0;
        }
    }
    
    // Obtenir le nombre de places disponibles
    public int obtenirNombrePlacesDisponibles() {
        try {
            return placeService.compterPlacesDisponibles();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des places disponibles: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir le nombre total de places
    public int obtenirNombreTotalPlaces() {
        try {
            return placeService.compterToutesPlaces();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des places totales: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir le nombre de places occupées
    public int obtenirNombrePlacesOccupees() {
        try {
            return placeService.compterPlacesOccupees();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des places occupées: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir le nombre de tickets en cours
    public int obtenirNombreTicketsEnCours() {
        try {
            return ticketService.compterTicketsEnCours();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des tickets en cours: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir le nombre de véhicules enregistrés
    public int obtenirNombreVehiculesEnregistres() {
        try {
            VehiculeService vehiculeService = new VehiculeService();
            return vehiculeService.compterVehicules();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des véhicules: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir le nombre d'abonnés valides
    public int obtenirNombreAbonnesValides() {
        try {
            AbonneService abonneService = new AbonneService();
            return abonneService.compterAbonnesValides();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des abonnés: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir le nombre total d'abonnés
    public int obtenirNombreTotalAbonnes() {
        try {
            AbonneService abonneService = new AbonneService();
            return abonneService.compterAbonnes();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des abonnés: " + e.getMessage());
            return 0;
        }
    }
    
    // Obtenir les statistiques journalières
    public Map<LocalDate, Integer> obtenirStatistiquesJournalieres(int nombreJours) {
        Map<LocalDate, Integer> statistiques = new HashMap<>();
        
        try {
            LocalDate aujourdhui = LocalDate.now();
            
            for (int i = nombreJours - 1; i >= 0; i--) {
                LocalDate date = aujourdhui.minusDays(i);
                int nombreEntrees = obtenirNombreEntrees(date, date);
                statistiques.put(date, nombreEntrees);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'obtention des statistiques journalières: " + e.getMessage());
        }
        
        return statistiques;
    }
    
    // Obtenir le revenu moyen par véhicule
    public double obtenirRevenuMoyenParVehicule(LocalDate dateDebut, LocalDate dateFin) {
        double chiffreAffaires = obtenirChiffreAffaires(dateDebut, dateFin);
        int nombreEntrees = obtenirNombreEntrees(dateDebut, dateFin);
        
        if (nombreEntrees == 0) {
            return 0.0;
        }
        
        return chiffreAffaires / nombreEntrees;
    }
    
    // Obtenir les heures de pointe
    public Map<Integer, Integer> obtenirHeuresDePointe(LocalDate dateDebut, LocalDate dateFin) {
        Map<Integer, Integer> heuresPointe = new HashMap<>();
        
        try {
            String sql = "SELECT HOUR(entree) as heure, COUNT(*) as nombre " +
                        "FROM Ticket " +
                        "WHERE DATE(entree) BETWEEN ? AND ? " +
                        "GROUP BY HOUR(entree) " +
                        "ORDER BY HOUR(entree)";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setDate(1, Date.valueOf(dateDebut));
                stmt.setDate(2, Date.valueOf(dateFin));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        heuresPointe.put(rs.getInt("heure"), rs.getInt("nombre"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'obtention des heures de pointe: " + e.getMessage());
        }
        
        return heuresPointe;
    }
    
    // Obtenir le temps moyen de stationnement
    public double obtenirTempsMoyenStationnement(LocalDate dateDebut, LocalDate dateFin) {
        try {
            String sql = "SELECT AVG(TIMESTAMPDIFF(MINUTE, entree, sortie)) as temps_moyen " +
                        "FROM Ticket " +
                        "WHERE entree IS NOT NULL " +
                        "AND sortie IS NOT NULL " +
                        "AND DATE(entree) BETWEEN ? AND ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setDate(1, Date.valueOf(dateDebut));
                stmt.setDate(2, Date.valueOf(dateFin));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("temps_moyen");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du temps moyen: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    // Générer un rapport complet
    public String genererRapportComplet(LocalDate dateDebut, LocalDate dateFin) {
        StringBuilder rapport = new StringBuilder();
        
        rapport.append("========================================\n");
        rapport.append("RAPPORT STATISTIQUE DU PARKING\n");
        rapport.append("Période: ").append(dateDebut).append(" au ").append(dateFin).append("\n");
        rapport.append("Généré le: ").append(LocalDate.now()).append("\n");
        rapport.append("========================================\n\n");
        
        // Chiffre d'affaires
        double ca = obtenirChiffreAffaires(dateDebut, dateFin);
        rapport.append("CHIFFRE D'AFFAIRES: ").append(String.format("%.2f", ca)).append(" €\n\n");
        
        // Nombre d'entrées
        int entrees = obtenirNombreEntrees(dateDebut, dateFin);
        rapport.append("NOMBRE D'ENTRÉES: ").append(entrees).append("\n\n");
        
        // Taux d'occupation actuel
        double tauxOccupation = obtenirTauxOccupation();
        rapport.append("TAUX D'OCCUPATION ACTUEL: ").append(String.format("%.1f", tauxOccupation)).append("%\n\n");
        
        // Places disponibles/occupées
        int placesDispo = obtenirNombrePlacesDisponibles();
        int placesOccupees = obtenirNombrePlacesOccupees();
        int placesTotales = obtenirNombreTotalPlaces();
        rapport.append("STATUT DES PLACES:\n");
        rapport.append("  Places disponibles: ").append(placesDispo).append("\n");
        rapport.append("  Places occupées: ").append(placesOccupees).append("\n");
        rapport.append("  Total places: ").append(placesTotales).append("\n\n");
        
        // Véhicules enregistrés
        int vehiculesEnregistres = obtenirNombreVehiculesEnregistres();
        rapport.append("VÉHICULES ENREGISTRÉS: ").append(vehiculesEnregistres).append("\n\n");
        
        // Abonnés
        int abonnesValides = obtenirNombreAbonnesValides();
        int totalAbonnes = obtenirNombreTotalAbonnes();
        rapport.append("ABONNÉS:\n");
        rapport.append("  Abonnés valides: ").append(abonnesValides).append("\n");
        rapport.append("  Total abonnés: ").append(totalAbonnes).append("\n\n");
        
        // Temps moyen de stationnement
        double tempsMoyen = obtenirTempsMoyenStationnement(dateDebut, dateFin);
        int heures = (int) (tempsMoyen / 60);
        int minutes = (int) (tempsMoyen % 60);
        rapport.append("TEMPS MOYEN DE STATIONNEMENT: ")
              .append(heures).append("h ").append(minutes).append("min\n\n");
        
        // Revenu moyen par véhicule
        double revenuMoyen = obtenirRevenuMoyenParVehicule(dateDebut, dateFin);
        rapport.append("REVENU MOYEN PAR VÉHICULE: ").append(String.format("%.2f", revenuMoyen)).append(" €\n\n");
        
        // Tickets en cours
        int ticketsEnCours = obtenirNombreTicketsEnCours();
        rapport.append("TICKETS EN COURS: ").append(ticketsEnCours).append("\n\n");
        
        rapport.append("========================================\n");
        rapport.append("FIN DU RAPPORT\n");
        rapport.append("========================================\n");
        
        return rapport.toString();
    }
    
    // Générer un rapport journalier
    public String genererRapportJournalier() {
        LocalDate aujourdhui = LocalDate.now();
        return genererRapportComplet(aujourdhui, aujourdhui);
    }
    
    // Générer un rapport mensuel
    public String genererRapportMensuel() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return genererRapportComplet(debutMois, finMois);
    }
}