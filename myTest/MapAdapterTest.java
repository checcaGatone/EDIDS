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
 * <p><b>Summary:</b>
 * La suite comprende ventotto metodi di test e verifica le principali
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
 * <p><b>Test Case Design:</b>
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
 * produrre un'eccezione si controlla anche che la mappa non sia stata modificata
 * solo in parte.
 *
 * Per verificare la direzione del confronto effettuato da {@code equals()} viene
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
     * <p>La reinizializzazione fa sì che ogni caso parta da uno stato noto
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
     * Il caso controlla direttamente lo stato prodotto dal costruttore senza
     * argomenti. Si confrontano {@code size()} e {@code isEmpty()} poichè descrivono lo stesso
     * stato con due tipi di risultato diversi e devono essere coerenti.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Dopo la preparazione della condizione iniziale, il test invoca prima {@code size()} e
     * ne confronta il risultato con zero; invoca poi {@code isEmpty()} e ne
     * verifica il valore booleano.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * JUnit ha eseguito {@link #setUp()}; {@link #map} riferendo quindi un nuovo
     * {@link MapAdapter} pulito evalido.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non esegue operazioni mutative; al termine delle due letture la
     * mappa risulta ancora vuota.
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
     * Il mapping {@code "a"="1"} usa dati non nulli, brevi e distinti, così il
     * caso esercita il normale percorso di inserimento senza coinvolgere i casi
     * di errore. Sono stati scelti quattro punti di osservazione: il valore
     * restituito da {@code put} distingue un nuovo inserimento da una
     * sostituzione; {@code get} verifica l'associazione; {@code size} rileva
     * inserimenti mancanti o duplicati; {@code isEmpty} conferma che, dopo
     * l'inserimento, la mappa non è più vuota.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test inserisce {@code "a"="1"} e verifica immediatamente il valore
     * restituito da {@code put}. Successivamente legge la chiave {@code "a"},
     * misura la dimensione della mappa e ne interroga lo stato di vuoto.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * JUnit ha creato tramite {@link #setUp()} una mappa vuota; la chiave
     * {@code "a"} e il valore {@code "1"} sono entrambi non nulli.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La fixture contiene un solo mapping, nel quale la chiave
     * {@code "a"} è associata al valore {@code "1"}.
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
     * Due chiamate a {@code put} usano la stessa chiave e due valori distinguibili.
     * Questo caso è necessario perché il normale inserimento non copre il
     * comportamento in presenza di una chiave già registrata. Il ritorno della
     * seconda chiamata verifica la disponibilità del valore precedente, mentre
     * lettura e dimensione distinguono una sostituzione corretta dall'aggiunta
     * accidentale di un secondo mapping.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Per prima cosa il test inserisce {@code "a"="1"}. Quindi invoca di nuovo
     * {@code put} con la stessa chiave e il valore {@code "2"}, confronta il
     * ritorno con {@code "1"}, legge il valore corrente e controlla la dimensione.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * All'avvio la fixture è vuota; prima della seconda chiamata a {@code put},
     * il test inserisce il solo mapping {@code "a"="1"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La chiave {@code "a"} è associata a {@code "2"} e la dimensione resta
     * pari a {@code 1}.
     * </p>
     *
     * <p><b>Expected Results:</b>
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
     * <p><b>Summary:</b>
     * Verifica che {@code put} rifiuti una chiave {@code null}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Il valore {@code "1"} è valido e soltanto la chiave è nulla, così
     * l'eventuale errore è attribuibile senza ambiguità alla chiave. Il blocco
     * {@code try/catch}, anziché la sola proprietà {@code expected} di JUnit,
     * permette di eseguire dopo l'eccezione l'asserzione sullo stato e di
     * escludere un inserimento parziale.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test tenta {@code put(null, "1")} nel blocco {@code try}. Se la chiamata
     * termina normalmente esegue {@code fail()}; se riceve una
     * {@link NullPointerException}, entra nel blocco {@code catch} e verifica che
     * la fixture sia ancora vuota.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * {@link #setUp()} ha prodotto una fixture vuota; il riferimento usato come
     * chiave è {@code null}, mentre il valore {@code "1"} è valido e non nullo.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Nessun mapping viene inserito.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code put(null, "1")} solleva {@link NullPointerException} e
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
     * <p><b>Summary:</b>
     * Verifica che {@code put} rifiuti un valore {@code null}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La chiave {@code "a"} è valida e soltanto il valore è nullo: il dato è
     * stato scelto per isolare il vincolo sui valori da quello sulle chiavi, già
     * coperto dal test precedente. La gestione esplicita dell'eccezione consente
     * di controllare anche che l'operazione non lasci un mapping incompleto.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test invoca {@code put("a", null)} in un blocco {@code try}. Un ritorno
     * normale provoca {@code fail()}; dopo l'intercettazione di una
     * {@link NullPointerException}, viene verificato lo stato di vuoto.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture predisposta da {@link #setUp()} è vuota; la chiave
     * {@code "a"} è non nulla, mentre il riferimento usato come valore è
     * {@code null}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La mappa non contiene alcun mapping.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code put("a", null)} solleva {@link NullPointerException} e
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
     * <p><b>Summary:</b>
     * Verifica il risultato di {@code get} per una chiave assente.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * La ricerca viene eseguita su una mappa vuota usando la chiave non nulla
     * {@code "missing"}. Questa combinazione separa il normale caso di chiave
     * assente dal caso eccezionale della chiave {@code null}. Il successivo
     * controllo di {@code isEmpty()} conferma che una ricerca senza
     * corrispondenza non inserisca implicitamente un mapping.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test chiama {@code get("missing")} e confronta il risultato con
     * {@code null}; subito dopo invoca {@code isEmpty()} sulla stessa fixture.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * {@link #setUp()} ha creato una mappa senza mapping e la chiave di ricerca
     * {@code "missing"} è un riferimento valido e non nullo.
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
     * La chiave {@code "key"} e il valore {@code "value"} sono intenzionalmente
     * distinti, poi entrambi vengono usati come argomenti di
     * {@code containsKey}. Il caso è stato scelto per rilevare
     * un'implementazione che cerchi per errore fra i valori: verificare soltanto
     * la chiave presente non sarebbe sufficiente a escludere tale difetto.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test inserisce {@code "key"="value"}. Quindi passa prima
     * {@code "key"} e poi {@code "value"} a {@code containsKey}, confrontando i
     * due risultati rispettivamente con {@code true} e {@code false}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture nasce vuota; prima delle ricerche il test vi inserisce un solo
     * mapping con chiave e valore non nulli e differenti.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non verifica una post-condizione sul contenuto della mappa; le
     * asserzioni riguardano soltanto i valori booleani restituiti dalle due
     * ricerche.
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
     * Si riutilizzano come argomenti sia il valore sia la chiave del mapping,
     * scelti diversi tra loro. La coppia di asserzioni positiva e negativa rende
     * osservabile che la ricerca opera sulla componente valore; la sola ricerca
     * positiva non escluderebbe una scansione indiscriminata di chiavi e valori.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test inserisce {@code "key"="value"}, chiama
     * {@code containsValue("value")} e successivamente
     * {@code containsValue("key")}, verificando entrambi i risultati.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è inizialmente vuota; prima delle ricerche viene popolata con
     * un mapping la cui chiave e il cui valore sono distinti e non nulli.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non verifica una post-condizione sul contenuto della mappa; le
     * asserzioni riguardano soltanto i valori booleani restituiti dalle due
     * ricerche.
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
     * oggetto. Il confronto è intenzionalmente asimmetrico: il risultato può
     * essere positivo solo chiamando {@code equals} sull'argomento cercato, come
     * richiede il contratto J2SE 1.4.2. Il test è stato scelto perché due
     * stringhe, avendo un'uguaglianza simmetrica, non permetterebbero di rilevare
     * l'inversione degli operandi. Dimensione e identità completano il controllo
     * escludendo alterazioni durante la ricerca.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test crea l'oggetto {@code stored}, costruisce {@code searched} come
     * probe che riconosce quel riferimento e inserisce {@code stored} con chiave
     * {@code "key"}. Poi esegue la ricerca, misura la dimensione e confronta per
     * identità il valore ancora associato alla chiave.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è stata inizializzata vuota; prima della ricerca contiene un
     * solo mapping il cui valore è il riferimento riconosciuto dal probe usato
     * come argomento di {@code containsValue}.
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
     * contenuto nella mappa, il test produrrebbe un falso positivo. Questo caso
     * negativo è stato affiancato al precedente perché, insieme, i due casi
     * discriminano entrambe le direzioni del confronto. Le asserzioni su
     * dimensione e identità verificano inoltre che la ricerca sia non mutativa.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test crea prima {@code searched} e poi il probe {@code stored} che lo
     * riconosce, memorizza il probe con chiave {@code "key"} ed esegue
     * {@code containsValue(searched)}. Infine controlla dimensione e identità
     * del valore recuperato dalla mappa.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è inizialmente vuota; prima della ricerca contiene il probe
     * asimmetrico come unico valore, mentre l'argomento cercato è un oggetto
     * distinto e non nullo.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La dimensione e il riferimento associato a {@code "key"} rimangono
     * invariati.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code containsValue(searched)} restituisce {@code false}; la
     * corrispondenza ottenibile solo con il confronto inverso viene ignorata, la
     * dimensione resta {@code 1} e {@code get("key")} restituisce lo stesso
     * riferimento memorizzato.
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
     * La mappa viene prima popolata con un mapping valido. Oltre alla categoria
     * dell'eccezione, vengono quindi controllati dimensione e valore. Il caso è
     * stato scelto per verificare il vincolo specifico di {@code Hashtable} sui
     * valori nulli; l'uso di una mappa non vuota permette anche di osservare che
     * la ricerca eccezionale non produca modifiche collaterali.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "key"="value"}, invoca {@code containsValue(null)} e
     * intercetta l'eccezione attesa. Un ritorno normale provoca {@code fail()};
     * nel blocco {@code catch} il test controlla dimensione e valore del mapping
     * preparato.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * JUnit ha predisposto una fixture vuota; prima della chiamata sottoposta
     * al test viene inserito un mapping valido e l'argomento di ricerca è
     * {@code null}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping iniziale rimane presente e invariato.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code containsValue(null)} solleva {@link NullPointerException}; la
     * dimensione resta {@code 1} e {@code get("key")} restituisce
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
     * <p><b>Summary:</b>
     * Verifica che {@code get} rifiuti una chiave {@code null} senza modificare
     * la mappa.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si parte da una mappa non vuota per poter osservare esplicitamente
     * l'assenza di effetti collaterali dopo l'errore, oltre a controllare il tipo
     * dell'eccezione. La chiave valida {@code "a"} e quella nulla distinguono
     * una lettura ordinaria dal caso non ammesso da {@code Hashtable}.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"}, invoca {@code get(null)} e verifica il
     * contenuto dopo aver intercettato l'eccezione. Se la chiamata termina senza
     * eccezioni, il test esegue {@code fail()}; nel blocco {@code catch} misura la
     * dimensione e rilegge la chiave valida.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è creata vuota; prima di {@code get(null)} il test vi inserisce
     * l'unico mapping valido {@code "a"="1"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il mapping {@code "a"="1"} è ancora presente.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code get(null)} solleva {@link NullPointerException}; la dimensione
     * vale ancora {@code 1} e
     * {@code get("a")} restituisce {@code "1"}.
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
     * della mappa. Il caso è separato da {@code get(null)} perché {@code get}
     * e {@code containsKey} sono metodi distinti e ciascuno deve gestire
     * correttamente una chiave nulla.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Dopo aver inserito {@code "a"="1"}, invoca {@code containsKey(null)} e
     * controlla nuovamente dimensione e valore associato. {@code fail()} rende
     * esplicito il fallimento in caso di ritorno normale.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * JUnit ha creato una mappa vuota; prima della ricerca il test stabilisce il
     * mapping {@code "a"="1"} e usa {@code null} come chiave da cercare.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Lo stato della fixture coincide con quello precedente alla ricerca.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code containsKey(null)} solleva {@link NullPointerException};
     * {@code size()} restituisce {@code 1} e
     * {@code get("a")} restituisce {@code "1"}.
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
     * La mappa viene popolata prima della chiamata, così è possibile controllare
     * che l'eccezione non provochi la rimozione del mapping già presente. Il caso
     * è mantenuto distinto dalle
     * ricerche con chiave nulla perché {@code remove} è un'operazione mutativa e
     * richiede una verifica esplicita contro la perdita di dati.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"}, chiama {@code remove(null)} e verifica il
     * mapping dopo la gestione dell'eccezione. Se non viene lanciata alcuna
     * eccezione, {@code fail()} interrompe il test; nel blocco {@code catch}
     * vengono controllati dimensione e valore.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è inizialmente vuota; prima della rimozione il test vi inserisce
     * l'unico mapping valido {@code "a"="1"} e prepara una chiave nulla.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Nessun mapping è stato rimosso.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code remove(null)} solleva {@link NullPointerException}; la dimensione
     * resta {@code 1} e il valore
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
     * una rimozione effettiva dalla sola restituzione del valore precedente. Si
     * parte da un unico mapping per rendere deterministica la post-condizione di
     * mappa vuota e collegarla direttamente alla rimozione eseguita.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test inserisce {@code "a"="1"}, invoca {@code remove("a")} e confronta
     * il valore restituito con {@code "1"}. Poi verifica che la chiave non sia più
     * contenuta e che la mappa risulti vuota.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture nasce vuota; immediatamente prima di {@code remove} contiene
     * soltanto il mapping non nullo {@code "a"="1"}.
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
     * valore di ritorno, la dimensione e il mapping esistente per controllare che
     * l'operazione non produca effetti collaterali. Il caso è necessario come
     * complemento alla rimozione presente: un'implementazione potrebbe gestire
     * correttamente la chiave trovata ma alterare lo stato quando la chiave manca.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test inserisce {@code "a"="1"}, invoca {@code remove("b")} e confronta
     * il ritorno con {@code null}. In seguito misura la dimensione e legge il
     * mapping originario.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture è stata creata vuota; prima della chiamata contiene soltanto la
     * chiave {@code "a"}, mentre la chiave non nulla {@code "b"} è assente.
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
     * entrambe le chiavi confermano lo svuotamento completo. Questo caso è stato
     * scelto separatamente da {@code remove}, poiché {@code clear} deve operare
     * sull'intero contenuto senza ricevere una chiave specifica.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"} e {@code "b"="2"}, quindi invoca
     * {@code clear()}. Dopo lo svuotamento misura la dimensione e cerca
     * separatamente entrambe le chiavi iniziali.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture nasce vuota; prima di {@code clear()} il test vi prepara due
     * mapping distinti con chiavi e valori non nulli.
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
     * già presente soltanto nella destinazione. Le tre asserzioni sui valori,
     * insieme alla dimensione, sono state scelte per rendere osservabili tutte le
     * categorie di chiavi coinvolte e non soltanto il numero finale di mapping.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test crea la sorgente e vi inserisce {@code "a"="1"} e
     * {@code "b"="2"}; prepara poi la fixture con {@code "a"="old"} e
     * {@code "c"="3"}. Dopo {@code map.putAll(source)} controlla la dimensione
     * e legge, una per una, le tre chiavi attese nella destinazione.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * JUnit ha creato una destinazione vuota; prima di {@code putAll}, il test ha
     * predisposto due mappe valide e non nulle, con la chiave {@code "a"}
     * presente in entrambe e con una chiave esclusiva per ciascuna.
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
     * dichiarata nell'annotazione JUnit. Il caso è stato scelto per distinguere
     * una sorgente inesistente da una sorgente valida ma vuota, verificata in un
     * test separato. L'annotazione {@code expected} è sufficiente perché questo
     * metodo intende osservare soltanto il rifiuto dell'argomento, non lo stato
     * successivo della destinazione.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Dopo l'esecuzione automatica di {@link #setUp()}, il test passa direttamente
     * {@code null} a {@code putAll}; JUnit confronta l'eccezione che termina il
     * metodo con quella dichiarata nell'annotazione {@link Test}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La destinazione è una mappa valida e vuota; il riferimento fornito al
     * posto della sorgente vale {@code null}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test controlla soltanto l'eccezione prodotta dall'argomento nullo e non
     * esegue ulteriori verifiche sul contenuto della destinazione.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * {@code putAll(null)} solleva {@link NullPointerException}.
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
     * sostituzioni indesiderate. Sono stati scelti due mapping, invece di uno,
     * per esercitare più di un passo dell'attraversamento senza introdurre
     * dipendenze dall'ordine di enumerazione.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test inserisce {@code "a"="1"} e {@code "b"="2"}, passa la fixture
     * stessa come argomento di {@code putAll}, quindi misura la dimensione e
     * rilegge separatamente entrambi i valori.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture nasce vuota; prima dell'operazione la stessa istanza, usata come
     * sorgente e destinazione, contiene {@code "a"="1"} e {@code "b"="2"}.
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
     * sorgente {@code null}. L'ultima asserzione controlla inoltre che
     * {@code equals} consideri uguali due mappe vuote distinte.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test crea una sorgente vuota, la passa a {@code map.putAll(empty)} e
     * verifica che la fixture sia vuota. Costruisce poi un nuovo
     * {@link MapAdapter} dalla stessa sorgente, ne controlla lo stato di vuoto e
     * infine confronta sorgente e copia con {@code equals}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * {@link #setUp()} ha creato una destinazione vuota e il test crea una seconda
     * istanza valida, anch'essa vuota, da usare come sorgente comune alle due
     * operazioni di copia.
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
     * Verifica l'uguaglianza iniziale della copia e, dopo una chiamata a
     * {@code remove("a")} sulla sorgente, controlla nella copia la presenza
     * della chiave {@code "a"} e la dimensione.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Prima si confrontano sorgente e copia, poi si invoca {@code remove("a")}
     * soltanto sulla sorgente. Questa seconda fase permette di controllare che
     * nella copia la chiave {@code "a"} sia ancora presente e la dimensione
     * valga due. Il test non richiede una copia profonda di chiavi e valori e
     * non rilegge il mapping {@code "b"} dopo la modifica della sorgente.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test inserisce due mapping nella fixture, costruisce {@code copy} dalla
     * fixture e verifica l'uguaglianza iniziale. Poi invoca
     * {@code map.remove("a")} e controlla che nella copia la chiave sia ancora
     * presente e la dimensione sia ancora pari a due.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture nasce vuota; prima della costruzione viene usata come sorgente e
     * contiene i due mapping distinti {@code "a"="1"} e {@code "b"="2"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Dopo {@code map.remove("a")}, {@code copy.containsKey("a")} restituisce
     * {@code true} e {@code copy.size()} restituisce {@code 2}. Il test non
     * ricontrolla il contenuto finale della sorgente né il valore della chiave
     * {@code "b"} nella copia.
     * </p>
     *
     * <p><b>Expected Results:</b>
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
     * <p><b>Summary:</b>
     * Verifica che il costruttore di copia rifiuti una sorgente {@code null}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Il cast esplicito a {@link HMap} seleziona senza ambiguità il costruttore
     * di copia. L'eccezione è dichiarata nell'annotazione JUnit perché non è
     * necessario eseguire altre verifiche dopo il tentativo di costruzione. Il
     * caso è distinto dalla copia di una mappa vuota per controllare che il
     * costruttore riconosca la differenza tra una sorgente valida priva di
     * mapping e una sorgente {@code null}.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test valuta l'espressione {@code new MapAdapter((HMap) null)}; il cast
     * forza la selezione del costruttore di copia e JUnit confronta l'eccezione
     * risultante con il tipo dichiarato nell'annotazione.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * Il riferimento fornito come sorgente di tipo {@link HMap} vale
     * {@code null}; non è richiesto alcuno stato della fixture.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * La costruzione di {@link MapAdapter} non viene completata. Il test non
     * esegue ulteriori verifiche sulla fixture.
     * </p>
     *
     * <p><b>Expected Results:</b>
     * La costruzione con una sorgente {@code null} solleva
     * {@link NullPointerException}.
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
     * {@code Hashtable}. Una sola istanza è sufficiente per la riflessività,
     * due direzioni di confronto sulla stessa coppia verificano la simmetria e la
     * terza istanza rende possibile controllare esplicitamente la transitività.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test crea {@code second} e {@code third}, quindi popola tutte e tre le
     * mappe con gli stessi due mapping, invertendo l'ordine in {@code second}.
     * Confronta la fixture con se stessa, la fixture e {@code second} in entrambe
     * le direzioni, poi {@code second} con {@code third} e infine la fixture con
     * {@code third}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * JUnit ha creato la fixture vuota e il test crea altre due mappe vuote; prima
     * dei confronti, le tre istanze contengono {@code "a"="1"} e
     * {@code "b"="2"}, pur essendo state popolate in sequenze diverse.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto finale delle mappe; le
     * asserzioni riguardano soltanto i risultati restituiti da {@code equals}.
     * </p>
     *
     * <p><b>Expected Results:</b>
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
     * e riferimento nullo. Gli ultimi due confronti controllano che
     * {@code map.equals(...)} restituisca {@code false} quando riceve una
     * stringa o un riferimento nullo. L'uso di quattro asserzioni separate permette di attribuire
     * un eventuale fallimento alla specifica categoria di non uguaglianza.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Il test prepara nella fixture {@code "a"="1"}, in {@code other}
     * {@code "b"="1"} e in {@code sameKeyDifferentValue}
     * {@code "a"="different"}. Poi confronta la fixture, nell'ordine, con le due
     * mappe, con {@code null} e con la stringa {@code "not a map"}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture e le due mappe locali sono istanze distinte e valide; prima dei
     * confronti ciascuna contiene un solo mapping, mentre gli altri argomenti
     * sono un riferimento nullo e un oggetto di tipo {@link String}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto finale delle mappe; le
     * asserzioni riguardano soltanto i risultati restituiti da {@code equals}.
     * </p>
     *
     * <p><b>Expected Results:</b>
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
     * <p><b>Summary:</b>
     * Verifica la formula usata per calcolare il codice hash della mappa.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Si usano due mapping formati da stringhe, i cui codici hash sono stabili e
     * possono essere combinati direttamente nel test. Il valore atteso viene
     * calcolato come somma dei codici delle entry, ciascuno ottenuto tramite XOR
     * tra chiave e valore, secondo il contratto di {@code Map}. Calcolare
     * l'oracolo nel test, senza riutilizzare {@code entrySet().hashCode()}, evita
     * che lo stesso eventuale errore dell'implementazione influenzi sia il valore
     * effettivo sia quello atteso.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce {@code "a"="1"} e {@code "b"="2"}, calcola esplicitamente il
     * risultato atteso applicando XOR a ogni coppia e sommando i due contributi;
     * infine confronta tale intero con il risultato di {@code map.hashCode()}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture nasce vuota; prima del calcolo contiene due mapping con chiavi e
     * valori non nulli, i cui metodi {@code hashCode} sono quelli di
     * {@link String}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto della mappa; l'unica
     * asserzione riguarda il codice hash restituito.
     * </p>
     *
     * <p><b>Expected Results:</b>
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
     * <p><b>Summary:</b>
     * Verifica la coerenza tra {@code equals} e {@code hashCode}.
     * </p>
     *
     * <p><b>Test Case Design:</b>
     * Due istanze vengono popolate con gli stessi mapping in ordine opposto.
     * Prima si accerta la loro uguaglianza, poi si confrontano i codici hash:
     * questa sequenza verifica direttamente l'implicazione richiesta dal
     * contratto degli oggetti uguali. L'ordine invertito è stato scelto per non
     * rendere il risultato dipendente dalla sequenza di inserimento o
     * dall'ordine di enumerazione interno.
     * </p>
     *
     * <p><b>Test Description:</b>
     * Inserisce le coppie {@code "a"="1"} e {@code "b"="2"} nelle due mappe
     * con ordine diverso. Verifica prima {@code assertEquals(map, other)} e poi
     * confronta gli interi restituiti dai due metodi {@code hashCode()}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture e {@code other} sono istanze distinte; prima delle asserzioni
     * contengono gli stessi due mapping, inseriti in sequenza opposta.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto finale delle due mappe; le
     * asserzioni riguardano la loro uguaglianza e i codici hash restituiti.
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
     * Verifica che la rappresentazione testuale includa le stringhe
     * {@code "a=1"} e {@code "b=2"} relative ai due mapping inseriti.
     * </p>
     *
     * <p><b>Test Case Design:</b>
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
     * <p><b>Test Description:</b>
     * Il test inserisce {@code "a"="1"} e {@code "b"="2"}, salva il risultato di
     * {@code toString()} in {@code representation} e applica separatamente
     * {@code indexOf} alle sottostringhe {@code "a=1"} e {@code "b=2"}.
     * </p>
     *
     * <p><b>Pre-Condition:</b>
     * La fixture nasce vuota; prima della conversione contiene esattamente i due
     * mapping non nulli {@code "a"="1"} e {@code "b"="2"}.
     * </p>
     *
     * <p><b>Post-Condition:</b>
     * Il test non verifica nuovamente il contenuto della mappa; le asserzioni
     * riguardano soltanto la stringa restituita da {@code toString()}.
     * </p>
     *
     * <p><b>Expected Results:</b>
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
     * <p>Nei test il probe riconosce il riferimento registrato, mentre l'oggetto
     * registrato non riconosce il probe. Le due direzioni del confronto
     * producono quindi risultati diversi e rendono osservabile quale operando
     * riceve la chiamata a {@code equals} in {@code containsValue}.</p>
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
