package fr.insalyon.pldagile.algorithme;

import fr.insalyon.pldagile.modele.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.time.LocalTime;

public class TestKMeansVisuel {

    public static void main(String[] args) {

        // 1️⃣ Générer des livraisons aléatoires
        List<Livraison> livraisons = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < 50; i++) {
            // Pickup
            NoeudDePassage pickup = new NoeudDePassage(
                    i,                            // id
                    rand.nextDouble() * 500,      // latitude
                    rand.nextDouble() * 500,      // longitude
                    NoeudDePassage.TypeNoeud.PICKUP,
                    0,                            // durée
                    null                          // horaireArrivee
            );

            // Delivery
            NoeudDePassage delivery = new NoeudDePassage(
                    1000 + i,
                    rand.nextDouble() * 500,
                    rand.nextDouble() * 500,
                    NoeudDePassage.TypeNoeud.DELIVERY,
                    0,
                    null
            );

            // Créer la livraison et ajouter à la liste
            Livraison l = new Livraison(pickup, delivery);
            livraisons.add(l);
        }

        // 2️⃣ Appliquer ton KMeans
        int k = 4;  // nombre de clusters/livreurs
        KMeans km = new KMeans(livraisons, k);
        List<List<Livraison>> clusters = km.cluster();

        // 3️⃣ Affichage Swing
        JFrame frame = new JFrame("K-Means Visualisation");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.ORANGE};

                // Affichage des livraisons par cluster
                for (int i = 0; i < clusters.size(); i++) {
                    List<Livraison> cluster = clusters.get(i);
                    g.setColor(colors[i % colors.length]);
                    for (Livraison l : cluster) {
                        int px = (int) l.getAdresseEnlevement().getLatitude();
                        int py = (int) l.getAdresseEnlevement().getLongitude();
                        g.fillOval(px, py, 8, 8); // point pickup

                        int dx = (int) l.getAdresseLivraison().getLatitude();
                        int dy = (int) l.getAdresseLivraison().getLongitude();
                        g.drawOval(dx, dy, 8, 8); // point delivery

                        // Ligne entre pickup et delivery
                        g.drawLine(px + 4, py + 4, dx + 4, dy + 4);
                    }
                }
            }
        };

        frame.add(panel);
        frame.setVisible(true);
    }
}
