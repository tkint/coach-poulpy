// @ts-nocheck
// Draws the brand book's live parts (Poulpy, ramps, roles, rules) into the page. Ported as it was in the standalone book.
export function drawBrandBook(): void {
(function () {
  var INK = "#141A26", BLUE = "#3AA6E0", EMBER = "#D9622B";
  var MOODS = [
    { id: "fuming",  name: "Fuming",     tone: "#C0352B", side: "ember", face: "fuming",  when: "Two planned sessions missed." },
    { id: "grumpy",  name: "Grumpy",     tone: "#D9622B", side: "ember", face: "grumpy",  when: "A planned session is still not done 24 h after its day." },
    { id: "annoyed", name: "Annoyed",    tone: "#E8873F", side: "ember", face: "annoyed", when: "Today's session isn't done yet and the evening is coming (the grace period)." },
    { id: "recover", name: "Recovering", tone: "#5DB4E6", side: "blue",  face: "calm",    when: "Rest days, the hours after a hard session, and pain or rehab days. A lighter, fresher blue, never grey." },
    { id: "happy",   name: "Happy",      tone: "#3AA6E0", side: "blue",  face: "happy",   when: "Session done, plan on track. The plush's own colour and the default." },
    { id: "proud",   name: "Proud",      tone: "#2C9CE0", side: "blue",  face: "cocky",   when: "Personal record, or a full octopus. Just a touch richer than Happy." }
  ];
  var M = {}; MOODS.forEach(function (m) { M[m.id] = m; });

  function s(d, w, c) { return '<path d="' + d + '" fill="none" stroke="' + (c || INK) + '" stroke-width="' + (w || 4.5) + '" stroke-linecap="round" stroke-linejoin="round"/>'; }
  function f(d, c) { return '<path d="' + d + '" fill="' + (c || INK) + '"/>'; }
  function eye(x, y, rx, ry, hx) { return '<ellipse cx="' + x + '" cy="' + y + '" rx="' + rx + '" ry="' + ry + '" fill="' + INK + '"/><circle cx="' + (x + (hx || -3)) + '" cy="' + (y - 4) + '" r="2.8" fill="#fff"/>'; }
  var EYES = eye(80, 96, 9, 10.5) + eye(120, 96, 9, 10.5);
  function blushB() { return '<ellipse cx="64" cy="112" rx="8" ry="4.5" fill="#F49A6B" opacity=".75"/><ellipse cx="136" cy="112" rx="8" ry="4.5" fill="#F49A6B" opacity=".75"/>'; }
  function blushE() { return '<ellipse cx="64" cy="114" rx="8" ry="4.5" fill="#8E2414" opacity=".35"/><ellipse cx="136" cy="114" rx="8" ry="4.5" fill="#8E2414" opacity=".35"/>'; }
  function drop(x, y, k, c) { k = k || 1; return '<path d="M' + x + ' ' + (y - 9 * k) + ' C' + (x + 6 * k) + ' ' + (y - 2 * k) + ' ' + (x + 6 * k) + ' ' + (y + 5 * k) + ' ' + x + ' ' + (y + 5 * k) + ' C' + (x - 6 * k) + ' ' + (y + 5 * k) + ' ' + (x - 6 * k) + ' ' + (y - 2 * k) + ' ' + x + ' ' + (y - 9 * k) + ' Z" fill="' + (c || "#A8DDF7") + '" stroke="#3AA6E0" stroke-width="1.5"/>'; }
  function spark(x, y, r, c) { return '<path d="M' + x + ' ' + (y - r) + ' L' + (x + r * .3) + ' ' + (y - r * .3) + ' L' + (x + r) + ' ' + y + ' L' + (x + r * .3) + ' ' + (y + r * .3) + ' L' + x + ' ' + (y + r) + ' L' + (x - r * .3) + ' ' + (y + r * .3) + ' L' + (x - r) + ' ' + y + ' L' + (x - r * .3) + ' ' + (y - r * .3) + ' Z" fill="' + (c || "#F0B53A") + '"/>'; }

  var FACES = {
    happy: EYES + s("M90 112 Q100 123 110 112") + blushB(),
    calm: s("M71 97 Q80 90 89 97", 4.5) + s("M111 97 Q120 90 129 97", 4.5) + s("M92 113 Q100 119 108 113", 4) + blushB(),
    cocky: f("M71 97 a9 9 0 0 0 18 0 Z") + s("M68 95 L92 93", 4) + eye(120, 96, 9, 10.5) + s("M109 79 Q120 70 132 77", 4.5) + s("M88 114 Q102 123 115 108", 4.5) + blushB(),
    proud: s("M70 99 Q80 86 90 99") + s("M110 99 Q120 86 130 99") + f("M87 109 Q100 132 113 109 Z") + blushB(),
    tired: f("M71 97 a9 9 0 0 0 18 0 Z") + f("M111 97 a9 9 0 0 0 18 0 Z") + s("M69 98 L91 95", 4) + s("M109 95 L131 98", 4) +
           s("M73 110 Q80 114 87 110", 2.5, "#4B6274") + s("M113 110 Q120 114 127 110", 2.5, "#4B6274") +
           '<ellipse cx="100" cy="121" rx="8" ry="6.5" fill="' + INK + '"/><ellipse cx="100" cy="125" rx="5" ry="2.6" fill="#F07A7A"/>',
    hurt: eye(80, 97, 9, 10.5) + eye(120, 97, 9, 10.5) + '<circle cx="84" cy="101" r="2" fill="#fff"/><circle cx="124" cy="101" r="2" fill="#fff"/>' +
          s("M68 82 Q78 76 88 80", 4) + s("M132 82 Q122 76 112 80", 4) + s("M91 117 q3 -3 6 0 t6 0 t6 0", 3.5) + blushB(),
    annoyed: f("M71 97 a9 9 0 0 0 18 0 Z") + f("M111 97 a9 9 0 0 0 18 0 Z") + s("M69 96 L91 96", 4) + s("M109 96 L131 96", 4) + s("M110 82 L130 78", 4) + s("M91 116 L109 116", 4.5) + blushE(),
    grumpy: eye(80, 98, 8.5, 9) + eye(120, 98, 8.5, 9) + s("M66 80 L92 89", 5) + s("M134 80 L108 89", 5) + s("M89 121 Q100 110 111 121") + blushE(),
    fuming: eye(81, 100, 7, 7.5) + eye(119, 100, 7, 7.5) + s("M64 82 L93 93", 5.5) + s("M136 82 L107 93", 5.5) +
            '<rect x="86" y="111" width="28" height="13" rx="5" fill="#fff" stroke="' + INK + '" stroke-width="3.5"/>' + s("M86 117.5 L114 117.5", 2.5) + s("M95 111 L95 124", 2) + s("M105 111 L105 124", 2) + blushE(),
    lift: s("M70 90 L87 96 L70 102", 4.5) + s("M130 90 L113 96 L130 102", 4.5) +
          '<rect x="87" y="110" width="26" height="12" rx="5" fill="#fff" stroke="' + INK + '" stroke-width="3.5"/>' + s("M87 116 L113 116", 2.5) + blushB(),
    crossed: eye(84, 94, 8.5, 9.5, -5) + eye(124, 94, 8.5, 9.5, -5) + s("M68 79 L92 85", 5) + s("M134 76 L110 85", 5) + s("M90 110 Q98 104 106 110", 4.5) + blushE()
  };

  function armsSkirt(fill, dy, raise) {
    var xs = [[26,126,25,17],[62,139,24,18],[100,143,24,18],[138,139,24,18],[174,126,25,17]];
    if (raise) xs = xs.filter(function (_, i) { return raise.indexOf(i) < 0; });
    return xs.map(function (a) { return '<ellipse cx="' + a[0] + '" cy="' + (a[1] + dy) + '" rx="' + a[2] + '" ry="' + a[3] + '" fill="' + fill + '"/>'; }).join("");
  }
  function lining(tone) { return MOODS.some(function (m) { return m.tone === tone && m.side === "ember"; }) ? BLUE : EMBER; }
  function body(tone, raise) {
    return armsSkirt(lining(tone), 6, raise) + armsSkirt(tone, 0, raise) +
      '<path d="M37 130 C33 68 64 30 100 30 C136 30 167 68 163 130 Z" fill="' + tone + '"/>' +
      '<ellipse cx="76" cy="56" rx="17" ry="9" fill="#fff" opacity=".22" transform="rotate(-20 76 56)"/>';
  }
  function tentacle(d, tone, w) { return s(d, w || 20, lining(tone)).replace('<path ', '<path transform="translate(0 4)" ') + s(d, w || 20, tone); }
  function mirror(d) { return d.replace(/(-?\d+(?:\.\d+)?) (-?\d+(?:\.\d+)?)/g, function (_, x, y) { return (200 - +x) + " " + y; }); }

  function svg(inner, vb, label) { return '<svg viewBox="' + (vb || "0 0 200 180") + '" role="img" aria-label="' + label + '">' + inner + '</svg>'; }
  function poulpy(face, tone, label) {
    var m = M[face] || null;
    tone = tone || (m ? m.tone : (["grumpy", "annoyed", "fuming", "crossed"].indexOf(face) > -1 ? EMBER : BLUE));
    return svg(body(tone) + FACES[(m && m.face) || face], null, label || ("Poulpy, " + face));
  }

  var VB = "-24 -36 248 226";
  // A lifted tentacle: one short plush lobe swung from its root. a = angle from hanging down (90 right, 180 up, 270 left).
  // lining "face" shows the underside towards us (folded over); otherwise the lining peeks along one edge.
  function lobe(x, y, a, len, wid, tone, opt) {
    opt = opt || {};
    var r = a * Math.PI / 180, dx = Math.sin(r), dy = Math.cos(r);
    var cx = x + dx * len / 2, cy = y + dy * len / 2, rot = (-a).toFixed(1);
    var el = function (fill, ox, oy, k) { return '<ellipse cx="' + (cx + (ox || 0)).toFixed(1) + '" cy="' + (cy + (oy || 0)).toFixed(1) + '" rx="' + (wid / 2 * (k || 1)).toFixed(1) + '" ry="' + (len / 2 * (k || 1)).toFixed(1) + '" fill="' + fill + '" transform="rotate(' + rot + ' ' + (cx + (ox || 0)).toFixed(1) + ' ' + (cy + (oy || 0)).toFixed(1) + ')"/>'; };
    var lin = lining(tone), out = "";
    if (opt.face) { out = el(tone) + el(lin, 0, 0, .8); }
    else { var px = Math.cos(r) * (opt.side || 4), py = -Math.sin(r) * (opt.side || 4); out = el(lin, px, py) + el(tone); }
    if (opt.bulge) { var k = opt.bulge, bx = cx + Math.cos(r) * k, by = cy - Math.sin(r) * k; out += '<circle cx="' + bx.toFixed(1) + '" cy="' + by.toFixed(1) + '" r="12" fill="' + tone + '"/><circle cx="' + (bx - 3).toFixed(1) + '" cy="' + (by - 4).toFixed(1) + '" r="3.5" fill="#fff" opacity=".3"/>'; }
    return out;
  }
  function tip(x, y, a, len) { var r = a * Math.PI / 180; return [x + Math.sin(r) * len, y + Math.cos(r) * len]; }
  function dumbbell(x, y) { return '<rect x="' + (x - 16) + '" y="' + (y - 2.5) + '" width="32" height="5" rx="2.5" fill="#56687A"/><rect x="' + (x - 22) + '" y="' + (y - 11) + '" width="9" height="22" rx="3" fill="' + INK + '"/><rect x="' + (x + 13) + '" y="' + (y - 11) + '" width="9" height="22" rx="3" fill="' + INK + '"/>'; }

  var POSES = {
    flex: function () {
      var t = M.proud.tone;
      return svg('<circle cx="100" cy="90" r="96" fill="#F0B53A" opacity=".12"/>' + body(t) + FACES.cocky +
        spark(6, 52, 10) + spark(196, 40, 8) + spark(176, 4, 5) + spark(24, 10, 5) +
        '<rect x="128" y="-12" width="54" height="24" rx="12" fill="' + INK + '"/><text x="155" y="5" text-anchor="middle" font-family="Nunito, sans-serif" font-weight="800" font-size="14" fill="#F0B53A">PR!</text>', VB, "Poulpy looking cocky after a personal record");
    },
    lift: function () {
      var t = M.happy.tone;
      return svg(body(t) + FACES.lift + drop(152, 50, .9) + drop(46, 64, .7) +
        s("M12 70 l-12 -4 M10 86 l-14 0", 3, "#56687A") + s("M188 70 l12 -4 M190 86 l14 0", 3, "#56687A") +
        '<g transform="rotate(-8 196 156)">' + dumbbell(196, 156) + '</g>', VB, "Poulpy straining with gritted teeth, a dumbbell beside him");
    },
    champion: function () {
      var t = M.proud.tone;
      var conf = [[-8,40,"#F0B53A",20],[20,0,EMBER,-30],[180,4,"#5BBEF0",40],[204,50,"#F0B53A",-15],[150,-14,EMBER,10],[46,-12,"#5BBEF0",60],[100,-24,"#F0B53A",30]]
        .map(function (c) { return '<rect x="' + c[0] + '" y="' + c[1] + '" width="9" height="5" rx="1.5" fill="' + c[2] + '" transform="rotate(' + c[3] + ' ' + c[0] + ' ' + c[1] + ')"/>'; }).join("");
      return svg(conf + body(t) + FACES.proud +
        s("M74 120 L96 146", 7, EMBER) + s("M126 120 L104 146", 7, EMBER) +
        '<circle cx="100" cy="152" r="14" fill="#F0B53A" stroke="#C98A0C" stroke-width="3"/>' + spark(100, 152, 7, "#FFF3C4"), VB, "Poulpy wearing a gold medal under confetti");
    },
    tall: function () {
      // Same plush, skirt stretched a little from the head's base so he stands taller with no gaps.
      var t = M.proud.tone;
      return svg('<ellipse cx="100" cy="172" rx="88" ry="6" fill="' + INK + '" opacity=".1"/>' +
        '<g transform="translate(0 -12)"><g transform="translate(0 -36) scale(1 1.3)">' + armsSkirt(lining(t), 6) + armsSkirt(t, 0) + '</g>' +
        '<path d="M37 130 C33 68 64 30 100 30 C136 30 167 68 163 130 Z" fill="' + t + '"/>' +
        '<ellipse cx="76" cy="56" rx="17" ry="9" fill="#fff" opacity=".22" transform="rotate(-20 76 56)"/>' + FACES.proud + '</g>' +
        s("M-4 70 l0 -16 M-10 60 l6 -6 6 6", 3, "#56687A") + s("M204 70 l0 -16 M198 60 l6 -6 6 6", 3, "#56687A"), VB, "Poulpy standing a little taller on his tentacles");
    },
    exhausted: function () {
      var t = M.recover.tone;
      return svg(body(t) +
        '<path d="M45 60 Q100 38 155 60 L158 76 Q100 54 42 76 Z" fill="#FFFFFF" stroke="' + INK + '" stroke-opacity=".25" stroke-width="1.5"/>' +
        s("M44 68 Q100 46 156 68", 4, EMBER) +
        s("M157 66 q16 0 22 12", 6, "#FFFFFF") + s("M157 70 q10 10 6 22", 6, "#FFFFFF") +
        FACES.tired + drop(30, 76, 1.1) + drop(172, 88, 1) + drop(160, 112, .7) + drop(12, 128, .8) + drop(194, 140, .9), VB, "Poulpy exhausted with a sweatband, dripping with sweat");
    },
    side: function () {
      var t = M.recover.tone;
      var face = f("M89 99 a8.5 8.5 0 0 0 17 0 Z") + f("M124 98 a6.5 7.5 0 0 0 13 0 Z") + s("M87 98 L108 96", 3.5) + s("M122 96 L139 97", 3.5) +
        s("M108 117 Q115 120 122 117", 4) + '<ellipse cx="86" cy="112" rx="7" ry="4" fill="#F49A6B" opacity=".7"/><ellipse cx="144" cy="111" rx="5" ry="3.5" fill="#F49A6B" opacity=".7"/>';
      return svg('<g transform="rotate(-6 100 140)">' + body(t) + '<path d="M70 40 Q56 86 62 128" fill="none" stroke="#fff" stroke-opacity=".25" stroke-width="2"/>' + face + '</g>' +
        '<text x="160" y="40" font-family="Baloo 2, sans-serif" font-weight="800" font-size="22" fill="#56687A">…</text>', VB, "Poulpy turned slightly to the side, tired");
    },
    sleeping: function () {
      var t = M.recover.tone;
      return svg(body(t) +
        '<path d="M62 52 Q100 30 140 46 C132 26 120 10 108 -4 C96 12 78 30 62 52 Z" fill="#1574B0"/>' + s("M62 52 Q100 32 140 46", 8, "#FFFFFF") + '<circle cx="108" cy="-6" r="8" fill="#FFFFFF" stroke="#B6C4D0" stroke-width="1.5"/>' +
        s("M71 96 Q80 104 89 96") + s("M111 96 Q120 104 129 96") + '<circle cx="100" cy="117" r="4" fill="' + INK + '"/>' + blushB() +
        '<text x="160" y="70" font-family="Baloo 2, sans-serif" font-weight="800" font-size="24" fill="#1574B0">z</text><text x="178" y="52" font-family="Baloo 2, sans-serif" font-weight="800" font-size="17" fill="#1574B0">z</text>', VB, "Poulpy asleep wearing a nightcap");
    },
    hurt: function () {
      var t = M.recover.tone;
      return svg(body(t) +
        '<g transform="rotate(35 132 56)"><rect x="112" y="50" width="40" height="13" rx="5" fill="#F5D7B5" stroke="#D9B48C" stroke-width="1.5"/></g>' +
        '<g transform="rotate(-35 132 56)"><rect x="112" y="50" width="40" height="13" rx="5" fill="#F5D7B5" stroke="#D9B48C" stroke-width="1.5"/></g>' +
        FACES.hurt + drop(66, 112, .55), VB, "Poulpy with a plaster on his head");
    },
    hide: function () {
      var t = M.happy.tone;
      return svg(body(t) + s("M70 100 Q80 92 90 100", 4.5) + s("M110 100 Q120 92 130 100", 4.5) + s("M93 116 Q100 121 107 116", 4) +
        '<ellipse cx="66" cy="112" rx="12" ry="6" fill="#F49A6B" opacity=".9"/><ellipse cx="134" cy="112" rx="12" ry="6" fill="#F49A6B" opacity=".9"/>' +
        s("M60 110 l4 4 M66 109 l4 4 M128 109 l4 4 M134 110 l4 4", 2, "#C96A3E") + drop(150, 62, .7), VB, "Poulpy blushing with his eyes squeezed shut");
    },
    scratch: function () {
      var t = M.happy.tone;
      return svg(body(t) + eye(80, 97, 8, 9.5, 0) + eye(120, 95, 9.5, 11, 0) + s("M66 84 L90 86", 4) + s("M110 78 Q120 70 132 76", 4) +
        s("M90 116 q5 -4 10 0 t10 0", 4) + blushB() +
        '<circle cx="160" cy="44" r="4" fill="#B6C4D0"/><circle cx="172" cy="28" r="6" fill="#B6C4D0"/>' +
        '<ellipse cx="196" cy="-4" rx="24" ry="18" fill="#FFFFFF" stroke="#B6C4D0" stroke-width="2"/>' +
        '<text x="196" y="6" text-anchor="middle" font-family="Baloo 2, sans-serif" font-weight="800" font-size="26" fill="#1574B0">?</text>', VB, "Poulpy thinking, with a question mark in a thought bubble");
    },
    crossed: function () {
      var t = M.grumpy.tone;
      return svg(body(t) + FACES.crossed +
        s("M196 150 q6 -6 12 0", 3, "#7A2C12") + s("M200 140 q8 -8 16 0", 3, "#7A2C12") +
        '<text x="-16" y="34" font-family="Baloo 2, sans-serif" font-weight="800" font-size="22" fill="#7A2C12">hmph</text>', VB, "Poulpy with two front tentacles crossed, looking away");
    },
    fuming: function () {
      var t = M.fuming.tone;
      function puff(x, y) { return [[0,0,12],[13,-6,10],[-12,-4,9],[3,-15,9]].map(function (p) { return '<circle cx="' + (x + p[0]) + '" cy="' + (y + p[1]) + '" r="' + p[2] + '" fill="#E6EEF4" stroke="#B6C4D0" stroke-width="1.5"/>'; }).join(""); }
      return svg(puff(52, 6) + puff(150, -2) + s("M70 30 L64 18", 4, "#B6C4D0") + s("M132 28 L140 14", 4, "#B6C4D0") +
        body(t) +
        s("M14 80 l-12 -4 M12 96 l-14 0", 3, "#7A2C12") + s("M186 80 l12 -4 M188 96 l14 0", 3, "#7A2C12") +
        FACES.fuming +
        s("M140 50 q6 6 0 12 M152 50 q-6 6 0 12 M140 50 q6 -6 12 0 M140 62 q6 6 12 0", 3.5, "#FFDCC7"), VB, "Poulpy fuming with steam, shaking two side tentacles");
    },
    spin: function () {
      // Comic-style spin: the arc behind him, Poulpy, then the arc in front, plus speed marks.
      var t = M.happy.tone;
      var back = s("M186 132 Q100 100 14 132", 4, "#7FCBF0");
      var front = '<path class="spin-arc" d="M8 140 Q100 192 194 140" fill="none" stroke="#1574B0" stroke-width="5" stroke-linecap="round"/>' +
        f("M202 132 L188 130 L194 146 Z", "#1574B0");
      var face = eye(88, 96, 9, 10.5, 2) + eye(128, 96, 9, 10.5, 2) + s("M98 112 Q108 122 118 112") + blushB();
      return svg(back + body(t) + face + front +
        s("M-14 70 l18 0 M-18 84 l14 0", 3, "#7FCBF0") + s("M196 64 l18 0 M200 78 l14 0", 3, "#7FCBF0") +
        '<path d="M150 40 q8 -10 18 -4" fill="none" stroke="#7FCBF0" stroke-width="3" stroke-linecap="round"/>', VB, "Poulpy spinning on the spot, drawn with comic motion lines");
    }
  };

  document.querySelectorAll("[data-poulpy]").forEach(function (el) {
    var k = el.getAttribute("data-poulpy");
    el.innerHTML = k === "crossedmini" ? poulpy("crossed", M.grumpy.tone, "Poulpy grumpy") : poulpy(k);
  });
  document.querySelectorAll("[data-pose]").forEach(function (el) { el.innerHTML = POSES[el.getAttribute("data-pose")](); });

  // logo
  document.querySelectorAll("[data-logo]").forEach(function (el) { el.innerHTML = poulpy("happy", BLUE, "Coach Poulpy logo"); });
  var iconInner = function (bg) { return '<rect width="200" height="200" fill="' + bg + '"/><g transform="translate(14 24) scale(.86)">' + body(BLUE) + FACES.happy + '</g>'; };
  document.getElementById("icons").innerHTML = [["#F4F9FC", 72, "Foam"], ["#0A1520", 72, "Deep sea"], ["#FFFFFF", 48, "48 px"], ["#FFFFFF", 28, "28 px"]].map(function (i) {
    return '<figure><svg viewBox="0 0 200 200" width="' + i[1] + '" height="' + i[1] + '" role="img" aria-label="App icon, ' + i[2] + '">' + iconInner(i[0]) + '</svg><figcaption>' + i[2] + '</figcaption></figure>';
  }).join("");

  // spectrum
  var bar = document.getElementById("specBar"), labels = document.getElementById("specLabels");
  bar.innerHTML = MOODS.map(function (m, i) { return '<button type="button" id="mood-' + m.id + '" aria-pressed="false" aria-label="' + m.name + '" style="background:' + m.tone + '" data-i="' + i + '"></button>'; }).join("");
  labels.innerHTML = MOODS.map(function (m) { return '<span>' + m.name + '</span>'; }).join("");
  function show(i) {
    var m = MOODS[i];
    document.getElementById("specArt").innerHTML = poulpy(m.id, m.tone, "Poulpy, " + m.name);
    document.getElementById("specArt").style.backgroundColor = m.side === "blue" ? "var(--primary-soft)" : "var(--ember-soft)";
    document.getElementById("specName").textContent = m.name;
    document.getElementById("specName").style.color = m.tone;
    var side = document.getElementById("specSide");
    side.textContent = m.side === "blue" ? "Blue side" : "Ember side";
    side.style.color = m.side === "blue" ? "var(--primary)" : "var(--ember)";
    document.getElementById("specWhen").textContent = m.when;
    document.getElementById("specHex").textContent = "mood." + m.id + " · " + m.tone;
    bar.querySelectorAll("button").forEach(function (b, j) { b.setAttribute("aria-pressed", j === i ? "true" : "false"); });
  }
  bar.addEventListener("click", function (e) { var b = e.target.closest("button"); if (b) show(+b.getAttribute("data-i")); });
  show(5);

  // ramps
  var ramps = [
    ["Lagoon", [["50","#EAF6FD"],["100","#CDEBFA"],["300","#7FCBF0"],["400","#3AA6E0"],["600","#1574B0"],["800","#0C4A73"],["900","#082F4A"]]],
    ["Ember", [["50","#FFF1E8"],["100","#FFDCC7"],["300","#F4A473"],["400","#E8873F"],["500","#D9622B"],["600","#B4471C"],["800","#7A2C12"]]],
    ["Neutral", [["0","#FFFFFF"],["50","#F4F9FC"],["100","#E6EFF5"],["300","#B6C4D0"],["500","#6B7C8C"],["700","#374555"],["900","#141A26"]]]
  ];
  document.getElementById("ramps").innerHTML = ramps.map(function (r) {
    return '<div class="ramp"><span class="lbl">' + r[0] + '</span>' + r[1].map(function (sw, i) {
      var light = i < 4; return '<div class="sw" style="background:' + sw[1] + ';color:' + (light ? "#141A26" : "#F4F9FC") + (sw[1] === "#FFFFFF" ? ";border:1px solid #D3E0E9" : "") + '">' + sw[0] + '<br>' + sw[1] + '</div>';
    }).join("") + '</div>';
  }).join("");

  function roles(el, rows) {
    el.insertAdjacentHTML("beforeend", rows.map(function (r) {
      return '<div class="role-row"><span class="dot" style="background:' + r[1] + '"></span><span>' + r[0] + '</span><code style="color:inherit;opacity:.7">' + r[1] + '</code></div>';
    }).join(""));
  }
  roles(document.getElementById("rolesLight"), [["Primary · buttons, links", "#1574B0"], ["Grumpy accent", "#B4471C"], ["Background", "#F4F9FC"], ["Surface", "#FFFFFF"], ["Text", "#141A26"], ["Success", "#2E9E6A"], ["Error", "#C8264A"]]);
  roles(document.getElementById("rolesDark"), [["Primary · buttons, links", "#5BBEF0"], ["Grumpy accent", "#FF9A66"], ["Background · deep sea", "#0A1520"], ["Surface", "#112230"], ["Text", "#E8F1F7"], ["Success", "#4CC38A"], ["Error", "#FF6B85"]]);

  document.getElementById("spacing").innerHTML = [["xs",4],["sm",8],["md",12],["lg",16],["xl",24],["2xl",32],["3xl",48]].map(function (sp) {
    return '<div class="sp-row"><span>' + sp[0] + ' · ' + sp[1] + '</span><div class="sp-bar" style="width:' + (sp[1] * 2.4) + 'px;max-width:100%"></div></div>';
  }).join("");

  // behaviour rules
  var RULES = [
    ["You finish any session", "happy", "Happy", "Always flips back to blue, with the flip animation. The flip is the reward."],
    ["You set a record", "proud", "Cocky", "Lasts until the end of the day, then back to Happy."],
    ["The eighth tentacle lights", "proud", "Champion", "Shown on the end-of-session screen and on Home; the next session starts a new ring."],
    ["A hard session is finished (effort 8+)", "recover", "Exhausted or Side glance", "For the rest of the day; Poulpy recommends recovery."],
    ["Today is a planned rest day", "recover", "Sleeping", "Rest days from the plan never count as missed."],
    ["You log pain, or a rehab session is marked too hard", "recover", "Patched up", "Blocks the ember side for 72 h. Poulpy never gets cross at someone who is hurt."],
    ["Today's session is not done yet by the evening", "annoyed", "Annoyed", "The grace period: a nudge, not a scolding."],
    ["A planned session is still not done 24 h after its day", "grumpy", "Pouting", "Flips to the ember side and offers a short catch-up session."],
    ["Two planned sessions missed", "fuming", "Fuming", "Same short catch-up offer. Any session flips him straight back to Happy."],
    ["No plan at all (free training)", "happy", "Happy or Sleeping", "With nothing promised, nothing can be missed. Sleeping after 7 days without training."]
  ];
  document.getElementById("rules").innerHTML = RULES.map(function (r) {
    var m = M[r[1]];
    return '<tr><td>' + r[0] + '</td><td><span class="pill"><i style="background:' + m.tone + '"></i>' + m.name + '</span></td><td>' + r[2] + '</td><td>' + r[3] + '</td></tr>';
  }).join("");

  // tentacle ring
  function arc(cx, cy, r, a0, a1) {
    var p = function (a) { var t = (a - 90) * Math.PI / 180; return [cx + r * Math.cos(t), cy + r * Math.sin(t)]; };
    var st = p(a0), e = p(a1);
    return "M" + st[0].toFixed(2) + " " + st[1].toFixed(2) + " A" + r + " " + r + " 0 0 1 " + e[0].toFixed(2) + " " + e[1].toFixed(2);
  }
  document.querySelectorAll("[data-ring]").forEach(function (el) {
    var lit = +el.getAttribute("data-ring"), em = el.getAttribute("data-ring-mood") === "ember";
    var on = em ? "var(--ember)" : "var(--primary)", segs = "";
    for (var i = 0; i < 8; i++) segs += '<path d="' + arc(100, 100, 86, i * 45 + 5, i * 45 + 40) + '" fill="none" stroke="' + (i < lit ? on : "var(--line)") + '" stroke-width="15" stroke-linecap="round"/>';
    el.outerHTML = '<svg viewBox="0 0 200 200" role="img" aria-label="' + lit + ' of 8 tentacles lit">' + segs + '</svg>';
  });

  // flip
  var stage = document.getElementById("stage"), state = document.getElementById("flipState");
  function flip() {
    var on = stage.classList.toggle("flipped");
    stage.setAttribute("aria-pressed", on ? "true" : "false");
    state.textContent = on ? "Ember side: a planned session slipped, Poulpy is grumpy." : "Blue side: you trained, Poulpy is happy.";
  }
  stage.addEventListener("click", flip);
  document.getElementById("flipBtn").addEventListener("click", flip);

  // token tabs
  document.querySelectorAll(".tab").forEach(function (t) {
    t.addEventListener("click", function () {
      document.querySelectorAll(".tab").forEach(function (o) { o.setAttribute("aria-selected", o === t ? "true" : "false"); });
      document.querySelectorAll("[data-panel]").forEach(function (p) { p.hidden = p.getAttribute("data-panel") !== t.getAttribute("data-tab"); });
    });
  });
})()
}
