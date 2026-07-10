/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MainFrame;
import javax.swing.JComponent;
import java.util.HashMap;
import java.util.Map;
import aeg.giocomap.View.ChatPanel;

/**
 *
 * @author Andrea
 */
public class SceneManager {
    // variabili scene
    private final MainFrame frame;
    private final Map<String, JComponent> sceneCache = new HashMap<>();
    
    // varibili mappa
    private JComponent scenario_precedente;
    private boolean mapOpen = false;
    
    // variabili inventario
    private boolean inventarioOpen = false;
    
// variabili chat
    private boolean chatOpen = false;
    private JComponent chatPanel;

    // nome della scena mostrata al momento (serve per il salvataggio)
    private String scenaCorrente = "MENU_PRINCIPALE";
    // nome della scena "vera" sotto un overlay (mappa/inventario), per poterlo
    // ripristinare alla chiusura ed evitare di salvare "MAPPA"/"INVENTARIO"
    private String scenaPrecedenteNome = "MENU_PRINCIPALE";

    public SceneManager(MainFrame frame){
        this.frame = frame;
    }

    // carica scena all'avvio nella memoria
    public void registraScena(String nomeScena, JComponent pannello){
        sceneCache.put(nomeScena,pannello);
    }

    // cambia scena
    public void mostraScena(String nomeScena){
        JComponent ns = sceneCache.get(nomeScena);
        if(ns!=null){
            frame.mostraPannello(ns);
            scenaCorrente = nomeScena;
        }
        else System.err.println("ERROR: Scena "+nomeScena+" non registrata");
    }

    public String getScenaCorrente(){
        return scenaCorrente;
    }
    
    // Logiche della Mappa
    public void ApriMappa(){
        if(!mapOpen){
            // salvo il contenuto della scena precedente
            if (frame.getContentPane().getComponentCount() > 0)
                scenario_precedente = (JComponent) frame.getContentPane().getComponent(0);
            scenaPrecedenteNome = scenaCorrente;

            mostraScena("MAPPA");

            mapOpen=true;
            System.out.println("DEBUG: Mappa Aperta");
        }
    }

    public void ChiudiMappa(){
        if(mapOpen && scenario_precedente != null){
            //Rinserimento scena precedente
            frame.mostraPannello(scenario_precedente);
            scenaCorrente = scenaPrecedenteNome;
            mapOpen=false;
            System.out.println("DEBUG: Chisura Mappa");
        }
    }
    
    public boolean isMapOpen(){
        return mapOpen;
    }
    
    // Logiche dell'Inventario
    public void ApriInventario(){
        if(!inventarioOpen){
            // Salvo la scena precedente di gioco
            if(frame.getContentPane().getComponentCount()>0) {
                scenario_precedente=(JComponent) frame.getContentPane().getComponent(0);
            }
            scenaPrecedenteNome = scenaCorrente;

            // Recupero dalla cache l'inventario
            JComponent invP = sceneCache.get("INVENTARIO");
            
            // Casting dell'oggetto prima di mostrarlo ed eliminazione dei duplicati
            if(invP instanceof aeg.giocomap.View.InventarioPanel inventarioPanel)
                inventarioPanel.aggiornaVista();
           
            // Apro la scena a schermo
            mostraScena("INVENTARIO");
            inventarioOpen = true;
            System.out.println("DEBUG: Scena aperta - Inventario");
        }
    }
    
    public void ChiudiInventario(){
        if(inventarioOpen && scenario_precedente!=null){
            frame.mostraPannello(scenario_precedente);
            scenaCorrente = scenaPrecedenteNome;
            inventarioOpen = false;
            System.out.println("DEBUG: Chisura inventario");
        }
    }
    
    public boolean isOpenInventario(){
        return inventarioOpen;
    }
    // Logiche della Chat
    public void toggleChat(JComponent chat) {
        if (!chatOpen) {
            // salvo la scena precedente
            if (frame.getContentPane().getComponentCount() > 0)
                scenario_precedente = (JComponent) frame.getContentPane().getComponent(0);

            this.chatPanel = chat;
            frame.mostraPannello(chat);
            chatOpen = true;
            System.out.println("DEBUG: Chat aperta");
        } else {
            // torno alla scena precedente
            if (scenario_precedente != null)
                frame.mostraPannello(scenario_precedente);
            chatOpen = false;
            System.out.println("DEBUG: Chat chiusa");
        }
    }

    public boolean isChatOpen() {
        return chatOpen;
}
}
