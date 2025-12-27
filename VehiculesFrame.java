package com.gestion.parking.view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import com.gestion.parking.service.*;
import com.gestion.parking.model.*;

public class VehiculesFrame extends JInternalFrame {
    private VehiculeService vehiculeService;
    private JTable table;
    private DefaultTableModel tableModel;
    
    public VehiculesFrame() {
        super("Gestion des Véhicules", true, true, true, true);
        setSize(1000, 600);
        
        vehiculeService = new VehiculeService();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Gestion des Véhicules");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Boutons d'action avec les couleurs de votre premier code
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton addBtn = createButton("Ajouter", new Color(33, 150, 243)); // Bleu
        JButton editBtn = createButton("Modifier", new Color(255, 152, 0)); // Orange
        JButton deleteBtn = createButton("Supprimer", new Color(244, 67, 54)); // Rouge
        JButton refreshBtn = createButton("Actualiser", new Color(76, 175, 80)); // Vert
        
        addBtn.addActionListener(e -> addVehicule());
        editBtn.addActionListener(e -> editVehicule());
        deleteBtn.addActionListener(e -> deleteVehicule());
        refreshBtn.addActionListener(e -> loadData());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Immatriculation", "Propriétaire"};
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
        
        // Style du header - garde la couleur jaune de votre premier code
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(255, 193, 7)); // Jaune comme dans TicketsFrame
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
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
        tableModel.setRowCount(0);
        List<Vehicule> vehicules = vehiculeService.listerTousVehicules();
        
        if (vehicules != null) {
            for (Vehicule v : vehicules) {
                tableModel.addRow(new Object[]{
                    v.getId(),
                    v.getImmatriculation(),
                    v.getProprietaire()
                });
            }
        }
        
