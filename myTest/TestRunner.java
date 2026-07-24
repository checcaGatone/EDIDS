package myTest;

import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * 
 * <p>
 * Le classi sono passate esplicitamente a
 * {@link JUnitCore#runClasses(Class...)}
 * per rendere verificabile la suddivisione .
 * </p>
 *
 * <p>
 * <b>Expected Results </b>
 * Il tempo è misurato immediatamente prima e dopo la sola chiamata a JUnit. Per
 * ogni fallimento vengono stampati i messaggi di errore e la descrizione
 * fornita da JUnit . Seguono il numero complessivo dei
 * test eseguiti, quello dei test superati, falliti e ignorati, il tempo in
 * millisecondi e l'esito finale {@code SUCCESSO} oppure {@code FALLIMENTO}.
 * Se {@link Result#wasSuccessful()}
 * restituisce {@code false}, il processo termina con codice {@code 1}; in caso
 * di successo termina normalmente con codice {@code 0}.
 * </p>
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
     * resoconto.
     * La composizione è definita direttamente
     * tramite {@link JUnitCore#runClasses(Class...)}.
     *
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
