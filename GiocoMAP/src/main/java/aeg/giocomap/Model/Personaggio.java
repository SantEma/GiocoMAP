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
    
    private String nome;
    private List<String> alberoDialoghi;
    private int indiceDialogoAttuale;

    public Personaggio(String nome) {
        this.nome = nome;
        this.alberoDialoghi = new ArrayList<>();
        this.indiceDialogoAttuale = 0;
    }

    public String getNome() {
        return nome;
    }

    // Metodo per settare i dialoghi letti dal file JSON
    public void setDialoghi(List<String> dialoghi) {
        this.alberoDialoghi = dialoghi;
        this.indiceDialogoAttuale = 0;
    }

    // Gestisce la progressione della conversazione
    public String parla() {
        if (alberoDialoghi == null || alberoDialoghi.isEmpty()) {
            return "..."; // Risposta di default se non ci sono dialoghi
        }
        
        // Se ci sono ancora battute nuove, le legge e avanza
        if (indiceDialogoAttuale < alberoDialoghi.size() - 1) {
            String battuta = alberoDialoghi.get(indiceDialogoAttuale);
            indiceDialogoAttuale++;
            return battuta;
        }
        
        // Se il dialogo è finito, l'NPC ripete l'ultima frase ciclicamente
        return alberoDialoghi.get(alberoDialoghi.size() - 1);
    }
    
    // Utile se l'NPC deve cambiare argomento (es. un nuovo enigma risolto)
    public void resetDialogo() {
        this.indiceDialogoAttuale = 0;
    }
}
