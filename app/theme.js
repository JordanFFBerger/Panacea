let themeMode=localStorage.getItem('rr-theme')||'auto';
function applyTheme(){
 const hour=+new Intl.DateTimeFormat('en-US',{timeZone:'America/Chicago',hour:'numeric',hourCycle:'h23'}).format(new Date());
 const dark=themeMode==='night'||themeMode==='auto'&&(hour<7||hour>=18);
 document.documentElement.dataset.theme=dark?'night':'day';
 document.querySelector('#theme span').textContent=themeMode[0].toUpperCase()+themeMode.slice(1);
 document.querySelector('#theme').setAttribute('aria-label',`Theme: ${themeMode}. Click to switch theme.`);
}
document.querySelector('#theme').addEventListener('click',()=>{themeMode=({auto:'day',day:'night',night:'auto'})[themeMode];localStorage.setItem('rr-theme',themeMode);applyTheme()});
applyTheme();setInterval(applyTheme,60000);
