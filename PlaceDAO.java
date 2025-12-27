package com.gestion.parking.dao;

import com.gestion.parking.model.Place;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaceDAO {
    
    private AbonneDAO abonneDAO = new AbonneDAO();
    
    // Créer une place
    public int create(Place place) throws SQLException {
        String sql = "INSERT INTO Place (numero, statut) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, place.getNumero());
            stmt.setString(2, place.getStatut());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        place.setId(id);
                        return id;
                    }
                }
            }
            return -1;
        }
    }
    
    // Trouver par ID
    public Place findById(int id) throws SQLException {
        String sql = "SELECT p.* FROM Place p WHERE p.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPlace(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver par numéro
    public Place findByNumero(String numero) throws SQLException {
        String sql = "SELECT p.* FROM Place p WHERE p.numero = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numero);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPlace(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver les places libres
    public List<Place> findPlacesLibres() throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT p.* FROM Place p WHERE p.statut = 'LIBRE' ORDER BY p.numero";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    places.add(mapResultSetToPlace(rs));
                }
            }
        }
        return places;
    }
    
    // Trouver les places occupées
    public List<Place> findPlacesOccupees() throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT p.* FROM Place p WHERE p.statut = 'OCCUPEE' ORDER BY p.numero";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                places.add(mapResultSetToPlace(rs));
            }
        }
        return places;
    }
    

    // Occuper une place
    public boolean occuperPlace(int placeId) throws SQLException {
        String sql = "UPDATE Place SET statut = 'OCCUPEE' WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, placeId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Libérer une place
    public boolean libererPlace(int placeId) throws SQLException {
        String sql = "UPDATE Place SET statut = 'LIBRE' WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, placeId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    
    
    // Mettre à jour une place
    public boolean update(Place place) throws SQLException {
        String sql = "UPDATE Place SET numero = ?, statut = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, place.getNumero());
            stmt.setString(2, place.getStatut());
            stmt.setInt(3, place.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Supprimer une place
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Place WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Lister toutes les places
    public List<Place> findAll() throws SQLException {
        List<Place> places = new ArrayList<>();
        String sql = "SELECT p.* FROM Place p ORDER BY p.numero";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                places.add(mapResultSetToPlace(rs));
            }
        }
        return places;
    }
    
    // Compter les places disponibles
    public int countPlacesDisponibles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Place WHERE statut = 'LIBRE'";
        
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
    
    // Compter toutes les places
    public int countAllPlaces() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Place";
        
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
    
    // Compter les places occupées
    public int countPlacesOccupees() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Place WHERE statut = 'OCCUPEE'";
        
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
    
    
    // Réinitialiser toutes les places (pour les tests)
    public boolean resetAllPlaces() throws SQLException {
        String sql = "UPDATE Place SET statut = 'LIBRE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated >= 0;
        }
    }
    
    // Méthode utilitaire pour mapper ResultSet -> Place
    private Place mapResultSetToPlace(ResultSet rs) throws SQLException {
        Place place = new Place();
        place.setId(rs.getInt("id"));
        place.setNumero(rs.getString("numero"));
        place.setStatut(rs.getString("statut"));
        return place;
    }
}