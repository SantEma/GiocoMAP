/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Personaggi;

/**
 *
 * @author emanuele
 */
public abstract class Entity {
    private final String nome;

    public Entity(String nome){
        this.nome = nome;
    }
    
    public String getNome(){ 
        return nome; 
    }
}
