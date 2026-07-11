/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author murgo
 */
public class TitoliDiCoda extends JPanel {

    private JButton btn_indietro;          // presente solo nelle Statistiche
    private PannelloScorrimento scroll;    // presente solo nei titoli di coda

    /**
     * @param soloHallOfFame true = mostra solo la Hall of Fame statica con il
     *                       bottone indietro (schermata Statistiche dal menu);
     *                       false = titoli di coda che scorrono, senza bottone,
     *                       che tornano da soli al menu a fine scorrimento.
     */
    public TitoliDiCoda(List<String[]> records, int punteggioAttuale,
                        String nomeGiocatore, boolean soloHallOfFame) {
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        if (soloHallOfFame) {
            // Statistiche: Hall of Fame statica + bottone indietro in basso
            add(creaHallOfFameStatica(records, punteggioAttuale, nomeGiocatore), BorderLayout.CENTER);

            JPanel sud = new JPanel();
            sud.setOpaque(false);
            btn_indietro = new JButton("[ indietro ]");
            btn_indietro.setFont(new Font("Monospaced", Font.PLAIN, 14));
            btn_indietro.setBackground(Color.BLACK);
            btn_indietro.setForeground(new Color(170, 170, 170));
            btn_indietro.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            btn_indietro.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn_indietro.setFocusable(false);
            sud.add(btn_indietro);
            add(sud, BorderLayout.SOUTH);
        } else {
            // Titoli di coda: solo lo scorrimento, nessun bottone
            scroll = new PannelloScorrimento(records, punteggioAttuale, nomeGiocatore);
            add(scroll, BorderLayout.CENTER);
        }
    }

    // Statistiche: il bottone indietro torna al menu
    public void addIndietroListener(ActionListener listener) {
        if (btn_indietro != null) btn_indietro.addActionListener(listener);
    }

    // Titoli di coda: azione eseguita quando lo scorrimento e' terminato
    public void setOnFine(Runnable onFine) {
        if (scroll != null) scroll.setOnFine(onFine);
    }

    // Hall of Fame statica (schermata Statistiche dal menu): niente scorrimento,
    // niente titolo/nomi, solo la classifica dei punteggi
    private static JComponent creaHallOfFameStatica(List<String[]> records,
                                                    int punteggioAttuale, String nomeGiocatore) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel contenuto = new JPanel();
        contenuto.setOpaque(false);
        contenuto.setLayout(new BoxLayout(contenuto, BoxLayout.Y_AXIS));

        JLabel titolo = new JLabel("Hall of Fame");
        titolo.setFont(new Font("Monospaced", Font.BOLD, 30));
        titolo.setForeground(Color.WHITE);
        titolo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenuto.add(titolo);
        contenuto.add(Box.createRigidArea(new Dimension(0, 30)));

        if (nomeGiocatore != null && !nomeGiocatore.isEmpty() && punteggioAttuale > 0) {
            JLabel p = new JLabel(nomeGiocatore + "  →  " + punteggioAttuale + " pts");
            p.setFont(new Font("Monospaced", Font.PLAIN, 18));
            p.setForeground(new Color(180, 180, 180));
            p.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenuto.add(p);
            contenuto.add(Box.createRigidArea(new Dimension(0, 30)));
        }

