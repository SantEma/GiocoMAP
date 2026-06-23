# The Royal Silk Adventure
## Trama principale

### Introduzione
Ci troviamo nel regno incantato di Shambhala. Sono Eryndor, un mercante di tessuti, alla ricerca di elevarmi dal mio status sociale basso per divenire nobile. Il re del regno ha inviato una lettera a tutti i mercanti alla corte per trovare il tessuto perfetto con cui fare il vestito da sposa della sua cara figlia, nonché principessa Marien del regno (il gioco si apre con la lettera). 

>[!TODO] Implementazioni basiche 
>- Il protagonista parla tramite Regex o tramite grafica e nella grafica il giocatore clicca sulle zone presenti nello sfondo per interagire (cliccare sul pescatore, su oggetti o frecce), il cursore diventa a forma di mano quando si trova un oggetto cliccabile
>- Più velocemente si risolvono gli enigmi più punteggio ottieni
>- Il personaggio si sposta tramite frecce direzionali presenti a schermo che lo portano a NORD, SUD, EST o OVEST per spostarsi tra le mappe
>- I dialoghi del NPC sono tutti presenti su un file da estrapolare
>- Non esiste una sconfitta il gioco non permette di perdere
>- Sul DB si salva la partita e i punteggi
### Trama
**6 enigmi** totali nel corso del videogioco di cui **2 al castello**.
Eryndor si trova nella sua **bottega** in piazza centrale e il gioco sia apre proprio da qui.
La **lettera** che giungerà alla sua posta, stile lettera iniziale di Mario64 sarà un invito a tutti i mercanti del regno al castello reale.

**Lettera**:
*"A voi mercanti di Shambhala, vostra maestà in persona vi spedisce questa lettera.
Sono lieto di invitarvi al palazzo reale con lo scopo di rendere uno di voi il mio artigiano di corte.

*Come certamente le voci di corridoio avranno già sussurrato alle vostre orecchie, il cuore di mia figlia, la Principessa Marien, è stato conquistato dal capitano delle guardie Jack, e i preparativi per il matrimonio dell'anno sono ufficialmente aperti. Un evento di tale magnitudo, destinato a rimanere scolpito negli annali della nostra società, esige un fasto senza precedenti. Gli occhi del mondo intero saranno puntati sull'abito della sposa. Desideriamo per lei non un tessuto comune, ma un capolavoro di trama e ordito, capace di catturare la luce dei lampadari di cristallo e la meraviglia di ogni invitato.

*Ordunque, necessito che mi portiate il tessuto migliore che avete e tra tutti voi concorrenti sceglierò il migliore con cui sarà vergato l'abito per mia figlia, e fidatevi, sarete ben che meno ricompensati.

*Distinti saluti, Vostra Imminenza"*

---
#### Fase della città col porto
Il protagonista non conosce la via per andare verso il castello ma si accorge che dietro la lettera è presente un **indovinello**. Vedere enigma N.1

Il protagonista arrivato al porto della città vedrà tante persone portuali, ma la sua attenzione si soffermerà su questa strana creatura. E' un costrutto ideato dal Re in persona che sta analizzando tutti gli arrivati per capire chi sono i mercanti che hanno risolto la prima prova.
	
*   **David**: *"Complimenti! Hai superato il primo ostacolo, ma il tuo cammino sarà ancora più tortuoso, sua maestà cerca solamente gente competente nel suo castello." Per ora ti sei meritato la mappa per andare verso il castello.*
	
>[!TODO] Da implementare
>Inseriremo nel programma una possibilità su 1000 che il clanker sia danneggiato e che risponda con rumori robotici e dia al protagonista una **strana birra**.
>
>Da implementare inoltre che se il giocatore non ha nell'inventario il tessuto il robot lo farà tornare indietro a prenderlo per proseguire nella storia.

Da questo momento in poi il protagonista aprendo la mappa capirà ( e se non lo capirà lo diranno gli NPC) e la prossima tappa è il bosco. 
>[!TODO] Mappa open
>Per aprire la mappa il player potrà sempre vederla tranne nei dialoghi con *M*
>

Per arrivare nel bosco gli serve una **carrozza**, quindi deve andare alla stalla. Se prova ad andare nel bosco senza un signore random NPC basic lo rispedirà indietro.

Arrivato alla stalla incontra il titolare del ranch **Mr.Cooper**.
*   **MrCooper**: *"Cosa posso fare per te giovanotto? Qui puoi trovare la mia scuderia, tutto ad ottimo prezzo, posso assicurartelo."*
*   **Eryndor**: *"Da inserire frase pertinente alla scelta di **carrozza**"*
*   **MrCooper**: *"Per lunghe distanze posso dartene una da 100 monete d'oro, altrimenti anche 80 d'argento vanno bene per brevi durate"*
*   **Eryndor**: *"Non ho soldi con me purtroppo**"
*   **MrCooper**: *"Mmm... Capisco, se proprio desideri quella a brevi distanze te la potrei concedere, ma dovrai fare qualcosa per me."*
*   **Eryndor sceglie se accettare S o N (se non accetta MrCooper ripeterà con frasi sempre più disperate e randomiche di accettare la proposta)**.
*   **MrCooper**: *"Ottimo! Dovrai andare a prendere le carote dal contadino per darle alle mie puledre. Sai ormai ho una certa età."*

