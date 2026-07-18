package myTest;

import myAdapter.HIterator;
import myAdapter.HMap;
import myAdapter.MapAdapter;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <h2>Summary</h2>Test case degli iteratori delle viste.
 * <h2>Test Case Design</h2>I test non assumono l'ordine di Hashtable.
 */
public class IteratorTest {
    private HMap map;

    /** Prepara una mappa con due mapping. */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
    }

    /**
     * <h3>Summary</h3>Verifica attraversamento e terminazione.
     * <h3>Test Case Design</h3>Conta gli elementi senza assumere l'ordine.
     * <h3>Test Description</h3>Consuma keySet.iterator fino a hasNext false.
     * <h3>Pre-Condition</h3>Due mapping presenti.
     * <h3>Post-Condition</h3>Mappa invariata.
     * <h3>Expected Results</h3>Due elementi distinti e iteratore terminato.
     */
    @Test
    public void iteratorVisitsEveryElementOnce() {
        HIterator iterator = map.keySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            assertTrue(map.containsKey(iterator.next()));
            count++;
        }
        assertEquals(2, count);
        assertFalse(iterator.hasNext());
    }

    /**
     * <h3>Summary</h3>Verifica iterator.remove.</h3>
     * <h3>Test Case Design</h3>Rimuove l'elemento appena restituito.
     * <h3>Test Description</h3>next, remove e controllo della dimensione.
     * <h3>Pre-Condition</h3>Iteratore con almeno un elemento.
     * <h3>Post-Condition</h3>Un mapping resta nella mappa.
     * <h3>Expected Results</h3>La chiave restituita non e' piu' presente.
     */
    @Test
    public void iteratorRemoveUpdatesMap() {
        HIterator iterator = map.keySet().iterator();
        Object key = iterator.next();
        iterator.remove();
        assertFalse(map.containsKey(key));
        assertEquals(1, map.size());
    }

    /**
     * <h3>Summary</h3>Verifica remove prima di next.
     * <h3>Test Case Design</h3>Esercita lo stato iniziale illegale.
     * <h3>Test Description</h3>Crea l'iteratore e invoca subito remove.
     * <h3>Pre-Condition</h3>Nessuna chiamata a next.
     * <h3>Post-Condition</h3>Mappa invariata.
     * <h3>Expected Results</h3>IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void removeBeforeNextIsIllegal() {
        map.keySet().iterator().remove();
    }

    /**
     * <h3>Summary</h3>Verifica la seconda remove consecutiva.
     * <h3>Test Case Design</h3>Invoca remove due volte dopo un solo next.
     * <h3>Test Description</h3>next, remove, remove.
     * <h3>Pre-Condition</h3>Iteratore non vuoto.
     * <h3>Post-Condition</h3>Un solo mapping rimosso.
     * <h3>Expected Results</h3>La seconda remove solleva IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void removeTwiceIsIllegal() {
        HIterator iterator = map.values().iterator();
        iterator.next();
        iterator.remove();
        iterator.remove();
    }

    /**
     * <h3>Summary</h3>Verifica next dopo l'esaurimento.
     * <h3>Test Case Design</h3>Consuma tutti gli elementi e chiama next.
     * <h3>Test Description</h3>Ciclo while(hasNext), quindi next.
     * <h3>Pre-Condition</h3>Iteratore valido.
     * <h3>Post-Condition</h3>Mappa invariata.
     * <h3>Expected Results</h3>NoSuchElementException.
     */
    @Test(expected = NoSuchElementException.class)
    public void nextAfterEndThrows() {
        HIterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        iterator.next();
    }
}

