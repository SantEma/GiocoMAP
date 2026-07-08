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
    private final String testo;
    public GameScreen(List<String> righe, Runnable AzioneSigillo) {
        this.testo = String.join("", righe);

        double imgW = 1058.0;
        double imgH = 996.0;
        
        double x_rosso = 881.0 / imgW;
        double y_rosso = 814.0 / imgH;
        double larghezza = (1058.0 - 881.0) / imgW;
        double altezza = (996.0 - 814.0) / imgH;

        try {
            sfondo = ImageIO.read(getClass().getResourceAsStream(
                "/sprites/StrumentiGrafici/LetteraIniziale.png"));
        } 
        catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }

        setLayout(null);  // layout libero → gestiamo noi le posizioni
    
        // Sigillo rosso per girare la lettera usando le percentuali (double[])
        Map<double[], Runnable> zone = new HashMap<>();
        double[] pulsante_rosso = new double[]{x_rosso, y_rosso, larghezza, altezza};
        zone.put(pulsante_rosso, AzioneSigillo);
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
        int w = getWidth();
        int h = getHeight();
        //disegna sfondo
        if (sfondo != null) {
           g.drawImage(sfondo, 0, 0, w, h, this);
           
        }
         // calcola font in base alla larghezza
        int fontSize = Math.max(10, w / 90);
        Font font = new Font("Garamond", Font.ITALIC | Font.BOLD, fontSize);
        g.setFont(font);
        g.setColor(new Color(60, 30, 10));

        // area del testo in percentuale sulla pergamena
        int testoX = (int)(w * 0.32);     // inizia al 32% da sinistra
        int testoY = (int)(h * 0.28);     // inizia al 28% dall'alto
        int testoW = (int)(w * 0.38);     // largo il 38% della finestra
        int testoH = (int)(h * 0.55);     // alto il 55% della finestra

        // disegna il testo con a capo automatico
        disegnaTesto(g, testo, testoX, testoY, testoW, testoH, fontSize);
    }
    
    private void disegnaTesto(Graphics g, String testo, int x, int y, int maxW, int maxH, int fontSize) {
       FontMetrics fm = g.getFontMetrics();
       int lineHeight = fm.getHeight();
       int currentY = y + fm.getAscent();
       int maxY = y + maxH;

       String[] parole = testo.split(" ");
       StringBuilder rigaCorrente = new StringBuilder();

       for (String parola : parole) {
           if (parola.contains("\n")) {
                String[] parti = parola.split("\n", -1);
               for (int i = 0; i < parti.length; i++) {
                   String prova = rigaCorrente.length() > 0
                       ? rigaCorrente + " " + parti[i]
                       : parti[i];

                   if (fm.stringWidth(prova) > maxW) {
                       if (currentY <= maxY) g.drawString(rigaCorrente.toString(), x, currentY);
                       currentY += lineHeight;
                       rigaCorrente = new StringBuilder(parti[i]);
                   } else {
                       rigaCorrente = new StringBuilder(prova);
                   }

                   if (i < parti.length - 1) {
                       if (currentY <= maxY) g.drawString(rigaCorrente.toString(), x, currentY);
                       currentY += lineHeight;
                       rigaCorrente = new StringBuilder();
                   }
               }
            } else {
                String prova = rigaCorrente.length() > 0
                    ? rigaCorrente + " " + parola
                    : parola;

                if (fm.stringWidth(prova) > maxW) {
                    if (currentY <= maxY) g.drawString(rigaCorrente.toString(), x, currentY);
                    currentY += lineHeight;
                    rigaCorrente = new StringBuilder(parola);
                } else {
                    rigaCorrente = new StringBuilder(prova);
                }
            }
        }

        
        if (rigaCorrente.length() > 0 && currentY <= maxY) {
            g.drawString(rigaCorrente.toString(), x, currentY);
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
    }
}

