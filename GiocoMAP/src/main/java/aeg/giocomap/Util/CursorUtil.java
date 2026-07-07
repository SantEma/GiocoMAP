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

public class CursorUtil {

    // per i bottoni semplici
    public static void setHandCursor(JComponent... componenti) {
        for (JComponent c : componenti) {
            c.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    /**
     * Registra zone cliccabili responsive su un pannello con sfondo grafico.
     *
     * Le zone sono definite in percentuale rispetto alle dimensioni del pannello,
     * quindi funzionano su qualsiasi schermo e si ridimensionano automaticamente.
     *
     * Le percentuali si ricavano con il Metodo 2 - Debug a runtime:
     * Aggiungere temporaneamente al pannello un MouseListener che stampa
     * le coordinate in percentuale di ogni click:
     *
     *   addMouseListener(new MouseAdapter() {
     *       public void mouseClicked(MouseEvent e) {
     *           double xPerc = (double) e.getX() / getWidth();
     *           double yPerc = (double) e.getY() / getHeight();
     *           System.out.println("x%=" + xPerc + " y%=" + yPerc);
     *       }
     *   });
     *
     * Esempio di utilizzo:
     *   Map<double[], Runnable> zone = new HashMap<>();
     *   zone.put(new double[]{0.45, 0.30, 0.10, 0.25}, () -> {
     *       // azione al click
     *   });
     *   CursorUtil.registraZone(this, zone);
     *
     * @param panel  il pannello su cui registrare le zone
     * @param zone   mappa che associa {xPerc, yPerc, wPerc, hPerc} alla sua azione
     */
    public static void registraZone(JPanel panel, Map<double[], Runnable> zone) {

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();
                boolean sopra = false;

                for (double[] perc : zone.keySet()) {
                    int rx = (int)(w * perc[0]);
                    int ry = (int)(h * perc[1]);
                    int rw = (int)(w * perc[2]);
                    int rh = (int)(h * perc[3]);

                    if (new Rectangle(rx, ry, rw, rh).contains(e.getPoint())) {
                        sopra = true;
                        break;
                    }
                }

                panel.setCursor(new Cursor(sopra ?
                    Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();

                for (Map.Entry<double[], Runnable> entry : zone.entrySet()) {
                    double[] perc = entry.getKey();
                    int rx = (int)(w * perc[0]);
                    int ry = (int)(h * perc[1]);
                    int rw = (int)(w * perc[2]);
                    int rh = (int)(h * perc[3]);

                    if (new Rectangle(rx, ry, rw, rh).contains(e.getPoint())) {
                        entry.getValue().run();
                    }
                }
            }
        });
    }
}