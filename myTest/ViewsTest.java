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
 * La suite verifica la parte del contratto J2SE 1.4.2 che riguarda le tre
 * viste di una mappa. I test sono eseguiti con JUnit 4.13 e sono organizzati
 * in categorie logiche descritte di seguito. La prima categoria controlla il
 * <em>backing</em>: le modifiche effettuate sulla mappa devono comparire nelle
 * viste già ottenute e le rimozioni eseguite attraverso una vista devono
 * modificare la stessa mappa. La seconda categoria riguarda le rimozioni
 * singole e le operazioni bulk, comprese le chiamate che ricevono la stessa
 * vista come argomento e i casi in cui nessun elemento viene modificato. La
 * terza categoria verifica {@code null} e le operazioni di aggiunta non
 * supportate, controllando anche che gli errori non lascino modifiche
 * parziali. La quarta categoria esamina appartenenza, direzione di
 * {@code equals()}, uguaglianza e codice hash, distinguendo
 * {@code keySet()} ed {@code entrySet()}, che sono insiemi, da
 * {@code values()}, che è una collezione e conserva i duplicati. L'ultima
 * categoria verifica entrambe le forme di {@code toArray()}, inclusi riuso,
 * allocazione, compatibilità del tipo runtime e gestione della coda libera.
 * </p>
 *
 * <h2>Test Case Design</h2>
 * <p>
 * La fixture ricrea prima di ogni metodo i mapping {@code a=1} e
 * {@code b=2}. Due mapping distinti rendono osservabili sia una modifica
 * parziale sia lo svuotamento completo, senza introdurre dati non necessari;
 * nei test in cui conta la molteplicità viene aggiunto {@code c=1}, così il
 * valore {@code 1} compare due volte pur essendo associato a chiavi diverse.
 * Le viste vengono acquisite prima della mutazione quando occorre distinguere
 * una vista backed da una copia. Per le operazioni mutative il risultato
 * booleano è controllato insieme a dimensione, mapping e contenuto delle
 * viste: il solo valore restituito non sarebbe sufficiente a escludere una
 * modifica parziale o applicata alla struttura sbagliata. Gli scenari con la
 * stessa vista come sorgente e destinazione verificano i casi di aliasing
 * senza attribuire all'implementazione uno specifico ordine di iterazione.
 * Gli oggetti probe rendono intenzionalmente asimmetrico {@code equals()} e
 * permettono di stabilire quale operando riceve la chiamata, cosa che due
 * stringhe ordinarie non renderebbero osservabile. Le conversioni in array
 * controllano gli elementi per appartenenza, poiché l'adaptee
 * {@code Hashtable} non garantisce l'ordine, e usano sentinelle nelle celle
 * libere per distinguere il terminatore {@code null} dalla coda che deve
 * restare invariata. Le classi e gli helper privati concentrano questi
 * controlli ripetuti, ma ciascun test conserva pre-condizioni e risultati
 * attesi specifici per la vista esercitata.
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
     * Mappa ricreata prima di ogni test; rappresenta la struttura backing
     * comune alle viste esercitate dal singolo metodo.
     */
    private HMap map;

    /**
     * Prepara una fixture indipendente contenente {@code a=1} e {@code b=2}.
     * Le due coppie rendono verificabili una rimozione selettiva e una
     * successiva condizione residua. La ricostruzione prima di ogni metodo
     * impedisce che le mutazioni, frequenti in questa suite, rendano i test
     * dipendenti dall'ordine scelto da JUnit.
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica il backing delle tre viste quando un mapping viene inserito
     * direttamente nella mappa.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le tre viste vengono salvate prima della mutazione: questa scelta
     * rende osservabile la differenza tra una vista backed e una fotografia
     * del contenuto. Il mapping {@code c=3} usa sia una chiave sia un valore
     * nuovi, così ognuna delle tre ricerche identifica senza ambiguità
     * l'inserimento appena eseguito.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test acquisisce nell'ordine {@code keySet()}, {@code values()} ed
     * {@code entrySet()}; inserisce {@code c=3} mediante {@code put()}; infine
     * cerca {@code c}, {@code 3} e una {@link EntryComp} equivalente nelle
     * tre viste acquisite prima dell'inserimento.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene esattamente {@code a=1} e {@code b=2}; le tre
     * viste sono state ottenute e nessuna contiene elementi riferibili a
     * {@code c=3}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa contiene anche {@code c=3}; gli stessi oggetti vista
     * espongono rispettivamente la nuova chiave, il nuovo valore e la nuova
     * coppia chiave-valore.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code keys.contains("c")}, {@code values.contains("3")} ed
     * {@code entries.contains(new EntryComp("c", "3"))} restituiscono tutte
     * {@code true}.</p>
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
     * <p>Verifica che rimozione e svuotamento effettuati sulla mappa siano
     * riflessi nelle tre viste già esistenti.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le viste sono ottenute una sola volta e riutilizzate dopo due
     * mutazioni consecutive. La rimozione selettiva di {@code a=1} controlla
     * che spariscano insieme chiave, valore ed entry; il successivo
     * {@code clear()} verifica lo stato limite vuoto senza creare nuove viste.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test acquisisce le tre viste, rimuove {@code a} dalla mappa e
     * controlla l'assenza di {@code a}, {@code 1} e {@code a=1}. In seguito
     * invoca {@code map.clear()} e verifica {@code isEmpty()} sugli stessi
     * tre riferimenti.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}; chiavi, valori ed entry
     * sono viste della medesima mappa e sono state acquisite prima delle
     * mutazioni.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo la rimozione {@code a=1} non è osservabile in alcuna vista;
     * dopo lo svuotamento la mappa e tutte le viste preesistenti sono vuote.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Le tre chiamate {@code contains()} relative ad {@code a=1}
     * restituiscono {@code false}; le tre successive chiamate
     * {@code isEmpty()} restituiscono {@code true}.</p>
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
     * <p>La chiave presente {@code a} copre il ramo che modifica la vista,
     * mentre {@code missing} copre il ramo senza effetto. Il valore booleano
     * viene affiancato ai controlli sulla chiave della mappa e sulla dimensione,
     * per escludere sia un risultato errato sia una rimozione non propagata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test rimuove {@code a} da {@code keySet()}, verifica nella mappa
     * che la chiave non esista più e che resti un solo mapping, quindi tenta
     * di rimuovere la chiave assente {@code missing}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa ha dimensione due e contiene le sole chiavi {@code a} e
     * {@code b}; {@code missing} non appartiene alla vista.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il mapping {@code a=1} è stato eliminato attraverso la vista e la
     * mappa conserva il solo mapping associato a {@code b}; il tentativo su
     * {@code missing} non introduce ulteriori modifiche.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La prima {@code remove()} restituisce {@code true},
     * {@code map.containsKey("a")} restituisce {@code false}, la dimensione
     * vale uno e la seconda {@code remove()} restituisce {@code false}.</p>
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
     * <p>La mappa è mantenuta non vuota per verificare due proprietà nella
     * stessa prova: il tipo esatto dell'eccezione imposto dall'adaptee, che
     * non ammette chiavi nulle, e l'assenza di modifiche collaterali. Il
     * controllo di entrambi i mapping è più informativo della sola dimensione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test invoca {@code keySet().contains(null)} dentro un blocco
     * controllato; fallisce esplicitamente se la chiamata termina normalmente;
     * nel ramo {@link NullPointerException} verifica dimensione e valori
     * associati ad {@code a} e {@code b}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene due mapping con chiavi e valori non nulli e non
     * contiene, per contratto della {@code Hashtable}, una chiave {@code null}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo l'eccezione la dimensione resta due e i mapping {@code a=1} e
     * {@code b=2} conservano i rispettivi valori.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La chiamata lancia {@link NullPointerException}; nessuna esecuzione
     * raggiunge {@code fail()} e tutte le asserzioni nel blocco
     * {@code catch} hanno esito positivo.</p>
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
     * <p>Il caso è separato dalla ricerca di {@code null} perché
     * {@code remove()} è un'operazione mutativa. Oltre al tipo di eccezione
     * vengono controllati dimensione e valori di entrambi i mapping, così
     * un errore non può mascherare una rimozione parziale.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test tenta {@code keySet().remove(null)}, invoca {@code fail()} se
     * non viene segnalato l'errore e, dopo aver intercettato
     * {@link NullPointerException}, ricontrolla dimensione e coppie della fixture.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa contiene {@code a=1} e {@code b=2}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La vista e la mappa conservano implicitamente le due chiavi; in modo
     * esplicito la mappa mantiene dimensione due e i valori {@code 1} e
     * {@code 2} associati alle chiavi originarie.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Viene intercettata {@link NullPointerException}; le tre asserzioni
     * sullo stato iniziale risultano vere e nessun mapping è rimosso.</p>
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
     * <p>Il mapping aggiuntivo {@code c=1} fa comparire due volte il valore
     * {@code 1}; questa scelta distingue la semantica di
     * {@code Collection.remove()}, che elimina una sola occorrenza, da quella
     * di un insieme. Il successivo valore {@code missing} copre il caso senza
     * corrispondenze e permette di verificare anche il risultato booleano falso.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, rimuove il valore {@code 1} attraverso
     * {@code values()}, controlla che la dimensione passi da tre a due e che
     * un'altra occorrenza di {@code 1} rimanga, quindi tenta la rimozione di
     * {@code missing}.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Subito prima della rimozione la mappa contiene tre mapping; due
     * mapping distinti hanno valore {@code 1} e nessuno ha valore
     * {@code missing}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Rimangono due mapping e almeno uno conserva il valore {@code 1}; il
     * tentativo sul valore assente non cambia ulteriormente la collezione.</p>
     * <p><b>Expected Results:</b></p>
     * <p>La prima rimozione restituisce {@code true}, la dimensione vale due,
     * {@code map.containsValue("1")} resta {@code true} e la rimozione di
     * {@code missing} restituisce {@code false}.</p>
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
     * <p>Il valore memorizzato {@link StoredRejectingValue} restituisce sempre
     * {@code false}, mentre l'argomento {@link MatchingRemovalProbe} riconosce
     * proprio quel tipo. Con questa asimmetria la rimozione può riuscire solo
     * se viene valutato {@code removalArgument.equals(storedValue)}; oggetti
     * con uguaglianza simmetrica non permetterebbero di rilevare tale scelta.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea una mappa locale, associa a {@code key} un
     * {@code StoredRejectingValue}, invoca {@code values().remove()} con un
     * {@code MatchingRemovalProbe} e controlla risultato, stato vuoto e
     * assenza della chiave originaria.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene il solo mapping formato dalla chiave
     * {@code key} e da un valore che non considera uguale alcun oggetto.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Il mapping selezionato dal probe è rimosso; la mappa locale ha
     * dimensione zero e non contiene più {@code key}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code remove()} restituisce {@code true}, {@code isEmpty()}
     * restituisce {@code true} e {@code containsKey("key")} restituisce
     * {@code false}; tali risultati sono possibili nella direzione di
     * confronto progettata dal test.</p>
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
     * <p>Questo caso è il complemento del precedente: il valore memorizzato
     * {@link StoredMatchingValue} riconosce il probe, mentre
     * {@link RejectingRemovalProbe} rifiuta ogni oggetto. Se
     * l'implementazione invocasse {@code storedValue.equals(argument)}, il
     * mapping sarebbe rimosso e il test fallirebbe.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test salva il riferimento del valore, lo inserisce sotto
     * {@code key}, tenta di rimuoverlo tramite un
     * {@code RejectingRemovalProbe}, quindi controlla il risultato falso, la
     * dimensione uno e l'identità del valore ancora associato alla chiave.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene un solo mapping; il valore memorizzato
     * accetterebbe il probe, ma il probe non accetta il valore.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa locale conserva un mapping e {@code key} continua a essere
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
     * <p>Verifica la direzione di {@code equals()} nella vista {@code values()}.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Un oggetto ordinario viene memorizzato e un
     * {@link AsymmetricContainsProbe} lo riconosce esclusivamente per
     * identità. Poiché l'oggetto memorizzato non riconosce il probe, il
     * risultato vero dimostra la direzione usata dalla delega a
     * {@link HMap#containsValue(Object)} senza affidarsi a un {@code equals()}
     * simmetrico.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test inserisce sotto {@code key} un riferimento salvato in
     * {@code stored}; costruisce un probe collegato a quel riferimento;
     * esegue {@code values().contains(probe)} e ricontrolla dimensione e
     * identità del valore nella mappa.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La mappa locale contiene un solo valore non nullo; il probe è un
     * oggetto diverso ma il suo {@code equals()} accetta esattamente il
     * riferimento memorizzato.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La ricerca non modifica la mappa: la dimensione resta uno e
     * {@code key} conserva lo stesso oggetto {@code stored}.</p>
     * <p><b>Expected Results:</b></p>
     * <p>{@code values().contains(probe)} restituisce {@code true}; le
     * asserzioni successive confermano dimensione uno e identità del valore.</p>
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
     * <p>Quattro input isolano le condizioni del confronto: {@code a=1}
     * coincide completamente, {@code a=2} mantiene solo la chiave,
     * {@code missing=1} mantiene solo il valore e {@code "a=1"} non
     * implementa {@link HMap.Entry}. Questa partizione impedisce che
     * {@code contains()} accetti una corrispondenza parziale o un tipo errato.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test interroga la stessa {@code entrySet()} prima con una
     * {@link EntryComp} equivalente al mapping {@code a=1}, poi con una entry
     * dal valore diverso, con una entry dalla chiave assente e infine con una
     * stringa che ne imita soltanto la rappresentazione testuale.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene {@code a=1} e {@code b=2}; nessuna chiave
     * {@code missing} è presente e gli elementi della vista implementano
     * {@link HMap.Entry}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Le quattro interrogazioni non modificano i due mapping della fixture.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il primo {@code contains()} restituisce {@code true}; i tre controlli
     * con valore errato, chiave assente e tipo non-entry restituiscono
     * {@code false}.</p>
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
     * <p>Due entry hanno la stessa chiave, ma solo una possiede anche il valore
     * corrente; in questo modo si evita una rimozione basata sulla sola chiave.
     * La presenza di {@code a} viene controllata tra i due tentativi, così il
     * secondo risultato non può nascondere un'errata rimozione anticipata.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test tenta prima di rimuovere {@code a=2}, verifica che
     * {@code a} sia ancora nella mappa, rimuove poi l'entry corretta
     * {@code a=1} e controlla infine l'assenza della chiave.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>Il mapping corrente per {@code a} è {@code a=1}.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>Dopo il secondo tentativo la chiave {@code a} non è più presente.</p>
     * <p><b>Expected Results:</b></p>
     * <p>Il primo tentativo restituisce {@code false}; il secondo restituisce {@code true}.</p>
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
     * <p>Verifica che {@code clear()} sulla vista dei valori svuoti la mappa backing.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Le tre viste sono conservate prima dello svuotamento e controllate
     * dopo l'operazione. Si sceglie {@code values()} come punto di mutazione
     * per dimostrare che anche la vista priva di semantica da insieme può
     * svuotare l'unica struttura backing; dimensione e quattro controlli di
     * vuoto rendono congiunta la verifica.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test acquisisce chiavi, valori ed entry, invoca
     * {@code values.clear()}, quindi controlla nell'ordine lo stato vuoto
     * della mappa e delle tre viste e verifica che la dimensione sia zero.</p>
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
     * <p>Una mappa locale costruita da {@link #populatedMap()} isola questo
     * scenario dalla fixture condivisa. Tutte le viste sono acquisite prima
     * di {@code clear()}, perché crearle dopo lo svuotamento non proverebbe il
     * loro aggiornamento; il caso completa separatamente il percorso mutativo
     * offerto da {@code keySet()}.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea la mappa locale, memorizza i tre riferimenti alle viste,
     * esegue {@code keys.clear()} e controlla che mappa, chiavi, valori ed
     * entry siano vuoti e che la dimensione della mappa valga zero.</p>
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
     * <p>Il caso completa il controllo dei tre punti di accesso a
     * {@code clear()}. Una mappa locale con due mapping e viste create prima
     * della chiamata permette di verificare che {@code entrySet()} agisca
     * sulla struttura condivisa e non soltanto sul proprio oggetto vista.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test costruisce la mappa locale, acquisisce le tre viste, invoca
     * {@code entries.clear()} e controlla in sequenza lo stato vuoto dei
     * quattro oggetti osservabili e la dimensione zero.</p>
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
     * prima identifica la chiave {@code a} da rimuovere, poi, dopo essere
     * stata ricostruita, la chiave {@code b} da conservare. L'inserimento
     * intermedio di {@code c=3} assicura che {@code retainAll()} debba
     * eliminare un elemento e non possa restituire {@code true} senza effetto.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea una selezione contenente {@code a}, esegue
     * {@code removeAll()} e verifica l'assenza della chiave; aggiunge quindi
     * {@code c=3}, sostituisce nella selezione {@code a} con {@code b},
     * esegue {@code retainAll()} e controlla dimensione uno e presenza di
     * {@code b}.</p>
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
     * distinguere l'operazione bulk dalla rimozione singola di {@code remove()}.
     * La sorgente contiene una sola occorrenza di {@code 1}: il risultato deve
     * dipendere dall'appartenenza alla sorgente e rimuovere entrambi i mapping
     * corrispondenti nella destinazione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, costruisce una seconda mappa con il
     * valore {@code 1}, passa la relativa vista a {@code removeAll()} e
     * verifica risultato vero, dimensione uno, permanenza di {@code b} e
     * assenza completa del valore {@code 1}.</p>
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
     * non il numero di occorrenze nella collezione argomento. I controlli
     * sulle tre chiavi distinguono i due mapping da conservare da
     * {@code b=2}, che deve essere rimosso.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, prepara una sorgente che espone il solo
     * valore {@code 1}, invoca {@code values().retainAll()}, quindi controlla
     * risultato vero, dimensione due, presenza di {@code a} e {@code c} e
     * assenza di {@code b}.</p>
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
     * valore differente, per controllare che siano confrontati entrambi i
     * componenti. Il terzo mapping {@code c=3}, non presente nella selezione,
     * offre un secondo controllo positivo di conservazione e rende più
     * evidente un'eventuale rimozione eccessiva.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=3}, costruisce una selezione con
     * {@code a=1} e {@code b=different}, invoca {@code removeAll()} sulla
     * vista delle entry e verifica l'assenza di {@code a}, i valori invariati
     * di {@code b} e {@code c} e la dimensione finale due.</p>
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
     * {@code b} un valore diverso. Le tre chiavi coprono quindi due
     * corrispondenze complete e una corrispondenza della sola chiave,
     * verificando che {@code retainAll()} usi l'uguaglianza integrale delle entry.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=3}, prepara la selezione
     * {@code a=1}, {@code b=different}, {@code c=3}, invoca
     * {@code retainAll()} e controlla i valori conservati di {@code a} e
     * {@code c}, l'assenza di {@code b} e la dimensione due.</p>
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
     * {@code retainAll()} la selezione è una copia completa della mappa.
     * I due scenari coprono entrambi i rami senza modifica e controllano che
     * il valore {@code false} sia coerente con dimensione e uguaglianza finali.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test tenta prima {@code removeAll()} con {@code a=different} e
     * {@code missing=value} e verifica dimensione due; costruisce poi una
     * copia completa della fixture, esegue {@code retainAll()} e confronta la
     * mappa finale con tale copia.</p>
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
     * <p>Il test salva {@code map.keySet()} in {@code view}, passa lo stesso
     * riferimento a {@code view.removeAll(view)}, controlla risultato vero,
     * stato vuoto di mappa e vista e dimensione zero, quindi ripete la
     * chiamata sullo stato vuoto e ne verifica il risultato falso.</p>
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
     * <p>Il test aggiunge {@code c=1}, salva la vista dei tre valori e la
     * passa come argomento della propria {@code removeAll()}; verifica poi
     * risultato vero, stato vuoto di mappa e vista e dimensione zero.</p>
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
     * <p>Destinazione e collezione argomento sono lo stesso oggetto. Questa
     * condizione di aliasing obbliga l'operazione a eliminare entrambe le
     * entry senza saltarne una e verifica l'effetto finale senza imporre una
     * particolare strategia interna o un ordine di attraversamento.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test salva {@code map.entrySet()} in {@code view}, esegue
     * {@code view.removeAll(view)} e controlla risultato vero, stato vuoto
     * della mappa e della vista e dimensione finale zero.</p>
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
     * <p>Il test passa {@code view} alla propria {@code retainAll()}, verifica
     * il risultato falso e controlla poi dimensione, valori associati ad
     * {@code a} e {@code b} e appartenenza di entrambe le chiavi alla vista.</p>
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
     * <p>Il test aggiunge {@code c=1}, passa la vista dei valori a se stessa
     * in {@code retainAll()}, controlla risultato falso, dimensione e tre
     * mapping, quindi converte la vista in array e verifica che {@code 1}
     * compaia ancora due volte.</p>
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
     * <p>Il test esegue {@code view.retainAll(view)}, verifica il risultato
     * falso, controlla dimensione due e appartenenza delle entry
     * {@code a=1} e {@code b=2}, quindi ricontrolla i due valori direttamente
     * nella mappa.</p>
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
        assertTrue(view.contains(new EntryComp("a", "1")));
        assertTrue(view.contains(new EntryComp("b", "2")));
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
     * <p>Il test costruisce una seconda mappa con chiavi diverse ma valori
     * {@code 1} e {@code 2}; confronta le due viste dei valori in entrambe le
     * direzioni e, come controllo di base, confronta la vista della fixture
     * con se stessa.</p>
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
     * <p>Il test costruisce una seconda {@code keySet()} contenente soltanto
     * {@code a} e verifica {@code containsAll()}; aggiunge poi
     * {@code missing} alla stessa sorgente e ripete la ricerca, isolando
     * l'unico elemento che deve rendere falso il risultato.</p>
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
     * <p>Un helper comune applica alla sola vista delle chiavi le quattro
     * chiamate con argomento nullo. Dopo ogni eccezione ricontrolla l'intera
     * fixture, invece di controllarla soltanto alla fine, così individua
     * l'eventuale operazione responsabile di una modifica parziale.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa {@code keySet()} all'helper, che invoca nell'ordine
     * {@code containsAll(null)}, {@code addAll(null)},
     * {@code removeAll(null)} e {@code retainAll(null)}; per ogni chiamata
     * richiede {@link NullPointerException} e verifica subito la fixture.</p>
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
     * <p>Si riutilizza la stessa procedura della vista delle chiavi, ma le si
     * fornisce esplicitamente {@code values()}: la scelta evita duplicazioni
     * nel codice di test senza confondere il risultato relativo alla
     * collection dei valori con quello delle altre viste.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa {@code values()} all'helper; l'helper esegue in
     * sequenza le quattro operazioni bulk con {@code null}, intercetta per
     * ciascuna {@link NullPointerException} e ricontrolla i mapping iniziali.</p>
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
     * <p>Il caso completa la stessa partizione sulle tre viste fornendo
     * {@code entrySet()} all'helper comune. La verifica dello stato dopo ogni
     * eccezione è particolarmente rilevante per {@code removeAll()} e
     * {@code retainAll()}, che altrimenti potrebbero lasciare rimozioni parziali.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test delega la vista delle entry all'helper, che tenta
     * {@code containsAll}, {@code addAll}, {@code removeAll} e
     * {@code retainAll} con argomento {@code null}, verificando eccezione e
     * fixture dopo ogni tentativo.</p>
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
     * fixture per assicurare che l'eccezione non causi effetti collaterali.
     * Il caso resta distinto da {@code values().remove(null)}, perché le due
     * operazioni hanno risultati contrattuali diversi.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test invoca {@code values().contains(null)}, fallisce se la
     * chiamata termina normalmente e, dopo aver intercettato
     * {@link NullPointerException}, usa {@link #assertFixtureUnchanged()} per
     * verificare dimensione, mapping, chiavi e valori.</p>
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
     * <p>Il test invoca {@code values().remove(null)}, richiede direttamente
     * il risultato {@code false} e poi controlla con l'helper condiviso
     * dimensione, valori associati, chiavi e valori della fixture.</p>
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
     * {@code contains()} e {@code remove()} per ogni tipo non valido.
     * {@code null} copre l'assenza di oggetto, mentre la stringa
     * {@code "not an entry"} è un oggetto valido ma del tipo sbagliato; il
     * controllo finale congiunto esclude rimozioni accidentali.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test esegue prima {@code contains(null)} e {@code remove(null)},
     * poi ripete entrambe le operazioni con la stringa non-entry; richiede
     * quattro risultati falsi e infine ricontrolla tutta la fixture.</p>
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
     * {@code retainAll()} riceve tutte le chiavi presenti. Sono così
     * esercitati i due modi in cui un'operazione bulk può non cambiare la
     * vista; dopo ciascun metodo, e non soltanto alla fine, viene controllata
     * la fixture completa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test costruisce prima una sorgente con {@code missing}, esegue
     * {@code keySet().removeAll()} e verifica risultato falso e fixture;
     * costruisce poi una sorgente con {@code a} e {@code b}, esegue
     * {@code retainAll()} e ripete gli stessi controlli.</p>
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
     * collezione che comprende tutti i valori presenti. Le sorgenti sono
     * ottenute da altre mappe per fornire vere {@link HCollection}, mentre il
     * controllo della fixture dopo ogni chiamata collega il valore
     * {@code false} all'assenza effettiva di modifiche.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test tenta {@code removeAll()} con una vista contenente
     * {@code missing} e controlla risultato e fixture; prepara poi una vista
     * contenente {@code 1} e {@code 2}, tenta {@code retainAll()} e ripete il
     * controllo completo dello stato.</p>
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
     * non dai valori associati nella mappa. I controlli con {@code null}, una
     * stringa, la stessa istanza, un altro tipo di vista e una dimensione
     * maggiore coprono separatamente i principali confini del contratto;
     * simmetria e hash code sono verificati insieme all'uguaglianza positiva.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea un secondo {@code keySet()} con chiavi {@code a} e
     * {@code b}, verifica i confronti con {@code null}, tipo errato e se
     * stesso, quindi uguaglianza nei due versi e hash code. Confronta poi il
     * set con un {@code entrySet()} e aggiunge {@code c} al set equivalente
     * per verificare il caso di dimensione diversa.</p>
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
     * l'uguaglianza di un insieme non deve dipendere dall'ordine della
     * {@code Hashtable}. I casi {@code null}, tipo diverso, identità,
     * simmetria e dimensione diversa delimitano il contratto, mentre il
     * confronto degli hash verifica la coerenza richiesta agli insiemi uguali.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test costruisce una seconda mappa con gli stessi mapping inseriti
     * in ordine inverso; confronta {@code entrySet()} con {@code null}, una
     * stringa e se stesso, verifica uguaglianza nei due versi e hash uguali,
     * quindi aggiunge {@code c=3} e richiede che l'uguaglianza diventi falsa.</p>
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
     * l'errore dipende dall'operazione, non dal tipo dell'elemento. L'helper
     * verifica per ogni vista sia l'eccezione locale prevista sia l'assenza
     * dell'elemento proposto; la dimensione finale collega i tre controlli
     * allo stato complessivo della mappa.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test tenta tramite l'helper di aggiungere {@code c} alle chiavi,
     * {@code 3} ai valori e {@code c=3} alle entry. Dopo le tre eccezioni e i
     * controlli di assenza, verifica che la mappa contenga ancora due mapping.</p>
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
        expectUnsupportedAdd(map.entrySet(), new EntryComp("c", "3"));
        assertEquals(2, map.size());
    }

    /**
     * <p><b>Summary:</b></p>
     * <p>Verifica {@code addAll()} con una collezione vuota.</p>
     * <p><b>Test Case Design:</b></p>
     * <p>Una sorgente vuota non provoca alcuna chiamata ad {@code add()} e
     * permette quindi di distinguere l'assenza di modifiche da un'aggiunta
     * non supportata. Lo stesso oggetto sorgente viene usato sulle tre viste,
     * così l'unica variabile del confronto è il tipo di vista destinataria.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test ottiene il {@code keySet()} di una mappa vuota, lo passa a
     * {@code addAll()} di chiavi, valori ed entry e richiede tre risultati
     * falsi; infine verifica che la dimensione della fixture resti due.</p>
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
     * l'eccezione dipende dal carattere non supportato dell'aggiunta e non da
     * una sorgente vuota o da un tipo palesemente scorretto. L'unica mappa
     * sorgente {@code c=3} produce coerentemente chiave, valore ed entry per i
     * tre tentativi.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea la sorgente {@code c=3}; passa rispettivamente il suo
     * {@code keySet()}, {@code values()} ed {@code entrySet()} agli helper che
     * richiedono l'eccezione; dopo i tre tentativi verifica dimensione due.</p>
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
     * perché la {@code Hashtable} non garantisce un ordine stabile. La
     * sostituzione di una cella con {@code "changed"}, seguita dal controllo
     * delle due chiavi nella mappa, verifica l'indipendenza del contenitore
     * array senza assumere quale chiave occupi l'indice zero.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test converte {@code keySet()} in un nuovo array, verifica
     * lunghezza due e appartenenza di {@code a} e {@code b}, sostituisce la
     * prima cella e controlla infine che entrambe le chiavi restino nella mappa.</p>
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
     * evita di trattare per errore la vista dei valori come un insieme. La
     * verifica separata dell'unica occorrenza di {@code 2} completa il
     * contenuto atteso; la modifica di una cella prova soltanto l'indipendenza
     * dell'array come contenitore, non degli oggetti eventualmente contenuti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test aggiunge {@code c=1}, converte {@code values()} in array,
     * verifica lunghezza tre, due occorrenze di {@code 1} e una di {@code 2},
     * modifica la prima cella e ricontrolla dimensione e presenza dei due
     * valori nella mappa.</p>
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
     * corrente, senza imporre l'ordine di attraversamento. La lunghezza viene
     * confrontata con la dimensione della mappa per richiedere un elemento
     * per mapping; il metodo non modifica entry né mappa e quindi non pretende
     * di verificare il backing dei singoli oggetti restituiti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test converte {@code entrySet()} in array, confronta la lunghezza
     * con {@code map.size()} e, per ogni cella, verifica il tipo
     * {@link HMap.Entry}, la presenza della chiave nella mappa e l'uguaglianza
     * tra valore della mappa e valore esposto dalla entry.</p>
     * <p><b>Pre-Condition:</b></p>
     * <p>La fixture contiene due mapping.</p>
     * <p><b>Post-Condition:</b></p>
     * <p>La mappa conserva i due mapping iniziali; l'array contiene soltanto
     * oggetti che descrivono coppie chiave-valore correnti.</p>
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
     * impostare a {@code null} sia la parte di coda che non deve essere
     * modificata. Con due valori e quattro celle, l'indice 2 è il primo
     * spazio libero e l'indice 3 rappresenta la coda; il controllo dei valori
     * resta indipendente dall'ordine di iterazione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test inizializza quattro celle con lo stesso riferimento
     * sentinella, passa l'array a {@code values().toArray()}, verifica che il
     * riferimento restituito sia lo stesso, controlla {@code null} all'indice
     * 2, sentinella all'indice 3 e presenza di {@code 1} e {@code 2}.</p>
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
     * della dimensione richiesta. La non identità dei riferimenti prova la
     * nuova allocazione, mentre lunghezza e appartenenza delle chiavi
     * verificano che il nuovo contenitore abbia capacità e contenuto corretti.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa un {@code Object[0]} a {@code keySet().toArray()},
     * confronta i riferimenti per richiedere un array diverso, verifica
     * lunghezza due e cerca {@code a} e {@code b} senza assumerne la posizione.</p>
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
     * né una nuova allocazione né una cella terminatrice. Il controllo di
     * identità distingue il riuso da una copia della stessa lunghezza e le
     * ricerche per appartenenza evitano dipendenze dall'ordine.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea un {@code Object[2]}, lo passa a
     * {@code keySet().toArray()}, verifica che il riferimento restituito sia
     * identico a quello fornito e cerca entrambe le chiavi nelle due celle.</p>
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
     * agli elementi dalla coda ulteriore, che il contratto lascia invariata.
     * Un array di quattro celle per due chiavi lascia esattamente un indice
     * terminatore e almeno un indice di coda osservabile.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test riempie quattro celle con la stessa sentinella, converte le
     * due chiavi nell'array, verifica il riuso del riferimento e controlla che
     * l'indice 2 sia {@code null} mentre l'indice 3 conservi la sentinella.</p>
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
     * in più, coprendo insieme compatibilità runtime, riuso e terminatore
     * nullo. La ricerca delle due chiavi non assume in quale ordine vengano
     * scritte dalla vista.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test passa un {@code String[3]} a {@code keySet().toArray()},
     * verifica l'identità del riferimento restituito, cerca {@code a} e
     * {@code b} e controlla che la terza cella sia il terminatore {@code null}.</p>
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
     * le chiavi {@link String}. La dimensione esatta esclude il ramo di
     * riallocazione e isola il controllo del tipo runtime durante la
     * memorizzazione; l'annotazione JUnit richiede il tipo preciso di eccezione.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test crea implicitamente un {@code Integer[2]} e lo passa a
     * {@code keySet().toArray()}; JUnit considera riuscito il metodo soltanto
     * se la copia delle chiavi termina con {@link ArrayStoreException}.</p>
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
     * <p>Il caso passa il riferimento {@code null} direttamente, isolando la
     * validazione dell'argomento dai rami relativi a tipo, capacità e riuso.
     * La vista non vuota impedisce che un'implementazione accetti il dato
     * soltanto perché non deve copiare elementi.</p>
     * <p><b>Test Description:</b></p>
     * <p>Il test invoca {@code keySet().toArray(null)} sulla fixture popolata;
     * l'annotazione JUnit considera riuscito il metodo esclusivamente quando
     * viene propagata {@link NullPointerException}.</p>
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
     * Entry di confronto minima e indipendente che implementa
     * {@link HMap.Entry}. Il suffisso {@code Comp}, abbreviazione di
     * {@code comparison}, chiarisce che la classe rappresenta coppie chiave-valore
     * attese da confrontare con le entry prodotte dall'adapter, senza dipendere
     * dalla loro classe privata. In questo modo il valore atteso non viene
     * costruito usando la stessa implementazione sottoposta al test.
     */
    private static final class EntryComp implements HMap.Entry {
        /** Chiave rappresentata dall'entry di confronto. */
        private final Object key;

        /** Valore corrente dell'entry di confronto. */
        private Object value;

        /**
         * Crea un'entry di confronto indipendente con la coppia indicata.
         *
         * @param entryKey chiave da rappresentare
         * @param entryValue valore iniziale associato alla chiave
         */
        private EntryComp(Object entryKey, Object entryValue) {
            key = entryKey;
            value = entryValue;
        }

        /**
         * Restituisce la chiave memorizzata nell'entry di confronto.
         *
         * @return chiave della entry
         */
        public Object getKey() {
            return key;
        }

        /**
         * Restituisce il valore corrente dell'entry di confronto.
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
