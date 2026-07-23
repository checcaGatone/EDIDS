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
 * La suite verifica il comportamento delle viste delle chiavi, dei valori e
 * delle entry previsto da J2SE 1.4.2. I test controllano che le viste restino
 * collegate alla mappa, sia quando la mappa viene modificata sia quando una
 * rimozione viene eseguita attraverso una vista. Sono inoltre verificate le
 * operazioni bulk, la gestione degli argomenti {@code null}, le aggiunte non
 * supportate, l'uguaglianza delle viste e le due forme di {@code toArray()}.
 * I casi sui valori tengono conto anche della presenza di valori duplicati.
 * </p>
 *
 * <h2>Test Case Design</h2>
 * <p>
 * Prima di ogni test viene creata una mappa con le associazioni {@code a=1} e
 * {@code b=2}. Quando serve controllare i valori duplicati viene aggiunta
 * {@code c=1}. Alcuni casi usano mappe locali per mantenere indipendenti le
 * verifiche o per preparare contenuti diversi. Le viste vengono ottenute prima
 * delle modifiche quando il test deve controllare che gli stessi riferimenti
 * riflettano il nuovo stato della mappa. Le operazioni che restituiscono un
 * valore booleano sono affiancate da controlli sul contenuto finale. Per
 * verificare la direzione di {@code equals()} vengono usati oggetti con
 * confronti volutamente asimmetrici. Nei test di {@code toArray()} gli
 * elementi sono controllati senza dipendere dall'ordine e, negli array più
 * grandi, alcune celle sono inizializzate con valori riconoscibili per
 * verificare quali posizioni vengono modificate.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see MapAdapter
 * @see HCollection
 * @see HSet
 */
public class ViewsTest {
    /**
     * Mappa usata dalla maggior parte dei test. Prima di ogni metodo contiene
     * le associazioni {@code a=1} e {@code b=2}.
     */
    private HMap map;

