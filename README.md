# EDIDS — Map Adapter per Java ME CLDC 1.1

Implementazione di un adapter dell'interfaccia `Map` del Java 2 Collections
Framework di J2SE 1.4.2, realizzata usando come adaptee `java.util.Hashtable`
disponibile in Java ME CLDC 1.1.

Il progetto consente a una libreria progettata per J2SE 1.4.2 di usare le
operazioni essenziali di `Map`, `Collection`, `Set`, `Iterator` e `Map.Entry`
in un ambiente nel quale il Java Collections Framework completo non è
disponibile. Per evitare collisioni con le omonime interfacce della versione
corrente di Java, il package `myAdapter` espone le interfacce locali `HMap`,
`HCollection`, `HSet` e `HIterator`.

## Indice

- [Obiettivi e vincoli](#obiettivi-e-vincoli)
- [Struttura del progetto](#struttura-del-progetto)
- [Architettura](#architettura)
- [API pubblica](#api-pubblica)
- [Comportamento dell'adapter](#comportamento-delladapter)
- [Compatibilità CLDC 1.1](#compatibilità-cldc-11)
- [Requisiti per compilazione e test](#requisiti-per-compilazione-e-test)
- [Compilazione ed esecuzione](#compilazione-ed-esecuzione)
- [Suite di test](#suite-di-test)
- [Generazione della documentazione Javadoc](#generazione-della-documentazione-javadoc)
- [Esempio d'uso](#esempio-duso)
- [Scelte progettuali e limiti noti](#scelte-progettuali-e-limiti-noti)
- [Preparazione della consegna](#preparazione-della-consegna)
- [Risoluzione dei problemi](#risoluzione-dei-problemi)

## Obiettivi e vincoli

Il progetto rispetta i seguenti vincoli architetturali:

- l'adaptee di `MapAdapter` è `java.util.Hashtable` di CLDC 1.1;
- il package di produzione è esattamente `myAdapter`, senza sottopackage;
- il package dei test è esattamente `myTest`, senza sottopackage;
- non vengono usate le interfacce moderne `java.util.Map`,
  `java.util.Collection`, `java.util.Set` e `java.util.Iterator`;
- il codice in `myAdapter` non usa generics, stream, lambda, reflection,
  collezioni moderne o altre API esterne a CLDC 1.1;
- tutte le operazioni di modifica applicabili alla mappa, alle viste e agli
  iteratori sono implementate;
- le viste `keySet()`, `values()` ed `entrySet()` sono backed dalla mappa;
- la suite usa JUnit 4.13 ed è eseguibile da riga di comando;
- non sono richiesti Maven, Gradle o altri sistemi di build.

Il codice viene compilato con un JDK Java SE corrente per eseguire i test, ma
il package `myAdapter` è deliberatamente limitato alle funzionalità disponibili
in CLDC 1.1.

## Struttura del progetto

```text
EDIDS/
├── myAdapter/
│   ├── HMap.java
│   ├── HCollection.java
│   ├── HSet.java
│   ├── HIterator.java
│   └── MapAdapter.java
├── myTest/
│   ├── TestRunner.java
│   ├── MapAdapterTest.java
│   ├── EntryTest.java
│   ├── ViewsTest.java
│   └── IteratorTest.java
├── JUnit/
│   ├── junit-4.13.jar
│   └── hamcrest-core-1.3.jar
├── doc/
│   ├── adapter/              # output Javadoc dell'adapter
│   └── test/                 # output Javadoc dei test
└── README.md
```

Le directory `doc/adapter` e `doc/test` sono destinazioni di output: possono
non essere presenti o essere vuote in un checkout nel quale la documentazione
non è ancora stata generata.

## Architettura

| Concetto J2SE 1.4.2 | Tipo locale | Implementazione |
| --- | --- | --- |
| `Map` | `HMap` | `MapAdapter` |
| `Map.Entry` | `HMap.Entry` | `MapAdapter.MapEntry` privata |
| `Collection` | `HCollection` | vista `Values` e base comune delle viste |
| `Set` | `HSet` | viste `KeySet` ed `EntrySet` |
| `Iterator` | `HIterator` | `ViewIterator` privato |
| struttura dati adattata | — | `java.util.Hashtable` |

`MapAdapter` contiene una singola `Hashtable` e crea tre viste live:

- `keySet()` espone le chiavi come `HSet`;
- `values()` espone i valori come `HCollection` e conserva gli eventuali
  duplicati;
- `entrySet()` espone i mapping come elementi `HMap.Entry`.

Le istanze delle viste sono memorizzate internamente e riutilizzate, ma la
proprietà importante del contratto è il backing: una modifica effettuata sulla
mappa è immediatamente osservabile dalle viste già ottenute e una modifica
supportata su una vista aggiorna la stessa mappa.

## API pubblica

### `HMap`

`HMap` riproduce i metodi di `Map` necessari alla libreria:

- interrogazione: `size()`, `isEmpty()`, `containsKey(Object)`,
  `containsValue(Object)` e `get(Object)`;
- modifica: `put(Object, Object)`, `remove(Object)`, `putAll(HMap)` e
  `clear()`;
- viste backed: `keySet()`, `values()` ed `entrySet()`;
- contratto degli oggetti: `equals(Object)` e `hashCode()`.

L'interfaccia annidata `HMap.Entry` espone `getKey()`, `getValue()`,
`setValue(Object)`, `equals(Object)` e `hashCode()`.

### `HCollection`

`HCollection` comprende:

- interrogazione: `size()`, `isEmpty()`, `contains(Object)` e
  `containsAll(HCollection)`;
- attraversamento e conversione: `iterator()`, `toArray()` e
  `toArray(Object[])`;
- operazioni opzionali: `add(Object)`, `remove(Object)`,
  `addAll(HCollection)`, `removeAll(HCollection)`,
  `retainAll(HCollection)` e `clear()`;
- `equals(Object)` e `hashCode()`.

### `HSet`

`HSet` estende `HCollection` senza aggiungere metodi. Le implementazioni
`KeySet` ed `EntrySet` applicano la semantica di uguaglianza e hash prevista
per gli insiemi.

### `HIterator`

`HIterator` espone `hasNext()`, `next()` e l'operazione opzionale `remove()`.

### `MapAdapter`

Sono disponibili due costruttori:

```java
new MapAdapter()
new MapAdapter(HMap source)
```

Il costruttore di copia crea una nuova `Hashtable` e copia tutti i mapping
della sorgente. La struttura risultante è indipendente dalla sorgente, mentre
chiavi e valori vengono copiati per riferimento, come in una copia superficiale.
Passare `null` al costruttore di copia produce `NullPointerException`.

## Comportamento dell'adapter

### Operazioni della mappa

- `put` inserisce un mapping o sostituisce il valore associato a una chiave e
  restituisce il valore precedente;
- `get` restituisce il valore corrente o `null` se la chiave non è presente;
- `remove` elimina il mapping e restituisce il valore precedente;
- `putAll` copia ogni mapping da un'altra `HMap`, sostituendo gli eventuali
  valori associati a chiavi già presenti;
- `clear` elimina tutti i mapping;
- `containsValue` scandisce la `Hashtable` e confronta ogni valore mediante
  `searchedValue.equals(storedValue)`, nella direzione prescritta dal contratto
  di `Map.containsValue`.

La formula applicata da `containsValue` è concettualmente:

```java
searchedValue == null
        ? storedValue == null
        : searchedValue.equals(storedValue)
```

Poiché `Hashtable` non ammette valori `null`, `containsValue(null)` solleva
esplicitamente `NullPointerException` senza modificare la mappa.

### Backing delle viste

Le tre viste non sono copie:

- `size()` e `isEmpty()` riflettono sempre lo stato corrente della mappa;
- `clear()` su qualunque vista svuota la mappa;
- `keySet().remove(key)` elimina il mapping associato;
- `values().remove(value)` elimina un solo mapping corrispondente, anche se lo
  stesso valore è associato a più chiavi;
- `entrySet().remove(entry)` elimina il mapping soltanto se coincidono sia la
  chiave sia il valore;
- `removeAll` e `retainAll` modificano la mappa tramite l'iteratore della vista;
- una `HMap.Entry` restituita da `entrySet()` legge il valore corrente e
  `setValue` scrive direttamente nella mappa.

La vista `values()` non applica la semantica di uguaglianza di un insieme. Le
viste `keySet()` ed `entrySet()` confrontano invece dimensione e contenuto con
altre istanze di `HSet`, indipendentemente dall'ordine di iterazione.

### Operazioni opzionali sulle viste

| Operazione | `keySet()` | `values()` | `entrySet()` |
| --- | --- | --- | --- |
| `add` | non supportata | non supportata | non supportata |
| `addAll` non vuoto | non supportata | non supportata | non supportata |
| `remove` | supportata | supportata | supportata |
| `removeAll` | supportata | supportata | supportata |
| `retainAll` | supportata | supportata | supportata |
| `clear` | supportata | supportata | supportata |
| `iterator().remove()` | supportata | supportata | supportata |

L'aggiunta diretta a una vista non è definibile senza una chiave o un valore
complementare. `add` solleva quindi `HUnsupportedOperationException`.
`addAll` con una collezione non vuota propaga la stessa eccezione; con una
collezione vuota non tenta alcuna aggiunta e restituisce `false`.

### Iteratori

Ogni chiamata a `iterator()` crea un iteratore indipendente. Al momento della
creazione viene acquisito uno snapshot delle chiavi; chiavi, valori o entry
vengono poi prodotti a partire da tale snapshot.

Il contratto operativo è il seguente:

- chiamate ripetute a `hasNext()` non consumano elementi;
- `next()` restituisce ogni elemento previsto senza imporre un ordine;
- i valori duplicati sono conservati durante l'iterazione di `values()`;
- `next()` dopo l'esaurimento solleva `NoSuchElementException`;
- `remove()` elimina l'ultimo mapping restituito da `next()`;
- `remove()` prima di `next()` o due volte dopo lo stesso `next()` solleva
  `MapAdapter.HIllegalStateException`;
- una chiamata a `hasNext()` tra `next()` e `remove()` non rende illegale la
  rimozione;
- gli iteratori non sono fail-fast.

La mappa non deve essere modificata strutturalmente direttamente durante
l'iterazione. Per rimuovere elementi mentre si attraversa una vista va usato
`HIterator.remove()`. Il comportamento dopo modifiche strutturali esterne è da
considerarsi non garantito, coerentemente con il fatto che l'implementazione
non mantiene un `modCount`.

### Entry backed

Le entry prodotte da `entrySet().iterator()` conservano la chiave e leggono il
valore dalla mappa al momento della chiamata:

- `getKey()` restituisce la chiave del mapping;
- `getValue()` restituisce il valore attualmente associato alla chiave;
- `setValue(value)` sostituisce il valore e restituisce quello precedente;
- `equals` confronta chiave e valore con un'altra `HMap.Entry`;
- `hashCode` è lo XOR tra hash della chiave e hash del valore;
- `toString()` usa il formato `chiave=valore`.

### Uguaglianza, hash e rappresentazione testuale

- `MapAdapter.equals` accetta altre implementazioni di `HMap`, verifica la
  stessa dimensione e confronta ogni mapping;
- una `java.util.Map` moderna non implementa `HMap` e non è quindi uguale a un
  `MapAdapter` solo perché contiene gli stessi mapping;
- `MapAdapter.hashCode` è la somma degli hash delle entry;
- `MapAdapter.toString` delega alla rappresentazione della `Hashtable`;
- l'ordine prodotto da `Hashtable`, dalle viste e dagli iteratori non è parte
  del contratto e non deve essere assunto dal chiamante.

### Gestione di `null`

Il comportamento deriva principalmente dal vincolo di `Hashtable`, che non
accetta chiavi o valori `null`.

| Operazione | Comportamento con `null` |
| --- | --- |
| `put(null, value)` | `NullPointerException` |
| `put(key, null)` | `NullPointerException` |
| `get(null)` | `NullPointerException` |
| `containsKey(null)` | `NullPointerException` |
| `containsValue(null)` | `NullPointerException` |
| `remove(null)` | `NullPointerException` |
| `putAll(null)` | `NullPointerException` |
| operazione bulk con collezione `null` | `NullPointerException` |
| `keySet().contains(null)` / `remove(null)` | `NullPointerException` |
| `values().contains(null)` | `NullPointerException` |
| `values().remove(null)` | `false`, mappa invariata |
| `entrySet().contains(null)` / `remove(null)` | `false`, mappa invariata |
| `toArray(null)` | `NullPointerException` |
| `HMap.Entry.setValue(null)` | `NullPointerException` |

## Compatibilità CLDC 1.1

Il package `myAdapter` usa soltanto:

- classi fondamentali di `java.lang`, incluse `Object`, `String`,
  `StringBuffer`, eccezioni e array;
- `java.util.Hashtable` come adaptee;
- `java.util.Enumeration` per le scansioni;
- `java.util.NoSuchElementException` per `HIterator.next()`;
- sincronizzazione intrinseca tramite `synchronized`.

Non sono usati:

- generics e diamond operator;
- enhanced `for`;
- lambda, stream o method reference;
- `java.util.Map`, `Collection`, `Set` o `Iterator` moderni;
- `java.lang.reflect` o creazione riflessiva di array;
- API concorrenti moderne;
- dipendenze di terze parti nel codice di produzione.

Le classi standard `UnsupportedOperationException` e
`IllegalStateException` non sono disponibili in CLDC 1.1. Il progetto usa
pertanto le equivalenti eccezioni locali:

- `MapAdapter.HUnsupportedOperationException`;
- `MapAdapter.HIllegalStateException`.

La compilazione con un JDK moderno può mostrare un avviso relativo a operazioni
`unchecked`: è atteso, perché CLDC 1.1 e J2SE 1.4.2 precedono i generics e la
`Hashtable` viene quindi usata come tipo raw.

## Requisiti per compilazione e test

Sono necessari:

- un JDK corrente con i comandi `javac`, `java` e, per la documentazione,
  `javadoc` disponibili nel `PATH`;
- `JUnit/junit-4.13.jar`;
- `JUnit/hamcrest-core-1.3.jar`;
- una shell da riga di comando.

I due JAR sono inclusi nel repository. Il progetto non scarica dipendenze e non
richiede connessione di rete.

Tutti i comandi seguenti devono essere eseguiti dalla radice del progetto, cioè
dalla directory che contiene `myAdapter`, `myTest`, `JUnit` e `README.md`.

## Compilazione ed esecuzione

### Windows PowerShell

Creare una directory separata per i file compilati:

```powershell
New-Item -ItemType Directory -Force -Path '.\build' | Out-Null
```

Compilare adapter e test:

```powershell
javac -d '.\build' `
  -cp '.\JUnit\junit-4.13.jar;.\JUnit\hamcrest-core-1.3.jar' `
  .\myAdapter\*.java .\myTest\*.java
```

Eseguire il runner richiesto dalla traccia:

```powershell
java -cp '.\build;.\JUnit\junit-4.13.jar;.\JUnit\hamcrest-core-1.3.jar' `
  myTest.TestRunner
```

### Prompt dei comandi Windows (`cmd.exe`)

```bat
mkdir build
javac -d build -cp "JUnit\junit-4.13.jar;JUnit\hamcrest-core-1.3.jar" myAdapter\*.java myTest\*.java
java -cp "build;JUnit\junit-4.13.jar;JUnit\hamcrest-core-1.3.jar" myTest.TestRunner
```

Se `build` esiste già, il messaggio di `mkdir` può essere ignorato.

### Linux e macOS

Su sistemi Unix-like il separatore del classpath è `:` invece di `;`:

```sh
mkdir -p build
javac -d build \
  -cp "JUnit/junit-4.13.jar:JUnit/hamcrest-core-1.3.jar" \
  myAdapter/*.java myTest/*.java
java -cp "build:JUnit/junit-4.13.jar:JUnit/hamcrest-core-1.3.jar" \
  myTest.TestRunner
```

### Output atteso

Nel corrente stato del progetto il runner esegue 105 test:

```text
Test eseguiti: 105
Test superati: 105
Test falliti: 0
Test ignorati: 0
Tempo impiegato: <tempo variabile> ms
Risultato: SUCCESSO
```

In caso di fallimento vengono stampati identificatore e stack trace di ogni
test fallito. Il processo termina con codice `1` quando JUnit segnala un
risultato non riuscito e con codice `0` in caso di successo.

Il numero dei test superati è calcolato sottraendo dal numero di test eseguiti
sia i fallimenti sia gli assumption failure. I test ignorati non vengono
sottratti perché JUnit non li include in `Result.getRunCount()`.

## Suite di test

La suite è eseguita da `myTest.TestRunner` tramite `JUnitCore.runClasses` e
comprende quattro test case:

| Test case | Test | Responsabilità principale |
| --- | ---: | --- |
| `MapAdapterTest` | 28 | costruttori, operazioni `HMap`, `null`, copia, uguaglianza, hash e `containsValue` |
| `ViewsTest` | 50 | backing, operazioni bulk, uguaglianza degli insiemi, array e operazioni opzionali |
| `IteratorTest` | 16 | attraversamento, duplicati, esaurimento e `remove()` |
| `EntryTest` | 11 | backing delle entry, `setValue`, uguaglianza, hash e stringa |
| **Totale** | **105** | regressione completa dell'adapter |

La suite non dipende dall'ordine di iterazione di `Hashtable`. Quando confronta
contenuti usa appartenenza, dimensione e occorrenze, evitando assunzioni sulla
sequenza prodotta dall'adaptee.

Tra gli scenari coperti figurano:

- inserimento, sostituzione, rimozione, copia e svuotamento della mappa;
- rifiuto di chiavi e valori `null` senza modifiche parziali;
- direzione asimmetrica corretta di `equals` in `containsValue`,
  `values().contains` e `values().remove`;
- backing bidirezionale delle tre viste;
- valori duplicati nella vista `values`;
- `removeAll` e `retainAll`, inclusi i casi in cui argomento e destinatario
  sono la stessa vista;
- operazioni non supportate e relativi tipi di eccezione;
- conversione in array con dimensioni e tipi diversi;
- iterazione vuota, completa, indipendente e con rimozione;
- comportamento delle entry dopo `setValue` o sostituzione dalla mappa;
- contratti di `equals`, `hashCode` e `toString`.

I nuovi test devono essere aggiunti a una delle quattro classi già incluse nel
runner. Ogni nuovo metodo di test va documentato secondo il template richiesto
dalla traccia:

- `Summary`;
- `Test Case Design`;
- `Test Description`;
- `Pre-Condition`;
- `Post-Condition`;
- `Expected Results`.

## Generazione della documentazione Javadoc

Creare le directory di destinazione e compilare prima il progetto seguendo i
comandi precedenti.

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force -Path '.\doc\adapter' | Out-Null
New-Item -ItemType Directory -Force -Path '.\doc\test' | Out-Null

javadoc -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 `
  -d '.\doc\adapter' .\myAdapter\*.java

javadoc -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 `
  -cp '.\build;.\JUnit\junit-4.13.jar;.\JUnit\hamcrest-core-1.3.jar' `
  -d '.\doc\test' .\myTest\*.java
```

### Linux e macOS

```sh
mkdir -p doc/adapter doc/test

javadoc -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 \
  -d doc/adapter myAdapter/*.java

javadoc -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 \
  -cp "build:JUnit/junit-4.13.jar:JUnit/hamcrest-core-1.3.jar" \
  -d doc/test myTest/*.java
```

Gli entry point della documentazione generata sono:

- `doc/adapter/index.html`;
- `doc/test/index.html`.

La documentazione generata deve essere inclusa nello ZIP finale se richiesta
dalla modalità di consegna.

## Esempio d'uso

Il seguente esempio usa esclusivamente l'API locale, senza generics:

```java
import myAdapter.HIterator;
import myAdapter.HMap;
import myAdapter.MapAdapter;

public class Example {
    public static void main(String[] args) {
        HMap map = new MapAdapter();
        map.put("language", "Java");
        map.put("profile", "CLDC 1.1");

        System.out.println(map.get("language"));

        HIterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            HMap.Entry entry = (HMap.Entry) iterator.next();
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }
}
```

Compilazione dell'esempio su Windows, assumendo che `Example.java` si trovi
nella radice e che il progetto sia già compilato in `build`:

```powershell
javac -d '.\build' -cp '.\build' .\Example.java
java -cp '.\build' Example
```

## Scelte progettuali e limiti noti

### Array tipizzati e assenza di reflection

J2SE 1.4.2 prescrive che `toArray(Object[])`, quando riceve un array troppo
piccolo, crei un nuovo array dello stesso tipo runtime. La creazione generica
di tale array richiederebbe reflection, non disponibile nel profilo CLDC 1.1
adottato dal progetto.

La politica implementata dà priorità alla compatibilità CLDC:

- un `Object[]` troppo piccolo viene sostituito da un nuovo `Object[]` della
  dimensione necessaria;
- un array tipizzato compatibile e sufficientemente grande viene riutilizzato;
- se l'array è più grande del contenuto, l'elemento immediatamente successivo
  all'ultimo valore copiato viene impostato a `null`;
- un array tipizzato troppo piccolo produce `ArrayStoreException`, perché non
  può essere creato genericamente un nuovo array dello stesso tipo senza
  reflection;
- un array incompatibile produce `ArrayStoreException` durante la copia.

Questa è una deviazione consapevole e circoscritta dal comportamento J2SE,
necessaria per mantenere il codice di produzione entro CLDC 1.1.

### Eccezioni personalizzate

Le eccezioni standard `UnsupportedOperationException` e
`IllegalStateException` previste da J2SE non appartengono al profilo CLDC 1.1.
Le classi annidate `HUnsupportedOperationException` e
`HIllegalStateException` mantengono la stessa finalità contrattuale senza
introdurre dipendenze non disponibili.

### Iteratori a snapshot

Lo snapshot delle chiavi rende indipendenti gli iteratori e permette la
rimozione tramite l'iteratore senza dipendere da un iteratore moderno di
`Hashtable`. Non viene implementato il comportamento fail-fast. Modifiche
strutturali eseguite direttamente sulla mappa dopo la creazione dell'iteratore
non devono quindi essere usate come base per aspettative portabili.

### Concorrenza

`Hashtable` sincronizza le proprie operazioni e `containsValue` e la creazione
dello snapshot sono esplicitamente sincronizzati sulla stessa istanza. Questo
non rende però atomiche le operazioni composte dell'adapter, le operazioni bulk
o un'intera iterazione. Il progetto non dichiara pertanto `MapAdapter` come una
struttura completamente thread-safe per sequenze di operazioni concorrenti.

## Preparazione della consegna

Prima di creare lo ZIP finale:

1. verificare che la struttura delle directory corrisponda a quella descritta;
2. compilare da zero adapter e test;
3. eseguire `myTest.TestRunner` e controllare che non vi siano fallimenti;
4. generare `doc/adapter` e `doc/test`;
5. verificare che i JAR JUnit siano presenti nella directory `JUnit`;
6. eliminare dalla consegna directory temporanee come `build`, file `.class`
   esterni alle directory previste e impostazioni specifiche dell'IDE;
7. includere `README.md`;
8. aprire lo ZIP e controllarne manualmente il contenuto prima dell'invio.

Contenuto minimo atteso nello ZIP:

```text
myAdapter/
myTest/
JUnit/
doc/
README.md
```

Per verificare l'assenza di file `.class` nelle directory sorgente con
PowerShell:

```powershell
Get-ChildItem -Path '.\myAdapter','.\myTest' -Filter '*.class' -Recurse
```

Il comando non deve produrre output.

## Risoluzione dei problemi

### `package org.junit does not exist`

Il classpath non contiene i JAR di JUnit e Hamcrest oppure il comando non viene
eseguito dalla radice del progetto. Verificare nomi e posizione dei file nella
directory `JUnit`.

### `Could not find or load main class myTest.TestRunner`

La directory indicata con `-d` durante la compilazione deve essere la stessa
posta all'inizio del classpath di esecuzione. Con i comandi proposti è `build`.

### `javadoc` non è riconosciuto come comando

È disponibile `java`, ma il `PATH` non punta alla directory `bin` di un JDK
completo oppure è installato soltanto un runtime. Verificare con
`Get-Command javadoc` su PowerShell o `command -v javadoc` su Linux/macOS,
installare un JDK completo se necessario e aggiungere la sua directory `bin`
al `PATH` prima di rigenerare la documentazione.

### Il comando funziona su Windows ma non su Linux/macOS

Nel classpath Windows gli elementi sono separati da `;`; su Linux e macOS sono
separati da `:`. I separatori dei percorsi sono rispettivamente `\` e `/`.

### Avvisi `unchecked or unsafe operations`

Sono dovuti all'uso intenzionale di tipi raw, coerente con CLDC 1.1 e J2SE
1.4.2. Non indicano da soli un errore di compilazione o un test fallito.

### I test dipendono da un ordine diverso

`Hashtable` non garantisce l'ordine richiesto da una lista. Codice e test non
devono fare affidamento sull'ordine di `keySet`, `values`, `entrySet` o dei loro
iteratori.

### `toArray` con un array tipizzato troppo piccolo solleva un'eccezione

È il compromesso CLDC descritto nella sezione
[Array tipizzati e assenza di reflection](#array-tipizzati-e-assenza-di-reflection).
