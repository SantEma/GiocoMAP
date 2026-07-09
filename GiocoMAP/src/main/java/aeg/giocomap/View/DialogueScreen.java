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
    private JLabel labelSpritePG;
    private JPanel boxDialogo;
    private JTextArea area_text;
    private JButton btnAvanti;
    
    public DialogueScreen(GameScreen scena_stanza,Runnable azioneAvanti){
        this.scena_stanza=scena_stanza;
        setLayout(null);
        
        // Livello di sfondo
        this.add(scena_stanza,JLayeredPane.DEFAULT_LAYER);
        
        // Livello di personaggio
        labelSpritePG = new JLabel();
        labelSpritePG.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(labelSpritePG, JLayeredPane.PALETTE_LAYER);
        
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

        if (labelSpritePG != null) {
            // Posizioniamo lo sprite
            int spriteW = (int) (width * 0.40);
            int spriteH = (int) (height * 0.70);
            // Evita il crash se boxDialogo è null (anche se in questo contesto non dovrebbe esserlo)
            int boxH = (int) (height * 0.28);
            labelSpritePG.setBounds((width - spriteW) / 2, (height - boxH - 30) - spriteH + 60, spriteW, spriteH);
        }
    }
    
    // Metodo per aggiornare i testi seguenti
    public void aggiornaSchermata(String nome,String battuta, ImageIcon sprite){
        if(nome!=null && !nome.equals("Fantoccio"))
            area_text.setText(nome+ ":\n"+ battuta);
        else area_text.setText(battuta);
        labelSpritePG.setIcon(sprite);
    }
}
