package fr.insalyon.pldagile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale de l'application Spring Boot {@code PldAgileApplication}.
 *
 * <p>Cette classe contient le point d'entrée de l'application et démarre
 * le contexte Spring Boot.</p>
 *
 * <p>Elle est annotée avec {@link SpringBootApplication}, ce qui inclut :
 * <ul>
 *   <li>{@link org.springframework.boot.autoconfigure.SpringBootApplication} pour la configuration automatique</li>
 *   <li>La détection automatique des composants dans le package courant et ses sous-packages</li>
 * </ul>
 *
 */
@SpringBootApplication
public class PldAgileApplication {

    /**
     * Point d'entrée de l'application Spring Boot.
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(PldAgileApplication.class, args);
    }
}
