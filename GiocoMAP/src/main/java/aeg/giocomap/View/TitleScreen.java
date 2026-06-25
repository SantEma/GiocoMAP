/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;

/**
 *
 * @author Andrea
 */

//estendo con JPanel per poter inserire i bottoni
public class TitleScreen extends JPanel {
    private JButton btn_nuova_partita;
    private JButton btn_carica_partita;
    private BufferedImage sfondo;
    
    // Costruttore che crea la griglia divisa in 5 zone
    // con questa griglia evito di dover dare la corretta posizione in pixel dei bottoni
    public TitleScreen(){
        setLayout(new BorderLayout());
        
        // Caricamento dei bottoni
        JPanel buttonPanel = new JPanel();
        //buttonPanel.SetOpaque(false);
        
        btn_nuova_partita = new JButton("Nuova Partita");
        btn_carica_partita = new JButton("Carica Partita");
        buttonPanel.add(btn_nuova_partita);
        buttonPanel.add(btn_carica_partita);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Caricamento dello sfondo
        try{ //Image.IO vuole perforza try-catch
            sfondo = ImageIO.read(getClass().getResourceAsStream("/sprites/StrumentiGrafici/BackgroundTitleScreen.png"));
        }
        catch(IOException e){
            System.err.println("Impossibile caricare l'immagine: "+e.getMessage());
        }
        
        
    }
    
    // Metodi Listener per chiamare la logica che eseguono i bottoni
    public void addNPListener(ActionListener listener){
        btn_nuova_partita.addActionListener(listener);
    }
    public void addCPListener(ActionListener listener){
        btn_carica_partita.addActionListener(listener);
    }
    
    //metodo per disegnare lo sfondo nel panel
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g); //puliamo lo sfondo attuale
        // g.drawImage(Cosa_disegnare, x, y, larghezza, altezza, osservatore)
        if(sfondo!=null) g.drawImage(sfondo,0,0,getWidth(),getHeight(),this);
    }
}
