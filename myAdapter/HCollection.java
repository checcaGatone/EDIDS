package myAdapter;

/**
 * Rappresenta il contratto comune delle collezioni utilizzate dal progetto,
 * seguendo il modello dell'interfaccia {@code Collection} di J2SE 1.4.2 e
 * mantenendo la compatibilità richiesta con CLDC 1.1.
 *
 * <h2>Introduzione</h2>
 *
 * <p>
 * {@code HCollection} è l'interfaccia più generale della gerarchia delle
 * collezioni definita nel progetto. Una collezione può essere vista come un
 * gruppo di oggetti, chiamati elementi, sul quale è possibile eseguire
 * operazioni di interrogazione, attraversamento e, quando consentito,
 * modifica.
 * </p>
 *
 * <p>
 * L'interfaccia stabilisce quali operazioni devono essere disponibili, ma non
 * specifica direttamente come gli elementi devono essere memorizzati. Non
 * contiene quindi una struttura dati propria, non possiede una
 * {@code Hashtable} e non crea autonomamente gli oggetti che rappresenta.
 * Il comportamento concreto deve essere fornito dalle classi che la
 * implementano.
 * </p>
 *
 * <p>
 * Questa distinzione è importante perché un'interfaccia descrive il contratto
 * visibile al client, mentre l'implementazione decide come rispettarlo.
 * Utilizzando una variabile di tipo {@code HCollection}, il client può
 * lavorare con una collezione senza conoscere necessariamente il nome e la
 * struttura interna della classe concreta che la realizza.
 * </p>
 *
 * <p>
 * {@code HCollection} non indica che tutti i suoi elementi siano distinti e
 * non stabilisce un ordine generale. Alcune implementazioni possono ammettere
 * più elementi uguali, mentre altre possono vietare i duplicati. Allo stesso
 * modo, un'implementazione può fornire un ordine preciso oppure lasciare
 * l'ordine non specificato. Queste caratteristiche devono essere determinate
 * dall'interfaccia più specifica o dalla classe concreta utilizzata.
 * </p>
 *
 * <h2>Motivazione della presenza nel progetto</h2>
 *
 * <p>
 * Il progetto deve riprodurre il comportamento dell'interfaccia
 * {@code Map} di J2SE 1.4.2 utilizzando le funzionalità disponibili
 * nell'ambiente CLDC 1.1. Poiché CLDC 1.1 non mette a disposizione l'intero
 * Java Collections Framework, sono state definite localmente le interfacce
 * necessarie.
 * </p>
 *
 * <p>
 * {@link HMap} rappresenta il contratto della mappa, {@code HCollection}
 * rappresenta il contratto generale di una collezione, {@link HSet}
 * rappresenta una collezione senza elementi duplicati e {@link HIterator}
 * permette di attraversare gli elementi. Queste interfacce collaborano tra
 * loro, ma mantengono compiti differenti.
 * </p>
 *
 * <p>
 * La lettera {@code H} serve a distinguere le interfacce locali da quelle
 * appartenenti al pacchetto standard {@code java.util}. Di conseguenza,
 * {@code HCollection} riproduce il contratto essenziale della corrispondente
 * interfaccia di J2SE 1.4.2, ma non è la stessa interfaccia e non può essere
 * utilizzata direttamente dove un metodo richiede una
 * {@code java.util.Collection}.
 * </p>
 *
 * <p>
 * Questa scelta evita anche di dipendere da funzionalità appartenenti a
 * versioni di Java successive rispetto a quella indicata nella consegna.
 * L'interfaccia rimane quindi coerente con le limitazioni dell'ambiente di
 * esecuzione utilizzato dal progetto.
 * </p>
 *
 * <h2>Posizione nella gerarchia delle interfacce</h2>
 *
 * <p>
 * {@code HCollection} si trova alla base della gerarchia locale delle
 * collezioni. L'interfaccia {@link HSet} la estende senza aggiungere nuovi
 * metodi, ma introduce un contratto più specifico: una collezione che
 * rappresenta un insieme non può contenere elementi duplicati.
 * </p>
 *
 * <p>
 * Questo significa che ogni {@code HSet} può essere utilizzato anche come
 * {@code HCollection}. Il contrario non è sempre possibile, perché una
 * collezione generica può ammettere più occorrenze dello stesso elemento e
 * quindi non possedere le caratteristiche richieste a un insieme.
 * </p>
 *
 * <p>
 * {@link HMap}, invece, non estende {@code HCollection}. Una mappa non viene
 * considerata una collezione di singoli elementi, perché conserva associazioni
 * tra chiavi e valori. Ogni associazione costituisce un mapping formato da due
 * componenti. Per osservare questi dati come collezioni, la mappa fornisce le
 * viste restituite da {@link HMap#keySet()}, {@link HMap#values()} e
 * {@link HMap#entrySet()}.
 * </p>
 *
 * <p>
 * Anche {@link HIterator} rimane separata da {@code HCollection}.
 * {@code HCollection} dichiara la possibilità di ottenere un iteratore,
 * mentre {@code HIterator} stabilisce le operazioni necessarie per effettuare
 * concretamente l'attraversamento. In questo modo la responsabilità della
 * collezione rimane distinta da quella dell'oggetto che mantiene la posizione
 * durante una visita.
 * </p>
 *
 * <h2>Collezioni generiche e insiemi</h2>
 *
 * <p>
 * Una {@code HCollection} può rappresentare sia una collezione che ammette
 * duplicati sia una collezione che non li ammette. Questa possibilità è
 * necessaria per descrivere correttamente le tre viste di una mappa.
 * </p>
 *
 * <p>
 * La vista delle chiavi restituita da {@link HMap#keySet()} è un
 * {@link HSet}. Una mappa non può contenere contemporaneamente due mapping
 * distinti con la stessa chiave: una nuova associazione con una chiave già
 * presente sostituisce il valore precedente. Per questo motivo ogni chiave
 * compare una sola volta nella vista.
 * </p>
 *
 * <p>
 * Anche la vista restituita da {@link HMap#entrySet()} è un {@code HSet}.
 * I suoi elementi sono oggetti di tipo {@code HMap.Entry}, ciascuno dei quali
 * rappresenta una coppia chiave-valore. Due mapping con la stessa chiave non
 * possono coesistere nella stessa mappa e quindi ogni entry rappresentata
 * dalla vista è unica.
 * </p>
 *
 * <p>
 * La vista dei valori restituita da {@link HMap#values()}, invece, è una
 * {@code HCollection} e non una {@code HSet}. Due chiavi differenti possono
 * essere associate a valori uguali. Le due occorrenze devono essere
 * conservate, perché corrispondono comunque a due mapping distinti.
 * </p>
 *
 * <pre>{@code
 * HMap map = new MapAdapter();
 *
 * map.put("studente1", "Informatica");
 * map.put("studente2", "Informatica");
 *
 * HCollection values = map.values();
 * }</pre>
 *
 * <p>
 * Nell'esempio la vista {@code values} contiene due elementi. Il fatto che
 * entrambi siano uguali alla stringa {@code "Informatica"} non riduce la
 * dimensione della collezione, perché ogni occorrenza proviene da un mapping
 * differente. La dimensione di ogni vista coincide quindi con il numero di
 * mapping presenti nella mappa, non con il numero di elementi distinti
 * osservati nella vista.
 * </p>
 *
 * <h2>Le viste della mappa</h2>
 *
 * <p>
 * Nel progetto, l'utilizzo principale di {@code HCollection} è quello di
 * fornire il tipo comune delle viste appartenenti a {@link MapAdapter}.
 * Una vista permette di osservare gli stessi mapping della mappa concentrandosi
 * sulle chiavi, sui valori oppure sulle entry.
 * </p>
 *
 * <p>
 * Le viste non sono copie indipendenti. Esse rimangono collegate al
 * {@code MapAdapter} dal quale sono state ottenute. Questo comportamento viene
 * normalmente descritto dicendo che le viste sono supportate dalla mappa, o
 * <em>backed by the map</em>.
 * </p>
 *
 * <p>
 * La vera struttura che memorizza chiavi e valori rimane la
 * {@code Hashtable} contenuta nel {@code MapAdapter}. Le viste forniscono
 * soltanto accessi differenti agli stessi dati. Non viene creata una seconda
 * struttura contenente una copia delle chiavi, dei valori o delle entry.
 * </p>
 *
 * <p>
 * Se la mappa viene modificata dopo aver ottenuto una vista, la modifica è
 * visibile anche attraverso la vista già esistente. Non è necessario chiamare
 * nuovamente {@code keySet()}, {@code values()} oppure {@code entrySet()}.
 * </p>
 *
 * <pre>{@code
 * HMap map = new MapAdapter();
 * HCollection values = map.values();
 *
 * map.put("a", "uno");
 * map.put("b", "due");
 * }</pre>
 *
 * <p>
 * Dopo le due chiamate a {@code put()}, la vista {@code values} contiene
 * {@code "uno"} e {@code "due"}, anche se era stata ottenuta quando la mappa
 * era ancora vuota. Questo comportamento dimostra che la vista continua a
 * osservare la mappa e non rappresenta una fotografia fissa del suo contenuto.
 * </p>
 *
 * <p>
 * Il collegamento funziona anche nella direzione opposta. Le operazioni di
 * rimozione eseguite attraverso una vista modificano la mappa originale.
 * Rimuovere una chiave elimina l'intero mapping associato, mentre rimuovere
 * un valore oppure una entry elimina il mapping individuato dall'operazione.
 * La modifica diventa immediatamente visibile anche alle altre viste già
 * ottenute.
 * </p>
 *
 * <p>
 * Le viste non permettono invece di aggiungere liberamente nuovi elementi.
 * L'aggiunta di una sola chiave non specificherebbe il valore da associarle,
 * mentre l'aggiunta di un solo valore non specificherebbe la chiave. Anche
 * l'aggiunta attraverso la vista delle entry viene esclusa dal contratto
 * adottato dal progetto. I nuovi mapping devono essere creati utilizzando
 * {@link HMap#put(Object, Object)}.
 * </p>
 *
 * <p>
 * Nell'implementazione corrente le tre viste vengono create in modo
 * <em>lazy</em>, cioè soltanto quando sono richieste per la prima volta.
 * Successivamente il {@code MapAdapter} conserva e riutilizza l'oggetto
 * corrispondente. Si tratta di una scelta dell'implementazione e non di una
 * proprietà imposta in generale dall'interfaccia {@code HCollection}.
 * </p>
 *
 * <h2>Separazione tra contratto e implementazione</h2>
 *
 * <p>
 * {@code HCollection} dichiara le operazioni che una collezione deve rendere
 * disponibili, ma non contiene il codice necessario per eseguirle. In
 * particolare, l'interfaccia non decide autonomamente dove si trovano gli
 * elementi, come vengono cercati e quali modifiche sono ammesse.
 * </p>
 *
 * <p>
 * All'interno di {@link MapAdapter} il comportamento comune delle viste viene
 * raccolto nella classe astratta interna {@code View}, che implementa
 * {@code HCollection}. Questa classe riutilizza le operazioni del
 * {@code MapAdapter} esterno e permette di evitare la duplicazione dello
 * stesso codice nelle tre viste.
 * </p>
 *
 * <p>
 * Le viste delle chiavi e delle entry derivano anche da {@code SetView},
 * perché devono rispettare il contratto di {@link HSet}. La vista dei valori
 * deriva invece direttamente dal comportamento generale di {@code View},
 * dato che deve mantenere le eventuali occorrenze duplicate.
 * </p>
 *
 * <p>
 * Alcune operazioni sono comuni a tutte le viste. Per esempio, la loro
 * dimensione dipende sempre dal numero di mapping presenti nella mappa.
 * Altre operazioni devono cambiare in base agli elementi rappresentati:
 * cercare una chiave, cercare un valore e verificare la presenza di una
 * coppia chiave-valore richiedono controlli differenti.
 * </p>
 *
 * <p>
 * È quindi necessario non confondere il significato dell'interfaccia con
 * quello di una singola implementazione. {@code HCollection} fornisce il
 * contratto generale; {@code Values}, {@code KeySet} ed {@code EntrySet}
 * stabiliscono il comportamento concreto richiesto dalle rispettive viste.
 * Una futura classe potrebbe implementare {@code HCollection} in modo diverso,
 * purché rispetti il contratto dichiarato.
 * </p>
 *
 * <p>
 * Poiché {@code HCollection} è un'interfaccia, non può definire o imporre
 * costruttori. Nel caso delle viste del progetto, inoltre, il client non deve
 * costruire direttamente gli oggetti concreti: deve ottenerli attraverso i
 * metodi pubblici messi a disposizione da {@link HMap}.
 * </p>
 *
 * <h2>Compatibilità con J2SE 1.4.2 e CLDC 1.1</h2>
 *
 * <p>
 * Le firme utilizzano {@link Object} perché J2SE 1.4.2 precede
 * l'introduzione dei generics. Per esempio, gli elementi non vengono descritti
 * attraverso un parametro di tipo come {@code E}, ma vengono ricevuti e
 * restituiti come oggetti.
 * </p>
 *
 * <p>
 * Questa soluzione consente alla stessa interfaccia di rappresentare
 * collezioni di chiavi, valori ed entry di tipi differenti. Il controllo del
 * tipo concreto avviene però durante l'esecuzione e, quando necessario, il
 * client deve effettuare un cast esplicito.
 * </p>
 *
 * <p>
 * Anche i metodi di conversione utilizzano {@code Object[]} invece delle
 * forme generiche introdotte nelle versioni successive di Java. Il progetto
 * non può quindi utilizzare firme come {@code E[]} oppure {@code T[]}.
 * </p>
 *
 * <p>
 * Per lo stesso motivo {@code HCollection} non estende {@code Iterable} e non
 * può essere attraversata direttamente mediante il ciclo enhanced
 * {@code for}. L'attraversamento viene effettuato esplicitamente ottenendo un
 * {@link HIterator}.
 * </p>
 *
 * <p>
 * Queste non sono mancanze accidentali dell'interfaccia, ma conseguenze della
 * versione di Java richiesta. Aggiungere generics, {@code Iterable}, stream o
 * altri strumenti moderni renderebbe l'interfaccia meno fedele al contratto
 * che il progetto deve riprodurre.
 * </p>
 *
 * <h2>Operazioni opzionali</h2>
 *
 * <p>
 * La presenza di un metodo di modifica nell'interfaccia non implica che ogni
 * implementazione sia obbligata a supportarlo. Nel contratto delle collezioni
 * alcune operazioni sono opzionali. Un'implementazione che non può eseguire
 * correttamente una determinata modifica deve segnalarlo attraverso
 * un'eccezione.
 * </p>
 *
 * <p>
 * Nel progetto le viste di {@code MapAdapter} supportano le rimozioni, perché
 * è possibile determinare quale mapping deve essere eliminato. Non supportano
 * invece le aggiunte, che devono essere effettuate attraverso la mappa. Il
 * rifiuto viene segnalato mediante
 * {@link MapAdapter.HUnsupportedOperationException}, definita localmente per
 * rispettare i limiti dell'ambiente CLDC considerato.
 * </p>
 *
 * <p>
 * Questa distinzione permette a {@code HCollection} di mantenere un contratto
 * generale. Un'altra eventuale implementazione potrebbe supportare anche
 * l'aggiunta, mentre le viste della mappa possono rifiutarla senza violare il
 * significato dell'interfaccia.
 * </p>
 *
 * <p>
 * Nel contratto generale, un'operazione opzionale che non produrrebbe alcun
 * cambiamento può terminare senza modificare la collezione oppure segnalare
 * comunque che l'operazione non è supportata. Il comportamento preciso deve
 * essere indicato nella documentazione del singolo metodo e
 * dell'implementazione concreta.
 * </p>
 *
 * <h2>Elementi ammessi e politica sui valori null</h2>
 *
 * <p>
 * {@code HCollection} non stabilisce una politica universale sugli elementi
 * ammessi. Una classe che la implementa può imporre restrizioni sul tipo degli
 * oggetti oppure vietare l'elemento {@code null}. Queste restrizioni
 * appartengono all'implementazione e devono essere documentate.
 * </p>
 *
 * <p>
 * Nel caso di {@code MapAdapter}, i dati sono memorizzati in una
 * {@code Hashtable} compatibile con CLDC 1.1. Questa struttura non permette
 * di inserire chiavi o valori {@code null}. Di conseguenza, nessuna delle
 * viste della mappa può contenere realmente un elemento {@code null}.
 * </p>
 *
 * <p>
 * Non bisogna però concludere che ogni operazione che riceve {@code null}
 * debba reagire nello stesso modo. In base alla vista e all'operazione
 * concreta, una richiesta non valida può produrre
 * {@link NullPointerException} oppure restituire {@code false}. Le differenze
 * devono essere specificate nella documentazione dei singoli metodi.
 * </p>
 *
 * <p>
 * Anche il significato del tipo dell'elemento dipende dalla vista. La vista
 * delle chiavi riceve oggetti da interpretare come chiavi, la vista dei valori
 * lavora con i valori e la vista delle entry riconosce come elementi validi
 * gli oggetti che rispettano il contratto di {@code HMap.Entry}. L'assenza
 * dei generics rende particolarmente importante descrivere questi requisiti.
 * </p>
 *
 * <h2>Ordine, uguaglianza e concorrenza</h2>
 *
 * <h3>Ordine degli elementi</h3>
 *
 * <p>
 * {@code HCollection} non impone un ordine generale agli elementi. Se una
 * specifica implementazione garantisse un ordine, il suo iteratore e i metodi
 * di conversione in array dovrebbero rispettarlo. In assenza di una garanzia
 * esplicita, il client non deve basare il proprio comportamento sulla
 * posizione con cui gli elementi vengono restituiti.
 * </p>
 *
 * <p>
 * Le viste di {@code MapAdapter} dipendono dall'enumerazione delle chiavi
 * effettuata dalla {@code Hashtable}. Questa enumerazione non corrisponde
 * necessariamente all'ordine di inserimento, a un ordinamento alfabetico o a
 * un ordinamento basato sui valori. Di conseguenza, l'ordine delle chiavi,
 * dei valori e delle entry non è garantito.
 * </p>
 *
 * <p>
 * Anche gli array prodotti dalle viste seguono l'ordine incontrato
 * dall'iteratore durante quella specifica operazione. La conversione in array
 * non introduce quindi un nuovo ordinamento e due attraversamenti distinti
 * non devono essere confrontati facendo affidamento sulla posizione degli
 * elementi.
 * </p>
 *
 * <h3>Uguaglianza e hash code</h3>
 *
 * <p>
 * La sola interfaccia {@code HCollection} non impone che due collezioni siano
 * uguali quando contengono gli stessi elementi. Le dichiarazioni di
 * {@code equals(Object)} e {@code hashCode()} permettono di documentare e
 * realizzare questi comportamenti, ma la semantica concreta dipende dal tipo
 * di collezione.
 * </p>
 *
 * <p>
 * Nell'implementazione corrente la vista {@code Values} non ridefinisce
 * l'uguaglianza per contenuto. Conserva quindi il comportamento ereditato da
 * {@link Object}, basato sull'identità. Due viste dei valori appartenenti a
 * mappe differenti non risultano automaticamente uguali soltanto perché
 * contengono valori uguali.
 * </p>
 *
 * <p>
 * Le viste {@code KeySet} ed {@code EntrySet}, invece, implementano
 * {@link HSet} e devono rispettare il significato dell'uguaglianza tra
 * insiemi. Due di queste viste sono uguali quando hanno la stessa dimensione
 * e contengono gli stessi elementi, indipendentemente dall'ordine con cui
 * vengono attraversate.
 * </p>
 *
 * <p>
 * Per le viste che rappresentano insiemi, l'hash code viene calcolato
 * sommando gli hash code degli elementi. In questo modo due insiemi uguali
 * producono lo stesso hash code anche se gli elementi vengono visitati in un
 * ordine differente. La vista {@code Values}, non ridefinendo
 * {@code equals}, conserva coerentemente anche l'hash code basato
 * sull'identità.
 * </p>
 *
 * <p>
 * Qualsiasi futura implementazione che scelga di ridefinire
 * {@link Object#equals(Object)} deve ridefinire in modo coerente anche
 * {@link Object#hashCode()}. In particolare, se due oggetti risultano uguali,
 * devono restituire lo stesso hash code. Deve inoltre essere rispettata la
 * simmetria dell'uguaglianza.
 * </p>
 *
 * <h3>Modifiche durante l'iterazione e concorrenza</h3>
 *
 * <p>
 * {@code HCollection} non definisce una politica generale di sincronizzazione
 * e non garantisce che tutte le implementazioni possano essere utilizzate in
 * modo sicuro da più thread contemporaneamente. Una classe concreta deve
 * stabilire quali garanzie offre.
 * </p>
 *
 * <p>
 * {@code MapAdapter} utilizza una {@code Hashtable}, le cui singole
 * operazioni sono sincronizzate. Questo non rende però automaticamente
 * atomiche le operazioni delle viste che richiedono più passaggi, come
 * l'attraversamento seguito da una serie di controlli o rimozioni. Un altro
 * thread potrebbe modificare la mappa tra due di questi passaggi.
 * </p>
 *
 * <p>
 * Nell'implementazione corrente, quando viene creato un iteratore, viene
 * costruito uno snapshot delle chiavi presenti in quel momento. La costruzione
 * dello snapshot viene eseguita sincronizzandosi sulla {@code Hashtable}, in
 * modo che l'elenco iniziale delle chiavi sia ottenuto in maniera coerente.
 * </p>
 *
 * <p>
 * Lo snapshot riguarda però soltanto le chiavi utilizzate
 * dall'attraversamento. Una chiave aggiunta alla mappa dopo la creazione
 * dell'iteratore non viene inserita nello snapshot e quindi non viene visitata
 * da quell'iteratore. Una chiave rimossa successivamente può invece essere
 * ancora presente nello snapshot.
 * </p>
 *
 * <p>
 * Per la vista dei valori e per quella delle entry, il valore associato alla
 * chiave viene recuperato dalla mappa durante l'utilizzo dell'iteratore o
 * dell'entry. Una modifica avvenuta dopo la costruzione dello snapshot può
 * quindi influenzare il valore osservato. La vista rimane dinamica, mentre
 * l'elenco delle chiavi appartenente a uno specifico iteratore rimane quello
 * stabilito al momento della sua creazione.
 * </p>
 *
 * <p>
 * Gli iteratori del progetto non sono <em>fail-fast</em>: non controllano un
 * contatore delle modifiche e non garantiscono il lancio di una
 * {@code ConcurrentModificationException} quando la mappa viene modificata
 * esternamente. Lo snapshot permette all'iteratore di proseguire, ma non
 * trasforma l'intera sequenza di operazioni in un'operazione atomica.
 * </p>
 *
 * <p>
 * Se più thread condividono la stessa mappa e almeno uno di essi la modifica,
 * il client deve quindi utilizzare una forma di coordinamento esterno comune
 * quando ha bisogno di osservare una sequenza completamente consistente.
 * Non è corretto considerare tutte le viste automaticamente thread-safe
 * soltanto perché la struttura sottostante è una {@code Hashtable}.
 * </p>
 *
 * @see HMap
 * @see HSet
 * @see HIterator
 * @see MapAdapter
 */
