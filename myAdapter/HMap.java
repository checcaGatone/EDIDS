package myAdapter;

/**
 * Interfaccia che riproduce {@code java.util.Map} di J2SE 1.4.2.
 *
 * <p>
 * {@code HMap} definisce un oggetto che associa chiavi a valori, senza
 * ammettere chiavi duplicate: ogni chiave può essere associata al più a un
 * valore. L'interfaccia non estende {@link HCollection}, esattamente come
 * {@code java.util.Map} non estende {@code java.util.Collection}: una mappa
 * memorizza coppie chiave-valore, mentre una collezione contiene singoli
 * elementi.I metodi usano riferimenti di tipo {@code Object} perché la versione considerata non
 * prevede ancora i generics.
 * </p>
 *
 * <p>
 * Il contenuto della mappa può essere osservato tramite tre viste
 * collezione: {@link #keySet()} restituisce le chiavi, {@link #values()} i
 * valori ed {@link #entrySet()} le singole associazioni sotto forma di
 * {@link Entry}. {@code keySet()} ed {@code entrySet()} restituiscono un
 * {@link HSet}, perché sia le chiavi sia le entry non possono ripetersi;
 * {@code values()} restituisce invece una semplice {@link HCollection},
 * perché valori associati a chiavi diverse possono essere uguali fra loro e
 * quindi comparire più volte.
 * </p>
 *
 * <p>
 * Le tre viste sono sostenute dalla mappa backed: non sono copie
 * indipendenti del contenuto, per cui una modifica della mappa è visibile
 * attraverso la vista e, viceversa, le operazioni di rimozione compiute
 * sulla vista si ripercuotono sulla mappa. Le viste supportano le
 * operazioni di rimozione previste da {@link HCollection}
 * ({@code remove}, {@code removeAll}, {@code retainAll}, {@code clear}),
 * ma non le operazioni di inserimento ({@code add}, {@code addAll}),
 * perché da una singola chiave, da un singolo valore o da una singola
 * entry non è in possibile ricostruire una nuova coppia
 * valida per la mappa: in questi casi viene lanciata una
 * {@link MapAdapter.HUnsupportedOperationException}.
 * </p>
 *
 * <p>
 * L'interfaccia lascia alle implementazioni
 * concrete la scelta di ammettere o meno chiavi e valori {@code null}. In
 * {@link MapAdapter}, che usa come adaptee la {@code Hashtable},
 * né le chiavi né i valori possono essere {@code null}: i singoli
 * metodi specificano nei rispettivi Javadoc i casi in cui viene lanciata
 * una {@code NullPointerException}.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see HCollection
 * @see HSet
 * @see HIterator
 * @see MapAdapter
 */
public interface HMap {

    /**
     * Restituisce il numero di coppie chiave-valore presenti nella
     * mappa.
     *
     * @return il numero corrente di associazioni; è zero quando la mappa è
     *         vuota
     */
    int size();

    /**
     * Verifica se la mappa non contiene alcuna coppia.
     *
     * <p>
     * Il risultato è equivalente a {@code size() == 0}.
     * </p>
     *
     * @return {@code true} se la mappa non contiene associazioni;
     *         {@code false} altrimenti
     */
    boolean isEmpty();

    /**
     * Verifica se la mappa contiene un'coppia per la chiave
     * specificata.
     *
     * @param key chiave della quale verificare la presenza
     * @return {@code true} se esiste un'associazione con questa chiave;
     *         {@code false} altrimenti
     * @throws ClassCastException   se il tipo della chiave impedisce il
     *                              confronto nell'implementazione concreta
     * @throws NullPointerException se {@code key} è {@code null} e
     *                              l'implementazione concreta non ammette
     *                              chiavi nulle; è il caso di
     *                              {@link MapAdapter}
     */
    boolean containsKey(Object key);

    /**
     * Verifica se la mappa contiene almeno un'associazione con il valore
     * specificato.
     *
     * <p>
     * A differenza di {@link #containsKey(Object)}, che sfrutta la
     * struttura associativa della mappa, questo controllo esamina
     * tutte le associazioni presenti nella mappa.
     * </p>
     *
     * @param value valore del quale verificare la presenza
     * @return {@code true} se almeno una chiave è associata a un valore
     *         uguale a {@code value}; {@code false} altrimenti
     * @throws ClassCastException   se il tipo del valore impedisce il
     *                              confronto nell'implementazione concreta
     * @throws NullPointerException se {@code value} è {@code null} e
     * l'implementazione concreta non ammette valori nulli; è il caso di
     * {@link MapAdapter}
     */
    boolean containsValue(Object value);

