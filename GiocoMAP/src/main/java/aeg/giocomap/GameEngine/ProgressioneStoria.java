package aeg.giocomap.GameEngine;

/**
 *
 * @author emanuele
 */
public class ProgressioneStoria {

    private final StatoProgressione stato = new StatoProgressione();
    private final RegistroNPC registroNPC = new RegistroNPC();
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