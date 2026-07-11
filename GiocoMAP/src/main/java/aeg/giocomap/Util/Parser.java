package aeg.giocomap.Util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser {
    
    // Controlla se la parola chiave è presente nell'input usando regex
    // Ignora spazi extra, punteggiatura e case. 
    // Cerca la parola chiave all'interno della stringa.
    public static boolean contieneParolaChiave(String input, String parolaChiave) {
        if (input == null || parolaChiave == null) return false;
        String regex = "(?i).*\\b" + Pattern.quote(parolaChiave) + "\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}
