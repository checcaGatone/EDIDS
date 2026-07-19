package myTest;

import myAdapter.HMap;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <h2>Summary</h2>
 * Verifica il contratto principale di HMap implementato da MapAdapter.
 *
 * <h2>Test Case Design</h2>
 * Ogni test utilizza una fixture nuova e controlla sia il valore restituito sia
 * lo stato osservabile della mappa, senza assumere l'ordine di Hashtable.
 */
public class MapAdapterTest {
    private HMap map;

    @Before
    public void setUp() {
        map = new MapAdapter();
    }

    /**
     * <h3>Summary</h3>Verifica lo stato iniziale.
     * <h3>Test Case Design</h3>Interroga una nuova istanza senza modificarla.
     * <h3>Test Description</h3>Controlla size e isEmpty subito dopo la costruzione.
     * <h3>Pre-Condition</h3>Nessuna.
     * <h3>Post-Condition</h3>La mappa resta vuota.
     * <h3>Expected Results</h3>size vale zero e isEmpty vale true.
     */
    @Test
    public void newMapIsEmpty() {
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    /**
     * <h3>Summary</h3>Verifica l'inserimento di un nuovo mapping.
     * <h3>Test Case Design</h3>Inserisce una coppia non presente e la rilegge.
     * <h3>Test Description</h3>Esegue put, get, size e isEmpty.
     * <h3>Pre-Condition</h3>Mappa vuota; chiave e valore non null.
     * <h3>Post-Condition</h3>La mappa contiene un mapping.
     * <h3>Expected Results</h3>put restituisce null e get restituisce il valore inserito.
     */
    @Test
    public void putAddsMappingAndGetReturnsValue() {
        assertNull(map.put("a", "1"));
        assertEquals("1", map.get("a"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
    }

    /**
     * <h3>Summary</h3>Verifica la sostituzione di un valore.
     * <h3>Test Case Design</h3>Usa due put sulla stessa chiave.
     * <h3>Test Description</h3>Inserisce a=1 e sostituisce il valore con 2.
     * <h3>Pre-Condition</h3>Esiste il mapping a=1.
     * <h3>Post-Condition</h3>Esiste soltanto a=2.
     * <h3>Expected Results</h3>Il secondo put restituisce 1 e size resta uno.
     */
    @Test
    public void putReplacementReturnsPreviousValue() {
        map.put("a", "1");
        assertEquals("1", map.put("a", "2"));
        assertEquals("2", map.get("a"));
        assertEquals(1, map.size());
    }

    /**
     * <h3>Summary</h3>Verifica il rifiuto delle chiavi null.
     * <h3>Test Case Design</h3>Esercita il vincolo dell'adaptee Hashtable.
     * <h3>Test Description</h3>Invoca put con chiave null e valore valido.
     * <h3>Pre-Condition</h3>Mappa vuota.
     * <h3>Post-Condition</h3>Nessun mapping viene aggiunto.
     * <h3>Expected Results</h3>Viene sollevata NullPointerException.
     */
    @Test
    public void putRejectsNullKey() {
        try {
            map.put(null, "1");
            fail();
        } catch (NullPointerException expected) {
            assertTrue(map.isEmpty());
        }
    }

    /**
     * <h3>Summary</h3>Verifica il rifiuto dei valori null.
     * <h3>Test Case Design</h3>Esercita il vincolo dell'adaptee Hashtable.
     * <h3>Test Description</h3>Invoca put con chiave valida e valore null.
     * <h3>Pre-Condition</h3>Mappa vuota.
     * <h3>Post-Condition</h3>Nessun mapping viene aggiunto.
     * <h3>Expected Results</h3>Viene sollevata NullPointerException.
     */
    @Test
    public void putRejectsNullValue() {
        try {
            map.put("a", null);
            fail();
        } catch (NullPointerException expected) {
            assertTrue(map.isEmpty());
        }
    }

    /**
     * <h3>Summary</h3>Verifica get per una chiave assente.
     * <h3>Test Case Design</h3>Interroga una mappa vuota.
     * <h3>Test Description</h3>Chiama get con una chiave valida non presente.
     * <h3>Pre-Condition</h3>Mappa vuota.
     * <h3>Post-Condition</h3>La mappa resta vuota.
     * <h3>Expected Results</h3>get restituisce null.
     */
    @Test
    public void getReturnsNullForAbsentKey() {
        assertNull(map.get("missing"));
        assertTrue(map.isEmpty());
    }

    /**
     * <h3>Summary</h3>Verifica containsKey.
     * <h3>Test Case Design</h3>Distingue la chiave dal valore dello stesso mapping.
     * <h3>Test Description</h3>Inserisce key=value e cerca entrambi come chiavi.
     * <h3>Pre-Condition</h3>Mappa vuota.
     * <h3>Post-Condition</h3>Il mapping resta invariato.
     * <h3>Expected Results</h3>Solo key viene riconosciuta come chiave.
     */
    @Test
    public void containsKeyDistinguishesKeyFromValue() {
        map.put("key", "value");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("value"));
    }

    /**
     * <h3>Summary</h3>Verifica containsValue.
     * <h3>Test Case Design</h3>Distingue il valore dalla chiave dello stesso mapping.
     * <h3>Test Description</h3>Inserisce key=value e cerca entrambi come valori.
     * <h3>Pre-Condition</h3>Mappa vuota.
     * <h3>Post-Condition</h3>Il mapping resta invariato.
     * <h3>Expected Results</h3>Solo value viene riconosciuto come valore.
     */
    @Test
    public void containsValueDistinguishesValueFromKey() {
        map.put("key", "value");
        assertTrue(map.containsValue("value"));
        assertFalse(map.containsValue("key"));
    }

    /**
     * <h3>Summary</h3>Verifica containsValue con null.
     * <h3>Test Case Design</h3>Propaga il vincolo di Hashtable sui valori null.
     * <h3>Test Description</h3>Invoca containsValue(null).
     * <h3>Pre-Condition</h3>Mappa valida.
     * <h3>Post-Condition</h3>La mappa non viene modificata.
     * <h3>Expected Results</h3>Viene sollevata NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void containsValueRejectsNull() {
        map.containsValue(null);
    }

    @Test
    public void getNullKeyThrowsAndPreservesMap() {
        map.put("a", "1");
        try {
            map.get(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(1, map.size());
            assertEquals("1", map.get("a"));
        }
    }

    @Test
    public void containsKeyNullThrowsAndPreservesMap() {
        map.put("a", "1");
        try {
            map.containsKey(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(1, map.size());
            assertEquals("1", map.get("a"));
        }
    }

    @Test
    public void removeNullKeyThrowsAndPreservesMap() {
        map.put("a", "1");
        try {
            map.remove(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(1, map.size());
            assertEquals("1", map.get("a"));
        }
    }

    /**
     * <h3>Summary</h3>Verifica remove su un mapping presente.
     * <h3>Test Case Design</h3>Controlla valore restituito e stato finale.
     * <h3>Test Description</h3>Inserisce a=1 e rimuove a.
     * <h3>Pre-Condition</h3>Esiste a=1.
     * <h3>Post-Condition</h3>La mappa è vuota.
     * <h3>Expected Results</h3>remove restituisce 1 e la chiave scompare.
     */
    @Test
    public void removePresentMappingReturnsValueAndDeletesIt() {
        map.put("a", "1");
        assertEquals("1", map.remove("a"));
        assertFalse(map.containsKey("a"));
        assertTrue(map.isEmpty());
    }

    /**
     * <h3>Summary</h3>Verifica remove su una chiave assente.
     * <h3>Test Case Design</h3>Rimuove una chiave diversa da quella memorizzata.
     * <h3>Test Description</h3>Inserisce a=1 e chiama remove su b.
     * <h3>Pre-Condition</h3>Esiste soltanto a=1.
     * <h3>Post-Condition</h3>a=1 resta presente.
     * <h3>Expected Results</h3>remove restituisce null e size non cambia.
     */
    @Test
    public void removeAbsentMappingReturnsNullWithoutChanges() {
        map.put("a", "1");
        assertNull(map.remove("b"));
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));
    }

