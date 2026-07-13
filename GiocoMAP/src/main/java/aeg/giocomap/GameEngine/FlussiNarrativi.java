package aeg.giocomap.GameEngine;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Personaggi.Personaggio;
import aeg.giocomap.View.GameScreen;
import aeg.giocomap.Util.Parser;
import aeg.giocomap.Model.Enigmi.EnigmaSceltaMultipla;
import aeg.giocomap.Model.Enigmi.IstanzaEnigma;

import com.google.gson.JsonObject;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Sequenze narrative della fase finale della storia.
 *
 * Estratte da {@link CostruttoreScene}: raccolgono i dialoghi a catena
 * dell'intercettazione e dell'accusa di Eripeta, e il flusso di David legato
 * all'Enigma del Vincolo (Enigma 5). I metodi sono per lo più auto-contenuti
 * (recuperano gli schermi via SceneManager o li ricevono come parametro); i
 * "ganci" sulle hitbox degli abitanti/di David sono creati dai costruttori di
 * scena e letti in modo lazy da {@link CostruttoreScene} al momento
 * dell'esecuzione del flusso.
 * 
 * @author emanuele
 * 
 */
public class FlussiNarrativi {

    private final GameEngine engine;
    private final StatoProgressione stato;
    private final CostruttoreScene costruttore;

    public FlussiNarrativi(GameEngine engine, StatoProgressione stato, CostruttoreScene costruttore) {
        this.engine = engine;
        this.stato = stato;
        this.costruttore = costruttore;
    }

    private void setStatoCity(int valore) {
        stato.setStatoCity(valore);
    }

    void avviaIntercettazioneEripeta() {
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
                Runnable riattiva = costruttore.getRiattivaDavidDopoMappa();
                if (riattiva != null) riattiva.run();
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

    void gestisciDavidDopoMappa(GameScreen portoScreen, Personaggio david, ImageIcon spriteDavid, JsonObject davidDb) {
        if (stato.getStato() == StatoStoria.ACCUSA_ERIPETA_SUPERATA) {
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
        } else if (stato.getStato() == StatoStoria.DAVID_INTERPELLATO) {
            lanciaEnigma5(portoScreen, david, spriteDavid, davidDb);
        } else if (stato.getStato() == StatoStoria.ENIGMA_VINCOLO_RISOLTO) {
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
        Runnable attiva = costruttore.getAttivaAiutiEnigma5();
        if (attiva != null) attiva.run();

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
                    Runnable disattiva = costruttore.getDisattivaAiutiEnigma5();
                    if (disattiva != null) disattiva.run();

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
}