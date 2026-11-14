import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{U as t,a as n}from"./iframe-Bgdnb2bH.js";import{ct as r,t as i}from"./src-Cc0VKo53.js";var a;function o(){return(o=e((()=>{a="`CpBodyMap`: the full body map, the front and back figure drawn from the apps' shapes (`design/body-map.json`), with the areas worked lit and the areas that hurt in the pain colour, and an optional Less to More legend.\n\n## Props\n\n| Prop | Takes | Says |\n| --- | --- | --- |\n| `front`, `back` | areas | what is lit on that side of the figure |\n| `front-injury`, `back-injury` | areas | what hurts on that side, in the pain colour |\n| `captions` | `{ front, back }` | the text under each figure |\n| `description` | text | the picture in words, for a screen reader |\n| `figure` | `shoulders` (default) or `hips` | the body figure |\n| `layout` | `both` (default), `front` or `back` | which figures are drawn |\n| `legend` | `hidden` (default), `top` or `right` | where the Less to More scale goes |\n| `less`, `more` | text | the words of the legend |\n| `interactive`, `area-label` | flag, `(area) => text` | an area `area-label` names is a button; `pick` says its `area` and `side` |\n\n## Areas\n\nThe four area props take the same object: an area by its name (`biceps`, `lower-back`, `quads`...), as the catalog and the API name it, and its value is a side or a side with a number.\n\n```ts\n{ biceps: 'left', pectoral: 'both', deltoid: { side: 'right', heat: 2 } }\n```\n\n- A side is `left`, `right` or `both`, the person's own left and right.\n- `heat` runs from 1 (a little) to 3 (most) and is `3` when left out. On an injury it says how much it hurts.\n- An area the figure does not have on that side is ignored, so `front` and `back` can be given the same object.\n- Where a muscle and an injury meet, the injury is drawn.\n\n| Vue | Classes |\n| --- | --- |\n| `<CpBodyMap>` | `div.cp-body-map` (`role=\"img\"`) around a `figure.cp-body-view` per side, each with an `svg.cp-body-figure` of `path.cp-body-part` (`data-heat` 1 to 3, `data-pain` 1 to 3) and `path.cp-body-outline`, and `p.cp-heat-legend` (only when `legend` is `top` or `right`) |\n| `<CpBodyMap interactive>` | `role=\"group\"`; a named area is a `path.cp-body-part.cp-body-part-pick[role=button]` (`aria-pressed` says whether it is lit) and says its area and side with `pick`, with the legend `hidden` or the screen's own |\n"})))()}var s,c,l,u,d,f,p,m,h,g;function _(){return(_=e((()=>{n(),i(),o(),{expect:s,userEvent:c,within:l}=__STORYBOOK_MODULE_TEST__,u={title:`Core/Body map`,parameters:{docs:{description:{component:a}}},component:r,args:{front:{pectoral:`both`,biceps:{side:`left`,heat:2},abs:{side:`both`,heat:1}},back:{deltoid:`both`,triceps:{side:`both`,heat:2}},captions:{front:`Front`,back:`Back`},description:`Chest and shoulders worked most`,legend:`top`,less:`Less`,more:`More`}},d={play:async({canvasElement:e})=>{s(l(e).getByRole(`img`)).toHaveAccessibleName(`Chest and shoulders worked most`)}},f={args:{front:void 0,back:void 0,frontInjury:{deltoid:{side:`right`,heat:3},biceps:`right`},backInjury:{deltoid:{side:`right`,heat:3},"lower-back":{side:`both`,heat:1}},legend:`hidden`,description:`Right shoulder hurts`}},p={args:{legend:`right`,layout:`front`}},m={args:{figure:`hips`}},h={args:{interactive:!0,front:void 0,back:void 0,legend:`hidden`},render:e=>({components:{CpBodyMap:r},setup:()=>{let n=t({biceps:`left`}),r=e=>e===`biceps`?`Biceps`:e===`quads`?`Quadriceps`:void 0,i=e=>n.value[e]===`both`?[`left`,`right`]:n.value[e]?[n.value[e]]:[];return{args:e,lit:n,areaLabel:r,pick:(e,t)=>{let r=i(e),a=r.includes(t)?r.filter(e=>e!==t):[...r,t],{[e]:o,...s}=n.value;n.value=a.length===2?{...s,[e]:`both`}:a.length?{...s,[e]:a[0]}:s}}},template:`<CpBodyMap v-bind="args" :front="lit" :area-label="areaLabel" @pick="pick" />`}),play:async({canvasElement:e})=>{let t=l(e).getAllByRole(`button`,{name:`Biceps`}),n=()=>t.filter(e=>e.getAttribute(`aria-pressed`)===`true`).length;s(t).toHaveLength(2),s(n()).toBe(1),await c.click(t.find(e=>e.getAttribute(`aria-pressed`)===`false`)),s(n()).toBe(2)}},g=[`Default`,`Injury`,`LegendRight`,`Hips`,`Interactive`],d.parameters={...d.parameters,docs:{...d.parameters?.docs,source:{originalSource:`{
  play: async ({
    canvasElement
  }) => {
    expect(within(canvasElement).getByRole('img')).toHaveAccessibleName('Chest and shoulders worked most');
  }
}`,...d.parameters?.docs?.source},description:{story:`An area is lit by its name; a side string lights that side at the most, an object says how much.`,...d.parameters?.docs?.description}}},f.parameters={...f.parameters,docs:{...f.parameters?.docs,source:{originalSource:`{
  args: {
    front: undefined,
    back: undefined,
    frontInjury: {
      deltoid: {
        side: 'right',
        heat: 3
      },
      biceps: 'right'
    },
    backInjury: {
      deltoid: {
        side: 'right',
        heat: 3
      },
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
    interactive: true,
    front: undefined,
    back: undefined,
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
      const areaLabel = (area: string) => area === 'biceps' ? 'Biceps' : area === 'quads' ? 'Quadriceps' : undefined;
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
        areaLabel,
        pick
      };
    },
    template: '<CpBodyMap v-bind="args" :front="lit" :area-label="areaLabel" @pick="pick" />'
  }),
  play: async ({
    canvasElement
  }) => {
    const biceps = within(canvasElement).getAllByRole('button', {
      name: 'Biceps'
    });
    const pressed = () => biceps.filter(one => one.getAttribute('aria-pressed') === 'true').length;
    expect(biceps).toHaveLength(2);
    expect(pressed()).toBe(1);
    await userEvent.click(biceps.find(one => one.getAttribute('aria-pressed') === 'false')!);
    expect(pressed()).toBe(2);
  }
}`,...h.parameters?.docs?.source},description:{story:"The picture is a field: an area `areaLabel` names is a button that says its area and side when it is pressed.",...h.parameters?.docs?.description}}}})))()}_();export{d as Default,m as Hips,f as Injury,h as Interactive,p as LegendRight,g as __namedExportsOrder,u as default};