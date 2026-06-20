L'idea di base è basata sull'ispirazione a *Monkey Island* e *Phoenix Wright* per un'avventura grafico-testuale. La trama e l'idee implementative sono state ultimate il 17 giungo e si prevede la consegna per il 16 Luglio.

---
### **1. La Trama: È realizzabile e rispetta i requisiti?**

**Assolutamente sì.** La Trama si mappa in modo perfetto sui concetti teorici delle avventure testuali richiesti. Non c'è nulla da stravolgere, ma dobbiamo assicurarci che ogni elemento narrativo sia adatto per implementare le tecnologie richieste dal corso.

Ecco come le nostre idee coprono le richieste del prof:

* **OOP, Classi e Proprietà:** La spada sincro che si "ricarica del 30% ad ogni enigma" è il perfetto esempio di un oggetto con una proprietà che cambia durante l'avventura.

* **Enigmi e Thread/Concorrenza:** *"Più velocemente si risolvono gli enigmi più punteggio ottieni"*. Questa è l'occasione d'oro per usare i **Thread**. Possiamo creare un thread fantasma che funge da timer durante la schermata dell'enigma: meno tempo ci mette, più punti accumula il giocatore.

* **Gestione NPC e Sockets/REST:** L'idea di poter parlare con NPC e amici *"nell'online"* per capire dove trovare la chiave è geniale. Sfrutta le **Socket o le API REST** per collegare il client del giocatore a un server rudimentale (per esempio il pc di chi avvia il gioco per primo).

* **Mappa e Luoghi:** Il castello, l'ingresso e le montagne si traducono facilmente in una mappa strutturata a grafo o a matrice.

* **Dialoghi:** L'opzione di scelta multipla (es. 1. Velluto, 2. Seta, ecc.) è esattamente ciò che viene suggerito per semplificare l'interazione coi personaggi.

**L'unica accortezza:** La documentazione finale deve includere una **Specifica Algebrica (non assiomatica)** di una struttura dati. Abbiamo ipotizzata di farla sulla struttura che gestisce l'**Inventario**.

---
### **2. Architettura del Progetto: Come dividere Logica e Grafica**

Usiamo **SWING**  e un'impostazione grafica (stile *Phoenix Wright/Monkey Island*), è **fondamentale** usare il pattern architetturale **MVC (Model-View-Controller)**. In NetBeans, dividiamo il codice in questi tre package separati. Rigorosamente:

#### **📦 Package 1: Model (Il Back-end e la Logica)**

Qui risiede il "cervello" del gioco. Nessuna classe in questo package deve sapere dell'esistenza di SWING.

* **`Stanza` (Room):** Ha un nome, una descrizione, una lista di oggetti presenti e i collegamenti ad altre stanze.

* **`Oggetto` (Item):** Interfaccia o classe astratta. Avrà nome, descrizione e uno *score* (punteggio). Da qui possiamo estendere classi specifiche come `SpadaSincro` (che implementa un metodo `incrementaPotere()`).

* **`Personaggio` (NPC):** Gestisce l'albero dei dialoghi.

* **`Giocatore`:** Contiene la posizione attuale (Stanza corrente) e l'`Inventario`.

* **`Inventario` (Generics/Lambda):** Usa i Generics per la collezione di oggetti. Usa le **Lambda Expression e gli Stream**  per cercare un oggetto (es. `inventario.stream().filter(obj -> obj.getNome().equals("Chiave")).findFirst()`).

#### **📦 Package 2: View (Il Front-end Grafico / SWING)**

Qui creo la GUI in NetBeans usando il designer visivo.

* **`MainFrame` (JFrame):** La finestra principale.
* *Stile Phoenix Wright:* Dividi la finestra in due. Un pannello superiore (es. `ImagePanel`) che mostra lo sfondo della location corrente (es. i cancelli del Castello) e forse lo sprite del PG presente. Un pannello inferiore (`DialogPanel`) contenente una `JTextArea` non modificabile per il testo e un pannello laterale con i pulsanti (`JButton`) per le azioni: "Esamina", "Parla", "Inventario", "Spostati".

