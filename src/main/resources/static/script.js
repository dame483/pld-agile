// DECLARATIONS

let map=null;
let carteData=null;
let demandeData=null;
let livraisonsLayer=null, entrepotLayer=null;
let animTimer=null, courierMarker=null, animPath= {};
let animControl=null, isAnimating=false, isPaused=false;
let animSpeed=8, animSamples=[], animIndex=0;
window.toutesLesTournees = [];
var selectedIndex = 0;

const colors=[
    '#e6194b','#3cb44b','#ffe119','#4363d8','#f58231','#911eb4','#46f0f0',
    '#f032e6','#bcf60c','#fabebe','#008080','#e6beff','#9a6324','#fffac8',
    '#800000','#aaffc3','#808000','#ffd8b1','#000075','#808080','#000000'
];
const STEP_M=10;


// ======================================================
// ===================   OUTILS   =======================
// ======================================================

function makeStepNumberIcon(n,color){
    const svg=`<svg xmlns="http://www.w3.org/2000/svg" width="34" height="18" viewBox="0 0 34 18">
                    <text x="17" y="14" font-size="12" font-weight="bold" text-anchor="middle"
                          fill="${color}" stroke="white" stroke-width="3" paint-order="stroke">${n}</text>
                 </svg>`;
    return L.divIcon({className:'',html:svg,iconSize:[34,18]});
}

function midPoint(a,b){ return [(a[0]+b[0])/2,(a[1]+b[1])/2]; }

function samplePathMeters(path, stepM){
    if(path.length<2) return path.slice();
    const out=[]; let prev=L.latLng(path[0][0],path[0][1]); out.push([prev.lat,prev.lng]);
    for(let i=1;i<path.length;i++){
        const curr=L.latLng(path[i][0],path[i][1]); let segLen=map.distance(prev,curr);
        if(segLen===0){ prev=curr; continue; }
        for(let d=stepM; d<=segLen; d+=stepM){
            const t=d/segLen; const lat=prev.lat+(curr.lat-prev.lat)*t; const lng=prev.lng+(curr.lng-prev.lng)*t;
            out.push([lat,lng]);
        }
        prev=curr; if(Math.abs(out.at(-1)[0]-curr.lat)>1e-12) out.push([curr.lat,curr.lng]);
    }
    return out;
}

function formatHoraireFourchette(horaire, deltaMinutes = 30) {
    if (!horaire) return "";

    const [h, m] = horaire.split(":").map(Number);
    const total = h * 60 + m;

    const arrondi30 = m => Math.round(m / 30) * 30;
    const min = Math.max(0, arrondi30(total - Math.floor(deltaMinutes/2)));
    const max = Math.min(24 * 60, arrondi30(total + Math.floor(deltaMinutes/2)));

    const format = t => {
        const hh = String(Math.floor(t / 60)).padStart(2, '0');
        const mm = String(t % 60).padStart(2, '0');
        return `${hh}:${mm}`;
    };

    return `${format(min)}-${format(max)}`;
}


// ======================================================
// ====================   CHARGEMENT   ===================
// ======================================================

async function uploadCarte(file) {
    const formData = new FormData();
    formData.append("file", file);

    try {
        const response = await fetch(`http://localhost:8080/api/upload-carte`, {
            method: "POST",
            body: formData
        });

        const res = await response.json(); // lire le JSON **avant** de traiter success/error

        if (!res.success) {
            envoyerNotification(res.message || "Erreur lors du chargement de la carte", "error");
            return;
        }

        // succès
        carteData = res.data.carte;
        resetCarte();
        resetLivraisons();
        drawCarte(carteData);
        await updateUIFromEtat();

    } catch (err) {
        envoyerNotification("Erreur : " + err.message, "error");
    } finally {
        document.getElementById('xmlCarte').value = '';
    }
}


