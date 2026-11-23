import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{H as t,t as n}from"./vue.esm-bundler-CexDUOsq.js";import{en as r,n as i}from"./iframe-DynmAsze.js";var a;function o(){return(o=e((()=>{a="`CpBodyMap`: the full web body map, the front and back figure drawn from the apps' shapes (`design/body-map.json`; the figures and area names are under Core / Body map), with the areas worked lit (the captions and the legend say their own words in the page's language) and the areas that hurt in the pain colour, and an optional Less to More legend.\n\n## Props\n\n| Prop | Takes | Says |\n| --- | --- | --- |\n| `heat`, `v-model:heat` | areas | what is lit; `v-model:heat` in a form |\n| `injury`, `v-model:injury` | areas | what hurts, in the pain colour; `v-model:injury` in a form |\n| `editing` | `heat`, `injury` or false (default) | which of the two a pick updates; left out or false, the map is read-only |\n| `description` | text | the picture in words, for a screen reader |\n| `figure` | `shoulders` (default) or `hips` | the body figure |\n| `layout` | `both` (default), `front` or `back` | which figures are drawn |\n| `legend` | `hidden` (default), `top` or `right` | where the Less to More scale goes |\n| `interactive-mode` | `individual` (default) or `both` | with `editing` and `v-model:heat` or `v-model:injury`: a tap toggles the side tapped, or both sides of the area |\n| `selectable` | areas | with `editing`, every area is a button, or only these; every area is named \"Left shoulder\" and the like from the shared words, and has that name as its title, editable or not; `pick` says its `area` and `side` |\n\n## Areas\n\n`heat` and `injury` take the same object: an area by its name (`biceps`, `lower-back`, `quads`...), as the catalog and the API name it, and its value is a side or a side with a number.\n\n```ts\n{ biceps: 'left', pectoral: 'both', deltoid: { side: 'right', heat: 2 } }\n```\n\n- A side is `left`, `right` or `both`, the person's own left and right.\n- `heat` runs from 1 (a little) to 3 (most) and is `3` when left out. On an injury it says how much it hurts.\n- An area is drawn on the figure that has it, so there is one object for both the front and the back.\n- Where a muscle and an injury meet, the injury is drawn.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpBodyMap>` | `div.cp-body-map` (`role=\"img\"`) around a `figure.cp-body-view` per side, each with an `svg.cp-body-figure` of `path.cp-body-part` (`data-heat` 1 to 3, `data-pain` 1 to 3) and `path.cp-body-outline`, and `p.cp-heat-legend` (only when `legend` is `top` or `right`) |\n| `<CpBodyMap editing=\"heat\">` | `role=\"group\"`; a named area is a `path.cp-body-part.cp-body-part-pick[role=button]` (`aria-pressed` says whether it is lit) and says its area and side with `pick`, with the legend `hidden` or the screen's own |\n"})))()}var s,c,l,u,d,f,p,m,h,g,_,v,y;function b(){return(b=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Web/Components/Display/Body map`,parameters:{docs:{description:{component:a}}},component:r,args:{heat:{pectoral:`both`,biceps:{side:`left`,heat:2},abs:{side:`both`,heat:1},deltoid:`both`,triceps:{side:`both`,heat:2}},description:`Chest and shoulders worked most`,legend:`top`}},d={play:async({canvasElement:e})=>{s(l(e).getByRole(`img`)).toHaveAccessibleName(`Chest and shoulders worked most`),s(e.querySelectorAll(`.cp-body-part title`).length).toBeGreaterThan(0)}},f={args:{heat:void 0,injury:{deltoid:{side:`right`,heat:3},biceps:`right`,"lower-back":{side:`both`,heat:1}},legend:`hidden`,description:`Right shoulder hurts`}},p={args:{legend:`right`,layout:`front`}},m={args:{figure:`hips`}},h={args:{heat:void 0,legend:`hidden`,editing:`heat`},render:e=>({components:{CpBodyMap:r},setup:()=>({args:e,areas:t({biceps:`left`})}),template:`<CpBodyMap v-bind="args" v-model:heat="areas" /><p data-testid="areas">{{ JSON.stringify(areas) }}</p>`}),play:async({canvasElement:e})=>{let t=l(e);await c.click(t.getAllByRole(`button`,{name:/biceps/i}).find(e=>e.getAttribute(`aria-pressed`)===`false`)),s(t.getByTestId(`areas`)).toHaveTextContent(`{"biceps":"both"}`)}},g={...h,args:{...h.args,interactiveMode:`both`},play:async({canvasElement:e})=>{let t=l(e);await c.click(t.getAllByRole(`button`,{name:/biceps/i})[0]),s(t.getByTestId(`areas`)).toHaveTextContent(`{"biceps":"both"}`),await c.click(t.getAllByRole(`button`,{name:/biceps/i})[0]),s(t.getByTestId(`areas`)).toHaveTextContent(`{}`)}},_={args:{heat:void 0,legend:`hidden`,editing:`injury`,description:`Where it hurts`},render:e=>({components:{CpBodyMap:r},setup:()=>({args:e,hurt:t({})}),template:`<CpBodyMap v-bind="args" v-model:injury="hurt" /><p data-testid="hurt">{{ JSON.stringify(hurt) }}</p>`}),play:async({canvasElement:e})=>{let t=l(e);await c.click(t.getAllByRole(`button`,{name:/shoulder/i})[0]),s(t.getByTestId(`hurt`)).toHaveTextContent(/"deltoid":"(left|right)"/)}},v={args:{editing:`heat`,heat:void 0,legend:`hidden`},render:e=>({components:{CpBodyMap:r},setup:()=>{let n=t({biceps:`left`}),r=e=>n.value[e]===`both`?[`left`,`right`]:n.value[e]?[n.value[e]]:[];return{args:e,lit:n,pick:(e,t)=>{let i=r(e),a=i.includes(t)?i.filter(e=>e!==t):[...i,t],{[e]:o,...s}=n.value;n.value=a.length===2?{...s,[e]:`both`}:a.length?{...s,[e]:a[0]}:s}}},template:`<CpBodyMap v-bind="args" :heat="lit" @pick="pick" />`}),play:async({canvasElement:e})=>{let t=l(e).getAllByRole(`button`,{name:/biceps/i}),n=()=>t.filter(e=>e.getAttribute(`aria-pressed`)===`true`).length;s(t).toHaveLength(2),s(n()).toBe(1),await c.click(t.find(e=>e.getAttribute(`aria-pressed`)===`false`)),s(n()).toBe(2)}},y=[`Default`,`Injury`,`LegendRight`,`Hips`,`FormModel`,`BothSides`,`InjuryModel`,`Interactive`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('img')).toHaveAccessibleName('Chest and shoulders worked most');
    expect(canvasElement.querySelectorAll('.cp-body-part title').length).toBeGreaterThan(0);
  }
}`,...d.parameters?.docs?.source},description:{story:`An area is lit by its name; a side string lights that side at the most, an object says how much.`,...d.parameters?.docs?.description}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    heat: undefined,
    injury: {
      deltoid: {
        side: 'right',
        heat: 3
      },
      biceps: 'right',
      'lower-back': {
        side: 'both',
        heat: 1
      }
    },
    legend: 'hidden',
    description: 'Right shoulder hurts'
  }
}`,...f.parameters?.docs?.source},description:{story:"An injury takes the same object as the muscles worked and is drawn in the pain colour, its `heat` how much it hurts.",...f.parameters?.docs?.description}}},p.parameters={...p.parameters,docs:{...p.parameters?.docs,source:{originalSource:`{
  args: {
    legend: 'right',
    layout: 'front'
  }
}`,...p.parameters?.docs?.source},description:{story:`The legend on the right, one side of the figure only.`,...p.parameters?.docs?.description}}},m.parameters={...m.parameters,docs:{...m.parameters?.docs,source:{originalSource:`{
  args: {
    figure: 'hips'
  }
}`,...m.parameters?.docs?.source},description:{story:`The other body figure.`,...m.parameters?.docs?.description}}},h.parameters={...h.parameters,docs:{...h.parameters?.docs,source:{originalSource:`{
  args: {
    heat: undefined,
    legend: 'hidden',
    editing: 'heat'
  },
  render: args => ({
    components: {
      CpBodyMap
    },
    setup: () => ({
      args,
      areas: ref<BodyAreas>({
        biceps: 'left'
      })
    }),
    template: '<CpBodyMap v-bind="args" v-model:heat="areas" /><p data-testid="areas">{{ JSON.stringify(areas) }}</p>'
  }),
  play: async ({
    canvasElement
  }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getAllByRole('button', {
      name: /biceps/i
    }).find(one => one.getAttribute('aria-pressed') === 'false')!);
    expect(canvas.getByTestId('areas')).toHaveTextContent('{"biceps":"both"}');
  }
}`,...h.parameters?.docs?.source},description:{story:"In a form, `v-model:heat` holds the areas: picking a side toggles it, and both sides make `both`.",...h.parameters?.docs?.description}}},g.parameters={...g.parameters,docs:{...g.parameters?.docs,source:{originalSource:`{
  ...FormModel,
  args: {
    ...FormModel.args,
    interactiveMode: 'both'
  },
  play: async ({
    canvasElement
  }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getAllByRole('button', {
      name: /biceps/i
    })[0]);
    expect(canvas.getByTestId('areas')).toHaveTextContent('{"biceps":"both"}');
    await userEvent.click(canvas.getAllByRole('button', {
      name: /biceps/i
    })[0]);
    expect(canvas.getByTestId('areas')).toHaveTextContent('{}');
  }
}`,...g.parameters?.docs?.source},description:{story:'`interactive-mode="both"`: a tap on either side selects both sides of the area.',...g.parameters?.docs?.description}}},_.parameters={..._.parameters,docs:{..._.parameters?.docs,source:{originalSource:`{
  args: {
    heat: undefined,
    legend: 'hidden',
    editing: 'injury',
    description: 'Where it hurts'
  },
  render: args => ({
    components: {
      CpBodyMap
    },
    setup: () => ({
      args,
      hurt: ref<BodyAreas>({})
    }),
    template: '<CpBodyMap v-bind="args" v-model:injury="hurt" /><p data-testid="hurt">{{ JSON.stringify(hurt) }}</p>'
  }),
  play: async ({
    canvasElement
  }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getAllByRole('button', {
      name: /shoulder/i
    })[0]);
    expect(canvas.getByTestId('hurt')).toHaveTextContent(/"deltoid":"(left|right)"/);
  }
}`,..._.parameters?.docs?.source},description:{story:'`v-model:injury` with `editing="injury"`: a pick edits what hurts, and the muscles worked stay as they are.',..._.parameters?.docs?.description}}},v.parameters={...v.parameters,docs:{...v.parameters?.docs,source:{originalSource:`{
  args: {
    editing: 'heat',
    heat: undefined,
    legend: 'hidden'
  },
  render: args => ({
    components: {
      CpBodyMap
    },
    setup: () => {
      const lit = ref<BodyAreas>({
        biceps: 'left'
      });
      const sides = (area: string) => lit.value[area] === 'both' ? ['left', 'right'] : lit.value[area] ? [lit.value[area]] : [];
      const pick = (area: string, side: 'left' | 'right') => {
        const now = sides(area);
        const next = now.includes(side) ? now.filter(one => one !== side) : [...now, side];
        const {
          [area]: _,
          ...rest
        } = lit.value;
        lit.value = next.length === 2 ? {
          ...rest,
          [area]: 'both'
        } : next.length ? {
          ...rest,
          [area]: next[0] as 'left' | 'right'
        } : rest;
      };
      return {
        args,
        lit,
        pick
      };
    },
    template: '<CpBodyMap v-bind="args" :heat="lit" @pick="pick" />'
  }),
  play: async ({
    canvasElement
  }) => {
    const biceps = within(canvasElement).getAllByRole('button', {
      name: /biceps/i
    });
    const pressed = () => biceps.filter(one => one.getAttribute('aria-pressed') === 'true').length;
    expect(biceps).toHaveLength(2);
    expect(pressed()).toBe(1);
    await userEvent.click(biceps.find(one => one.getAttribute('aria-pressed') === 'false')!);
    expect(pressed()).toBe(2);
  }
}`,...v.parameters?.docs?.source},description:{story:"The picture is a field: every area is a button (`selectable` narrows which) that says its area and side when it is pressed.",...v.parameters?.docs?.description}}}})))()}b();export{g as BothSides,d as Default,h as FormModel,m as Hips,f as Injury,_ as InjuryModel,v as Interactive,p as LegendRight,y as __namedExportsOrder,u as default};