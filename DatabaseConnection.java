package com.gestion.parking.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_parking";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    // Bloc static pour charger le driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL chargé avec succès.");
        } catch (ClassNotFoundException e) {
            System.err.println("ERREUR: Driver MySQL non trouvé!");
            throw new RuntimeException("Driver MySQL requis non trouvé", e);
        }
    }
    
    // Obtenir une connexion (version simplifiée et fiable)
    public static Connection getConnection() throws SQLException {
        SQLException lastException = null;
        
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                
                // Configuration de la connexion
                conn.setAutoCommit(true); // Auto-commit activé par défaut
                
                // Vérifier que la connexion est valide
                if (conn.isValid(5)) {
                    return conn;
                } else {
                    conn.close();
                    throw new SQLException("Connexion invalide");
                }
                
            } catch (SQLException e) {
                lastException = e;
                System.err.println("Tentative " + (i + 1) + "/" + MAX_RETRIES + " échouée: " + e.getMessage());
                
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interruption lors de la reconnexion", ie);
                    }
                }
            }
        }
        
        // Si on arrive ici, toutes les tentatives ont échoué
        throw new SQLException("Impossible d'établir la connexion après " + MAX_RETRIES + " tentatives", lastException);
    }
    
    // Fermer une connexion
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
    
    // Fermer les ressources (Statement et ResultSet)
    public static void closeResources(java.sql.Statement stmt, java.sql.ResultSet rs) {
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
    
    // Fermer les ressources (Connection, Statement et ResultSet)
    public static void closeAll(Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
        closeResources(stmt, rs);
        closeConnection(conn);
    }
    
    // Vérifier si la connexion est valide
    public static boolean isConnectionValid() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed() && conn.isValid(5);
        } catch (SQLException e) {
            return false;
        } finally {
            closeConnection(conn);
        }
    }
    
    // Tester la connexion (utile pour le démarrage)
    public static void testConnection() {
        System.out.println("Test de connexion à la base de données...");
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn != null && conn.isValid(5)) {
                System.out.println("✓ Connexion à la base de données réussie!");
                System.out.println("  URL: " + URL);
                System.out.println("  Utilisateur: " + USER);
            }
        } catch (SQLException e) {
            System.err.println("✗ Échec de connexion à la base de données!");
            System.err.println("  Erreur: " + e.getMessage());
            System.err.println("  Assurez-vous que:");
            System.err.println("  1. MySQL est démarré");
            System.err.println("  2. La base 'gestion_parking' existe");
            System.err.println("  3. Les identifiants sont corrects");
            System.err.println("  4. Le serveur MySQL écoute sur le port 3306");
        } finally {
            closeConnection(conn);
        }
    }
    
    // Méthode principale pour tester directement
    public static void main(String[] args) {
        testConnection();
    }
}