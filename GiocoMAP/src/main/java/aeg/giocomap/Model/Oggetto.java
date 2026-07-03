/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

/**
 *
 * @author emanu
 */
public abstract class Oggetto {
    int id;
    String nome;
    
    public Oggetto(int id, String nome){
        this.id=id;
        this.nome=nome;
    }

    public int getId(){
        return id;
    }
    
    public String getNome(){
        return nome;
    }
    
    public class Spada extends Oggetto {
    private int caricaSincro;
    
    public Spada(int id, String nome){
        super(id, nome);
        this.caricaSincro=0;
    }
    
    // Metodo per ricaricare la spada dopo un enigma
    public void ricarica(int percentuale) {
        this.caricaSincro+=percentuale;
        if (this.caricaSincro>100) {
            this.caricaSincro=100;
        }
        System.out.println("La " + getNome() + "ha come carica attuale: " + this.caricaSincro + "%");
    }

    public int getCaricaSincro() {
        return caricaSincro;
    }
}
}