public interface HCollection {
    /**
     * Restituisce il numero di elementi presenti nella collezione.
     *
     * <p>Nelle viste di {@link MapAdapter} il valore è sempre letto dalla
     * mappa backing. Non viene memorizzata una dimensione separata: questa
     * scelta evita disallineamenti e rende immediatamente visibili inserimenti
     * e rimozioni effettuati attraverso la mappa o un'altra vista.</p>
     *
     * @return il numero corrente di elementi; è zero quando la collezione è
     *         vuota
     */
    int size();

    /**
     * Verifica se la collezione non contiene elementi.
     *
     * <p>Il risultato è equivalente a {@code size() == 0}. Le viste correnti
     * delegano alla mappa backing, evitando un'iterazione completa.</p>
     *
     * @return {@code true} se la collezione non contiene elementi;
     *         {@code false} altrimenti
     */
    boolean isEmpty();

    /**
     * Verifica se è presente almeno un elemento corrispondente all'argomento.
     *
     * <p>La corrispondenza segue la formula J2SE 1.4.2
     * {@code object == null ? element == null : object.equals(element)}. È
     * importante che, quando l'argomento non è {@code null}, {@code equals}
     * sia invocato sull'oggetto cercato e non sull'elemento memorizzato: oggetti
     * con uguaglianza asimmetrica rendono osservabile la direzione del
     * confronto.</p>
     *
     * <p>Le viste applicano il controllo al proprio tipo di elemento:
     * {@code keySet} cerca una chiave, {@code values} cerca un valore ed
     * {@code entrySet} richiede una {@link HMap.Entry} con chiave e valore
     * entrambi corrispondenti.</p>
     *
     * @param object elemento del quale verificare la presenza
     * @return {@code true} se almeno un elemento corrisponde all'argomento;
     *         {@code false} altrimenti
     * @throws ClassCastException se il tipo dell'argomento impedisce il
     *         confronto nell'implementazione concreta
     * @throws NullPointerException se l'argomento è {@code null} e la vista
     *         concreta non ammette la ricerca di {@code null}; nelle viste
     *         correnti accade per {@code keySet} e {@code values}
     */
    boolean contains(Object object);

