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

/**
 *
 * @author emanu
 */
public class GameEngine {
    //Sfrutto il controller per far dialogare la logica di base alla logica visiva
    private Game_base_model model;
    private MainFrame frame;
    private TitleScreen title_screen;
    
    // Costruttore
    public GameEngine(Game_base_model model, MainFrame frame){
        // Salvo model e frame del main qui
        this.model = model;
        this.frame = frame;
        
        // Carico la schermata del titolo
        this.title_screen = new TitleScreen();
        TitleScreenImp();
        this.frame.mostraPannello(title_screen); //mostro lo schermo all'utente
    }
    
    // Metodo per aggangicare il listener ai bottoni
    private void TitleScreenImp(){
        // Nuova partita viene cliccato
        title_screen.addNPListener(e ->{
            // Lambda Expression
                model.NewStart();
                avviaGioco(); // Passiamo alla view del gioco
        });
        
        // Carica partita viene cliccato
        title_screen.addCPListener(e ->{
            model.LoadGame();
            avviaGioco();
        });
        
        // Record viene cliccato
        title_screen.addRecordListener(e ->{
            model.Record();
            Statistiche();
        });
    }
    
    private void avviaGioco(){
        // BUBUBUBUBU
    }
    
    private void Statistiche(){
        // Da fare la lista con i record
    }
}
