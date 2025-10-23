let map=null, carteData=null, demandeData=null;
let livraisonsLayer=null, entrepotLayer=null;
let animTimer=null, courierMarker=null, animPath=[];
let animControl=null, isAnimating=false, isPaused=false;
let animSpeed=8, animSamples=[], animIndex=0;

const colors=[
  '#e6194b','#3cb44b','#ffe119','#4363d8','#f58231','#911eb4','#46f0f0',
  '#f032e6','#bcf60c','#fabebe','#008080','#e6beff','#9a6324','#fffac8',
  '#800000','#aaffc3','#808000','#ffd8b1','#000075','#808080','#000000'
];
const STEP_M=10;

function resetCarte() {
  if (map) {
    map.eachLayer(l => {
      if (!(l instanceof L.TileLayer)) map.removeLayer(l);
    });
    if (animControl) map.removeControl(animControl);
  }
  carteData=null; demandeData=null;
  animPath=[]; isAnimating=false; isPaused=false;
  if (animTimer) { clearInterval(animTimer); animTimer=null; }
  courierMarker=null; livraisonsLayer=null; entrepotLayer=null; animControl=null;
  document.getElementById('xmlDemande').disabled = true;
  document.getElementById('labelDemande').style.background = '#999';
  document.getElementById('labelDemande').style.cursor = 'not-allowed';
  document.getElementById('btnCalculTournee').disabled = true;
  document.getElementById('fileNameDemande').textContent = '';
}

async function uploadCarte(file){
  resetCarte();
  const f=new FormData(); f.append("file",file);
  const r=await fetch("http://localhost:8080/api/upload-carte",{method:"POST",body:f});
  if(!r.ok){ alert(await r.text()); return; }
  carteData=await r.json();
  drawCarte(carteData);
  xmlDemande.disabled=false;
  labelDemande.style.background="#b2d1d2";
  labelDemande.style.cursor="pointer";
}

async function uploadDemande(file){
  resetTournee();
  const f=new FormData(); f.append("file",file);
  const r=await fetch("http://localhost:8080/api/upload-demande",{method:"POST",body:f});
  if(!r.ok){ alert(await r.text()); return; }
  demandeData=await r.json();
  drawLivraisons(demandeData);
  drawEntrepot(demandeData.entrepot);
  if(carteData) btnCalculTournee.disabled=false;
}

function resetTournee(){
  if(window.tourneeLayer) window.tourneeLayer.clearLayers();
  if(window.directionNumbersLayer) window.directionNumbersLayer.clearLayers();
  stopAnimation();
  if(animControl){ map.removeControl(animControl); animControl=null; }
  animPath=[]; isAnimating=false; isPaused=false;
}

function drawCarte(c){
  const n=c.noeuds||{}, t=c.troncons||[];
  const m=document.getElementById('map'); m.style.display="block";
  if(!map){
    map=L.map('map');
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'© OpenStreetMap'}).addTo(map);
    addLegend();
  }
  livraisonsLayer=L.layerGroup().addTo(map);
  entrepotLayer=L.layerGroup().addTo(map);
  t.forEach(tc=>{
    const o=n[tc.idOrigine], d=n[tc.idDestination];
    if(o&&d) L.polyline([[o.latitude,o.longitude],[d.latitude,d.longitude]],{color:'#1a74bb',weight:2}).addTo(map);
  });
  const all=Object.values(n).map(x=>[x.latitude,x.longitude]);
  if(all.length>0) map.fitBounds(L.latLngBounds(all).pad(0.1));
}

