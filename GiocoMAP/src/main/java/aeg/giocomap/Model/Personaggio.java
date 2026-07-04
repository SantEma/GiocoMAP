/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emanuele
 */
public class Personaggio {
    
    private String nomePersonaggio;
    private List<String> alberoDialoghi;
    private int indiceDialogoAttuale;

    public Personaggio(String nomePersonaggio) {
        this.nomePersonaggio=nomePersonaggio;
        this.alberoDialoghi=new ArrayList<>();
        this.indiceDialogoAttuale=0;
    }

    public String getNomePersonaggio() {
        return nomePersonaggio;
    }
    
    public void setDialoghi(List<String> dialoghi) {
        this.alberoDialoghi=dialoghi;
        this.indiceDialogoAttuale=0;
    }
    
    public String parla() {
        if (alberoDialoghi==null || alberoDialoghi.isEmpty()) {
            return "..."; // Risposta di default 
        }
        
        if (indiceDialogoAttuale<alberoDialoghi.size()-1) {
            String battuta=alberoDialoghi.get(indiceDialogoAttuale);
            indiceDialogoAttuale++;
            return battuta;
        }
        
        // Quando il dialogo è finito l'NPC ripete la sua ultima frase ciclicamente
        return alberoDialoghi.get(alberoDialoghi.size()-1);
    }
    
    // Utile se l'NPC deve cambiare argomento (da vedere se verrà utilizzato)
    public void resetDialogo() {
        this.indiceDialogoAttuale=0;
    }
    
}
