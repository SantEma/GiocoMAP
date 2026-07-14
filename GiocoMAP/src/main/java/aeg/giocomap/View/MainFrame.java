package aeg.giocomap.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UnsupportedLookAndFeelException;
/**
 *
 * @author Andrea
 */
public class MainFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(MainFrame.class.getName());

    // Bottone chat sempre visibile: viene messo sul JLayeredPane gia' integrato
    // nel JFrame (getLayeredPane), su un livello sopra al content pane. Cosi
    // fluttua sopra qualunque scena senza interferire con mostraPannello()
    private JButton btnChatFluttuante;

    // Pannello del lampo arcobaleno, mostrato in dissolvenza sopra la glass pane
    private JPanel lampoArcobaleno;
    private float opacitaLampo = 0f;
    
    // Frecce direzionali in sovraimpressione
    private JButton btnNord;
    private JButton btnSud;
    private JButton btnEst;
    private JButton btnOvest;
    
    // Immagini originali delle frecce per il ridimensionamento
    private Image imgNord;
    private Image imgSud;
    private Image imgEst;
    private Image imgOvest;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();

        // Impedire di rimpicciolire o modificare la grandezza della finestra
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));


        URL iconURL = getClass().getResource("/sprites/Oggetti/Tessuto.png");
        if(iconURL != null){
            ImageIcon icona = new ImageIcon(iconURL);
            this.setIconImage(icona.getImage());
            System.out.println("DEBUG: Immagine caricata");
        }
        else System.out.println("DEBUG: Errore di caricamento foto");

        impostaBottoneChat();
        impostaFrecceDirezionali();
    }

    private void impostaBottoneChat() {
        // stesso stile dei bottoni del TitleScreen: bianco, testo nero, Arial bold
        btnChatFluttuante = new JButton("Chat");
        btnChatFluttuante.setFocusable(false);
        btnChatFluttuante.setBackground(Color.WHITE);
        btnChatFluttuante.setForeground(Color.BLACK);
        btnChatFluttuante.setFont(new Font("Arial", Font.BOLD, 16));
        btnChatFluttuante.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // POPUP_LAYER sta sopra al content pane (dove vanno le scene)
        getLayeredPane().add(btnChatFluttuante, JLayeredPane.POPUP_LAYER);

        // riposiziona i bottoni quando la finestra cambia dimensione
        getRootPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                riposizionaBottoneChat();
                riposizionaFrecce();
            }
        });
        riposizionaBottoneChat();
    }
    
    // recuperiamo il file delle frecce
    private void impostaFrecceDirezionali() {
        imgNord = caricaImmagineOriginale("/sprites/StrumentiGrafici/NORDarrow.png");
        imgSud = caricaImmagineOriginale("/sprites/StrumentiGrafici/SUDarrow.png");
        imgEst = caricaImmagineOriginale("/sprites/StrumentiGrafici/ESTarrow.png");
        imgOvest = caricaImmagineOriginale("/sprites/StrumentiGrafici/OVESTarrow.png");
        
        btnNord = creaBottoneFrecciaVuoto();
        btnSud = creaBottoneFrecciaVuoto();
        btnEst = creaBottoneFrecciaVuoto();
        btnOvest = creaBottoneFrecciaVuoto();
        
        getLayeredPane().add(btnNord, JLayeredPane.POPUP_LAYER);
        getLayeredPane().add(btnSud, JLayeredPane.POPUP_LAYER);
        getLayeredPane().add(btnEst, JLayeredPane.POPUP_LAYER);
        getLayeredPane().add(btnOvest, JLayeredPane.POPUP_LAYER);
        
        setFrecceVisibili(false);
    }
    
    private Image caricaImmagineOriginale(String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }
        return null;
    }

    private JButton creaBottoneFrecciaVuoto() {
        JButton btn = new JButton();
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }


    private void riposizionaBottoneChat() {
        if (btnChatFluttuante == null) return;
        // angolo in alto a destra, cosi non copre la box di dialogo in basso
        int btnW = 110;
        int btnH = 38;
        int margine = 20;
        int w = getLayeredPane().getWidth();
        btnChatFluttuante.setBounds(w - btnW - margine, margine, btnW, btnH);
    }
    
    // riposizioniamo le frecce quando si torna su una scena dove servono
    private void riposizionaFrecce() {
        if (btnNord == null) return;
        int w = getLayeredPane().getWidth();
        int h = getLayeredPane().getHeight();
        
        // Ridimensionamento dinamico: le frecce saranno circa l'8% della larghezza o altezza dello schermo
        int dimFreccia = Math.min(w, h) * 8 / 100;
        
        ridimensionaEImposta(btnNord, imgNord, dimFreccia);
        ridimensionaEImposta(btnSud, imgSud, dimFreccia);
        ridimensionaEImposta(btnEst, imgEst, dimFreccia);
        ridimensionaEImposta(btnOvest, imgOvest, dimFreccia);
        
        int margine = 15; // Più vicine ai bordi
        
        btnNord.setLocation((w - btnNord.getWidth()) / 2, margine);
        btnSud.setLocation((w - btnSud.getWidth()) / 2, h - btnSud.getHeight() - margine);
        btnEst.setLocation(w - btnEst.getWidth() - margine, (h - btnEst.getHeight()) / 2);
        btnOvest.setLocation(margine, (h - btnOvest.getHeight()) / 2);
    }

    private void ridimensionaEImposta(JButton btn, Image imgOriginale, int targetDim) {
        if (imgOriginale != null) {
            int origW = imgOriginale.getWidth(null);
            int origH = imgOriginale.getHeight(null);
            if (origW <= 0 || origH <= 0) return;
            
            int newW, newH;
            if (origW > origH) {
                newW = targetDim;
                newH = (int)((double)origH / origW * targetDim);
            } else {
                newH = targetDim;
                newW = (int)((double)origW / origH * targetDim);
            }
            
            Image scaled = imgOriginale.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaled));
            btn.setSize(newW, newH);
        }
    }

    public void setFrecceVisibili(boolean visibili) {
        if (btnNord != null) {
            btnNord.setVisible(visibili);
            btnSud.setVisible(visibili);
            btnEst.setVisible(visibili);
            btnOvest.setVisible(visibili);
        }
    }

    // registra l'azione da eseguire al click del bottone chat fluttuante
    public void setChatListener(ActionListener listener) {
        btnChatFluttuante.addActionListener(listener);
    }

    // nasconde/mostra il bottone chat fluttuante (es. durante i titoli di coda)
    public void setChatButtonVisibile(boolean visibile) {
        if (btnChatFluttuante != null) {
            btnChatFluttuante.setVisible(visibile);
        }
    }

    // Mezzo lampo arcobaleno a tutto schermo, per dare enfasi ai momenti clou
    // (es. la Spada Sincro che raggiunge il MAX). Dissolvenza rapida in entrata
    // e in uscita, non intercetta i click perche' resta visibile solo un istante.
    public void mostraLampoArcobaleno() {
        if (lampoArcobaleno == null) {
            lampoArcobaleno = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (opacitaLampo <= 0f) return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    int w = getWidth();
                    int h = getHeight();
                    int alpha = Math.max(0, Math.min(255, Math.round(140 * opacitaLampo)));
                    Color[] colori = {
                        new Color(255, 0, 0, alpha),
                        new Color(255, 165, 0, alpha),
                        new Color(255, 255, 0, alpha),
                        new Color(0, 200, 0, alpha),
                        new Color(0, 120, 255, alpha),
                        new Color(150, 0, 255, alpha)
                    };
                    float[] frazioni = {0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f};
                    g2.setPaint(new LinearGradientPaint(new Point(0, 0), new Point(w, h), frazioni, colori));
                    g2.fillRect(0, 0, w, h);
                    g2.dispose();
                }
            };
            lampoArcobaleno.setOpaque(false);
        }

        setGlassPane(lampoArcobaleno);
        lampoArcobaleno.setVisible(true);

        Timer fadeIn = new Timer(20, null);
        fadeIn.addActionListener(e -> {
            opacitaLampo += 0.15f;
            lampoArcobaleno.repaint();
            if (opacitaLampo >= 1f) {
                opacitaLampo = 1f;
                ((Timer) e.getSource()).stop();

                Timer pausa = new Timer(250, null);
                pausa.setRepeats(false);
                pausa.addActionListener(ev -> {
                    Timer fadeOut = new Timer(20, null);
                    fadeOut.addActionListener(ev2 -> {
                        opacitaLampo -= 0.08f;
                        lampoArcobaleno.repaint();
                        if (opacitaLampo <= 0f) {
                            opacitaLampo = 0f;
                            lampoArcobaleno.setVisible(false);
                            ((Timer) ev2.getSource()).stop();
                        }
                    });
                    fadeOut.start();
                });
                pausa.start();
            }
        });
        fadeIn.start();
    }

    // registra le azioni per le frecce direzionali
    public void setFrecceListener(ActionListener nord, ActionListener sud, ActionListener est, ActionListener ovest) {
        if (nord != null) btnNord.addActionListener(nord);
        if (sud != null) btnSud.addActionListener(sud);
        if (est != null) btnEst.addActionListener(est);
        if (ovest != null) btnOvest.addActionListener(ovest);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Adventure Game MAP");
        setIconImage(getIconImage());
        setIconImages(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1063, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 514, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
    // Funzione per cambiare scenari
    public void mostraPannello(JComponent newPanel){
        // Rimuovo gli elementi della scena attuale
        this.getContentPane().removeAll();

        // Creo una nuova paginazione con nuove regole per la nuova scena
        this.getContentPane().setLayout(new BorderLayout());

        // Inserisco la nuova scena
        this.getContentPane().add(newPanel, BorderLayout.CENTER);

        // Ricalibrare la finestra grafica
        this.revalidate();
        this.repaint();

        // il bottone chat e le frecce vivono sul layeredPane del frame, li tengo sempre
        // in primo piano sopra la nuova scena
        if (btnChatFluttuante != null) {
            getLayeredPane().setLayer(btnChatFluttuante, JLayeredPane.POPUP_LAYER);
            riposizionaBottoneChat();
            btnChatFluttuante.repaint();
        }
        if (btnNord != null) {
            getLayeredPane().setLayer(btnNord, JLayeredPane.POPUP_LAYER);
            getLayeredPane().setLayer(btnSud, JLayeredPane.POPUP_LAYER);
            getLayeredPane().setLayer(btnEst, JLayeredPane.POPUP_LAYER);
            getLayeredPane().setLayer(btnOvest, JLayeredPane.POPUP_LAYER);
            riposizionaFrecce();
            btnNord.repaint(); btnSud.repaint(); btnEst.repaint(); btnOvest.repaint();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
