package aeg.giocomap.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import aeg.giocomap.Util.CursorUtil;

public class TitleScreen extends JPanel {

    private JButton btn_nuova_partita;
    private JButton btn_carica_partita;
    private JButton btn_record;
    private JButton btn_connetti;    // ← aggiunto
    private BufferedImage sfondo;

    public TitleScreen() {
        try {
            sfondo = ImageIO.read(getClass().getResourceAsStream(
                "/sprites/StrumentiGrafici/BackgroundTitleScreen.png"));
        } catch (IOException e) {
            System.err.println("Impossibile caricare l'immagine: " + e.getMessage());
        }

        setLayout(new GridBagLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        btn_nuova_partita = new JButton("Nuova Partita");
        btn_carica_partita = new JButton("Carica Partita");
        btn_record = new JButton("Statistiche");
        btn_connetti = new JButton("Partecipa alla Chat");    // ← aggiunto

        // estetica
        Font fontBottoni = new Font("Arial", Font.BOLD, 18);
        Dimension dimB = new Dimension(200, 40);

        JButton[] bottoni = {btn_nuova_partita, btn_carica_partita, btn_record, btn_connetti};
        for (JButton btn : bottoni) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setFont(fontBottoni);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setPreferredSize(dimB);
            btn.setMaximumSize(dimB);
        }

        CursorUtil.setHandCursor(btn_nuova_partita, btn_carica_partita, btn_record, btn_connetti);

        // aggiungo i bottoni
        buttonPanel.add(btn_nuova_partita);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(btn_carica_partita);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(btn_record);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(btn_connetti);    // ← aggiunto

        GridBagConstraints gb = new GridBagConstraints();
        gb.gridx = 0;
        gb.gridy = 0;
        gb.insets = new Insets(80, 0, 0, 0);
        add(buttonPanel, gb);
    }

    public void addNPListener(ActionListener listener) {
        btn_nuova_partita.addActionListener(listener);
    }

    public void addCPListener(ActionListener listener) {
        btn_carica_partita.addActionListener(listener);
    }

    public void addRecordListener(ActionListener listener) {
        btn_record.addActionListener(listener);
    }

    public void addConnettiListener(ActionListener listener) {
        btn_connetti.addActionListener(listener);    // ← aggiunto
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (sfondo != null) g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
    }
}