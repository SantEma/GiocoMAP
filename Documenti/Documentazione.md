# **The Royal Silk Adventure**
Il progetto è stato presentato dal gruppo **AEG**  per l'**esame di Metodi Avanzati di Programmazione** *M-Z* *A.A. 2025/2026*
### Componenti
- **Andrea Milo**
- **Emanuele Santoruvo**
- **Giulio Murgo**
# Descrizione dell’avventura

L’avventura consiste nella storia del protagonista **Eryndor**, mercante di tessuti alla ricerca di un titolo nobiliare.

Nel regno si dovranno svolgere le nozze della **Principessa Marien**, ed il Re del regno è alla ricerca del **tessuto** perfetto per il matrimonio.

Da qui inizia il lungo e tortuoso cammino verso il **castello**, che avrà diversi imprevisti ed **enigmi** da risolvere incontrando diversi personaggi che lo ostacoleranno o lo aiuteranno nel suo percorso.

Avventuratevi e scoprite se il nostro protagonista riuscirà a raggiungere il suo obiettivo, e qual è il suo **vero obiettivo….**

Per consultare la **trama completa nel suo sviluppo** e visualizzare gli **enigmi con la loro soluzione** e spiegazione matematica, visualizzare il file [**Trama.md**](Idee%20trama/Trama.md)
# Progettazione

Fornire dettagli sulla progettazione. Come sono state individuate le classi, quali sono le competenze di ogni classe, come sono state organizzate le classi in package, strumenti esterni usati (plant, ia, scelte per la grafica, librerie, ppt ecc…).

## Diagramma delle classi

[Cliccare qui per visualizzare l’UML.](UML%20e%20design/UML%20delle%20classi.png)

**[Inserire una diagramma delle classi di una porzione significativa del progetto e commentare il diagramma fornendo dettagli sui principi della programmazione ad oggetti che sono stati utilizzati (ereditarietà, interfacce, classi astratte, composizione, …) COMMENTARE]**

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

Per ciascun argomento del corso spiegare se e come è stato utilizzato all’interno del progetto.

### Programmazione generica

bla bla bla…

### File

bla bla bla…

### Database (JDBC)

bla bla bla…

### Lamba Expression (compresi stream e pipeline)

bla bla bla…

### SWING

bla bla bla…

### Thread e programmazione concorrente

bla bla bla…

### Socket e/o REST

bla bla bla…

# Informazioni sul lavoro di gruppo e sul progetto


---
# NOTE VALUTATIVE e IMPLEMENTATIVE CHE SARANNO TOLTE DA QUESTO FILE, SERVONO SOLO ORA IN VIA DI SVILUPPO
**La struttura del documento è solo un suggerimento. È possibile modificarla purché il documento contenga le informazioni richieste. È possibile inserire altre sezioni, ad esempio: soluzione del gioco, dettagli sull’organizzazione del lavoro di gruppo, ecc…**

### Note sulla valutazione

Il caso di studio verrà valutato in una scala da 0 a 50. Il voto finale verrà rapportato in trentesimi.

Il voto è determinato da 10 criteri, ognuno dei quali può avere un voto tra 0 e 5. I criteri sono:

1. qualità dell’avventura
2. qualità della programmazione ad oggetti
3. utilizzo dei file
4. utilizzo di database/JDBC
5. utilizzo dei thread e della programmazione concorrente
6. utilizzo delle socket e/o delle REST
7. utilizzo delle SWING
8. utilizzo delle lambda expression, stream e pipeline
9. qualità della documentazione (documentazione progetto + documentazione codice)
10. punteggio bonus che tiene conto della complessità del progetto rapportata anche al numero dei componenti del gruppo

**Tutto il materiale deve essere consegnato 5 giorni prima della prova orale. Deve essere consegnato tramite mail allegando uno zip o un link per il download. Il testo della mail deve riportare in modo chiaro tutti i membri del gruppo.**

### Svolgimento della prova orale

Ogni gruppo presenterà il caso di studio e una demo live dell’avventura realizzata per un tempo massimo di 20 minuti. È possibile preparare delle slide, ma non è obbligatorio. Al termine della presentazione, ogni membro del gruppo sarà interrogato su tutti gli argomenti del corso.

Il voto finale terrà conto sia della valutazione del caso di studio sia della qualità della prova orale.

**Le note sulla valutazione e lo svolgimento della prova vanno eliminate dal documento che verrà consegnato.**

