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

    public Tournee ajouterNoeudPickup(Tournee tournee, long idNoeudAjoute, long idNoeudPrecedentNoeudAjoute, double dureeEnlevement) {
        NoeudDePassage noeudAjoute = creerNoeudDePassagePickup(idNoeudAjoute, dureeEnlevement);
        NoeudDePassage noeudPrecedentNoeudAjoute = recupererNoeud(tournee, idNoeudPrecedentNoeudAjoute);


        if ( noeudPrecedentNoeudAjoute == null) {
            System.out.println("les noeuds ajoutés ne sont pas récupérer correctement");
            return tournee;
        }

        Chemin cheminAutourNoeudPrecedentNoeudAjoute[] = trouverCheminsAutourNoeud(tournee, noeudPrecedentNoeudAjoute);

        Chemin apres = cheminAutourNoeudPrecedentNoeudAjoute[1];
        Chemin avant = cheminAutourNoeudPrecedentNoeudAjoute[0];

        int indexAvant = (avant != null) ? tournee.getChemins().indexOf(avant) : 0;
        int indexApres = (apres != null) ? tournee.getChemins().indexOf(apres) : 0;

        NoeudDePassage noeudSuivantNoeudPrecedent = (apres !=null ) ? apres.getNoeudDePassageArrivee() : null;

        supprimerChemin(tournee, apres);

        recalculerChemin(tournee, noeudPrecedentNoeudAjoute, noeudAjoute, indexAvant);
        recalculerChemin(tournee, noeudAjoute, noeudSuivantNoeudPrecedent, indexApres);
        Chemin newCheminAutourNoeudPrecedent[] = trouverCheminsAutourNoeud(tournee, noeudPrecedentNoeudAjoute);
        Chemin newApres = newCheminAutourNoeudPrecedent[1];
        LocalTime horaireArriveeNoeudAjoute = noeudPrecedentNoeudAjoute.getHoraireDepart().plusSeconds((long) (newApres.getLongueurTotal() / vitesse));
        LocalTime horaireDepartNoeudAjoute = horaireArriveeNoeudAjoute.plusSeconds((long) dureeEnlevement);
        noeudAjoute.setHoraireArrivee(horaireArriveeNoeudAjoute);
        noeudAjoute.setHoraireDepart(horaireDepartNoeudAjoute);

        mettreAJourTotaux(tournee);
        mettreAJourHoraires(tournee, vitesse);

        return tournee;

    }

    public Tournee ajouterNoeudDelivery(Tournee tournee, long idNoeudAjoute, long idNoeudPrecedentNoeudAjoute, double dureeLivraison) {
        NoeudDePassage noeudAjoute = creerNoeudDePassageDelevery(idNoeudAjoute, dureeLivraison);
        NoeudDePassage noeudPrecedentNoeudAjoute = recupererNoeud(tournee, idNoeudPrecedentNoeudAjoute);


        if ( noeudPrecedentNoeudAjoute == null) {
            System.out.println("les noeuds ajoutés ne sont pas récupérer correctement");
            return tournee;
        }

        Chemin cheminAutourNoeudPrecedentNoeudAjoute[] = trouverCheminsAutourNoeud(tournee, noeudPrecedentNoeudAjoute);

        Chemin apres = cheminAutourNoeudPrecedentNoeudAjoute[1];
        Chemin avant = cheminAutourNoeudPrecedentNoeudAjoute[0];

        int indexAvant = (avant != null) ? tournee.getChemins().indexOf(avant) : 0;
        int indexApres = (apres != null) ? tournee.getChemins().indexOf(apres) : 0;

        NoeudDePassage noeudSuivantNoeudPrecedent = (apres !=null ) ? apres.getNoeudDePassageArrivee() : null;
        supprimerChemin(tournee, apres);

        recalculerChemin(tournee, noeudPrecedentNoeudAjoute, noeudAjoute, indexAvant);
        recalculerChemin(tournee, noeudAjoute, noeudSuivantNoeudPrecedent, indexApres);
        Chemin newCheminAutourNoeudPrecedent[] = trouverCheminsAutourNoeud(tournee, noeudPrecedentNoeudAjoute);
        Chemin newApres = newCheminAutourNoeudPrecedent[1];
        LocalTime horaireArriveeNoeudAjoute = noeudPrecedentNoeudAjoute.getHoraireDepart().plusSeconds((long) (newApres.getLongueurTotal() / vitesse));
        LocalTime horaireDepartNoeudAjoute = horaireArriveeNoeudAjoute.plusSeconds((long) dureeLivraison);
        noeudAjoute.setHoraireArrivee(horaireArriveeNoeudAjoute);
        noeudAjoute.setHoraireDepart(horaireDepartNoeudAjoute);

        mettreAJourTotaux(tournee);
        mettreAJourHoraires(tournee, vitesse);

        return tournee;

    }



    public Tournee supprimerNoeud(Tournee tournee, long idNoeud) {
        NoeudDePassage n = recupererNoeud(tournee, idNoeud);
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


    private NoeudDePassage recupererNoeud(Tournee tournee, long idNoeud) {
        NoeudDePassage n = tournee.getNoeudParId(idNoeud);
        if (n == null) return null;
        if (!tournee.getChemins().isEmpty()) {
            NoeudDePassage entrepot = tournee.getChemins().get(0).getNoeudDePassageDepart();
            if (n.equals(entrepot)) return null;
        }
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

    private NoeudDePassage creerNoeudDePassageDelevery(long idNoeud, double dureeLivraison) {
        Noeud noeudAAjouter = calculChemins.getCarte().getNoeudParId(idNoeud);
        long id = noeudAAjouter.getId();
        double latitude = noeudAAjouter.getLatitude();
        double longitude = noeudAAjouter.getLongitude();
        NoeudDePassage noeudDePassageDeliveryAAjouter = new NoeudDePassage(id, latitude, longitude, NoeudDePassage.TypeNoeud.DELIVERY, dureeLivraison);
        return noeudDePassageDeliveryAAjouter;
    }

}
