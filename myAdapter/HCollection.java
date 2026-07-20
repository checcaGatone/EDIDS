package myAdapter;

/**
 * Contratto locale, non generico, corrispondente all'interfaccia
 * {@code java.util.Collection} del Java 2 Collections Framework di
 * J2SE 1.4.2.
 *
 * <h2>Descrizione e responsabilità</h2>
 *
 * <p>L'interfaccia rappresenta un gruppo di oggetti, denominati elementi,
 * sul quale è possibile eseguire interrogazioni, iterazioni, conversioni in
 * array e operazioni di modifica. Non memorizza direttamente alcun dato e non
 * impone una specifica struttura dati: definisce esclusivamente il contratto
 * che le implementazioni devono rispettare.</p>
 *
 * <p>Nel progetto EDIDS le implementazioni concrete sono le viste backed
 * restituite da {@link HMap#keySet()}, {@link HMap#values()} e
 * {@link HMap#entrySet()}. Tali viste sono realizzate internamente da
 * {@link MapAdapter} e delegano la memorizzazione alla
 * {@code java.util.Hashtable} adattata. Di conseguenza, questa interfaccia non
 * possiede né duplica chiavi o valori: ogni operazione osserva o modifica la
 * mappa dalla quale la vista è stata ottenuta.</p>
 *
 * <h2>Motivazioni delle scelte progettuali</h2>
 *
 * <table border="1">
 * <caption>Scelte adottate per HCollection</caption>
 * <tr>
 * <th>Scelta</th>
 * <th>Motivazione</th>
 * <th>Conseguenza osservabile</th>
 * </tr>
 * <tr>
 * <td>Nome {@code HCollection}</td>
 * <td>Evita collisioni con {@code java.util.Collection} del JDK usato per
 * compilare e provare il progetto.</td>
 * <td>La libreria adattata deve riferirsi al tipo locale del package
 * {@code myAdapter}.</td>
 * </tr>
 * <tr>
 * <td>Elementi rappresentati da {@code Object}</td>
 * <td>CLDC 1.1 e J2SE 1.4.2 precedono l'introduzione dei generics.</td>
 * <td>I controlli di tipo avvengono a runtime e gli array restituiti hanno
 * tipo base {@code Object[]}.</td>
 * </tr>
 * <tr>
 * <td>Separazione tra interfaccia e memorizzazione</td>
 * <td>La dipendenza da {@code Hashtable} deve rimanere confinata
 * nell'adapter.</td>
 * <td>Il contratto può essere usato senza esporre l'adaptee e senza dipendere
 * dal Java Collections Framework moderno.</td>
 * </tr>
 * <tr>
 * <td>Viste backed</td>
 * <td>Le viste di una {@code Map} J2SE devono riflettere la mappa e non essere
 * copie indipendenti.</td>
 * <td>Le modifiche supportate su una vista aggiornano la mappa; le modifiche
 * della mappa sono visibili dalle viste già ottenute.</td>
 * </tr>
 * <tr>
 * <td>Iteratore locale {@link HIterator}</td>
 * <td>L'interfaccia {@code java.util.Iterator} non appartiene al sottoinsieme
 * usato dall'adapter CLDC 1.1.</td>
 * <td>L'attraversamento e la rimozione avvengono senza importare l'iteratore
 * del framework moderno.</td>
 * </tr>
 * <tr>
 * <td>Operazioni opzionali esplicite</td>
 * <td>La compatibilità con J2SE 1.4.2 richiede che i metodi siano presenti
 * anche quando una specifica vista non può supportarli.</td>
 * <td>Le rimozioni sono supportate; le aggiunte alle viste della mappa
 * sollevano {@link MapAdapter.HUnsupportedOperationException}.</td>
 * </tr>
 * <tr>
 * <td>Eccezioni locali per stato e operazioni non supportate</td>
 * <td>{@code IllegalStateException} e {@code UnsupportedOperationException}
 * non sono disponibili in CLDC 1.1.</td>
 * <td>Il progetto usa {@link MapAdapter.HIllegalStateException} e
 * {@link MapAdapter.HUnsupportedOperationException}.</td>
 * </tr>
 * </table>
 *
 * <h2>Implementazioni presenti nel progetto</h2>
 *
 * <table border="1">
 * <caption>Semantica delle viste che implementano HCollection</caption>
 * <tr>
 * <th>Vista</th>
 * <th>Tipo degli elementi</th>
 * <th>Duplicati</th>
 * <th>Uguaglianza</th>
 * <th>Effetto della rimozione</th>
 * </tr>
 * <tr>
 * <td>{@code keySet()}</td>
 * <td>Chiavi della mappa</td>
 * <td>Non ammessi, per definizione di mappa</td>
 * <td>Semantica di {@link HSet}</td>
 * <td>Elimina il mapping associato alla chiave</td>
 * </tr>
 * <tr>
 * <td>{@code values()}</td>
 * <td>Valori della mappa</td>
 * <td>Ammessi quando chiavi diverse hanno valori uguali</td>
 * <td>Identità dell'oggetto vista; non usa la semantica di insieme</td>
 * <td>Elimina un solo mapping corrispondente per {@link #remove(Object)}</td>
 * </tr>
 * <tr>
 * <td>{@code entrySet()}</td>
 * <td>Mapping rappresentati da {@link HMap.Entry}</td>
 * <td>Non ammessi per la stessa coppia chiave-valore</td>
 * <td>Semantica di {@link HSet}</td>
 * <td>Elimina il mapping solo se chiave e valore coincidono</td>
 * </tr>
 * </table>
 *
 * <h2>Supporto delle operazioni opzionali</h2>
 *
 * <table border="1">
 * <caption>Operazioni opzionali nelle viste di MapAdapter</caption>
 * <tr>
 * <th>Operazione</th>
 * <th>keySet</th>
 * <th>values</th>
 * <th>entrySet</th>
 * <th>Ragione</th>
 * </tr>
 * <tr>
 * <td>{@link #add(Object)}</td>
 * <td>Non supportata</td>
 * <td>Non supportata</td>
 * <td>Non supportata</td>
 * <td>Da un solo elemento della vista non è possibile ricostruire sempre una
 * coppia chiave-valore valida.</td>
 * </tr>
 * <tr>
 * <td>{@link #addAll(HCollection)}</td>
 * <td>Non supportata se la sorgente contiene elementi</td>
 * <td>Non supportata se la sorgente contiene elementi</td>
 * <td>Non supportata se la sorgente contiene elementi</td>
 * <td>Delega logicamente ad {@code add}; con sorgente vuota non tenta
 * aggiunte e restituisce {@code false}.</td>
 * </tr>
 * <tr>
 * <td>{@link #remove(Object)}</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>La rimozione identifica un mapping esistente senza doverne creare uno
 * nuovo.</td>
 * </tr>
 * <tr>
 * <td>{@link #removeAll(HCollection)}</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>Usa {@link HIterator#remove()} e aggiorna la mappa backing.</td>
 * </tr>
 * <tr>
 * <td>{@link #retainAll(HCollection)}</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>Rimuove mediante l'iteratore gli elementi non presenti nella
 * collezione di selezione.</td>
 * </tr>
 * <tr>
 * <td>{@link #clear()}</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>Supportata</td>
 * <td>Svuota direttamente la mappa backing.</td>
 * </tr>
 * </table>
 *
 * <h2>Politica relativa a null</h2>
 *
 * <p>L'interfaccia non impone un divieto universale di passare {@code null}:
 * il comportamento dipende dall'operazione e dalla vista concreta, come
 * consentito dal contratto di {@code Collection}. La distinzione è necessaria
 * perché la {@code Hashtable} backing non accetta chiavi o valori
 * {@code null}, mentre alcune operazioni di confronto possono semplicemente
 * stabilire che {@code null} non è presente.</p>
 *
 * <table border="1">
 * <caption>Comportamento corrente con argomenti null</caption>
 * <tr>
 * <th>Operazione</th>
 * <th>keySet</th>
 * <th>values</th>
 * <th>entrySet</th>
 * </tr>
 * <tr>
 * <td>{@code contains(null)}</td>
 * <td>{@code NullPointerException}</td>
 * <td>{@code NullPointerException}</td>
 * <td>{@code false}</td>
 * </tr>
 * <tr>
 * <td>{@code remove(null)}</td>
 * <td>{@code NullPointerException}</td>
 * <td>{@code false}</td>
 * <td>{@code false}</td>
 * </tr>
 * <tr>
 * <td>Operazione bulk con collezione {@code null}</td>
 * <td>{@code NullPointerException}</td>
 * <td>{@code NullPointerException}</td>
 * <td>{@code NullPointerException}</td>
 * </tr>
 * <tr>
 * <td>{@code toArray(null)}</td>
 * <td>{@code NullPointerException}</td>
 * <td>{@code NullPointerException}</td>
 * <td>{@code NullPointerException}</td>
 * </tr>
 * </table>
 *
 * <h2>Ordine, uguaglianza e concorrenza</h2>
 *
 * <p>Questa interfaccia non garantisce alcun ordine di iterazione. Le viste
 * correnti dipendono dall'ordine della {@code Hashtable}, che non deve essere
 * interpretato come ordine di inserimento. Gli iteratori di
 * {@link MapAdapter} acquisiscono uno snapshot delle chiavi e non sono
 * fail-fast; durante l'attraversamento la rimozione portabile è quella eseguita
 * tramite {@link HIterator#remove()}.</p>
 *
 * <p>La {@code Hashtable} sincronizza le proprie operazioni elementari, ma
 * l'interfaccia non promette che iterazioni o operazioni bulk composte siano
 * atomiche. Un chiamante che condivide una vista tra thread deve quindi
 * coordinare esternamente le sequenze di operazioni.</p>
 *
 * <p>Per una collezione generale J2SE 1.4.2 non impone una semantica di
 * uguaglianza basata sul contenuto. Nel progetto tale semantica è implementata
 * soltanto dalle viste che sono anche {@link HSet}; la vista dei valori
 * conserva invece il comportamento di identità ereditato da {@code Object}.
 * Ogni implementazione che ridefinisce {@link #equals(Object)} deve ridefinire
 * coerentemente anche {@link #hashCode()}.</p>
 *
 * @see HMap
 * @see HMap.Entry
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