async function uploadDemande(file){
    if(!carteData){
        envoyerNotification("Charger le plan XML d'abord.", "error");
        return;
    }
    const formData=new FormData();
    formData.append("file",file);
    try{
        const response=await fetch("http://localhost:8080/api/upload-demande",{method:"POST",body:formData});
        if(!response.ok){
            envoyerNotification(await response.text(), "error");
            return;
        }
        const res=await response.json();
        demandeData = res.data.demande;
        const input = document.getElementById('nbLivreurs');
        input.min = 1;
        input.step = 1;
        input.required = true;
        input.max = demandeData.livraisons.length || 1;
        input.value = 1;
        input.addEventListener('keydown', e => e.preventDefault());
        drawLivraisons(demandeData);
        drawEntrepot(demandeData.entrepot);
        await updateUIFromEtat();
    }catch(err){
        envoyerNotification(err.message, "error");
    }finally{
        document.getElementById('xmlDemande').value = '';
    }
}

// ======================================================
// ============   AFFICHAGE CARTE ET DONNÉES   ==========
// ======================================================

function resetCarte() {
    if (map) {
        try { map.remove(); } catch (e) {}
        map = null;
    }
    const mapDiv = document.getElementById('map');
    if (mapDiv) {
        mapDiv._leaflet_id = null;
        mapDiv.innerHTML = "";
        mapDiv.style.display = "block";
    }
    livraisonsLayer = null;
    entrepotLayer = null;
    window.tronconsLayer = null;
    window.tourneeLayer = null;
    window.directionNumbersLayer = null;
    window.animPaths = {};
}

function resetLivraisons(){
    demandeData = null;
    document.getElementById('tableauDemandes').innerHTML = "";
    document.getElementById('tableauTournees').innerHTML = "";
    if (livraisonsLayer) livraisonsLayer.clearLayers();
    if (entrepotLayer) entrepotLayer.clearLayers();
    if (window.tourneeLayer) window.tourneeLayer.clearLayers();
    if (window.directionNumbersLayer) window.directionNumbersLayer.clearLayers();

    stopAnimation();
    if (animControl && map) { map.removeControl(animControl); animControl = null; }
    animPath = []; isAnimating = false; isPaused = false;
}

function drawCarte(carte) {
    const n = carte.noeuds || {};
    const t = carte.troncons || [];
    const mapDiv = document.getElementById('map');
    mapDiv.style.display = "block";

    if (!map) {
        map = L.map('map');
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap'
        }).addTo(map);

        livraisonsLayer = L.layerGroup().addTo(map);
        entrepotLayer = L.layerGroup().addTo(map);
        addLegend();
    }

    if (window.tronconsLayer) window.tronconsLayer.clearLayers();
    else window.tronconsLayer = L.layerGroup().addTo(map);

    t.forEach(tc => {
        const o = n[tc.idOrigine], d = n[tc.idDestination];
        if (o && d) {
            L.polyline([[o.latitude, o.longitude], [d.latitude, d.longitude]], { color: '#1a74bb', weight: 2 })
                .addTo(window.tronconsLayer);
        }
    });

    const all = Object.values(n).map(x => [x.latitude, x.longitude]);
    if (all.length > 0) map.fitBounds(L.latLngBounds(all).pad(0.1));

    map.on('click', function(e) {
        if (!carteData || !carteData.noeuds) return;
        let closest = null, minDist = Infinity;

        for (const node of Object.values(carteData.noeuds)) {
            const d = map.distance(e.latlng, L.latLng(node.latitude, node.longitude));
            if (d < minDist) { minDist = d; closest = node; }
        }

        if (closest && minDist < 50) {
            L.popup()
                .setLatLng([closest.latitude, closest.longitude])
                .setContent(`<b>Nœud ${closest.id}</b><br>Lat: ${closest.latitude.toFixed(6)}<br>Lng: ${closest.longitude.toFixed(6)}`)
                .openOn(map);
            console.log("Nœud proche cliqué :", closest.id);
        } else {
            console.log("Aucun nœud proche du clic.");
        }
    });
}


// ======================================================
// ============   LIVRAISONS + ENTREPOS   ===============
// ======================================================

