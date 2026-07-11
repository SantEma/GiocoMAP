/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package aeg.giocomap.GameEngine;

import javax.swing.Timer;

/**
 *
 * @author murgo
 *
 * Il tempo trascorso viene calcolato dall'orologio di sistema (non da un
 * contatore incrementato in un thread): cosi getSecondi() e' sempre corretto,
 * anche mentre una finestra modale (JOptionPane) tiene occupato l'EDT.
 */
public class TimerEnigma {

    private long inizioMs;
    private int secondiFinali;
    private boolean attivo;

    // usato solo per l'azione periodica di debug, eseguita sull'EDT
    private final Timer tickTimer;

    public TimerEnigma(Runnable onTick) {
        this.tickTimer = new Timer(1000, e -> {
            if (onTick != null) onTick.run();
        });
        this.secondiFinali = 0;
        this.attivo = false;
    }

    public boolean isAttivo() {
        return attivo;
    }

    // Secondi trascorsi: calcolati in tempo reale mentre e' attivo,
    // oppure il valore finale congelato dopo ferma()
    public int getSecondi() {
        if (attivo) {
            return (int) ((System.currentTimeMillis() - inizioMs) / 1000);
        }
        return secondiFinali;
    }

    // Avvia il timer
    public void avvia() {
        inizioMs = System.currentTimeMillis();
        secondiFinali = 0;
        attivo = true;
        tickTimer.start();
        System.out.println("DEBUG: Timer avviato");
    }

    // Ferma il timer e congela i secondi trascorsi
    public void ferma() {
        if (attivo) {
            secondiFinali = (int) ((System.currentTimeMillis() - inizioMs) / 1000);
            attivo = false;
        }
        tickTimer.stop();
        System.out.println("DEBUG: Timer fermato a " + secondiFinali + "s");
    }
}
