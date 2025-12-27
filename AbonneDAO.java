package com.gestion.parking.dao;

import com.gestion.parking.model.Abonne;
import com.gestion.parking.model.Vehicule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonneDAO {
    
    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    
    // Créer un abonné
    public int create(Abonne abonne) throws SQLException {
        String sql = "INSERT INTO Abonne (vehicule_id, dateDebut, dateFin) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, abonne.getVehicule().getId());
            stmt.setDate(2, Date.valueOf(abonne.getDateDebut()));
            stmt.setDate(3, Date.valueOf(abonne.getDateFin()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        abonne.setId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }
    
    // Trouver par ID
    public Abonne findById(int id) throws SQLException {
        String sql = "SELECT a.* FROM Abonne a WHERE a.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAbonne(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver par ID du véhicule
    public Abonne findByVehiculeId(int vehiculeId) throws SQLException {
        String sql = "SELECT a.* FROM Abonne a WHERE a.vehicule_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehiculeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAbonne(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver par immatriculation du véhicule
    public Abonne findByVehiculeImmatriculation(String immatriculation) throws SQLException {
        String sql = "SELECT a.* FROM Abonne a JOIN Vehicule v ON a.vehicule_id = v.id WHERE v.immatriculation = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, immatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAbonne(rs);
                }
            }
        }
        return null;
    }
    
    // Mettre à jour un abonné
    public boolean update(Abonne abonne) throws SQLException {
        String sql = "UPDATE Abonne SET vehicule_id = ?, dateDebut = ?, dateFin = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, abonne.getVehicule().getId());
            stmt.setDate(2, Date.valueOf(abonne.getDateDebut()));
            stmt.setDate(3, Date.valueOf(abonne.getDateFin()));
            stmt.setInt(4, abonne.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Supprimer un abonné
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Abonne WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Lister tous les abonnés
    public List<Abonne> findAll() throws SQLException {
        List<Abonne> abonnes = new ArrayList<>();
        String sql = "SELECT a.* FROM Abonne a ORDER BY a.dateDebut DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                abonnes.add(mapResultSetToAbonne(rs));
            }
        }
        return abonnes;
    }
    
    // Lister les abonnés valides (dont l'abonnement est en cours)
    public List<Abonne> findAbonnesValides() throws SQLException {
        List<Abonne> abonnes = new ArrayList<>();
        String sql = "SELECT a.* FROM Abonne a WHERE dateDebut <= CURDATE() AND dateFin >= CURDATE() ORDER BY a.dateFin";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                abonnes.add(mapResultSetToAbonne(rs));
            }
        }
        return abonnes;
    }
    
    // Lister les abonnés expirés
    public List<Abonne> findAbonnesExpires() throws SQLException {
        List<Abonne> abonnes = new ArrayList<>();
        String sql = "SELECT a.* FROM Abonne a WHERE dateFin < CURDATE() ORDER BY a.dateFin DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                abonnes.add(mapResultSetToAbonne(rs));
            }
        }
        return abonnes;
    }
    
    // Vérifier si un véhicule est abonné et valide
    public boolean isVehiculeAbonneValide(int vehiculeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Abonne WHERE vehicule_id = ? AND dateDebut <= CURDATE() AND dateFin >= CURDATE()";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vehiculeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    // Compter tous les abonnés
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Abonne";
        
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
    
    // Compter les abonnés valides
    public int countAbonnesValides() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Abonne WHERE dateDebut <= CURDATE() AND dateFin >= CURDATE()";
        
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
    
    // Méthode utilitaire pour mapper ResultSet -> Abonne
    private Abonne mapResultSetToAbonne(ResultSet rs) throws SQLException {
        Abonne abonne = new Abonne();
        abonne.setId(rs.getInt("id"));
        
        // Récupérer le véhicule associé
        int vehiculeId = rs.getInt("vehicule_id");
        Vehicule vehicule = vehiculeDAO.findById(vehiculeId);
        abonne.setVehicule(vehicule);
        
        Date dateDebut = rs.getDate("dateDebut");
        Date dateFin = rs.getDate("dateFin");
        abonne.setDateDebut(dateDebut != null ? dateDebut.toLocalDate() : null);
        abonne.setDateFin(dateFin != null ? dateFin.toLocalDate() : null);
        
        return abonne;
    }
}