    /**
     * Restituisce un iteratore sugli elementi della collezione.
     *
     * <p>Non è garantito alcun ordine. In {@link MapAdapter} ogni iteratore
     * acquisisce alla costruzione uno snapshot delle chiavi e produce da esso
     * chiavi, valori oppure entry. La scelta permette di implementare
     * {@link HIterator#remove()} usando soltanto {@code Hashtable},
     * {@code Enumeration} e array disponibili in CLDC 1.1, senza dipendere da
     * {@code java.util.Iterator}.</p>
     *
     * <p>Gli iteratori sono indipendenti e non fail-fast. La modifica
     * strutturale supportata durante un attraversamento è la rimozione tramite
     * lo stesso iteratore; gli effetti di modifiche strutturali esterne non
     * fanno parte del contratto garantito.</p>
     *
     * @return un nuovo {@link HIterator} posizionato prima del primo elemento
     */
    HIterator iterator();

    /**
     * Restituisce un nuovo array contenente tutti gli elementi prodotti
     * dall'iteratore della collezione.
     *
     * <p>L'array è indipendente dalla vista: modificarne le celle non modifica
     * la mappa backing e le successive modifiche della mappa non ne cambiano la
     * lunghezza o il contenuto. Gli oggetti contenuti non vengono clonati, ma
     * copiati per riferimento. L'ordine degli elementi coincide con quello
     * dell'iteratore e non è altrimenti specificato.</p>
     *
     * <p>Viene sempre creato un {@code Object[]} di dimensione pari a
     * {@link #size()}; questa scelta non richiede generics o reflection ed è
     * quindi compatibile con CLDC 1.1.</p>
     *
     * @return un nuovo {@code Object[]} contenente tutti gli elementi della
     *         collezione
     */
    Object[] toArray();