function drawLivraisons(d){
  if(!map || !carteData) return;
  livraisonsLayer.clearLayers();
  const n = carteData.noeuds;
  const allCoords = [];
  d.livraisons.forEach((l,i)=>{
    const color = colors[i % colors.length];
    const en = n[l.adresseEnlevement.id];
    const lv = n[l.adresseLivraison.id];
    if(!en || !lv) return;
    allCoords.push([en.latitude, en.longitude]);
    allCoords.push([lv.latitude, lv.longitude]);
    L.marker([en.latitude,en.longitude],{
      icon: L.divIcon({
        className:'',
        iconSize:[18,18],
        html:`<div style="width:18px;height:18px;background:${color};border:2px solid black;border-radius:3px;"></div>`
      })
    }).addTo(livraisonsLayer);
    L.marker([lv.latitude,lv.longitude],{
      icon: L.divIcon({
        className:'',
        iconSize:[18,18],
        html:`<div style="width:18px;height:18px;background:${color};border:2px solid black;border-radius:50%;"></div>`
      })
    }).addTo(livraisonsLayer);
  });
  if(allCoords.length > 0) {
    map.fitBounds(L.latLngBounds(allCoords).pad(0.1));
  }
}

function drawEntrepot(e){
  if(!map||!e) return;
  entrepotLayer.clearLayers();
  L.marker([e.latitude,e.longitude],{
    icon:L.divIcon({className:'',iconSize:[24,24],
      html:`<svg width="24" height="24" viewBox="0 0 26 26">
              <polygon points="13,3 23,23 3,23" fill="#000" stroke="black" stroke-width="2"/>
            </svg>`})
  }).addTo(entrepotLayer);
}

async function calculTournee(){
  if(!carteData||!demandeData){ alert("Charger la carte et la demande d'abord."); return; }
  const r=await fetch("http://localhost:8080/api/tournee/calculer",{method:"POST"});
  if(!r.ok){ alert(await r.text()); return; }
  const t=await r.json();
  drawTournee(t);
}

function makeStepNumberIcon(n,color){
  const svg=`<svg xmlns="http://www.w3.org/2000/svg" width="34" height="18" viewBox="0 0 34 18">
                <text x="17" y="14" font-size="12" font-weight="bold" text-anchor="middle"
                      fill="${color}" stroke="white" stroke-width="3" paint-order="stroke">${n}</text>
             </svg>`;
  return L.divIcon({className:'',html:svg,iconSize:[34,18]});
}

function midPoint(a,b){ return [(a[0]+b[0])/2,(a[1]+b[1])/2]; }

function drawTournee(t){
  if(!map||!carteData) return;
  if(window.tourneeLayer) window.tourneeLayer.clearLayers(); else window.tourneeLayer=L.layerGroup().addTo(map);
  if(window.directionNumbersLayer) window.directionNumbersLayer.clearLayers(); else window.directionNumbersLayer=L.layerGroup().addTo(map);
  const n=carteData.noeuds, all=[], color='#000', K=6; let labelCount=0; animPath=[];
  t.chemins.forEach(c=>{
    const latlngs=[];
    c.troncons.forEach((tc,i)=>{
      const o=n[tc.idOrigine], d=n[tc.idDestination]; if(!o||!d) return;
      const A=[o.latitude,o.longitude], B=[d.latitude,d.longitude];
      latlngs.push(A); latlngs.push(B); all.push(A); all.push(B);
      if(i%K===0){ labelCount++; L.marker(midPoint(A,B),{icon:makeStepNumberIcon(labelCount,color)}).addTo(window.directionNumbersLayer); }
    });
    if(latlngs.length>0){
      L.polyline(latlngs,{color,weight:3,opacity:0.9}).addTo(window.tourneeLayer);
      animPath=animPath.concat(animPath.length>0&&animPath.at(-1)[0]===latlngs[0][0]?latlngs.slice(1):latlngs);
    }
  });
  if(all.length>0) map.fitBounds(L.latLngBounds(all).pad(0.1));
  addAnimationButton();
}

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

