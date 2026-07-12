package aeg.giocomap.View;

import javax.swing.JPanel;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Interfaccia per abilitare il debug delle coordinate percentuali su uno schermo di gioco.
 */
public interface CoordinateDebuggable {
    
    /**
     * Riferimento al pannello su cui abilitare il listener del mouse.
     * Le classi che implementano questa interfaccia (es. GameScreen, MappaPanel) 
     * restituiranno 'this'.
     * @return 
     */
    JPanel getPanel();

    /**
     * Calcola l'area effettiva in cui è disegnata l'immagine.
     * Di default è l'intero pannello, ma può essere sovrascritto se l'immagine
     * ha un offset (es. per mantenere l'aspect ratio con bande nere).
     * @param panelWidth
     * @param panelHeight
     * @return 
     */
    default Rectangle getAreaImmagine(int panelWidth, int panelHeight) {
        return new Rectangle(0, 0, panelWidth, panelHeight);
    }

    /**
     * Abilita un MouseListener che stampa le coordinate percentuali basate
     * sull'area dell'immagine calcolata.
     */
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
