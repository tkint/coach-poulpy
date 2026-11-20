import{n as e}from"./rolldown-runtime-sJBDdmOt.js";import{A as t,H as n,J as r,K as ee,L as i,W as a,b as o,f as s,h as c,k as l,l as te,m as u,p as d,t as f,u as ne,v as p,y as m}from"./vue.esm-bundler-CexDUOsq.js";import{a as re,i as h,n as g,o as _,r as ie,t as ae}from"./circle-check-C1KuRArJ.js";import{n as v,t as oe}from"./chevrons-up-down-B6orTOhl.js";import{n as y,t as se}from"./rotate-ccw-Bv7sQS_2.js";import{n as b,t as ce}from"./trash-L_UPV9SV.js";import{Cn as x,D as S,En as C,F as le,Jt as ue,On as de,Sn as fe,T as pe,Xt as w,a as me,d as he,n as T,on as E,s as ge,x as D,y as _e}from"./iframe-CFDVK3oo.js";import{n as O,t as ve}from"./AdminShell-VRdt-0ae.js";var k,A,j,M,N,P,F,I,L,R,z,ye,be,xe,Se,Ce,we,Te,B;function Ee(){return(Ee=e((()=>{f(),T(),_(),x(),g(),v(),h(),y(),b(),O(),k={class:`flex min-h-full`},A={class:`flex min-w-0 grow flex-col gap-6 p-4 md:p-8`},j={class:`cp-card-body grid gap-4 p-4 sm:grid-cols-2 lg:grid-cols-4 md:p-6`},M=[`aria-sort`],N={type:`button`,class:`cp-btn cp-btn-ghost cp-btn-sm -ms-3 gap-1 whitespace-nowrap`},P=[`aria-selected`],F={class:`flex items-center gap-3`},I={class:`whitespace-nowrap`},L={class:`flex flex-col items-center justify-between gap-3 sm:flex-row`},R={class:`text-sm text-base-content/70`},z={class:`grid gap-3`},ye={key:1,class:`text-sm text-base-content/70`},be={key:2,class:`text-sm text-base-content/70`},xe={class:`flex flex-wrap gap-2`},Se={class:`py-2`},Ce={class:`break-all`},we={class:`py-2`},Te={class:`break-all`},B=o({__name:`AdminAccountsScreen`,props:{accounts:{},showing:{},pages:{default:3},signIn:{default:``},search:{default:``},sort:{default:`created`},order:{default:`desc`},selected:{},disabledSince:{},deleting:{type:Boolean},confirming:{type:Boolean},justConfirmed:{type:Boolean},typed:{default:``}},setup(e){let o=e,f=n(o.search),h=n(o.signIn),g=n(``),_=n(``),v=n(o.typed),y=n(1),b=n(!0),x=()=>o.accounts.find(e=>e.email===o.selected),T=[{key:`email`,label:`Email`},{key:`role`,label:`Role`},{key:`status`,label:`Email status`},{key:`signin`,label:`Sign-in`},{key:`created`,label:`Created`}],O=e=>e===o.sort?o.order===`asc`?`ascending`:`descending`:`none`;return(n,o)=>(l(),d(ve,{title:`Accounts`,active:`accounts`},{default:i(()=>[s(`main`,k,[s(`div`,A,[o[11]||=s(`div`,{class:`flex flex-col gap-1`},[s(`h1`,{class:`cp-type-display`},`Accounts`),s(`p`,{class:`text-base-content/70`},`Every account, with its state and never its workouts or health data. Open one to disable or delete it.`)],-1),s(`form`,{class:`cp-card bg-base-100 shadow-sm`,role:`search`,"aria-label":`Filter the accounts`,onSubmit:o[4]||=te(()=>{},[`prevent`])},[s(`div`,j,[m(a(me),{modelValue:f.value,"onUpdate:modelValue":o[0]||=e=>f.value=e,class:`sm:col-span-2 lg:col-span-1`,type:`search`,label:`Email contains`,autocomplete:`off`},null,8,[`modelValue`]),m(a(S),{modelValue:g.value,"onUpdate:modelValue":o[1]||=e=>g.value=e,label:`Role`,options:[{value:``,text:`All`},{value:`member`,text:`Member`},{value:`admin`,text:`Admin`}]},null,8,[`modelValue`]),m(a(S),{modelValue:_.value,"onUpdate:modelValue":o[2]||=e=>_.value=e,label:`Email`,options:[{value:``,text:`All`},{value:`yes`,text:`Confirmed`},{value:`no`,text:`Not confirmed`}]},null,8,[`modelValue`]),m(a(S),{modelValue:h.value,"onUpdate:modelValue":o[3]||=e=>h.value=e,label:`Sign-in`,options:[{value:``,text:`All`},{value:`active`,text:`Active`},{value:`disabled`,text:`Disabled`}]},null,8,[`modelValue`])])],32),e.accounts.length?(l(),d(a(ue),{key:1},{default:i(()=>[m(a(ge),null,{default:i(()=>[s(`thead`,null,[s(`tr`,null,[(l(),c(ne,null,t(T,t=>s(`th`,{key:t.key,"aria-sort":O(t.key)},[s(`button`,N,[p(r(t.label)+` `,1),t.key===e.sort?e.order===`asc`?(l(),d(a(C),{key:1,icon:a(ie),size:`sm`},null,8,[`icon`])):(l(),d(a(C),{key:2,icon:a(fe),size:`sm`},null,8,[`icon`])):(l(),d(a(C),{key:0,icon:a(oe),size:`sm`},null,8,[`icon`]))])],8,M)),64))])]),s(`tbody`,null,[(l(!0),c(ne,null,t(e.accounts,t=>(l(),c(`tr`,{key:t.email,class:ee([`cp-hover`,{"bg-base-200":t.email===e.selected}]),"aria-selected":t.email===e.selected?`true`:void 0},[s(`td`,null,[s(`div`,F,[m(a(de),{initials:t.email.charAt(0).toUpperCase()},null,8,[`initials`]),s(`a`,{href:`#`,class:`cp-link cp-link-hover whitespace-nowrap text-start font-semibold`,onClick:o[5]||=te(()=>{},[`prevent`])},r(t.email),1)])]),s(`td`,null,[m(a(E),{tone:t.role===`Admin`?`primary`:`neutral`},{default:i(()=>[p(r(t.role),1)]),_:2},1032,[`tone`])]),s(`td`,null,[m(a(E),{tone:t.confirmed?`success`:`outline`},{default:i(()=>[p(r(t.confirmed?`Confirmed`:`Not confirmed`),1)]),_:2},1032,[`tone`])]),s(`td`,null,[m(a(E),{tone:t.disabled?`outline`:`success`},{default:i(()=>[p(r(t.disabled?`Disabled`:`Active`),1)]),_:2},1032,[`tone`])]),s(`td`,I,r(t.created),1)],10,P))),128))])]),_:1}),s(`div`,L,[s(`p`,R,r(e.showing),1),m(a(pe),{size:`sm`,page:y.value,"onUpdate:page":o[6]||=e=>y.value=e,pages:e.pages},null,8,[`page`,`pages`])])]),_:1})):(l(),d(a(le),{key:0,title:`No account matches`},{default:i(()=>[...o[10]||=[p(`Change or clear the filters to see more accounts.`,-1)]]),_:1}))]),x()?(l(),d(a(_e),{key:0,title:x().email,"close-label":`Close the details`},{avatar:i(()=>[m(a(de),{initials:x().email.charAt(0).toUpperCase(),size:`md`},null,8,[`initials`])]),subtitle:i(()=>[m(a(E),{tone:x().disabled?`outline`:`success`,class:`w-fit`},{default:i(()=>[p(r(x().disabled?`Disabled`:`Active`),1)]),_:1},8,[`tone`])]),default:i(()=>[s(`dl`,z,[s(`div`,null,[o[12]||=s(`dt`,{class:`cp-type-label`},`Email`,-1),s(`dd`,null,r(x().confirmed?`Confirmed`:`Not confirmed`),1)]),s(`div`,null,[o[13]||=s(`dt`,{class:`cp-type-label`},`Role`,-1),s(`dd`,null,r(x().role),1)]),s(`div`,null,[o[14]||=s(`dt`,{class:`cp-type-label`},`Created`,-1),s(`dd`,null,r(x().created),1)]),s(`div`,null,[o[15]||=s(`dt`,{class:`cp-type-label`},`Sign-in`,-1),s(`dd`,null,r(x().disabled?`Blocked since ${e.disabledSince}`:`Allowed`),1)])]),e.justConfirmed?(l(),d(a(he),{key:0},{default:i(()=>[p(r(x().email)+` is confirmed and can sign in.`,1)]),_:1})):u(``,!0),x().confirmed?u(``,!0):(l(),c(`p`,ye,`This email was never confirmed. Confirm it only when you trust the address, for example when the confirmation email never arrived.`)),x().disabled?u(``,!0):(l(),c(`p`,be,`Disabling signs this account out everywhere and blocks sign-in until you enable it again. Nothing is deleted.`)),s(`div`,xe,[x().confirmed?u(``,!0):(l(),d(a(w),{key:0,size:`md`,class:`gap-2`},{default:i(()=>[m(a(C),{icon:a(ae),size:`sm`},null,8,[`icon`]),o[16]||=p(`Confirm email`,-1)]),_:1})),x().disabled?(l(),d(a(w),{key:1,size:`md`,class:`gap-2`},{default:i(()=>[m(a(C),{icon:a(se),size:`sm`},null,8,[`icon`]),o[17]||=p(`Enable account`,-1)]),_:1})):(l(),d(a(w),{key:2,variant:`outline`,size:`md`,class:`gap-2`},{default:i(()=>[m(a(C),{icon:a(re),size:`sm`},null,8,[`icon`]),o[18]||=p(`Disable account`,-1)]),_:1})),m(a(w),{variant:`danger`,size:`md`,class:`gap-2`},{default:i(()=>[m(a(C),{icon:a(ce),size:`sm`},null,8,[`icon`]),o[19]||=p(`Delete account`,-1)]),_:1})])]),_:1},8,[`title`])):u(``,!0),x()&&e.confirming?(l(),d(a(D),{key:1,open:b.value,"onUpdate:open":o[7]||=e=>b.value=e,title:`Confirm this email?`},{actions:i(()=>[m(a(w),{variant:`ghost`,size:`md`},{default:i(()=>[...o[22]||=[p(`Cancel`,-1)]]),_:1}),m(a(w),{size:`md`,class:`gap-2`},{default:i(()=>[m(a(C),{icon:a(ae),size:`sm`},null,8,[`icon`]),o[23]||=p(`Confirm email`,-1)]),_:1})]),default:i(()=>[s(`p`,Se,[o[20]||=p(`You vouch for `,-1),s(`strong`,Ce,r(x().email),1),o[21]||=p(`, an address nobody proved. The account can sign in at once and its confirmation links stop working. This is written to the audit log.`,-1)])]),_:1},8,[`open`])):u(``,!0),x()&&e.deleting?(l(),d(a(D),{key:2,open:b.value,"onUpdate:open":o[9]||=e=>b.value=e,title:`Delete this account?`},{actions:i(()=>[m(a(w),{variant:`ghost`,size:`md`},{default:i(()=>[...o[26]||=[p(`Cancel`,-1)]]),_:1}),m(a(w),{variant:`danger`,size:`md`,class:`gap-2`,disabled:v.value!==x().email},{default:i(()=>[m(a(C),{icon:a(ce),size:`sm`},null,8,[`icon`]),o[27]||=p(`Delete for good`,-1)]),_:1},8,[`disabled`])]),default:i(()=>[s(`p`,we,[o[24]||=p(`This removes `,-1),s(`strong`,Te,r(x().email),1),o[25]||=p(` and everything saved for it, for good. It is the same deletion the user can ask for in the app. It cannot be undone.`,-1)]),m(a(me),{modelValue:v.value,"onUpdate:modelValue":o[8]||=e=>v.value=e,label:`Type the email to confirm`,autocomplete:`off`},null,8,[`modelValue`])]),_:1},8,[`open`])):u(``,!0)])]),_:1}))}})})))()}var De;function Oe(){return(Oe=e((()=>{Ee(),De=B,B.__docgenInfo=Object.assign({displayName:B.name??B.__name},{exportName:`default`,displayName:`AdminAccountsScreen`,description:``,tags:{},props:[{name:`accounts`,required:!0,type:{name:`Array`,elements:[{name:`Account`}]}},{name:`showing`,required:!1,type:{name:`string`}},{name:`pages`,required:!1,type:{name:`number`},defaultValue:{func:!1,value:`3`}},{name:`signIn`,required:!1,type:{name:`string`},defaultValue:{func:!1,value:`''`}},{name:`search`,required:!1,type:{name:`string`},defaultValue:{func:!1,value:`''`}},{name:`sort`,required:!1,type:{name:`union`,elements:[{name:`"email"`},{name:`"created"`}]},defaultValue:{func:!1,value:`'created'`}},{name:`order`,required:!1,type:{name:`union`,elements:[{name:`"asc"`},{name:`"desc"`}]},defaultValue:{func:!1,value:`'desc'`}},{name:`selected`,required:!1,type:{name:`string`}},{name:`disabledSince`,required:!1,type:{name:`string`}},{name:`deleting`,required:!1,type:{name:`boolean`}},{name:`confirming`,required:!1,type:{name:`boolean`}},{name:`justConfirmed`,required:!1,type:{name:`boolean`}},{name:`typed`,required:!1,type:{name:`string`},defaultValue:{func:!1,value:`''`}}],sourceFiles:[`/home/runner/work/coach-poulpy/coach-poulpy/web/storybook/stories/web/AdminAccountsScreen.vue`]})})))()}var V,H,U,W,G,K,q,J,Y,X,Z,Q,ke;function $(){return($=e((()=>{Oe(),V={title:`Web/Screens/Admin/Accounts`,component:De,tags:[`!autodocs`,`agreed`],parameters:{layout:`fullscreen`,mockup:{issue:336},a11y:{test:`todo`}}},H=[{email:`lea@example.com`,role:`Member`,confirmed:!0,disabled:!1,created:`9 Nov 2026`},{email:`tom@example.com`,role:`Member`,confirmed:!1,disabled:!1,created:`8 Nov 2026`},{email:`ana@example.com`,role:`Admin`,confirmed:!0,disabled:!1,created:`2 Nov 2026`},{email:`sam@example.com`,role:`Member`,confirmed:!0,disabled:!0,created:`28 Oct 2026`},{email:`noa@example.com`,role:`Member`,confirmed:!0,disabled:!1,created:`21 Oct 2026`},{email:`ines@example.com`,role:`Member`,confirmed:!0,disabled:!1,created:`14 Oct 2026`},{email:`hugo@example.com`,role:`Member`,confirmed:!0,disabled:!1,created:`9 Oct 2026`},{email:`zoe@example.com`,role:`Member`,confirmed:!0,disabled:!1,created:`2 Oct 2026`}],U={accounts:H,showing:`Showing 1 to 8 of 21 accounts`},W={args:U},G={args:{accounts:[H[3]],showing:`Showing 1 to 1 of 1 account`,pages:1,signIn:`disabled`,sort:`email`,order:`asc`}},K={args:{accounts:[],search:`nobody`}},q={args:{...U,selected:`lea@example.com`}},J={args:{...U,accounts:[{...H[0],disabled:!0},...H.slice(1)],selected:`lea@example.com`,disabledSince:`9 Nov, 09:02`}},Y={args:{...U,selected:`lea@example.com`,deleting:!0,typed:`lea@exam`}},X={parameters:{mockup:{issue:382}},args:{...U,selected:`tom@example.com`}},Z={parameters:{mockup:{issue:382}},args:{...U,selected:`tom@example.com`,confirming:!0}},Q={parameters:{mockup:{issue:382}},args:{...U,accounts:H.map(e=>e.email===`tom@example.com`?{...e,confirmed:!0}:e),selected:`tom@example.com`,justConfirmed:!0}},ke=[`List`,`Filtered`,`NoMatch`,`Details`,`DetailsDisabled`,`Delete`,`DetailsNotConfirmed`,`ConfirmEmail`,`EmailConfirmed`],W.parameters={...W.parameters,docs:{...W.parameters?.docs,source:{originalSource:`{
  args: base
}`,...W.parameters?.docs?.source}}},G.parameters={...G.parameters,docs:{...G.parameters?.docs,source:{originalSource:`{
  args: {
    accounts: [accounts[3]],
    showing: 'Showing 1 to 1 of 1 account',
    pages: 1,
    signIn: 'disabled',
    sort: 'email',
    order: 'asc'
  }
}`,...G.parameters?.docs?.source}}},K.parameters={...K.parameters,docs:{...K.parameters?.docs,source:{originalSource:`{
  args: {
    accounts: [],
    search: 'nobody'
  }
}`,...K.parameters?.docs?.source}}},q.parameters={...q.parameters,docs:{...q.parameters?.docs,source:{originalSource:`{
  args: {
    ...base,
    selected: 'lea@example.com'
  }
}`,...q.parameters?.docs?.source}}},J.parameters={...J.parameters,docs:{...J.parameters?.docs,source:{originalSource:`{
  args: {
    ...base,
    accounts: [{
      ...accounts[0],
      disabled: true
    }, ...accounts.slice(1)],
    selected: 'lea@example.com',
    disabledSince: '9 Nov, 09:02'
  }
}`,...J.parameters?.docs?.source}}},Y.parameters={...Y.parameters,docs:{...Y.parameters?.docs,source:{originalSource:`{
  args: {
    ...base,
    selected: 'lea@example.com',
    deleting: true,
    typed: 'lea@exam'
  }
}`,...Y.parameters?.docs?.source}}},X.parameters={...X.parameters,docs:{...X.parameters?.docs,source:{originalSource:`{
  parameters: {
    mockup: {
      issue: 382
    }
  },
  args: {
    ...base,
    selected: 'tom@example.com'
  }
}`,...X.parameters?.docs?.source},description:{story:`(#382) an unconfirmed account's details offer "Confirm email" beside the other actions.`,...X.parameters?.docs?.description}}},Z.parameters={...Z.parameters,docs:{...Z.parameters?.docs,source:{originalSource:`{
  parameters: {
    mockup: {
      issue: 382
    }
  },
  args: {
    ...base,
    selected: 'tom@example.com',
    confirming: true
  }
}`,...Z.parameters?.docs?.source},description:{story:`(#382) the confirmation dialog; the admin vouches for an address nobody proved.`,...Z.parameters?.docs?.description}}},Q.parameters={...Q.parameters,docs:{...Q.parameters?.docs,source:{originalSource:`{
  parameters: {
    mockup: {
      issue: 382
    }
  },
  args: {
    ...base,
    accounts: accounts.map(a => a.email === 'tom@example.com' ? {
      ...a,
      confirmed: true
    } : a),
    selected: 'tom@example.com',
    justConfirmed: true
  }
}`,...Q.parameters?.docs?.source},description:{story:`(#382) after confirming, the row reads Confirmed, the button is gone and a note says the account can sign in.`,...Q.parameters?.docs?.description}}}})))()}$();export{Z as ConfirmEmail,Y as Delete,q as Details,J as DetailsDisabled,X as DetailsNotConfirmed,Q as EmailConfirmed,G as Filtered,W as List,K as NoMatch,ke as __namedExportsOrder,V as default};