function startAnimation(){
  stopAnimation();
  if(animPath.length<2) return;
  animSamples=samplePathMeters(animPath,STEP_M); animIndex=0;
  courierMarker=L.marker(animSamples[0],{icon:L.divIcon({className:'',iconSize:[24,24],
    html:`<div style="font-size:20px;text-shadow:1px 1px 3px rgba(0,0,0,0.4);">🛵</div>`})}).addTo(map);
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

function addAnimationButton(){
  if(animControl){ map.removeControl(animControl); animControl=null; }
  animControl=L.control({position:'topleft'});
  animControl.onAdd=function(){
    const div=L.DomUtil.create('div','leaflet-bar leaflet-control leaflet-control-custom');
    div.innerHTML=`
      <div style="background:rgba(255,255,255,0.85);border-radius:10px;
                  box-shadow:0 0 5px rgba(0,0,0,0.3);padding:8px;
                  font-size:13px;text-align:center;width:160px;">
        <h4 style="margin:0 0 8px 0;font-weight:bold;">Livreur</h4>
        <button id="btnStartStop" style="background:#b2d1d2;border:none;padding:5px 8px;
                 border-radius:6px;cursor:pointer;width:100%;margin-bottom:5px;">Lancer l’animation</button>
        <button id="btnPause" style="background:#ddd;border:none;padding:5px 8px;
                 border-radius:6px;cursor:pointer;width:100%;">Pause</button>
      </div>`;
    const start=div.querySelector('#btnStartStop'), pause=div.querySelector('#btnPause');
    start.onclick=e=>{
      e.stopPropagation();
      if(!isAnimating){ start.textContent='Arrêter l’animation'; startAnimation(); isAnimating=true; pause.disabled=false; }
      else{ start.textContent='Lancer l’animation'; stopAnimation(); isAnimating=false; isPaused=false; pause.textContent='Pause'; pause.disabled=true; }
    };
    pause.onclick=e=>{
      e.stopPropagation(); if(!isAnimating) return;
      if(!isPaused){ pauseAnimation(); isPaused=true; pause.textContent='Reprendre'; }
      else{ resumeAnimation(); isPaused=false; pause.textContent='Pause'; }
    };
    pause.disabled=true; return div;
  };
  animControl.addTo(map);
}

function addLegend(){
  const legend=L.control({position:'bottomleft'});
  legend.onAdd=function(){
    const div=L.DomUtil.create('div','legend');
    div.innerHTML=`
      <h4 style="margin:0 0 4px 0; text-align:center; font-weight:bold; font-size:12px;">Légende</h4>
      <div><svg width="16" height="16"><polygon points="8,2 14,14 2,14" fill="#000" stroke="black" stroke-width="2"/></svg> Entrepôt</div>
      <div><div class="legend-symbol" style="background:#777;border:2px solid black;border-radius:3px;"></div> Pickup</div>
      <div><div class="legend-symbol" style="background:#777;border:2px solid black;border-radius:50%;"></div> Delivery</div>
      <div style="font-size:10px;color:#333;margin:3px 0;text-align:left;">Chaque couple Pickup / Delivery partage la même couleur</div>
      <div style="display:flex;align-items:center;"><div class="legend-symbol" style="width:20px;height:20px;display:flex;align-items:center;justify-content:center;font-size:14px;">🛵</div> Livreur</div>
      <div style="display:flex;align-items:center;"><svg width="32" height="16" viewBox="0 0 40 20" style="margin-right:4px;"><text x="16" y="13" font-size="12" font-weight="bold" text-anchor="middle" fill="#777" stroke="white" stroke-width="3" paint-order="stroke">1…n</text></svg> Sens de la tournée</div>`;
    return div;
  };
  legend.addTo(map);
}

document.addEventListener('DOMContentLoaded',()=>{
  xmlCarte.addEventListener('change',()=>{ const f=xmlCarte.files[0]; fileNameCarte.textContent=f?f.name:""; if(f) uploadCarte(f); });
  xmlDemande.addEventListener('change',()=>{ const f=xmlDemande.files[0]; fileNameDemande.textContent=f?f.name:""; if(f) uploadDemande(f); });
  btnCalculTournee.addEventListener('click', async ()=>{ await calculTournee(); });
});