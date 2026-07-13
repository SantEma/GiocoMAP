package aeg.giocomap.GameEngine;

/**
 * Coordinatore della progressione della storia.
 *
 * Dopo lo scorporamento della god class originale, questa classe si limita a
 * orchestrare i collaboratori specializzati e a mantenere la superficie
 * pubblica usata da {@link GameEngine} (routing frecce, costruzione scene,
 * stato di salvataggio/caricamento). La logica vera vive in:
 * <ul>
 *   <li>{@link StatoProgressione} — stato e flag di save/load</li>
 *   <li>{@link RegistroNpc} — registro dei personaggi</li>
 *   <li>{@link NavigazioneMappa} — routing delle frecce</li>
 *   <li>{@link CostruttoreScene} — assemblaggio scene e sequenze narrative</li>
 * </ul>
 *
 * @author emanuele
 */
public class ProgressioneStoria {

    private final StatoProgressione stato = new StatoProgressione();
    private final RegistroNpc registroNPC = new RegistroNpc();
    private final CostruttoreScene costruttore;
    private final NavigazioneMappa navigazione;

    public ProgressioneStoria(GameEngine engine) {
        this.costruttore = new CostruttoreScene(engine, stato, registroNPC);
        this.navigazione = new NavigazioneMappa(engine, stato, registroNPC, costruttore);
    }

    public void impostaFrecceLogica() {
        navigazione.impostaFrecceLogica();
    }

    public void costruisciScene() {
        costruttore.costruisciScene();
    }

    // stato di avanzamento della fase città (per salvataggio/caricamento)
    public int getStatoCity() {
        return stato.getStatoCity();
    }

    public void setStatoCity(int valore) {
        stato.setStatoCity(valore);
    }

    public boolean isPrimoAccessoPalazzo() {
        return stato.isPrimoAccessoPalazzo();
    }

    public void setPrimoAccessoPalazzo(boolean valore) {
        stato.setPrimoAccessoPalazzo(valore);
    }

    public void setParlatoConGuardia(boolean valore) {
        stato.setParlatoConGuardia(valore);
    }
}