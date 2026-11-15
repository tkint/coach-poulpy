import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{J as t,X as n,t as r}from"./src-CYMcdRI4.js";import{n as i,t as a}from"./createLucideIcon-DXHbsEqp.js";var o,s;function c(){return(c=e((()=>{i(),o={name:`dumbbell`,size:24,node:[[`path`,{d:`M17.596 12.768a2 2 0 1 0 2.829-2.829l-1.768-1.767a2 2 0 0 0 2.828-2.829l-2.828-2.828a2 2 0 0 0-2.829 2.828l-1.767-1.768a2 2 0 1 0-2.829 2.829z`,key:`9m4mmf`}],[`path`,{d:`m2.5 21.5 1.4-1.4`,key:`17g3f0`}],[`path`,{d:`m20.1 3.9 1.4-1.4`,key:`1qn309`}],[`path`,{d:`M5.343 21.485a2 2 0 1 0 2.829-2.828l1.767 1.768a2 2 0 1 0 2.829-2.829l-6.364-6.364a2 2 0 1 0-2.829 2.829l1.768 1.767a2 2 0 0 0-2.828 2.829z`,key:`1t2c92`}],[`path`,{d:`m9.6 14.4 4.8-4.8`,key:`6umqxw`}]]},s=a(o)})))()}var l,u;function d(){return(d=e((()=>{i(),l={name:`menu`,size:24,node:[[`path`,{d:`M4 5h16`,key:`1tepv9`}],[`path`,{d:`M4 12h16`,key:`1lakjw`}],[`path`,{d:`M4 19h16`,key:`1djgab`}]]},u=a(l)})))()}var f;function p(){return(p=e((()=>{f="`CpDrawer`: the admin's page frame. A full-height sidebar from a large screen up, folded to icons by the button in the top bar; on a smaller screen it slides over the page. It fills the browser window, so it has no live sample here: see it in the [Admin · Audit mockup](https://tkint.github.io/coach-poulpy/mockups/web/agreed/admin-audit).\n\n| Vue | Classes |\n| --- | --- |\n| `<CpDrawer>` | `cp-drawer lg:cp-drawer-open` with `input.cp-drawer-toggle`, `div.cp-drawer-content` (a `cp-navbar` and the page) and `div.cp-drawer-side` with `label.cp-drawer-overlay` |\n| `<CpDrawerItem label>` | `li` and a `button` (an `a` when it has `href`, a link to another page) with `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` and `data-tip`; `cp-menu-active` when current |\n| In the sidebar | `cp-is-drawer-close:hidden` hides a label when folded, `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` with `data-tip` names the icon. The variants carry the `cp-` prefix. |\n"})))()}var m,h,g,_,v,y;function b(){return(b=e((()=>{c(),d(),r(),p(),{expect:m,userEvent:h,within:g}=__STORYBOOK_MODULE_TEST__,_={title:`Web/Components/Navigation/Drawer`,component:n,args:{toggleLabel:`Toggle the menu`,closeLabel:`Close the menu`},parameters:{docs:{description:{component:f}},a11y:{test:`todo`}},render:e=>({components:{CpDrawer:n,CpDrawerItem:t,Menu:u,Dumbbell:s},setup:()=>({args:e}),template:`<CpDrawer v-bind="args">
      <template #toggle-icon><Menu class="size-5" aria-hidden="true" /></template>
      <template #title>Coach Poulpy</template>
      <main class="p-4">Page content</main>
      <template #sidebar>
        <ul class="cp-menu w-full grow gap-1">
          <CpDrawerItem label="Exercises" active><template #icon><Dumbbell class="size-5" aria-hidden="true" /></template></CpDrawerItem>
        </ul>
      </template>
    </CpDrawer>`})},v={play:async({canvasElement:e})=>{m(g(e).getByLabelText(`Toggle the menu`,{selector:`input`})).toBeInTheDocument()}},y=[`Default`],v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByLabelText('Toggle the menu', {
      selector: 'input'
    })).toBeInTheDocument();
  }
}`,...v.parameters?.docs?.source}}}})))()}b();export{v as Default,y as __namedExportsOrder,_ as default};