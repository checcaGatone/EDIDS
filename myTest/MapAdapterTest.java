package myTest;

import myAdapter.HMap;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case dedicato alle operazioni fondamentali di {@link MapAdapter} viste
 * attraverso l'interfaccia {@link HMap}.
 *
 * <p><b>Summary:</b>
 * La classe verifica il contratto di {@code Map} previsto da J2SE 1.4.2 per le
 * operazioni che non riguardano direttamente le viste: costruzione, inserimento,
 * ricerca, rimozione, copia, svuotamento, uguaglianza, codice hash e
 * rappresentazione testuale. Vengono inoltre controllati i vincoli sui valori
 * {@code null} introdotti dall'uso di {@code Hashtable} come oggetto adattato.
 * </p>
 *
 * <p><b>Test Case Design:</b>
 * Prima di ogni test viene creata una nuova mappa, in modo che le verifiche siano
 * indipendenti e non risentano dello stato lasciato dai test precedenti. Le
 * stringhe brevi usate come chiavi e valori rendono immediato distinguere i
 * mapping e permettono di calcolare in modo esplicito il codice hash atteso.
 * Quando un'operazione modifica la mappa vengono controllati sia il valore
 * restituito sia il contenuto finale; nei casi di errore si verifica anche che
 * i mapping già presenti non vengano alterati. Oggetti ausiliari con
 * {@code equals} asimmetrico consentono di controllare la direzione del
 * confronto richiesta da {@code containsValue}, mentre mappe popolate in ordine
 * diverso evitano di legare i test all'ordine non garantito da
 * {@code Hashtable}. Il costruttore di copia viene infine verificato sia per il
 * contenuto prodotto sia per l'indipendenza tra sorgente e copia.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see HMap
 * @see MapAdapter
 */
public class MapAdapterTest {
    /**
     * Mappa nuova usata come fixture da ciascun metodo di test. Il riferimento
     * è dichiarato come {@link HMap} per esercitare il contratto pubblico senza
     * dipendere dai dettagli interni di {@link MapAdapter}.
     */
    private HMap map;