    /**
     * Restituisce il valore associato alla chiave specificata.
     *
     * @param key chiave della quale si vuole ottenere il valore associato
     * @return il valore associato a {@code key}, oppure {@code null} se
     *         non esiste un'associazione per quella chiave (oppure se la
     *         chiave è associata proprio al valore {@code null}, cosa che
     *         non può accadere in {@link MapAdapter})
     * @throws ClassCastException se il tipo della chiave impedisce il
     *  confronto nell'implementazione concreta
     * @throws NullPointerException se {@code key} è {@code null} e
     * l'implementazione  non ammette chiavi nulle; è il caso di
     * {@link MapAdapter}
     */
    Object get(Object key);

    /**
     * Associa il valore specificato alla chiave specificata. Se la mappa
     * conteneva già una coppia per questa chiave, il vecchio valore
     * viene sostituito.
     *
     * @param key chiave con cui il valore specificato deve essere
     * associato
     * @param value valore da associare alla chiave specificata
     * @return il valore precedentemente associato a {@code key}, oppure
     * {@code null} se non esisteva un'associazione precedente
     * @throws ClassCastException se il tipo della chiave o del valore
     * ne impedisce l'inserimento nell'implementazione concreta
     * @throws NullPointerException se {@code key} o {@code value} sono
     * {@code null} e l'implementazione concreta non li ammette; è il caso
     * di {@link MapAdapter}, dove nessuno dei due argomenti può essere
     * {@code null}
     * @throws IllegalArgumentException se una proprietà della chiave o del
     * valore ne impedisce l'inserimento
     */
    Object put(Object key, Object value);

    /**
     * Rimuove, se presente, l'associazione per la chiave specificata.
     *
     * @param key chiave la cui associazione deve essere rimossa
     * @return il valore precedentemente associato a {@code key}, oppure
     * {@code null} se non esisteva un'associazione per quella
     * chiave
     * @throws ClassCastException se il tipo della chiave impedisce il
     * confronto nell'implementazione concreta
     * @throws NullPointerException se {@code key} è {@code null} e
     * l'implementazione concreta non ammette chiavi nulle
     */
    Object remove(Object key);

    /**
     * Copia nella mappa ricevente tutte le associazioni della mappa
     * specificata, sovrascrivendo le eventuali associazioni già presenti
     * per le stesse chiavi.
     *
     * <p>
     * L'effetto è equivalente a invocare {@link #put(Object, Object)} per
     * ogni associazione della mappa {@code map}.
     * </p>
     *
     * @param map mappa contenente le coppie da inserire in questa
     *            mappa
     * @throws NullPointerException se {@code map} è {@code null}, oppure
     *                              se contiene una chiave o un valore
     *                              nullo e l'implementazione ricevente non
     *                              li ammette
     * @throws ClassCastException   se il tipo di una chiave o di un valore
     *                              della mappa specificata ne impedisce
     *                              l'inserimento nell'implementazione
     *                              ricevente
     */
    void putAll(HMap map);

    /**
     * Rimuove tutte le associazioni dalla mappa.
     *
     * <p>
     * Dopo la chiamata la mappa è vuota e le viste ottenute in precedenza
     * tramite {@link #keySet()}, {@link #values()} ed {@link #entrySet()}
     * osservano immediatamente lo stato vuoto, essendo sostenute dalla
     * mappa.
     * </p>
     */
    void clear();

    /**
     * Restituisce una vista collezione delle chiavi della mappa.
     *
     * <p>
     * La vista è sostenuta dalla mappa: le rimozioni eseguite su di essa
     * rimuovono la corrispondente associazione dalla mappa. Non sono invece supportate
     * le operazioni di inserimento.
     * </p>
     *
     * @return un {@link HSet} sostenuto dalla mappa contenente tutte le
     *         chiavi correntemente presenti
     */
    HSet keySet();

    /**
     * Restituisce una vista collezione dei valori contenuti nella mappa.
     *
     * <p>
     * La vista è sostenuta dalla mappa, con le stesse regole di
     * {@link #keySet()}. A differenza di {@code keySet()} ed
     * {@code entrySet()}, il tipo restituito è una semplice
     * {@link HCollection} e non un {@link HSet}, perché valori uguali
     * associati a chiavi diverse compaiono più volte in questa vista.
     * </p>
     *
     * @return una {@link HCollection} sostenuta dalla mappa contenente
     *         tutti i valori correntemente presenti
     */
    HCollection values();

