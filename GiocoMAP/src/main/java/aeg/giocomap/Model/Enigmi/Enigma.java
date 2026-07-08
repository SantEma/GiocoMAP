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

public abstract class Enigma {

    protected final String id;
    protected final String testo;
    protected final List<String> aiuti;
    protected final Oggetto reward;
    protected boolean risolto;
    protected int indiceAiutoCorrente;

    public Enigma(String id, String testo, List<String> aiuti, Oggetto reward) {
        this.id = id;
        this.testo = testo;
        this.aiuti = aiuti;
        this.reward = reward;
        this.risolto = false;
        this.indiceAiutoCorrente = 0;
    }

        public String getId() { 
        return id; 
    }
    
    public String getTesto() {
        return testo; 
    }
    
    public boolean isRisolto() { 
        return risolto; 
    }
    
    public Oggetto getReward() { 
        return reward; 
    }
    
    public abstract boolean verifica(String risposta);

    // Restituisce il prossimo aiuto disponibile
    public String prossimoAiuto() {
        if (aiuti == null || aiuti.isEmpty())
            return "Nessun aiuto disponibile.";
        if (indiceAiutoCorrente >= aiuti.size())
            return "Nessun altro aiuto disponibile.";
        return aiuti.get(indiceAiutoCorrente++);
    }


}
