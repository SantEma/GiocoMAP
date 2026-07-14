package aeg.giocomap.GameEngine;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 *
 * @author andrea
 */
public class MusicPlayer {
    /*È un oggetto della libreria javax.sound.sampled 
     progettato appositamente per caricare in memoria file audio .wav e riprodurli
    */
    private Clip clip;
    private final String[] tracceAudio;
    
    public static final int TITLE_SCREEN_MUSIC = 0;
    public static final int END_TITLE_MUSIC = 1;
    
    // Costruttore con tutte le tracce audio
    public MusicPlayer(){
        tracceAudio = new String[3];
        
        tracceAudio[0] = "/musiche/GreenlandsTitleScreen.wav"; //Ttile screen
        tracceAudio[1] = "/musiche/DEAFKEVEndTitle.wav"; // End Screen
        //tracceAudio[2] = "daviderechemettere";
    }
    
    public void playMusic(int i){
        try{
            //Seleziono la traccia dall'array
            BufferedInputStream bs = new BufferedInputStream(getClass().getResourceAsStream(tracceAudio[i])); 
            // Java ha bisogno del Buffered per scorrere la canzone per intero, il resto del procedimento segue lo stile delle immagini
            AudioInputStream as = AudioSystem.getAudioInputStream(bs); // Traduce il wav nel formato leggibile java
            clip = AudioSystem.getClip();
            clip.open(as); // Inseriamo la traccia loop dentro il lettore java
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
        catch (IOException | LineUnavailableException | UnsupportedAudioFileException e){
            System.out.println("DEBUG: Traccia numero "+i+" non trovata "+ e.getMessage());
        }
    }
    
    public void stopMusic(){
        if(clip != null && clip.isRunning()){
            // Deallocare le risorse quando la musica si dovrà fermare
            clip.stop();
            clip.close();
            clip=null;
        }
    } 
}
