package myAdapter;
public interface HMap{

    int size();

    boolean isEmpty();
    
    boolean containsKey(Object key);

    boolean containsValue(Object value);

    Object get(Object key);

    Object put(Object key, Object value);

    Object remove(Object key);

    void putAll(HMap m);

    void clear();

    HSet keySet();

    HCollection values();

    HSet entrySet();

    //interfaccia interna che serve per rappresentare Map.Entry che premette di mantenere una coppia chiave-valore
    //definita localmente per evitare collisioni
    interface Entry
    {
        
        Object getKey();

        Object getValue();

        Object setValue(Object value);

        boolean equals(Object o);

        int hashCode();
    }

    boolean equals(Object o);

    int hashCode();
}