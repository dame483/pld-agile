package fr.insalyon.pldagile.configuration;


import fr.insalyon.pldagile.modele.Carte;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Carte carte() {
        return new Carte();
    }
}

