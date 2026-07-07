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
import aeg.giocomap.View.TitoliDiCoda;

import aeg.giocomap.Model.Storage.*;
import aeg.giocomap.Model.Giocatore.Inventario;
import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Util.JsonLoader;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

public class GameEngine {

    private final ModelDB db;
    private final ModelTXTOggetti txt;
    private final MainFrame frame;
    private final TitleScreen title_screen;
    private final SceneManager sceneManager;
    private final MusicPlayer music_player;
    private Inventario<Oggetto> inventario_p;
    
    private boolean isDialogoActive = false;
    private boolean possiedeMappa = false;
    private boolean possiedeInventario = false;

    // Variabili per il punteggio inserite dal collega
    private long tempoInizioEnigma = 0;
    private int punteggioTotale = 0;

    public GameEngine(MainFrame frame) {
        this.db = new ModelDB();
        this.txt = new ModelTXTOggetti();
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
        sceneManager.registraScena("MENU_PRINCIPALE", title_screen);
        
        sceneManager.registraScena("MAPPA", new MappaPanel());
        impostaKeyBindingMappa();
        
        this.inventario_p = new Inventario<>();
        // Eryndor ha già il tessuto con se da inizio gioco
        Oggetto tessutoIniziale_temp = txt.getOggettoDaCatalogo(1);
        if(tessutoIniziale_temp != null) this.inventario_p.aggiungiOggetto(tessutoIniziale_temp);
        
        sceneManager.registraScena("INVENTARIO", new InventarioPanel(inventario_p));
        impostaKeyBindingInventario();
        
        TitleScreenImp();
        sceneManager.mostraScena("MENU_PRINCIPALE");
    }

    private void TitleScreenImp() {
        title_screen.addNPListener(e -> {
            db.NewStart();
            music_player.stopMusic();
            avviaGioco(false);
        });

        title_screen.addCPListener(e -> {
            music_player.stopMusic();
            avviaGioco(true);
        });

        title_screen.addRecordListener(e -> {
            music_player.stopMusic();
            Statistiche(false); // Modificato per supportare il nuovo metodo del collega
        });
    }

    private List<String> leggiLetteraIniziale() {
        JsonObject root = JsonLoader.caricaJson("/dialoghi/walloftext.json");
        if (root == null) return new ArrayList<>();
        JsonObject lettera = root.getAsJsonObject("Lettera");
        return JsonLoader.estraiLista(lettera, "lettera_iniziale");
    }

    private void avviaGioco(boolean carica) {
        String[] salvataggio = db.LoadGame();

        if (carica) {
            if (salvataggio == null) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Nessuna partita salvata trovata!",
                    "Attenzione",
                    JOptionPane.WARNING_MESSAGE
                );
                return;  
            }
            music_player.stopMusic();
            
