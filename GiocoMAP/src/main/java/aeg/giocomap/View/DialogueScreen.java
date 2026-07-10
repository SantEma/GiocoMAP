/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author Andrea
 */
public class DialogueScreen extends JLayeredPane {
    private GameScreen scena_stanza;
    private JPanel panelSpritePG;
    private Image imageSprite;
    private JPanel boxDialogo;
    private JTextArea area_text;
    private JButton btnAvanti;
    
    public DialogueScreen(GameScreen scena_stanza,Runnable azioneAvanti){
        this.scena_stanza=scena_stanza;
        setLayout(null);
        
        // Livello di sfondo
        this.add(scena_stanza,JLayeredPane.DEFAULT_LAYER);
        
        // Livello di personaggio
        panelSpritePG = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imageSprite != null) {
                    int imgW = imageSprite.getWidth(null);
                    int imgH = imageSprite.getHeight(null);
                    int compW = getWidth();
                    int compH = getHeight();

                    if (imgW > 0 && imgH > 0 && compW > 0 && compH > 0) {
                        double scaleX = (double) compW / imgW;
                        double scaleY = (double) compH / imgH;
                        double scale = Math.min(scaleX, scaleY);

                        int drawW = (int) (imgW * scale);
                        int drawH = (int) (imgH * scale);

                        // Allineato a sinistra e in basso
                        int x = 0;
                        int y = compH - drawH;

                        g.drawImage(imageSprite, x, y, drawW, drawH, this);
                    }
                }
            }
        };
        panelSpritePG.setOpaque(false);
        this.add(panelSpritePG, JLayeredPane.PALETTE_LAYER);
        
        // Livello della BoxDialogo
        boxDialogo = new JPanel(new BorderLayout());
        boxDialogo.setBackground(new Color(0,0,0,200));
        boxDialogo.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
        
        // Area di testo dove andranno i dialoghi
        area_text = new JTextArea();
        area_text.setEditable(false);
        area_text.setLineWrap(true);
        area_text.setWrapStyleWord(true);
        area_text.setFont(new Font("Book Antiqua",Font.BOLD,20));
        area_text.setOpaque(false);
        area_text.setForeground(Color.WHITE);
        area_text.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        boxDialogo.add(area_text,BorderLayout.CENTER); // Dialogo centrato
        
        //(DA TESTARE) il bottone richiamato
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setOpaque(false);
        southPanel.add(btnAvanti=new BottoneAvanti(e -> azioneAvanti.run()));
        boxDialogo.add(southPanel,BorderLayout.SOUTH); // Pannello con il dialogo dentro sotto
        
        // Tutto ciò che abbiamo inserito va messo tutto nel gestore dei livelli di Layout
        this.add(boxDialogo,JLayeredPane.MODAL_LAYER);
        
    }

    // Metodo per fare la "cornice morbida"
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        if (scena_stanza != null) {
            // Diciamo alla GameScreen di occupare tutto lo spazio
            scena_stanza.setBounds(0, 0, width, height);
        }

        if (boxDialogo != null) {
            // Posizioniamo il box in basso
            int boxH = (int) (height * 0.28);
            int boxW = (int) (width * 0.85);
            boxDialogo.setBounds((width - boxW) / 2, height - boxH - 30, boxW, boxH);
        }

        if (panelSpritePG != null) {
            // Posizioniamo lo sprite in basso a sinistra
            int spriteW = (int) (width * 0.40);
            int spriteH = (int) (height * 0.70);
            int boxH = (int) (height * 0.28);
            
            // Posizione X a sinistra, Y calcolata in modo da sovrapporsi poco sotto o pari al boxDialogo
            int xPos = (int) (width * 0.05);
            int yPos = height - boxH - spriteH + 30;
            panelSpritePG.setBounds(xPos, yPos, spriteW, spriteH);
        }
    }
    
    // Metodo per aggiornare i testi seguenti
    public void aggiornaSchermata(String nome,String battuta, ImageIcon sprite){
        if(nome!=null && !nome.equals("Fantoccio"))
            area_text.setText(nome+ ":\n"+ battuta);
        else area_text.setText(battuta);
        
        if (sprite != null) {
            this.imageSprite = sprite.getImage();
        } else {
            this.imageSprite = null;
        }
        if (panelSpritePG != null) {
            panelSpritePG.repaint();
        }
    }
}
