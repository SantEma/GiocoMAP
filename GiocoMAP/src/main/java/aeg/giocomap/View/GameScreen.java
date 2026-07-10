/**
 * @author Andrea
 */

package aeg.giocomap.View;

import aeg.giocomap.Util.CursorUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;

public class GameScreen extends JPanel {
    private BufferedImage immagine;
    
    // Riferimenti ai bottoni direzionali (se impostati)
    private JButton btnNord, btnSud, btnEst, btnOvest;
    private boolean frecceAttive = false;
    
    public GameScreen(BufferedImage sfondo, Map<double[],Runnable> zoneCliccabili) {
        this.immagine = sfondo;
        setLayout(null);
        
        if(zoneCliccabili != null && !zoneCliccabili.isEmpty())
            CursorUtil.registraZone(this, zoneCliccabili);
            
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (frecceAttive) {
                    riposizionaFrecce();
                }
            }
        });
    }
    
    public void setSfondo(BufferedImage nuovoSfondo){
        this.immagine = nuovoSfondo;
        repaint();
    }
    
    public void impostaFrecce(Runnable azNORD, Runnable azSUD, Runnable azEST, Runnable azOVEST) {
        this.frecceAttive = true;
        
        btnNord = creaBottoneFreccia("/sprites/StrumentiGrafici/NORDarrow.png", azNORD);
        btnSud = creaBottoneFreccia("/sprites/StrumentiGrafici/SUDarrow.png", azSUD);
        btnEst = creaBottoneFreccia("/sprites/StrumentiGrafici/ESTarrow.png", azEST);
        btnOvest = creaBottoneFreccia("/sprites/StrumentiGrafici/OVESTarrow.png", azOVEST);
        
        if (btnNord != null) add(btnNord);
        if (btnSud != null) add(btnSud);
        if (btnEst != null) add(btnEst);
        if (btnOvest != null) add(btnOvest);
        
        riposizionaFrecce();
    }
    
    private JButton creaBottoneFreccia(String resourcePath, Runnable azione) {
        URL iconUrl = getClass().getResource(resourcePath);
        if (iconUrl == null) {
            System.err.println("Errore: Immagine freccia non trovata -> " + resourcePath);
            return null;
        }
        
        ImageIcon icona = new ImageIcon(iconUrl);
        JButton btn = new JButton(icona);
        
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setSize(icona.getIconWidth(), icona.getIconHeight());
        
        btn.addActionListener(e -> {
            if (azione != null) {
                azione.run();
            } else {
                System.out.println("Nessuna azione impostata per questa direzione.");
            }
        });
        
        return btn;
    }
    
    private void riposizionaFrecce() {
        int w = getWidth();
        int h = getHeight();
        int margin = 20;
        
        if (btnNord != null) {
            btnNord.setLocation((w - btnNord.getWidth()) / 2, margin);
        }
        if (btnSud != null) {
            btnSud.setLocation((w - btnSud.getWidth()) / 2, h - btnSud.getHeight() - margin);
        }
        if (btnEst != null) {
            btnEst.setLocation(w - btnEst.getWidth() - margin, (h - btnEst.getHeight()) / 2);
        }
        if (btnOvest != null) {
            btnOvest.setLocation(margin, (h - btnOvest.getHeight()) / 2);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(immagine != null){
            //Disegno lo sfondo
            g.drawImage(immagine,0,0,getWidth(),getHeight(),this);
        }
    }
    
    /**
     * Metodo di debug richiamabile per farsi stampare a schermo
     * le coordinate e le percentuali del click del mouse sulla scena.
     * Molto utile per creare le hit-box dei personaggi.
     */
    public void abilitaDebugCoordinate() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double xPerc = (double) e.getX() / getWidth();
                double yPerc = (double) e.getY() / getHeight();
                System.out.printf("DEBUG CLICK: x%% = %.4f, y%% = %.4f (pixel reali: x=%d, y=%d)\n", 
                                  xPerc, yPerc, e.getX(), e.getY());
            }
        });
    }
}

