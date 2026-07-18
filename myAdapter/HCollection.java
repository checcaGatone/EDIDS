package myAdapter;

public interface HCollection {
    int size();

    boolean isEmpty();

    boolean contains(Object object);

    HIterator iterator();

    Object[] toArray();

    Object[] toArray(Object[] array);

    boolean add(Object object);

    boolean remove(Object object);

    boolean containsAll(HCollection collection);

    boolean addAll(HCollection collection);

    boolean removeAll(HCollection collection);

    boolean retainAll(HCollection collection);

    void clear();

    boolean equals(Object object);

    int hashCode();
}
