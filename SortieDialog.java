package com.gestion.parking.view;

import javax.swing.*;
import java.awt.*;
import com.gestion.parking.service.*;
import com.gestion.parking.model.*;

public class SortieDialog extends JDialog {
    private TicketService ticketService;
    private boolean success = false;
    
    public SortieDialog(JFrame parent) {
        super(parent, "Enregistrer une Sortie", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        ticketService = new TicketService();
        
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
        JLabel titleLabel = new JLabel("Enregistrement de Sortie");
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
        
        // Vérifier si le véhicule est dans le parking
        gbc.gridx = 1; gbc.gridy = 2;
        JButton checkBtn = new JButton("Vérifier présence");
        checkBtn.addActionListener(e -> {
            String immat = immatField.getText().trim().toUpperCase();
            if (!immat.isEmpty()) {
                boolean dansParking = ticketService.estDansParking(immat);
                if (dansParking) {
                    JOptionPane.showMessageDialog(this, 
                        "Véhicule trouvé dans le parking.", 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Ce véhicule n'est pas dans le parking.", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(checkBtn, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton enregistrerBtn = createButton("Enregistrer Sortie", new Color(255, 87, 34));
        JButton annulerBtn = createButton("Annuler", Color.LIGHT_GRAY);
        
        enregistrerBtn.addActionListener(e -> {
            String immatriculation = immatField.getText().trim().toUpperCase();
            
            if (immatriculation.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Veuillez saisir l'immatriculation", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Ticket ticket = ticketService.enregistrerSortie(immatriculation);
                
                if (ticket != null) {
                    // Vérifier si c'est un abonné
                    AbonneService abonneService = new AbonneService();
                    boolean estAbonne = abonneService.estAbonneValide(immatriculation);
                    
                    String message;
                    if (estAbonne) {
                        message = String.format(
                            "==Sortie enregistrée!==\n\n" +
                            "Détails:\n" +
                            "Véhicule: %s\n" +
                            "Durée: %s\n" +
                            "Montant: GRATUIT (Abonné)\n\n" +
                            "La place a été libérée.",
                            ticket.getVehicule().getImmatriculation(),
                            ticketService.obtenirDureeStationnement(ticket)
                        );
                    } else {
                        message = String.format(
                            "==Sortie enregistrée!==\n\n" +
                            "Détails:\n" +
                            "Véhicule: %s\n" +
                            "Durée: %s\n" +
                            "Montant dû: %.2f DH\n\n" +
                            "La place a été libérée.\n" +
                            "Veuillez procéder au paiement.",
                            ticket.getVehicule().getImmatriculation(),
                            ticketService.obtenirDureeStationnement(ticket),
                            ticket.getMontant()
                        );
                    }
                    
                    JOptionPane.showMessageDialog(this, message, 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    success = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Aucun ticket en cours trouvé pour ce véhicule.", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
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