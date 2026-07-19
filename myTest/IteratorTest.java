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

public class IteratorTest {
    private HMap map;

    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
    }

    @Test
    public void emptyIteratorHasNoNextElement() {
        HIterator iterator = new MapAdapter().keySet().iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void keyIteratorVisitsEverySnapshotKeyOnce() {
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

    @Test(expected = NoSuchElementException.class)
    public void nextOnEmptyIteratorThrowsNoSuchElementException() {
        new MapAdapter().values().iterator().next();
    }

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

    @Test
    public void removeAfterNextDeletesLastReturnedMapping() {
        HIterator iterator = map.keySet().iterator();
        Object key = iterator.next();
        iterator.remove();
        assertFalse(map.containsKey(key));
        assertEquals(2, map.size());
        assertFalse(map.keySet().contains(key));
    }

    @Test
    public void hasNextBetweenNextAndRemoveKeepsRemoveLegal() {
        HIterator iterator = map.keySet().iterator();
        Object key = iterator.next();
        iterator.hasNext();
        iterator.remove();
        assertFalse(map.containsKey(key));
        assertEquals(2, map.size());
    }

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

    @Test
    public void independentIteratorsTraverseTheirOwnSnapshots() {
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
