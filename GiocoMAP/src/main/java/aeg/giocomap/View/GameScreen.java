package aeg.giocomap.View;

import aeg.giocomap.Util.CursorUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 *
 * @author andrea
 */
public class GameScreen extends JPanel {
    private BufferedImage immagine;

    public GameScreen(BufferedImage sfondo, Map<double[],Runnable> zoneCliccabili) {
        this.immagine = sfondo;
        setLayout(null);

        if(zoneCliccabili != null && !zoneCliccabili.isEmpty())
            CursorUtil.registraZone(this, zoneCliccabili);
    }

    public JPanel getPanel() {
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(immagine != null){
            //Disegno lo sfondo
            g.drawImage(immagine,0,0,getWidth(),getHeight(),this);
        }
    }
}

