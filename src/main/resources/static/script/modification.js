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
    console.log(body);
    const response = await fetch("http://localhost:8080/api/tournee/current");
    const data = await response.json();
    console.log("Tournée côté back :", data.data.tournee); //debug
    console.log("Tournée côté front :", window.toutesLesTournees[selectedIndex]); //debug

    try {
        const response = await fetch("http://localhost:8080/api/tournee/modifier", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(body)
        });
        const data = await response.json();
        console.log(data);
        if (data.success) {
            const nouvelleTournee = data.data.tournee;
            resetTournee();

            drawTourneeNodes(nouvelleTournee);
            drawTournee(nouvelleTournee, colors[0], 0);
            majTableauTournee(nouvelleTournee, window.toutesLesTournees[selectedIndex])
            window.toutesLesTournees[selectedIndex] = nouvelleTournee;
            await updateUIFromEtat();
        } else {
            alert("Erreur : " + (data.message || "Impossible de supprimer le point."));
        }
    } catch (err) {
        console.error(err);
        alert("Erreur réseau : " + err.message);
    } finally {
        modeSuppressionActif = false;
        idNoeudPickup = null;
        idNoeudDelivery = null;
    }
}

function majTableauTournee(nouvelleTournee, ancienneTournee) {
    const tableauTournees = document.getElementById("tableauTournees");

    const anciensHoraires = new Map();
    if (ancienneTournee?.chemins) {
        ancienneTournee.chemins.forEach(c => {
            const noeuds = [c.noeudDePassageDepart, c.noeudDePassageArrivee];
            noeuds.forEach(noeud => {
                if (noeud && noeud.id) {
                    const horaire = formatHoraireFourchette(noeud.horaireArrivee || noeud.horaireDepart);
                    anciensHoraires.set(noeud.id, horaire || "-");
                }
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
                    <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Ancien horaire</th>
                    <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Nouveau horaire</th>
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

                const ancienHoraire = anciensHoraires.get(noeud.id) || "-";
                let nouveauHoraire = "-";
                if (noeud === premierNoeud && noeud.type === "ENTREPOT") {
                    nouveauHoraire = noeud.horaireDepart || "-";
                } else {
                    nouveauHoraire = formatHoraireFourchette(noeud.horaireArrivee) || "-";
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
                <td style="border-left:1px solid #ccc;padding:4px;text-align:center;${styleDiff}">${nouveauHoraire}</td>
            `;
                tbody.appendChild(row);
            }
        });
    });
}
