/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

import aeg.giocomap.View.GameScreen;
import aeg.giocomap.View.MainFrame;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrea
 */
public class SceneManager {
    // variabili scene
    private final MainFrame frame;
    private final Map<String, JComponent> sceneCache = new HashMap<>();
    
    private String scenaCorrente;
    private String scenaPrecedente;
    
    // varibili mappa
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
        if(ns!=null) {
            frame.mostraPannello(ns);
            scenaCorrente = nomeScena;
            aggiornaVisibilitaFrecce(nomeScena);
        }
        else System.err.println("ERROR: Scena "+nomeScena+" non registrata");
    }

    public String getScenaCorrente(){
        return scenaCorrente;
    }
    
    // Che elementi grafici ci sono nella scena
    public JComponent getScena(String nomeScena) {
        return sceneCache.get(nomeScena);
    }
    
    // In che zona della mappa stiamo, utile per salvarla
    public String getScenaCorrente() {
        return scenaCorrente;
    }
    
    private void aggiornaVisibilitaFrecce(String nomeScena) {
       if (nomeScena == null) return;
        // Nascondi le frecce se siamo in una grafica e non uno scenario
        if (nomeScena.equals("MENU_PRINCIPALE") || 
            nomeScena.equals("MAPPA") || 
            nomeScena.equals("INVENTARIO") || 
            nomeScena.equals("TITOLI_CODA") || 
            nomeScena.equals("LETTERA") || 
            nomeScena.equals("LETTERA_RETRO") || 
            nomeScena.equals("DIALOGO_CORRENTE") ||
            chatOpen) {
            frame.setFrecceVisibili(false);
        } else {
            frame.setFrecceVisibili(true);
        }
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
        if(mapOpen && scenaPrecedente != null){
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
            
            // Forza la rimozione del fumetto in sovraimpressione chiudendo
            ToolTipManager.sharedInstance().setEnabled(false);
            ToolTipManager.sharedInstance().setEnabled(true);
            
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
            scenaPrecedente = scenaCorrente;

            frame.mostraPannello(chat);
            chatOpen = true;
            frame.setFrecceVisibili(false);
            System.out.println("DEBUG: Chat aperta");
        } else {
            // torno alla scena precedente
            chatOpen = false;
            if (scenaPrecedente != null) {
                mostraScena(scenaPrecedente);
            }
            System.out.println("DEBUG: Chat chiusa");
        }
    }

    public boolean isChatOpen() {
        return chatOpen;
    }
    
    // Metodo helper per creare velocemente le GameScreen
    public GameScreen creaScenaBase(String imagePath, Map<double[], Runnable> zone) {
        BufferedImage sfondo = null;
        try {
            sfondo = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/" + imagePath));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo " + imagePath + ": " + e.getMessage());
        }
        return new GameScreen(sfondo, zone);
    }
}
