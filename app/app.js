const $ = s => document.querySelector(s);
let category='All', view='discover';
let saved=JSON.parse(localStorage.getItem('rr-saved')||'[]');
let joined=JSON.parse(localStorage.getItem('rr-joined')||'[]');
const money = n => n===0?'Free':'$'+n;
const day = e => {const d=new Date(base);d.setDate(d.getDate()+e.offset);return d};
const dateLabel = e => day(e).toLocaleDateString('en-US',{month:'short',day:'numeric',weekday:'short'});

function render(){
 $('#categories').innerHTML=['All','Music','Food','Arts','Books','Gaming','Outdoors'].map(c=>`<button class="${category===c?'active':''}" data-category="${c}" aria-pressed="${category===c}">${c==='All'?'✳ ':''}${c}</button>`).join('');
 let list=events.filter(e=>(category==='All'||e.category===category)&&`${e.title} ${e.venue} ${e.activities.join(' ')} ${e.category}`.toLowerCase().includes($('#search').value.toLowerCase()));
 const period=$('#date').value;
 list=list.filter(e=>period==='all'||period==='week'&&e.offset<=7||period==='month'&&e.offset<=30||period==='weekend'&&[0,6].includes(day(e).getDay()));
 list=list.filter(e=>$('#price').value==='all'||$('#price').value==='free'&&e.price===0||$('#price').value==='25'&&e.price<=25);
 if(view==='saved')list=list.filter(e=>saved.includes(e.id));
 if(view==='profile')list=list.filter(e=>joined.includes(e.id));
 if($('#sort').value==='trending')list.sort((a,b)=>b.recent-a.recent);
 if($('#sort').value==='price')list.sort((a,b)=>a.price-b.price);
 if($('#sort').value==='date')list.sort((a,b)=>a.offset-b.offset);
 $('#view-title').textContent=({discover:'Around the Red Stick',nearby:'Good things, close by',saved:'Your saved discoveries',profile:'Your next good stories'})[view];
 $('#result-count').textContent=`${list.length} ${list.length===1?'event':'events'} to get into · Sample listings`;
 $('#saved-count').textContent=saved.length;
 $('#events').innerHTML=list.length?list.map(e=>`<article class="card"><button class="art ${e.art}" data-open="${e.id}" aria-label="Details for ${e.title}"><span class="poster-word">${e.category.toUpperCase()}</span><span class="poster-symbol">${e.icon}</span><span class="poster-caption">BATON ROUGE / GOOD TIMES AHEAD</span></button><button class="save ${saved.includes(e.id)?'selected':''}" data-save="${e.id}" aria-label="${saved.includes(e.id)?'Unsave':'Save'} ${e.title}" aria-pressed="${saved.includes(e.id)}">${saved.includes(e.id)?'♥':'♡'}</button><div class="card-body"><div class="card-meta"><span>${e.category} / ${dateLabel(e)}</span><strong class="price">${money(e.price)}</strong></div><button class="event-title" data-open="${e.id}">${e.title} ↗</button><p class="venue">${e.time} · ${e.venue}</p><p class="activities">${e.activities.join(' · ')}</p><div class="card-bottom"><span class="${trending(e)?'trending':'steady'}">${trending(e)?'↗ Trending':'◌ Gathering good company'}</span><span>${e.signups+(joined.includes(e.id)?1:0)} signed up</span></div></div></article>`).join(''):'<div class="empty"><h3>A little quiet here.</h3><p>Try another filter, or discover something new to save.</p><button id="reset">Explore all events ↗</button></div>';
 $('#nearby-map').hidden=view!=='nearby';
 if(view==='nearby')$('#nearby-map').innerHTML=`<div class="map-river"></div><span class="map-label">MISSISSIPPI RIVER</span><span class="map-city">BATON ROUGE</span>${list.map(e=>`<button class="map-pin" style="left:${e.x}%;top:${e.y}%" data-open="${e.id}">${e.icon} <span>${e.title} · ${e.distance} mi</span></button>`).join('')}<small>Illustrative area map · distances are samples</small>`;
 $('#profile').hidden=view!=='profile';
 $('#profile').innerHTML='<h3>Your local guest profile</h3><p>Plans and saved events stay in this browser. Demo signups are not real reservations.</p>';
 document.querySelectorAll('[data-view]').forEach(b=>b.classList.toggle('active',b.dataset.view===view));
}
document.addEventListener('click',ev=>{const b=ev.target.closest('button');if(!b)return;if(b.dataset.category){category=b.dataset.category;render()}if(b.dataset.view){view=b.dataset.view;render()}if(b.dataset.open)detail(+b.dataset.open);if(b.dataset.save){const id=+b.dataset.save;saved=saved.includes(id)?saved.filter(x=>x!==id):[...saved,id];localStorage.setItem('rr-saved',JSON.stringify(saved));render();if($('#detail').open)detail(id)}if(b.dataset.join){const id=+b.dataset.join;joined=joined.includes(id)?joined.filter(x=>x!==id):[...joined,id];localStorage.setItem('rr-joined',JSON.stringify(joined));render();detail(id)}if(b.id==='reset'){category='All';view='discover';$('#search').value='';$('#date').value='all';$('#price').value='all';render()}});
$('#close-detail').onclick=()=>$('#detail').close();$('#detail').addEventListener('click',e=>{if(e.target===$('#detail'))$('#detail').close()});
['search','date','price','sort'].forEach(id=>$('#'+id).addEventListener(id==='search'?'input':'change',render));
render();

