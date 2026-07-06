package aeg.giocomap.View;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;

public class GameScreen extends JPanel {
    private BufferedImage sfondo;
    
    public GameScreen(List<String> righe) {
        String testo = String.join("", righe);
        try {
            sfondo = ImageIO.read(getClass().getResourceAsStream(
                "/sprites/StrumentiGrafici/LetteraIniziale.png"));
        } catch (IOException e) {
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
        }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (sfondo != null) {
            g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