    /**
     * <h3>Summary</h3>Verifica clear.
     * <h3>Test Case Design</h3>Svuota una mappa contenente più mapping.
     * <h3>Test Description</h3>Inserisce due coppie e invoca clear.
     * <h3>Pre-Condition</h3>La mappa contiene due mapping.
     * <h3>Post-Condition</h3>La mappa è vuota.
     * <h3>Expected Results</h3>size vale zero e le chiavi non sono presenti.
     */
    @Test
    public void clearRemovesEveryMapping() {
        map.put("a", "1");
        map.put("b", "2");
        map.clear();
        assertEquals(0, map.size());
        assertFalse(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
    }

    /**
     * <h3>Summary</h3>Verifica putAll.
     * <h3>Test Case Design</h3>Copia mapping in una destinazione con una chiave sovrapposta.
     * <h3>Test Description</h3>Sostituisce il valore associato alla chiave comune e conserva il mapping estraneo.
     * <h3>Pre-Condition</h3>Sorgente e destinazione valide.
     * <h3>Post-Condition</h3>La destinazione contiene tutti i mapping.
     * <h3>Expected Results</h3>Le tre coppie sono accessibili nella destinazione.
     */
    @Test
    public void putAllCopiesEveryMapping() {
        HMap source = new MapAdapter();
        source.put("a", "1");
        source.put("b", "2");
        map.put("a", "old");
        map.put("c", "3");
        map.putAll(source);
        assertEquals(3, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
    }

    /**
     * <h3>Summary</h3>Verifica putAll con argomento null.
     * <h3>Test Case Design</h3>Passa un riferimento HMap nullo.
     * <h3>Test Description</h3>Invoca putAll(null) su una mappa vuota.
     * <h3>Pre-Condition</h3>Mappa valida.
     * <h3>Post-Condition</h3>La mappa resta vuota.
     * <h3>Expected Results</h3>Viene sollevata NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void putAllRejectsNullMap() {
        map.putAll(null);
    }

    /**
     * <h3>Summary</h3>Verifica putAll con la mappa stessa.
     * <h3>Test Case Design</h3>Controlla l'auto-copia senza modifiche strutturali.
     * <h3>Test Description</h3>Popola la mappa e passa la stessa istanza a putAll.
     * <h3>Pre-Condition</h3>Due mapping presenti.
     * <h3>Post-Condition</h3>I mapping restano invariati.
     * <h3>Expected Results</h3>Dimensione e valori non cambiano.
     */
    @Test
    public void putAllWithSameMapIsStable() {
        map.put("a", "1");
        map.put("b", "2");
        map.putAll(map);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    @Test
    public void copyingEmptyMapPreservesEmptyContent() {
        HMap empty = new MapAdapter();
        map.putAll(empty);
        assertTrue(map.isEmpty());
        HMap copy = new MapAdapter(empty);
        assertTrue(copy.isEmpty());
        assertEquals(empty, copy);
    }

    /**
     * <h3>Summary</h3>Verifica il costruttore di copia.
     * <h3>Test Case Design</h3>Confronta contenuto e indipendenza delle istanze.
     * <h3>Test Description</h3>Copia la mappa, poi modifica soltanto l'originale.
     * <h3>Pre-Condition</h3>La sorgente contiene due mapping.
     * <h3>Post-Condition</h3>La copia conserva il contenuto iniziale.
     * <h3>Expected Results</h3>Le mappe sono inizialmente uguali ma indipendenti.
     */
    @Test
    public void copyConstructorCreatesIndependentEqualMap() {
        map.put("a", "1");
        map.put("b", "2");
        HMap copy = new MapAdapter(map);
        assertEquals(map, copy);
        map.remove("a");
        assertTrue(copy.containsKey("a"));
        assertEquals(2, copy.size());
    }

    @Test
    public void copyConstructorDoesNotCallOverridablePutBeforeInitialization() {
        HMap source = new MapAdapter();
        source.put("a", "1");
        source.put("b", "2");

        HMap copy = new InitializationGuardMapAdapter(source);

        assertEquals(source, copy);
        assertEquals("1", copy.get("a"));
        assertEquals("2", copy.get("b"));

        source.put("c", "3");
        assertFalse(copy.containsKey("c"));
        assertEquals(2, copy.size());
    }

    /**
     * <h3>Summary</h3>Verifica il costruttore di copia con null.
     * <h3>Test Case Design</h3>Passa esplicitamente una HMap nulla.
     * <h3>Test Description</h3>Costruisce MapAdapter con argomento null.
     * <h3>Pre-Condition</h3>Nessuna.
     * <h3>Post-Condition</h3>Nessuna istanza valida viene prodotta.
     * <h3>Expected Results</h3>Viene sollevata NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void copyConstructorRejectsNullMap() {
        new MapAdapter((HMap) null);
    }

    /**
     * <h3>Summary</h3>Verifica equals indipendentemente dall'ordine.
     * <h3>Test Case Design</h3>Usa tre mappe popolate in ordini diversi.
     * <h3>Test Description</h3>Controlla riflessività, simmetria e transitività.
     * <h3>Pre-Condition</h3>Le mappe contengono gli stessi mapping.
     * <h3>Post-Condition</h3>Nessuna mappa viene modificata.
     * <h3>Expected Results</h3>Tutti i confronti di uguaglianza risultano veri.
     */
    @Test
    public void equalsIsReflexiveSymmetricAndTransitive() {
        HMap second = new MapAdapter();
        HMap third = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
        second.put("b", "2");
        second.put("a", "1");
        third.put("a", "1");
        third.put("b", "2");
        assertTrue(map.equals(map));
        assertTrue(map.equals(second));
        assertTrue(second.equals(map));
        assertTrue(second.equals(third));
        assertTrue(map.equals(third));
    }

    /**
     * <h3>Summary</h3>Verifica equals con contenuti o tipi diversi.
     * <h3>Test Case Design</h3>Confronta mappe della stessa dimensione con chiavi o valori diversi.
     * <h3>Test Description</h3>Confronta con due HMap diverse, null e una String.
     * <h3>Pre-Condition</h3>Le mappe di confronto contengono un mapping.
     * <h3>Post-Condition</h3>Gli oggetti restano invariati.
     * <h3>Expected Results</h3>Tutti i confronti restituiscono false.
     */
    @Test
    public void equalsRejectsDifferentMappingsNullAndOtherTypes() {
        HMap other = new MapAdapter();
        HMap sameKeyDifferentValue = new MapAdapter();
        map.put("a", "1");
        other.put("b", "1");
        sameKeyDifferentValue.put("a", "different");
        assertFalse(map.equals(other));
        assertFalse(map.equals(sameKeyDifferentValue));
        assertFalse(map.equals(null));
        assertFalse(map.equals("not a map"));
    }

    /**
     * <h3>Summary</h3>Verifica il contratto hashCode della mappa.
     * <h3>Test Case Design</h3>Calcola la somma degli hash delle entry.
     * <h3>Test Description</h3>Inserisce due mapping e confronta l'hash atteso.
     * <h3>Pre-Condition</h3>Chiavi e valori hanno hashCode deterministico.
     * <h3>Post-Condition</h3>La mappa resta invariata.
     * <h3>Expected Results</h3>L'hash coincide con la somma degli XOR.
     */
    @Test
    public void hashCodeIsSumOfEntryHashCodes() {
        map.put("a", "1");
        map.put("b", "2");
        int expected = ("a".hashCode() ^ "1".hashCode())
                + ("b".hashCode() ^ "2".hashCode());
        assertEquals(expected, map.hashCode());
    }

    /**
     * <h3>Summary</h3>Verifica hashCode per mappe uguali.
     * <h3>Test Case Design</h3>Popola due mappe nello stesso contenuto in ordine diverso.
     * <h3>Test Description</h3>Confronta uguaglianza e codici hash.
     * <h3>Pre-Condition</h3>Le mappe contengono gli stessi mapping.
     * <h3>Post-Condition</h3>Le mappe restano invariate.
     * <h3>Expected Results</h3>Mappe uguali hanno lo stesso hashCode.
     */
    @Test
    public void equalMapsHaveEqualHashCodes() {
        HMap other = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
        other.put("b", "2");
        other.put("a", "1");
        assertEquals(map, other);
        assertEquals(map.hashCode(), other.hashCode());
    }

    /**
     * <h3>Summary</h3>Verifica la rappresentazione testuale.
     * <h3>Test Case Design</h3>Non assume l'ordine prodotto da Hashtable.
     * <h3>Test Description</h3>Inserisce due mapping e cerca entrambe le coppie.
     * <h3>Pre-Condition</h3>La mappa contiene a=1 e b=2.
     * <h3>Post-Condition</h3>La mappa resta invariata.
     * <h3>Expected Results</h3>La stringa contiene entrambe le forme chiave=valore.
     */
    @Test
    public void toStringContainsEveryMapping() {
        map.put("a", "1");
        map.put("b", "2");
        String representation = map.toString();
        assertTrue(representation.indexOf("a=1") >= 0);
        assertTrue(representation.indexOf("b=2") >= 0);
    }

    private static final class InitializationGuardMapAdapter
            extends MapAdapter {
        private boolean initialized;

        private InitializationGuardMapAdapter(HMap source) {
            super(source);
            initialized = true;
        }

        public Object put(Object key, Object value) {
            if (!initialized) {
                throw new AssertionError(
                        "put invocato prima dell'inizializzazione");
            }
            return super.put(key, value);
        }
    }
}
