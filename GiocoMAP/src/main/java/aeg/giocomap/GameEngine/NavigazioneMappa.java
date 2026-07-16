package aeg.giocomap.GameEngine;

import aeg.giocomap.Model.Stanza.Stanza;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.Model.Personaggi.Fantoccio;
import aeg.giocomap.View.GameScreen;
import aeg.giocomap.Util.JsonLoader;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author giulio
 */
public class NavigazioneMappa {

    private final GameEngine engine;
    private final StatoProgressione stato;
    private final RegistroNPC registroNPC;
    private final CostruttoreScene costruttore;

    // Struttura dati per astrarre la logica dei percorsi
    private final Map<String, Map<String, Runnable>> collegamentiMappa = new HashMap<>();

    // Mappa per le stanze 
    private final Map<String, Stanza> mappaStanze = new HashMap<>();

    public NavigazioneMappa(GameEngine engine, StatoProgressione stato,
                            RegistroNPC registroNPC, CostruttoreScene costruttore) {
        this.engine = engine;
        this.stato = stato;
        this.registroNPC = registroNPC;
        this.costruttore = costruttore;
    }

    private Stanza getOrCreaStanza(String nome) {
        return mappaStanze.computeIfAbsent(nome, n -> new Stanza(n));
    }

    // Direzioni delle frecce, 
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
                engine.exitGame();
            }
        });

        String testoLocali = engine.getDbStoria().getAsJsonObject("Eryndor").getAsJsonObject("inizio").get("locali_chiusi").getAsString();
        Fantoccio fantoccioOvest = registroNPC.registraFantoccio("Fantoccio_Ovest", Arrays.asList(testoLocali));

        registraCollegamento(CostantiMappa.PIAZZA_CENTRALE, CostantiMappa.OVEST, () -> {
            if (engine.getGiocatore().isPossiedeMappa()) {
                engine.getSceneManager().mostraScena(CostantiMappa.STALLA);
                if ((stato.getStato() == StatoStoria.INIZIO || stato.getStato() == StatoStoria.CONSEGNATA_CENA) && costruttore.getMrCooperInteraction() != null) {
                    costruttore.getMrCooperInteraction().run();
                }
            } else {
                GameScreen piazza = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.PIAZZA_CENTRALE);
                engine.mostraDialogoNPC(piazza, CostantiMappa.PIAZZA_CENTRALE, fantoccioOvest, null);
            }
        });

        String testoBlocco = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Guardiano").get("stop_carrozza").getAsString();
        Personaggio guardiano = registroNPC.registraNPC("Guardiano", Arrays.asList(testoBlocco));

        registraCollegamento(CostantiMappa.PIAZZA_CENTRALE, CostantiMappa.EST, () -> {
            if (stato.getStato() == StatoStoria.CAROTE_CONSEGNATE) {
                engine.getSceneManager().mostraScena(CostantiMappa.BOSCO);
                if (costruttore.getFoxInteraction() != null) costruttore.getFoxInteraction().run();
            } else if (stato.getStato().getValore() > 5) {
                engine.getSceneManager().mostraScena(CostantiMappa.BOSCO);
            } else {
                GameScreen piazza = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.PIAZZA_CENTRALE);
                engine.mostraDialogoNPC(piazza, CostantiMappa.PIAZZA_CENTRALE, guardiano, null);
            }
        });

        registraCollegamentoSemplice(CostantiMappa.STALLA, CostantiMappa.NORD, CostantiMappa.PIAZZA_CENTRALE);

        registraCollegamentoSemplice(CostantiMappa.BOSCO, CostantiMappa.SUD, CostantiMappa.PIAZZA_CENTRALE);
        registraCollegamento(CostantiMappa.BOSCO, CostantiMappa.NORD, () -> {
            if (stato.getStato().getValore() >= 7) {
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
            if (stato.getStato() == StatoStoria.INCONTRO_FOX) {
                boolean haFiore = engine.getGiocatore().getInventario().cercaOggetto("Fiore Viola") != null ||
                                  engine.getGiocatore().getInventario().cercaOggetto("Fiore Rosso") != null ||
                                  engine.getGiocatore().getInventario().cercaOggetto("Fiore Blu") != null;
                if (haFiore && costruttore.getFoxInteraction() != null) {
                    costruttore.getFoxInteraction().run();
                }
            }
        });

        registraCollegamentoSemplice(CostantiMappa.KARUNDIS, CostantiMappa.OVEST, CostantiMappa.BOSCO);

        String testoBloccoKarundis = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Guardiano").get("stop_palazzo").getAsString();
        Personaggio fantoccioNord = registroNPC.registraNPC("Guardiano", Arrays.asList(testoBloccoKarundis));

        registraCollegamento(CostantiMappa.KARUNDIS, CostantiMappa.NORD, () -> {
            if (engine.getGiocatore().getInventario().cercaOggetto("Spada Sincro") != null) {
                if (stato.isPrimoAccessoPalazzo()) {
                    stato.setPrimoAccessoPalazzo(false);
                    // Al primo ingresso al cancello, attiviamo gli indizi per la chiave negli abitanti di Karundis
                    if (costruttore.getNpcKarundis1() != null && costruttore.getNpcKarundis2() != null) {
                        List<String> hints1 = JsonLoader.estraiLista(engine.getDbHint(), "Ricerca_Chiave_Castello");
                        costruttore.getNpcKarundis1().setDialoghi(Arrays.asList(hints1.get(0)));
                        costruttore.getNpcKarundis2().setDialoghi(Arrays.asList(hints1.get(1)));
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
        Fantoccio bloccoChiaveNord = registroNPC.registraFantoccio("Blocco_Chiave_Nord", Arrays.asList(testoBloccoChiave));

        registraCollegamento(CostantiMappa.INGRESSO_PALAZZO, CostantiMappa.NORD, () -> {
            if (stato.getStato().getValore() >= 9) {
                engine.getSceneManager().mostraScena(CostantiMappa.SCALE);
                if (stato.getStato() == StatoStoria.CHIAVE_CONSEGNATA) {
                    costruttore.avviaIntercettazioneEripeta();
                }
            } else {
                GameScreen ingresso = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.INGRESSO_PALAZZO);
                engine.mostraDialogoNPC(ingresso, CostantiMappa.INGRESSO_PALAZZO, bloccoChiaveNord, null);
            }
        });

        registraCollegamentoSemplice(CostantiMappa.SCALE, CostantiMappa.SUD, CostantiMappa.INGRESSO_PALAZZO);
        registraCollegamento(CostantiMappa.SCALE, CostantiMappa.NORD, () -> {
            if (stato.getStato().getValore() >= 15) {
                engine.getSceneManager().mostraScena(CostantiMappa.PALAZZO_PRINCIPESSA);
            } else {
                GameScreen scale = (GameScreen) engine.getSceneManager().getScena(CostantiMappa.SCALE);
                String rifiuto = engine.getDbStoria().getAsJsonObject("Dialoghi_NPC").getAsJsonObject("Eripeta").get("rifiuto").getAsString();
                engine.mostraDialogoCallback(scale, CostantiMappa.SCALE, "Eripeta", rifiuto, new ImageIcon(getClass().getResource("/sprites/Personaggi/Eripeta.png")), null);
            }
        });
        registraCollegamento(CostantiMappa.SCALE, CostantiMappa.OVEST, () -> {
            engine.getSceneManager().mostraScena(CostantiMappa.CRIPTA_ERIPETA);
            if (stato.getStato().getValore() < 15) {
                if (costruttore.getInterazioneEripeta() != null) costruttore.getInterazioneEripeta().run();
            }
        });

        registraCollegamentoSemplice(CostantiMappa.PALAZZO_PRINCIPESSA, CostantiMappa.SUD, CostantiMappa.SCALE);
        registraCollegamentoSemplice(CostantiMappa.CRIPTA_ERIPETA, CostantiMappa.EST, CostantiMappa.SCALE);
    }

    private void registraCollegamento(String daScena, String direzione, Runnable azione) {
        collegamentiMappa.computeIfAbsent(daScena, k -> new HashMap<>()).put(direzione, azione);
    }

    private void registraCollegamentoSemplice(String daScena, String direzione, String aScena) {
        Stanza da = getOrCreaStanza(daScena);
        Stanza a = getOrCreaStanza(aScena);
        da.impostaUscita(direzione, a);
    }

    private void eseguiCollegamento(String direzione) {
        String scena = engine.getSceneManager().getScenaCorrente();
        if (scena == null) return;

        // Assicuriamoci che il giocatore sappia in che stanza si trova
        Stanza stanzaCorrente = getOrCreaStanza(scena);

        Map<String, Runnable> usciteSpeciali = collegamentiMappa.get(scena);
        if (usciteSpeciali != null && usciteSpeciali.containsKey(direzione)) {
            // Usa la logica custom/condizionale
            usciteSpeciali.get(direzione).run();
        } else {
            // Usa la classe Stanza per il routing standard
            Stanza adiacente = stanzaCorrente.getStanzaAdiacente(direzione);
            if (adiacente != null) {
                engine.getSceneManager().mostraScena(adiacente.getNome());
            } else {
                System.out.println("DEBUG: Nessuna direzione a " + direzione + " da " + scena);
            }
        }
    }
}