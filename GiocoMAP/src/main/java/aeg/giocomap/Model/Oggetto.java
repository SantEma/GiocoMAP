/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model;

/**
 *
 * @author emanuele
 */
public class Oggetto {
    int id;
    String nomeOggetto;
    
    public Oggetto(int id, String nome){
        this.id=id;
        this.nomeOggetto=nome;
    }

    public int getIdOggetto(){
        return id;
    }
    
    public String getNomeOggetto(){
        return nomeOggetto;
    }
    
}