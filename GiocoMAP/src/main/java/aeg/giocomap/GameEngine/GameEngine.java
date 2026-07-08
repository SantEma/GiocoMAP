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
import aeg.giocomap.Model.Enigmi.Enigma;
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
    private Inventario<Oggetto> inventario_p; //ToDo: appena si mergia, questo deve andare su Giocatore
    private boolean isDialogoActive = false;
    private boolean possiedeMappa = false;
    private boolean possiedeInventario = false; //ToDo: appena si mergia, questo deve andare su Giocatore
    private int punteggioTotale = 0;
    private TimerEnigma timerEnigma;

    public GameEngine(MainFrame frame) {
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

        this.inventario_p = new Inventario<>();
        Oggetto tessutoIniziale_temp = txt.getOggettoDaCatalogo(1);
        if (tessutoIniziale_temp != null){
            this.inventario_p.aggiungiOggetto(tessutoIniziale_temp);
        }
        
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
            Statistiche(false);
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
                JOptionPane.showMessageDialog(frame,"Nessuna partita salvata trovata!","Attenzione",JOptionPane.WARNING_MESSAGE);
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
        testo_test.setForeground(Color.RED);
        testo_test.setFont(new Font("Arial", Font.BOLD, 24));
        giocoTest.add(testo_test, BorderLayout.CENTER);
        sceneManager.registraScena("GIOCO_TEST", giocoTest);

        GameScreen game_screen = new GameScreen(righe, () -> {
            System.out.println("DEBUG: Sigillo cliccato");
            possiedeInventario = true;
        });

        sceneManager.registraScena("LETTERA_INIZIALE", game_screen);
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

        switch (fascia) {
            case 1: return 1000;
            case 2: return 700;
            case 3: return 500;
            case 4: return 300;
            default: return 100;
        }
    }

   

    private void Statistiche(boolean fineGioco) {
        String nome = "";
        int punteggio = 0;

        if (fineGioco) {
            while (nome == null || nome.trim().isEmpty()) {
                nome = JOptionPane.showInputDialog(frame,"Inserisci il tuo nome per salvare il punteggio:","Fine gioco!",JOptionPane.PLAIN_MESSAGE);
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        frame,"Devi inserire un nome per continuare!","Attenzione",JOptionPane.WARNING_MESSAGE);
                }
            }
            punteggio = punteggioTotale;
            db.salvaSeNecessario(nome.trim(), punteggio);
        }

        List<String[]> records = db.getRecords();
        TitoliDiCoda titoli = new TitoliDiCoda(
            records, punteggio, nome != null ? nome.trim() : "");

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
        if (!possiedeInventario) {
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