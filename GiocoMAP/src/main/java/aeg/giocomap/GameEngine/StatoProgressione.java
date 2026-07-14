package aeg.giocomap.GameEngine;

/**
 *
 * @author emanuele
 */
public class StatoProgressione {

    private StatoStoria statoAttuale = StatoStoria.INIZIO;

    // Primo accesso al palazzo: serve a mostrare una sola volta una frase
    private boolean primoAccessoPalazzo = true;

    // Indica se abbiamo già parlato alla guardia del cancello almeno una volta
    private boolean parlatoConGuardia = false;

    /** Stato logico corrente come enum. */
    public StatoStoria getStato() {
        return statoAttuale;
    }

    /** Stato di avanzamento come intero (per salvataggio/caricamento). */
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
}