package fr.insalyon.pldagile.sortie;

import fr.insalyon.pldagile.modele.*;
import java.util.List;

/**
 * DTO (Data Transfer Object) représentant les modifications liées à une demande de livraison.
 * Contient la demande initiale ainsi que la liste des tournées affectées ou modifiées.
 */
public class ModificationsDTO {

    /** La demande de livraison concernée par les modifications. */
    private DemandeDeLivraison demande;

    /** La liste des tournées éventuellement impactées par ces modifications. */
    private List<Tournee> tournees;

    /** Constructeur par défaut. */
    public ModificationsDTO() {}

    /**
     * Constructeur avec initialisation des champs.
     * @param demande la demande de livraison modifiée
     * @param tournees les tournées impactées
     */
    public ModificationsDTO(DemandeDeLivraison demande, List<Tournee> tournees) {
        this.demande = demande;
        this.tournees = tournees;
    }

    /** @return la demande de livraison concernée */
    public DemandeDeLivraison getDemande() { return demande; }

    /** @param demande la demande de livraison à définir */
    public void setDemande(DemandeDeLivraison demande) { this.demande = demande; }

    /** @return la liste des tournées impactées */
    public List<Tournee> getTournees() { return tournees; }

    /** @param tournees la liste des tournées à définir */
    public void setTournees(List<Tournee> tournees) { this.tournees = tournees; }
}
