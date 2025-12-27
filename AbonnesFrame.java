package com.gestion.parking.view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.gestion.parking.service.*;
import com.gestion.parking.model.*;

public class AbonnesFrame extends JInternalFrame {
    private AbonneService abonneService;
    private VehiculeService vehiculeService;
    private JTable table;
    private DefaultTableModel tableModel;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public AbonnesFrame() {
        super("Gestion des Abonnés", true, true, true, true);
        setSize(1300, 600);
        
        abonneService = new AbonneService();
        vehiculeService = new VehiculeService();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Gestion des Abonnés - Prix mensuel: 700 DH");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        
        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton addBtn = createButton("Nouvel Abonné", new Color(255, 193, 7));
        JButton editBtn = createButton("Modifier", new Color(33, 150, 243));
        JButton renewBtn = createButton("Renouveler", new Color(76, 175, 80));
        JButton deleteBtn = createButton("Supprimer", new Color(244, 67, 54));
        JButton detailsBtn = createButton("Détails", new Color(156, 39, 176));
        JButton refreshBtn = createButton("Actualiser", Color.LIGHT_GRAY);
        
        addBtn.addActionListener(e -> addAbonne());
        editBtn.addActionListener(e -> editAbonne());
        renewBtn.addActionListener(e -> renewAbonnement());
        deleteBtn.addActionListener(e -> deleteAbonne());
        detailsBtn.addActionListener(e -> showMontantDetails());
        refreshBtn.addActionListener(e -> loadData());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(renewBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(detailsBtn);
        buttonPanel.add(refreshBtn);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "ID Véhicule", "Immatriculation", "Propriétaire", 
                           "Date Début", "Date Fin", "Durée (mois)", "Montant (DH)", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6 || columnIndex == 7) {
                    return Double.class;
                }
                return String.class;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Style du header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(255, 193, 7));
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Renderer pour la durée et le montant
        table.getColumnModel().getColumn(6).setCellRenderer(new MontantRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new MontantRenderer());
        
        // Renderer pour le statut
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String statut = value.toString();
                    if (statut.equals("Valide")) {
                        c.setBackground(new Color(200, 230, 201));
                        setForeground(new Color(27, 94, 32));
                    } else {
                        c.setBackground(new Color(255, 205, 210));
                        setForeground(new Color(183, 28, 28));
                    }
                    setHorizontalAlignment(CENTER);
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
     
