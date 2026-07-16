package aeg.giocomap.GameEngine;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.View.GameScreen;
import aeg.giocomap.View.LetteraScreen;
import aeg.giocomap.View.SchermataFinale;
import aeg.giocomap.Util.JsonLoader;
import aeg.giocomap.Util.Parser;

import aeg.giocomap.Model.Enigmi.Enigma;
import aeg.giocomap.Model.Enigmi.EnigmaSceltaMultipla;
import aeg.giocomap.Model.Enigmi.EnigmaOrdinamento;
import aeg.giocomap.Model.Enigmi.IstanzaEnigma;
import aeg.giocomap.View.DialogoOrdinamentoVestiti;

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
public class CostruttoreScene {

    private final GameEngine engine;
    private final RegistroNPC registroNPC;
    private final StatoProgressione stato;

    // Sequenze narrative della fase finale del giocop
    private final FlussiNarrativi flussi;

    // Istanziazione personaggi
    private Personaggio ab1;
    private Personaggio ab2;
    private Personaggio ab3;
    private Personaggio mrCooper;
    private Personaggio contadino;
    private Personaggio pescivendolo;
    private Personaggio david;
    private Personaggio ladroFox;
    private Personaggio guardiaReale;
    private Personaggio marien;
    
    // Azione interattiva di Eripeta
    private Runnable interazioneEripeta;
    
    // Variabili per ogni scena creata
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
    private SchermataFinale schermataFinale;

    // Variabili di servizio
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

    // Azione interattiva di Mr. Cooper
    private Runnable mrCooperInteraction;
    private Runnable foxInteraction;

    /* Azione che ri-registra la hitbox di David al Porto quando la storia lo rende
    di nuovo interpellabile (quindi viene raggiuntostatoCity >= 11)*/
    private Runnable riattivaDavidDopoMappa;

    /* Azione che abilita le hitbox degli abitanti 1 e 3 in piazza per il quinto enigma
    Invocata solo quando David annuncia l'enigma, non prima*/
    private Runnable attivaAiutiEnigma5;

    // Azione che disabilita le hitbox degli abitanti 1 e 3 in piazza per il quinto enigma
    private Runnable disattivaAiutiEnigma5;

    // Riferimenti agli NPC di Karundis per poter aggiornare i loro dialoghi
    private Personaggio npcKarundis1;
    private Personaggio npcKarundis2;

    // Si salva tutto il necessario e si crea il gestore dei flussi narrativi
    public CostruttoreScene(GameEngine engine, StatoProgressione stato, RegistroNPC registroNPC) {
        this.engine = engine;
        this.stato = stato;
        this.registroNPC = registroNPC;
        this.flussi = new FlussiNarrativi(engine, stato, this);
    }

    // Setter dello statoCity
    private void setStatoCity(int valore) {
        stato.setStatoCity(valore);
    }

    //  Interazione con Mr.Cooper alla stalla
    Runnable getMrCooperInteraction() {
        return mrCooperInteraction;
    }

    // Interazione con la volpe
    Runnable getFoxInteraction() {
        return foxInteraction;
    }

    // Usato per l'intercettazione di Eripeta durante la storia
    Runnable getInterazioneEripeta() {
        return interazioneEripeta;
    }

    // Utile per impostare i dialoghi hint del primo Karundis
    Personaggio getNpcKarundis1() {
        return npcKarundis1;
    }

    // Utile per impostare i dialoghi hint del secondo Karundis
    Personaggio getNpcKarundis2() {
        return npcKarundis2;
    }

    // Usato per riattivare David dopo la mappa
    Runnable getRiattivaDavidDopoMappa() {
        return riattivaDavidDopoMappa;
    }

    // Usato per attivare gli aiuti dell'enigma 5
    Runnable getAttivaAiutiEnigma5() {
        return attivaAiutiEnigma5;
    }

    // Usato per disattivare gli aiuti dell'enigma 5
    Runnable getDisattivaAiutiEnigma5() {
        return disattivaAiutiEnigma5;
    }

    // Delega alla sequenza narrativa; invocato dal routing (NavigazioneMappa)
    // e mantenuto qui come punto d'ingresso stabile.
    void avviaIntercettazioneEripeta() {
        flussi.avviaIntercettazioneEripeta();
    }

    //Costruzione di tutte le scene
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

    /*
        Precaricamento dei dati che dovrebbero essere utilizzati prossimamente
        (dialoghi specifici e generici + sprite)
    */
    private void inizializzaPersonaggiPrincipali() {

        hints = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");

        mrCooperDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("MrCooper");
        pescivendoloDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Pescivendolo");
        davidDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("David");
        spriteMrCooper = new ImageIcon(getClass().getResource("/sprites/Personaggi/MrCooper.png"));
        idleDialogs = JsonLoader.estraiLista(engine.getDbHint(), "Dialoghi_Generici_Idle");
    }
    
    /*
        Qui registriamo abitanti (con hint per il primo e quinto enigma) e tutti
        i personaggi della piazza 
    */
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
        
        
        ImageIcon spriteGreen = new ImageIcon(getClass().getResource("/sprites/Personaggi/Green.png"));

