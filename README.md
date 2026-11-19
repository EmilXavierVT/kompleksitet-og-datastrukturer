# Portfolio: Kompleksitet og datastrukturer

Java-løsning og skriftlig besvarelse til portfolioopgaven om datastrukturer.

**Start med [besvarelsen](docs/besvarelse.md).** Den indeholder de målte resultater,
kompleksitetstabellerne og svar på alle spørgsmål i [opgaven](docs/opgave.md).

## Kør projektet

Du skal have en JDK med Java 17 eller nyere. Der er ingen eksterne biblioteker.

```bash
./run.sh --check
./run.sh
```

Første kommando kontrollerer kontrakterne for `Student` og brugen af klassen i
`TreeSet` og `HashSet`. Anden kommando kompilerer og kører alle forsøg.
Hver måleserie har tre opvarmningsrunder og fem gemte målinger. Programmet viser
medianen og gemmer alle målinger i `results/measurements.csv`.

Resultaterne varierer fra maskine til maskine og mellem kørsler. En ny kørsel
overskriver CSV-filen. Den skriftlige besvarelse beskriver den gemte kørsel fra
19. november 2026; dens tabeller opdateres ikke automatisk.

Gem eventuelt en ny kørsel separat:

```bash
./run.sh results/new-measurements.csv > results/new-run.txt
```

På Windows kan klasserne køres fra IntelliJ: åbn `pom.xml`, vælg en JDK fra
version 17 og kør `complexity.ComplexityDemo`. Kør fra projektets rodmappe,
så resultatfilerne havner det samme sted. `StudentChecks.main()` kan også køres
direkte. Maven-filen gør projektet let at importere; `run.sh` bruger kun JDK'en.

## Filer

| Fil | Indhold |
|---|---|
| [docs/besvarelse.md](docs/besvarelse.md) | Skriftlig besvarelse med resultater og kilder |
| [docs/opgave.md](docs/opgave.md) | Den udleverede opgave |
| [ComplexityDemo.java](src/main/java/complexity/ComplexityDemo.java) | Forsøg med lister, søgning, sæt og køer |
| [Student.java](src/main/java/complexity/Student.java) | Entitet med `Comparable`, `equals` og `hashCode` |
| [Benchmark.java](src/main/java/complexity/Benchmark.java) | Opvarmning, tidsmåling, median og CSV |
| [StudentChecks.java](src/test/java/complexity/StudentChecks.java) | Kontrol af entitetens kontrakter, også ekstreme id'er |
| [results/run.txt](results/run.txt) | Det gemte programoutput med kørselsmiljø |
| [results/measurements.csv](results/measurements.csv) | Alle 100 rå målinger fra den gemte kørsel |

## Hurtig gennemgang

1. Læs kompleksitetstabellerne og forklaringen på `LinkedList` i besvarelsen.
2. Se, at alle tre identitetsmetoder i `Student` bruger samme `id`.
3. Kør `./run.sh --check` og derefter forsøgene, hvis du vil sammenligne egne tider.
4. Gennemgå målemetodens begrænsninger og forklar forskellen på O(1), O(log n)
   og O(n) med dine egne ord.

Valideret med OpenJDK 25 på macOS/aarch64. Koden kompileres med `--release 17`.
