package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsable de la modification des tournées dans le système de livraison.
 *
 * <p>Elle permet d'ajouter ou de supprimer des nœuds de type pickup ou delivery
 * dans une {@link Tournee}, et met à jour les chemins, durées et horaires en conséquence.</p>
 */
public class ModificationTournee {

    /** Instance de {@link CalculChemins} utilisée pour calculer les chemins entre les nœuds. */
    private final CalculChemins calculChemins;

    /** Vitesse moyenne utilisée pour le calcul des durées de déplacement. */
    private final double vitesse;

    /**
     * Constructeur de {@code ModificationTournee}.
     *
     * @param calculChemins l'objet de calcul des chemins
     * @param vitesse la vitesse moyenne pour les calculs de durée
     */
    public ModificationTournee(CalculChemins calculChemins, double vitesse) {
        this.calculChemins = calculChemins;
        this.vitesse = vitesse;
    }

    /**
     * Ajoute un nœud de type pickup à la tournée.
     *
     * @param tournee la tournée à modifier
     * @param idNoeudAjoute l'identifiant du nœud à ajouter
     * @param idNoeudPrecedent l'identifiant du nœud précédent où insérer le nouveau nœud
     * @param dureeEnlevement durée de l'enlèvement au nœud
     * @return la tournée modifiée
     */
    public Tournee ajouterNoeudPickup(Tournee tournee, long idNoeudAjoute, long idNoeudPrecedent, double dureeEnlevement) {
        return ajouterNoeud(tournee, idNoeudAjoute, idNoeudPrecedent, dureeEnlevement, true);
    }

    /**
     * Ajoute un nœud de type delivery à la tournée.
     *
     * @param tournee la tournée à modifier
     * @param idNoeudAjoute l'identifiant du nœud à ajouter
     * @param idNoeudPrecedent l'identifiant du nœud précédent où insérer le nouveau nœud
     * @param dureeLivraison durée de livraison au nœud
     * @return la tournée modifiée
     */
    public Tournee ajouterNoeudDelivery(Tournee tournee, long idNoeudAjoute, long idNoeudPrecedent, double dureeLivraison) {
        return ajouterNoeud(tournee, idNoeudAjoute, idNoeudPrecedent, dureeLivraison, false);
    }

    /**
     * Ajoute un nœud à la tournée et met à jour les chemins, durées et horaires.
     *
     * @param tournee la tournée à modifier
     * @param idNoeudAjoute l'identifiant du nœud à ajouter
     * @param idNoeudPrecedent l'identifiant du nœud précédent où insérer le nouveau nœud
     * @param dureeOperation durée de l'opération au nœud
     * @param isPickup {@code true} si le nœud est un pickup, {@code false} si c'est un delivery
     * @return la tournée modifiée
     */
    public Tournee ajouterNoeud(Tournee tournee, long idNoeudAjoute, long idNoeudPrecedent, double dureeOperation, boolean isPickup)
    {
        // Création du noeud
        NoeudDePassage noeudAjoute = isPickup
                ? creerNoeudDePassagePickup(idNoeudAjoute, dureeOperation)
                : creerNoeudDePassageDelivery(idNoeudAjoute, dureeOperation);

        NoeudDePassage noeudPrecedent = recupererNoeud(tournee, idNoeudPrecedent);
        if (noeudPrecedent == null) {
            System.out.println("Les noeuds ajoutés ne sont pas récupérés correctement");
            return tournee;
        }

        // Trouver le chemin qui vient après le noeud précédent
        Chemin[] cheminsAutour = trouverCheminsAutourNoeud(tournee, noeudPrecedent);
        Chemin cheminApres = cheminsAutour[1];
        NoeudDePassage noeudSuivant = (cheminApres != null)
                ? cheminApres.getNoeudDePassageArrivee()
                : null;

        // Trouver l’index où insérer les nouveaux chemins
        int indexApres = (cheminApres != null)
                ? tournee.getChemins().indexOf(cheminApres)
                : tournee.getChemins().size();


        supprimerChemin(tournee, cheminApres);
        recalculerChemin(tournee, noeudPrecedent, noeudAjoute, indexApres);
        recalculerChemin(tournee, noeudAjoute, noeudSuivant, indexApres + 1);

        mettreAJourTotaux(tournee);
        mettreAJourHoraires(tournee, vitesse);

        return tournee;
    }


