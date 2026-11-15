import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{J as t,X as n,t as r}from"./src-DX2fDrfP.js";import{i,n as a,r as o,t as s}from"./menu-DnuSM7Lr.js";var c;function l(){return(l=e((()=>{c="`CpDrawer`: the admin's page frame. A full-height sidebar from a large screen up, folded to icons by the button in the top bar; on a smaller screen it slides over the page. It fills the browser window, so it has no live sample here: see it in the [Admin · Audit mockup](https://tkint.github.io/coach-poulpy/mockups/web/agreed/admin-audit).\n\n| Vue | Classes |\n| --- | --- |\n| `<CpDrawer>` | `cp-drawer lg:cp-drawer-open` with `input.cp-drawer-toggle`, `div.cp-drawer-content` (a `cp-navbar` and the page) and `div.cp-drawer-side` with `label.cp-drawer-overlay` |\n| `<CpDrawerItem label>` | `li` and a `button` (an `a` when it has `href`, a link to another page) with `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` and `data-tip`; `cp-menu-active` when current |\n| In the sidebar | `cp-is-drawer-close:hidden` hides a label when folded, `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` with `data-tip` names the icon. The variants carry the `cp-` prefix. |\n"})))()}var u,d,f,p,m,h;function g(){return(g=e((()=>{i(),a(),r(),l(),{expect:u,userEvent:d,within:f}=__STORYBOOK_MODULE_TEST__,p={title:`Web/Components/Navigation/Drawer`,component:n,args:{toggleLabel:`Toggle the menu`,closeLabel:`Close the menu`},parameters:{docs:{description:{component:c}},a11y:{test:`todo`}},render:e=>({components:{CpDrawer:n,CpDrawerItem:t,Menu:s,Dumbbell:o},setup:()=>({args:e}),template:`<CpDrawer v-bind="args">
      <template #toggle-icon><Menu class="size-5" aria-hidden="true" /></template>
      <template #title>Coach Poulpy</template>
      <main class="p-4">Page content</main>
      <template #sidebar>
        <ul class="cp-menu w-full grow gap-1">
          <CpDrawerItem label="Exercises" active><template #icon><Dumbbell class="size-5" aria-hidden="true" /></template></CpDrawerItem>
        </ul>
      </template>
    </CpDrawer>`})},m={play:async({canvasElement:e})=>{u(f(e).getByLabelText(`Toggle the menu`,{selector:`input`})).toBeInTheDocument()}},h=[`Default`],m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByLabelText('Toggle the menu', {
      selector: 'input'
    })).toBeInTheDocument();
  }
}`,...m.parameters?.docs?.source}}}})))()}g();export{m as Default,h as __namedExportsOrder,p as default};