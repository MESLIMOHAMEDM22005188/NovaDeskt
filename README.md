# NovaDeskt

Application desktop de gestion de projets construite avec Java 21,
JavaFX, Maven et SQLite.

## Architecture

- model : objets métier
- view : interfaces FXML et styles CSS
- controller : actions et liaison avec les services
- service : règles métier et validations
- repository : accès aux données
- config : configuration et initialisation SQLite

## Lancement

Dans IntelliJ, rechargez le projet Maven puis lancez la cible
javafx:run depuis la fenêtre Maven.

La base locale est créée au premier démarrage dans data/novadeskt.db.
