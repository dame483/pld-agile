let modeAjoutActif = false;
let idNoeudPickupAjout = null;
let idNoeudDeliveryAjout = null;
let idPrecedentPickup = null;
let idPrecedentDelivery = null;
let dureeEnlevement = 500;
let dureeLivraison = 500;
let couleurAjout = null;
let noeudsGrises = [];
let clickLocked = false;

const RAYON_METRES_PICKUP_DELIVERY = 50;
const FACTEUR_BASE_RAYON_PIXEL = 10;

const tempMarkers = [];
function addTempMarker(lat, lng, html, nodeId = null) {
    const colorHTML = html.replace(/background:[^;"]*;/, 'background:#FFFFFF;');

    const m = L.marker([lat, lng], {
        icon: L.divIcon({
            className: 'temp-marker',
            iconSize: [18, 18],
            html: colorHTML
        }),
        id: nodeId
    })
    .addTo(livraisonsLayer)
    .on('click', (e) => {
        const marker = e.target;
        const node = carteData.noeuds[marker.options.id];
        if (node) handleAjoutClick({ latlng: marker.getLatLng(), noeudDirect: node });
    });

    tempMarkers.push(m);
}

function clearTempMarkers() {
    tempMarkers.forEach(m => {
        try { livraisonsLayer.removeLayer(m); } catch {}
    });
    tempMarkers.length = 0;
}

const etatsAjout = [];
let indexEtatCourant = -1;

function sauvegarderEtatAjout() {
    const snapshot = {
        idNoeudPickupAjout,
        idPrecedentPickup,
        idNoeudDeliveryAjout,
        idPrecedentDelivery,
        couleurAjout,
        tempMarkersData: tempMarkers.map(m => {
            const ll = m.getLatLng();
            const icon = m.options && m.options.icon;
            const html = icon && icon.options && typeof icon.options.html === "string"
                ? icon.options.html
                : (m.getElement && m.getElement() ? (m.getElement().innerHTML || "") : "");
            return { lat: ll.lat, lng: ll.lng, html };
        })
    };

    // Coupe les états futurs si on a fait "Annuler" puis on reprend
    etatsAjout.splice(indexEtatCourant + 1);
    etatsAjout.push(JSON.parse(JSON.stringify(snapshot)));
    indexEtatCourant = etatsAjout.length - 1;
}

function restaurerEtatAjout(direction = -1) {
    const newIndex = indexEtatCourant + direction;
    if (newIndex < 0 || newIndex >= etatsAjout.length) {
        envoyerNotification(direction < 0 ? "Aucun état précédent à annuler." : "Aucun état à rétablir.", "error");
        return;
    }

    const etat = etatsAjout[newIndex];
    indexEtatCourant = newIndex;

    idNoeudPickupAjout   = etat.idNoeudPickupAjout;
    idPrecedentPickup    = etat.idPrecedentPickup;
    idNoeudDeliveryAjout = etat.idNoeudDeliveryAjout;
    idPrecedentDelivery  = etat.idPrecedentDelivery;
    couleurAjout         = etat.couleurAjout;

    clearTempMarkers();
    etat.tempMarkersData.forEach(m => addTempMarker(m.lat, m.lng, m.html));

    if (!idNoeudPickupAjout) {
        envoyerNotification("Retour à l'étape : sélection du nœud Pickup", "info");
    } else if (idNoeudPickupAjout && !idPrecedentPickup) {
        envoyerNotification("Retour à l'étape : sélection du nœud précédent du Pickup", "info");
    } else if (idPrecedentPickup && !idNoeudDeliveryAjout) {
        envoyerNotification("Retour à l'étape : sélection du nœud Delivery", "info");
    } else if (idNoeudDeliveryAjout && !idPrecedentDelivery) {
        envoyerNotification("Retour à l'étape : sélection du nœud précédent du Delivery", "info");
    } else {
        envoyerNotification("Aucune étape à restaurer.", "warning");
    }
}

window.annulerEtapeAjout = function () {
    if (!modeAjoutActif) {
        envoyerNotification("Aucune opération d’ajout en cours.", "error");
        return;
    }
    restaurerEtatAjout(-1);
};
window.retablirEtapeAjout = function () {
    if (!modeAjoutActif) {
        envoyerNotification("Aucune opération d’ajout en cours.", "error");
        return;
    }
    restaurerEtatAjout(+1);
};

window.quitterModeAjout = function () {
    modeAjoutActif = false;
    idNoeudPickupAjout = null;
    idNoeudDeliveryAjout = null;
    idPrecedentPickup = null;
    idPrecedentDelivery = null;
    clearTempMarkers();
    document.getElementById('annulerEtapeAjout').style.display='none';
    document.getElementById('retablirEtapeAjout').style.display='none';
    document.getElementById('quitterModeAjout').style.display='none';
    document.getElementById('annulerModification').style.display='inline';
    document.getElementById('retablirModification').style.display='inline';
    document.getElementById('sauvegarderModification').style.display='inline';
    document.getElementById('modeSupression').style.display='inline';
    document.getElementById('modeAjout').style.display='inline';
}

function estNoeudValideCommePrecedent(noeud) {
    if (!noeud || !noeud.type) return false;
    const type = noeud.type.toUpperCase();
    return ["ENTREPOT", "PICKUP", "DELIVERY"].includes(type) || noeud.id === idNoeudPickupAjout;
}

function trouverNoeudProcheGeo(latlng, rayonM = RAYON_METRES_PICKUP_DELIVERY) {
    let closest = null, minDist = Infinity;
    for (const node of Object.values(carteData.noeuds)) {
        const d = map.distance(latlng, L.latLng(node.latitude, node.longitude));
        if (d < minDist && d <= rayonM) {
            minDist = d;
            closest = node;
        }
    }
    return closest;
}

function trouverNoeudValideProcheVisuel(latlng) {
    if (!carteData?.noeuds) return null;

    const clickPt = map.latLngToContainerPoint(latlng);
    const zoom = map.getZoom();
    const facteurZoom = Math.pow(1.1, zoom - 10);

    let plusProche = null;
    let minDistPixels = Infinity;

    for (const n of Object.values(carteData.noeuds)) {
        if (!estNoeudValideCommePrecedent(n)) continue;

        const nodePt = map.latLngToContainerPoint(L.latLng(n.latitude, n.longitude));
        const dx = nodePt.x - clickPt.x;
        const dy = nodePt.y - clickPt.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        let rayonPx = FACTEUR_BASE_RAYON_PIXEL * facteurZoom;
        switch (n.type.toUpperCase()) {
            case "PICKUP":   rayonPx = 10 * facteurZoom; break;
            case "DELIVERY": rayonPx = 12 * facteurZoom; break;
            case "ENTREPOT": rayonPx = 14 * facteurZoom; break;
        }

        if (dist <= rayonPx && dist < minDistPixels) {
            minDistPixels = dist;
            plusProche = n;
        }
    }

    return plusProche;
}

function highlightNode(noeud) {
    const el = document.querySelector(`[id-noeud="${noeud.id}"]`);
    if (!el) return;
    el.style.boxShadow = "0 0 10px 3px yellow";
    setTimeout(() => el.style.boxShadow = "", 1000);
}

async function handleAjoutClick(e) {
    if (!modeAjoutActif || !carteData || clickLocked) return;

    clickLocked = true; // bloque les clics suivants
    setTimeout(() => clickLocked = false, 300); // débloque après 300ms

    let closest = e.noeudDirect || null;

    // --- Étape 1 : sélection Pickup
    if (!idNoeudPickupAjout) {
        if (!closest) closest = trouverNoeudProcheGeo(e.latlng);
        if (!closest) {
            envoyerNotification("Aucun nœud proche trouvé pour le Pickup !", "error");
            return;
        }

        idNoeudPickupAjout = closest.id;
        couleurAjout = colors[selectedIndex % colors.length];

        addTempMarker(
            closest.latitude,
            closest.longitude,
            `<div id-noeud="${closest.id}" style="width:18px;height:18px;background:${couleurAjout};
             border:2px solid black;border-radius:3px;"></div>`,
             closest.id
        );

        highlightNode(closest);
        envoyerNotification("Sélectionnez le nœud précédent du Pickup", "success");
        sauvegarderEtatAjout();
        return;
    }

    // --- Étape 2 : précédent du Pickup
    if (idNoeudPickupAjout && !idPrecedentPickup) {
        if (!closest) closest = trouverNoeudValideProcheVisuel(e.latlng);
        if (!closest) {
            envoyerNotification("Aucun nœud précédent valide trouvé à proximité visuelle !", "error");
            return;
        }
        idPrecedentPickup = closest.id;
        highlightNode(closest);
        envoyerNotification("Sélectionnez maintenant le nœud Delivery", "success");
        sauvegarderEtatAjout();
        return;
    }

    // --- Étape 3 : sélection Delivery
    if (idPrecedentPickup && !idNoeudDeliveryAjout) {
        if (!closest) closest = trouverNoeudProcheGeo(e.latlng);
        if (!closest) {
            envoyerNotification("Aucun nœud proche trouvé pour la livraison !", "error");
            return;
        }

        idNoeudDeliveryAjout = closest.id;

        addTempMarker(
            closest.latitude,
            closest.longitude,
            `<div id-noeud="${closest.id}" style="width:18px;height:18px;background:${couleurAjout};
             border:2px solid black;border-radius:50%;"></div>`
        );

        highlightNode(closest);
        envoyerNotification("Sélectionnez maintenant le nœud précédent du Delivery", "success");
        sauvegarderEtatAjout();
        return;
    }

    // --- Étape 4 : précédent du Delivery
    if (idNoeudDeliveryAjout && !idPrecedentDelivery) {
        if (!closest) closest = trouverNoeudValideProcheVisuel(e.latlng);
        if (!closest) {
            envoyerNotification("Aucun nœud précédent valide trouvé à proximité visuelle !", "error");
            return;
        }

        idPrecedentDelivery = closest.id;
        highlightNode(closest);
        sauvegarderEtatAjout();
        await ajouterLivraison();
    }
}

async function ajouterLivraison() {
    const pickupId = idNoeudPickupAjout;
    const deliveryId = idNoeudDeliveryAjout;

    const body = {
        mode: "ajouter",
        idNoeudPickup: pickupId,
        idNoeudDelivery: deliveryId,
        idPrecedentPickup,
        idPrecedentDelivery,
        dureeEnlevement,
        dureeLivraison
    };

    try {
        const response = await fetch("http://localhost:8080/api/tournee/modifier", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        const data = await response.json();
        const nouvelleTournee = data.data?.tournee;

        if (response.ok && data.success && nouvelleTournee) {
            if (demandeData) {
                if (!demandeData.livraisons) demandeData.livraisons = [];
                if (!demandeData.noeudsDePassage) demandeData.noeudsDePassage = [];

                const existeDeja = demandeData.livraisons.some(l =>
                    l?.adresseEnlevement?.id === pickupId || l?.adresseLivraison?.id === deliveryId
                );

                if (!existeDeja) {
                    const nouvelleLivraison = {
                        adresseEnlevement: {
                            id: pickupId,
                            latitude: null,
                            longitude: null,
                            type: 'PICKUP',
                            duree: 500,
                            horaireArrivee: null,
                            horaireDepart: null
                        },
                        adresseLivraison: {
                            id: deliveryId,
                            latitude: null,
                            longitude: null,
                            type: 'DELIVERY',
                            duree: 500,
                            horaireArrivee: null,
                            horaireDepart: null
                        }
                    };

                    demandeData.livraisons.push(nouvelleLivraison);

                    const ajouterNoeud = (adresse) => {
                        const exists = demandeData.noeudsDePassage.some(n => n.id === adresse.id);
                        if (!exists) {
                            demandeData.noeudsDePassage.push({ ...adresse });
                        }
                    };

                    ajouterNoeud(nouvelleLivraison.adresseEnlevement);
                    ajouterNoeud(nouvelleLivraison.adresseLivraison);
                }
            }
            resetTournee();

            window.toutesLesTournees[selectedIndex] = nouvelleTournee;

            drawTourneeNodes(nouvelleTournee);
            drawTournee(nouvelleTournee, colors[selectedIndex % colors.length], selectedIndex);
            majTableauTournee(nouvelleTournee, window.tourneeBaseline);

            envoyerNotification("Nouvelle livraison ajoutée avec succès !", "success");
        } else {
            envoyerNotification("Erreur : " + (data.message || "Impossible d’ajouter la livraison !"), "error");
        }
    } catch (err) {
        envoyerNotification("Erreur réseau : " + err.message, "error");
    } finally {
        modeAjoutActif = false;
        idNoeudPickupAjout = null;
        idNoeudDeliveryAjout = null;
        idPrecedentPickup = null;
        idPrecedentDelivery = null;
        clearTempMarkers();
        document.getElementById('annulerEtapeAjout').style.display='none';
        document.getElementById('retablirEtapeAjout').style.display='none';
        document.getElementById('quitterModeAjout').style.display='none';
        document.getElementById('annulerModification').style.display='inline';
        document.getElementById('retablirModification').style.display='inline';
        document.getElementById('sauvegarderModification').style.display='inline';
        document.getElementById('modeSupression').style.display='inline';
        document.getElementById('modeAjout').style.display='inline';

        etatsAjout.length = 0;
        indexEtatCourant = -1;
    }
}

function attachAjoutListener() {
    if (map && !map._ajoutListenerSet) {
        map.on('click', e => {
            if (!modeAjoutActif) return;
            handleAjoutClick(e);
        });
        map._ajoutListenerSet = true;
    }
}

if (typeof window.drawCarteOriginal === "undefined" && typeof drawCarte !== "undefined") {
    window.drawCarteOriginal = drawCarte;
    drawCarte = function (...args) {
        window.drawCarteOriginal.apply(this, args);
        attachAjoutListener();
    };
}

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
            majTableauTournee(nouvelleTournee, window.tourneeBaseline)
            window.toutesLesTournees[selectedIndex] = nouvelleTournee;
            await updateUIFromEtat();
        } else {
            envoyerNotification("Erreur : " + (data.message || "Impossible de supprimer le point."),"error");
        }
    } catch (err) {
        console.error(err);
        envoyerNotification("Erreur réseau : " + err.message, "error");
    } finally {
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
                    horaire = noeud.horaireDepart ? noeud.horaireDepart.slice(0, 5) : "-";
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
                    nouveauHoraire = noeud.horaireDepart ? noeud.horaireDepart.slice(0, 5) : "-";
                    nouveauHoraireExact = noeud.horaireDepart ? noeud.horaireDepart.slice(0, 5) : "-";
                } else {
                    nouveauHoraire = formatHoraireFourchette(noeud.horaireArrivee) || "-";
                    nouveauHoraireExact = noeud.horaireArrivee ? noeud.horaireArrivee.slice(0, 5) : "-";
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
    modeSuppressionActif = false;
    try {
        const response = await fetch("http://localhost:8080/api/annuler", {method : "POST"});
        const data = await response.json();
        if (data.success){
            const tournee = data.data.tournee;
            resetTournee();
            drawTourneeNodes(tournee);
            drawTournee(tournee, colors[0], 0);
            majTableauTournee(tournee, window.tourneeBaseline);
            window.toutesLesTournees[selectedIndex] = tournee;
            await updateUIFromEtat();
        } else{
            envoyerNotification("Erreur : " + (data.message || "Impossible d'annuler la dernière modification."),"error");
        }
    } catch (err){
        envoyerNotification("Erreur réseau : " + err.message, "error");
    }
};

window.retablirModification = async function () {
    modeSuppressionActif = false;
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
            majTableauTournee(tournee, window.tourneeBaseline);
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
};

window.sauvegarderModification = async function () {
    modeSuppressionActif = false;
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
            drawTourneeTable(window.toutesLesTournees[selectedIndex]);
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
};

window.activerModeSuppression = function () {
    if (!modeSuppressionActif) {
        modeSuppressionActif = true;
        idNoeudPickup = null;
        idNoeudDelivery = null;
        envoyerNotification("Mode suppression activé : cliquez sur un point Pickup ou Livraison à supprimer.","success");
    }
};

window.activerModeModification = async function (){
    if (modeAjoutActif) {
        modeAjoutActif = false;
        etatsAjout.length = 0;
        indexEtatCourant = -1;
        clearTempMarkers();
    }

    const tournee = window.toutesLesTournees[selectedIndex];
    fetch("http://localhost:8080/api/tournee/mode-modification", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(tournee)
    })
    .then(response => response.json())
    .then(async data => {
        window.tourneeBaseline = JSON.parse(JSON.stringify(tournee));
        resetTournee();
        drawTourneeNodes(tournee);
        drawTournee(tournee, colors[0], 0);

        await updateUIFromEtat();
        document.getElementById('annulerEtapeAjout').style.display='none';
        document.getElementById('retablirEtapeAjout').style.display='none';
        document.getElementById('quitterModeAjout').style.display='none';

    });
};

