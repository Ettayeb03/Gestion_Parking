package com.gestion.parking.view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Comparator;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import com.gestion.parking.service.*;
import com.gestion.parking.model.*;

public class TicketsFrame extends JInternalFrame {
    private TicketService ticketService;
    private JTable table;
    private DefaultTableModel tableModel;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean sortAscending = true; // Pour alterner entre ascendant et descendant
    private int lastSortedColumn = -1; // Pour suivre la dernière colonne triée
    
    public TicketsFrame() {
        super("Gestion des Tickets", true, true, true, true);
        setSize(1200, 600);
        
        ticketService = new TicketService();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Gestion des Tickets");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton detailsBtn = createButton("Détails", new Color(33, 150, 243));
        JButton refreshBtn = createButton("Actualiser", new Color(76, 175, 80));
        
        detailsBtn.addActionListener(e -> showDetails());
        refreshBtn.addActionListener(e -> {
            sortAscending = true; // Réinitialiser le tri
            lastSortedColumn = -1;
            loadData();
        });
        
        buttonPanel.add(detailsBtn);
        buttonPanel.add(refreshBtn);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Immatriculation", "Propriétaire", "Place", 
                           "Entrée", "Sortie", "Montant", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Définir le type de chaque colonne pour un tri correct
                switch (columnIndex) {
                    case 0: // ID
                        return Integer.class;
                    case 6: // Montant
                        return Double.class;
                    default: // Toutes les autres colonnes sont des String
                        return String.class;
                }
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
        
        // Ajouter un listener pour le tri au clic sur l'en-tête
        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = table.columnAtPoint(evt.getPoint());
                
                // Si on clique sur la colonne "Statut" (colonne 7)
                if (column == 7) {
                    // Vérifier si c'est la même colonne que la dernière fois
                    if (lastSortedColumn == column) {
                        // Inverser l'ordre de tri
                        sortAscending = !sortAscending;
                    } else {
                        // Nouvelle colonne, tri ascendant par défaut
                        sortAscending = true;
                    }
                    
                    // Trier les données par statut
                    sortByStatus();
                    lastSortedColumn = column;
                    
                    // Mettre à jour l'en-tête pour montrer l'ordre de tri
                    updateHeaderSortIndicator();
                } else {
                    // Pour les autres colonnes, réinitialiser l'indicateur de tri
                    lastSortedColumn = -1;
                    updateHeaderSortIndicator();
                }
            }
        });
        
        // AJOUT DU RENDERER POUR L'ID (ALIGNÉ À GAUCHE) - CORRECTION ICI
        // Ce renderer doit être placé APRÈS la création de la table et AVANT les autres renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(LEFT); // Alignement à gauche
                return c;
            }
        });
        
        // Renderer pour le statut
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String statut = value.toString();
                    if (statut.equals("En cours")) {
                        c.setBackground(new Color(255, 224, 178)); // Orange clair
                        setForeground(new Color(245, 124, 0));
                    } else if (statut.equals("Terminé")) {
                        c.setBackground(new Color(200, 230, 201)); // Vert clair
                        setForeground(new Color(27, 94, 32));
                    }
                    setHorizontalAlignment(CENTER);
                }
                return c;
            }
        });
        
        // Renderer pour le montant
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    setHorizontalAlignment(RIGHT);
                    try {
                        double montant = Double.parseDouble(value.toString());
                        setText(String.format("%.2f DH", montant));
                    } catch (NumberFormatException e) {
                        setText("0.00 DH");
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private void updateHeaderSortIndicator() {
        JTableHeader header = table.getTableHeader();
        
        // Réinitialiser tous les en-têtes
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            String title = column.getHeaderValue().toString();
            
            // Supprimer les indicateurs précédents
            if (title.endsWith(" ▲") || title.endsWith(" ▼")) {
                title = title.substring(0, title.length() - 2);
            }
            
            column.setHeaderValue(title);
        }
        
        // Ajouter l'indicateur de tri à la colonne "Statut" si elle a été triée
        if (lastSortedColumn == 7) {
            TableColumn column = table.getColumnModel().getColumn(7);
            String title = column.getHeaderValue().toString();
            
            // Supprimer les indicateurs précédents
            if (title.endsWith(" ▲") || title.endsWith(" ▼")) {
                title = title.substring(0, title.length() - 2);
            }
            
            // Ajouter le nouvel indicateur
            title += sortAscending ? " ▲" : " ▼";
            column.setHeaderValue(title);
        }
        
        // Redessiner l'en-tête
        header.repaint();
    }
    
    private void sortByStatus() {
        // Récupérer toutes les lignes de la table
        int rowCount = tableModel.getRowCount();
        Object[][] rows = new Object[rowCount][tableModel.getColumnCount()];
        
        // Copier les données
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                rows[i][j] = tableModel.getValueAt(i, j);
            }
        }
        
        // Trier les données par statut
        java.util.Arrays.sort(rows, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] row1, Object[] row2) {
                String status1 = (String) row1[7]; // Colonne "Statut"
                String status2 = (String) row2[7];
                
                // Ordre de priorité des statuts
                int priority1 = getStatusPriority(status1);
                int priority2 = getStatusPriority(status2);
                
                // Comparer selon l'ordre de priorité
                if (priority1 != priority2) {
                    return sortAscending ? 
                        Integer.compare(priority1, priority2) : 
                        Integer.compare(priority2, priority1);
                } else {
                    // Si même statut, trier par ID
                    Integer id1 = (Integer) row1[0];
                    Integer id2 = (Integer) row2[0];
                    return sortAscending ? 
                        id1.compareTo(id2) : 
                        id2.compareTo(id1);
                }
            }
            
            private int getStatusPriority(String status) {
                switch (status) {
                    case "En cours": return 1;
                    case "Terminé": return 2;
                    default: return 3;
                }
            }
        });
        
        // Mettre à jour le modèle de table avec les données triées
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        
        tableModel.fireTableDataChanged();
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
        List<Ticket> tickets = ticketService.listerTousTickets();
        
        if (tickets != null) {
            for (Ticket t : tickets) {
                String immatriculation = t.getVehicule() != null ? 
                    t.getVehicule().getImmatriculation() : "Inconnu";
                String proprietaire = t.getVehicule() != null ? 
                    t.getVehicule().getProprietaire() : "Inconnu";
                String place = t.getPlace() != null ? 
                    t.getPlace().getNumero() : "Inconnu";
                String entree = t.getEntree() != null ? 
                    t.getEntree().format(dtf) : "";
                String sortie = t.getSortie() != null ? 
                    t.getSortie().format(dtf) : "";
                String statut = t.estEnCours() ? "En cours" : "Terminé";
                
                tableModel.addRow(new Object[]{
                    t.getId(),
                    immatriculation,
                    proprietaire,
                    place,
                    entree,
                    sortie,
                    t.getMontant(),
                    statut
                });
            }
        }
        
        tableModel.fireTableDataChanged();
        updateHeaderSortIndicator(); // Mettre à jour l'affichage des indicateurs de tri
    }
    
    private void showDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un ticket", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int ticketId = (int) tableModel.getValueAt(selectedRow, 0);
        Ticket ticket = ticketService.trouverTicketParId(ticketId);
        
        if (ticket == null) {
            JOptionPane.showMessageDialog(this, "Ticket non trouvé", 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Créer un dialogue avec les détails
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Détails du Ticket", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel titleLabel = new JLabel("Détails du Ticket #" + ticket.getId());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Détails
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setBackground(new Color(240, 240, 240));
        
        StringBuilder details = new StringBuilder();
        details.append("==========================================\n");
        details.append("             INFORMATIONS TICKET          \n");
        details.append("==========================================\n");
        details.append(" ID: ").append(String.format("%-35s", ticket.getId())).append("\n");
        
        if (ticket.getVehicule() != null) {
            details.append(" Véhicule: ").append(String.format("%-29s", ticket.getVehicule().getImmatriculation())).append("\n");
            details.append(" Propriétaire: ").append(String.format("%-25s", ticket.getVehicule().getProprietaire())).append("\n");
        }
        
        if (ticket.getPlace() != null) {
            details.append(" Place: ").append(String.format("%-32s", ticket.getPlace().getNumero())).append("\n");
        }
        
        if (ticket.getEntree() != null) {
            details.append(" Entrée: ").append(String.format("%-31s", ticket.getEntree().format(dtf))).append("\n");
        }
        
        if (ticket.getSortie() != null) {
            details.append(" Sortie: ").append(String.format("%-31s", ticket.getSortie().format(dtf))).append("\n");
            details.append(" Durée: ").append(String.format("%-32s", ticketService.obtenirDureeStationnement(ticket))).append("\n");
        }
        
        details.append(" Montant: ").append(String.format("%-30s", String.format("%.2f DH", ticket.getMontant()))).append("\n");
        details.append(" Statut: ").append(String.format("%-31s", ticket.estEnCours() ? "EN COURS" : "TERMINÉ")).append("\n");
        
        // Informations additionnelles
        if (ticket.getVehicule() != null) {
            // Vérifier si c'est un abonné
            AbonneService abonneService = new AbonneService();
            boolean estAbonne = abonneService.estVehiculeAbonne(ticket.getVehicule().getImmatriculation());
            if (estAbonne) {
                details.append("\n  Ce véhicule est abonné (gratuit)\n");
            }
        }
        
        detailsArea.setText(details.toString());
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeBtn = createButton("Fermer", Color.LIGHT_GRAY);
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(closeBtn);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}