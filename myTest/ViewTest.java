package myTest;

import myAdapter.HCollection;
import myAdapter.HIterator;
import myAdapter.HMap;
import myAdapter.HSet;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <h2>Summary</h2>Test case delle tre viste backed.
 * <h2>Test Case Design</h2>Le viste sono acquisite prima delle modifiche.
 */
public class ViewTest {
    private HMap map;

    /** Prepara una mappa con due mapping. */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
    }

    /**
     * <h3>Summary</h3>Verifica che keySet sia una vista viva.
     * <h3>Test Case Design</h3>Modifica la mappa dopo aver acquisito la vista.
     * <h3>Test Description</h3>Ottiene keySet, aggiunge c e rimuove a.
     * <h3>Pre-Condition</h3>Mappa con a e b.
     * <h3>Post-Condition</h3>Mappa e vista contengono b e c.
     * <h3>Expected Results</h3>La vista riflette entrambe le modifiche.
     */
    @Test
    public void keySetIsBackedByMap() {
        HSet keys = map.keySet();
        map.put("c", "3");
        assertTrue(keys.contains("c"));
        assertTrue(keys.remove("a"));
        assertFalse(map.containsKey("a"));
    }

    /**
     * <h3>Summary</h3>Verifica la rimozione tramite values.
     * <h3>Test Case Design</h3>Rimuove un valore dalla vista.
     * <h3>Test Description</h3>Chiama values.remove("1") e controlla la chiave.
     * <h3>Pre-Condition</h3>Esiste a=1.
     * <h3>Post-Condition</h3>a=1 non esiste piu'.
     * <h3>Expected Results</h3>remove true e chiave a assente.
     */
    @Test
    public void valuesRemovalUpdatesMap() {
        HCollection values = map.values();
        assertTrue(values.remove("1"));
        assertFalse(map.containsKey("a"));
    }

    /**
     * <h3>Summary</h3>Verifica setValue su un entry backed.
     * <h3>Test Case Design</h3>Modifica il primo entry ottenuto dall'iteratore.
     * <h3>Test Description</h3>Legge chiave e valore, imposta x e rilegge la map.
     * <h3>Pre-Condition</h3>Mappa non vuota.
     * <h3>Post-Condition</h3>Il mapping osservato vale x.
     * <h3>Expected Results</h3>setValue restituisce il valore precedente.
     */
    @Test
    public void entrySetValueUpdatesMap() {
        HMap.Entry entry = (HMap.Entry) map.entrySet().iterator().next();
        Object old = entry.getValue();
        assertEquals(old, entry.setValue("x"));
        assertEquals("x", map.get(entry.getKey()));
    }

    /**
     * <h3>Summary</h3>Verifica removeAll e retainAll sulle viste.
     * <h3>Test Case Design</h3>Usa un keySet di appoggio come argomento.
     * <h3>Test Description</h3>Rimuove a, poi conserva soltanto b.
     * <h3>Pre-Condition</h3>Mappa con a, b, c.
     * <h3>Post-Condition</h3>Resta soltanto b.
     * <h3>Expected Results</h3>Entrambe le operazioni modificano la mappa.
     */
    @Test
    public void bulkRemovalOperationsUpdateMap() {
        HMap selected = new MapAdapter();
        selected.put("a", "x");
        assertTrue(map.keySet().removeAll(selected.keySet()));
        map.put("c", "3");
        selected.clear();
        selected.put("b", "x");
        assertTrue(map.keySet().retainAll(selected.keySet()));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("b"));
    }

    /**
     * <h3>Summary</h3>Verifica il contratto toArray.</h3>
     * <h3>Test Case Design</h3>Controlla lunghezza e riuso di un array capiente.
     * <h3>Test Description</h3>Converte keySet con entrambe le overload.
     * <h3>Pre-Condition</h3>Vista con due elementi.
     * <h3>Post-Condition</h3>Nessuna modifica alla mappa.
     * <h3>Expected Results</h3>Due elementi; array fornito riusato e terminato da null.
     */
    @Test
    public void toArrayReturnsAllElementsAndReusesLargeArray() {
        assertEquals(2, map.keySet().toArray().length);
        Object[] supplied = new Object[3];
        Object[] result = map.keySet().toArray(supplied);
        assertSame(supplied, result);
        assertNull(result[2]);
    }

    /**
     * <h3>Summary</h3>Verifica clear tramite vista.
     * <h3>Test Case Design</h3>Svuota la vista values.
     * <h3>Test Description</h3>Acquisisce values e invoca clear.
     * <h3>Pre-Condition</h3>Mappa con due mapping.
     * <h3>Post-Condition</h3>Mappa e tutte le viste vuote.
     * <h3>Expected Results</h3>size della mappa uguale a zero.
     */
    @Test
    public void clearingViewClearsMap() {
        map.values().clear();
        assertTrue(map.isEmpty());
        assertTrue(map.keySet().isEmpty());
    }
}

