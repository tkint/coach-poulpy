import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{nn as t,rn as n}from"./iframe-BZLf3Zvl.js";var r;function i(){return(i=e((()=>{r="The body map is a front and a back muscle chart whose areas, left and right apart, light up: what an exercise works, where it hurts, and as a heat map how much a workout works each muscle. The figures are generated from `design/body-map.json` and shared by every platform: the apps draw the same shapes, in Wider shoulders or Wider hips.\n\nAn area is named without its side (`biceps`, `lower-back`) and lit on `left`, `right` or `both`, with a heat from 1 (a little) to 3 (most); an injury is drawn in the pain colour. The web component that draws it and its props are under Web, Components, Display, Body map.\n"})))()}var a,o,s,c,l,u,d;function f(){return(f=e((()=>{n(),i(),{expect:a,within:o}=__STORYBOOK_MODULE_TEST__,s=[`shoulders`,`hips`].map(e=>({figure:e,views:[`front`,`back`].map(n=>({view:n,regions:t[e][n]}))})),c=e=>[...new Set(s[0].views.find(t=>t.view===e).regions.flatMap(e=>e.key?[e.key.replace(/-(left|right)$/,``)]:[]))].sort(),l={title:`Core/Body map`,parameters:{docs:{description:{component:r}}},render:()=>({setup:()=>({figures:s,front:c(`front`),back:c(`back`)}),template:`<div class="flex flex-col gap-8">
      <div v-for="item in figures" :key="item.figure" class="flex flex-col gap-3">
        <h3 class="cp-type-title">{{ item.figure === 'shoulders' ? 'Wider shoulders' : 'Wider hips' }}</h3>
        <div class="cp-body-map">
          <figure v-for="view in item.views" :key="view.view" class="cp-body-view">
            <svg viewBox="0 0 120 268" class="cp-body-figure" aria-hidden="true">
              <path v-for="(region, i) in view.regions" :key="i" :class="region.skin ? 'cp-body-outline' : 'cp-body-part'" :d="region.d" />
            </svg>
            <figcaption>{{ view.view === 'front' ? 'Front' : 'Back' }}</figcaption>
          </figure>
        </div>
      </div>
      <div class="flex flex-wrap gap-8">
        <p><strong>Front areas</strong><br />{{ front.join(', ') }}</p>
        <p><strong>Back areas</strong><br />{{ back.join(', ') }}</p>
      </div>
    </div>`})},u={play:async({canvasElement:e})=>{a(o(e).getByText(`Wider hips`)).toBeInTheDocument()}},d=[`Reference`],u.parameters={...u.parameters,docs:{...u.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByText('Wider hips')).toBeInTheDocument();
  }
}`,...u.parameters?.docs?.source}}}})))()}f();export{u as Reference,d as __namedExportsOrder,l as default};