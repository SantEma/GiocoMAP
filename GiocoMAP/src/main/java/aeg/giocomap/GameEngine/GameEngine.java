package aeg.giocomap.GameEngine;

import aeg.giocomap.View.MappaPanel;
import aeg.giocomap.View.InventarioPanel;
import aeg.giocomap.View.TitleScreen;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.GameScreen;
import aeg.giocomap.View.TitoliDiCoda;
import aeg.giocomap.View.DialogueScreen;
import aeg.giocomap.View.LetteraScreen;
import aeg.giocomap.View.ChatPanel;

import aeg.giocomap.Network.GameServer;
import aeg.giocomap.Network.GameClient;
import aeg.giocomap.Network.Message;
import aeg.giocomap.Network.TipoMessaggio;

import aeg.giocomap.Model.Storage.*;
import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.Model.Personaggi.Fantoccio;
import aeg.giocomap.Model.Giocatore.Giocatore;
import aeg.giocomap.Model.Enigmi.Enigma;

import aeg.giocomap.Util.JsonLoader;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class GameEngine {

    // Variabili per immagazzinare i dialoghi dei file JSON già da subito
    private final JsonObject dbWallOfText;
    private final JsonObject dbStoria;
    private final JsonObject dbHint;

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

    // punteggio e timer
    private long tempoInizioEnigma = 0;
    private int punteggioTotale = 0;
    private TimerEnigma timerEnigma;

    // networking
    private GameServer gameServer;
    private GameClient gameClient;
    private ChatPanel chatPanel;
    private boolean serverAvviato = false;

    // scena di gioco da cui e' stato aperto il menu di pausa (per "Continua"
    // e per il salvataggio: la scena vera, non "MENU_PAUSA")
    private String scenaDaSalvare = "MENU_PRINCIPALE";
    // Struttura dati per astrarre la logica dei percorsi (routing table)
    private final Map<String, Map<String, Runnable>> collegamentiMappa = new HashMap<>();

    // Registro dei personaggi attivi nella partita (ogni NPC usa setDialoghi/parla)
    private final Map<String, Personaggio> registroNPC = new HashMap<>();

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

        sceneManager.registraScena("MAPPA", new MappaPanel());
        impostaKeyBindingMappa();
        impostaKeyBindingChat();
        this.frame.setChatListener(e -> chatButtonClick());

        this.giocatore = new Giocatore("Eryndor");

        Oggetto tessutoIniziale_temp = txt.getOggettoDaCatalogo(1);
        if (tessutoIniziale_temp != null)
            this.giocatore.getInventario().aggiungiOggetto(tessutoIniziale_temp);

        sceneManager.registraScena("INVENTARIO", new InventarioPanel(this.giocatore.getInventario()));
        impostaKeyBindingInventario();
        impostaKeyBindingEsc();

        registraMenuPausa();
        registraComandi();

        TitleScreenImp();
        impostaFrecceLogica();
        sceneManager.mostraScena("MENU_PRINCIPALE");
    }

    private void impostaFrecceLogica() {
        inizializzaRoot();
        
        frame.setFrecceListener(
            e -> eseguiCollegamento("NORD"),
            e -> eseguiCollegamento("SUD"),
            e -> eseguiCollegamento("EST"),
            e -> eseguiCollegamento("OVEST")
        );
    }
    
    private void inizializzaRoot() {
        // Collegamenti semplici
        registraCollegamentoSemplice("PIAZZA_CENTRALE", "NORD", "PORTO");
        registraCollegamentoSemplice("PORTO", "SUD", "PIAZZA_CENTRALE");
        
        // Uscita dal regno verso SUD (dealloca DB e risorse prima di chiudere)
        registraCollegamento("PIAZZA_CENTRALE", "SUD", () -> {
            int scelta = JOptionPane.showConfirmDialog(frame, 
                "Stai per uscire dal regno e andare nel regno di Luluna, sei sicuro di proseguire?", 
                "Attenzione", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            if (scelta == JOptionPane.YES_OPTION) {
                ExitGame();
            }
        });
        
        // PIAZZA_CENTRALE - Rotte laterali (Con check di storyline)
        // Fantoccio per il testo a schermo senza nome (OVEST bloccato)
        String testoLocali = dbStoria.getAsJsonObject("Eryndor").getAsJsonObject("inizio").get("locali_chiusi").getAsString();
        Fantoccio fantoccioOvest = registraFantoccio("Fantoccio_Ovest", Arrays.asList(testoLocali));
        
        registraCollegamento("PIAZZA_CENTRALE", "OVEST", () -> {
            if (giocatore.isPossiedeMappa()) { // Dopo aver parlato con David ottiene la mappa
                sceneManager.mostraScena("STALLA");
            } else {
                GameScreen piazza = (GameScreen) sceneManager.getScena("PIAZZA_CENTRALE");
                mostraDialogoNPC(piazza, "PIAZZA_CENTRALE", fantoccioOvest, null);
            }
        });
        
        // Personaggio "Guardiano" per il blocco EST (mostra il nome)
        String testoBlocco = dbStoria.getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Bloccatore").get("stop_carrozza").getAsString();
        Personaggio guardiano = registraNPC("Guardiano", Arrays.asList(testoBlocco));
        
        registraCollegamento("PIAZZA_CENTRALE", "EST", () -> {
            // Controlla se il giocatore ha completato l'enigma della stalla
            // (TODO: Cambiare 'false' con il check reale quando verrà implementato l'enigma)
            if (false) {
                sceneManager.mostraScena("BOSCO");
            } else {
                GameScreen piazza = (GameScreen) sceneManager.getScena("PIAZZA_CENTRALE");
                mostraDialogoNPC(piazza, "PIAZZA_CENTRALE", guardiano, null);
            }
        });

        // ----------------------------------------------------
        // STALLA: Uscita verso la Piazza (da NORD)
        // ----------------------------------------------------
        registraCollegamentoSemplice("STALLA", "NORD", "PIAZZA_CENTRALE");
        
        // ----------------------------------------------------
        // BOSCO: Snodo principale (BoscoLosco.png)
        // ----------------------------------------------------
        // Ritorno in Piazza
        registraCollegamentoSemplice("BOSCO", "SUD", "PIAZZA_CENTRALE");
        // Verso Karundis
        registraCollegamentoSemplice("BOSCO", "NORD", "KARUNDIS");
        // Verso Bosco Deep (BoscoINN.png)
        registraCollegamentoSemplice("BOSCO", "OVEST", "BOSCO_DEEP");
        
        // ----------------------------------------------------
        // BOSCO_DEEP: Ritorno al Bosco (speculare a OVEST)
        // ----------------------------------------------------
        registraCollegamentoSemplice("BOSCO_DEEP", "EST", "BOSCO");
        
        // ----------------------------------------------------
        // KARUNDIS E GROTTA
        // ----------------------------------------------------
        registraCollegamentoSemplice("KARUNDIS", "OVEST", "BOSCO");
        registraCollegamentoSemplice("KARUNDIS", "NORD", "INGRESSO_PALAZZO");
        // Nota: ingresso GROTTA gestito dalla zona cliccabile in "creaScene"
        
        registraCollegamentoSemplice("GROTTA", "SUD", "KARUNDIS");
        
        // ----------------------------------------------------
        // CASTELLO E SOTTERRANEI
        // ----------------------------------------------------
        registraCollegamentoSemplice("INGRESSO_PALAZZO", "SUD", "KARUNDIS");
        registraCollegamentoSemplice("INGRESSO_PALAZZO", "NORD", "SCALE");
        
        registraCollegamentoSemplice("SCALE", "SUD", "INGRESSO_PALAZZO");
        registraCollegamentoSemplice("SCALE", "NORD", "PALAZZO_PRINCIPESSA");
        registraCollegamentoSemplice("SCALE", "OVEST", "CRIPTA_ERIPETA");
        
        registraCollegamentoSemplice("PALAZZO_PRINCIPESSA", "SUD", "SCALE");
        
        registraCollegamentoSemplice("CRIPTA_ERIPETA", "EST", "SCALE");
    }
    
    // La possibilità di procedere nelle zone sarà gestita dai controlli
    private void registraCollegamento(String daScena, String direzione, Runnable azione) {
        collegamentiMappa.computeIfAbsent(daScena, k -> new HashMap<>()).put(direzione, azione);
    }
    
    // Spostamenti che non richiedono controlli
    private void registraCollegamentoSemplice(String daScena, String direzione, String aScena) {
        registraCollegamento(daScena, direzione, () -> sceneManager.mostraScena(aScena));
    }
    
    // Esegue la direzione cliccata a cosa deve accadere
    private void eseguiCollegamento(String direzione) {
        String scena = sceneManager.getScenaCorrente();
        if (scena == null) return;
        
        // Cerco nella tabella quali uscite posso prendere dalla scena attuale
        Map<String, Runnable> uscite = collegamentiMappa.get(scena);
        if (uscite != null && uscite.containsKey(direzione)) {
            uscite.get(direzione).run();
        } else {
            System.out.println("DEBUG: Nessuna direzione a " + direzione + " da " + scena);
        }
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

       
        title_screen.addConnettiListener(e -> {
            String ip = JOptionPane.showInputDialog(
                frame,
                "Inserisci l'IP dell'host:",
                "Partecipa alla Chat",
                JOptionPane.PLAIN_MESSAGE
            );
            if (ip != null && !ip.trim().isEmpty()) {
                connettiComeClient(ip.trim());
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
            System.out.println("Carico partita dalla stanza: " + stanza);

            // ricreo e registro le scene cosi quella salvata esiste,
            // ripristino lo stato del giocatore e la mostro
            costruisciScene();
            giocatore.setPossiedeInventario(true);
            giocatore.setPossiedeMappa(true);
            sceneManager.mostraScena(stanza);
            return;
        }

        costruisciScene();
        sceneManager.mostraScena("LETTERA");
    }

    // Costruisce e registra tutte le scene di gioco (piazza, porto, lettere).
    // Va chiamato sia in partita nuova sia in caricamento, cosi le scene
    // esistono prima di essere mostrate.
    private void costruisciScene() {
        
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

        // Creazione NPC della piazza tramite Personaggio.setDialoghi()/parla()
        Personaggio ab1 = registraNPC("Abitante 1", Arrays.asList(hints.get(0)));
        Personaggio ab2 = registraNPC("Abitante 2", Arrays.asList(hints.get(1)));
        Personaggio ab3 = registraNPC("Abitante 3", Arrays.asList(hints.get(2)));

        // Circa posizione NPC1
        zonePiazza.put(new double[]{0.3090, 0.6083, 0.08, 0.25}, () -> mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab1, null));
        // Circa posizione NPC2
        zonePiazza.put(new double[]{0.5349, 0.4436, 0.08, 0.25}, () -> mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab2, null));
        // Circa posizione NPC3
        zonePiazza.put(new double[]{0.6228, 0.6285, 0.08, 0.25}, () -> mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab3, null));

        BufferedImage sfondoPiazza = null;
        try {
            sfondoPiazza = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/PiazzaCentrale.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo: " + e.getMessage());
        }

        GameScreen piazzaCentrale = new GameScreen(sfondoPiazza, zonePiazza);
        
        piazzaCentraleArr[0] = piazzaCentrale;
        sceneManager.registraScena("PIAZZA_CENTRALE", piazzaCentrale);
        

        // Inizializzazione scena PORTO
        Map<double[], Runnable> zonePorto = new HashMap<>();
        final GameScreen[] portoScreenArr = new GameScreen[1];

        // Creazione NPC David tramite Personaggio.setDialoghi()/parla() e caricamento sprite
        String dialogoDavid = dbStoria.getAsJsonObject("Dialoghi_NPC").getAsJsonObject("David").get("incontro_1").getAsString();
        Personaggio david = registraNPC("David", Arrays.asList(dialogoDavid));
        ImageIcon spriteDavid = new ImageIcon(getClass().getResource("/sprites/Personaggi/David.png"));

        // coordinate david (testa a 0.3525, piedi a 0.7270, da x=0.7187 a 0.8476)
        zonePorto.put(new double[]{0.7187, 0.3525, 0.1289, 0.3745}, () -> {
            mostraDialogoNPC(portoScreenArr[0], "PORTO", david, spriteDavid);
            // Sblocca la mappa post interazione David
            giocatore.setPossiedeMappa(true);
        });

        BufferedImage sfondoPorto = null;
        try {
            sfondoPorto = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/PortoMareBlu.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo porto: " + e.getMessage());
        }
        GameScreen portoScreen = new GameScreen(sfondoPorto, zonePorto);
        //DEBUG TEST COORDINATE
        portoScreen.abilitaDebugCoordinate();
        portoScreenArr[0] = portoScreen;
        
        
        sceneManager.registraScena("PORTO", portoScreen);
        
        // ---------------------------------------------------------------------------------
        // Inizializzazione altre zone della mappa (Senza controlli o NPC per ora)
        // ---------------------------------------------------------------------------------
        
        // Prima macro-zona (Piazza -> Stalla / Bosco)
        sceneManager.registraScena("STALLA", sceneManager.creaScenaBase("Stalla.png", null));
        // Il Bosco principale usa BoscoLosco.png
        sceneManager.registraScena("BOSCO", sceneManager.creaScenaBase("BoscoLosco.png", null));
        // Il Bosco Deep (raggiungibile ad Ovest) usa BoscoINN.png
        sceneManager.registraScena("BOSCO_DEEP", sceneManager.creaScenaBase("BoscoINN.png", null));
        
        // Seconda macro-zona (Karundis)
        Map<double[], Runnable> zoneKarundis = new HashMap<>();
        // Hitbox temporanea al centro dello schermo. Usa abilitaDebugCoordinate() per aggiustarla!
        zoneKarundis.put(new double[]{0.4, 0.4, 0.2, 0.2}, () -> sceneManager.mostraScena("GROTTA"));
        GameScreen karundisScreen = sceneManager.creaScenaBase("Karundis.png", zoneKarundis);
        // karundisScreen.abilitaDebugCoordinate(); // Scommentare per posizionare l'entrata della grotta
        sceneManager.registraScena("KARUNDIS", karundisScreen);

        // Terza macro-zona (Castello e sotterranei)
        sceneManager.registraScena("GROTTA", sceneManager.creaScenaBase("GrottaDellaFucina.png", null));
        sceneManager.registraScena("INGRESSO_PALAZZO", sceneManager.creaScenaBase("CancelloCastello.png", null));
        sceneManager.registraScena("SCALE", sceneManager.creaScenaBase("ScalePalazzo.png", null));
        sceneManager.registraScena("PALAZZO_PRINCIPESSA", sceneManager.creaScenaBase("SalaDellaPrincipessa.png", null));
        sceneManager.registraScena("CRIPTA_ERIPETA", sceneManager.creaScenaBase("Cripta.png", null));

        // Utilizzo del bottone per il dietro della lettera
        LetteraScreen schermata_retro = new LetteraScreen(letteraRetro, () -> {
            System.out.println("DEBUG: Lettera retro finita, gioco START");
            
            // Lettera finita, alla prossima scena sblocca l'inventario e mostro piazza centrale
            giocatore.setPossiedeInventario(true);
            
            sceneManager.mostraScena("PIAZZA_CENTRALE");
        });
        sceneManager.registraScena("LETTERA_RETRO", schermata_retro);

        // Lettera avanti, passa al retro appena finito
        LetteraScreen schermata_lettera = new LetteraScreen(lettera, () -> {
            System.out.println("DEBUG: Lettera finita, mostro retro");
            
            // Retro della lettera con l'enigma sopra
            sceneManager.mostraScena("LETTERA_RETRO");

        });
        sceneManager.registraScena("LETTERA", schermata_lettera);
    }


    // Chiamato quando il giocatore vede un enigma
    public void iniziaEnigma() {
        timerEnigma = new TimerEnigma(() -> {
            System.out.println("DEBUG: Secondi: " + timerEnigma.getSecondi());
        });
        timerEnigma.avvia();
        System.out.println("DEBUG: Enigma iniziato");
    }

    public void enigmaRisolto(Enigma enigma) {
        if (timerEnigma != null) timerEnigma.ferma();

        int secondi = timerEnigma != null ? timerEnigma.getSecondi() : 0;
        int punti = calcolaPunti(secondi);
        punteggioTotale += punti;

        if (enigma.getReward() != null) {
            giocatore.getInventario().aggiungiOggetto(enigma.getReward());
            System.out.println("DEBUG: Reward aggiunto: " + enigma.getReward().getNomeOggetto());
        }

        System.out.println("DEBUG: " + secondi + "s, " + punti + " punti, totale: " + punteggioTotale);
    }

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


    /*
    ToDo:
    Ragazzi vi lascio questo commento qui in modo che possiate capire:
    I COMMENTI PER FAVORE CONTROLLATELI. Si vede che non li avete scritti voi.
    Aggiustateli sempre ASAP, anche con l'aiuto di AI se deve essere necessario
    */
    public void toggleChat() {
        if (gameClient == null) {
            // nessuna sessione attiva: il primo che apre la chat diventa host
            gameServer = new GameServer();
            gameServer.avvia();
            serverAvviato = true;

            gameClient = new GameClient("Eryndor");
            chatPanel = new ChatPanel();
            gameClient.connetti("127.0.0.1");
            chatPanel.setClient(gameClient);

            JOptionPane.showMessageDialog(
                frame,
                "Chat avviata! Il tuo IP: " + ottieniIP() +
                "\nCondividilo con i tuoi amici!",
                "Server Online",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        sceneManager.toggleChat(chatPanel);
    }

    // CLIENT → si connette al server dell'host
    private void connettiComeClient(String ip) {
        String nome = JOptionPane.showInputDialog(
            frame,
            "Inserisci il tuo nome:",
            "Partecipa alla Chat",
            JOptionPane.PLAIN_MESSAGE
        );

        if (nome == null || nome.trim().isEmpty()) return;

        // Eryndor è riservato all'host
        if (nome.trim().equalsIgnoreCase("Eryndor")) {
            JOptionPane.showMessageDialog(
                frame,
                "Il nome Eryndor è riservato all'host!",
                "Nome non valido",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        gameClient = new GameClient(nome.trim());
        chatPanel = new ChatPanel();
        boolean ok = gameClient.connetti(ip);

        if (ok) {
            // gestisci nome duplicato
            gameClient.getThreadRicezione().setOnNomeDuplicato(() -> {
                JOptionPane.showMessageDialog(
                    frame,
                    "Nome già in uso! Riprova con un altro nome.",
                    "Nome duplicato",
                    JOptionPane.WARNING_MESSAGE
                );
                gameClient.disconnetti();
                gameClient = null;
            });

            chatPanel.setClient(gameClient);
            JOptionPane.showMessageDialog(
                frame,
                "Connesso alla chat!",
                "Connessione riuscita",
                JOptionPane.INFORMATION_MESSAGE
            );
            sceneManager.toggleChat(chatPanel);
        } else {
            JOptionPane.showMessageDialog(
                frame,
                "Server non trovato! Assicurati che l'host abbia avviato la chat.",
                "Errore connessione",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // chiede al sistema operativo quale indirizzo locale userebbe per
    // raggiungere l'esterno (non serve connessione reale, la connect() su
    // UDP fa solo scegliere l'interfaccia giusta in base alla tabella di
    // routing): funziona automaticamente sia su hotspot che su LAN normale,
    // senza bisogno di indovinare tra le varie interfacce di rete
    private String ottieniIP() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            if (ip != null && !ip.equals("0.0.0.0")) return ip;
        } catch (Exception e) {
            System.err.println("Errore rilevamento IP (routing): " + e.getMessage());
        }

        // fallback: cerca manualmente tra le interfacce di rete
        try {
            Enumeration<NetworkInterface> interfacce =
                NetworkInterface.getNetworkInterfaces();

            while (interfacce.hasMoreElements()) {
                NetworkInterface ni = interfacce.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

                Enumeration<InetAddress> indirizzi = ni.getInetAddresses();
                while (indirizzi.hasMoreElements()) {
                    InetAddress addr = indirizzi.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Errore fallback ricerca IP: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    private void Statistiche(boolean fineGioco) {
        String nome = "";
        int punteggio = 0;

        if (fineGioco) {
            punteggio = punteggioTotale;

            while (nome == null || nome.trim().isEmpty()) {
                nome = JOptionPane.showInputDialog(frame,
                    "Inserisci il tuo nome per salvare il punteggio:",
                    "Fine gioco!", JOptionPane.PLAIN_MESSAGE);
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                        "Devi inserire un nome per continuare!",
                        "Attenzione", JOptionPane.WARNING_MESSAGE);
                }
            }
            giocatore.setNomePlayer(nome.trim());
            db.salvaSeNecessario(giocatore.getNomePlayer(), punteggio);
        }

        List<String[]> records = db.getRecords();
        String nome_passato = (giocatore != null && giocatore.getNomePlayer() != null
            ? giocatore.getNomePlayer() : "");
        music_player.stopMusic();
        if (fineGioco) music_player.playMusic(MusicPlayer.END_TITLE_MUSIC);
        TitoliDiCoda titoli = new TitoliDiCoda(records, punteggio, nome_passato);
        

        titoli.addIndietroListener(e -> {
            music_player.stopMusic();
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

    // --- Metodi helper per il sistema NPC basato su Personaggio ---

    // Crea e registra un Personaggio con i suoi dialoghi
    private Personaggio registraNPC(String nome, List<String> dialoghi) {
        Personaggio pg = new Personaggio(nome);
        pg.setDialoghi(dialoghi);
        registroNPC.put(nome, pg);
        return pg;
    }

    // Crea e registra un Fantoccio (testi a schermo senza nome) con i suoi dialoghi
    private Fantoccio registraFantoccio(String chiaveRegistro, List<String> dialoghi) {
        Fantoccio f = new Fantoccio();
        f.setDialoghi(dialoghi);
        registroNPC.put(chiaveRegistro, f);
        return f;
    }

    // Mostra dialogo prendendo la battuta da Personaggio.parla()
    private void mostraDialogoNPC(GameScreen scenaSfondo, String idScenaSfondo,
                                   Personaggio pg, ImageIcon sprite) {
        String battuta = pg.parla();
        if (battuta == null) {
            // Dialoghi finiti, reset e riparte dal primo
            pg.resetDialogo();
            battuta = pg.parla();
        }
        if (battuta != null) {
            mostraDialogo(scenaSfondo, idScenaSfondo, pg.getNome(), battuta, sprite);
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

    // la chat si chiude solo dal bottone "Chiudi Chat" nel pannello,
    // cosi digitare "c" in un messaggio non la richiude accidentalmente
    private void apriChatDaTastiera() {
        if (sceneManager.isChatOpen()) return;
        if (isDialogoActive) {
            System.out.println("DEBUG: Testo in corso, chat non apribile");
            return;
        }
        if (sceneManager.isMapOpen()) sceneManager.ChiudiMappa();
        if (sceneManager.isOpenInventario()) sceneManager.ChiudiInventario();
        toggleChat();
    }

    // bottone fluttuante: a differenza del tasto rapido puo anche chiudere
    // la chat (un click non ha conflitti con la digitazione dei messaggi)
    private void chatButtonClick() {
        if (!sceneManager.isChatOpen()) {
            if (isDialogoActive) {
                System.out.println("DEBUG: Testo in corso, chat non apribile");
                return;
            }
            if (sceneManager.isMapOpen()) sceneManager.ChiudiMappa();
            if (sceneManager.isOpenInventario()) sceneManager.ChiudiInventario();
        }
        toggleChat();
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
        // durante un dialogo l'ESC non apre il menu
        if (isDialogoActive) return;

        // ESC come "indietro": prima chiude eventuali pannelli sovrapposti.
        // chat/mappa/inventario NON aggiornano scenaCorrente, vanno intercettati coi flag
        if (sceneManager.isChatOpen()) { toggleChat(); return; }
        if (sceneManager.isMapOpen()) { sceneManager.ChiudiMappa(); return; }
        if (sceneManager.isOpenInventario()) { sceneManager.ChiudiInventario(); return; }

        switch (sceneManager.getScenaCorrente()) {
            // scene "finali": ESC non fa nulla
            case "MENU_PRINCIPALE":
            case "TITOLI_CODA":
                return;

            // gia' nel menu di pausa: ESC riprende il gioco
            case "MENU_PAUSA":
                sceneManager.mostraScena(scenaDaSalvare);
                return;

            // nella schermata comandi: ESC torna al menu di pausa
            case "COMANDI":
                sceneManager.mostraScena("MENU_PAUSA");
                return;

            // scene di gioco: apre il menu di pausa
            default:
                scenaDaSalvare = sceneManager.getScenaCorrente();
                sceneManager.mostraScena("MENU_PAUSA");
                break;
        }
    }

    // menu di pausa: immagine unica (Menu.png) con 3 zone cliccabili sui bottoni
    private void registraMenuPausa() {
        BufferedImage sfondoMenu = null;
        try {
            sfondoMenu = ImageIO.read(getClass().getResourceAsStream(
                "/sprites/StrumentiGrafici/Menu.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento Menu.png: " + e.getMessage());
        }

        // zone in percentuale {x, y, larghezza, altezza} sui tre bottoni
        Map<double[], Runnable> zone = new HashMap<>();
        zone.put(new double[]{0.085, 0.375, 0.28, 0.105},           // Continua
            () -> sceneManager.mostraScena(scenaDaSalvare));
        zone.put(new double[]{0.085, 0.535, 0.29, 0.105},           // Comandi gioco
            () -> sceneManager.mostraScena("COMANDI"));
        zone.put(new double[]{0.085, 0.690, 0.28, 0.105},           // Salva ed esci
            this::salvaEdEsci);

        GameScreen menuPausa = new GameScreen(sfondoMenu, zone);
        sceneManager.registraScena("MENU_PAUSA", menuPausa);
    }

    // schermata con la lista dei comandi (testo provvisorio, da rifinire)
    private void registraComandi() {
        JPanel comandi = new JPanel(new BorderLayout());
        comandi.setBackground(new Color(30, 20, 20));

        JLabel titolo = new JLabel("Comandi di gioco", SwingConstants.CENTER);
        titolo.setForeground(Color.WHITE);
        titolo.setFont(new Font("Arial", Font.BOLD, 34));
        titolo.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        comandi.add(titolo, BorderLayout.NORTH);

        JTextArea lista = new JTextArea(
            "   I      Apri / chiudi Inventario\n\n" +
            "   M      Apri / chiudi Mappa\n\n" +
            "   C      Apri / chiudi Chat\n\n" +
            "   ESC    Menu di pausa\n\n" +
            "   Click sui personaggi per parlare\n"
        );
        lista.setEditable(false);
        lista.setOpaque(false);
        lista.setForeground(Color.WHITE);
        lista.setFont(new Font("Monospaced", Font.BOLD, 24));
        lista.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));
        comandi.add(lista, BorderLayout.CENTER);

        JButton indietro = new JButton("Indietro");
        indietro.setFont(new Font("Arial", Font.BOLD, 18));
        indietro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        indietro.addActionListener(e -> sceneManager.mostraScena("MENU_PAUSA"));
        JPanel sud = new JPanel();
        sud.setOpaque(false);
        sud.setBackground(new Color(30, 20, 20));
        sud.add(indietro);
        comandi.add(sud, BorderLayout.SOUTH);

        sceneManager.registraScena("COMANDI", comandi);
    }

    private void salvaEdEsci() {
        db.NewStart();
        db.salvaPartita(scenaDaSalvare, "0");
        ExitGame();
    }

    public void ExitGame() {
        System.out.println("WARNING: Stiamo uscendo dal gioco");
        if (gameClient != null) gameClient.disconnetti();
        if (gameServer != null) gameServer.ferma();
        db.chiudiConnessione();
        System.exit(0);
    }
}