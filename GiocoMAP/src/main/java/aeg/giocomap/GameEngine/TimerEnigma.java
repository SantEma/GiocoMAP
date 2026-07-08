/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

/**
 *
 * @author murgo
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
    
    //Metodo che restituisce i secondi trascorsi
    public int getSecondi() { 
        return secondi; 
    }
    
    //Avvia il timer
    public void avvia() {
        attivo = true;
        secondi = 0;
        new Thread(this).start();
        System.out.println("DEBUG: Timer avviato");
    }

    //Ferma il timer
    public void ferma() {
        attivo = false;
        System.out.println("DEBUG: Timer fermato a " + secondi + "s");
    }

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
