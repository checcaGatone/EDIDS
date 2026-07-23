package myTest;

import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Punto di ingresso da riga di comando per l'intera suite JUnit del progetto.
 *
 * <p><b>Requisiti di esecuzione:</b>
 * Il progetto utilizza esplicitamente JUnit 4.13, distribuito nel file
 * {@code JUnit/junit-4.13.jar}, e Hamcrest Core 1.3, contenuto in
 * {@code JUnit/hamcrest-core-1.3.jar}. Dopo avere compilato i package
 * {@code myAdapter} e {@code myTest} nella directory {@code build}, in ambiente
 * Windows il runner può essere avviato dalla radice della consegna con
 * {@code java -cp "build;JUnit/junit-4.13.jar;JUnit/hamcrest-core-1.3.jar"
 * myTest.TestRunner}. Su sistemi che usano i due punti come separatore del
 * classpath occorre sostituire i punti e virgola con {@code :}. Non è richiesto
 * un IDE e non sono previsti argomenti applicativi.</p>
 *
 * <p><b>Composizione della suite:</b>
 * Le classi sono passate esplicitamente a {@link JUnitCore#runClasses(Class...)}
 * per rendere verificabile la suddivisione richiesta dalla consegna. La suite
 * comprende i 28 test di {@link MapAdapterTest}, i 50 test di {@link ViewsTest},
 * i 16 test di {@link IteratorTest} e gli 11 test di {@link EntryTest}, per un
 * totale attuale di 105 metodi di test. L'ordine dell'elenco stabilisce soltanto
 * la composizione del run e non crea dipendenze tra le fixture delle classi.</p>
 *
 * <p><b>Output prodotto:</b>
 * Il tempo è misurato immediatamente prima e dopo la sola chiamata a JUnit. Per
 * ogni fallimento vengono stampati la dicitura {@code FALLIMENTO}, la descrizione
 * fornita da JUnit e lo stack trace completo. Seguono il numero complessivo dei
 * test eseguiti, quello dei test superati, falliti e ignorati, il tempo in
 * millisecondi e l'esito finale {@code SUCCESSO} oppure {@code FALLIMENTO}. Il
 * conteggio dei test superati è calcolato sottraendo dal numero eseguito sia i
 * fallimenti sia gli assumption failure. Se {@link Result#wasSuccessful()}
 * restituisce {@code false}, il processo termina con codice {@code 1}; in caso
 * di successo termina normalmente con codice {@code 0}.</p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 * @see MapAdapterTest
 * @see ViewsTest
 * @see IteratorTest
 * @see EntryTest
 */
public class TestRunner {
    /**
     * Avvia tramite JUnit 4.13 le quattro classi della suite e stampa il
     * resoconto richiesto dalla consegna. Gli argomenti della riga di comando
     * non sono utilizzati, perché la composizione è definita direttamente
     * tramite {@link JUnitCore#runClasses(Class...)}.
     *
     * @param args eventuali argomenti della riga di comando; sono accettati dal
     *             punto di ingresso Java ma ignorati dal runner
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Result result = JUnitCore.runClasses(
                MapAdapterTest.class,
                ViewsTest.class,
                IteratorTest.class,
                EntryTest.class);
        long elapsed = System.currentTimeMillis() - start;

        List<Failure> failures = result.getFailures();
        int passed = result.getRunCount()
                - result.getFailureCount()
                - result.getAssumptionFailureCount();
        int index;
        for (index = 0; index < failures.size(); index++) {
            Failure failure = failures.get(index);
            System.out.println("FALLIMENTO: " + failure.toString());
            System.out.println(failure.getTrace());
        }

        System.out.println("Test eseguiti: " + result.getRunCount());
        System.out.println("Test superati: " + passed);
        System.out.println("Test falliti: " + result.getFailureCount());
        System.out.println("Test ignorati: " + result.getIgnoreCount());
        System.out.println("Tempo impiegato: " + elapsed + " ms");
        System.out.println("Risultato: "
                + (result.wasSuccessful() ? "SUCCESSO" : "FALLIMENTO"));

        if (!result.wasSuccessful()) {
            System.exit(1);
        }
    }
}
