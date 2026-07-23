package myAdapter;

/**
 * Interfaccia che rappresenta una collezione di elementi senza duplicati.
 *
 * <p>
 * Il compito di questa interfaccia è riprodurre, nell'ambiente CLDC 1.1,
 * il ruolo svolto da {@code java.util.Set}
 * della piattaforma J2SE 1.4.2. Un {@code HSet} rappresenta quindi un insieme
 * nel quale ogni elemento può comparire al massimo una volta.
 * </p>
 *
 * <p>
 * {@code HSet} estende {@link HCollection} e ne eredita tutti i metodi, come
 * quelli che permettono di controllare la dimensione dell'insieme, verificare
 * la presenza di un elemento, ottenere un iteratore, convertire il contenuto
 * in un array oppure rimuovere degli elementi. Questa interfaccia non aggiunge
 * nuovi metodi perché la differenza rispetto a una normale
 * {@code HCollection} non riguarda le operazioni disponibili, ma la condizione
 * che deve essere rispettata: un insieme non può contenere elementi duplicati.
 * </p>
 *
 * <p>
 * Due elementi sono considerati duplicati quando risultano uguali secondo il
 * confronto previsto dall'implementazione. Di conseguenza, un'operazione di
 * inserimento non deve aggiungere un elemento se nell'insieme è già presente
 * un elemento considerato uguale.
 * </p>
 *
 * <p>
 * Nel progetto questa interfaccia viene utilizzata soprattutto come tipo delle
 * viste restituite dai metodi {@code keySet()} ed {@code entrySet()} della
 * mappa. La vista delle chiavi è un insieme perché una mappa non può contenere
 * più associazioni con la stessa chiave. Anche la vista delle associazioni è
 * rappresentata come un insieme, poiché ogni elemento identifica una distinta
 * coppia chiave-valore.
 * </p>
 *
 * <p>
 * Il metodo {@code values()}, invece, restituisce una {@link HCollection} e
 * non un {@code HSet}, perché chiavi differenti possono essere associate a
 * valori uguali e quindi la vista dei valori può contenere duplicati.
 * </p>
 *
 * <p>
 * Quando un {@code HSet} rappresenta una vista della mappa, l'insieme è
 * collegato direttamente alla mappa di origine. Le modifiche effettuate
 * attraverso la vista, quando supportate, si riflettono quindi sulla mappa e
 * le modifiche apportate alla mappa risultano visibili anche attraverso la
 * vista. Le operazioni disponibili e le eventuali operazioni non supportate
 * dipendono dalla specifica implementazione.
 * </p>
 *
 * @author Filippo Barban
 * @version 1.1.0
 *
 * @see HCollection
 */
public interface HSet extends HCollection {
}