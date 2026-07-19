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

public class ViewsTest {
    private HMap map;

    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        map.put("b", "2");
    }

    @Test
    public void repeatedViewRequestsReturnCachedInstances() {
        assertSame(map.keySet(), map.keySet());
        assertSame(map.values(), map.values());
        assertSame(map.entrySet(), map.entrySet());
    }

    @Test
    public void mapPutIsVisibleInPreviouslyObtainedViews() {
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        map.put("c", "3");
        assertTrue(keys.contains("c"));
        assertTrue(values.contains("3"));
        assertTrue(entries.contains(new EntryStub("c", "3")));
    }

    @Test
    public void mapRemoveAndClearAreVisibleInExistingViews() {
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        map.remove("a");
        assertFalse(keys.contains("a"));
        assertFalse(values.contains("1"));
        assertFalse(entries.contains(new EntryStub("a", "1")));
        map.clear();
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
    }

    @Test
    public void keySetRemoveUpdatesMap() {
        assertTrue(map.keySet().remove("a"));
        assertFalse(map.containsKey("a"));
        assertEquals(1, map.size());
        assertFalse(map.keySet().remove("missing"));
    }

    @Test
    public void valuesRemoveDeletesOnlyOneDuplicateMapping() {
        map.put("c", "1");
        assertTrue(map.values().remove("1"));
        assertEquals(2, map.size());
        assertTrue(map.containsValue("1"));
        assertFalse(map.values().remove("missing"));
    }

    @Test
    public void entrySetContainsRequiresMatchingKeyAndValue() {
        assertTrue(map.entrySet().contains(new EntryStub("a", "1")));
        assertFalse(map.entrySet().contains(new EntryStub("a", "2")));
        assertFalse(map.entrySet().contains(new EntryStub("missing", "1")));
        assertFalse(map.entrySet().contains("a=1"));
    }

    @Test
    public void entrySetRemoveRequiresMatchingKeyAndValue() {
        assertFalse(map.entrySet().remove(new EntryStub("a", "2")));
        assertTrue(map.containsKey("a"));
        assertTrue(map.entrySet().remove(new EntryStub("a", "1")));
        assertFalse(map.containsKey("a"));
    }

    @Test
    public void clearingViewClearsBackingMap() {
        map.values().clear();
        assertTrue(map.isEmpty());
        assertTrue(map.keySet().isEmpty());
        assertTrue(map.entrySet().isEmpty());
    }

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

    @Test
    public void keySetsWithSameElementsAreEqualAndHaveSameHashCode() {
        HMap other = new MapAdapter();
        other.put("b", "different");
        other.put("a", "values");
        assertEquals(map.keySet(), other.keySet());
        assertEquals(map.keySet().hashCode(), other.keySet().hashCode());
        other.put("c", "3");
        assertFalse(map.keySet().equals(other.keySet()));
    }

    @Test
    public void entrySetsWithSameMappingsAreEqualAndHaveSameHashCode() {
        HMap other = new MapAdapter();
        other.put("b", "2");
        other.put("a", "1");
        assertEquals(map.entrySet(), other.entrySet());
        assertEquals(map.entrySet().hashCode(), other.entrySet().hashCode());
        other.put("a", "different");
        assertFalse(map.entrySet().equals(other.entrySet()));
    }

    @Test
    public void valuesViewsDoNotUseSetEquality() {
        HMap other = new MapAdapter();
        other.put("x", "1");
        other.put("y", "2");
        assertFalse(map.values().equals(other.values()));
        assertFalse(other.values().equals(map.values()));
        assertTrue(map.values().equals(map.values()));
    }

    @Test
    public void containsAllRecognizesSubsetsAndRejectsMissingElements() {
        HMap subset = new MapAdapter();
        subset.put("a", "x");
        assertTrue(map.keySet().containsAll(subset.keySet()));
        subset.put("missing", "x");
        assertFalse(map.keySet().containsAll(subset.keySet()));
    }

    @Test
    public void addIsUnsupportedByEveryView() {
        expectUnsupportedAdd(map.keySet(), "c");
        expectUnsupportedAdd(map.values(), "3");
        expectUnsupportedAdd(map.entrySet(), new EntryStub("c", "3"));
        assertEquals(2, map.size());
    }

    @Test
    public void addAllWithEmptyCollectionReturnsFalse() {
        HCollection empty = new MapAdapter().keySet();
        assertFalse(map.keySet().addAll(empty));
        assertFalse(map.values().addAll(empty));
        assertFalse(map.entrySet().addAll(empty));
        assertEquals(2, map.size());
    }

    @Test
    public void addAllWithElementsIsUnsupportedByEveryView() {
        HMap source = new MapAdapter();
        source.put("c", "3");
        expectUnsupportedAddAll(map.keySet(), source.keySet());
        expectUnsupportedAddAll(map.values(), source.values());
        expectUnsupportedAddAll(map.entrySet(), source.entrySet());
        assertEquals(2, map.size());
    }

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

    @Test
    public void toArrayWithTooSmallObjectArrayAllocatesRequiredSize() {
        Object[] supplied = new Object[0];
        Object[] result = map.keySet().toArray(supplied);
        assertNotSame(supplied, result);
        assertEquals(2, result.length);
        assertTrue(arrayContains(result, "a"));
        assertTrue(arrayContains(result, "b"));
    }

    @Test
    public void toArrayWithExactObjectArrayReusesIt() {
        Object[] supplied = new Object[2];
        Object[] result = map.keySet().toArray(supplied);
        assertSame(supplied, result);
        assertTrue(arrayContains(result, "a"));
        assertTrue(arrayContains(result, "b"));
    }

    @Test
    public void toArrayWithLargeArraySetsOnlyFollowingElementToNull() {
        Object sentinel = new Object();
        Object[] supplied = new Object[] {sentinel, sentinel, sentinel, sentinel};
        Object[] result = map.keySet().toArray(supplied);
        assertSame(supplied, result);
        assertNull(result[2]);
        assertSame(sentinel, result[3]);
    }

    @Test
    public void toArrayFillsCompatibleTypedArrayWhenLargeEnough() {
        String[] supplied = new String[3];
        Object[] result = map.keySet().toArray(supplied);
        assertSame(supplied, result);
        assertTrue(arrayContains(result, "a"));
        assertTrue(arrayContains(result, "b"));
        assertNull(result[2]);
    }

    @Test(expected = ArrayStoreException.class)
    public void toArrayRejectsIncompatibleTypedArrayDuringStorage() {
        map.keySet().toArray(new Integer[2]);
    }

    @Test(expected = NullPointerException.class)
    public void toArrayRejectsNullArgument() {
        map.keySet().toArray(null);
    }

    private static final class EntryStub implements HMap.Entry {
        private final Object key;
        private Object value;

        private EntryStub(Object entryKey, Object entryValue) {
            key = entryKey;
            value = entryValue;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object newValue) {
            Object previous = value;
            value = newValue;
            return previous;
        }

        public boolean equals(Object object) {
            if (!(object instanceof HMap.Entry)) {
                return false;
            }
            HMap.Entry other = (HMap.Entry) object;
            return equal(key, other.getKey())
                    && equal(value, other.getValue());
        }

        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        private static boolean equal(Object first, Object second) {
            return first == null ? second == null : first.equals(second);
        }
    }

    private static void expectUnsupportedAdd(HCollection collection,
            Object object) {
        try {
            collection.add(object);
            fail();
        } catch (MapAdapter.HUnsupportedOperationException expected) {
            assertFalse(collection.contains(object));
        }
    }

    private static void expectUnsupportedAddAll(HCollection destination,
            HCollection source) {
        try {
            destination.addAll(source);
            fail();
        } catch (MapAdapter.HUnsupportedOperationException expected) {
            return;
        }
    }

    private static boolean arrayContains(Object[] array, Object expected) {
        int index;
        for (index = 0; index < array.length; index++) {
            if (expected.equals(array[index])) {
                return true;
            }
        }
        return false;
    }
}
