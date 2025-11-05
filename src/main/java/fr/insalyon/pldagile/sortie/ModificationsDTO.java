package fr.insalyon.pldagile.sortie;
import fr.insalyon.pldagile.modele.*;

import java.util.List;

public class ModificationsDTO {
    private DemandeDeLivraison demande;
    private List<Tournee> tournees;

    public ModificationsDTO() {}
    public ModificationsDTO(DemandeDeLivraison demande, List<Tournee> tournees) {
        this.demande = demande;
        this.tournees = tournees;
    }

    public DemandeDeLivraison getDemande() { return demande; }
    public void setDemande(DemandeDeLivraison demande) { this.demande = demande; }

    public List<Tournee> getTournees() { return tournees; }
    public void setTournees(List<Tournee> tournees) { this.tournees = tournees; }
}
