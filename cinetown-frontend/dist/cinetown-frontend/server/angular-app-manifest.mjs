
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  baseHref: "/",
  criticalCssPlans: [[1,"/styles-5INURTSO.css",0,[],[],[]]],
  locale: undefined,
  routes: [
  {
    "renderMode": 2,
    "route": "/"
  }
],
  entryPointToBrowserMapping: undefined,
  assets: {
    "index.csr.html": {size: 441, hash: "604aa2a51747b183", text: () => import("./assets-chunks/index_csr_html.mjs").then(m => m.default)},
    "index.server.html": {size: 954, hash: "2cb32fa29ab3a2ee", text: () => import("./assets-chunks/index_server_html.mjs").then(m => m.default)},
    "index.html": {size: 883, hash: "b9712ce158ae1e86", text: () => import("./assets-chunks/index_html.mjs").then(m => m.default)}
  },
};
