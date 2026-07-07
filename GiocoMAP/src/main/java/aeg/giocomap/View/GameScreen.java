package aeg.giocomap.View;
import aeg.giocomap.Util.CursorUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen extends JPanel {
    private BufferedImage sfondo;
    
    public GameScreen(List<String> righe, Runnable AzioneSigillo) {
        String testo = String.join("", righe);
        int x_rosso=881;
        int y_rosso=814;
        int larghezza=1058-x_rosso;
        int altezza=996-y_rosso;
        try {
            sfondo = ImageIO.read(getClass().getResourceAsStream(
                "/sprites/StrumentiGrafici/LetteraIniziale.png"));
        } 
        catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }

        setLayout(new GridBagLayout());

        JTextArea area = new JTextArea(testo);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Palatino Linotype", Font.ITALIC | Font.BOLD, 14));
        area.setPreferredSize(new Dimension(600, 420));
        area.setMaximumSize(new Dimension(600, 420));

        GridBagConstraints gb = new GridBagConstraints();
        gb.insets = new Insets(40, 0, 0, 0);
        add(area, gb);
    
        //Sigillo rosso per girare la lettera
        Map<Rectangle, Runnable>zone=new HashMap<>();
        Rectangle pulsante_rosso = new Rectangle(x_rosso,y_rosso,larghezza,altezza);
        zone.put(pulsante_rosso,AzioneSigillo);
        CursorUtil.registraZone(this, zone);
        this.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                System.out.println("DEBUG: Cordinate cliccate: X-> "+e.getX()+" Y-> "+e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (sfondo != null) {
            g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

