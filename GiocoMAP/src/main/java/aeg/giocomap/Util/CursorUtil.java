

package aeg.giocomap.Util;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import aeg.giocomap.View.CoordinateDebuggable;

/**
 *
 * @author andrea
 */
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
     * Le zone sono definite in percentuale rispetto alle dimensioni del pannello
     * o all'area dell'immagine (se il pannello implementa CoordinateDebuggable).
     *
     * Le percentuali si ricavano abilitando il Metodo Debug a runtime, 
     * chiamando semplicemente abilitaDebugCoordinate() sul pannello se implementa l'interfaccia.
     *
     * @param panel  il pannello su cui registrare le zone
     * @param zone   mappa che associa {xPerc, yPerc, wPerc, hPerc} alla sua azione
     */
    public static void registraZone(JPanel panel, Map<double[], Runnable> zone) {

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Rectangle area;
                if (panel instanceof CoordinateDebuggable) {
                    area = ((CoordinateDebuggable) panel).getAreaImmagine(panel.getWidth(), panel.getHeight());
                } else {
                    area = new Rectangle(0, 0, panel.getWidth(), panel.getHeight());
                }
                                 
                boolean sopra = false;

                for (double[] perc : zone.keySet()) {
                    int rx = area.x + (int)(area.width * perc[0]);
                    int ry = area.y + (int)(area.height * perc[1]);
                    int rw = (int)(area.width * perc[2]);
                    int rh = (int)(area.height * perc[3]);

                    if (new Rectangle(rx, ry, rw, rh).contains(e.getPoint())) {
                        sopra = true;
                        break;
                    }
                }

                panel.setCursor(new Cursor(sopra ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Rectangle area;
                if (panel instanceof CoordinateDebuggable) {
                    area = ((CoordinateDebuggable) panel).getAreaImmagine(panel.getWidth(), panel.getHeight());
                } else {
                    area = new Rectangle(0, 0, panel.getWidth(), panel.getHeight());
                }

                List<Runnable> actionsToRun = new ArrayList<>();
                for (Map.Entry<double[], Runnable> entry : zone.entrySet()) {
                    double[] perc = entry.getKey();
                    int rx = area.x + (int)(area.width * perc[0]);
                    int ry = area.y + (int)(area.height * perc[1]);
                    int rw = (int)(area.width * perc[2]);
                    int rh = (int)(area.height * perc[3]);

                    if (new Rectangle(rx, ry, rw, rh).contains(e.getPoint())) {
                        actionsToRun.add(entry.getValue());
                    }
                }
                
                for (Runnable action : actionsToRun) {
                    action.run();
                }
            }
        });
    }
}
