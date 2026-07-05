/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 *
 * @author Andrea
 */
public class MappaPanel extends JPanel {
    private BufferedImage immagineMappa;
   
    public MappaPanel() {
        try{
            //Carico la mappa
            immagineMappa = ImageIO.read(getClass().getResourceAsStream("/sprites/StrumentiGrafici/MappaShambhala.png"));
        }
        catch(IOException | IllegalArgumentException e){
            System.err.println("Impossibile caricare Immagine non trovata "+ e.getMessage());
        }
        
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        
        // Istruzioni per far capire come usare la Mappa
        JLabel istruzioni = new JLabel("Premi M per aprire e chiudere la mappa",SwingConstants.CENTER);
        istruzioni.setFont(new Font("Arial",Font.BOLD,20));
        istruzioni.setForeground(Color.WHITE);
        
        //Testo posizionato sotto l'immagine
        istruzioni.setOpaque(true);
        istruzioni.setBackground(new Color(0,0,0,150));
        istruzioni.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        
        add(istruzioni,BorderLayout.SOUTH);
    }
    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(immagineMappa != null){
            // 1. Prendo le dimensioni (finestra) dell'immagine originale
            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = immagineMappa.getWidth();
            int imgH = immagineMappa.getHeight();
            
            // 2. Calcolo il fattore di scala per mantenere le proporzioni intatte
            double scaleX = (double) panelW / imgW;
            double scaleY = (double) panelH / imgH;
            double scale = Math.min(scaleX, scaleY); // Prendo il fattore più piccolo per farla entrare tutta
            
            // 3. Calcolo la grandezza finale scalata
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            
            // 4. Calcolo le coordinate X e Y per centrare l'immagine perfettamente
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;
            
            // 5. Disegno l'immagine con le nuove grandezze proporzionate e centrate
            g.drawImage(immagineMappa, x, y, drawW, drawH, this);
        }
        else{
            g.setColor(Color.WHITE);
            g.drawString("Immagine Mappa non trovata!", 50, 50);
        }
    } 
}
