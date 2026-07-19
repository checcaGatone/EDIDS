package myAdapter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

public class MapAdapter implements HMap {
    public static final class HUnsupportedOperationException
            extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public HUnsupportedOperationException() {
        }

        public HUnsupportedOperationException(String message) {
            super(message);
        }
    }

    public static final class HIllegalStateException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public HIllegalStateException() {
        }

        public HIllegalStateException(String message) {
            super(message);
        }
    }

    private final Hashtable table;
    private HSet keys;
    private HCollection values;
    private HSet entries;

    public MapAdapter() {
        table = new Hashtable();
    }

    public MapAdapter(HMap map) {
        table = new Hashtable();
        copyFrom(map);
    }

    public int size() {
        return table.size();
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public boolean containsKey(Object key) {
        return table.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return table.contains(value);
    }

    public Object get(Object key) {
        return table.get(key);
    }

    public Object put(Object key, Object value) {
        return table.put(key, value);
    }

    public Object remove(Object key) {
        return table.remove(key);
    }

    public void putAll(HMap map) {
        copyFrom(map);
    }

    private void copyFrom(HMap map) {
        if (map == null) {
            throw new NullPointerException();
        }
        HIterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            HMap.Entry entry = (HMap.Entry) iterator.next();
            table.put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        table.clear();
    }

    public HSet keySet() {
        if (keys == null) {
            keys = new KeySet();
        }
        return keys;
    }

    public HCollection values() {
        if (values == null) {
            values = new Values();
        }
        return values;
    }

    public HSet entrySet() {
        if (entries == null) {
            entries = new EntrySet();
        }
        return entries;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof HMap)) {
            return false;
        }
        HMap map = (HMap) object;
        if (map.size() != size()) {
            return false;
        }
        try {
            Enumeration enumeration = table.keys();
            while (enumeration.hasMoreElements()) {
                Object key = enumeration.nextElement();
                Object value = table.get(key);
                if (!map.containsKey(key)) {
                    return false;
                }
                if (!value.equals(map.get(key))) {
                    return false;
                }
            }
        } catch (ClassCastException exception) {
            return false;
        } catch (NullPointerException exception) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 0;
        Enumeration enumeration = table.keys();
        while (enumeration.hasMoreElements()) {
            Object key = enumeration.nextElement();
            hash += key.hashCode() ^ table.get(key).hashCode();
        }
        return hash;
    }

    public String toString() {
        return table.toString();
    }

    private Object[] snapshotKeys() {
        synchronized (table) {
            Object[] snapshot = new Object[table.size()];
            Enumeration enumeration = table.keys();
            int index = 0;
            while (enumeration.hasMoreElements()) {
                snapshot[index] = enumeration.nextElement();
                index++;
            }
            return snapshot;
        }
    }

    private boolean containsEntry(Object object) {
        if (!(object instanceof HMap.Entry)) {
            return false;
        }
        HMap.Entry entry = (HMap.Entry) object;
        Object key = entry.getKey();
        if (!table.containsKey(key)) {
            return false;
        }
        return equal(table.get(key), entry.getValue());
    }

    private static boolean equal(Object first, Object second) {
        return first == null ? second == null : first.equals(second);
    }

    private abstract class View implements HCollection {
        public int size() {
            return MapAdapter.this.size();
        }

        public boolean isEmpty() {
            return MapAdapter.this.isEmpty();
        }

        public abstract boolean contains(Object object);

        public abstract HIterator iterator();

        public Object[] toArray() {
            Object[] array = new Object[size()];
            HIterator iterator = iterator();
            int index = 0;
            while (iterator.hasNext()) {
                array[index] = iterator.next();
                index++;
            }
            return array;
        }

        public Object[] toArray(Object[] array) {
            if (array == null) {
                throw new NullPointerException();
            }
            int currentSize = size();
            Object[] destination = array;
            if (array.length < currentSize) {
                if (array.getClass() != Object[].class) {
                    throw new ArrayStoreException();
                }
                destination = new Object[currentSize];
            }
            HIterator iterator = iterator();
            int index = 0;
            while (iterator.hasNext()) {
                destination[index] = iterator.next();
                index++;
            }
            if (destination.length > index) {
                destination[index] = null;
            }
            return destination;
        }

        public boolean add(Object object) {
            throw new HUnsupportedOperationException();
        }

        public abstract boolean remove(Object object);

        public boolean containsAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            HIterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (!contains(iterator.next())) {
                    return false;
                }
            }
            return true;
        }

        public boolean addAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            boolean modified = false;
            HIterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (add(iterator.next())) {
                    modified = true;
                }
            }
            return modified;
        }

        public boolean removeAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            boolean modified = false;
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                if (collection.contains(iterator.next())) {
                    iterator.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public boolean retainAll(HCollection collection) {
            if (collection == null) {
                throw new NullPointerException();
            }
            boolean modified = false;
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                if (!collection.contains(iterator.next())) {
                    iterator.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public void clear() {
            MapAdapter.this.clear();
        }
    }

    private abstract class SetView extends View implements HSet {
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof HSet)) {
                return false;
            }
            HSet set = (HSet) object;
            if (set.size() != size()) {
                return false;
            }
            try {
                return containsAll(set);
            } catch (ClassCastException exception) {
                return false;
            } catch (NullPointerException exception) {
                return false;
            }
        }

        public int hashCode() {
            int hash = 0;
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                if (object != null) {
                    hash += object.hashCode();
                }
            }
            return hash;
        }
    }

    private final class KeySet extends SetView {
        public boolean contains(Object object) {
            return containsKey(object);
        }

        public HIterator iterator() {
            return new ViewIterator(ViewIterator.KEYS);
        }

        public boolean remove(Object object) {
            if (!containsKey(object)) {
                return false;
            }
            MapAdapter.this.remove(object);
            return true;
        }
    }

    private final class Values extends View {
        public boolean contains(Object object) {
            return containsValue(object);
        }

        public HIterator iterator() {
            return new ViewIterator(ViewIterator.VALUES);
        }

        public boolean remove(Object object) {
            HIterator iterator = iterator();
            while (iterator.hasNext()) {
                Object value = iterator.next();
                if (object == null
                        ? value == null
                        : object.equals(value)) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }
    }

    private final class EntrySet extends SetView {
        public boolean contains(Object object) {
            return containsEntry(object);
        }

        public HIterator iterator() {
            return new ViewIterator(ViewIterator.ENTRIES);
        }

        public boolean remove(Object object) {
            if (!containsEntry(object)) {
                return false;
            }
            HMap.Entry entry = (HMap.Entry) object;
            MapAdapter.this.remove(entry.getKey());
            return true;
        }
    }

    private final class ViewIterator implements HIterator {
        private static final int KEYS = 0;
        private static final int VALUES = 1;
        private static final int ENTRIES = 2;

        private final Object[] snapshot;
        private final int type;
        private int cursor;
        private Object lastKey;
        private boolean removable;

        private ViewIterator(int iteratorType) {
            snapshot = snapshotKeys();
            type = iteratorType;
        }

        public boolean hasNext() {
            return cursor < snapshot.length;
        }

        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastKey = snapshot[cursor];
            cursor++;
            removable = true;
            if (type == KEYS) {
                return lastKey;
            }
            if (type == VALUES) {
                return table.get(lastKey);
            }
            return new MapEntry(lastKey);
        }

        public void remove() {
            if (!removable) {
                throw new HIllegalStateException();
            }
            MapAdapter.this.remove(lastKey);
            removable = false;
        }
    }

    private final class MapEntry implements HMap.Entry {
        private final Object key;

        private MapEntry(Object entryKey) {
            key = entryKey;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return table.get(key);
        }

        public Object setValue(Object value) {
            return table.put(key, value);
        }

        public boolean equals(Object object) {
            if (!(object instanceof HMap.Entry)) {
                return false;
            }
            HMap.Entry entry = (HMap.Entry) object;
            return equal(key, entry.getKey())
                    && equal(getValue(), entry.getValue());
        }

        public int hashCode() {
            Object value = getValue();
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(key);
            buffer.append('=');
            buffer.append(getValue());
            return buffer.toString();
        }
    }
}
