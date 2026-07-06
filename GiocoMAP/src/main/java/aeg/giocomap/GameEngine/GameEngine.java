/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MappaPanel;
import aeg.giocomap.Model.Game_base_model;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.TitleScreen;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import com.google.gson.JsonObject;
import aeg.giocomap.Util.JsonLoader;
import aeg.giocomap.View.GameScreen;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author emanu
 */
public class GameEngine {
    //Sfrutto il controller per far dialogare la logica di base alla logica visiva
    private final Game_base_model model;
    private final MainFrame frame;
    private final TitleScreen title_screen;
    private final MusicPlayer music_player;
    
    // Variabili Mappa
    private boolean isDialogoActive = false;
    private boolean isMapOpen = false;
    private final MappaPanel mappa_panel;
    private JPanel scenario_precedente;
    private boolean possiedeMappa = false;
    
    // Costruttore
    public GameEngine(Game_base_model model, MainFrame frame){
        // Salvo model e frame del main qui
        this.model = model;
        this.frame = frame;
        this.music_player = new MusicPlayer();
        
        // Inizializzare la mappa
        this.mappa_panel = new MappaPanel();
        impostaKeyBindingMappa();
        
        // Carico la schermata del titolo
        this.title_screen = new TitleScreen();
        music_player.playMusic(0);
        TitleScreenImp();
        this.frame.mostraPannello(title_screen); //mostro lo schermo all'utente
    }
    
    // Metodo per aggangicare il listener ai bottoni
    private void TitleScreenImp(){
        // Nuova partita viene cliccato
        title_screen.addNPListener(e ->{
            // Lambda Expression
                model.NewStart();
                music_player.stopMusic(); // Cambiando scena stoppiamo la musica
                avviaGioco(); // Passiamo alla view del gioco
        });

        
        
        // Carica partita viene cliccato
        title_screen.addCPListener(e ->{
            model.LoadGame();
            music_player.stopMusic();
            caricaGioco();
        });
        
        // Record viene cliccato
        title_screen.addRecordListener(e ->{
            model.Record();
            music_player.stopMusic();
            Statistiche();
        });
    }
    
    // DOPO
    private List<String> leggiLetteraIniziale() {
    JsonObject root = JsonLoader.caricaJson("/dialoghi/walloftext.json");
    if (root == null) return new ArrayList<>();
    JsonObject lettera = root.getAsJsonObject("Lettera");
    return JsonLoader.estraiLista(lettera, "lettera_iniziale");
}

    private void avviaGioco() {
      
    /* Aggancio il KeyBinding
       scenario_precedente = mockGamePanel;
       frame.mostraPannello(mockGamePanel);
       scenario_precedente = mockGamePane;
       */
    List<String> righe = leggiLetteraIniziale();
    GameScreen game_screen = new GameScreen(righe);
    frame.mostraPannello(game_screen);
    }
  // Integrazione dei dialoghi e verifica di quando essi sono attivi
    public void setDialogueActive(boolean active){
        this.isDialogoActive = active;
    }
    
    // David consegna la mappa (POSSIBILE METODO)
    public void setPossiedeMappa(boolean possiede){
        this.possiedeMappa = possiede;
        if(possiede) System.out.println("DEBUG: Il giocatore ha ottenuto la mappa");
    }
   
    
    
    private void Statistiche(){
        // Da fare la lista con i record (hall of fame)
    }

    private void impostaKeyBindingMappa() {
        // Cambio il livello più alto, sostituendo tutta la visualizzazione dello schermo momentaneamente
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); //Mantiene sempre il focus attivo sul possibile input da tastiera
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "toggle_mappa"); // Quando legge M senza nessun CTRL associato lancia il toggle_mappa
        
        // Chiamo la funzione toggleMappa associandola al segnale arrivato precedentemente ("toggle_mappa")
        am.put("toggle_mappa", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                toggleMappa();
            }
        });
    }
    
    private void toggleMappa(){
        if(!possiedeMappa){
            System.out.println("DEBUG: Il giocatore non possiede ancora la Mappa");
            return;
        }
        
        if(isDialogoActive){
            System.out.println("DEBUG: Testo in corso mappa non apribile");
            return;
        }
        
        if (!isMapOpen) {
            if (frame.getContentPane().getComponentCount() > 0) {
                scenario_precedente = (JPanel) frame.getContentPane().getComponent(0); //salvo lo scenario attuale in scenario precedente
            }
            frame.mostraPannello(mappa_panel);
            isMapOpen = true;
            System.out.println("DEBUG: Mappa Aperta");
        } else {
            if (scenario_precedente != null) {
                frame.mostraPannello(scenario_precedente); //ricarico lo scenario che era prima in evidenza
            }
            isMapOpen = false; //chiudo la mappa
            System.out.println("DEBUG: Mappa Chiusa");
        }
        
    }

    private void caricaGioco(){
        
    }
}