    /**
     * Crea una nuova {@link MapAdapter} e inserisce le associazioni
     * {@code a=1} e {@code b=2}. In questo modo ogni test parte dallo stesso
     * contenuto e le modifiche eseguite da un metodo non influenzano quelli
     * successivi.
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che un'associazione inserita nella mappa compaia nelle tre
     * viste ottenute in precedenza.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le viste vengono salvate prima dell'inserimento. L'associazione
     * {@code c=3} usa una nuova chiave e un nuovo valore, quindi può essere
     * riconosciuta separatamente in {@code keySet()}, {@code values()} ed
     * {@code entrySet()}.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test ottiene le tre viste, inserisce {@code c=3} con
     * {@code put()} e cerca la nuova chiave, il nuovo valore e l'entry
     * corrispondente nelle stesse viste ottenute prima dell'inserimento.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}; le tre viste sono state
     * ottenute prima di inserire {@code c=3}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Le viste già ottenute espongono gli elementi che formano
     * l'associazione {@code c=3}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le ricerche di {@code c}, {@code 3} e
     * {@code new EntryComp("c", "3")} restituiscono {@code true} nelle
     * rispettive viste.</p>
     */
    @Test
    public void mapPutIsVisibleInPreviouslyObtainedViews() {
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        map.put("c", "3");
        assertTrue(keys.contains("c"));
        assertTrue(values.contains("3"));
        assertTrue(entries.contains(new EntryComp("c", "3")));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che le viste già ottenute riflettano una rimozione e il
     * successivo svuotamento della mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le tre viste vengono salvate una sola volta. Dopo la rimozione di
     * {@code a} si controllano separatamente chiave, valore ed entry; dopo
     * {@code clear()} si riutilizzano gli stessi riferimenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test rimuove {@code a} direttamente dalla mappa e verifica che
     * {@code a}, {@code 1} e l'entry {@code a=1} non siano più presenti.
     * Svuota poi la mappa e controlla nuovamente le tre viste.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}; le viste sono state
     * ottenute prima delle due operazioni.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo la prima operazione l'associazione {@code a=1} non compare nelle
     * viste; dopo la seconda le tre viste sono vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le tre chiamate a {@code contains()} restituiscono {@code false} e le
     * successive chiamate a {@code isEmpty()} restituiscono {@code true}.</p>
     */
    @Test
    public void mapRemoveAndClearAreVisibleInExistingViews() {
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        map.remove("a");
        assertFalse(keys.contains("a"));
        assertFalse(values.contains("1"));
        assertFalse(entries.contains(new EntryComp("a", "1")));
        map.clear();
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che la rimozione da {@code keySet()} aggiorni la mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il test usa prima la chiave presente {@code a} e poi la chiave assente
     * {@code missing}. Per la prima rimozione controlla sia il risultato
     * booleano sia lo stato della mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test rimuove {@code a} da {@code keySet()}, controlla l'assenza
     * della chiave nella mappa e la nuova dimensione, quindi tenta di rimuovere
     * {@code missing}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene le chiavi {@code a} e {@code b}, ma non contiene
     * {@code missing}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La chiave {@code a} non è più presente e la dimensione della mappa
     * vale uno.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La rimozione di {@code a} restituisce {@code true}, la ricerca della
     * chiave restituisce {@code false}, la dimensione vale uno e la rimozione
     * di {@code missing} restituisce {@code false}.</p>
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
     * <p>Verifica che {@code keySet().contains(null)} segnali l'argomento non
     * valido senza modificare la mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Oltre al tipo di eccezione, vengono controllati la dimensione e i
     * valori associati a entrambe le chiavi. Queste asserzioni verificano che
     * la chiamata non abbia cambiato il contenuto della mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test chiama {@code keySet().contains(null)} e usa {@code fail()} se
     * non viene lanciata un'eccezione. Nel blocco {@code catch} controlla la
     * dimensione e legge i valori associati ad {@code a} e {@code b}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene le associazioni {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo l'eccezione la dimensione vale ancora due e i valori associati
     * ad {@code a} e {@code b} non sono cambiati.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La chiamata lancia {@link NullPointerException}; la dimensione vale
     * due e {@code get("a")} e {@code get("b")} restituiscono rispettivamente
     * {@code 1} e {@code 2}.</p>
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
     * <p>Verifica che {@code keySet().remove(null)} lanci
     * {@link NullPointerException} e lasci invariata la mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Poiché {@code remove()} può modificare la vista, dopo l'eccezione
     * vengono controllati sia il numero di associazioni sia i valori delle due
     * chiavi presenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test tenta {@code keySet().remove(null)}, fallisce se la chiamata
     * termina normalmente e, nel blocco {@code catch}, controlla dimensione e
     * valori presenti nella mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Le due associazioni iniziali sono ancora presenti con gli stessi
     * valori.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Viene intercettata {@link NullPointerException}; la dimensione resta
     * due e le letture di {@code a} e {@code b} restituiscono {@code 1} e
     * {@code 2}.</p>
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
     * <p>Verifica che {@code values().remove()} elimini una sola associazione
     * quando lo stesso valore compare due volte.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'associazione {@code c=1} aggiunge una seconda occorrenza del valore
     * {@code 1}. La diminuzione della dimensione e la presenza dello stesso
     * valore dopo la rimozione mostrano che è stata eliminata una sola
     * associazione. Viene provato anche un valore assente.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, rimuove il valore {@code 1} dalla vista
     * e controlla risultato, dimensione e presenza di un'altra occorrenza.
     * Infine tenta di rimuovere {@code missing}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene tre associazioni e il valore {@code 1} è associato
     * sia ad {@code a} sia a {@code c}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo la rimozione la dimensione vale due e il valore {@code 1} è
     * ancora presente nella mappa.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La rimozione di {@code 1} restituisce {@code true},
     * {@code containsValue("1")} restituisce ancora {@code true} e la
     * rimozione di {@code missing} restituisce {@code false}.</p>
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
     * <p>Il valore memorizzato restituisce sempre {@code false} da
     * {@code equals()}, mentre l'argomento passato a {@code remove()} riconosce
     * quel valore. La rimozione può quindi riuscire solo se il confronto parte
     * dall'argomento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test inserisce il valore nella mappa locale e passa a
     * {@code values().remove()} l'oggetto il cui {@code equals()} riconosce il
     * valore memorizzato. Controlla poi il risultato, lo stato vuoto della mappa
     * e l'assenza di {@code key}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene una sola associazione con chiave
     * {@code key}; i due oggetti restituiscono risultati opposti da
     * {@code equals()}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'associazione viene rimossa e la chiave {@code key} non è più
     * presente.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code remove()} restituisce {@code true}, {@code isEmpty()}
     * restituisce {@code true} e {@code containsKey("key")} restituisce
     * {@code false}.</p>
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
     * <p>Verifica che {@code values().remove()} non usi {@code equals()} sul
     * valore memorizzato.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il valore memorizzato riconosce l'argomento di rimozione, ma
     * l'argomento restituisce sempre {@code false}. Il risultato permette di
     * distinguere le due possibili direzioni del confronto.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test salva un valore sotto {@code key}, tenta la rimozione con un
     * oggetto che non lo riconosce e controlla il risultato, la dimensione e
     * il riferimento ancora associato alla chiave.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene una sola associazione; il valore e
     * l'argomento producono risultati diversi quando vengono usati come primo
     * operando di {@code equals()}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa locale conserva un'associazione e {@code key} continua a essere
     * associata allo stesso riferimento memorizzato prima della chiamata.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code remove()} restituisce {@code false}, la dimensione resta uno
     * e {@code assertSame(stored, local.get("key"))} ha esito positivo.</p>
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
     * <p>Verifica la direzione di {@code equals()} usata da
     * {@code values().contains()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'argomento di ricerca riconosce per identità il valore memorizzato,
     * mentre il confronto nella direzione opposta restituisce {@code false}.
     * Il risultato di {@code contains()} permette quindi di verificare quale
     * oggetto riceve la chiamata a {@code equals()}.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test inserisce un oggetto sotto {@code key}, lo usa per costruire
     * l'argomento di ricerca e chiama {@code values().contains()}. Controlla
     * poi la dimensione e il riferimento ancora associato alla chiave.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene una sola associazione e l'argomento di
     * ricerca riconosce il riferimento memorizzato.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La ricerca non modifica la mappa: la dimensione resta uno e
     * {@code key} conserva lo stesso oggetto {@code stored}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code values().contains()} restituisce {@code true}, la dimensione
     * vale uno e {@code assertSame(stored, local.get("key"))} ha esito
     * positivo.</p>
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
     * <p>Verifica che {@code entrySet().contains()} richieda la corrispondenza
     * della chiave e del valore.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Vengono usati un'entry completa, un'entry con valore errato, un'entry
     * con chiave assente e una stringa. I quattro casi controllano che una
     * corrispondenza parziale o un oggetto di tipo diverso non siano accettati.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test cerca in {@code entrySet()} le entry {@code a=1},
     * {@code a=2} e {@code missing=1}; infine prova l'oggetto
     * {@code "a=1"}, che non implementa {@link HMap.Entry}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2} e non contiene la chiave
     * {@code missing}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La vista riconosce soltanto l'entry che corrisponde completamente a
     * un'associazione della mappa.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La ricerca di {@code a=1} restituisce {@code true}; le altre tre
     * chiamate a {@code contains()} restituiscono {@code false}.</p>
     */
    @Test
    public void entrySetContainsRequiresMatchingKeyAndValue() {
        assertTrue(map.entrySet().contains(new EntryComp("a", "1")));
        assertFalse(map.entrySet().contains(new EntryComp("a", "2")));
        assertFalse(map.entrySet().contains(new EntryComp("missing", "1")));
        assertFalse(map.entrySet().contains("a=1"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che {@code entrySet().remove()} richieda la corrispondenza completa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le due entry usate hanno la stessa chiave, ma soltanto {@code a=1}
     * contiene anche il valore corretto. Tra i due tentativi viene controllata
     * la presenza di {@code a} nella mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test tenta di rimuovere {@code a=2}, controlla che {@code a} sia
     * ancora presente, rimuove {@code a=1} e verifica nuovamente la chiave.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La chiave {@code a} è associata al valore {@code 1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La prima chiamata conserva l'associazione, mentre la seconda la
     * rimuove dalla mappa.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code remove(new EntryComp("a", "2"))} restituisce {@code false};
     * {@code remove(new EntryComp("a", "1"))} restituisce {@code true}. I
     * controlli intermedi sulla chiave restituiscono rispettivamente
     * {@code true} e {@code false}.</p>
     */
    @Test
    public void entrySetRemoveRequiresMatchingKeyAndValue() {
        assertFalse(map.entrySet().remove(new EntryComp("a", "2")));
        assertTrue(map.containsKey("a"));
        assertTrue(map.entrySet().remove(new EntryComp("a", "1")));
        assertFalse(map.containsKey("a"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica l'effetto di {@code clear()} sulle viste delle chiavi, dei
     * valori e delle entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Ogni vista viene provata su una mappa distinta contenente due
     * associazioni. Le tre viste della stessa mappa sono salvate prima dello
     * svuotamento, così si può controllare il loro stato dopo la chiamata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test chiama {@code clear()} prima su {@code keySet()}, poi su
     * {@code values()} e infine su {@code entrySet()}, usando tre
     * configurazioni separate. Dopo ogni chiamata verifica la mappa e tutte le
     * sue viste.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Ciascuna mappa contiene {@code a=1} e {@code b=2}; i riferimenti alle
     * tre viste sono già disponibili.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Ogni mappa e le tre viste collegate risultano vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Nei tre casi le quattro chiamate a {@code isEmpty()} restituiscono
     * {@code true} e {@code size()} sulla mappa restituisce zero.</p>
     */
    @Test
    public void clearOnEveryViewClearsBackingMap() {
        HMap keysCaseMap = populatedMap();
        HSet keysCaseKeys = keysCaseMap.keySet();
        HCollection keysCaseValues = keysCaseMap.values();
        HSet keysCaseEntries = keysCaseMap.entrySet();
        keysCaseKeys.clear();
        assertTrue(keysCaseMap.isEmpty());
        assertTrue(keysCaseKeys.isEmpty());
        assertTrue(keysCaseValues.isEmpty());
        assertTrue(keysCaseEntries.isEmpty());
        assertEquals(0, keysCaseMap.size());

        HMap valuesCaseMap = populatedMap();
        HSet valuesCaseKeys = valuesCaseMap.keySet();
        HCollection valuesCaseValues = valuesCaseMap.values();
        HSet valuesCaseEntries = valuesCaseMap.entrySet();
        valuesCaseValues.clear();
        assertTrue(valuesCaseMap.isEmpty());
        assertTrue(valuesCaseKeys.isEmpty());
        assertTrue(valuesCaseValues.isEmpty());
        assertTrue(valuesCaseEntries.isEmpty());
        assertEquals(0, valuesCaseMap.size());

        HMap entriesCaseMap = populatedMap();
        HSet entriesCaseKeys = entriesCaseMap.keySet();
        HCollection entriesCaseValues = entriesCaseMap.values();
        HSet entriesCaseEntries = entriesCaseMap.entrySet();
        entriesCaseEntries.clear();
        assertTrue(entriesCaseMap.isEmpty());
        assertTrue(entriesCaseKeys.isEmpty());
        assertTrue(entriesCaseValues.isEmpty());
        assertTrue(entriesCaseEntries.isEmpty());
        assertEquals(0, entriesCaseMap.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code removeAll()} e {@code retainAll()} sulla vista delle chiavi.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Una seconda vista delle chiavi contiene prima {@code a}, usata da
     * {@code removeAll()}, e poi {@code b}, usata da {@code retainAll()}.
     * Prima della seconda operazione viene aggiunta {@code c=3}, che deve
     * essere eliminata perché la sua chiave non appartiene alla selezione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test rimuove {@code a} mediante {@code removeAll()}, aggiunge
     * {@code c=3}, prepara una selezione contenente {@code b} e chiama
     * {@code retainAll()}. Dopo le due operazioni controlla le chiavi della
     * mappa interessate dal test.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>All'inizio la mappa contiene {@code a} e {@code b}; prima di
     * {@code retainAll()} contiene {@code b} e {@code c}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa contiene una sola associazione, con chiave {@code b}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Entrambe le operazioni restituiscono {@code true}. Dopo
     * {@code removeAll()} la chiave {@code a} è assente; dopo
     * {@code retainAll()} la dimensione vale uno e rimane la chiave {@code b}.</p>
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
     * <p>Verifica che {@code values().removeAll()} elimini tutte le
     * associazioni che contengono un valore selezionato.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le chiavi {@code a} e {@code c} hanno entrambe valore {@code 1},
     * mentre la collezione passata al metodo contiene una sola occorrenza dello
     * stesso valore. Il test controlla quindi che siano rimosse entrambe le
     * associazioni corrispondenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, crea una seconda mappa che contiene il
     * valore {@code 1} e passa la sua vista dei valori a {@code removeAll()}.
     * Controlla poi risultato, dimensione e contenuto rimasto.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1}, {@code b=2} e {@code c=1}; la
     * collezione argomento contiene il valore {@code 1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Rimane soltanto {@code b=2}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code true}, la dimensione vale uno,
     * {@code b} è presente e nessun valore {@code 1} rimane.</p>
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
     * <p>Verifica che {@code values().retainAll()} conservi tutte le
     * associazioni con un valore presente nella collezione argomento.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Nella mappa il valore {@code 1} compare sotto due chiavi, mentre nella
     * collezione argomento compare una volta sola. I controlli sulle chiavi
     * verificano che entrambe le associazioni con quel valore siano conservate
     * e che {@code b=2} venga rimossa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, prepara una seconda vista che contiene
     * il valore {@code 1} e chiama {@code retainAll()}. Controlla poi il
     * risultato, la dimensione e la presenza delle tre chiavi iniziali.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Sono presenti {@code a=1}, {@code b=2} e {@code c=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Restano le associazioni {@code a=1} e {@code c=1}; {@code b} non è
     * più presente.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code retainAll()} restituisce {@code true}; la dimensione vale due,
     * {@code a} e {@code c} sono presenti, mentre {@code b} è assente.</p>
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
     * <p>Verifica che {@code entrySet().removeAll()} rimuova soltanto le entry
     * che coincidono per chiave e valore.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La collezione argomento contiene {@code a=1}, che coincide
     * completamente, e {@code b=different}, che ha solo la chiave corretta.
     * L'associazione {@code c=3} non compare nella selezione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=3}, prepara le due entry di selezione e
     * chiama {@code removeAll()}. Infine controlla l'assenza di {@code a}, i
     * valori associati a {@code b} e {@code c} e la dimensione.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1}, {@code b=2} e {@code c=3}; soltanto
     * {@code a=1} coincide con un'entry della collezione argomento.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Restano invariati {@code b=2} e {@code c=3}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code true}; {@code a} è assente,
     * {@code b} e {@code c} conservano rispettivamente i valori {@code 2} e
     * {@code 3}, e la dimensione vale due.</p>
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
     * <p>Verifica che {@code entrySet().retainAll()} conservi soltanto le
     * entry che coincidono per chiave e valore.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La collezione argomento contiene le entry corrette {@code a=1} e
     * {@code c=3}, ma associa alla chiave {@code b} un valore diverso. In
     * questo modo vengono controllate sia le corrispondenze complete sia il
     * caso in cui coincide soltanto la chiave.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=3}, prepara le tre entry della collezione
     * argomento e chiama {@code retainAll()}. Controlla poi i valori di
     * {@code a} e {@code c}, l'assenza di {@code b} e la dimensione finale.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1}, {@code b=2} e {@code c=3}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Restano {@code a=1} e {@code c=3}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'operazione restituisce {@code true}; {@code a=1} e {@code c=3}
     * restano osservabili, {@code b} è assente e la dimensione vale due.</p>
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
     * <p>Verifica due operazioni bulk che non devono rimuovere entry dalla
     * vista {@code entrySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La collezione passata a {@code removeAll()} contiene
     * {@code a=different}, che ha una chiave presente ma un valore diverso, e
     * {@code missing=value}, che ha una chiave assente. Per
     * {@code retainAll()} viene invece usata una copia della mappa nello stato
     * raggiunto dopo il primo controllo.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test esegue {@code removeAll()} con le due entry non corrispondenti
     * e controlla il risultato e la dimensione. Crea quindi {@code same},
     * richiama {@code retainAll()} con le entry della copia e confronta la
     * mappa finale con {@code same}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene le associazioni {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo {@code removeAll()} la dimensione è ancora due. Dopo
     * {@code retainAll()} la mappa è uguale alla copia creata tra le due
     * operazioni.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Entrambe le operazioni restituiscono {@code false}; il primo controllo
     * conferma che la dimensione è due e il confronto finale con {@code same}
     * restituisce {@code true}.</p>
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
     * <p>Verifica {@code removeAll()} quando la vista delle chiavi viene
     * passata come argomento a se stessa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Lo stesso riferimento è usato come destinatario e come collezione
     * degli elementi da rimuovere. Una seconda chiamata sulla vista ormai
     * vuota controlla anche il caso in cui l'operazione non produce
     * cambiamenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test salva {@code keySet()} in {@code view}, esegue
     * {@code view.removeAll(view)} e controlla mappa, vista e dimensione.
     * Ripete poi la stessa operazione sulla vista vuota.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}; {@code view} contiene
     * quindi le chiavi {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa e la vista delle chiavi sono vuote e la dimensione della
     * mappa è zero.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La prima chiamata restituisce {@code true}; i controlli di
     * {@code isEmpty()} hanno esito positivo e la dimensione vale zero. La
     * seconda chiamata restituisce {@code false}.</p>
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
     * <p>Verifica {@code removeAll()} sulla stessa vista dei valori quando un
     * valore è presente più volte.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'associazione {@code c=1} fa comparire due volte il valore
     * {@code 1}. Passare la vista a se stessa permette di controllare che
     * vengano rimosse tutte le associazioni, compresa quella con valore
     * ripetuto.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, salva {@code values()} in
     * {@code view} ed esegue {@code view.removeAll(view)}. Controlla quindi il
     * risultato, lo stato vuoto della mappa e della vista e la dimensione
     * finale.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene tre associazioni: {@code a=1}, {@code b=2} e
     * {@code c=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa e la vista dei valori sono vuote e la dimensione della mappa
     * è zero.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code removeAll()} restituisce {@code true}; entrambi i controlli
     * {@code isEmpty()} hanno esito positivo e la dimensione vale zero.</p>
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
     * <p>Verifica {@code removeAll()} quando la vista delle entry viene
     * passata come argomento a se stessa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il destinatario e la collezione degli elementi da rimuovere sono lo
     * stesso oggetto. Il controllo dello stato finale permette di verificare
     * che entrambe le entry presenti vengano eliminate.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test salva {@code entrySet()} in {@code view}, richiama
     * {@code view.removeAll(view)} e controlla il risultato, lo stato vuoto
     * della mappa e della vista e la dimensione finale.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene le associazioni {@code a=1} e {@code b=2}, entrambe
     * presenti nella vista delle entry.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa e la vista delle entry sono vuote e la dimensione della
     * mappa è zero.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code removeAll()} restituisce {@code true}; i due controlli
     * {@code isEmpty()} restituiscono {@code true} e la dimensione vale
     * zero.</p>
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
     * <p>Verifica {@code retainAll()} quando ciascuna vista viene passata come
     * argomento a se stessa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>{@code keySet()}, {@code values()} ed {@code entrySet()} vengono
     * controllati su tre mappe separate. Per la vista dei valori viene
     * aggiunta {@code c=1}, così il test verifica anche che due associazioni
     * con lo stesso valore restino presenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test richiama {@code retainAll()} sulle tre viste, usando ogni
     * vista anche come argomento. Dopo ciascuna chiamata controlla il risultato
     * restituito, la dimensione e gli elementi che devono rimanere nella
     * relativa mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Le mappe usate per le chiavi e le entry contengono {@code a=1} e
     * {@code b=2}. La mappa usata per i valori contiene anche
     * {@code c=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Le tre mappe conservano le associazioni iniziali. La vista dei valori
     * contiene ancora due occorrenze di {@code 1}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le tre chiamate restituiscono {@code false}. Le dimensioni sono due,
     * tre e due; le chiavi {@code a} e {@code b}, i valori associati e le due
     * entry controllate rimangono presenti. L'array dei valori contiene due
     * occorrenze di {@code 1}.</p>
     */
    @Test
    public void retainAllWithSameViewMakesNoChangesForEveryView() {
        HMap keySetMap = populatedMap();
        HSet keySet = keySetMap.keySet();
        assertFalse(keySet.retainAll(keySet));
        assertEquals(2, keySetMap.size());
        assertEquals("1", keySetMap.get("a"));
        assertEquals("2", keySetMap.get("b"));
        assertTrue(keySet.contains("a"));
        assertTrue(keySet.contains("b"));

        HMap valuesMap = populatedMap();
        valuesMap.put("c", "1");
        HCollection values = valuesMap.values();
        assertFalse(values.retainAll(values));
        assertEquals(3, valuesMap.size());
        assertEquals("1", valuesMap.get("a"));
        assertEquals("2", valuesMap.get("b"));
        assertEquals("1", valuesMap.get("c"));
        assertEquals(2, arrayOccurrences(values.toArray(), "1"));

        HMap entrySetMap = populatedMap();
        HSet entrySet = entrySetMap.entrySet();
        assertFalse(entrySet.retainAll(entrySet));
        assertEquals(2, entrySetMap.size());
        assertTrue(entrySet.contains(new EntryComp("a", "1")));
        assertTrue(entrySet.contains(new EntryComp("b", "2")));
        assertEquals("1", entrySetMap.get("a"));
        assertEquals("2", entrySetMap.get("b"));
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica che due viste {@code values()} con gli stessi elementi non
     * vengano confrontate come insiemi.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La seconda mappa usa chiavi diverse, ma contiene gli stessi valori
     * {@code 1} e {@code 2}. I confronti vengono eseguiti in entrambe le
     * direzioni; un ultimo confronto riguarda due chiamate a
     * {@code values()} sulla stessa mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea {@code other} con {@code x=1} e {@code y=2}, confronta
     * la sua vista dei valori con quella della mappa iniziale nei due versi e
     * infine confronta due viste richieste alla mappa iniziale.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Le due mappe contengono entrambe i valori {@code 1} e {@code 2}, ma
     * associati a chiavi diverse.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il test esegue soltanto confronti e non rimuove né aggiunge
     * associazioni dopo la preparazione delle mappe.</p>
     * <p><b>Expected Results:</b></p>
     * <p>I due confronti tra viste appartenenti a mappe diverse restituiscono
     * {@code false}; il confronto tra le due chiamate a
     * {@code map.values()} restituisce {@code true}.</p>
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
     * <p>Verifica {@code containsAll()} su {@code keySet()} con una chiave
     * presente e con una chiave assente.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La stessa vista di confronto contiene prima soltanto {@code a} e
     * viene poi estesa con {@code missing}. In questo modo il secondo risultato
     * cambia per una sola chiave.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea una mappa secondaria con la chiave {@code a} e passa il
     * suo {@code keySet()} a {@code containsAll()}. Aggiunge quindi
     * {@code missing} alla stessa mappa e ripete il controllo.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista controllata contiene le chiavi {@code a} e {@code b};
     * {@code missing} non è presente.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa secondaria contiene le chiavi {@code a} e
     * {@code missing}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La prima chiamata restituisce {@code true}; dopo l'aggiunta di
     * {@code missing}, la seconda restituisce {@code false}.</p>
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
     * <p>Verifica gli argomenti {@code null} nelle operazioni bulk di
     * {@code keySet()}, {@code values()} ed {@code entrySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Ogni vista viene controllata su una nuova mappa con
     * {@code a=1} e {@code b=2}. Dopo ciascuna eccezione vengono verificati
     * subito dimensione, associazioni, chiavi e valori, prima di passare
     * all'operazione successiva.</p>
     * <p><b>Test Description:</b></p>
     * <p>Per ciascuna vista il test esegue, tramite
     * {@link #assertNullBulkArgumentsRejected(HCollection)},
     * {@code containsAll(null)}, {@code addAll(null)},
     * {@code removeAll(null)} e {@code retainAll(null)}. La mappa viene
     * ricreata prima di controllare la vista successiva.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>All'inizio di ogni gruppo di quattro chiamate, la mappa ha dimensione
     * due e contiene {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo ogni eccezione la mappa conserva dimensione due, i valori
     * associati ad {@code a} e {@code b}, entrambe le chiavi ed entrambi i
     * valori.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Ognuna delle dodici operazioni lancia
     * {@link NullPointerException}; nessuna chiamata raggiunge
     * {@code fail()} e tutti i controlli sullo stato hanno esito positivo.</p>
     */
    @Test
    public void everyViewRejectsNullBulkArgumentsWithoutChanges() {
        map = populatedMap();
        assertNullBulkArgumentsRejected(map.keySet());

        map = populatedMap();
        assertNullBulkArgumentsRejected(map.values());

        map = populatedMap();
        assertNullBulkArgumentsRejected(map.entrySet());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il comportamento di {@code values().contains(null)} e lo
     * stato della mappa dopo l'eccezione.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La ricerca viene eseguita su una vista contenente due valori. Nel
     * blocco {@code catch} vengono controllate la dimensione, le due
     * associazioni e la presenza di chiavi e valori nelle rispettive viste.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test richiama {@code values().contains(null)} e usa
     * {@code fail()} se la chiamata termina normalmente. Quando intercetta
     * {@link NullPointerException}, verifica che il contenuto iniziale sia
     * ancora presente.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa ha dimensione due e contiene le associazioni {@code a=1} e
     * {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo l'eccezione la dimensione resta due; i valori associati, le
     * chiavi {@code a} e {@code b} e i valori {@code 1} e {@code 2} sono
     * ancora presenti.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La ricerca lancia {@link NullPointerException}, quindi
     * {@code fail()} non viene raggiunto. Tutti i controlli successivi hanno
     * esito positivo.</p>
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
     * <p>Oltre al valore restituito da {@code remove(null)}, vengono
     * controllati dimensione, associazioni, chiavi e valori della mappa. In
     * questo modo il risultato {@code false} è accompagnato dalla verifica
     * del contenuto.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test esegue {@code values().remove(null)}, controlla che il metodo
     * restituisca {@code false} e verifica subito lo stato della mappa e delle
     * sue viste.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa ha dimensione due e contiene {@code a=1} e
     * {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La dimensione resta due; le associazioni iniziali, le chiavi
     * {@code a} e {@code b} e i valori {@code 1} e {@code 2} sono ancora
     * presenti.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code remove(null)} restituisce {@code false} e tutti i controlli
     * successivi sul contenuto hanno esito positivo.</p>
     */
    @Test
    public void valuesRemoveNullReturnsFalseAndPreservesMap() {
        assertFalse(map.values().remove(null));
        assertFixtureUnchanged();
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code contains()} e {@code remove()} su
     * {@code entrySet()} con argomenti che non sono entry.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Vengono usati {@code null} e la stringa
     * {@code "not an entry"}, due argomenti che non rappresentano una
     * {@link HMap.Entry}. Al termine viene controllato l'intero contenuto
     * iniziale.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test richiama {@code contains()} e {@code remove()} prima con
     * {@code null} e poi con la stringa. Dopo i quattro risultati verifica
     * dimensione, associazioni, chiavi e valori della mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>{@code entrySet()} contiene le entry {@code a=1} e
     * {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa conserva dimensione due, le associazioni iniziali, entrambe
     * le chiavi ed entrambi i valori.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le quattro chiamate restituiscono {@code false}; tutti i controlli
     * finali sul contenuto hanno esito positivo.</p>
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
     * <p>Verifica due operazioni bulk che non devono modificare
     * {@code keySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>{@code removeAll()} riceve una vista contenente soltanto la chiave
     * assente {@code missing}; {@code retainAll()} riceve invece una vista con
     * entrambe le chiavi presenti. Lo stato viene controllato dopo ciascuna
     * operazione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test prepara la prima vista di confronto, richiama
     * {@code keySet().removeAll()} e verifica risultato e contenuto. Prepara
     * poi una vista con {@code a} e {@code b}, richiama
     * {@code retainAll()} e ripete gli stessi controlli.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>{@code keySet()} contiene le chiavi {@code a} e {@code b}, mentre
     * {@code missing} è assente.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo entrambe le operazioni la mappa ha dimensione due e conserva
     * {@code a=1}, {@code b=2}, le due chiavi e i due valori.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code removeAll()} e {@code retainAll()} restituiscono
     * {@code false}; i controlli eseguiti dopo ciascuna chiamata hanno esito
     * positivo.</p>
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
     * <p>Verifica due operazioni bulk che non devono modificare
     * {@code values()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>{@code removeAll()} riceve una collezione con il solo valore assente
     * {@code missing}; {@code retainAll()} riceve invece una collezione con
     * entrambi i valori presenti. Dopo ogni operazione viene controllato il
     * contenuto della mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea una vista contenente {@code missing}, esegue
     * {@code values().removeAll()} e verifica risultato e stato. Crea poi una
     * vista con {@code 1} e {@code 2}, esegue {@code retainAll()} e ripete i
     * controlli.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>{@code values()} contiene {@code 1} e {@code 2}, mentre
     * {@code missing} è assente.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo entrambe le operazioni la mappa ha dimensione due e conserva
     * {@code a=1}, {@code b=2}, le due chiavi e i due valori.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code removeAll()} e {@code retainAll()} restituiscono
     * {@code false}; i controlli eseguiti dopo ciascuna chiamata hanno esito
     * positivo.</p>
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
     * <p>Verifica {@code equals()} e {@code hashCode()} sulla vista
     * {@code keySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La seconda mappa contiene le stesse chiavi, inserite in ordine diverso,
     * ma valori differenti. In questo modo il confronto riguarda gli elementi
     * del {@code keySet()} e non i valori associati. Vengono controllati anche
     * {@code null}, un oggetto di tipo diverso, la stessa vista, un
     * {@code entrySet()} e un insieme con una chiave aggiuntiva.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test confronta la vista delle chiavi con {@code null}, una stringa
     * e se stessa. Successivamente confronta nei due versi due
     * {@code keySet()} con le stesse chiavi e verifica i relativi codici hash.
     * Infine esegue il confronto con un {@code entrySet()} e aggiunge la chiave
     * {@code c} alla seconda mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Entrambi i {@code keySet()} contengono le chiavi {@code a} e
     * {@code b}; nella seconda mappa sono associate a valori diversi.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La seconda mappa contiene anche {@code c=3}, quindi la sua vista delle
     * chiavi ha un elemento in più.</p>
     * <p><b>Expected Results:</b></p>
     * <p>I confronti con {@code null}, la stringa, {@code entrySet()} e il
     * {@code keySet()} con la chiave aggiuntiva restituiscono {@code false}.
     * Il confronto con la stessa vista e quelli nei due versi tra viste
     * equivalenti restituiscono {@code true}; le viste equivalenti hanno lo
     * stesso codice hash.</p>
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
     * <p>Verifica {@code equals()} e {@code hashCode()} sulla vista
     * {@code entrySet()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La seconda mappa contiene le stesse associazioni, inserite in ordine
     * diverso. Questo permette di confrontare il contenuto delle viste senza
     * dipendere dall'ordine con cui vengono restituite le entry. L'aggiunta di
     * {@code c=3} crea poi una vista con dimensione diversa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test confronta {@code entrySet()} con {@code null}, una stringa e
     * se stesso. Verifica poi l'uguaglianza nei due versi e i codici hash delle
     * due viste equivalenti. Dopo aver aggiunto {@code c=3} alla seconda
     * mappa, ripete il confronto.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Le due viste contengono le entry {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La seconda mappa contiene anche l'associazione {@code c=3}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>I confronti con {@code null} e con la stringa restituiscono
     * {@code false}, mentre quello con la stessa vista restituisce
     * {@code true}. Le viste equivalenti sono uguali nei due versi e hanno lo
     * stesso codice hash; dopo l'aggiunta di {@code c=3} non sono più uguali.</p>
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
     * <p>Verifica che {@code add()} non sia supportato dalle tre viste della
     * mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Per ogni vista viene usato un elemento compatibile: una chiave per
     * {@code keySet()}, un valore per {@code values()} e un'entry per
     * {@code entrySet()}. L'helper controlla sia l'eccezione prevista sia
     * l'assenza dell'elemento dopo il tentativo.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test prova ad aggiungere {@code c} alla vista delle chiavi,
     * {@code 3} alla vista dei valori e {@code c=3} alla vista delle entry.
     * Dopo i tre tentativi controlla la dimensione della mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}; gli elementi proposti
     * non sono presenti nelle rispettive viste.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Gli elementi proposti restano assenti e la dimensione della mappa
     * rimane due.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Ogni chiamata lancia
     * {@link MapAdapter.HUnsupportedOperationException}. I tre controlli di
     * assenza hanno esito positivo e la dimensione finale è due.</p>
     */
    @Test
    public void addIsUnsupportedByEveryView() {
        expectUnsupportedAdd(map.keySet(), "c");
        expectUnsupportedAdd(map.values(), "3");
        expectUnsupportedAdd(map.entrySet(), new EntryComp("c", "3"));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code addAll()} con una collezione vuota sulle tre viste.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La stessa collezione vuota viene passata a {@code keySet()},
     * {@code values()} ed {@code entrySet()}. Il caso controlla il risultato
     * dell'operazione quando non ci sono elementi da aggiungere.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test usa il {@code keySet()} di una nuova mappa vuota come
     * sorgente. Esegue {@code addAll()} sulle tre viste della mappa in esame e
     * controlla infine la sua dimensione.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La collezione sorgente è vuota e la mappa contiene due associazioni.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La dimensione della mappa rimane due.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le tre chiamate restituiscono {@code false} e la dimensione finale
     * della mappa è due.</p>
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
     * <p>Verifica che {@code addAll()} con una collezione non vuota non sia
     * supportato dalle tre viste.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Una mappa con l'associazione {@code c=3} fornisce una collezione non
     * vuota adatta a ciascuna vista: la chiave, il valore oppure l'entry. In
     * questo modo tutti i tentativi richiedono realmente l'aggiunta di un
     * elemento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa {@code source.keySet()}, {@code source.values()} e
     * {@code source.entrySet()} alle rispettive viste della mappa. L'helper
     * controlla l'eccezione per ogni chiamata; al termine viene verificata la
     * dimensione della mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La sorgente contiene {@code c=3}, mentre la mappa di destinazione
     * contiene due associazioni.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo i tre tentativi la dimensione della mappa di destinazione rimane
     * due.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Ogni operazione lancia
     * {@link MapAdapter.HUnsupportedOperationException} e la dimensione finale
     * della mappa è due.</p>
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
     * <p>Verifica il contenuto e l'indipendenza dell'array restituito da
     * {@code keySet().toArray()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le chiavi vengono cercate nell'array senza fissarne la posizione,
     * perché la mappa non stabilisce un ordine di iterazione. La modifica della
     * prima cella permette poi di controllare che il contenuto della vista non
     * cambi.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test converte {@code keySet()} in array, ne controlla la lunghezza
     * e cerca {@code a} e {@code b}. Sostituisce poi la prima cella con
     * {@code "changed"} e verifica nuovamente la presenza delle due chiavi
     * nella vista.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene le chiavi {@code a} e {@code b}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La prima cella dell'array è stata modificata, mentre
     * {@code keySet()} contiene ancora entrambe le chiavi.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'array ha lunghezza due e contiene {@code a} e {@code b}. Dopo la
     * modifica dell'array, le due chiamate a {@code contains()} restituiscono
     * {@code true}.</p>
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
     * <p>Verifica che {@code values().toArray()} conservi i valori duplicati e
     * controlla lo stato della mappa dopo la modifica di una cella
     * dell'array.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'associazione {@code c=1} introduce una seconda occorrenza del valore
     * {@code 1}. Le occorrenze vengono contate senza usare l'ordine
     * dell'array. Dopo il conteggio viene modificata una cella per controllare
     * che la mappa conservi dimensione e valori presenti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, converte {@code values()} in array,
     * verifica lunghezza tre, due occorrenze di {@code 1} e una di {@code 2},
     * modifica la prima cella e controlla dimensione della mappa e contenuto
     * della vista dei valori.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1}, {@code b=2} e {@code c=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo la modifica dell'array la dimensione della mappa vale tre e la
     * vista contiene ancora {@code 1} e {@code 2}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>L'array ha lunghezza tre, con due occorrenze di {@code 1} e una di
     * {@code 2}. Dopo la modifica della prima cella, la dimensione resta tre e
     * le ricerche di {@code 1} e {@code 2} nella vista restituiscono
     * {@code true}.</p>
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
     * <p>Verifica che l'array prodotto da {@code entrySet().toArray()} abbia
     * la lunghezza corretta e contenga entry coerenti con le associazioni
     * presenti.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>La lunghezza dell'array viene confrontata con la dimensione della
     * mappa. Ogni elemento viene poi controllato singolarmente per tipo, chiave
     * e valore, senza dipendere dall'ordine delle entry nell'array.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test converte {@code entrySet()} in array, confronta la lunghezza
     * con {@code map.size()} e, per ogni cella, verifica il tipo
     * {@link HMap.Entry}, la presenza della chiave nella mappa e l'uguaglianza
     * tra valore della mappa e valore esposto dall'entry.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene le associazioni {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'array restituito ha tante celle quante sono le associazioni della
     * mappa. Ogni cella contiene un'{@link HMap.Entry} con una chiave presente
     * nella mappa e il valore corrispondente.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La lunghezza è pari a {@code map.size()}; ogni cella contiene una
     * {@link HMap.Entry} la cui chiave è presente nella mappa e il cui valore
     * coincide con quello ottenuto tramite {@code map.get()}.</p>
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
     * <p>Verifica il riuso di un array più grande da parte di
     * {@code values().toArray(Object[])}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'array ha quattro celle, mentre la vista contiene due valori. Tutte
     * le celle vengono inizializzate con lo stesso oggetto di controllo: questo
     * permette di distinguere la cella successiva ai valori, che deve diventare
     * {@code null}, dall'ultima cella, che deve rimanere invariata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa l'array a {@code values().toArray()}, verifica che venga
     * restituita la stessa istanza e controlla le celle di indice 2 e 3.
     * Infine cerca nell'array i valori {@code 1} e {@code 2} senza fissarne la
     * posizione.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene {@code 1} e {@code 2}; l'array fornito ha quattro
     * celle inizializzate con lo stesso riferimento.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Lo stesso array contiene i due valori, {@code null} all'indice 2 e
     * conserva il riferimento iniziale all'indice 3.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code result} e {@code supplied} indicano lo stesso array. La cella 2
     * è {@code null}, la cella 3 contiene ancora l'oggetto iniziale e le
     * ricerche di {@code 1} e {@code 2} hanno esito positivo.</p>
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
     * <p>Verifica che {@code toArray(Object[])} crei un nuovo array quando
     * quello fornito è troppo piccolo.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Viene passato un {@code Object[]} di lunghezza zero a una vista che
     * contiene due chiavi. Il confronto tra i riferimenti controlla la nuova
     * allocazione, mentre lunghezza e contenuto verificano l'array ottenuto.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa un {@code Object[0]} a {@code keySet().toArray()},
     * controlla che il risultato sia un oggetto diverso, verifica la lunghezza
     * due e cerca le chiavi {@code a} e {@code b}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>{@code keySet()} contiene due chiavi e l'array fornito ha lunghezza
     * zero.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il metodo restituisce un nuovo array di lunghezza due.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il riferimento restituito è diverso da quello fornito; il nuovo array
     * ha lunghezza due e contiene {@code a} e {@code b}.</p>
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
     * <p>Verifica che {@code toArray(Object[])} riutilizzi un
     * {@code Object[]} con dimensione esatta.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'array fornito ha due celle, come il {@code keySet()}. Il confronto
     * dei riferimenti verifica che non venga creato un altro array; le chiavi
     * sono cercate per contenuto perché la loro posizione non è stabilita.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea un {@code Object[2]}, lo passa a
     * {@code keySet().toArray()}, confronta il risultato con l'array fornito e
     * cerca al suo interno {@code a} e {@code b}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene due chiavi e l'array fornito ha due celle.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>L'array fornito è stato riempito con le chiavi della mappa.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il riferimento restituito coincide con quello fornito e l'array
     * contiene le chiavi {@code a} e {@code b}.</p>
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
     * <p>Verifica il riuso di un {@code String[]} abbastanza grande per le
     * chiavi della mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le chiavi sono stringhe e l'array tipizzato ha tre celle, una in più
     * rispetto agli elementi della vista. Il test può quindi controllare sia
     * il riuso dell'array sia il valore {@code null} scritto nella prima cella
     * libera.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa un {@code String[3]} a {@code keySet().toArray()},
     * verifica che venga restituito lo stesso array, cerca le chiavi {@code a}
     * e {@code b} e controlla la cella di indice 2.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>{@code keySet()} contiene due stringhe e l'array fornito ha tre celle
     * di tipo {@link String}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Lo stesso {@code String[]} contiene le due chiavi e {@code null}
     * nella cella successiva.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il riferimento restituito coincide con quello fornito, le ricerche di
     * {@code a} e {@code b} hanno esito positivo e la cella di indice 2 è
     * {@code null}.</p>
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
     * <p>Verifica il comportamento di {@code toArray(Object[])} con un array
     * di tipo incompatibile con le chiavi.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>L'array {@code Integer[2]} ha spazio per entrambe le chiavi, ma il suo
     * tipo non permette di memorizzare oggetti {@link String}. La dimensione
     * esatta fa sì che il metodo provi a usare direttamente questo array.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa un {@code Integer[2]} a
     * {@code keySet().toArray(Object[])} e dichiara nell'annotazione JUnit
     * l'eccezione attesa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La vista contiene le stringhe {@code a} e {@code b}; l'array fornito
     * può contenere soltanto oggetti {@link Integer}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La copia delle chiavi si interrompe durante la scrittura nell'array.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La chiamata solleva {@link ArrayStoreException}.</p>
     */
    @Test(expected = ArrayStoreException.class)
    public void toArrayRejectsIncompatibleTypedArrayDuringStorage() {
        map.keySet().toArray(new Integer[2]);
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code toArray(Object[])} quando l'array fornito è
     * {@code null}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Il riferimento {@code null} viene passato direttamente alla vista
     * delle chiavi di una mappa popolata. L'annotazione JUnit specifica
     * l'eccezione richiesta per questo argomento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test esegue {@code map.keySet().toArray(null)} e attende
     * l'eccezione dichiarata da {@code @Test}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>{@code keySet()} contiene {@code a} e {@code b}; l'argomento passato
     * a {@code toArray()} è {@code null}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La chiamata termina senza restituire un array.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La chiamata solleva {@link NullPointerException}.</p>
     */
    @Test(expected = NullPointerException.class)
    public void toArrayRejectsNullArgument() {
        map.keySet().toArray(null);
    }

    /**
     * Entry usata nei test per rappresentare un'associazione attesa. Conserva
     * una chiave e un valore e implementa {@link HMap.Entry}, quindi può essere
     * confrontata con le entry restituite da {@link MapAdapter#entrySet()}.
     */
    private static final class EntryComp implements HMap.Entry {
        /** Chiave dell'associazione rappresentata dall'entry. */
        private final Object key;

        /** Valore corrente associato alla chiave. */
        private Object value;

        /**
         * Crea un'entry con la chiave e il valore indicati.
         *
         * @param entryKey chiave dell'entry
         * @param entryValue valore da associare alla chiave
         */
        private EntryComp(Object entryKey, Object entryValue) {
            key = entryKey;
            value = entryValue;
        }

        /**
         * Restituisce la chiave dell'entry.
         *
         * @return riferimento memorizzato in {@link #key}
         */
        public Object getKey() {
            return key;
        }

        /**
         * Restituisce il valore corrente dell'entry.
         *
         * @return riferimento memorizzato in {@link #value}
         */
        public Object getValue() {
            return value;
        }

        /**
         * Sostituisce il valore dell'entry e restituisce quello precedente.
         *
         * @param newValue valore da associare alla chiave
         * @return valore presente prima della sostituzione
         */
        public Object setValue(Object newValue) {
            Object previous = value;
            value = newValue;
            return previous;
        }

        /**
         * Confronta questa entry con un'altra {@link HMap.Entry}. Il risultato
         * è positivo soltanto se chiave e valore sono uguali.
         *
         * @param object oggetto da confrontare
         * @return {@code true} se {@code object} è un'entry con la stessa
         *         chiave e lo stesso valore, {@code false} altrimenti
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
         * Calcola il codice hash eseguendo lo XOR tra i codici della chiave e
         * del valore. Per un componente {@code null} usa zero.
         *
         * @return codice hash coerente con {@link #equals(Object)}
         */
        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * Confronta due oggetti, considerando uguali due riferimenti
         * {@code null}.
         *
         * @param first primo oggetto del confronto
         * @param second secondo oggetto del confronto
         * @return {@code true} se entrambi sono {@code null} oppure se
         *         {@code first.equals(second)} restituisce {@code true}
         */
        private static boolean equal(Object first, Object second) {
            return first == null ? second == null : first.equals(second);
        }
    }

    /**
     * Oggetto di supporto memorizzato nella mappa per verificare la direzione
     * di {@code equals()} usata da {@code values().remove()}. Il suo
     * {@code equals()} restituisce sempre {@code false}, mentre
     * {@link MatchingRemovalProbe} riconosce questo tipo.
     */
    private static final class StoredRejectingValue {
        /**
         * Restituisce {@code false} per qualsiasi oggetto ricevuto.
         *
         * @param object oggetto ricevuto, non usato dal metodo
         * @return sempre {@code false}
         */
        public boolean equals(Object object) {
            return false;
        }

        /**
         * Restituisce il codice hash costante usato da questo oggetto.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Oggetto di supporto passato a {@code values().remove()}. Considera
     * uguali soltanto le istanze di {@link StoredRejectingValue}, che invece
     * restituiscono sempre {@code false} nel confronto opposto.
     */
    private static final class MatchingRemovalProbe {
        /**
         * Controlla se l'oggetto ricevuto è uno
         * {@link StoredRejectingValue}.
         *
         * @param object oggetto da esaminare
         * @return {@code true} per un {@link StoredRejectingValue}
         */
        public boolean equals(Object object) {
            return object instanceof StoredRejectingValue;
        }

        /**
         * Restituisce il codice hash costante usato da questo oggetto.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Oggetto di supporto memorizzato nella mappa. Il suo {@code equals()}
     * riconosce {@link RejectingRemovalProbe}, mentre l'oggetto passato alla
     * rimozione restituisce sempre {@code false}. Questa differenza permette
     * di controllare la direzione del confronto.
     */
    private static final class StoredMatchingValue {
        /**
         * Controlla se l'oggetto ricevuto è un
         * {@link RejectingRemovalProbe}.
         *
         * @param object oggetto da esaminare
         * @return {@code true} per un {@link RejectingRemovalProbe}
         */
        public boolean equals(Object object) {
            return object instanceof RejectingRemovalProbe;
        }

        /**
         * Restituisce il codice hash costante usato da questo oggetto.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Oggetto di supporto passato a {@code values().remove()}. Il metodo
     * {@code equals()} restituisce sempre {@code false}, anche se
     * {@link StoredMatchingValue} riconosce questo tipo nel confronto opposto.
     */
    private static final class RejectingRemovalProbe {
        /**
         * Restituisce {@code false} per qualsiasi oggetto ricevuto.
         *
         * @param object oggetto ricevuto, non usato dal metodo
         * @return sempre {@code false}
         */
        public boolean equals(Object object) {
            return false;
        }

        /**
         * Restituisce il codice hash costante usato da questo oggetto.
         *
         * @return il valore costante {@code 1}
         */
        public int hashCode() {
            return 1;
        }
    }

    /**
     * Oggetto di supporto usato con {@code values().contains()}. Conserva un
     * riferimento e considera uguale soltanto quello stesso oggetto, usando il
     * confronto per identità.
     */
    private static final class AsymmetricContainsProbe {
        /** Unico riferimento che deve essere riconosciuto da {@link #equals(Object)}. */
        private final Object match;

        /**
         * Crea l'oggetto di supporto associato al riferimento atteso.
         *
         * @param expected riferimento da riconoscere
         */
        private AsymmetricContainsProbe(Object expected) {
            match = expected;
        }

        /**
         * Confronta l'oggetto ricevuto con il riferimento memorizzato usando
         * l'operatore {@code ==}.
         *
         * @param object oggetto da confrontare
         * @return {@code true} soltanto se {@code object == match}
         */
        public boolean equals(Object object) {
            return object == match;
        }

        /**
         * Restituisce il codice hash del riferimento memorizzato.
         *
         * @return codice hash del riferimento atteso
         */
        public int hashCode() {
            return match.hashCode();
        }
    }

    /**
     * Tenta di aggiungere un elemento a una vista. Il metodo fallisce se
     * {@code add()} termina normalmente; dopo
     * {@link MapAdapter.HUnsupportedOperationException} controlla che
     * l'elemento non sia presente.
     *
     * @param collection vista sulla quale chiamare {@code add()}
     * @param object elemento da aggiungere
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
     * Tenta di aggiungere gli elementi di una collezione a una vista. Il
     * metodo fallisce se {@code addAll()} non lancia
     * {@link MapAdapter.HUnsupportedOperationException}.
     *
     * @param destination vista sulla quale chiamare {@code addAll()}
     * @param source collezione passata come argomento
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
     * Cerca un elemento confrontandolo con tutte le celle dell'array.
     *
     * @param array array da esaminare
     * @param expected elemento cercato e primo operando di {@code equals()}
     * @return {@code true} se {@code expected.equals(array[index])} è vero per
     *         almeno un indice, {@code false} altrimenti
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
     * Conta quante celle dell'array sono uguali all'elemento indicato.
     *
     * @param array array da analizzare
     * @param expected elemento da contare e primo operando di {@code equals()}
     * @return numero di indici per cui
     *         {@code expected.equals(array[index])} restituisce {@code true}
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
     * Crea una nuova mappa contenente le due associazioni usate come
     * configurazione iniziale nei test.
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
     * Verifica che {@code containsAll(null)}, {@code addAll(null)},
     * {@code removeAll(null)} e {@code retainAll(null)} lancino
     * {@link NullPointerException}. Dopo ogni eccezione controlla che
     * {@link #map} contenga ancora i dati iniziali.
     *
     * @param view vista collegata a {@link #map} sulla quale eseguire le
     *             quattro operazioni
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
     * Controlla che {@link #map} abbia dimensione due e contenga ancora
     * {@code a=1} e {@code b=2}. Verifica le due associazioni sia tramite
     * {@code get()} sia attraverso le viste delle chiavi e dei valori.
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
