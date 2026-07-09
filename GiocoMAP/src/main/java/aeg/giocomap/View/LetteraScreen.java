/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;
/**
 *
 * @author Andrea e Giulio
 */

public class LetteraScreen extends JPanel {
    private BufferedImage sfondo;
    private final String testo;
    private JButton btnAvanti;

    public LetteraScreen(List<String> righe, Runnable azioneAvanti) {
        // Unisce tutte le righe del JSON aggiungendo un a capo tra un paragrafo e l'altro
        this.testo = String.join("", righe);

        try {
            sfondo = ImageIO.read(getClass().getResourceAsStream("/sprites/StrumentiGrafici/LetteraIniziale.png"));
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }

        setLayout(null);

        // Bottone Avanti verrà chiamato per proseguire nella storia
        btnAvanti = new BottoneAvanti(e -> azioneAvanti.run());
        this.add(btnAvanti);

        // Mantiene il bottone in basso a destra
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                btnAvanti.setBounds(w - 180, h - 70, 150, 40);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        
        if (sfondo != null) {
           g.drawImage(sfondo, 0, 0, w, h, this);
        }
        
        // Font dinamico stile antico
        int fontSize = Math.max(12, w / 85);
        Font font = new Font("Garamond", Font.ITALIC | Font.BOLD, fontSize);
        g.setFont(font);
        g.setColor(new Color(60, 30, 10)); // Colore inchiostro

        // Disegna il testo partendo da coordinate precise sulla pergamena
        int testoX = (int)(w * 0.32);
        int testoY = (int)(h * 0.25);
        int testoW = (int)(w * 0.38);
        int testoH = (int)(h * 0.57);

        disegnaTesto(g, testo, testoX, testoY, testoW, testoH);
    }
    
    private void disegnaTesto(Graphics g, String testo, int x, int y, int maxW, int maxH) {
       FontMetrics fm = g.getFontMetrics();
       int lineHeight = fm.getHeight();
       int currentY = y + fm.getAscent();
       int maxY = y + maxH;

       String[] parole = testo.split(" ");
       StringBuilder rigaCorrente = new StringBuilder();

       for (String parola : parole) {
           if (parola.contains("\n")) {
                String[] parti = parola.split("\n", -1);
               for (int i = 0; i < parti.length; i++) {
                   String prova = rigaCorrente.length() > 0 ? rigaCorrente + " " + parti[i] : parti[i];

                   if (fm.stringWidth(prova) > maxW) {
                       if (currentY <= maxY) g.drawString(rigaCorrente.toString(), x, currentY);
                       currentY += lineHeight;
                       rigaCorrente = new StringBuilder(parti[i]);
                   } else {
                       rigaCorrente = new StringBuilder(prova);
                   }

                   if (i < parti.length - 1) {
                       if (currentY <= maxY) g.drawString(rigaCorrente.toString(), x, currentY);
                       currentY += lineHeight;
                       rigaCorrente = new StringBuilder();
                   }
               }
            } else {
                String prova = rigaCorrente.length() > 0 ? rigaCorrente + " " + parola : parola;
                if (fm.stringWidth(prova) > maxW) {
                    if (currentY <= maxY) g.drawString(rigaCorrente.toString(), x, currentY);
                    currentY += lineHeight;
                    rigaCorrente = new StringBuilder(parola);
                } else {
                    rigaCorrente = new StringBuilder(prova);
                }
            }
        }
        if (rigaCorrente.length() > 0 && currentY <= maxY) {
            g.drawString(rigaCorrente.toString(), x, currentY);
        }
    }
}