        setContentPane(mainPanel);
    }
    
    // Renderer personnalisé pour les montants
    private class MontantRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Number) {
                double montant = ((Number) value).doubleValue();
                int montantInt = (int) Math.round(montant);
                setText(montantInt + " "); 
                setForeground(new Color(0, 100, 0));
                setHorizontalAlignment(RIGHT);
            } else if (value != null) {
                setText(value.toString());
                setForeground(Color.BLACK);
                setHorizontalAlignment(LEFT);
            }
            
            return c;
        }
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
    
    private void loadData() {
        tableModel.setRowCount(0);
        List<Abonne> abonnes = abonneService.listerTousAbonnes();
        
        if (abonnes != null) {
            for (Abonne a : abonnes) {
                String dateDebut = a.getDateDebut() != null ? 
                    a.getDateDebut().format(dateFormatter) : "";
                String dateFin = a.getDateFin() != null ? 
                    a.getDateFin().format(dateFormatter) : "";
                String statut = a.estValide() ? "Valide" : "Expiré";
                String immatriculation = "Inconnu";
                String proprietaire = "Inconnu";
                
                if (a.getVehicule() != null) {
                    immatriculation = a.getVehicule().getImmatriculation();
                    proprietaire = a.getVehicule().getProprietaire();
                }
                
                int dureeMois = a.getDureeMois();
                double montantTotal = a.getMontantTotal();
                
                tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getVehiculeId(),
                    immatriculation,
                    proprietaire,
                    dateDebut,
                    dateFin,
                    dureeMois,
                    montantTotal,
                    statut
                });
            }
        }
        
        tableModel.fireTableDataChanged();
    }
    
    // Méthode pour afficher les détails du montant
    private void showMontantDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un abonné", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int abonneId = (int) tableModel.getValueAt(selectedRow, 0);
        Abonne abonne = abonneService.trouverAbonneParId(abonneId);
        
        if (abonne == null) {
            JOptionPane.showMessageDialog(this, "Abonné non trouvé", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calculer les différentes valeurs
        int dureeMois = abonne.getDureeMois();
        double montantTotal = abonne.getMontantTotal();
        double montantPaye = abonne.getMontantPaye();
        
        // Créer le message détaillé
        String details = "<html><body style='width: 350px;'>" +
            "<h3>Détails de l'Abonnement</h3>" +
            "<hr>" +
            "<table border='0' cellpadding='5'>" +
            "<tr><td><b>Immatriculation:</b></td><td>" + abonne.getImmatriculation() + "</td></tr>" +
            "<tr><td><b>Propriétaire:</b></td><td>" + abonne.getProprietaire() + "</td></tr>" +
            "<tr><td><b>Date Début:</b></td><td>" + abonne.getDateDebut().format(dateFormatter) + "</td></tr>" +
            "<tr><td><b>Date Fin:</b></td><td>" + abonne.getDateFin().format(dateFormatter) + "</td></tr>" +
            "<tr><td><b>Durée totale:</b></td><td>" + dureeMois + " mois</td></tr>" +
            "<tr><td><b>Montant total:</b></td><td><b>" + String.format("%.2f DH", montantTotal) + "</b></td></tr>" +
            "</table><hr>" +
            "<table border='0' cellpadding='5'>" +
            "</table>" +
            "</body></html>";
        
        JOptionPane.showMessageDialog(this, details, 
            "Détails du Montant - Abonné ID: " + abonneId, 
            JOptionPane.INFORMATION_MESSAGE);
    }
    private void addAbonne() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Nouvel Abonné", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Immatriculation
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Immatriculation:"), gbc);
        gbc.gridx = 1;
        JTextField immatField = new JTextField(20);
        panel.add(immatField, gbc);
        
        // Vérifier le véhicule
        gbc.gridx = 1; gbc.gridy = 1;
        JButton checkBtn = new JButton("Vérifier véhicule");
        checkBtn.addActionListener(e -> {
            String immat = immatField.getText().trim().toUpperCase();
            if (!immat.isEmpty()) {
                Vehicule v = vehiculeService.trouverVehiculeParImmatriculation(immat);
                if (v == null) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Véhicule non trouvé. Il sera créé automatiquement.", 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Véhicule trouvé: " + v.getProprietaire(), 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        panel.add(checkBtn, gbc);
        
        // Dates
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date Début:"), gbc);
        gbc.gridx = 1;
        JSpinner debutSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor debutEditor = new JSpinner.DateEditor(debutSpinner, "dd/MM/yyyy");
        debutSpinner.setEditor(debutEditor);
        panel.add(debutSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Date Fin:"), gbc);
        gbc.gridx = 1;
        JSpinner finSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor finEditor = new JSpinner.DateEditor(finSpinner, "dd/MM/yyyy");
        finSpinner.setEditor(finEditor);
        panel.add(finSpinner, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = createButton("💾 Enregistrer", new Color(255, 193, 7));
        JButton cancelBtn = createButton("❌ Annuler", Color.LIGHT_GRAY);
        
        saveBtn.addActionListener(e -> {
            try {
                String immatriculation = immatField.getText().trim().toUpperCase();
                
                java.util.Date debutDate = (java.util.Date) debutSpinner.getValue();
                java.util.Date finDate = (java.util.Date) finSpinner.getValue();
                LocalDate dateDebut = debutDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                LocalDate dateFin = finDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                
                // Validation
                if (immatriculation.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Veuillez saisir l'immatriculation", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (dateFin.isBefore(dateDebut)) {
                    JOptionPane.showMessageDialog(dialog, "La date de fin doit être après la date de début", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Créer l'abonné
                Abonne abonne = abonneService.creerAbonne(immatriculation, dateDebut, dateFin);
                
                if (abonne != null) {
                    JOptionPane.showMessageDialog(dialog, "Abonné créé avec succès!", 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Erreur lors de la création de l'abonné", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void editAbonne() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un abonné", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int abonneId = (int) tableModel.getValueAt(selectedRow, 0);
        Abonne abonne = abonneService.trouverAbonneParId(abonneId);
        
        if (abonne == null) {
            JOptionPane.showMessageDialog(this, "Abonné non trouvé", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Modifier Abonné", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Informations (lecture seule)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Immatriculation:"), gbc);
        gbc.gridx = 1;
        String immat = abonne.getVehicule() != null ? 
            abonne.getVehicule().getImmatriculation() : "Inconnu";
        JLabel immatLabel = new JLabel(immat);
        panel.add(immatLabel, gbc);
        
        // Dates
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Date Début:"), gbc);
        gbc.gridx = 1;
        JSpinner debutSpinner = new JSpinner(new SpinnerDateModel(
            java.sql.Date.valueOf(abonne.getDateDebut()), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor debutEditor = new JSpinner.DateEditor(debutSpinner, "dd/MM/yyyy");
        debutSpinner.setEditor(debutEditor);
        panel.add(debutSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date Fin:"), gbc);
        gbc.gridx = 1;
        JSpinner finSpinner = new JSpinner(new SpinnerDateModel(
            java.sql.Date.valueOf(abonne.getDateFin()), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor finEditor = new JSpinner.DateEditor(finSpinner, "dd/MM/yyyy");
        finSpinner.setEditor(finEditor);
        panel.add(finSpinner, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = createButton("💾 Enregistrer", new Color(255, 193, 7));
        JButton cancelBtn = createButton("❌ Annuler", Color.LIGHT_GRAY);
        
        saveBtn.addActionListener(e -> {
            try {
                java.util.Date debutDate = (java.util.Date) debutSpinner.getValue();
                java.util.Date finDate = (java.util.Date) finSpinner.getValue();
                LocalDate dateDebut = debutDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                LocalDate dateFin = finDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                
                if (dateFin.isBefore(dateDebut)) {
                    JOptionPane.showMessageDialog(dialog, "La date de fin doit être après la date de début", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                abonne.setDateDebut(dateDebut);
                abonne.setDateFin(dateFin);
                
                boolean success = abonneService.mettreAJourAbonne(abonne);
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Abonné modifié avec succès!", 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Erreur lors de la modification", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void renewAbonnement() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un abonné", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int abonneId = (int) tableModel.getValueAt(selectedRow, 0);
        String immatriculation = (String) tableModel.getValueAt(selectedRow, 2);
        
        // Récupérer l'abonné actuel
        Abonne abonne = abonneService.trouverAbonneParId(abonneId);
        if (abonne == null) {
            JOptionPane.showMessageDialog(this, "Abonné non trouvé", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Boîte de dialogue pour la nouvelle date
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(new JLabel("Immatriculation:"));
        JLabel immatLabel = new JLabel(immatriculation);
        panel.add(immatLabel);
        
        panel.add(new JLabel("Date Début (nouvelle):"));
        JSpinner debutSpinner = new JSpinner(new SpinnerDateModel(
            java.sql.Date.valueOf(LocalDate.now()), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor debutEditor = new JSpinner.DateEditor(debutSpinner, "dd/MM/yyyy");
        debutSpinner.setEditor(debutEditor);
        panel.add(debutSpinner);
        
        panel.add(new JLabel("Date Fin (nouvelle):"));
        JSpinner finSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor finEditor = new JSpinner.DateEditor(finSpinner, "dd/MM/yyyy");
        finSpinner.setEditor(finEditor);
        panel.add(finSpinner);
        
        int result = JOptionPane.showConfirmDialog(this, 
            panel,
            "Renouveler Abonnement", 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                java.util.Date debutDate = (java.util.Date) debutSpinner.getValue();
                java.util.Date finDate = (java.util.Date) finSpinner.getValue();
                LocalDate nouvelleDateDebut = debutDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                LocalDate nouvelleDateFin = finDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                
                // Validation
                if (nouvelleDateFin.isBefore(nouvelleDateDebut)) {
                    JOptionPane.showMessageDialog(this, "La date de fin doit être après la date de début", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Calculer le montant du renouvellement
                int dureeMois = (int) java.time.temporal.ChronoUnit.MONTHS.between(nouvelleDateDebut, nouvelleDateFin) + 1;
                double montantRenouvellement = dureeMois * Abonne.getPrixMensuel();
                
                // Demander confirmation avec le montant
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "<html><b>Confirmer le renouvellement</b><br><br>" +
                    "Immatriculation: " + immatriculation + "<br>" +
                    "Date début: " + nouvelleDateDebut.format(dateFormatter) + "<br>" +
                    "Date fin: " + nouvelleDateFin.format(dateFormatter) + "<br>" +
                    "Durée: " + dureeMois + " mois<br>" +
                    "<b>Montant: " + String.format("%.0f DH", montantRenouvellement) + "</b></html>", 
                    "Confirmation du renouvellement", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
                
                // Mettre à jour les dates de l'abonné
                abonne.setDateDebut(nouvelleDateDebut);
                abonne.setDateFin(nouvelleDateFin);
                
                boolean success = abonneService.mettreAJourAbonne(abonne);
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "<html>Abonnement renouvelé avec succès!<br>" +
                        "Montant: <b>" + String.format("%.0f DH", montantRenouvellement) + "</b></html>", 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Erreur lors du renouvellement", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteAbonne() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un abonné", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int abonneId = (int) tableModel.getValueAt(selectedRow, 0);
        String immatriculation = (String) tableModel.getValueAt(selectedRow, 2);
        
        // Demander confirmation
        int confirm = JOptionPane.showConfirmDialog(this, 
            "<html><b>Êtes-vous sûr de vouloir supprimer cet abonné ?</b><br><br>" +
            "Immatriculation: " + immatriculation + "<br>" +
            "ID: " + abonneId + "</html>", 
            "Confirmation de suppression", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = abonneService.supprimerAbonne(abonneId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Abonné supprimé avec succès!", 
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Erreur lors de la suppression.", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}