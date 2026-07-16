# **The Royal Silk Adventure**
Il progetto è stato presentato dal gruppo **AEG**  per l'**esame di Metodi Avanzati di Programmazione** *M-Z* *A.A. 2025/2026*
### Componenti
- **Andrea Milo**
- **Emanuele Santoruvo**
- **Giulio Murgo**
# Indice
1. [[#Descrizione dell’avventura]]  
2. [[#Progettazione]]
	 2.1 [[#Diagramma delle classi]]
	 2.2 [[#Specifica algebrica]]
	 2.3 [[#Dettagli implementativi]]
3. [[#Informazioni sul lavoro di gruppo e sul progetto]]
# Descrizione dell’avventura
L’avventura consiste nella storia del protagonista **Eryndor**, mercante di tessuti alla ricerca di un titolo nobiliare.

Nel regno si dovranno svolgere le nozze della **Principessa Marien**, ed il Re del regno è alla ricerca del **tessuto** perfetto per il matrimonio.

Da qui inizia il lungo e tortuoso cammino verso il **castello**, che avrà diversi imprevisti ed **enigmi** da risolvere incontrando diversi personaggi che lo ostacoleranno o lo aiuteranno nel suo percorso.

Avventuratevi e scoprite se il nostro protagonista riuscirà a raggiungere il suo obiettivo, e qual è il suo **vero obiettivo….**

Per consultare la **trama completa nel suo sviluppo** e visualizzare gli **enigmi con la loro soluzione** e spiegazione matematica, visualizzare il file [**Trama.md**](Idee%20trama/Trama.md)
# Progettazione
Le classi sono state individuate a partire da un'analisi del **dominio del problema** e dei **casi d'uso** del gioco.

Ad ogni classe è stato assegnato il principio della **singola responsabilità (SRP)**: ogni classe è stata pensata per avere una sola ragione di cambiamento. Da qui la scelta di separare, ad esempio, la logica di un enigma (`Enigma`) dalla sua rappresentazione grafica (`DialogueScreen`, `DialogoOrdinamentoVestiti`) e dalla sua persistenza (`ModelDB`).

Dove più classi condividevano attributi e comportamenti comuni si è introdotta una **gerarchia di ereditarietà** ( es.  `Enigma` come base astratta dei tre tipi di enigma). Dove invece serviva un contratto comune senza condivisione di implementazione si è usata un'**interfaccia** (`GiveObject`).

#### Organizzazione in package
L'applicazione è strutturata secondo il pattern architetturale **MVC (Model–View–Controller):**

| Package | Ruolo | Contenuto |
| --- | --- | --- |
| `aeg.giocomap` | Entry point | `GiocoMAP` (metodo `main`) |
| `aeg.giocomap.View` | **View** – interfaccia grafica (Swing) | `MainFrame`, `GameScreen`, `DialogueScreen`, `ChatPanel`, `MappaPanel`, `InventarioPanel`, … |
| `aeg.giocomap.GameEngine` | **Controller** – orchestrazione e logica di gioco | `GameEngine`, `SceneManager`, `GameStatistics`, `NavigazioneMappa`, `TimerEnigma`, … |
| `aeg.giocomap.Model` | **Model** – dominio, suddiviso in sotto-package | vedi sotto |
| `aeg.giocomap.Network` | Modulo multiplayer (chat) | `GameServer`, `GameClient`, `ClientHandler`, `Message`, … |
| `aeg.giocomap.Util` | Utilità trasversali | `JsonLoader`, `Parser`, `CursorUtil` |

Il **Model** è ulteriormente scomposto per area di responsabilità:

- `Model.Enigmi`: `Enigma`, `EnigmaTestuale`, `EnigmaSceltaMultipla`, `EnigmaOrdinamento`, `IstanzaEnigma`
- `Model.Giocatore` : `Giocatore`, `Inventario`
- `Model.Oggetti` : `Oggetto`, `Spada`, `GiveObject`
- `Model.Personaggi` : `Entity`, `Personaggio`, `Fantoccio`
- `Model.Stanza` : `Stanza`
- `Model.Storage` : `ModelDB`, `ModelTXTOggetti`

L'idea base è stata quella di massimizzare la **coesione interna**  e minimizzare l'**accoppiamento tra package**. Le dipendenze fluiscono coerentemente con MVC: la View non conosce il Model se non attraverso il Controller; il `GameEngine` (Controller) coordina Model e View; il Model non dipende né dalla View né dal Controller.

#### Controller — `aeg.giocomap.GameEngine`

- **`GameEngine`** : è il **controller** centrale creato dal `main`. Riceve gli input, coordina Model e View e mantiene lo stato di alto livello della partita (scena da salvare, dialogo attivo). Precarica i database dei dialoghi/storia/hint (JSON), istanzia `ModelDB`/`ModelTXTOggetti`, `SceneManager` e la rete, e gestisce il ciclo di gioco (`avviaGioco`, `salvaEdEsci`, mostra dialoghi con NPC e wall-of-text).
- **`SceneManager`** : gestisce il **cambio di scena** e la cache dei pannelli. Sa quale scena è correntemente mostrata (per il salvataggio), gestisce gli overlay (mappa, puntatore, inventario, chat) e ripristina la scena sottostante alla loro chiusura, delegando a `MainFrame` la visualizzazione concreta.
- **`GameStatistics`** : tiene il **punteggio** e le statistiche di sessione: avvia/chiude gli enigmi (`iniziaEnigma`, `enigmaRisolto`), calcola i punti in base al tempo impiegato (`calcolaPunti`) tramite `TimerEnigma`.
- **`TimerEnigma`** : cronometra la risoluzione di un enigma, fornendo a `GameStatistics` il tempo impiegato.
- Oltre alle classi mostrate, il package comprende alcune classi di supporto al controller: **`StatoProgressione`** conserva lo stato di avanzamento della partita e si appoggia all'enum **`StatoStoria`**, che elenca in ordine le tappe narrative; **`CostruttoreScene`** costruisce le scene di gioco (sfondi, personaggi, enigmi) ed è usata da `ProgressioneStoria`, mentre **`FlussiNarrativi`** gestisce le sequenze narrative complesse ed è composta da `CostruttoreScene`. **`MusicPlayer`**, infine, riproduce le tracce audio ed è richiamata dal `GameEngine`, senza dipendere da altre classi del progetto.

#### Model — dominio
Esistono vari package Model che gestiscono logiche di back in base al compito da svolgere, favorendo così l'**incapsuletion hiding**, di seguito vengono riportati con le loro classi:

- **Enigmi (`Model.Enigmi`)**:
	- **`Enigma`** *(astratta)*:  Definisce lo stato comune (`id`, `testo`, `aiuti`, `reward`, `risolto`) e la risposta `verifica(risposta)`, lasciato astratto perché ogni tipo di enigma valida la risposta in modo diverso (**polimorfismo**).
	- **`EnigmaTestuale`**: enigma a risposta libera: confronta la risposta con una `soluzione`.
	- **`EnigmaSceltaMultipla`**: enigma a opzioni: valida l'indice/valore scelto tra le `opzioni`.
	- **`EnigmaOrdinamento`**: enigma in cui va ricostruito l'ordine corretto (es. sequenza di vestiti), con `verificaPosizione` per la validazione parziale.
	- Il package include anche **`IstanzaEnigma`**, che funge da costruttore concreto degli enigmi: carica testi e aiuti dai file JSON (tramite `JsonLoader`) e istanzia il tipo di `Enigma` appropriato, restituendo l'oggetto `Oggetto` da assegnare come ricompensa.

* **Giocatore (`Model.Giocatore`)**

	- **`Giocatore`**: rappresenta lo **stato del giocatore**: nome, possesso di mappa/inventario, insieme degli enigmi risolti. Espone le operazioni sul progresso (`aggiungiEnigmaRisolto`, `isEnigmaRisolto`) e possiede un `Inventario`.
	- **`Inventario<T>`**: collezione **generica** di oggetti posseduti dal giocatore (aggiunta/rimozione/ricerca).

- **Oggetti (`Model.Oggetti`)**

	- **`Oggetto`**: entità  raccoglibile/utilizzabile, identificata da `idOggetto`, `nome`, `descrizione`.
	- **`Spada`**: oggetto speciale che implementa `GiveObject`: reagisce alla risoluzione di un enigma (`reagisciRisoluzioneEnigma`) e gestisce una carica (`caricaSincro`) con soglia massima.
	- **`GiveObject`** *(interfaccia)*: contratto per gli oggetti che devono **reagire** al completamento di un enigma; disaccoppia chi risolve l'enigma dall'oggetto che ne subisce l'effetto.

- **Personaggi (`Model.Personaggi`)**

	- **`Entità`** *(astratta)*: base di tutti i personaggi, incapsula il `nome`.
	- **`Personaggio`**: NPC con albero di dialoghi: gestisce l'avanzamento delle battute (`parla`, `setDialoghi`, `resetDialogo`).
	- **`Fantoccio`**: personaggio "muto"/decorativo, specializzazione minimale di `Personaggio`

- **Storage (`Model.Storage`)**

	- **`ModelDB`**:  connessione, salvataggio/caricamento partita (`salvaPartita`, `loadGame`), gestione record e punteggi.
	- **`ModelTXTOggetti`**: carica il **catalogo degli oggetti** da file di testo in una `Map<Integer, Oggetto>`, fungendo da sorgente dati per gli oggetti di gioco.

- **Model (`Model.Stanza`)**
	Gestisce il salvataggio della stanza adiacente, il nome della stanza da salvare per il DB e il `Map<String, Stanza>` per gestire la stanza, da stringa da prendere a stanza restituita.

####  View — `aeg.giocomap.View`

- **`MainFrame`**: la **finestra principale** (`JFrame`). Ospita i pannelli delle scene tramite un `JLayeredPane`, gestisce elementi sempre in sovraimpressione (bottone chat, frecce di navigazione, effetti) e il ridimensionamento. È l'unico punto di ingresso grafico verso cui il Controller invia i pannelli da mostrare.
- **`GameScreen`**: pannello che **disegna una scena** di gioco: renderizza l'immagine di sfondo e registra le zone cliccabili (hotspot) associate ad azioni (`Runnable`).
- **`DialogueScreen`**: schermata di **dialogo/enigma**: mostra testo, opzioni e aiuti e raccoglie la risposta del giocatore.
- **`InventarioPanel` / `MappaPanel`**: overlay per **inventario** e **mappa**, mostrati sopra la scena corrente e gestiti dal `SceneManager`.
  
![alt text|505](InventarioSenzaDescrizione.png)
![alt text|626](Mappa%201.png)

- **`ChatPanel`**: pannello grafico della **chat multiplayer**: area messaggi, campo di input e invio. Fa da ponte tra la View e il `GameClient` del package Network.

![alt text|697](ProvaDiChatting%201.png)

#### Network — `aeg.giocomap.Network`
Modulo autonomo che implementa una **chat multiplayer** con architettura **client-server su socket TCP**.

- **`GameNetwork`**: Coordina l'avvio come **host** (`GameServer` + `GameClient` locale) o l'ingresso come client (`connettiComeClient`), rileva l'IP locale da **condividere** e collega il `ChatPanel` alla connessione.
- **`GameServer`** *(Runnable)*: il **server**: apre il `ServerSocket` sulla porta, accetta le connessioni creando un `ClientHandler` per ciascun client, gestisce l'elenco dei nomi connessi (**unicità**) e il **broadcast** dei messaggi a tutti.
- **`GameClient`**: il **client**: si connette al server, invia messaggi (`invia`) e avvia il `ThreadRicezione` per l'ascolto asincrono.
- **`ClientHandler`** *(Runnable)*: gestisce **un singolo client** lato server, su **thread** dedicato: legge i messaggi in arrivo, verifica i nomi duplicati e inoltra al server per il broadcast (*JOIN/CHAT/LEAVE*).
- **`ThreadRicezione`** *(Runnable)*: thread di **ricezione asincrona** dei messaggi lato client; formatta i messaggi ricevuti e notifica la View tramite callback (`SwingUtilities.invokeLater`), garantendo l'aggiornamento sul thread grafico.
- **`Message`**: oggetto  che modella un messaggio (tipo, mittente, contenuto).
- **`TipoMessaggio`** *(enum)*: tipi di messaggio del protocollo: `JOIN`, `LEAVE`, `CHAT`, `NOME_DUPLICATO`.
- **`MessageParser`**: **serializzazione/deserializzazione** dei messaggi in stringhe da trasmettere sul socket (formato `tipo|mittente|contenuto`), incapsulando il protocollo di comunicazione.

#### Util — `aeg.giocomap.Util`
Raccolta di classi di supporto trasversali, senza logica di dominio:

- **`JsonLoader`**: lettura dei database JSON di dialoghi/storia/hint
- **`Parser`**: parsing di dati testuali
- **`CursorUtil`**: gestione del cursore e delle **zone cliccabili** nelle scene

####  Strumenti esterni utilizzati

- **Mermaid**: per la stesura dei diagrammi UML (diagramma delle classi, e volendo diagrammi di sequenza/stato per i flussi di gioco e di rete).
- **Gson**: libreria per la (de)serializzazione JSON, usata da `JsonLoader` per leggere i file di dialoghi/storia/hint.
- **H2**: database relazionale embedded usato da `ModelDB` per il salvataggio/caricamento delle partite.
- **Java Swing**: libreria grafica per l'interfaccia utente (`MainFrame`, `GameScreen`, `DialogueScreen`, pannelli overlay), con zone cliccabili responsive basate su percentuali (`CursorUtil`) per adattarsi al ridimensionamento della finestra.
- **Socket TCP**: per il modulo di rete multiplayer (`GameServer`/`GameClient`),

## Diagramma delle classi

[Cliccare qui per visualizzare l’UML.](UML%20e%20design/UML%20delle%20classi.png)

Il diagramma delle classi rappresenta la struttura del sistema: le classi, i loro attributi e metodi e le relazioni che le legano. Si è fatto uso dei principali meccanismi della progettazione a oggetti **astrazione**, **classi astratte**, **ereditarietà**, **interfacce** e **composizione** (insieme alle altre forme di associazione), descritti di seguito.

Attraverso l'**astrazione** e l'**incapsulamento**, ogni entità espone unicamente la propria interfaccia pubblica necessaria, mantenendo privato e nascosto il proprio stato interno (come avviene, ad esempio, per le proprietà della classe `Oggetto`). Per modellare i concetti più generali si è fatto ricorso alle **classi astratte** e all'**ereditarietà**. L'astratta `Entity` accomuna le basi di tutti i personaggi e viene specializzata da `Personaggio` e `Fantoccio`; similmente, la classe `Enigma` centralizza gli attributi condivisi ma delega alle sue tre sottoclassi l'implementazione specifica del metodo di verifica, applicando di fatto il **polimorfismo**.

L'architettura è ulteriormente disaccoppiata dall'uso delle **interfacce**, come `GiveObject`, che definiscono contratti di comportamento (realizzati concretamente da classi come `Spada`) svincolando i dettagli implementativi.

Infine, le **relazioni tra le classi** sono state modellate con diversi gradi di intensità per riflettere le reali interazioni del gioco. Troviamo:
- legami forti di **composizione**, utilizzati quando un elemento dipende vitalmente da un altro e ne condivide il ciclo di vita (come il `Giocatore` che genera e possiede il proprio `Inventario`).
- Si passa poi all'**aggregazione**, utile per raggruppare elementi che mantengono una loro esistenza autonoma (come gli oggetti che vengono semplicemente raccolti o eliminati momentaneamente). 
- Connessioni più deboli e momentanee, definite di **dipendenza** o **associazione**.
  Queste ultime servono per **scambi temporanei di dati che non richiedono un legame fisso**. Un chiaro esempio è la risoluzione delle sfide: la classe astratta `Enigma` produce il risultato limitandosi a esporre metodi come `verifica(String risposta)`, `getId()` e `getReward()`, ma non conosce né tiene riferimenti a classi come `GameStatistics` o all'`Inventario`. A sua volta, `GameStatistics` consuma il risultato per calcolare il punteggio tramite il metodo `enigmaRisolto(Enigma)`, non mantiene un campo persistente per l'enigma, ma lo riceve solo come parametro al volo, ne estrae i dati utili (come la ricompensa o il timer) e lo scarta. In modo del tutto simile, l'`Inventario<T Oggetto extends>` incamera la ricompensa tramite il metodo `aggiungiOggetto(T oggetto)`, invocato passandogli l'oggetto transitoriamente.
  
  Il ruolo di far dialogare queste componenti è affidato a classi orchestratrici come `CostruttoreScene` e `FlussiNarrativi`: sono loro a fungere da mediatori creando l'enigma, chiamando il metodo `verifica()`, funzione astratta che verifica la correttezza della soluzione, adattato in base alla tipologia d'enigma (inserimento, testuale, scelta multipla) e, in caso di esito positivo, invocando le funzioni opportune del Controller (come `engine.getStatistics().enigmaRisolto(enigma)` e `inventario.aggiungiOggetto(...)`).
## Specifica algebrica
La specifica algebrica implementata è quella della struttura dati **Insieme**. Abbiamo utilizzato questa struttura dati per gestire **l'inventario del giocatore**.

Il tipo parametro _tipoelem_ è istanziato con **Oggetto (`Inventario<T extends Oggetto>`)**. L'uguaglianza $x=y$ su _tipoelem_ è definita per nome (`getNomeOggetto()` confrontato con `equalsIgnoreCase`), coerentemente con `cercaOggetto`: due oggetti con lo stesso nome sono lo stesso elemento.
#### Specifica sintattica

- **Tipi:** insieme, boolean, tipoelem

- **Operazioni:**

	• `creainsieme () -> insieme`

	• `insiemevuoto (insieme) -> boolean`

	• `appartiene (insieme, tipoelem) -> boolean`

	• `inserisci (insieme, tipoelem) -> insieme`

	• `cancella (insieme, tipoelem) -> insieme`

	• `uguale (insieme, insieme) -> boolean`
#### Specifica semantica

**Dichiarazione variabili da usare:**

- `A, A'`: **insieme**
- `x,y`: **tipoelem**  
- `b`: **boolean**

**Tabella delle osservazioni**:

| **Osservazione**       | **Costruttore di A': creainsieme ()** | **Costruttore di A': inserisci (A, x)**                    |
| ---------------------- | ------------------------------------- | ---------------------------------------------------------- |
| **insiemevuoto (A')**  | True                                  | False                                                      |
| **appartiene (A', y)** | False                                 | If x = y then true<br><br>else appartiene (A, y)           |
| **cancella (A', y)**   | Error                                 | If x = y then A<br><br>else inserisci (cancella (A, y), x) |

**Regole di valutazione:**

- `insiemevuoto (creainsieme ()) = true`

- `insiemevuoto (inserisci (A, x)) = false`

- `appartiene (creainsieme (), y) = false`

- `appartiene (inserisci (A, x), y) = If x = y then true else appartiene (A, y)`

- `cancella (inserisci (A, x), y) = If x = y then A else inserisci (cancella (A, y), x)`

- `inserisci (inserisci (A, x), y) = If x = y then inserisci (A, x) else inserisci (inserisci (A, y), x)`

L'ultimo assioma esprime le due proprietà caratteristiche dell'insieme: l'inserimento di un elemento già presente lascia l'insieme **invariato e l'ordine di inserimento è irrilevante**.

**Specifica di restrizione**

`cancella (creainsieme (), y) = error`

#### *Implementazione nuovo operatore binario: uguale (insieme, insieme) -> boolean*

*A, B: insieme;   b: boolean;   x, y: tipoelem*

| **uguale (B', A')**                     | **Costruttore di A': creainsieme ()** | **Costruttore di A': inserisci (A, x)**                                                                                                                  |
| --------------------------------------- | ------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Costruttore di B': creainsieme ()**   | True                                  | False                                                                                                                                                    |
| **Costruttore di B': inserisci (B, y)** | False                                 | If x = y then uguale (B, A)<br><br>else if appartiene (A, y) and appartiene (B, x) <br>then uguale (cancella (B, x), cancella (A, y)) <br><br>else false |

**Regole di valutazione:**

- `uguale (creainsieme (), creainsieme ()) = true`
  
- `uguale (creainsieme (), inserisci (A, x)) = false`
  
- `uguale (inserisci (B, y), creainsieme ()) = false`
  
- `uguale (inserisci (B, y), inserisci (A, x)) = If x = y then uguale (B, A) else if appartiene (A, y) and appartiene (B, x) then uguale (cancella (B, x), cancella (A, y)) else false`

Poiché l'insieme non è ordinato, nel caso in cui i due elementi confrontati siano diversi non è corretto concludere immediatamente false: si verifica tramite *appartiene* che ciascuno dei due sia **presente nell'altro** insieme e si ricorre sugli insiemi privati di tali elementi.

#### *Implementazione nuovo operatore binario: inclusione (insieme, insieme) -> boolean*

*A, B: insieme;   b: boolean;   x, y: tipoelem*

| **inclusione (B', A')**                 | **Costruttore di A': creainsieme ()** | **Costruttore di A': inserisci (A, x)**                                                        |
| --------------------------------------- | ------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **Costruttore di B': creainsieme ()**   | True                                  | True                                                                                           |
| **Costruttore di B': inserisci (B, y)** | False                                 | If appartiene (inserisci (A, x), y)<br>then inclusione (B, inserisci (A, x))<br><br>else false |

**Regole di valutazione:**

• inclusione (creainsieme (), creainsieme ()) = true

• inclusione (creainsieme (), inserisci (A, x)) = true

• inclusione (inserisci (B, y), creainsieme ()) = false

• inclusione (inserisci (B, y), inserisci (A, x)) = If appartiene (inserisci   (A, x), y) then inclusione (B, inserisci (A, x)) else false

si verifica che ogni singolo elemento estratto dal primo insieme appartenga al secondo. Non appena si incontra un elemento mancante, la valutazione restituisce false; in caso contrario, prosegue fino allo svuotamento del primo insieme, confermando l'inclusione.

## Dettagli implementativi

### Programmazione generica

Le **Generics** nel progetto vengono utilizzate per estendere le **liste e le mappe** nel gioco così da non far prendere qualsiasi tipo di dato dalle `HashMap` ma per definire esattamente che tipo di dato deve contenere la **collezione di dati** e senza fare il **casting** dal tipo oggetto al tipo corretto.
Un esempio lo si trova nella **gestione della mappa e delle uscite** nel file `Stanza.java`:

``` JAVA
private final Map<String, Stanza> uscite;
// ...
public void impostaUscita(String direzione, Stanza stanza) {
    uscite.put(direzione.toUpperCase(),stanza);
}
```

Qui l'**uscita** da una stanza viene salvata in questa `HashMap` dove la chiave è la stringa della direzione (che nel codice viene definito che può essere uno dei **punti cardinali**) e l'oggetto è la `stanza` di destinazione.

Una **Generics nuova**, creata direttamente per il gioco è stata ideata nel package `Giocatore` per la gestione del suo `Inventario`. Se si analizza il codice della classe `Inventario` si nota come la sintassi `<T extends Oggetto>` definisce la sua regola di accettare solamente le istanze della classe `Oggetto` e le sue sottoclassi annesse (`Spada`).
Con questa implementazione evitiamo di dover controllare l'inserimento di tipi non validi quando vengono usate le funzioni di aggiunta di un oggetto all'inventario e di ricerca di quest'ultimo:

```JAVA
public class Inventario<T extends Oggetto> {

    // La lista non cambia, cambiano i contenuti all'interno
    private final List<T> listaOggetti; 

    public Inventario() {
        this.listaOggetti = new ArrayList<>();
    }

    public void aggiungiOggetto(T oggetto) {
        if (cercaOggetto(oggetto.getNomeOggetto()) == null) {
            listaOggetti.add(oggetto);
        }
    }

    public void rimuoviOggetto(T oggetto) {
        listaOggetti.remove(oggetto);
    }
       
    public T cercaOggetto(String nome) {
        return listaOggetti.stream()
                .filter(obj -> obj.getNomeOggetto().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    public List<T> getListaOggetti() {
        return listaOggetti;
    }
}
```

Alla luca di questa implementazione se torniamo ad analizzare `Giocatore` e specificamente in questa sezione di codice:

```JAVA
import aeg.giocomap.Model.Oggetti.Oggetto;
public class Giocatore {
    private final String nome_lore;
    private String nome_player;
    private Inventario<Oggetto> inventario;
    
    //...
    
    public Inventario<Oggetto> getInventario(){
        // creamo l'inventario solo alla prima chiamata
        if(this.inventario==null) this.inventario=new Inventario<>();
        
        // se non esiste da questo inventario perché non ha ancora quello esistente
        return this.inventario;
    }
}
```

si nota come l'istanza di inventario venga da subito specificata che conterrà un oggetto di tipo `Oggetto` (si precisa che c'è una grande differenza tra `Oggetto` e `Object`, anche se Java è basato sulla classe `Object`, quest'ultima non è `Oggetto`, poiché quest'ultima gestisce gli **oggetti del gioco**).
Così la funzione `Inventario<Oggetto> getInventario()` sia vincolata all'uso di quel tipo di dato.

Oltre all'implementazione di classi generiche create da noi, il progetto fa un uso nativo della programmazione generica di Java attraverso il *Java Collections* per le **liste**: `List<String> alberoDialoghi` nella classe `Personaggio` e `List<ClientHandler> clientConnessi` in `GameServer`.
### File

Nel progetto usiamo **due approcci differenti** per la lettura dai **file**.
I file usati generalmente vengono gestiti con `getResourceAsStream` per estrarre la **posizione fisica** di quel file, questa metodologia viene eseguita per i file **JSON**, i quali contengono i **dialoghi** e le frasi a schermo del gioco, i file `.wav` per la **musica** letta tramite l'apposita classe e libreria e per le **immagini** `.png` del gioco.
Mentre per il **catalogo degli oggetti** usiamo il `FileReader("src/main/resources/oggetti/oggetti.txt")`, che a differenza del metodo precedente legge solo **flussi di carattere** e non immagini e suoni e all'interno della classe `ModelTXTOggetti.java`, il costruttore utilizza un percorso relativo, il `FileReader` dice al sistema operativo di iniziare a cercare a partire dalla cartella da cui è stato avviato il programma (questo ovviamente in base a chi gioca al gioco può essere diverso come percorso, ma partendo da `src`, che è unico per tutti, non crea problemi).

Siamo consci che per un ottimo videogioco *"pubblicabile"* era buona prassi usare **solamente una delle due implementazioni** e tra le due implementazioni la migliore è quella usata per i JSON, ma abbiamo preferito mostrare anche la seconda metodologia per mostrare a livello di progetto universitario la padronanza di entrambe.

#### JSON
La gestione dei file viene pensata principalmente per i file JSON, centralizzati nella classe `JsonLoader`:

```JAVA
import com.google.gson.*;
import java.io.*;

//...

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
    
	// ...

```

In questo metodo della classe `JsonLoader` andiamo a caricare il file JSON specificato dal `percorso`, così da poterlo utilizzare (come vedremo dopo). Il JSON viene aperto tramite `InputStream` e viene letto tramite `BufferedReader` con specifica **UTF-8** per poter prendere gli accenti nelle `TextArea` dei dialoghi, come mostrato nella foto seguente:

![[utf-8_mostrato.png|506]]

Il metodo restituisce `null`, così da non dover far bloccare il gioco in caso di dialogo mancante, ma proseguirebbe senza quella parte di dialogo. Ovviamente nella **console** durante i test di gioco veniva mostrato questo messaggio per verificarne l'esattezza.

Come anticipato prima, questo metodo viene richiamato in `GameEngine` dove i tre file JSON dei dialoghi vengono caricati inizialmente e tenuti in memoria per tutta la partita così da **non doverli rileggere** ogni volta che servivano:
```JAVA
this.dbWallOfText = JsonLoader.caricaJson("/dialoghi/walloftext.json");
this.dbStoria = JsonLoader.caricaJson("/dialoghi/dialoghi_storia.json");
this.dbHint = JsonLoader.caricaJson("/dialoghi/dialoghi_hint.json");
```

I file sono tre, gestiti tutti da **chiave - valore** nel JSON così da richiamare ad ogni scena la chiave di quella frase e contengono rispettivamente:
- dialoghi lunghi per la narrazione (`walloftext.json`)
- dialoghi della trama principale, tra cui enigmi e discorsi dei personaggi (`dialoghi_storia.json`):
```JSON
...
  
"Marien": {
	"presentazione_enigma": "Viandante, vorresti risolvere tu, come tutti i qui presenti, il problema che io stessa ho ideato.\nDovete sapere tutti che fino ad ora nessuno è mai riuscito a risolverlo e io sono una grande amante delle sfide che richiedono cervello.\nNemmeno il mio promesso sposo Jack è mai riuscito a risolverlo. A tal punto, non diteglielo quando arriverà qui in stanza o andrà su tutte le furie.",

...

```
- dialoghi dei consigli alle soluzioni degli enigmi, soluzioni trovabili consultando zone e NPC del gioco (`dialoghi_hint.json`)
#### TXT
Una gestione diversa riguarda invece il catalogo degli oggetti ottenibili, definito nel file di testo `oggetti.txt` e caricato dalla classe `ModelTXTOggetti`. A differenza dei JSON qui usiamo un formato a righe ideato appositamente per il progetto, così da comprendere i 10 oggetti da inizio gioco e assegnarli nell'inventario quando saranno effettivamente ottenuti, con i campi separati dal carattere `;`:

```JAVA
private void loadOggettiDaCatologo(){
        catalogoOggetti = new HashMap<>();
        
        try{
            //Leggiamo il file
            BufferedReader reader=new BufferedReader(new FileReader("src/main/resources/oggetti/oggetti.txt"));
            
            // Fin tanto che la linea letta da file non è nulla...
            while ((linea=reader.readLine())!=null) {
                //... e fin tanto che non è vuota..
                if (linea.trim().isEmpty()){
                    continue;
                }
                /* 
                ... allora divido la linea in diverse parti dove trovo
                il punto e virgola (deciso tra noi come separatore nel file)
                e poi salvo tutto in 3 parti, in modo da istanziare
                */
                String[] parti=linea.split(";",3);
                
                if (parti.length==3){
                    try {
                        // Rimuovo gli spazi bianchi tramite trim()
                        currentId = Integer.parseInt(parti[0].trim());
                        currentNome = parti[1].trim();
                        currentDesc = parti[2].trim();
                        inserisciOggetto(currentId, currentNome, currentDesc);
                    } catch (NumberFormatException e) {
                        System.err.println("DEBUG: Impossibile convertire ID in numero sulla linea n."+linea);
                    }
                } else {
                    System.err.println("DEBUG: Formato della riga non valido, attese 3 parti:"+ linea);
                }
            }
                reader.close();
                System.out.println("DEBUG: Catalogo oggetti: "+ catalogoOggetti.size()+" presenti all'interno");
        }
        catch(IOException e){
            System.out.println("DEBUG: Errore imprevisto: " + e);
        }
    }
```

Ogni riga del file rappresenta un oggetto nel formato `id;nome;descrizione` (es. `1;Tessuto;Il prezioso tessuto da consegnare al Re di Shambhala.`); il metodo `split(";", 3)` divide la riga in massimo tre parti, così che eventuali `;` presenti nella descrizione non spezzino ulteriormente la stringa. Così da visualizzare nell'inventario queste caratteristiche e caricare nell'inventario, in base al momento nella storia, l'oggetto con **id**: $n$.

![[Inventario.png]]

#### PNG
Le **immagini** sono basati sulla libreria standard `javax.imageio.ImageIO`. Il caricamento è **dinamico**, dove il nome del file non è scritto nel codice ma viene costruito a runtime a partire da un **dato di gioco**. Lo si vede bene in `InventarioPanel`, dove lo *sprite* di ogni oggetto viene recuperato usando il nome dell'oggetto stesso come nome del file:

```java
String nomeFileImmagine = obj.getNomeOggetto() + ".png";
InputStream is = getClass().getResourceAsStream("/sprites/Oggetti/" + nomeFileImmagine);

if (is != null) {
    BufferedImage img = ImageIO.read(is);
    Image scaledImage = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
    iconLabel.setIcon(new ImageIcon(scaledImage));
} else {
    iconLabel.setText(obj.getNomeOggetto().substring(0,1));
}
```

Questo approccio evita di dover mappare manualmente ogni oggetto al proprio file immagine: basta rispettare la convenzione **"nome oggetto = nome file"** nella cartella delle risorse.
Comunque in caso non venga trovato, nel ramo `else` viene gestito un metodo per far apparire comunque l'oggetto, tramite la sua lettera iniziale nella **griglia visiva dell'inventario**. Stessa logica viene gestita per gli scenari, solo che se non viene trovato viene impostato a `null`, mostrando schermo nero senza far bloccare il gioco.
#### WAV
La musica è gestita dalla classe `MusicPlayer`, che si appoggia alla libreria `javax.sound.sampled` per riprodurre file `.wav` in loop continuo:

```JAVA
public void playMusic(int i){
    try{
	    //Seleziono la traccia dall'array (usato come lettore di canzoni)
        BufferedInputStream bs = new BufferedInputStream(getClass().getResourceAsStream(tracceAudio[i])); 
	    
	    // Traduce il wav nel formato leggibile java
        AudioInputStream as = AudioSystem.getAudioInputStream(bs); 
        clip = AudioSystem.getClip();
        clip.open(as); // Inseriamo la traccia loop dentro il lettore java
        clip.loop(Clip.LOOP_CONTINUOUSLY);
	    clip.start();
    }
    catch (IOException | LineUnavailableException | UnsupportedAudioFileException e){
        System.out.println("DEBUG: Traccia numero "+i+" non trovata "+ e.getMessage());
    }
}
```

Come possiamo notare, viene detto che si usa un array per gestire il *"nastro musicale"* del player delle canzoni, di fatto i percorsi delle tracce sono definiti in un **array di stringhe nel costruttore** della classe, così da poter essere richiamati altrove nel codice tramite **costanti** (`MusicPlayer.TITLE_SCREEN_MUSIC`, `MusicPlayer.END_TITLE_MUSIC`) invece che con indici numerici scritti a mano, così da poter ampliare il `MusicPlayer`, in caso si vogliano aggiungere altre tracce.

```JAVA
public static final int TITLE_SCREEN_MUSIC = 0;
    public static final int END_TITLE_MUSIC = 1;
    
    // Costruttore con tutte le tracce audio
    public MusicPlayer(){
        tracceAudio = new String[3];
        
        tracceAudio[0] = "/musiche/GreenlandsTitleScreen.wav"; //Ttile screen
        tracceAudio[1] = "/musiche/DEAFKEVEndTitle.wav"; // End Screen
        //tracceAudio[2] = "traccasuccessiva";
    }
```

### Database (JDBC)

L'utilizzo della JDBC nel progetto è immagazzinato nel package `giocomap.Model.Storage.ModelDB`, così la logica del DB viene separata dal resto della logica di gioco.

La gestione della JDBC in `ModelDB.java` segue le classiche fasi del **ciclo di vita di una connessione a un database**, gestendo tutto in modo strutturato.
Per eseguire le sue operazioni nel suo opportuno file abbiamo importato a monte le seguenti librerie:

```JAVA
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
```
 
 Essi sono le librerie per utilizzare il linguaggio di **interrogazione SQL**.
 Ma per far funzionare il DB di H2 che lavora in maniera locale, abbiamo dovuto comunque inserire nelle **dipendenze** di maven (`pom.xml`) la struttura di h2:
 
 ```xml
 <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.4.200</version>
        </dependency>
        
        ...
        
 ```
 
 La connessione al DB avviene tramite il comando `DriverManager.getConnection("jdbc:h2:./saves/DB")` all'interno dell'apposito metodo privato:

```JAVA
// Funzione che connette al Database e crea/aggiorna le tabelle
private void connettiDatabase(){
    try{
        conn = DriverManager.getConnection("jdbc:h2:./saves/DB");
        System.out.println("TEST: Connessione al DB avvenuta");

        // saves: la stanza e' il NOME della scena (testo), non un numero
        eseguiUpdate("CREATE TABLE IF NOT EXISTS saves ("
                    + "id INT PRIMARY KEY,"
                    + "stanza_attuale VARCHAR(100),"
                    + "enigma_attuale INT)");

        // migrazione per i vecchi DB dove stanza_attuale era INT:
        // converto la colonna in testo senza perdere l'eventuale riga
        try {
            eseguiUpdate("ALTER TABLE saves ALTER COLUMN stanza_attuale VARCHAR(100)");
        } catch (SQLException e) {
            // colonna gia' del tipo giusto: ignoro
        }

        // colonne per lo stato di avanzamento (aggiunte se mancano)
        try {
            eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS stato_city INT DEFAULT 0");
            eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS possiede_mappa BOOLEAN DEFAULT FALSE");
            eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS enigmi_risolti VARCHAR(1000)");
            eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS inventario VARCHAR(1000)");
            eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS primo_accesso_palazzo BOOLEAN DEFAULT TRUE");
            eseguiUpdate("ALTER TABLE saves ADD COLUMN IF NOT EXISTS carica_spada INT DEFAULT 0");
        } catch (SQLException e) {
            System.err.println("Errore aggiornamento colonne saves: " + e.getMessage());
        }

        // records: classifica dei punteggi
        eseguiUpdate("CREATE TABLE IF NOT EXISTS records ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "nome VARCHAR(50),"
                    + "punteggio INT,"
                    + "data TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        // difesa contro vecchi DB creati senza la colonna nome
        try {
            eseguiUpdate("ALTER TABLE records ADD COLUMN IF NOT EXISTS nome VARCHAR(50)");
        } catch (SQLException e) {
            // colonna gia' presente: ignoro
        }
    }
    catch (SQLException e){
        System.err.println("Errore di connessione al DB: "+e.getMessage());
    }
}
```

La connessione ad **H2** avviene subito appena viene chiamata la classe nel progetto poiché la funzione precedente, `connettiDatabase()`, viene chiamata dentro al costruttore di `ModelDB`, creando immediatamente così la cartella locale del giocatore con le tabelle relative ai suoi **salvataggi** e **records**. Questa creazione avviene nel momento in cui si clicca su **Nuova Partita** nello schermo iniziale del gioco:

![[NuovaPartitaTitleScreen.png]]

Per interagire con i dati, il codice sfrutta prevalentemente le istanze di `PreparedStatement` (es. nei metodi `salvaPartita` e `salvaSeNecessario`). Questa scelta previene le **SQL injection** e semplifica il caricamento delle variabili Java all'interno delle query (tramite set di metodi come `pstm.setString()`).

Come abbiamo fatto per i file, anche qui gli **errori** vengono gestiti tramite `try-catch`, con le `SQLException` così da visualizzare in fase di debug gli errori dati e risolverli ma specialmente per non far fermare il gioco in caso di errori.

Ogni metodo è ferreo sulla **chiusura dei DB** per deallocare le risorse, specialmente se si chiude il gioco dalla finestra e non dai comandi di uscita implementati, così da non lasciare H2 sempre in ascolto, tra cui i metodi `loadGame()` e `salvaPartita()`, utilizzano addirittura la clausola `finally` in modo tale da chiudere sempre il DB anche in caso di errore: 

```JAVA

//...

catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
        finally {
            if (rs != null) {
                try { rs.close(); }
                catch (SQLException ex) { System.err.println("Errore chiusura ResultSet: " + ex.getMessage()); }
            }
            if (pstm != null) {
                try { pstm.close(); }
                catch (SQLException ex) { System.err.println("Errore chiusura Statement: " + ex.getMessage()); }
            }
        }
```

Questa chiusura avviene tramite il metodo `chiudiConnessione()`, il quale effettua un controllo che se la **Connection** esiste ed è aperta allora procede alla sua corretta terminazione:

```JAVA
// Chiudo la connessione ad h2 se aperta
    public void chiudiConnessione(){
        try{
            if(conn != null && !conn.isClosed()){
                conn.close();
                System.out.println("DEBUG: DB chiuso");    
            } 
        }
        catch (SQLException e){
            System.out.println("DEBUG: Errore durante la chiusura del DB "+e.getMessage());
        }
    }
```

L'uso principale del DB è stato quello di creare i dati di un giocatore in caso iniziasse una nuova partita, altrimenti se clicca nel titlescreen **Carica Partita**, nel momento in cui lui ha salvato la partita regolarmente durante una sessione di gioco:

![[SalvaedEsci.png]]

può rientrare nella partita nel punto in cui era rimasto, poiché nelle colonne del DB vengono **salvate le seguenti colonne**, importanti per tenere traccia del punto in cui vi si è fermati nella storia: `stanza_attuale, stato_city, possiede_mappa, enigmi_risolti, inventario, primo_accesso_palazzo, carica_spada`, tutte controllate rigorosamente dalla funzione `loadGame()` presente sempre in `ModelDB` al momento dell'avvio del gioco (da `avvioGioco()` di `GameEngine`) tramite il tasto predisposto.

### Lamba Expression (compresi stream e pipeline)

Le **Lambda Expression** vengono usate nel progetto principalmente per **sostituire le classi anonime** nell'implementazione delle *interfacce funzionali* (interfacce con un solo metodo astratto), sia quelle native di Java (`Runnable`, `Consumer<T>`) sia negli `ActionListener`, rendendo il codice più compatto quando si devono passare **comportamenti** da un metodo all'altro.

L'unico utilizzo della **Stream** a **pipeline** si trova, come sempre, proprio nella classe generica `Inventario<T>` già vista nella sezione sulle *Generics*, nel metodo `cercaOggetto()`:

```JAVA
//...
private final List<T> listaOggetti;
//...

public T cercaOggetto(String nome) {
    return listaOggetti.stream()
            .filter(obj -> obj.getNomeOggetto().equalsIgnoreCase(nome))
            .findFirst()
            .orElse(null);
}
```

Qui si nota una vera e propria **pipeline**: la `List<T>` viene trasformata in uno `Stream<T>` tramite `.stream()`, il quale viene **filtrato** con una _lambda_ (`obj -> obj.getNomeOggetto().equalsIgnoreCase(nome)`) che scarta tutti gli oggetti il cui nome non corrisponde, per poi richiamare `.findFirst`, metodo usato grazie all'import della libreria `util.stream.Stream`, che restituisce un `Optional<T>` col primo elemento rimasto (se esiste), gestito subito con `.orElse(null)` per non dover gestire esplicitamente l'`Optional` in tutto il resto del codice. Questo metodo viene poi richiamato ovunque nel gioco, serve a controllare se un oggetto è già in inventario (es. `aggiungiOggetto`) o per recuperarlo prima di rimuoverlo, come accade nella scena con Fox.
#### Runnable e dialoghi a catena

La maggior parte delle **lambda** del progetto, però, non riguarda gli `Stream`, ma l'uso di `Runnable` come **callback**, per concatenare più dialoghi ed eventi di gioco uno dopo l'altro, usata come se fosse una **pipeline di eventi**: ogni schermata di dialogo, una volta chiusa dal giocatore, esegue il `Runnable` che le è stato passato, il quale a sua volta può aprirne un'altra con un nuovo `Runnable` agganciato, e così via con tutte le altri che si susseguono. 
Il cuore di questo meccanismo è in `GameEngine.mostraDialogoCallback`:

```JAVA
public void mostraDialogoCallback(GameScreen scenaSfondo, String idScenaSfondo, String nome, String battuta, ImageIcon sprite, Runnable callback) {
    if (isDialogoActive) return;
    setDialogueActive(true);
    DialogueScreen ds = new DialogueScreen(scenaSfondo, () -> {
        setDialogueActive(false);
        getSceneManager().mostraScena(idScenaSfondo);
        if (callback != null) callback.run();
    });
    ds.aggiornaSchermata(nome, battuta, sprite);
    getSceneManager().registraScena("DIALOGO_CORRENTE", ds);
    getSceneManager().mostraScena("DIALOGO_CORRENTE");
}
```

Qui la **lambda** passata al costruttore di `DialogueScreen` viene eseguita solo alla **chiusura** del dialogo (quando il giocatore clicca "*avanti*" sull'ultima battuta) e al suo interno richiama `callback.run()`, cioè il `Runnable` ricevuto come parametro, in questo modo il metodo non deve sapere **cosa** succederà dopo il dialogo, riceve semplicemente il "*cosa fare dopo*" già pronto sotto forma di espressione lambda, così che chi chiama il metodo possa decidere se non fare nulla, aprirne un altro o avviare un enigma.

Un esempio concreto di questa catena si trova nella gestione dell'interazione col **Contadino Green** in `CostruttoreScene.costruisciPiazza`, dove più `Runnable` vengono incastrati uno dentro l'altro:

```JAVA
zonePiazza.put(CostantiHitbox.PIAZZA_CONTADINO, () -> {
    if (stato.getStato() == StatoStoria.MISSIONE_COOPER_ACCETTATA) {
        contadino.setDialoghi(Arrays.asList(contadinoDb.get("saluto").getAsString()));
        Runnable loopContadino = new Runnable() {
            @Override
            public void run() {
                String input = JOptionPane.showInputDialog(engine.getFrame(), "Cosa chiedi al Contadino Green?");
                if (input == null) return;
                if (Parser.contieneRadiceParola(input, "carot*")) {
                    contadino.setDialoghi(Arrays.asList(contadinoDb.get("richiesta").getAsString()));
                    engine.mostraDialogoNPCCallback(CostruttoreScene.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, () -> {
                        // ...altro Runnable annidato per il loop di accettazione...
                    });
                } else {
                    contadino.setDialoghi(Arrays.asList(contadinoDb.get("incomprensione").getAsString()));
                    engine.mostraDialogoNPCCallback(CostruttoreScene.this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, this);
                }
            }
        };
        engine.mostraDialogoNPCCallback(this.piazzaCentrale, CostantiMappa.PIAZZA_CENTRALE, contadino, spriteGreen, loopContadino);
    }
    // ...
});
```

Qui il valore inserito nella `Map<double[], Runnable> zonePiazza` (la stessa mappa vista nella sezione sulle *Generics* per la gestione delle **zone cliccabili**) è direttamente una *lambda*, richiamata quando il giocatore clicca sull'omino del contadino.
#### Il limite delle Lambda

Non tutte le **interfacce funzionali** del progetto vengono implementate con le lambda expression. `loopContadino`, per esempio, **non può** essere scritto come `() -> { ... }`, perché al suo interno, nel ramo `else`, richiama sé stesso passando `this` ( `..., contadino, spriteGreen, this)`) per far **ripetere il dialogo** finché il giocatore non risponde correttamente. 
In una espressione lambda, `this` farebbe riferimento alla classe che la contiene (`CostruttoreScene`), non all'espressione stessa, quindi per un `Runnable` **ricorsivo** serve necessariamente una classe anonima (`new Runnable() { ... }`).

Allo stesso modo, in `GameEngine.impostaKeyBindingMappa()` usiamo una **classe anonima** e non una lambda per gestire la **scorciatoia da tastiera che apre/chiude la mappa**:

```JAVA
am.put("toggle_mappa", new AbstractAction() {
    @Override
    public void actionPerformed(ActionEvent e) {
        toggleMappa();
    }
});
```

In questo caso il motivo è diverso: `AbstractAction` è una **classe astratta**, non un'interfaccia funzionale (ha più metodi, alcuni già implementati), quindi Java non permette proprio di sostituirla con una lambda, che può implementare solo *interfacce* con **un unico metodo astratto**.
#### Consumer

Oltre a `Runnable`, il progetto usa in due punti l'interfaccia funzionale nativa `Consumer<T>` (`java.util.function.Consumer`), utile quando il "*comportamento*" da eseguire ha bisogno di **ricevere un dato** in ingresso invece di essere eseguito e basta. Lo si vede nel dialogo per l'enigma finale della principessa, `DialogoOrdinamentoVestiti`, dove ogni *bottone-vestito*, al click, non decide da solo cosa fare ma **avvisare**  la scelta a `CostruttoreScene`, ovvero colui che apre il pop up:

```JAVA
public DialogoOrdinamentoVestiti(JFrame owner, List<String> vestiti, Consumer<String> alClick) {
    // ...
    bottone.addActionListener(e -> alClick.accept(vestito));
    // ...
}
```

`CostruttoreScene`, passa la lambda che riceve il **nome del vestito scelto** e decide se è nella **posizione corretta**, aggiornando lo stato dell'enigma (di fatto alla prima posizione non corretta l'enigma si ferma):

```JAVA
DialogoOrdinamentoVestiti popup = new DialogoOrdinamentoVestiti(engine.getFrame(), enigmaFinale.getVestiti(), vestitoScelto -> {
    int posizione = posizioneCorrente[0];
    if (enigmaFinale.verificaPosizione(posizione, vestitoScelto)) {
        popupCorrente[0].posizionaVestito(posizione, vestitoScelto);
        popupCorrente[0].disabilitaVestito(vestitoScelto);
        posizioneCorrente[0]++;
        // ...
    }
    else { 
	    // Vestito fuori posto -> parte subito la scena dell'errore, l'enigma andrà 
	    // ripetuto popupCorrente[0].dispose(); stepPiumatoNarrazione.run();
    }
});
```

Questo disaccoppia completamente `DialogoOrdinamentoVestiti` (che sa solo disegnare i bottoni e notificare i click) dalla logica dell'enigma (che sa se la scelta è giusta o sbagliata), che resta interamente in `CostruttoreScene`. 

Da notare anche `posizioneCorrente` e `popupCorrente`, poiché in Java una *lambda* può usare solo variabili locali che dopo essere state assegnate una prima volta, non vengono più **riassegnate**. Questo normalmente impedirebbe di incrementare un contatore dentro una lambda, perché sarebbe un riassegnamento non consentito. 
Per aggirare il problema, `posizioneCorrente` e `popupCorrente` non sono variabili semplici ma **array di un solo elemento**, dichiarati poco prima della lambda:

```JAVA
final int[] posizioneCorrente = {0};
final DialogoOrdinamentoVestiti[] popupCorrente = new DialogoOrdinamentoVestiti[1];
```

La variabile che punta all'array (`posizioneCorrente`, `popupCorrente`) non cambia mai e quindi la lambda può catturarla, ed è il **contenuto** dell'array (`posizioneCorrente[0]`, `popupCorrente[0]`) che può essere modificato liberamente, sia dentro la lambda sia fuori (come alla riga `posizioneCorrente[0] = 0;` in `avviaPopupVestiti`). 
In questo modo più *lambda* annidate possono condividere e aggiornare nel tempo lo stesso valore, per questo nel gioco poi avviene questo:

![[AvviaPopupVestiti.png]]
### SWING

L'idea iniziale prevedeva tre meccanismi **Swing** distinti per rendere l'avventura grafico-testuale interattiva: un puntatore dinamico sulle zone sensibili, delle frecce direzionali per lo spostamento e un parser a **Regex** per l'input da tastiera. Tutti e tre sono stati effettivamente realizzate nel progetto.
#### Puntatore dinamico (hotspot)
La classe `CursorUtil` (package `Util`) espone il metodo `registraZone(JPanel panel, Map<double[], Runnable> zone)`, richiamato da ogni `GameScreen` per registrare le proprie **zone cliccabili**. Un `MouseMotionAdapter` intercetta `mouseMoved` e, se il cursore entra in una delle zone, cambia l'icona in `Cursor.HAND_CURSOR` (altrimenti resta `Cursor.DEFAULT_CURSOR`); un `MouseAdapter` separato gestisce `mouseClicked`, ricalcola la stessa zona ed esegue il `Runnable` associato:

```JAVA
panel.addMouseMotionListener(new MouseMotionAdapter() {
    @Override
    public void mouseMoved(MouseEvent e) {
        boolean sopraZona = false;
        for (double[] perc : zone.keySet()) {
            if (rettangoloZona(panel, perc).contains(e.getPoint())) {
                sopraZona = true;
                break;
            }
        }
        panel.setCursor(Cursor.getPredefinedCursor(
            sopraZona ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }
});
```

Le zone non sono coordinate assolute in pixel ma **percentuali** (`{xPerc, yPerc, wPerc, hPerc}`), ricalcolate rispetto all'area dell'immagine di sfondo: questo permette alle zone cliccabili di restare correttamente posizionate anche quando la finestra viene **ridimensionata**.
Le coordinate in fase di debug le prendavamo dall'**interfaccia** `CoordinateDebuggable` 
#### Marcatore di posizione sulla mappa
Quando si apre la mappa (overlay `MappaPanel`, tasto **M**), sopra l'immagine della mappa viene disegnato anche un piccolo **marcatore** (`Posizione.png`) che indica in quale stanza si trova attualmente il giocatore, caricato come `BufferedImage` e ridisegnato ad ogni `paintComponent`:

```JAVA
private BufferedImage immaginePosizione;
// ...
immaginePosizione = ImageIO.read(getClass().getResourceAsStream("/sprites/StrumentiGrafici/Posizione.png"));
```

La posizione del marcatore non viene calcolata dinamicamente, ma associata alla scena in cui si trovava il giocatore prima di aprire la mappa (`sceneManager.getScenaPrecedente()`) tramite uno `switch` che mappa ogni nome di scena a una coppia di coordinate percentuali sulla mappa, con la stessa logica di ridimensionamento percentuale già vista per `CursorUtil`:

```JAVA
String scena = sceneManager.getScenaPrecedente();
switch (scena) {
    case "PORTO":
        relX = 0.1920; relY = 0.2373;
        break;
    // ... una coppia di coordinate per ciascuna stanza
}
```

Va precisato che non si tratta di un `Cursor` del mouse personalizzato (nel progetto non viene mai usato `Cursor.createCustomCursor`), ma di uno **sprite fisso in overlay**: la mappa resta **puramente consultiva**, senza alcun click-to-travel, dato che `MappaPanel` non registra alcun `MouseListener` proprio; lo spostamento reale resta affidato solo alle frecce direzionali, che infatti vengono nascoste automaticamente da `SceneManager` quando la mappa è aperta.

![[PosizioneMappa.png]]
#### Frecce direzionali
`MainFrame` istanzia quattro `JButton` dedicati (`btnNord`, `btnSud`, `btnEst`, `btnOvest`), posizionati come overlay fisso sul `JLayeredPane` tramite `impostaFrecceLogica()`, e li espone tramite `setFrecceListener(ActionListener nord, sud, est, ovest)`. La logica di navigazione vera e propria, però, non risiede nella View ma nel Controller: è `NavigazioneMappa.impostaFrecceLogica()` a collegare ai quattro bottoni le rispettive callback, ciascuna delle quali invoca `eseguiCollegamento(CostantiMappa.NORD/SUD/EST/OVEST)` per determinare la stanza successiva.
#### Analisi testuale (Regex)
La classe `Parser` (package `Util`) usa `java.util.regex.Pattern`/`Matcher` nel metodo `contieneRadiceParola(String input, String pattern)`: costruisce dinamicamente un'espressione regolare inserendo `\s*` opzionale tra ogni lettera del pattern (per tollerare spazi extra digitati dal giocatore) e, se il pattern termina con `*`, aggiunge un suffisso libero `\w*` per accettare variazioni della parola (gestito principalmente per i plurali):

```JAVA
String regex = haWildcard
    ? "(?i).*\\b" + regexRadice.toString() + "\\w*\\b.*"
    : "(?i).*\\b" + regexRadice.toString() + "\\b.*";
```

Il flag **inline** `(?i)` rende il confronto **case-insensitive**. Questo **parser** viene richiamato ogni volta che il gioco chiede una risposta libera da tastiera tramite `JOptionPane.showInputDialog`, ad esempio in `CostruttoreScene.contieneRadiceParola(input, "carot*")` nel dialogo col Contadino Green, così da riconoscere "carota", "carote" o varianti con spazi/maiuscole senza dover elencare ogni possibile risposta.
#### Icona e titolo della finestra
`MainFrame.java` non è rimasto invariato rispetto al codice generato automaticamente dal Form Editor di **NetBeans**: il titolo era già impostato in `initComponents()` (`setTitle("Adventure Game MAP")`), ma l'**icona** della finestra (quella mostrata nella barra del titolo e nella taskbar) non veniva assegnata a un'immagine reale, dato che il placeholder generato automaticamente è `setIconImage(getIconImage())`, che di fatto non imposta nulla poiché in quel punto `getIconImage()` restituisce ancora `null`. Nel costruttore di `MainFrame` è stata quindi aggiunta manualmente l'assegnazione dell'icona, usando come immagine lo sprite del **Tessuto**, l'oggetto centrale della trama:

```JAVA
URL iconURL = getClass().getResource("/sprites/Oggetti/Tessuto.png");
if (iconURL != null) {
    ImageIcon icona = new ImageIcon(iconURL);
    this.setIconImage(icona.getImage());
}
```
#### Cambio di scena e overlay
`MainFrame.mostraPannello(JComponent newPanel)` non usa un `CardLayout`, ma sostituisce direttamente il contenuto del `contentPane`, impostato a `BorderLayout`:

```JAVA
public void mostraPannello(JComponent newPanel){
    this.getContentPane().removeAll();
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(newPanel, BorderLayout.CENTER);
    this.revalidate();
    this.repaint();
}
```

Gli elementi che devono restare **sempre visibili** a prescindere dalla scena (bottone chat, frecce direzionali) non vivono nel `contentPane` appena svuotato, ma nel `JLayeredPane` del frame (`getLayeredPane()`), tutti sul livello `JLayeredPane.POPUP_LAYER`: in questo modo sopravvivono a ogni `removeAll()` e restano sempre sopra alla scena corrente. Il frame usa anche un `glassPane` dedicato per un effetto di transizione ("lampo" a schermo intero con dissolvenza tramite `Timer`), completamente separato dal `JLayeredPane`.

La gestione di **quale** pannello mostrare è affidata a `SceneManager`, che mantiene una cache (`Map<String, JComponent> sceneCache`) popolata una sola volta all'avvio tramite `registraScena(...)`: aprire la mappa o l'inventario (`apriMappa()`, `apriInventario()`) non crea una nuova istanza del pannello, ma pesca quella già esistente dalla cache e la passa a `mostraPannello(...)`, salvando la scena precedente per poterla ripristinare alla chiusura. Fa eccezione la `ChatPanel`, creata alla prima apertura (sia in `GameNetwork.toggleChat()` come host, sia in `connettiComeClient(...)` come client) e poi riutilizzata allo stesso modo per le aperture successive, dopo che viene mostrato l'**IP address da inserire**.
Gestiamo tramite il **LayeredPane** la disposizione di una griglia a livelli, così da inserire le immagini e testi in questo ordine:
$Sfondo<Immagine\ personaggio<Testo\ di \ dialogo$
In merito a ciò lo analizziamo nella seguente descrizione:
#### Composizione a livelli
La schermata di dialogo estende direttamente `JLayeredPane` e sovrappone tre componenti su livelli semanticamente coerenti con Swing: lo sfondo della scena sottostante, lo sprite del personaggio sopra di esso, e il box di testo del dialogo in primo piano:

```JAVA
this.add(scena_stanza, JLayeredPane.DEFAULT_LAYER);  // sfondo (GameScreen)
this.add(panelSpritePG, JLayeredPane.PALETTE_LAYER);  // sprite del personaggio
this.add(boxDialogo, JLayeredPane.MODAL_LAYER);       // box di testo
```
#### Ridimensionamento responsive
La finestra parte non massimizzata ma resta **ridimensionabile** fino a una dimensione minima (`setMinimumSize(1024, 768)`). Un `ComponentAdapter` registrato sul `rootPane` intercetta ogni ridimensionamento e riposiziona gli elementi fissi:

```JAVA
getRootPane().addComponentListener(new ComponentAdapter() {
    @Override
    public void componentResized(ComponentEvent e) {
        riposizionaBottoneChat();
        riposizionaFrecce();
    }
});
```

Le frecce direzionali vengono ridimensionate in **percentuale** rispetto al lato più corto della finestra (`Math.min(larghezza, altezza) * 8 / 100`) e ridisegnate a partire dalle immagini originali salvate in memoria (non da versioni già scalate), per evitare che ridimensionamenti ripetuti degradino la qualità dell'icona.

Questa **scalatura percentuale**, sia delle frecce che degli sfondi e delle zone cliccabili, è stata testata manualmente anche su **sistemi operativi diversi** tra i membri del gruppo (Emanuele usa Linux), verificando il ridimensionamento migliore da usare come media generale per i vari SO (purtroppo non è perfetta per tutti, ma è un ottimo compromesso architetturale scelto assieme tra i membri del gruppo).
#### Scalatura delle immagini
Lo sfondo di ogni `GameScreen` viene disegnato manualmente sovrascrivendo `paintComponent`, con layout assoluto (`setLayout(null)`) per poter posizionare sopra di esso le zone cliccabili di `CursorUtil`:

```JAVA
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (immagine != null) {
        g.drawImage(immagine, 0, 0, getWidth(), getHeight(), this);
    }
}
```

L'immagine viene qui **stirata** per riempire tutto il pannello (senza mantenere le proporzioni originali), scelta coerente con le zone cliccabili percentuali già viste per `CursorUtil`. Dato che sfondo e zone si scalano allo stesso modo, restano sempre allineati. 
Lo sprite del personaggio in `DialogueScreen`, invece, viene disegnato con un `paintComponent` distinto che **mantiene l'aspect ratio** (fattore di scala minimo tra larghezza e altezza), per evitare che i personaggi appaiano deformati.
#### Key Bindings con `InputMap`/`ActionMap`
Le scorciatoie da tastiera globali del gioco (mappa, chat, inventario, ESC) non usano un `KeyListener`, ma sono registrate sul `rootPane` tramite `InputMap`/`ActionMap` con condizione `WHEN_IN_FOCUSED_WINDOW` (Snippet già mostrato nella sezione sul [[#Il limite delle Lambda|limite delle lambda]] per `impostaKeyBindingMappa()`). 
`mostraPannello` sostituisce di continuo il pannello centrale con componenti diversi (bottoni, `JTextArea`, ecc., ognuno potenzialmente titolare del focus), un `KeyListener` legato a un singolo componente smetterebbe di funzionare non appena il focus si sposta altrove o la scena cambia. Legando invece i tasti al `rootPane`, che non viene mai rimosso dalla finestra, le **scorciatoie restano attive indipendentemente da quale componente figlio abbia il focus** in quel momento.
#### `JOptionPane` per input e conferme
Oltre agli usi già visti per l'input testuale degli enigmi (gestito poi da `Parser`), `JOptionPane` viene usato nel progetto anche per le **conferme** di navigazione (`JOptionPane.showConfirmDialog(..., JOptionPane.YES_NO_OPTION, ...)` in `NavigazioneMappa`, per chiedere conferma prima di uno spostamento significativo) e per i **messaggi informativi/di errore** della chat multiplayer (es. "Sei già l'host!", "Nome già in uso!" in `GameNetwork`), oltre che per la richiesta del nome giocatore a fine partita in `GameStatistics.statistiche()`.
### Thread e programmazione concorrente

L'idea originale era di affiancare al calcolo del punteggio un **thread concorrente** che misurasse il tempo di risoluzione di un enigma. Nell'implementazione finale, però, `TimerEnigma` non avvia un thread dedicato: calcola il tempo trascorso in modo **sincrono**, salvando l'istante di partenza e confrontandolo con l'istante corrente:

```JAVA
public int getSecondi() {
    if (isAttivo()) {
        return (int) ((System.currentTimeMillis() - inizioMs) / 1000);
    }
    return secondiFinali;
}
```

L'unico `javax.swing.Timer` presente in `TimerEnigma` serve solo per un ticchettio periodico di debug (stampa in console), eseguito sull'**Event Dispatch Thread**, e non introduce concorrenza reale; `GameStatistics.calcolaPunti()` assegna poi il punteggio in base a delle fasce di secondi trascorsi, in modo anch'esso interamente sincrono.

La vera **programmazione concorrente** del progetto si trova invece nel modulo di rete (package `Network`), dove più thread lavorano realmente in parallelo per gestire la chat multiplayer. `GameServer` si avvia come `Runnable` su un thread dedicato (`new Thread(this).start()`) che resta in ascolto di nuove connessioni; per **ogni client** che si collega viene creato un `ClientHandler` eseguito a sua volta su un proprio thread (`new Thread(handler).start()`), così che i messaggi di più giocatori vengano letti in parallelo senza bloccarsi a vicenda. Lato client, `GameClient` avvia un `ThreadRicezione` dedicato all'ascolto asincrono dei messaggi in arrivo, mentre l'interfaccia grafica resta libera di rispondere agli input dell'utente. Poiché più thread accedono contemporaneamente alle stesse strutture condivise (l'elenco dei client connessi e dei nomi), `GameServer` protegge questi accessi rendendo `synchronized` i metodi che li leggono o modificano (`nomeDisponibile`, `aggiungiNome`, `rimuoviNome`, `broadcast`, `rimuoviClient`), evitando così race condition tra il thread di accettazione e i thread dei singoli `ClientHandler`.

#### Thread-safety tra rete e interfaccia grafica
Proprio perché il modulo di rete gira su thread propri, si pone il problema di farlo comunicare con l'interfaccia grafica senza violare le regole di **Swing**, che non è *thread-safe*: i componenti grafici vanno aggiornati solo sull'**Event Dispatch Thread** (EDT). Il thread `ThreadRicezione`, che legge i messaggi in arrivo bloccandosi sul socket, non aggiorna quindi mai direttamente la `ChatPanel`, ma inoltra la callback all'EDT tramite `SwingUtilities.invokeLater`:

```JAVA
if (onMessaggio != null) {
    SwingUtilities.invokeLater(onMessaggio);
}
```

Questo evita che un messaggio ricevuto in un momento imprevedibile (asincrono rispetto al ciclo di eventi di Swing) provochi un aggiornamento della `JTextArea` della chat da un thread diverso da quello grafico, causa comune di comportamenti imprevedibili o crash nelle applicazioni Swing multithread.

### Socket e/o REST

Tra Socket e REST, l'idea iniziale lasciava aperte entrambe le strade per la **chat multiplayer**; nel progetto è stata scelta l'implementazione a **Socket TCP** (package `Network`, già descritto nella sezione sull'architettura). La scelta è dovuta alla natura stessa della funzionalità: una chat richiede uno scambio di messaggi **continuo e bidirezionale** tra client connessi contemporaneamente, mentre REST è pensato per interazioni **stateless** di tipo richiesta/risposta, meno adatte a notificare in tempo reale un client quando un altro giocatore scrive. Con i socket, invece, ogni client mantiene una connessione TCP persistente col server (`GameServer`/`ClientHandler`), che può quindi fare da subito il **broadcast** di ogni messaggio a tutti i partecipanti non appena arriva, senza dover attendere che i client lo richiedano tramite polling.

# Informazioni sul lavoro di gruppo e sul progetto
La suddivisione dei compiti all'interno del gruppo è avvenuta in modo concreto, tramite consultazione: ognuno ha scelto di occuparsi delle parti che gli interessavano o in cui si sentiva più a suo agio, senza una vera e propria assegnazione dall'alto. Anche la divisione dei compiti, pur essendo nata in modo naturale e senza una pianificazione rigida, si è rivelata efficace, il che ha reso il lavoro più rapido.
#### Architettura e gestione del codice

- **Pattern architetturale:** Dal punto di vista architetturale abbiamo scelto di seguire il pattern **MVC**, separando la logica di gioco (**Model**), l'interfaccia grafica (**View**) e il coordinamento tra le due (**Controller - GameEngine**).
    
- **Gestione tramite repository:** Per la gestione del codice abbiamo lavorato su **GitHub**, utilizzando branch separati per le diverse funzionalità e aprendo **pull request** per unire il lavoro sul **branch principale**. Questo approccio ci ha permesso di rivedere le modifiche prima di integrarle ed evitare conflitti quando lavoravamo in parallelo sulle stesse parti del progetto.
#### Punti di forza e difficoltà

- **Comunicazione efficace:** Il punto di forza principale del gruppo è stata la comunicazione: ci siamo confrontati spesso in chiamata su **Discord**, il che ha reso più semplice tenere tutti allineati sullo stato di avanzamento e risolvere velocemente eventuali dubbi o blocchi.
  Uno dei punti forti del progetto è stato sicuramente utilizzare i JSON, così i dialoghi sono stati facilmente **modificabili** (nelle fasi di fix), senza che nessuna `TextArea` e `label` di testo vengano alterate.
    
- **Risoluzione dei conflitti:** La difficoltà principale è stata gestire alcuni conflitti di merge su GitHub, soprattutto nei momenti in cui più persone lavoravano in parallelo su parti del codice che finivano per toccarsi (ad esempio quando modifiche diverse interessavano le stesse classi o gli stessi file di configurazione). Li abbiamo risolti confrontandoci direttamente sulle modifiche prima di integrarle, il che ci ha fatto capire l'importanza di comunicare in anticipo quando si stava per toccare una parte condivisa del codice, piuttosto che scoprirlo solo al momento della pull request. Un altra difficoltà incontrata è stato ritrovarci molto spesso con delle **GodClass**, così da attuare varie separazioni su file paralleli del lavoro che eseguiva un solo file, così da comprendere a pieno quanto sia importante l'**incapsulamento** e la **suddivisione dei compiti** che deve gestire ogni file senza mischiare i compiti di una classe con un altra e astrarre i compiti il più possibile.
#### Asset esterni e funzionalità accantonate

- **Grafica:** Per quanto riguarda la parte grafica, le immagini di base sono state reperite su **Pinterest** e poi ritoccate tramite **AI generativa** per adattarle allo stile e alle esigenze del gioco, purtroppo nessuno del team sa disegnare e volevamo tanto fare la parte grafica.
    
- **Audio:** Per l'audio abbiamo invece attinto a librerie di suoni **free-copyright** reperite su **YouTube**.
    
- **Idee non implementate:** Tra le funzionalità accantonate per motivi di tempo, la più significativa era l'idea di un **finale alternativo**, che sarebbe dovuto comparire in modo casuale con una probabilità **randomica**, implementabile ad avvio del gioco molto bassa (circa 1 su 1000). È un'idea che avrebbe aggiunto rigiocabilità al progetto, ma che abbiamo scelto di non implementare per concentrare il tempo a disposizione sulle funzionalità madre del gioco.
