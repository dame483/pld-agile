package fr.insalyon.pldagile.modele;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Carte {
    private HashMap<Long, Noeud> noeuds = new HashMap();
    private List<Troncon> troncons = new ArrayList();

    public void AjouterNoeud(Noeud n) {
        this.noeuds.put(n.getId(), n);
    }

    public void AjouterTroncon(Troncon t) {
        this.troncons.add(t);
    }

    public HashMap<Long, Noeud> getNoeuds() {
        return this.noeuds;
    }

    public List<Troncon> getTroncons() {
        return this.troncons;
    }

    public Troncon getTronconEntre(long idOrigine, long idDestination) {
        for (Troncon t : troncons) {
            if (t.getIdOrigine() == idOrigine && t.getIdDestination() == idDestination) {
                return t; // renvoie le premier tronçon trouvé
            }
        }
        return null; // aucun tronçon direct trouvé
    }

}