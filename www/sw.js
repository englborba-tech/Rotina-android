const CACHE="rotina-v2";
const ARQ=["./","index.html","manifest.json","icon-192.png","icon-512.png","vendor/firebase-app-compat.js","vendor/firebase-auth-compat.js","vendor/firebase-firestore-compat.js"];
self.addEventListener("install",e=>{e.waitUntil(caches.open(CACHE).then(c=>Promise.all(ARQ.map(u=>c.add(u).catch(()=>{})))));self.skipWaiting();});
self.addEventListener("activate",e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!==CACHE).map(x=>caches.delete(x)))));self.clients.claim();});
self.addEventListener("fetch",e=>{
  if(e.request.method!=="GET")return;
  const u=new URL(e.request.url),proprio=u.origin===location.origin;
  if(!proprio&&!/fonts\.(googleapis|gstatic)\.com/.test(u.href))return;
  // arquivos do app: sempre confere se há versão nova; sem internet, usa a cópia guardada
  const busca=proprio?fetch(e.request.url,{cache:"no-cache",credentials:"same-origin"}):fetch(e.request);
  e.respondWith(busca.then(r=>{if(r.ok){const cp=r.clone();caches.open(CACHE).then(c=>c.put(e.request,cp));}return r;})
    .catch(()=>caches.match(e.request,{ignoreSearch:true}).then(r=>r||caches.match("index.html"))));
});
