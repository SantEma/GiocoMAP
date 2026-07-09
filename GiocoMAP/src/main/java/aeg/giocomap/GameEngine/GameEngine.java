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
import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.Model.Giocatore.Giocatore;
import aeg.giocomap.Model.Enigmi.Enigma;

import aeg.giocomap.Util.JsonLoader;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GameEngine {

    // Variabili per immagazzinare i dialoghi dei file JSON già da subito
    private final JsonObject dbWallOfText;
    private final JsonObject dbStoria;
    private final JsonObject dbHint;
    
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
        
        // Estraggo lettera (dal database)
        List<String> lettera=JsonLoader.estraiLista(dbWallOfText.getAsJsonObject("Lettera"),"lettera_iniziale");
        
        // Estrazione testo enigma
        String enigmaText = dbWallOfText.getAsJsonObject("Schermo").get("Enigma_1_Lettera").getAsString();
        List<String> letteraRetro = Arrays.asList(enigmaText);

        // Estrazione dei 3 aiuti
        List<String> hints = JsonLoader.estraiLista(dbHint.getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");

        // Mappa per fare storage delle posizioni dei personaggi
        Map<double[], Runnable> zonePiazza = new HashMap<>();
        final GameScreen[] piazzaCentraleArr = new GameScreen[1];

        // Circa posizione NPC1
        zonePiazza.put(new double[]{0.3090, 0.6083, 0.08, 0.25}, () -> mostraDialogo(piazzaCentraleArr[0], "PIAZZA_CENTRALE", "Abitante 1", hints.get(0), null));
        // Circa posizione NPC2
        zonePiazza.put(new double[]{0.5349, 0.4436, 0.08, 0.25}, () -> mostraDialogo(piazzaCentraleArr[0], "PIAZZA_CENTRALE", "Abitante 2", hints.get(1), null));
        // Circa posizione NPC3
        zonePiazza.put(new double[]{0.6228, 0.6285, 0.08, 0.25}, () -> mostraDialogo(piazzaCentraleArr[0], "PIAZZA_CENTRALE", "Abitante 3", hints.get(2), null));

        BufferedImage sfondoPiazza = null;
        try {
            sfondoPiazza = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/PiazzaCentrale.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo: " + e.getMessage());
        }

        GameScreen piazzaCentrale = new GameScreen(sfondoPiazza, zonePiazza);
        
        piazzaCentraleArr[0] = piazzaCentrale;
        sceneManager.registraScena("PIAZZA_CENTRALE", piazzaCentrale);
        
        // Avvio la possibilità di usare il bottone per il retro della lettera
        LetteraScreen schermata_retro = new LetteraScreen(letteraRetro, () -> {
            System.out.println("TEST: Lettera retro finita, gioco START");
            
            // Lettera finita alla prossima scena sblocca l'inventario
            giocatore.setPossiedeInventario(true);
            
            sceneManager.mostraScena("PIAZZA_CENTRALE");
        });
        sceneManager.registraScena("LETTERA_RETRO", schermata_retro);

        // Lettera di default 
        LetteraScreen schermata_lettera = new LetteraScreen(lettera, () -> {
            System.out.println("TEST: Lettera finita, mostro retro");
            
            // Retro della lettera con l'enigma sopra
            sceneManager.mostraScena("LETTERA_RETRO");
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
            giocatore.getInventario().aggiungiOggetto(enigma.getReward());
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
        
        TitoliDiCoda titoli = new TitoliDiCoda(records, punteggio, nome_passato);

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

    private void mostraDialogo(GameScreen scenaSfondo, String idScenaSfondo, String nome, String battuta, ImageIcon sprite) {
        if (isDialogoActive) return;
        setDialogueActive(true);
        DialogueScreen ds = new DialogueScreen(scenaSfondo, () -> {
            setDialogueActive(false);
            sceneManager.mostraScena(idScenaSfondo);
        });
        ds.aggiornaSchermata(nome, battuta, sprite);
        sceneManager.registraScena("DIALOGO_CORRENTE", ds);
        sceneManager.mostraScena("DIALOGO_CORRENTE");
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
        if (!giocatore.isPossiedeMappa()) {
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