Il protagonista va alla ricerca del contadino (lo può trovare in giro chiedendo a degli NPC, si trova vicino alla piazza iniziale).
Parlando con il contadino inizia la sua richiesta:
*   **Contadino**: *"Salve bottegaio come posso esserle utile?"*
*   **Erynodr**: *"Inserire testo pertinente alle **carote**"*
*   **Contadino**: *"Certo, nessun problema se non fosse che avrei un favore da chiederti in cambio. Purtroppo le mie galline continuano a scappare in continuazione e non riesco ad andare al porto per prendere la cena di questa sera, potresti andare tu in cambio delle carote?"*
*   **Eryndor**: *"Accetterà o meno si ricade nel loop di MrCooper"*
*   **Contadino**: *"Eryndor, non so come ringraziarti. Non puoi sbagliarti il pescivendolo è proprio all'ingresso del Porto. Attendo un tuo ritorno"*.

Arrivato al porto e parlando con il negozio ittico, che prima era chiuso se si provava ad interagire, ci sarà quest'uomo molto rude e grezzo.
*   **Pescivendolo**: *"Pesce, seppie, aragoste e molluschi. Solito pescato a km0 , direttamente da questo porto, amico!"*
*   **Eryndor**: *"Dirà che è qui per la cena di Green"*
*   **Pescivendolo**: *"Ah ma certo certo... Solo che devi sapere, -mi vergogno a dirlo-, sono analfabeta e dovrei capire assolutamente come risolvere questo problema presente su questo foglio. Ma non so leggere, potresti risolverlo per me è molto importante e personale. Mia figlia mi accetterà nuovamente a casa se gli dimostro un minimo di istruzione."*

Il protagonista deve risolvere l'indovinello N.2 per acquistare la cena di Green.
Risolto l'enigma ottiene la cena, va a dare la cena a Green che gli da le carote e le porta a Cooper.
A questo punto Cooper gli da la carrozza e il protagonista può dirigersi al Bosco Losco.

---
#### Fase del Bosco Losco
Nel bosco durante il tragitto il giocatore si imbatte in un **ladro** che gli ruberà il tessuto.
*   **Fox**: *"Andrò io al palazzo reale spacciandomi per te e andrò direttamente IO dalla principessa. Addio babbeo!"*
*   **Eryndor**: *"Ridammela né ho bisogno per il mio scopo. Non ti darò pace fin quando non ti trovo."*
*   **Fox**: *"Se proprio ci tieni a riprendere il tuo tessuto vieni a prenderlo... Anzi, lavora per me, come lavori per tutti."*
*   **Eryndor**: *"Mi stavi seguendo dall'inizio quindi... Va bene, ne va del mio onore, dimmi cosa vuoi."*
*   **Fox**: *"Splendido... Vedi, facendo questo LAVORO, sono molto malato e ho bisogno di una pianta medica presente nei meandri di questo bosco. Se la trovi il resto è tutto tuo. Ma tanto nessuno sa dove sia o come sia fatta (Lui non sa che tanto non gli darò nulla e cercherà a vuoto)"*
*   **Eryndor**: *"(Ottimo e ora... Un attimo, se nessuno sa dov'è o com'è fatta posso fregarlo a mio vantaggio). Va bene, resta qui. La cerco immediatamente!"*

>[!NOTE] Scelta stilistica
> I pensieri interiori dei personaggi sono esposti tra parentesi blu, seguendo lo stile dell'avventura testuale tipica del gioco Phoenix Wrigth.

Nel bosco il protagonista potrà interagire con molti oggetti (come alberi, cartelli, piante, ecc...).
Così attiverà il terzo enigma.
Alla risoluzione del terzo enigma e all'ottenimento del tessuto, sussegue il seguente testo a schermo.

*Nelle tasche di Fox trovi anche una chiave, sembra importante così' decidi di tenerla*.

---
#### Karundis
Risolte queste avversità il protagonista potrà uscire finalmente dal bosco arrivando a Karundis la periferia a valle situata poco lontana dal palazzo reale.
Interagendo con ciò che è presente nella città, come due / tre NPC [da implementare], si intravede il sentiero che porta sopra alla montagna al castello e in lontananza c'è una piccola fucina in una grotta.
Andando verso la grotta il protagonista incontra un potente mago che si manifesta a lui.

*   **Saggio Clock**: *"Benvenuto, Eryndor. So chi sei. So tutto di tutti nel regno io. Lascia che mi presenti, sono il Saggio Clock, antico mago temporale e aiutante del Re prima del mio ritiro. Sai sono io che ho dato vita al costrutto del Re che ti ha dato la Mappa."*
*   **Eryndor**: *"Wow, sua saggezza è ... strano e strabiliante. Cosa posso fare per lei?"*
*   **Saggio Clock**: *"Se al castello vuoi arrivare, una prova dovrai superare. Vedo nella tua anima che vendere il tessuto al Re non è il tuo unico scopo, vuoi molto di più. Sappiamo entrambi di cosa stiamo parlando..."*
*   **Eryndor**: *"Si è inutile che lo nascondo, specialmente ad un mago."*
*   **Saggio Clock**: *"Il tempo scorre senza mai fermarsi ricordalo. Dimostra quanto vali, vai verso quella fucina."*

Dopo questa frase il mago scompare.

Analizzando la fucina, il protagonista vede una spada rovinata e mal messa incastrata nell'incudine con inciso un cartellino sul fodero:
*"Una spada dall'elsa opaca e priva di filo, incastrata nell'incudine. Sulla guardia c'è uno strano contatore magico a forma di cuore metallico che batte lentamente, attualmente fermo allo 0%"*.
	
**A schermo**: *"Nemmeno con tutta la forza del mondo riusciresti a prendere questa cianfrusaglia. C'è un disegno dietro all'incudine però..."*
 
Analizzando l'incudine, Inizia il quarto enigma.

Risolto l'enigma la spada si sblocca e il giocatore ottiene la spada sincro, ancora ignaro il suo potere a lui.