        // Inizializzazione hitbox
        hitboxAb1 = CostantiHitbox.PIAZZA_ABITANTE_1;
        double[] hitboxAb2 = CostantiHitbox.PIAZZA_ABITANTE_2;
        hitboxAb3 = CostantiHitbox.PIAZZA_ABITANTE_3;

        /* Interazione abitante 1:
           Se non ha la mappa l'NPC da gli aiuti del primo enigma (Stalla)
           Altrimenti da quelli del quinto enigma (Eripeta)
        */
        interazioneAb1 = () -> {
            if (!engine.getGiocatore().isPossiedeMappa()) {
                List<String> hintsE1 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");
                Personaggio tempAb1 = new Personaggio("Abitante 1");
                tempAb1.setDialoghi(Arrays.asList(hintsE1.get(0)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb1, null);
            } else if (stato.getStato() == StatoStoria.ACCUSA_ERIPETA_SUPERATA || stato.getStato() == StatoStoria.DAVID_INTERPELLATO) {
                List<String> hintsE5 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_5_Vincolo");
                Personaggio tempAb1 = new Personaggio("Abitante 1");
                tempAb1.setDialoghi(Arrays.asList(hintsE5.get(0)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb1, null);
            }
        };
        if (!engine.getGiocatore().isPossiedeMappa() || stato.getStato() == StatoStoria.ACCUSA_ERIPETA_SUPERATA || stato.getStato() == StatoStoria.DAVID_INTERPELLATO) {
            zonePiazza.put(hitboxAb1, interazioneAb1);
        }
        
        /*
        Lui è l'NPC sfigato, da soltanto un aiuto al primo enigma e poi ti saluta
        per tutto il resto del gioco. Un grande.
        */
        zonePiazza.put(hitboxAb2, () -> {
            Personaggio tempAb2 = new Personaggio("Abitante 2");
            if (!engine.getGiocatore().isPossiedeMappa()) {
                List<String> hintsE1 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");
                tempAb2.setDialoghi(Arrays.asList(hintsE1.get(1)));
            } else if (stato.getStato().getValore() < 3) {
                tempAb2.setDialoghi(Arrays.asList(davidDb.get("consiglio_stalla").getAsString()));
            } else {
                tempAb2.setDialoghi(Arrays.asList(davidDb.get("saluto_generico").getAsString()));
            }
            engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb2, null);
        });
        
        /* Interazione abitante 3:
           Se non ha la mappa l'NPC da gli aiuti del primo enigma (Stalla)
           Altrimenti da quelli del quinto enigma (Eripeta)
        */
        interazioneAb3 = () -> {
            if (!engine.getGiocatore().isPossiedeMappa()) {
                List<String> hintsE1 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_1_Porto");
                Personaggio tempAb3 = new Personaggio("Abitante 3");
                tempAb3.setDialoghi(Arrays.asList(hintsE1.get(2)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb3, null);
            } else if (stato.getStato() == StatoStoria.ACCUSA_ERIPETA_SUPERATA || stato.getStato() == StatoStoria.DAVID_INTERPELLATO) {
                List<String> hintsE5 = JsonLoader.estraiLista(engine.getDbHint().getAsJsonObject("Aiuti_Enigmi"), "Enigma_5_Vincolo");
                Personaggio tempAb3 = new Personaggio("Abitante 3");
                tempAb3.setDialoghi(Arrays.asList(hintsE5.get(1)));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, tempAb3, null);
            }
        };
        if (!engine.getGiocatore().isPossiedeMappa() || stato.getStato() == StatoStoria.ACCUSA_ERIPETA_SUPERATA || stato.getStato() == StatoStoria.DAVID_INTERPELLATO) {
            zonePiazza.put(hitboxAb3, interazioneAb3);
        }

        zonePiazza.put(CostantiHitbox.PIAZZA_CONTADINO, () -> {
            if (stato.getStato() == StatoStoria.MISSIONE_COOPER_ACCETTATA) { 
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("saluto").getAsString()));
                Runnable loopContadino = new Runnable() {
                    @Override
                    public void run() {
                        String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi al Contadino Green?");
                        if (input == null) return;
                        if (Parser.contieneRadiceParola(input, "carot*")) {
                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("richiesta").getAsString()));
                            engine.mostraDialogoNPCCallback(CostruttoreScene.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, () -> {
                                Runnable loopConfermaContadino = new Runnable() {
                                    int countRifiuti = 0; // Arrivato a tot. rifiuti ripete la stessa frase
                                    JsonArray rifiutiJson = engine.getDbHint().getAsJsonObject("Loop_Rifiuti_Quest").getAsJsonArray("Contadino_Green");
                                    
                                    // Creazione finestra insistente
                                    @Override
                                    public void run() {
                                        int scelta = JOptionPane.showConfirmDialog(engine.getFrame(), "Accetti la proposta del Contadino?", "Scelta", JOptionPane.YES_NO_OPTION);
                                        if (scelta == JOptionPane.YES_OPTION) {
                                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("ringraziamento").getAsString()));
                                            engine.mostraDialogoNPC(CostruttoreScene.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen);
                                            setStatoCity(2); 
                                        } else {
                                            String frase = rifiutiJson.get(Math.min(countRifiuti, rifiutiJson.size() - 1)).getAsString();
                                            countRifiuti++;
                                            contadino.setDialoghi(Arrays.asList(frase));
                                            engine.mostraDialogoNPCCallback(CostruttoreScene.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, this);
                                        }
                                    }
                                };
                                loopConfermaContadino.run();
                            });
                        } else {
                            contadino.setDialoghi(Arrays.asList(contadinoDb.get("incomprensione").getAsString())); //Visualizzazione dialogo dove non comprende
                            engine.mostraDialogoNPCCallback(CostruttoreScene.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, this);
                        }
                    }
                };
                engine.mostraDialogoNPCCallback(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, loopContadino);
            } else if (stato.getStato() == StatoStoria.ENIGMA_PESCIVENDOLO_RISOLTO) { 
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("consegna").getAsString()));
                engine.mostraDialogoNPC(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen);
                Oggetto cena = engine.getGiocatore().getInventario().cercaOggetto("Cena di pesce");
                if (cena != null) engine.getGiocatore().getInventario().rimuoviOggetto(cena);
                engine.getGiocatore().getInventario().aggiungiOggetto(engine.getTxt().getOggettoDaCatalogo(3));
                setStatoCity(4);
                contadino.setDialoghi(Arrays.asList(contadinoDb.get("augurio").getAsString()));
            } else if (stato.getStato().getValore() >= 4) {
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

    // Porto: David, il Pescivendolo e gli enigmi 1 e 2
    private void costruisciPorto() {
        Map<double[], Runnable> zonePorto = new HashMap<>();

        String dialogoDavid = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("David").get("incontro_1").getAsString();
        david = registraNPC("David", Arrays.asList(dialogoDavid));
        ImageIcon spriteDavid = new ImageIcon(getClass().getResource("/sprites/Personaggi/David.png"));

        double[] davidHitbox = CostantiHitbox.PORTO_DAVID;

        // Ripristina la hitbox di David con il comportamento post mappa
        attivaAiutiEnigma5 = () -> {
            zonePiazza.put(hitboxAb1, interazioneAb1);
            zonePiazza.put(hitboxAb3, interazioneAb3);
        };

        riattivaDavidDopoMappa = () -> {
            zonePorto.put(davidHitbox, () ->
                    flussi.gestisciDavidDopoMappa(this.portoScreen, spriteDavid, davidDb));
            /* Gli aiuti degli abitanti 1 e 3 si abilitano solo dopo che David annuncia
            l'Enigma del Vincolo (statoCity 12), quando il player torna da Eripeta*/
            if (stato.getStato() == StatoStoria.DAVID_INTERPELLATO) {
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
        } else if (stato.getStato().getValore() >= 11) {
            // Salvataggio ricaricato in fase avanzata: la hitbox va ripristinata subito
            riattivaDavidDopoMappa.run();
        }

        final boolean[] pescivendoloPreambleShown = {false};
        zonePorto.put(CostantiHitbox.PORTO_PESCIVENDOLO, () -> {
            if (stato.getStato() == StatoStoria.PARLATO_CONTADINO) {
                Runnable startEnigma = () -> {
                    Enigma enigma2 = IstanzaEnigma.creaEnigma2(engine.getTxt().getOggettoDaCatalogo(4));
                    engine.getStatistics().iniziaEnigma(enigma2);
                    // Quando ricominciano a parlare (per gli aiuti dell'Enigma 2), assegno i nuovi testi presi dal JSON e riaggiungo le hitbox così tornano cliccabili (icona mano)
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
                                engine.mostraDialogoNPC(CostruttoreScene.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null);
                                setStatoCity(3);
                                zonePiazza.remove(hitboxAb1);
                                zonePiazza.remove(hitboxAb3);
                                ab2.setDialoghi(Arrays.asList(davidDb.get("saluto_generico").getAsString()));
                            } else {
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("risposta_errata").getAsString()));
                                engine.mostraDialogoNPCCallback(CostruttoreScene.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, this);
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
                                engine.mostraDialogoCallback(CostruttoreScene.this.portoScreen, CostantiMappa.PORTO, engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer(), EryndorText, null, () -> {
                                    pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("richiesta").getAsString()));
                                    engine.mostraDialogoNPCCallback(CostruttoreScene.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, () -> {
                                        pescivendoloPreambleShown[0] = true;
                                        startEnigma.run();
                                    });
                                });
                            } else {
                                pescivendolo.setDialoghi(Arrays.asList(pescivendoloDb.get("incomprensione").getAsString()));
                                engine.mostraDialogoNPCCallback(CostruttoreScene.this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, this);
                            }
                        }
                    };
                    engine.mostraDialogoNPCCallback(this.portoScreen, CostantiMappa.PORTO, pescivendolo, null, loopPescivendolo);
                }
            } else if (stato.getStato().getValore() >= 3) {
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
        this.portoScreen = portoScreen;
        
        
        engine.getSceneManager().registraScena(CostantiMappa.PORTO, portoScreen);
    }

    // Stalla: Mr. Cooper e tutta la storia delle carote
    private void costruisciStalla() {
        Map<double[], Runnable> zoneStalla = new HashMap<>();
        mrCooperInteraction = () -> {
            if (stato.getStato().getValore() >= 7) { // Da ENIGMA_FIORI_RISOLTO in poi (chiave ottenuta)
                mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("buona_fortuna").getAsString()));
                engine.mostraDialogoNPC(this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
            } else if (stato.getStato().getValore() >= 5) { // Da CAROTE_CONSEGNATE in poi
                mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("vai_al_bosco").getAsString()));
                engine.mostraDialogoNPC(this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
            } else {
                switch (stato.getStato()) {
                    case INIZIO:
                        mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("saluto").getAsString()));
                        Runnable loopCooper = new Runnable() {
                            @Override
                            public void run() {
                                String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi a Mr.Cooper?");
                                if (input == null) return;
                                if (Parser.contieneRadiceParola(input, "carroz*")) {
                                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("prezzo").getAsString()));
                                    engine.mostraDialogoNPCCallback(CostruttoreScene.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, () -> {
                                        String EryndorText = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("MrCooper").get("no_soldi").getAsString();
                                        engine.mostraDialogoCallback(CostruttoreScene.this.stallaScreen, CostantiMappa.STALLA, engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer(), EryndorText, null, () -> {
                                            mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("proposta").getAsString()));
                                            engine.mostraDialogoNPCCallback(CostruttoreScene.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, () -> {
                                                Runnable loopConfermaCooper = new Runnable() {
                                                    int countRifiuti = 0;
                                                    JsonArray rifiutiJson = engine.getDbHint().getAsJsonObject("Loop_Rifiuti_Quest").getAsJsonArray("MrCooper");
                                                    
                                                    @Override
                                                    public void run() {
                                                        int scelta = JOptionPane.showConfirmDialog(engine.getFrame(), "Accetti la proposta di Mr.Cooper?", "Scelta", JOptionPane.YES_NO_OPTION);
                                                        if (scelta == JOptionPane.YES_OPTION) {
                                                            mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("missione").getAsString()));
                                                            engine.mostraDialogoNPC(CostruttoreScene.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper);
                                                            setStatoCity(1);
                                                        } else {
                                                            String frase = rifiutiJson.get(Math.min(countRifiuti, rifiutiJson.size() - 1)).getAsString();
                                                            countRifiuti++;
                                                            mrCooper.setDialoghi(Arrays.asList(frase));
                                                            engine.mostraDialogoNPCCallback(CostruttoreScene.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, this);
                                                        }
                                                    }
                                                };
                                                loopConfermaCooper.run();
                                            });
                                        });
                                    });
                                } else {
                                    mrCooper.setDialoghi(Arrays.asList(mrCooperDb.get("non_capisco").getAsString()));
                                    engine.mostraDialogoNPCCallback(CostruttoreScene.this.stallaScreen, CostantiMappa.STALLA, mrCooper, spriteMrCooper, this);
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

    // Bosco e Bosco Deep: Fox, l'enigma dei fiori (Enigma 3) e la raccolta dei fiori
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
            if (stato.getStato().getValore() != 6) return;
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
            if (stato.getStato().getValore() < 6) {
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
            } else if (stato.getStato() == StatoStoria.INCONTRO_FOX) {
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
            if (stato.getStato().getValore() != 6) return;
            
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



        // Ripristino hitbox se l'utente ha già letto il cartello / ha già un fiore ricaricandole
        if (stato.getStato() == StatoStoria.INCONTRO_FOX) {
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

        // Hitbox cartello
        zoneBoscoDeep.put(CostantiHitbox.BOSCODEEP_CARTELLO, () -> {
            if (stato.getStato() == StatoStoria.INCONTRO_FOX) {
                cartelloLetto[0] = true;
                // Aggiungiamo i fiori dinamicamente in modo che il cursore a mano si attivi
                zoneBoscoDeep.put(hitboxFioreBlu, () -> raccogliFiore.accept(6));
                zoneBoscoDeep.put(hitboxFioreRosso, () -> raccogliFiore.accept(5));
                zoneBoscoDeep.put(hitboxFioreViola, () -> raccogliFiore.accept(7));

                EnigmaSceltaMultipla enigma3 = IstanzaEnigma.creaEnigma3(engine.getTxt().getOggettoDaCatalogo(1));
                engine.getStatistics().iniziaEnigma(enigma3);
                engine.mostraDialogoCallback(this.boscoDeepScreen, CostantiMappa.BOSCO_DEEP, "Cartello", enigma3.getTesto(), null, null);
            } else if (stato.getStato().getValore() >= 7) {
                String strFoxAddormentato = engine.getDbWallOfText().getAsJsonObject("Schermo").get("fox_addormentato").getAsString();
                engine.mostraDialogoCallback(this.boscoDeepScreen, CostantiMappa.BOSCO_DEEP, "Fantoccio", strFoxAddormentato, null, null);
            }
        });

        GameScreen boscoScreen = engine.getSceneManager().creaScenaBase("BoscoLosco.png", zoneBosco);
        this.boscoScreen = boscoScreen;
        engine.getSceneManager().registraScena(CostantiMappa.BOSCO, boscoScreen);
        
        GameScreen boscoDeepScreen = engine.getSceneManager().creaScenaBase("BoscoINN.png", zoneBoscoDeep);
        this.boscoDeepScreen = boscoDeepScreen;
        engine.getSceneManager().registraScena(CostantiMappa.BOSCO_DEEP, boscoDeepScreen);
        
        // Karundis
    }

    // Karundis: due NPC di passaggio e l'accesso alla Grotta
    private void costruisciKarundis() {
        Map<double[], Runnable> zoneKarundis = new HashMap<>();
        
        this.npcKarundis1 = registraNPC("Abitante 1", Arrays.asList(idleDialogs.get(0)));
        this.npcKarundis2 = registraNPC("Abitante 2", Arrays.asList(idleDialogs.get(1)));
        
        List<String> hintChiave = JsonLoader.estraiLista(engine.getDbHint(), "Ricerca_Chiave_Castello");
        if (stato.getStato().getValore() >= 8 && stato.getStato().getValore() < 9) {
            if (!stato.isPrimoAccessoPalazzo()) {
                npcKarundis1.setDialoghi(Arrays.asList(hintChiave.get(0)));
                npcKarundis2.setDialoghi(Arrays.asList(hintChiave.get(1)));
            } else {
                npcKarundis1.setDialoghi(Arrays.asList(idleDialogs.get(0)));
                npcKarundis2.setDialoghi(Arrays.asList(idleDialogs.get(1)));
            }
        } else if (stato.getStato().getValore() >= 9) {
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
            if (stato.getStato() == StatoStoria.ENIGMA_FIORI_RISOLTO) {
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
        
        
    }

    // Grotta
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
                        engine.mostraDialogoCallback(CostruttoreScene.this.grottaScreen, CostantiMappa.GROTTA, "Fantoccio", txtSblocco, null, () -> {
                            engine.mostraDialogoCallback(CostruttoreScene.this.grottaScreen, CostantiMappa.GROTTA, "Eryndor", txtSuccesso, null, null);
                        });
                    } else {
                        engine.mostraDialogoCallback(CostruttoreScene.this.grottaScreen, CostantiMappa.GROTTA, "Eryndor", engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Grotta").get("fallimento_spada").getAsString(), null, this);
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

    // Ingresso Palazzo: la Guardia Reale non ti fa passare senza la chiave
    private void costruisciIngressoPalazzo() {
        Map<double[], Runnable> zoneIngresso = new HashMap<>();
        JsonObject guardiaDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Guardiano");
        JsonObject eryndorGuardiaDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Guardiano");
        guardiaReale = registraNPC("Guardiano", Arrays.asList(guardiaDb.get("richiesta_chiave").getAsString()));
        
        double[] guardiaHitbox = CostantiHitbox.INGRESSO_GUARDIA;
        if (stato.getStato().getValore() < 9) {
            zoneIngresso.put(guardiaHitbox, () -> {
                if (!stato.isParlatoConGuardia()) {
                    guardiaReale.setDialoghi(Arrays.asList(guardiaDb.get("richiesta_chiave").getAsString()));
                    engine.mostraDialogoNPCCallback(this.ingressoScreen, CostantiMappa.INGRESSO_PALAZZO, guardiaReale, null, () -> {
                        stato.setParlatoConGuardia(true);
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
        this.ingressoScreen = ingressoScreen;
        engine.getSceneManager().registraScena(CostantiMappa.INGRESSO_PALAZZO, ingressoScreen);
        engine.getSceneManager().registraScena(CostantiMappa.SCALE, engine.getSceneManager().creaScenaBase("ScalePalazzo.png", null));
        
        
    }

    // Cripta: qui vive Eripeta
    private void costruisciCripta() {
        Map<double[], Runnable> zoneCripta = new HashMap<>();
        JsonObject eripetaDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta");
        registraNPC("Eripeta", Arrays.asList(eripetaDb.get("rifiuto").getAsString()));
        
        interazioneEripeta = () -> {
            JsonObject eryndorDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Eripeta");
            String eryName = engine.getGiocatore().getNomePlayer().isEmpty() ? "Eryndor" : engine.getGiocatore().getNomePlayer();
            ImageIcon spriteEripeta = new ImageIcon(getClass().getResource("/sprites/Personaggi/Eripeta.png"));

            if (stato.getStato().getValore() < 13) {
                String indizio2 = eripetaDb.get("indizio_2").getAsString();
                engine.mostraDialogoCallback(this.criptaScreen, CostantiMappa.CRIPTA_ERIPETA, "Eripeta", indizio2, spriteEripeta, null);
            } else if (stato.getStato() == StatoStoria.ENIGMA_VINCOLO_RISOLTO) {
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

    // Palazzo della Principessa: l'enigma finale dei vestiti e il gran finale
    private void costruisciPalazzoPrincipessa() {
        Map<double[], Runnable> zonePalazzo = new HashMap<>();
        JsonObject marienDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Marien");
        marien = registraNPC("Principessa Marien", Arrays.asList(marienDb.get("sfida").getAsString()));
        
        ImageIcon spriteMarien = new ImageIcon(getClass().getResource("/sprites/Personaggi/Marien.png"));
        ImageIcon spriteJack = new ImageIcon(getClass().getResource("/sprites/Personaggi/SirJack.png"));

        final boolean[] enigmaGiaVisto = {false};
        final String[] aiuti = new String[3];
        try {
            JsonObject hintRoot = aeg.giocomap.Util.JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");
            if(hintRoot != null && hintRoot.has("Aiuti_Enigmi")) {
                JsonObject aiutiEnigmi = hintRoot.getAsJsonObject("Aiuti_Enigmi");
                if (aiutiEnigmi.has("Enigma_Finale_Mercanti_Collaborano")) {
                    com.google.gson.JsonArray aiutiPrincipessa = aiutiEnigmi.getAsJsonArray("Enigma_Finale_Mercanti_Collaborano");
                    if (aiutiPrincipessa.size() >= 3) {
                        aiuti[0] = aiutiPrincipessa.get(0).getAsString();
                        aiuti[1] = aiutiPrincipessa.get(1).getAsString();
                        aiuti[2] = aiutiPrincipessa.get(2).getAsString();
                    }
                }
            }
        } catch(Exception e) {}

        zonePalazzo.put(CostantiHitbox.PALAZZO_MARIEN, () -> {
            if (engine.getGiocatore().isEnigmaRisolto("Enigma_Finale_Principessa")) {
                marien.setDialoghi(Arrays.asList("Sei già il mio sposo! Il regno è salvo."));
                engine.mostraDialogoNPC(this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, marien, spriteMarien);
                return;
            }
            
            // Il titolo nobiliare e' un premio narrativo, non un oggetto del catalogo:
            // nessuna reward da caricare o aggiungere all'inventario per questo enigma.
            EnigmaOrdinamento enigmaFinale = IstanzaEnigma.creaEnigmaFinale(null);
            engine.getStatistics().iniziaEnigma(enigmaFinale);
            
            // Testi
            String altriMercanti = engine.getDbWallOfText().getAsJsonObject("Schermo").get("altri_mercanti").getAsString();
            String mercantePiumatoNarrazione = engine.getDbWallOfText().getAsJsonObject("Schermo").get("mercante_piumato").getAsString();
            JsonObject piumatoDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Mercante_Piumato");
            String piumatoPresunzione = piumatoDb.get("presunzione").getAsString();
            String piumatoDisposizione = engine.getDbWallOfText().getAsJsonObject("Schermo").get("disposizione_mercante").getAsString();
            String marienErrore = marienDb.get("errore_enigma").getAsString();
            String piumatoDisperazione = piumatoDb.get("disperazione").getAsString();
            String catturaPiumato = engine.getDbWallOfText().getAsJsonObject("Schermo").get("cattura_piumato").getAsString();
            String marienPresentazione = marienDb.get("presentazione_enigma").getAsString();
            String marienTessuto1 = marienDb.get("enigma_tessuto_1").getAsString();
            String marienTessuto2 = marienDb.get("enigma_tessuto_2").getAsString();
            String marienTessuto3 = marienDb.get("enigma_tessuto_3").getAsString();
            String marienTessuto4 = marienDb.get("enigma_tessuto_4").getAsString();
            String marienTessuto5 = marienDb.get("enigma_tessuto_5").getAsString();
            String marienLeggiIntro = marienDb.get("enigma_leggi_intro").getAsString();
            String marienLeggi1 = marienDb.get("enigma_leggi_1").getAsString();
            String marienLeggi2 = marienDb.get("enigma_leggi_2").getAsString();
            String marienLeggi3 = marienDb.get("enigma_leggi_3").getAsString();
            String marienLeggi4 = marienDb.get("enigma_leggi_4").getAsString();
            JsonObject eryndorMarienDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Marien");

            // Sequenza Errore (Mercante Piumato interviene e viene cacciato)
            Runnable stepErroreEryndor = () -> {
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorMarienDb.get("errore_ordine").getAsString(), null, null);
            };
            Runnable stepCattura = () -> {
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", catturaPiumato, null, stepErroreEryndor);
            };
            Runnable stepDisperazione = () -> {
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Mercante Piumato", piumatoDisperazione, null, stepCattura);
            };
            Runnable stepMarienErrore = () -> {
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienErrore, spriteMarien, stepDisperazione);
            };
            Runnable stepDisposizione = () -> {
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", piumatoDisposizione, null, stepMarienErrore);
            };
            Runnable stepPresunzione = () -> {
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Mercante Piumato", piumatoPresunzione, null, stepDisposizione);
            };
            Runnable stepPiumatoNarrazione = () -> {
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", mercantePiumatoNarrazione, null, stepPresunzione);
            };

            // Popup interattivo: Eryndor sceglie e dispone i vestiti sui manichini uno alla volta.
            // Al primo vestito fuori posto l'enigma si interrompe subito e riparte la scena dell'errore.
            final int[] posizioneCorrente = {0};
            final DialogoOrdinamentoVestiti[] popupCorrente = new DialogoOrdinamentoVestiti[1];

            Runnable avviaPopupVestiti = () -> {
                posizioneCorrente[0] = 0;
                DialogoOrdinamentoVestiti popup = new DialogoOrdinamentoVestiti(engine.getFrame(), enigmaFinale.getVestiti(), vestitoScelto -> {
                    int posizione = posizioneCorrente[0];

                    if (enigmaFinale.verificaPosizione(posizione, vestitoScelto)) {
                        popupCorrente[0].posizionaVestito(posizione, vestitoScelto);
                        popupCorrente[0].disabilitaVestito(vestitoScelto);
                        posizioneCorrente[0]++;

                        if (posizioneCorrente[0] == enigmaFinale.getVestiti().size()) {
                            popupCorrente[0].dispose();
                            engine.getStatistics().enigmaRisolto(enigmaFinale);

                            // Vittoria
                            String successo = marienDb.get("successo_enigma").getAsString();
                            String ammirazione1 = marienDb.get("ammirazione_1").getAsString();
                            String ammirazione2 = marienDb.get("ammirazione_2").getAsString();
                            String ammirazione3 = marienDb.get("ammirazione_3").getAsString();
                            String rivelazione = marienDb.get("rivelazione").getAsString();
                            String finale = marienDb.get("finale").getAsString();

                            String eryndorPresentazione = eryndorMarienDb.get("presentazione").getAsString();
                            String eryndorRacconto = eryndorMarienDb.get("racconto").getAsString();
                            String eryndorPensieroSeduzione1 = eryndorMarienDb.get("pensiero_seduzione_1").getAsString();
                            String eryndorParlatoSeduzione1 = eryndorMarienDb.get("parlato_seduzione_1").getAsString();
                            String eryndorPensieroSeduzione2 = eryndorMarienDb.get("pensiero_seduzione_2").getAsString();
                            String eryndorParlatoSeduzione2 = eryndorMarienDb.get("parlato_seduzione_2").getAsString();

                            JsonObject jackDb = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Jack");
                            JsonObject eryndorJackDb = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("Jack");
                            String interruzioneJack = engine.getDbWallOfText().getAsJsonObject("Schermo").get("interruzione_jack").getAsString();
                            String jackIngresso = jackDb.get("ingresso").getAsString();
                            String jackSfida = jackDb.get("sfida").getAsString();
                            String potereSpada = engine.getDbWallOfText().getAsJsonObject("Schermo").get("potere_spada").getAsString();
                            String jackSconfitta = jackDb.get("sconfitta").getAsString();
                            String eryndorPensieroSfida = eryndorJackDb.get("pensiero_sfida").getAsString();
                            String eryndorPensieroSpada = eryndorJackDb.get("pensiero_spada").getAsString();
                            String eryndorPensieroVittoria = eryndorJackDb.get("pensiero_vittoria").getAsString();
                            String eryndorParlatoVittoria = eryndorJackDb.get("parlato_vittoria").getAsString();

                            // Avvia la scena muta di chiusura (nessun ESC/mappa/inventario/chat), poi i titoli di coda
                            Runnable stepAvvioFinale = () -> {
                                engine.setDialogueActive(true);
                                engine.getSceneManager().mostraScena(CostantiMappa.LETTERA_FINALE);
                                CostruttoreScene.this.schermataFinale.avviaSequenza();
                            };
                            Runnable stepFinale = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", finale, spriteMarien, stepAvvioFinale); };
                            // La Spada Sincro e' ormai al MAX (vedi stepPensieroSpada): l'inventario si
                            // sblocca da solo anche a dialogo attivo. Qui aggiungiamo solo il lampo
                            // arcobaleno a tutto schermo per dare enfasi a questo momento.
                            Runnable stepPotereSpada = () -> {
                                engine.getFrame().mostraLampoArcobaleno();
                                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", potereSpada, null, stepFinale);
                            };
                            Runnable stepParlatoVittoria = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorParlatoVittoria, null, stepPotereSpada); };
                            Runnable stepPensieroVittoria = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorPensieroVittoria, null, stepParlatoVittoria); };
                            Runnable stepSconfittaJack = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Jack", jackSconfitta, spriteJack, stepPensieroVittoria); };
                            Runnable stepPensieroSpada = () -> {
                                engine.getGiocatore().ricaricaSpadaSincro();
                                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorPensieroSpada, null, stepSconfittaJack);
                            };
                            Runnable stepPensieroSfida = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorPensieroSfida, null, stepPensieroSpada); };
                            Runnable stepJackSfida = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Jack", jackSfida, spriteJack, stepPensieroSfida); };
                            Runnable stepJackIngresso = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Jack", jackIngresso, spriteJack, stepJackSfida); };
                            Runnable stepInterruzioneJack = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", interruzioneJack, null, stepJackIngresso); };
                            Runnable stepRivelazione = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", rivelazione, spriteMarien, stepInterruzioneJack); };
                            Runnable stepParlatoSeduzione2 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorParlatoSeduzione2, null, stepRivelazione); };
                            Runnable stepPensieroSeduzione2 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorPensieroSeduzione2, null, stepParlatoSeduzione2); };
                            Runnable stepAmmirazione3 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", ammirazione3, spriteMarien, stepPensieroSeduzione2); };
                            Runnable stepParlatoSeduzione1 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorParlatoSeduzione1, null, stepAmmirazione3); };
                            Runnable stepPensieroSeduzione1 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorPensieroSeduzione1, null, stepParlatoSeduzione1); };
                            Runnable stepAmmirazione2 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", ammirazione2, spriteMarien, stepPensieroSeduzione1); };
                            Runnable stepRaccontoEryndor = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorRacconto, null, stepAmmirazione2); };
                            Runnable stepAmmirazione1 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", ammirazione1, spriteMarien, stepRaccontoEryndor); };
                            Runnable stepPresentazioneEryndor = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Eryndor", eryndorPresentazione, null, stepAmmirazione1); };

                            engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", successo, spriteMarien, stepPresentazioneEryndor);
                        }
                    } else {
                        // Vestito fuori posto -> parte subito la scena dell'errore, l'enigma andrà ripetuto
                        popupCorrente[0].dispose();
                        stepPiumatoNarrazione.run();
                    }
                });
                popupCorrente[0] = popup;
                popup.setVisible(true);
            };
            
            // Lettura Enigma e attivazione hitbox
            Runnable stepLeggi = () -> {
                enigmaGiaVisto[0] = true;
                if (aiuti[0] != null) {
                    zonePalazzo.put(CostantiHitbox.PALAZZO_MERCANTE_1, () -> {
                        engine.mostraDialogoCallback(this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", aiuti[0], null, null);
                    });
                    zonePalazzo.put(CostantiHitbox.PALAZZO_MERCANTE_2, () -> {
                        engine.mostraDialogoCallback(this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", aiuti[1], null, null);
                    });
                    zonePalazzo.put(CostantiHitbox.PALAZZO_MERCANTE_3, () -> {
                        engine.mostraDialogoCallback(this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", aiuti[2], null, null);
                    });
                }
                
                Runnable stepLeggi4 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienLeggi4, spriteMarien, avviaPopupVestiti); };
                Runnable stepLeggi3 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienLeggi3, spriteMarien, stepLeggi4); };
                Runnable stepLeggi2 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienLeggi2, spriteMarien, stepLeggi3); };
                Runnable stepLeggi1 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienLeggi1, spriteMarien, stepLeggi2); };
                Runnable stepLeggiIntro = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienLeggiIntro, spriteMarien, stepLeggi1); };
                
                Runnable stepTessuto5 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienTessuto5, spriteMarien, stepLeggiIntro); };
                Runnable stepTessuto4 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienTessuto4, spriteMarien, stepTessuto5); };
                Runnable stepTessuto3 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienTessuto3, spriteMarien, stepTessuto4); };
                Runnable stepTessuto2 = () -> { engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienTessuto2, spriteMarien, stepTessuto3); };
                
                engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienTessuto1, spriteMarien, stepTessuto2);
            };
            
            // Presentazione (solo prima volta)
            if (!enigmaGiaVisto[0]) {
                Runnable stepAltriMercanti = () -> {
                    engine.mostraDialogoCallback(CostruttoreScene.this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Fantoccio", altriMercanti, null, stepLeggi);
                };
                engine.mostraDialogoCallback(this.palazzoScreen, CostantiMappa.PALAZZO_PRINCIPESSA, "Principessa Marien", marienPresentazione, spriteMarien, stepAltriMercanti);
            } else {
                stepLeggi.run();
            }
        });
        
        GameScreen palazzoScreen = engine.getSceneManager().creaScenaBase("SalaDellaPrincipessa.png", zonePalazzo);
        this.palazzoScreen = palazzoScreen;

        engine.getSceneManager().registraScena(CostantiMappa.PALAZZO_PRINCIPESSA, palazzoScreen);
    }

    // Le schermate a tutto schermo delle lettere (iniziale, retro e finale)
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
        
        List<String> letteraFinale = JsonLoader.estraiLista(engine.getDbWallOfText().getAsJsonObject("Lettera"), "lettera_finale");
        this.schermataFinale = new SchermataFinale(letteraFinale, () -> {
            engine.setDialogueActive(false);
            engine.getStatistics().statistiche(true);
        });
        engine.getSceneManager().registraScena(CostantiMappa.LETTERA_FINALE, this.schermataFinale);
    }

    // Scarica il lavoro sporco al RegistroNPC
    private Personaggio registraNPC(String nome, List<String> dialoghi) {
        return registroNPC.registraNPC(nome, dialoghi);
    }
}