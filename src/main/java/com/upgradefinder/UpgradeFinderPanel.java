package com.upgradefinder;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.PluginPanel;

public class UpgradeFinderPanel extends PluginPanel {

    public UpgradeFinderPanel() {
        super();
        setLayout(new BorderLayout());
        showWelcomeMessage();
    }

    private void showWelcomeMessage() {
        removeAll();
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        welcomePanel.setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Right-click an item and select 'Check Upgrades'");
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

        add(welcomePanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void showItemStats(String upgradeName, String slashBonus, String requirements) {
        removeAll();
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        resultsPanel.setLayout(new BorderLayout(0, 10));

        // Title
        JLabel titleLabel = new JLabel("Upgrade: " + upgradeName);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Stats Panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BorderLayout());
        statsPanel.add(new JLabel("Slash Bonus: +" + slashBonus), BorderLayout.NORTH);
        statsPanel.add(new JLabel("Requires: " + requirements), BorderLayout.SOUTH);

        resultsPanel.add(titleLabel, BorderLayout.NORTH);
        resultsPanel.add(statsPanel, BorderLayout.CENTER);

        add(resultsPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}