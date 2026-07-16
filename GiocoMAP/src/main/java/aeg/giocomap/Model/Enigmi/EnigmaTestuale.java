package aeg.giocomap.Model.Enigmi;

import aeg.giocomap.Model.Oggetti.Oggetto;
import java.util.List;

/**
 *
 * @author giulio
 */
public class EnigmaTestuale extends Enigma {

    private final String soluzione;

    public EnigmaTestuale(String id, String testo, List<String> aiuti,Oggetto reward, String soluzione) {
        super(id, testo, aiuti, reward);
        this.soluzione = soluzione.trim().toLowerCase();
    }

    @Override
    public boolean verifica(String risposta) {
        if (risposta == null) return false;

        /* Pulizia input con regex, praticamente rimuove spazi extra e 
           converte in minuscolo
        */
        String pulita = risposta.trim().toLowerCase().replaceAll("\\s+", " ");
        boolean corretta = pulita.equals(soluzione);

        if (corretta) {
            risolto = true;
            System.out.println("DEBUG: Enigma " + id + " risolto");
        } else {
            System.out.println("DEBUG: Risposta errata: " + pulita);
        }

        return corretta;
    }
}