    /**
     * Crea una fixture vuota prima di ogni test.
     *
     * <p>La reinizializzazione garantisce che ogni caso parta da uno stato noto
     * e che un eventuale fallimento non condizioni i test eseguiti in seguito.</p>
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
    }

    /**
     * <p><b>Summary:</b>
     * Verifica lo stato iniziale di un {@link MapAdapter} appena costruito.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si interrogano dimensione e stato di vuoto, perché i due metodi esprimono
     * lo stesso stato attraverso risultati di tipo diverso e devono essere
     * coerenti tra loro.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Invoca {@code size()} e {@code isEmpty()} senza effettuare inserimenti.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è stata appena creata da {@link #setUp()}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa non viene modificata e rimane vuota.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code size()} restituisce {@code 0} e {@code isEmpty()} restituisce
     * {@code true}.
     * </p>
     */
    @Test
    public void newMapIsEmpty() {
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica l'inserimento di un mapping nuovo e la successiva lettura del
     * valore associato.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si controllano insieme il valore restituito da {@code put}, il risultato
     * di {@code get}, la dimensione e lo stato di vuoto. In questo modo il test
     * non si limita a osservare il ritorno del metodo, ma accerta che il mapping
     * sia stato realmente memorizzato.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce il mapping {@code "a"="1"} e interroga la mappa con la stessa
     * chiave.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa è vuota e chiave e valore sono entrambi diversi da {@code null}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa contiene un solo mapping, associato alla chiave {@code "a"}.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code put} restituisce {@code null}, {@code get("a")} restituisce
     * {@code "1"}, la dimensione vale {@code 1} e la mappa non è vuota.
     * </p>
     */
    @Test
    public void putAddsMappingAndGetReturnsValue() {
        assertNull(map.put("a", "1"));
        assertEquals("1", map.get("a"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica la sostituzione del valore associato a una chiave già presente.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Due chiamate a {@code put} usano la stessa chiave e valori diversi. La
     * scelta permette di distinguere una sostituzione corretta dall'aggiunta
     * accidentale di un secondo mapping.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"}, sostituisce il valore con {@code "2"} e legge
     * nuovamente il mapping.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene il solo mapping {@code "a"="1"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La chiave {@code "a"} è associata a {@code "2"} e la dimensione resta
     * pari a {@code 1}.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La seconda chiamata a {@code put} restituisce il valore precedente
     * {@code "1"} e {@code get("a")} restituisce {@code "2"}.
     * </p>
     */
    @Test
    public void putReplacementReturnsPreviousValue() {
        map.put("a", "1");
        assertEquals("1", map.put("a", "2"));
        assertEquals("2", map.get("a"));
        assertEquals(1, map.size());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code put} rifiuti una chiave {@code null}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Il valore è valido e soltanto la chiave è nulla, così la causa
     * dell'errore rimane isolata. Dopo aver intercettato l'eccezione viene
     * controllato anche lo stato della mappa per escludere modifiche parziali.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Invoca {@code put(null, "1")} e considera fallito il test se il metodo
     * termina normalmente.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è vuota e il valore {@code "1"} è valido.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Nessun mapping viene inserito.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Viene sollevata esattamente una {@link NullPointerException} e la mappa
     * resta vuota, in accordo con il vincolo di {@code Hashtable} sulle chiavi.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica che {@code put} rifiuti un valore {@code null}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si usa una chiave valida e un valore nullo per verificare separatamente
     * il secondo vincolo imposto da {@code Hashtable}. Il controllo finale
     * sulla fixture accerta che l'operazione non lasci effetti collaterali.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Invoca {@code put("a", null)} e forza un fallimento se non viene lanciata
     * alcuna eccezione.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è vuota e la chiave {@code "a"} è valida.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa non contiene alcun mapping.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Viene sollevata esattamente una {@link NullPointerException} e la mappa
     * rimane vuota.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica il risultato di {@code get} per una chiave assente.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La ricerca viene eseguita su una mappa vuota con una chiave non nulla. In
     * questo modo il test controlla il normale caso di assenza senza confonderlo
     * con il comportamento di errore previsto per una chiave {@code null}.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Richiede il valore associato alla chiave {@code "missing"} e verifica
     * successivamente che la mappa sia ancora vuota.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture non contiene mapping.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La ricerca non modifica lo stato della mappa.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code get("missing")} restituisce {@code null} e {@code isEmpty()}
     * restituisce {@code true}.
     * </p>
     */
    @Test
    public void getReturnsNullForAbsentKey() {
        assertNull(map.get("missing"));
        assertTrue(map.isEmpty());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code containsKey} cerchi esclusivamente tra le chiavi.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La chiave e il valore dello stesso mapping sono stringhe diverse; entrambi
     * vengono passati a {@code containsKey}. Questa scelta evidenzia un'eventuale
     * confusione tra le due componenti del mapping.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "key"="value"}, cerca prima {@code "key"} e poi
     * {@code "value"} come chiavi.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene un solo mapping con chiave e valore distinti.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping inserito rimane invariato.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La ricerca di {@code "key"} restituisce {@code true}, mentre la ricerca
     * di {@code "value"} restituisce {@code false}.
     * </p>
     */
    @Test
    public void containsKeyDistinguishesKeyFromValue() {
        map.put("key", "value");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("value"));
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code containsValue} cerchi esclusivamente tra i valori.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si riutilizzano come argomenti sia il valore sia la chiave del mapping. Il
     * confronto rende evidente che la ricerca opera sulla parte corretta delle
     * associazioni.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "key"="value"} e cerca entrambe le stringhe come valori.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene un mapping con chiave e valore distinti.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il contenuto della mappa non viene modificato.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code containsValue("value")} restituisce {@code true} e
     * {@code containsValue("key")} restituisce {@code false}.
     * </p>
     */
    @Test
    public void containsValueDistinguishesValueFromKey() {
        map.put("key", "value");
        assertTrue(map.containsValue("value"));
        assertFalse(map.containsValue("key"));
    }

    /**
     * <p><b>Summary:</b>
     * Verifica la direzione con cui {@code containsValue} invoca
     * {@code equals}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Un oggetto ordinario viene memorizzato come valore, mentre la ricerca usa
     * un {@link AsymmetricEqualsProbe} che riconosce per identità soltanto tale
     * oggetto. Il confronto è intenzionalmente asimmetrico: può riuscire solo
     * chiamando {@code equals} sul valore cercato, come stabilito dal contratto
     * J2SE 1.4.2 di {@code Map.containsValue}.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce l'oggetto {@code stored} e lo cerca tramite {@code searched};
     * controlla poi dimensione e identità del valore memorizzato.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene un solo mapping il cui valore è l'oggetto riconosciuto
     * dal probe.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La ricerca non sostituisce né rimuove il valore originale.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code containsValue(searched)} restituisce {@code true}, la dimensione
     * resta {@code 1} e {@code get("key")} restituisce lo stesso riferimento
     * memorizzato.
     * </p>
     */
    @Test
    public void containsValueInvokesEqualsOnSearchArgument() {
        Object stored = new Object();
        Object searched = new AsymmetricEqualsProbe(stored);
        map.put("key", stored);

        assertTrue(map.containsValue(searched));
        assertEquals(1, map.size());
        assertSame(stored, map.get("key"));
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code containsValue} non accetti una corrispondenza valida
     * soltanto nella direzione opposta a quella prevista dal contratto.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Questa volta il probe asimmetrico è il valore memorizzato e riconosce
     * l'oggetto cercato. L'oggetto cercato, invece, non riconosce il probe. Se
     * l'implementazione invocasse erroneamente {@code equals} sul valore
     * contenuto nella mappa, il test produrrebbe un falso positivo.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Memorizza {@code stored}, cerca {@code searched} e controlla che il
     * mapping originario sia ancora presente per identità.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene un probe con confronto asimmetrico come unico valore.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La dimensione e il riferimento associato a {@code "key"} rimangono
     * invariati.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code containsValue(searched)} restituisce {@code false}; la
     * corrispondenza ottenibile solo con il confronto inverso viene ignorata.
     * </p>
     */
    @Test
    public void containsValueRejectsReverseOnlyMatch() {
        Object searched = new Object();
        Object stored = new AsymmetricEqualsProbe(searched);
        map.put("key", stored);

        assertFalse(map.containsValue(searched));
        assertEquals(1, map.size());
        assertSame(stored, map.get("key"));
    }

    /**
     * <p><b>Summary:</b>
     * Verifica il rifiuto di {@code null} come argomento di
     * {@code containsValue}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La mappa viene prima popolata con un mapping valido. Oltre al tipo esatto
     * dell'eccezione, vengono quindi controllati dimensione e valore per
     * assicurarsi che la ricerca non produca modifiche collaterali.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "key"="value"}, invoca {@code containsValue(null)} e
     * intercetta l'eccezione attesa.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene un mapping valido e non nullo.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping iniziale rimane presente e invariato.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Viene sollevata esattamente una {@link NullPointerException}; la dimensione
     * resta {@code 1} e la chiave continua a essere associata a {@code "value"}.
     * </p>
     */
    @Test
    public void containsValueRejectsNull() {
        map.put("key", "value");
        try {
            map.containsValue(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(1, map.size());
            assertEquals("value", map.get("key"));
        }
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code get} rifiuti una chiave {@code null} senza modificare
     * la mappa.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si parte da una mappa non vuota per poter osservare esplicitamente
     * l'assenza di effetti collaterali dopo l'errore, oltre a controllare il tipo
     * dell'eccezione.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"}, invoca {@code get(null)} e verifica il
     * contenuto dopo aver intercettato l'eccezione.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene un unico mapping valido.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping {@code "a"="1"} è ancora presente.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code get(null)} solleva esattamente una {@link NullPointerException}; la
     * dimensione vale ancora {@code 1} e {@code get("a")} restituisce
     * {@code "1"}.
     * </p>
     */
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

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code containsKey} rifiuti una chiave {@code null} senza
     * alterare i mapping esistenti.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La fixture contiene già una coppia valida, così il test può distinguere il
     * semplice lancio dell'eccezione da un comportamento che danneggia lo stato
     * della mappa.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Dopo aver inserito {@code "a"="1"}, invoca {@code containsKey(null)} e
     * controlla nuovamente dimensione e valore associato.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene esattamente il mapping {@code "a"="1"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Lo stato della fixture coincide con quello precedente alla ricerca.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Viene sollevata esattamente una {@link NullPointerException}; dimensione e
     * valore del mapping rimangono invariati.
     * </p>
     */
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

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code remove} rifiuti una chiave {@code null} e non rimuova
     * altri mapping.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Una mappa già popolata permette di controllare che l'operazione non
     * interpreti {@code null} come una richiesta generica di rimozione e che
     * l'eccezione lasci intatto il contenuto.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"}, chiama {@code remove(null)} e verifica il
     * mapping dopo la gestione dell'eccezione.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene un unico mapping valido.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Nessun mapping è stato rimosso.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code remove(null)} solleva esattamente una
     * {@link NullPointerException}; la dimensione resta {@code 1} e il valore
     * associato a {@code "a"} resta {@code "1"}.
     * </p>
     */
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
     * <p><b>Summary:</b>
     * Verifica la rimozione di un mapping presente.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Il test controlla sia il valore restituito da {@code remove} sia la
     * scomparsa della chiave e lo stato di vuoto. Queste verifiche distinguono
     * una rimozione effettiva dalla sola restituzione del valore precedente.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"} e rimuove il mapping mediante la chiave
     * {@code "a"}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene soltanto {@code "a"="1"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La chiave non è più presente e la mappa è vuota.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code remove("a")} restituisce {@code "1"},
     * {@code containsKey("a")} restituisce {@code false} e
     * {@code isEmpty()} restituisce {@code true}.
     * </p>
     */
    @Test
    public void removePresentMappingReturnsValueAndDeletesIt() {
        map.put("a", "1");
        assertEquals("1", map.remove("a"));
        assertFalse(map.containsKey("a"));
        assertTrue(map.isEmpty());
    }

    /**
     * <p><b>Summary:</b>
     * Verifica la rimozione richiesta con una chiave assente.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La chiave cercata è diversa da quella memorizzata. Vengono controllati il
     * valore di ritorno, la dimensione e il mapping esistente per dimostrare che
     * l'operazione non produce effetti collaterali.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"} e invoca {@code remove("b")}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene soltanto la chiave {@code "a"}; {@code "b"} è assente.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping {@code "a"="1"} rimane nella mappa.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code remove("b")} restituisce {@code null}, la dimensione resta
     * {@code 1} e {@code get("a")} restituisce ancora {@code "1"}.
     * </p>
     */
    @Test
    public void removeAbsentMappingReturnsNullWithoutChanges() {
        map.put("a", "1");
        assertNull(map.remove("b"));
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));
    }

    /**
     * <p><b>Summary:</b>
     * Verifica che {@code clear} elimini tutti i mapping.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si usano due chiavi distinte per evitare che il controllo coincida con il
     * caso più semplice di una singola rimozione. La dimensione e l'assenza di
     * entrambe le chiavi confermano lo svuotamento completo.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"} e {@code "b"="2"}, quindi invoca
     * {@code clear()}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene due mapping distinti.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa non contiene più alcun mapping.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code size()} restituisce {@code 0} e {@code containsKey} restituisce
     * {@code false} per entrambe le chiavi iniziali.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica la copia di tutti i mapping mediante {@code putAll}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Sorgente e destinazione condividono la chiave {@code "a"}, ma con valori
     * diversi; ciascuna possiede inoltre una chiave esclusiva. Questa
     * configurazione controlla nello stesso caso la sostituzione del valore
     * sovrapposto, l'aggiunta di un mapping nuovo e la conservazione del mapping
     * già presente soltanto nella destinazione.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Copia i mapping {@code "a"="1"} e {@code "b"="2"} in una mappa che
     * contiene {@code "a"="old"} e {@code "c"="3"}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * Sorgente e destinazione sono mappe valide; la chiave {@code "a"} è
     * presente in entrambe.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La destinazione contiene l'unione delle chiavi e usa il valore proveniente
     * dalla sorgente per la chiave comune.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La dimensione finale è {@code 3}; i valori associati ad {@code "a"},
     * {@code "b"} e {@code "c"} sono rispettivamente {@code "1"},
     * {@code "2"} e {@code "3"}.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica che {@code putAll} rifiuti una mappa sorgente {@code null}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Il riferimento nullo viene passato direttamente e l'eccezione attesa è
     * dichiarata nell'annotazione JUnit. Il caso controlla il limite del
     * contratto prima che possa iniziare l'iterazione sui mapping della sorgente.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Invoca {@code putAll(null)} sulla fixture.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La destinazione è una mappa valida e la sorgente non esiste.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * L'operazione si interrompe prima di poter copiare un mapping.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * JUnit rileva esattamente una {@link NullPointerException}; una conclusione
     * normale del metodo rende il test fallito.
     * </p>
     */
    @Test(expected = NullPointerException.class)
    public void putAllRejectsNullMap() {
        map.putAll(null);
    }

    /**
     * <p><b>Summary:</b>
     * Verifica la stabilità di {@code putAll} quando sorgente e destinazione
     * coincidono.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La mappa contiene due mapping e viene passata a se stessa. Il caso è utile
     * perché l'implementazione attraversa la sorgente mentre scrive nella
     * destinazione; dimensione e valori permettono di rilevare perdite o
     * duplicazioni indesiderate.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce due coppie e invoca {@code map.putAll(map)}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La stessa istanza, usata come sorgente e destinazione, contiene
     * {@code "a"="1"} e {@code "b"="2"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Entrambi i mapping rimangono invariati.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La dimensione resta {@code 2} e le due chiavi conservano i rispettivi
     * valori.
     * </p>
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

    /**
     * <p><b>Summary:</b>
     * Verifica le operazioni di copia quando la mappa sorgente è vuota.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Lo stesso caso limite viene applicato sia a {@code putAll} sia al
     * costruttore di copia. In questo modo si controlla che entrambi i percorsi
     * accettino una sorgente valida priva di mapping, senza confonderla con una
     * sorgente {@code null}.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Copia una mappa vuota nella fixture e costruisce poi un nuovo
     * {@link MapAdapter} a partire dalla stessa sorgente.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * Sorgente e destinazione sono istanze valide e vuote.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Sia la fixture sia la nuova copia rimangono vuote.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code isEmpty()} restituisce {@code true} per entrambe le destinazioni e
     * la copia risulta uguale alla sorgente vuota.
     * </p>
     */
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
     * <p><b>Summary:</b>
     * Verifica contenuto e indipendenza strutturale della mappa prodotta dal
     * costruttore di copia.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Prima si confrontano sorgente e copia, poi si rimuove una chiave soltanto
     * dalla sorgente. Questa seconda fase è necessaria perché due mappe uguali
     * subito dopo la costruzione potrebbero ancora condividere erroneamente la
     * stessa struttura interna.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Copia una sorgente con due mapping, ne verifica l'uguaglianza e rimuove
     * {@code "a"} soltanto dall'originale.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La sorgente contiene {@code "a"="1"} e {@code "b"="2"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La sorgente non contiene più {@code "a"}, mentre la copia conserva
     * entrambi i mapping iniziali.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Le mappe risultano uguali subito dopo la copia; in seguito
     * {@code copy.containsKey("a")} restituisce {@code true} e la dimensione
     * della copia resta {@code 2}.
     * </p>
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

    /**
     * <p><b>Summary:</b>
     * Verifica che il costruttore di copia non invochi il metodo sovrascrivibile
     * {@code put} prima dell'inizializzazione completa della sottoclasse.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * {@link InitializationGuardMapAdapter} sovrascrive {@code put} e lancia un
     * {@link AssertionError} se il metodo viene chiamato durante l'esecuzione del
     * costruttore della superclasse. Questo oggetto sentinella rende osservabile
     * una scelta progettuale importante: la copia deve inizializzare direttamente
     * il proprio stato, senza effettuare chiamate virtuali su un oggetto ancora
     * incompleto.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Costruisce la sottoclasse da una sorgente con due mapping, ne controlla il
     * contenuto e aggiunge poi un terzo mapping soltanto alla sorgente.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La sorgente contiene {@code "a"="1"} e {@code "b"="2"}; il flag della
     * sottoclasse diventa vero soltanto dopo il ritorno da {@code super(source)}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La copia contiene i due mapping iniziali ed è indipendente dalle modifiche
     * successive della sorgente.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La costruzione termina senza {@link AssertionError}, copia e sorgente sono
     * inizialmente uguali e l'aggiunta di {@code "c"="3"} alla sorgente non
     * cambia la copia, la cui dimensione resta {@code 2}.
     * </p>
     */
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
     * <p><b>Summary:</b>
     * Verifica che il costruttore di copia rifiuti una sorgente {@code null}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Il cast esplicito a {@link HMap} seleziona senza ambiguità il costruttore
     * di copia. L'eccezione è dichiarata nell'annotazione JUnit perché non è
     * necessario eseguire altre verifiche dopo il tentativo di costruzione.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Tenta di creare un {@link MapAdapter} passando {@code null} come sorgente.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * Non è disponibile alcuna istanza sorgente da copiare.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Non viene restituita una nuova mappa valida.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La costruzione solleva esattamente una {@link NullPointerException}.
     * </p>
     */
    @Test(expected = NullPointerException.class)
    public void copyConstructorRejectsNullMap() {
        new MapAdapter((HMap) null);
    }

    /**
     * <p><b>Summary:</b>
     * Verifica riflessività, simmetria e transitività di {@code equals} per
     * mappe con gli stessi mapping.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Tre mappe distinte vengono popolate con lo stesso contenuto; nella seconda
     * l'ordine di inserimento è invertito. La scelta controlla sia le proprietà
     * generali di {@code equals} sia l'indipendenza dall'ordine non garantito da
     * {@code Hashtable}.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Confronta la fixture con se stessa, confronta la prima e la seconda mappa
     * in entrambe le direzioni e usa la terza per completare la verifica di
     * transitività.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * Le tre istanze contengono i mapping {@code "a"="1"} e
     * {@code "b"="2"}, pur essendo state popolate in sequenze diverse.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Nessuna delle mappe viene modificata dai confronti.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Tutte le relazioni di uguaglianza controllate restituiscono
     * {@code true}.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica che {@code equals} rifiuti contenuti differenti, {@code null} e
     * oggetti che non implementano {@link HMap}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Le mappe confrontate hanno tutte dimensione {@code 1}, ma differiscono
     * prima per chiave e poi per valore. Mantenere uguale la dimensione impedisce
     * che un controllo preliminare sul numero di elementi nasconda errori nel
     * confronto dei mapping. Si aggiungono inoltre i casi di tipo incompatibile
     * e riferimento nullo.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Confronta {@code "a"="1"} con {@code "b"="1"}, con
     * {@code "a"="different"}, con {@code null} e con una {@link String}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * Ciascuna mappa contiene un solo mapping e gli oggetti di confronto sono
     * distinti dalla fixture.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Tutti gli oggetti coinvolti conservano il proprio contenuto.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Ognuna delle quattro chiamate a {@code equals} restituisce {@code false}.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica la formula usata per calcolare il codice hash della mappa.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si usano due mapping formati da stringhe, i cui codici hash sono stabili e
     * possono essere combinati direttamente nel test. Il valore atteso viene
     * calcolato come somma dei codici delle entry, ciascuno ottenuto tramite XOR
     * tra chiave e valore, secondo il contratto di {@code Map}.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"} e {@code "b"="2"}, calcola esplicitamente il
     * risultato atteso e lo confronta con {@code map.hashCode()}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene due mapping con chiavi e valori non nulli.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il calcolo non modifica il contenuto della mappa.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Il codice restituito coincide con
     * {@code (hash("a") ^ hash("1")) + (hash("b") ^ hash("2"))}.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica la coerenza tra {@code equals} e {@code hashCode}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Due istanze vengono popolate con gli stessi mapping in ordine opposto.
     * Prima si accerta la loro uguaglianza, poi si confrontano i codici hash:
     * questa sequenza verifica direttamente l'implicazione richiesta dal
     * contratto degli oggetti uguali.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce le coppie {@code "a"="1"} e {@code "b"="2"} nelle due mappe
     * con ordine diverso e confronta uguaglianza e hash.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * Le mappe sono istanze distinte ma contengono gli stessi mapping.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Entrambe le mappe rimangono invariate.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * Le mappe risultano uguali e producono lo stesso valore di
     * {@code hashCode()}.
     * </p>
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
     * <p><b>Summary:</b>
     * Verifica che la rappresentazione testuale contenga tutti i mapping.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Il test cerca separatamente le due sottostringhe {@code "a=1"} e
     * {@code "b=2"}, senza confrontare l'intera stringa. Questa scelta evita di
     * imporre un ordine che {@code Hashtable} non garantisce e concentra la
     * verifica sulle informazioni effettivamente richieste.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce due mapping, ottiene il risultato di {@code toString()} e cerca
     * al suo interno entrambe le forme {@code chiave=valore}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La mappa contiene {@code "a"="1"} e {@code "b"="2"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La conversione in stringa non modifica la mappa.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La rappresentazione contiene sia {@code "a=1"} sia {@code "b=2"}, in
     * qualunque ordine.
     * </p>
     */
    @Test
    public void toStringContainsEveryMapping() {
        map.put("a", "1");
        map.put("b", "2");
        String representation = map.toString();
        assertTrue(representation.indexOf("a=1") >= 0);
        assertTrue(representation.indexOf("b=2") >= 0);
    }

    /**
     * Oggetto di supporto che riconosce per identità un solo riferimento.
     *
     * <p>La relazione prodotta è volutamente asimmetrica rispetto a un normale
     * {@link Object}: serve esclusivamente a rendere osservabile quale operando
     * riceve la chiamata a {@code equals} nei test di {@code containsValue}.</p>
     */
    private static final class AsymmetricEqualsProbe {
        /** Riferimento che deve essere riconosciuto dal confronto. */
        private final Object match;

        /**
         * Crea un probe associato al riferimento atteso.
         *
         * @param expected oggetto che il probe deve riconoscere per identità
         */
        private AsymmetricEqualsProbe(Object expected) {
            match = expected;
        }

        /**
         * Confronta l'argomento con il solo riferimento registrato.
         *
         * @param object oggetto da confrontare con il riferimento atteso
         * @return {@code true} soltanto se {@code object} e {@link #match}
         *         indicano la stessa istanza; {@code false} altrimenti
         */
        public boolean equals(Object object) {
            return object == match;
        }

        /**
         * Restituisce il codice hash dell'oggetto riconosciuto.
         *
         * <p>Il metodo mantiene coerente il probe con il riferimento per il quale
         * {@link #equals(Object)} restituisce {@code true}.</p>
         *
         * @return codice hash del riferimento memorizzato in {@link #match}
         */
        public int hashCode() {
            return match.hashCode();
        }
    }

    /**
     * Sottoclasse sentinella usata per controllare il costruttore di copia.
     *
     * <p>Il metodo {@link #put(Object, Object)} segnala con un errore qualsiasi
     * chiamata virtuale effettuata dalla superclasse prima che il costruttore
     * della sottoclasse abbia completato la propria inizializzazione.</p>
     */
    private static final class InitializationGuardMapAdapter
            extends MapAdapter {
        /**
         * Indica che l'esecuzione di {@code super(source)} è terminata e che la
         * sottoclasse può ricevere chiamate ai propri metodi sovrascritti.
         */
        private boolean initialized;

        /**
         * Costruisce la mappa sentinella copiando la sorgente e abilita
         * {@code put} soltanto dopo il completamento del costruttore della
         * superclasse.
         *
         * @param source mappa valida dalla quale copiare i mapping
         */
        private InitializationGuardMapAdapter(HMap source) {
            super(source);
            initialized = true;
        }

        /**
         * Inserisce un mapping soltanto dopo la completa inizializzazione della
         * sottoclasse.
         *
         * @param key chiave del mapping da inserire
         * @param value valore da associare alla chiave
         * @return valore precedentemente associato alla chiave, oppure
         *         {@code null} se la chiave non era presente
         * @throws AssertionError se il metodo viene invocato prima che
         *         {@link #initialized} sia diventato {@code true}
         */
        public Object put(Object key, Object value) {
            if (!initialized) {
                throw new AssertionError(
                        "put invocato prima dell'inizializzazione");
            }
            return super.put(key, value);
        }
    }
}
