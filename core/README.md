
# Tâche 2 – IFT3913 : Qualité du logiciel et métriques

## Équipe
- Yasmine Ben Youssef (20237210)
- Hazem Ben Amor (20236062)

## Choix du de la classe
#### Classe : `com.graphhopper.util.StopWatch`

La classe `StopWatch` a été choisie pour sa simplicité, sa précision et sa flexibilité, ce qui la rend idéale pour mesurer les performances dans des projets comme GraphHopper.Les tests unitaires réalisés confirment sa robustesse. De plus elle est légère et absente de dépendances externes.


---

## Documentation des tests
#### Test 1 : `testStartAndStopShouldIncreaseTime()`

**Intention du test :**
Ce test vérifie que quand on démarre le chronomètre et qu’on l’arrête, il mesure bien un temps positif.

**Oracles :** Le comportement attendu est que le temps mesuré par le chronomètre soit strictement positif après un appel à **start()** suivi de **stop()**. Cela repose sur l'hypothèse que l'horloge système **(System.nanoTime())** avance toujours et que le temps écoulé entre les deux appels est non nul.


---

#### Test 2 : `testGetSecondsOracleComparison()`

**Intention du test :**
Ce test compare les deux méthodes de mesure : **getNanos()** et **getSeconds()**.

**Oracles :** Le comportement attendu est que la méthode **getSeconds()** retourne une valeur cohérente avec **getNanos() / 1e9**. L'assertion compare ces deux valeurs avec une tolérance **(1e-9)** pour tenir compte des imprécisions dues aux calculs en virgule flottante

---

#### Test 3 : `testGetCurrentSecondsWhileRunning()`

**Intention du test :**
On teste la méthode **getCurrentSeconds()** pendant que le chrono tourne pour prouver que le temps augmente tant que le chrono n’est pas arrêté

**Oracles :**
Le comportement attendu est que la valeur retournée par **getCurrentSeconds()** augmente pendant que le chronomètre est en cours d'exécution. Cela est vérifié en comparant deux appels successifs à **getCurrentSeconds()** avec un délai entre eux.

---

#### Test 4 : `testGetTimeStringCoversAllUnits()`

**Intention du test :**
Ce test vérifie que **getTimeString()** affiche bien une unité de temps correcte.

**Oracles :**
Le comportement attendu est que la méthode **getTimeString()** retourne une chaîne contenant une unité de temps valide (ns, µs, ms, ou s). Cela est déterminé en vérifiant que la chaîne contient l'une de ces unités, en fonction de la durée mesurée.


---

#### Test 5 : `testToStringIncludesNameIfPresent()`

**Intention du test :**
Ce test vérifie que si on donne un nom au chronomètre (ici "Timer")

**Oracles :**
Le comportement attendu est que la méthode **toString()** inclut le nom passé au constructeur si un nom est défini. Cela est vérifié en s'assurant que la sortie commence par le nom donné.

---

#### Test 6 : `testStopWithoutStartDoesNotCrash()`

**Intention du test :**
Ce test vérifie qu’on peut appeler **stop()** même sans avoir appelé **start()** avant.

**Oracles :**
Le comportement attendu est que l'appel à **stop()** avant **start()** ne provoque pas d'erreur et que le temps mesuré reste valide (supérieur ou égal à zéro). Cela repose sur la gestion correcte des états internes de la classe.

---

#### Test 7 : `testWithFakerGeneratedName()`

**Intention du test :**
Ce test Vérifie que le nom aléatoire crée par un faker se retrouve bien dans la sortie texte
**Oracles :**
Le comportement attendu est que le nom aléatoire généré par Faker apparaisse dans la sortie de la méthode **toString()**. Cela est vérifié en s'assurant que la chaîne retournée contient le nom généré ou une partie standard de la sortie **(time:)**.


## Pitest et ses mutants

### Vue d'ensemble globale
#### `com.graphhopper.util.StopWatch`
### Avant
| Métrique |  | 
|----------|-------|
| **Line Coverage** | 24% (10/42) | 
| **Mutation Coverage** | 3% (2/65) |
| **Test Strength** | 25% (2/8) |

### Après
| Métrique |  | 
|----------|-------|
| **Line Coverage** | 64% (27/342) | 
| **Mutation Coverage** | 28% (18/65) |
| **Test Strength** | 49% (18/37) |

### Évolution
| Métrique | Évolution |
|----------|-----------|
| **Line Coverage** | + 40% |
| **Mutation Coverage** | + 25% |
| **Test Strength** | + 24% |

---

### Détails
- Avant, `StopWatch` était très peu sollicité par les tests → beaucoup de mutants survivaient.
- Nos 7 tests tuent 16 mutants de plus (2 → 18), principalement :
- Calculs (conversion ns→s via l’oracle du **Test 2**),
- Valeurs en cours (**Test 3** : progression du temps),
- Formatage (**Test 4** : unités cohérentes).
- Les mutants restants concernent surtout des chemins longs de formatage (minutes/heures) et quelques subtilités textuelles qui demanderaient soit des délais plus grands, soit un “mock du temps”


## Java Faker
L'utilisation de la bibliothèque Faker dans les tests unitaires est justifiée par sa capacité à générer des données aléatoires réalistes, ce qui permet de tester des cas variés et imprévisibles. Dans le contexte de la classe StopWatch, Faker est utilisé pour créer des noms aléatoires via la méthode `faker.app().name()`, ce qui garantit que les tests ne se limitent pas à des valeurs statiques ou prédéfinies. Cela renforce la robustesse des tests en vérifiant que la méthode `toString()` inclut correctement un nom généré dynamiquement, peu importe sa valeur. De plus, Faker simplifie la création de données de test tout en rendant les tests plus représentatifs de scénarios réels, sans nécessiter d'efforts supplémentaires pour définir manuellement des données variées
