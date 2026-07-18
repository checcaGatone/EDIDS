package myAdapter;

public interface HCollection {

    int size();

    boolean isEmpty();

    boolean contains(Object obj);

    Object[] toArray(Object arrayTarget[]);

    boolean add(Object obj);

    boolean remove(Object obj);

    boolean containsAll(HCollection coll);

    boolean addAll(HCollection coll);

    boolean removeAll(HCollection coll);

    boolean retainAll(HCollection coll);

    void clear();
    
    boolean equals(Object obj);

    int hashCode();

}