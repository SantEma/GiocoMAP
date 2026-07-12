/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

import aeg.giocomap.GameEngine.SceneManager;
import java.awt.Rectangle;

/**
 *
 * @author Andrea
 */
public final class MappaPanel extends JPanel implements CoordinateDebuggable {
    private BufferedImage immagineMappa;
    private BufferedImage mappaPre; 
    private BufferedImage immaginePosizione;
    private int ultima_l=-1;
    private int ultima_h=-1;
    private SceneManager sceneManager;
   
    public MappaPanel(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        try{
            //Carico la mappa e il marker posizione
            immagineMappa = ImageIO.read(getClass().getResourceAsStream("/sprites/StrumentiGrafici/MappaShambhala.png"));
            immaginePosizione = ImageIO.read(getClass().getResourceAsStream("/sprites/StrumentiGrafici/Posizione.png"));
            System.out.println("DEBUG: Mappa e cursore posizione caricati con successo in MappaPanel!");
        }
        catch(IOException | IllegalArgumentException e){
            System.err.println("Impossibile caricare Immagine: "+ e.getMessage());
        }
        
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        
        // Istruzioni per far capire come usare la Mappa
        JLabel istruzioni = new JLabel("Premi M per aprire e chiudere la mappa",SwingConstants.CENTER);
        istruzioni.setFont(new Font("Arial",Font.BOLD,20));
        istruzioni.setForeground(Color.WHITE);
        
        //Testo posizionato sotto l'immagine
        istruzioni.setOpaque(true);
        istruzioni.setBackground(new Color(0,0,0,150));
        istruzioni.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        
        add(istruzioni,BorderLayout.SOUTH);
        
        this.addComponentListener(new ComponentAdapter() {
           @Override
           public void componentResized(ComponentEvent e){
               rigeneraMappa();
               repaint();
           }
        });
    }
    
    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public Rectangle getAreaImmagine(int panelW, int panelH) {
        if (immagineMappa == null) return new Rectangle(0, 0, panelW, panelH);
        
        int imgW = immagineMappa.getWidth();
        int imgH = immagineMappa.getHeight();
        
        double scaleX = (double) panelW / imgW;
        double scaleY = (double) panelH / imgH;
        double scale = Math.min(scaleX, scaleY);
            
        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);
            
        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;
        
        return new Rectangle(x, y, drawW, drawH);
    }
    
    public void rigeneraMappa(){
        // Prendo le dimensioni (finestra) dell'immagine originale
        int panelW = getWidth();
        int panelH = getHeight();
        int imgW = immagineMappa.getWidth();
        int imgH = immagineMappa.getHeight();
        
        // Calcolo il fattore di scala per mantenere le proporzioni intatte
        double scaleX = (double) panelW / imgW;
        double scaleY = (double) panelH / imgH;
        double scale = Math.min(scaleX, scaleY); // Prendo il fattore più piccolo per farla entrare tutta
            
        // Calcolo la grandezza finale scalata
        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);
            
        // Calcolo le coordinate X e Y per centrare l'immagine perfettamente
        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;
        
        // Disegno l'immagine con le nuove grandezze proporzionate e centrate
        BufferedImage tela=new BufferedImage(panelW,panelH,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tela.createGraphics();
        
        g.drawImage(immagineMappa, x, y, drawW, drawH, this);
        g.dispose(); // deallocare memoria
        
        // Tela salvata nella variabile globale
        mappaPre = tela;
        ultima_l = panelW;
        ultima_h = panelH;
    }
    
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(immagineMappa != null){
            
            // Se la finestra è cambiata ricreo la mappa in sottofondo
            if(getWidth() != ultima_l || getHeight() != ultima_h || mappaPre == null) rigeneraMappa();
            
            if(mappaPre != null) {
                g.drawImage(mappaPre, 0, 0, this);
            }
            
            // Disegno l'icona della posizione in base alla scena precedente
            if (immaginePosizione != null && sceneManager != null) {
                String scena = sceneManager.getScenaPrecedente();
                double relX = -1, relY = -1;
                
                // Switch per associare ogni scena alle sue coordinate relative sulla mappa
                if (scena != null) {
                    switch (scena) {
                        case "PORTO":
                            relX = 0.1920; relY = 0.2373;
                            break;
                        case "PIAZZA_CENTRALE":
                        case "STALLA":
                            relX = 0.1795; relY = 0.4184;
                            break;
                        case "BOSCO":
                        case "BOSCO_DEEP":
                            relX = 0.5023; relY = 0.5075;
                            break;
                        case "KARUNDIS":
                        case "GROTTA":
                            relX = 0.8216; relY = 0.5789;
                            break;
                        case "INGRESSO_PALAZZO":
                            relX = 0.800; relY=0.4000; //inserire;
                            break;
                        case "SCALE":
                        case "PALAZZO_PRINCIPESSA":
                        case "CRIPTA_ERIPETA":
                            relX = 0.7977; relY = 0.1413;
                            break;
                    }
                }
                
                if (relX >= 0 && relY >= 0) {
                    int panelW = getWidth();
                    int panelH = getHeight();
                    int imgW = immagineMappa.getWidth();
                    int imgH = immagineMappa.getHeight();
                    double scaleX = (double) panelW / imgW;
                    double scaleY = (double) panelH / imgH;
                    double scale = Math.min(scaleX, scaleY);
                    
                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);
                    int xOff = (panelW - drawW) / 2;
                    int yOff = (panelH - drawH) / 2;
                    
                    double markerScale = 0.12; // Modificatore per rimpicciolire ulteriormente l'icona posizione
                    int posW = (int)(immaginePosizione.getWidth() * scale * markerScale);
                    int posH = (int)(immaginePosizione.getHeight() * scale * markerScale);
                    
                    // Disegniamo centrando l'immagine sul punto relativo e traslando verso l'alto
                    int drawX = xOff + (int)(drawW * relX) - (posW / 2);
                    int drawY = yOff + (int)(drawH * relY) - posH; // la punta del marker dovrebbe stare in relY
                    
                    g.drawImage(immaginePosizione, drawX, drawY, posW, posH, this);
                }
            }
        }
        else{
            g.setColor(Color.WHITE);
            g.drawString("Immagine Mappa non trovata!", 50, 50);
        }
    } 
}