function drawLivraisons(d){
    if(!map||!carteData) return;
    livraisonsLayer.clearLayers();
    resetTournee();
    const n=carteData.noeuds;

    const tableau = document.getElementById("tableauDemandes");
    tableau.innerHTML = "";
    const header = `
    <table style="border-collapse:collapse;">
      <thead>
        <tr>
          <th style="border-bottom:1px solid #ccc;padding:4px;">Couleur</th>
          <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">N° d'enlèvement</th>
          <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">N° de livraison</th>
        </tr>
      </thead>
      <tbody id="livraisonBody"></tbody>
    </table>
  `;
    tableau.innerHTML = header;
    const tbody = document.getElementById("livraisonBody");
    window.colorByNodeId = {};

    d.livraisons.forEach((l,i)=>{
        const color=colors[i%colors.length];
        const en=n[l.adresseEnlevement.id];
        const lv=n[l.adresseLivraison.id];
        if(!en||!lv) return;
        window.colorByNodeId[en.id] = color;
        window.colorByNodeId[lv.id] = color;
        L.marker([en.latitude,en.longitude],{
            icon:L.divIcon({className:'',iconSize:[18,18],
                html:`<div style="width:18px;height:18px;background:${color};border:2px solid black;border-radius:3px;"></div>`}),
            id: en.id
        }).addTo(livraisonsLayer)
          .on('click', e => {
              const nodeId = e.target.options.id;
              L.popup().setLatLng(e.latlng)
                       .setContent(`<b>Pickup ID ${nodeId}</b>`)
                       .openOn(map);
              console.log("Pickup cliqué :", nodeId);
          });

        // --- Livraison
        L.marker([lv.latitude,lv.longitude],{
            icon:L.divIcon({className:'',iconSize:[18,18],
                html:`<div style="width:18px;height:18px;background:${color};border:2px solid black;border-radius:50%;"></div>`}),
            id: lv.id
        }).addTo(livraisonsLayer)
          .on('click', e => {
              const nodeId = e.target.options.id;
              L.popup().setLatLng(e.latlng)
                       .setContent(`<b>Livraison ID ${nodeId}</b>`)
                       .openOn(map);
              console.log("Livraison cliquée :", nodeId);
          });
    });
}

function drawEntrepot(e){
    if(!map||!e) return;
    entrepotLayer.clearLayers();
    L.marker([e.latitude,e.longitude],{
        icon:L.divIcon({className:'',iconSize:[24,24],
            html:`<svg width="24" height="24" viewBox="0 0 26 26">
                    <polygon points="13,3 23,23 3,23" fill="#000" stroke="black" stroke-width="2"/>
                  </svg>`}),
        id: e.id
    }).addTo(entrepotLayer)
      .on('click', ev => {
          const nodeId = ev.target.options.id;
          L.popup().setLatLng(ev.latlng)
                   .setContent(`<b>Entrepôt ID ${nodeId}</b>`)
                   .openOn(map);
          console.log("Entrepôt cliqué :", nodeId);
      });
}

function addLegend(){
    const legend=L.control({position:'bottomleft'});
    legend.onAdd=function(){
        const div=L.DomUtil.create('div','legend');
        div.innerHTML=`
          <h4 style="margin:0 0 4px 0; text-align:center; font-weight:bold; font-size:12px;">Légende</h4>
          <div><svg width="16" height="16" style="margin-right:8px;"><polygon points="8,2 14,14 2,14" fill="#000" stroke="black" stroke-width="2"/></svg> Entrepôt</div>
          <div><div class="legend-symbol" style="background:#777;border:2px solid black;border-radius:3px;"></div> Pickup</div>
          <div><div class="legend-symbol" style="background:#777;border:2px solid black;border-radius:50%;"></div> Delivery</div>
          <div style="font-size:10px;color:#333;margin:3px 0;text-align:left;">Chaque couple Pickup / Delivery partage la même couleur</div>
          <div style="display:flex;align-items:center;"><div class="legend-symbol" style="width:20px;height:20px;display:flex;align-items:center;justify-content:center;font-size:14px;">🛵</div> Livreur</div>
          <div style="display:flex;align-items:center;"><svg width="32" height="16" viewBox="0 0 40 20" style="margin-right:4px;"><text x="16" y="13" font-size="12" font-weight="bold" text-anchor="middle" fill="#777" stroke="white" stroke-width="3" paint-order="stroke">1…n</text></svg> Sens de la tournée</div>`;
        return div;
    };
    legend.addTo(map);
}

