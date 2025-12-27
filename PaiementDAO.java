package com.gestion.parking.dao;

import com.gestion.parking.model.Paiement;
import com.gestion.parking.model.Ticket;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO {
    
    private TicketDAO ticketDAO = new TicketDAO();
    
    // Créer un paiement
    public int create(Paiement paiement) throws SQLException {
        String sql = "INSERT INTO Paiement (ticket_id, montant, date) VALUES (?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, paiement.getTicket().getId());
            stmt.setDouble(2, paiement.getMontant());
            stmt.setTimestamp(3, Timestamp.valueOf(paiement.getDate()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    paiement.setId(id);
                    return id;
                }
            }
            return -1;
        } finally {
            closeResources(rs, stmt);
        }
    }
    
    // Trouver par ID
    public Paiement findById(int id) throws SQLException {
        String sql = "SELECT p.* FROM Paiement p WHERE p.id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPaiement(rs);
            }
        } finally {
            closeResources(rs, stmt);
        }
        return null;
    }
    
    // Trouver les paiements d'un ticket
    public List<Paiement> findByTicketId(int ticketId) throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT p.* FROM Paiement p WHERE p.ticket_id = ? ORDER BY p.date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ticketId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        } finally {
            closeResources(rs, stmt);
        }
        return paiements;
    }
    
    // Trouver les paiements par date
    public List<Paiement> findByDate(LocalDateTime date) throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT p.* FROM Paiement p WHERE DATE(p.date) = DATE(?) ORDER BY p.date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(date));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        } finally {
            closeResources(rs, stmt);
        }
        return paiements;
    }
    
    
    // Lister tous les paiements
    public List<Paiement> findAll() throws SQLException {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT p.* FROM Paiement p ORDER BY p.date DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                paiements.add(mapResultSetToPaiement(rs));
            }
        } finally {
            closeResources(rs, stmt);
        }
        return paiements;
    }
    
    // Calculer le total des paiements pour un ticket
    public double calculerTotalPaiementsTicket(int ticketId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM Paiement WHERE ticket_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ticketId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } finally {
            closeResources(rs, stmt);
        }
        return 0.0;
    }
    
    // Calculer le total des paiements entre deux dates (chiffre d'affaire)
    public double calculerTotalPaiementsDateRange(LocalDateTime dateDebut, LocalDateTime dateFin) throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM Paiement WHERE date >= ? AND date <= ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } finally {
            closeResources(rs, stmt);
        }
        return 0.0;
    }
    
    // Vérifier si un ticket est entièrement payé
    public boolean estTicketPaye(int ticketId) throws SQLException {
        String sql = "SELECT t.montant, COALESCE(SUM(p.montant), 0) as total_paye " +
                    "FROM Ticket t " +
                    "LEFT JOIN Paiement p ON t.id = p.ticket_id " +
                    "WHERE t.id = ? " +
                    "GROUP BY t.id, t.montant";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ticketId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                double montantTicket = rs.getDouble("montant");
                double totalPaye = rs.getDouble("total_paye");
                return totalPaye >= montantTicket;
            }
        } finally {
            closeResources(rs, stmt);
        }
        return false;
    }
    
    // Compter tous les paiements
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Paiement";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            closeResources(rs, stmt);
        }
        return 0;
    }
    
    // Méthode utilitaire pour mapper ResultSet -> Paiement
    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setId(rs.getInt("id"));
        
        // Récupérer le ticket associé
        int ticketId = rs.getInt("ticket_id");
        Ticket ticket = ticketDAO.findById(ticketId);
        paiement.setTicket(ticket);
        
        paiement.setMontant(rs.getDouble("montant"));
        
        Timestamp date = rs.getTimestamp("date");
        if (date != null) {
            paiement.setDate(date.toLocalDateTime());
        }
        
        return paiement;
    }
    
    // Méthode pour fermer les ressources
    private void closeResources(ResultSet rs, Statement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du Statement: " + e.getMessage());
            }
        }
    }
}