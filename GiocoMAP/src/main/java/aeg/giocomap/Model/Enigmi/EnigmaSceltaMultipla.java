/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Enigmi;

import aeg.giocomap.Model.Oggetti.Oggetto;
import java.util.List;

/**
 *
 * @author murgo
 */
/**
 * Enigma con risposta a scelta multipla.
 * Il giocatore passa l'indice dell'opzione scelta (0-based).
 * Viene confrontato con l'indice della soluzione corretta.
 */

public class EnigmaSceltaMultipla extends Enigma {

    private final List<String> opzioni;
    private final int indiceSoluzione;  // indice corretto (0-based)

    public EnigmaSceltaMultipla(String id, String testo, List<String> aiuti,
                                 Oggetto reward, List<String> opzioni,
                                 int indiceSoluzione) {
        super(id, testo, aiuti, reward);
        this.opzioni = opzioni;
        this.indiceSoluzione = indiceSoluzione;
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
                System.out.println("DEBUG: Opzione errata → " + scelta);
            }

            return corretta;
        } catch (NumberFormatException e) {
            System.out.println("DEBUG: Input non valido → " + risposta);
            return false;
        }
    }

    public List<String> getOpzioni() { return opzioni; }
}