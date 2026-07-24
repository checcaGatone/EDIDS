package myAdapter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

/**
 * Classe Adapter che adatta {@code java.util.Hashtable} di CLDC 1.1
 * all'interfaccia {@link HMap}, riproducendo il comportamento di
 * {@code java.util.Map}.
 *
 * <p>
 * L'adaptee usato è una {@code Hashtable}, struttura dati
 * associativa disponibile in CLDC 1.1. Tutte le operazioni della mappa
 * vengono quindi delegate, direttamente o indirettamente, ai metodi della
 * {@code Hashtable} interna {@code table}. Non viene mantenuta nessuna
 * struttura dati ausiliaria per chiavi, valori o entry: le tre viste
 * ({@link #keySet()}, {@link #values()}, {@link #entrySet()}) leggono
 * sempre lo stato corrente della tabella, così da restituire dati sempre
 * coerenti anche a fronte di modifiche esterne alla mappa.
 * </p>
 *
 * <p>
 * Utilizzo le eccezioni definite al proprio interno; non controllate
 * {@link HUnsupportedOperationException} e {@link HIllegalStateException},
 * in modo da mantenere lo stesso significato
 * semantico previsto dal contratto originale pur restando dentro i vincoli
 * della piattaforma.
 * </p>
 *
 * <p>
 * Le viste restituite da {@code keySet()}, {@code values()} ed
 * {@code entrySet()} sono backed dalla mappa: non sono copie, e
 * ogni modifica strutturale eseguita tramite la vista (rimozione singola,
 * {@code removeAll}, {@code retainAll}, {@code clear}) si ripercuote sulla
 * {@code Hashtable} sottostante e viceversa. Le operazioni di inserimento
 * ({@code add}, {@code addAll}) non sono invece supportate dalle viste, in
 * quanto da un solo elemento (chiave, valore o entry) non è in generale
 * possibile ricostruire un'associazione chiave-valore valida; in questi
 * casi viene lanciata {@link HUnsupportedOperationException}.
 * </p>
 *
 * <p>
 * Ogni vista viene creata una sola volta e mantenuta nei campi
 * {@code keys}, {@code values} ed {@code entries}: Le tre viste vengono create al primo accesso e successivamente
riutilizzate. Questa è una scelta interna di MapAdapter; il requisito
contrattuale è che le viste siano collegate alla mappa sottostante,
non che mantengano sempre la stessa identità.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see HMap
 * @see HCollection
 * @see HSet
 * @see HIterator
 */
public class MapAdapter implements HMap {

    /**
     * Eccezione non controllata lanciata al posto di
     * {@code java.lang.UnsupportedOperationException}, che non è presente
     * in CLDC 1.1.
     *
     * <p>
     * Viene sollevata dalle viste della mappa quando si tenta di invocare
     * un'operazione opzionale non supportata, in particolare
     * {@link HCollection#add(Object)} e {@link HCollection#addAll(HCollection)}.
     * </p>
     */
    public static final class HUnsupportedOperationException
            extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Costruisce l'eccezione senza un messaggio di dettaglio.
         */
        public HUnsupportedOperationException() {
        }