    /**
     * Supprime un nœud de la tournée et met à jour les chemins, durées et horaires.
     *
     * @param tournee la tournée à modifier
     * @param idNoeud l'identifiant du nœud à supprimer
     * @return la tournée modifiée
     */
    public Tournee supprimerNoeud(Tournee tournee, long idNoeud) {
        NoeudDePassage n = recupererNoeudSuppression(tournee, idNoeud);
        if (n == null) return tournee;

        Chemin avant, apres;
        int index;
        Chemin[] cheminsAutour = trouverCheminsAutourNoeud(tournee, n);
        avant = cheminsAutour[0];
        apres = cheminsAutour[1];
        index = (avant != null) ? tournee.getChemins().indexOf(avant) : 0;

        supprimerChemin(tournee, avant);
        supprimerChemin(tournee, apres);

        NoeudDePassage precedent = (avant != null) ? avant.getNoeudDePassageDepart() : null;
        NoeudDePassage suivant = (apres != null) ? apres.getNoeudDePassageArrivee() : null;

        if (precedent != null && suivant != null) {
            recalculerChemin(tournee, precedent, suivant, index);
        }

        mettreAJourTotaux(tournee);
        mettreAJourHoraires(tournee, vitesse);

        return tournee;
    }

    /**
     * Récupère un nœud pour suppression en s'assurant qu'il n'est pas le dépôt.
     *
     * @param tournee la tournée
     * @param idNoeud l'identifiant du nœud
     * @return le nœud à supprimer ou {@code null} si impossible
     */
    private NoeudDePassage recupererNoeudSuppression(Tournee tournee, long idNoeud) {
        NoeudDePassage n = tournee.getNoeudParId(idNoeud);
        if (n == null) return null;
        if (!tournee.getChemins().isEmpty()) {
            NoeudDePassage entrepot = tournee.getChemins().get(0).getNoeudDePassageDepart();
            if (n.equals(entrepot)) return null;
        }
        return n;
    }

    /**
     * Récupère un nœud existant dans la tournée.
     *
     * @param tournee la tournée
     * @param idNoeud l'identifiant du nœud
     * @return le nœud correspondant ou {@code null} si non trouvé
     */
    private NoeudDePassage recupererNoeud(Tournee tournee, long idNoeud) {
        NoeudDePassage n = tournee.getNoeudParId(idNoeud);
        if (n == null) return null;
        return n;
    }

    /**
     * Trouve les chemins avant et après un nœud donné.
     *
     * @param tournee la tournée
     * @param n le nœud de référence
     * @return un tableau avec le chemin précédent et le chemin suivant
     */
    private Chemin[] trouverCheminsAutourNoeud(Tournee tournee, NoeudDePassage n) {
        Chemin avant = null, apres = null;
        for (Chemin c : tournee.getChemins()) {
            if (c.getNoeudDePassageArrivee().equals(n)) avant = c;
            if (c.getNoeudDePassageDepart().equals(n)) apres = c;
        }
        return new Chemin[]{avant, apres};
    }

    /**
     * Supprime un chemin spécifique de la tournée.
     *
     * @param tournee la tournée
     * @param chemin le chemin à supprimer
     */
    private void supprimerChemin(Tournee tournee, Chemin chemin) {
        if (chemin != null) {
            tournee.getChemins().removeIf(c -> c.equals(chemin));
        }
    }

    /**
     * Recalcule un chemin entre deux nœuds et l'insère à l'index donné.
     *
     * @param tournee la tournée
     * @param precedent nœud de départ
     * @param suivant nœud d'arrivée
     * @param index position dans la liste des chemins
     */
    private void recalculerChemin(Tournee tournee, NoeudDePassage precedent, NoeudDePassage suivant, int index) {
        Chemin nouveauChemin = calculChemins.calculerCheminPlusCourt(precedent, suivant);
        if (nouveauChemin == null) {
            throw new IllegalStateException("Impossible de recalculer le chemin entre les nœuds "
                    + precedent.getId() + " et " + (suivant != null ? suivant.getId() : "null"));
        }
        tournee.getChemins().add(index, nouveauChemin);
    }

