package com.gestion.parking.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import com.gestion.parking.service.*;
import com.gestion.parking.model.*;

public class DashboardFrame extends JInternalFrame {
    private StatistiqueService statsService;
    private PlaceService placeService;
    private TicketService ticketService;
    private AbonneService abonneService;
    
    // Couleurs jaune/noir
    private static final Color COLOR_PRIMARY = new Color(255, 193, 7); // Jaune doré
    private static final Color COLOR_SECONDARY = new Color(33, 33, 33); // Noir
    private static final Color COLOR_BACKGROUND = new Color(250, 250, 250); // Gris très clair
    private static final Color COLOR_TEXT_DARK = new Color(33, 33, 33); // Noir pour texte
    private static final Color COLOR_TEXT_LIGHT = new Color(100, 100, 100); // Gris pour texte secondaire
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_ACCENT = new Color(46, 204, 113); // Vert pour succès
    private static final Color COLOR_WARNING = new Color(231, 76, 60); // Rouge pour alerte
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public DashboardFrame() {
        super("Tableau de Bord", true, true, true, true);
        // Taille maximisée au démarrage
        setSize(Toolkit.getDefaultToolkit().getScreenSize().width - 100, 
                Toolkit.getDefaultToolkit().getScreenSize().height - 100);
        
        statsService = new StatistiqueService();
        placeService = new PlaceService();
        ticketService = new TicketService();
        abonneService = new AbonneService();
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, COLOR_PRIMARY),
            BorderFactory.createEmptyBorder(0, 0, 20, 0)
        ));
        
        JLabel titleLabel = new JLabel("TABLEAU DE BORD DU PARKING");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(COLOR_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(COLOR_BACKGROUND);
        
        JLabel dateLabel = new JLabel(LocalDate.now().format(DATE_FORMATTER));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(COLOR_TEXT_LIGHT);
        
        JButton refreshBtn = createButton("ACTUALISER", COLOR_PRIMARY, COLOR_SECONDARY);
        refreshBtn.addActionListener(e -> refreshDashboard());
        
        rightPanel.add(dateLabel);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(refreshBtn);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Contenu principal
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        contentPanel.setBackground(COLOR_BACKGROUND);
        
        // Première ligne: Statistiques
        contentPanel.add(createStatsPanel());
        
        // Deuxième ligne: Tableau d'activité
        contentPanel.add(createActivityPanel());
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 2),
            BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
                btn.setForeground(COLOR_SECONDARY);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker().darker(), 2),
                    BorderFactory.createEmptyBorder(12, 25, 12, 25)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
                btn.setForeground(fg);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 2),
                    BorderFactory.createEmptyBorder(12, 25, 12, 25)
                ));
            }
        });
        
        return btn;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 15, 0));
        panel.setBackground(COLOR_BACKGROUND);
        
        // Récupérer les statistiques
        double caJour = calculerChiffreAffairesDuJour();
        int totalPlaces = statsService.obtenirNombreTotalPlaces();
        int placesOccupees = statsService.obtenirNombrePlacesOccupees();
        int placesDispo = statsService.obtenirNombrePlacesDisponibles();
        double tauxOccupation = statsService.obtenirTauxOccupation();
        int abonnesValides = statsService.obtenirNombreAbonnesValides();
        
        // Couleurs jaunes nuancées pour les cartes
        Color caColor = new Color(255, 193, 7); // Jaune doré principal
        Color totalColor = new Color(255, 213, 79); // Jaune clair
        Color occupeeColor = new Color(255, 171, 0); // Jaune orange
        Color libreColor = new Color(200, 230, 100); // Jaune vert
        Color tauxColor = new Color(255, 221, 87); // Jaune moyen
        Color abonneColor = new Color(255, 204, 0); // Jaune doré foncé
        
        panel.add(createStatCard("CHIFFRE D'AFFAIRE", 
            String.format("%.0f DH", caJour), "Aujourd'hui", caColor));
        
        panel.add(createStatCard("PLACES TOTALES", 
            String.valueOf(totalPlaces), "Capacité max", totalColor));
        
        panel.add(createStatCard("PLACES OCCUPÉES", 
            String.valueOf(placesOccupees), "En cours", occupeeColor));
        
        panel.add(createStatCard("PLACES LIBRES", 
            String.valueOf(placesDispo), "Disponibles", libreColor));
        
        panel.add(createStatCard("TAUX D'OCCUPATION", 
            String.format("%.1f%%", tauxOccupation), tauxColor));
        
        panel.add(createStatCard("ABONNÉS ACTIFS", 
            String.valueOf(abonnesValides), abonneColor));
        
        return panel;
    }
    
    // Méthode pour calculer le chiffre d'affaires du jour (tickets + abonnements + renouvellements)
    private double calculerChiffreAffairesDuJour() {
        double caTotal = 0.0;
        LocalDate aujourdhui = LocalDate.now();
        
        // 1. Chiffre d'affaires des tickets (parking journalier)
        try {
            caTotal = statsService.obtenirChiffreAffairesDuJour();
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul CA tickets: " + e.getMessage());
        }
        
        // 2. Ajouter le montant des abonnements créés aujourd'hui
        List<Abonne> abonnes = abonneService.listerTousAbonnes();
        
        if (abonnes != null) {
            for (Abonne abonne : abonnes) {
                if (abonne.getDateDebut() != null && abonne.getDateDebut().equals(aujourdhui)) {
                    // Si l'abonnement a été créé aujourd'hui, ajouter son montant total
                    caTotal += abonne.getMontantTotal();
                }
                
                // 3. Ajouter le montant des renouvellements effectués aujourd'hui
                // Vérifier si l'abonnement a été renouvelé aujourd'hui
                // Pour cela, on vérifie si l'abonnement existait déjà et a eu une nouvelle date de fin aujourd'hui
                // Note: Cette logique nécessite un historique. Pour simplifier, on peut:
                // 1. Vérifier si l'abonnement a été modifié aujourd'hui et que la date de fin est après aujourd'hui
                // OU
                // 2. Ajouter un champ "dateRenouvellement" dans la classe Abonne (meilleure solution)
                
                // Solution simplifiée: si la date de fin est aujourd'hui ou plus tard et dateDebut n'est pas aujourd'hui
                if (abonne.getDateDebut() != null && abonne.getDateFin() != null) {
                    // Si l'abonnement a commencé avant aujourd'hui mais a une nouvelle période aujourd'hui
                    // On considère ça comme un renouvellement
                    if (!abonne.getDateDebut().equals(aujourdhui) && 
                        abonne.getDateFin().isAfter(LocalDate.now().minusDays(1))) {
                        
                        // Calculer le montant payé pour la période de renouvellement
                        // Pour simplifier, on prend le montant total de l'abonnement
                        // Mais idéalement, on devrait calculer uniquement la période renouvelée
                        caTotal += abonne.getMontantTotal();
                    }
                }
            }
        }
        
        return caTotal;
    }
    
    private JPanel createActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY, 2, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Titre du panel d'activité
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(COLOR_CARD);
        
        JLabel titleLabel = new JLabel("ACTIVITÉ RÉCENTE (AUJOURD'HUI)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(COLOR_PRIMARY);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Créer le modèle de table
        String[] columns = {"Heure", "Action", "Véhicule", "Détails", "Montant"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(COLOR_CARD);
        table.setForeground(COLOR_TEXT_DARK);
        table.setGridColor(new Color(240, 240, 240));
        table.setShowGrid(true);
        
        // Style de l'en-tête de la table
        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(COLOR_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setReorderingAllowed(false);
        
        // Créer un renderer pour centrer tout le texte
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Centrer tout le texte
                setHorizontalAlignment(CENTER);
                
                // Alternance des couleurs de ligne
                if (row % 2 == 0 && !isSelected) {
                    c.setBackground(new Color(250, 250, 250));
                } else if (!isSelected) {
                    c.setBackground(COLOR_CARD);
                }
                
                // Couleur du texte en noir
                c.setForeground(COLOR_TEXT_DARK);
                
                return c;
            }
        };
        
        // Appliquer le renderer à toutes les colonnes
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Renderer spécial pour les colonnes Action et Montant avec couleurs
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(CENTER);
                c.setForeground(COLOR_TEXT_DARK);
                
                if (value != null) {
                    String action = value.toString();
                    if (action.equals("ENTREE")) {
                        c.setBackground(new Color(220, 245, 220));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else if (action.equals("SORTIE")) {
                        c.setBackground(new Color(255, 230, 230));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else if (action.equals("ABONNEMENT")) {
                        c.setBackground(new Color(220, 235, 255));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else if (action.equals("RENOUVELLEMENT")) {
                        c.setBackground(new Color(255, 235, 220));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setBackground(COLOR_CARD);
                    }
                }
                
                if (row % 2 == 0 && !isSelected && !c.getBackground().equals(new Color(220, 245, 220)) 
                    && !c.getBackground().equals(new Color(255, 230, 230))
                    && !c.getBackground().equals(new Color(220, 235, 255))
                    && !c.getBackground().equals(new Color(255, 235, 220))) {
                    c.setBackground(new Color(250, 250, 250));
                }
                
                return c;
            }
        });
        
        // Renderer pour la colonne Montant
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(CENTER);
                
                if (value != null && !value.toString().equals("--")) {
                    // Si c'est un montant, le mettre en vert
                    setForeground(new Color(0, 150, 0));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setForeground(COLOR_TEXT_DARK);
                }
                
                return c;
            }
        });
        
        // Récupérer et trier les activités d'aujourd'hui
        List<ActivityItem> activities = getTodaysActivities();
        
        // Trier par heure décroissante
        activities.sort((a1, a2) -> a2.time.compareTo(a1.time));
        
        // Ajouter les activités à la table
        for (ActivityItem activity : activities) {
            model.addRow(new Object[]{
                activity.time.format(TIME_FORMATTER),
                activity.action,
                activity.vehicle,
                activity.details,
                activity.montant
            });
        }
        
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"--:--", "Aucune activité", "aujourd'hui", "--", "--"});
        }
        
        // Définir des largeurs de colonnes optimales
        table.getColumnModel().getColumn(0).setPreferredWidth(100);  // Heure
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Action
        table.getColumnModel().getColumn(2).setPreferredWidth(180); // Véhicule
        table.getColumnModel().getColumn(3).setPreferredWidth(200); // Détails
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Montant
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(COLOR_CARD);
        
        // Ajouter un footer avec le nombre d'activités
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(COLOR_CARD);
        
        JLabel countLabel = new JLabel(activities.size() + " activités enregistrées aujourd'hui");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(COLOR_TEXT_LIGHT);
        footerPanel.add(countLabel);
        
        panel.add(footerPanel, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Classe interne pour gérer les activités
    private class ActivityItem {
        LocalDateTime time;
        String action;
        String vehicle;
        String details;
        String montant;
        
        ActivityItem(LocalDateTime time, String action, String vehicle, String details, String montant) {
            this.time = time;
            this.action = action;
            this.vehicle = vehicle;
            this.details = details;
            this.montant = montant;
        }
    }
    
    // Méthode pour récupérer les activités d'aujourd'hui (tickets + abonnements + renouvellements)
    private List<ActivityItem> getTodaysActivities() {
        List<ActivityItem> activities = new ArrayList<>();
        LocalDate aujourdhui = LocalDate.now();
        
        // 1. Récupérer les activités de parking (tickets)
        List<Ticket> tickets = ticketService.listerTousTickets();
        
        if (tickets != null) {
            for (Ticket ticket : tickets) {
                // Ajouter l'entrée si elle est d'aujourd'hui
                if (ticket.getEntree() != null && 
                    ticket.getEntree().toLocalDate().equals(aujourdhui)) {
                    
                    String immat = ticket.getVehicule() != null ? 
                        ticket.getVehicule().getImmatriculation() : "Inconnu";
                    String place = ticket.getPlace() != null ? 
                        ticket.getPlace().getNumero() : "Inconnu";
                    
                    activities.add(new ActivityItem(
                        ticket.getEntree(),
                        "ENTREE",
                        immat,
                        "Place: " + place,
                        "--"
                    ));
                }
                
                // Ajouter la sortie si elle est d'aujourd'hui
                if (ticket.getSortie() != null && 
                    ticket.getSortie().toLocalDate().equals(aujourdhui)) {
                    
                    String immat = ticket.getVehicule() != null ? 
                        ticket.getVehicule().getImmatriculation() : "Inconnu";
                    String place = ticket.getPlace() != null ? 
                        ticket.getPlace().getNumero() : "-";
                    double montantTicket = ticket.getMontant();
                    
                    activities.add(new ActivityItem(
                        ticket.getSortie(),
                        "SORTIE",
                        immat,
                        "Place: " + place + " | Durée: " + ticketService.obtenirDureeStationnement(ticket),
                        String.format("%.0f DH", montantTicket)
                    ));
                }
            }
        }
        
        // 2. Récupérer les créations d'abonnements d'aujourd'hui
        List<Abonne> abonnes = abonneService.listerTousAbonnes();
        
        if (abonnes != null) {
            for (Abonne abonne : abonnes) {
                // Vérifier si l'abonnement a été créé aujourd'hui
                if (abonne.getDateDebut() != null && abonne.getDateDebut().equals(aujourdhui)) {
                    String immat = abonne.getImmatriculation();
                    double montantAbonne = abonne.getMontantTotal();
                    int dureeMois = abonne.getDureeMois();
                    
                    String dayName = abonne.getDateDebut().getDayOfWeek()
                    	    .getDisplayName(TextStyle.FULL, Locale.FRENCH);

                    	activities.add(new ActivityItem(
                    	    abonne.getDateDebut().atStartOfDay(), // Timestamp à minuit
                    	    "ABONNEMENT",
                    	    immat,
                    	    "Nouvel abonnement " + dureeMois + " mois souscrit un " + dayName + 
                    	    " (" + abonne.getDateDebut().format(DATE_FORMATTER) + " → " + 
                    	    abonne.getDateFin().format(DATE_FORMATTER) + ")",
                    	    String.format("%.0f DH", montantAbonne)
                    	));
                }
                
                // 3. Détecter le
                if (abonne.getDateDebut() != null && abonne.getDateFin() != null) {
                    // Si l'abonnement a commencé avant aujourd'hui mais finit aujourd'hui ou plus tard
                    if (!abonne.getDateDebut().equals(aujourdhui) && 
                        abonne.getDateFin().isAfter(LocalDate.now().minusDays(1))) {
                        
                        if (abonne.getDateFin().isAfter(LocalDate.now().minusDays(7))) {
                            String immat = abonne.getImmatriculation();
                            double montantRenouvellement = abonne.getMontantTotal();
                            int dureeMois = abonne.getDureeMois();
                            
                            // Pour simuler l'heure, on utilisera l'heure actuelle
                            LocalDateTime time = LocalDateTime.now();
                            
                            activities.add(new ActivityItem(
                                time,
                                "RENOUVELLEMENT",
                                immat,
                                "Renouvellement " + dureeMois + " mois (" + 
                                abonne.getDateDebut().format(DATE_FORMATTER) + " → " + 
                                abonne.getDateFin().format(DATE_FORMATTER) + ")",
                                String.format("%.0f DH", montantRenouvellement)
                            ));
                        }
                    }
                }
            }
        }
        
        return activities;
    }
    
    private JPanel createStatCard(String title, String value, String subtitle, Color accentColor) {
        return createStatCard(title, value, accentColor);
    }
    
    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
            )
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(COLOR_TEXT_LIGHT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(COLOR_TEXT_DARK);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(valueLabel);
        
        return card;
    }
    
    private void refreshDashboard() {
        Container parent = this.getParent();
        if (parent instanceof JDesktopPane) {
            JDesktopPane desktopPane = (JDesktopPane) parent;
            
            // Fermer le dashboard actuel
            this.dispose();
            
            // Créer un nouveau dashboard
            DashboardFrame newDashboard = new DashboardFrame();
            desktopPane.add(newDashboard);
            
            // Centrer et afficher
            Dimension desktopSize = desktopPane.getSize();
            Dimension frameSize = newDashboard.getSize();
            newDashboard.setLocation(
                (desktopSize.width - frameSize.width) / 2,
                (desktopSize.height - frameSize.height) / 2
            );
            newDashboard.setVisible(true);
            try {
                newDashboard.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
                e.printStackTrace();
            }
        }
    }
}