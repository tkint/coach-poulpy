import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{K as t,W as n,ct as r,lt as i,t as a}from"./src-CtrKF2-p.js";var o,s;function c(){return(c=e((()=>{i(),o={name:`dumbbell`,size:24,node:[[`path`,{d:`M17.596 12.768a2 2 0 1 0 2.829-2.829l-1.768-1.767a2 2 0 0 0 2.828-2.829l-2.828-2.828a2 2 0 0 0-2.829 2.828l-1.767-1.768a2 2 0 1 0-2.829 2.829z`,key:`9m4mmf`}],[`path`,{d:`m2.5 21.5 1.4-1.4`,key:`17g3f0`}],[`path`,{d:`m20.1 3.9 1.4-1.4`,key:`1qn309`}],[`path`,{d:`M5.343 21.485a2 2 0 1 0 2.829-2.828l1.767 1.768a2 2 0 1 0 2.829-2.829l-6.364-6.364a2 2 0 1 0-2.829 2.829l1.768 1.767a2 2 0 0 0-2.828 2.829z`,key:`1t2c92`}],[`path`,{d:`m9.6 14.4 4.8-4.8`,key:`6umqxw`}]]},s=r(o)})))()}var l,u;function d(){return(d=e((()=>{i(),l={name:`menu`,size:24,node:[[`path`,{d:`M4 5h16`,key:`1tepv9`}],[`path`,{d:`M4 12h16`,key:`1lakjw`}],[`path`,{d:`M4 19h16`,key:`1djgab`}]]},u=r(l)})))()}var f,p,m,h,g,_;function v(){return(v=e((()=>{c(),d(),a(),{expect:f,userEvent:p,within:m}=__STORYBOOK_MODULE_TEST__,h={title:`Navigation/Drawer`,component:t,args:{toggleLabel:`Toggle the menu`,closeLabel:`Close the menu`},parameters:{a11y:{test:`todo`}},render:e=>({components:{CpDrawer:t,CpDrawerItem:n,Menu:u,Dumbbell:s},setup:()=>({args:e}),template:`<CpDrawer v-bind="args">
      <template #toggle-icon><Menu class="size-5" aria-hidden="true" /></template>
      <template #title>Coach Poulpy</template>
      <main class="p-4">Page content</main>
      <template #sidebar>
        <ul class="cp-menu w-full grow gap-1">
          <CpDrawerItem label="Exercises" active><template #icon><Dumbbell class="size-5" aria-hidden="true" /></template></CpDrawerItem>
        </ul>
      </template>
    </CpDrawer>`})},g={play:async({canvasElement:e})=>{f(m(e).getByLabelText(`Toggle the menu`,{selector:`input`})).toBeInTheDocument()}},_=[`Default`],g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByLabelText('Toggle the menu', {
      selector: 'input'
    })).toBeInTheDocument();
  }
}`,...g.parameters?.docs?.source}}}})))()}v();export{g as Default,_ as __namedExportsOrder,h as default};