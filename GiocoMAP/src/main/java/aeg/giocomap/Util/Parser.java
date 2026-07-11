package aeg.giocomap.Util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser {
    
    // Controlla se la parola chiave è presente nell'input usando regex
    public static boolean contieneParolaChiave(String input, String parolaChiave) {
        // Se non mette nulla o non è quella giusta, dare indietro false
        if (input == null || parolaChiave == null) return false;
        
        // Rimuovo gli spazi dalla parola chiave 
        String chiavePulita = parolaChiave.replaceAll("\\s+", "");
        
        // Regex che accetta spazi opzionali tra ogni carattere della parola chiave
        StringBuilder regexChiave = new StringBuilder();
        for (int i = 0; i < chiavePulita.length(); i++) {
            regexChiave.append(Pattern.quote(String.valueOf(chiavePulita.charAt(i))));
            if (i < chiavePulita.length() - 1) {
                regexChiave.append("\\s*");
            }
        }
        
        String regex = "(?i).*\\b" + regexChiave.toString() + "\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}
