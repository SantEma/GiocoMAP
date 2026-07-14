package aeg.giocomap.Model.Enigmi;

import aeg.giocomap.Model.Oggetti.Oggetto;
import java.util.List;

/**
 *
 * @author giulio
 */
public class EnigmaSceltaMultipla extends Enigma {

    private final List<String> opzioni;
    private final int indiceSoluzione;  // Indice corretto (0-based)

    public EnigmaSceltaMultipla(String id, String testo, List<String> aiuti, Oggetto reward, List<String> opzioni,int indiceSoluzione) {
        super(id, testo, aiuti, reward);
        this.opzioni = opzioni;
        this.indiceSoluzione = indiceSoluzione;
    }

    public List<String> getOpzioni() { 
        return opzioni; 
    }
        
    @Override
    public boolean verifica(String risposta) {
        try {
            int scelta = Integer.parseInt(risposta.trim());
            boolean corretta = (scelta == indiceSoluzione);

            if (corretta) {
                risolto = true;
                System.out.println("DEBUG: Enigma " + id + " risolto!");
            } else {
                System.out.println("DEBUG: Opzione errata : " + scelta);
            }

            return corretta;
        } catch (NumberFormatException e) {
            System.out.println("DEBUG: Input non valido : " + risposta);
            return false;
        }
    }


}