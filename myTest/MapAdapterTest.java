package myTest;

import myAdapter.HMap;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <h2>Summary</h2>
 * Test case del contratto principale di HMap.
 *
 * <h2>Test Case Design</h2>
 * Ogni metodo parte da una fixture nuova e verifica valore restituito e stato.
 */
public class MapAdapterTest {
    private HMap map;

    /** Prepara una mappa vuota prima di ciascun test. */
    @Before
    public void setUp() {
        map = new MapAdapter();
    }

    /**
     * <h3>Summary</h3>Verifica lo stato iniziale.
     * <h3>Test Case Design</h3>Osserva dimensione e predicato di vuotezza.
     * <h3>Test Description</h3>Crea la fixture e interroga size/isEmpty.
     * <h3>Pre-Condition</h3>Costruttore completato senza errori.
     * <h3>Post-Condition</h3>La mappa non viene modificata.
     * <h3>Expected Results</h3>Dimensione zero e isEmpty true.
     */
    @Test
    public void newMapIsEmpty() {
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    /**
     * <h3>Summary</h3>Verifica inserimento e sostituzione.
     * <h3>Test Case Design</h3>Usa due put sulla stessa chiave.
     * <h3>Test Description</h3>Inserisce v1, poi v2, e legge il mapping.
     * <h3>Pre-Condition</h3>Mappa vuota.
     * <h3>Post-Condition</h3>Un solo mapping k=v2.
     * <h3>Expected Results</h3>I put restituiscono null e v1; size resta uno.
     */
    @Test
    public void putReturnsPreviousValue() {
        assertNull(map.put("k", "v1"));
        assertEquals("v1", map.put("k", "v2"));
        assertEquals("v2", map.get("k"));
        assertEquals(1, map.size());
    }

    /**
     * <h3>Summary</h3>Verifica containsKey e containsValue.
     * <h3>Test Case Design</h3>Distingue chiavi e valori uguali/non uguali.
     * <h3>Test Description</h3>Inserisce un mapping ed esegue quattro ricerche.
     * <h3>Pre-Condition</h3>Mappa vuota e oggetti non null.
     * <h3>Post-Condition</h3>Il mapping resta invariato.
     * <h3>Expected Results</h3>Solo chiave e valore inseriti sono trovati.
     */
    @Test
    public void containsFindsKeysAndValues() {
        map.put("a", "one");
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("one"));
        assertTrue(map.containsValue("one"));
        assertFalse(map.containsValue("a"));
    }

    /**
     * <h3>Summary</h3>Verifica la rimozione di mapping presenti e assenti.
     * <h3>Test Case Design</h3>Ripete remove sulla stessa chiave.
     * <h3>Test Description</h3>Inserisce, rimuove e rimuove di nuovo.
     * <h3>Pre-Condition</h3>Esiste il mapping k=v.
     * <h3>Post-Condition</h3>La mappa e' vuota.
     * <h3>Expected Results</h3>Le remove restituiscono v e poi null.
     */
    @Test
    public void removeReturnsPreviousValue() {
        map.put("k", "v");
        assertEquals("v", map.remove("k"));
        assertNull(map.remove("k"));
        assertTrue(map.isEmpty());
    }

    /**
     * <h3>Summary</h3>Verifica putAll e clear.
     * <h3>Test Case Design</h3>Copia due mapping da una seconda HMap.
     * <h3>Test Description</h3>Popola source, copia, confronta, poi svuota.
     * <h3>Pre-Condition</h3>Source contiene due mapping validi.
     * <h3>Post-Condition</h3>Target vuoto dopo clear.
     * <h3>Expected Results</h3>La copia e' uguale alla source prima di clear.
     */
    @Test
    public void putAllCopiesAndClearEmpties() {
        HMap source = new MapAdapter();
        source.put("a", "1");
        source.put("b", "2");
        map.putAll(source);
        assertEquals(source, map);
        map.clear();
        assertTrue(map.isEmpty());
    }

    /**
     * <h3>Summary</h3>Verifica il contratto equals/hashCode.
     * <h3>Test Case Design</h3>Inserisce gli stessi mapping in ordine diverso.
     * <h3>Test Description</h3>Confronta due mappe e i relativi hash code.
     * <h3>Pre-Condition</h3>Mappe indipendenti con mapping uguali.
     * <h3>Post-Condition</h3>Nessuna modifica.
     * <h3>Expected Results</h3>Equals simmetrico e hash code uguali.
     */
    @Test
    public void equalMapsHaveEqualHashCodes() {
        HMap other = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
        other.put("b", "2");
        other.put("a", "1");
        assertEquals(map, other);
        assertEquals(other, map);
        assertEquals(map.hashCode(), other.hashCode());
    }

    /**
     * <h3>Summary</h3>Verifica il rifiuto di chiavi null.
     * <h3>Test Case Design</h3>Usa il vincolo ereditato da Hashtable CLDC.
     * <h3>Test Description</h3>Esegue put con chiave null.
     * <h3>Pre-Condition</h3>Mappa vuota.
     * <h3>Post-Condition</h3>Nessun mapping inserito.
     * <h3>Expected Results</h3>Viene sollevata NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void nullKeyIsRejected() {
        map.put(null, "value");
    }
}

