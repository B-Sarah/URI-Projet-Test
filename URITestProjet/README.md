# Objectif

L'objectif de ce projet est d'évaluer et d'améliorer la qualité de la classe `URI`, qui fait partie du projet [EMF](https://github.com/eclipse/emf). 
EMF (Eclipse Modeling Framework) est un cadre de modélisation, une infrastructure de génération de code et des applications basées sur des modèles de données structurées. 
Il est développé et maintenu par la fondation Eclipse et est utilisé par différents outils de modélisation, comme Acceleo, ATL ou GMF.
Il est au coeur de la plateforme Eclipse depuis la version 4.

# Travail à réaliser

Le projet sera réalisé individuellement ou en binômes. Les projets réalisés par des groupes de plus de 2 étudiants ne seront pas évalués.
Avant l'échéance, chaque groupe déposera ses livrables sur le serveur [Madoc](http://madoc.univ-nantes.fr/).
Les rapports seront rendus en format PDF, accompagné des ses sources (.tex, .doc, .jpg, etc.). 
Le code source des classes et des tests sera rendu en un fichier archive \texttt{tar.gz}.

L'archive \code{.jar} ainsi que les fichiers intermédiaires de compilation (*.class, etc.) ne doivent pas être rendus avec le code source.

# Préparation
Le code source de la classe `URI` se trouve sur GitLab de l'Université, sur le dépôt suivant:
[tp-est-uri](https://gitlab.univ-nantes.fr/sunye-g/tp-test-uri). 
Clonez ce projet pour commencer le projet?.

## Première étape

L'objectif de cette première étape est d'utiliser les techniques du test statique pour évaluer la qualité de la classe `URI`:
    - Réalisez une relecture du code de cette classe et listez les fautes et/ou les mauvaises pratiques que vous rencontrez.
    - Utilisez des outils d'analyse statique de code (PMD, FindBugs, Checkstyle) pour évaluer le code source de la classe sous test.
	- Préparez un rapport décrivant les résultats de l'évaluation ainsi que vos propositions d'amélioration de la suite de tests.


## Deuxième étape
L'objectif de cette deuxième étape est d'écrire des test unitaires. Comme vous allez modifier cette classe, vous aurez besoin d'une suite fiable de tests:
    - Utilisez la technique de test fonctionnel pour proposez des données de test pour les méthodes publiques de la classe `URI`.
    - Utilisez JUnit ou TestNG pour écrire les tests correspondant aux données de test précédentes.
    - Utilisez des outils de couverture de code (Emma, Cobertura) et d'analyse de mutation (PIT) pour évaluer la qualité de votre suite de tests

## Troisième étape
L'objectif de cette troisième étape est d'améliorer la qualité du code de la classe `URI`:
    - Utilisez les _refactorings_ pour réaliser les modifications proposées dans la première étape.
    - Assurez-vous que le comportement de la classe sous test reste le même.
    - Utilisez les outils d'analyse statique de code pour évaluer à nouveau la classe sous test.
    - Résumez, dans votre rapport, les résultats de l'analyse statique et des améliorations que vous avez effectuées.

# Travail à rendre

Travail à rendre le 12 décembre 2016:
    - Le rapport.
    - Le code de la ou des nouvelles classes.
