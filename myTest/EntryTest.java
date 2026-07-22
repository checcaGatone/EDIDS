package myTest;

import myAdapter.HMap;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case dedicato alle entry restituite da {@link MapAdapter#entrySet()}.
 *
 * <h2>Summary</h2>
 * <p>
 * La classe verifica il comportamento di {@link HMap.Entry}: lettura della
 * chiave e del valore, aggiornamento tramite {@code setValue()}, collegamento
 * con la mappa originale, uguaglianza, codice hash e rappresentazione testuale.
 * Viene controllato anche il rifiuto di un valore {@code null}, coerente con il
 * vincolo imposto dalla {@code Hashtable} usata come adaptee.
 * </p>
 *
 * <h2>Test Case Design</h2>
 * <p>
 * Ogni test usa una nuova {@link MapAdapter} contenente il solo mapping
 * {@code a=1}. La presenza di una sola coppia rende univoca l'entry ottenuta
 * dall'iteratore e permette di non fare assunzioni sull'ordine della
 * {@code Hashtable}. I valori sono stringhe semplici, così uguaglianza e hash
 * sono deterministici. Quando serve un confronto tra oggetti distinti viene
 * usata {@link EntryStub}, un'implementazione indipendente di {@code HMap.Entry}.
 * Le asserzioni controllano sia lo stato dell'entry sia, nei test dedicati al
 * backing, gli effetti osservabili sulla mappa e sulle sue viste.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see MapAdapter
 * @see HMap.Entry
 */
public class EntryTest {
    /** Mappa ricreata prima di ogni test e usata come backing dell'entry. */
    private HMap map;

    /** Entry associata all'unico mapping {@code a=1} della fixture. */
    private HMap.Entry entry;

    /**
     * Prepara una fixture indipendente con il mapping {@code a=1} e ne ricava
     * l'unica entry. Ricostruire entrambi gli oggetti evita che un aggiornamento
     * eseguito da un test possa influenzare quelli successivi.
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        entry = (HMap.Entry) map.entrySet().iterator().next();
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che i metodi di lettura restituiscano il mapping corrente.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La fixture contiene una sola coppia, quindi l'entry selezionata è
     * certamente quella associata ad {@code a} e il test non dipende
     * dall'ordine di iterazione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Invoca {@code getKey()} e {@code getValue()} sull'entry della fixture.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene il mapping {@code a=1} e l'entry è già stata ottenuta.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'entry e la mappa non vengono modificate.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La chiave restituita è {@code a} e il valore restituito è {@code 1}.</p>
     */
    @Test
    public void gettersReturnCurrentKeyAndValue() {
        assertEquals("a", entry.getKey());
        assertEquals("1", entry.getValue());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il valore restituito da {@code setValue()} e quello poi esposto dall'entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il nuovo valore {@code 2} è diverso da {@code 1}: in questo modo si
     * distinguono chiaramente il valore precedente restituito dal metodo e il
     * valore corrente letto dopo l'aggiornamento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Sostituisce {@code 1} con {@code 2} e interroga nuovamente l'entry.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'entry rappresenta il mapping {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'entry rappresenta il mapping aggiornato {@code a=2}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code setValue()} restituisce {@code 1} e {@code getValue()} restituisce {@code 2}.</p>
     */
    @Test
    public void setValueReturnsPreviousValue() {
        assertEquals("1", entry.setValue("2"));
        assertEquals("2", entry.getValue());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code setValue()} aggiorni la mappa e le viste collegate.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Non viene controllata soltanto l'entry: il nuovo valore viene cercato
     * anche tramite {@code get()}, {@code values()} ed {@code entrySet()}.
     * Poiché {@code 1} è inizialmente unico, la sua assenza dimostra che il
     * mapping è stato sostituito e non semplicemente affiancato.</p>
     * <p><b>Test Description:</b></p>
     * <p>Imposta {@code 2} attraverso l'entry e osserva il contenuto della mappa
     * e delle due viste interessate.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa e l'entry rappresentano {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il mapping corrente è {@code a=2} e il vecchio valore non è più presente.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La mappa restituisce {@code 2}, le viste contengono il mapping aggiornato
     * e {@code values()} non contiene più {@code 1}.</p>
     */
    @Test
    public void setValueWritesThroughToMapAndViews() {
        HMap.Entry sameEntry = entry;
        sameEntry.setValue("2");
        assertEquals("2", map.get("a"));
        assertTrue(map.values().contains("2"));
        assertTrue(map.entrySet().contains(new EntryStub("a", "2")));
        assertFalse(map.values().contains("1"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il rifiuto di un valore {@code null} assegnato tramite l'entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il tentativo viene eseguito su un mapping già presente, così dopo
     * l'eccezione è possibile controllare che il valore valido non sia stato perso.</p>
     * <p><b>Test Description:</b></p>
     * <p>Invoca {@code entry.setValue(null)} e intercetta l'eccezione prodotta
     * dal vincolo della {@code Hashtable} sottostante.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene il mapping valido {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il mapping {@code a=1} resta invariato.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Viene lanciata {@link NullPointerException} e {@code map.get("a")}
     * continua a restituire {@code 1}.</p>
     */
    @Test
    public void setValueRejectsNullThroughHashtable() {
        try {
            entry.setValue(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("1", map.get("a"));
        }
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica uguaglianza simmetrica e hash coerente tra entry equivalenti.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il confronto usa una nuova {@link EntryStub} con lo stesso mapping,
     * anziché riutilizzare la stessa istanza. La scelta controlla il contratto
     * tra due implementazioni diverse di {@code HMap.Entry}.</p>
     * <p><b>Test Description:</b></p>
     * <p>Confronta le due entry in entrambe le direzioni e poi i rispettivi hash.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Entrambe le entry rappresentano {@code a=1}, ma sono oggetti distinti.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Nessuna entry e nessun mapping vengono modificati.</p>
     * <p><b>Expected Results:</b></p>
     * <p>I due confronti restituiscono {@code true} e i codici hash coincidono.</p>
     */
    @Test
    public void equalEntriesRepresentSameMappingSymmetrically() {
        HMap.Entry other = new EntryStub("a", "1");
        assertTrue(entry.equals(other));
        assertTrue(other.equals(entry));
        assertEquals(entry.hashCode(), other.hashCode());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la proprietà riflessiva dell'uguaglianza di un'entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il confronto con la stessa istanza isola il caso base del contratto di
     * {@code equals()} dagli altri confronti tra mapping.</p>
     * <p><b>Test Description:</b></p>
     * <p>Confronta l'entry della fixture con se stessa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'entry rappresenta il mapping {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La fixture resta invariata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code entry.equals(entry)} restituisce {@code true}.</p>
     */
    @Test
    public void entryEqualityIsReflexive() {
        assertTrue(entry.equals(entry));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica i principali casi in cui due oggetti non rappresentano la stessa entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Sono separati il caso di chiave diversa, quello di valore diverso e i
     * confini di tipo costituiti da {@code null} e da una stringa che ha soltanto
     * la stessa forma testuale dell'entry.</p>
     * <p><b>Test Description:</b></p>
     * <p>Confronta {@code a=1} con {@code b=1}, {@code a=2}, {@code null} e {@code "a=1"}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'entry della fixture rappresenta {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il mapping originale non viene modificato.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Tutti i confronti restituiscono {@code false}.</p>
     */
    @Test
    public void entryEqualityRejectsDifferentMappingsAndOtherObjects() {
        assertFalse(entry.equals(new EntryStub("b", "1")));
        assertFalse(entry.equals(new EntryStub("a", "2")));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("a=1"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la formula del codice hash prevista per una entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le stringhe {@code a} e {@code 1} consentono di calcolare direttamente
     * lo XOR tra gli hash. Il confronto con una entry indipendente controlla
     * anche che la formula non dipenda dalla classe concreta.</p>
     * <p><b>Test Description:</b></p>
     * <p>Calcola {@code "a".hashCode() ^ "1".hashCode()} e lo confronta con
     * l'hash dell'entry e di una {@link EntryStub} equivalente.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'entry rappresenta il mapping {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La fixture non subisce modifiche.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'hash dell'entry coincide sia con il valore calcolato sia con quello dello stub.</p>
     */
    @Test
    public void hashCodeIsKeyHashXorValueHash() {
        int expected = "a".hashCode() ^ "1".hashCode();
        assertEquals(expected, entry.hashCode());
        assertEquals(new EntryStub("a", "1").hashCode(), entry.hashCode());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che l'hash rappresenti il valore corrente dopo {@code setValue()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'hash atteso viene ricalcolato usando {@code updated}; una nuova entry
     * equivalente permette di controllare insieme uguaglianza e coerenza degli hash.</p>
     * <p><b>Test Description:</b></p>
     * <p>Aggiorna il valore, calcola il nuovo XOR e confronta l'entry con uno
     * stub che rappresenta {@code a=updated}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'entry rappresenta inizialmente {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'entry rappresenta {@code a=updated}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il nuovo hash coincide con la formula attesa e con quello dell'entry equivalente.</p>
     */
    @Test
    public void hashCodeTracksValueUpdatedThroughSetValue() {
        entry.setValue("updated");
        int expected = "a".hashCode() ^ "updated".hashCode();
        HMap.Entry equivalent = new EntryStub("a", "updated");
        assertEquals(expected, entry.hashCode());
        assertTrue(entry.equals(equivalent));
        assertEquals(equivalent.hashCode(), entry.hashCode());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il formato testuale {@code chiave=valore} di una entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La rappresentazione viene controllata prima e dopo un aggiornamento,
     * per escludere che la stringa conservi il valore iniziale.</p>
     * <p><b>Test Description:</b></p>
     * <p>Legge {@code toString()}, imposta il valore {@code 2} e legge di nuovo la stringa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'entry rappresenta {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'entry rappresenta {@code a=2}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le due stringhe sono rispettivamente {@code a=1} e {@code a=2}.</p>
     */
    @Test
    public void toStringUsesKeyEqualsValueFormat() {
        assertEquals("a=1", entry.toString());
        entry.setValue("2");
        assertEquals("a=2", entry.toString());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che l'entry di {@link MapAdapter} legga il valore sostituito dalla mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La stessa entry viene conservata mentre {@code map.put()} sostituisce il
     * valore associato alla sua chiave. Questo test documenta una scelta concreta
     * di {@code MapAdapter}: non afferma una garanzia generale di
     * {@code HMap.Entry} dopo modifiche effettuate direttamente sulla mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Sostituisce {@code 1} con {@code updated} attraverso la mappa, poi legge
     * il valore dalla vecchia entry e la confronta con uno stub aggiornato.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'entry è stata ottenuta quando la mappa conteneva {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa contiene {@code a=updated} e la stessa entry espone il nuovo valore.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code getValue()} restituisce {@code updated} e l'entry risulta uguale
     * a una entry indipendente che rappresenta il mapping aggiornato.</p>
     */
    @Test
    public void entryReadsCurrentValueAfterMapReplacement() {
        map.put("a", "updated");
        assertEquals("updated", entry.getValue());
        assertEquals(new EntryStub("a", "updated"), entry);
    }

    /**
     * Implementazione minima e indipendente di {@link HMap.Entry}, usata per
     * costruire mapping attesi senza ottenere una seconda entry dalla mappa.
     * La chiave resta fissa, mentre il valore può essere sostituito localmente;
     * lo stub non è collegato a una {@link MapAdapter}.
     */
    private static final class EntryStub implements HMap.Entry {
        /** Chiave immutabile del mapping rappresentato dallo stub. */
        private final Object key;

        /** Valore corrente del mapping rappresentato dallo stub. */
        private Object value;

        /**
         * Crea una entry di supporto con la coppia indicata.
         *
         * @param entryKey chiave da memorizzare
         * @param entryValue valore iniziale da associare alla chiave
         */
        private EntryStub(Object entryKey, Object entryValue) {
            key = entryKey;
            value = entryValue;
        }

        /**
         * Restituisce la chiave dello stub.
         *
         * @return chiave immutabile del mapping
         */
        public Object getKey() {
            return key;
        }

        /**
         * Restituisce il valore corrente dello stub.
         *
         * @return valore associato alla chiave
         */
        public Object getValue() {
            return value;
        }

        /**
         * Sostituisce localmente il valore dello stub.
         *
         * @param newValue nuovo valore da memorizzare
         * @return valore presente prima della sostituzione
         */
        public Object setValue(Object newValue) {
            Object previous = value;
            value = newValue;
            return previous;
        }

        /**
         * Confronta lo stub con un'altra entry in base a chiave e valore.
         *
         * @param object oggetto da confrontare con questo stub
         * @return {@code true} se l'oggetto è una {@code HMap.Entry} con la
         *         stessa chiave e lo stesso valore, {@code false} altrimenti
         */
        public boolean equals(Object object) {
            if (!(object instanceof HMap.Entry)) {
                return false;
            }
            HMap.Entry other = (HMap.Entry) object;
            return equal(key, other.getKey())
                    && equal(value, other.getValue());
        }

        /**
         * Calcola l'hash come XOR tra l'hash della chiave e quello del valore.
         *
         * @return codice hash coerente con {@link #equals(Object)}
         */
        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * Confronta due riferimenti gestendo esplicitamente il caso {@code null}.
         *
         * @param first primo oggetto da confrontare
         * @param second secondo oggetto da confrontare
         * @return {@code true} se i riferimenti sono entrambi {@code null} o se
         *         il primo oggetto è uguale al secondo
         */
        private static boolean equal(Object first, Object second) {
            return first == null ? second == null : first.equals(second);
        }
    }
}
