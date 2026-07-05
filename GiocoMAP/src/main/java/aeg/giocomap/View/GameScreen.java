package aeg.giocomap.View;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GameScreen extends JPanel {

    private BufferedImage sfondo;

    public GameScreen(String testo) {
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
        area.setFont(new Font("Palatino Linotype", Font.ITALIC | Font.BOLD, 11));
        area.setPreferredSize(new Dimension(340, 340));
        area.setMaximumSize(new Dimension(340, 340));

        GridBagConstraints gb = new GridBagConstraints();
        gb.insets = new Insets(80, 0, 0, 0);
        add(area, gb);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (sfondo != null) g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
    }
}