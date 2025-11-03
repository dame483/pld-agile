package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.List;

public class ModificationTournee {

    private final CalculChemins calculChemins;
    private final double vitesse;

    public ModificationTournee(CalculChemins calculChemins, double vitesse) {
        this.calculChemins = calculChemins;
        this.vitesse = vitesse;
    }

    public Tournee ajouterNoeudPickup(Tournee tournee, long idNoeudAjoute, long idNoeudPrecedent, double dureeEnlevement) {
        return ajouterNoeud(tournee, idNoeudAjoute, idNoeudPrecedent, dureeEnlevement, true);
    }

    public Tournee ajouterNoeudDelivery(Tournee tournee, long idNoeudAjoute, long idNoeudPrecedent, double dureeLivraison) {
        return ajouterNoeud(tournee, idNoeudAjoute, idNoeudPrecedent, dureeLivraison, false);
    }

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


    private NoeudDePassage recupererNoeudSuppression(Tournee tournee, long idNoeud) {
        NoeudDePassage n = tournee.getNoeudParId(idNoeud);
        if (n == null) return null;
        if (!tournee.getChemins().isEmpty()) {
            NoeudDePassage entrepot = tournee.getChemins().get(0).getNoeudDePassageDepart();
            if (n.equals(entrepot)) return null;
        }
        return n;
    }

    private NoeudDePassage recupererNoeud(Tournee tournee, long idNoeud) {
        NoeudDePassage n = tournee.getNoeudParId(idNoeud);
        if (n == null) return null;
        return n;
    }

    private Chemin[] trouverCheminsAutourNoeud(Tournee tournee, NoeudDePassage n) {
        Chemin avant = null, apres = null;
        for (Chemin c : tournee.getChemins()) {
            if (c.getNoeudDePassageArrivee().equals(n)) avant = c;
            if (c.getNoeudDePassageDepart().equals(n)) apres = c;
        }
        return new Chemin[]{avant, apres};
    }

    private void supprimerChemin(Tournee tournee, Chemin chemin) {
        if (chemin != null) {
            tournee.getChemins().removeIf(c -> c.equals(chemin));
        }
    }


    private void recalculerChemin(Tournee tournee, NoeudDePassage precedent, NoeudDePassage suivant, int index) {
        Chemin nouveauChemin = calculChemins.calculerCheminPlusCourt(precedent, suivant);
        if (nouveauChemin == null) {
            throw new IllegalStateException("Impossible de recalculer le chemin entre les nœuds "
                    + precedent.getId() + " et " + (suivant != null ? suivant.getId() : "null"));
        }
        tournee.getChemins().add(index, nouveauChemin);
    }


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

    private NoeudDePassage creerNoeudDePassagePickup(long idNoeud, double dureeEnlevement) {
        Noeud noeudAAjouter = calculChemins.getCarte().getNoeudParId(idNoeud);

        long id = noeudAAjouter.getId();
        double latitude = noeudAAjouter.getLatitude();
        double longitude = noeudAAjouter.getLongitude();
        NoeudDePassage noeudDePassagePickupAAjouter = new NoeudDePassage(id, latitude, longitude, NoeudDePassage.TypeNoeud.PICKUP, dureeEnlevement);
        return noeudDePassagePickupAAjouter;
    }

    private NoeudDePassage creerNoeudDePassageDelivery(long idNoeud, double dureeLivraison) {
        Noeud noeudAAjouter = calculChemins.getCarte().getNoeudParId(idNoeud);
        long id = noeudAAjouter.getId();
        double latitude = noeudAAjouter.getLatitude();
        double longitude = noeudAAjouter.getLongitude();
        NoeudDePassage noeudDePassageDeliveryAAjouter = new NoeudDePassage(id, latitude, longitude, NoeudDePassage.TypeNoeud.DELIVERY, dureeLivraison);
        return noeudDePassageDeliveryAAjouter;
    }

}
