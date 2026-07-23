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
 * Test suite dedicata alle operazioni fondamentali di {@link MapAdapter},
 * osservate principalmente attraverso il contratto di {@link HMap}.
 *
 * <p>
 * <b>Summary:</b>
 * La suite comprende test di verifica per le principali
 * operazioni di {@code Map} previste da J2SE 1.4.2 che non dipendono
 * direttamente dalle viste della mappa.
 *
 * I primi test controllano lo stato iniziale della mappa, l'inserimento di una
 * nuova associazione e la sostituzione del valore associato a una chiave già
 * presente. Successivamente vengono verificate le operazioni di ricerca
 * {@code get()}, {@code containsKey()} e {@code containsValue()}, compreso il
 * modo in cui viene utilizzato il metodo {@code equals()} durante i confronti.
 *
 * Un altro gruppo di test riguarda la rimozione delle associazioni e lo
 * svuotamento completo della mappa. Il metodo {@code putAll()} viene provato
 * usando una mappa popolata, una mappa vuota, la stessa mappa di destinazione
 * e una sorgente {@code null}. Anche il costruttore di copia viene verificato
 * con una sorgente popolata, vuota oppure {@code null}. Nel caso della sorgente
 * popolata viene inoltre controllato che la copia sia indipendente, quindi che
 * una modifica successiva della mappa originale non cambi quella copiata.
 *
 * Gli ultimi test verificano i metodi {@code equals()}, {@code hashCode()} e
 * {@code toString()}. Per la rappresentazione testuale si controlla la presenza
 * delle associazioni, senza fare ipotesi sul loro ordine. Nei test in cui è
 * necessario vengono considerati anche i casi con chiavi, valori o mappe
 * {@code null}, seguendo i vincoli della {@code Hashtable} utilizzata come
 * oggetto adattato.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * Prima di ogni test viene creata una nuova mappa, in modo che il risultato
 * dipenda solamente dai dati preparati nel test corrente e non dalle modifiche
 * eseguite dai test precedenti.
 *
 * Quando non è necessario accedere alle caratteristiche specifiche
 * dell'implementazione, le mappe vengono dichiarate usando l'interfaccia
 * {@link HMap}. Il tipo concreto {@link MapAdapter} viene invece utilizzato per
 * creare le istanze e verificare i costruttori.
 *
 * Le chiavi e i valori sono rappresentati da stringhe brevi e diverse tra loro.
 * Questa scelta rende immediato riconoscere le associazioni presenti nella
 * mappa e permette di calcolare facilmente il codice hash atteso. Per i metodi
 * di modifica che restituiscono un valore, come {@code put()} e
 * {@code remove()}, vengono controllati sia il valore restituito sia il
 * contenuto finale della mappa. Per {@code clear()} e per i casi normali di
 * {@code putAll()}, che non restituiscono alcun valore, viene invece verificato
 * direttamente lo stato della mappa dopo la chiamata. Nei casi che devono
 * produrre un'eccezione si controlla anche che la mappa non sia stata
 * modificata
 * solo in parte.
 *
 * Per verificare la direzione del confronto effettuato da {@code equals()}
 * viene
 * utilizzato un oggetto con uguaglianza non simmetrica, così è possibile capire
 * quale dei due oggetti riceve effettivamente la chiamata.
 *
 * Infine, nei confronti tra mappe le associazioni vengono inserite in ordini
 * differenti, così si verifica che l'uguaglianza dipenda dal contenuto e non
 * dall'ordine di inserimento. Anche il risultato di {@code toString()} viene
 * controllato solamente in base alle associazioni presenti, perché la
 * {@code Hashtable} non garantisce uno specifico ordine di iterazione.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see HMap
 * @see MapAdapter
 */
public class MapAdapterTest {
    /**
     * Mappa di partenza usata dalla maggior parte dei metodi di test. Il
     * riferimento è dichiarato come {@link HMap} per esercitare il contratto
     * pubblico senza dipendere dai dettagli interni di {@link MapAdapter}.
     */
    private HMap map;