Layer per gli sprite e per gli oggetti cliccabili:
- **L'uso del Layered Pane:** I contenitori di alto livello di Swing (come il `JFrame` che userai per la finestra principale) possiedono una gerarchia di pannelli che include il **Layered Pane**. Questo specifico pannello ha proprio il compito di "gestire la disposizione dei componenti" su più livelli (asse Z). Grazie al `LayeredPane` puoi impostare un'immagine di sfondo sul livello più basso, piazzare gli oggetti cliccabili su un livello intermedio e lo sprite del personaggio su un livello ancora superiore.
- **Creare gli elementi cliccabili (Hotspots):** Come hai previsto nelle tue idee implementative, puoi posizionare delle componenti (ad esempio delle `JLabel` con le tue immagini PNG o anche aree trasparenti) sopra l'immagine di sfondo. A queste componenti puoi aggiungere un `MouseMotionListener` e un `MouseListener`. Quando le coordinate del mouse entrano in queste "aree sensibili" tra il background e il personaggio, puoi far cambiare l'icona del cursore (es. usando l'`HAND_CURSOR`) e intercettare il click effettivo del giocatore per fargli esaminare o raccogliere l'oggetto.
- **Il Glass Pane:** Swing dispone inoltre di un **Glass Pane**, un livello trasparente che si posiziona sopra tutto il `Root Pane` (il contenitore base) e che può essere usato per intercettare gli eventi esterni, come i click del mouse, a livello globale.

#### **📦 Package 3: Controller (L'intermediario)**

* **`GameEngine`:** Inizializza la mappa e gestisce le interazioni. Quando l'utente preme il pulsante "Prendi Tessuto Reale", il Controller riceve l'input dalla *View*, chiama il metodo sul *Model* (es. `giocatore.aggiungi(tessuto)`), e poi dice alla *View* di aggiornare la casella di testo: *"Hai recuperato il Tessuto Reale!"*. (Facendo un esempio)

---
Tecnicismi:
### **1. Gestione Database (JDBC): Salvataggi e Ranking**

Dato che il gioco non finisce mai in modo prematuro, il database avrà una doppia funzione strutturale fondamentale per l'esperienza del giocatore:

* **Sistema di Salvataggio:** Crea una tabella `Salvataggi`. Quando il giocatore decide di salvare, il database memorizza lo stato esatto di Eryndor: la stanza in cui si trova, gli ID degli oggetti nell'inventario, il punteggio accumulato fino a quel momento.

* **Punteggi (Hall of Fame):** Crea una tabella `Classifica`. Poiché il punteggio si basa sulla velocità di risoluzione degli enigmi tramite i Thread (meno tempo = più punti), alla fine del gioco, quando Eryndor sposa la Principessa Marien, il punteggio finale totale viene salvato nel DB associato al nome del giocatore, che sarà visualizzabile dopo i titoli di coda.

---
### **2. Gestione File (I/O): Il Copione degli NPC**

Nessun dialogo deve essere scritto direttamente nel codice sorgente Java. Questo pulisce il codice e rispetta appieno il requisito dell'uso dei file.

* **File di Testo/JSON:** Crea un file dedicato (es. `dialoghi_npc.json` o `script_shambhala.txt`). In questo file inserirai tutte le battute di Mr.Cooper, Fox, Saggio Clock, Eripeta, David e degli altri personaggi.
* **Lettura all'Avvio:** Durante il caricamento del gioco, una classe dedicata (es. `DialogueLoader`) leggerà il file tramite `BufferedReader` o librerie JSON, salvando i dialoghi in una struttura dati appropriata (come una `HashMap` in cui la chiave è il nome dell'NPC e il valore è la lista delle sue battute).

---
### **3. Interfaccia Grafica e Controlli (SWING & Regex)**

L'interazione ibrida (grafica + testuale/regex) si sposa perfettamente con l'uso delle librerie SWING.

* **Puntatore Dinamico (Hotspots):** Sulla tua `JLabel` o `JPanel` che contiene l'immagine di sfondo, imposta un `MouseMotionListener`. Quando le coordinate del mouse entrano in un'"area sensibile" (es. sopra la figura del pescatore o su un oggetto nascosto nell'ambiente ), cambia l'icona del cursore utilizzando `setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))`. Un `MouseListener` intercetterà poi il click effettivo.

* **Frecce Direzionali:** Posiziona a schermo quattro pulsanti grafici (NORD, SUD, EST, OVEST). Quando il giocatore clicca su uno di essi, il pulsante innesca l'azione di spostamento. Questo simula internamente i comandi classici come "vai nord" che verrebbero interpretati in un'avventura puramente testuale.

* **Analisi Testuale (Regex):** Per i momenti in cui è necessario l'input da tastiera (es. digitare la risposta esatta all'enigma N.7 della Principessa), utilizza le Espressioni Regolari (Regex) per creare un parser. La Regex permetterà al tuo codice di ignorare spazi extra, maiuscole/minuscole o parole inutili, catturando solo la parola chiave necessaria per risolvere l'enigma.