            String stanza = salvataggio[0];
            String enigma = salvataggio[1];
            System.out.println("Carico partita dalla stanza: " + stanza);
            return;
        }

        List<String> righe = leggiLetteraIniziale();
        
        JPanel giocoTest = new JPanel(new BorderLayout());
        giocoTest.setBackground(Color.BLACK);
        JLabel testo_test = new JLabel("Premi I per aprire inventario TEST");
        testo_test.setForeground(Color.red);
        testo_test.setFont(new Font("Arial", Font.BOLD, 24));
        giocoTest.add(testo_test, BorderLayout.CENTER);
        
        sceneManager.registraScena("GIOCO_TEST", giocoTest);
        
        GameScreen game_screen = new GameScreen(righe, () -> {
            System.out.println("DEBUG: Sigillo a schermo cliccato");
            possiedeInventario = true;
            
            // Per testare i titoli di coda a fine gioco potrai chiamare Statistiche(true) al posto di questa scena
            sceneManager.mostraScena("GIOCO_TEST");
        });
        
        sceneManager.registraScena("LETTERA_INIZIALE", game_screen);
        sceneManager.mostraScena("LETTERA_INIZIALE");
    }

    // --- METODI PUNTEGGIO DEL COLLEGA ---
    
    public void iniziaEnigma() {
        tempoInizioEnigma = System.currentTimeMillis();
        System.out.println("DEBUG: Enigma iniziato");
    }

    public void risolviEnigma() {
        if (tempoInizioEnigma == 0) return;
        int punti = calcolaPunti(tempoInizioEnigma);
        punteggioTotale += punti;
        tempoInizioEnigma = 0;
        System.out.println("DEBUG: Enigma risolto → " + punti + " punti, totale: " + punteggioTotale);
    }

    private int calcolaPunti(long inizioMs) {
        int secondi = (int)((System.currentTimeMillis() - inizioMs) / 1000);
        if (secondi <= 60)  return 1000;
        if (secondi <= 90)  return 800;
        if (secondi <= 120) return 600;
        if (secondi <= 300) return 400;
        return 200;
    }
    
    // --- FINE METODI PUNTEGGIO ---

    public void setDialogueActive(boolean active) {
        this.isDialogoActive = active;
    }

    public void setPossiedeMappa(boolean possiede) {
        this.possiedeMappa = possiede;
        if (possiede) System.out.println("DEBUG: Il giocatore ha ottenuto la mappa");
    }

    // --- METODO STATISTICHE E FINE GIOCO DEL COLLEGA ADATTATO A SCENEMANAGER ---
    private void Statistiche(boolean fineGioco) {
        String nome = "";
        int punteggio = 0;

        if (fineGioco) {
            while (nome == null || nome.trim().isEmpty()) {
                nome = JOptionPane.showInputDialog(
                    frame,
                    "Inserisci il tuo nome per salvare il punteggio:",
                    "Fine gioco!",
                    JOptionPane.PLAIN_MESSAGE
                );
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "Devi inserire un nome per continuare!",
                        "Attenzione",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            }
            punteggio = punteggioTotale;
            // Presuppone che ModelDB abbia il metodo salvaSeNecessario
            db.salvaSeNecessario(nome.trim(), punteggio); 
        }

        // Presuppone che ModelDB abbia il metodo getRecords
        List<String[]> records = db.getRecords(); 
        TitoliDiCoda titoli = new TitoliDiCoda(records, punteggio, nome != null ? nome.trim() : "");

        titoli.addIndietroListener(e -> {
            music_player.playMusic(MusicPlayer.TITLE_SCREEN_MUSIC);
            sceneManager.mostraScena("MENU_PRINCIPALE");
        });

        sceneManager.registraScena("TITOLI_CODA", titoli);
        sceneManager.mostraScena("TITOLI_CODA");
    }

    // KeyBinding e Apertura/Chiusura del pannello Mappa
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
        if (sceneManager.isOpenInventario()){
            sceneManager.ChiudiInventario();
        }
        
        if (!sceneManager.isMapOpen()) sceneManager.ApriMappa();
        else sceneManager.ChiudiMappa();
    }

    // KeyBinding e Apertura/Chiusura del pannello Inventario
    private void impostaKeyBindingInventario() {
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "toggle_inventario");
        
        am.put("toggle_inventario", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleInventario();
            }
        });
    }
    
    private void toggleInventario() {
        if (isDialogoActive) {
            System.out.println("DEBUG: Testo in corso, inventario non apribile");
            return;
        }
        if (!possiedeInventario){
            System.out.println("DEBUG: Inventario non ancora disponibile");
            return;
        }
        if (sceneManager.isMapOpen()) {
            sceneManager.ChiudiMappa();
        }

        if (!sceneManager.isOpenInventario()) sceneManager.ApriInventario();
        else sceneManager.ChiudiInventario();
    }
    
    public void ExitGame(){
        System.out.println("WARNING: Stiamo uscendo dal gioco");
        db.chiudiConnessione(); 
        System.exit(0);
    }
}