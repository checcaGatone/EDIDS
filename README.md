PROGETTO ELEMENTI DI INGEGNERIA DEL SOFTWARE
Barban filippo matricola: 2147711

Il codice rispetta i vincoli di compatibilità e risolve le problematiche strutturali legate alla propagazione ricorsiva delle variazioni topologiche nelle sotto-mappe.

Il progetto è stato sviluppato seguendo l'approccio Test Driven Development: i test (con relativi Javadoc) sono stati redatti prima dell'implementazione dell'Adapter.

STRUTTURA DEL PROGETTO 
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

COMPILAZIONE DEL CODICE 
javac -d bin myAdapter/*.java
javac -cp "bin;JUnit/junit-4.13.jar;JUnit/hamcrest-core-1.3.jar" -d bin myTest/*.java
java -cp "bin;JUnit/junit-4.13.jar;JUnit/hamcrest-core-1.3.jar" myTest.TestRunner
 
APERTURA JAVADOCS 
Start-Process .\doc\completo\index.html

OSSERVAZIONE
J2SE 1.4.2 prescrive che `toArray(Object[])`, quando riceve un array troppo
piccolo, crei un nuovo array dello stesso tipo runtime. La creazione generica
di tale array richiederebbe reflection, non disponibile nel profilo CLDC 1.1
adottato dal progetto.

OUTPUT
In caso di esecuzione corretta, il programma mostrerà a terminale il report riepilogativo 

Test eseguiti: 99
Test superati: 99
Test falliti: 0
Test ignorati: 0
Tempo impiegato: 318 ms
Risultato: SUCCESSO
