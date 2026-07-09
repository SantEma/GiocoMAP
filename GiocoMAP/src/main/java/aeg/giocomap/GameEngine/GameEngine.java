package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MappaPanel;
import aeg.giocomap.View.InventarioPanel;
import aeg.giocomap.View.TitleScreen;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.GameScreen;
import aeg.giocomap.View.TitoliDiCoda;
import aeg.giocomap.View.DialogueScreen;
import aeg.giocomap.View.LetteraScreen;

import aeg.giocomap.Model.Storage.*;
import aeg.giocomap.Model.Giocatore.Inventario;
import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.Model.Giocatore.Giocatore;
import aeg.giocomap.Model.Enigmi.Enigma;

import aeg.giocomap.Util.JsonLoader;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

public class GameEngine {

    // variabili per immagazzinare i dialoghi dei file JSON
    private JsonObject dbWallOfText;
    private JsonObject dbStoria;
    private JsonObject dbHint;
    
    // variabili gestione dei dialoghi
    private DialogueScreen schermata_dialogo_corrente;
    private Personaggio npcCorrente;
    private ImageIcon spriteNpcAttuale;
    private GameScreen scenaSfondoCorrente;
    private Runnable azione_post_dialogo;
    
    private final ModelDB db;
    private final ModelTXTOggetti txt;
    private final MainFrame frame;
    private final TitleScreen title_screen;
    private final SceneManager sceneManager;
    private final MusicPlayer music_player;
    private final Giocatore giocatore; 
    
    private boolean isDialogoActive = false;
    private boolean possiedeMappa = false;

    // Variabili per il punteggio inserite dal collega
    private long tempoInizioEnigma = 0;
    private int punteggioTotale = 0;
    private TimerEnigma timerEnigma;

