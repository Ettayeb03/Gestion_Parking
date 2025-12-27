package com.gestion.parking.view;

import javax.swing.*;
import java.awt.*;
import com.gestion.parking.service.*;
import com.gestion.parking.model.*;

public class EntreeDialog extends JDialog {
    private TicketService ticketService;
    private VehiculeService vehiculeService;
    private PlaceService placeService;
    private boolean success = false;
    
    public EntreeDialog(JFrame parent) {
        super(parent, "Enregistrer une Entrée", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        ticketService = new TicketService();
        vehiculeService = new VehiculeService();
        placeService = new PlaceService();
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Titre
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Enregistrement d'Entrée");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Immatriculation
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Immatriculation:"), gbc);
        gbc.gridx = 1;
        JTextField immatField = new JTextField(15);
        panel.add(immatField, gbc);
        
        // Vérifier le véhicule
        gbc.gridx = 1; gbc.gridy = 2;
        JButton checkBtn = new JButton("Vérifier véhicule");
        checkBtn.addActionListener(e -> {
            String immat = immatField.getText().trim().toUpperCase();
            if (!immat.isEmpty()) {
                Vehicule v = vehiculeService.trouverVehiculeParImmatriculation(immat);
                if (v == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Véhicule non enregistré.\n" +
                        "Il sera créé automatiquement.\n" +
                        "Veuillez saisir le nom du propriétaire.", 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Véhicule trouvé:\n" +
                        "Propriétaire: " + v.getProprietaire(), 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        panel.add(checkBtn, gbc);
        
        // Propriétaire (si nouveau véhicule)
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Propriétaire:"), gbc);
        gbc.gridx = 1;
        JTextField proprioField = new JTextField(15);
        panel.add(proprioField, gbc);
        
        // Vérifier les places disponibles
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        int placesDispo = placeService.compterPlacesDisponibles();
        boolean parkingComplet = placeService.estParkingComplet();
        
        JLabel placesLabel = new JLabel();
        placesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        if (parkingComplet) {
            placesLabel.setText("PARKING COMPLET - Aucune place disponible");
            placesLabel.setForeground(Color.RED);
        } else {
            placesLabel.setText("Places disponibles: " + placesDispo);
            placesLabel.setForeground(new Color(0, 150, 0));
        }
        
        panel.add(placesLabel, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton enregistrerBtn = createButton("Enregistrer Entrée", new Color(76, 175, 80));
        JButton annulerBtn = createButton("Annuler", Color.LIGHT_GRAY);
        
        enregistrerBtn.addActionListener(e -> {
            String immatriculation = immatField.getText().trim().toUpperCase();
            String proprietaire = proprioField.getText().trim();
            
            if (immatriculation.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez saisir l'immatriculation", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (parkingComplet) {
                JOptionPane.showMessageDialog(this, 
                    "Impossible d'enregistrer l'entrée:\n" +
                    "Le parking est complet.", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Vérifier si le véhicule existe, sinon le créer
                Vehicule vehicule = vehiculeService.trouverVehiculeParImmatriculation(immatriculation);
                if (vehicule == null) {
                    if (proprietaire.isEmpty()) {
                        JOptionPane.showMessageDialog(this, 
                            "Veuillez saisir le nom du propriétaire\n" +
                            "pour ce nouveau véhicule.", 
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    vehicule = vehiculeService.enregistrerVehicule(immatriculation, proprietaire);
                }
                
                // Enregistrer l'entrée
                Ticket ticket = ticketService.enregistrerEntree(immatriculation);
                
                if (ticket != null) {
                    String message = String.format(
                        "✅ Entrée enregistrée!\n\n" +
                        "Détails:\n" +
                        "Véhicule: %s\n" +
                        "Propriétaire: %s\n" +
                        "Place: %s\n" +
                        "Heure d'entrée: %s\n\n" +
                        "N° Ticket: %d",
                        vehicule.getImmatriculation(),
                        vehicule.getProprietaire(),
                        ticket.getPlace().getNumero(),
                        ticket.getEntree().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                        ticket.getId()
                    );
                    
                    JOptionPane.showMessageDialog(this, message, 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    success = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Erreur lors de l'enregistrement de l'entrée.", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        annulerBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(enregistrerBtn);
        buttonPanel.add(annulerBtn);
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        return button;
    }
    
    public boolean isSuccess() {
        return success;
    }
}