        if (records == null || records.isEmpty()) {
            JLabel n = new JLabel("nessun record.");
            n.setFont(new Font("Monospaced", Font.PLAIN, 16));
            n.setForeground(new Color(120, 120, 120));
            n.setAlignmentX(Component.CENTER_ALIGNMENT);
            contenuto.add(n);
        } else {
            for (int i = 0; i < records.size(); i++) {
                String[] r = records.get(i);
                String riga = (i + 1) + ".   " + r[0] + "   —   " + r[1] + " pts";
                JLabel l = new JLabel(riga);
                l.setFont(new Font("Monospaced", Font.PLAIN, 16));
                boolean mio = nomeGiocatore != null && r[0].equals(nomeGiocatore);
                l.setForeground(mio ? Color.WHITE : new Color(120, 120, 120));
                l.setAlignmentX(Component.CENTER_ALIGNMENT);
                contenuto.add(l);
                contenuto.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        wrap.add(contenuto);
        return wrap;
    }

    // =========================================================================
    // Pannello con i titoli di coda che scorrono verso l'alto una volta
    // =========================================================================
    private static class PannelloScorrimento extends JPanel {

        private final List<Riga> righe = new ArrayList<>();
        private final Timer timer;
        private double scrollY;
        private int altezzaTotale;
        private boolean iniziato = false;
        private Runnable onFine;

        PannelloScorrimento(List<String[]> records, int punteggio, String nomeGiocatore) {
            setOpaque(false);
            costruisciRighe(records, punteggio, nomeGiocatore);

            // ~80 px al secondo: scorre una volta, poi esegue onFine (torna al menu)
            timer = new Timer(20, e -> {
                scrollY -= 1.6;
                if (altezzaTotale > 0 && scrollY < -altezzaTotale) {
                    ((Timer) e.getSource()).stop();
                    if (onFine != null) onFine.run();
                    return;
                }
                repaint();
            });
        }

        void setOnFine(Runnable onFine) {
            this.onFine = onFine;
        }

        private void costruisciRighe(List<String[]> records, int punteggio, String nomeGiocatore) {
            Font fontLogo   = new Font("Serif", Font.BOLD, 64);
            Font fontGrazie = new Font("Georgia", Font.ITALIC, 36);
            Font fontEtichetta = new Font("Monospaced", Font.PLAIN, 18);
            Font fontNome   = new Font("Georgia", Font.PLAIN, 42);
            Font fontTitolo = new Font("Monospaced", Font.BOLD, 30);
            Font fontRecord = new Font("Monospaced", Font.PLAIN, 18);

            Color bianco = new Color(235, 230, 220);
            Color grigio = new Color(150, 150, 150);
            Color grigioScuro = new Color(110, 110, 110);

            // Titolo del gioco come immagine con glow morbido (pre-renderizzata)
            righe.add(new Riga(creaTitolo("The Royal Silk Adventure", fontLogo), 30));
            righe.add(new Riga("Grazie di aver giocato!", fontGrazie, bianco, 70));

            righe.add(new Riga("— Sviluppato da —", fontEtichetta, grigio, 40));
            righe.add(new Riga("Giulio Murgo", fontNome, bianco, 20));
            righe.add(new Riga("Emanuele Santoruvo", fontNome, bianco, 20));
            righe.add(new Riga("Andrea Milo", fontNome, bianco, 80));

            // Punteggio della partita appena conclusa (se presente)
            if (nomeGiocatore != null && !nomeGiocatore.isEmpty() && punteggio > 0) {
                righe.add(new Riga(nomeGiocatore + "  →  " + punteggio + " pts",
                        fontRecord, bianco, 60));
            }

            // Hall of Fame
            righe.add(new Riga("Hall of Fame", fontTitolo, bianco, 30));
            if (records == null || records.isEmpty()) {
                righe.add(new Riga("nessun record.", fontRecord, grigioScuro, 12));
            } else {
                for (int i = 0; i < records.size(); i++) {
                    String[] r = records.get(i);
                    String testo = (i + 1) + ".   " + r[0] + "   —   " + r[1] + " pts";
                    boolean mio = nomeGiocatore != null && r[0].equals(nomeGiocatore);
                    righe.add(new Riga(testo, fontRecord, mio ? bianco : grigioScuro, 12));
                }
            }

            righe.add(new Riga("", fontTitolo, grigio, 60));
            righe.add(new Riga("— Fine —", fontEtichetta, grigio, 200));
        }

        @Override
        public void addNotify() {
            super.addNotify();
            timer.start();
        }

        @Override
        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (!iniziato && getHeight() > 0) {
                scrollY = getHeight();
                iniziato = true;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int hp = getHeight();
            double y = scrollY;

            for (Riga r : righe) {
                if (r.immagine != null) {
                    int ih = r.immagine.getHeight();
                    if (y + ih > 0 && y < hp) {
                        int x = (w - r.immagine.getWidth()) / 2;
                        g2.drawImage(r.immagine, x, (int) y, null);
                    }
                    y += ih + r.spazioSotto;
                } else {
                    g2.setFont(r.font);
                    FontMetrics fm = g2.getFontMetrics();
                    int baseline = (int) y + fm.getAscent();
                    if (baseline > -80 && baseline < hp + 80 && !r.testo.isEmpty()) {
                        int tw = fm.stringWidth(r.testo);
                        int x = (w - tw) / 2;
                        g2.setColor(r.colore);
                        g2.drawString(r.testo, x, baseline);
                    }
                    y += fm.getHeight() + r.spazioSotto;
                }
            }

            altezzaTotale = (int) (y - scrollY);
            g2.dispose();
        }

        // Pre-renderizza il titolo: alone dorato sfocato (glow morbido) + testo
        // pieno con gradiente oro. Disegnato una sola volta, poi solo spostato.
        private BufferedImage creaTitolo(String testo, Font font) {
            BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gm = tmp.createGraphics();
            gm.setFont(font);
            FontMetrics fm = gm.getFontMetrics();
            int tw = fm.stringWidth(testo);
            int ascent = fm.getAscent();
            int th = fm.getHeight();
            gm.dispose();

            int pad = 36;
            int w = tw + pad * 2;
            int h = th + pad * 2;

            BufferedImage glow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = glow.createGraphics();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gg.setFont(font);
            gg.setColor(new Color(255, 190, 60));
            gg.drawString(testo, pad, pad + ascent);
            gg.dispose();
            glow = sfoca(glow, 8);

            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.drawImage(glow, 0, 0, null);
            g2.drawImage(glow, 0, 0, null);
            g2.setFont(font);
            GradientPaint oro = new GradientPaint(
                0, pad,          new Color(255, 240, 175),
                0, pad + ascent, new Color(196, 142, 40));
            g2.setPaint(oro);
            g2.drawString(testo, pad, pad + ascent);
            g2.dispose();
            return img;
        }

        // Sfocatura gaussiana (per il glow morbido)
        private BufferedImage sfoca(BufferedImage src, int raggio) {
            int size = raggio * 2 + 1;
            float[] dati = new float[size * size];
            float sigma = raggio / 2f;
            float somma = 0;
            for (int yy = -raggio; yy <= raggio; yy++) {
                for (int xx = -raggio; xx <= raggio; xx++) {
                    float v = (float) Math.exp(-(xx * xx + yy * yy) / (2 * sigma * sigma));
                    dati[(yy + raggio) * size + (xx + raggio)] = v;
                    somma += v;
                }
            }
            for (int i = 0; i < dati.length; i++) dati[i] /= somma;
            Kernel kernel = new Kernel(size, size, dati);
            return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null).filter(src, null);
        }

        private static class Riga {
            final String testo;
            final Font font;
            final Color colore;
            final int spazioSotto;
            final BufferedImage immagine;   // != null => riga-immagine (titolo)

            Riga(String testo, Font font, Color colore, int spazioSotto) {
                this.testo = testo;
                this.font = font;
                this.colore = colore;
                this.spazioSotto = spazioSotto;
                this.immagine = null;
            }

            Riga(BufferedImage immagine, int spazioSotto) {
                this.immagine = immagine;
                this.spazioSotto = spazioSotto;
                this.testo = "";
                this.font = null;
                this.colore = null;
            }
        }
    }
}
