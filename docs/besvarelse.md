# Portfolio: Kompleksitet og datastrukturer

## Målemetode

Forsøgene er kørt den 19. november 2026 med OpenJDK 25 på macOS/aarch64.
Koden er kompatibel med Java 17. Det fulde output og alle rå målinger findes i
[run.txt](../results/run.txt) og [measurements.csv](../results/measurements.csv).

Tiden måles med `System.nanoTime()`. Hvert forsøg har tre opvarmningsrunder og
derefter fem målinger. Tabellerne viser medianen af de fem målinger.
En frisk datastruktur oprettes før hver runde, og oprettelsen ligger uden for
tidsmålingen. Resultater og størrelser kontrolleres efter målingen.

Listerne og heltalssættet indeholder de samme 500.000 værdier, fra 0 til 499.999.
De korte listeoperationer gentages 200 gange, indsættelse forrest gentages
10.000 gange, og hvert `contains`-forsøg gentages 1.000 gange.
Tiderne nedenfor er **millisekunder for hele gentagelsen**, ikke for ét kald.
Programoutputtet viser også batchtiden divideret med antal operationer som `ns/op`.

`ArrayList` får på forhånd kapacitet til 510.000 elementer. Det gør det muligt at
undersøge flytning af elementer uden samtidig at måle en udvidelse af arrayet.
`LinkedList` skal stadig oprette en node ved hver indsættelse, da det er en del
af selve operationen. Ved gentagne tilføjelser vokser listen, og ved gentagne
fjernelser bliver den mindre. Hver ny runde starter igen med 500.000 elementer.

Målingerne er undervisningseksempler. JVM-optimering, cache, garbage collection
og andre programmer påvirker tiderne. Et opsummeret resultat bruges efter hver
måling, men det udelukker ikke alle JVM-optimeringer. Især de hurtigste kald
påvirkes af løkke- og måleoverhead. En måling ved én størrelse beviser ikke en
Big O-kompleksitet: Big O beskriver væksten, når antallet af elementer ændres.
Tiderne skal derfor læses sammen med forklaringen af datastrukturens arbejde.

## 1. ArrayList vs LinkedList

### Resultater

| Operation | Gentagelser | ArrayList, ms i alt | LinkedList, ms i alt |
|---|---:|---:|---:|
| `get(250000)` | 200 | 0,006 | 84,634 |
| `add(250000, 99)` | 200 | 3,758 | 84,115 |
| `remove(250000)` | 200 | 3,751 | 85,473 |
| `add(0, 99)` | 10.000 | 379,114 | 0,066 |

### Kompleksitet pr. operation

| Operation | ArrayList | LinkedList |
|---|---|---|
| Hent i midten | O(1) | O(n) |
| Indsæt i midten med indeks | O(n) | O(n) |
| Fjern i midten med indeks | O(n) | O(n) |
| Indsæt forrest | O(n) | O(1) |

En `ArrayList` gemmer referencerne i et sammenhængende array. Et indeks giver
direkte adgang til den ønskede plads. Derfor er `get(250000)` O(1).
Ved indsættelse midt i listen skal de efterfølgende referencer flyttes én plads
til højre. Ved fjernelse flyttes de til venstre. Det giver O(n) arbejde.
Disse egenskaber stemmer med [Javas dokumentation for ArrayList](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ArrayList.html).

En `LinkedList` består af noder med forbindelser til den foregående og næste
node. Der er ingen direkte adgang via et indeks. Java starter fra den ende,
der er nærmest, og følger forbindelserne frem til den ønskede node.
For indeks 250.000 i en liste med 500.000 elementer skal der følges omkring
250.000 forbindelser. Det koster O(n), også ved indsættelse og fjernelse.
Selve ændringen af forbindelserne koster derefter O(1).
Se [Javas dokumentation for LinkedList](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/LinkedList.html).

Det er derfor utilstrækkeligt at sige, at indsættelse i en hægtet liste er O(1).
Det gælder, når positionen allerede er fundet, eksempelvis med en placeret
iterator. I forsøget bruges `add(index, value)`, så søgningen efter positionen
er en del af operationen.

Begge strukturer bruger O(n) tid til de to ændringer i midten, men `ArrayList`
er hurtigere i målingen. Big O siger ikke noget om den konstante faktor.
Samlet flytning af referencer i et array kan udføres effektivt, mens en hægtet
liste skal følge mange afhængige forbindelser mellem noder.

Forrest i listen vender resultatet. `ArrayList` skal flytte alle eksisterende
referencer ved hver indsættelse. `LinkedList` har allerede en reference til
første node og kan sætte en ny node ind med få ændringer af forbindelser.

For k indsættelser forrest i en liste, der starter med n elementer, flytter
`ArrayList` i alt omtrent `n + (n + 1) + ... + (n + k - 1)` referencer.
Det giver O(kn + k²), mens `LinkedList` bruger O(k) tid på de k indsættelser.

Begge implementerer `List`, fordi de tilbyder de samme operationer og samme
betydning af eksempelvis et indeks. Interfacet kræver ikke samme interne
repræsentation eller samme tidsforbrug.

