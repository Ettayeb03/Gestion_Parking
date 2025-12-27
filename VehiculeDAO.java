package com.gestion.parking.dao;

import com.gestion.parking.model.Vehicule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {
    
	// Créer un véhicule
	public int create(Vehicule vehicule) throws SQLException {
	    // Valider l'immatriculation avant l'insertion
	    validerImmatriculation(vehicule.getImmatriculation());
	    
	    String sql = "INSERT INTO Vehicule (immatriculation, proprietaire) VALUES (?, ?)";
	    
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	        
	        stmt.setString(1, vehicule.getImmatriculation().toUpperCase());
	        stmt.setString(2, vehicule.getProprietaire());
	        
	        int affectedRows = stmt.executeUpdate();
	        
	        if (affectedRows > 0) {
	            try (ResultSet rs = stmt.getGeneratedKeys()) {
	                if (rs.next()) {
	                    int id = rs.getInt(1);
	                    vehicule.setId(id);
	                    return id;
	                }
	            }
	        }
	        return -1;
	    }
	}

	// Méthode de validation de l'immatriculation
	private void validerImmatriculation(String immatriculation) throws IllegalArgumentException {
	    if (immatriculation == null || immatriculation.trim().isEmpty()) {
	        throw new IllegalArgumentException("L'immatriculation ne peut pas être vide");
	    }
	    
	    // Normaliser l'immatriculation
	    immatriculation = immatriculation.trim().toUpperCase();
	    
	    // Vérifier les conditions requises
	    boolean aLettreMajuscule = false;
	    boolean aTiret = false;
	    boolean aChiffre = false;
	    
	    for (char c : immatriculation.toCharArray()) {
	        if (Character.isUpperCase(c)) {
	            aLettreMajuscule = true;
	        } else if (Character.isDigit(c)) {
	            aChiffre = true;
	        } else if (c == '-') {
	            aTiret = true;
	        }
	    }
	    
	    // Vérifier que les 3 conditions sont remplies
	    if (!aLettreMajuscule) {
	        throw new IllegalArgumentException("L'immatriculation doit contenir au moins une lettre majuscule");
	    }
	    
	    if (!aChiffre) {
	        throw new IllegalArgumentException("L'immatriculation doit contenir au moins un chiffre");
	    }
	    
	    if (!aTiret) {
	        throw new IllegalArgumentException("L'immatriculation doit contenir au moins un tiret (-)");
	    }
	 
	    
	    // Vérifier les caractères autorisés
	    if (!immatriculation.matches("^[A-Z0-9-]+$")) {
	        throw new IllegalArgumentException(
	            "L'immatriculation ne peut contenir que:\n" +
	            "- Lettres majuscules (A-Z)\n" +
	            "- Chiffres (0-9)\n" +
	            "- Tiret (-)\n" +
	            "Format saisi: " + immatriculation
	        );
	    }
	    
	    // Vérifier que le tiret n'est pas au début ou à la fin
	    if (immatriculation.startsWith("-") || immatriculation.endsWith("-")) {
	        throw new IllegalArgumentException("Le tiret ne peut pas être au début ou à la fin");
	    }
	    
	    // Vérifier qu'il n'y a pas de doubles tirets
	    if (immatriculation.contains("--")) {
	        throw new IllegalArgumentException("Les doubles tirets ne sont pas autorisés");
	    }
	    
	    // Vérifier la longueur totale (entre 5 et 10 caractères)
	    if (immatriculation.length() < 5) {
	        throw new IllegalArgumentException("L'immatriculation doit contenir au moins 5 caractères");
	    }
	    
	    if (immatriculation.length() > 10) {
	        throw new IllegalArgumentException("L'immatriculation ne peut pas dépasser 10 caractères");
	    }
	    
	    // Vérifier que le format général est correct
	    if (!immatriculation.matches("^(?=.*[A-Z])(?=.*[0-9]).*$")) {
	        throw new IllegalArgumentException(
	            "L'immatriculation doit contenir à la fois des lettres et des chiffres"
	        );
	    }
	}
    
    // Trouver par immatriculation
    public Vehicule findByImmatriculation(String immatriculation) throws SQLException {
        String sql = "SELECT * FROM Vehicule WHERE immatriculation = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, immatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicule(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver par ID
    public Vehicule findById(int id) throws SQLException {
        String sql = "SELECT * FROM Vehicule WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicule(rs);
                }
            }
        }
        return null;
    }
    
    // Trouver par propriétaire
    public List<Vehicule> findByProprietaire(String proprietaire) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM Vehicule WHERE proprietaire LIKE ? ORDER BY immatriculation";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + proprietaire + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        return vehicules;
    }
    
    // Mettre à jour un véhicule
    public boolean update(Vehicule vehicule) throws SQLException {
        String sql = "UPDATE Vehicule SET immatriculation = ?, proprietaire = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getProprietaire());
            stmt.setInt(3, vehicule.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Supprimer un véhicule
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Vehicule WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Lister tous les véhicules
    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM Vehicule ORDER BY immatriculation";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        }
        return vehicules;
    }
    
    // Vérifier si immatriculation existe
    public boolean exists(String immatriculation) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicule WHERE immatriculation = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, immatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    // Compter tous les véhicules
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicule";
        
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
    
    // Méthode utilitaire pour mapper ResultSet -> Vehicule
    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getInt("id"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        vehicule.setProprietaire(rs.getString("proprietaire"));
        return vehicule;
    }
}