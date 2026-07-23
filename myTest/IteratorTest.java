package myTest;

import java.util.NoSuchElementException;
import myAdapter.HIterator;
import myAdapter.HMap;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case dedicato agli iteratori restituiti dalle viste di
 * {@link MapAdapter}.
 *
 * <p><b>Summary:</b>
 * La classe verifica il contratto locale di {@link HIterator} nelle situazioni
 * di attraversamento normale, iteratore vuoto, esaurimento e rimozione. I test
 * controllano gli iteratori di {@code keySet()}, {@code values()} ed
 * {@code entrySet()}, compresa la conservazione dei valori duplicati e
 * l'effetto di {@link HIterator#remove()} sulla mappa sottostante alle viste.
 * Vengono inoltre verificate {@link NoSuchElementException} e l'eccezione
 * locale {@link MapAdapter.HIllegalStateException} prevista dal progetto per
 * gli stati in cui {@code remove()} non è consentito. I sedici test riguardano
 * l'attraversamento delle tre viste e la gestione dei duplicati, i limiti di
 * {@code next()} e la natura non distruttiva di {@code hasNext()}, stati legali e
 * illegali di {@code remove()}, continuità e indipendenza degli iteratori
 * realizzati da {@code MapAdapter}.</p>
 *
 * <p><b>Test Case Design:</b>
 * La fixture contiene tre mapping con chiavi e valori distinti, così è
 * possibile controllare quantità, appartenenza e modifiche della mappa senza
 * dipendere dall'ordine non specificato della {@code Hashtable}. Mappe locali
 * vengono usate per isolare i casi vuoti e quello con valori duplicati. I test
 * verificano congiuntamente ciò che l'iteratore restituisce e lo stato della
 * mappa dopo le rimozioni, perché il solo conteggio non individuerebbe un
 * mapping eliminato in modo errato. Nei casi di attraversamento non viene mai
 * imposto un ordine, dato che la {@code Hashtable} non ne garantisce uno. Le
 * prove che rimuovono e poi proseguono esercitano una scelta concreta di
 * {@code MapAdapter}: al momento della costruzione ogni iteratore memorizza uno
 * snapshot delle sole chiavi, usa un proprio cursore e consulta la mappa
 * originale per produrre valori o entry e per applicare {@code remove()}.
 * Questa strategia viene documentata separatamente dal contratto generale di
 * {@link HIterator}. Non sono introdotte modifiche strutturali esterne durante
 * l'iterazione e non viene quindi affermato né richiesto un comportamento
 * fail-fast.</p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see HIterator
 * @see HMap
 * @see MapAdapter
 */
public class IteratorTest {
    /**
     * Mappa realizzata con una nuova istanza di {@link MapAdapter} e ricreata
     * prima di ogni test. I tre mapping distinti permettono di riconoscere le
     * rimozioni senza fare assunzioni sull'ordine di iterazione.
     */
    private HMap map;