// TOURNEES

async function calculTournee(nombreLivreurs = 1){
    if(!carteData || !demandeData){
        envoyerNotification("Charger la carte et la demande d'abord.", "error");
        return;
    }
    const r = await fetch(`http://localhost:8080/api/tournee/calculer?nombreLivreurs=${nombreLivreurs}`, { method:"POST" });
    if(!r.ok){
        envoyerNotification(await r.text(), "error");
        return;
    }
    envoyerNotification("Toutes les tournées ont été calculées avec succès");
    const toutesLesTournees = await r.json();
    window.toutesLesTournees = toutesLesTournees.data.tournees;

    resetTournee();
    window.toutesLesTournees.forEach((tournee, i) => {
        const color = colors[i % colors.length];
        drawTournee(tournee, color, i);
    });
    drawTourneeTable(window.toutesLesTournees[0]);
    addAnimationButton();
    await updateUIFromEtat();
}

function resetTournee(){
    if(window.tourneeLayer) window.tourneeLayer.clearLayers();
    if(window.directionNumbersLayer) window.directionNumbersLayer.clearLayers();
    stopAnimation();
    if(map && animControl){ map.removeControl(animControl); animControl=null; }
    window.animPaths = {}; isAnimating=false; isPaused=false;
}

function drawTournee(t, color='#000', index) {
    if (!map || !carteData) return;
    if (!window.tourneeLayer) window.tourneeLayer = L.layerGroup().addTo(map);
    if (!window.directionNumbersLayer) window.directionNumbersLayer = L.layerGroup().addTo(map);
    if (!window.animPaths) window.animPaths = {};

    const n = carteData.noeuds;
    const all = [];
    const K = 6;
    let labelCount = 0;
    let localAnimPath = [];

    t.chemins.forEach(c => {
        const latlngs = [];
        c.troncons.forEach((tc, i) => {
            const o = n[tc.idOrigine], d = n[tc.idDestination];
            if (!o || !d) return;
            const A = [o.latitude, o.longitude], B = [d.latitude, d.longitude];
            latlngs.push(A);
            latlngs.push(B);
            all.push(A);
            all.push(B);
            if (i % K === 0) {
                labelCount++;
                L.marker(midPoint(A, B), {icon: makeStepNumberIcon(labelCount, color)}).addTo(window.directionNumbersLayer);
            }
        });
        if (latlngs.length > 0) {
            L.polyline(latlngs, {color, weight: 3, opacity: 0.9}).addTo(window.tourneeLayer);
            localAnimPath = localAnimPath.concat(
                localAnimPath.length > 0 && localAnimPath.at(-1)[0] === latlngs[0][0]
                    ? latlngs.slice(1)
                    : latlngs
            );
        }
    });
    if (all.length > 0) map.fitBounds(L.latLngBounds(all).pad(0.1));
    window.animPaths[index] = [...localAnimPath];
}

