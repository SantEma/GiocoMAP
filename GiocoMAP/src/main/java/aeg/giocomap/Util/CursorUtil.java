/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package aeg.giocomap.Util;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.Map;

/**
 *
 * @author murgo
 */



public class CursorUtil {

    // nelle classi in cui ci sono bottoni semplici,
    //basta importare la classe CursorLite e chiamare questo metodo inserendo 
    //come parametro il nome del bottone che vogliamo cliccare
    public static void setHandCursor(JComponent... componenti) {
        for (JComponent c : componenti) {
            c.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    /**
 * Registra zone cliccabili su un pannello con sfondo grafico.
 * 
 * Ogni zona è definita da un Rectangle che rappresenta un'area
 * sensibile dello sfondo (es. un NPC o un oggetto di scena).
 * Quando il cursore entra in una zona, diventa una manina.
 * Quando il cursore esce da tutte le zone, torna al default.
 * Al click su una zona, viene eseguita l'azione associata (Runnable).
 *
 * @param panel  il pannello su cui registrare le zone
 * @param zone   mappa che associa ogni Rectangle alla sua azione
 *               es. zona del pescatore → avvia dialogo con pescatore
 */
 /**
 * 
  - Debug a runtime:
 *   Aggiungere temporaneamente al pannello un MouseListener che
 *   stampa in console le coordinate di ogni click:
 *
 *   addMouseListener(new MouseAdapter() {
 *       public void mouseClicked(MouseEvent e) {
 *           System.out.println("x=" + e.getX() + " y=" + e.getY());
 *       }
 *   });
 *   Cliccare sui 4 angoli del personaggio per ricavare x, y,
 *   larghezza e altezza del Rectangle corrispondente.
 *
 * Esempio:
 *   Map<Rectangle, Runnable> zone = new HashMap<>();
 *   zone.put(new Rectangle(200, 300, 100, 150), () -> {
 *       // azione al click sul personaggio
 *   });
 *   CursorUtil.registraZone(this, zone);
 */   
    
    public static void registraZone(JPanel panel, Map<Rectangle, Runnable> zone) {

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean sopraZona = zone.keySet().stream()
                    .anyMatch(r -> r.contains(e.getPoint()));

                if (sopraZona) {
                    panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        
        
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Map.Entry<Rectangle, Runnable> entry : zone.entrySet()) {
                    if (entry.getKey().contains(e.getPoint())) {
                        entry.getValue().run();
                    }
                }
            }
        });
    }
}