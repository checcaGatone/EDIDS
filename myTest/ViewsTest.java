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
    public void keySetContainsNullThrowsAndPreservesMap() {
        try {
            map.keySet().contains(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(2, map.size());
            assertEquals("1", map.get("a"));
            assertEquals("2", map.get("b"));
        }
    }

    @Test
    public void keySetRemoveNullThrowsAndPreservesMap() {
        try {
            map.keySet().remove(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals(2, map.size());
            assertEquals("1", map.get("a"));
            assertEquals("2", map.get("b"));
        }
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
    public void valuesRemoveInvokesEqualsOnRemovalArgument() {
        HMap local = new MapAdapter();
        local.put("key", new StoredRejectingValue());

        assertTrue(local.values().remove(new MatchingRemovalProbe()));
        assertTrue(local.isEmpty());
        assertFalse(local.containsKey("key"));
    }

    @Test
    public void valuesRemoveDoesNotInvokeEqualsOnStoredValue() {
        HMap local = new MapAdapter();
        Object stored = new StoredMatchingValue();
        local.put("key", stored);

        assertFalse(local.values().remove(new RejectingRemovalProbe()));
        assertEquals(1, local.size());
        assertSame(stored, local.get("key"));
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
        HSet keys = map.keySet();
        HCollection values = map.values();
        HSet entries = map.entrySet();
        values.clear();
        assertTrue(map.isEmpty());
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void keySetClearEmptiesMapAndEveryExistingView() {
        HMap local = populatedMap();
        HSet keys = local.keySet();
        HCollection values = local.values();
        HSet entries = local.entrySet();
        keys.clear();
        assertTrue(local.isEmpty());
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
        assertEquals(0, local.size());
    }

    @Test
    public void entrySetClearEmptiesMapAndEveryExistingView() {
        HMap local = populatedMap();
        HSet keys = local.keySet();
        HCollection values = local.values();
        HSet entries = local.entrySet();
        entries.clear();
        assertTrue(local.isEmpty());
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
        assertEquals(0, local.size());
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
    public void entrySetRemoveAllRemovesOnlyMatchingMappings() {
        map.put("c", "3");
        HMap selected = new MapAdapter();
        selected.put("a", "1");
        selected.put("b", "different");
        assertTrue(map.entrySet().removeAll(selected.entrySet()));
        assertFalse(map.containsKey("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
        assertEquals(2, map.size());
    }

    @Test
    public void entrySetRetainAllKeepsOnlyMatchingMappings() {
        map.put("c", "3");
        HMap selected = new MapAdapter();
        selected.put("a", "1");
        selected.put("b", "different");
        selected.put("c", "3");
        assertTrue(map.entrySet().retainAll(selected.entrySet()));
        assertEquals("1", map.get("a"));
        assertFalse(map.containsKey("b"));
        assertEquals("3", map.get("c"));
        assertEquals(2, map.size());
    }

    @Test
    public void entrySetBulkOperationsReturnFalseWithoutChanges() {
        HMap noMatches = new MapAdapter();
        noMatches.put("a", "different");
        noMatches.put("missing", "value");
        assertFalse(map.entrySet().removeAll(noMatches.entrySet()));
        assertEquals(2, map.size());

        HMap same = new MapAdapter(map);
        assertFalse(map.entrySet().retainAll(same.entrySet()));
        assertEquals(same, map);
    }

    @Test
    public void keySetRemoveAllWithSameViewEmptiesMap() {
        HSet view = map.keySet();
        assertTrue(view.removeAll(view));
        assertTrue(map.isEmpty());
        assertTrue(view.isEmpty());
        assertEquals(0, map.size());
        assertFalse(view.removeAll(view));
    }

    @Test
    public void valuesRemoveAllWithSameViewRemovesDuplicateMappings() {
        map.put("c", "1");
        HCollection view = map.values();
        assertTrue(view.removeAll(view));
        assertTrue(map.isEmpty());
        assertTrue(view.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void entrySetRemoveAllWithSameViewEmptiesMap() {
        HSet view = map.entrySet();
        assertTrue(view.removeAll(view));
        assertTrue(map.isEmpty());
        assertTrue(view.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void keySetRetainAllWithSameViewMakesNoChanges() {
        HSet view = map.keySet();
        assertFalse(view.retainAll(view));
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertTrue(view.contains("a"));
        assertTrue(view.contains("b"));
    }

    @Test
    public void valuesRetainAllWithSameViewPreservesDuplicateMappings() {
        map.put("c", "1");
        HCollection view = map.values();
        assertFalse(view.retainAll(view));
        assertEquals(3, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("1", map.get("c"));
        assertEquals(2, arrayOccurrences(view.toArray(), "1"));
    }

    @Test
    public void entrySetRetainAllWithSameViewMakesNoChanges() {
        HSet view = map.entrySet();
        assertFalse(view.retainAll(view));
        assertEquals(2, map.size());
        assertTrue(view.contains(new EntryStub("a", "1")));
        assertTrue(view.contains(new EntryStub("b", "2")));
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
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
    public void keySetRejectsNullBulkArgumentsWithoutChanges() {
        assertNullBulkArgumentsRejected(map.keySet());
    }

    @Test
    public void valuesRejectNullBulkArgumentsWithoutChanges() {
        assertNullBulkArgumentsRejected(map.values());
    }

    @Test
    public void entrySetRejectsNullBulkArgumentsWithoutChanges() {
        assertNullBulkArgumentsRejected(map.entrySet());
    }

    @Test
    public void valuesContainsNullThrowsAndPreservesMap() {
        try {
            map.values().contains(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }
    }

    @Test
    public void valuesRemoveNullReturnsFalseAndPreservesMap() {
        assertFalse(map.values().remove(null));
        assertFixtureUnchanged();
    }

    @Test
    public void entrySetRejectsNullAndNonEntryElementsWithoutChanges() {
        assertFalse(map.entrySet().contains(null));
        assertFalse(map.entrySet().remove(null));
        assertFalse(map.entrySet().contains("not an entry"));
        assertFalse(map.entrySet().remove("not an entry"));
        assertFixtureUnchanged();
    }

    @Test
    public void keySetBulkOperationsReturnFalseWithoutChanges() {
        HMap noMatches = new MapAdapter();
        noMatches.put("missing", "x");
        assertFalse(map.keySet().removeAll(noMatches.keySet()));
        assertFixtureUnchanged();

        HMap allKeys = new MapAdapter();
        allKeys.put("a", "x");
        allKeys.put("b", "x");
        assertFalse(map.keySet().retainAll(allKeys.keySet()));
        assertFixtureUnchanged();
    }

    @Test
    public void valuesBulkOperationsReturnFalseWithoutChanges() {
        HMap noMatches = new MapAdapter();
        noMatches.put("x", "missing");
        assertFalse(map.values().removeAll(noMatches.values()));
        assertFixtureUnchanged();

        HMap allValues = new MapAdapter();
        allValues.put("x", "1");
        allValues.put("y", "2");
        assertFalse(map.values().retainAll(allValues.values()));
        assertFixtureUnchanged();
    }

    @Test
    public void keySetSatisfiesSetEqualityBoundaryContracts() {
        HSet set = map.keySet();
        HMap equivalentMap = new MapAdapter();
        equivalentMap.put("b", "different");
        equivalentMap.put("a", "values");
        HSet equivalent = equivalentMap.keySet();

        assertFalse(set.equals(null));
        assertFalse(set.equals("not a set"));
        assertTrue(set.equals(set));
        assertTrue(set.equals(equivalent));
        assertTrue(equivalent.equals(set));
        assertEquals(set.hashCode(), equivalent.hashCode());

        equivalentMap.put("c", "3");
        assertFalse(set.equals(equivalent));
    }

    @Test
    public void entrySetSatisfiesSetEqualityBoundaryContracts() {
        HSet set = map.entrySet();
        HMap equivalentMap = new MapAdapter();
        equivalentMap.put("b", "2");
        equivalentMap.put("a", "1");
        HSet equivalent = equivalentMap.entrySet();

        assertFalse(set.equals(null));
        assertFalse(set.equals("not a set"));
        assertTrue(set.equals(set));
        assertTrue(set.equals(equivalent));
        assertTrue(equivalent.equals(set));
        assertEquals(set.hashCode(), equivalent.hashCode());

        equivalentMap.put("c", "3");
        assertFalse(set.equals(equivalent));
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
    public void valuesToArrayPreservesDuplicatesAndIsIndependent() {
        map.put("c", "1");
        Object[] result = map.values().toArray();
        assertEquals(3, result.length);
        assertEquals(2, arrayOccurrences(result, "1"));
        assertEquals(1, arrayOccurrences(result, "2"));
        result[0] = "changed";
        assertEquals(3, map.size());
        assertTrue(map.values().contains("1"));
        assertTrue(map.values().contains("2"));
    }

    @Test
    public void entrySetToArrayContainsEntriesForEveryMapping() {
        Object[] result = map.entrySet().toArray();
        assertEquals(map.size(), result.length);
        int index;
        for (index = 0; index < result.length; index++) {
            assertTrue(result[index] instanceof HMap.Entry);
            HMap.Entry entry = (HMap.Entry) result[index];
            assertTrue(map.containsKey(entry.getKey()));
            assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }

    @Test
    public void valuesToArrayReusesLargeArrayAndPreservesTail() {
        Object sentinel = new Object();
        Object[] supplied = new Object[] {
                sentinel, sentinel, sentinel, sentinel
        };
        Object[] result = map.values().toArray(supplied);
        assertSame(supplied, result);
        assertNull(result[2]);
        assertSame(sentinel, result[3]);
        assertTrue(arrayContains(result, "1"));
        assertTrue(arrayContains(result, "2"));
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

    private static final class StoredRejectingValue {
        public boolean equals(Object object) {
            return false;
        }

        public int hashCode() {
            return 1;
        }
    }

    private static final class MatchingRemovalProbe {
        public boolean equals(Object object) {
            return object instanceof StoredRejectingValue;
        }

        public int hashCode() {
            return 1;
        }
    }

    private static final class StoredMatchingValue {
        public boolean equals(Object object) {
            return object instanceof RejectingRemovalProbe;
        }

        public int hashCode() {
            return 1;
        }
    }

    private static final class RejectingRemovalProbe {
        public boolean equals(Object object) {
            return false;
        }

        public int hashCode() {
            return 1;
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

    private static int arrayOccurrences(Object[] array, Object expected) {
        int occurrences = 0;
        int index;
        for (index = 0; index < array.length; index++) {
            if (expected.equals(array[index])) {
                occurrences++;
            }
        }
        return occurrences;
    }

    private static HMap populatedMap() {
        HMap result = new MapAdapter();
        result.put("a", "1");
        result.put("b", "2");
        return result;
    }

    private void assertNullBulkArgumentsRejected(HCollection view) {
        try {
            view.containsAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }

        try {
            view.addAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }

        try {
            view.removeAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }

        try {
            view.retainAll(null);
            fail();
        } catch (NullPointerException expected) {
            assertFixtureUnchanged();
        }
    }

    private void assertFixtureUnchanged() {
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertTrue(map.keySet().contains("a"));
        assertTrue(map.keySet().contains("b"));
        assertTrue(map.values().contains("1"));
        assertTrue(map.values().contains("2"));
    }
}
