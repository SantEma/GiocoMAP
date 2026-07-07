/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MappaPanel;
import aeg.giocomap.View.InventarioPanel;
import aeg.giocomap.View.TitleScreen;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.GameScreen;

import aeg.giocomap.Model.Game_base_model;
import aeg.giocomap.Model.Inventario;
import aeg.giocomap.Model.Oggetto;
import aeg.giocomap.Util.JsonLoader;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import javax.swing.*;

public class GameEngine {

    private final Game_base_model model;
    private final MainFrame frame;
    private final TitleScreen title_screen;
    private final SceneManager sceneManager;
    private final MusicPlayer music_player;
    private Inventario<Oggetto> inventario_p;
    
    private boolean isDialogoActive = false;
    private boolean possiedeMappa = false;

    public GameEngine(Game_base_model model, MainFrame frame) {
        this.model = model;
        this.frame = frame;
        this.frame.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(java.awt.event.WindowEvent e){
                ExitGame();
            }
        });
        
        this.music_player = new MusicPlayer();
        music_player.playMusic(MusicPlayer.TITLE_SCREEN_MUSIC);
        
        this.sceneManager = new SceneManager(frame);
        this.title_screen = new TitleScreen();
        sceneManager.registraScena("MENU_PRINCIPALE",title_screen);
        
        sceneManager.registraScena("MAPPA", new MappaPanel());
        impostaKeyBindingMappa();
        
        //Eryndor ha già il tessuto con se da inizio gioco
        Oggetto tessutoIniziale_temp = model.getOggettoDaCatalogo(1);
        if(tessutoIniziale_temp!=null) this.inventario_p.aggiungiOggetto(tessutoIniziale_temp); //Inserisco nella lista degli oggetti il primo oggetto
        
        sceneManager.registraScena("INVENTARIO",new InventarioPanel(inventario_p));
        impostaKeyBlindingInventario();
        
        TitleScreenImp();
        sceneManager.mostraScena("MENU_PRINCIPALE");
    }

    private void TitleScreenImp() {
        title_screen.addNPListener(e -> {
            model.NewStart();
            music_player.stopMusic();
            avviaGioco(false);
        });

        title_screen.addCPListener(e -> {
            avviaGioco(true);
        });

        title_screen.addRecordListener(e -> {
            music_player.stopMusic();
            Statistiche();
        });
    }

    private List<String> leggiLetteraIniziale() {
        JsonObject root = JsonLoader.caricaJson("/dialoghi/walloftext.json");
        if (root == null) return new ArrayList<>();
        JsonObject lettera = root.getAsJsonObject("Lettera");
        return JsonLoader.estraiLista(lettera, "lettera_iniziale");
    }

    private void avviaGioco(boolean carica) {
    String[] salvataggio = model.LoadGame();

    if (carica) {
        // ha premuto carica partita
        if (salvataggio == null) {
            JOptionPane.showMessageDialog(
                frame,
                "Nessuna partita salvata trovata!",
                "Attenzione",
                JOptionPane.WARNING_MESSAGE
            );
            return;  // torna al titolo senza fare nulla
        }
        // partita trovata → carica
        music_player.stopMusic();
        
        String stanza = salvataggio[0];
        String enigma = salvataggio[1];
        System.out.println("Carico partita dalla stanza: " + stanza);
        // qui in futuro carichi la scena giusta
        return;
    }

    // ha premuto nuova partita → mostra sempre la lettera
    List<String> righe = leggiLetteraIniziale();
    GameScreen game_screen = new GameScreen(righe);
    sceneManager.registraScena("LETTERA_INIZIALE",game_screen);
    sceneManager.mostraScena("LETTERA_INIZIALE");
}

    public void setDialogueActive(boolean active) {
        this.isDialogoActive = active;
    }

    public void setPossiedeMappa(boolean possiede) {
        this.possiedeMappa = possiede;
        if (possiede) System.out.println("DEBUG: Il giocatore ha ottenuto la mappa");
    }

    private void Statistiche() {
        // Da fare
    }
    
    // KeyBinding e Apertura/Chiusura del pannello
    private void impostaKeyBindingMappa() {
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "toggle_mappa");

        am.put("toggle_mappa", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleMappa();
            }
        });
    }

    private void toggleMappa() {
        if (!possiedeMappa) {
            System.out.println("DEBUG: Il giocatore non possiede ancora la Mappa");
            return;
        }

        if (isDialogoActive) {
            System.out.println("DEBUG: Testo in corso mappa non apribile");
            return;
        }
        
        // Verifico che lo scenario precedente non sia l'inventario se no non tornerò mai alla scena di gioco
        if(sceneManager.isOpenInventario()){
            sceneManager.ChiudiInventario();
            System.out.println("TEST: Inventario chiuso brutalmente");
        }
        
        if(!sceneManager.isMapOpen()) sceneManager.ApriMappa();
        else sceneManager.ChiudiMappa();
    }

    private void impostaKeyBlindingInventario() {
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "toogle_inventario");
        
        // passa il segnale ricevuto inventario al posto di mappa
        am.put("toogle_inventario", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleInventario();
            }
        });
    }
    
    // Come per mappa eseguo l'operazione dell'inventario
    private void toggleInventario() {
        if (isDialogoActive) {
            System.out.println("DEBUG: Testo in corso, inventario non apribile");
            return;
        }

        if (sceneManager.isMapOpen()) {
            System.out.println("DEBUG: Chiusura forzata mappa per aprire l'inventario");
            sceneManager.ChiudiMappa();
        }

        if (!sceneManager.isOpenInventario()) sceneManager.ApriInventario();
        else sceneManager.ChiudiInventario();
    }
    
    
    // Chiudiamo in modo pulito il gioco
    public void ExitGame(){
        System.out.println("WARNING: Stiamo uscendo dal gioco");
        model.chiudiConnessione(); // chiudo il DB se è aperto
        System.exit(0);
    }
}