    /**
     * Restituisce una vista collezione delle associazioni chiave-valore
     * contenute nella mappa, sotto forma di oggetti {@link Entry}.
     *
     * <p>
     * La vista è sostenuta dalla mappa, con le stesse regole di
     * {@link #keySet()}. Le entry restituite dall'iteratore di questa
     * vista sono a loro volta sostenute dalla mappa: il valore letto con
     * {@link Entry#getValue()} riflette lo stato corrente della mappa, e
     * {@link Entry#setValue(Object)} modifica l'associazione sottostante.
     * </p>
     *
     * @return un {@link HSet} sostenuto dalla mappa contenente una
     *         {@link Entry} per ogni associazione chiave-valore presente
     */
    HSet entrySet();

    /**
     * Confronta la mappa con l'oggetto specificato.
     *
     * <p>
     * Due mappe sono considerate uguali se rappresentano lo stesso insieme
     * di associazioni chiave-valore, indipendentemente dall'ordine di
     * iterazione: devono cioè avere la stessa dimensione e, per ogni
     * chiave dell'una, un'associazione corrispondente nell'altra con lo
     * stesso valore.
     * </p>
     *
     * @param object oggetto da confrontare con questa mappa
     * @return {@code true} se {@code object} è una {@code HMap} con le
     *         stesse associazioni di questa mappa; {@code false}
     *         altrimenti
     */
    boolean equals(Object object);

    /**
     * Restituisce il codice hash della mappa.
     *
     * <p>
     * Il valore deve essere coerente con {@link #equals(Object)}: due
     * mappe uguali devono produrre lo stesso codice hash.
     * </p>
     *
     * @return il codice hash di questa mappa
     */
    int hashCode();

    /**
     * Interfaccia che riproduce {@code java.util.Map.Entry} di J2SE 1.4.2, rappresentando una
     * singola associazione chiave-valore della mappa.
     *
     * <p>
     * Un {@code Entry} ha senso solo in relazione alla mappa da cui
     * proviene: nelle implementazioni correnti, ottenute tramite
     * l'iteratore di {@link HMap#entrySet()}, l'oggetto resta sostenuto
     * dalla mappa e non ne rappresenta una copia indipendente. Il
     * comportamento di una entry dopo che la corrispondente associazione è
     * stata rimossa dalla mappa (ad esempio tramite
     * {@link HIterator#remove()}) non è specificato dal contratto di
     * questa interfaccia.
     * </p>
     */
    interface Entry {

        /**
         * Restituisce la chiave corrispondente a questa entry.
         *
         * @return la chiave della entry
         */
        Object getKey();

        /**
         * Restituisce il valore corrispondente a questa entry.
         *
         * <p>
         * Nelle entry sostenute dalla mappa il valore restituito è quello
         * correntemente associato alla chiave nella mappa al momento della
         * chiamata, e può quindi riflettere modifiche avvenute dopo la
         * creazione della entry.
         * </p>
         *
         * @return il valore correntemente associato alla chiave di questa
         *         entry
         */
        Object getValue();

        /**
         * Sostituisce il valore corrispondente a questa entry con quello
         * specificato, aggiornando anche la mappa sottostante.
         * È un'operazione opzionale.
         *
         * @param value nuovo valore da associare alla chiave di questa
         *              entry
         * @return il valore precedentemente associato alla chiave
         * @throws MapAdapter.HUnsupportedOperationException se
         *  l'implementazione non supporta la modifica del valore
         * @throws ClassCastException se il tipo del valore ne
         * impedisce l'inserimento nella mappa sottostante
         * @throws NullPointerException se {@code value} è {@code null}
         * e la mappa sottostante non ammette valori nulli
         */
        Object setValue(Object value);

        /**
         * Confronta la entry con l'oggetto specificato.
         *
         * <p>
         * Due entry sono considerate uguali se hanno chiavi uguali fra
         * loro e valori uguali fra loro.
         * </p>
         *
         * @param object oggetto da confrontare con questa entry
         * @return {@code true} se {@code object} è una {@code Entry} con
         *         chiave e valore uguali a quelli di questa entry;
         *         {@code false} altrimenti
         */
        boolean equals(Object object);

        /**
         * Restituisce il codice hash della entry.
         *
         * <p>
         * Il valore deve essere coerente con {@link #equals(Object)} ed è
         * calcolato come XOR fra il codice hash della chiave e
         * quello del valore.
         * </p>
         *
         * @return il codice hash di questa entry
         */
        int hashCode();
    }
}