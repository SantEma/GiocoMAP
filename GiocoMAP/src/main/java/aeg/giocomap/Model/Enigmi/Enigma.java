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

    public Enigma(String id, String testo, List<String> aiuti, Oggetto reward) {
        this.id = id;
        this.testo = testo;
        this.aiuti = aiuti;
        this.reward = reward;
        this.risolto = false;
    }

        public String getId() { 
        return id; 
    }
    
    public String getTesto() {
        return testo; 
    }
    
    public Oggetto getReward() { 
        return reward; 
    }
    
    public List<String> getAiuti() {
        return aiuti;
    }

    public abstract boolean verifica(String risposta);

}
