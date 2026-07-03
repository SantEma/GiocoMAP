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
}