    /**
     * Met à jour les longueurs et durées totales de la tournée.
     *
     * @param tournee la tournée
     */
    private void mettreAJourTotaux(Tournee tournee) {
        double longueur = 0, duree = 0;
        for (Chemin c : tournee.getChemins()) {
            longueur += c.getLongueurTotal();
            duree += c.getLongueurTotal() / vitesse;
            duree += c.getNoeudDePassageArrivee().getDuree();
        }
        tournee.setLongueurTotale(longueur);
        tournee.setDureeTotale(duree);
    }


    /**
     * Met à jour les horaires de départ et d'arrivée pour tous les nœuds.
     *
     * @param tournee la tournée
     * @param vitesse vitesse moyenne
     */
    private void mettreAJourHoraires(Tournee tournee, double vitesse) {
        LocalTime heureCourante = tournee.getChemins().get(0).getNoeudDePassageDepart().getHoraireDepart();
        List<Chemin> chemins = tournee.getChemins();

        for (int i = 0; i < chemins.size(); i++) {
            Chemin c = chemins.get(i);
            c.getNoeudDePassageDepart().setHoraireDepart(heureCourante);
            heureCourante = heureCourante.plusSeconds((long)(c.getLongueurTotal() / vitesse));
            c.getNoeudDePassageArrivee().setHoraireArrivee(heureCourante);
            heureCourante = heureCourante.plusSeconds((long)(c.getNoeudDePassageArrivee().getDuree()));

            if (i != chemins.size() - 1) {
                c.getNoeudDePassageArrivee().setHoraireDepart(heureCourante);
            }
        }
    }

    /**
     * Crée un nœud de passage de type pickup.
     *
     * @param idNoeud identifiant du nœud
     * @param dureeEnlevement durée de l'enlèvement
     * @return le nœud de passage pickup créé
     */
    private NoeudDePassage creerNoeudDePassagePickup(long idNoeud, double dureeEnlevement) {
        Noeud noeudAAjouter = calculChemins.getCarte().getNoeudParId(idNoeud);

        long id = noeudAAjouter.getId();
        double latitude = noeudAAjouter.getLatitude();
        double longitude = noeudAAjouter.getLongitude();
        NoeudDePassage noeudDePassagePickupAAjouter = new NoeudDePassage(id, latitude, longitude, NoeudDePassage.TypeNoeud.PICKUP, dureeEnlevement);
        return noeudDePassagePickupAAjouter;
    }

    /**
     * Crée un nœud de passage de type delivery.
     *
     * @param idNoeud identifiant du nœud
     * @param dureeLivraison durée de livraison
     * @return le nœud de passage delivery créé
     */
    private NoeudDePassage creerNoeudDePassageDelivery(long idNoeud, double dureeLivraison) {
        Noeud noeudAAjouter = calculChemins.getCarte().getNoeudParId(idNoeud);
        long id = noeudAAjouter.getId();
        double latitude = noeudAAjouter.getLatitude();
        double longitude = noeudAAjouter.getLongitude();
        NoeudDePassage noeudDePassageDeliveryAAjouter = new NoeudDePassage(id, latitude, longitude, NoeudDePassage.TypeNoeud.DELIVERY, dureeLivraison);
        return noeudDePassageDeliveryAAjouter;
    }

    public boolean contrainteDePrecedence(Tournee tournee, long idDelivery, long idNoeudPrecedentDelivery, long idNoeudPrecedentPickup) {
        List<Long> idsVisites = new ArrayList<>();
        for (Chemin chemin : tournee.getChemins()) {
            idsVisites.add(chemin.getNoeudDePassageDepart().getId());
            if ((chemin.getNoeudDePassageArrivee().getId() == idNoeudPrecedentPickup)) {
                idsVisites.add(idNoeudPrecedentPickup);
                break;
            }
        }
        if (idsVisites.contains(idNoeudPrecedentDelivery) || idsVisites.contains(idDelivery)) {
            return false;
        }
        return true;
    }

}