    /**
     * Copia gli elementi della collezione nell'array specificato quando esso
     * dispone di spazio sufficiente, oppure crea un nuovo {@code Object[]}
     * quando l'argomento è esattamente un {@code Object[]} troppo piccolo.
     *
     * <p>Se l'array viene riutilizzato ed è più grande del numero di elementi,
     * la cella immediatamente successiva all'ultimo elemento copiato viene
     * impostata a {@code null}; le celle ancora successive non vengono
     * modificate. Gli elementi sono copiati nell'ordine dell'iteratore.</p>
     *
     * <p><strong>Scelta CLDC:</strong> J2SE 1.4.2 richiede, per un array
     * tipizzato troppo piccolo, la creazione di un nuovo array con lo stesso
     * tipo runtime. Una creazione generica di questo tipo richiederebbe
     * reflection, esclusa dal vincolo CLDC 1.1 del progetto. Per non introdurre
     * API non disponibili, l'implementazione crea un nuovo array soltanto
     * quando il tipo runtime dell'argomento è {@code Object[]}; un array
     * tipizzato troppo piccolo provoca {@code ArrayStoreException}. Un array
     * tipizzato compatibile e sufficientemente grande viene invece riutilizzato
     * regolarmente.</p>
     *
     * @param array array di destinazione proposto dal chiamante
     * @return {@code array} se dispone di spazio sufficiente; altrimenti un
     *         nuovo {@code Object[]} della dimensione richiesta, purché il
     *         tipo runtime dell'argomento sia esattamente {@code Object[]}
     * @throws NullPointerException se {@code array} è {@code null}
     * @throws ArrayStoreException se l'array tipizzato è troppo piccolo oppure
     *         se il tipo runtime di una sua cella non può contenere un elemento
     *         della collezione
     */
    Object[] toArray(Object[] array);