>[!TODO]
>Da implementare che d'ora in poi ad ogni enigma la sua descrizione nell'inventario salga del 30%, fino alla fine prima dello scontro che arriverà al max.

---
#### Castello ingresso
Il protagonista scalando il sentiero della montagna arriva al cancello principale del castello ma una guardia reale gli blocca il passaggio spiegandoli che per accedere al portone gli serve una chiave. Il protagonista potrà parlare con gli NPC presenti nella mappa (o nell'online) per capire dove trovare questa chiave, mostrandogli volendo anche vari oggetti.

>[!TODO] Thread and Socket
>Da implementare che gli amici con l'online ti aiutano negli enigmi

Alla conclusione di ciò e dei dialoghi da implementare con gli NPC il giocatore userà sulla guardia reale la chiave che ha preso da Fox e entrerà nel castello.

---
#### Interno del castello
Salendo le scale il protagonista viene fermato da una **Lamia Nobile**, di razza centaura, mezzo uomo mezzo cavallo.

**Eripeta**: *"Viaggiatore, sei diretto nel Palazzo della principessa situato in cima a queste scale per la riunione dei mercanti?"*
**Eryndor**: *"Si e lei chi sarebbe?"*
**Eripeta**: *"Che scortese, perdonami. Sono Eripeta. Sono la veggente del Re. Devi sapere che ci sono molte persone nella stanza già. Il capo delle guardie reali Jack mi ha detto di esaminare gli ultimi arrivati, dato che lui si è allontanato un attimo. Di solito non rispondo a lui ma per oggi sto facendo un eccezione in vista del matrimonio della Principessa."*
**Eryndor**: *"Cosa posso fare per lei dunque?"*
**Eripeta**: *"Entra nella stanza qui accanto, è la mia cripta, devo studiarti con la mia sfera di cristallo."*

Il giocatore viene portato nella stanza. 
**Eripeta**: *"Vedo con la mia sfera che sei un uomo... DISONESTO, vuoi entrare nelle braghe di sua maestà solo per arricchirti di più ed ottenere un titolo nobiliare. Che razza di uomo di valore vorrebbe arrivare a ciò. Che disgusto vai via dal castello prima che ti incenerisca seduta stante."*
**Eryndor**: *"Diamine! (Dovevo immaginarlo che avrebbero scoperto il mio obiettivo prima o poi...). La prego, le prometto che adempirò al mio dovere e non farò nulla di male al regno. In cambio di qualche favore può chiudere un occhio, tutti lo fanno no? E tutti hanno bisogno di qualcosa, parola di mercante."*
**Eripeta**: *"Non provare a comprarmi insolente. Se sei davvero determinato e credi di meritarti un titolo nobiliare te lo dovrai sudare"*
**Eryndor**: *"Come posso fare allora?"*
**Eripeta**: *"Uff, sei proprio ostinato. E va bene allora. Torna dove la tua avventura è cominciata.
 ==Vedo nella sfera una zona piena di blu salato con un signore che ti può dare una mano, di il mio nome e sarai ricompensato==.
 Questo posso dirti, ora sparisci e torna quando avrai trovato qualcosa che ne vale davvero la pena di essere accettato"*.

Il protagonista torna indietro e dovrà andare al Porto come si evince dalle parole di Eripeta. Al porto dovrà riparlare con **David**.

**David**: *"Ti serve qualcosa amico, vedo che sei tornato da me."*
**Erynodr**: *"Inserire parser con il nome di Eripeta"*
**David**: *"Ohhh capisco... Quindi ti ha mandato per questo, speravo che prima o poi si ricongiungesse a lui. Per darti questo oggetto di estrema importanza devo testare la tua preparazione. Eryndor hai fatto molta strada vediamo se riesci a superare un problema così complesso però"*.

Enigma N.5
Il protagonista ottiene **l'ampolla d'oro**, dovrà tornare da **Eripeta** e dargliela.

**David**: *"Purtroppo le lacrime di questo indovinello sono quelle di Eripeta. Devi sapere che è diventata veggente proprio per trovare chi ha trasformato suo figlio in oro liquido. Prima era una guerriera ma il Re non voleva consegnarli questo oggetto, credeva che l'avrebbe fatta soffrire troppo. Per questo l'ha dato via. Ma una persona onesta e giusta dal cuore **nobile** credo che debba riunire sempre una madre col proprio figlio, per quanto male possa fare, è la cosa giusta da dover eseguire."*

Il player torna da Eripeta e gli consegna l'ampolla d'oro
**Eripeta**: *"Ti ho visto dalla mia sfera. Ho visto tutto. Grazie del tuo gesto puoi proseguire. Voglio restare un po' da sola a riflettere ora. Comprendimi"*.
**Eryndor**: *"SI capisco, perdonami se ti ho incuso timore e se ho scoperto della tua storia non era mia intenzione. Ecco... quindi... Io proseguo"*
**Eripeta**: *"... Vai"*

---
#### Finale
Il mercante arriva dalla Principessa, in questa stanza lussuosa e accogliente con già alcuni mercanti del regno all'interno, la stanza è enorme, piena di oggetti vetrati e specchi d'orati.
Marien parla quest'ultima espone il suo enigma:

**Marien**: *"Viandante, vorresti risolvere tu, come tutti i qui presenti il problema che io stessa ho ideato. Dovete sapere tutti che fino ad ora nessuno è mai riuscito a risolverlo e io sono una grande amante delle sfide che richiedono cervello. Nemmeno il mio promesso sposo Jack è mai riuscito a risolverlo. A tal punto, non diteglielo quando arriverà qui in stanza o andrà su tutte le furie"*.

Eryndor, assieme a tutti gli altri mercanti non si tirano indietro e si prostano con un inchino a sua signoria, procedendo ad ascoltare il suo dettato. Alcuni mercanti pensano già di collaborare tra loro per risolverlo, così il giocatore potrà ascoltare i loro dialoghi se cerca una soluzione.

Guardare l'Enigma 7.
Alla risoluzione dell'enigma:
**Marien**: *"Non ho mai assistito a così tanta astuzia... Sono commossa, posso sapere lei chi è buon uomo?"*

**Eryndor**: *"Eryndor, sua signoria. Al suo servizio."*

**Marien**: *"Eryndor... Un nome che non scorderò facilmente. Dimmi, come può un mercante possedere una mente così affilata e un'anima così caparbia? Questo enigma era stato concepito per respingere i presuntuosi e rivelare chi sa guardare oltre la mera apparenza."*

**Eryndor**: *"Sua altezza, il viaggio per giungere fin qui è stato il mio vero maestro d'astuzia. Ho dovuto decifrare i segreti delle maree al porto di MareBlu, comprendere i pericoli del Bosco Losco per sventare i piani di un furfante, e persino svelare i misteri dello scorrere del tempo."*

**Marien**: *(Si avvicina lentamente a lui, i suoi occhi brillano di una luce nuova. Ignora i sussurri degli altri mercanti e osserva il volto di Eryndor).* *"Hai affrontato tutto questo... contando solo sul tuo ingegno? I nobili di questa corte non farebbero un singolo passo senza la scorta delle guardie. Jack... il mio promesso sposo, avrebbe semplicemente sguainato la spada contro ogni ostacolo, cieco di fronte alla bellezza della logica e dell'intelletto."*

**Eryndor**: *(Le sue parole confermano ciò che speravo. Ho la sua totale ammirazione. Devo continuare a tessere questa tela e assecondarla... lei è la mia chiave per ottenere ciò che merito davvero).* *"Siete troppo gentile, mia signora. La vera forza non risiede sempre nell'acciaio, ma nella trama delle nostre scelte. Ogni tappa del mio viaggio era solo un filo necessario per tessere la strada che mi avrebbe condotto al cospetto della vostra impareggiabile intelligenza."*

**Marien**: *"Eryndor. Per tutta la vita mi hanno circondata di sfarzo vuoto, impavidi guerrieri e forza bruta. Ma tu... tu sei diverso. La tua mente ha danzato con la mia attraverso i fili di questo enigma. Sento che tu riesci a comprendermi come nessuno ha mai fatto."*

**Eryndor**: *(Perfetto. È completamente rapita. Un mercante sa sempre cogliere l'occasione quando gli si presenta davanti).* *"Ho sfidato le insidie del regno intero per portarvi il mio tessuto migliore, Principessa. Essere compreso e apprezzato da una mente brillante come la vostra... è un onore che per me vale molto più di qualsiasi ricompensa in oro."*

**A schermo**: *(La Principessa Marien, arrossisce. La Spada Sincro, ormai caricata al MAX della sua potenza, pulsa nel fodero emanando una luce calda, risuonando con il grande destino che si sta per compiere).*

**Marien**: *"Eryndor... io credo che il mio enigma non servisse solamente a scegliere un artigiano. In cuor mio, sapevo che serviva a trovare..."*

**A schermo**: *(Il momento viene spezzato all'improvviso. Le gigantesche porte della sala vengono spalancate con una violenza tale da far tremare i lampadari di cristallo. Una figura imponente in armatura pesante fa il suo ingresso, accecata dall'ira).*

**Sir Jack**: *"MARIEN! Che significa tutto questo?! Chi è questo pezzente di un bottegaio che osa avvicinarsi a te?!"*

Il mercante, ha dimostrato che tramite l'enigma della principessa, era il vero amore della sua vita. Dopo l'enigma avverrà la sfida finale con il cavaliere Jack, dove grazie alla Spada Sincro che sconfiggerà il cavaliere.

Il giocatore dovrà difendersi dal cavaliere.
**Eryndor**: *"(Diamine ho solo una spada mal messa per combattere... O la va o la spacca, ne va della mia vita. Arrivato fin qui non posso tirarmi indietro)"*
**Jack**: *"Ti presento Daniel la mia sciabola più fidata. Preparati a sguainare la tua misera spada e a pregare Tiamat di restare in vita."*
**Eryndor**: *"(Un secondo... ma la mia spada ora è carica, come sarà successo. Speriamo che questo 100% mi porti alla vittoria)"*

Il giocatore selezionando la spada sincro la sguaina contro il nemico. A schermo appaiono lampi e rumori di spade.

**Jack**: *"Impossibile! Chi ti ha concesso di impugnare la Spada Sincro?! La sua lama è già illuminata... dovete aver condiviso un intero viaggio. Questa spada accresce la sua potenza risuonando con chi la brandisce, fondendo l'arma e il suo padrone in un'unica, devastante forza!"*

**Eryndor**: *"(Ecco perché Clock era fissato col tempo, potevo arrivarci da solo) E' la mia occasione allora bene, fatti avanti. Potere a me!"*

**A schermo**: *"Una luce arcobaleno irradia la stanza e un fendente d'orata trafigge Jack, facendo cadere la sua sciabola per terra ricoprendo la stanza di un silenzio profondo che dura per qualche minuto. Dopo di che, alcuni mercanti cominciano ad applaudire sempre più forte"*

**Marien**: *"Mio salvatore, Jack ci avrebbe decapitato entrambi. Hai salvato la vita del nostro amore. Per le leggi di Shambala editate da mio padre in persona chi uccide il promesso sposso della principessa è destinato ad essere il nuovo sposo. E sono molto contenta che sia tu"*

Il mercante si sposa con la principessa e prende il suo **titolo da nobile**.

**Partono le nozze regali e tutti i mercanti ricorderanno per sempre il nome di Eryndor come uno dei mercanti più astuti e forti della storia. Tutti sono fieri della sua mercanzia nel regno, diventando un bene di lusso. Nel tempo le leggende parleranno di lui come un ottimo stratega, altre come un ciarlatano assassino e altre ancora come un super uomo pieno di forza magica.
Ma il viaggio del nostro eroe è concluso e fiero di esser arrivato al suo misero egoista scopo di vita, chissà se capirà mai che i soldi non sono tutto. D'altronde se non lo capisce il personaggio di un gioco come può capirlo il mondo reale. Oh mi sto dilungando troppo, perdonami amico giocatore. Grazie per aver provato il gioco, Addio!**

La frase finale viene detta su sfondo nero ma alla fine si capisce che è Clock a dirla perché si intravede la sua sagoma.

Si avviano i titoli di coda stile Dungeon Quest.

---
## Luoghi
- Piazza iniziale del gioco
- Porto di MareBlu
- Palazzo reale
- Stalla
- Bosco Losco
- Karundis
- Grotta della fucina
- Cancello del castello
- Sala della principessa
- Cripta di Eripeta

## Personaggi
- Eryndor - non grafico
- Re di Shambala - non grafico
- Principessa Marien
- Costrutto magico David
- Mr.Cooper il rancher
- Contadino Green
- Pescivendolo - non grafico
- Saggio Clock

### Nemici
- Ned Fox il ladro
- Sir Jack
- Eripeta

## Oggetti
- **Chiave**: apre il portone del castello
- **Spada Sincro**: la spada utilizzata per sconfiggere il cavaliere nel finale
- **Mappa**: Mappa che permette di tenere traccia del percorso per arrivare al castello
  >[!TODO] Da implementare
  >La mappa sarà apribile in ogni momento (fuori dalle scelte e dialoghi) e darà informazioni con un tracciato rosso delle città da percorrere per arrivare al castello del regno
  
- **Strana Birra**: Automaticamente la birra renderà ubriaco il protagonista facendolo risvegliare come marito di Sir Jack e così si otterrà il finale segreto.
- **Tessuto**: Da consegnare al Re.
- **Carote e cena**: Oggetti per la quest della carrozza
- **Fiori**: Per avvelenare Fox
- **Chiave**: chiave dal dubbio utilizzo, servirà per aprire il castello
- **Ampolla d'oro**: Il figlio di Eripeta per proseguire

---
## Enigmi (e soluzioni)

Ogni enigma da un oggetto che andrà nell'inventario e che servirà per poter risolvere altri tipi di enigmi sparsi nella trama, per esempio la chiave data da un enigma N servirà per aprire un portone dell'enigma N+X.

In qualunque situazione ogni oggetto può essere recuperato se lasciato per far iniziare la sequenza di quel preciso oggetto utile al conseguimento della storia.
Gli NPC durante la storia possono dare consigli randomici su enigmi di varia entità, in base a quale punto il protagonista è bloccato nel gioco.

### Enigma N.1
L'enigma N.1 definito come **indovinello** che porterà al luogo dove si trova la mappa che indica la strada per arrivare al castello.
La mappa sarà situata al porto e la detiene un Costrutto reale

>[!TODO] *Indovinello*:
>_Lascia il telaio e segui la via a Nord, guidato dalla voce della grande campana di bronzo._ _Il tuo cammino termina dove la strada si fa di legno e il vento sa di sale._ _Cerca la piccola vasca di pietra che respira: due volte al giorno il mare la riempie, due volte al giorno la svuota._ _Lì, all'ombra dei giganti che riposano con le ali di tela chiuse, il custode della via ti attende._

>[!WARNING] **Soluzione**
>- *"Lascia il telaio... grande campana"*: Il punto di partenza. Eryndor si trova al mercato o alla bottega e deve dirigersi a Nord partendo dalla piazza principale.
>  - *"Dove la strada si fa di legno e il vento sa di sale"*: Il giocatore capisce subito che l'aria salmastra e i pontili di legno indicano una zona costiera.
>- *"Vasca di pietra che respira... due volte al giorno"*: Ricalca l'idea originale della pozza di marea. Fa capire che il punto esatto di interazione sarà legato all'acqua e all'osservazione delle maree.
>- *"Giganti con le ali di tela chiuse"*: La conferma finale. Le "ali di tela" sono le vele delle navi ammainate. Questo chiarisce senza ombra di dubbio che la destinazione è il **porto**.

Se il giocatore non capisce la direzione da prendere potrà parlare con gli NPC presenti nella mappa per avere informazioni generiche di vario tipo e randomiche.

### Enigma N.2
L'enigma N.2 definito come **indovinello** darà al protagonista la cena di Green, così da avere le carote per poter avere la carrozza e andare nel Bosco.

>[!TODO] Indovinello:
>Hai un vaso di vetro con dentro un solo germe. Dopo un minuto, il germe si suddivide in due germi. Dopo un altro minuto, i due germi si suddividono di nuovo, formando un totale di quattro germi. Con questo ritmo, partendo da un germe il vaso sarà pieno di germi dopo esattamente un'ora di tempo.
>Sapresti dire dopo quanti minuti si riempirà il vaso partendo con due germi anziché uno?

>[!WARNING] Soluzione
>- **Aiuto 1**: Se un germe si divide in due germi e due germi si dividono in quattro, significa che ogni minuto che passa il numero di germi raddoppia.
> - **Aiuto 2**: Se all'inizio c'è un solo germe, il vaso si riempirà in un'ora. 
>   Perciò, se parti con un germe, quanti germi avrai dopo un minuto?
>   Leggi il problema con attenzione.
> - _**Aiuto 3**_ 
>Fai un passo indietro e rifletti. Devi indicare quanto tempo impiegano due germi a riempire il vaso. Se un germe si suddivide in due in un minuto, quanto altro tempo occorrerà per riempire il vaso?

La risposta è **59 minuti**.  
Un germe si suddivide in un minuto, pertanto se all'inizio hai già due germi anziché uno, il vaso si riempirà in un'ora meno un minuto.

**La Formula della Crescita Esponenziale**
La situazione descritta ("ogni minuto che passa il numero di germi raddoppia") si modella perfettamente con una **funzione esponenziale** in base 2.
La formula generale per il numero di germi $N$ al tempo $t$ (in minuti) è:
$$N(t) = N_0 \cdot 2^t$$
Dove:
- $N(t)$ = Numero totale di germi al tempo $t$.
- $N_0$ = Numero iniziale di germi (al tempo $t=0$).
- $2$ = È il tasso di crescita (raddoppio).
- $t$ = Tempo trascorso in minuti.

Ora, l'Aiuto 3 ti fa riflettere su cosa succede se parti con due germi fin dall'inizio ($N_0 = 2$). Vogliamo trovare il tempo $t$ necessario affinché il numero di germi $N(t)$ raggiunga la capacità massima del vaso, che abbiamo calcolato essere $2^{60}$.

Impostiamo l'equazione:
$$N(t) = 2 \cdot 2^t$$

Sappiamo che il vaso è pieno quando $N(t) = 2^{60}$, quindi:
$$2 \cdot 2^t = 2^{60}$$

Per le proprietà delle potenze (prodotto di potenze con la stessa base: $x^a \cdot x^b = x^{a+b}$), possiamo scrivere $2$ come $2^1$:
$$2^1 \cdot 2^t = 2^{60}$$
$$2^{t+1} = 2^{60}$$

Poiché le basi sono uguali (entrambe 2), gli esponenti devono essere uguali:
$$t + 1 = 60$$
$$t = 60 - 1$$
$$t = 59$$

### Enigma N.3
L'enigma N.3 si basa sulla ricerca di una **pianta velenosa**.
Eryndor si addentra nella radura. Il giocatore se interagisce col cartello inizierà l'enigma.
Gli altri oggetti importanti con cui l'utente può interagire nel bosco, facenti parte della quest, sono i **fiori colorati**.

>[!NOTE] Esamina: Cartello degli Esploratori
> *"La febbre dei boschi si cura solo con il fiore blu, il colore del cielo. Attenzione al fiore viola: il suo veleno addormenta all'istante anche un orso."*
>
> **Esamina il fiore Rosso**: "È un fiore Rosso. Emana un odore molto dolce."
> 
> **Esamina il fiore Blu**: "È un fiore Blu, lo stesso colore del cielo limpido."
> 
> **Esamina il fiore Viola**: "È un fiore Viola. Cresce tra le radici nodose di un albero."

*(Eryndor torna da Fox con le piante nella borsa)*
**Scegli quale erba consegnare a Fox:**
- **Opzione 1:** Consegna l'Erba Rossa.
- **Opzione 2:** Consegna l'Erba Blu.
- **Opzione 3:** Consegna l'Erba Viola.

Se il giocatore sceglie l'Opzione 1 o l'Opzione 2
*   **Eryndor:** *"Ecco, prendi questa. È la pianta che cercavi."*
*   **Fox:** *"Ah, fammi vedere... Sì, sembra proprio lei! Grazie del disturbo, babbeo! Ora, come promesso... col cavolo che ti ridò il tessuto! Ciao!"*
*   **Eryndor:** *"(Un attimo! Se gli do la cura reale o un'erba qualunque, questo furfante si rimetterà in sesto o scapperà comunque con la mia stoffa... Mi sta palesemente truffando!)"*
*   **Eryndor:** *"(Non posso rischiare. Devo dargli la pianta velenosa viola per addormentarlo, così potrò riprendermi il tessuto dalle sue tasche mentre è svenuto. Meglio fermarlo prima che scappi!)"*
*   **Eryndor:** *"Aspetta, Fox! Mi sono sbagliato, quella non è la pianta giusta. Guarda meglio nella mia borsa..."*

_(Il gioco reindirizza il giocatore al menu di scelta delle tre erbe)_

#### (Se il giocatore sceglie l'Opzione 3 - SCELTA CORRETTA)
*   **Eryndor:** *"Ecco a te. Questa è la rarissima erba medica che cercavi."*
*   **Fox:** *"Davvero? Ottimo! Dammela subito!"*

_(Fox beve avidamente l'estratto dell'erba viola)_
*   **Fox:** *"Ahah! Funziona, sento già gli effetti! E ora, come ti dicevo prima... il tessuto me lo tengo io, fesso! Non ti ridò un bel nient..."*
*   **Fox:** *"Aspetta... perché... mi sento le palpebre... così pesanti...?"*

_(Fox barcolla e crolla a terra, addormentato profondamente)_
*   **Eryndor:** *"(Perfetto. Il cartello degli esploratori aveva ragione: il fiore viola è un potente sonnifero, altro che medicina!)"*
*   **Eryndor:** *"(Ora che sta dormendo come un sasso, posso riprendermi il tessuto della Principessa direttamente dalle sue tasche senza che possa muovere un dito. Giustizia è fatta!)"*

_(Schermata di notifica: **Hai recuperato il Tessuto Reale!**)_

### Enigma N.4
Il quarto enigma è quello sul tempo, che alla sua soluzione permetterà di ottenere la **spada sincro**.
La spada sincro è apparentemente una spada molto debole, che si ricaricherà (anche tramite qualche barra di ricarica come easter egg) enigma dopo enigma risolti nel castello, divenendo sempre più unita col protagonista.

>[!NOTE] Enigma 4
>Un normalissimo orologio tradizionale ha due lancette e naturalmente la lancetta più lunga gira più in fretta rispetto all'altra. Supponendo che l'orologio qui raffigurato funzioni perfettamente, sapresti dire quante volte le lancette passeranno l'una sull'altra nell'arco di tempo compreso tra mezzogiorno e mezzanotte?

>[!HINT] Aiuti
>**Aiuto 2**:  Nel momento in cui inizi a contare, ovvero a mezzogiorno, le lancette sono già sovrapposte, per cui il passaggio di una lancetta sull'altra non avviene.
>
>**Aiuto 3**: Un passagio ogni ora. Attenzione però: le lancette non passano l'una sull'altra allo scoccare di ogni ora, ma un po'dopo, ad esempio all'una e 5 o alle sei e 33.
>
>Gli aiuti saranno trovabili interagendo con altri oggetti presenti nella fucina, come l'orologio appeso al soffito per il primo o il secondo presente dietro un calderone.

>[!WARNING] Soluzione
>Le lancette passano l'una sull'altra per un totale di 10 volte. Se ci pensi bene, la risposta è ovvia, ma la formulazione dell'enigma potrebbe averti tratto in inganno.  Le lancette passano l'una sull'altra una volta l'ora, ma dato che all'inizio, a mezzogiorno, sono già perfettamente sovrapposte, due delle dodici ore complessive non vanno calcolate al fini della misurazione.

Affinché le lancette siano una sopra l'altra, la lancetta dei minuti deve trovarsi nello stesso identico punto di quella delle ore. Visto che va più veloce, per trovarsi nello stesso punto significa che deve aver fatto lo stesso tragitto, **più un certo numero di giri completi di vantaggio** (ovvero l'ha "doppiata").

Chiamiamo $k$ questo numero di giri completi di vantaggio ($k$ deve essere per forza un numero intero: 1, 2, 3, ecc.).

L'equazione algebrica diventa quindi:
$$T = \frac{T}{12} + k$$
_("I giri della lancetta dei minuti sono uguali ai giri della lancetta delle ore più $k$ giri interi di vantaggio")_

Ora facciamo i calcoli per isolare il tempo $T$:

Portiamo $\frac{T}{12}$ a sinistra:
$$T - \frac{T}{12} = k$$

Risolviamo la frazione:
$$\frac{11T}{12} = k$$

Isoliamo $T$:
$$T = k \cdot \frac{12}{11}$$

Questa è la nostra formula magica: ci dice esattamente a che ora ($T$) avviene la sovrapposizione numero $k$.

Il tuo enigma chiede quante volte succede **tra** le 12:00 ($T = 0$) e mezzanotte ($T = 12$). Dobbiamo quindi trovare quanti numeri interi $k$ possiamo inserire nella formula per ottenere un risultato strettamente compreso tra $0$ e $12$.

Impostiamo la disequazione:
$$0 < k \cdot \frac{12}{11} < 12$$

Dividiamo tutto per $12$:
$$0 < \frac{k}{11} < 1$$

Moltiplichiamo tutto per $11$:
$$0 < k < 11$$

Gli unici numeri interi strettamente compresi tra $0$ e $11$ sono **1, 2, 3, 4, 5, 6, 7, 8, 9 e 10**.

Quindi ci sono esattamente **10** valori di $k$ validi, il che significa 10 sovrapposizioni!

### Enigma N.5
Indovinello sulla **bugia e sulla verità**.

>[!NOTE] Enigma del Vincolo 
>Possiedi un antico Vincolo d'Oro, una linea ininterrotta forgiata da sette lacrime di luce. Il Custode della locanda esige un pedaggio rigoroso: una lacrima esatta per ogni alba, per sette albe consecutive.
>Il Custode è equo e scambierà con te le lacrime dei giorni precedenti come resto, ma fai attenzione: ogni volta che spezzi il Vincolo, la sua essenza magica sanguina e si indebolisce.
>Quante ferite, al minimo indispensabile, dovrai infliggere al tuo Vincolo per saziare il Custode ogni singolo giorno?
>
>Le opzioni sono:
>- Uno
>- Tre
>- Cinque

>[!WARNING] Soluzione 
>La risposta è **1 sola ferita (taglio)**.
>Infrangendo solo il terzo elemento del Vincolo, otterrai esattamente tre parti distinte:
>- La singola lacrima spezzata (valore 1)
> - Un filo da due lacrime intatte (valore 2)
>  - Un filo da quattro lacrime intatte (valore 4)
> 
> I pagamenti avverranno in questo modo perfetto:
> - **1° Giorno**: Cedi il frammento da 1.
>  - **2° Giorno**: Cedi il frammento da 2, ricevendo l'1 come resto.
>  - **3° Giorno**: Cedi il frammento da 1 (ora il Custode ha 1 e 2).
>  - **4° Giorno**: Cedi il frammento da 4, ricevendo 1 e 2 come resto.
>   - **5° Giorno**: Cedi il frammento da 1.
>   - **6° Giorno**: Cedi il frammento da 2, ricevendo l'1 come resto.
>   - **7° Giorno**: Cedi l'ultimo frammento da 1.

>[!HINT] Aiuti 
>- **Aiuto 1**: Non pensare di dover presentare un pezzo tagliato su misura per ogni nuovo giorno. Ricorda che il Custode può darti il resto. Se il primo giorno gli cedi un frammento singolo, il secondo giorno potresti cedergliene un frammento doppio, riprendendoti indietro quello singolo!
> 
>- **Aiuto 2**: Pensa alla matematica dei raddoppi. Se tu avessi a disposizione un frammento che vale 1, un frammento che vale 2 e un frammento che vale 4, potresti scambiarli per ottenere qualsiasi valore da 1 a 7.

### Enigma Finale, L'enigma della principessa
Ci sono ora cinque manichini (numerati da 1 a 5) e cinque stoffe:
>[!NOTE] Enigma della principessa
>
>- **Seta Celeste**: lucente, leggerissima, evoca l'acqua del porto.  
>- **Velluto Cremisi**: rosso profondo, il più pesante di tutti, tessuto storicamente regale.  
>- **Broccato Dorato**: rigido, intessuto con fili d'oro zecchino, il più costoso del regno.  
>- **Lino Candido**: opaco, grezzo, il più umile, privo di qualsiasi riflesso.  
>- **Damasco Verde Smeraldo**: ricamato con motivi di foglie e rami, custodisce il ricordo del bosco attraversato da Eryndor prima di arrivare al castello.
>
>"Il mio abito racconterà il mio cammino attraverso cinque tappe cerimoniali. Ascoltate le nuove leggi restrittive di Shambhala:
>- Il **primo manichino**, dedicato all'Origine della mia stirpe, rifiuta la superbia dell'oro e la grezza umiltà del lino; il suo peso, inoltre, supera sia quello del tessuto legato al porto, sia quello del tessuto che custodisce il ricordo del bosco.
>- Il **tessuto del porto**, simbolo del mio Amore con il capitano Jack, non potrà mai occupare l'ultimo manichino, ed è legato al Broccato Dorato da un vincolo aritmetico inderogabile: il Broccato dovrà trovarsi esattamente tre posizioni dopo di esso.
>- Il **Lino Candido**, simbolo dell'Umiltà, dovrà occupare una posizione dispari, ma mai prima del tessuto del porto.
>- Il **Damasco Verde Smeraldo**, custode del ricordo del bosco, dovrà occupare una posizione pari, e non potrà mai trovarsi accanto al tessuto più pesante del tavolo."

**Marien**: *"Dovete scegliere tra le seguenti opzioni:
- **Opzione 1**: 1. Velluto Cremisi, 2. Seta Celeste, 3. Damasco Verde Smeraldo, 4. Lino Candido, 5. Broccato Dorato.  
- **Opzione 2**: 1. Velluto Cremisi, 2. Broccato Dorato, 3. Lino Candido, 4. Damasco Verde Smeraldo, 5. Seta Celeste.  
- **Opzione 3**: 1. Damasco Verde Smeraldo, 2. Seta Celeste, 3. Lino Candido, 4. Velluto Cremisi, 5. Broccato Dorato.  
- **Opzione 4**: 1. Velluto Cremisi, 2. Seta Celeste, 3. Lino Candido, 4. Damasco Verde Smeraldo, 5. Broccato Dorato."*

**Se viene scelta l'opzione sbagliata**:
Se il giocatore seleziona l'Opzione 1, 2 o 3 (Scelta Errata):

**A schermo**: (Un mercante con un turbante piumato spinge via Eryndor con il gomito, ridacchiando)
**Mercante Piumato**: *"Fatti da parte, bottegaio! Con cinque manichini ci vuole un occhio allenato come il mio, non quello di un dilettante di provincia!"*
(Il mercante dispone i tessuti esattamente secondo l'opzione scelta dal protagonista e salvata in una var.')
**Principessa Marien**: *"Fermo! Avete spezzato l'ordine matematico. Guardie, fuori di qui!"*
**Mercante Piumato**: *"No! Aspettate, vi prego, posso correggere, posso—!"*
**A schermo**: (Le guardie lo trascinano via mentre i suoi nastri piumati volano per la sala)
**Eryndor**: *(Per poco non ho commesso lo stesso errore. Con cinque tessuti la trappola è doppia: non basta più incrociare due indizi, bisogna verificarli tutti insieme prima di muovere un solo dito.)*

Questa dinamica si ripete in loop fino alla scelta corretta.

>[!WARNING] Soluzione
>- La prima legge esclude dal Manichino 1 il Broccato, il Lino, e — per via del confronto di peso — sia la Seta che il Damasco, perché entrambi più leggeri del tessuto richiesto. Resta solo il Velluto Cremisi. **Manichino 1: Velluto**.
>- La seconda legge lega Seta e Broccato con uno scarto fisso di tre posizioni: Broccato = Seta + 3. Tra le posizioni libere (2, 3, 4, 5), l'unica coppia che regge il conto è Seta al 2 e Broccato al 5, perché 2+3=5, 2 + 3 = 5, 2+3=5. Se provassi Seta al 3, il Broccato dovrebbe stare all'inesistente posizione 6. Dunque **Manichino 2: Seta, Manichino 5: Broccato**.
>- Restano solo le posizioni 3 e 4 per Lino e Damasco. La terza legge vuole il Lino su un numero dispari: tra 3 e 4, solo il 3 è dispari. **Manichino 3: Lino**. Per esclusione, al **Damasco non resta che la posizione 4**, che è anche pari come richiesto dalla quarta legge — e infatti non è adiacente al Velluto, che sta lontano al Manichino 1.

La sequenza è bloccata e unica: **1. Velluto, 2. Seta, 3. Lino, 4. Damasco, 5. Broccato**.

(Il gioco reindirizza il giocatore al menu di scelta)
Se il giocatore sceglie l'Opzione 4 (SCELTA CORRETTA):
**Eryndor**: *"Per me l'opzione è la numero 4"*.
(La spada sincro ora è al MAX)


