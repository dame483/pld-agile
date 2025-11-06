package fr.insalyon.pldagile.configuration;


import fr.insalyon.pldagile.modele.Carte;
import fr.insalyon.pldagile.modele.DemandeDeLivraison;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration principale de l'application Spring.
 * <p>
 * Cette classe définit les beans centraux utilisés dans l'application,
 * notamment la {@link fr.insalyon.pldagile.modele.Carte} et la {@link fr.insalyon.pldagile.modele.DemandeDeLivraison}.
 * Elle est annotée avec {@code @Configuration}, ce qui permet à Spring
 * de la détecter et d'enregistrer les beans retournés par les méthodes annotées {@code @Bean}.
 * </p>
 */
@Configuration
public class AppConfig {

    /**
     * Crée et fournit une instance vide de {@link fr.insalyon.pldagile.modele.Carte}.
     * <p>
     * Ce bean représente la carte de la ville utilisée pour le calcul des chemins
     * et la gestion des intersections, tronçons et livraisons.
     * </p>
     *
     * @return une nouvelle instance de {@link Carte}
     */
    @Bean
    public Carte carte() {
        return new Carte();
    }

    /**
     * Crée et fournit une instance vide de {@link fr.insalyon.pldagile.modele.DemandeDeLivraison}.
     * <p>
     * Cette demande regroupe l'entrepôt de départ et les livraisons à effectuer.
     * Elle est initialisée ici avec des valeurs nulles, mais sera configurée
     * dynamiquement par la suite à partir des fichiers XML chargés.
     * </p>
     *
     * @return une nouvelle instance de {@link DemandeDeLivraison}
     */
    @Bean
    public DemandeDeLivraison demandeDeLivraison() { return new DemandeDeLivraison(null, null);
    }
}

