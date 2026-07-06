/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

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
    
    // Costruttore
    public GameEngine(Game_base_model model, MainFrame frame){
        // Salvo model e frame del main qui
        this.model = model;
        this.frame = frame;
        this.music_player = new MusicPlayer();
        
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
    List<String> righe = leggiLetteraIniziale();
    GameScreen game_screen = new GameScreen(righe);
    frame.mostraPannello(game_screen);
}
    
    private void Statistiche(){
        // Da fare la lista con i record
    }

    private void caricaGioco(){
        
    }
}