        tableModel.fireTableDataChanged();
    }
    
    private void addVehicule() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Ajouter un Véhicule", true);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label1 = new JLabel("Immatriculation:");
        label1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label1, gbc);
        gbc.gridx = 1;
        JTextField immatField = new JTextField(20);
        immatField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(immatField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel label2 = new JLabel("Propriétaire:");
        label2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label2, gbc);
        gbc.gridx = 1;
        JTextField proprioField = new JTextField(20);
        proprioField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(proprioField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton saveBtn = createButton("Enregistrer", new Color(76, 175, 80)); // Vert
        JButton cancelBtn = createButton("Annuler", Color.LIGHT_GRAY);
        
        saveBtn.addActionListener(e -> {
            try {
                String immatriculation = immatField.getText().trim().toUpperCase();
                String proprietaire = proprioField.getText().trim();
                
                // Validation améliorée avec messages clairs
                if (immatriculation.isEmpty() && proprietaire.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Veuillez saisir l'immatriculation ET le nom du propriétaire.", 
                        "Champs obligatoires manquants", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (immatriculation.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Le champ 'Immatriculation' est obligatoire.", 
                        "Immatriculation manquante", JOptionPane.WARNING_MESSAGE);
                    immatField.requestFocus();
                    return;
                }
                
                if (proprietaire.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Le champ 'Propriétaire' est obligatoire.", 
                        "Propriétaire manquant", JOptionPane.WARNING_MESSAGE);
                    proprioField.requestFocus();
                    return;
                }
                
                // Validation du format d'immatriculation (optionnel mais recommandé)
                if (!isValidImmatriculation(immatriculation)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Format d'immatriculation incorrect.\n" +
                        "Exemples valides : AB-123-CD, 1234-AB-56\n" +
                        "L'immatriculation sera enregistrée telle quelle.", 
                        "Format d'immatriculation", JOptionPane.INFORMATION_MESSAGE);
                }
                
                Vehicule vehicule = vehiculeService.enregistrerVehicule(immatriculation, proprietaire);
                if (vehicule != null) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Véhicule enregistré avec succès !\n" +
                        "Immatriculation : " + immatriculation + "\n" +
                        "Propriétaire : " + proprietaire, 
                        "Enregistrement réussi", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Échec de l'enregistrement.\n" +
                        "Ce véhicule existe peut-être déjà dans le système.", 
                        "Erreur d'enregistrement", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Erreur lors de l'enregistrement :\n" + 
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
    
    private boolean isValidImmatriculation(String immat) {
        return immat.matches("[A-Z0-9\\- ]{5,10}");
    }
    
    private void editVehicule() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un véhicule dans la liste pour le modifier.", 
                "Sélection requise", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int vehiculeId = (int) tableModel.getValueAt(selectedRow, 0);
        Vehicule vehicule = vehiculeService.trouverVehiculeParId(vehiculeId);
        
        if (vehicule == null) {
            JOptionPane.showMessageDialog(this, 
                "Véhicule non trouvé dans la base de données.\n" +
                "Il a peut-être été supprimé par un autre utilisateur.", 
                "Véhicule introuvable", JOptionPane.ERROR_MESSAGE);
            loadData(); // Recharger pour synchroniser
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Modifier Véhicule #" + vehiculeId, true);
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label1 = new JLabel("Immatriculation:");
        label1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label1, gbc);
        gbc.gridx = 1;
        JTextField immatField = new JTextField(vehicule.getImmatriculation(), 20);
        immatField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(immatField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel label2 = new JLabel("Propriétaire:");
        label2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(label2, gbc);
        gbc.gridx = 1;
        JTextField proprioField = new JTextField(vehicule.getProprietaire(), 20);
        proprioField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(proprioField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton saveBtn = createButton("Enregistrer", new Color(76, 175, 80)); // Vert
        JButton cancelBtn = createButton("Annuler", Color.LIGHT_GRAY);
        
        saveBtn.addActionListener(e -> {
            try {
                String immatriculation = immatField.getText().trim().toUpperCase();
                String proprietaire = proprioField.getText().trim();
                
                // Validation améliorée
                if (immatriculation.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "L'immatriculation ne peut pas être vide.", 
                        "Immatriculation invalide", JOptionPane.WARNING_MESSAGE);
                    immatField.requestFocus();
                    return;
                }
                
                if (proprietaire.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Le nom du propriétaire ne peut pas être vide.", 
                        "Propriétaire invalide", JOptionPane.WARNING_MESSAGE);
                    proprioField.requestFocus();
                    return;
                }
                
                // Vérifier si des modifications ont été faites
                if (immatriculation.equals(vehicule.getImmatriculation()) && 
                    proprietaire.equals(vehicule.getProprietaire())) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Aucune modification détectée.\n" +
                        "Les informations sont identiques aux données existantes.", 
                        "Aucune modification", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                vehicule.setImmatriculation(immatriculation);
                vehicule.setProprietaire(proprietaire);
                
                boolean success = vehiculeService.mettreAJourVehicule(vehicule);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Véhicule modifié avec succès !\n" +
                        "Les nouvelles informations ont été enregistrées.", 
                        "Modification réussie", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
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
    
    private void deleteVehicule() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "ℹ️ Veuillez sélectionner un véhicule à supprimer dans la liste.", 
                "Sélection requise", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int vehiculeId = (int) tableModel.getValueAt(selectedRow, 0);
        String immatriculation = (String) tableModel.getValueAt(selectedRow, 1);
        String proprietaire = (String) tableModel.getValueAt(selectedRow, 2);
        
        // Message de confirmation plus détaillé
        int confirm = JOptionPane.showConfirmDialog(this, 
            "CONFIRMATION DE SUPPRESSION\n\n" +
            "Voulez-vous vraiment supprimer ce véhicule ?\n\n" +
            "Détails du véhicule :\n" +
            "ID : " + vehiculeId + "\n" +
            "Immatriculation : " + immatriculation + "\n" +
            "Propriétaire : " + proprietaire + "\n\n" +
            "ATTENTION : Cette action est irréversible !\n" +
            "Toutes les données associées seront perdues.", 
            "Confirmer la suppression", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = vehiculeService.supprimerVehicule(vehiculeId);
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Véhicule supprimé avec succès !\n" +
                        "Immatriculation : " + immatriculation + "\n" +
                        "Propriétaire : " + proprietaire, 
                        "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
                
            } catch (IllegalStateException e) {
            	// Gérer les erreurs métier avec des messages spécifiques
                JOptionPane.showMessageDialog(this, 
                    "Impossible de supprimer le véhicule :\n\n" +
                    e.getMessage() + "\n\n" +
                    "Veuillez vérifier que :\n" +
                    "1. Le véhicule n'est pas actuellement dans le parking\n" +
                    "2. Tous ses tickets ont été supprimés ou archivés", 
                    "Suppression impossible", JOptionPane.ERROR_MESSAGE);
                
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