package aeg.giocomap.GameEngine;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.Model.Personaggi.Fantoccio;
import aeg.giocomap.View.GameScreen;
import aeg.giocomap.View.LetteraScreen;
import aeg.giocomap.Util.JsonLoader;
import aeg.giocomap.Util.Parser;

import aeg.giocomap.Model.Enigmi.Enigma;
import aeg.giocomap.Model.Enigmi.EnigmaSceltaMultipla;
import aeg.giocomap.Model.Enigmi.IstanzaEnigma;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import java.io.IOException;

public class ProgressioneStoria {

    private final GameEngine engine;

    // Struttura dati per astrarre la logica dei percorsi (routing table)
    private final Map<String, Map<String, Runnable>> collegamentiMappa = new HashMap<>();

    // Registro dei personaggi attivi nella partita
    private final Map<String, Personaggio> registroNPC = new HashMap<>();

    // Stato logico della fase "Città con il porto"
    private final int[] statoCity = {0};
    
    // Azione interattiva di Mr. Cooper
    private Runnable mrCooperInteraction;
    private Runnable foxInteraction;

    public ProgressioneStoria(GameEngine engine) {
        this.engine = engine;
    }

    // stato di avanzamento della fase città (per salvataggio/caricamento)
    public int getStatoCity() {
        return statoCity[0];
    }

    public void setStatoCity(int valore) {
        statoCity[0] = valore;
    }

    public void impostaFrecceLogica() {
        inizializzaRoot();
        
        engine.getFrame().setFrecceListener(
            e -> eseguiCollegamento("NORD"),
            e -> eseguiCollegamento("SUD"),
            e -> eseguiCollegamento("EST"),
            e -> eseguiCollegamento("OVEST")
        );
    }

