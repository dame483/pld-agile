
# pld-agile

# Prérequis
Avant de lancer le projet, assurez-vous d’avoir installé :
[Java Development Kit (JDK) – version 17 ou supérieure](https://lp.jetbrains.com/intellij-idea-promo/?source=google&medium=cpc&campaign=EMEA_en_FR_IDEA_Search&term=java%20jdk%20download&content=602400219725&gad_source=1&gad_campaignid=9730750225&gbraid=0AAAAADloJzix9EwMMkNYgzI_oreXQ7C3k&gclid=EAIaIQobChMIz9-c4ITckAMVwmhBAh02VhRqEAAYASAAEgJkNPD_BwE) recommandée 

[Apache Maven](https://maven.apache.org/install.html) – outil de gestion de dépendances et de construction du projet

# Téléchargement des dépendances et compilation
Cette commande `mvn clean install` nettoie le projet, télécharge toutes les dépendances nécessaires (Spring Boot, etc.) et compile le code source 


# Exécution du backend

Une fois la compilation terminée, vous pouvez lancer le serveur ackend Spring Boot :

mvn spring-boot:run

Le serveur démarre par défaut sur http://localhost:8080/

Si vous avez une interface web, elle sera servie automatiquement à cette adresse si elle est placée dans le dossier `src/main/resources/static`

# Lancement des tests

Pour exécuter les tests unitaires, lancez cette commande :

`mvn -q test`

# Exécution via le fichier JAR

Après la commande `mvn clean install`, un fichier JAR exécutable est généré. Vous pouvez le run directement sans Maven ni IDE:

`java -jar target/pld-agile-0.0.1-SNAPSHOT.jar`



