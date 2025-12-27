package com.gestion.parking.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import com.gestion.parking.service.PlaceService;

public class MainFrame extends JFrame {
    private JDesktopPane desktopPane;
    private JPanel navBar;
    
    // Couleurs jaune/gris
    private static final Color PRIMARY_COLOR = new Color(255, 193, 7); // Jaune doré
    private static final Color SECONDARY_COLOR = new Color(245, 245, 245); // Gris clair (remplace le noir)
    private static final Color ACCENT_COLOR = new Color(255, 221, 87); // Jaune clair
    private static final Color WARNING_COLOR = new Color(255, 87, 51); // Orange/rouge
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250); // Gris très clair
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33); // Noir pour texte
    private static final Color TEXT_SECONDARY = new Color(100, 100, 100); // Gris pour texte secondaire
    
    // Références aux fenêtres
    private DashboardFrame dashboardFrame;
    private PlaceService placeService;
    
    public MainFrame() {
        setTitle("Système de Gestion de Parking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Lance l'application agrandie
        
        placeService = new PlaceService();
        
        initUI();
    }
    
    private void initUI() {
        // Configuration de la fenêtre principale
        setLayout(new BorderLayout());
        
        // Panel principal
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(BACKGROUND_COLOR);
        add(desktopPane, BorderLayout.CENTER);
        
        // Créer la barre de navigation
        createNavBar();
        
        // Ouvrir directement le tableau de bord au démarrage
        SwingUtilities.invokeLater(() -> openDashboardFrame());
    }
    
    private void createNavBar() {
        navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        navBar.setBackground(SECONDARY_COLOR); // Gris clair
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, PRIMARY_COLOR));
        navBar.setPreferredSize(new Dimension(getWidth(), 70));
        
        // Titre
        JLabel titleLabel = new JLabel("GESTION PARKING");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        navBar.add(titleLabel);
        
        // Espacement après le titre
        navBar.add(Box.createHorizontalStrut(40));
        
        // Bouton Tableau de Bord
        JButton btnDashboard = createStyledButton("TABLEAU DE BORD");
        btnDashboard.addActionListener(e -> openDashboardFrame());
        navBar.add(btnDashboard);
        
        // Espacement entre boutons
        navBar.add(Box.createHorizontalStrut(15));
        
        // Bouton Gestion
        JButton btnGestion = createStyledButton("GESTION");
        JPopupMenu gestionMenu = new JPopupMenu();
        stylePopupMenu(gestionMenu);
        
        JMenuItem menuPlaces = createStyledMenuItem("Gestion des Places");
        JMenuItem menuVehicules = createStyledMenuItem("Gestion des Véhicules");
        JMenuItem menuAbonnes = createStyledMenuItem("Gestion des Abonnés");
        JMenuItem menuTickets = createStyledMenuItem("Gestion des Tickets");
        
        menuPlaces.addActionListener(e -> openGestionPlaces());
        menuVehicules.addActionListener(e -> openGestionVehicules());
        menuAbonnes.addActionListener(e -> openGestionAbonnes());
        menuTickets.addActionListener(e -> openGestionTickets());
        
        gestionMenu.add(menuPlaces);
        gestionMenu.add(menuVehicules);
        gestionMenu.add(menuAbonnes);
        gestionMenu.add(menuTickets);
        
        btnGestion.addActionListener(e -> {
            gestionMenu.show(btnGestion, 0, btnGestion.getHeight());
        });
        
        navBar.add(btnGestion);
        navBar.add(Box.createHorizontalStrut(15));
        
        // Bouton Entrée/Sortie
        JButton btnOperations = createStyledButton("ENTRÉE/SORTIE");
        JPopupMenu operationsMenu = new JPopupMenu();
        stylePopupMenu(operationsMenu);
        
        JMenuItem menuEntree = createStyledMenuItem("Enregistrer Entrée");
        JMenuItem menuSortie = createStyledMenuItem("Enregistrer Sortie");
        
        menuEntree.addActionListener(e -> openEntreeDialog());
        menuSortie.addActionListener(e -> openSortieDialog());
        
        operationsMenu.add(menuEntree);
        operationsMenu.add(menuSortie);
        
        btnOperations.addActionListener(e -> {
            operationsMenu.show(btnOperations, 0, btnOperations.getHeight());
        });
        
        navBar.add(btnOperations);
        navBar.add(Box.createHorizontalStrut(15));
        
        
        
        // Espace flexible à droite pour pousser tout vers la gauche
        navBar.add(Box.createHorizontalGlue());
        
        add(navBar, BorderLayout.NORTH);
    }
    
    private void stylePopupMenu(JPopupMenu menu) {
        menu.setBackground(SECONDARY_COLOR); // Gris clair
        menu.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SECONDARY_COLOR); // Gris clair
        button.setForeground(PRIMARY_COLOR); // Jaune
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(SECONDARY_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
                button.setForeground(PRIMARY_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
        });
        
        return button;
    }
    
    private JMenuItem createStyledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(SECONDARY_COLOR); // Gris clair
        item.setForeground(PRIMARY_COLOR); // Jaune
        item.setFont(new Font("Segoe UI", Font.BOLD, 12));
        item.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                item.setBackground(PRIMARY_COLOR);
                item.setForeground(SECONDARY_COLOR);
            }
            public void mouseExited(MouseEvent evt) {
                item.setBackground(SECONDARY_COLOR);
                item.setForeground(PRIMARY_COLOR);
            }
        });
        
        return item;
    }
    
    private void openDashboardFrame() {
        // Fermer toutes les fenêtres ouvertes sauf le dashboard
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames) {
            if (frame != dashboardFrame) {
                frame.dispose();
            }
        }
        
        if (dashboardFrame != null && !dashboardFrame.isClosed()) {
            try {
                dashboardFrame.setSelected(true);
                dashboardFrame.moveToFront();
                return;
            } catch (Exception e) {
                dashboardFrame = null;
            }
        }
        
        dashboardFrame = new DashboardFrame();
        desktopPane.add(dashboardFrame);
        centerFrame(dashboardFrame);
        dashboardFrame.setVisible(true);
        
        try {
            dashboardFrame.setSelected(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    private void openGestionPlaces() {
        // Fermer toutes les fenêtres ouvertes
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames) {
            frame.dispose();
        }
        
        PlacesFrame placesFrame = new PlacesFrame();
        desktopPane.add(placesFrame);
        centerFrame(placesFrame);
        placesFrame.setVisible(true);
    }
    
    private void openGestionVehicules() {
        // Fermer toutes les fenêtres ouvertes
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames) {
            frame.dispose();
        }
        
        VehiculesFrame vehiculesFrame = new VehiculesFrame();
        desktopPane.add(vehiculesFrame);
        centerFrame(vehiculesFrame);
        vehiculesFrame.setVisible(true);
    }
    
    private void openGestionAbonnes() {
        // Fermer toutes les fenêtres ouvertes
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames) {
            frame.dispose();
        }
        
        AbonnesFrame abonnesFrame = new AbonnesFrame();
        desktopPane.add(abonnesFrame);
        centerFrame(abonnesFrame);
        abonnesFrame.setVisible(true);
    }
    
    private void openGestionTickets() {
        // Fermer toutes les fenêtres ouvertes
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames) {
            frame.dispose();
        }
        
        TicketsFrame ticketsFrame = new TicketsFrame();
        desktopPane.add(ticketsFrame);
        centerFrame(ticketsFrame);
        ticketsFrame.setVisible(true);
    }
    
    private void openEntreeDialog() {
        EntreeDialog dialog = new EntreeDialog(this);
        dialog.setVisible(true);
    }
    
    private void openSortieDialog() {
        SortieDialog dialog = new SortieDialog(this);
        dialog.setVisible(true);
    }
    
    private void centerFrame(JInternalFrame frame) {
        SwingUtilities.invokeLater(() -> {
            Dimension desktopSize = desktopPane.getSize();
            Dimension frameSize = frame.getSize();
            
            if (desktopSize.width <= 0 || desktopSize.height <= 0) {
                desktopSize = desktopPane.getPreferredSize();
            }
            
            int x = (desktopSize.width - frameSize.width) / 2;
            int y = (desktopSize.height - frameSize.height) / 2;
            
            x = Math.max(0, Math.min(x, desktopSize.width - frameSize.width));
            y = Math.max(0, Math.min(y, desktopSize.height - frameSize.height));
            
            frame.setLocation(x, y);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Erreur lors du démarrage de l'application: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}