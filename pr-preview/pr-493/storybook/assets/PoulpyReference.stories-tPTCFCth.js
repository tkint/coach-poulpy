import{n as e,r as t}from"./rolldown-runtime-sJBDdmOt.js";import{H as n,i as r}from"./iframe-qPPljojb.js";var i;function a(){return(a=e((()=>{i="Poulpy is a friendly, reversible plush octopus: blue and smiling by default, with a playful grumpy mood on his ember side. The side is the status and the shade is the feeling. His moods and poses are pictures painted from the 3D model by `design/build-poulpy-pictures.mjs` into `design/poulpy-3d/pictures/`, and so is the loader, Poulpy from above; they are transparent so they sit on a light or a dark screen. The website and both apps use these pictures.\n\nA **mood** is Poulpy's face and shade for what just happened (Proud, Happy, Recovering are blue; Annoyed, Grumpy, Fuming are ember). A **pose** shows an activity and keeps whatever mood he is in. Pain and rehab always show the blue side; Poulpy is never guilt. The rules for when each drawing shows are in the Brand book; the web component that draws them is under Web, Components, Display, Poulpy.\n"})))()}var o=t({Reference:()=>f,__namedExportsOrder:()=>p,default:()=>d}),s,c,l,u,d,f,p;function m(){return(m=e((()=>{a(),r(),{expect:s,within:c}=__STORYBOOK_MODULE_TEST__,l=Object.entries(n).map(([e,t])=>({key:e,src:t,name:e.replace(/^(mood|pose)_/,``).replace(/_/g,` `).replace(/^./,e=>e.toUpperCase())})),u=[{heading:`Moods`,items:l.filter(e=>e.key.startsWith(`mood_`))},{heading:`Poses`,items:l.filter(e=>e.key.startsWith(`pose_`))},{heading:`Loader`,items:l.filter(e=>e.key.startsWith(`loader`))}],d={title:`Core/Poulpy`,parameters:{docs:{description:{component:i}}},render:()=>({setup:()=>({groups:u}),template:`<div class="flex flex-col gap-8">
      <section v-for="group in groups" :key="group.heading" class="flex flex-col gap-3">
        <h3 class="cp-type-title">{{ group.heading }}</h3>
        <div class="flex flex-wrap gap-6">
          <figure v-for="item in group.items" :key="item.key" class="flex w-32 flex-col items-center gap-2 text-center">
            <img :src="item.src" :alt="item.name" class="h-24 w-24 object-contain" />
            <figcaption><strong>{{ item.name }}</strong><br /><code>{{ item.key }}</code></figcaption>
          </figure>
        </div>
      </section>
    </div>`})},f={play:async({canvasElement:e})=>{s(c(e).getByAltText(`Happy`)).toBeInTheDocument()}},p=[`Reference`],f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByAltText('Happy')).toBeInTheDocument();
  }
}`,...f.parameters?.docs?.source}}}})))()}export{f as n,m as r,o as t};