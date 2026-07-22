package myTest;

import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Punto di ingresso da riga di comando per l'intera suite JUnit del progetto.
 *
 * <p>
 * Il runner esegue insieme i test della mappa, delle viste, degli iteratori e
 * delle entry. Al termine stampa un riepilogo leggibile con test eseguiti,
 * superati, falliti e ignorati; per ogni fallimento mostra anche lo stack
 * trace, così è possibile individuare il test responsabile senza usare un IDE.
 * </p>
 *
 * <p>
 * Le classi sono elencate esplicitamente per rendere evidente la composizione
 * della suite richiesta dalla consegna. Il tempo viene misurato attorno alla
 * sola esecuzione di JUnit, mentre il numero dei test superati esclude sia i
 * fallimenti sia gli assumption failure. Un risultato non riuscito termina il
 * processo con codice {@code 1}, scelta utile negli script di validazione.
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
     * Esegue le quattro classi di test e stampa il resoconto complessivo.
     * Gli argomenti della riga di comando non sono utilizzati, perché la suite
     * è definita direttamente tramite {@link JUnitCore#runClasses(Class...)}.
     *
     * @param args argomenti della riga di comando, ignorati dal runner
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