    /**
     * Richiede l'aggiunta dell'elemento alla collezione.
     *
     * <p>È un'operazione opzionale. Le tre implementazioni correnti sono viste
     * di una mappa e non la supportano: aggiungere una sola chiave o un solo
     * valore non fornisce l'altra metà del mapping, mentre l'aggiunta arbitraria
     * di una entry non è prevista dal contratto della vista di una mappa.
     * Pertanto esse sollevano sempre
     * {@link MapAdapter.HUnsupportedOperationException} senza modificare la
     * mappa.</p>
     *
     * @param object elemento di cui si richiede l'aggiunta
     * @return {@code true} se un'implementazione che supporta l'operazione
     *         modifica la collezione; {@code false} se il suo contratto ammette
     *         l'elemento ma la collezione non cambia
     * @throws MapAdapter.HUnsupportedOperationException se l'implementazione
     *         non supporta l'aggiunta; è sempre il caso delle viste correnti
     * @throws ClassCastException se il tipo dell'elemento ne impedisce
     *         l'aggiunta in un'implementazione che supporta l'operazione
     * @throws NullPointerException se l'elemento è {@code null} e
     *         un'implementazione che supporta l'operazione non ammette
     *         elementi nulli
     * @throws IllegalArgumentException se una proprietà dell'elemento ne
     *         impedisce l'aggiunta in un'implementazione che la supporta
     */
    boolean add(Object object);

