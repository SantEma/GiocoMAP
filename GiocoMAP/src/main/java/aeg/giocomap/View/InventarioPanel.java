/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import aeg.giocomap.Model.Giocatore.Inventario;
import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Oggetti.Spada;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.InputStream;

/**
 *
 * @author Andrea
 */
public class InventarioPanel extends JPanel {
    private final Inventario<Oggetto> inventario; // Estensione della Generics Oggetto
    private final JPanel gridPanel;
    
    public InventarioPanel(Inventario<Oggetto> inventario){
        this.inventario = inventario;
        
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);
        
        // Creazione della griglia dell'inventario che si espande automaticamente all'aggiunta di altri elementi
        gridPanel = new JPanel(new GridLayout(0,5,15,15));
        gridPanel.setBackground(Color.DARK_GRAY);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        
        // La griglia sarà interna ad un contenitore a nord per poterla vederla lineare e non centrata
        JPanel contenitore = new JPanel(new BorderLayout());
        contenitore.setBackground(Color.DARK_GRAY);
        ToolTipManager.sharedInstance().setInitialDelay(100); // il fumetto appare all'istante sopra senza aspettare
        ToolTipManager.sharedInstance().setDismissDelay(2000);
        contenitore.add(gridPanel, BorderLayout.NORTH);
        
        // Con scroll possiamo scorrere la griglia tramite mouse
        JScrollPane scrollPane = new JScrollPane(contenitore);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane,BorderLayout.CENTER); // Lo inseriamo non nel contenitore ma direttamente nel pannello principale di questa classe
        
        // Titolo Inventario
        JLabel titolo=new JLabel("Inventario",SwingConstants.CENTER);
        titolo.setFont(new Font("Arial",Font.BOLD,28));
        titolo.setForeground(Color.WHITE);
        titolo.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        add(titolo,BorderLayout.NORTH);
    }
    
    public void aggiornaVista(){
        gridPanel.removeAll(); // elimino gli oggetti presenti in precedenza per non duplicarli ogni volta che apro l'inventario e il costruttore viene richiamato
        
        if(inventario.getListaOggetti().isEmpty())
            System.out.println("TEST: Inventario vuoto");
        else{
            //Ciclo for per creare i quadratini nella griglia per ogni oggetto in Model
            for(Oggetto obj : inventario.getListaOggetti()){
                JPanel slot = creaSlotOggetto(obj);
                gridPanel.add(slot);
            }
        }
        
        // Ricalcola le coordinate e le ristampa
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // Crea la grafica dell'oggetto da mettere nell'inventario
    private JPanel creaSlotOggetto(Oggetto obj) {
        
        // Creazione dello slot
        JPanel slot = new JPanel(new BorderLayout());
        slot.setPreferredSize(new Dimension(120,120));
        slot.setBackground(new Color(60,60,60));
        slot.setBorder(BorderFactory.createLineBorder(new Color(150,150,105),2)); //Bordo
        
        // Caricamento dell'immagine dell'oggetto all'interno del suo quadratino
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        try{
            String nomeFileImmagine=obj.getNomeOggetto()+".png";
            InputStream is= getClass().getResourceAsStream("/sprites/Oggetti/"+nomeFileImmagine);
            
            if(is!=null){
                BufferedImage img=ImageIO.read(is);
                Image scaledImage = img.getScaledInstance(90,90,Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledImage));
            }
            else{
                // Foto non trovata
                System.out.println("DEBUG: Foto non caricata");
                iconLabel.setText(obj.getNomeOggetto().substring(0,1));
                iconLabel.setFont(new Font("Arial",Font.BOLD,40));
                iconLabel.setForeground(Color.WHITE);
            }
        }
        catch(IOException e){
            System.err.println("Sprite non trovato per "+obj.getNomeOggetto());
        }
        
        // Creazione label tooltip
        StringBuilder tooltip=new StringBuilder();
        tooltip.append("<html><div style='width: 250px; padding:5px;'>")
               .append("<b style='color:#FFD700;'>").append(obj.getNomeOggetto()).append("</b><br>")
               .append("<i>").append(obj.getDescrizioneOggetto()).append("</i>");
        
        // Controllo se sto analizzando la spada col cursore
        if(obj instanceof Spada spada){ //Casting
            tooltip.append("<br><br><b style='color: #00FFFF;'>Carica Sincro: ")
                   .append(spada.getCaricaSincro())
                   .append("%</b>");
        }
        tooltip.append("</div></html>"); //chiudo il tag html aperto per scrivere
        slot.setToolTipText(tooltip.toString());
        iconLabel.setToolTipText(tooltip.toString()); // la label a schermo sarà visibile non solo sull'immagine ma anche se si va sul quadrato della label
        slot.add(iconLabel,BorderLayout.CENTER);
        
        return slot;
    }
}
