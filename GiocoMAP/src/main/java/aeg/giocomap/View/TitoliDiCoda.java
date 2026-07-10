/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 *
 * @author murgo
 */


public class TitoliDiCoda extends JPanel {

    public TitoliDiCoda(List<String[]> records, int punteggioAttuale, String nomeGiocatore) {
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        JPanel contenuto = new JPanel();
        contenuto.setOpaque(false);
        contenuto.setLayout(new BoxLayout(contenuto, BoxLayout.Y_AXIS));

        JLabel titolo = new JLabel("Hall of Fame");
        titolo.setFont(new Font("Monospaced", Font.PLAIN, 28));
        titolo.setForeground(Color.WHITE);
        titolo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenuto.add(titolo);
        contenuto.add(Box.createRigidArea(new Dimension(0, 30)));

        if (!nomeGiocatore.isEmpty() && punteggioAttuale > 0) {
            JLabel punteggioLabel = new JLabel(
                nomeGiocatore + "  →  " + punteggioAttuale + " pts"
            );
            punteggioLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));
            punteggioLabel.setForeground(new Color(180, 180, 180));
            punteggioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenuto.add(punteggioLabel);
            contenuto.add(Box.createRigidArea(new Dimension(0, 30)));
        }

        if (records.isEmpty()) {
            JLabel nessuno = new JLabel("nessun record.");
            nessuno.setFont(new Font("Monospaced", Font.PLAIN, 16));
            nessuno.setForeground(new Color(120, 120, 120));
            nessuno.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenuto.add(nessuno);
        } else {
            for (int i = 0; i < records.size(); i++) {
                String[] r = records.get(i);
                String riga = (i + 1) + ".  " + r[0] + "  —  " + r[1] + " pts";
                JLabel label = new JLabel(riga);
                label.setFont(new Font("Monospaced", Font.PLAIN, 16));
                if (!nomeGiocatore.isEmpty() && r[0].equals(nomeGiocatore)) {
                    label.setForeground(Color.WHITE);
                } else {
                    label.setForeground(new Color(120, 120, 120));
                }
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                contenuto.add(label);
                contenuto.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        contenuto.add(Box.createRigidArea(new Dimension(0, 40)));

        JButton btn_indietro = new JButton("[ indietro ]");
        btn_indietro.setFont(new Font("Monospaced", Font.PLAIN, 14));
        btn_indietro.setBackground(Color.BLACK);
        btn_indietro.setForeground(new Color(120, 120, 120));
        btn_indietro.setBorder(BorderFactory.createEmptyBorder());
        btn_indietro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_indietro.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenuto.add(btn_indietro);

        GridBagConstraints gb = new GridBagConstraints();
        add(contenuto, gb);
    }

    public void addIndietroListener(ActionListener listener) {
        for (Component c : ((JPanel) getComponent(0)).getComponents()) {
            if (c instanceof JButton) {
                ((JButton) c).addActionListener(listener);
            }
        }
    }
}