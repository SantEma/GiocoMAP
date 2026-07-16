package aeg.giocomap.View;

import javax.swing.JPanel;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author emanuele
 */
public interface CoordinateDebuggable {
    
    /* 
        CLASSE NON PIÙ UTILIZZATA. UTILE SOLO PER CALCOLARE LE COORDINATE
        DELLE HITBOX
    */
    JPanel getPanel();

    /*
     * Calcola l'area effettiva in cui è disegnata l'immagine, di default è l'intero pannello 
     */
    default Rectangle getAreaImmagine(int panelWidth, int panelHeight) {
        return new Rectangle(0, 0, panelWidth, panelHeight);
    }

    
    // Stampa le coordinate percentuali basate sull'area dell'immagine calcolata ad ogni click
    
    default void abilitaDebugCoordinate() {
        JPanel panel = getPanel();
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Rectangle area = getAreaImmagine(panel.getWidth(), panel.getHeight());
                
                // Se il click avviene fuori dall'immagine (es. sulle bande nere), ignora
                if (!area.contains(e.getPoint())) return; 
                
                double xPerc = (double) (e.getX() - area.x) / area.width;
                double yPerc = (double) (e.getY() - area.y) / area.height;
                
                System.out.printf("DEBUG CLICK [%s]: x%% = %.4f, y%% = %.4f (pixel reali: x=%d, y=%d)\n", 
                                  panel.getClass().getSimpleName(), xPerc, yPerc, e.getX(), e.getY());
            }
        });
    }
}
