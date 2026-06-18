## Idea originale
L'idea di base è basata sull'ispirazione a *Monkey Island* e *Phoenix Wright* per un'avventura grafico-testuale.

### La trama rispetto al codice
La trama si adatta in modo perfetto sui concetti teorici delle avventure testuali richiesti.
In base alle richieste del prof, si è preventivato di adattare il codice con la trama nella seguente maniera:

* **OOP, Classi e Proprietà:** La spada sincro che si "ricarica del 30% ad ogni enigma" è l'esempio di un oggetto con una proprietà che cambia durante l'avventura.

* **Enigmi e Thread/Concorrenza:** Per poter calcolare un punteggio, si può utilizzare un thread concorrente all'esecuzione dell'enigma per il calcolo del tempo e del punteggio in base a questo.

* **Gestione NPC e Sockets/REST:** L'idea di poter parlare con NPC e amici *"nell'online"* per capire dove trovare la chiave è geniale. Sfrutta le **Socket o le API REST** per collegare il client del giocatore a un server rudimentale (per esempio il pc di chi avvia il gioco per primo).

* **Mappa e Luoghi:** Il castello, l'ingresso e le montagne si traducono in una mappa strutturata a grafo o a matrice. 

* **Dialoghi:** L'opzione di scelta multipla (1. Velluto, 2. Seta, ecc.) è quello che serve per semplificare l'interazione nel gioco (user-friendly).

### Architettura del Progetto
Divideremo in tre categorie il codice:

#### Package 1: Model (Il Back-end e la Logica)
* **`Stanza` (Room):** Ha un nome, una descrizione, una lista di oggetti presenti e i collegamenti ad altre stanze.

* **`Oggetto` (Item):** Interfaccia o classe astratta. Avrà nome, descrizione e uno *score* (punteggio).

* **`Personaggio` (NPC):** Gestisce l'albero dei dialoghi.

* **`Giocatore`:** Contiene la posizione attuale (Stanza corrente) e l'`Inventario`.

* **`Inventario` (Generics/Lambda):** Usa i Generics per la collezione di oggetti. Usa le **Lambda Expression e gli Stream**  per cercare un oggetto (es. `inventario.stream().filter(obj -> obj.getNome().equals("Chiave")).findFirst()`).

#### Package 2: View (Il Front-end Grafico / SWING)
* **`MainFrame` (JFrame):** La finestra principale, che verrà divisa in due. 
1. Un pannello superiore (`ImagePanel`) che mostra lo sfondo della location corrente (come i cancelli del Castello) e (forse) lo sprite del PG presente. 
2. Un pannello inferiore (`DialogPanel`) contenente una `JTextArea` non modificabile per il testo e un pannello laterale con i pulsanti (`JButton`) per le azioni: "Esamina", "Parla", "Inventario", "Spostati".

#### Package 3: Controller (L'intermediario)
* **`GameEngine`:** Inizializza la mappa e gestisce le interazioni. Quando l'utente preme il pulsante "Prendi Tessuto Reale", il Controller riceve l'input dalla *View*, chiama il metodo sul *Model* (es. `giocatore.aggiungi(tessuto)`), e poi dice alla *View* di aggiornare la casella di testo: *"Hai recuperato il Tessuto Reale!"*. (Facendo un esempio)

## Tecnicismi

###  Gestione Database (JDBC): Salvataggi e Ranking
Dato che il gioco non finisce mai in modo prematuro, il database avrà una doppia funzione strutturale fondamentale per l'esperienza del giocatore:

* **Sistema di Salvataggio:** Crea una tabella `Salvataggi`. Quando il giocatore decide di salvare, il database memorizza lo stato esatto di Eryndor: la stanza in cui si trova, gli ID degli oggetti nell'inventario, il punteggio accumulato fino a quel momento.

* **Punteggi (Hall of Fame):** Crea una tabella `Classifica`. Poiché il punteggio si basa sulla velocità di risoluzione degli enigmi tramite i Thread (meno tempo = più punti), alla fine del gioco, quando Eryndor sposa la Principessa Marien, il punteggio finale totale viene salvato nel DB associato al nome del giocatore, che sarà visualizzabile dopo i titoli di coda.

### Gestione File (I/O): Il Copione degli NPC
Nessun dialogo deve essere scritto direttamente nel codice sorgente Java. Questo pulisce il codice e rispetta appieno il requisito dell'uso dei file.

* **File di Testo/JSON:** Creeremo un file dedicato con tutte le battute di Mr.Cooper, Fox, Saggio Clock, Eripeta, David e degli altri personaggi.
* **Lettura all'Avvio:** Durante il caricamento del gioco, una classe dedicata (ipotizziamo `DialogueLoader`) leggerà il file tramite `BufferedReader` o librerie JSON, salvando i dialoghi in una struttura dati appropriata (come una `HashMap` in cui la chiave è il nome dell'NPC e il valore è la lista delle sue battute).

### Interfaccia Grafica e Controlli (SWING & Regex)
* **Puntatore Dinamico (Hotspots):** Sulla `JLabel` o `JPanel` che contiene l'immagine di sfondo, imposteremo un `MouseMotionListener`. Quando le coordinate del mouse entrano in un'"area sensibile" (es. sopra la figura del pescatore o su un oggetto nascosto nell'ambiente ), cambia l'icona del cursore utilizzando `setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))`. Un `MouseListener` intercetterà poi il click effettivo.

* **Frecce Direzionali:** Posizioneremo a schermo quattro pulsanti grafici (NORD, SUD, EST, OVEST). Quando il giocatore clicca su uno di essi, il pulsante innesca l'azione di spostamento. Simuleremo l'esperienza di una avventura testuale in modalità ibrida .

* **Analisi Testuale (Regex):** Per i momenti in cui è necessario l'input da tastiera (es. digitare la risposta esatta all'enigma N.7 della Principessa), utilizzeremo le Espressioni Regolari (Regex) per creare un parser. La Regex permetterà di ignorare spazi extra, maiuscole/minuscole o parole inutili, catturando solo la parola chiave necessaria per risolvere l'enigma.
