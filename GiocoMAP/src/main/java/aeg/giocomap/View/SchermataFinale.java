package aeg.giocomap.View;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Scena muta di chiusura del gioco: nessun tasto (ESC, mappa, inventario,
 * chat) e nessuna icona di movimento è disponibile qui, la scena avanza da
 * sola. Il testo finale appare e scompare in dissolvenza su sfondo nero,
 * poi si intravede per qualche secondo la sagoma del Saggio Clock prima di
 * richiamare {@code alTermine} (che avvia i titoli di coda).
 *
 * @author emanuele
 */
public class SchermataFinale extends JPanel {

    private static final int PASSO_MS = 40;
    private static final float INCREMENTO_DISSOLVENZA = 0.04f;

    private final String testo;
    private BufferedImage spriteClock;

    private float opacitaTesto = 0f;
    private float opacitaClock = 0f;
    private boolean mostraClock = false;
    private boolean sequenzaAvviata = false;
    private final Runnable alTermine;

    public SchermataFinale(List<String> righe, Runnable alTermine) {
        this.testo = String.join("", righe);
        this.alTermine = alTermine;

        try {
            spriteClock = ImageIO.read(getClass().getResourceAsStream("/sprites/Personaggi/Clock.png"));
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }

        setBackground(Color.BLACK);
        setFocusable(false);
    }

    // Da richiamare esplicitamente quando la scena viene mostrata al giocatore
    // (non nel costruttore: le scene vengono tutte istanziate all'avvio del
    // gioco, molto prima che il finale venga effettivamente raggiunto).
    public void avviaSequenza() {
        if (sequenzaAvviata) return;
        sequenzaAvviata = true;

        opacitaTesto = 0f;
        opacitaClock = 0f;
        mostraClock = false;

        avviaFadeInTesto(alTermine);
    }

    private void avviaFadeInTesto(Runnable alTermine) {
        Timer fadeIn = new Timer(PASSO_MS, null);
        fadeIn.addActionListener(e -> {
            opacitaTesto += INCREMENTO_DISSOLVENZA;
            if (opacitaTesto >= 1f) {
                opacitaTesto = 1f;
                fadeIn.stop();
                Timer pausa = new Timer(6000, ev -> avviaFadeOutTesto(alTermine));
                pausa.setRepeats(false);
                pausa.start();
            }
            repaint();
        });
        fadeIn.start();
    }

    private void avviaFadeOutTesto(Runnable alTermine) {
        Timer fadeOut = new Timer(PASSO_MS, null);
        fadeOut.addActionListener(e -> {
            opacitaTesto -= INCREMENTO_DISSOLVENZA;
            if (opacitaTesto <= 0f) {
                opacitaTesto = 0f;
                fadeOut.stop();
                avviaFadeInClock(alTermine);
            }
            repaint();
        });
        fadeOut.start();
    }

    private void avviaFadeInClock(Runnable alTermine) {
        mostraClock = true;
        Timer fadeIn = new Timer(PASSO_MS, null);
        fadeIn.addActionListener(e -> {
            opacitaClock += INCREMENTO_DISSOLVENZA;
            if (opacitaClock >= 1f) {
                opacitaClock = 1f;
                fadeIn.stop();
                Timer pausa = new Timer(1200, ev -> avviaFadeOutClock(alTermine));
                pausa.setRepeats(false);
                pausa.start();
            }
            repaint();
        });
        fadeIn.start();
    }

    private void avviaFadeOutClock(Runnable alTermine) {
        Timer fadeOut = new Timer(PASSO_MS, null);
        fadeOut.addActionListener(e -> {
            opacitaClock -= INCREMENTO_DISSOLVENZA;
            if (opacitaClock <= 0f) {
                opacitaClock = 0f;
                fadeOut.stop();
                if (alTermine != null) alTermine.run();
            }
            repaint();
        });
        fadeOut.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (mostraClock) {
            if (spriteClock != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacitaClock));
                int dimensione = Math.min(w, h) / 3;
                int x = (w - dimensione) / 2;
                int y = (h - dimensione) / 2;
                g2.drawImage(spriteClock, x, y, dimensione, dimensione, this);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacitaTesto));
            int fontSize = Math.max(22, w / 45);
            g2.setFont(new Font("Georgia", Font.PLAIN, fontSize));
            g2.setColor(Color.WHITE);
            disegnaTestoCentrato(g2, testo, w, h);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private void disegnaTestoCentrato(Graphics2D g, String testo, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int maxW = (int) (w * 0.7);
        List<String> righe = spezzaInRighe(testo, fm, maxW);

        int lineHeight = fm.getHeight();
        int startY = (h - righe.size() * lineHeight) / 2 + fm.getAscent();
        for (int i = 0; i < righe.size(); i++) {
            int lineWidth = fm.stringWidth(righe.get(i));
            int x = (w - lineWidth) / 2;
            g.drawString(righe.get(i), x, startY + i * lineHeight);
        }
    }

    private List<String> spezzaInRighe(String testo, FontMetrics fm, int maxW) {
        List<String> righe = new ArrayList<>();
        StringBuilder rigaCorrente = new StringBuilder();

        for (String parola : testo.split(" ")) {
            if (parola.contains("\n")) {
                String[] parti = parola.split("\n", -1);
                for (int i = 0; i < parti.length; i++) {
                    rigaCorrente = accodaParola(righe, rigaCorrente, parti[i], fm, maxW);
                    if (i < parti.length - 1) {
                        righe.add(rigaCorrente.toString());
                        rigaCorrente = new StringBuilder();
                    }
                }
            } else {
                rigaCorrente = accodaParola(righe, rigaCorrente, parola, fm, maxW);
            }
        }
        if (rigaCorrente.length() > 0) righe.add(rigaCorrente.toString());

        return righe;
    }

    private StringBuilder accodaParola(List<String> righe, StringBuilder rigaCorrente, String parola, FontMetrics fm, int maxW) {
        String prova = rigaCorrente.length() > 0 ? rigaCorrente + " " + parola : parola;
        if (fm.stringWidth(prova) > maxW) {
            righe.add(rigaCorrente.toString());
            return new StringBuilder(parola);
        }
        return new StringBuilder(prova);
    }
}
