package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MappaPanel;
import aeg.giocomap.View.InventarioPanel;
import aeg.giocomap.View.TitleScreen;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.GameScreen;
import aeg.giocomap.View.DialogueScreen;

import aeg.giocomap.Network.GameNetwork;

import aeg.giocomap.Model.Storage.*;
import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Oggetti.Spada;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.Model.Giocatore.Giocatore;

import aeg.giocomap.Util.JsonLoader;
import com.google.gson.JsonObject;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

public class GameEngine {

    // Variabili per immagazzinare i dialoghi dei file JSON già da subito
    private final JsonObject dbWallOfText;
    private final JsonObject dbStoria;
    private final JsonObject dbHint;

    private boolean isDialogoActive = false;
    private String scenaDaSalvare = "MENU_PRINCIPALE";

    private final ModelDB db;
    private final ModelTXTOggetti txt;
    private final MainFrame frame;
    private final TitleScreen title_screen;
    private final SceneManager sceneManager;
    private final MusicPlayer music_player;
    private final Giocatore giocatore;

    private final GameStatistics statistics;
    private final GameNetwork network;
    private final ProgressioneStoria progression;

    public GameEngine(MainFrame frame) {
        this.dbWallOfText = JsonLoader.caricaJson("/dialoghi/walloftext.json");
        this.dbStoria = JsonLoader.caricaJson("/dialoghi/dialoghi_storia.json");
        this.dbHint = JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");

        this.db = new ModelDB();
        this.txt = new ModelTXTOggetti();
        this.frame = frame;

        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ExitGame();
            }
        });

        this.music_player = new MusicPlayer();
        music_player.playMusic(MusicPlayer.TITLE_SCREEN_MUSIC);

        this.sceneManager = new SceneManager(frame);
        this.title_screen = new TitleScreen();
        sceneManager.registraScena("MENU_PRINCIPALE", title_screen);

        sceneManager.registraScena("MAPPA", new MappaPanel(sceneManager));

        this.giocatore = new Giocatore("Eryndor");

        Oggetto tessutoIniziale_temp = txt.getOggettoDaCatalogo(1);
        if (tessutoIniziale_temp != null)
            this.giocatore.getInventario().aggiungiOggetto(tessutoIniziale_temp);

        sceneManager.registraScena("INVENTARIO", new InventarioPanel(this.giocatore.getInventario()));
        
        this.statistics = new GameStatistics(frame, giocatore, db, music_player, sceneManager);
        this.network = new GameNetwork(frame, sceneManager);
        this.progression = new ProgressioneStoria(this);

        impostaKeyBindingMappa();
        impostaKeyBindingChat();
        this.frame.setChatListener(e -> chatButtonClick());
        impostaKeyBindingInventario();
        impostaKeyBindingEsc();

        registraMenuPausa();
        registraComandi();

        TitleScreenImp();
        progression.impostaFrecceLogica();
        sceneManager.mostraScena("MENU_PRINCIPALE");
    }

        public JsonObject getDbWallOfText() { 
        return dbWallOfText; 
    }
    
    public JsonObject getDbStoria() { 
        return dbStoria; 
    }
    
    public JsonObject getDbHint() { 
        return dbHint; 
    }
    
    public ModelTXTOggetti getTxt() {
        return txt;
    }
    
    public SceneManager getSceneManager() { 
        return sceneManager; 
    }
    
    public Giocatore getGiocatore() { 
        return giocatore; 
    }
    
    public MainFrame getFrame() { 
        return frame; 
    }
    
    public GameStatistics getStatistics() { 
        return statistics; 
    }
    
    public GameNetwork getNetwork() { 
        return network; 
    }
    
    public ProgressioneStoria getProgression() { 
        return progression; 
    }
    
    private void TitleScreenImp() {
        title_screen.addNPListener(e -> {
            db.NewStart();
            music_player.stopMusic();
            avviaGioco(false);
        });

        title_screen.addCPListener(e -> {
            avviaGioco(true);
        });

        title_screen.addRecordListener(e -> {
            music_player.stopMusic();
            statistics.Statistiche(false);
        });
       
        title_screen.addConnettiListener(e -> {
            String ip = JOptionPane.showInputDialog(
                frame,
                "Inserisci l'IP dell'host:",
                "Partecipa alla Chat",
                JOptionPane.PLAIN_MESSAGE
            );
            if (ip != null && !ip.trim().isEmpty()) {
                network.connettiComeClient(ip.trim());
            }
        });
    }

    private void avviaGioco(boolean carica) {
        String[] salvataggio = db.LoadGame();

        if (carica) {
            if (salvataggio == null) {
                JOptionPane.showMessageDialog(frame,
                    "Nessuna partita salvata trovata!", "Attenzione",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            music_player.stopMusic();
            String stanza = salvataggio[0];
            int statoCitySalvato = Integer.parseInt(salvataggio[1]);
            boolean possiedeMappa = Boolean.parseBoolean(salvataggio[2]);
            String enigmiStr = salvataggio[3];
            System.out.println("Carico partita dalla stanza: " + stanza);

            // ripristino lo stato PRIMA di costruire le scene, cosi vengono
            // ricreate coerenti con l'avanzamento (enigmi gia' risolti compresi)
            progression.setStatoCity(statoCitySalvato);
            giocatore.setPossiedeInventario(true);
            giocatore.setPossiedeMappa(possiedeMappa);
            if (enigmiStr != null && !enigmiStr.isEmpty()) {
                giocatore.setEnigmiRisolti(Arrays.asList(enigmiStr.split(",")));
            }

            // Ricostruzione Dinamica dell'Inventario
            giocatore.getInventario().getListaOggetti().clear(); // Svuoto l'inventario dai default
            if (salvataggio.length > 4 && salvataggio[4] != null && !salvataggio[4].isEmpty()) {
                String inventarioStr = salvataggio[4];
                for (String idStr : inventarioStr.split(",")) {
                    try {
                        int id = Integer.parseInt(idStr.trim());
                        Oggetto obj = txt.getOggettoDaCatalogo(id);
                        if (obj != null) {
                            giocatore.getInventario().aggiungiOggetto(obj);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Errore parsing ID oggetto salvato: " + idStr);
                    }
                }
            }

            // Ripristino la carica della Spada Sincro, persistita a parte perché
            // l'inventario salva solo gli ID e non lo stato interno degli oggetti
            if (salvataggio.length > 6 && salvataggio[6] != null) {
                try {
                    int caricaSpada = Integer.parseInt(salvataggio[6].trim());
                    Oggetto spadaObj = giocatore.getInventario().cercaOggetto("Spada Sincro");
                    if (spadaObj instanceof Spada spada) {
                        spada.setCaricaSincro(caricaSpada);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Errore parsing carica spada salvata: " + salvataggio[6]);
                }
            }

            // Ripristino del flag primoAccessoPalazzo
            if (salvataggio.length > 5 && salvataggio[5] != null) {
                boolean primoAccesso = Boolean.parseBoolean(salvataggio[5]);
                progression.setPrimoAccessoPalazzo(primoAccesso);
            } else {
                progression.setPrimoAccessoPalazzo(true);
            }
            progression.setParlatoConGuardia(false);

            progression.costruisciScene();
            sceneManager.mostraScena(stanza);
            return;
        }

        // NUOVA PARTITA: Reset completo dello stato
        progression.setStatoCity(0);
        progression.setPrimoAccessoPalazzo(true);
        progression.setParlatoConGuardia(false);
        giocatore.setPossiedeInventario(false);
        giocatore.setPossiedeMappa(false);
        giocatore.setEnigmiRisolti(new ArrayList<>());
        giocatore.getInventario().getListaOggetti().clear();
        // Azzero la carica della Spada Sincro nel catalogo (istanza condivisa): evita
        // che una partita precedente nella stessa sessione lasci una carica residua
        Oggetto spadaCatalogo = txt.getOggettoDaCatalogo(10);
        if (spadaCatalogo instanceof Spada spada) {
            spada.setCaricaSincro(0);
        }
        Oggetto tessutoIniziale_temp = txt.getOggettoDaCatalogo(1);
        if (tessutoIniziale_temp != null) {
            giocatore.getInventario().aggiungiOggetto(tessutoIniziale_temp);
        }

        progression.costruisciScene();
        sceneManager.mostraScena("LETTERA");
    }

    public void setDialogueActive(boolean active) {
        this.isDialogoActive = active;
    }

    public void mostraDialogo(GameScreen scenaSfondo, String idScenaSfondo, String nome, String battuta, ImageIcon sprite) {
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

    public void mostraDialogoNPC(GameScreen scenaSfondo, String idScenaSfondo,
                                   Personaggio pg, ImageIcon sprite) {
        String battuta = pg.parla();
        if (battuta == null) {
            pg.resetDialogo();
            battuta = pg.parla();
        }
        if (battuta != null) {
            mostraDialogo(scenaSfondo, idScenaSfondo, pg.getNome(), battuta, sprite);
        }
    }

    public void mostraDialogoCallback(GameScreen scenaSfondo, String idScenaSfondo, String nome, String battuta, ImageIcon sprite, Runnable callback) {
        if (isDialogoActive) return;
        setDialogueActive(true);
        DialogueScreen ds = new DialogueScreen(scenaSfondo, () -> {
            setDialogueActive(false);
            sceneManager.mostraScena(idScenaSfondo);
            if (callback != null) callback.run();
        });
        ds.aggiornaSchermata(nome, battuta, sprite);
        sceneManager.registraScena("DIALOGO_CORRENTE", ds);
        sceneManager.mostraScena("DIALOGO_CORRENTE");
    }

    public void mostraDialogoNPCCallback(GameScreen scenaSfondo, String idScenaSfondo,
                                   Personaggio pg, ImageIcon sprite, Runnable callback) {
        String battuta = pg.parla();
        if (battuta == null) {
            pg.resetDialogo();
            battuta = pg.parla();
        }
        if (battuta != null) {
            mostraDialogoCallback(scenaSfondo, idScenaSfondo, pg.getNome(), battuta, sprite, callback);
        } else {
            if (callback != null) callback.run();
        }
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
        if (sceneManager.isChatOpen()) return;
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

    private void impostaKeyBindingChat() {
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "toggle_chat");

        am.put("toggle_chat", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                apriChatDaTastiera();
            }
        });
    }

    private void apriChatDaTastiera() {
        if (sceneManager.isChatOpen()) return;
        if (chatNonApribileQui()) return;
        if (isDialogoActive) {
            System.out.println("DEBUG: Testo in corso, chat non apribile");
            return;
        }
        if (sceneManager.isMapOpen()) sceneManager.ChiudiMappa();
        if (sceneManager.isOpenInventario()) sceneManager.ChiudiInventario();
        network.toggleChat();
    }

    private void chatButtonClick() {
        if (!sceneManager.isChatOpen()) {
            if (chatNonApribileQui()) return;
            if (isDialogoActive) {
                System.out.println("DEBUG: Testo in corso, chat non apribile");
                return;
            }
            if (sceneManager.isMapOpen()) sceneManager.ChiudiMappa();
            if (sceneManager.isOpenInventario()) sceneManager.ChiudiInventario();
        }
        network.toggleChat();
    }

    // la chat non si apre nei titoli di coda / statistiche
    private boolean chatNonApribileQui() {
        return "TITOLI_CODA".equals(sceneManager.getScenaCorrente());
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
        if (sceneManager.isChatOpen()) return;
        if (isDialogoActive) {
            System.out.println("DEBUG: Testo in corso, inventario non apribile");
            return;
        }
        if (!giocatore.isPossiedeInventario()) {
            System.out.println("DEBUG: Inventario non ancora disponibile");
            return;
        }
        if (sceneManager.isMapOpen()) sceneManager.ChiudiMappa();
        if (!sceneManager.isOpenInventario()) sceneManager.ApriInventario();
        else sceneManager.ChiudiInventario();
    }

    private void impostaKeyBindingEsc() {
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");

        am.put("esc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gestisciEsc();
            }
        });
    }

    private void gestisciEsc() {
        if (isDialogoActive) return;

        if (sceneManager.isChatOpen()) { network.toggleChat(); return; }
        if (sceneManager.isMapOpen()) { sceneManager.ChiudiMappa(); return; }
        if (sceneManager.isOpenInventario()) { sceneManager.ChiudiInventario(); return; }

        switch (sceneManager.getScenaCorrente()) {
            case "MENU_PRINCIPALE":
            case "TITOLI_CODA":
                return;
            case "MENU_PAUSA":
                sceneManager.mostraScena(scenaDaSalvare);
                return;
            case "COMANDI":
                sceneManager.mostraScena("MENU_PAUSA");
                return;
            default:
                scenaDaSalvare = sceneManager.getScenaCorrente();
                sceneManager.mostraScena("MENU_PAUSA");
                break;
        }
    }

    private void registraMenuPausa() {
        BufferedImage sfondoMenu = null;
        try {
            sfondoMenu = ImageIO.read(getClass().getResourceAsStream(
                "/sprites/StrumentiGrafici/Menu.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento Menu.png: " + e.getMessage());
        }

        Map<double[], Runnable> zone = new HashMap<>();
        zone.put(new double[]{0.085, 0.375, 0.28, 0.105},           
            () -> sceneManager.mostraScena(scenaDaSalvare));
        zone.put(new double[]{0.085, 0.535, 0.29, 0.105},           
            () -> sceneManager.mostraScena("COMANDI"));
        zone.put(new double[]{0.085, 0.690, 0.28, 0.105},           
            this::salvaEdEsci);

        GameScreen menuPausa = new GameScreen(sfondoMenu, zone);
        sceneManager.registraScena("MENU_PAUSA", menuPausa);
    }

    private void registraComandi() {
        JPanel comandi = new JPanel(new BorderLayout());
        comandi.setBackground(new Color(30, 20, 20));

        JLabel titolo = new JLabel("Comandi di gioco", SwingConstants.CENTER);
        titolo.setForeground(Color.WHITE);
        titolo.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        comandi.add(titolo, BorderLayout.NORTH);

        JTextArea lista = new JTextArea(
            "   I      Apri / chiudi Inventario\n\n" +
            "   M      Apri / chiudi Mappa\n\n" +
            "   C      Apri / chiudi Chat\n\n" +
            "   ESC    Menu di pausa\n\n" +
            "   Click sui personaggi per parlare\n\n" +
            "   Suggerimento: Mentre si leggono alcuni enigmi si può tornare indietro\n" +
            "   per parlare con i personaggi, qualcuno saprà sicuramente darti un consiglio!\n"
        );
        lista.setEditable(false);
        lista.setOpaque(false);
        lista.setForeground(Color.WHITE);
        lista.setLineWrap(true);
        lista.setWrapStyleWord(true);
        lista.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));
        comandi.add(lista, BorderLayout.CENTER);

        JButton indietro = new JButton("Indietro");
        indietro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        indietro.addActionListener(e -> sceneManager.mostraScena("MENU_PAUSA"));
        JPanel sud = new JPanel();
        sud.setOpaque(false);
        sud.setBackground(new Color(30, 20, 20));
        sud.add(indietro);
        comandi.add(sud, BorderLayout.SOUTH);

        comandi.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = comandi.getWidth();
                int titleSize = Math.max(20, w / 35);
                int listSize = Math.max(14, w / 55);
                int btnSize = Math.max(14, w / 70);
                
                titolo.setFont(new Font("Arial", Font.BOLD, titleSize));
                lista.setFont(new Font("Monospaced", Font.BOLD, listSize));
                indietro.setFont(new Font("Arial", Font.BOLD, btnSize));
                
                int marginLR = Math.max(20, w / 15);
                lista.setBorder(BorderFactory.createEmptyBorder(20, marginLR, 20, marginLR));
            }
        });

        sceneManager.registraScena("COMANDI", comandi);
    }

    private void salvaEdEsci() {
        db.NewStart();
        String enigmi = String.join(",", giocatore.getEnigmiRisolti());
        
        ArrayList<String> invIds = new ArrayList<>();
        int caricaSpada = 0;
        for (Oggetto o : giocatore.getInventario().getListaOggetti()) {
            invIds.add(String.valueOf(o.getIdOggetto()));
            // Gli oggetti sono serializzati solo per ID: salvo a parte lo stato
            // interno della Spada Sincro (la sua carica), altrimenti andrebbe perso
            if (o instanceof Spada spada) {
                caricaSpada = spada.getCaricaSincro();
            }
        }
        String inventario = String.join(",", invIds);

        db.salvaPartita(scenaDaSalvare, progression.getStatoCity(),
                        giocatore.isPossiedeMappa(), enigmi, inventario, progression.isPrimoAccessoPalazzo(),
                        caricaSpada);
        ExitGame();
    }

    public void ExitGame() {
        System.out.println("WARNING: Stiamo uscendo dal gioco");
        network.fermaNetwork();
        db.chiudiConnessione();
        System.exit(0);
    }
}