package aeg.giocomap.Model.Enigmi;

import aeg.giocomap.Model.Oggetti.Oggetto;
import java.util.List;

/**
 *
 * @author emanuele
 */
public class EnigmaOrdinamento extends Enigma {

    private final List<String> vestiti;
    private final List<String> ordineCorretto;

    public EnigmaOrdinamento(String id, String testo, List<String> aiuti, Oggetto reward, List<String> vestiti, List<String> ordineCorretto) {
        super(id, testo, aiuti, reward);
        this.vestiti = vestiti;
        this.ordineCorretto = ordineCorretto;
    }

    public List<String> getVestiti() {
        return vestiti;
    }

    public List<String> getOrdineCorretto() {
        return ordineCorretto;
    }

    // Verifica un singolo vestito appena viene posizionato su un manichino,
    // per poter interrompere l'enigma al primo errore invece di aspettare la combinazione completa.
    public boolean verificaPosizione(int posizione, String vestito) {
        return posizione >= 0 && posizione < getOrdineCorretto().size()
                && getOrdineCorretto().get(posizione).equals(vestito);
    }

    @Override
    public boolean verifica(String risposta) {
        if (risposta == null) return false;
        String[] scelta = risposta.split(",");
        if (scelta.length != getOrdineCorretto().size()) return false;

        for (int i = 0; i < scelta.length; i++) {
            if (!getOrdineCorretto().get(i).equals(scelta[i].trim())) {
                System.out.println("DEBUG: Vestito errato in posizione " + i + ": " + scelta[i].trim());
                return false;
            }
        }

        risolto = true;
        System.out.println("DEBUG: Enigma " + id + " risolto!");
        return true;
    }
}
