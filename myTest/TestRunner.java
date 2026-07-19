package myTest;

import java.util.List;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Result result = JUnitCore.runClasses(
                MapAdapterTest.class,
                ViewsTest.class,
                IteratorTest.class,
                EntryTest.class);
        long elapsed = System.currentTimeMillis() - start;

        List<Failure> failures = result.getFailures();
        int index;
        for (index = 0; index < failures.size(); index++) {
            Failure failure = failures.get(index);
            System.out.println("FALLIMENTO: " + failure.toString());
            System.out.println(failure.getTrace());
        }

        System.out.println("Test eseguiti: " + result.getRunCount());
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
