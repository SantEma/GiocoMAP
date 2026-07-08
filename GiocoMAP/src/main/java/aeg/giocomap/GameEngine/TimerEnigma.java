/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

/**
 *
 * @author murgo
 */
/**
 * Thread concorrente che misura il tempo impiegato dal giocatore
 * per risolvere un enigma. Gira in background senza bloccare la UI.
 * onTick viene eseguito sul thread di Swing ogni secondo.
 */ 

public class TimerEnigma implements Runnable {

    private boolean attivo;
    private int secondi;
    private final Runnable onTick;//azione eseguita ogni secondo

    public TimerEnigma(Runnable onTick) {
        this.onTick = onTick;
        this.secondi = 0;
        this.attivo = false;
    }
    
    //avvia il timer
    public void avvia() {
        attivo = true;
        secondi = 0;
        new Thread(this).start();
        System.out.println("DEBUG: Timer avviato");
    }

    //ferma il timer
    public void ferma() {
        attivo = false;
        System.out.println("DEBUG: Timer fermato a " + secondi + "s");
    }
    
    //metodo che restituisce i secondi trascorsi
    public int getSecondi() { return secondi; }

    @Override
    public void run() {
        while (attivo) {
            try {
                Thread.sleep(1000);
                secondi++;
                if (onTick != null) {
                    javax.swing.SwingUtilities.invokeLater(onTick);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