    /**
     * Rimuove una singola occorrenza corrispondente all'argomento, se presente.
     *
     * <p>La corrispondenza segue la formula
     * {@code object == null ? element == null : object.equals(element)}. Nella
     * vista dei valori viene eliminato un solo mapping anche se il valore
     * compare più volte; nella vista delle chiavi viene eliminato il mapping
     * della chiave; nella vista delle entry sono richiesti chiave e valore
     * corrispondenti. Tutte le viste correnti supportano l'operazione.</p>
     *
     * @param object elemento del quale rimuovere una singola occorrenza
     * @return {@code true} se la collezione e la mappa backing sono state
     *         modificate; {@code false} se non esiste una corrispondenza
     * @throws ClassCastException se il tipo dell'argomento impedisce il
     *         confronto nell'implementazione concreta
     * @throws NullPointerException se l'argomento è {@code null} e la vista
     *         concreta non ne ammette la ricerca; nelle viste correnti accade
     *         per {@code keySet}
     * @throws MapAdapter.HUnsupportedOperationException se una futura
     *         implementazione dichiara la rimozione non supportata
     */
    boolean remove(Object object);

    /**
     * Verifica che la collezione contenga tutti gli elementi della collezione
     * specificata.
     *
     * <p>L'implementazione corrente attraversa {@code collection} e invoca
     * {@link #contains(Object)} sulla collezione ricevente per ogni elemento.
     * Restituisce immediatamente {@code false} alla prima assenza; una sorgente
     * vuota produce {@code true}. Il metodo non modifica nessuna delle due
     * collezioni.</p>
     *
     * @param collection collezione contenente gli elementi da verificare
     * @return {@code true} se ogni elemento di {@code collection} è contenuto
     *         nella collezione ricevente; {@code false} altrimenti
     * @throws NullPointerException se {@code collection} è {@code null}, oppure
     *         se contiene {@code null} e la vista ricevente non ne ammette la
     *         ricerca
     * @throws ClassCastException se il tipo di un elemento della sorgente
     *         impedisce il confronto con la vista ricevente
     */
    boolean containsAll(HCollection collection);

