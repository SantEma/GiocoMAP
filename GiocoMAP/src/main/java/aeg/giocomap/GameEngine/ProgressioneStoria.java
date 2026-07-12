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
import java.util.function.Consumer;

/**
 *
 * @author emanuele
 */
public class ProgressioneStoria {

    private final GameEngine engine;

    // Struttura dati per astrarre la logica dei percorsi (routing table)
    private final Map<String, Map<String, Runnable>> collegamentiMappa = new HashMap<>();

    // Registro dei personaggi attivi nella partita
    private final Map<String, Personaggio> registroNPC = new HashMap<>();

    // Stato logico della fase "Città con il porto"
    private StatoStoria statoAttuale = StatoStoria.INIZIO;
    private Personaggio ab1;
    private Personaggio ab2;
    private Personaggio ab3;
    private Personaggio mrCooper;
    private Personaggio contadino;
    private Personaggio pescivendolo;
    private Personaggio david;
    private Personaggio ladroFox;
    private Personaggio guardiaReale;
    private Personaggio eripeta;
    private Personaggio marien;
    
    // Azione interattiva di Eripeta (rimpiazza il vecchio gestisciEripetaInCripta)
    private Runnable interazioneEripeta;

    private GameScreen palazzoScreen;
    private GameScreen criptaScreen;
    private GameScreen ingressoScreen;
    private GameScreen grottaScreen;
    private GameScreen karundisScreen;
    private GameScreen boscoDeepScreen;
    private GameScreen boscoScreen;
    private GameScreen stallaScreen;
    private GameScreen portoScreen;
    private GameScreen piazzaCentrale;

    // Variabili condivise tra i metodi costruisciPiazzaCentrale / costruisciPorto / costruisciStalla / ecc.
    private Map<double[], Runnable> zonePiazza;
    private double[] hitboxAb1;
    private double[] hitboxAb3;
    private Runnable interazioneAb1;
    private Runnable interazioneAb3;
    private JsonObject davidDb;
    private JsonObject mrCooperDb;
    private JsonObject pescivendoloDb;
    private ImageIcon spriteMrCooper;
    private List<String> hints;
    private List<String> idleDialogs;

    // Stato logico del primo accesso al palazzo per mostrare solo una volta la frase
    private boolean primoAccessoPalazzo = true;

    // Indica se abbiamo già parlato alla guardia del cancello almeno una volta
    private boolean parlatoConGuardia = false;
    
    // Azione interattiva di Mr. Cooper
    private Runnable mrCooperInteraction;
    private Runnable foxInteraction;

    // Azione che ri-registra la hitbox di David al Porto quando la storia lo rende
    // di nuovo interpellabile (ritorno mandato da Eripeta, a statoCity >= 11)
    private Runnable riattivaDavidDopoMappa;

    // Azione che abilita le hitbox degli abitanti 1 e 3 in Piazza (aiuti Enigma 5).
    // Va invocata solo quando David annuncia l'Enigma del Vincolo, non prima.
    private Runnable attivaAiutiEnigma5;

    // Azione che rimuove le hitbox degli abitanti 1 e 3 in Piazza una volta risolto
    // l'Enigma 5 (i loro aiuti non servono più), simmetrica all'attivazione
    private Runnable disattivaAiutiEnigma5;

    // Riferimenti agli NPC di Karundis per poter aggiornare i loro dialoghi
    private Personaggio npcKarundis1;
    private Personaggio npcKarundis2;

    public ProgressioneStoria(GameEngine engine) {
        this.engine = engine;
    }

    // stato di avanzamento della fase città (per salvataggio/caricamento)
    public int getStatoCity() {
        return statoAttuale.getValore();
    }

    public void setStatoCity(int valore) {
        statoAttuale = StatoStoria.daValore(valore);
    }

    public boolean isPrimoAccessoPalazzo() {
        return primoAccessoPalazzo;
    }

    public void setPrimoAccessoPalazzo(boolean valore) {
        this.primoAccessoPalazzo = valore;
    }

    public boolean isParlatoConGuardia() {
        return parlatoConGuardia;
    }

    public void setParlatoConGuardia(boolean valore) {
        this.parlatoConGuardia = valore;
    }

    public void impostaFrecceLogica() {
        inizializzaRoot();
        
        engine.getFrame().setFrecceListener(
            e -> eseguiCollegamento(CostantiMappa.NORD),
            e -> eseguiCollegamento(CostantiMappa.SUD),
            e -> eseguiCollegamento(CostantiMappa.EST),
            e -> eseguiCollegamento(CostantiMappa.OVEST)
        );
    }

    private void inizializzaRoot() {
        registraCollegamentoSemplice(CostantiMappa.PIAZZA_CENTRALE, CostantiMappa.NORD, CostantiMappa.PORTO);
        registraCollegamentoSemplice(CostantiMappa.PORTO, CostantiMappa.SUD, CostantiMappa.PIAZZA_CENTRALE);
        
        registraCollegamento(CostantiMappa.PIAZZA_CENTRALE, CostantiMappa.SUD, () -> {
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
        
        registraCollegamento(CostantiMappa.PIAZZA_CENTRALE, CostantiMappa.OVEST, () -> {
            if (engine.getGiocatore().isPossiedeMappa()) { 
                engine.getSceneManager().mostraScena(CostantiMappa.STALLA);
                if ((statoAttuale == StatoStoria.INIZIO || statoAttuale == StatoStoria.CONSEGNATA_CENA) && mrCooperInteraction != null) {
                    mrCooperInteraction.run();
                }
            } else {
                GameScreen piazza = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.PIAZZA_CENTRALE);
                engine.mostraDialogoNPC(piazza, CostantiMappa.PIAZZA_CENTRALE, fantoccioOvest, null);
            }
        });
        
        String testoBlocco = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Guardiano").get("stop_carrozza").getAsString();
        Personaggio guardiano = registraNPC("Guardiano", Arrays.asList(testoBlocco));
        
        registraCollegamento(CostantiMappa.PIAZZA_CENTRALE, CostantiMappa.EST, () -> {
            if (statoAttuale == StatoStoria.CAROTE_CONSEGNATE) {
                engine.getSceneManager().mostraScena(CostantiMappa.BOSCO);
                if (foxInteraction != null) foxInteraction.run();
            } else if (statoAttuale.getValore() > 5) {
                engine.getSceneManager().mostraScena(CostantiMappa.BOSCO);
            } else {
                GameScreen piazza = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.PIAZZA_CENTRALE);
                engine.mostraDialogoNPC(piazza, CostantiMappa.PIAZZA_CENTRALE, guardiano, null);
            }
        });

        registraCollegamentoSemplice(CostantiMappa.STALLA, CostantiMappa.NORD, CostantiMappa.PIAZZA_CENTRALE);
        
        registraCollegamentoSemplice(CostantiMappa.BOSCO, CostantiMappa.SUD, CostantiMappa.PIAZZA_CENTRALE);
        registraCollegamento(CostantiMappa.BOSCO, CostantiMappa.NORD, () -> {
            if (statoAttuale.getValore() >= 7) {
                engine.getSceneManager().mostraScena(CostantiMappa.KARUNDIS);
            } else {
                String bloccoFox = engine.getDbWallOfText().getAsJsonObject("Schermo").get("blocco_fox_karundis").getAsString();
                GameScreen bosco = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.BOSCO);
                engine.mostraDialogoCallback(bosco, CostantiMappa.BOSCO, "Fox", bloccoFox, new ImageIcon(getClass().getResource("/sprites/Personaggi/Fox.png")), null);
            }
        });
        registraCollegamentoSemplice(CostantiMappa.BOSCO, CostantiMappa.OVEST, CostantiMappa.BOSCO_DEEP);
        
        registraCollegamento(CostantiMappa.BOSCO_DEEP, CostantiMappa.EST, () -> {
            engine.getSceneManager().mostraScena(CostantiMappa.BOSCO);
            if (statoAttuale == StatoStoria.INCONTRO_FOX) {
                boolean haFiore = engine.getGiocatore().getInventario().cercaOggetto("Fiore Viola") != null ||
                                  engine.getGiocatore().getInventario().cercaOggetto("Fiore Rosso") != null ||
                                  engine.getGiocatore().getInventario().cercaOggetto("Fiore Blu") != null;
                if (haFiore && foxInteraction != null) {
                    foxInteraction.run();
                }
            }
        });
        
        registraCollegamentoSemplice(CostantiMappa.KARUNDIS, CostantiMappa.OVEST, CostantiMappa.BOSCO);
        
        String testoBloccoKarundis = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Guardiano").get("stop_palazzo").getAsString();
        Personaggio fantoccioNord = registraNPC("Guardiano", Arrays.asList(testoBloccoKarundis));
        
