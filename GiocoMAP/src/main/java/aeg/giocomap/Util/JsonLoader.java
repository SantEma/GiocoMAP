/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.Util;

/**
 *
 * @author murgo
 */

import com.google.gson.*;
import java.io.*;


public class JsonLoader {
    
    public static JsonObject caricaJson(String percorso){
        try{
            // Apre il file come uno stream, in percorso viene inserito il percorso del file json a cui si vuole accedere
            InputStream is = JsonLoader.class.getResourceAsStream(percorso);
            
            // Se non trova il percorso restituisce errore
            if(is == null){
                System.err.println("File JSON non trovato: " + percorso);
                return null;
            }
            
            // Legge lo stream e restituisce un oggetto json 
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
        catch(Exception e){
            System.err.println("Errore lettura JSON " + percorso + ": " + e.getMessage());
            return null;
        }
    }
    
    /**

 */
    public static String estraiStringa(JsonObject oggetto, String chiave){
    try{
        return oggetto.get(chiave).getAsString();
    }
    catch(Exception e){
        System.err.println("Errore estrazione stringa " + chiave + ": " + e.getMessage());
        return "";
    }
}

    /**
     * Estrae un array di stringhe da un JsonObject e le deserializza
     */
    public static String estraiTesto(JsonObject oggetto, String chiave){
        try{
            JsonArray array = oggetto.getAsJsonArray(chiave);
            StringBuilder sb = new StringBuilder();
            for(JsonElement riga : array){
                sb.append(riga.getAsString());
            }
            return sb.toString();
        }
        catch(Exception e){
            System.err.println("Errore estrazione chiave " + chiave + ": " + e.getMessage());
            return "";
        }
    }
}