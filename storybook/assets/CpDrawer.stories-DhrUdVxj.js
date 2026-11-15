import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{n as t,t as n}from"./dumbbell-DuqGz8h_.js";import{n as r,t as i}from"./menu-BDxql7Ke.js";import{Dn as a,Vt as o,i as s,zt as c}from"./iframe-BSV0U_UY.js";var l;function u(){return(u=e((()=>{l="`CpDrawer`: the admin's page frame. A full-height sidebar from a large screen up, folded to icons by the button in the top bar; on a smaller screen it slides over the page. It fills the browser window, so it has no live sample here: see it in [Web > Screens > Admin > Audit log](https://tkint.github.io/coach-poulpy/storybook/?path=/docs/web-screens-admin-audit-log--docs).\n\n| Vue | Classes |\n| --- | --- |\n| `<CpDrawer>` | `cp-drawer lg:cp-drawer-open` with `input.cp-drawer-toggle`, `div.cp-drawer-content` (a `cp-navbar` and the page) and `div.cp-drawer-side` with `label.cp-drawer-overlay` |\n| `<CpDrawerItem label>` | `li` and a `button` (an `a` when it has `href`, a link to another page) with `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` and `data-tip`; `cp-menu-active` when current |\n| In the sidebar | `cp-is-drawer-close:hidden` hides a label when folded, `cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right` with `data-tip` names the icon. The variants carry the `cp-` prefix. |\n"})))()}var d,f,p,m,h,g;function _(){return(_=e((()=>{t(),r(),s(),u(),{expect:d,userEvent:f,within:p}=__STORYBOOK_MODULE_TEST__,m={title:`Web/Components/Navigation/Drawer`,component:o,args:{toggleLabel:`Toggle the menu`,closeLabel:`Close the menu`},parameters:{docs:{description:{component:l}},a11y:{test:`todo`}},render:e=>({components:{CpDrawer:o,CpDrawerItem:c,CpIcon:a},setup:()=>({args:e,Menu:i,Dumbbell:n}),template:`<CpDrawer v-bind="args">
      <template #toggle-icon><CpIcon :icon="Menu" /></template>
      <template #title>Coach Poulpy</template>
      <main class="p-4">Page content</main>
      <template #sidebar>
        <ul class="cp-menu w-full grow gap-1">
          <CpDrawerItem label="Exercises" active><template #icon><CpIcon :icon="Dumbbell" /></template></CpDrawerItem>
        </ul>
      </template>
    </CpDrawer>`})},h={play:async({canvasElement:e})=>{d(p(e).getByLabelText(`Toggle the menu`,{selector:`input`})).toBeInTheDocument()}},g=[`Default`],h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByLabelText('Toggle the menu', {
      selector: 'input'
    })).toBeInTheDocument();
  }
}`,...h.parameters?.docs?.source}}}})))()}_();export{h as Default,g as __namedExportsOrder,m as default};