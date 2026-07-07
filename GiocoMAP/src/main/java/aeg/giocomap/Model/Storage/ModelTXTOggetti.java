/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Model.Storage;

import aeg.giocomap.Model.Oggetti.Oggetto;
import aeg.giocomap.Model.Oggetti.Spada;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author emanuele
 */
public class ModelTXTOggetti {
    private Map<Integer, Oggetto> catalogoOggetti;
            
    public ModelTXTOggetti(){
        loadOggettiDaCatologo();
    }
    
    private void loadOggettiDaCatologo(){
        catalogoOggetti = new HashMap<>();
        
        try{
            //Leggiamo il file
            BufferedReader reader=new BufferedReader(new FileReader("src/main/resources/oggetti/oggetti.txt"));
            
            //Variabili temporanee che verranno man mano sovrascritte
            String linea;
            int currentId=-1;
            String currentNome="";
            String currentDesc="";
            boolean isNuovoOggetto;
            
            // Fin tanto che la linea letta da file non è nulla...
            while ((linea=reader.readLine())!=null) {
                //... e fin tanto che non è vuota..
                if (linea.trim().isEmpty()){
                    continue;
                }
                /* 
                ... allora divido la linea in diverse parti dove trovo
                il punto e virgola (deciso tra noi come separatore nel file)
                e poi salvo tutto in 3 parti, in modo da istanziare
                */
                String[] parti=linea.split(";",3);
                
                if (parti.length==3){
                    try {
                        // Rimuovo gli spazi bianchi tramite trim()
                        currentId = Integer.parseInt(parti[0].trim());
                        currentNome = parti[1].trim();
                        currentDesc = parti[2].trim();
                        inserisciOggetto(currentId, currentNome, currentDesc);
                    } catch (NumberFormatException e) {
                        System.err.println("DEBUG: Impossibile convertire ID in numero sulla linea n."+linea);
                    }
                } else {
                    System.err.println("DEBUG: Formato della riga non valido, attese 3 parti:"+ linea);
                }
            }
                reader.close();
                System.out.println("DEBUG: Catalogo oggetti: "+ catalogoOggetti.size()+" presenti all'interno");
        }
        catch(IOException e){
            System.out.println("DEBUG: Errore imprevisto: " + e);
        }
    }
    
    public Oggetto getOggettoDaCatalogo(int id){
        return catalogoOggetti.get(id);
    }
    
    private void inserisciOggetto(int id, String nome, String descrizione){
        Oggetto nuovoOggetto;
        
        switch(id){
            case 10:
                nuovoOggetto=new Spada(id, nome, descrizione);
                break;
            default:
                nuovoOggetto=new Oggetto(id, nome, descrizione);
                break;
        }
        
        catalogoOggetti.put(id, nuovoOggetto);
    }
}
