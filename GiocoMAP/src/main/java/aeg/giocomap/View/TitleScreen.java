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
    private JButton btn_record;
    private BufferedImage sfondo;
    
    // Costruttore che crea la griglia divisa in 5 zone
    // con questa griglia evito di dover dare la corretta posizione in pixel dei bottoni
    public TitleScreen(){
        // Caricamento dello sfondo
        try{ //Image.IO vuole perforza try-catch
            sfondo = ImageIO.read(getClass().getResourceAsStream("/sprites/StrumentiGrafici/BackgroundTitleScreen.png"));
        }
        catch(IOException e){
            System.err.println("Impossibile caricare l'immagine: "+e.getMessage());
        }
        
        // Layout dei bottoni in sovraposizione
        setLayout(new GridBagLayout()); // GridBagLayout sovrappone elemennti liberamente nello spazio.
        
        // Caricamento dei bottoni
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // Y li mette in verticale, uno sotto l'altro
        
        btn_nuova_partita = new JButton("Nuova Partita");
        btn_carica_partita = new JButton("Carica Partita");
        btn_record = new JButton("Statistiche");
        
        // Estetica dei btn
        btn_nuova_partita.setBackground(Color.WHITE);
        btn_carica_partita.setBackground(Color.WHITE);
        btn_nuova_partita.setForeground(Color.BLACK);
        btn_carica_partita.setForeground(Color.BLACK);
        btn_record.setBackground(Color.WHITE);
        btn_record.setForeground(Color.BLACK);
        
        // font dei bottoni
        Font fontBottoni = new Font("Arial", Font.BOLD, 18);
        btn_nuova_partita.setFont(fontBottoni);
        btn_carica_partita.setFont(fontBottoni);
        btn_record.setFont(fontBottoni);
        
        // allineamento
        btn_nuova_partita.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn_carica_partita.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn_record.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // dimensione dei bottoni
        Dimension dimB = new Dimension(200,40); //200w e 40h
        btn_nuova_partita.setPreferredSize(dimB);
        btn_nuova_partita.setMaximumSize(dimB);
        btn_carica_partita.setPreferredSize(dimB);
        btn_carica_partita.setMaximumSize(dimB);
        btn_record.setPreferredSize(dimB);
        btn_record.setMaximumSize(dimB);
        
        // Aggiungo i bottoni al panel
        buttonPanel.add(btn_nuova_partita);
        buttonPanel.add(Box.createRigidArea(new Dimension(0,20))); // Spazio vuoto tra i btn e Box permette di impilare i bottoni in verticale (uno sotto l'altro) come in una colonna.
        buttonPanel.add(btn_carica_partita);
        buttonPanel.add(Box.createRigidArea(new Dimension(0,20)));
        buttonPanel.add(btn_record);
        
        // Istruzioni per il posizionamento nello schermo
        GridBagConstraints gb = new GridBagConstraints();
        gb.gridx = 0; // L'oggetto da inserire parte dalla colonna 1(0) della griglia
        gb.gridy = 0; // L'oggetto ... parte dalla riga 1(0) della griglia
        
        /*Con Grid se nella griglia c'è un solo elemento
         la "cella 0,0" diventa enorme e occupa automaticamente tutto lo spazio disponibile.
        */
        
        gb.insets = new Insets(80,0,0,0); // 200 di spazio dal TOP per metterlo sotto il title screen
        
        // Aggiungo le istruzioni di gb al panel dei bottoni
        add(buttonPanel,gb);
    }
    
    // Metodi Listener per chiamare la logica che eseguono i bottoni
    public void addNPListener(ActionListener listener){
        btn_nuova_partita.addActionListener(listener);
    }
    public void addCPListener(ActionListener listener){
        btn_carica_partita.addActionListener(listener);
    }
    
    public void addRecordListener(ActionListener listener){
        btn_record.addActionListener(listener);
    }
    
    //metodo per disegnare lo sfondo nel panel
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g); //puliamo lo sfondo attuale
        // g.drawImage(Cosa_disegnare, x, y, larghezza, altezza, osservatore)
        if(sfondo!=null) g.drawImage(sfondo,0,0,getWidth(),getHeight(),this);
    }
}