    /**
     * Richiede l'aggiunta di tutti gli elementi della collezione specificata.
     *
     * <p>L'implementazione comune itera sulla sorgente e richiama
     * {@link #add(Object)} per ogni elemento. Poiché le viste correnti non
     * supportano {@code add}, una sorgente non vuota causa
     * {@link MapAdapter.HUnsupportedOperationException} al primo elemento. Una
     * sorgente vuota non esegue alcuna aggiunta e restituisce {@code false}.
     * Questa distinzione deriva dall'esecuzione effettiva dell'operazione
     * opzionale e viene mantenuta intenzionalmente.</p>
     *
     * <p>Come nel contratto J2SE, il comportamento non è definito se la sorgente
     * viene modificata durante l'operazione. Il caso di auto-aggiunta con
     * collezione non vuota non è supportato dalle viste correnti.</p>
     *
     * @param collection collezione i cui elementi dovrebbero essere aggiunti
     * @return {@code true} se la collezione ricevente viene modificata;
     *         {@code false} se la sorgente è vuota o nessuna aggiunta produce
     *         una modifica
     * @throws NullPointerException se {@code collection} è {@code null}
     * @throws MapAdapter.HUnsupportedOperationException se la sorgente contiene
     *         almeno un elemento e l'aggiunta non è supportata; è il
     *         comportamento delle viste correnti
     * @throws ClassCastException se il tipo di un elemento ne impedisce
     *         l'aggiunta in un'implementazione che la supporta
     * @throws IllegalArgumentException se una proprietà di un elemento ne
     *         impedisce l'aggiunta in un'implementazione che la supporta
     */
    boolean addAll(HCollection collection);