function drawTourneeTable(tournee){
    const tableauTournees = document.getElementById("tableauTournees");
    tableauTournees.innerHTML = `
  <table style="border-collapse:collapse;">
    <thead>
      <tr>
        <th style="border-bottom:1px solid #ccc;padding:4px;">Couleur</th>
        <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Ordre</th>
        <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Type</th>
        <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">ID</th>
        <th style="border-bottom:1px solid #ccc;border-left:1px solid #ccc;padding:4px;">Fourchette horaire</th>
      </tr>
    </thead>
    <tbody id="tourneeBody"></tbody>
  </table>
`;
    const tbody = document.getElementById("tourneeBody");
    let ordre = 1;

    const chemins = tournee.chemins;
    const firstNode = chemins[0]?.noeudDePassageDepart;
    const lastNode = chemins[chemins.length-1]?.noeudDePassageArrivee;

    chemins.forEach(c => {
        const noeudsChemin = [(c.noeudDePassageDepart && c.noeudDePassageDepart.type === "ENTREPOT") ? c.noeudDePassageDepart : null, c.noeudDePassageArrivee];
        noeudsChemin.forEach(noeud => {
            if (!noeud) return;
            if (["ENTREPOT","PICKUP","DELIVERY"].includes(noeud.type)) {

                const color = window.colorByNodeId?.[noeud.id] || "#000000";
                const bordercolor = color !== "#000000"? "solid black":"white";

                let horaire = "";
                if (noeud === firstNode && noeud.type === "ENTREPOT") {
                    horaire = noeud.horaireDepart || "-";
                } else if (noeud === lastNode && noeud.type === "ENTREPOT") {
                    horaire = formatHoraireFourchette(noeud.horaireArrivee) || "-";
                } else {
                    horaire = formatHoraireFourchette(noeud.horaireArrivee) || "-";
                }

                const row = document.createElement("tr");
                row.innerHTML = `
              <td style="padding:4px;text-align:center;">
                <div style="width:18px;height:18px;background:${color};
                    border:1px solid ${bordercolor};border-radius:3px;margin:auto;"></div>
              </td>
              <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${ordre++}</td>
              <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${noeud.type}</td>
              <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${noeud.id}</td>
              <td style="border-left:1px solid #ccc;padding:4px;text-align:center;">${horaire}</td>
            `;
                tbody.appendChild(row);
            }
        });
    });
}

async function creerFeuillesDeRoute() {
    try {
        const response = await fetch("http://localhost:8080/api/tournee/feuille-de-route", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            const errText = await response.text();
            envoyerNotification("Erreur lors de la création de la feuille de route : " + errText, "error");
            return;
        }

        const res = await response.json();
        envoyerNotification(res.message);
    } catch (error) {
        console.error(error);
        envoyerNotification("Erreur réseau ou serveur : " + error.message, "error");
    }
}

async function sauvegarderTournee() {
    try {
        const response = await fetch("http://localhost:8080/api/tournee/sauvegarde", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            const errText = await response.text();
            envoyerNotification("Erreur lors de la sauvegarde de la tournée : " + errText, "error");
            return;
        }

        const res = await response.json();
        envoyerNotification(res.message);
    } catch (error) {
        console.error(error);
        envoyerNotification("Erreur réseau ou serveur : " + error.message, "error");
    }
}

// ANIMATION

function startAnimation(index = 0) {
    stopAnimation();

    const path = window.animPaths[index];
    if (!path || path.length < 2) return;
    animSamples = samplePathMeters(path, STEP_M);
    animIndex = 0;

    courierMarker = L.marker(animSamples[0], {
        icon: L.divIcon({
            className: '',
            iconSize: [24, 24],
            html: `<div style="font-size:20px;text-shadow:1px 1px 3px rgba(0,0,0,0.4);">🛵</div>`
        })
    }).addTo(map);

    runAnimation();
}

function runAnimation(){
    const interval=50;
    animTimer=setInterval(()=>{
        if(animIndex>=animSamples.length-1){ stopAnimation(); return; }
        const metersPerFrame=animSpeed*(interval/1000); let next=animIndex+1;
        let dist=map.distance(L.latLng(animSamples[animIndex]),L.latLng(animSamples[next]));
        while(next<animSamples.length-1 && dist<metersPerFrame){ next++; dist+=map.distance(L.latLng(animSamples[next-1]),L.latLng(animSamples[next])); }
        animIndex=Math.min(next,animSamples.length-1); courierMarker.setLatLng(animSamples[animIndex]);
    },interval);
}

