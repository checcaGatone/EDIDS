package myTest;

import myAdapter.HCollection;
import myAdapter.HMap;
import myAdapter.HSet;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case dedicato alle viste restituite da {@link MapAdapter#keySet()},
 * {@link MapAdapter#values()} e {@link MapAdapter#entrySet()}.
 *
 * <h2>Summary</h2>
 * <p>
 * La classe verifica la parte del contratto J2SE 1.4.2 relativa alle viste di
 * una mappa: collegamento con la mappa originale, operazioni di rimozione,
 * operazioni bulk, uguaglianza degli insiemi, gestione di {@code null},
 * operazioni di aggiunta non supportate e conversione in array. I controlli
 * distinguono {@code keySet()} ed {@code entrySet()}, che hanno semantica da
 * insieme, da {@code values()}, che può contenere valori duplicati.
 * </p>
 *
 * <h2>Test Case Design</h2>
 * <p>
 * Ogni test parte da una nuova mappa con i mapping {@code a=1} e {@code b=2}.
 * Due elementi sono sufficienti per verificare sia modifiche parziali sia
 * operazioni che svuotano la struttura; quando servono duplicati viene
 * aggiunto {@code c=1}. Le asserzioni controllano insieme valore restituito,
 * contenuto della vista e stato della mappa, perché una vista backed non deve
 * aggiornarsi in modo indipendente. I confronti non assumono mai l'ordine di
 * iterazione della {@code Hashtable}. Oggetti di supporto con
 * {@code equals()} asimmetrico permettono inoltre di verificare la direzione
 * esatta del confronto, senza basarsi soltanto su {@link String}.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see MapAdapter
 * @see HCollection
 * @see HSet
 */
public class ViewsTest {
    /** Mappa ricreata prima di ogni test con i mapping base della fixture. */
    private HMap map;

    /**
     * Prepara una fixture indipendente contenente {@code a=1} e {@code b=2}.
     * La ricostruzione evita che una rimozione effettuata da un test influenzi
     * quelli eseguiti successivamente.
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che un inserimento nella mappa sia visibile nelle viste già ottenute.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le tre viste vengono salvate prima di inserire {@code c=3}, così il
     * test distingue una vista backed da una copia creata al momento della chiamata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Inserisce il nuovo mapping e ne cerca chiave, valore ed entry nelle
     * rispettive viste preesistenti.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene {@code a=1} e {@code b=2}; le tre viste sono già state create.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa contiene anche {@code c=3} e tutte le viste lo rappresentano.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le tre ricerche restituiscono {@code true}.</p>
     */
    @Test
    public void mapPutIsVisibleInPreviouslyObtainedViews() {
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        map.put("c", "3");
        assertTrue(keys.contains("c"));
        assertTrue(values.contains("3"));
        assertTrue(entries.contains(new EntryStub("c", "3")));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la propagazione di {@code remove()} e {@code clear()} alle viste esistenti.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le viste sono ottenute una sola volta e riutilizzate dopo entrambe le
     * modifiche, per controllare che restino collegate alla stessa mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Rimuove {@code a=1}, controlla la sua assenza e poi svuota la mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene due mapping e le tre viste sono già disponibili.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa e tutte le viste sono vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il mapping rimosso scompare da ogni vista e, dopo {@code clear()},
     * ciascuna vista segnala lo stato vuoto.</p>
     */
    @Test
    public void mapRemoveAndClearAreVisibleInExistingViews() {
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        map.remove("a");
        assertFalse(keys.contains("a"));
        assertFalse(values.contains("1"));
        assertFalse(entries.contains(new EntryStub("a", "1")));
        map.clear();
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che la rimozione da {@code keySet()} aggiorni la mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Confronta una chiave presente con una assente e controlla sia il
     * valore booleano sia la dimensione finale.</p>
     * <p><b>Test Description:</b></p>
     * <p>Rimuove {@code a} dalla vista e tenta poi di rimuovere {@code missing}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene le chiavi {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Rimane soltanto il mapping associato a {@code b}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La prima rimozione restituisce {@code true}, la seconda {@code false}.</p>
     */
    @Test
    public void keySetRemoveUpdatesMap() {
        assertTrue(map.keySet().remove("a"));
        assertFalse(map.containsKey("a"));
        assertEquals(1, map.size());
        assertFalse(map.keySet().remove("missing"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il comportamento di {@code keySet().contains(null)}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La mappa è popolata prima della chiamata per controllare anche
     * l'assenza di modifiche collaterali dovute all'errore.</p>
     * <p><b>Test Description:</b></p>
     * <p>Ricerca una chiave {@code null} e intercetta l'eccezione prodotta
     * dalla {@code Hashtable} sottostante.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Sono presenti i due mapping validi della fixture.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>I due mapping restano invariati.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Viene lanciata {@link NullPointerException} senza alterare la mappa.</p>
     */
    @Test
    public void keySetContainsNullThrowsAndPreservesMap() {
        try {
            map.keySet().contains(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(2, map.size());
            assertEquals("1", map.get("a"));
            assertEquals("2", map.get("b"));
        }
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il comportamento di {@code keySet().remove(null)}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Oltre al tipo di eccezione vengono ricontrollati entrambi i mapping,
     * così un errore non può lasciare una modifica parziale.</p>
     * <p><b>Test Description:</b></p>
     * <p>Tenta di rimuovere la chiave {@code null} dalla vista delle chiavi.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La fixture conserva dimensione e contenuto iniziali.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Viene lanciata {@link NullPointerException} e nessun mapping è rimosso.</p>
     */
    @Test
    public void keySetRemoveNullThrowsAndPreservesMap() {
        try {
            map.keySet().remove(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(2, map.size());
            assertEquals("1", map.get("a"));
            assertEquals("2", map.get("b"));
        }
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la rimozione di una sola occorrenza da {@code values()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il valore {@code 1} è associato a due chiavi per mostrare la
     * differenza tra una collection di valori e un insieme.</p>
     * <p><b>Test Description:</b></p>
     * <p>Rimuove {@code 1}, verifica che un duplicato rimanga e prova infine
     * a rimuovere un valore assente.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Tre mapping sono presenti e due hanno valore {@code 1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Rimangono due mapping, uno dei quali conserva il valore {@code 1}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La rimozione presente restituisce {@code true}; quella assente restituisce {@code false}.</p>
     */
    @Test
    public void valuesRemoveDeletesOnlyOneDuplicateMapping() {
        map.put("c", "1");
        assertTrue(map.values().remove("1"));
        assertEquals(2, map.size());
        assertTrue(map.containsValue("1"));
        assertFalse(map.values().remove("missing"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la direzione di {@code equals()} usata da {@code values().remove()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il valore memorizzato rifiuta ogni confronto, mentre l'argomento di
     * rimozione riconosce quel tipo: solo la direzione prescritta può riuscire.</p>
     * <p><b>Test Description:</b></p>
     * <p>Inserisce un valore sentinella e lo rimuove con un probe diverso ma corrispondente.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene un solo mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa locale è vuota.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code remove()} restituisce {@code true} perché invoca
     * {@code equals()} sull'argomento ricevuto.</p>
     */
    @Test
    public void valuesRemoveInvokesEqualsOnRemovalArgument() {
        HMap local = new MapAdapter();
        local.put("key", new StoredRejectingValue());

        assertTrue(local.values().remove(new MatchingRemovalProbe()));
        assertTrue(local.isEmpty());
        assertFalse(local.containsKey("key"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Esclude che {@code values().remove()} usi il confronto nella direzione inversa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il valore memorizzato riconosce il probe, ma il probe non riconosce il
     * valore: il risultato cambierebbe se fosse interrogato l'oggetto sbagliato.</p>
     * <p><b>Test Description:</b></p>
     * <p>Tenta la rimozione e verifica identità e presenza del valore originale.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene il solo valore con confronto asimmetrico.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il mapping originale rimane invariato.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code remove()} restituisce {@code false} e conserva lo stesso riferimento.</p>
     */
    @Test
    public void valuesRemoveDoesNotInvokeEqualsOnStoredValue() {
        HMap local = new MapAdapter();
        Object stored = new StoredMatchingValue();
        local.put("key", stored);

        assertFalse(local.values().remove(new RejectingRemovalProbe()));
        assertEquals(1, local.size());
        assertSame(stored, local.get("key"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la direzione di {@code equals()} nella vista {@code values()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Un confronto asimmetrico riesce soltanto dal valore cercato verso il
     * valore memorizzato, rendendo osservabile la direzione della chiamata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Cerca dalla vista un probe che riconosce il riferimento memorizzato.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene un solo mapping con valore non {@code null}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dimensione e riferimento memorizzato restano invariati.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code values().contains()} restituisce {@code true}, coerentemente
     * con la delega a {@link HMap#containsValue(Object)}.</p>
     */
    @Test
    public void valuesContainsUsesContainsValueEqualityDirection() {
        HMap local = new MapAdapter();
        Object stored = new Object();
        local.put("key", stored);

        assertTrue(local.values().contains(
                new AsymmetricContainsProbe(stored)));
        assertEquals(1, local.size());
        assertSame(stored, local.get("key"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica i criteri di appartenenza di {@code entrySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Confronta una entry equivalente con entry che differiscono per valore
     * o chiave e con un oggetto che non implementa {@link HMap.Entry}.</p>
     * <p><b>Test Description:</b></p>
     * <p>Interroga la vista con quattro rappresentanti dei principali casi di confronto.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene il mapping {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa non viene modificata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Soltanto l'entry con chiave e valore corrispondenti viene riconosciuta.</p>
     */
    @Test
    public void entrySetContainsRequiresMatchingKeyAndValue() {
        assertTrue(map.entrySet().contains(new EntryStub("a", "1")));
        assertFalse(map.entrySet().contains(new EntryStub("a", "2")));
        assertFalse(map.entrySet().contains(new EntryStub("missing", "1")));
        assertFalse(map.entrySet().contains("a=1"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code entrySet().remove()} richieda la corrispondenza completa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Due entry hanno la stessa chiave, ma solo una possiede anche il valore
     * corrente; in questo modo si evita una rimozione basata sulla sola chiave.</p>
     * <p><b>Test Description:</b></p>
     * <p>Tenta prima la rimozione di {@code a=2} e poi quella di {@code a=1}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Il mapping corrente per {@code a} è {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo il secondo tentativo la chiave {@code a} non è più presente.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il primo tentativo restituisce {@code false}; il secondo restituisce {@code true}.</p>
     */
    @Test
    public void entrySetRemoveRequiresMatchingKeyAndValue() {
        assertFalse(map.entrySet().remove(new EntryStub("a", "2")));
        assertTrue(map.containsKey("a"));
        assertTrue(map.entrySet().remove(new EntryStub("a", "1")));
        assertFalse(map.containsKey("a"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code clear()} sulla vista dei valori svuoti la mappa backing.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le tre viste sono conservate prima dello svuotamento e controllate
     * dopo l'operazione, così si osserva la propagazione in entrambe le direzioni.</p>
     * <p><b>Test Description:</b></p>
     * <p>Invoca {@code values().clear()} e interroga mappa, chiavi, valori ed entry.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene due mapping e tutte le viste sono già state ottenute.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La struttura condivisa non contiene più mapping.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Mappa e viste risultano vuote e la dimensione vale zero.</p>
     */
    @Test
    public void clearingViewClearsBackingMap() {
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        values.clear();
        assertTrue(map.isEmpty());
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
        assertEquals(0, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code clear()} attraverso {@code keySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Usa una mappa locale popolata per controllare esplicitamente anche le
     * viste dei valori e delle entry già create.</p>
     * <p><b>Test Description:</b></p>
     * <p>Svuota la vista delle chiavi e osserva tutti gli oggetti collegati.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene due mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Mappa e viste sono vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Tutti i controlli di vuoto hanno esito positivo e la dimensione è zero.</p>
     */
    @Test
    public void keySetClearEmptiesMapAndEveryExistingView() {
        HMap local = populatedMap();
        HSet keys = local.keySet();
        HCollection values = local.values();
        HSet entries = local.entrySet();
        keys.clear();
        assertTrue(local.isEmpty());
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
        assertEquals(0, local.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code clear()} attraverso {@code entrySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il caso completa il controllo delle tre possibili viste e assicura
     * che anche la vista delle entry agisca direttamente sulla mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Svuota {@code entrySet()} e controlla la mappa e le viste preesistenti.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene due mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Nessun mapping è ancora presente.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Mappa, chiavi, valori ed entry risultano vuoti.</p>
     */
    @Test
    public void entrySetClearEmptiesMapAndEveryExistingView() {
        HMap local = populatedMap();
        HSet keys = local.keySet();
        HCollection values = local.values();
        HSet entries = local.entrySet();
        entries.clear();
        assertTrue(local.isEmpty());
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
        assertEquals(0, local.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code removeAll()} e {@code retainAll()} sulla vista delle chiavi.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La collezione di selezione è costruita come un altro {@code keySet()}:
     * prima identifica la chiave da rimuovere, poi quella da conservare.</p>
     * <p><b>Test Description:</b></p>
     * <p>Rimuove {@code a}, aggiunge {@code c} e conserva infine soltanto {@code b}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene le chiavi {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa contiene soltanto il mapping con chiave {@code b}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Entrambe le operazioni restituiscono {@code true} e aggiornano la mappa.</p>
     */
    @Test
    public void keySetRemoveAllAndRetainAllUpdateMap() {
        HMap selected = new MapAdapter();
        selected.put("a", "x");
        assertTrue(map.keySet().removeAll(selected.keySet()));
        assertFalse(map.containsKey("a"));
        map.put("c", "3");
        selected.clear();
        selected.put("b", "x");
        assertTrue(map.keySet().retainAll(selected.keySet()));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code values().removeAll()} elimini tutte le occorrenze corrispondenti.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Due chiavi condividono il valore {@code 1}; il dato duplicato serve a
     * distinguere l'operazione bulk dalla rimozione singola di {@code remove()}.</p>
     * <p><b>Test Description:</b></p>
     * <p>Usa una seconda vista dei valori come selezione e rimuove ogni {@code 1}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene tre mapping, due dei quali hanno valore {@code 1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Rimane soltanto {@code b=2}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code true} e nessun valore {@code 1} rimane.</p>
     */
    @Test
    public void valuesRemoveAllDeletesEveryMatchingMapping() {
        map.put("c", "1");
        HMap selected = new MapAdapter();
        selected.put("x", "1");
        assertTrue(map.values().removeAll(selected.values()));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("b"));
        assertFalse(map.containsValue("1"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code values().retainAll()} conservi tutte le occorrenze ammesse.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La selezione contiene una sola occorrenza di {@code 1}, mentre la
     * destinazione ne contiene due: il contratto considera l'appartenenza e
     * non il numero di occorrenze nella collezione argomento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Conserva il valore {@code 1} nella vista della fixture.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Sono presenti {@code a=1}, {@code b=2} e {@code c=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Restano i mapping {@code a=1} e {@code c=1}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code retainAll()} restituisce {@code true} e rimuove soltanto {@code b=2}.</p>
     */
    @Test
    public void valuesRetainAllKeepsEveryMatchingDuplicate() {
        map.put("c", "1");
        HMap selected = new MapAdapter();
        selected.put("x", "1");
        assertTrue(map.values().retainAll(selected.values()));
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("c"));
        assertFalse(map.containsKey("b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code removeAll()} sulla vista delle entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La selezione contiene una entry esatta e una con chiave corretta ma
     * valore differente, per controllare che siano confrontati entrambi i componenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Rimuove dalla mappa le entry presenti nella seconda vista.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La destinazione contiene tre mapping; solo {@code a=1} coincide completamente.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Restano invariati {@code b=2} e {@code c=3}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code true} e rimuove soltanto {@code a=1}.</p>
     */
    @Test
    public void entrySetRemoveAllRemovesOnlyMatchingMappings() {
        map.put("c", "3");
        HMap selected = new MapAdapter();
        selected.put("a", "1");
        selected.put("b", "different");
        assertTrue(map.entrySet().removeAll(selected.entrySet()));
        assertFalse(map.containsKey("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code retainAll()} sulla vista delle entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La collezione argomento condivide due mapping completi e presenta per
     * {@code b} un valore diverso, così il test verifica l'uguaglianza delle entry.</p>
     * <p><b>Test Description:</b></p>
     * <p>Conserva soltanto le entry che appartengono anche alla mappa di selezione.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1}, {@code b=2} e {@code c=3}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Restano {@code a=1} e {@code c=3}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code true} e rimuove solo il mapping non equivalente.</p>
     */
    @Test
    public void entrySetRetainAllKeepsOnlyMatchingMappings() {
        map.put("c", "3");
        HMap selected = new MapAdapter();
        selected.put("a", "1");
        selected.put("b", "different");
        selected.put("c", "3");
        assertTrue(map.entrySet().retainAll(selected.entrySet()));
        assertEquals("1", map.get("a"));
        assertFalse(map.containsKey("b"));
        assertEquals("3", map.get("c"));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica i risultati {@code false} delle operazioni bulk sulle entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Per {@code removeAll()} non esistono entry coincidenti; per
     * {@code retainAll()} la selezione è una copia completa della mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Esegue entrambe le operazioni e confronta dimensione e contenuto finali.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene due mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa conserva entrambi i mapping.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Entrambe le operazioni restituiscono {@code false} perché non modificano la vista.</p>
     */
    @Test
    public void entrySetBulkOperationsReturnFalseWithoutChanges() {
        HMap noMatches = new MapAdapter();
        noMatches.put("a", "different");
        noMatches.put("missing", "value");
        assertFalse(map.entrySet().removeAll(noMatches.entrySet()));
        assertEquals(2, map.size());

        HMap same = new MapAdapter(map);
        assertFalse(map.entrySet().retainAll(same.entrySet()));
        assertEquals(same, map);
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code keySet().removeAll(keySet())} sulla stessa vista.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Argomento e destinatario sono lo stesso oggetto; il caso controlla
     * che l'iterazione con rimozione non salti elementi e gestisca anche lo stato vuoto.</p>
     * <p><b>Test Description:</b></p>
     * <p>Rimuove la vista da se stessa e ripete l'operazione dopo lo svuotamento.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene le due chiavi della fixture.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Mappa e vista sono vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La prima chiamata restituisce {@code true}; la seconda {@code false}.</p>
     */
    @Test
    public void keySetRemoveAllWithSameViewEmptiesMap() {
        HSet view = map.keySet();
        assertTrue(view.removeAll(view));
        assertTrue(map.isEmpty());
        assertTrue(view.isEmpty());
        assertEquals(0, map.size());
        assertFalse(view.removeAll(view));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code removeAll()} sulla stessa vista dei valori con duplicati.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il valore {@code 1} compare due volte: serve a controllare che ogni
     * mapping attraversato venga rimosso anche quando gli elementi non sono unici.</p>
     * <p><b>Test Description:</b></p>
     * <p>Passa {@code values()} come argomento della propria {@code removeAll()}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Tre mapping sono presenti e due condividono lo stesso valore.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa backing e la vista risultano vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code true} e rimuove tutti e tre i mapping.</p>
     */
    @Test
    public void valuesRemoveAllWithSameViewRemovesDuplicateMappings() {
        map.put("c", "1");
        HCollection view = map.values();
        assertTrue(view.removeAll(view));
        assertTrue(map.isEmpty());
        assertTrue(view.isEmpty());
        assertEquals(0, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code entrySet().removeAll(entrySet())} sulla stessa vista.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'auto-rimozione esercita contemporaneamente confronto delle entry e
     * rimozione tramite l'iteratore della vista backed.</p>
     * <p><b>Test Description:</b></p>
     * <p>Passa la vista delle entry come argomento di se stessa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene i due mapping della fixture.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Mappa e vista delle entry sono vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code removeAll()} restituisce {@code true} e la dimensione diventa zero.</p>
     */
    @Test
    public void entrySetRemoveAllWithSameViewEmptiesMap() {
        HSet view = map.entrySet();
        assertTrue(view.removeAll(view));
        assertTrue(map.isEmpty());
        assertTrue(view.isEmpty());
        assertEquals(0, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code keySet().retainAll(keySet())} sulla stessa vista.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Poiché ogni chiave appartiene necessariamente alla collezione usata
     * per la selezione, nessuna rimozione deve avvenire.</p>
     * <p><b>Test Description:</b></p>
     * <p>Conserva nella vista tutti gli elementi della vista stessa e ricontrolla i mapping.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dimensione, chiavi e valori della fixture restano invariati.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code retainAll()} restituisce {@code false}.</p>
     */
    @Test
    public void keySetRetainAllWithSameViewMakesNoChanges() {
        HSet view = map.keySet();
        assertFalse(view.retainAll(view));
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertTrue(view.contains("a"));
        assertTrue(view.contains("b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica l'auto-{@code retainAll()} della vista dei valori con duplicati.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La presenza di due {@code 1} conferma che il metodo conserva ogni
     * mapping, senza ridurre la vista a un insieme di valori distinti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Passa la stessa vista come selezione e conta poi le occorrenze nell'array.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1}, {@code b=2} e {@code c=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Tutti e tre i mapping, inclusi i duplicati, restano presenti.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il metodo restituisce {@code false} e sono ancora osservabili due {@code 1}.</p>
     */
    @Test
    public void valuesRetainAllWithSameViewPreservesDuplicateMappings() {
        map.put("c", "1");
        HCollection view = map.values();
        assertFalse(view.retainAll(view));
        assertEquals(3, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("1", map.get("c"));
        assertEquals(2, arrayOccurrences(view.toArray(), "1"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica l'auto-{@code retainAll()} della vista delle entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Contenuto della vista e mapping della mappa vengono controllati
     * entrambi per escludere modifiche non segnalate dal valore di ritorno.</p>
     * <p><b>Test Description:</b></p>
     * <p>Conserva tutte le entry già appartenenti alla stessa vista.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Sono presenti {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Le due entry e i relativi mapping restano invariati.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code retainAll()} restituisce {@code false}.</p>
     */
    @Test
    public void entrySetRetainAllWithSameViewMakesNoChanges() {
        HSet view = map.entrySet();
        assertFalse(view.retainAll(view));
        assertEquals(2, map.size());
        assertTrue(view.contains(new EntryStub("a", "1")));
        assertTrue(view.contains(new EntryStub("b", "2")));
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che due viste {@code values()} non adottino l'uguaglianza degli insiemi.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Due mappe espongono gli stessi valori tramite chiavi diverse: il
     * contenuto coincide, ma il contratto generale di {@link HCollection} non
     * richiede un confronto strutturale.</p>
     * <p><b>Test Description:</b></p>
     * <p>Confronta le viste nei due versi e controlla la riflessività.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Le due viste contengono {@code 1} e {@code 2}, ma sono istanze diverse.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Entrambe le mappe restano invariate.</p>
     * <p><b>Expected Results:</b></p>
     * <p>I confronti tra viste distinte sono falsi; quello con se stessa è vero.</p>
     */
    @Test
    public void valuesViewsDoNotUseSetEquality() {
        HMap other = new MapAdapter();
        other.put("x", "1");
        other.put("y", "2");
        assertFalse(map.values().equals(other.values()));
        assertFalse(other.values().equals(map.values()));
        assertTrue(map.values().equals(map.values()));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code containsAll()} con un sottoinsieme e con una chiave assente.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La stessa collezione di selezione viene prima usata come sottoinsieme
     * valido e poi estesa, rendendo chiaro quale elemento cambia il risultato.</p>
     * <p><b>Test Description:</b></p>
     * <p>Cerca prima {@code a}, quindi {@code a} e {@code missing}, nella vista delle chiavi.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene soltanto le chiavi {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Nessuna delle due mappe viene modificata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il sottoinsieme è riconosciuto; la collezione con la chiave assente non lo è.</p>
     */
    @Test
    public void containsAllRecognizesSubsetsAndRejectsMissingElements() {
        HMap subset = new MapAdapter();
        subset.put("a", "x");
        assertTrue(map.keySet().containsAll(subset.keySet()));
        subset.put("missing", "x");
        assertFalse(map.keySet().containsAll(subset.keySet()));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica gli argomenti {@code null} delle operazioni bulk di {@code keySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Un helper comune applica lo stesso controllo alle quattro operazioni,
     * evitando differenze accidentali tra test equivalenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Invoca {@code containsAll}, {@code addAll}, {@code removeAll} e
     * {@code retainAll} con una collezione nulla.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista delle chiavi contiene i due elementi della fixture.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa conserva il proprio contenuto dopo ciascun errore.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Ogni operazione lancia {@link NullPointerException}.</p>
     */
    @Test
    public void keySetRejectsNullBulkArgumentsWithoutChanges() {
        assertNullBulkArgumentsRejected(map.keySet());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica gli argomenti {@code null} delle operazioni bulk di {@code values()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Riutilizza lo stesso controllo della vista delle chiavi per dimostrare
     * che il contratto comune è applicato anche alla collection dei valori.</p>
     * <p><b>Test Description:</b></p>
     * <p>Esegue le quattro operazioni bulk con argomento nullo.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene {@code 1} e {@code 2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La fixture rimane invariata dopo ogni tentativo.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Tutte le operazioni considerate lanciano {@link NullPointerException}.</p>
     */
    @Test
    public void valuesRejectNullBulkArgumentsWithoutChanges() {
        assertNullBulkArgumentsRejected(map.values());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica gli argomenti {@code null} delle operazioni bulk di {@code entrySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il caso completa il confronto tra le tre viste usando una procedura comune.</p>
     * <p><b>Test Description:</b></p>
     * <p>Passa {@code null} alle quattro operazioni bulk della vista delle entry.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene le due entry della fixture.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa conserva entrambi i mapping.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Ciascuna chiamata lancia {@link NullPointerException} senza effetti collaterali.</p>
     */
    @Test
    public void entrySetRejectsNullBulkArgumentsWithoutChanges() {
        assertNullBulkArgumentsRejected(map.entrySet());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la ricerca di {@code null} nella vista dei valori.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il test parte da una mappa popolata e usa il controllo completo della
     * fixture per assicurare che l'eccezione non causi effetti collaterali.</p>
     * <p><b>Test Description:</b></p>
     * <p>Invoca {@code values().contains(null)} e intercetta l'errore.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La {@code Hashtable} sottostante contiene soltanto valori non nulli.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>I due mapping iniziali restano presenti.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La ricerca lancia {@link NullPointerException}.</p>
     */
    @Test
    public void valuesContainsNullThrowsAndPreservesMap() {
        try {
            map.values().contains(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica la rimozione di {@code null} dalla vista dei valori.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Questo caso è separato da {@code contains(null)} perché
     * l'implementazione di {@code remove()} attraversa la vista e tratta
     * esplicitamente l'argomento nullo.</p>
     * <p><b>Test Description:</b></p>
     * <p>Tenta di rimuovere {@code null} e ricontrolla l'intera fixture.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Nessun valore nullo può essere contenuto nella mappa.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa è invariata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code false} senza lanciare eccezioni.</p>
     */
    @Test
    public void valuesRemoveNullReturnsFalseAndPreservesMap() {
        assertFalse(map.values().remove(null));
        assertFixtureUnchanged();
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica gli argomenti che non possono rappresentare una entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Sono usati sia {@code null} sia una {@link String}, controllando
     * {@code contains()} e {@code remove()} per ogni tipo non valido.</p>
     * <p><b>Test Description:</b></p>
     * <p>Interroga e modifica apparentemente {@code entrySet()} con i quattro casi.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene due oggetti {@link HMap.Entry} validi.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Nessun mapping viene rimosso.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Tutte le chiamate restituiscono {@code false} e la fixture è invariata.</p>
     */
    @Test
    public void entrySetRejectsNullAndNonEntryElementsWithoutChanges() {
        assertFalse(map.entrySet().contains(null));
        assertFalse(map.entrySet().remove(null));
        assertFalse(map.entrySet().contains("not an entry"));
        assertFalse(map.entrySet().remove("not an entry"));
        assertFixtureUnchanged();
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica operazioni bulk senza modifiche su {@code keySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>{@code removeAll()} riceve solo una chiave assente, mentre
     * {@code retainAll()} riceve tutte le chiavi presenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Esegue le due operazioni e controlla la fixture dopo ciascuna chiamata.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Entrambi i mapping restano invariati.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le due operazioni restituiscono {@code false}.</p>
     */
    @Test
    public void keySetBulkOperationsReturnFalseWithoutChanges() {
        HMap noMatches = new MapAdapter();
        noMatches.put("missing", "x");
        assertFalse(map.keySet().removeAll(noMatches.keySet()));
        assertFixtureUnchanged();

        HMap allKeys = new MapAdapter();
        allKeys.put("a", "x");
        allKeys.put("b", "x");
        assertFalse(map.keySet().retainAll(allKeys.keySet()));
        assertFixtureUnchanged();
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica operazioni bulk senza modifiche su {@code values()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>{@code removeAll()} usa un valore assente e {@code retainAll()} una
     * collezione che comprende tutti i valori presenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Esegue le due operazioni e confronta lo stato dopo ogni passaggio.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene i valori {@code 1} e {@code 2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa conserva dimensione e contenuto iniziali.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Entrambi i metodi restituiscono {@code false}.</p>
     */
    @Test
    public void valuesBulkOperationsReturnFalseWithoutChanges() {
        HMap noMatches = new MapAdapter();
        noMatches.put("x", "missing");
        assertFalse(map.values().removeAll(noMatches.values()));
        assertFixtureUnchanged();

        HMap allValues = new MapAdapter();
        allValues.put("x", "1");
        allValues.put("y", "2");
        assertFalse(map.values().retainAll(allValues.values()));
        assertFixtureUnchanged();
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica i casi limite di {@code equals()} e {@code hashCode()} per {@code keySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Un secondo insieme contiene le stesse chiavi ma valori diversi,
     * mostrando che l'uguaglianza dipende soltanto dagli elementi del set e
     * non dall'ordine o dai valori associati nella mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Controlla tipo, riflessività, simmetria, hash e differenze di tipo o dimensione.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>I due {@code keySet()} equivalenti contengono {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Le mappe non vengono modificate, salvo la mappa di confronto usata
     * per creare scenari non equivalenti.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Gli insiemi equivalenti sono uguali e hanno lo stesso hash; gli altri confronti sono falsi.</p>
     */
    @Test
    public void keySetSatisfiesSetEqualityBoundaryContracts() {
        HSet set = map.keySet();
        HMap equivalentMap = new MapAdapter();
        equivalentMap.put("b", "different");
        equivalentMap.put("a", "values");
        HSet equivalent = equivalentMap.keySet();

        assertFalse(set.equals(null));
        assertFalse(set.equals("not a set"));
        assertTrue(set.equals(set));
        assertTrue(set.equals(equivalent));
        assertTrue(equivalent.equals(set));
        assertEquals(set.hashCode(), equivalent.hashCode());

        equivalentMap.put("a", "different");
        assertFalse(set.equals(equivalentMap.entrySet()));
        equivalentMap.put("a", "1");

        equivalentMap.put("c", "3");
        assertFalse(set.equals(equivalent));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica i casi limite dell'uguaglianza di {@code entrySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La mappa equivalente viene popolata in ordine diverso, perché
     * l'uguaglianza di un insieme non deve dipendere dall'ordine della {@code Hashtable}.</p>
     * <p><b>Test Description:</b></p>
     * <p>Controlla {@code null}, tipo errato, riflessività, simmetria, hash e dimensione differente.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Le due viste contengono inizialmente gli stessi mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La fixture resta invariata; la mappa di confronto riceve una terza entry.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le viste equivalenti sono uguali e coerenti nell'hash; gli altri oggetti non lo sono.</p>
     */
    @Test
    public void entrySetSatisfiesSetEqualityBoundaryContracts() {
        HSet set = map.entrySet();
        HMap equivalentMap = new MapAdapter();
        equivalentMap.put("b", "2");
        equivalentMap.put("a", "1");
        HSet equivalent = equivalentMap.entrySet();

        assertFalse(set.equals(null));
        assertFalse(set.equals("not a set"));
        assertTrue(set.equals(set));
        assertTrue(set.equals(equivalent));
        assertTrue(equivalent.equals(set));
        assertEquals(set.hashCode(), equivalent.hashCode());

        equivalentMap.put("c", "3");
        assertFalse(set.equals(equivalent));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code add()} non sia supportato da nessuna vista della mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Vengono forniti una chiave, un valore e una entry formalmente validi:
     * l'errore dipende dall'operazione, non dal tipo dell'elemento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Tenta l'aggiunta su tutte le viste tramite un helper comune.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene due mapping e gli elementi proposti sono nuovi.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Nessun mapping viene aggiunto.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Ogni chiamata lancia {@link MapAdapter.HUnsupportedOperationException}.</p>
     */
    @Test
    public void addIsUnsupportedByEveryView() {
        expectUnsupportedAdd(map.keySet(), "c");
        expectUnsupportedAdd(map.values(), "3");
        expectUnsupportedAdd(map.entrySet(), new EntryStub("c", "3"));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code addAll()} con una collezione vuota.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Una sorgente vuota non provoca alcuna chiamata ad {@code add()} e
     * permette quindi di distinguere l'assenza di modifiche da un'aggiunta non supportata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Passa lo stesso {@code keySet()} vuoto alle tre viste della fixture.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La sorgente non contiene elementi; la destinazione contiene due mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa di destinazione rimane invariata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le tre chiamate restituiscono {@code false} senza eccezioni.</p>
     */
    @Test
    public void addAllWithEmptyCollectionReturnsFalse() {
        HCollection empty = new MapAdapter().keySet();
        assertFalse(map.keySet().addAll(empty));
        assertFalse(map.values().addAll(empty));
        assertFalse(map.entrySet().addAll(empty));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code addAll()} non vuoto su tutte le viste.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Ogni sorgente contiene un elemento adatto alla propria vista, così
     * l'eccezione documenta il carattere opzionale dell'aggiunta.</p>
     * <p><b>Test Description:</b></p>
     * <p>Tenta di aggiungere chiavi, valori ed entry provenienti da una seconda mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La sorgente contiene {@code c=3}; la fixture non contiene quel mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La fixture conserva soltanto i due mapping iniziali.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Ogni operazione lancia {@link MapAdapter.HUnsupportedOperationException}.</p>
     */
    @Test
    public void addAllWithElementsIsUnsupportedByEveryView() {
        HMap source = new MapAdapter();
        source.put("c", "3");
        expectUnsupportedAddAll(map.keySet(), source.keySet());
        expectUnsupportedAddAll(map.values(), source.values());
        expectUnsupportedAddAll(map.entrySet(), source.entrySet());
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code toArray()} senza array di destinazione sulla vista delle chiavi.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il contenuto viene controllato per appartenenza e non per posizione,
     * perché la {@code Hashtable} non garantisce un ordine stabile.</p>
     * <p><b>Test Description:</b></p>
     * <p>Converte la vista, controlla dimensione e chiavi, quindi modifica una cella.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>{@code keySet()} contiene {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La modifica al contenitore array non cambia la mappa.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'array ha due elementi completi e indipendenti come celle dalla vista.</p>
     */
    @Test
    public void toArrayWithoutArgumentReturnsIndependentCompleteArray() {
        Object[] result = map.keySet().toArray();
        assertEquals(2, result.length);
        assertTrue(arrayContains(result, "a"));
        assertTrue(arrayContains(result, "b"));
        result[0] = "changed";
        assertTrue(map.keySet().contains("a"));
        assertTrue(map.keySet().contains("b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica duplicati e indipendenza del contenitore prodotto da {@code values().toArray()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Due mapping hanno valore {@code 1}; il conteggio delle occorrenze
     * evita di trattare per errore la vista dei valori come un insieme.</p>
     * <p><b>Test Description:</b></p>
     * <p>Converte tre valori, conta le occorrenze e sostituisce una cella dell'array.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene i valori {@code 1}, {@code 2}, {@code 1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa conserva i tre mapping originali.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'array conserva entrambi i duplicati; cambiarne una cella non modifica la vista.</p>
     */
    @Test
    public void valuesToArrayPreservesDuplicatesAndIsIndependent() {
        map.put("c", "1");
        Object[] result = map.values().toArray();
        assertEquals(3, result.length);
        assertEquals(2, arrayOccurrences(result, "1"));
        assertEquals(1, arrayOccurrences(result, "2"));
        result[0] = "changed";
        assertEquals(3, map.size());
        assertTrue(map.values().contains("1"));
        assertTrue(map.values().contains("2"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code entrySet().toArray()} produca entry valide per i mapping.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Ogni elemento viene controllato per tipo, chiave presente e valore
     * corrente, senza imporre l'ordine di attraversamento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Converte la vista e confronta ciascuna entry con la mappa backing.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene due mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa non viene modificata; le entry restano oggetti backed.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'array ha dimensione due e ogni elemento descrive un mapping corrente.</p>
     */
    @Test
    public void entrySetToArrayContainsEntriesForEveryMapping() {
        Object[] result = map.entrySet().toArray();
        assertEquals(map.size(), result.length);
        int index;
        for (index = 0; index < result.length; index++) {
            assertTrue(result[index] instanceof HMap.Entry);
            HMap.Entry entry = (HMap.Entry) result[index];
            assertTrue(map.containsKey(entry.getKey()));
            assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il riuso di un array più grande da parte di {@code values().toArray(Object[])}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Quattro sentinelle rendono osservabili sia la cella terminatrice da
     * impostare a {@code null} sia la parte di coda che non deve essere modificata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Fornisce un array di lunghezza quattro a una vista con due valori.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'array è sufficientemente grande e contiene la stessa sentinella in ogni cella.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'array fornito contiene valori, terminatore nullo e ultima sentinella.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Viene restituita la stessa istanza; la cella 2 è nulla e la cella 3 è invariata.</p>
     */
    @Test
    public void valuesToArrayReusesLargeArrayAndPreservesTail() {
        Object sentinel = new Object();
        Object[] supplied = new Object[] {
                sentinel, sentinel, sentinel, sentinel
        };
        Object[] result = map.values().toArray(supplied);
        assertSame(supplied, result);
        assertNull(result[2]);
        assertSame(sentinel, result[3]);
        assertTrue(arrayContains(result, "1"));
        assertTrue(arrayContains(result, "2"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica l'allocazione quando un {@code Object[]} è troppo piccolo.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Un array vuoto rende inequivocabile la necessità di crearne uno nuovo
     * della dimensione richiesta.</p>
     * <p><b>Test Description:</b></p>
     * <p>Passa un {@code Object[0]} a una vista contenente due chiavi.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'array non ha spazio per alcun elemento.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'array originale resta distinto da quello restituito.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il nuovo array ha lunghezza due e contiene entrambe le chiavi.</p>
     */
    @Test
    public void toArrayWithTooSmallObjectArrayAllocatesRequiredSize() {
        Object[] supplied = new Object[0];
        Object[] result = map.keySet().toArray(supplied);
        assertNotSame(supplied, result);
        assertEquals(2, result.length);
        assertTrue(arrayContains(result, "a"));
        assertTrue(arrayContains(result, "b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il riuso di un {@code Object[]} con dimensione esatta.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La capacità coincide con la dimensione della vista, quindi non serve
     * né una nuova allocazione né una cella terminatrice.</p>
     * <p><b>Test Description:</b></p>
     * <p>Converte le due chiavi nell'array fornito dal chiamante.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>L'array ha due celle e la vista contiene due elementi.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Lo stesso array contiene entrambe le chiavi.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il riferimento restituito coincide con quello fornito.</p>
     */
    @Test
    public void toArrayWithExactObjectArrayReusesIt() {
        Object[] supplied = new Object[2];
        Object[] result = map.keySet().toArray(supplied);
        assertSame(supplied, result);
        assertTrue(arrayContains(result, "a"));
        assertTrue(arrayContains(result, "b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica quale parte di un array sovradimensionato viene azzerata.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le sentinelle permettono di distinguere la cella subito successiva
     * agli elementi dalla coda ulteriore, che il contratto lascia invariata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Converte due chiavi in un array di quattro celle precompilate.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Tutte le celle contengono la stessa sentinella non nulla.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La cella 2 è nulla, mentre la cella 3 conserva la sentinella.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'array è riutilizzato e soltanto il primo elemento libero viene azzerato.</p>
     */
    @Test
    public void toArrayWithLargeArraySetsOnlyFollowingElementToNull() {
        Object sentinel = new Object();
        Object[] supplied = new Object[] {sentinel, sentinel, sentinel, sentinel};
        Object[] result = map.keySet().toArray(supplied);
        assertSame(supplied, result);
        assertNull(result[2]);
        assertSame(sentinel, result[3]);
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il riuso di un array tipizzato compatibile.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le chiavi sono {@link String} e l'array {@code String[]} ha una cella
     * in più, coprendo insieme compatibilità runtime e terminatore nullo.</p>
     * <p><b>Test Description:</b></p>
     * <p>Copia le chiavi in un {@code String[3]}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Tutti gli elementi della vista sono assegnabili a {@link String}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Lo stesso array contiene le due chiavi e un {@code null} finale.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'array tipizzato viene riutilizzato senza {@link ArrayStoreException}.</p>
     */
    @Test
    public void toArrayFillsCompatibleTypedArrayWhenLargeEnough() {
        String[] supplied = new String[3];
        Object[] result = map.keySet().toArray(supplied);
        assertSame(supplied, result);
        assertTrue(arrayContains(result, "a"));
        assertTrue(arrayContains(result, "b"));
        assertNull(result[2]);
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il rifiuto di un array con tipo runtime incompatibile.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Un {@code Integer[2]} ha capacità sufficiente, ma non può contenere
     * le chiavi {@link String}; l'errore avviene quindi durante la memorizzazione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Passa l'array incompatibile a {@code keySet().toArray()}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene due stringhe e l'array contiene due celle per interi.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa non viene modificata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>JUnit osserva esattamente {@link ArrayStoreException}.</p>
     */
    @Test(expected = ArrayStoreException.class)
    public void toArrayRejectsIncompatibleTypedArrayDuringStorage() {
        map.keySet().toArray(new Integer[2]);
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il rifiuto di un array di destinazione {@code null}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il caso isola la validazione dell'argomento senza coinvolgere tipo o dimensione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Invoca {@code keySet().toArray(null)}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista è valida e non vuota; manca soltanto l'array di destinazione.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa rimane invariata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>JUnit osserva esattamente {@link NullPointerException}.</p>
     */
    @Test(expected = NullPointerException.class)
    public void toArrayRejectsNullArgument() {
        map.keySet().toArray(null);
    }

    /**
     * Implementazione minima di {@link HMap.Entry} usata per confrontare le
     * entry prodotte dall'adapter senza dipendere dalla loro classe privata.
     */
    private static final class EntryStub implements HMap.Entry {
        /** Chiave rappresentata dalla entry di supporto. */
        private final Object key;

        /** Valore corrente della entry di supporto. */
        private Object value;

        /**
         * Crea una entry indipendente con la coppia indicata.
         *
         * @param entryKey chiave da rappresentare
         * @param entryValue valore iniziale associato alla chiave
         */
        private EntryStub(Object entryKey, Object entryValue) {
            key = entryKey;
            value = entryValue;
        }

        /**
         * Restituisce la chiave memorizzata nello stub.
         *
         * @return chiave della entry
         */
        public Object getKey() {
            return key;
        }

        /**
         * Restituisce il valore corrente dello stub.
         *
         * @return valore associato localmente
         */
        public Object getValue() {
            return value;
        }

        /**
         * Sostituisce il valore locale, riproducendo il contratto di
         * {@link HMap.Entry#setValue(Object)} senza modificare una mappa.
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
         * Confronta chiave e valore con un'altra {@link HMap.Entry}.
         *
         * @param object oggetto da confrontare
         * @return {@code true} se entrambi i componenti sono uguali
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
         * Calcola lo XOR tra i codici hash di chiave e valore.
         *
         * @return codice hash coerente con {@link #equals(Object)}
         */
        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * Confronta due riferimenti gestendo esplicitamente {@code null}.
         *
         * @param first oggetto che riceve l'eventuale chiamata a {@code equals()}
         * @param second oggetto con cui effettuare il confronto
         * @return {@code true} se i riferimenti rappresentano valori uguali
         */
        private static boolean equal(Object first, Object second) {
            return first == null ? second == null : first.equals(second);
        }
    }

    /**
     * Valore memorizzato che rifiuta ogni confronto. In coppia con
     * {@link MatchingRemovalProbe} rende osservabile la direzione di
     * {@code equals()} usata da {@code values().remove()}.
     */
    private static final class StoredRejectingValue {
        /**
         * Rifiuta intenzionalmente qualunque oggetto.
         *
         * @param object oggetto ignorato dal probe
         * @return sempre {@code false}
         */
        public boolean equals(Object object) {
            return false;
        }

        /**
         * Restituisce un valore stabile, sufficiente per l'uso nei test.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Argomento di rimozione che riconosce un {@link StoredRejectingValue}.
     */
    private static final class MatchingRemovalProbe {
        /**
         * Riconosce soltanto il tipo memorizzato complementare.
         *
         * @param object oggetto da esaminare
         * @return {@code true} per un {@link StoredRejectingValue}
         */
        public boolean equals(Object object) {
            return object instanceof StoredRejectingValue;
        }

        /**
         * Restituisce lo stesso hash del valore complementare.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Valore memorizzato che riconosce {@link RejectingRemovalProbe}; serve a
     * dimostrare che il confronto inverso non deve determinare la rimozione.
     */
    private static final class StoredMatchingValue {
        /**
         * Accetta soltanto il probe complementare.
         *
         * @param object oggetto da esaminare
         * @return {@code true} per un {@link RejectingRemovalProbe}
         */
        public boolean equals(Object object) {
            return object instanceof RejectingRemovalProbe;
        }

        /**
         * Restituisce un hash stabile condiviso dai probe.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Argomento di rimozione che rifiuta ogni valore, anche quando il valore
     * memorizzato lo riconoscerebbe nella direzione opposta.
     */
    private static final class RejectingRemovalProbe {
        /**
         * Rifiuta intenzionalmente ogni confronto.
         *
         * @param object oggetto ignorato
         * @return sempre {@code false}
         */
        public boolean equals(Object object) {
            return false;
        }

        /**
         * Restituisce un hash stabile condiviso dai probe.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Probe che considera uguale soltanto uno specifico riferimento. È usato
     * per verificare che {@code values().contains()} interroghi l'argomento di
     * ricerca nella direzione prevista da {@link HMap#containsValue(Object)}.
     */
    private static final class AsymmetricContainsProbe {
        /** Riferimento che il probe deve riconoscere. */
        private final Object match;

        /**
         * Crea un probe associato al riferimento atteso.
         *
         * @param expected unico riferimento che deve risultare uguale
         */
        private AsymmetricContainsProbe(Object expected) {
            match = expected;
        }

        /**
         * Confronta per identità con il riferimento atteso.
         *
         * @param object oggetto da confrontare
         * @return {@code true} soltanto se {@code object == match}
         */
        public boolean equals(Object object) {
            return object == match;
        }

        /**
         * Usa il codice hash dell'oggetto riconosciuto.
         *
         * @return codice hash del riferimento atteso
         */
        public int hashCode() {
            return match.hashCode();
        }
    }

    /**
     * Controlla che una singola aggiunta sia rifiutata e che l'elemento non
     * compaia nella vista dopo l'eccezione.
     *
     * @param collection vista sulla quale tentare l'aggiunta
     * @param object elemento proposto
     */
    private static void expectUnsupportedAdd(HCollection collection,
            Object object) {
        try {
            collection.add(object);
            fail();
        } catch (MapAdapter.HUnsupportedOperationException expected) {
            assertFalse(collection.contains(object));
        }
    }

    /**
     * Controlla che l'aggiunta bulk di una sorgente non vuota produca
     * l'eccezione locale prevista dal progetto.
     *
     * @param destination vista che dovrebbe ricevere gli elementi
     * @param source collezione non vuota usata come sorgente
     */
    private static void expectUnsupportedAddAll(HCollection destination,
            HCollection source) {
        try {
            destination.addAll(source);
            fail();
        } catch (MapAdapter.HUnsupportedOperationException expected) {
            return;
        }
    }

    /**
     * Cerca un elemento in un array senza assumere l'ordine di iterazione.
     *
     * @param array array da esaminare
     * @param expected elemento cercato, che deve essere non {@code null}
     * @return {@code true} se almeno una cella è uguale all'elemento atteso
     */
    private static boolean arrayContains(Object[] array, Object expected) {
        int index;
        for (index = 0; index < array.length; index++) {
            if (expected.equals(array[index])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Conta le occorrenze di un elemento, permettendo di verificare i duplicati
     * senza dipendere dall'ordine dell'array.
     *
     * @param array array da analizzare
     * @param expected elemento non {@code null} da contare
     * @return numero di celle uguali all'elemento atteso
     */
    private static int arrayOccurrences(Object[] array, Object expected) {
        int occurrences = 0;
        int index;
        for (index = 0; index < array.length; index++) {
            if (expected.equals(array[index])) {
                occurrences++;
            }
        }
        return occurrences;
    }

    /**
     * Costruisce una mappa locale con la stessa configurazione base della fixture.
     *
     * @return nuova mappa contenente {@code a=1} e {@code b=2}
     */
    private static HMap populatedMap() {
        HMap result = new MapAdapter();
        result.put("a", "1");
        result.put("b", "2");
        return result;
    }

    /**
     * Applica a una vista le quattro operazioni bulk con argomento
     * {@code null}, verificando dopo ogni eccezione che la fixture non sia
     * stata modificata.
     *
     * @param view vista sulla quale eseguire i controlli
     */
    private void assertNullBulkArgumentsRejected(HCollection view) {
        try {
            view.containsAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }

        try {
            view.addAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }

        try {
            view.removeAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }

        try {
            view.retainAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }
    }

    /**
     * Verifica dimensione, mapping, chiavi e valori della fixture. Il controllo
     * ridondante è intenzionale: permette di individuare anche modifiche
     * parziali non visibili osservando soltanto {@link HMap#size()}.
     */
    private void assertFixtureUnchanged() {
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertTrue(map.keySet().contains("a"));
        assertTrue(map.keySet().contains("b"));
        assertTrue(map.values().contains("1"));
        assertTrue(map.values().contains("2"));
    }
}