    /**
     * Rimuove dalla collezione ricevente tutti gli elementi contenuti nella
     * collezione specificata.
     *
     * <p>L'implementazione corrente attraversa la collezione ricevente, prova
     * ogni elemento con {@code collection.contains(element)} e usa
     * {@link HIterator#remove()} in caso di corrispondenza. Questa scelta è
     * necessaria per aggiornare correttamente la mappa backing e per rimuovere
     * tutte le occorrenze corrispondenti dalla vista dei valori.</p>
     *
     * <p>Passare la stessa vista come argomento è supportato: tutti gli elementi
     * vengono riconosciuti e rimossi, perciò la mappa viene svuotata.</p>
     *
     * @param collection collezione che determina quali elementi rimuovere
     * @return {@code true} se almeno un elemento viene rimosso;
     *         {@code false} se la collezione ricevente non cambia
     * @throws NullPointerException se {@code collection} è {@code null}, oppure
     *         se una sua operazione {@code contains} non ammette un elemento
     *         {@code null}
     * @throws ClassCastException se un elemento non è confrontabile con la
     *         collezione specificata
     * @throws MapAdapter.HUnsupportedOperationException se una futura
     *         implementazione non supporta la rimozione
     */
    boolean removeAll(HCollection collection);

    /**
     * Conserva nella collezione ricevente soltanto gli elementi contenuti nella
     * collezione specificata.
     *
     * <p>L'implementazione corrente attraversa la collezione ricevente e usa
     * {@link HIterator#remove()} quando
     * {@code collection.contains(element)} restituisce {@code false}. La
     * rimozione tramite iteratore mantiene sincronizzata la vista con la mappa
     * backing e gestisce correttamente i valori duplicati.</p>
     *
     * <p>Passare la stessa vista come argomento non produce modifiche e
     * restituisce {@code false}, perché ogni elemento è necessariamente
     * contenuto nella collezione di selezione.</p>
     *
     * @param collection collezione che determina quali elementi conservare
     * @return {@code true} se almeno un elemento viene rimosso;
     *         {@code false} se la collezione ricevente non cambia
     * @throws NullPointerException se {@code collection} è {@code null}, oppure
     *         se una sua operazione {@code contains} non ammette un elemento
     *         {@code null}
     * @throws ClassCastException se un elemento non è confrontabile con la
     *         collezione specificata
     * @throws MapAdapter.HUnsupportedOperationException se una futura
     *         implementazione non supporta la rimozione
     */
    boolean retainAll(HCollection collection);

    /**
     * Rimuove tutti gli elementi dalla collezione.
     *
     * <p>Tutte le viste correnti delegano a {@link HMap#clear()}; di
     * conseguenza viene svuotata l'intera mappa backing, non soltanto un oggetto
     * vista separato. Le altre viste ottenute in precedenza osservano
     * immediatamente lo stato vuoto.</p>
     *
     * @throws MapAdapter.HUnsupportedOperationException se una futura
     *         implementazione non supporta lo svuotamento; le viste correnti lo
     *         supportano sempre
     */
    void clear();

    /**
     * Confronta la collezione con l'oggetto specificato.
     *
     * <p>L'interfaccia dichiara esplicitamente il metodo ereditato da
     * {@code Object} per rendere completo il contratto locale di
     * {@code Collection}. Non impone tuttavia a ogni collezione una singola
     * semantica di uguaglianza: J2SE 1.4.2 lascia alle sottocategorie la
     * definizione di equivalenza strutturale.</p>
     *
     * <p>Nel progetto {@code keySet} ed {@code entrySet}, in quanto
     * {@link HSet}, sono uguali a un altro {@code HSet} con la stessa dimensione
     * e gli stessi elementi, indipendentemente dall'ordine. La vista
     * {@code values} non ridefinisce l'uguaglianza e risulta uguale soltanto a
     * se stessa. Il metodo deve essere riflessivo, simmetrico, transitivo,
     * consistente e restituire {@code false} per {@code null}.</p>
     *
     * @param object oggetto con il quale confrontare la collezione
     * @return {@code true} se l'oggetto è uguale alla collezione secondo la
     *         semantica dell'implementazione concreta; {@code false} altrimenti
     */
    boolean equals(Object object);

    /**
     * Restituisce il codice hash della collezione.
     *
     * <p>Il valore deve essere coerente con {@link #equals(Object)}: due
     * collezioni considerate uguali devono produrre lo stesso codice hash.
     * Nelle viste {@link HSet} correnti il codice è la somma dei codici hash
     * degli elementi, perciò non dipende dall'ordine di iterazione. La vista
     * {@code values}, che conserva l'uguaglianza per identità, conserva anche
     * il codice hash per identità ereditato da {@code Object}.</p>
     *
     * @return il codice hash della collezione
     */
    int hashCode();
}
