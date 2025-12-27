package com.gestion.parking.view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import com.gestion.parking.service.*;
import com.gestion.parking.model.*;

public class PlacesFrame extends JInternalFrame {
    private PlaceService placeService;
    private JTable table;
    private DefaultTableModel tableModel;
    
    public PlacesFrame() {
        super("Gestion des Places", true, true, true, true);
        setSize(1000, 600);
        
        placeService = new PlaceService();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Gestion des Places");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Boutons d'action avec les mêmes couleurs que les autres frames
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton addBtn = createButton("Ajouter", new Color(33, 150, 243)); // Bleu
        JButton editBtn = createButton("Modifier", new Color(255, 152, 0)); // Orange
        JButton deleteBtn = createButton("Supprimer", new Color(244, 67, 54)); // Rouge
        JButton refreshBtn = createButton("Actualiser", new Color(76, 175, 80)); // Vert
        
        addBtn.addActionListener(e -> addPlace());
        editBtn.addActionListener(e -> editPlace());
        deleteBtn.addActionListener(e -> deletePlace());
        refreshBtn.addActionListener(e -> {
            System.out.println("Bouton Actualiser cliqué"); // Debug
            loadData();
        });
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Numéro", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Style du header - même jaune que les autres frames
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(255, 193, 7));
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Renderer pour les statuts - adapté au style des autres frames
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String statut = value.toString();
                    if (statut.equals("LIBRE")) {
                        c.setBackground(new Color(200, 230, 201)); // Vert clair
                        setForeground(new Color(27, 94, 32));
                    } else if (statut.equals("OCCUPEE")) {
                        c.setBackground(new Color(255, 205, 210)); // Rouge clair
                        setForeground(new Color(183, 28, 28));
                    }
                    setHorizontalAlignment(CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel statistiques - style simplifié
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainPanel.add(statsPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Effet hover léger
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void loadData() {
        try {
            System.out.println("Chargement des données..."); // Debug
            
            // Vider la table
            tableModel.setRowCount(0);
            
            // Récupérer les données depuis le service
            List<Place> places = placeService.listerToutesPlaces();
            
            if (places == null) {
                System.out.println("La liste des places est null");
                JOptionPane.showMessageDialog(this, 
                    "Impossible de charger les places.\n" +
                    "Vérifiez la connexion à la base de données.", 
                    "Erreur de chargement", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            System.out.println("Nombre de places trouvées: " + places.size()); // Debug
            
            // Ajouter les places à la table
            for (Place place : places) {
                tableModel.addRow(new Object[]{
                    place.getId(),
                    place.getNumero(),
                    place.getStatut()
                });
            }
            
            // Mettre à jour l'affichage
            tableModel.fireTableDataChanged();
            
            // Mettre à jour les statistiques
            updateStatsPanel();
            
            // Message de confirmation (optionnel)
            System.out.println("Données chargées avec succès: " + places.size() + " places");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données: " + e.getMessage());
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement des places :\n" + 
                e.getMessage() + "\n" +
                "Veuillez vérifier la connexion à la base de données.", 
                "Erreur de chargement", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatsPanel() {
        // Obtenir le panel de statistiques
        JPanel mainPanel = (JPanel) getContentPane();
        JPanel statsPanel = (JPanel) mainPanel.getComponent(2); // Index 2 = BorderLayout.SOUTH
        
        // Vider le panel de statistiques
        statsPanel.removeAll();
        
        try {
            // Calculer les statistiques
            int totalPlaces = placeService.compterToutesPlaces();
            int placesOccupees = placeService.compterPlacesOccupees();
            int placesLibres = placeService.compterPlacesDisponibles();
            double tauxOccupation = placeService.calculerTauxOccupation();
            
            // Ajouter les statistiques mises à jour
            statsPanel.add(createStatItem("Total Places", String.valueOf(totalPlaces), Color.LIGHT_GRAY));
            statsPanel.add(createStatItem("Places Libres", String.valueOf(placesLibres), new Color(200, 230, 201)));
            statsPanel.add(createStatItem("Places Occupées", String.valueOf(placesOccupees), new Color(255, 205, 210)));
            statsPanel.add(createStatItem("Taux Occupation", String.format("%.1f%%", tauxOccupation), new Color(255, 224, 178)));
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des statistiques: " + e.getMessage());
            
            // Afficher des valeurs par défaut en cas d'erreur
            statsPanel.add(createStatItem("Total Places", "Erreur", Color.LIGHT_GRAY));
            statsPanel.add(createStatItem("Places Libres", "Erreur", new Color(200, 230, 201)));
            statsPanel.add(createStatItem("Places Occupées", "Erreur", new Color(255, 205, 210)));
            statsPanel.add(createStatItem("Taux Occupation", "Erreur", new Color(255, 224, 178)));
        }
        
        // Rafraîchir l'affichage
        statsPanel.revalidate();
        statsPanel.repaint();
    }
    
    private JPanel createStatItem(String title, String value, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        panel.setBackground(bgColor);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(Color.BLACK);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(Color.BLACK);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addPlace() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Ajouter une Place", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label = new JLabel("Numéro:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label, gbc);
        gbc.gridx = 1;
        JTextField numeroField = new JTextField(15);
        numeroField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(numeroField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton saveBtn = createButton("Enregistrer", new Color(76, 175, 80)); // Vert
        JButton cancelBtn = createButton("Annuler", Color.LIGHT_GRAY);
        
        saveBtn.addActionListener(e -> {
            try {
                String numero = numeroField.getText().trim();
                
                // Validation améliorée avec messages clairs
                if (numero.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Veuillez saisir un numéro de place.", 
                        "Numéro manquant", JOptionPane.WARNING_MESSAGE);
                    numeroField.requestFocus();
                    return;
                }
                
                Place place = placeService.ajouterPlace(numero);
                if (place != null) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Place ajoutée avec succès !\n" +
                        "Numéro : " + numero, 
                        "Ajout réussi", JOptionPane.INFORMATION_MESSAGE);
                    loadData(); // Recharger les données après l'ajout
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Échec de l'ajout.\n" +
                        "Cette place existe peut-être déjà dans le système.", 
                        "Erreur d'ajout", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erreur lors de l'ajout :\n" + 
                    ex.getMessage() + "\n" +
                    "Veuillez vérifier vos données et réessayer.", 
                    "Erreur système", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void editPlace() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une place dans la liste pour la modifier.", 
                "Sélection requise", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int placeId = (int) tableModel.getValueAt(selectedRow, 0);
        Place place = placeService.trouverPlaceParId(placeId);
        
        if (place == null) {
            JOptionPane.showMessageDialog(this, 
                "Place non trouvée dans la base de données.\n" +
                "Elle a peut-être été supprimée par un autre utilisateur.", 
                "Place introuvable", JOptionPane.ERROR_MESSAGE);
            loadData(); // Recharger pour synchroniser
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Modifier la Place #" + placeId, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label1 = new JLabel("Numéro:");
        label1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label1, gbc);
        gbc.gridx = 1;
        JTextField numeroField = new JTextField(place.getNumero(), 15);
        numeroField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(numeroField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel label2 = new JLabel("Statut:");
        label2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label2, gbc);
        gbc.gridx = 1;
        JComboBox<String> statutCombo = new JComboBox<>(new String[]{"LIBRE", "OCCUPEE"});
        statutCombo.setSelectedItem(place.getStatut());
        statutCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(statutCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton saveBtn = createButton("Enregistrer", new Color(76, 175, 80)); // Vert
        JButton cancelBtn = createButton("Annuler", Color.LIGHT_GRAY);
        
        saveBtn.addActionListener(e -> {
            try {
                String numero = numeroField.getText().trim();
                String statut = (String) statutCombo.getSelectedItem();
                
                // Validation améliorée
                if (numero.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Le numéro de place ne peut pas être vide.", 
                        "Numéro invalide", JOptionPane.WARNING_MESSAGE);
                    numeroField.requestFocus();
                    return;
                }
                
                // Vérifier si des modifications ont été faites
                if (numero.equals(place.getNumero()) && statut.equals(place.getStatut())) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Aucune modification détectée.\n" +
                        "Les informations sont identiques aux données existantes.", 
                        "Aucune modification", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                place.setNumero(numero);
                place.setStatut(statut);
                
                boolean success = placeService.mettreAJourPlace(place);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Place modifiée avec succès !\n" +
                        "Les nouvelles informations ont été enregistrées.", 
                        "Modification réussie", JOptionPane.INFORMATION_MESSAGE);
                    loadData(); // Recharger les données après modification
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Échec de la modification.\n" +
                        "Une erreur est survenue lors de la mise à jour.", 
                        "Erreur de modification", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erreur lors de la modification :\n" + 
                    ex.getMessage() + "\n" +
                    "Veuillez vérifier vos données et réessayer.", 
                    "Erreur système", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deletePlace() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner une place à supprimer dans la liste.", 
                "Sélection requise", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int placeId = (int) tableModel.getValueAt(selectedRow, 0);
        String numero = (String) tableModel.getValueAt(selectedRow, 1);
        String statut = (String) tableModel.getValueAt(selectedRow, 2);
        
        // Message de confirmation plus détaillé
        int confirm = JOptionPane.showConfirmDialog(this, 
            "CONFIRMATION DE SUPPRESSION\n\n" +
            "Voulez-vous vraiment supprimer cette place ?\n\n" +
            "Détails de la place :\n" +
            "• ID : " + placeId + "\n" +
            "• Numéro : " + numero + "\n" +
            "• Statut : " + statut + "\n\n" +
            "ATTENTION : Cette action est irréversible !\n" +
            "Toutes les données associées seront perdues.", 
            "Confirmer la suppression", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = placeService.supprimerPlace(placeId);
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Place supprimée avec succès !\n" +
                        "Numéro : " + numero, 
                        "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
                    loadData(); // Recharger les données après suppression
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Échec de la suppression.\n" +
                        "Cette place ne peut peut-être pas être supprimée car elle est utilisée dans des tickets actifs.", 
                        "Erreur de suppression", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la suppression :\n\n" + 
                    "Message : " + e.getMessage() + "\n\n" +
                    "Veuillez réessayer ou contacter l'administrateur.", 
                    "Erreur système", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}