function pauseAnimation(){ if(animTimer){ clearInterval(animTimer); animTimer=null; } }
function resumeAnimation(){ if(!animTimer) runAnimation(); }
function stopAnimation(){ if(animTimer){ clearInterval(animTimer); animTimer=null; } if(courierMarker){ map.removeLayer(courierMarker); courierMarker=null; } }

function addAnimationButton() {
    if (animControl) { map.removeControl(animControl); animControl = null; }

    animControl = L.control({ position: 'topleft' });
    animControl.onAdd = function () {
        const div = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-custom');
        const tourneeOptions = Object.keys(window.animPaths)
            .map(i => `<option value="${i}">Tournée ${parseInt(i) + 1}</option>`)
            .join('');

        div.innerHTML = `
          <div style="background:rgba(255,255,255,0.85);border-radius:10px;
                      box-shadow:0 0 5px rgba(0,0,0,0.3);padding:8px;
                      font-size:13px;text-align:center;width:160px;color:black;">
            <h4 style="margin:0 0 8px 0;font-weight:bold;">Livreur</h4>
            <select id="tourneeSelect" style="width:100%;margin-bottom:6px;">${tourneeOptions}</select>
            <button id="btnStartStop" style="background:#b2d1d2;border:none;padding:5px 8px;
                     border-radius:6px;cursor:pointer;width:100%;margin-bottom:5px;">Lancer l’animation</button>
            <button id="btnPause" style="background:#ddd;border:none;padding:5px 8px;
                     border-radius:6px;cursor:pointer;width:100%;">Pause</button>
          </div>`;

        const start = div.querySelector('#btnStartStop');
        const pause = div.querySelector('#btnPause');
        const select = div.querySelector('#tourneeSelect');

        select.onchange = e => {
            selectedIndex = parseInt(e.target.value);
            const tournee = window.toutesLesTournees[selectedIndex];
            drawTourneeTable(tournee);
        };

        start.onclick = e => {
            e.stopPropagation();
            if (!isAnimating) {
                start.textContent = 'Arrêter l’animation';
                startAnimation(selectedIndex);
                isAnimating = true;
                pause.disabled = false;
            } else {
                start.textContent = 'Lancer l’animation';
                stopAnimation();
                isAnimating = false;
                isPaused = false;
                pause.textContent = 'Pause';
                pause.disabled = true;
            }
        };

        pause.onclick = e => {
            e.stopPropagation();
            if (!isAnimating) return;
            if (!isPaused) {
                pauseAnimation();
                isPaused = true;
                pause.textContent = 'Reprendre';
            } else {
                resumeAnimation();
                isPaused = false;
                pause.textContent = 'Pause';
            }
        };

        pause.disabled = true;
        return div;
    };
    animControl.addTo(map);
}

// AFFICHAGE GLOBAL

