# Indice

1. [Spiegazione gioco](#giocomap)
2. [Trama](#trama)
3. [Comandi](#comandi)
4. [Regole per il gioco](#regole)
5. [Avvio Gioco](#avvio-gioco)

# GiocoMAP
Gioco creato da **Emanuele Santoruvo, Giulio Murgo e Andrea Milo per il corso Metodi Avanzati di Programmazione A.A. 2025/2026**.  
Il gioco è un **point&click grafico-testuale** con lo scopo di risolvere enigmi di vario tipo per avanzare nella trama.  
Gli enigmi sono di tipo **logici, backtracking ed enigmi visivi**.

La trama e gli indovinelli del gioco sono frutto delle idee di tutto il team collaborando con le nostre conoscenze e idee generali, traendo ispirazione però da vari franchise per la scelta grafica e degli enigmi, tra questi risultano specialmente:
- Professor Layton
- Phoenix Wright Ace Attorney
- Monkey Island

## Trama
La trama generale si basa sulla scalata sociale del protagonista **Eryndor**, il quale è stato invitato a palazzo di corte, assieme a tutti i mercanti del regno di **Shambhala**, per portare il loro tessuto più pregiato, con il migliore sarà cucito il vestito nuziale per il matrimonio della **figlia del Re** e il capo delle guardie reali **Jack**.

Dalla città iniziale fino al castello, **Eryndor** deve superare vari **enigmi e indovinelli**, incontrando molti personaggi lungo il suo cammino e molte avversità.
Arrivato al castello, tramite alcuni di questi personaggi, nel corso dei dialoghi il giocatore scoprirà che il vero obiettivo del protagonista è sposare la principessa **Marien** per diventare a tutti gli effetti un nobile e ampliare la sua attività mercantile.  
Su questa parte sarà presente la riflessione finale, attuando una **critica al giocatore** e sul mondo **consumer** attuale.

>[!NOTE]
>Per più informazioni sulla **trama, idee implementative e tecniche usate** verificare la **documentazione del gioco**.

## Comandi
I comandi di gioco sono molto semplici e variano a seconda della situazione presente nel gioco, tra cui:
- **Click del mouse**, la maggior parte delle situazioni presenti nel gioco, come i dialoghi, scovare oggetti e il movimento stesso del protagonista avvengono tramite click del mouse sugli scenari e sull'ambiente circostante
- **Comandi da tastiera**, i quali possono essere input di parole tramite **Parser** o premere dei veri e propri **caratteri da tastiera**; di questi ultimi si ha:
    - *M* per l'apertura della mappa (una volta ottenuta)
    - *I* per l'apertura dell'inventario
    - *C* per avviare la chat di gioco e vedere il proprio **indirizzo IP** per far collegare gli altri giocatori nella chat
    - *ESC* per aprire il menù di pausa

# Regole
 Il gioco per essere giocato al meglio presenta alcune **regole da seguire**:
 - Non possono essere create più istanze all'avvio dello stesso gioco, altrimenti il **DB** va in conflitto, specialmente per caricare i salvataggi della partita
 - Il giocatore a fine punteggio può scegliere qualsiasi **nickname** lui voglia
 - Per un'ottima esperienza di gioco è preferibile soffermarsi a leggere con calma qualsiasi indovinello e tornare indietro per cercare aiuti e non cliccare *all'impazzata*

## Punteggio
Per le statistiche abbiamo decretato un nostro sistema di punteggio, basato sulla fascia di tempo che il giocatore impiega per risolvere le sezioni degli enigmi, sulla base della logica di un giocatore che si interfaccia per la prima volta e creando varie fasce per spaziare ed essere più vasti così da aumentare in futuro la propria rapidità.

Di seguito sono riportate le fasce di punteggio:

| Tempo impiegato | Punteggio ottenuto |
| :--- | :---: |
| Sotto i 100 secondi | **1000** |
| Entro i 150 secondi | **700** |
| Entro i 220 secondi | **500** |
| Entro i 380 secondi | **300** |
| Maggiore di 380 secondi | **100** |

# Avvio Gioco
Essendo il progetto basato su **Maven**, il gioco è facilmente eseguibile dall'IDE di NetBeans ( o qualsiasi altra IDE che supporti i **pre-requisiti elencati**), non avendo così la necessità di avere un file `.jar` compilato.

### Pre-requisiti
Per avviare correttamente il gioco, assicurarsi di avere installati sul proprio sistema:
- **Java Development Kit (JDK)** (versione 25)
- **Apache Maven**

### Istruzioni all'avvio
1. Clonare il repository in locale
2. Spostarsi all'interno della cartella principale del progetto
3. Build & Clean del progetto, così da far installare le 'dipendenze' nel `pom.xml`

#### Note Tecniche (Database e Chat)
*   **Database Locale (H2):** Il gioco utilizza **H2 Database** in modalità file locale.   Al primo avvio o salvataggio, il sistema creerà automaticamente una cartella `saves` nella directory principale del progetto per memorizzare la partita in corso e la classifica (Hall of Fame) dei giocatori. 
*   **Gestione Chat Multigiocatore:** Il sistema di chat interno si basa su un'architettura Host/Client che comunica tramite socket sulla porta TCP **12345**.
    *   Il primo giocatore che apre la chat diventa automaticamente il server (**Host**) e gli verrà riservato il nome "Eryndor". Il gioco calcolerà e mostrerà a schermo il suo indirizzo IP locale.
    *   Gli altri giocatori (Client) dovranno selezionare "Partecipa alla Chat", inserire l'indirizzo IP dell'Host e scegliere un nome utente non duplicato e diverso da "Eryndor".
    *   *Avvertenza per il Firewall:* Per il corretto funzionamento in una rete locale (LAN) tra macchine fisicamente diverse, assicurarsi che il firewall del computer Host consenta le connessioni in entrata sulla porta **12345**. Altrimenti si può provare tramite *VPN* o tramite software che simulino la rete locale.

Un'altra piccola nota tecnica meno importante, anche se nei contributors sono segnate 4 persone, due sono la stessa persona. MiloMilu e AndreaMilo sono la stessa persona, solamente che per motivi di spostamenti ho dovuto accedere per una commit da un dispositivo diverso.

Inoltre consigliamo vivamente di scaricare tutto il progetto per vedere i link inseriti nei file `.md` poiché usano i collegamenti formattati in stile obsidian, dato che abbiamo usato questo software di editor markdown per la stesura dei file di testo
