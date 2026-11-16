
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  baseHref: "/",
  criticalCssPlans: [[1,"/styles-NAWXTFEV.css",6608,["","*",":after",":before","hr","h1","h2","h3","h4","h5","h6","a","b","strong","{font-weight:bolder}","code","kbd","samp","pre","small","sub","sup","table","progress","summary","ol","ul","menu","img","svg","video","canvas","audio","iframe","embed","object","button","input","select","optgroup","textarea","btn-primary",".btn-primary:focus-visible",".btn-primary:disabled","alert-error","@layer utilities{","@layer properties{"],[["@layer theme{"],["@layer base{"],[46],[46,"@media(hover:hover){"],["@layer components{"],[47],[47,"@supports ((-webkit-hyphens: none) and (not (margin-trim: block))) or ((-moz-orient: inline) and (not (color:rgb(from red r g b)))){"]],[[10,"@charset \"UTF-8\";"],[10,"@layer properties;"],[10,"@layer theme,base,components,utilities;"],[26,1,0],[21,":root","{--font-sans:\"Inter\", ui-sans-serif, system-ui, sans-serif;--font-mono:ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace;--spacing:.25rem;--font-weight-medium:500;--font-weight-semibold:600;--radius-md:.375rem;--radius-lg:.5rem;--animate-pulse:pulse 2s cubic-bezier(.4, 0, .6, 1) infinite;--default-transition-duration:.15s;--default-transition-timing-function:cubic-bezier(.4, 0, .2, 1);--default-font-family:var(--font-sans);--default-mono-font-family:var(--font-mono);--color-fondo:#0B0B0F;--color-superficie:#15151B;--color-primario:#8A0009;--color-primario-hover:#A5000B;--color-texto:#F5F5F7;--color-texto-secundario:#A5A5B0;--color-error:#D9363E;--color-info:#4C8DFF;--text-14:.875rem;--text-16:1rem}",[0],0],[26,1,1],[21,[2,3,4],"{box-sizing:border-box;margin:0;padding:0;border:0 solid}",[0,0,0],1],[53,"html","{line-height:1.5;-webkit-text-size-adjust:100%;tab-size:4;font-family:var(--default-font-family, -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", \"Noto Sans\", Arial, sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\");font-feature-settings:var(--default-font-feature-settings, normal);font-variation-settings:var(--default-font-variation-settings, normal);-webkit-tap-highlight-color:transparent}",[0],1,["var(--default-font-family, -apple-system, blinkmacsystemfont, \"segoe ui\", roboto, \"helvetica neue\", \"noto sans\", arial, sans-serif, \"apple color emoji\", \"segoe ui emoji\", \"segoe ui symbol\", \"noto color emoji\")"]],[21,5,"{height:0;color:inherit;border-top-width:1px}",[3],1],[21,"abbr:where([title])","{-webkit-text-decoration:underline dotted;text-decoration:underline dotted}",[[0,0,["abbr"],0]],1],[21,[6,7,8,9,10,11],"{font-size:inherit;font-weight:inherit}",[3,3,3,3,3,3],1],[21,12,"{color:inherit;-webkit-text-decoration:inherit;text-decoration:inherit}",[3],1],[21,[13,14],15,[3,3],1],[53,[16,17,18,19],"{font-family:var(--default-mono-font-family, ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace);font-feature-settings:var(--default-mono-font-feature-settings, normal);font-variation-settings:var(--default-mono-font-variation-settings, normal);font-size:1em}",[3,3,3,3],1,["var(--default-mono-font-family, ui-monospace, sfmono-regular, menlo, monaco, consolas, \"liberation mono\", \"courier new\", monospace)"]],[21,20,"{font-size:80%}",[3],1],[21,[21,22],"{font-size:75%;line-height:0;position:relative;vertical-align:baseline}",[3,3],1],[21,21,"{bottom:-.25em}",[3],1],[21,22,"{top:-.5em}",[3],1],[21,23,"{text-indent:0;border-color:inherit;border-collapse:collapse}",[3],1],[21,24,"{vertical-align:baseline}",[3],1],[21,25,"{display:list-item}",[3],1],[21,[26,27,28],"{list-style:none}",[3,3,3],1],[21,[29,30,31,32,33,34,35,36],"{display:block;vertical-align:middle}",[3,3,3,3,3,3,3,3],1],[21,[29,31],"{max-width:100%;height:auto}",[3,3],1],[21,[37,38,39,40,41],"{font:inherit;font-feature-settings:inherit;font-variation-settings:inherit;letter-spacing:inherit;color:inherit;border-radius:0;background-color:transparent;opacity:1}",[3,3,3,3,3],1],[21,":where(select:is([multiple],[size])) optgroup",15,[[0,0,[40],0]],1],[21,":where(select:is([multiple],[size])) optgroup option","{padding-inline-start:20px}",[[" ",[[40,0,0,0],["option",0,0,0]]]],1],[21,41,"{resize:vertical}",[3],1],[21,[37,"input:where([type=button],[type=reset],[type=submit])"],"{appearance:button}",[3,[0,0,[38],0]],1],[21,"[hidden]:where(:not([hidden=until-found]))","{display:none!important}",[[0,0,0,["hidden"]]],1],[26,1,2],[21,".static","{position:static}",[1],2],[21,".mt-2","{margin-top:calc(var(--spacing) * 2)}",[1],2],[21,".w-full","{width:100%}",[1],2],[21,".btn-primary","{border-radius:var(--radius-lg);background-color:var(--color-primario);padding-inline:calc(var(--spacing) * 5);padding-block:calc(var(--spacing) * 2.5);font-size:var(--text-16);--tw-font-weight:var(--font-weight-medium);font-weight:var(--font-weight-medium);color:var(--color-texto);transition-property:color,background-color,border-color,outline-color,text-decoration-color,fill,stroke,--tw-gradient-from,--tw-gradient-via,--tw-gradient-to;transition-timing-function:var(--tw-ease, var(--default-transition-timing-function));transition-duration:var(--tw-duration, var(--default-transition-duration))}",[1],2],[21,".btn-primary:hover","{background-color:var(--color-primario-hover)}",[[[42],0,0,0]],3],[21,43,"{outline-style:var(--tw-outline-style);outline-width:1px}",[[[42],0,0,0]],2],[21,43,"{outline-style:var(--tw-outline-style);outline-width:2px}",[[[42],0,0,0]],2],[21,43,"{outline-offset:2px}",[[[42],0,0,0]],2],[21,43,"{outline-color:var(--color-info)}",[[[42],0,0,0]],2],[21,44,"{cursor:not-allowed}",[[[42],0,0,0]],2],[21,44,"{opacity:40%}",[[[42],0,0,0]],2],[26,1,1],[53,"body","{background-color:var(--color-fondo);font-family:var(--font-sans);color:var(--color-texto);-webkit-font-smoothing:antialiased;-moz-osx-font-smoothing:grayscale}",[0],1,["var(--font-sans)"]],[26,1,4],[21,".alert-error","{border-radius:var(--radius-md);border-left-style:var(--tw-border-style);border-left-width:4px;border-color:var(--color-error);background-color:var(--color-superficie);padding:calc(var(--spacing) * 4);color:var(--color-texto)}",[1],4],[21,".alert-error .alert-error-title","{font-size:var(--text-16);--tw-font-weight:var(--font-weight-semibold);font-weight:var(--font-weight-semibold)}",[[" ",[45,"alert-error-title"]]],4],[21,".alert-error .alert-error-message","{margin-top:var(--spacing);font-size:var(--text-14);color:var(--color-texto-secundario)}",[[" ",[45,"alert-error-message"]]],4],[21,".is-selected","{background-color:var(--color-error);color:var(--color-texto)}",[1],4],[85,".is-loading","{pointer-events:none;animation:var(--animate-pulse);opacity:60%}",[1],4,["var(--animate-pulse)"]],[10,"@property --tw-font-weight{syntax:\"*\";inherits:false;};"],[10,"@property --tw-outline-style{syntax:\"*\";inherits:false;initial-value:solid;};"],[10,"@property --tw-border-style{syntax:\"*\";inherits:false;initial-value:solid;};"],[258,"@keyframes pulse{50%{opacity:.5}}","pulse"],[26,1,5],[21,[2,4,3],"{--tw-font-weight:initial;--tw-outline-style:solid;--tw-border-style:solid}",[0,0,0],6]]]],
  locale: undefined,
  routes: [
  {
    "renderMode": 2,
    "route": "/"
  },
  {
    "renderMode": 2,
    "route": "/registro"
  },
  {
    "renderMode": 2,
    "route": "/login"
  },
  {
    "renderMode": 0,
    "preload": [
      "chunk-AJ7LUF3U.js",
      "chunk-B18FdogV.js"
    ],
    "route": "/peliculas/*"
  },
  {
    "renderMode": 0,
    "preload": [
      "chunk-CzQlAcRZ.js",
      "chunk-B18FdogV.js"
    ],
    "route": "/peliculas/*/sesiones"
  },
  {
    "renderMode": 0,
    "preload": [
      "chunk-BZdc_99t.js",
      "chunk-B18FdogV.js"
    ],
    "route": "/sesiones/*"
  }
],
  entryPointToBrowserMapping: undefined,
  assets: {
    "index.csr.html": {size: 2789, hash: "80adc22b1a0706d2", text: () => import("./assets-chunks/index_csr_html.mjs").then(m => m.default)},
    "index.server.html": {size: 954, hash: "f2dcd7b8d96b02e7", text: () => import("./assets-chunks/index_server_html.mjs").then(m => m.default)},
    "registro/index.html": {size: 18242, hash: "70ac9573b8102eff", text: () => import("./assets-chunks/registro_index_html.mjs").then(m => m.default)},
    "login/index.html": {size: 18052, hash: "511f7e26c487efc4", text: () => import("./assets-chunks/login_index_html.mjs").then(m => m.default)},
    "index.html": {size: 31829, hash: "905ccb3d7cc27fa8", text: () => import("./assets-chunks/index_html.mjs").then(m => m.default)}
  },
};