window.activerModeAjout = async function (){
    modeSuppressionActif = false;
    document.getElementById('annulerEtapeAjout').style.display='inline';
    document.getElementById('retablirEtapeAjout').style.display='inline';
    document.getElementById('quitterModeAjout').style.display='inline';
    document.getElementById('modeAjout').style.display='none';
    document.getElementById('modeSupression').style.display='none';
    document.getElementById('annulerModification').style.display='none';
    document.getElementById('retablirModification').style.display='none';
    document.getElementById('sauvegarderModification').style.display='none';

    if (!carteData || !window.toutesLesTournees.length) {
        envoyerNotification("Veuillez charger une tournée avant d'ajouter une livraison", "error");
        return;
    }

        modeAjoutActif = true;

        idNoeudPickupAjout = null;
        idNoeudDeliveryAjout = null;
        idPrecedentPickup = null;
        idPrecedentDelivery = null;
        clearTempMarkers();

        etatsAjout.length = 0;
        indexEtatCourant = -1;
        sauvegarderEtatAjout();

        if (livraisonsLayer) {
            livraisonsLayer.eachLayer(layer => {
                layer.on("click", e => {
                    if (modeAjoutActif) {
                        const nodeId = e.target.options.id;
                        const noeud = carteData.noeuds[nodeId];
                        if (noeud) handleAjoutClick({ latlng: e.latlng, noeudDirect: noeud });
                    }
                });
            });
        }
        if (entrepotLayer) {
            entrepotLayer.eachLayer(layer => {
                layer.on("click", e => {
                    if (modeAjoutActif) {
                        const nodeId = e.target.options.id;
                        const noeud = carteData.noeuds[nodeId];
                        if (noeud) handleAjoutClick({ latlng: e.latlng, noeudDirect: noeud });
                    }
                });
            });
        }

        envoyerNotification("Cliquez sur le nœud Pickup à ajouter", "success");
};