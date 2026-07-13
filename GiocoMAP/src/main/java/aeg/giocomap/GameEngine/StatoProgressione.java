package aeg.giocomap.GameEngine;

/**
 * Stato di avanzamento della storia (fase "Città con il porto").
 *
 * Estratto da {@link ProgressioneStoria}: raccoglie i dati che descrivono
 * a che punto è la partita e che costituiscono la superficie di
 * salvataggio/caricamento. Nessuna logica di gioco: solo lo stato e i suoi
 * accessori, con lo stesso comportamento che avevano come campi della god
 * class.
 */

/**
 *
 * @author Emanuele
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