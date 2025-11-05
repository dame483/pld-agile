async function checkEtSupprimer() {
    if (!modeSuppressionActif) return;
    if (!idNoeudPickup && !idNoeudDelivery) return;

    const livraisons = demandeData?.livraisons || [];
    if (idNoeudPickup && !idNoeudDelivery) {
        const lAssocie = livraisons.find(l => l.adresseEnlevement.id === idNoeudPickup);
        if (lAssocie) idNoeudDelivery = lAssocie.adresseLivraison.id;
    } else if (idNoeudDelivery && !idNoeudPickup) {
        const lAssocie = livraisons.find(l => l.adresseLivraison.id === idNoeudDelivery);
        if (lAssocie) idNoeudPickup = lAssocie.adresseEnlevement.id;
    }

    if (!idNoeudPickup || !idNoeudDelivery) return;

    const body = {idNoeudPickup, idNoeudDelivery, mode:"supprimer"};
    try {
        const response = await fetch("http://localhost:8080/api/tournee/modifier", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(body)
        });
        const data = await response.json();
        if (data.success) {
            const nouvelleTournee = data.data.tournee;
            resetTournee();

            drawTourneeNodes(nouvelleTournee);
            drawTournee(nouvelleTournee, colors[0], 0);
            majTableauTournee(nouvelleTournee, window.toutesLesTournees[selectedIndex])
            window.toutesLesTournees[selectedIndex] = nouvelleTournee;
            await updateUIFromEtat();
        } else {
            envoyerNotification("Erreur : " + (data.message || "Impossible de supprimer le point."),"error");
        }
    } catch (err) {
        console.error(err);
        envoyerNotification("Erreur réseau : " + err.message, "error");
    } finally {
        modeSuppressionActif = false;
        idNoeudPickup = null;
        idNoeudDelivery = null;
    }
}

function majTableauTournee(nouvelleTournee, ancienneTournee) {
    const tableauTournees = document.getElementById("tableauTournees");

    const anciensHoraires = new Map();

    if (ancienneTournee?.chemins?.length) {
        let premier = true;

        ancienneTournee.chemins.forEach(c => {
            const noeuds = [c.noeudDePassageDepart, c.noeudDePassageArrivee];

            noeuds.forEach(noeud => {
                if (!noeud || !noeud.id) return;

                let horaire = "-";
                let key = noeud.id;

                if (premier && noeud.type === "ENTREPOT") {
                    horaire = noeud.horaireDepart || "-";
                    key = `${noeud.id}_depart`;
                    premier = false;
                }
                else if (noeud.type === "ENTREPOT") {
                    horaire = formatHoraireFourchette(noeud.horaireArrivee) || "-";
                    key = `${noeud.id}_arrivee`;
                }
                else {
                    horaire = formatHoraireFourchette(noeud.horaireArrivee) || "-";
                }

                anciensHoraires.set(key, horaire);
            });
        });
    }

    tableauTournees.innerHTML = `
        <table style="border-collapse:collapse;">
            <thead>
                <tr>
                    <th style="border-bottom:1px solid #ccc;padding:4px;">Couleur</th>
                    <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Ordre</th>
                    <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Type</th>
                    <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">ID</th>
                    <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Ancienne fourchette horaire</th>
                    <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Nouvel horaire exact</th>
                </tr>
            </thead>
            <tbody id="tourneeBody"></tbody>
        </table>
    `;
    const tbody = document.getElementById("tourneeBody");

    let ordre = 1;
    const chemins = nouvelleTournee.chemins || [];
    const premierNoeud = chemins[0]?.noeudDePassageDepart;

    chemins.forEach(c => {
        const noeudsChemin = [
            (c.noeudDePassageDepart && c.noeudDePassageDepart.type === "ENTREPOT") ? c.noeudDePassageDepart : null,
            c.noeudDePassageArrivee
        ];
        noeudsChemin.forEach(noeud => {
            if (!noeud) return;
            if (["ENTREPOT", "PICKUP", "DELIVERY"].includes(noeud.type)) {

                const color = window.colorByNodeId?.[noeud.id] || "#000000";
                const bordercolor = (color.toLowerCase() === "#000000" || color.toLowerCase() === "black")
                    ? "white"
                    : "black";

                let key = noeud.id;

                if (noeud.type === "ENTREPOT") {
                    if (noeud === premierNoeud) key = `${noeud.id}_depart`;
                    else if (noeud === chemins[chemins.length - 1]?.noeudDePassageArrivee)
                        key = `${noeud.id}_arrivee`;
                }

                const ancienHoraire = anciensHoraires.get(key) || "-";
                let nouveauHoraire = "-";
                let nouveauHoraireExact = "-";
                if (noeud === premierNoeud && noeud.type === "ENTREPOT") {
                    nouveauHoraire = noeud.horaireDepart || "-";
                    nouveauHoraireExact = noeud.horaireDepart || "-";
                } else {
                    nouveauHoraire = formatHoraireFourchette(noeud.horaireArrivee) || "-";
                    nouveauHoraireExact = noeud.horaireArrivee || "-";
                }

                const styleDiff = (ancienHoraire !== "-" && nouveauHoraire !== ancienHoraire)
                    ? "color:red;font-weight:bold;"
                    : "";

                let shapeHTML = "";
                if (noeud.type === "PICKUP") {
                    shapeHTML = `<div style="width:18px;height:18px;background:${color};
                              border:1px solid ${bordercolor};border-radius:3px;margin:auto;"></div>`;
                } else if (noeud.type === "DELIVERY") {
                    shapeHTML = `<div style="width:18px;height:18px;background:${color};
                              border:1px solid ${bordercolor};border-radius:50%;margin:auto;"></div>`;
                } else if (noeud.type === "ENTREPOT") {
                    const strokeColor = (color.toLowerCase() === "#000000" || color.toLowerCase() === "black") ? "white" : "black";
                    shapeHTML = `
                    <svg width="18" height="18" viewBox="0 0 24 24" style="display:block;margin:auto;">
                        <polygon points="12,3 21,21 3,21"
                            fill="${color}"
                            stroke="${strokeColor}"
                            stroke-width="1.5"/>
                    </svg>`;
                }

                const row = document.createElement("tr");
                row.innerHTML = `
                <td style="padding:4px;text-align:center;">${shapeHTML}</td>
                <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${ordre++}</td>
                <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${noeud.type}</td>
                <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${noeud.id}</td>
                <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${ancienHoraire}</td>
                <td style="border-left:1px solid #ccc;padding:4px;text-align:center;${styleDiff}">${nouveauHoraireExact}</td>
            `;
                tbody.appendChild(row);
            }
        });
    });
}

