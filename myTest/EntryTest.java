package myTest;

import myAdapter.HMap;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case dedicato alle entry restituite da {@link MapAdapter#entrySet()}.
 *
 * <p>
 * <b>Summary:</b>
 * La classe verifica il comportamento di {@link HMap.Entry}: lettura della
 * chiave e valore, aggiornamento tramite {@code setValue()}, collegamento
 * con la mappa originale, uguaglianza, codice hash e rappresentazione testuale.
 * Viene controllato anche il rifiuto di valori {@code null}, coerente con il
 * vincolo imposto da {@code Hashtable}, che costituisce l'oggetto adattato. I
 * metodi coprono
 * lettura e rappresentazione dell'entry, modifica del valore e
 * propagazione alle viste, proprietà di {@code equals()} e {@code hashCode()},
 * comportamento dell'entry già ottenuta quando la mappa viene aggiornata.
 * </p>
 *
 * <p>
 * <b>Test Case Design:</b>
 * Ogni test usa una nuova {@link MapAdapter} contenente la sola associazione
 * {@code a=1}. La presenza di una sola coppia rende univoca l'entry ottenuta
 * dall'iteratore e permette di non fare assunzioni sull'ordine della
 * {@code Hashtable}. I valori sono stringhe semplici, così uguaglianza e hash
 * sono deterministici. Quando serve un confronto tra oggetti distinti viene
 * usata {@link EntryComp}, un'implementazione indipendente di
 * {@code HMap.Entry}.
 * Questa classe permette di rappresentare una
 * coppia attesa senza ottenere una seconda entry dalla mappa.
 * Nei test di {@code setValue()} non viene controllato soltanto il valore
 * restituito dall'entry. Il nuovo valore viene cercato anche nella mappa
 * tramite
 * {@code get()} e nelle viste restituite da {@code values()} ed
 * {@code entrySet()}. In questo modo si verifica che {@code setValue()}
 * aggiorni
 * realmente il mapping, invece di modificare soltanto un dato interno
 * dell'entry. Un test distinto modifica poi la mappa con {@code map.put()} e
 * controlla il valore attraverso un'entry ottenuta in precedenza. Questo test
 * documenta una scelta specifica di {@code MapAdapter}: a ogni chiamata a
 * {@code getValue()}, l'entry legge il valore corrente dalla mappa. Tale
 * comportamento non viene considerato un requisito generale di
 * {@code HMap.Entry}, ma una caratteristica dell'implementazione realizzata.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see MapAdapter
 * @see HMap.Entry
 */
public class EntryTest {
    /** Mappa ricreata prima di ogni test e alla quale l'entry è collegata. */
    private HMap map;

    /** Entry associata all'unico mapping {@code a=1} della fixture. */
    private HMap.Entry entry;

    /**
     * Prepara un set up indipendente con il mapping ({@code a=1}) e ne ricava
     * l'unica entry. Ricostruire entrambi gli oggetti prima di ogni test evita
     * che un aggiornamento eseguito da un metodo possa influenzare i successivi.
     */
    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        entry = (HMap.Entry) map.entrySet().iterator().next();
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica che i metodi di accesso espongano la chiave e il valore del
     * mapping rappresentato dall'entry.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Lo stato iniziale contiene la sola coppia {@code a=1}; di conseguenza l'entry
     * prelevata da {@code entrySet()} è determinata senza dipendere dall'ordine
     * della {@code Hashtable}. Le due asserzioni sono mantenute separate per
     * individuare in modo preciso un eventuale errore su chiave o valore.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Come primo passo invoca {@code getKey()} e confronta il risultato con
     * {@code "a"}. Successivamente invoca {@code getValue()} e confronta il
     * risultato con {@code "1"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * {@code map} contiene esattamente {@code a=1} ed {@code entry} è l'unica
     * entry ricavata dalla relativa vista.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test esegue soltanto le due letture; non ricontrolla separatamente il
     * contenuto della mappa al termine.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code getKey()} restituisce {@code "a"} e {@code getValue()} restituisce
     * {@code "1"}; entrambe le asserzioni devono essere soddisfatte.
     * </p>
     */
    @Test
    public void gettersReturnCurrentKeyAndValue() {
        assertEquals("a", entry.getKey());
        assertEquals("1", entry.getValue());
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica il valore di ritorno di {@code setValue()} e il valore
     * corrente esposto dall'entry dopo la sostituzione.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il nuovo valore {@code "2"} :
     * permette alla prima asserzione di riconoscere il valore precedente e
     * alla seconda di riconoscere quello nuovo. Controllare entrambi gli aspetti
     * evita che un'implementazione che aggiorna correttamente ma restituisce il
     * valore errato possa superare il test.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Per prima cosa chiama {@code entry.setValue("2")} e confronta il valore
     * restituito con {@code "1"}. In seguito legge nuovamente l'entry tramite
     * {@code getValue()} e confronta il risultato con {@code "2"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * L'entry è collegata all'unico mapping {@code a=1} della fixture.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * L'entry espone il nuovo valore {@code "2"}. Il test non legge
     * direttamente il contenuto della mappa dopo la sostituzione.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code setValue("2")} restituisce {@code "1"} e la successiva
     * {@code getValue()} restituisce {@code "2"}.
     * </p>
     */
    @Test
    public void setValueReturnsPreviousValue() {
        assertEquals("1", entry.setValue("2"));
        assertEquals("2", entry.getValue());
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code setValue()} aggiorni il mapping memorizzato nella mappa
     * e che l'aggiornamento sia osservabile dalle viste collegate.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il controllo non si limita al riferimento {@code entry}: il valore nuovo
     * viene osservato tramite {@code map.get()}, {@code values()} ed
     * {@code entrySet()}. Il valore iniziale {@code "1"} è unico, quindi la sua
     * assenza da {@code values()} esclude che il nuovo valore sia stato soltanto
     * affiancato. L'oggetto {@link EntryComp} consente di cercare la coppia attesa
     * senza ricavare dalla mappa una seconda entry dipendente dall'implementazione.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Prima conserva in {@code sameEntry} lo stesso riferimento della fixture e
     * imposta {@code "2"}. Poi controlla, nell'ordine, il valore letto con
     * {@code map.get("a")}, la presenza di {@code "2"} in {@code values()}, la
     * presenza di {@code a=2} in {@code entrySet()} e l'assenza del vecchio
     * valore {@code "1"} dalla vista dei valori.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa contiene soltanto {@code a=1} e {@code entry} è collegata a tale
     * mapping.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * {@code get("a")} restituisce {@code "2"}; la vista dei valori contiene
     * {@code "2"} ma non {@code "1"}, mentre la vista delle entry contiene
     * {@code a=2}. Il test non ricontrolla la dimensione della mappa.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code get("a")} restituisce {@code "2"}, {@code values()} contiene
     * {@code "2"} ma non {@code "1"}, ed {@code entrySet()} contiene un'entry
     * equivalente a {@code a=2}.
     * </p>
     */
    @Test
    public void setValueWritesThroughToMapAndViews() {
        HMap.Entry sameEntry = entry;
        sameEntry.setValue("2");
        assertEquals("2", map.get("a"));
        assertTrue(map.values().contains("2"));
        assertTrue(map.entrySet().contains(new EntryComp("a", "2")));
        assertFalse(map.values().contains("1"));
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica che l'entry rifiuti l'assegnazione di un valore {@code null} e
     * che il mapping valido preesistente non venga perso.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il test usa un mapping già presente anziché un oggetto isolato: dopo il
     * tentativo è così possibile verificare il tipo dell'eccezione e controllare
     * che {@code map.get("a")} restituisca ancora {@code "1"}. Il controllo è
     * rilevante perché
     * {@code MapAdapter} delega l'aggiornamento alla {@code Hashtable}, che non
     * accetta valori {@code null}.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Per prima cosa tenta {@code entry.setValue(null)}. Se la chiamata termina
     * normalmente esegue {@link org.junit.Assert#fail()}; se intercetta
     * {@link NullPointerException}, legge {@code map.get("a")} e lo confronta
     * con il valore iniziale {@code "1"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa contiene esclusivamente il mapping valido {@code a=1} e l'entry
     * è ancora collegata a esso.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Dopo l'eccezione {@code map.get("a")} restituisce ancora {@code "1"}.
     * Il test non richiama {@code entry.getValue()}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code setValue(null)} solleva {@link NullPointerException}; nel relativo
     * ramo di gestione {@code map.get("a")} restituisce ancora {@code "1"}.
     * </p>
     */
    @Test
    public void setValueRejectsNullThroughHashtable() {
        try {
            entry.setValue(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("1", map.get("a"));
        }
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica la simmetria di {@code equals()} e la coerenza dei codici hash tra
     * due
     * entry equivalenti ma implementate da classi diverse.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Viene creato un nuovo oggetto `EntryTest.EntryComp` che rappresenta lo stesso
     * mapping `a=1`, senza riutilizzare l’entry preparata nella fixture. In questo
     * modo si verifica che l’uguaglianza dipenda dalla coppia chiave-valore
     * rappresentata e non dal fatto che i due riferimenti indichino lo stesso
     * oggetto o appartengano alla stessa classe concreta. Il confronto viene
     * eseguito in entrambe le direzioni per controllare la simmetria
     * dell’uguaglianza. Infine, si verifica che due entry considerate uguali
     * abbiano anche lo stesso codice hash, come richiesto dal contratto tra
     * `equals()` e `hashCode()`.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Come prima cosa crea {@code other} come entry di confronto per {@code a=1}.
     * Verifica quindi {@code entry.equals(other)}, poi
     * {@code other.equals(entry)} ed infine
     * confronta i codici restituiti dai due {@code hashCode()}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La configurazione iniziale del test contiene il mapping {@code a=1} e
     * {@code entry} è collegata a
     * tale coppia chiave-valore.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Dopo la preparazione il test esegue soltanto confronti; non ricontrolla
     * separatamente il mapping del set up.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * Entrambi i confronti di uguaglianza restituiscono {@code true} e i due
     * codici hash risultano uguali.
     * </p>
     */
    @Test
    public void equalEntriesRepresentSameMappingSymmetrically() {
        HMap.Entry other = new EntryComp("a", "1");
        assertTrue(entry.equals(other));
        assertTrue(other.equals(entry));
        assertEquals(entry.hashCode(), other.hashCode());
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica la proprietà riflessiva dell'uguaglianza dell'entry.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il confronto usa esattamente lo stesso riferimento per isolare la
     * riflessività dalle verifiche su chiave, valore e classe concreta svolte
     * dagli altri test.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Invoca {@code entry.equals(entry)} usando l'entry preparata dalla fixture
     * sia come ricevente sia come argomento, quindi verifica il risultato con
     * {@code assertTrue}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * {@code entry} è un riferimento non nullo al mapping {@code a=1}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test esegue soltanto il confronto riflessivo; non ricontrolla il
     * mapping della fixture al termine.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code entry.equals(entry)} restituisce {@code true}.
     * </p>
     */
    @Test
    public void entryEqualityIsReflexive() {
        assertTrue(entry.equals(entry));
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica che {@code equals()} rifiuti le coppie differenti, {@code null} e
     * oggetti che non implementano {@link HMap.Entry}.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * I quattro dati separano altrettante cause di disuguaglianza: l'entry di
     * confronto {@code b=1} cambia soltanto la chiave, mentre l'entry
     * {@code a=2} cambia soltanto il valore; {@code null} copre il caso di un
     * argomento nullo e la stringa
     * {@code "a=1"} ha la stessa forma testuale ma un tipo non valido. Cambiare
     * una sola caratteristica alla volta consente di attribuire con precisione
     * un eventuale fallimento al controllo corrispondente.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Confronta in sequenza l'entry {@code a=1} con le entry di confronto
     * {@code b=1} e {@code a=2}, quindi con {@code null} e con la stringa
     * {@code "a=1"}; ciascun risultato viene passato a
     * {@code assertFalse}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * L'entry della fixture rappresenta {@code a=1} e non è stata modificata.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test esegue soltanto confronti; non ricontrolla l'entry o il mapping
     * originale al termine.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * Tutti e quattro i confronti restituiscono {@code false}.
     * </p>
     */
    @Test
    public void entryEqualityRejectsDifferentMappingsAndOtherObjects() {
        assertFalse(entry.equals(new EntryComp("b", "1")));
        assertFalse(entry.equals(new EntryComp("a", "2")));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("a=1"));
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica che il codice hash dell’entry sia ottenuto applicando l’operatore
     * XOR tra il codice hash della chiave e quello del valore
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Le stringhe note {@code "a"} e {@code "1"} consentono di costruire
     * indipendentemente il risultato atteso mediante l'operatore XOR ({@code ^}).
     * La prima asserzione
     * verifica direttamente la formula; la seconda usa un {@link EntryComp}
     * equivalente per controllare che due implementazioni dello stesso mapping
     * producano un hash coerente.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Calcola prima {@code "a".hashCode() ^ "1".hashCode()} e memorizza il
     * risultato. Lo confronta poi con {@code entry.hashCode()}; infine costruisce
     * direttamente un'entry di confronto {@code a=1} e confronta il relativo
     * hash con quello dell'entry della mappa.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * L'entry iniziale rappresenta il mapping non modificato {@code a=1}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Il test esegue soltanto calcoli e confronti; non ricontrolla il contenuto
     * della mappa al termine.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * L'hash dell'entry coincide sia con lo XOR calcolato sia con l'hash
     * dell'oggetto di confronto equivalente.
     * </p>
     */
    @Test
    public void hashCodeIsKeyHashXorValueHash() {
        int expected = "a".hashCode() ^ "1".hashCode();
        assertEquals(expected, entry.hashCode());
        assertEquals(new EntryComp("a", "1").hashCode(), entry.hashCode());
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica che uguaglianza e codice hash riflettano il valore corrente dopo
     * un aggiornamento effettuato tramite {@code setValue()}.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Il valore {@code "updated"} è diverso da quello iniziale e viene utilizzato
     * sia per calcolare il nuovo codice hash tramite XOR, sia per creare un’entry
     * equivalente da usare nel confronto. Le tre asserzioni permettono di
     * verificare separatamente che il codice hash sia aggiornato in base al nuovo
     * valore, che l’entry rispetti il criterio di uguaglianza e che due entry
     * uguali abbiano lo stesso codice hash.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * sostituisce {@code "1"} con {@code "updated"}. Calcola
     * quindi lo XOR tra gli hash della chiave e del nuovo valore e crea un'entry
     * di confronto {@code a=updated}. Infine confronta l'hash dell'entry con lo
     * XOR, verifica
     * l'uguaglianza con l'oggetto di confronto e confronta i due codici hash.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * La mappa contiene {@code a=1} e l'entry è collegata a tale mapping.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * L'entry e l'oggetto di confronto rappresentano la coppia
     * {@code a=updated}. Il test non legge direttamente la mappa dopo
     * {@code setValue()}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * Il nuovo hash coincide con la formula attesa, l'entry è uguale all'oggetto
     * di confronto e i rispettivi codici hash coincidono.
     * </p>
     */
    @Test
    public void hashCodeTracksValueUpdatedThroughSetValue() {
        entry.setValue("updated");
        int expected = "a".hashCode() ^ "updated".hashCode();
        HMap.Entry equivalent = new EntryComp("a", "updated");
        assertEquals(expected, entry.hashCode());
        assertTrue(entry.equals(equivalent));
        assertEquals(equivalent.hashCode(), entry.hashCode());
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica il formato testuale code chiave=valore e il suo aggiornamento
     * quando cambia il valore del mapping.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * La rappresentazione viene verificata sia nello stato iniziale sia dopo
     * {@code setValue("2")}. Il doppio controllo è stato scelto per verificare
     * contemporaneamente il separatore {@code =}, l'ordine chiave-valore e il
     * fatto che la seconda stringa usi il valore aggiornato, non quello
     * iniziale.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Prima invoca {@code toString()} su {@code a=1} e confronta la stringa con
     * {@code "a=1"}. Poi imposta {@code "2"} tramite l'entry, richiama
     * {@code toString()} e confronta il nuovo risultato con {@code "a=2"}.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * L'entry rappresenta inizialmente l'unico mapping {@code a=1}.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * L'entry espone {@code a=2} nella rappresentazione testuale. Il test non
     * legge direttamente il mapping dalla mappa dopo {@code setValue()}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * Le due chiamate a {@code toString()} restituiscono rispettivamente
     * {@code "a=1"} e {@code "a=2"}.
     * </p>
     */
    @Test
    public void toStringUsesKeyEqualsValueFormat() {
        assertEquals("a=1", entry.toString());
        entry.setValue("2");
        assertEquals("a=2", entry.toString());
    }

    /**
     * <p>
     * <b>Summary:</b>
     * Verifica che un'entry già ottenuta da {@link MapAdapter} legga il valore
     * sostituito successivamente attraverso la mappa.
     * </p>
     *
     * <p>
     * <b>Test Case Design:</b>
     * Lo stesso riferimento a {@code entry} viene conservato mentre
     * {@code map.put()} sostituisce il valore della sua chiave. Il test è stato
     * scelto per documentare la natura live della classe interna usata da
     * {@code MapAdapter}, il cui {@code getValue()} consulta la
     * {@code Hashtable} corrente. Si tratta di una scelta
     * dell'adapter e non di una garanzia generale attribuita a ogni
     * {@link HMap.Entry} dopo una modifica diretta della mappa.
     * </p>
     *
     * <p>
     * <b>Test Description:</b>
     * Per prima cosa esegue {@code map.put("a", "updated")} senza richiedere
     * una nuova entry. Successivamente invoca {@code getValue()} sul riferimento
     * preesistente e lo confronta con {@code "updated"}. Infine costruisce
     * un'entry {@link EntryComp} per {@code a=updated} e la confronta con la
     * stessa entry.
     * </p>
     *
     * <p>
     * <b>Pre-Condition:</b>
     * {@code entry} è stata ottenuta quando la mappa conteneva soltanto
     * {@code a=1} e non è stata rimossa dalla relativa vista.
     * </p>
     *
     * <p>
     * <b>Post-Condition:</b>
     * Dopo {@code map.put("a", "updated")}, il riferimento a {@code entry}
     * creato nella fixture espone il valore {@code "updated"}.
     * </p>
     *
     * <p>
     * <b>Expected Results:</b>
     * {@code getValue()} restituisce {@code "updated"}. Inoltre
     * {@code EntryComp.equals(entry)} restituisce {@code true}; questa seconda
     * asserzione controlla il confronto eseguito dall'oggetto atteso, non
     * costituisce una verifica separata di {@code MapEntry.equals()}.
     * </p>
     */
    @Test
    public void entryReadsCurrentValueAfterMapReplacement() {
        map.put("a", "updated");
        assertEquals("updated", entry.getValue());
        assertEquals(new EntryComp("a", "updated"), entry);
    }

    /**
     * Entry di confronto minima e indipendente che implementa
     * {@link HMap.Entry}. la classe serve a rappresentare una coppia
     * chiave-valore e a confrontarla con le entry prodotte dalla mappa.
     * Il riferimento alla chiave non viene sostituito, mentre il valore può
     * essere aggiornato localmente; l'oggetto non è collegato a una
     * {@link MapAdapter}. Questa indipendenza
     * evita che il risultato atteso sia costruito mediante la stessa
     * implementazione sottoposta al test.
     */
    private static final class EntryComp implements HMap.Entry {
        /** Riferimento alla chiave, assegnato nel costruttore e non sostituito. */
        private final Object key;

        /** Valore corrente del mapping rappresentato per il confronto. */
        private Object value;

        /**
         * Crea un'entry di confronto con la coppia indicata.
         *
         * @param entryKey   chiave da memorizzare
         * @param entryValue valore iniziale da associare alla chiave
         */
        private EntryComp(Object entryKey, Object entryValue) {
            key = entryKey;
            value = entryValue;
        }

        /**
         * Restituisce la chiave dell'entry di confronto.
         *
         * @return riferimento alla chiave del mapping
         */
        public Object getKey() {
            return key;
        }

        /**
         * Restituisce il valore corrente dell'entry di confronto.
         *
         * @return valore associato alla chiave
         */
        public Object getValue() {
            return value;
        }

        /**
         * Sostituisce localmente il valore dell'entry di confronto.
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
         * Confronta quest'entry con un'altra entry in base a chiave e valore.
         *
         * @param object oggetto da confrontare con quest'entry di confronto
         * @return {@code true} se l'oggetto è una {@code HMap.Entry} con la
         *         stessa chiave e lo stesso valore, {@code false} altrimenti
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
         * Calcola l'hash come XOR tra l'hash della chiave e quello del valore.
         *
         * @return codice hash coerente con {@link #equals(Object)}
         */
        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * Confronta due riferimenti gestendo esplicitamente il caso {@code null}.
         *
         * @param first  primo oggetto da confrontare
         * @param second secondo oggetto da confrontare
         * @return {@code true} se i riferimenti sono entrambi {@code null} o se
         *         il primo oggetto è uguale al secondo
         */
        private static boolean equal(Object first, Object second) {
            return first == null ? second == null : first.equals(second);
        }
    }
}
