package aeg.giocomap.GameEngine;

import aeg.giocomap.Model.Giocatore.Giocatore;
import aeg.giocomap.Model.Storage.ModelDB;
import aeg.giocomap.View.MainFrame;
import aeg.giocomap.View.TitoliDiCoda;
import aeg.giocomap.Model.Enigmi.Enigma;
import javax.swing.JOptionPane;
import java.util.List;

public class GameStatistics {

    private int punteggioTotale = 0;
    private TimerEnigma timerEnigma;
    private String enigmaCorrenteId;   // id dell'enigma attualmente cronometrato

    private final MainFrame frame;
    private final Giocatore giocatore;
    private final ModelDB db;
    private final MusicPlayer music_player;
    private final SceneManager sceneManager;

    public GameStatistics(MainFrame frame, Giocatore giocatore, ModelDB db, MusicPlayer music_player, SceneManager sceneManager) {
        this.frame = frame;
        this.giocatore = giocatore;
        this.db = db;
        this.music_player = music_player;
        this.sceneManager = sceneManager;
    }

    public void iniziaEnigma(Enigma enigma) {
        String id = (enigma != null) ? enigma.getId() : null;

        // se sto gia' cronometrando LO STESSO enigma non ancora risolto,
        // NON resetto il timer (evita che ri-cliccando l'NPC riparta il tempo)
        if (timerEnigma != null && timerEnigma.isAttivo()
                && id != null && id.equals(enigmaCorrenteId)) {
            System.out.println("DEBUG: Timer gia' attivo per " + id + ", non resetto");
            return;
        }

        // enigma diverso (o nessun timer attivo): fermo il precedente e riparto
        if (timerEnigma != null) timerEnigma.ferma();
        enigmaCorrenteId = id;
        timerEnigma = new TimerEnigma(() -> {
            System.out.println("DEBUG: Secondi: " + timerEnigma.getSecondi());
        });
        timerEnigma.avvia();
        System.out.println("DEBUG: Enigma iniziato: " + id);
    }

    public void enigmaRisolto(Enigma enigma) {
        // registro l'enigma come risolto (per il salvataggio / no-redo)
        if (enigma != null) giocatore.aggiungiEnigmaRisolto(enigma.getId());

        if (timerEnigma != null) timerEnigma.ferma();

        int secondi = timerEnigma != null ? timerEnigma.getSecondi() : 0;
        int punti = calcolaPunti(secondi);
        punteggioTotale += punti;

        if (enigma.getReward() != null) {
            giocatore.getInventario().aggiungiOggetto(enigma.getReward());
            System.out.println("DEBUG: Reward aggiunto: " + enigma.getReward().getNomeOggetto());
        }

        System.out.println("DEBUG: " + secondi + "s, " + punti + " punti, totale: " + punteggioTotale);
    }

    private int calcolaPunti(int secondi) {
        int fascia;
        if (secondi <= 100)      fascia = 1;
        else if (secondi <= 150) fascia = 2;
        else if (secondi <= 220) fascia = 3;
        else if (secondi <= 380) fascia = 4;
        else                     fascia = 5;

        return switch (fascia) {
            case 1 -> 1000;
            case 2 -> 700;
            case 3 -> 500;
            case 4 -> 300;
            default -> 100;
        };
    }

    public void Statistiche(boolean fineGioco) {
        String nome = "";
        int punteggio = 0;

        if (fineGioco) {
            punteggio = punteggioTotale;

            while (nome == null || nome.trim().isEmpty()) {
                nome = JOptionPane.showInputDialog(frame,
                    "Inserisci il tuo nome per salvare il punteggio:",
                    "Fine gioco!", JOptionPane.PLAIN_MESSAGE);
                if (nome == null || nome.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                        "Devi inserire un nome per continuare!",
                        "Attenzione", JOptionPane.WARNING_MESSAGE);
                }
            }
            giocatore.setNomePlayer(nome.trim());
            db.salvaSeNecessario(giocatore.getNomePlayer(), punteggio);
        }

        List<String[]> records = db.getRecords();
        String nome_passato = (giocatore != null && giocatore.getNomePlayer() != null
            ? giocatore.getNomePlayer() : "");
        music_player.stopMusic();
        if (fineGioco) music_player.playMusic(MusicPlayer.END_TITLE_MUSIC);

        // dal menu (fineGioco=false) mostro solo la Hall of Fame;
        // a fine partita (fineGioco=true) i titoli di coda completi
        TitoliDiCoda titoli = new TitoliDiCoda(records, punteggio, nome_passato, !fineGioco);

        Runnable tornaAlMenu = () -> {
            music_player.stopMusic();
            music_player.playMusic(MusicPlayer.TITLE_SCREEN_MUSIC);
            sceneManager.mostraScena("MENU_PRINCIPALE");
        };

        if (fineGioco)
            titoli.setOnFine(tornaAlMenu);              // titoli: tornano da soli a fine scorrimento
        else
            titoli.addIndietroListener(e -> tornaAlMenu.run()); // statistiche: bottone indietro

        sceneManager.registraScena("TITOLI_CODA", titoli);
        sceneManager.mostraScena("TITOLI_CODA");
    }
}
