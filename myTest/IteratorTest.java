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
 * l'effetto di {@link HIterator#remove()} sulla mappa sostenuta dalle viste.
 * Vengono inoltre verificate {@link NoSuchElementException} e l'eccezione
 * locale {@link MapAdapter.HIllegalStateException} prevista dal progetto per
 * gli stati in cui {@code remove()} non è consentito.</p>
 *
 * <p><b>Test Case Design:</b>
 * La fixture contiene tre mapping con chiavi e valori distinti, così è
 * possibile controllare quantità, appartenenza e modifiche della mappa senza
 * dipendere dall'ordine non specificato della {@code Hashtable}. Mappe locali
 * vengono usate per isolare i casi vuoti e quello con valori duplicati. I test
 * che rimuovono durante l'attraversamento esercitano la scelta concreta di
 * {@code MapAdapter}: ogni iteratore conserva uno snapshot delle sole chiavi e
 * usa la mappa originale per applicare {@code remove()}. Non vengono invece
 * effettuate modifiche strutturali esterne durante l'iterazione e non viene
 * richiesto un comportamento fail-fast.</p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see HIterator
 * @see HMap
 * @see MapAdapter
 */
public class IteratorTest {
    /**
     * Mappa sostenuta da una nuova istanza di {@link MapAdapter} e ricreata
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
     * Verifica che l'iteratore di una vista vuota non annunci elementi.</p>
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
     * L'iteratore resta posizionato oltre l'insieme vuoto e la mappa locale non
     * viene modificata.</p>
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
     * La fixture conserva i tre mapping iniziali e la mappa ausiliaria contiene
     * tutte e sole le chiavi visitate.</p>
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
     * La mappa locale conserva entrambi i mapping e l'iteratore risulta
     * esaurito dopo due risultati.</p>
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
     * Verifica che l'iteratore di {@code entrySet()} rappresenti tutti i
     * mapping della fixture.</p>
     *
     * <p><b>Test Case Design:</b>
     * Ogni oggetto restituito viene interpretato come {@link HMap.Entry} e
     * confrontato con la mappa tramite chiave e valore. Il conteggio finale
     * controlla la completezza senza dipendere dall'ordine.</p>
     *
     * <p><b>Test Description:</b>
     * Attraversa l'insieme delle entry, verifica che ogni chiave sia presente e
     * che {@code getValue()} coincida con il valore corrente letto dalla mappa,
     * quindi conta le entry prodotte.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e non viene modificata durante
     * l'attraversamento.</p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa mantiene il contenuto iniziale e l'iteratore ha visitato tutte
     * le entry.</p>
     *
     * <p><b>Expected Results:</b>
     * Ogni entry descrive un mapping effettivo e il numero delle entry
     * restituite coincide con {@code map.size()}.</p>
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
     * direttamente {@code next()}, così viene esercitato il limite inferiore
     * dell'attraversamento senza dipendere dalla fixture.</p>
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
     * L'iteratore resta esaurito e la mappa non viene modificata.</p>
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
     * Nessun mapping viene rimosso e la dimensione della mappa resta tre.</p>
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
     * prevedere quale elemento venga visitato per primo. Presenza e dimensione
     * controllano sia il target della rimozione sia il suo unico effetto.</p>
     *
     * <p><b>Test Description:</b>
     * Legge una chiave, invoca {@code remove()} sullo stesso iteratore e
     * verifica il contenuto attraverso la mappa e la vista {@code keySet()}.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e {@code next()} ha restituito una delle
     * tre chiavi.</p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping associato alla chiave restituita non è più presente e la mappa
     * contiene due elementi.</p>
     *
     * <p><b>Expected Results:</b>
     * La rimozione è visibile sia dalla mappa sia dalla sua vista backed delle
     * chiavi.</p>
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
     * interrogazione dello stato. Il caso distingue un controllo non
     * distruttivo da una nuova chiamata a {@code next()}, che cambierebbe il
     * target della rimozione.</p>
     *
     * <p><b>Test Description:</b>
     * Memorizza la prima chiave restituita, invoca {@code hasNext()}, quindi
     * rimuove e controlla la mappa.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e l'iteratore ha un ultimo elemento
     * restituito valido.</p>
     *
     * <p><b>Post-Condition:</b>
     * La chiave memorizzata non è più presente e la dimensione della mappa è
     * pari a due.</p>
     *
     * <p><b>Expected Results:</b>
     * {@code remove()} termina normalmente perché {@code hasNext()} non
     * consuma elementi e non modifica lo stato di rimozione.</p>
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
     * La fixture contiene tre mapping; prima del tentativo fallito l'iteratore
     * ha restituito con successo tutte le chiavi.</p>
     *
     * <p><b>Post-Condition:</b>
     * L'ultima chiave ottenuta viene rimossa e la mappa conserva due mapping.</p>
     *
     * <p><b>Expected Results:</b>
     * Il tentativo oltre la fine solleva {@code NoSuchElementException}, ma non
     * cancella il diritto di rimuovere l'ultimo risultato valido.</p>
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
     * Verifica l'interazione tra una entry backed, {@code setValue()} e la
     * successiva rimozione tramite iteratore.</p>
     *
     * <p><b>Test Case Design:</b>
     * Il valore della prima entry viene sostituito prima di rimuoverla. I valori
     * iniziali distinti e il nuovo valore {@code "updated"} permettono di
     * controllare che scompaia l'intero mapping, non soltanto una delle due
     * rappresentazioni del valore.</p>
     *
     * <p><b>Test Description:</b>
     * Ottiene una {@link HMap.Entry}, ne salva chiave e valore, aggiorna il
     * mapping con {@link HMap.Entry#setValue(Object)} e chiama
     * {@code remove()} sul medesimo iteratore.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping; l'iteratore ha restituito una entry
     * ancora collegata alla mappa.</p>
     *
     * <p><b>Post-Condition:</b>
     * La chiave della entry non è più presente, la mappa contiene due mapping e
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
     * La fixture contiene tre mapping; dopo la prima rimozione non è stato
     * eseguito un nuovo {@code next()}.</p>
     *
     * <p><b>Post-Condition:</b>
     * È stato eliminato un solo mapping e la dimensione resta pari a due.</p>
     *
     * <p><b>Expected Results:</b>
     * Il secondo tentativo solleva
     * {@link MapAdapter.HIllegalStateException} e non produce ulteriori
     * modifiche.</p>
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
     * contatore registra le operazioni eseguite. Il caso esercita la scelta
     * concreta dello snapshot di chiavi, che mantiene separato lo stato di
     * attraversamento dalla mappa modificata dallo stesso iteratore.</p>
     *
     * <p><b>Test Description:</b>
     * Attraversa {@code entrySet()}, rimuove ogni entry restituita e controlla
     * infine contatore, mappa e vista.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e nessuna modifica strutturale viene
     * eseguita dall'esterno dell'iteratore.</p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa e la sua vista delle entry sono vuote.</p>
     *
     * <p><b>Expected Results:</b>
     * Vengono completate tre rimozioni e l'iteratore termina normalmente senza
     * richiedere un comportamento fail-fast.</p>
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
     * La prima chiave restituita viene rimossa e usata come riferimento per
     * controllare che non ricompaia. Il numero degli elementi successivi
     * verifica il comportamento concreto dello snapshot senza assumere un
     * ordine.</p>
     *
     * <p><b>Test Description:</b>
     * Esegue {@code next()} e {@code remove()}, poi consuma il resto
     * dell'iteratore contando le chiavi e confrontandole con quella eliminata.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping; l'unica modifica durante
     * l'attraversamento è quella richiesta allo stesso iteratore.</p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa contiene i due mapping non rimossi e l'iteratore ha completato
     * il proprio snapshot.</p>
     *
     * <p><b>Expected Results:</b>
     * Dopo la rimozione vengono restituiti altri due elementi, nessuno dei quali
     * coincide con la chiave eliminata, e {@code map.size()} vale due.</p>
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
     * Verifica che chiamate ripetute a {@code hasNext()} non consumino
     * elementi.</p>
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
     * L'iteratore è esaurito e la mappa conserva tutti i mapping iniziali.</p>
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
     * Verifica che due iteratori ottenuti dalla stessa vista mantengano stati
     * di attraversamento indipendenti.</p>
     *
     * <p><b>Test Case Design:</b>
     * I due iteratori vengono creati prima di iniziare le visite e poi consumati
     * separatamente. Si confrontano soltanto i conteggi, perché ciascuno ha un
     * proprio cursore e un proprio snapshot ma non è richiesto che esponga un
     * particolare ordine.</p>
     *
     * <p><b>Test Description:</b>
     * Crea due iteratori di {@code keySet()}, attraversa completamente prima
     * l'uno e poi l'altro e registra quanti elementi produce ciascuno.</p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture contiene tre mapping e non viene modificata tra la creazione e
     * l'esaurimento dei due iteratori.</p>
     *
     * <p><b>Post-Condition:</b>
     * Entrambi gli iteratori sono esauriti e la mappa è rimasta invariata.</p>
     *
     * <p><b>Expected Results:</b>
     * Ogni iteratore restituisce tre chiavi; l'esaurimento del primo non avanza
     * né consuma il secondo.</p>
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