## Idea originale
L'idea di base è basata sull'ispirazione data dai giochi Monkey Island e Phoenix Wright per un'avventura grafico-testuale.
### La trama rispetto al codice
Abbiamo adattato la trama per poter coincidere con le richieste fatte dal professore in termini di 
* **OOP, Classi e Proprietà:** La spada sincro che si "ricarica del 30% ad ogni enigma" è l'esempio di un oggetto con una proprietà che cambia durante l'avventura.
* **Enigmi e Thread/Concorrenza:** Per poter calcolare un punteggio, si può utilizzare un thread concorrente all'esecuzione dell'enigma per il calcolo del tempo e del punteggio in base a quest'ultimo.
* **Gestione chat con Sockets e/o REST:** Per poter parlare con altri giocatori che stanno giocando il gioco (in qualunque punto siano) in modo da poterne interagire o parlare con loro
* **Mappa e Luoghi:** Il castello, l'ingresso e le montagne verranno trasformate in una mappa strutturata a grafo o a matrice. 
* **Dialoghi:** L'opzione di scelta multipla (1. Velluto, 2. Seta, ecc.) è utilizzata per semplificare l'interazione nel gioco (rendendolo user-friendly).
### Architettura del Progetto
Divideremo in tre categorie il codice:
#### Package 1: Model (Il Back-end e la Logica)
* **`Stanza` (Room):** Ha un nome, una descrizione e i collegamenti ad altre stanze.
* **`Oggetto` (Item):** Interfaccia o classe astratta. Avrà nome, descrizione e uno score (punteggio, solo per la spada).
* **`Personaggio` (NPC):** Gestisce l'albero dei dialoghi, richiamando opportunamente il suo dialogo prestabilito. Un fantoccio verrà istanziato per poter dare al giocatore i vari oggetti
* **`Giocatore`:** Contiene la posizione attuale (Stanza corrente) e l'`Inventario`.
* **`Inventario` (Generics/Lambda):** Usiamo i Generics per la collezione di oggetti. Usa le **Lambda Expression e gli Stream**  per cercare un oggetto (es. `inventario.stream().filter(obj -> obj.getNome().equals("Chiave")).findFirst()`).
#### Package 2: View (Il Front-end Grafico / SWING)
* **`MainFrame` (JFrame):** La finestra principale, che verrà divisa in due. 
1. Un pannello superiore (`ImagePanel`) che mostra lo sfondo della location corrente (come i cancelli del Castello) e (forse) lo sprite del PG presente. 
2. Un pannello inferiore (`DialogPanel`) contenente una `JTextArea` non modificabile per il testo e un pannello laterale con i pulsanti (`JButton`) per le azioni: "Esamina", "Parla", "Inventario", "Spostati".
#### Package 3: Controller (L'intermediario)
* **`GameEngine`:** Inizializza la mappa e gestisce le interazioni. Quando l'utente preme il pulsante "Prendi Tessuto Reale", il Controller riceve l'input dalla View, chiama il metodo sul Model (es. `giocatore.aggiungi(tessuto)`), e poi dice alla View di aggiornare la casella di testo: "Hai recuperato il Tessuto Reale!". (Facendo un esempio)
## Tecnicismi
### Gestione Database (JDBC): Salvataggi e Ranking
Dato che il gioco non finisce mai in modo prematuro, il database avrà una doppia funzione strutturale fondamentale per l'esperienza del giocatore:
* **Sistema di Salvataggio:** Crea una tabella `Salvataggi`. Quando il giocatore decide di salvare, il database memorizza lo stato esatto di Eryndor: la stanza in cui si trova, gli ID degli oggetti nell'inventario, il punteggio accumulato fino a quel momento.
* **Punteggi (Hall of Fame):** Crea una tabella `Classifica`. Poiché il punteggio si basa sulla velocità di risoluzione degli enigmi tramite i Thread (meno tempo = più punti), alla fine del gioco, quando Eryndor sposa la Principessa Marien, il punteggio finale totale viene salvato nel DB associato al nome del giocatore, che sarà visualizzabile dopo i titoli di coda.
### Gestione File (I/O): Il Copione degli NPC
Nessun dialogo deve essere scritto direttamente nel codice sorgente Java. Questo pulisce il codice e rispetta appieno il requisito dell'uso dei file.

* **File di Testo/JSON:** Creeremo un file dedicato con tutte le battute di Mr.Cooper, Fox, Saggio Clock, Eripeta, David e degli altri personaggi.
* **Lettura all'Avvio:** Durante il caricamento del gioco, una classe dedicata (ipotizziamo `DialogueLoader`) leggerà il file tramite `BufferedReader` o librerie JSON, salvando i dialoghi in una struttura dati appropriata (come una `HashMap` in cui la chiave è il nome dell'NPC e il valore è la lista delle sue battute).
### Interfaccia Grafica e Controlli (SWING & Regex)
* **Puntatore Dinamico (Hotspots):** Sulla `JLabel` o `JPanel` che contiene l'immagine di sfondo, imposteremo un `MouseMotionListener`. Quando le coordinate del mouse entrano in un'"area sensibile" (es. sopra la figura del pescatore o su un oggetto nascosto nell'ambiente ), cambia l'icona del cursore. Un `MouseListener` intercetterà poi il click effettivo.
* **Frecce Direzionali:** Posizioneremo a schermo quattro pulsanti grafici (NORD, SUD, EST, OVEST), quando il giocatore clicca su uno di essi il pulsante innesca l'azione di spostamento. Simuleremo l'esperienza di una avventura testuale in modalità "ibrida" .
* **Analisi Testuale (Regex):** Per i momenti in cui è necessario l'input da tastiera (es. digitare la risposta esatta all'enigma N.7 della Principessa), utilizzeremo le Espressioni Regolari (Regex) per creare un parser. Questa permetterà di ignorare spazi extra, maiuscole/minuscole o parole inutili, catturando solo la parola chiave necessaria per risolvere l'enigma.