async function updateUIFromEtat() {
    try {
        const res = await fetch("http://localhost:8080/api/etat");
        if(!res.ok) return;
        const data = await res.json();

        document.getElementById('welcome-message').style.display = "none";
        document.getElementById('carte-chargee-message').style.display = "none";
        document.getElementById('livraisons').style.display = "none";
        document.getElementById('tableauDemandes').style.display = "none";
        document.getElementById('tournee-chargee').style.display = "none";
        document.getElementById('tableauTournees').style.display = "none";
        document.getElementById('calcul-tournee').style.display = "none";
        document.getElementById('fileNameCarte').style.display = "none";
        document.getElementById('fileNameDemande').style.display = "none";
        document.getElementById('xmlDemande').disabled = false;
        document.getElementById('inputTournee').disabled = false;
        document.querySelector('.navbar-item img[alt="Ajouter une demande de livraison"]').style.filter = "";
        document.querySelector('.navbar-item img[alt="Ajouter une carte"]').style.filter = "";
        document.querySelector('.navbar-item img[alt="Ajouter une demande de livraison"]').src = "tools/colis-logo-white.png";
        document.querySelector('.navbar-item img[alt="Ajouter une demande de livraison"]').style.cursor = "pointer";
        document.querySelector('.navbar-item img[alt="Charger une tournée"]').src = "tools/open-logo.png";
        document.querySelector('.navbar-item img[alt="Charger une tournée"]').style.cursor = "pointer";
        document.getElementById('map').style.display = "block";
        document.getElementById('tournee-modifier').style.display = "none";
        if(data.data.etat === "Etat Initial") {
            document.getElementById('welcome-message').style.display = "flex";
            document.querySelector('.navbar-item img[alt="Ajouter une carte"]').style.filter = "drop-shadow(0 0 10px rgba(225,225,0,1))";
            document.querySelector('.navbar-item img[alt="Ajouter une demande de livraison"]').src = "tools/colis-logo-gray.png";
            document.querySelector('.navbar-item img[alt="Ajouter une demande de livraison"]').style.cursor = "not-allowed";
            document.querySelector('.navbar-item img[alt="Charger une tournée"]').style.cursor = "not-allowed";
            document.querySelector('.navbar-item img[alt="Charger une tournée"]').src = "tools/open-logo-gray.png";
            document.getElementById('xmlDemande').disabled = true;
            document.getElementById('inputTournee').disabled = true;
            document.getElementById('tableauDemandes').innerHTML = "";
            document.getElementById('tableauTournees').innerHTML = "";
            document.getElementById('map').style.display = "none";
        } else if (data.data.etat === "ModeModificationTournee"){
            document.getElementById('tableauTournees').style.display = "inline";
            document.getElementById('tournee-modifier').style.display = "inline";
        } else if(data.data.tourneeChargee) {
            document.getElementById('tournee-chargee').style.display = "inline";
            document.getElementById('tableauTournees').style.display = "inline";
        } else if(data.data.demandeChargee) {
            document.getElementById('livraisons').style.display = "inline";
            document.getElementById('fileNameDemande').style.display = "inline";
            document.getElementById('calcul-tournee').style.display = "flex";
            document.getElementById('tableauDemandes').style.display = "inline";
        } else if (data.data.carteChargee) {
            document.getElementById('carte-chargee-message').style.display = "flex";
            document.getElementById('fileNameCarte').style.display = "inline";
            document.querySelector('.navbar-item img[alt="Ajouter une demande de livraison"]').style.filter = "drop-shadow(0 0 10px rgba(225,225,0,1))";
        }

    } catch(err){
        console.error(err);
    }
}

// GESTION DES CLICS

