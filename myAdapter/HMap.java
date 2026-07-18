package myAdapter;

public interface HMap {
    int size();

    boolean isEmpty();

    boolean containsKey(Object key);

    boolean containsValue(Object value);

    Object get(Object key);

    Object put(Object key, Object value);

    Object remove(Object key);

    void putAll(HMap map);

    void clear();

    HSet keySet();

    HCollection values();

    HSet entrySet();

    boolean equals(Object object);

    int hashCode();

    interface Entry {
        Object getKey();

        Object getValue();

        Object setValue(Object value);

        boolean equals(Object object);

        int hashCode();
    }
}