function filtreDemande(tournees) {
    if (!demandeData || !Array.isArray(tournees)) return;

    const idsDansTournees = new Set();
    tournees.forEach(tournee => {
        tournee.chemins?.forEach(c => {
            if (c.noeudDePassageDepart?.id) idsDansTournees.add(c.noeudDePassageDepart.id);
            if (c.noeudDePassageArrivee?.id) idsDansTournees.add(c.noeudDePassageArrivee.id);
        });
    });

    const anciennesLivraisons = demandeData.livraisons || [];
    const nouvellesLivraisons = anciennesLivraisons.filter(l => {
        const idPickup = l.adresseEnlevement?.id;
        const idDelivery = l.adresseLivraison?.id;
        return idsDansTournees.has(idPickup) || idsDansTournees.has(idDelivery);
    });

    demandeData.livraisons = nouvellesLivraisons;
}

window.annulerModification = async function () {
    try {
        const response = await fetch("http://localhost:8080/api/annuler", {method : "POST"});
        const data = await response.json();
        if (data.success){
            const tournee = data.data.tournee;
            resetTournee();
            drawTourneeNodes(tournee);
            drawTournee(tournee, colors[0], 0);
            majTableauTournee(tournee, window.toutesLesTournees[selectedIndex])
            window.toutesLesTournees[selectedIndex] = tournee;
            await updateUIFromEtat();
        } else{
            envoyerNotification("Erreur : " + (data.message || "Impossible d'annuler la dernière modification."),"error");
        }
    } catch (err){
        envoyerNotification("Erreur réseau : " + err.message, "error");
    }
}

window.retablirModification = async function () {
    try {
        const response = await fetch("http://localhost:8080/api/restaurer", {
            method: "POST"
        });

        const data = await response.json();

        if (data.success) {
            const tournee = data.data.tournee;

            resetTournee();
            drawTourneeNodes(tournee);
            drawTournee(tournee, colors[0], 0);
            majTableauTournee(tournee, window.toutesLesTournees[selectedIndex]);
            window.toutesLesTournees[selectedIndex] = tournee;

            await updateUIFromEtat();
        } else {
            envoyerNotification(
                "Erreur : " + (data.message || "Impossible de restaurer la dernière modification."),
                "error"
            );
        }
    } catch (err) {
        envoyerNotification("Erreur réseau : " + err.message, "error");
    }
}

window.sauvegarderModification = async function () {
    try {
        const body = {
            demande: demandeData,
            tournees: window.toutesLesTournees
        };
        const response = await fetch("http://localhost:8080/api/sauvegarder-modifications", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });

        const data = await response.json();
        if (data.success) {
            envoyerNotification("Modifications sauvegardées avec succès", "success");
            resetTournee();
            filtreDemande(window.toutesLesTournees); //actualise demandeData avec les noeuds des tournees
            drawLivraisons(demandeData);
            window.toutesLesTournees.forEach((t, i) => {
                const color = colors[i % colors.length];
                drawTournee(t, color, i);
            });
            drawTourneeTable(window.toutesLesTournees[selectedIndex])
            await updateUIFromEtat();
        } else{
            console.error("Erreur serveur :", data.message);
            envoyerNotification(data.message, "error");
        }
    }
    catch (err){
        console.error("Erreur sauvegarde :", err);
        envoyerNotification("Erreur réseau lors de la sauvegarde des modifications", "error");
    }
}