    public GameEngine(MainFrame frame) {
        this.dbWallOfText = JsonLoader.caricaJson("/dialoghi/walloftext.json");
        this.dbStoria = JsonLoader.caricaJson("/dialoghi/dialoghi_storia.json");
        this.dbHint=JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");
                
        this.db = new ModelDB();
        this.txt = new ModelTXTOggetti();
        this.frame = frame;
       

        this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
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
        
        this.giocatore=new Giocatore("Eryndor");
        
        Oggetto tessutoIniziale_temp = txt.getOggettoDaCatalogo(1);
        if(tessutoIniziale_temp != null) this.giocatore.getInventario().aggiungiOggetto(tessutoIniziale_temp);
        
        sceneManager.registraScena("INVENTARIO", new InventarioPanel(this.giocatore.getInventario()));
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
            Statistiche(false);
        });
    }
    
    private void avviaGioco(boolean carica) {
        String[] salvataggio = db.LoadGame();

        if (carica) {
            if (salvataggio == null) {
                JOptionPane.showMessageDialog(frame,"Nessuna partita salvata trovata!","Attenzione",JOptionPane.WARNING_MESSAGE);
                return;
            }
            music_player.stopMusic();
            String stanza = salvataggio[0];
            String enigma = salvataggio[1];
            System.out.println("Carico partita dalla stanza: " + stanza);
            return;
        }
        
        // Estraggo lettera dal DB
        List<String> lettera=JsonLoader.estraiLista(dbWallOfText.getAsJsonObject("Lettera"),"lettera_iniziale");
        
        // Creiamo la schermata del menù
        JPanel giocoTest = new JPanel(new BorderLayout());
        giocoTest.setBackground(Color.BLACK);
        JLabel testo_test = new JLabel("Premi I per aprire inventario TEST");
        testo_test.setForeground(Color.RED);
        testo_test.setFont(new Font("Arial", Font.BOLD, 24));
        testo_test.setHorizontalAlignment(SwingConstants.CENTER);
        giocoTest.add(testo_test, BorderLayout.CENTER);
        sceneManager.registraScena("GIOCO_TEST", giocoTest);
        
        // Avvio la possibilità di usare il bottone
        LetteraScreen schermata_lettera=new LetteraScreen(lettera,()->{
            System.out.println("TEST: Lettera finita, gioco START");
            
            // Lettera finita alla prossima scena sblocca l'inventario
            giocatore.setPossiedeInventario(true);
            // Qui passo la variabile della piazza centrale sceneManager.mostraScena
            // e faccio vedere il retro della lettera con l'enigma sopra e inizia l'esplorazione
            //sceneManager.mostraScena("GIOCO_TEST"); TEST MOMENTANEI ANDRANNO SOSTITUITI CON LE VERE SCENE
        });
        
        // Mostro scena
        sceneManager.registraScena("LETTERA_INIZIALE", schermata_lettera);
        sceneManager.mostraScena("LETTERA_INIZIALE");
    }

    
    // Chiamato quando il giocatore vede un enigma
    public void iniziaEnigma() {
        timerEnigma = new TimerEnigma(() -> {
            System.out.println("DEBUG: Secondi: " + timerEnigma.getSecondi());
        });
        timerEnigma.avvia();
        System.out.println("DEBUG: Enigma iniziato");
    }

    // Chiamato quando l'enigma viene risolto correttamente
    public void enigmaRisolto(Enigma enigma) {
        if (timerEnigma != null) timerEnigma.ferma();

        int secondi = timerEnigma != null ? timerEnigma.getSecondi() : 0;
        int punti = calcolaPunti(secondi);
        punteggioTotale += punti;

        
        if (enigma.getReward() != null) {
            inventario_p.aggiungiOggetto(enigma.getReward());
            System.out.println("DEBUG: Reward aggiunto: " + enigma.getReward().getNomeOggetto());
        }

        System.out.println("DEBUG: " + secondi + "s , " + punti + " punti, totale: " + punteggioTotale);
    }
    
    //Calcola il punteggio in base al tempo impiegato
    private int calcolaPunti(int secondi) {
        int fascia;
        if (secondi <= 100)      fascia = 1;
        else if (secondi <= 150) fascia = 2;
        else if (secondi <= 220) fascia = 3;
        else if (secondi <= 380) fascia = 4;
        else                     fascia = 5;

        return switch (fascia) {
            case 1 -> 1000;
            case 2 -> 700;
            case 3 -> 500;
            case 4 -> 300;
            default -> 100;
        };
    }

   

    private void Statistiche(boolean fineGioco) {
        String nome = "";
        int punteggio = 0;

        if (fineGioco) {
            punteggio = punteggioTotale;
            
            while (nome == null || nome.trim().isEmpty()) {
                nome = JOptionPane.showInputDialog(frame,"Inserisci il tuo nome per salvare il punteggio:","Fine gioco!",JOptionPane.PLAIN_MESSAGE);
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        frame,"Devi inserire un nome per continuare!","Attenzione",JOptionPane.WARNING_MESSAGE);
                }
            }
            giocatore.setNomePlayer(nome.trim());
            db.salvaSeNecessario(giocatore.getNomePlayer(), punteggio);
        }

        // Presuppone che ModelDB abbia il metodo getRecords
        List<String[]> records = db.getRecords(); 
        
        // Passiamo il nome del giocatore
        String nome_passato = (giocatore != null && giocatore.getNomePlayer()!= null ? giocatore.getNomePlayer():"");
        
        TitoliDiCoda titoli = new TitoliDiCoda(records, punteggio, nome != null ? nome.trim() : "");

        titoli.addIndietroListener(e -> {
            music_player.playMusic(MusicPlayer.TITLE_SCREEN_MUSIC);
            sceneManager.mostraScena("MENU_PRINCIPALE");
        });

        sceneManager.registraScena("TITOLI_CODA", titoli);
        sceneManager.mostraScena("TITOLI_CODA");
    }

    
    public void setDialogueActive(boolean active) {
        this.isDialogoActive = active;
    }

    public void setPossiedeMappa(boolean possiede) {
        this.possiedeMappa = possiede;
        if (possiede) System.out.println("DEBUG: Il giocatore ha ottenuto la mappa");
    }

   
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
        if (sceneManager.isOpenInventario()) sceneManager.ChiudiInventario();
        if (!sceneManager.isMapOpen()) sceneManager.ApriMappa();
        else sceneManager.ChiudiMappa();
    }

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
        if (!giocatore.isPossiedeInventario()){
            System.out.println("DEBUG: Inventario non ancora disponibile");
            return;
        }
        if (sceneManager.isMapOpen()) sceneManager.ChiudiMappa();
        if (!sceneManager.isOpenInventario()) sceneManager.ApriInventario();
        else sceneManager.ChiudiInventario();
    }

    public void ExitGame() {
        System.out.println("WARNING: Stiamo uscendo dal gioco");
        db.chiudiConnessione();
        System.exit(0);
    }
}