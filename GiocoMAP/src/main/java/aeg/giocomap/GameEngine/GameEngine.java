/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MappaPanel;
import aeg.giocomap.Model.Game_base_model;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.TitleScreen;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 *
 * @author emanu
 */
public class GameEngine {
    //Sfrutto il controller per far dialogare la logica di base alla logica visiva
    private Game_base_model model;
    private MainFrame frame;
    private TitleScreen title_screen;
    private MusicPlayer music_player;
    
    // Variabili Mappa
    private boolean isDialogoActive = false;
    private boolean isMapOpen = false;
    private MappaPanel mappa_panel;
    private JPanel scenario_precedente;
    private boolean possiedeMappa = false;
    
    // Costruttore
    public GameEngine(Game_base_model model, MainFrame frame){
        // Salvo model e frame del main qui
        this.model = model;
        this.frame = frame;
        this.music_player = new MusicPlayer();
        
        // Inizializzare mappa
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
            avviaGioco();
        });
        
        // Record viene cliccato
        title_screen.addRecordListener(e ->{
            model.Record();
            music_player.stopMusic();
            Statistiche();
        });
    }
    
    private void avviaGioco(){
        /* BUBUBUBUBU Creo scenario finto per TEST
        mockGamePanel = new JPanel();
        mockGamePanel.setBackground(Color.DARK_GRAY);
        
        // Aggancio il KeyBinding
        scenario_precedente = mockGamePanel;
        frame.mostraPannello(mockGamePanel);
        scenario_precedente = mockGamePanel;
        */
    }
    
    // Integrazione e possibili feedback dei dialoghi
    public void setDialogueActive(boolean active){
        this.isDialogoActive = active;
    }
    
    // David consegna la mappa POSSIBILE METODO
    public void setPossiedeMappa(boolean possiede){
        this.possiedeMappa = possiede;
        if(possiede) System.out.println("DEBUG: Il giocatore ha ottenuto la mappa");
    }
    
    private void Statistiche(){
        // Da fare la lista con i record
    }

    private void impostaKeyBindingMappa() {
        // Cambiamo il livello più alto datoc he sostituisce tutta la visualizzazione dello schermo momentaneamente
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); //Mantiene sempre il focus attivo sul possibile input da tastiera
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "toggle_mappa"); // Quando legge M senza nessun CTRL associato lancia il toggle_mappa
        
        // Chiamo la funzione toggleMappa associandola al segnale arrivato precedentemente
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
                frame.mostraPannello(scenario_precedente); //ricarico lo scenario che era prima in efidenza
            }
            isMapOpen = false; //chiudo la mappa
            System.out.println("DEBUG: Mappa Chiusa");
        }
        
    }
}