    private void inizializzaRoot() {
        registraCollegamentoSemplice("PIAZZA_CENTRALE", "NORD", "PORTO");
        registraCollegamentoSemplice("PORTO", "SUD", "PIAZZA_CENTRALE");
        
        registraCollegamento("PIAZZA_CENTRALE", "SUD", () -> {
            int scelta = JOptionPane.showConfirmDialog(engine.getFrame(), 
                "Stai per uscire dal regno e andare nel regno di Luluna, sei sicuro di proseguire?", 
                "Attenzione", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            if (scelta == JOptionPane.YES_OPTION) {
                engine.ExitGame();
            }
        });
        
        String testoLocali = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("inizio").get("locali_chiusi").getAsString();
        Fantoccio fantoccioOvest = registraFantoccio("Fantoccio_Ovest", Arrays.asList(testoLocali));
        
        registraCollegamento("PIAZZA_CENTRALE", "OVEST", () -> {
            if (engine.getGiocatore().isPossiedeMappa()) { 
                engine.getSceneManager().mostraScena("STALLA");
                if ((statoCity[0] == 0 || statoCity[0] == 4) && mrCooperInteraction != null) {
                    mrCooperInteraction.run();
                }
            } else {
                GameScreen piazza = (GameScreen) engine.getSceneManager().getScena("PIAZZA_CENTRALE");
                engine.mostraDialogoNPC(piazza, "PIAZZA_CENTRALE", fantoccioOvest, null);
            }
        });
        
        String testoBlocco = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Bloccatore").get("stop_carrozza").getAsString();
        Personaggio guardiano = registraNPC("Guardiano", Arrays.asList(testoBlocco));
        
        registraCollegamento("PIAZZA_CENTRALE", "EST", () -> {
            if (statoCity[0] == 5) {
                engine.getSceneManager().mostraScena("BOSCO");
                if (foxInteraction != null) foxInteraction.run();
            } else if (statoCity[0] > 5) {
                engine.getSceneManager().mostraScena("BOSCO");
            } else {
                GameScreen piazza = (GameScreen) engine.getSceneManager().getScena("PIAZZA_CENTRALE");
                engine.mostraDialogoNPC(piazza, "PIAZZA_CENTRALE", guardiano, null);
            }
        });

        registraCollegamentoSemplice("STALLA", "NORD", "PIAZZA_CENTRALE");
        
        registraCollegamentoSemplice("BOSCO", "SUD", "PIAZZA_CENTRALE");
        registraCollegamentoSemplice("BOSCO", "NORD", "KARUNDIS");
        registraCollegamentoSemplice("BOSCO", "OVEST", "BOSCO_DEEP");
        
        registraCollegamentoSemplice("BOSCO_DEEP", "EST", "BOSCO");
        
        registraCollegamentoSemplice("KARUNDIS", "OVEST", "BOSCO");
        registraCollegamentoSemplice("KARUNDIS", "NORD", "INGRESSO_PALAZZO");
        
        registraCollegamentoSemplice("GROTTA", "SUD", "KARUNDIS");
        
        registraCollegamentoSemplice("INGRESSO_PALAZZO", "SUD", "KARUNDIS");
        registraCollegamentoSemplice("INGRESSO_PALAZZO", "NORD", "SCALE");
        
        registraCollegamentoSemplice("SCALE", "SUD", "INGRESSO_PALAZZO");
        registraCollegamentoSemplice("SCALE", "NORD", "PALAZZO_PRINCIPESSA");
        registraCollegamentoSemplice("SCALE", "OVEST", "CRIPTA_ERIPETA");
        
        registraCollegamentoSemplice("PALAZZO_PRINCIPESSA", "SUD", "SCALE");
        registraCollegamentoSemplice("CRIPTA_ERIPETA", "EST", "SCALE");
    }

    private void registraCollegamento(String daScena, String direzione, Runnable azione) {
        collegamentiMappa.computeIfAbsent(daScena, k -> new HashMap<>()).put(direzione, azione);
    }
    
    private void registraCollegamentoSemplice(String daScena, String direzione, String aScena) {
        registraCollegamento(daScena, direzione, () -> engine.getSceneManager().mostraScena(aScena));
    }
    
    private void eseguiCollegamento(String direzione) {
        String scena = engine.getSceneManager().getScenaCorrente();
        if (scena == null) return;
        
        Map<String, Runnable> uscite = collegamentiMappa.get(scena);
        if (uscite != null && uscite.containsKey(direzione)) {
            uscite.get(direzione).run();
        } else {
            System.out.println("DEBUG: Nessuna direzione a " + direzione + " da " + scena);
        }
    }

    public void costruisciScene() {
        
        List<String> lettera=JsonLoader.estraiLista(engine.getDbWallOfText().getAsJsonObject("Lettera"),"lettera_iniziale");
        
        String enigmaText = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Enigma_1_Lettera").getAsString();
        List<String> letteraRetro = Arrays.asList(enigmaText);

        List<String> hints = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");

        Map<double[], Runnable> zonePiazza = new HashMap<>();
        final GameScreen[] piazzaCentraleArr = new GameScreen[1];

        Personaggio ab1 = registraNPC("Abitante 1", Arrays.asList(hints.get(0)));
        Personaggio ab2 = registraNPC("Abitante 2", Arrays.asList(hints.get(1)));
        Personaggio ab3 = registraNPC("Abitante 3", Arrays.asList(hints.get(2)));

        JsonObject mrCooperDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("MrCooper");
        JsonObject contadinoDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Contadino_Green");
        JsonObject pescivendoloDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Pescivendolo");
        JsonObject davidDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("David");

        Personaggio mrCooper = registraNPC("Mr. Cooper", Arrays.asList(
            mrCooperDb.get("saluto").getAsString() + "\n" +
            mrCooperDb.get("prezzo").getAsString() + "\n" +
            mrCooperDb.get("proposta").getAsString()
        ));
        
        Personaggio contadino = registraNPC("Contadino Green", Arrays.asList(
            contadinoDb.get("saluto").getAsString() + "\n" +
            contadinoDb.get("richiesta").getAsString()
        ));
        
        Personaggio pescivendolo = registraNPC("Pescivendolo", Arrays.asList(
            pescivendoloDb.get("saluto").getAsString() + "\n" +
            pescivendoloDb.get("richiesta").getAsString()
        ));

        ImageIcon spriteMrCooper = new ImageIcon(getClass().getResource("/sprites/Personaggi/MrCooper.png"));
        ImageIcon spriteGreen = new ImageIcon(getClass().getResource("/sprites/Personaggi/Green.png"));

        double[] hitboxAb1 = new double[]{0.3090, 0.6083, 0.08, 0.25};
        double[] hitboxAb2 = new double[]{0.5349, 0.4436, 0.08, 0.25};
        double[] hitboxAb3 = new double[]{0.6228, 0.6285, 0.08, 0.25};

        if (statoCity[0] < 3) {
            zonePiazza.put(hitboxAb1, () -> engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab1, null));
            zonePiazza.put(hitboxAb3, () -> engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab3, null));
        } else {
            ab2.setDialoghi(Arrays.asList(davidDb.get("saluto_generico").getAsString()));
        }
        zonePiazza.put(hitboxAb2, () -> engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab2, null));

        zonePiazza.put(new double[]{0.7187, 0.3525, 0.1289, 0.3745}, () -> {
            if (statoCity[0] == 1) { 
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("saluto").getAsString()));
                Runnable loopContadino = new Runnable() {
                    @Override
                    public void run() {
                        String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi al Contadino Green?");
                        if (input == null) return;
                        if (Parser.contieneParolaChiave(input, "carote")) {
                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("richiesta").getAsString()));
                            engine.mostraDialogoNPCCallback(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen, () -> {
                                Runnable loopConfermaContadino = new Runnable() {
                                    int countRifiuti = 0;
                                    JsonArray rifiutiJson = engine.getDbHint().getAsJsonObject("Loop_Rifiuti_Quest").getAsJsonArray("Contadino_Green");
                                    
                                    @Override
                                    public void run() {
                                        int scelta = JOptionPane.showConfirmDialog(engine.getFrame(), "Accetti la proposta del Contadino?", "Scelta", JOptionPane.YES_NO_OPTION);
                                        if (scelta == JOptionPane.YES_OPTION) {
                                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("ringraziamento").getAsString()));
                                            engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen);
                                            statoCity[0] = 2; 
                                        } else {
                                            String frase = rifiutiJson.get(Math.min(countRifiuti, rifiutiJson.size() - 1)).getAsString();
                                            countRifiuti++;
                                            contadino.setDialoghi(Arrays.asList(frase));
                                            engine.mostraDialogoNPCCallback(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen, this);
                                        }
                                    }
                                };
                                loopConfermaContadino.run();
                            });
                        } else {
                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("incomprensione").getAsString()));
                            engine.mostraDialogoNPCCallback(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen, this);
                        }
                    }
                };
                engine.mostraDialogoNPCCallback(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen, loopContadino);
            } else if (statoCity[0] == 3) { 
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("consegna").getAsString()));
                engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen);
                Oggetto cena = engine.getGiocatore().getInventario().cercaOggetto("Cena di pesce");
                if (cena != null) engine.getGiocatore().getInventario().rimuoviOggetto(cena);
                engine.getGiocatore().getInventario().aggiungiOggetto(engine.getTxt().getOggettoDaCatalogo(3));
                statoCity[0] = 4;
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("augurio").getAsString()));
            } else if (statoCity[0] >= 4) {
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("fretta").getAsString()));
                engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen);
            } else {
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("fretta").getAsString()));
                engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", contadino, spriteGreen);
            }
        });

        BufferedImage sfondoPiazza = null;
        try {
            sfondoPiazza = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/PiazzaCentrale.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo: " + e.getMessage());
        }

        GameScreen piazzaCentrale = new GameScreen(sfondoPiazza, zonePiazza);
        
        piazzaCentraleArr[0] = piazzaCentrale;
        engine.getSceneManager().registraScena("PIAZZA_CENTRALE", piazzaCentrale);
        

        Map<double[], Runnable> zonePorto = new HashMap<>();
        final GameScreen[] portoScreenArr = new GameScreen[1];

        String dialogoDavid = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("David").get("incontro_1").getAsString();
        Personaggio david = registraNPC("David", Arrays.asList(dialogoDavid));
        ImageIcon spriteDavid = new ImageIcon(getClass().getResource("/sprites/Personaggi/David.png"));

        double[] davidHitbox = new double[]{0.7187, 0.3525, 0.1289, 0.3745};
        zonePorto.put(davidHitbox, () -> {
            engine.mostraDialogoNPC(portoScreenArr[0], "PORTO", david, spriteDavid);
            if (!engine.getGiocatore().isPossiedeMappa()) {
                Enigma enigma1 = IstanzaEnigma.creaEnigma1(null);
                engine.getStatistics().enigmaRisolto(enigma1);
                engine.getGiocatore().setPossiedeMappa(true);
                zonePorto.remove(davidHitbox);
                david.setDialoghi(new ArrayList<>());
                // Svuoto i dialoghi e rimuovo le hitbox dalla mappa della zona
                // così il MouseListener in CursorUtil non fa apparire l'icona della mano
                ab1.setDialoghi(new ArrayList<>());
                zonePiazza.remove(hitboxAb1);
                
                ab3.setDialoghi(new ArrayList<>());
                zonePiazza.remove(hitboxAb3);
                
                ab2.setDialoghi(Arrays.asList(davidDb.get("consiglio_stalla").getAsString()));
            }
        });

        final boolean[] pescivendoloPreambleShown = {false};
        zonePorto.put(new double[]{0.1, 0.5, 0.2, 0.2}, () -> {
            if (statoCity[0] == 2) {
                Runnable startEnigma = () -> {
                    Enigma enigma2 = IstanzaEnigma.creaEnigma2(engine.getTxt().getOggettoDaCatalogo(4));
                    engine.getStatistics().iniziaEnigma(enigma2);
                    // Quando ricominciano a parlare (per gli aiuti dell'Enigma 2),
                    // assegno i nuovi testi presi dal JSON e riaggiungo le hitbox così tornano cliccabili (icona mano)
                    ab1.setDialoghi(Arrays.asList(enigma2.getAiuti().get(0)));
                    zonePiazza.put(hitboxAb1, () -> engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab1, null));
                    
                    ab2.setDialoghi(Arrays.asList(enigma2.getAiuti().get(1)));
                    
                    ab3.setDialoghi(Arrays.asList(enigma2.getAiuti().get(2)));
                    zonePiazza.put(hitboxAb3, () -> engine.mostraDialogoNPC(piazzaCentraleArr[0], "PIAZZA_CENTRALE", ab3, null));
                    Runnable loopEnigma = new Runnable() {
                        @Override
                        public void run() {
                            String risposta = JOptionPane.showInputDialog(engine.getFrame(), "Risposta (Scrivi il numero):");
                            if (risposta == null) return;
                            if (enigma2.verifica(risposta)) {
                                engine.getStatistics().enigmaRisolto(enigma2);
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("risolto").getAsString()));
                                engine.mostraDialogoNPC(portoScreenArr[0], "PORTO", pescivendolo, null);
                                statoCity[0] = 3;
                                zonePiazza.remove(hitboxAb1);
                                zonePiazza.remove(hitboxAb3);
                                ab2.setDialoghi(Arrays.asList(davidDb.get("saluto_generico").getAsString()));
                            } else {
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("risposta_errata").getAsString()));
                                engine.mostraDialogoNPCCallback(portoScreenArr[0], "PORTO", pescivendolo, null, this);
                            }
                        }
                    };
                    engine.mostraDialogoCallback(portoScreenArr[0], "PORTO", "Fantoccio", enigma2.getTesto(), null, loopEnigma);
                };

                if (pescivendoloPreambleShown[0]) {
                    startEnigma.run();
                } else {
                    pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("saluto").getAsString()));
                    Runnable loopPescivendolo = new Runnable() {
                        @Override
                        public void run() {
                            String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi al Pescivendolo?");
                            if (input == null) return;
                            if (Parser.contieneParolaChiave(input, "cena")) {
                                String EryndorText = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Pescivendolo").get("cena_green").getAsString();
                                engine.mostraDialogoCallback(portoScreenArr[0], "PORTO", engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer(), EryndorText, null, () -> {
                                    pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("richiesta").getAsString()));
                                    engine.mostraDialogoNPCCallback(portoScreenArr[0], "PORTO", pescivendolo, null, () -> {
                                        pescivendoloPreambleShown[0] = true;
                                        startEnigma.run();
                                    });
                                });
                            } else {
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("incomprensione").getAsString()));
                                engine.mostraDialogoNPCCallback(portoScreenArr[0], "PORTO", pescivendolo, null, this);
                            }
                        }
                    };
                    engine.mostraDialogoNPCCallback(portoScreenArr[0], "PORTO", pescivendolo, null, loopPescivendolo);
                }
            } else if (statoCity[0] >= 3) {
                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("ringraziamento_finale").getAsString()));
                engine.mostraDialogoNPC(portoScreenArr[0], "PORTO", pescivendolo, null);
            } else {
                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("chiuso").getAsString()));
                engine.mostraDialogoNPC(portoScreenArr[0], "PORTO", pescivendolo, null);
            }
        });

        BufferedImage sfondoPorto = null;
        try {
            sfondoPorto = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/PortoMareBlu.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo porto: " + e.getMessage());
        }
        GameScreen portoScreen = new GameScreen(sfondoPorto, zonePorto);
        portoScreen.abilitaDebugCoordinate();
        portoScreenArr[0] = portoScreen;
        
        
        engine.getSceneManager().registraScena("PORTO", portoScreen);
        
        Map<double[], Runnable> zoneStalla = new HashMap<>();
        final GameScreen[] stallaScreenArr = new GameScreen[1];
        mrCooperInteraction = () -> {
            switch (statoCity[0]) {
                case 0:
                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("saluto").getAsString()));
                    Runnable loopCooper = new Runnable() {
                        @Override
                        public void run() {
                            String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi a Mr.Cooper?");
                            if (input == null) return;
                            if (Parser.contieneParolaChiave(input, "car rozza")) {
                                mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("prezzo").getAsString()));
                                engine.mostraDialogoNPCCallback(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper, () -> {
                                    String EryndorText = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("MrCooper").get("no_soldi").getAsString();
                                    engine.mostraDialogoCallback(stallaScreenArr[0], "STALLA", engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer(), EryndorText, null, () -> {
                                        mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("proposta").getAsString()));
                                        engine.mostraDialogoNPCCallback(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper, () -> {
                                            Runnable loopConfermaCooper = new Runnable() {
                                                int countRifiuti = 0;
                                                JsonArray rifiutiJson = engine.getDbHint().getAsJsonObject("Loop_Rifiuti_Quest").getAsJsonArray("MrCooper");
                                                
                                                @Override
                                                public void run() {
                                                    int scelta = JOptionPane.showConfirmDialog(engine.getFrame(), "Accetti la proposta di Mr.Cooper?", "Scelta", JOptionPane.YES_NO_OPTION);
                                                    if (scelta == JOptionPane.YES_OPTION) {
                                                        mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("missione").getAsString()));
                                                        engine.mostraDialogoNPC(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper);
                                                        statoCity[0] = 1;
                                                    } else {
                                                        String frase = rifiutiJson.get(Math.min(countRifiuti, rifiutiJson.size() - 1)).getAsString();
                                                        countRifiuti++;
                                                        mrCooper.setDialoghi(Arrays.asList(frase));
                                                        engine.mostraDialogoNPCCallback(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper, this);
                                                    }
                                                }
                                            };
                                            loopConfermaCooper.run();
                                        });
                                    });
                                });
                            } else {
                                mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("non_capisco").getAsString()));
                                engine.mostraDialogoNPCCallback(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper, this);
                            }
                        }
                    };  engine.mostraDialogoNPCCallback(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper, loopCooper);
                    break;
                case 4:
                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("ringraziamento").getAsString()));
                    engine.mostraDialogoNPC(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper);
                    Oggetto carote = engine.getGiocatore().getInventario().cercaOggetto("Carote");
                    if (carote != null) engine.getGiocatore().getInventario().rimuoviOggetto(carote);
                    statoCity[0] = 5;
                    break;
                case 5:
                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("vai_al_bosco").getAsString()));
                    engine.mostraDialogoNPC(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper);
                    break;
                default:
                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("hai_carote").getAsString()));
                    engine.mostraDialogoNPC(stallaScreenArr[0], "STALLA", mrCooper, spriteMrCooper);
                    break;
            }
        };
        zoneStalla.put(new double[]{0.3, 0.3, 0.4, 0.5}, mrCooperInteraction);
        GameScreen stallaScreen = engine.getSceneManager().creaScenaBase("Stalla.png", zoneStalla);
        stallaScreenArr[0] = stallaScreen;
        engine.getSceneManager().registraScena("STALLA", stallaScreen);
        Map<double[], Runnable> zoneBosco = new HashMap<>();
        final GameScreen[] boscoScreenArr = new GameScreen[1];
        JsonObject foxDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Fox");
        JsonObject eryndorFoxDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Fox");
        Personaggio ladroFox = registraNPC("Fox", new ArrayList<>());
        ImageIcon spriteFox = new ImageIcon(getClass().getResource("/sprites/Personaggi/Fox.png"));
        
        foxInteraction = () -> {
            ladroFox.setDialoghi(Arrays.asList(foxDb.get("incontro_iniziale").getAsString()));
            engine.mostraDialogoNPCCallback(boscoScreenArr[0], "BOSCO", ladroFox, spriteFox, () -> {
                engine.mostraDialogoCallback(boscoScreenArr[0], "BOSCO", "Eryndor", eryndorFoxDb.get("reazione_furto").getAsString(), null, () -> {
                    ladroFox.setDialoghi(Arrays.asList(foxDb.get("ricatto").getAsString()));
                    engine.mostraDialogoNPCCallback(boscoScreenArr[0], "BOSCO", ladroFox, spriteFox, () -> {
                        engine.mostraDialogoCallback(boscoScreenArr[0], "BOSCO", "Eryndor", eryndorFoxDb.get("reazione_inseguimento").getAsString(), null, () -> {
                            ladroFox.setDialoghi(Arrays.asList(foxDb.get("richiesta_pianta").getAsString()));
                            engine.mostraDialogoNPCCallback(boscoScreenArr[0], "BOSCO", ladroFox, spriteFox, () -> {
                                engine.mostraDialogoCallback(boscoScreenArr[0], "BOSCO", "Eryndor", eryndorFoxDb.get("accordo").getAsString(), null, () -> {
                                    Oggetto tessuto = engine.getGiocatore().getInventario().cercaOggetto("Tessuto");
                                    if (tessuto != null) {
                                        engine.getGiocatore().getInventario().rimuoviOggetto(tessuto);
                                    }
                                    statoCity[0] = 6;
                                    ladroFox.setDialoghi(new ArrayList<>());
                                });
                            });
                        });
                    });
                });
            });
        };

        zoneBosco.put(new double[]{0.4, 0.4, 0.2, 0.2}, () -> {
            System.out.println("Click su Fox/Cartello, da implementare in seguito.");
        });
        GameScreen boscoScreen = engine.getSceneManager().creaScenaBase("BoscoLosco.png", zoneBosco);
        boscoScreenArr[0] = boscoScreen;
        engine.getSceneManager().registraScena("BOSCO", boscoScreen);
        
        engine.getSceneManager().registraScena("BOSCO_DEEP", engine.getSceneManager().creaScenaBase("BoscoINN.png", null));
        
        Map<double[], Runnable> zoneKarundis = new HashMap<>();
        zoneKarundis.put(new double[]{0.4, 0.4, 0.2, 0.2}, () -> engine.getSceneManager().mostraScena("GROTTA"));
        GameScreen karundisScreen = engine.getSceneManager().creaScenaBase("Karundis.png", zoneKarundis);
        engine.getSceneManager().registraScena("KARUNDIS", karundisScreen);

        Map<double[], Runnable> zoneGrotta = new HashMap<>();
        final GameScreen[] grottaScreenArr = new GameScreen[1];
        zoneGrotta.put(new double[]{0.3, 0.3, 0.4, 0.4}, () -> {
            Enigma enigma4 = IstanzaEnigma.creaEnigma4((aeg.giocomap.Model.Oggetti.Spada) engine.getTxt().getOggettoDaCatalogo(10));
            engine.getStatistics().iniziaEnigma(enigma4);
            Runnable loopEnigma = new Runnable() {
                @Override
                public void run() {
                    String risposta = JOptionPane.showInputDialog(engine.getFrame(), "Risposta:");
                    if (risposta == null) return;
                    if (enigma4.verifica(risposta)) {
                        engine.getStatistics().enigmaRisolto(enigma4);
                        engine.mostraDialogoCallback(grottaScreenArr[0], "GROTTA", "Eryndor", engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Grotta").get("successo_spada").getAsString(), null, null);
                    } else {
                        engine.mostraDialogoCallback(grottaScreenArr[0], "GROTTA", "Eryndor", engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Grotta").get("fallimento_spada").getAsString(), null, this);
                    }
                }
            };
            engine.mostraDialogoCallback(grottaScreenArr[0], "GROTTA", "Incudine", enigma4.getTesto(), null, loopEnigma);
        });
        GameScreen grottaScreen = engine.getSceneManager().creaScenaBase("GrottaDellaFucina.png", zoneGrotta);
        grottaScreenArr[0] = grottaScreen;
        engine.getSceneManager().registraScena("GROTTA", grottaScreen);
        
        engine.getSceneManager().registraScena("INGRESSO_PALAZZO", engine.getSceneManager().creaScenaBase("CancelloCastello.png", null));
        engine.getSceneManager().registraScena("SCALE", engine.getSceneManager().creaScenaBase("ScalePalazzo.png", null));
        
        Map<double[], Runnable> zoneCripta = new HashMap<>();
        final GameScreen[] criptaScreenArr = new GameScreen[1];
        JsonObject eripetaDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta");
        Personaggio eripeta = registraNPC("Eripeta", Arrays.asList(eripetaDb.get("rifiuto").getAsString()));
        zoneCripta.put(new double[]{0.4, 0.4, 0.2, 0.2}, () -> {
            EnigmaSceltaMultipla enigma5 = IstanzaEnigma.creaEnigma5(engine.getTxt().getOggettoDaCatalogo(9));
            engine.getStatistics().iniziaEnigma(enigma5);
            Runnable loopEnigma = new Runnable() {
                @Override
                public void run() {
                    String[] opzioni = enigma5.getOpzioni().toArray(new String[0]);
                    int scelta = JOptionPane.showOptionDialog(engine.getFrame(), "Quante ferite?", "Enigma del Vincolo", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opzioni, opzioni[0]);
                    if (scelta < 0) return;
                    if (enigma5.verifica(String.valueOf(scelta))) {
                        engine.getStatistics().enigmaRisolto(enigma5);
                        eripeta.setDialoghi(Arrays.asList(eripetaDb.get("consenso").getAsString()));
                        engine.mostraDialogoNPC(criptaScreenArr[0], "CRIPTA_ERIPETA", eripeta, null);
                    } else {
                        eripeta.setDialoghi(Arrays.asList(eripetaDb.get("errore").getAsString()));
                        engine.mostraDialogoNPCCallback(criptaScreenArr[0], "CRIPTA_ERIPETA", eripeta, null, this);
                    }
                }
            };
            engine.mostraDialogoCallback(criptaScreenArr[0], "CRIPTA_ERIPETA", "Eripeta", enigma5.getTesto(), null, loopEnigma);
        });
        GameScreen criptaScreen = engine.getSceneManager().creaScenaBase("Cripta.png", zoneCripta);
        criptaScreenArr[0] = criptaScreen;
        engine.getSceneManager().registraScena("CRIPTA_ERIPETA", criptaScreen);
        
        Map<double[], Runnable> zonePalazzo = new HashMap<>();
        final GameScreen[] palazzoScreenArr = new GameScreen[1];
        JsonObject marienDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Marien");
        Personaggio marien = registraNPC("Principessa Marien", Arrays.asList(marienDb.get("sfida").getAsString()));
        zonePalazzo.put(new double[]{0.4, 0.4, 0.2, 0.2}, () -> {
            if (engine.getGiocatore().isEnigmaRisolto("Enigma_7_Principessa")) {
                marien.setDialoghi(Arrays.asList("Sei già il mio sposo! Il regno è salvo."));
                engine.mostraDialogoNPC(palazzoScreenArr[0], "PALAZZO_PRINCIPESSA", marien, null);
                return;
            }
            EnigmaSceltaMultipla enigma7 = IstanzaEnigma.creaEnigma7(new Oggetto(8, "Titolo Nobile", "Hai vinto il cuore della principessa e il titolo."));
            engine.getStatistics().iniziaEnigma(enigma7);
            Runnable loopEnigma = new Runnable() {
                @Override
                public void run() {
                    String[] opzioni = enigma7.getOpzioni().toArray(new String[0]);
                    int scelta = JOptionPane.showOptionDialog(engine.getFrame(), "Scegli l'ordine:", "Enigma della Principessa", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opzioni, opzioni[0]);
                    if (scelta < 0) return;
                    if (enigma7.verifica(String.valueOf(scelta))) {
                        engine.getStatistics().enigmaRisolto(enigma7);
                        marien.setDialoghi(Arrays.asList(marienDb.get("vittoria_finale").getAsString()));
                        engine.mostraDialogoNPC(palazzoScreenArr[0], "PALAZZO_PRINCIPESSA", marien, null);
                    } else {
                        engine.mostraDialogoCallback(palazzoScreenArr[0], "PALAZZO_PRINCIPESSA", "Principessa Marien", marienDb.get("errore_cacciata").getAsString(), null, () -> {
                            engine.mostraDialogoCallback(palazzoScreenArr[0], "PALAZZO_PRINCIPESSA", "Eryndor", engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Marien").get("errore_ordine").getAsString(), null, this);
                        });
                    }
                }
            };
            engine.mostraDialogoCallback(palazzoScreenArr[0], "PALAZZO_PRINCIPESSA", "Principessa Marien", enigma7.getTesto(), null, loopEnigma);
        });
        GameScreen palazzoScreen = engine.getSceneManager().creaScenaBase("SalaDellaPrincipessa.png", zonePalazzo);
        palazzoScreenArr[0] = palazzoScreen;
        engine.getSceneManager().registraScena("PALAZZO_PRINCIPESSA", palazzoScreen);

        LetteraScreen schermata_retro = new LetteraScreen(letteraRetro, () -> {
            System.out.println("DEBUG: Lettera retro finita, gioco START");
            
            engine.getGiocatore().setPossiedeInventario(true);
            
            engine.getSceneManager().mostraScena("PIAZZA_CENTRALE");
        });
        engine.getSceneManager().registraScena("LETTERA_RETRO", schermata_retro);

        LetteraScreen schermata_lettera = new LetteraScreen(lettera, () -> {
            System.out.println("DEBUG: Lettera finita, mostro retro");
            
            engine.getSceneManager().mostraScena("LETTERA_RETRO");

        });
        engine.getSceneManager().registraScena("LETTERA", schermata_lettera);
    }

    private Personaggio registraNPC(String nome, List<String> dialoghi) {
        Personaggio pg = new Personaggio(nome);
        pg.setDialoghi(dialoghi);
        registroNPC.put(nome, pg);
        return pg;
    }

    private Fantoccio registraFantoccio(String chiaveRegistro, List<String> dialoghi) {
        Fantoccio f = new Fantoccio();
        f.setDialoghi(dialoghi);
        registroNPC.put(chiaveRegistro, f);
        return f;
    }
}