    /**
     * <p><b>Summary:</b>
     * Prepara la fixture condivisa dai test sugli iteratori.</p>
     *
     * <p><b>Test Case Design:</b>
     * Vengono scelti tre mapping con chiavi e valori differenti per rendere
     * osservabili sia un attraversamento completo sia la rimozione di un solo
     * elemento, indipendentemente dall'ordine della {@code Hashtable}.</p>
     *
     * <p><b>Test Description:</b>
     * Crea un nuovo {@link MapAdapter} e inserisce i mapping {@code a=1},
     * {@code b=2} e {@code c=3}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * JUnit sta iniziando un nuovo metodo di test; l'eventuale fixture usata in
     * precedenza non deve essere riutilizzata.</p>
     *
     * <p><b>Post-Condition:</b>
     * {@code map} fa riferimento a una mappa nuova contenente esattamente tre
     * mapping.</p>
     *
     * <p><b>Expected Results:</b>
     * Ogni test parte dallo stesso contenuto noto, ma da un'istanza indipendente
     * da quelle impiegate dagli altri test.</p>
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code hasNext()} restituisca {@code false} per un iteratore
     * vuoto.</p>
     *
     * <p><b>Test Case Design:</b>
     * Si usa una mappa locale appena costruita, invece della fixture popolata,
     * per rappresentare direttamente il caso limite di una collezione vuota.</p>
     *
     * <p><b>Test Description:</b>
     * Ottiene l'iteratore di {@code keySet()} da una nuova mappa e invoca
     * {@link HIterator#hasNext()} prima di qualsiasi chiamata a
     * {@link HIterator#next()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa locale e la sua vista delle chiavi non contengono elementi.</p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non modifica esplicitamente la mappa locale e non ne conserva un
     * riferimento per ricontrollarne lo stato.</p>
     *
     * <p><b>Expected Results:</b>
     * {@code hasNext()} restituisce {@code false}.</p>
     */
    @Test
    public void emptyIteratorHasNoNextElement() {
        HIterator iterator = new MapAdapter().keySet().iterator();
        assertFalse(iterator.hasNext());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che l'iteratore delle chiavi visiti una volta ciascuna chiave
     * quando la mappa non viene modificata.</p>
     *
     * <p><b>Test Case Design:</b>
     * Una seconda {@link HMap} registra le chiavi già incontrate. Questa scelta
     * consente di controllare contemporaneamente appartenenza, assenza di
     * duplicazioni e completezza, senza imporre una sequenza di visita.</p>
     *
     * <p><b>Test Description:</b>
     * Attraversa {@code keySet()}, verifica che ogni risultato sia una chiave
     * della fixture e non sia già stato registrato, quindi confronta il numero
     * di chiavi visitate con la dimensione della mappa.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre chiavi distinte e resta invariata per tutta
     * l'iterazione.</p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa ausiliaria contiene tutte e sole le chiavi osservate dal ciclo.
     * Il test non ricontrolla separatamente i mapping della fixture.</p>
     *
     * <p><b>Expected Results:</b>
     * Ogni chiave appartiene alla mappa, non compare due volte e il totale dei
     * risultati è uguale a {@code map.size()}.</p>
     */
    @Test
    public void keyIteratorVisitsEveryKeyOnceWhenMapIsUnmodified() {
        HIterator iterator = map.keySet().iterator();
        HMap visited = new MapAdapter();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            assertTrue(map.containsKey(key));
            assertFalse(visited.containsKey(key));
            visited.put(key, "seen");
        }
        assertEquals(map.size(), visited.size());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che l'iteratore di {@code values()} conservi i valori duplicati.</p>
     *
     * <p><b>Test Case Design:</b>
     * Una mappa locale associa due chiavi diverse allo stesso valore
     * {@code "x"}. Il caso distingue la semantica di una collezione di valori
     * da quella degli insiemi restituiti da {@code keySet()} ed
     * {@code entrySet()}.</p>
     *
     * <p><b>Test Description:</b>
     * Attraversa la vista dei valori, controlla ogni elemento restituito e
     * conta il numero complessivo delle occorrenze.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa locale contiene due mapping distinti, entrambi con valore
     * {@code "x"}.</p>
     *
     * <p><b>Post-Condition:</b>
     * Il ciclo termina dopo due risultati. Il test non esegue operazioni
     * mutative, ma non ricontrolla i mapping della mappa locale.</p>
     *
     * <p><b>Expected Results:</b>
     * L'iteratore restituisce due volte {@code "x"}; nessuna delle due
     * occorrenze viene eliminata come duplicato.</p>
     */
    @Test
    public void valueIteratorPreservesDuplicateValues() {
        HMap duplicates = new MapAdapter();
        duplicates.put("a", "x");
        duplicates.put("b", "x");
        HIterator iterator = duplicates.values().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            assertEquals("x", iterator.next());
            count++;
        }
        assertEquals(2, count);
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che l'iteratore di {@code entrySet()} produca tre entry, ciascuna
     * coerente con un mapping corrente della fixture.</p>
     *
     * <p><b>Test Case Design:</b>
     * Ogni oggetto restituito viene interpretato come {@link HMap.Entry}; il
     * cast rende osservabile anche un eventuale tipo errato. Presenza della
     * chiave e uguaglianza del valore controllano la validità di ciascun
     * risultato, mentre il conteggio finale verifica che siano prodotte tre
     * entry senza imporre un ordine. Non si attribuisce a questo metodo una
     * verifica esplicita dell'unicità, perché il codice non mantiene un insieme
     * separato delle chiavi già visitate.</p>
     *
     * <p><b>Test Description:</b>
     * Crea l'iteratore di {@code entrySet()} e inizializza il contatore a zero.
     * A ogni passo converte il risultato in {@link HMap.Entry}, verifica che la
     * chiave appartenga alla mappa, confronta il valore dell'entry con
     * {@code map.get(entry.getKey())} e incrementa il contatore. Al termine
     * confronta il conteggio con {@code map.size()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e non viene modificata durante
     * l'attraversamento.</p>
     *
     * <p><b>Post-Condition:</b>
     * Il ciclo termina dopo un numero di risultati pari a {@code map.size()}.
     * Il test non ricontrolla lo stato finale della mappa e non registra le
     * chiavi già incontrate.</p>
     *
     * <p><b>Expected Results:</b>
     * Ciascun'entry prodotta possiede una chiave presente e il valore associato
     * corrente; il numero complessivo dei risultati è pari a tre, cioè a
     * {@code map.size()}.</p>
     */
    @Test
    public void entryIteratorVisitsEveryMapping() {
        HIterator iterator = map.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            HMap.Entry entry = (HMap.Entry) iterator.next();
            assertTrue(map.containsKey(entry.getKey()));
            assertEquals(map.get(entry.getKey()), entry.getValue());
            count++;
        }
        assertEquals(map.size(), count);
    }

    /**
     * <p><b>Summary:</b>
     * Verifica l'eccezione prodotta da {@code next()} su un iteratore vuoto.</p>
     *
     * <p><b>Test Case Design:</b>
     * Il caso usa la vista {@code values()} di una nuova mappa vuota e invoca
     * direttamente {@code next()}, così viene esercitato il caso limite
     * dell'iteratore vuoto senza dipendere dalla fixture.</p>
     *
     * <p><b>Test Description:</b>
     * Costruisce mappa, vista e iteratore in un'unica espressione, quindi tenta
     * di ottenere un elemento inesistente.</p>
     *
     * <p><b>Pre-Condition:</b>
     * L'iteratore non contiene elementi e non ha mai restituito un valore.</p>
     *
     * <p><b>Post-Condition:</b>
     * Nessun elemento viene prodotto e l'esecuzione del test termina quando
     * JUnit riconosce l'eccezione attesa.</p>
     *
     * <p><b>Expected Results:</b>
     * {@link HIterator#next()} solleva {@link NoSuchElementException}, come
     * previsto dal contratto di attraversamento.</p>
     */
    @Test(expected = NoSuchElementException.class)
    public void nextOnEmptyIteratorThrowsNoSuchElementException() {
        new MapAdapter().values().iterator().next();
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code next()} sollevi un'eccezione dopo l'esaurimento
     * dell'iteratore.</p>
     *
     * <p><b>Test Case Design:</b>
     * L'iteratore viene consumato con {@code hasNext()} invece di assumere il
     * numero o l'ordine delle chiavi. Una successiva chiamata a {@code next()}
     * controlla lo stato oltre l'ultimo elemento.</p>
     *
     * <p><b>Test Description:</b>
     * Attraversa completamente {@code keySet()}, tenta un'ulteriore lettura e,
     * dopo aver intercettato l'eccezione, verifica di nuovo
     * {@code hasNext()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore è inizialmente posizionato
     * prima del primo elemento.</p>
     *
     * <p><b>Post-Condition:</b>
     * Dopo l'eccezione {@code hasNext()} restituisce ancora {@code false}. Il
     * test non esegue mutazioni, ma non ricontrolla il contenuto della mappa.</p>
     *
     * <p><b>Expected Results:</b>
     * La lettura successiva all'ultimo elemento solleva
     * {@link NoSuchElementException} e {@code hasNext()} continua a restituire
     * {@code false}.</p>
     */
    @Test
    public void nextAfterExhaustionThrowsNoSuchElementException() {
        HIterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        try {
            iterator.next();
            fail();
        } catch (NoSuchElementException expected) {
            assertFalse(iterator.hasNext());
        }
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code remove()} non sia consentito prima di
     * {@code next()}.</p>
     *
     * <p><b>Test Case Design:</b>
     * Il metodo di rimozione viene invocato appena creato l'iteratore, quando
     * non esiste ancora un ultimo elemento restituito. Si controlla anche la
     * dimensione per escludere una rimozione accidentale.</p>
     *
     * <p><b>Test Description:</b>
     * Crea l'iteratore delle chiavi, chiama immediatamente {@code remove()} e
     * intercetta il tipo di eccezione locale scelto per CLDC 1.1.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e sull'iteratore non è mai stato invocato
     * con successo {@code next()}.</p>
     *
     * <p><b>Post-Condition:</b>
     * Dopo l'eccezione {@code map.size()} restituisce ancora {@code 3}; il test
     * non ricontrolla singolarmente i mapping.</p>
     *
     * <p><b>Expected Results:</b>
     * {@code remove()} solleva {@link MapAdapter.HIllegalStateException}, non
     * la classe standard assente nel profilo CLDC adottato.</p>
     */
    @Test
    public void removeBeforeNextThrowsHIllegalStateException() {
        HIterator iterator = map.keySet().iterator();
        try {
            iterator.remove();
            fail();
        } catch (MapAdapter.HIllegalStateException expected) {
            assertEquals(3, map.size());
        }
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code remove()} elimini il mapping dell'ultima chiave
     * restituita.</p>
     *
     * <p><b>Test Case Design:</b>
     * La chiave viene salvata dal risultato di {@code next()}, evitando di
     * prevedere quale elemento venga visitato per primo. I controlli successivi
     * verificano la chiave rimossa e la riduzione della dimensione da tre a due.</p>
     *
     * <p><b>Test Description:</b>
     * Legge una chiave, invoca {@code remove()} sullo stesso iteratore e
     * verifica il contenuto attraverso la mappa e la vista {@code keySet()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore delle chiavi è appena stato
     * creato; non è ancora stata eseguita alcuna rimozione.</p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping associato alla chiave restituita non è più presente e la mappa
     * contiene due elementi.</p>
     *
     * <p><b>Expected Results:</b>
     * La chiave restituita non è più presente né nella mappa né nella vista
     * delle chiavi, e {@code map.size()} restituisce {@code 2}.</p>
     */
    @Test
    public void removeAfterNextDeletesLastReturnedMapping() {
        HIterator iterator = map.keySet().iterator();
        Object key = iterator.next();
        iterator.remove();
        assertFalse(map.containsKey(key));
        assertEquals(2, map.size());
        assertFalse(map.keySet().contains(key));
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che una chiamata a {@code hasNext()} non renda illegale la
     * rimozione dell'ultimo elemento restituito.</p>
     *
     * <p><b>Test Case Design:</b>
     * Tra {@code next()} e {@code remove()} viene inserita soltanto una
     * chiamata a {@code hasNext()}. Il caso controlla che questa chiamata non
     * renda illegale la rimozione; la mancata consumazione degli elementi è
     * verificata separatamente da {@link #repeatedHasNextDoesNotConsumeElements()}.</p>
     *
     * <p><b>Test Description:</b>
     * Memorizza la prima chiave restituita, invoca {@code hasNext()}, quindi
     * rimuove e controlla la mappa.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore delle chiavi è appena stato
     * creato.</p>
     *
     * <p><b>Post-Condition:</b>
     * La chiave memorizzata non è più presente e la dimensione della mappa è
     * pari a due.</p>
     *
     * <p><b>Expected Results:</b>
     * Dopo la chiamata intermedia a {@code hasNext()}, {@code remove()} termina
     * normalmente; la chiave salvata non è più presente e la dimensione vale
     * due.</p>
     */
    @Test
    public void hasNextBetweenNextAndRemoveKeepsRemoveLegal() {
        HIterator iterator = map.keySet().iterator();
        Object key = iterator.next();
        iterator.hasNext();
        iterator.remove();
        assertFalse(map.containsKey(key));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che un {@code next()} fallito non sostituisca l'ultimo elemento
     * restituito con successo come target di {@code remove()}.</p>
     *
     * <p><b>Test Case Design:</b>
     * Durante l'attraversamento viene conservata l'ultima chiave effettivamente
     * ottenuta. Dopo il tentativo oltre la fine si controlla prima che la chiave
     * esista ancora e poi che possa essere rimossa.</p>
     *
     * <p><b>Test Description:</b>
     * Esaurisce l'iteratore, intercetta {@link NoSuchElementException} da una
     * lettura ulteriore e invoca infine {@code remove()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore delle chiavi è inizialmente
     * posizionato prima del primo risultato.</p>
     *
     * <p><b>Post-Condition:</b>
     * L'ultima chiave ottenuta viene rimossa e la mappa conserva due mapping.</p>
     *
     * <p><b>Expected Results:</b>
     * Il tentativo oltre la fine solleva {@code NoSuchElementException}, ma non
     * rende illegale la successiva rimozione dell'ultimo risultato valido.</p>
     */
    @Test
    public void failedNextAfterEndPreservesLastSuccessfulRemoveTarget() {
        HIterator iterator = map.keySet().iterator();
        Object last = null;
        while (iterator.hasNext()) {
            last = iterator.next();
        }

        try {
            iterator.next();
            fail();
        } catch (NoSuchElementException expected) {
            assertTrue(map.containsKey(last));
        }

        iterator.remove();
        assertFalse(map.containsKey(last));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica l'interazione tra un'entry collegata alla mappa,
     * {@code setValue()} e la
     * successiva rimozione tramite iteratore.</p>
     *
     * <p><b>Test Case Design:</b>
     * Il valore della prima entry viene sostituito prima di rimuoverla. Salvare
     * il valore precedente e quello aggiornato permette di verificare che, dopo
     * la rimozione, nessuno dei due sia ancora presente nella vista dei valori.</p>
     *
     * <p><b>Test Description:</b>
     * Ottiene un'entry di tipo {@link HMap.Entry}, ne salva chiave e valore,
     * aggiorna il
     * mapping con {@link HMap.Entry#setValue(Object)} e chiama
     * {@code remove()} sul medesimo iteratore.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore delle entry è appena stato
     * creato.</p>
     *
     * <p><b>Post-Condition:</b>
     * La chiave dell'entry non è più presente, la mappa contiene due mapping e
     * la vista dei valori non contiene né il vecchio né il nuovo valore di quel
     * mapping.</p>
     *
     * <p><b>Expected Results:</b>
     * {@code remove()} opera sulla chiave ricordata dall'iteratore ed elimina
     * l'associazione completa anche dopo il cambio di valore.</p>
     */
    @Test
    public void entrySetValueThenIteratorRemoveDeletesWholeMapping() {
        HIterator iterator = map.entrySet().iterator();
        HMap.Entry entry = (HMap.Entry) iterator.next();
        Object key = entry.getKey();
        Object oldValue = entry.getValue();
        Object newValue = "updated";

        entry.setValue(newValue);
        iterator.remove();

        assertFalse(map.containsKey(key));
        assertFalse(map.values().contains(oldValue));
        assertFalse(map.values().contains(newValue));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che lo stesso elemento non possa essere rimosso due volte
     * consecutive tramite l'iteratore.</p>
     *
     * <p><b>Test Case Design:</b>
     * Dopo una sequenza valida {@code next()}-{@code remove()} viene ripetuta
     * soltanto la rimozione. La dimensione finale permette di verificare che il
     * secondo tentativo non elimini un altro mapping.</p>
     *
     * <p><b>Test Description:</b>
     * Usa l'iteratore dei valori, rimuove il primo mapping restituito e tenta
     * immediatamente una seconda chiamata a {@code remove()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore dei valori è appena stato
     * creato.</p>
     *
     * <p><b>Post-Condition:</b>
     * È stato eliminato un solo mapping e la dimensione resta pari a due.</p>
     *
     * <p><b>Expected Results:</b>
     * Il secondo tentativo solleva
     * {@link MapAdapter.HIllegalStateException} e la dimensione rimane pari a
     * due.</p>
     */
    @Test
    public void twoConsecutiveRemovesThrowHIllegalStateException() {
        HIterator iterator = map.values().iterator();
        iterator.next();
        iterator.remove();
        try {
            iterator.remove();
            fail();
        } catch (MapAdapter.HIllegalStateException expected) {
            assertEquals(2, map.size());
        }
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che l'iteratore possa rimuovere tutti i mapping durante lo stesso
     * attraversamento.</p>
     *
     * <p><b>Test Case Design:</b>
     * Ogni {@code next()} è seguito immediatamente da {@code remove()} e un
     * contatore registra le operazioni eseguite. Il risultato osservato è
     * coerente con lo snapshot di chiavi usato da {@code MapAdapter}, che
     * mantiene separato l'avanzamento dalla mappa modificata dall'iteratore.</p>
     *
     * <p><b>Test Description:</b>
     * Attraversa {@code entrySet()}, rimuove ogni entry restituita e controlla
     * infine contatore, mappa e vista.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e non viene modificata direttamente
     * durante l'attraversamento.</p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa e la sua vista delle entry sono vuote.</p>
     *
     * <p><b>Expected Results:</b>
     * Il contatore vale tre, {@code map.isEmpty()} restituisce {@code true} e
     * anche {@code map.entrySet().isEmpty()} restituisce {@code true}. Il
     * risultato documenta che l'iteratore snapshot di {@code MapAdapter}
     * supporta le proprie rimozioni durante l'attraversamento; non formula
     * alcuna aspettativa sulle modifiche strutturali eseguite dall'esterno.</p>
     */
    @Test
    public void iteratorCanRemoveEveryMapping() {
        HIterator iterator = map.entrySet().iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            removed++;
        }
        assertEquals(3, removed);
        assertTrue(map.isEmpty());
        assertTrue(map.entrySet().isEmpty());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che l'iteratore continui l'attraversamento dopo una propria
     * rimozione.</p>
     *
     * <p><b>Test Case Design:</b>
     * Dopo la sequenza {@code next()}-{@code remove()}, il test conta i
     * risultati successivi e verifica che siano diversi dalla prima chiave
     * restituita. Non controlla direttamente quali mapping siano rimasti nella
     * mappa.</p>
     *
     * <p><b>Test Description:</b>
     * Esegue {@code next()} e {@code remove()}, poi consuma il resto
     * dell'iteratore contando le chiavi e confrontandole con la prima chiave
     * restituita.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping; l'unica modifica durante
     * l'attraversamento è quella richiesta allo stesso iteratore.</p>
     *
     * <p><b>Post-Condition:</b>
     * Il ciclo successivo alla rimozione produce due risultati diversi dalla
     * prima chiave restituita e la dimensione della mappa vale due. Il test non
     * controlla direttamente quali mapping siano rimasti.</p>
     *
     * <p><b>Expected Results:</b>
     * Dopo la rimozione vengono restituiti altri due elementi, nessuno dei quali
     * coincide con la prima chiave restituita, e {@code map.size()} vale due.</p>
     */
    @Test
    public void iteratorContinuesAfterItsOwnRemove() {
        HIterator iterator = map.keySet().iterator();
        Object removed = iterator.next();
        iterator.remove();
        int remaining = 0;
        while (iterator.hasNext()) {
            Object current = iterator.next();
            assertFalse(removed.equals(current));
            remaining++;
        }
        assertEquals(2, remaining);
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che due chiamate iniziali a {@code hasNext()} non riducano il
     * numero di risultati prodotti dal ciclo successivo.</p>
     *
     * <p><b>Test Case Design:</b>
     * Prima di iniziare il ciclo, {@code hasNext()} viene invocato due volte.
     * Il conteggio completo delle tre chiavi distingue un semplice controllo
     * dello stato da un avanzamento dell'iteratore.</p>
     *
     * <p><b>Test Description:</b>
     * Interroga due volte l'iteratore delle chiavi e successivamente lo consuma
     * con una normale sequenza {@code hasNext()}-{@code next()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore è posizionato prima del
     * primo elemento.</p>
     *
     * <p><b>Post-Condition:</b>
     * L'iteratore è esaurito dopo tre risultati. Il test non esegue mutazioni,
     * ma non ricontrolla i mapping della fixture.</p>
     *
     * <p><b>Expected Results:</b>
     * Entrambe le interrogazioni iniziali restituiscono {@code true} e il ciclo
     * successivo produce comunque tre elementi.</p>
     */
    @Test
    public void repeatedHasNextDoesNotConsumeElements() {
        HIterator iterator = map.keySet().iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        assertEquals(3, count);
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che l'esaurimento del primo iteratore non riduca il numero di
     * risultati prodotti dal secondo.</p>
     *
     * <p><b>Test Case Design:</b>
     * I due iteratori vengono creati prima di iniziare le visite e poi consumati
     * separatamente. Si confrontano soltanto i conteggi: ciò permette di
     * controllare che esaurire il primo non riduca i risultati disponibili nel
     * secondo, ma non verifica contenuto o unicità delle chiavi prodotte.</p>
     *
     * <p><b>Test Description:</b>
     * Crea due iteratori ottenuti da {@code keySet()} della stessa mappa,
     * attraversa completamente prima l'uno e poi l'altro e registra quanti
     * elementi produce ciascuno.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e non viene modificata tra la creazione e
     * l'esaurimento dei due iteratori.</p>
     *
     * <p><b>Post-Condition:</b>
     * Entrambi gli iteratori risultano esauriti dopo tre risultati. Il test non
     * esegue mutazioni, ma non ricontrolla il contenuto della mappa.</p>
     *
     * <p><b>Expected Results:</b>
     * Ogni iteratore produce tre risultati; l'esaurimento del primo non riduce
     * il conteggio ottenuto dal secondo. Non viene controllato quali chiavi
     * siano restituite né se siano prive di duplicati.</p>
     */
    @Test
    public void independentIteratorsTraverseAllKeysWhenMapIsUnmodified() {
        HIterator first = map.keySet().iterator();
        HIterator second = map.keySet().iterator();
        int firstCount = 0;
        int secondCount = 0;
        while (first.hasNext()) {
            first.next();
            firstCount++;
        }
        while (second.hasNext()) {
            second.next();
            secondCount++;
        }
        assertEquals(3, firstCount);
        assertEquals(3, secondCount);
    }
}