## 2. Søgning: List vs HashSet

### Resultater

Hver række er 1.000 opslag i en struktur med 500.000 elementer.

| Opslag | ArrayList, ms i alt | HashSet, ms i alt |
|---|---:|---:|
| `contains(0)` – første element i listen | 0,027 | 0,035 |
| `contains(499999)` – sidste element i listen | 173,227 | 0,027 |
| `contains(-1)` – findes ikke | 173,534 | 0,028 |

`ArrayList.contains` undersøger elementerne fra starten og stopper ved et
match. Første element findes med én sammenligning: O(1). Sidste element
kræver 500.000 sammenligninger: O(n). Et manglende element kræver også en
gennemgang af alle 500.000 elementer: O(n).

**Både sidste element og et manglende element er worst case for listen.**
I begge tilfælde skal hele listen undersøges. De målte tider ligger også tæt.
Forskellen på de to tider ændrer ikke deres kompleksitet.

`HashSet` bruger en hashtabel. Hashværdien leder frem til en bucket, hvor
eventuelle kandidater undersøges. Med en god fordeling af hashværdier er
`contains` forventet O(1) i alle tre tilfælde. Sættet har ingen første eller
sidste position svarende til listens rækkefølge. Kollisioner kan gøre et opslag
dyrere, så O(1) er ikke en ubetinget garanti for alle input.
Det fremgår af [Javas dokumentation for HashSet](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/HashSet.html).

Ved et match på første plads skal listen næsten intet arbejde udføre og var
lidt hurtigere i denne kørsel. Fordelen ved hashing ses tydeligt, når listen
ellers skulle gennemgå mange elementer. O(1) betyder ikke, at alle konstante
operationer tager samme tid.

## 3. Comparable, equals() og hashCode()

### Entitetsklassen

[Student.java](../src/main/java/complexity/Student.java) har et `id` og et navn.
Et id identificerer en studerende, så to instanser med samme id repræsenterer
samme entitet, også hvis navnene er forskellige. Klassen er `final`, og felterne
er `final`, så identiteten ikke ændres, mens objektet ligger i et sæt.

De tre metoder bruger samme id:

```java
@Override
public int compareTo(Student other) {
    return Integer.compare(id, other.id);
}

@Override
public boolean equals(Object other) {
    if (this == other) {
        return true;
    }
    return other instanceof Student student && id == student.id;
}

@Override
public int hashCode() {
    return Integer.hashCode(id);
}
```

Programmet tilføjer id 42, 7 og 18 til begge sæt. En ny instans med id 7
afvises som dublet. En anden instans med id 18 findes med `contains` og fjernes
med `remove`. Det viser, at objekterne genkendes ud fra identitetens værdi,
selvom der bruges forskellige objektreferencer.

### Kompleksitet og tidsmåling

| Operation | TreeSet | HashSet |
|---|---|---|
| `add` | O(log n) | Forventet amortiseret O(1) |
| `contains` | O(log n) | Forventet O(1) |
| `remove` | O(log n) | Forventet O(1) |

`TreeSet` bruger et balanceret søgetræ. Arbejdet følger en sti i træet, hvis
højde er O(log n). Træet bevarer sorteringen, også efter ændringer.
`HashSet` bruger hashværdien til at finde en bucket. En udvidelse af
hashtabellen kan gøre en enkelt indsættelse dyrere, men udgiften fordeles over
mange indsættelser. Se dokumentationen for
[TreeSet](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/TreeSet.html)
og [HashSet](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/HashSet.html).

Tidsforsøget bruger 100.000 studerende og måler hver operation separat:

| Operation, 100.000 kald | TreeSet, ms i alt | HashSet, ms i alt |
|---|---:|---:|
| Tilføj alle objekter | 18,694 | 2,558 |
| Find alle objekter | 19,229 | 0,569 |
| Fjern alle objekter | 19,255 | 1,204 |

Ved tilføjelse starter sættet tomt. Ved søgning og fjernelse er det fyldt inden
målingen. Objekterne oprettes på forhånd, og rækkefølgen blandes med faste
random seeds. Opslag og fjernelser bruger nye instanser med de samme id'er.
`HashSet` må gerne udvide sig under de målte indsættelser.

Tiderne er for n kald. Samlet er tilføjelse, opslag af alle elementer og
fjernelse af alle elementer O(n log n) i `TreeSet`, mens de er forventet O(n)
i `HashSet` under forudsætning af god hashing. HashSet var hurtigere i alle
tre forsøg. Til gengæld tilbyder TreeSet en sorteret rækkefølge.

### Hvilke metoder bruger sættene?

`TreeSet` bruger i dette eksempel `compareTo` til at placere og finde objekter.
Resultatet 0 betyder, at objektet allerede findes i sættets forstand.
Det bruger ikke `equals` eller `hashCode` til disse opslag. Et TreeSet kan
alternativt oprettes med en `Comparator`.

