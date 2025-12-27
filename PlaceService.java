package com.gestion.parking.service;

import com.gestion.parking.dao.PlaceDAO;
import com.gestion.parking.model.Place;
import java.sql.SQLException;
import java.util.List;

public class PlaceService {
    private PlaceDAO placeDAO;
    
    public PlaceService() {
        this.placeDAO = new PlaceDAO();
    }
    
    // Ajouter une nouvelle place
    public Place ajouterPlace(String numero) {
        try {
            // Vérifier si la place existe déjà
            Place existante = placeDAO.findByNumero(numero);
            if (existante != null) {
                throw new IllegalArgumentException("Une place avec ce numéro existe déjà");
            }
            
            Place place = new Place();
            place.setNumero(numero);
            place.setStatut("LIBRE"); // Par défaut, la place est libre
            
            placeDAO.create(place);
            return place;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la place: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Trouver une place par numéro
    public Place trouverPlaceParNumero(String numero) {
        try {
            return placeDAO.findByNumero(numero);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de la place: " + e.getMessage());
            return null;
        }
    }
    
    // Trouver une place par ID
    public Place trouverPlaceParId(int id) {
        try {
            return placeDAO.findById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de la place par ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Trouver une place libre (méthode CORRIGÉE)
    public Place trouverPlaceLibre() {
        try {
            List<Place> placesLibres = placeDAO.findPlacesLibres();
            if (placesLibres == null || placesLibres.isEmpty()) {
                return null;
            }
            return placesLibres.get(0); // Prendre la première place libre
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche d'une place libre: " + e.getMessage());
            return null;
        }
    }
    
    // Occuper une place
    public boolean occuperPlace(int placeId) {
        try {
            // Vérifier si la place est libre
            Place place = trouverPlaceParId(placeId);
            if (place == null) {
                throw new IllegalArgumentException("Place non trouvée");
            }
            
            if (!"LIBRE".equals(place.getStatut())) {
                throw new IllegalStateException("La place n'est pas libre");
            }
            
            // Mettre à jour le statut
            place.setStatut("OCCUPEE");
            return placeDAO.update(place);
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'occupation de la place: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Libérer une place
    public boolean libererPlace(int placeId) {
        try {
            // Vérifier si la place est occupée
            Place place = trouverPlaceParId(placeId);
            if (place == null) {
                throw new IllegalArgumentException("Place non trouvée");
            }
            
            if (!"OCCUPEE".equals(place.getStatut())) {
                throw new IllegalStateException("La place n'est pas occupée");
            }
            
            // Mettre à jour le statut
            place.setStatut("LIBRE");
            return placeDAO.update(place);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la libération de la place: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Mettre à jour une place
    public boolean mettreAJourPlace(Place place) {
        try {
            return placeDAO.update(place);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la place: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Supprimer une place
    public boolean supprimerPlace(int id) {
        try {
            Place place = trouverPlaceParId(id);
            if (place == null) {
                System.err.println("Place non trouvée avec ID: " + id);
                return false;
            }
            
            if (!"LIBRE".equals(place.getStatut())) {
                throw new IllegalStateException("Impossible de supprimer une place occupée");
            }
            
            return placeDAO.delete(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la place: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IllegalStateException e) {
            System.err.println("Erreur métier: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la suppression: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Lister toutes les places
    public List<Place> listerToutesPlaces() {
        try {
            return placeDAO.findAll();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des places: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Lister les places libres (méthode CORRIGÉE)
    public List<Place> listerPlacesLibres() {
        try {
            return placeDAO.findPlacesLibres();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des places libres: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Lister les places occupées
    public List<Place> listerPlacesOccupees() {
        try {
            return placeDAO.findPlacesOccupees();
        } catch (SQLException e) {
            System.err.println("Erreur lors du listage des places occupées: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Vérifier si une place est libre
    public boolean estPlaceLibre(int placeId) {
        try {
            Place place = trouverPlaceParId(placeId);
            return place != null && "LIBRE".equals(place.getStatut());
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de la place: " + e.getMessage());
            return false;
        }
    }
    
    // Compter les places disponibles (méthode CORRIGÉE)
    public int compterPlacesDisponibles() {
        try {
            return placeDAO.countPlacesDisponibles();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des places disponibles: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    // Compter toutes les places (méthode CORRIGÉE)
    public int compterToutesPlaces() {
        try {
            return placeDAO.countAllPlaces();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des places totales: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    // Compter les places occupées (méthode CORRIGÉE)
    public int compterPlacesOccupees() {
        try {
            return placeDAO.countPlacesOccupees();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des places occupées: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    // Calculer le taux d'occupation (méthode CORRIGÉE)
    public double calculerTauxOccupation() {
        int total = compterToutesPlaces();
        int occupees = compterPlacesOccupees();
        
        if (total == 0) {
            return 0.0;
        }
        
        return (double) occupees / total * 100.0;
    }
    
    // Vérifier si le parking est complet
    public boolean estParkingComplet() {
        return compterPlacesDisponibles() == 0;
    }
    
    // Réinitialiser toutes les places (pour les tests)
    public boolean reinitialiserPlaces() {
        try {
            return placeDAO.resetAllPlaces();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réinitialisation des places: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}