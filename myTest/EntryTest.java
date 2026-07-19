package myTest;

import myAdapter.HMap;
import myAdapter.MapAdapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EntryTest {
    private HMap map;
    private HMap.Entry entry;

    @Before
    public void setUp() {
        map = new MapAdapter();
        map.put("a", "1");
        entry = (HMap.Entry) map.entrySet().iterator().next();
    }

    @Test
    public void gettersReturnCurrentKeyAndValue() {
        assertEquals("a", entry.getKey());
        assertEquals("1", entry.getValue());
    }

    @Test
    public void setValueReturnsPreviousValue() {
        assertEquals("1", entry.setValue("2"));
        assertEquals("2", entry.getValue());
    }

    @Test
    public void setValueWritesThroughToMapAndViews() {
        HMap.Entry sameEntry = entry;
        sameEntry.setValue("2");
        assertEquals("2", map.get("a"));
        assertTrue(map.values().contains("2"));
        assertTrue(map.entrySet().contains(new EntryStub("a", "2")));
        assertFalse(map.values().contains("1"));
    }

    @Test
    public void setValueRejectsNullThroughHashtable() {
        try {
            entry.setValue(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("1", map.get("a"));
        }
    }

    @Test
    public void equalEntriesRepresentSameMappingSymmetrically() {
        HMap.Entry other = new EntryStub("a", "1");
        assertTrue(entry.equals(other));
        assertTrue(other.equals(entry));
    }

    @Test
    public void entryEqualityRejectsDifferentMappingsAndOtherObjects() {
        assertFalse(entry.equals(new EntryStub("b", "1")));
        assertFalse(entry.equals(new EntryStub("a", "2")));
        assertFalse(entry.equals(null));
        assertFalse(entry.equals("a=1"));
    }

    @Test
    public void hashCodeIsKeyHashXorValueHash() {
        int expected = "a".hashCode() ^ "1".hashCode();
        assertEquals(expected, entry.hashCode());
        assertEquals(new EntryStub("a", "1").hashCode(), entry.hashCode());
    }

    @Test
    public void toStringUsesKeyEqualsValueFormat() {
        assertEquals("a=1", entry.toString());
        entry.setValue("2");
        assertEquals("a=2", entry.toString());
    }

    @Test
    public void entryReadsCurrentValueAfterMapReplacement() {
        map.put("a", "updated");
        assertEquals("updated", entry.getValue());
        assertEquals(new EntryStub("a", "updated"), entry);
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
}
