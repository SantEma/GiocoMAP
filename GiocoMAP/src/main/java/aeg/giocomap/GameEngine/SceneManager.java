/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MainFrame;
import javax.swing.JComponent;
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
    
    // varibili mappa
    private JComponent scenario_precedente;
    private boolean mapOpen = false;
    
    // variabili inventario
    private boolean inventarioOpen = false;
    
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
        if(ns!=null) frame.mostraPannello(ns);
        else System.err.println("ERROR: Scena "+nomeScena+" non registrata");
    }
    
    // Logiche della Mappa
    public void ApriMappa(){
        if(!mapOpen){
            // salvo il contenuto della scena precedente
            if (frame.getContentPane().getComponentCount() > 0) 
                scenario_precedente = (JComponent) frame.getContentPane().getComponent(0);
            
            mostraScena("MAPPA");
            
            mapOpen=true;
            System.out.println("DEBUG: Mappa Aperta");
        }
    }
    
    public void ChiudiMappa(){
        if(mapOpen && scenario_precedente != null){
            //Rinserimento scena precedente
            frame.mostraPannello(scenario_precedente);
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
            inventarioOpen = false;
            System.out.println("DEBUG: Chisura inventario");
        }
    }
    
    public boolean isOpenInventario(){
        return inventarioOpen;
    }
}
