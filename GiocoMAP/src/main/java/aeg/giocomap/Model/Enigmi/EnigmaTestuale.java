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
 * Enigma con risposta testuale libera.
 * L'input viene normalizzato con Regex prima del confronto
 * per ignorare maiuscole e spazi extra.
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

        /* Pulizia input con Regex, praticamente rimuove spazi extra e 
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