        registraCollegamento(CostantiMappa.KARUNDIS, CostantiMappa.NORD, () -> {
            if (engine.getGiocatore().getInventario().cercaOggetto("Spada Sincro") != null) {
                if (primoAccessoPalazzo) {
                    primoAccessoPalazzo = false;
                    // Al primo ingresso al cancello, attiviamo gli indizi per la chiave negli abitanti di Karundis
                    if (npcKarundis1 != null && npcKarundis2 != null) {
                        List<String> hints1 = JsonLoader.estraiLista(engine.getDbHint(), "Ricerca_Chiave_Castello");
                        npcKarundis1.setDialoghi(Arrays.asList(hints1.get(0)));
                        npcKarundis2.setDialoghi(Arrays.asList(hints1.get(1)));
                    }
                    if (engine.getGiocatore().isEnigmaRisolto("EVENT_BLOCCO_KARUNDIS")) {
                        String testoPassaggio = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("inizio").get("passaggio_cavaglieri").getAsString();
                        GameScreen karundis = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.KARUNDIS);
                        engine.mostraDialogoCallback(karundis, CostantiMappa.KARUNDIS, "Eryndor", testoPassaggio, null, () -> {
                            engine.getSceneManager().mostraScena(CostantiMappa.INGRESSO_PALAZZO);
                        });
                    } else {
                        engine.getSceneManager().mostraScena(CostantiMappa.INGRESSO_PALAZZO);
                    }
                } else {
                    engine.getSceneManager().mostraScena(CostantiMappa.INGRESSO_PALAZZO);
                }
            } else {
                engine.getGiocatore().aggiungiEnigmaRisolto("EVENT_BLOCCO_KARUNDIS");
                GameScreen karundis = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.KARUNDIS);
                engine.mostraDialogoNPC(karundis, CostantiMappa.KARUNDIS, fantoccioNord, null);
            }
        });
        
        registraCollegamentoSemplice(CostantiMappa.GROTTA, CostantiMappa.SUD, CostantiMappa.KARUNDIS);
        
        registraCollegamentoSemplice(CostantiMappa.INGRESSO_PALAZZO, CostantiMappa.SUD, CostantiMappa.KARUNDIS);
        
        String testoBloccoChiave = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Guardiano").get("stop_chiave").getAsString();
        Fantoccio bloccoChiaveNord = registraFantoccio("Blocco_Chiave_Nord", Arrays.asList(testoBloccoChiave));
        
        registraCollegamento(CostantiMappa.INGRESSO_PALAZZO, CostantiMappa.NORD, () -> {
            if (statoAttuale.getValore() >= 9) {
                engine.getSceneManager().mostraScena(CostantiMappa.SCALE);
                if (statoAttuale == StatoStoria.CHIAVE_CONSEGNATA) {
                    avviaIntercettazioneEripeta();
                }
            } else {
                GameScreen ingresso = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.INGRESSO_PALAZZO);
                engine.mostraDialogoNPC(ingresso, CostantiMappa.INGRESSO_PALAZZO, bloccoChiaveNord, null);
            }
        });
        
        registraCollegamentoSemplice(CostantiMappa.SCALE, CostantiMappa.SUD, CostantiMappa.INGRESSO_PALAZZO);
        registraCollegamento(CostantiMappa.SCALE, CostantiMappa.NORD, () -> {
            if (statoAttuale.getValore() >= 15) {
                engine.getSceneManager().mostraScena(CostantiMappa.PALAZZO_PRINCIPESSA);
            } else {
                GameScreen scale = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.SCALE);
                String rifiuto = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta").get("rifiuto").getAsString();
                engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", rifiuto, new ImageIcon(getClass().getResource("/sprites/Personaggi/Eripeta.png")), null);
            }
        });
        registraCollegamento(CostantiMappa.SCALE, CostantiMappa.OVEST, () -> {
            engine.getSceneManager().mostraScena(CostantiMappa.CRIPTA_ERIPETA);
            if (statoAttuale.getValore() < 15) {
                if (interazioneEripeta != null) interazioneEripeta.run();
            }
        });
        
        registraCollegamentoSemplice(CostantiMappa.PALAZZO_PRINCIPESSA, CostantiMappa.SUD, CostantiMappa.SCALE);
        registraCollegamentoSemplice(CostantiMappa.CRIPTA_ERIPETA, CostantiMappa.EST, CostantiMappa.SCALE);
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
        inizializzaPersonaggiPrincipali();
        costruisciPiazzaCentrale();
        costruisciPorto();
        costruisciStalla();
        costruisciBosco();
        costruisciKarundis();
        costruisciGrotta();
        costruisciIngressoPalazzo();
        costruisciCripta();
        costruisciPalazzoPrincipessa();
        costruisciLettere();
    }

    private void inizializzaPersonaggiPrincipali() {

        hints = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");

        mrCooperDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("MrCooper");
        pescivendoloDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Pescivendolo");
        davidDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("David");
        spriteMrCooper = new ImageIcon(getClass().getResource("/sprites/Personaggi/MrCooper.png"));
        idleDialogs = JsonLoader.estraiLista(engine.getDbHint(), "Dialoghi_Generici_Idle");
    }

    private void costruisciPiazzaCentrale() {
        zonePiazza = new HashMap<>();

        ab1 = registraNPC("Abitante 1", Arrays.asList(hints.get(0)));
        ab2 = registraNPC("Abitante 2", Arrays.asList(hints.get(1)));
        ab3 = registraNPC("Abitante 3", Arrays.asList(hints.get(2)));

        JsonObject contadinoDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Contadino_Green");

        mrCooper = registraNPC("Mr. Cooper", Arrays.asList(
            mrCooperDb.get("saluto").getAsString() + "\n" +
            mrCooperDb.get("prezzo").getAsString() + "\n" +
            mrCooperDb.get("proposta").getAsString()
        ));
        
        contadino = registraNPC("Contadino Green", Arrays.asList(
            contadinoDb.get("saluto").getAsString() + "\n" +
            contadinoDb.get("richiesta").getAsString()
        ));
        
        pescivendolo = registraNPC("Pescivendolo", Arrays.asList(
            pescivendoloDb.get("saluto").getAsString() + "\n" +
            pescivendoloDb.get("richiesta").getAsString()
        ));

        // spriteMrCooper è già un campo di istanza, inizializzato in inizializzaPersonaggiPrincipali()
        ImageIcon spriteGreen = new ImageIcon(getClass().getResource("/sprites/Personaggi/Green.png"));

        hitboxAb1 = CostantiHitbox.PIAZZA_ABITANTE_1;
        double[] hitboxAb2 = CostantiHitbox.PIAZZA_ABITANTE_2;
        hitboxAb3 = CostantiHitbox.PIAZZA_ABITANTE_3;

        // Interazione abitante 1: hint Enigma 1 (prima della mappa) o hint Enigma 5 (ritorno da Eripeta).
        // Estratta in variabile per poterla ri-registrare quando la storia riabilita gli aiuti.
        interazioneAb1 = () -> {
            if (!engine.getGiocatore().isPossiedeMappa()) {
                List<String> hintsE1 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");
                Personaggio tempAb1 = new Personaggio("Abitante 1");
                tempAb1.setDialoghi(Arrays.asList(hintsE1.get(0)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb1, null);
            } else if (statoAttuale == StatoStoria.ACCUSA_ERIPETA_SUPERATA || statoAttuale == StatoStoria.DAVID_INTERPELLATO) {
                List<String> hintsE5 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_5_Vincolo");
                Personaggio tempAb1 = new Personaggio("Abitante 1");
                tempAb1.setDialoghi(Arrays.asList(hintsE5.get(0)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb1, null);
            }
        };
        if (!engine.getGiocatore().isPossiedeMappa() || statoAttuale == StatoStoria.ACCUSA_ERIPETA_SUPERATA || statoAttuale == StatoStoria.DAVID_INTERPELLATO) {
            zonePiazza.put(hitboxAb1, interazioneAb1);
        }

        zonePiazza.put(hitboxAb2, () -> {
            Personaggio tempAb2 = new Personaggio("Abitante 2");
            if (!engine.getGiocatore().isPossiedeMappa()) {
                List<String> hintsE1 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");
                tempAb2.setDialoghi(Arrays.asList(hintsE1.get(1)));
            } else if (statoAttuale.getValore() < 3) {
                tempAb2.setDialoghi(Arrays.asList(davidDb.get("consiglio_stalla").getAsString()));
            } else {
                tempAb2.setDialoghi(Arrays.asList(davidDb.get("saluto_generico").getAsString()));
            }
            engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb2, null);
        });

        // Interazione abitante 3: hint Enigma 1 (prima della mappa) o hint Enigma 5 (ritorno da Eripeta).
        interazioneAb3 = () -> {
            if (!engine.getGiocatore().isPossiedeMappa()) {
                List<String> hintsE1 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");
                Personaggio tempAb3 = new Personaggio("Abitante 3");
                tempAb3.setDialoghi(Arrays.asList(hintsE1.get(2)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb3, null);
            } else if (statoAttuale == StatoStoria.ACCUSA_ERIPETA_SUPERATA || statoAttuale == StatoStoria.DAVID_INTERPELLATO) {
                List<String> hintsE5 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_5_Vincolo");
                Personaggio tempAb3 = new Personaggio("Abitante 3");
                tempAb3.setDialoghi(Arrays.asList(hintsE5.get(1)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb3, null);
            }
        };
        if (!engine.getGiocatore().isPossiedeMappa() || statoAttuale == StatoStoria.ACCUSA_ERIPETA_SUPERATA || statoAttuale == StatoStoria.DAVID_INTERPELLATO) {
            zonePiazza.put(hitboxAb3, interazioneAb3);
        }

        zonePiazza.put(CostantiHitbox.PIAZZA_CONTADINO, () -> {
            if (statoAttuale == StatoStoria.MISSIONE_COOPER_ACCETTATA) { 
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("saluto").getAsString()));
                Runnable loopContadino = new Runnable() {
                    @Override
                    public void run() {
                        String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi al Contadino Green?");
                        if (input == null) return;
                        if (Parser.contieneRadiceParola(input, "carot*")) {
                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("richiesta").getAsString()));
                            engine.mostraDialogoNPCCallback(ProgressioneStoria.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, () -> {
                                Runnable loopConfermaContadino = new Runnable() {
                                    int countRifiuti = 0;
                                    JsonArray rifiutiJson = engine.getDbHint().getAsJsonObject("Loop_Rifiuti_Quest").getAsJsonArray("Contadino_Green");
                                    
                                    @Override
                                    public void run() {
                                        int scelta = JOptionPane.showConfirmDialog(engine.getFrame(), "Accetti la proposta del Contadino?", "Scelta", JOptionPane.YES_NO_OPTION);
                                        if (scelta == JOptionPane.YES_OPTION) {
                                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("ringraziamento").getAsString()));
                                            engine.mostraDialogoNPC(ProgressioneStoria.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen);
                                            setStatoCity(2); 
                                        } else {
                                            String frase = rifiutiJson.get(Math.min(countRifiuti, rifiutiJson.size() - 1)).getAsString();
                                            countRifiuti++;
                                            contadino.setDialoghi(Arrays.asList(frase));
                                            engine.mostraDialogoNPCCallback(ProgressioneStoria.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, this);
                                        }
                                    }
                                };
                                loopConfermaContadino.run();
                            });
                        } else {
                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("incomprensione").getAsString()));
                            engine.mostraDialogoNPCCallback(ProgressioneStoria.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, this);
                        }
                    }
                };
                engine.mostraDialogoNPCCallback(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, loopContadino);
            } else if (statoAttuale == StatoStoria.ENIGMA_PESCIVENDOLO_RISOLTO) { 
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("consegna").getAsString()));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen);
                Oggetto cena = engine.getGiocatore().getInventario().cercaOggetto("Cena di pesce");
                if (cena != null) engine.getGiocatore().getInventario().rimuoviOggetto(cena);
                engine.getGiocatore().getInventario().aggiungiOggetto(engine.getTxt().getOggettoDaCatalogo(3));
                setStatoCity(4);
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("augurio").getAsString()));
            } else if (statoAttuale.getValore() >= 4) {
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("fretta").getAsString()));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen);
            } else {
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("fretta").getAsString()));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen);
            }
        });

        BufferedImage sfondoPiazza = null;
        try {
            sfondoPiazza = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/PiazzaCentrale.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo: " + e.getMessage());
        }

        GameScreen piazzaCentrale = new GameScreen(sfondoPiazza, zonePiazza);
        
        this.piazzaCentrale = piazzaCentrale;
        engine.getSceneManager().registraScena(CostantiMappa.PIAZZA_CENTRALE, piazzaCentrale);
    }

    private void costruisciPorto() {
        Map<double[], Runnable> zonePorto = new HashMap<>();

        String dialogoDavid = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("David").get("incontro_1").getAsString();
        david = registraNPC("David", Arrays.asList(dialogoDavid));
        ImageIcon spriteDavid = new ImageIcon(getClass().getResource("/sprites/Personaggi/David.png"));

        double[] davidHitbox = CostantiHitbox.PIAZZA_CONTADINO;

        // Ripristina la hitbox di David con il comportamento "dopo la mappa".
        // Viene invocata sia al caricamento di un salvataggio avanzato, sia live
        // quando l'accusa di Eripeta porta a statoCity 11 (vedi avviaAccusaEripeta).
        attivaAiutiEnigma5 = () -> {
            zonePiazza.put(hitboxAb1, interazioneAb1);
            zonePiazza.put(hitboxAb3, interazioneAb3);
        };

        riattivaDavidDopoMappa = () -> {
            zonePorto.put(davidHitbox, () ->
                    gestisciDavidDopoMappa(this.portoScreen, david, spriteDavid, davidDb));
            // Gli aiuti degli abitanti 1 e 3 si abilitano solo dopo che David annuncia
            // l'Enigma del Vincolo (statoCity 12). A statoCity 11 (appena tornato da
            // Eripeta) l'enigma non è ancora stato dettato, quindi niente aiuti.
            // Qui copro il caso del salvataggio ricaricato con enigma già in corso.
            if (statoAttuale == StatoStoria.DAVID_INTERPELLATO) {
                attivaAiutiEnigma5.run();
            }
        };

        disattivaAiutiEnigma5 = () -> {
            zonePiazza.remove(hitboxAb1);
            zonePiazza.remove(hitboxAb3);
        };

        if (!engine.getGiocatore().isPossiedeMappa()) {
            zonePorto.put(davidHitbox, () -> {
                engine.mostraDialogoNPC(this.portoScreen, CostantiMappa.PORTO, david, spriteDavid);
                Enigma enigma1 = IstanzaEnigma.creaEnigma1(null);
                engine.getStatistics().enigmaRisolto(enigma1);
                engine.getGiocatore().setPossiedeMappa(true);
                
                ab1.setDialoghi(new ArrayList<>());
                zonePiazza.remove(hitboxAb1);
                
                ab3.setDialoghi(new ArrayList<>());
                zonePiazza.remove(hitboxAb3);
                
                ab2.setDialoghi(Arrays.asList(davidDb.get("consiglio_stalla").getAsString()));
                
                // Rendi David non cliccabile per il resto della fase iniziale
                zonePorto.remove(davidHitbox);
            });
        } else if (statoAttuale.getValore() >= 11) {
            // Salvataggio ricaricato in fase avanzata: la hitbox va ripristinata subito
            riattivaDavidDopoMappa.run();
        }

        final boolean[] pescivendoloPreambleShown = {false};
        zonePorto.put(CostantiHitbox.PORTO_PESCIVENDOLO, () -> {
            if (statoAttuale == StatoStoria.PARLATO_CONTADINO) {
                Runnable startEnigma = () -> {
                    Enigma enigma2 = IstanzaEnigma.creaEnigma2(engine.getTxt().getOggettoDaCatalogo(4));
                    engine.getStatistics().iniziaEnigma(enigma2);
                    // Quando ricominciano a parlare (per gli aiuti dell'Enigma 2),
                    // assegno i nuovi testi presi dal JSON e riaggiungo le hitbox così tornano cliccabili (icona mano)
                    ab1.setDialoghi(Arrays.asList(enigma2.getAiuti().get(0)));
                    zonePiazza.put(hitboxAb1, () -> engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, ab1, null));
                    
                    ab2.setDialoghi(Arrays.asList(enigma2.getAiuti().get(1)));
                    zonePiazza.put(CostantiHitbox.PIAZZA_ABITANTE_2, () -> engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, ab2, null));
                    
                    ab3.setDialoghi(Arrays.asList(enigma2.getAiuti().get(2)));
                    zonePiazza.put(hitboxAb3, () -> engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, ab3, null));
                    Runnable loopEnigma = new Runnable() {
                        @Override
                        public void run() {
                            String risposta = JOptionPane.showInputDialog(engine.getFrame(), "Risposta (Scrivi il numero):");
                            if (risposta == null) return;
                            if (enigma2.verifica(risposta)) {
                                engine.getStatistics().enigmaRisolto(enigma2);
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("risolto").getAsString()));
                                engine.mostraDialogoNPC(ProgressioneStoria.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null);
                                setStatoCity(3);
                                zonePiazza.remove(hitboxAb1);
                                zonePiazza.remove(hitboxAb3);
                                ab2.setDialoghi(Arrays.asList(davidDb.get("saluto_generico").getAsString()));
                            } else {
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("risposta_errata").getAsString()));
                                engine.mostraDialogoNPCCallback(ProgressioneStoria.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, this);
                            }
                        }
                    };
                    engine.mostraDialogoCallback(this.portoScreen, CostantiMappa.PORTO, "Fantoccio", enigma2.getTesto(), null, loopEnigma);
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
                            if (Parser.contieneRadiceParola(input, "cen*")) {
                                String EryndorText = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Pescivendolo").get("cena_green").getAsString();
                                engine.mostraDialogoCallback(ProgressioneStoria.this.portoScreen, CostantiMappa.PORTO, engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer(), EryndorText, null, () -> {
                                    pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("richiesta").getAsString()));
                                    engine.mostraDialogoNPCCallback(ProgressioneStoria.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, () -> {
                                        pescivendoloPreambleShown[0] = true;
                                        startEnigma.run();
                                    });
                                });
                            } else {
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("incomprensione").getAsString()));
                                engine.mostraDialogoNPCCallback(ProgressioneStoria.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, this);
                            }
                        }
                    };
                    engine.mostraDialogoNPCCallback(this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, loopPescivendolo);
                }
            } else if (statoAttuale.getValore() >= 3) {
                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("ringraziamento_finale").getAsString()));
                engine.mostraDialogoNPC(this.portoScreen, CostantiMappa.PORTO, pescivendolo, null);
            } else {
                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("chiuso").getAsString()));
                engine.mostraDialogoNPC(this.portoScreen, CostantiMappa.PORTO, pescivendolo, null);
            }
        });

        BufferedImage sfondoPorto = null;
        try {
            sfondoPorto = ImageIO.read(getClass().getResourceAsStream("/sprites/Luoghi/PortoMareBlu.png"));
        } catch (IOException e) {
            System.err.println("Errore caricamento sfondo porto: " + e.getMessage());
        }
        GameScreen portoScreen = new GameScreen(sfondoPorto, zonePorto);
        portoScreen.abilitaDebugCoordinate(); // disabilitato per test
        this.portoScreen = portoScreen;
        
        
        engine.getSceneManager().registraScena(CostantiMappa.PORTO, portoScreen);
    }

    private void costruisciStalla() {
        Map<double[], Runnable> zoneStalla = new HashMap<>();
        mrCooperInteraction = () -> {
            if (statoAttuale.getValore() >= 7) { // Da ENIGMA_FIORI_RISOLTO in poi (chiave ottenuta)
                mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("buona_fortuna").getAsString()));
                engine.mostraDialogoNPC(this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
            } else if (statoAttuale.getValore() >= 5) { // Da CAROTE_CONSEGNATE in poi
                mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("vai_al_bosco").getAsString()));
                engine.mostraDialogoNPC(this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
            } else {
                switch (statoAttuale) {
                    case INIZIO:
                        mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("saluto").getAsString()));
                        Runnable loopCooper = new Runnable() {
                            @Override
                            public void run() {
                                String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi a Mr.Cooper?");
                                if (input == null) return;
                                if (Parser.contieneRadiceParola(input, "carroz*")) {
                                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("prezzo").getAsString()));
                                    engine.mostraDialogoNPCCallback(ProgressioneStoria.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, () -> {
                                        String EryndorText = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("MrCooper").get("no_soldi").getAsString();
                                        engine.mostraDialogoCallback(ProgressioneStoria.this.stallaScreen, CostantiMappa.STALLA, engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer(), EryndorText, null, () -> {
                                            mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("proposta").getAsString()));
                                            engine.mostraDialogoNPCCallback(ProgressioneStoria.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, () -> {
                                                Runnable loopConfermaCooper = new Runnable() {
                                                    int countRifiuti = 0;
                                                    JsonArray rifiutiJson = engine.getDbHint().getAsJsonObject("Loop_Rifiuti_Quest").getAsJsonArray("MrCooper");
                                                    
                                                    @Override
                                                    public void run() {
                                                        int scelta = JOptionPane.showConfirmDialog(engine.getFrame(), "Accetti la proposta di Mr.Cooper?", "Scelta", JOptionPane.YES_NO_OPTION);
                                                        if (scelta == JOptionPane.YES_OPTION) {
                                                            mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("missione").getAsString()));
                                                            engine.mostraDialogoNPC(ProgressioneStoria.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
                                                            setStatoCity(1);
                                                        } else {
                                                            String frase = rifiutiJson.get(Math.min(countRifiuti, rifiutiJson.size() - 1)).getAsString();
                                                            countRifiuti++;
                                                            mrCooper.setDialoghi(Arrays.asList(frase));
                                                            engine.mostraDialogoNPCCallback(ProgressioneStoria.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, this);
                                                        }
                                                    }
                                                };
                                                loopConfermaCooper.run();
                                            });
                                        });
                                    });
                                } else {
                                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("non_capisco").getAsString()));
                                    engine.mostraDialogoNPCCallback(ProgressioneStoria.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, this);
                                }
                            }
                        };  engine.mostraDialogoNPCCallback(this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, loopCooper);
                        break;
                    case CONSEGNATA_CENA:
                        mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("ringraziamento").getAsString()));
                        engine.mostraDialogoNPC(this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
                        Oggetto carote = engine.getGiocatore().getInventario().cercaOggetto("Carote");
                        if (carote != null) engine.getGiocatore().getInventario().rimuoviOggetto(carote);
                        setStatoCity(5);
                        break;
                    default:
                        mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("hai_carote").getAsString()));
                        engine.mostraDialogoNPC(this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
                        break;
                }
            }
        };
        zoneStalla.put(CostantiHitbox.STALLA_MR_COOPER, mrCooperInteraction);
        GameScreen stallaScreen = engine.getSceneManager().creaScenaBase("Stalla.png", zoneStalla);
        this.stallaScreen = stallaScreen;
        engine.getSceneManager().registraScena(CostantiMappa.STALLA, stallaScreen);
    }

    private void costruisciBosco() {
        Map<double[], Runnable> zoneBosco = new HashMap<>();
        JsonObject foxDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Fox");
        JsonObject eryndorFoxDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Fox");
        ladroFox = registraNPC("Fox", new ArrayList<>());
        ImageIcon spriteFox = new ImageIcon(getClass().getResource("/sprites/Personaggi/Fox.png"));

        Map<double[], Runnable> zoneBoscoDeep = new HashMap<>();        // Array per bypassare final e permettere di capire se il cartello è stato letto (anche per il raccogliFiore)
        final boolean[] cartelloLetto = {false};

        double[] hitboxFioreBlu = CostantiHitbox.BOSCODEEP_FIORE_BLU; // ID 6
        double[] hitboxFioreRosso = CostantiHitbox.BOSCODEEP_FIORE_ROSSO; // ID 5
        double[] hitboxFioreViola = CostantiHitbox.BOSCODEEP_FIORE_VIOLA; // ID 7

        java.util.function.Consumer<String> verificaFiore = (scelta) -> {
            if (statoAttuale.getValore() != 6) return;
            EnigmaSceltaMultipla enigma3 = IstanzaEnigma.creaEnigma3(engine.getTxt().getOggettoDaCatalogo(1));
            if (enigma3.verifica(scelta)) {
                engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("consegna_corretta").getAsString(), null, () -> {
                    ladroFox.setDialoghi(Arrays.asList(foxDb.get("reazione_pianta_corretta_1").getAsString()));
                    engine.mostraDialogoNPCCallback(this.boscoScreen, CostantiMappa.BOSCO, ladroFox, spriteFox, () -> {
                        ladroFox.setDialoghi(Arrays.asList(foxDb.get("reazione_pianta_corretta_2").getAsString()));
                        engine.mostraDialogoNPCCallback(this.boscoScreen, CostantiMappa.BOSCO, ladroFox, spriteFox, () -> {
                            ladroFox.setDialoghi(Arrays.asList(foxDb.get("addormentato").getAsString()));
                            engine.mostraDialogoNPCCallback(this.boscoScreen, CostantiMappa.BOSCO, ladroFox, spriteFox, () -> {
                                engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("pensiero_vittoria_1").getAsString(), null, () -> {
                                    engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("pensiero_vittoria_2").getAsString(), null, () -> {
                                        engine.getStatistics().enigmaRisolto(enigma3);
                                        engine.getGiocatore().getInventario().aggiungiOggetto(engine.getTxt().getOggettoDaCatalogo(8)); // Chiave Fox
                                        setStatoCity(7);
                                        // Rimuovi il fiore viola dall'inventario
                                        Oggetto fv = engine.getGiocatore().getInventario().cercaOggetto("Fiore Viola");
                                        if (fv != null) engine.getGiocatore().getInventario().rimuoviOggetto(fv);

                                        // Rimuovo le zone cliccabili dei fiori per non far apparire la manina se si ripassa in Bosco Deep
                                        zoneBoscoDeep.remove(hitboxFioreBlu);
                                        zoneBoscoDeep.remove(hitboxFioreRosso);
                                        zoneBoscoDeep.remove(hitboxFioreViola);

                                        String recTessuto = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Recuper_tessuto").getAsString();
                                        String narrBorsa = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Narrazione_Fox_Borsa").getAsString();

                                        engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Fantoccio", recTessuto, null, () -> {
                                            engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Fantoccio", narrBorsa, null, null);
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            } else {
                engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("consegna_errata").getAsString(), null, () -> {
                    ladroFox.setDialoghi(Arrays.asList(foxDb.get("reazione_pianta_errata").getAsString()));
                    engine.mostraDialogoNPCCallback(this.boscoScreen, CostantiMappa.BOSCO, ladroFox, spriteFox, () -> {
                        engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("pensiero_consegna_errata_1").getAsString(), null, () -> {
                            engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("pensiero_consegna_errata_2").getAsString(), null, () -> {
                                engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("correzione_consegna").getAsString(), null, () -> {
                                    // Rimuovi il fiore sbagliato dall'inventario
                                    Oggetto f = engine.getGiocatore().getInventario().cercaOggetto(scelta.equals("0") ? "Fiore Rosso" : "Fiore Blu");
                                    if (f != null) engine.getGiocatore().getInventario().rimuoviOggetto(f);
                                });
                            });
                        });
                    });
                });
            }
        };

        foxInteraction = () -> {
            if (statoAttuale.getValore() < 6) {
                ladroFox.setDialoghi(Arrays.asList(foxDb.get("incontro").getAsString()));
                engine.mostraDialogoNPCCallback(this.boscoScreen, CostantiMappa.BOSCO, ladroFox, spriteFox, () -> {
                    engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("reazione_furto").getAsString(), null, () -> {
                        ladroFox.setDialoghi(Arrays.asList(foxDb.get("ricatto").getAsString()));
                        engine.mostraDialogoNPCCallback(this.boscoScreen, CostantiMappa.BOSCO, ladroFox, spriteFox, () -> {
                            engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("reazione_inseguimento").getAsString(), null, () -> {
                                engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Fox", foxDb.get("parlato_richiesta_pianta").getAsString(), spriteFox, () -> {
                                    engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Fox", foxDb.get("pensiero_richiesta_pianta").getAsString(), spriteFox, () -> {
                                        engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("pensiero_accordo").getAsString(), null, () -> {
                                            engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Eryndor", eryndorFoxDb.get("parlato_accordo").getAsString(), null, () -> {
                                                Oggetto tessuto = engine.getGiocatore().getInventario().cercaOggetto("Tessuto");
                                                if (tessuto != null) {
                                                    engine.getGiocatore().getInventario().rimuoviOggetto(tessuto);
                                                }
                                                setStatoCity(6);
                                                ladroFox.setDialoghi(new ArrayList<>());
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            } else if (statoAttuale == StatoStoria.INCONTRO_FOX) {
                if (engine.getGiocatore().getInventario().cercaOggetto("Fiore Viola") != null) {
                    verificaFiore.accept("2");
                } else if (engine.getGiocatore().getInventario().cercaOggetto("Fiore Rosso") != null) {
                    verificaFiore.accept("0");
                } else if (engine.getGiocatore().getInventario().cercaOggetto("Fiore Blu") != null) {
                    verificaFiore.accept("1");
                } else {
                    engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Fox", foxDb.get("sbrigati").getAsString(), spriteFox, null);
                }
            } else {
                engine.mostraDialogoCallback(this.boscoScreen, CostantiMappa.BOSCO, "Fox", foxDb.get("addormentato").getAsString(), spriteFox, null);
            }
        };


        Consumer<Integer> raccogliFiore = (idFiore) -> {
            if (statoAttuale.getValore() != 6) return;
            
            Oggetto fR = engine.getGiocatore().getInventario().cercaOggetto("Fiore Rosso");
            Oggetto fB = engine.getGiocatore().getInventario().cercaOggetto("Fiore Blu");
            Oggetto fV = engine.getGiocatore().getInventario().cercaOggetto("Fiore Viola");
            
            boolean haGiaUnFiore = fR != null || fB != null || fV != null;
            if (!cartelloLetto[0] && !haGiaUnFiore) {
                // Non ha ancora letto il cartello e non ha fiori (magari salvataggio ricaricato e non ri-letto, costringiamo a leggere)
                return;
            }
            
            Oggetto nuovoFiore = engine.getTxt().getOggettoDaCatalogo(idFiore);
            if (engine.getGiocatore().getInventario().cercaOggetto(nuovoFiore.getNomeOggetto()) != null) {
                String strGiaRaccolto = engine.getDbWallOfText().getAsJsonObject("Schermo").get("fiore_gia_raccolto").getAsString();
                engine.mostraDialogoCallback(this.boscoDeepScreen, CostantiMappa.BOSCO_DEEP, "Fantoccio", strGiaRaccolto, null, null);
                return;
            }
            
            boolean fioreLasciato = false;
            if (fR != null) { engine.getGiocatore().getInventario().rimuoviOggetto(fR); fioreLasciato = true; }
            if (fB != null) { engine.getGiocatore().getInventario().rimuoviOggetto(fB); fioreLasciato = true; }
            if (fV != null) { engine.getGiocatore().getInventario().rimuoviOggetto(fV); fioreLasciato = true; }
            
            engine.getGiocatore().getInventario().aggiungiOggetto(nuovoFiore);
            
            if (fioreLasciato) {
                String strLasciato = engine.getDbWallOfText().getAsJsonObject("Schermo").get("fiore_sostituito").getAsString();
                engine.mostraDialogoCallback(this.boscoDeepScreen, CostantiMappa.BOSCO_DEEP, "Fantoccio", strLasciato + nuovoFiore.getNomeOggetto(), null, null);
            } else {
                String strRaccolto = engine.getDbWallOfText().getAsJsonObject("Schermo").get("fiore_raccolto").getAsString();
                engine.mostraDialogoCallback(this.boscoDeepScreen, CostantiMappa.BOSCO_DEEP, "Fantoccio", strRaccolto + nuovoFiore.getNomeOggetto(), null, null);
            }
        };



        // Ripristino hitboxes se l'utente ha già letto il cartello / ha già un fiore ricaricando
        if (statoAttuale == StatoStoria.INCONTRO_FOX) {
            boolean haGiaUnFiore = engine.getGiocatore().getInventario().cercaOggetto("Fiore Rosso") != null ||
                                   engine.getGiocatore().getInventario().cercaOggetto("Fiore Blu") != null ||
                                   engine.getGiocatore().getInventario().cercaOggetto("Fiore Viola") != null;
            if (haGiaUnFiore) {
                cartelloLetto[0] = true;
                zoneBoscoDeep.put(hitboxFioreBlu, () -> raccogliFiore.accept(6));
                zoneBoscoDeep.put(hitboxFioreRosso, () -> raccogliFiore.accept(5));
                zoneBoscoDeep.put(hitboxFioreViola, () -> raccogliFiore.accept(7));
            }
        }

        // Hitbox Cartello
        zoneBoscoDeep.put(CostantiHitbox.BOSCODEEP_CARTELLO, () -> {
            if (statoAttuale == StatoStoria.INCONTRO_FOX) {
                cartelloLetto[0] = true;
                // Aggiungiamo i fiori dinamicamente in modo che il cursore a mano si attivi solo ora
                zoneBoscoDeep.put(hitboxFioreBlu, () -> raccogliFiore.accept(6));
                zoneBoscoDeep.put(hitboxFioreRosso, () -> raccogliFiore.accept(5));
                zoneBoscoDeep.put(hitboxFioreViola, () -> raccogliFiore.accept(7));

                EnigmaSceltaMultipla enigma3 = IstanzaEnigma.creaEnigma3(engine.getTxt().getOggettoDaCatalogo(1));
                engine.getStatistics().iniziaEnigma(enigma3);
                engine.mostraDialogoCallback(this.boscoDeepScreen, CostantiMappa.BOSCO_DEEP, "Cartello", enigma3.getTesto(), null, null);
            } else if (statoAttuale.getValore() >= 7) {
                String strFoxAddormentato = engine.getDbWallOfText().getAsJsonObject("Schermo").get("fox_addormentato").getAsString();
                engine.mostraDialogoCallback(this.boscoDeepScreen, CostantiMappa.BOSCO_DEEP, "Fantoccio", strFoxAddormentato, null, null);
            }
        });

        GameScreen boscoScreen = engine.getSceneManager().creaScenaBase("BoscoLosco.png", zoneBosco);
        this.boscoScreen = boscoScreen;
        engine.getSceneManager().registraScena(CostantiMappa.BOSCO, boscoScreen);
        
        GameScreen boscoDeepScreen = engine.getSceneManager().creaScenaBase("BoscoINN.png", zoneBoscoDeep);
        boscoDeepScreen.abilitaDebugCoordinate(); // disabilitato per test
        this.boscoDeepScreen = boscoDeepScreen;
        engine.getSceneManager().registraScena(CostantiMappa.BOSCO_DEEP, boscoDeepScreen);
        
        // Karundis
    }

    private void costruisciKarundis() {
        Map<double[], Runnable> zoneKarundis = new HashMap<>(); // Dichiarato qui per poterlo usare nel callback
        
        // idleDialogs è già un campo di istanza, inizializzato in inizializzaPersonaggiPrincipali()
        this.npcKarundis1 = registraNPC("Abitante 1", Arrays.asList(idleDialogs.get(0)));
        this.npcKarundis2 = registraNPC("Abitante 2", Arrays.asList(idleDialogs.get(1)));
        
        List<String> hintChiave = JsonLoader.estraiLista(engine.getDbHint(), "Ricerca_Chiave_Castello");
        if (statoAttuale.getValore() >= 8 && statoAttuale.getValore() < 9) {
            if (!primoAccessoPalazzo) {
                npcKarundis1.setDialoghi(Arrays.asList(hintChiave.get(0)));
                npcKarundis2.setDialoghi(Arrays.asList(hintChiave.get(1)));
            } else {
                npcKarundis1.setDialoghi(Arrays.asList(idleDialogs.get(0)));
                npcKarundis2.setDialoghi(Arrays.asList(idleDialogs.get(1)));
            }
        } else if (statoAttuale.getValore() >= 9) {
            npcKarundis1.setDialoghi(Arrays.asList(idleDialogs.get(2)));
            npcKarundis2.setDialoghi(Arrays.asList(idleDialogs.get(3)));
        }
        
        zoneKarundis.put(CostantiHitbox.KARUNDIS_ABITANTE_1, () -> {
            engine.mostraDialogoNPC(this.karundisScreen, CostantiMappa.KARUNDIS, npcKarundis1, null);
        });
        zoneKarundis.put(CostantiHitbox.KARUNDIS_ABITANTE_2, () -> {
            engine.mostraDialogoNPC(this.karundisScreen, CostantiMappa.KARUNDIS, npcKarundis2, null);
        });

        zoneKarundis.put(CostantiHitbox.KARUNDIS_GROTTA, () -> {
            engine.getSceneManager().mostraScena(CostantiMappa.GROTTA);
            if (statoAttuale == StatoStoria.ENIGMA_FIORI_RISOLTO) {
                JsonObject clockDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Saggio_Clock");
                JsonObject eryndorDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Saggio_Clock");
                
                String clock1 = clockDb.get("presentazione").getAsString();
                String ery1 = eryndorDb.get("stupore").getAsString();
                String clock2 = clockDb.get("intenzioni").getAsString();
                String ery2 = eryndorDb.get("ammissione").getAsString();
                String clock3 = clockDb.get("missione").getAsString();
                
                Runnable step5 = () -> {
                    engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Saggio Clock", clock3, new ImageIcon(getClass().getResource("/sprites/Personaggi/Clock.png")), () -> {
                        setStatoCity(8);
                    });
                };
                Runnable step4 = () -> {
                    engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Eryndor", ery2, null, step5);
                };
                Runnable step3 = () -> {
                    engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Saggio Clock", clock2, new ImageIcon(getClass().getResource("/sprites/Personaggi/Clock.png")), step4);
                };
                Runnable step2 = () -> {
                    engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Eryndor", ery1, null, step3);
                };
                engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Saggio Clock", clock1, new ImageIcon(getClass().getResource("/sprites/Personaggi/Clock.png")), step2);
            }
        });
        
        GameScreen karundisScreen = engine.getSceneManager().creaScenaBase("Karundis.png", zoneKarundis);
        this.karundisScreen = karundisScreen;
        engine.getSceneManager().registraScena(CostantiMappa.KARUNDIS, karundisScreen);
        
        // Grotta
    }

    private void costruisciGrotta() {
        Map<double[], Runnable> zoneGrotta = new HashMap<>();
        final boolean[] enigma4Attivo = {false};
        List<String> hintsEnigma4 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_4_Orologio_Fucina");

        double[] orologioHitbox = CostantiHitbox.GROTTA_OROLOGIO;
        double[] calderoneHitbox = CostantiHitbox.GROTTA_CALDERONE;

        String titoloOrologio = engine.getDbHint().getAsJsonObject("Titoli_Aiuti").get("Titolo_Orologio_Fucina").getAsString();
        String titoloCalderone = engine.getDbHint().getAsJsonObject("Titoli_Aiuti").get("Titolo_Calderone_Fucina").getAsString();

        Runnable actOrologio = () -> {
            engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, titoloOrologio, hintsEnigma4.get(0), null, null);
        };
        Runnable actCalderone = () -> {
            engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, titoloCalderone, hintsEnigma4.get(1), null, null);
        };

        zoneGrotta.put(CostantiHitbox.GROTTA_INCUDINE, () -> { // Incudine
            if (engine.getGiocatore().getInventario().cercaOggetto("Spada Sincro") != null || engine.getGiocatore().getInventario().cercaOggetto("Spada") != null) {
                engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Eryndor", "L'incudine è vuota. Ho già preso la Spada.", null, null);
                return;
            }

            Enigma enigma4 = IstanzaEnigma.creaEnigma4((aeg.giocomap.Model.Oggetti.Spada) engine.getTxt().getOggettoDaCatalogo(10));
            
            Runnable loopEnigma = new Runnable() {
                @Override
                public void run() {
                    String risposta = JOptionPane.showInputDialog(engine.getFrame(), "Risposta:");
                    if (risposta == null) return;
                    if (enigma4.verifica(risposta)) {
                        enigma4Attivo[0] = false;
                        zoneGrotta.remove(orologioHitbox);
                        zoneGrotta.remove(calderoneHitbox);

                        engine.getStatistics().enigmaRisolto(enigma4);
                        engine.getGiocatore().getInventario().aggiungiOggetto(engine.getTxt().getOggettoDaCatalogo(10));
                        // Ottenimento della Spada Sincro: parte allo 0%
                        String txtSblocco = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Spada_sbloccata").getAsString();
                        String txtSuccesso = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Grotta").get("successo_spada").getAsString();
                        engine.mostraDialogoCallback(ProgressioneStoria.this.grottaScreen, CostantiMappa.GROTTA, "Fantoccio", txtSblocco, null, () -> {
                            engine.mostraDialogoCallback(ProgressioneStoria.this.grottaScreen, CostantiMappa.GROTTA, "Eryndor", txtSuccesso, null, null);
                        });
                    } else {
                        engine.mostraDialogoCallback(ProgressioneStoria.this.grottaScreen, CostantiMappa.GROTTA, "Eryndor", engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Grotta").get("fallimento_spada").getAsString(), null, this);
                    }
                }
            };

            if (!enigma4Attivo[0]) {
                enigma4Attivo[0] = true;
                zoneGrotta.put(orologioHitbox, actOrologio);
                zoneGrotta.put(calderoneHitbox, actCalderone);
                
                engine.getStatistics().iniziaEnigma(enigma4);
                
                String cartelloTesto = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Cartello_Spada_Fucina").getAsString();
                String narrazioneTesto = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Narrazione_Spada_Fucina").getAsString();

                Runnable apriInput = () -> {
                    engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Fantoccio", enigma4.getTesto(), null, loopEnigma);
                };

                Runnable step2 = () -> {
                    engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Fantoccio", narrazioneTesto, null, apriInput);
                };

                engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Fantoccio", cartelloTesto, null, step2);
            } else {
                engine.mostraDialogoCallback(this.grottaScreen, CostantiMappa.GROTTA, "Fantoccio", enigma4.getTesto(), null, loopEnigma);
            }
        });
        GameScreen grottaScreen = engine.getSceneManager().creaScenaBase("GrottaDellaFucina.png", zoneGrotta);
        this.grottaScreen = grottaScreen;
        engine.getSceneManager().registraScena(CostantiMappa.GROTTA, grottaScreen);
        
        // Cancello del Castello - Guardia Reale
    }

    private void costruisciIngressoPalazzo() {
        Map<double[], Runnable> zoneIngresso = new HashMap<>();
        JsonObject guardiaDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Guardiano");
        JsonObject eryndorGuardiaDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Guardiano");
        guardiaReale = registraNPC("Guardiano", Arrays.asList(guardiaDb.get("richiesta_chiave").getAsString()));
        
        double[] guardiaHitbox = CostantiHitbox.INGRESSO_GUARDIA;
        if (statoAttuale.getValore() < 9) {
            zoneIngresso.put(guardiaHitbox, () -> {
                if (!parlatoConGuardia) {
                    guardiaReale.setDialoghi(Arrays.asList(guardiaDb.get("richiesta_chiave").getAsString()));
                    engine.mostraDialogoNPCCallback(this.ingressoScreen, CostantiMappa.INGRESSO_PALAZZO, guardiaReale, null, () -> {
                        parlatoConGuardia = true;
                    });
                } else {
                    Oggetto chiave = engine.getGiocatore().getInventario().cercaOggetto("Chiave Fox");
                    if (chiave != null) {
                        guardiaReale.setDialoghi(Arrays.asList(guardiaDb.get("domanda_chiave").getAsString()));
                        engine.mostraDialogoNPCCallback(this.ingressoScreen, CostantiMappa.INGRESSO_PALAZZO, guardiaReale, null, () -> {
                            int scelta = JOptionPane.showConfirmDialog(
                                engine.getFrame(),
                                "Sicuro che hai la chiave?",
                                "Consegna Oggetto",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                            );
                            if (scelta == JOptionPane.YES_OPTION) {
                                guardiaReale.setDialoghi(Arrays.asList(guardiaDb.get("accettazione").getAsString()));
                                engine.mostraDialogoNPCCallback(this.ingressoScreen, CostantiMappa.INGRESSO_PALAZZO, guardiaReale, null, () -> {
                                    engine.getGiocatore().getInventario().rimuoviOggetto(chiave);
                                    setStatoCity(9);
                                    zoneIngresso.remove(guardiaHitbox);
                                    // Ripristino dialoghi generici NPC Karundis
                                    npcKarundis1.setDialoghi(Arrays.asList(idleDialogs.get(2)));
                                    npcKarundis2.setDialoghi(Arrays.asList(idleDialogs.get(3)));
                                    // Pensiero di Eryndor sulla chiave rubata da Fox
                                    engine.mostraDialogoCallback(this.ingressoScreen, CostantiMappa.INGRESSO_PALAZZO, "Eryndor", eryndorGuardiaDb.get("pensiero_chiave").getAsString(), null, null);
                                });
                            }
                        });
                    } else {
                        guardiaReale.setDialoghi(Arrays.asList(guardiaDb.get("richiesta_chiave").getAsString()));
                        engine.mostraDialogoNPC(this.ingressoScreen, CostantiMappa.INGRESSO_PALAZZO, guardiaReale, null);
                    }
                }
            });
        }
        
        GameScreen ingressoScreen = engine.getSceneManager().creaScenaBase("CancelloCastello.png", zoneIngresso);
        ingressoScreen.abilitaDebugCoordinate(); // disabilitato per test
        this.ingressoScreen = ingressoScreen;
        engine.getSceneManager().registraScena(CostantiMappa.INGRESSO_PALAZZO, ingressoScreen);
        engine.getSceneManager().registraScena(CostantiMappa.SCALE, engine.getSceneManager().creaScenaBase("ScalePalazzo.png", null));
        
        // Cripta Eripeta
    }

    private void costruisciCripta() {
        Map<double[], Runnable> zoneCripta = new HashMap<>();
        JsonObject eripetaDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta");
        eripeta = registraNPC("Eripeta", Arrays.asList(eripetaDb.get("rifiuto").getAsString()));
        
        interazioneEripeta = () -> {
            JsonObject eryndorDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Eripeta");
            String eryName = engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer();
            ImageIcon spriteEripeta = new ImageIcon(getClass().getResource("/sprites/Personaggi/Eripeta.png"));

            if (statoAttuale.getValore() < 13) {
                String indizio2 = eripetaDb.get("indizio_2").getAsString();
                engine.mostraDialogoCallback(this.criptaScreen, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", indizio2, spriteEripeta, null);
            } else if (statoAttuale == StatoStoria.ENIGMA_VINCOLO_RISOLTO) {
                String ringr1 = eripetaDb.get("ringraziamento_1").getAsString();
                String ringr2 = eripetaDb.get("ringraziamento_2").getAsString();
                String comprensione = eryndorDb.get("comprensione").getAsString();
                String congedo = eripetaDb.get("congedo").getAsString();
                
                Runnable step4 = () -> {
                    engine.mostraDialogoCallback(this.criptaScreen, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", congedo, spriteEripeta, () -> {
                        Oggetto ampolla = engine.getGiocatore().getInventario().cercaOggetto("Ampolla d'oro");
                        if (ampolla != null) {
                            engine.getGiocatore().getInventario().rimuoviOggetto(ampolla);
                            engine.getGiocatore().ricaricaSpadaSincro();
                        }
                        setStatoCity(15);
                    });
                };
                Runnable step3 = () -> {
                    engine.mostraDialogoCallback(this.criptaScreen, CostantiMappa.CRIPTA_ERIPETA, eryName, comprensione, null, step4);
                };
                Runnable step2 = () -> {
                    engine.mostraDialogoCallback(this.criptaScreen, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", ringr2, spriteEripeta, step3);
                };
                
                engine.mostraDialogoCallback(this.criptaScreen, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", ringr1, spriteEripeta, step2);
            } else {
                String congedo = eripetaDb.get("congedo").getAsString();
                engine.mostraDialogoCallback(this.criptaScreen, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", congedo, spriteEripeta, null);
            }
        };

        zoneCripta.put(CostantiHitbox.CRIPTA_ERIPETA, interazioneEripeta);
        GameScreen criptaScreen = engine.getSceneManager().creaScenaBase("Cripta.png", zoneCripta);
        this.criptaScreen = criptaScreen;
        engine.getSceneManager().registraScena(CostantiMappa.CRIPTA_ERIPETA, criptaScreen);
        
        // palazzo reale principessa
    }

    private void costruisciPalazzoPrincipessa() {
        Map<double[], Runnable> zonePalazzo = new HashMap<>();
        JsonObject marienDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Marien");
        marien = registraNPC("Principessa Marien", Arrays.asList(marienDb.get("sfida").getAsString()));
        zonePalazzo.put(CostantiHitbox.PALAZZO_MARIEN, () -> {
            if (engine.getGiocatore().isEnigmaRisolto("Enigma_Finale_Principessa")) {
                marien.setDialoghi(Arrays.asList("Sei già il mio sposo! Il regno è salvo."));
                engine.mostraDialogoNPC(this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, marien, null);
                return;
            }
            EnigmaSceltaMultipla enigmaFinale = IstanzaEnigma.creaEnigmaFinale(new Oggetto(8, "Titolo Nobile", "Hai vinto il cuore della principessa e il titolo."));
            engine.getStatistics().iniziaEnigma(enigmaFinale);
            Runnable loopEnigma = new Runnable() {
                @Override
                public void run() {
                    String[] opzioni = enigmaFinale.getOpzioni().toArray(new String[0]);
                    int scelta = JOptionPane.showOptionDialog(engine.getFrame(), "Scegli l'ordine:", "Enigma della Principessa", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opzioni, opzioni[0]);
                    if (scelta < 0) return;
                    if (enigmaFinale.verifica(String.valueOf(scelta))) {
                        engine.getStatistics().enigmaRisolto(enigmaFinale);
                        // Enigma finale: ultima ricarica della Spada Sincro (99%)
                        engine.getGiocatore().ricaricaSpadaSincro();
                        marien.setDialoghi(Arrays.asList(marienDb.get("vittoria_finale").getAsString()));
                        engine.mostraDialogoNPC(ProgressioneStoria.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, marien, null);
                    } else {
                        engine.mostraDialogoCallback(ProgressioneStoria.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienDb.get("errore_cacciata").getAsString(), null, () -> {
                            engine.mostraDialogoCallback(ProgressioneStoria.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Marien").get("errore_ordine").getAsString(), null, this);
                        });
                    }
                }
            };
            engine.mostraDialogoCallback(this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienDb.get("enigma_leggi").getAsString(), null, loopEnigma);
        });
        GameScreen palazzoScreen = engine.getSceneManager().creaScenaBase("SalaDellaPrincipessa.png", zonePalazzo);
        this.palazzoScreen = palazzoScreen;
        engine.getSceneManager().registraScena(CostantiMappa.PALAZZO_PRINCIPESSA, palazzoScreen);

    }

    private void costruisciLettere() {
        List<String> lettera=JsonLoader.estraiLista(engine.getDbWallOfText().getAsJsonObject("Lettera"),"lettera_iniziale");
        String enigmaText = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Enigma_1_Lettera").getAsString();
        List<String> letteraRetro = Arrays.asList(enigmaText);
 LetteraScreen schermata_retro = new LetteraScreen(letteraRetro, () -> {
            System.out.println("DEBUG: Lettera retro finita, gioco START");
            
            engine.getGiocatore().setPossiedeInventario(true);
            
            engine.getSceneManager().mostraScena(CostantiMappa.PIAZZA_CENTRALE);
        });
        engine.getSceneManager().registraScena(CostantiMappa.LETTERA_RETRO, schermata_retro);

        LetteraScreen schermata_lettera = new LetteraScreen(lettera, () -> {
            System.out.println("DEBUG: Lettera finita, mostro retro");
            
            engine.getSceneManager().mostraScena(CostantiMappa.LETTERA_RETRO);

        });
        engine.getSceneManager().registraScena(CostantiMappa.LETTERA, schermata_lettera);
    }

    private void avviaIntercettazioneEripeta() {
        GameScreen scale = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.SCALE);
        ImageIcon spriteEripeta = new ImageIcon(getClass().getResource("/sprites/Personaggi/Eripeta.png"));
        
        JsonObject eripetaDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta");
        JsonObject eryndorDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Eripeta");
        
        String eryName = engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer();
        
        String txt1 = eripetaDb.get("blocco").getAsString();
        String txt2 = eryndorDb.get("domanda").getAsString();
        String txt3_1 = eripetaDb.get("presentazione_1").getAsString();
        String txt3_2 = eripetaDb.get("presentazione_2").getAsString();
        String txt3_3 = eripetaDb.get("presentazione_3").getAsString();
        String txt3_4 = eripetaDb.get("presentazione_4").getAsString();
        String txt4 = eryndorDb.get("risposta").getAsString();
        String txt5 = eripetaDb.get("invito").getAsString();
        
        Runnable step7 = () -> {
            engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", txt5, spriteEripeta, () -> {
                setStatoCity(10);
                engine.getSceneManager().mostraScena(CostantiMappa.CRIPTA_ERIPETA);
                avviaAccusaEripeta();
            });
        };
        Runnable step6 = () -> {
            engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, eryName, txt4, null, step7);
        };
        Runnable step5 = () -> {
            engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", txt3_4, spriteEripeta, step6);
        };
        Runnable step4 = () -> {
            engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", txt3_3, spriteEripeta, step5);
        };
        Runnable step3_2 = () -> {
            engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", txt3_2, spriteEripeta, step4);
        };
        Runnable step3_1 = () -> {
            engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", txt3_1, spriteEripeta, step3_2);
        };
        Runnable step2 = () -> {
            engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, eryName, txt2, null, step3_1);
        };
        
        engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", txt1, spriteEripeta, step2);
    }

    private void avviaAccusaEripeta() {
        GameScreen cripta = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.CRIPTA_ERIPETA);
        ImageIcon spriteEripeta = new ImageIcon(getClass().getResource("/sprites/Personaggi/Eripeta.png"));
        
        JsonObject eripetaDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta");
        JsonObject eryndorDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Eripeta");
        
        String eryName = engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer();
        
        String txt1_1 = eripetaDb.get("visione_1").getAsString();
        String txt1_2 = eripetaDb.get("visione_2").getAsString();
        String txt2_pensiero = eryndorDb.get("pensiero_difesa").getAsString();
        String txt2_parlato_1 = eryndorDb.get("parlato_difesa_1").getAsString();
        String txt2_parlato_2 = eryndorDb.get("parlato_difesa_2").getAsString();
        String txt3 = eripetaDb.get("sfida").getAsString();
        String txt4 = eryndorDb.get("domanda_sfida").getAsString();
        String txt5_1 = eripetaDb.get("indizio_1").getAsString();
        String txt5_2 = eripetaDb.get("indizio_2").getAsString();
        String txt5_3 = eripetaDb.get("indizio_3").getAsString();
        
        Runnable step9 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", txt5_3, spriteEripeta, () -> {
                setStatoCity(11);
                // David torna interpellabile al Porto (mandato da Eripeta): ne riattivo la hitbox
                if (riattivaDavidDopoMappa != null) riattivaDavidDopoMappa.run();
                engine.getSceneManager().mostraScena(CostantiMappa.SCALE);
            });
        };
        Runnable step8_2 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", txt5_2, spriteEripeta, step9);
        };
        Runnable step8_1 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", txt5_1, spriteEripeta, step8_2);
        };
        Runnable step7 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, eryName, txt4, null, step8_1);
        };
        Runnable step6 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", txt3, spriteEripeta, step7);
        };
        Runnable step5_2 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, eryName, txt2_parlato_2, null, step6);
        };
        Runnable step5_1 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, eryName, txt2_parlato_1, null, step5_2);
        };
        Runnable step4 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, eryName, txt2_pensiero, null, step5_1);
        };
        Runnable step3 = () -> {
            engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", txt1_2, spriteEripeta, step4);
        };
        
        engine.mostraDialogoCallback(cripta, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", txt1_1, spriteEripeta, step3);
    }

    private void gestisciDavidDopoMappa(GameScreen portoScreen, Personaggio david, ImageIcon spriteDavid, JsonObject davidDb) {
        if (statoAttuale == StatoStoria.ACCUSA_ERIPETA_SUPERATA) {
            String ritorno1 = davidDb.get("ritorno_1").getAsString();
            engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", ritorno1, spriteDavid, () -> {
                String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa rispondi a David?");
                if (input == null || input.trim().isEmpty()) return;
                
                // Usiamo contieneParolaChiave anziché contieneRadiceParola con wildcard
                // perché "Eripeta" è un nome proprio e non necessita di declinazioni (es. singolare/plurale)
                if (Parser.contieneParolaChiave(input, "Eripeta")) {
                    setStatoCity(12);
                    String ritorno2 = davidDb.get("ritorno_2").getAsString();
                    engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", ritorno2, spriteDavid, () -> {
                        lanciaEnigma5(portoScreen, david, spriteDavid, davidDb);
                    });
                } else {
                    String incomprensione = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Contadino_Green").get("incomprensione").getAsString();
                    engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", incomprensione, spriteDavid, null);
                }
            });
        } else if (statoAttuale == StatoStoria.DAVID_INTERPELLATO) {
            lanciaEnigma5(portoScreen, david, spriteDavid, davidDb);
        } else if (statoAttuale == StatoStoria.ENIGMA_VINCOLO_RISOLTO) {
            mostraStoriaEripeta(portoScreen, spriteDavid, davidDb);
        } else {
            String salutoGenerico = davidDb.get("saluto_generico").getAsString();
            engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", salutoGenerico, spriteDavid, null);
        }
    }

    private void lanciaEnigma5(GameScreen portoScreen, Personaggio david, ImageIcon spriteDavid, JsonObject davidDb) {
        Oggetto reward = engine.getTxt().getOggettoDaCatalogo(9);
        EnigmaSceltaMultipla enigma5 = IstanzaEnigma.creaEnigma5(reward);
        engine.getStatistics().iniziaEnigma(enigma5);

        // David sta annunciando l'Enigma del Vincolo: da ora gli abitanti 1 e 3 in
        // Piazza possono dare i relativi aiuti (se esci dalla risposta per cercarli)
        if (attivaAiutiEnigma5 != null) attivaAiutiEnigma5.run();
        
        String descrizione = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Enigma_5_Vincolo").getAsString();
        String domanda = engine.getDbWallOfText().getAsJsonObject("Schermo").get("Enigma_5_Vincolo_domanda").getAsString();
        
        Runnable loopEnigma = new Runnable() {
            @Override
            public void run() {
                String[] opzioni = enigma5.getOpzioni().toArray(new String[0]);
                int scelta = JOptionPane.showOptionDialog(
                    engine.getFrame(),
                    "Seleziona la risposta all'enigma (oppure chiudi per cercare indizi):",
                    "Enigma del Vincolo",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opzioni,
                    opzioni[0]
                );
                if (scelta < 0) return;
                
                if (enigma5.verifica(String.valueOf(scelta))) {
                    engine.getStatistics().enigmaRisolto(enigma5);
                    setStatoCity(13);
                    // Prima ricarica della Spada Sincro (33%)
                    engine.getGiocatore().ricaricaSpadaSincro();
                    // Enigma 5 risolto: gli aiuti degli abitanti 1 e 3 non servono più
                    if (disattivaAiutiEnigma5 != null) disattivaAiutiEnigma5.run();

                    mostraEsitoEnigma5(portoScreen, spriteDavid, davidDb);
                } else {
                    String erroreMsg = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta").get("errore").getAsString();
                    engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", erroreMsg, spriteDavid, this);
                }
            }
        };
        
        // Visualizza il testo dell'indovinello diviso in due passaggi nel pannello dei dialoghi di gioco
        engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", descrizione, spriteDavid, () -> {
            engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", domanda, spriteDavid, loopEnigma);
        });
    }

    // Esito della risoluzione dell'Enigma 5: David espone prima la soluzione del Vincolo
    // (divisa in due pannelli perché lunga) e poi racconta la storia di Eripeta.
    private void mostraEsitoEnigma5(GameScreen portoScreen, ImageIcon spriteDavid, JsonObject davidDb) {
        String soluzione1 = davidDb.get("soluzione_enigma_1").getAsString();
        String soluzione2 = davidDb.get("soluzione_enigma_2").getAsString();
        String soluzione3 = davidDb.get("soluzione_enigma_3").getAsString();
        engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", soluzione1, spriteDavid, () ->
            engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", soluzione2, spriteDavid, () ->
                engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", soluzione3, spriteDavid, () ->
                    mostraStoriaEripeta(portoScreen, spriteDavid, davidDb))));
    }

    // Il racconto di David sulla storia di Eripeta è lungo: lo mostro in due pannelli
    // di dialogo consecutivi per non farlo apparire tagliato a schermo.
    private void mostraStoriaEripeta(GameScreen portoScreen, ImageIcon spriteDavid, JsonObject davidDb) {
        String parte1 = davidDb.get("storia_eripeta_1").getAsString();
        String parte2 = davidDb.get("storia_eripeta_2").getAsString();
        engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", parte1, spriteDavid, () ->
            engine.mostraDialogoCallback(portoScreen, CostantiMappa.PORTO, "David", parte2, spriteDavid, null));
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