document.addEventListener('DOMContentLoaded',async () => {
    await updateUIFromEtat();

    document.querySelector('.navbar-item img[alt="Ajouter une carte"]').addEventListener('click', () => {
        document.getElementById('xmlCarte').click();
    });

    document.querySelector('.navbar-item img[alt="Ajouter une demande de livraison"]').addEventListener('click', () => {
        if (!document.getElementById('xmlDemande').disabled) {
            document.getElementById('xmlDemande').click();
        }
    });

    document.getElementById('calculerTournee').addEventListener('click', async () => {
        let nbLivreurs = parseInt(document.getElementById('nbLivreurs').value);

        if (isNaN(nbLivreurs) || nbLivreurs < 1) nbLivreurs = 1;
        const max = demandeData?.livraisons?.length || 1;
        if (nbLivreurs > max) nbLivreurs = max;

        document.getElementById('nbLivreurs').value = nbLivreurs;

        await calculTournee(nbLivreurs);
    });

    document.getElementById('xmlCarte').addEventListener('change', function () {
        const file = this.files[0];
        document.getElementById('fileNameCarte').textContent = file ? `Affichage de ${file.name}` : "";
        if (file) uploadCarte(file);
    });

    document.getElementById('xmlDemande').addEventListener('change', function () {
        const file = this.files[0];
        document.getElementById('fileNameDemande').textContent = file ? `Affichage de ${file.name}` : "";
        if (file) uploadDemande(file);
    });

    document.getElementById('homeButton').addEventListener('click', () => {
        const confirmReset = confirm("Voulez-vous vraiment revenir à la page d’accueil ? Toutes les données chargées seront perdues.");

        if (confirmReset) {
            fetch("http://localhost:8080/api/reset", {method: "POST"})
                .then(response => response.json())
                .then(async data => {
                    if (map) {
                        map.eachLayer(l => map.removeLayer(l));
                        map.remove();
                        map = null;
                    }

                    carteData = null;
                    demandeData = null;
                    livraisonsLayer = null;
                    entrepotLayer = null;
                    window.tronconsLayer = null;
                    window.tourneeLayer = null;
                    window.directionNumbersLayer = null;
                    window.animPaths = {};
                    window.toutesLesTournees = [];

                    await updateUIFromEtat();
                })
                .catch(err => {
                    console.error("Erreur lors de la réinitialisation :", err);
                    envoyerNotification("Erreur lors de la réinitialisation du serveur: " + err.message, "error");
                });
        }
    });

    document.getElementById('creerFeuillesDeRoute').addEventListener('click', () => {
        creerFeuillesDeRoute();
    });

    document.getElementById('sauvegarderTournee').addEventListener('click', () => {
        sauvegarderTournee();
    });

    document.getElementById('modifierTournee').addEventListener('click', () => {
        const tournee = window.toutesLesTournees[selectedIndex];
        fetch("http://localhost:8080/api/tournee/mode-modification", {method: "POST", headers: {"Content-Type": "application/json"},body: JSON.stringify(tournee)})
            .then(response => response.json())
            .then(async data => {
                resetTournee();
                drawTournee(tournee, colors[0], 0)
                await updateUIFromEtat();
            });
    });

    document.querySelector('.navbar-item img[alt="Charger une tournée"]').addEventListener("click", () => {
        document.getElementById("inputTournee").click();
    });

    document.querySelectorAll('.navbar-item img').forEach(img => {
        img.title = img.alt;
    });

    document.getElementById("inputTournee").addEventListener("change", async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch("http://localhost:8080/api/upload-tournee", {
                method: "POST",
                body: formData
            });

            const data = await response.json();

            if (data.success) {
                window.toutesLesTournees = data.data.tournees || [];
                demandeData = data.data.demande ||[];
                resetLivraisons();
                resetTournee();

                drawLivraisons(data.data.demande);
                window.toutesLesTournees.forEach((t, i) => {
                    const color = colors[i % colors.length];
                    drawTournee(t, color, i);
                });
                drawTourneeTable(window.toutesLesTournees[0])
                addAnimationButton();
                await updateUIFromEtat();
            } else {
                console.error("Erreur serveur :", data.data.message);
                envoyerNotification(data.data.message, "error");
            }
        } catch (err) {
            console.error("Erreur fetch :", err);
            envoyerNotification("Erreur réseau lors du chargement de la tournée", "error");
        } finally {
            document.getElementById('inputTournee').value = '';
        }
    });

    window.addEventListener("beforeunload", () => {
        fetch("http://localhost:8080/api/reset", { method: "POST" });
    });

    window.addEventListener("load", async () => {
        await updateUIFromEtat();
    });

    document.getElementById('modeSupression').addEventListener("click", async () => {
        const formData = new FormData();
        formData.append("idNoeudPickup", idNoeudPickup);
        formData.append("idNoeudDelivery", idNoeudDelivery);
        formData.append("mode", "supprimer");
        const response = await fetch("http://localhost:8080/api/tournee/mode-modification", {
            method: "POST",
            body: formData
        });
        const data = await response.json();
        if (data.success) {
            console.log(data.data);
            // TRAITEMENT DE LA SUPPRESSION
        }
    });
});

function envoyerNotification(message, type = "success", duration = 3000) {
    const notification = document.getElementById("notification");
    notification.textContent = message;
    notification.className = "notification " + type;
    notification.style.display = "block";

    setTimeout(() => {
        notification.style.display = "none";
    }, duration);
}