    /**
     * Crea una mappa iniziale vuota prima di ogni test.
     *
     * <p>
     * La reinizializzazione fa sì che ogni caso parta da uno stato noto
     * e che un eventuale fallimento non condizioni i test eseguiti in seguito.
     * </p>
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica lo stato iniziale di un {@link MapAdapter} appena costruito.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il caso controlla direttamente lo stato prodotto dal costruttore senza
     * argomenti. Si confrontano {@code size()} e {@code isEmpty()} poichè
     * descrivono lo stesso
     * stato con due tipi di risultato diversi e devono essere coerenti.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Dopo la preparazione della condizione iniziale, il test invoca prima
     * {@code size()} e
     * ne confronta il risultato con zero; invoca poi {@code isEmpty()} e ne
     * verifica il valore booleano.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * JUnit ha eseguito {@link #setUp()}; {@link #map} riferendo quindi un nuovo
     * {@link MapAdapter} pulito evalido.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non esegue cambiamenti sulla mappa.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
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
     * <p>
     * <b>Summary:</b>
     * Verifica l'inserimento di una coppia nuova e la successiva lettura del
     * valore associato.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * la coppia {@code "a"="1"} il
     * test esercita il normale percorso di inserimento senza casi
     * di errore. Verifica: il valore
     * restituito da {@code put} distingue un nuovo inserimento da una
     * sostituzione; {@code get} verifica l'associazione; {@code size} rileva
     * inserimenti mancanti o duplicati; {@code isEmpty} conferma che, dopo
     * l'inserimento, la mappa non è più vuota.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test inserisce {@code "a"="1"} e verifica immediatamente il valore
     * restituito da {@code put}. Successivamente legge la chiave {@code "a"},
     * misura la dimensione della mappa e ne interroga lo stato di vuoto.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * JUnit ha creato tramite {@link #setUp()} una mappa vuota; la chiave
     * {@code "a"} e il valore {@code "1"}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * La mappa contiene una sola coppia, nel quale la chiave
     * {@code "a"} è associata al valore {@code "1"}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
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
     * <p>
     * <b>Summary:</b>
     * Verifica la sostituzione del valore associato a una chiave già presente.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Due chiamate a {@code put} usano la stessa chiave e due valori diversi.
     * Il normale inserimento non copre il
     * comportamento in presenza di una chiave già registrata. Il ritorno della
     * seconda chiamata verifica la disponibilità del valore precedente, mentre
     * lettura e dimensione distinguono una sostituzione corretta dall'aggiunta
     * accidentale di un seconda coppia.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test inserisce {@code "a"="1"}. Quindi invoca di nuovo
     * {@code put} con la stessa chiave e il valore {@code "2"}, confronta il
     * ritorno con {@code "1"}, legge il valore corrente e controlla la dimensione.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappae è vuota; prima della seconda chiamata a {@code put},
     * il test inserisce il solo mapping {@code "a"="1"}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * La chiave {@code "a"} è associata a {@code "2"} e la dimensione resta
     * pari a {@code 1}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * La seconda chiamata a {@code put} restituisce il valore precedente
     * {@code "1"}, {@code get("a")} restituisce {@code "2"} e la dimensione
     * rimane pari a {@code 1}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code put} rifiuti una chiave {@code null}.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * inserisco il valore 1 accoppiato con chiave nulla. così facendo si verifica
     * che la mappa non accetti chiavi nulle. La gestione esplicita dell'eccezione
     * consente di controllare anche che l'operazione non lasci un mapping
     * incompleto.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test tenta {@code put(null, "1")} nel blocco {@code try}. Se la chiamata
     * termina normalmente esegue {@code fail()}; se riceve una
     * {@link NullPointerException}, entra nel blocco {@code catch} e verifica che
     * la mappa sia ancora vuota.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * {@link #setUp()} ha prodotto una mappa vuota; il riferimento usato come
     * chiave è {@code null}, mentre il valore {@code "1"} è valido e non nullo.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Nessun coppia viene inserita.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code put(null, "1")} lancia {@link NullPointerException} e
     * {@code isEmpty()} conferma che la mappa è ancora vuota.
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code put} rifiuti un valore {@code null}.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * La chiave {@code "a"} è valida e soltanto il valore è nullo,così facendo
     * isolo il vincolo sui valori da quello sulle chiavi, già
     * coperto dal test precedente. La gestione esplicita dell'eccezione consente
     * di controllare anche che l'operazione non lasci una coppia incompleta.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test invoca {@code put("a", null)} in un blocco {@code try}. Un ritorno
     * normale provoca {@code fail()}; dopo l'intercettazione di una
     * {@link NullPointerException}, viene verificato lo stato di vuoto.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa predisposta da {@link #setUp()} è vuota; la chiave
     * {@code "a"} è non nulla, mentre il riferimento usato come valore è
     * {@code null}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * La mappa non contiene alcun coppia.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code put("a", null)} lancia {@link NullPointerException} e
     * {@code isEmpty()} conferma che la mappa è ancora vuota.
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
     * <p>
     * <b>Summary:</b>
     * Verifica il risultato di {@code get} per una chiave assente.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * La ricerca viene eseguita su una mappa vuota usando la chiave non nulla
     * {@code "missing"}. Il successivo
     * controllo di {@code isEmpty()} conferma che una ricerca senza
     * corrispondenza non inserisca implicitamente un mapping.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test chiama {@code get("missing")} e confronta il risultato con
     * {@code null}; subito dopo invoca {@code isEmpty()} sulla stessa mappa
     * iniziale.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * {@link #setUp()} ha creato una mappa senza coppie e la chiave di ricerca
     * {@code "missing"} è un riferimento valido e non nullo.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * La ricerca non modifica lo stato della mappa.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code containsKey} cerchi solo tra le chiavi.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * La chiave {@code "key"} e il valore {@code "value"}; vengono entrambi usati
     * come argomenti di
     * {@code containsKey}. l'obbiettivo è verificare che la funzione cerchi
     * esclusivamente tra le chiavi, quindi la ricerca di {@code "key"} deve
     * restituire {@code true}, mentre la ricerca di {@code "value"} deve restituire
     * {@code false}.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test inserisce {@code "key"="value"}. Quindi passa prima
     * {@code "key"} e poi {@code "value"} a {@code containsKey}, confrontando i
     * due risultati rispettivamente con {@code true} e {@code false}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa nasce vuota; prima delle ricerche il test vi inserisce una sola
     * coppia con chiave e valore non nulli e diversi.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non verifica una post-condizione della mappa; riguardano soltanto i
     * valori booleani restituiti dalle due
     * ricerche.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code containsValue} cerchi esclusivamente tra i valori.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * La chiave {@code "key"} e il valore {@code "value"}; vengono entrambi usati
     * come argomenti di
     * {@code containsValue}. l'obbiettivo è verificare che la funzione cerchi
     * esclusivamente tra i valori, quindi la ricerca di {@code "value"} deve
     * restituire {@code true}, mentre la ricerca di {@code "key"} deve restituire
     * {@code false}.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test inserisce {@code "key"="value"}, chiama
     * {@code containsValue("value")} e successivamente
     * {@code containsValue("key")}, verificando entrambi i risultati.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La condizione iniziale della mappa è vuota; poi viene popolata con
     * una coppia la cui chiave e il cui valore sono diversi e non nulli.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non verifica una post-condizione della mappa; riguardano soltanto i
     * valori booleani restituiti dalle due
     * ricerche.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code containsValue()} richiami {@code equals()} sul valore
     * cercato e non sul valore già presente nella mappa.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Come valore della mappa viene inserito un normale {@code Object}. Per la
     * ricerca viene invece usato un {@link AsymmetricEqualsProbe}, il cui metodo
     * {@code equals()} restituisce {@code true} solamente quando riceve lo stesso
     * riferimento memorizzato nella mappa.
     * il test ha esito positivo
     * soltanto se {@code equals()} viene chiamato sul valore cercato.
     * L'uso di due stringhe non permetterebbe di verificare questa
     * differenza, perché la loro uguaglianza è simmetrica.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Inserisce {@code stored} nella mappa con chiave {@code "key"}, quindi cerca
     * tale valore usando l'oggetto {@code searched}. Dopo la ricerca controlla che
     * la mappa contenga ancora una sola associazione e che alla chiave
     * {@code "key"} sia ancora collegato lo stesso oggetto inserito inizialmente.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa contiene solamente l'associazione tra {@code "key"} e
     * {@code stored}. L'oggetto {@code searched} riconosce il riferimento
     * {@code stored}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * La ricerca non modifica il contenuto della mappa.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code containsValue(searched)} restituisce {@code true}, la dimensione
     * rimane {@code 1} e {@code get("key")} restituisce lo stesso riferimento
     * contenuto in {@code stored}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code containsValue()} non consideri valido un confronto
     * effettuato nella direzione opposta a quella prevista.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il valore {@code stored} riconosce {@code searched} tramite il proprio
     * {@code equals()}, mentre {@code searched} non riconosce {@code stored}.
     * In questo modo la ricerca deve restituire {@code false}: un risultato
     * {@code true} indicherebbe che il confronto è stato eseguito sul valore
     * contenuto nella mappa.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Inserisce {@code stored} con chiave {@code "key"} e richiama
     * {@code containsValue(searched)}. Controlla poi che la ricerca non abbia
     * modificato la mappa.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa contiene solamente l'associazione tra {@code "key"} e
     * {@code stored}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * L'associazione inserita rimane invariata.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code containsValue(searched)} restituisce {@code false}, la dimensione
     * rimane {@code 1} e {@code get("key")} restituisce lo stesso oggetto
     * memorizzato.
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code containsValue(null)} lanci una
     * {@link NullPointerException} senza modificare la mappa.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Prima della ricerca viene inserita un'associazione valida. In questo modo il
     * test controlla sia il rifiuto del valore {@code null}, dovuto ai vincoli
     * della {@code Hashtable}, sia che l'eccezione non provochi
     * modifiche al contenuto già presente.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Inserisce l'associazione {@code key=value} e richiama
     * {@code containsValue(null)}. Se la chiamata termina normalmente viene
     * eseguito {@code fail()}; dopo aver intercettato l'eccezione vengono
     * controllati la dimensione e il valore associato alla chiave
     * {@code "key"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa contiene solamente l'associazione {@code key=value}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * L'associazione presente prima della ricerca rimane invariata.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code containsValue(null)} solleva una {@link NullPointerException};
     * la dimensione rimane {@code 1} e {@code get("key")} restituisce
     * {@code "value"}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code get(null)} lanci una
     * {@link NullPointerException} senza modificare la mappa.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * La chiamata viene eseguita dopo aver inserito un'associazione valida. La
     * mappa non vuota permette di controllare sia il rifiuto della chiave
     * {@code null}, dovuto ai vincoli della {@code Hashtable} adattata, sia la
     * conservazione del contenuto dopo l'eccezione.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Inserisce l'associazione {@code a=1} e richiama {@code get(null)}. Se non
     * viene sollevata alcuna eccezione il test esegue {@code fail()}; dopo aver
     * intercettato la {@link NullPointerException} controlla la dimensione della
     * mappa e rilegge il valore associato alla chiave {@code "a"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa contiene solamente l'associazione {@code a=1}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * L'associazione {@code a=1} rimane presente e invariata.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code get(null)} solleva una {@link NullPointerException}; la dimensione
     * rimane {@code 1} e {@code get("a")} restituisce {@code "1"}.
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
 * <p>
 * <b>Summary:</b>
 * Verifica che {@code containsKey(null)} sollevi una
 * {@link NullPointerException} senza modificare la mappa.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * Prima della ricerca viene inserita un'associazione valida. In questo modo
 * viene controllato sia il rifiuto della chiave {@code null}, dovuto ai
 * vincoli della {@code Hashtable} adattata, sia che il contenuto della mappa
 * rimanga invariato dopo l'eccezione.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Inserisce l'associazione {@code a=1} e richiama
 * {@code containsKey(null)}. Se non viene lanciata alcuna eccezione, il test
 * esegue {@code fail()}; dopo aver intercettato la
 * {@link NullPointerException} controlla la dimensione e il valore associato
 * alla chiave {@code "a"}.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La mappa contiene solamente l'associazione {@code a=1}.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * L'associazione {@code a=1} rimane presente e invariata.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
 * {@code containsKey(null)} solleva una {@link NullPointerException}; la
 * dimensione rimane {@code 1} e {@code get("a")} restituisce {@code "1"}.
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
 * <p>
 * <b>Summary:</b>
 * Verifica che {@code remove(null)} lanci una
 * {@link NullPointerException} senza rimuovere associazioni dalla mappa.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * La chiamata viene eseguita su una mappa contenente un'associazione valida.
 * Poiché {@code remove()} è un metodo che modifica la mappa, dopo l'eccezione
 * viene controllato che l'associazione iniziale non sia stata rimossa.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Inserisce la coppia {@code a=1} e richiama {@code remove(null)}. Se la
 * chiamata termina normalmente viene eseguito {@code fail()}; dopo aver
 * intercettato la {@link NullPointerException} vengono controllati la
 * dimensione e il valore ancora associato alla chiave {@code "a"}.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La mappa contiene solamente l'associazione {@code a=1}.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * Nessuna associazione viene rimossa e il contenuto della mappa rimane
 * invariato.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
 * {@code remove(null)} solleva una {@link NullPointerException}; la dimensione
 * rimane {@code 1} e {@code get("a")} restituisce {@code "1"}.
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
 * <p>
 * <b>Summary:</b>
 * Verifica che {@code remove()} restituisca il valore precedente e rimuova
 * un'associazione presente nella mappa.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * La mappa contiene una sola coppia. Il test controlla sia il valore
 * restituito da {@code remove()} sia lo stato finale della mappa, verificando
 * che la chiave sia stata eliminata e che la mappa sia vuota.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Inserisce l'associazione {@code "a"="1"} e richiama {@code remove("a")}.
 * Successivamente controlla il valore restituito, l'assenza della chiave
 * {@code "a"} e lo stato vuoto della mappa.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La mappa contiene solamente la coppia {@code "a"="1"}.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * La coppia viene rimossa e la mappa rimane vuota.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
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
 * <p>
 * <b>Summary:</b>
 * Verifica che la rimozione di una chiave assente restituisca {@code null}
 * senza modificare la mappa.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * Nella mappa viene inserita un'associazione valida, mentre la rimozione viene
 * richiesta usando una chiave diversa. Oltre al valore restituito vengono
 * controllati la dimensione e il contenuto, in modo da verificare che
 * la coppia chiave-valore esistente non venga modificata.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Inserisce l'associazione {@code "a"="1"} e richiama
 * {@code remove("b")}. Controlla poi il valore restituito, la dimensione
 * della mappa e il valore ancora associato alla chiave {@code "a"}.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La mappa contiene solamente {@code "a"="1"}, mentre la
 * chiave {@code "b"} è assente.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * L'associazione {@code "a"="1"} rimane presente e immutata.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
 * {@code remove("b")} restituisce {@code null}, la dimensione rimane
 * {@code 1} e {@code get("a")} restituisce {@code "1"}.
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
 * <p>
 * <b>Summary:</b>
 * Verifica che {@code clear()} rimuova tutte le associazioni presenti nella
 * mappa.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * Prima della chiamata vengono inseriti due abbinamenti con chiavi diverse.
 * Dopo {@code clear()} vengono controllati la dimensione e l'assenza di
 * entrambe le chiavi, così da verificare lo svuotamento completo della mappa.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Inserisce {@code "a"="1"} e {@code "b"="2"}, quindi
 * richiama {@code clear()}. Infine controlla la dimensione della mappa e
 * verifica che le due chiavi non siano più presenti.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La mappa contiene {@code "a"="1"} e
 * {@code "b"="2"}.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * Tutte le coppie vengono rimosse e la mappa rimane vuota.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
 * {@code size()} restituisce {@code 0} e {@code containsKey()} restituisce
 * {@code false} sia per la chiave {@code "a"} sia per la chiave
 * {@code "b"}.
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
 * <p>
 * <b>Summary:</b>
 * Verifica che {@code putAll()} copi tutte le associazioni dalla mappa
 * sorgente alla destinazione.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * La sorgente e la destinazione contengono entrambe la chiave {@code "a"},
 * ma con valori diversi. La sorgente contiene inoltre la chiave
 * {@code "b"}, mentre la destinazione contiene la chiave {@code "c"}.
 * Questa configurazione permette di verificare la sostituzione di un valore,
 * l'aggiunta di una nuova associazione e la conservazione di quella già
 * presente solamente nella destinazione.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Inserisce nella sorgente {@code "a"="1"} e
 * {@code "b"="2"} e nella destinazione {@code "a"="old"} e
 * {@code "c"="3"}. Dopo {@code putAll(source)}, controlla la dimensione
 * finale e i valori associati alle tre chiavi.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La sorgente contiene {@code "a"="1"} e {@code "b"="2"}, mentre la
 * destinazione contiene {@code "a"="old"} e {@code "c"="3"}.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * La destinazione contiene tutte le chiavi delle due mappe e, per la chiave
 * comune {@code "a"}, conserva il valore proveniente dalla sorgente.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
 * La dimensione finale è {@code 3}; {@code get("a")},
 * {@code get("b")} e {@code get("c")} restituiscono rispettivamente
 * {@code "1"}, {@code "2"} e {@code "3"}.
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
 * <p>
 * <b>Summary:</b>
 * Verifica che {@code putAll(null)} lanci una
 * {@link NullPointerException}.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * Alla mappa di destinazione viene passato direttamente un valore
 * {@code null}. Il caso verifica che {@code putAll()} distingua una sorgente
 * nulla da una mappa sorgente valida.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Richiama {@code putAll(null)} sulla mappa di destinazione. L'eccezione
 * attesa viene indicata nell'annotazione {@link Test}.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La mappa di destinazione è valida, mentre il riferimento passato come
 * sorgente è {@code null}.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * L'operazione termina sollevando l'eccezione prevista.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
 * {@code putAll(null)} solleva una {@link NullPointerException}.
 * </p>
 */
@Test(expected = NullPointerException.class)
public void putAllRejectsNullMap() {
    map.putAll(null);
}

/**
 * <p>
 * <b>Summary:</b>
 * Verifica che {@code putAll()} non modifichi il contenuto quando la stessa
 * mappa viene usata come sorgente e destinazione.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * Nella mappa vengono inserite due coppie e la stessa istanza viene
 * passata a {@code putAll()}. La dimensione e i due valori vengono controllati
 * per verificare che nessuna associazione venga rimossa o modificata.
 * </p>
 *
 * <p>
 * <b>Test Description:</b>
 * Inserisce le associazioni {@code "a"="1"} e {@code "b"="2"}, quindi
 * richiama {@code map.putAll(map)}. Infine controlla la dimensione e i valori
 * associati alle due chiavi.
 * </p>
 *
 * <p>
 * <b>Pre-Condition:</b>
 * La stessa mappa, utilizzata come sorgente e destinazione, contiene
 * {@code "a"="1"} e {@code "b"="2"}.
 * </p>
 *
 * <p>
 * <b>Post-Condition:</b>
 * Le due associazioni rimangono presenti e invariate.
 * </p>
 *
 * <p>
 * <b>Expected Results:</b>
 * La dimensione rimane {@code 2}; {@code get("a")} restituisce
 * {@code "1"} e {@code get("b")} restituisce {@code "2"}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica le operazioni di copia quando la mappa sorgente è vuota.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Lo stesso caso limite viene applicato sia a {@code putAll} sia al
     * costruttore di copia. In questo modo si controlla che entrambi i percorsi
     * accettino una sorgente valida priva di mapping, senza confonderla con una
     * sorgente {@code null}. L'ultima asserzione controlla inoltre che
     * {@code equals} consideri uguali due mappe vuote distinte.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test crea una sorgente vuota, la passa a {@code map.putAll(empty)} e
     * verifica che la fixture sia vuota. Costruisce poi un nuovo
     * {@link MapAdapter} dalla stessa sorgente, ne controlla lo stato di vuoto e
     * infine confronta sorgente e copia con {@code equals}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * {@link #setUp()} ha creato una destinazione vuota e il test crea una seconda
     * istanza valida, anch'essa vuota, da usare come sorgente comune alle due
     * operazioni di copia.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Sia la fixture sia la nuova copia rimangono vuote.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
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
     * <p>
     * <b>Summary:</b>
     * Verifica l'uguaglianza iniziale della copia e, dopo una chiamata a
     * {@code remove("a")} sulla sorgente, controlla nella copia la presenza
     * della chiave {@code "a"} e la dimensione.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Prima si confrontano sorgente e copia, poi si invoca {@code remove("a")}
     * soltanto sulla sorgente. Questa seconda fase permette di controllare che
     * nella copia la chiave {@code "a"} sia ancora presente e la dimensione
     * valga due. Il test non richiede una copia profonda di chiavi e valori e
     * non rilegge il mapping {@code "b"} dopo la modifica della sorgente.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test inserisce due mapping nella fixture, costruisce {@code copy} dalla
     * fixture e verifica l'uguaglianza iniziale. Poi invoca
     * {@code map.remove("a")} e controlla che nella copia la chiave sia ancora
     * presente e la dimensione sia ancora pari a due.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La fixture nasce vuota; prima della costruzione viene usata come sorgente e
     * contiene i due mapping distinti {@code "a"="1"} e {@code "b"="2"}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Dopo {@code map.remove("a")}, {@code copy.containsKey("a")} restituisce
     * {@code true} e {@code copy.size()} restituisce {@code 2}. Il test non
     * ricontrolla il contenuto finale della sorgente né il valore della chiave
     * {@code "b"} nella copia.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code assertEquals(map, copy)} riesce subito dopo la costruzione; dopo la
     * modifica della sorgente, {@code copy.containsKey("a")} restituisce
     * {@code true} e {@code copy.size()} restituisce {@code 2}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica che il costruttore di copia rifiuti una sorgente {@code null}.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il cast esplicito a {@link HMap} seleziona senza ambiguità il costruttore
     * di copia. L'eccezione è dichiarata nell'annotazione JUnit perché non è
     * necessario eseguire altre verifiche dopo il tentativo di costruzione. Il
     * caso è distinto dalla copia di una mappa vuota per controllare che il
     * costruttore riconosca la differenza tra una sorgente valida priva di
     * mapping e una sorgente {@code null}.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test valuta l'espressione {@code new MapAdapter((HMap) null)}; il cast
     * forza la selezione del costruttore di copia e JUnit confronta l'eccezione
     * risultante con il tipo dichiarato nell'annotazione.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * Il riferimento fornito come sorgente di tipo {@link HMap} vale
     * {@code null}; non è richiesto alcuno stato della fixture.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * La costruzione di {@link MapAdapter} non viene completata. Il test non
     * esegue ulteriori verifiche sulla fixture.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * La costruzione con una sorgente {@code null} solleva
     * {@link NullPointerException}.
     * </p>
     */
    @Test(expected = NullPointerException.class)
    public void copyConstructorRejectsNullMap() {
        new MapAdapter((HMap) null);
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica riflessività, simmetria e transitività di {@code equals} per
     * mappe con gli stessi mapping.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Tre mappe distinte vengono popolate con lo stesso contenuto; nella seconda
     * l'ordine di inserimento è invertito. La scelta controlla sia le proprietà
     * generali di {@code equals} sia l'indipendenza dall'ordine non garantito da
     * {@code Hashtable}. Una sola istanza è sufficiente per la riflessività,
     * due direzioni di confronto sulla stessa coppia verificano la simmetria e la
     * terza istanza rende possibile controllare esplicitamente la transitività.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test crea {@code second} e {@code third}, quindi popola tutte e tre le
     * mappe con gli stessi due mapping, invertendo l'ordine in {@code second}.
     * Confronta la fixture con se stessa, la fixture e {@code second} in entrambe
     * le direzioni, poi {@code second} con {@code third} e infine la fixture con
     * {@code third}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * JUnit ha creato la fixture vuota e il test crea altre due mappe vuote; prima
     * dei confronti, le tre istanze contengono {@code "a"="1"} e
     * {@code "b"="2"}, pur essendo state popolate in sequenze diverse.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto finale delle mappe; le
     * asserzioni riguardano soltanto i risultati restituiti da {@code equals}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * Restituiscono {@code true} i cinque confronti eseguiti: riflessività della
     * fixture, due direzioni tra fixture e {@code second}, uguaglianza tra
     * {@code second} e {@code third} e uguaglianza tra fixture e {@code third}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code equals} rifiuti contenuti differenti, {@code null} e
     * oggetti che non implementano {@link HMap}.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Le mappe confrontate hanno tutte dimensione {@code 1}, ma differiscono
     * prima per chiave e poi per valore. Mantenere uguale la dimensione impedisce
     * che un controllo preliminare sul numero di elementi nasconda errori nel
     * confronto dei mapping. Si aggiungono inoltre i casi di tipo incompatibile
     * e riferimento nullo. Gli ultimi due confronti controllano che
     * {@code map.equals(...)} restituisca {@code false} quando riceve una
     * stringa o un riferimento nullo. L'uso di quattro asserzioni separate permette
     * di attribuire
     * un eventuale fallimento alla specifica categoria di non uguaglianza.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test prepara nella fixture {@code "a"="1"}, in {@code other}
     * {@code "b"="1"} e in {@code sameKeyDifferentValue}
     * {@code "a"="different"}. Poi confronta la fixture, nell'ordine, con le due
     * mappe, con {@code null} e con la stringa {@code "not a map"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La fixture e le due mappe locali sono istanze distinte e valide; prima dei
     * confronti ciascuna contiene un solo mapping, mentre gli altri argomenti
     * sono un riferimento nullo e un oggetto di tipo {@link String}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto finale delle mappe; le
     * asserzioni riguardano soltanto i risultati restituiti da {@code equals}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * Restituiscono {@code false} tutti e quattro i confronti: quello con chiave
     * diversa, quello con valore diverso, quello con {@code null} e quello con
     * l'oggetto che non implementa {@link HMap}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica la formula usata per calcolare il codice hash della mappa.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Si usano due mapping formati da stringhe, i cui codici hash sono stabili e
     * possono essere combinati direttamente nel test. Il valore atteso viene
     * calcolato come somma dei codici delle entry, ciascuno ottenuto tramite XOR
     * tra chiave e valore, secondo il contratto di {@code Map}. Calcolare
     * l'oracolo nel test, senza riutilizzare {@code entrySet().hashCode()}, evita
     * che lo stesso eventuale errore dell'implementazione influenzi sia il valore
     * effettivo sia quello atteso.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Inserisce {@code "a"="1"} e {@code "b"="2"}, calcola esplicitamente il
     * risultato atteso applicando XOR a ogni coppia e sommando i due contributi;
     * infine confronta tale intero con il risultato di {@code map.hashCode()}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La fixture nasce vuota; prima del calcolo contiene due mapping con chiavi e
     * valori non nulli, i cui metodi {@code hashCode} sono quelli di
     * {@link String}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto della mappa; l'unica
     * asserzione riguarda il codice hash restituito.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * Il codice restituito coincide con
     * {@code ("a".hashCode() ^ "1".hashCode())
     * + ("b".hashCode() ^ "2".hashCode())}.
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
     * <p>
     * <b>Summary:</b>
     * Verifica la coerenza tra {@code equals} e {@code hashCode}.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Due istanze vengono popolate con gli stessi mapping in ordine opposto.
     * Prima si accerta la loro uguaglianza, poi si confrontano i codici hash:
     * questa sequenza verifica direttamente l'implicazione richiesta dal
     * contratto degli oggetti uguali. L'ordine invertito è stato scelto per non
     * rendere il risultato dipendente dalla sequenza di inserimento o
     * dall'ordine di enumerazione interno.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Inserisce le coppie {@code "a"="1"} e {@code "b"="2"} nelle due mappe
     * con ordine diverso. Verifica prima {@code assertEquals(map, other)} e poi
     * confronta gli interi restituiti dai due metodi {@code hashCode()}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La fixture e {@code other} sono istanze distinte; prima delle asserzioni
     * contengono gli stessi due mapping, inseriti in sequenza opposta.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto finale delle due mappe; le
     * asserzioni riguardano la loro uguaglianza e i codici hash restituiti.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
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
     * <p>
     * <b>Summary:</b>
     * Verifica che la rappresentazione testuale includa le stringhe
     * {@code "a=1"} e {@code "b=2"} relative ai due mapping inseriti.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il test cerca separatamente le due sottostringhe {@code "a=1"} e
     * {@code "b=2"}, senza confrontare l'intera stringa. Questa scelta evita di
     * imporre un ordine che {@code Hashtable} non garantisce e concentra la
     * verifica sulle due associazioni controllate dal test. Due mapping distinti
     * sono stati scelti per controllare che entrambe le coppie compaiano nella
     * rappresentazione. {@code indexOf >= 0} controlla la presenza delle due
     * coppie senza imporre parentesi, posizione o separatori tra i diversi
     * mapping; il test richiede invece il formato {@code chiave=valore}.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Il test inserisce {@code "a"="1"} e {@code "b"="2"}, salva il risultato di
     * {@code toString()} in {@code representation} e applica separatamente
     * {@code indexOf} alle sottostringhe {@code "a=1"} e {@code "b=2"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La fixture nasce vuota; prima della conversione contiene esattamente i due
     * mapping non nulli {@code "a"="1"} e {@code "b"="2"}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto della mappa; le asserzioni
     * riguardano soltanto la stringa restituita da {@code toString()}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * La rappresentazione contiene sia {@code "a=1"} sia {@code "b=2"}, in
     * qualunque ordine; per entrambe le ricerche {@code indexOf} restituisce un
     * indice maggiore o uguale a zero.
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
     * <p>
     * Nei test il probe riconosce il riferimento registrato, mentre l'oggetto
     * registrato non riconosce il probe. Le due direzioni del confronto
     * producono quindi risultati diversi e rendono osservabile quale operando
     * riceve la chiamata a {@code equals} in {@code containsValue}.
     * </p>
     */
    private static final class AsymmetricEqualsProbe {
        /** Riferimento che deve essere riconosciuto dal confronto. */
        private final Object match;

        /**
         * Crea un probe associato al riferimento atteso.
         *
         * @param expected riferimento non nullo che il probe deve riconoscere
         *                 per identità
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
         * Restituisce il codice hash dell'oggetto riconosciuto. Nei test il
         * riferimento registrato è sempre non nullo; il probe e tale riferimento
         * producono quindi lo stesso codice hash.
         *
         * @return codice hash del riferimento memorizzato in {@link #match}
         */
        public int hashCode() {
            return match.hashCode();
        }
    }

}
