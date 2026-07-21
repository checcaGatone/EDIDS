package myAdapter;

/**
/**
 * Interfaccia Target che riproduce il contratto di
 * {@code java.util.Collection} di J2SE 1.4.2.
 *
 * <p>Una collezione rappresenta un gruppo di oggetti, chiamati elementi.
 * L'interfaccia definisce le operazioni comuni che permettono di conoscere
 * il numero di elementi presenti, verificare la presenza di un oggetto,
 * ottenere un iteratore, trasformare il contenuto in un array e, quando
 * consentito, modificare la collezione.</p>
 *
 * <p>{@code HCollection} non stabilisce direttamente se gli elementi siano
 * ordinati o se possano comparire più volte. Queste caratteristiche dipendono
 * dalla sotto-interfaccia utilizzata e dall'implementazione concreta. Come
 * nell'interfaccia originale di J2SE 1.4.2, i metodi utilizzano riferimenti
 * di tipo {@code Object}, poiché la versione considerata non prevede ancora
 * l'uso dei tipi generici.</p>
 *
 * <p><b>Ruolo nell'architettura dell'adapter:</b>
 * {@code HCollection} costituisce il contratto comune delle collezioni
 * utilizzate dal progetto, ma non rappresenta direttamente una mappa.
 * Per questo motivo {@link HMap} non estende questa interfaccia: una mappa
 * memorizza associazioni tra chiavi e valori, mentre una collezione contiene
 * singoli elementi.</p>
 *
 * <p>{@link HSet} estende {@code HCollection} aggiungendo il vincolo di
 * unicità degli elementi. Di conseguenza, {@link HMap#keySet()} e
 * {@link HMap#entrySet()} restituiscono oggetti di tipo {@code HSet}.
 * Il metodo {@link HMap#values()}, invece, restituisce una
 * {@code HCollection}, perché valori associati a chiavi differenti possono
 * essere uguali e quindi possono comparire più volte.</p>
 *
 * <p>Gli elementi delle collezioni possono essere attraversati mediante
 * {@link HIterator}. L'iteratore permette di esaminare gli elementi senza
 * esporre la rappresentazione interna usata dall'adapter.</p>
 *
 * <p><b>Viste collegate alla mappa:</b>
 * le collezioni restituite da {@code keySet()}, {@code values()} ed
 * {@code entrySet()} sono viste sostenute dalla mappa originale. Non
 * contengono quindi una copia indipendente dei suoi dati. Una modifica
 * effettuata sulla mappa si riflette nella vista e, allo stesso modo, le
 * operazioni di rimozione eseguite attraverso la vista modificano la mappa
 * sottostante.</p>
 *
 * <p>Le viste permettono le operazioni di rimozione previste dal contratto,
 * comprese {@code remove}, {@code removeAll}, {@code retainAll} e
 * {@code clear}. Le operazioni {@code add} e {@code addAll}, invece, non
 * sono supportate dalle viste della mappa, perché da una sola chiave, da un
 * solo valore o da una singola entry non è sempre possibile costruire in
 * modo corretto una nuova associazione. In questi casi viene lanciata una
 * {@link UnsupportedOperationException}, coerentemente con il contratto
 * delle viste di {@code java.util.Map}.</p>
 *
 * <p><b>Gestione degli elementi null:</b>
 * l'interfaccia {@code HCollection}, considerata in modo generale, permette
 * alle implementazioni concrete di stabilire eventuali limitazioni sugli
 * elementi accettati. Nel caso di {@link MapAdapter}, le viste dipendono
 * dalla {@code Hashtable} di CLDC 1.1 usata come adaptee. Poiché tale
 * struttura non ammette chiavi o valori {@code null}, nemmeno le viste della
 * mappa possono contenere elementi nulli. I singoli metodi specificano nei
 * propri Javadoc i casi in cui viene lanciata una
 * {@link NullPointerException}.</p>
 *
 * <p><b>Ordine, uguaglianza e concorrenza:</b>
 * {@code HCollection} non garantisce un particolare ordine di iterazione.
 * Per le viste di {@code MapAdapter}, l'ordine dipende dalla
 * {@code Hashtable} sottostante e non deve quindi essere considerato
 * stabile.</p>
 *
 * <p>L'interfaccia non aggiunge un contratto generale di uguaglianza oltre
 * a quello definito da {@link Object}. Le viste che implementano
 * {@link HSet}, cioè {@code KeySet} ed {@code EntrySet}, confrontano il
 * proprio contenuto secondo la semantica degli insiemi. La vista
 * {@code Values}, invece, mantiene il normale confronto per identità
 * ereditato da {@code Object}, poiché il contratto generale di
 * {@code Collection} non impone un confronto basato sul contenuto.</p>
 *
 * <p>L'implementazione non fornisce garanzie di thread safety per le
 * operazioni complessive eseguite sulle viste. La sincronizzazione dei
 * singoli metodi della {@code Hashtable} non rende automaticamente atomiche
 * le operazioni formate da più passaggi. Gli iteratori utilizzano uno
 * snapshot delle chiavi presenti al momento della loro creazione e non
 * implementano il comportamento fail-fast: eventuali modifiche successive
 * non vengono quindi segnalate mediante
 * {@code ConcurrentModificationException}.</p>
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
