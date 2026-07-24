package myAdapter;

/**
 * Interfaccia  che riproduce
 * {@code java.util.Iterator} di J2SE 1.4.2.
 *
 * <p>
 * {@code HIterator} permette di attraversare gli elementi di una
 * {@link HCollection} uno alla volta, senza esporre la rappresentazione
 * interna usata dall'implementazione. A differenza della
 * {@code Enumeration} di CLDC 1.1, che fornisce solo la scansione in sola
 * lettura, {@code HIterator} espone anche l'operazione opzionale
 * {@link #remove()}, che consente di eliminare dalla collezione sottostante
 * l'ultimo elemento restituito.
 * </p>
 *
 * <p>
 * Non viene definita alcuna interfaccia locale
 * {@code HConcurrentModificationException}: CLDC 1.1 non mette a
 * disposizione {@code java.util.ConcurrentModificationException} e il
 * progetto non introduce un'eccezione equivalente, per cui gli iteratori
 * restituiti da {@link MapAdapter} non sono fail-fast. Il comportamento di
 * un iteratore quando la collezione viene modificata al di fuori
 * dell'iteratore stesso (cioè non tramite {@link #remove()}) non è quindi
 * specificato dal contratto di questa interfaccia; ogni implementazione
 * può comunque documentare un comportamento più preciso, come
 * avviene per {@code MapAdapter}.
 * </p>
 *
 * <p>
 * Un {@code HIterator} è posizionato, alla propria creazione, prima del
 * primo elemento della collezione. Ogni chiamata a {@link #next()} fa
 * avanzare l'iteratore restituendo l'elemento corrente, mentre
 * {@link #hasNext()} permette di verificare, senza modificare lo stato
 * dell'iteratore, se esiste almeno un altro elemento da restituire.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see HCollection
 * @see MapAdapter
 */
public interface HIterator {

    /**
     * Verifica se l'iterazione ha ancora almeno un elemento da restituire.
     *
     * <p>
     * La chiamata non modifica lo stato dell'iteratore: può quindi essere
     * invocata più volte di seguito, o alternata a {@link #next()}, senza
     * effetti collaterali sull'attraversamento.
     * </p>
     *
     * @return {@code true} se {@link #next()} restituirebbe un elemento
     *         anziché lanciare un'eccezione; {@code false} altrimenti
     */
    boolean hasNext();

    /**
     * Restituisce il prossimo elemento dell'iterazione e fa avanzare
     * l'iteratore di una posizione.
     *
     * <p>
     * Nelle viste di {@link MapAdapter} l'elemento restituito dipende dal
     * tipo di vista da cui l'iteratore è stato ottenuto: una chiave per
     * {@code keySet()}, un valore per {@code values()}, oppure una
     * {@link HMap.Entry} per {@code entrySet()}.
     * </p>
     *
     * @return il prossimo elemento dell'iterazione
     * @throws java.util.NoSuchElementException se l'iterazione non ha
     * ulteriori elementi, cioè se {@link #hasNext()}
     * restituirebbe {@code false}
     */
    Object next();

    /**
     * Rimuove dalla collezione sottostante l'ultimo elemento restituito da
     * questo iteratore. È un'operazione opzionale.
     *
     * <p>
     * Il metodo può essere chiamato una sola volta per ogni chiamata a
     * {@link #next()}: non è ammesso invocare {@code remove()} prima di
     * aver mai chiamato {@code next()}, né richiamarlo due volte di
     * seguito senza un {@code next()} intermedio. Negli iteratori di
     * {@link MapAdapter} questo vincolo è segnalato con
     * {@link MapAdapter.HIllegalStateException}, usata al posto di
     * {@code java.lang.IllegalStateException} per restare coerenti con lo
     * stile delle altre eccezioni locali dell'adapter.
     * </p>
     *
     * @throws MapAdapter.HIllegalStateException se {@code next()} non è
     * ancora stato chiamato, oppure se {@code remove()}
     * è già stato invocato dopo l'ultima chiamata a {@code next()}
     */
    void remove();
}