        /**
         * Costruisce l'eccezione con un messaggio che descrive l'operazione
         * non supportata.
         *
         * @param message messaggio di dettaglio associato all'eccezione
         */
        public HUnsupportedOperationException(String message) {
            super(message);
        }
    }

    /**
     * Eccezione non controllata lanciata al posto di
     * {@code java.lang.IllegalStateException} per segnalare una chiamata a
     * {@link HIterator#remove()} non preceduta da una corrispondente
     * chiamata a {@code next()}, oppure una seconda rimozione consecutiva
     * senza un {@code next()} intermedio.
     */
    public static final class HIllegalStateException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Costruisce l'eccezione senza un messaggio di dettaglio.
         */
        public HIllegalStateException() {
        }

        /**
         * Costruisce l'eccezione con un messaggio di dettaglio.
         *
         * @param message messaggio di dettaglio associato all'eccezione
         */
        public HIllegalStateException(String message) {
            super(message);
        }
    }

    /** Hashtable di CLDC 1.1 su cui è costruito l'intero adapter. */
    private final Hashtable table;

    /** Vista delle chiavi, creata al primo accesso da {@link #keySet()}. */
    private HSet keys;

    /** Vista dei valori, creata al primo accesso da {@link #values()}. */
    private HCollection values;

    /** Vista delle entry, creata al primo accesso da {@link #entrySet()}. */
    private HSet entries;

    /**
     * Costruisce una mappa vuota.
     */
    public MapAdapter() {
        table = new Hashtable();
    }

    /**
     * Costruisce una nuova mappa contenente tutte le associazioni presenti
     * nella mappa specificata.
     *
     * @param map mappa da cui copiare le associazioni iniziali
     * @throws NullPointerException se {@code map} è {@code null}
     */
    public MapAdapter(HMap map) {
        table = new Hashtable();
        copyFrom(map);
    }

    /**
     * Restituisce il numero di associazioni chiave-valore presenti nella mappa.
     * 
     * @return il numero di associazioni chiave-valore presenti nella mappa
     */
    public int size() {
        return table.size();
    }

    /**
     * Verifica se la mappa è vuota.
     *
     * @return {@code true} se la mappa non contiene associazioni;
     *         {@code false} altrimenti
     */
    public boolean isEmpty() {
        return table.isEmpty();
    }

    /**
     * Verifica se esiste un'associazione per la chiave specificata.
     *
     * @param key chiave di cui verificare la presenza
     * @return {@code true} se esiste un'associazione con questa chiave;
     *         {@code false} altrimenti
     */
    public boolean containsKey(Object key) {
        return table.containsKey(key);
    }

    /**
     * Verifica se esiste almeno un'associazione con il valore specificato.
     *
     * <p>
     * L'implementazione scorre tutte le chiavi della {@code Hashtable} e
     * confronta il valore associato con {@code value} tramite
     * {@code equals}, restituendo {@code true} alla prima corrispondenza
     * trovata. Nel caso peggiore il costo è quindi lineare nel numero di
     * elementi della mappa.
     * </p>
     *
     * @param value valore di cui verificare la presenza
     * @return {@code true} se almeno una chiave è associata a un valore
     *         uguale a {@code value}; {@code false} altrimenti
     * @throws NullPointerException se {@code value} è {@code null}, dato
     *                              che {@code Hashtable} non ammette valori
     *                              nulli
     */
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        synchronized (table) {
            Enumeration enumeration = table.keys();
            while (enumeration.hasMoreElements()) {
                Object key = enumeration.nextElement();
                if (value.equals(table.get(key))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Restituisce il valore associato alla chiave specificata.
     *
     * @param key chiave di cui si vuole ottenere il valore associato
     * @return il valore associato a {@code key}, oppure {@code null} se non
     *         esiste un'associazione per quella chiave
     */
    public Object get(Object key) {
        return table.get(key);
    }

    /**
     * Associa il valore specificato alla chiave specificata.
     *
     * @param key   chiave con cui il valore specificato deve essere
     *              associato
     * @param value valore da associare alla chiave specificata
     * @return il valore precedentemente associato a {@code key}, oppure
     *         {@code null} se non esisteva un'associazione precedente
     * @throws NullPointerException se {@code key} o {@code value} sono
     *                              {@code null}, poiché {@code Hashtable}
     *                              non ammette chiavi o valori nulli
     */
    public Object put(Object key, Object value) {
        return table.put(key, value);
    }

    /**
     * Rimuove l'associazione per la chiave specificata.
     *
     * @param key chiave la cui associazione deve essere rimossa
     * @return il valore precedentemente associato a {@code key}, oppure
     *         {@code null} se non esisteva un'associazione per quella
     *         chiave
     */
    public Object remove(Object key) {
        return table.remove(key);
    }

    /**
     * Inserisce tutte le associazioni della mappa specificata in questa mappa.
     *
     * @param map mappa contenente le associazioni da inserire in questa
     *            mappa
     * @throws NullPointerException se {@code map} è {@code null}
     */
    public void putAll(HMap map) {
        copyFrom(map);
    }

    /**
     * Copia in {@code table} tutte le associazioni della mappa specificata,
     * leggendole dall'{@link HMap#entrySet()} della sorgente.
     *
     * <p>
     * Metodo di supporto usato sia dal costruttore
     * {@link #MapAdapter(HMap)} sia da {@link #putAll(HMap)}, così da non
     * duplicare la logica di copia.
     * </p>
     *
     * @param map mappa sorgente da cui copiare le associazioni
     * @throws NullPointerException se {@code map} è {@code null}
     */
    private void copyFrom(HMap map) {
        if (map == null) {
            throw new NullPointerException();
        }
        HIterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            HMap.Entry entry = (HMap.Entry) iterator.next();
            table.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Cancella tutte le associazioni dalla mappa.
     */
    public void clear() {
        table.clear();
    }

    /**
     * Restituisce una vista delle chiavi presenti nella mappa.
     *
     * <p>
     * L'oggetto restituito è creato una sola volta e mantenuto in cache nel
     * campo {@code keys}: le chiamate successive restituiscono sempre lo
     * stesso riferimento. La vista è sostenuta dalla mappa ({@code backed}):
     * non è una copia delle chiavi.
     * </p>
     *
     * @return una vista {@link HSet} sostenuta dalla mappa contenente tutte
     *         le chiavi correntemente presenti
     */
    public HSet keySet() {
        if (keys == null) {
            keys = new KeySet();
        }
        return keys;
    }

    /**
     * Restituisce una vista dei valori presenti nella mappa.
     *
     * <p>
     * L'oggetto restituito è creato una sola volta e mantenuto in cache nel
     * campo {@code values}. A differenza di {@code keySet()} ed
     * {@code entrySet()} questa vista non è un {@link HSet}, perché valori
     * associati a chiavi diverse possono essere uguali tra loro e quindi
     * comparire più volte.
     * </p>
     *
     * @return una vista {@link HCollection} sostenuta dalla mappa
     *         contenente tutti i valori correntemente presenti
     */
    public HCollection values() {
        if (values == null) {
            values = new Values();
        }
        return values;
    }

    /**
     * Restituisce una vista delle entry presenti nella mappa.
     *
     * <p>
     * L'oggetto restituito è creato una sola volta e mantenuto in cache nel
     * campo {@code entries}. Ogni elemento della vista è un'istanza di
     * {@link HMap.Entry} sostenuta dalla mappa: il valore restituito da
     * {@link HMap.Entry#getValue()} è sempre letto dalla {@code Hashtable}
     * al momento della chiamata.
     * </p>
     *
     * @return una vista {@link HSet} sostenuta dalla mappa contenente
     *         un'entry per ogni associazione chiave-valore presente
     */
    public HSet entrySet() {
        if (entries == null) {
            entries = new EntrySet();
        }
        return entries;
    }

    /**
     * Verifica se questa mappa è uguale all'oggetto specificato.
     *
     * <p>
     * Due mappe sono considerate uguali se hanno la stessa dimensione e le
     * stesse associazioni chiave-valore, indipendentemente dall'ordine di
     * inserimento. Il confronto è quindi coerente con il contratto di
     * {@code java.util.Map#equals(Object)}.
     * </p>
     *
     * @param object oggetto da confrontare con questa mappa
     * @return {@code true} se {@code object} è una {@link HMap} con le
     *         stesse associazioni di questa mappa; {@code false} altrimenti
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof HMap)) {
            return false;
        }
        HMap map = (HMap) object;
        if (map.size() != size()) {
            return false;
        }
        try {
            Enumeration enumeration = table.keys();
            while (enumeration.hasMoreElements()) {
                Object key = enumeration.nextElement();
                Object value = table.get(key);
                if (!map.containsKey(key)) {
                    return false;
                }
                if (!value.equals(map.get(key))) {
                    return false;
                }
            }
        } catch (ClassCastException exception) {
            return false;
        } catch (NullPointerException exception) {
            return false;
        }
        return true;
    }

    /**
     * Restituisce il codice hash di questa mappa.
     *
     * <p>
     * Il codice hash è calcolato come somma, per ogni associazione, dello
     * XOR tra il codice hash della chiave e quello del valore, in modo
     * indipendente dall'ordine di iterazione della {@code Hashtable}. Il
     * risultato è quindi coerente con {@link #equals(Object)}.
     * </p>
     *
     * @return il codice hash di questa mappa
     */
    public int hashCode() {
        int hash = 0;
        Enumeration enumeration = table.keys();
        while (enumeration.hasMoreElements()) {
            Object key = enumeration.nextElement();
            hash += key.hashCode() ^ table.get(key).hashCode();
        }
        return hash;
    }

    /**
     * Restituisce una stringa della mappa, delegando
     * direttamente a {@code Hashtable#toString()}.
     *
     * @return la rappresentazione testuale della {@code Hashtable}
     *         sottostante
     */
    public String toString() {
        return table.toString();
    }

    /**
     * Costruisce e restituisce uno snapshot delle chiavi correntemente
     * presenti nella mappa, da usare come base per un nuovo
     * {@link ViewIterator}.
     *
     * <p>
     * Lo snapshot è un semplice array di riferimenti alle chiavi: non è
     * una copia dell'intera mappa e non congela i valori associati, che
     * continuano a essere letti dalla {@code Hashtable} al momento della
     * richiesta. L'accesso alla tabella è sincronizzato per evitare che la
     * dimensione dell'array e il contenuto effettivamente copiato risultino
     * disallineati nel caso (non garantito comunque altrove) di accessi
     * concorrenti.
     * </p>
     *
     * @return un nuovo array {@code Object[]} contenente tutte le chiavi
     *         presenti nella mappa al momento della chiamata
     */
    private Object[] snapshotKeys() {
        synchronized (table) {
            Object[] snapshot = new Object[table.size()];
            Enumeration enumeration = table.keys();
            int index = 0;
            while (enumeration.hasMoreElements()) {
                snapshot[index] = enumeration.nextElement();
                index++;
            }
            return snapshot;
        }
    }

    /**
     * Verifica se l'oggetto specificato sia una {@link HMap.Entry} la cui
     * chiave e il cui valore corrispondono a una coppia realmente
     * presente nella mappa.
     *
     * @param object oggetto da verificare, tipicamente ottenuto da un
     *               iteratore su {@code entrySet()}
     * @return {@code true} se {@code object} è una {@code HMap.Entry} con
     *         chiave presente nella mappa e valore uguale a quello
     *         associato; {@code false} altrimenti
     */
    private boolean containsEntry(Object object) {
        if (!(object instanceof HMap.Entry)) {
            return false;
        }
        HMap.Entry entry = (HMap.Entry) object;
        Object key = entry.getKey();
        if (!table.containsKey(key)) {
            return false;
        }
        return equal(entry.getValue(), table.get(key));
    }

    /**
     * Confronta due riferimenti gestendo correttamente il caso in cui uno
     * di essi sia {@code null}, evitando di dover ripetere ovunque il
     * controllo esplicito.
     *
     * @param first  primo oggetto da confrontare, può essere {@code null}
     * @param second secondo oggetto da confrontare, può essere {@code null}
     * @return {@code true} se entrambi gli argomenti sono {@code null},
     *         oppure se {@code first} non è {@code null} e
     *         {@code first.equals(second)} restituisce {@code true};
     *         {@code false} altrimenti
     */
    private static boolean equal(Object first, Object second) {
        return first == null ? second == null : first.equals(second);
    }

    /**
     * Classe astratta di base per le tre viste della mappa
     * ({@code KeySet}, {@code Values}, {@code EntrySet}).
     *
     * <p>
     * Raccoglie l'implementazione comune dei metodi di {@link HCollection}
     * che non dipendono dal tipo specifico di elemento restituito
     * (chiave, valore o entry): {@code size}, {@code isEmpty},
     * {@code toArray}, {@code add} (sempre non supportato),
     * {@code containsAll}, {@code addAll}, {@code removeAll},
     * {@code retainAll} e {@code clear}. Le sottoclassi devono fornire solo
     * {@code contains}, {@code iterator} e {@code remove}, che dipendono
     * dal tipo di elemento della vista.
     * </p>
     */
    private abstract class View implements HCollection {

        /**
         * restituisce il numero di elementi della vista, che corrisponde
         *
         * @return il numero di elementi della vista, sempre uguale a
         *         {@link MapAdapter#size()}
         */
        public int size() {
            return MapAdapter.this.size();
        }

        /**
         * Verifica se la mappa è vuota.
         *
         * @return {@code true} se la mappa sottostante è vuota;
         *         {@code false} altrimenti
         */
        public boolean isEmpty() {
            return MapAdapter.this.isEmpty();
        }

        /**
         * Verifica se l'elemento specificato è presente nella vista.
         *
         * @param object elemento del quale verificare la presenza
         * @return {@code true} se l'elemento è presente nella vista;
         *         {@code false} altrimenti
         */
        public abstract boolean contains(Object object);

        /**
         * Restituisce un nuovo {@link HIterator} sugli elementi della vista.
         *
         * @return un nuovo {@link HIterator} sugli elementi della vista
         */
        public abstract HIterator iterator();

        /**
         * Restituisce un nuovo array {@code Object[]} contenente tutti gli elementi
         * della vista, nell'ordine dell'iteratore.
         *
         * @return un nuovo {@code Object[]} contenente tutti gli elementi
         *         della vista, nell'ordine dell'iteratore
         */
        public Object[] toArray() {
            Object[] array = new Object[size()];
            HIterator iterator = iterator();
            int index = 0;
            while (iterator.hasNext()) {
                array[index] = iterator.next();
                index++;
            }
            return array;
        }

        /**
         * Restituisce un nuovo array {@code Object[]} contenente tutti gli elementi
         * della vista, nell'ordine dell'iteratore,rispettando CLDC1.1 (per dimensione del array).
         *
         * @param array array di destinazione proposto dal chiamante
         * @return {@code array}, se dispone di spazio sufficiente,
         *         altrimenti un nuovo {@code Object[]} della dimensione
         *         corretta
         * @throws NullPointerException se {@code array} è {@code null}
         * @throws ArrayStoreException  se {@code array} è troppo piccolo e
         *                              il suo tipo runtime non è
         *                              esattamente {@code Object[]}
         */
        public Object[] toArray(Object[] array) {
            if (array == null) {
                throw new NullPointerException();
            }
            int currentSize = size();
            Object[] destination = array;
            if (array.length < currentSize) {
                if (array.getClass() != Object[].class) {
                    throw new ArrayStoreException();
                }
                destination = new Object[currentSize];
            }
            HIterator iterator = iterator();
            int index = 0;
            while (iterator.hasNext()) {
                destination[index] = iterator.next();
                index++;
            }
            if (destination.length > index) {
                destination[index] = null;
            }
            return destination;
        }

        /**
         * Aggiunge l'elemento specificato alla vista.
         *
         * <p>
         * Operazione non supportata da nessuna delle viste della mappa:
         * viene sempre lanciata {@link HUnsupportedOperationException}
         * senza modificare la mappa sottostante.
         * </p>
         *
         * @param object elemento di cui si richiederebbe l'aggiunta
         * @return mai restituito, perché il metodo lancia sempre
         *         un'eccezione
         * @throws HUnsupportedOperationException sempre, dato che le viste
         *                                        non supportano
         *                                        l'inserimento
         */
        public boolean add(Object object) {
            throw new HUnsupportedOperationException();
        }

        /**
         * Rimuove l'elemento specificato dalla vista.
         *
         * @param object elemento del quale rimuovere una singola occorrenza
         * @return {@code true} se la mappa è stata modificata;
         *         {@code false} se non esisteva una corrispondenza
         */
        public abstract boolean remove(Object object);

        /**
         * Verifica se tutti gli elementi della collezione specificata sono presenti
         * nella vista.
         *
         * @param collection collezione contenente gli elementi da
         *                   verificare
         * @return {@code true} se ogni elemento di {@code collection} è
         *         contenuto in questa vista; {@code false} altrimenti
         * @throws NullPointerException se {@code collection} è
         *                              {@code null}
         */
        public boolean containsAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            HIterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (!contains(iterator.next())) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Aggiunge tutti gli elementi della collezione specificata alla vista.
         * 
         * <p>
         * Poiché add(Object) non è supportato dalle viste, una
         * sorgente non vuota provoca sempre
         * {@link HUnsupportedOperationException} al primo elemento; una
         * sorgente vuota non ha invece alcun effetto.
         * </p>
         *
         * @param collection collezione i cui elementi dovrebbero essere
         *                   aggiunti
         * @return {@code false} se {@code collection} è vuota
         * @throws NullPointerException           se {@code collection} è
         *                                        {@code null}
         * @throws HUnsupportedOperationException se {@code collection}
         *                                        contiene almeno un
         *                                        elemento
         */
        public boolean addAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            boolean modified = false;
            HIterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (add(iterator.next())) {
                    modified = true;
                }
            }
            return modified;
        }

        /**
         * Rimuove tutti gli elementi della collezione specificata dalla vista.
         *
         * @param collection collezione che determina quali elementi
         *                   rimuovere
         * @return {@code true} se almeno un elemento è stato rimosso;
         *         {@code false} altrimenti
         * @throws NullPointerException se {@code collection} è
         *                              {@code null}
         */
        public boolean removeAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            boolean modified = false;
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                if (collection.contains(iterator.next())) {
                    iterator.remove();
                    modified = true;
                }
            }
            return modified;
        }

        /**
         * Rimuove tutti gli elementi della collezione specificata dalla vista.
         * 
         * @param collection collezione che determina quali elementi
         *                   conservare
         * @return {@code true} se almeno un elemento è stato rimosso;
         *         {@code false} altrimenti
         * @throws NullPointerException se {@code collection} è
         *                              {@code null}
         */
        public boolean retainAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            boolean modified = false;
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                if (!collection.contains(iterator.next())) {
                    iterator.remove();
                    modified = true;
                }
            }
            return modified;
        }

        /**
         * pulisce la vista, rimuovendo tutti gli elementi dalla mappa sottostante.
         *
         * <p>
         * Delega direttamente a {@link MapAdapter#clear()}: viene svuotata
         * l'intera mappa, non solo questa vista.
         * </p>
         */
        public void clear() {
            MapAdapter.this.clear();
        }
    }

    /**
     * Estensione di {@link View} comune a {@code KeySet} ed
     * {@code EntrySet}, che aggiunge l'uguaglianza e il
     * calcolo del codice hash tipici di un {@link HSet}: confronto basato
     * sul contenuto e non sull'ordine di iterazione.
     */
    private abstract class SetView extends View implements HSet {

        /**
         * Verifica se l'oggetto specificato è uguale a questa vista.
         * 
         * @param object oggetto da confrontare con questa vista
         * @return {@code true} se {@code object} è un {@link HSet} con la
         *         stessa dimensione e gli stessi elementi di questa vista;
         *         {@code false} altrimenti
         */
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof HSet)) {
                return false;
            }
            HSet set = (HSet) object;
            if (set.size() != size()) {
                return false;
            }
            try {
                return containsAll(set);
            } catch (ClassCastException exception) {
                return false;
            } catch (NullPointerException exception) {
                return false;
            }
        }

        /**
         * Calcola il codice hash della vista.
         *
         * @return la somma dei codici hash degli elementi della vista,
         *         indipendente dall'ordine di iterazione
         */
        public int hashCode() {
            int hash = 0;
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                if (object != null) {
                    hash += object.hashCode();
                }
            }
            return hash;
        }
    }

    /**
     * Vista {@link HSet} sulle chiavi della mappa, sostenuta dalla
     * {@code Hashtable} di {@link MapAdapter}.
     */
    private final class KeySet extends SetView {

        /**
         * Verifica se l'oggetto specificato è presente nella vista.
         * 
         * @param object oggetto da cercare tra le chiavi
         * @return {@code true} se {@code object} è una chiave presente
         *         nella mappa; {@code false} altrimenti
         */
        public boolean contains(Object object) {
            return containsKey(object);
        }

        /**
         * Restituisce un nuovo {@link HIterator} che restituisce le chiavi
         * della mappa
         *
         * @return un nuovo {@link HIterator} che restituisce le chiavi
         *         della mappa
         */
        public HIterator iterator() {
            return new ViewIterator(ViewIterator.KEYS);
        }

        /**
         * rimuove la chiave specificata dalla vista.
         * 
         * <p>
         * Rimuove dalla mappa l'intera associazione corrispondente alla
         * chiave, non solo la chiave stessa.
         * </p>
         *
         * @param object chiave della quale richiedere la rimozione
         * @return {@code true} se la chiave era presente ed è stata
         *         rimossa insieme al proprio valore; {@code false}
         *         altrimenti
         */
        public boolean remove(Object object) {
            if (!containsKey(object)) {
                return false;
            }
            MapAdapter.this.remove(object);
            return true;
        }
    }

    /**
     * Vista {@link HCollection} sui valori della mappa, sostenuta dalla
     * {@code Hashtable} di {@link MapAdapter}.
     *
     * <p>
     * A differenza di {@code KeySet} ed {@code EntrySet} non implementa
     * {@link HSet}, perché valori uguali associati a chiavi diverse
     * compaiono più volte nella vista; per lo stesso motivo non ridefinisce
     * {@code equals} ed {@code hashCode}, che restano quelli per identità
     * ereditati da {@code Object}.
     * </p>
     */
    private final class Values extends View {

        /**
         * Verifica se l'oggetto specificato è presente nella vista.
         * 
         * @param object oggetto da cercare tra i valori
         * @return {@code true} se {@code object} è il valore di almeno
         *         un'associazione; {@code false} altrimenti
         */
        public boolean contains(Object object) {
            return containsValue(object);
        }

        /**
         * Restituisce un nuovo {@link HIterator} che restituisce i valori
         * della mappa
         *
         * @return un nuovo {@link HIterator} che restituisce i valori
         *         della mappa
         */
        public HIterator iterator() {
            return new ViewIterator(ViewIterator.VALUES);
        }

        /**
         * Rimuove una singola occorrenza del valore specificato dalla vista.
         *
         * <p>
         * Scorre la vista finché non trova un valore corrispondente e ne
         * rimuove la coppia tramite l'iteratore, così da eliminare
         * una sola occorrenza anche quando lo stesso valore compare più
         * volte.
         * </p>
         *
         * @param object valore del quale rimuovere una singola occorrenza
         * @return {@code true} se un'associazione con questo valore è
         *         stata trovata e rimossa; {@code false} altrimenti
         */
        public boolean remove(Object object) {
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                Object value = iterator.next();
                if (object == null
                        ? value == null
                        : object.equals(value)) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Vista {@link HSet} sulle entry della mappa, basata sulla
     * {@code Hashtable} di {@link MapAdapter}. Ogni elemento restituito
     * dall'iteratore è un'istanza di {@link MapEntry}.
     */
    private final class EntrySet extends SetView {

        /**
         * Verifica se l'oggetto specificato è presente nella vista.
         *
         * @param object oggetto da cercare tra le entry, tipicamente una
         *               {@link HMap.Entry}
         * @return {@code true} se {@code object} è una {@code HMap.Entry}
         *         con chiave e valore corrispondenti a un'associazione
         *         presente nella mappa; {@code false} altrimenti
         */
        public boolean contains(Object object) {
            return containsEntry(object);
        }

        /**
         * Restituisce un nuovo {@link HIterator} che restituisce le entry della
         * mappa
         *
         * @return un nuovo {@link HIterator} che restituisce le entry della
         *         mappa
         */
        public HIterator iterator() {
            return new ViewIterator(ViewIterator.ENTRIES);
        }

        /**
         * Rimuove l'associazione corrispondente all'entry specificata dalla vista.
         *
         * @param object entry della quale richiedere la rimozione,
         *               tipicamente una {@link HMap.Entry}
         * @return {@code true} se l'associazione corrispondente è stata
         *         trovata e rimossa; {@code false} altrimenti
         */
        public boolean remove(Object object) {
            if (!containsEntry(object)) {
                return false;
            }
            HMap.Entry entry = (HMap.Entry) object;
            MapAdapter.this.remove(entry.getKey());
            return true;
        }
    }

    /**
     * Implementazione di {@link HIterator} comune alle tre viste della
     * mappa. Al momento della creazione acquisisce, tramite
     * {@link MapAdapter#snapshotKeys()}, uno snapshot delle chiavi
     * correntemente presenti e lo usa come base per l'attraversamento; il
     * tipo di elemento restituito da {@code next()} (chiave, valore o
     * entry) dipende dal parametro passato al costruttore.
     *
     * <p>
     * L'iteratore non è fail-fast: non viene mantenuto alcun contatore
     * di modifiche strutturali e non viene lanciata alcuna eccezione se la
     * mappa viene modificata al di fuori dell'iteratore durante
     * l'attraversamento. È supportata la rimozione tramite
     * {@link #remove()}, che aggiorna sia lo stato interno dell'iteratore
     * sia la mappa backing.
     * </p>
     */
    private final class ViewIterator implements HIterator {

        /** Costante che identifica un iteratore sulle chiavi. */
        private static final int KEYS = 0;

        /** Costante che identifica un iteratore sui valori. */
        private static final int VALUES = 1;

        /** Costante che identifica un iteratore sulle entry. */
        private static final int ENTRIES = 2;

        /** Snapshot delle chiavi acquisito alla creazione dell'iteratore. */
        private final Object[] snapshot;

        /** Tipo di elemento restituito da {@link #next()}. */
        private final int type;

        /** Indice della prossima chiave dello snapshot da restituire. */
        private int cursor;

        /** Ultima chiave restituita da {@link #next()}. */
        private Object lastKey;

        /** Indica se è lecito invocare {@link #remove()} in questo momento. */
        private boolean removable;

        /**
         * Costruisce un nuovo iteratore del tipo specificato, acquisendo
         * subito uno snapshot delle chiavi della mappa.
         *
         * @param iteratorType uno tra {@link #KEYS}, {@link #VALUES} ed
         *                     {@link #ENTRIES}
         */
        private ViewIterator(int iteratorType) {
            snapshot = snapshotKeys();
            type = iteratorType;
        }

        /**
         * Verifica se l'iteratore ha ancora elementi da restituire.
         *
         * @return {@code true} se lo snapshot contiene ancora chiavi non
         *         ancora restituite; {@code false} altrimenti
         */
        public boolean hasNext() {
            return cursor < snapshot.length;
        }

        /**
         * Restituisce il prossimo elemento dello snapshot, che può essere una chiave,
         * un valore o un'entry a seconda del tipo dell'iteratore.
         *
         * @return la prossima chiave, valore o entry, a seconda del tipo
         *         dell'iteratore
         * @throws NoSuchElementException se l'iterazione non ha ulteriori
         *                                elementi
         */
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastKey = snapshot[cursor];
            cursor++;
            removable = true;
            if (type == KEYS) {
                return lastKey;
            }
            if (type == VALUES) {
                return table.get(lastKey);
            }
            return new MapEntry(lastKey);
        }

        /**
         * Rimuove l'elemento restituito dall'ultima chiamata a {@link #next()} dalla
         * mappa backing.
         *
         * <p>
         * Rimuove dalla mappa backing l'associazione corrispondente
         * all'ultima chiave restituita da {@link #next()}. Dopo la
         * chiamata non è possibile chiamare nuovamente {@code remove()}
         * senza un'ulteriore chiamata a {@code next()}.
         * </p>
         *
         * @throws HIllegalStateException se {@code next()} non è stato
         *                                ancora chiamato, oppure se
         *                                {@code remove()} è già stato
         *                                chiamato dopo l'ultima
         *                                chiamata a {@code next()}
         */
        public void remove() {
            if (!removable) {
                throw new HIllegalStateException();
            }
            MapAdapter.this.remove(lastKey);
            removable = false;
        }
    }

    /**
     * Implementazione di {@link HMap.Entry} sostenuta dalla mappa: non
     * memorizza il valore, ma lo legge dalla {@code Hashtable} ogni volta
     * che viene richiesto tramite {@link #getValue()}, così da riflettere
     * eventuali modifiche successive alla creazione dell'entry.
     */
    private final class MapEntry implements HMap.Entry {

        /** Chiave dell'associazione rappresentata da questa entry. */
        private final Object key;

        /**
         * Costruisce una entry per la chiave specificata.
         *
         * @param entryKey chiave dell'associazione
         */
        private MapEntry(Object entryKey) {
            key = entryKey;
        }

        /**
         * Restituisce la chiave associata a questa entry.
         *
         * @return la chiave associata a questa entry
         */
        public Object getKey() {
            return key;
        }

        /**
         * restituisce il valore associato a questa entry
         *
         * @return il valore correntemente associato alla chiave nella
         *         mappa
         */
        public Object getValue() {
            return table.get(key);
        }

        /**
         * Associa il valore specificato alla chiave di questa entry nella mappa,
         * restituendo il valore precedentemente associato.
         *
         * @param value nuovo valore da associare alla chiave di questa
         *              entry
         * @return il valore precedentemente associato alla chiave
         * @throws NullPointerException se {@code value} è {@code null}
         */
        public Object setValue(Object value) {
            return table.put(key, value);
        }

        /**
         * Verifica se l'oggetto specificato è uguale a questa entry.
         *
         * @param object oggetto da confrontare con questa entry
         * @return {@code true} se {@code object} è una {@code HMap.Entry}
         *         con chiave e valore uguali a quelli di questa entry;
         *         {@code false} altrimenti
         */
        public boolean equals(Object object) {
            if (!(object instanceof HMap.Entry)) {
                return false;
            }
            HMap.Entry entry = (HMap.Entry) object;
            return equal(key, entry.getKey())
                    && equal(getValue(), entry.getValue());
        }

        /**
         * Restituisce il codice hash dell'entry.
         *
         * @return lo XOR tra il codice hash della chiave e quello del
         *         valore corrente, coerente con {@link #equals(Object)}
         */
        public int hashCode() {
            Object value = getValue();
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * Restituisce una rappresentazione testuale dell'entry nella
         * forma {@code chiave=valore}.
         *
         * @return la rappresentazione testuale dell'entry
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(key);
            buffer.append('=');
            buffer.append(getValue());
            return buffer.toString();
        }
    }
}