`HashSet` bruger `hashCode` til at finde en bucket og `equals` til at afgøre,
om en kandidat er samme entitet. `compareTo` definerer ikke medlemskab i et
HashSet. TreeSet gav rækkefølgen 7, 18, 42 i forsøget, mens HashSet gav
18, 7, 42. HashSet garanterer ingen sortering; andre kørsler eller andre id'er
kan give en anden rækkefølge.

### Hvad skal metoderne overholde?

- `equals` skal være refleksiv, symmetrisk, transitiv og konsistent.
  Et objekt skal ikke være lig med `null`.
- Hvis `a.equals(b)` er sand, skal de have samme `hashCode`.
  Den modsatte slutning gælder ikke: forskellige objekter må godt have samme hash.
- `compareTo` skal give et negativt tal, 0 eller et positivt tal, alt efter
  sorteringen. Sorteringen skal være transitiv, og fortegnet skal vendes,
  når objekterne byttes om.
- `compareTo` bør være konsistent med `equals`:
  `a.compareTo(b) == 0` skal svare til `a.equals(b)`. Ellers kan TreeSet og
  HashSet blive uenige om, hvad der er en dublet.
- Felter, som bestemmer identitet, hash eller sortering, må ikke ændres, mens
  objektet ligger i sættet. Ellers kan objektet ligge et sted, hvor et nyt
  opslag ikke finder det.

`Integer.compare` bruges frem for `id - other.id`, fordi subtraktion kan
give overflow og forkert sortering. Kontrollerne dækker også
`Integer.MIN_VALUE` og `Integer.MAX_VALUE`.
Kontrakterne er beskrevet i dokumentationen for
[Object.equals/hashCode](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html)
og [Comparable](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Comparable.html).

## 4. PriorityQueue og ArrayDeque

Her betyder »tage ud«, at det næste element hentes **og fjernes**.

| Struktur | Sætte ind | Tage ud | Hvilket element kommer ud? |
|---|---|---|---|
| `PriorityQueue` | `offer` / `add`: O(log n) | `poll`: O(log n) | Det mindste ifølge sorteringen |
| `ArrayDeque` som kø | `offerLast`: amortiseret O(1) | `pollFirst`: O(1) | Det først indsatte, FIFO |
| `ArrayDeque` som stak | `push`: amortiseret O(1) | `pop`: O(1) | Det sidst indsatte, LIFO |

Tabellen følger klassebeskrivelserne i Javas dokumentation for
[PriorityQueue](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/PriorityQueue.html)
og [ArrayDeque](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ArrayDeque.html).
`PriorityQueue.peek()` er O(1), fordi elementet bliver liggende.
`PriorityQueue.remove(Object)` er O(n), da et bestemt objekt først skal findes.
Tabellen handler om udtagning af det næste element, ikke søgning efter et objekt.

### PriorityQueue

En `PriorityQueue` bruger en heap. Den mindste værdi ligger ved roden ved
naturlig sortering; en `Comparator` kan definere en anden prioritet.
Ved indsættelse skal den nye værdi muligvis flyttes op gennem heapen.
Ved udtagning erstattes roden med det sidste element, som derefter muligvis
skal flyttes ned. Heapens højde er O(log n), så der er højst et logaritmisk
antal trin for at genskabe heapegenskaben. Hele samlingen er ikke sorteret;
egenskaben sikrer blot, at næste prioritet står klar ved roden.
En intern arrayudvidelse kan desuden gøre en enkelt indsættelse dyrere;
tabellens O(log n) er den dokumenterede heap-kompleksitet, med udvidelser
fordelt over flere indsættelser.

### ArrayDeque som kø og stak

En `ArrayDeque` bruger et cirkulært array med positioner til de to ender.
Som kø tilføjes elementet i den ene ende og fjernes i den anden. Som stak
tilføjes og fjernes elementet i samme ende. Normalt skal kun én plads og
endens position ændres. De andre elementer skal ikke flyttes ved hvert kald.

Hvis arrayet bliver fuldt, oprettes et større array, og elementerne flyttes.
Den enkelte udvidelse er O(n). **Amortiseret O(1)** betyder, at den samlede
udgift over mange indsættelser giver en konstant udgift pr. indsættelse.
Det betyder ikke, at alle enkelte kald er lige hurtige, og det er ikke en
antagelse om tilfældige input. Udtagning fra en ende kræver ingen udvidelse
og er O(1).

### Hvorfor er der forskel på de to køer?

En almindelig kø skal bevare indsættelsesrækkefølgen. En prioritetskø skal
kunne vælge efter prioritet, også når et nyligt indsat element har højere
prioritet end alle de gamle. Det kræver arbejde med heapens rækkefølge.

Programmet indsætter 30, 10 og 20 og viser:

```text
PriorityQueue poll(): [10, 20, 30]
ArrayDeque som kø poll(): [30, 10, 20]
ArrayDeque som stak pop(): [20, 10, 30]
```

Det er også grunden til valget i grafalgoritmerne: Dijkstra og A* vælger næste
kandidat efter en beregnet prioritet. BFS bruger en FIFO-kø til at undersøge
lagene i rækkefølge, mens DFS kan bruge en LIFO-stak til at fortsætte i dybden.
