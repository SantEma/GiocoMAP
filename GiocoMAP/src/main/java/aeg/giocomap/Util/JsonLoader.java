package aeg.giocomap.Util;

import com.google.gson.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class JsonLoader {
    
    public static JsonObject caricaJson(String percorso){
        try{
            InputStream is = JsonLoader.class.getResourceAsStream(percorso);
            if(is == null){
                System.err.println("File JSON non trovato: " + percorso);
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
        catch(JsonIOException | JsonSyntaxException | UnsupportedEncodingException e){
            System.err.println("Errore lettura JSON " + percorso + ": " + e.getMessage());
            return null;
        }
    }
    
    public static String estraiStringa(JsonObject oggetto, String chiave){
        try{
            return oggetto.get(chiave).getAsString();
        }
        catch(Exception e){
            System.err.println("Errore estrazione stringa " + chiave + ": " + e.getMessage());
            return "";
        }
    }

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

    public static List<String> estraiLista(JsonObject oggetto, String chiave){
        List<String> lista = new ArrayList<>();
        try{
            JsonArray array = oggetto.getAsJsonArray(chiave);
            for(JsonElement riga : array){
                lista.add(riga.getAsString());
            }
        }
        catch(Exception e){
            System.err.println("Errore estrazione lista " + chiave + ": " + e.getMessage());
        }
        return lista;
        }
    
    }