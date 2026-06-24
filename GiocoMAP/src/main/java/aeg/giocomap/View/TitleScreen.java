/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 *
 * @author Andrea
 */

//estendo con JPanel per poter inserire i bottoni
public class TitleScreen extends JPanel {
    private JButton btn_nuova_partita;
    private JButton btn_carica_partita;
    
    // Costruttore che crea la griglia divisa in 5 zone
    // con questa griglia evito di dover dare la corretta posizione in pixel dei bottoni
    public TitleScreen(){
        setLayout(new BorderLayout());
        
        JPanel buttonPanel = new JPanel();
        //buttonPanel.SetOpaque(false);
        
        btn_nuova_partita = new JButton("Nuova Partita");
        btn_carica_partita = new JButton("Carica Partita");
        buttonPanel.add(btn_nuova_partita);
        buttonPanel.add(btn_carica_partita);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // Metodi Listener per chiamare la logica che eseguono i bottoni
    public void addNPListener(ActionListener listener){
        btn_nuova_partita.addActionListener(listener);
    }
    public void addCPListener(ActionListener listener){
        btn_carica_partita.addActionListener(listener);
    }
}
