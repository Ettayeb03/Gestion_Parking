package com.gestion.parking.dao;

import com.gestion.parking.model.Ticket;
import com.gestion.parking.model.Vehicule;
import com.gestion.parking.model.Place;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    
    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private PlaceDAO placeDAO = new PlaceDAO();
    
    // Créer un ticket
    public int create(Ticket ticket) throws SQLException {
        String sql = "INSERT INTO Ticket (place_id, vehicule_id, entree, sortie, montant) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, ticket.getPlace().getId());
            stmt.setInt(2, ticket.getVehicule().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(ticket.getEntree()));
            
            if (ticket.getSortie() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(ticket.getSortie()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            
            stmt.setDouble(5, ticket.getMontant());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        ticket.setId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }
    
    // Trouver par ID
    public Ticket findById(int id) throws SQLException {
        String sql = "SELECT t.* FROM Ticket t WHERE t.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTicket(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver les tickets en cours (sans date de sortie)
    public List<Ticket> findTicketsEnCours() throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.* FROM Ticket t WHERE t.sortie IS NULL ORDER BY t.entree DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        }
        return tickets;
    }
    
    // Trouver le ticket en cours d'un véhicule
    public Ticket findTicketEnCoursByVehicule(int vehiculeId) throws SQLException {
        String sql = "SELECT t.* FROM Ticket t WHERE t.vehicule_id = ? AND t.sortie IS NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehiculeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTicket(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver les tickets par date d'entrée
    public List<Ticket> findTicketsByDateEntree(LocalDateTime date) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.* FROM Ticket t WHERE DATE(t.entree) = DATE(?) ORDER BY t.entree DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapResultSetToTicket(rs));
                }
            }
        }
        return tickets;
    }
    
    // Trouver les tickets par véhicule
    public List<Ticket> findTicketsByVehicule(int vehiculeId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.* FROM Ticket t WHERE t.vehicule_id = ? ORDER BY t.entree DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehiculeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapResultSetToTicket(rs));
                }
            }
        }
        return tickets;
    }
    
    // Trouver les tickets par place
    public List<Ticket> findTicketsByPlace(int placeId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.* FROM Ticket t WHERE t.place_id = ? ORDER BY t.entree DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, placeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapResultSetToTicket(rs));
                }
            }
        }
        return tickets;
    }
    
    // Enregistrer la sortie d'un véhicule
    public boolean enregistrerSortie(int ticketId, LocalDateTime sortie, double montant) throws SQLException {
        String sql = "UPDATE Ticket SET sortie = ?, montant = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(sortie));
            stmt.setDouble(2, montant);
            stmt.setInt(3, ticketId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Mettre à jour un ticket
    public boolean update(Ticket ticket) throws SQLException {
        String sql = "UPDATE Ticket SET place_id = ?, vehicule_id = ?, entree = ?, sortie = ?, montant = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ticket.getPlace().getId());
            stmt.setInt(2, ticket.getVehicule().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(ticket.getEntree()));
            
            if (ticket.getSortie() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(ticket.getSortie()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            
            stmt.setDouble(5, ticket.getMontant());
            stmt.setInt(6, ticket.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    
    // Lister tous les tickets
    public List<Ticket> findAll() throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.* FROM Ticket t ORDER BY t.entree DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        }
        return tickets;
    }
    
    // Calculer le chiffre d'affaires entre deux dates
    public double calculerChiffreAffaires(Date dateDebut, Date dateFin) throws SQLException {
        String sql = "SELECT SUM(montant) FROM Ticket WHERE entree >= ? AND entree <= ? AND montant > 0";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dateDebut);
            stmt.setDate(2, dateFin);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
    
    // Compter les tickets en cours
    public int countTicketsEnCours() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ticket WHERE sortie IS NULL";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    // Méthode utilitaire pour mapper ResultSet -> Ticket
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        
        // Récupérer la place
        int placeId = rs.getInt("place_id");
        Place place = placeDAO.findById(placeId);
        ticket.setPlace(place);
        
        // Récupérer le véhicule
        int vehiculeId = rs.getInt("vehicule_id");
        Vehicule vehicule = vehiculeDAO.findById(vehiculeId);
        ticket.setVehicule(vehicule);
        
        Timestamp entree = rs.getTimestamp("entree");
        Timestamp sortie = rs.getTimestamp("sortie");
        
        ticket.setEntree(entree != null ? entree.toLocalDateTime() : null);
        ticket.setSortie(sortie != null ? sortie.toLocalDateTime() : null);
        
        ticket.setMontant(rs.getDouble("montant"));
        
        return ticket;
    }
}