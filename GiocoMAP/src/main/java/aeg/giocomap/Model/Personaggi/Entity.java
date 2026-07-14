package aeg.giocomap.Model.Personaggi;

/**
 *
 * @author emanuele
 */

/*
    [Da commentare]
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
