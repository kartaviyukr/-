'use strict';
/*
 * Мнемо — карточки и уроки для запоминания слов.
 * Один файл без сборки: работает и внутри APK (WebView), и как PWA на iPhone.
 * Все данные хранятся локально в localStorage.
 */
(() => {
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const view = $('#view');
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const DAY = 864e5, MIN = 6e4;
const now = () => Date.now();
const pad = n => String(n).padStart(2, '0');
const dayKey = t => { const d = new Date(t); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`; };
const startOfDay = t => { const d = new Date(t); d.setHours(0, 0, 0, 0); return d.getTime(); };
const addDays = (t, n) => { const d = new Date(t); d.setDate(d.getDate() + n); return d.getTime(); };
const rnd = n => Math.floor(Math.random() * n);
const shuffle = a => { a = [...a]; for (let i = a.length - 1; i > 0; i--) { const j = rnd(i + 1); [a[i], a[j]] = [a[j], a[i]]; } return a; };
const pick = a => a[rnd(a.length)];
const uid = () => now().toString(36) + Math.random().toString(36).slice(2, 8);
const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
const plural = (n, one, few, many) => { const a = n % 10, b = n % 100; return a === 1 && b !== 11 ? one : a >= 2 && a <= 4 && (b < 10 || b >= 20) ? few : many; };
const nWords = n => `${n} ${plural(n, 'слово', 'слова', 'слов')}`;
const nCards = n => `${n} ${plural(n, 'карточка', 'карточки', 'карточек')}`;
const isAndroid = !!window.Android;

/* ───────────────────────── Языки и уровни ───────────────────────── */
const LANGS = {
  en: { name: 'Английский', flag: '🇬🇧', front: 'en-US' },
  it: { name: 'Итальянский', flag: '🇮🇹', front: 'it-IT' },
  uk: { name: 'Украинский', flag: '🇺🇦', front: 'uk-UA' },
  xx: { name: 'Другие', flag: '🌐', front: 'en-US' },
};
const LEVEL_ORDER = ['A0', 'A1', 'A2', 'B1', 'B2', 'C1', 'C2', ''];
const LEVEL_NAMES = { A0: 'Стартовый', A1: 'Начальный', A2: 'Элементарный', B1: 'Средний', B2: 'Выше среднего', C1: 'Продвинутый', C2: 'Свободный', '': 'Без уровня' };
const levelRank = l => { const i = LEVEL_ORDER.indexOf(l || ''); return i < 0 ? 99 : i; };
const langOfCode = code => { const p = String(code || '').slice(0, 2).toLowerCase(); return LANGS[p] && p !== 'xx' ? p : 'xx'; };

/* ───────────────────────── Хранилище ─────────────────────────
 * Встроенные наборы (тысячи слов) не копируются в localStorage — он маленький.
 * Храним только то, что изменил пользователь: правки карточек (cedit),
 * удалённые карточки (removed), скрытые наборы (hidden), переименования и
 * добавленные в встроенный набор карточки (bdeck), а также свои наборы (decks).
 */
const KEY = 'mnemo.v1';
const DEF_SETTINGS = {
  theme: 'dark', goal: 50, autoplay: false, rate: 0.9, dir: 'td', lessonSize: 6,
  newPerDay: 10, typos: true, sound: true, lang: 'en',
};
let db;
let BUILTIN = [];
let srcIdx = new Map();

function buildBuiltin() {
  BUILTIN = []; srcIdx = new Map();
  for (const b of window.MNEMO_DECKS || []) {
    const lang = b.lang || 'en';
    const cards = [];
    for (const line of String(b.cards).split('\n')) {
      if (!line.trim()) continue;
      const [t, d, ex, exT] = line.split('|').map(s => (s || '').trim());
      if (!t || !d) continue;
      const id = `${lang}:${t}`;
      if (srcIdx.has(id)) continue; // одно слово — одна карточка на язык
      const c = { id, t, d, ex: ex || '', exT: exT || '', note: '' };
      srcIdx.set(id, c);
      cards.push(c);
    }
    if (!cards.length) continue;
    BUILTIN.push({ id: b.id, lang, level: b.level || '', title: b.title, emoji: b.emoji, desc: b.desc || '',
      front: b.front || LANGS[lang].front, back: b.back || 'ru-RU', builtin: true, cards });
  }
  // стабильная сортировка по уровню, внутри уровня — порядок файлов
  BUILTIN = BUILTIN.map((d, i) => [d, i]).sort((a, b) => levelRank(a[0].level) - levelRank(b[0].level) || a[1] - b[1]).map(x => x[0]);
}

function load() {
  try { db = JSON.parse(localStorage.getItem(KEY) || 'null'); } catch (e) { db = null; }
  if (!db || !Array.isArray(db.decks)) db = { decks: [], created: now() };
  db.settings = { ...DEF_SETTINGS, ...(db.settings || {}) };
  for (const k of ['prog', 'lessons', 'days', 'best', 'removed', 'flags', 'bdeck', 'cedit', 'hidden']) db[k] = db[k] || {};
  if (!db.settings.active || typeof db.settings.active !== 'object') db.settings.active = {};
  if (!db.settings.open || typeof db.settings.open !== 'object') db.settings.open = {};
  if (db.settings.activeDeck) { db.settings.active.en = db.settings.active.en || db.settings.activeDeck; delete db.settings.activeDeck; }
  if (db.seeded || db.decks.some(d => d.builtin)) migrateV1();
  if (!LANGS[db.settings.lang]) db.settings.lang = 'en';
  rebuild();
}

// Первая версия копировала встроенные наборы целиком и называла карточки «набор:слово».
function migrateV1() {
  const remap = id => { const m = /^en-[a-z]+:(.*)$/.exec(id); return m ? 'en:' + m[1] : id; };
  const np = {};
  for (const [k, v] of Object.entries(db.prog)) { const nk = remap(k); if (!np[nk] || (v.last || 0) > (np[nk].last || 0)) np[nk] = v; }
  db.prog = np;
  for (const dk of Object.keys(db.lessons)) {
    const o = {};
    for (const [k, v] of Object.entries(db.lessons[dk])) o[k.replace(/^(\w+):en-[a-z]+:/, '$1:en:')] = v;
    db.lessons[dk] = o;
  }
  const nr = {};
  for (const k of Object.keys(db.removed)) nr[remap(k)] = 1;
  db.removed = nr;
  const b0 = new Map(BUILTIN.map(b => [b.id, b]));
  const owner = new Map();
  for (const b of BUILTIN) for (const c of b.cards) owner.set(c.id, b.id);
  for (const d of db.decks.filter(x => x.builtin)) {
    const src = b0.get(d.id);
    const o = {};
    if (src) for (const f of ['title', 'emoji', 'desc', 'front', 'back']) if (d[f] && d[f] !== src[f]) o[f] = d[f];
    for (const c of d.cards) {
      if (/^c/.test(c.id)) { (o.extra = o.extra || []).push(c); continue; }
      const nid = remap(c.id), s = srcIdx.get(nid);
      // слово могло быть в двух наборах — переносим правки только из «своего»
      if (!s || owner.get(nid) !== d.id) continue;
      const e = {};
      for (const f of ['t', 'd', 'ex', 'exT']) if ((c[f] || '') !== (s[f] || '')) e[f] = c[f];
      if (c.note) e.note = c.note;
      if (c.star) e.star = true;
      if (Object.keys(e).length) db.cedit[nid] = e;
    }
    if (Object.keys(o).length) db.bdeck[d.id] = o;
  }
  for (const id of db.seeded || []) if (!db.decks.some(d => d.id === id)) db.hidden[id] = 1;
  db.decks = db.decks.filter(d => !d.builtin);
  delete db.seeded;
}

let DECKS = [];
function rebuild() {
  DECKS = [];
  for (const b of BUILTIN) {
    if (db.hidden[b.id]) continue;
    const o = db.bdeck[b.id] || {};
    const cards = [];
    for (const s of b.cards) {
      if (db.removed[s.id]) continue;
      const e = db.cedit[s.id];
      cards.push(e ? { ...s, ...e } : { ...s });
    }
    if (o.extra) cards.push(...o.extra);
    DECKS.push({ ...b, title: o.title || b.title, emoji: o.emoji || b.emoji, desc: o.desc ?? b.desc, front: o.front || b.front, back: o.back || b.back, cards });
  }
  for (const d of db.decks) { d.lang = d.lang || langOfCode(d.front); DECKS.push(d); }
  reindex();
}

let saveT = 0;
function saveSoon() { clearTimeout(saveT); saveT = setTimeout(save, 400); }
function save() {
  clearTimeout(saveT);
  try { localStorage.setItem(KEY, JSON.stringify(db)); } catch (e) { toast('Не удалось сохранить: память переполнена'); }
}
window.appFlush = save;
window.addEventListener('pagehide', save);
document.addEventListener('visibilitychange', () => { if (document.hidden) save(); });

let cardIdx = new Map();
let byLang = {};
function reindex() {
  cardIdx = new Map(); byLang = {};
  for (const d of DECKS) {
    const L = byLang[d.lang] || (byLang[d.lang] = []);
    for (const c of d.cards) { if (cardIdx.has(c.id)) continue; cardIdx.set(c.id, { c, d }); L.push(c); }
  }
}
const curLang = () => db.settings.lang;
const deckById = id => DECKS.find(d => d.id === id);
const langDecks = (l = curLang()) => DECKS.filter(d => d.lang === l);
const langList = () => Object.keys(LANGS).filter(l => l !== 'xx' || DECKS.some(d => d.lang === 'xx'));
const allCards = () => byLang[curLang()] || [];
const everyCard = () => [...cardIdx.values()].map(x => x.c);

// Изменения карточек и наборов: для встроенных записываем только разницу.
function editCard(c, f) {
  Object.assign(c, f);
  if (srcIdx.has(c.id)) db.cedit[c.id] = { ...(db.cedit[c.id] || {}), ...f };
  saveSoon();
}
function addCard(d, c, noIndex) {
  d.cards.push(c);
  if (d.builtin) { const o = db.bdeck[d.id] || (db.bdeck[d.id] = {}); (o.extra = o.extra || []).push(c); }
  if (!noIndex) reindex();
  saveSoon();
}
function removeCard(d, c) {
  d.cards = d.cards.filter(x => x !== c);
  if (d.builtin) {
    if (srcIdx.has(c.id)) db.removed[c.id] = 1;
    else { const o = db.bdeck[d.id]; if (o && o.extra) o.extra = o.extra.filter(x => x !== c); }
  }
  delete db.prog[c.id];
  reindex();
  saveSoon();
}
function editDeck(d, f) {
  Object.assign(d, f);
  if (d.builtin) db.bdeck[d.id] = { ...(db.bdeck[d.id] || {}), ...f };
  saveSoon();
}

const SMART = {
  '@all': { title: 'Все слова', emoji: '📚', desc: 'Все карточки из всех наборов вперемешку.' },
  '@seen': { title: 'Изученные', emoji: '🧠', desc: 'Слова, которые вы уже встречали в занятиях.' },
  '@star': { title: 'Избранное', emoji: '⭐', desc: 'Карточки, отмеченные звёздочкой.' },
  '@hard': { title: 'Трудные слова', emoji: '🎯', desc: 'Слова, в которых вы чаще всего ошибаетесь. Занимайтесь ими отдельно.' },
};
function smartCards(id) {
  const all = allCards();
  if (id === '@all') return all;
  if (id === '@seen') return all.filter(c => db.prog[c.id]?.seen);
  if (id === '@star') return all.filter(c => c.star);
  if (id === '@hard') {
    return all.filter(c => { const p = db.prog[c.id]; return p && p.wrong >= 1 && p.wrong / (p.wrong + p.right) >= 0.25; })
      .sort((a, b) => db.prog[b.id].wrong - db.prog[a.id].wrong).slice(0, 60);
  }
  return [];
}
function getDeck(id) {
  if (id && id[0] === '@') return { id, ...SMART[id], smart: true, cards: smartCards(id) };
  return deckById(id);
}
function langsOf(c) {
  const d = cardIdx.get(c.id)?.d;
  return { fl: d?.front || 'en-US', bl: d?.back || 'ru-RU' };
}

/* ───────────────────────── Интервальные повторения ───────────────────────── */
function prog(id) {
  return db.prog[id] || (db.prog[id] = { ef: 2.5, ivl: 0, due: 0, reps: 0, lapses: 0, right: 0, wrong: 0, seen: 0, last: 0 });
}
function level(id) {
  const p = db.prog[id];
  if (!p || !p.seen) return 0;
  if (p.ivl >= 21) return 4;
  if (p.ivl >= 7) return 3;
  if (p.ivl >= 2) return 2;
  return 1;
}
const LEVELS = ['новое', 'изучаю', 'знакомо', 'знаю', 'выучено'];
const isDue = c => { const p = db.prog[c.id]; return !!(p && p.seen && p.due <= now()); };
const dueCards = (cards = allCards()) => cards.filter(isDue);

// SM-2 с четырьмя кнопками, как в Anki: 0 — снова, 1 — трудно, 2 — хорошо, 3 — легко.
function nextState(p, q) {
  const t = now();
  if (q === 0) return { ivl: 0, ef: Math.max(1.3, p.ef - 0.2), reps: 0, due: t + 10 * MIN };
  let ivl;
  if (p.reps === 0) ivl = q === 3 ? 4 : 1;
  else if (p.reps === 1) ivl = q === 1 ? 2 : q === 2 ? 3 : 6;
  else ivl = Math.round(Math.max(1, p.ivl) * (q === 1 ? 1.2 : q === 2 ? p.ef : p.ef * 1.3));
  ivl = clamp(ivl, 1, 3650);
  const ef = clamp(p.ef + (q === 1 ? -0.15 : q === 3 ? 0.15 : 0), 1.3, 3.2);
  return { ivl, ef, reps: p.reps + 1, due: startOfDay(addDays(t, ivl)) + 3 * 3600e3 };
}
function grade(id, q) {
  const p = prog(id);
  const n = nextState(p, q);
  if (q === 0) { if (p.reps > 0) p.lapses++; p.wrong++; } else p.right++;
  Object.assign(p, n);
  if (!p.seen) { p.seen = now(); dayRec().nw++; }
  p.last = now();
  saveSoon();
}
// Ответ в уроке, тесте или игре: тоже двигает карточку по шкале повторений.
function practice(id, ok) {
  const p = prog(id);
  const d = dayRec();
  ok ? d.ok++ : d.bad++;
  if (ok) {
    if (!p.seen || p.due <= now()) grade(id, 2);
    else { p.right++; p.last = now(); saveSoon(); }
  } else if (p.seen && p.ivl >= 1) {
    grade(id, 0);
  } else {
    if (!p.seen) { p.seen = now(); d.nw++; }
    p.wrong++; p.due = now(); p.last = now(); saveSoon();
  }
}
function fmtIvl(ms) {
  const m = ms / MIN;
  if (m < 60) return `${Math.max(1, Math.round(m))} мин`;
  const days = Math.round(ms / DAY);
  if (days < 1) return `${Math.round(m / 60)} ч`;
  if (days < 14) return `${days} д`;
  if (days < 60) return `${Math.round(days / 7)} нед`;
  if (days < 365) return `${Math.round(days / 30)} мес`;
  return `${(days / 365).toFixed(1).replace('.0', '')} г`;
}

/* ───────────────────────── Статистика дня, XP, серия ───────────────────────── */
function dayRec(k = dayKey(now())) {
  const d = db.days[k] || (db.days[k] = { xp: 0, ok: 0, bad: 0, nw: 0 });
  d.nw = d.nw || 0;
  return d;
}
function addXp(n) {
  const d = dayRec();
  const before = d.xp;
  d.xp += n;
  if (before < db.settings.goal && d.xp >= db.settings.goal) setTimeout(() => toast('🎉 Дневная цель выполнена!'), 250);
  saveSoon();
}
function streak() {
  const d = new Date(); d.setHours(12, 0, 0, 0);
  if (!(db.days[dayKey(d)]?.xp > 0)) d.setDate(d.getDate() - 1);
  let s = 0;
  while (db.days[dayKey(d)]?.xp > 0) { s++; d.setDate(d.getDate() - 1); }
  return s;
}
function bestStreak() {
  const keys = Object.keys(db.days).filter(k => db.days[k].xp > 0).sort();
  let best = 0, run = 0, prev = null;
  for (const k of keys) {
    const t = new Date(k + 'T12:00:00').getTime();
    run = prev !== null && Math.round((t - prev) / DAY) === 1 ? run + 1 : 1;
    best = Math.max(best, run);
    prev = t;
  }
  return best;
}
const totalXp = () => Object.values(db.days).reduce((s, d) => s + (d.xp || 0), 0);

/* ───────────────────────── Проверка ответов ───────────────────────── */
function norm(s) {
  return String(s || '').toLowerCase().replace(/ё/g, 'е').replace(/[’`]/g, "'")
    .replace(/\([^)]*\)/g, ' ').replace(/[.,!?;:"«»…\-—–/]/g, ' ').replace(/\s+/g, ' ').trim();
}
// Английские и итальянские артикли не обязательны в ответе.
function stripArticles(s) { return s.replace(/^(to|a|an|the|il|lo|la|i|gli|le|un|uno|una) /, '').replace(/^(l|un)'/, ''); }
function variants(answer) {
  const set = new Set();
  const add = s => { const n = norm(s); if (n) { set.add(n); set.add(stripArticles(n)); } };
  add(answer);
  String(answer).replace(/\([^)]*\)/g, ' ').split(/[,;/]| или /).forEach(add);
  return [...set];
}
function lev(a, b) {
  if (a === b) return 0;
  const m = a.length, n = b.length;
  if (!m || !n) return m || n;
  let prev = Array.from({ length: n + 1 }, (_, j) => j);
  for (let i = 1; i <= m; i++) {
    const cur = [i];
    for (let j = 1; j <= n; j++) cur[j] = Math.min(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + (a[i - 1] === b[j - 1] ? 0 : 1));
    prev = cur;
  }
  return prev[n];
}
// Без диакритики: è→e, ї→і, й→и. Совпадение без неё засчитываем как опечатку.
const loose = s => s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/ʼ/g, "'");
function checkAnswer(input, answer) {
  const n = stripArticles(norm(input));
  if (!n) return { ok: false };
  const v = variants(answer);
  if (v.includes(n) || v.includes(norm(input))) return { ok: true };
  const ln = loose(n);
  if (v.some(x => loose(x) === ln)) return { ok: true, typo: true };
  if (db.settings.typos) {
    for (const x of v) {
      const allowed = x.length >= 8 ? 2 : x.length >= 4 ? 1 : 0;
      if (allowed && lev(ln, loose(x)) <= allowed) return { ok: true, typo: true };
    }
  }
  return { ok: false };
}

/* ───────────────────────── Озвучка, звук, вибрация ───────────────────────── */
function speechText(t) { return String(t).replace(/\s[—–-]\s/g, ', ').replace(/\//g, ' ').replace(/…/g, ' ').replace(/\([^)]*\)/g, ''); }
let voices = [];
function loadVoices() { try { voices = window.speechSynthesis ? speechSynthesis.getVoices() : []; } catch (e) { voices = []; } }
if (window.speechSynthesis) { loadVoices(); speechSynthesis.onvoiceschanged = loadVoices; }
function speak(text, lang = 'en-US', slow = false) {
  if (!text) return;
  const rate = db.settings.rate * (slow ? 0.6 : 1);
  const t = speechText(text);
  if (window.Android && Android.speak) { try { Android.speak(t, lang, rate); } catch (e) {} return; }
  if (!window.speechSynthesis) { toast('Озвучка недоступна на этом устройстве'); return; }
  try {
    speechSynthesis.cancel();
    const u = new SpeechSynthesisUtterance(t);
    u.lang = lang; u.rate = rate;
    const base = lang.slice(0, 2);
    const v = voices.find(v => v.lang.replace('_', '-') === lang) || voices.find(v => v.lang.slice(0, 2) === base);
    if (v) u.voice = v;
    speechSynthesis.speak(u);
  } catch (e) {}
}
let actx = null;
function beep(ok) {
  if (!db.settings.sound) return;
  try {
    actx = actx || new (window.AudioContext || window.webkitAudioContext)();
    const t = actx.currentTime;
    const notes = ok ? [[660, 0], [880, 0.09]] : [[220, 0], [185, 0.12]];
    for (const [f, dt] of notes) {
      const o = actx.createOscillator(), g = actx.createGain();
      o.type = ok ? 'sine' : 'triangle';
      o.frequency.value = f;
      g.gain.setValueAtTime(0.0001, t + dt);
      g.gain.exponentialRampToValueAtTime(0.12, t + dt + 0.02);
      g.gain.exponentialRampToValueAtTime(0.0001, t + dt + 0.18);
      o.connect(g).connect(actx.destination);
      o.start(t + dt); o.stop(t + dt + 0.2);
    }
  } catch (e) {}
  if (!ok) vibrate(60);
}
function vibrate(ms) {
  if (!db.settings.sound) return;
  try { if (window.Android && Android.vibrate) Android.vibrate(ms); else if (navigator.vibrate) navigator.vibrate(ms); } catch (e) {}
}

/* ───────────────────────── Общий интерфейс ───────────────────────── */
const I = {
  back: '<svg viewBox="0 0 24 24"><path d="M15 5l-7 7 7 7"/></svg>',
  close: '<svg viewBox="0 0 24 24"><path d="M6 6l12 12M18 6L6 18"/></svg>',
  spk: '<svg viewBox="0 0 24 24"><path d="M4 9v6h4l5 4V5L8 9z"/><path d="M16.5 8.5a5 5 0 0 1 0 7M19 6a8.5 8.5 0 0 1 0 12"/></svg>',
  star: '<svg viewBox="0 0 24 24"><path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z"/></svg>',
  more: '<svg viewBox="0 0 24 24"><circle cx="5" cy="12" r="1.6"/><circle cx="12" cy="12" r="1.6"/><circle cx="19" cy="12" r="1.6"/></svg>',
  plus: '<svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>',
  shuffle: '<svg viewBox="0 0 24 24"><path d="M3 7h3c5 0 7 10 12 10h3M3 17h3c2 0 3.3-1.6 4.6-3.6M14 9.6C15.2 8 16.5 7 18 7h3M18 4l3 3-3 3M18 14l3 3-3 3"/></svg>',
  play: '<svg viewBox="0 0 24 24"><path d="M7 5l12 7-12 7z"/></svg>',
  pause: '<svg viewBox="0 0 24 24"><path d="M8 5v14M16 5v14"/></svg>',
  undo: '<svg viewBox="0 0 24 24"><path d="M9 14L4 9l5-5"/><path d="M4 9h10a6 6 0 0 1 0 12h-3"/></svg>',
  search: '<svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="6.5"/><path d="M16 16l4.5 4.5"/></svg>',
  chev: '<svg viewBox="0 0 24 24"><path d="M9 5l7 7-7 7"/></svg>',
  lock: '<svg viewBox="0 0 24 24"><rect x="5" y="11" width="14" height="10" rx="2"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/></svg>',
  turtle: '<svg viewBox="0 0 24 24"><path d="M4 15c0-4 3.5-7 8-7s8 3 8 7z"/><path d="M20 13h2M6 15l-1 3M18 15l1 3M9 9l2 6M15 9l-2 6"/></svg>',
};
function hdr(title, opts = {}) {
  return `<header class="hdr">${opts.root ? '' : `<button class="ib" data-act="back" aria-label="Назад">${I.back}</button>`}
    <h1>${title}</h1><div class="hdr-r">${opts.right || ''}</div></header>`;
}
function toast(msg) {
  const t = $('#toast');
  t.textContent = msg;
  t.classList.add('on');
  clearTimeout(toast.t);
  toast.t = setTimeout(() => t.classList.remove('on'), 2400);
}
let modalCb = null;
function openModal(html, cls = '') {
  const m = $('#modal');
  m.innerHTML = `<div class="sheet ${cls}">${html}</div>`;
  m.hidden = false;
  requestAnimationFrame(() => m.classList.add('on'));
  const af = $('[autofocus]', m); if (af) setTimeout(() => af.focus(), 60);
}
function closeModal() {
  const m = $('#modal');
  m.classList.remove('on');
  m.hidden = true;
  m.innerHTML = '';
  modalCb = null;
}
function confirmBox(text, okLabel, cb, danger = true) {
  modalCb = cb;
  openModal(`<p class="mtext">${text}</p><div class="row2"><button class="btn ghost" data-act="modal-close">Отмена</button>
    <button class="btn ${danger ? 'danger' : ''}" data-act="modal-ok">${okLabel}</button></div>`);
}
function lvlDot(id) { const l = level(id); return `<i class="lvl-dot l${l}" title="${LEVELS[l]}"></i>`; }
function masteryBar(cards) {
  const n = cards.length || 1;
  const cnt = [0, 0, 0, 0, 0];
  for (const c of cards) cnt[level(c.id)]++;
  return `<div class="mbar">${[4, 3, 2, 1].map(l => cnt[l] ? `<i class="l${l}" style="width:${cnt[l] / n * 100}%"></i>` : '').join('')}</div>`;
}
function spkBtn(text, lang, cls = '') {
  return `<button class="ib spk ${cls}" data-act="speak" data-text="${esc(text)}" data-lang="${esc(lang)}" aria-label="Озвучить">${I.spk}</button>`;
}

/* ───────────────────────── Навигация ───────────────────────── */
const screens = {};
const after = {};
const TABS = ['home', 'decks', 'lessons', 'stats', 'more'];
let stack = [{ name: 'home' }];
let timers = [];
const cur = () => stack[stack.length - 1];
let tickT = 0;
function stopTimers() { timers.forEach(t => clearInterval(t)); timers = []; clearInterval(tickT); if (window.speechSynthesis) try { speechSynthesis.cancel(); } catch (e) {} }
// Один повторяющийся таймер экрана: повторный рендер не плодит новые интервалы.
function setTick(fn, ms) { clearInterval(tickT); tickT = setInterval(fn, ms); }

function render(keep = false) {
  const c = cur();
  const st = keep ? view.scrollTop : 0;
  view.innerHTML = (screens[c.name] || screens.home)(c);
  const isTab = TABS.includes(c.name);
  document.body.classList.toggle('no-tabs', !isTab);
  document.body.dataset.screen = c.name;
  $$('#tabs button').forEach(b => b.classList.toggle('on', isTab && b.dataset.tab === c.name));
  view.scrollTop = st;
  if (after[c.name]) after[c.name](c, keep);
  if (!keep) { const af = $('[autofocus]', view); if (af) af.focus({ preventScroll: true }); }
}
function go(name, params = {}) { stopTimers(); stack.push({ name, ...params }); render(); }
function replace(name, params = {}) { stopTimers(); stack[stack.length - 1] = { name, ...params }; render(); }
function tab(name) { stopTimers(); stack = [{ name }]; render(); }
function back(force) {
  if (!$('#modal').hidden) { closeModal(); return true; }
  const c = cur();
  if (!force && c.name === 'run' && S && !S.finished && S.ok + S.bad > 2) {
    confirmBox('Выйти из занятия? Ответы уже засчитаны в повторение, но урок не будет отмечен пройденным.', 'Выйти', () => back(true));
    return true;
  }
  if (stack.length > 1) { stopTimers(); stack.pop(); render(); return true; }
  if (c.name !== 'home') { tab('home'); return true; }
  return false;
}
window.appBack = () => back();

/* ───────────────────────── Главная ───────────────────────── */
function greet() {
  const h = new Date().getHours();
  return h < 5 ? 'Доброй ночи' : h < 12 ? 'Доброе утро' : h < 18 ? 'Добрый день' : 'Добрый вечер';
}
function ring(pct, size = 64, stroke = 7) {
  const r = (size - stroke) / 2, c = 2 * Math.PI * r;
  return `<svg class="ring" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
    <circle cx="${size / 2}" cy="${size / 2}" r="${r}" stroke-width="${stroke}" class="ring-bg"/>
    <circle cx="${size / 2}" cy="${size / 2}" r="${r}" stroke-width="${stroke}" class="ring-fg"
      stroke-dasharray="${c}" stroke-dashoffset="${c * (1 - clamp(pct, 0, 1))}" transform="rotate(-90 ${size / 2} ${size / 2})"/></svg>`;
}
function hashStr(s) { let h = 2166136261; for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 16777619); } return h >>> 0; }
function wordOfDay() {
  const all = allCards().filter(c => c.ex);
  if (!all.length) return null;
  return all[hashStr(dayKey(now())) % all.length];
}
function activeDeck() {
  const d = deckById(db.settings.active[curLang()]);
  return d && d.lang === curLang() && d.cards.length >= 3 ? d : langDecks().find(x => x.cards.length >= 3) || null;
}
function setActive(id) { const d = deckById(id); if (d) { db.settings.active[d.lang] = id; saveSoon(); } }
const deckDone = d => lessonNodes(d).every(n => nodeDone(d, n));
function nextDeckAfter(d) {
  const list = langDecks().filter(x => x.cards.length >= 3);
  const i = list.indexOf(d);
  return list.slice(i + 1).find(x => !deckDone(x)) || list.find(x => x !== d && !deckDone(x)) || null;
}
// Набор для «Продолжить курс»: текущий, а если он пройден — следующий непройденный.
function courseDeck() { const d = activeDeck(); return d && deckDone(d) ? nextDeckAfter(d) || d : d; }
function nextDue() {
  let min = Infinity;
  for (const c of allCards()) { const p = db.prog[c.id]; if (p && p.seen && p.due > now()) min = Math.min(min, p.due); }
  return min;
}

screens.home = () => {
  const st = streak(), d = dayRec(), goal = db.settings.goal;
  const due = dueCards().length;
  const ad = courseDeck();
  let lessonCard = '';
  if (ad) {
    const nodes = lessonNodes(ad);
    const ni = nodes.findIndex(n => !nodeDone(ad, n));
    const done = nodes.filter(n => nodeDone(ad, n)).length;
    const n = nodes[ni];
    lessonCard = `<div class="card cta">
      <div class="cta-ico">${esc(ad.emoji || '📘')}</div>
      <div class="cta-body"><div class="cta-t">${n ? esc(n.title) : 'Все уроки пройдены!'}</div>
        <div class="cta-s">${esc(ad.title)} · ${done}/${nodes.length} уроков</div>
        <div class="pbar sm"><i style="width:${nodes.length ? done / nodes.length * 100 : 0}%"></i></div></div>
      ${n ? `<button class="btn" data-act="lesson-start" data-deck="${esc(ad.id)}" data-n="${ni}">Начать</button>`
        : `<button class="btn ghost" data-act="tab" data-tab="lessons">Выбрать</button>`}
    </div>`;
  }
  const nd = nextDue();
  const w = wordOfDay();
  const tips = window.MNEMO_TIPS || [];
  const tip = tips.length ? tips[hashStr('t' + dayKey(now())) % tips.length] : '';
  const seenN = smartCards('@seen').length;
  const qdeck = seenN >= 8 ? '@seen' : '@all';
  return `<div class="pad home">
    <div class="home-top">
      <div><div class="hello">${greet()}!</div><div class="logo">Мнемо</div></div>
      <div class="streak ${d.xp > 0 ? 'on' : ''}" title="Серия дней">🔥<b>${st}</b></div>
    </div>
    ${langBar()}
    <div class="card goal">
      <div class="goal-ring">${ring(d.xp / goal, 72, 8)}<span>${Math.min(100, Math.round(d.xp / goal * 100))}%</span></div>
      <div><div class="cta-t">Цель дня</div><div class="cta-s">${d.xp} из ${goal} XP · ${d.ok + d.bad} ответов сегодня</div>
      <div class="cta-s">${d.xp >= goal ? 'Отлично! Цель выполнена 🎉' : 'Урок ≈ 150–250 XP, повторение — 10 XP за слово'}</div></div>
    </div>
    <div class="card cta ${due ? 'hot' : ''}">
      <div class="cta-ico">🔁</div>
      <div class="cta-body"><div class="cta-t">Повторение</div>
        <div class="cta-s">${due ? `${nCards(due)} ждут повторения` : nd < Infinity ? `Всё повторено. Следующее через ${fmtIvl(nd - now())}` : 'Пройдите первый урок — слова появятся здесь'}</div></div>
      ${due ? `<button class="btn" data-act="mode" data-mode="review" data-deck="@due">Повторить</button>` : ''}
    </div>
    ${lessonCard}
    <h2 class="sec">Быстрая тренировка</h2>
    <div class="quick">
      <button class="qt" data-act="mode" data-mode="sprint" data-deck="${qdeck}"><b>⚡</b><span>Спринт</span><small>60 секунд</small></button>
      <button class="qt" data-act="mode" data-mode="match" data-deck="${qdeck}"><b>🧩</b><span>Подбор</span><small>на время</small></button>
      <button class="qt" data-act="open-deck" data-id="@hard"><b>🎯</b><span>Трудные</span><small>${nWords(smartCards('@hard').length)}</small></button>
      <button class="qt" data-act="mode" data-mode="learn" data-deck="${qdeck}"><b>🎓</b><span>Смешанное</span><small>заучивание</small></button>
    </div>
    ${w ? `<h2 class="sec">Слово дня</h2>
    <div class="card wotd">
      <div class="wotd-t">${esc(w.t)} ${spkBtn(w.t, langsOf(w).fl)}</div>
      <div class="wotd-d">${esc(w.d)}</div>
      ${w.ex ? `<div class="ex-line">${esc(w.ex)}<br><span>${esc(w.exT)}</span></div>` : ''}
      <button class="lnk" data-act="star" data-id="${esc(w.id)}">${w.star ? '★ В избранном' : '☆ В избранное'}</button>
    </div>` : ''}
    ${tip ? `<div class="tip">💡 ${esc(tip)}</div>` : ''}
  </div>`;
};

/* ───────────────────────── Наборы ───────────────────────── */
function deckRow(d) {
  const cards = d.cards;
  const due = dueCards(cards).length;
  const known = cards.filter(c => level(c.id) >= 2).length;
  return `<button class="deck" data-act="open-deck" data-id="${esc(d.id)}">
    <span class="deck-e">${esc(d.emoji || '📘')}</span>
    <span class="deck-b"><span class="deck-t">${esc(d.title)}</span>
      <span class="deck-s">${nWords(cards.length)}${d.level ? ' · ' + esc(d.level) : ''} · знаю ${known}</span>
      ${masteryBar(cards)}</span>
    ${due ? `<span class="badge">${due}</span>` : ''}
  </button>`;
}
function langBar() {
  const L = curLang();
  return `<div class="langbar">${langList().map(l => `<button class="lb ${l === L ? 'on' : ''}" data-act="lang" data-l="${l}"><b>${LANGS[l].flag}</b><span>${LANGS[l].name}</span></button>`).join('')}</div>`;
}
function byLevel(decks) {
  const m = new Map();
  for (const d of decks) { const k = d.level || ''; if (!m.has(k)) m.set(k, []); m.get(k).push(d); }
  return [...m.entries()].sort((a, b) => levelRank(a[0]) - levelRank(b[0]));
}
function levelOpen(lv) {
  const v = db.settings.open[curLang() + ':' + lv];
  if (v !== undefined) return v;
  const ad = activeDeck();
  return !!ad && (ad.level || '') === lv;
}
function levelHead(lv, list, act = 'lvl-toggle', open = levelOpen(lv)) {
  const cards = list.flatMap(d => d.cards);
  const known = cards.filter(c => level(c.id) >= 2).length;
  return `<button class="lvl-h ${open ? 'open' : ''}" data-act="${act}" data-l="${esc(lv)}">
    <span class="lvl-b">${esc(lv || '—')}</span>
    <span class="lvl-t"><b>${LEVEL_NAMES[lv] || esc(lv)}</b><small>${list.length} ${plural(list.length, 'набор', 'набора', 'наборов')} · ${nWords(cards.length)} · знаю ${known}</small>${masteryBar(cards)}</span>${I.chev}</button>`;
}
function decksHtml(q) {
  const ds = langDecks();
  if (q) {
    const list = ds.filter(d => d.title.toLowerCase().includes(q));
    return (list.map(deckRow).join('') || '') + wordSearch(q) || '<p class="empty">Ничего не найдено</p>';
  }
  const mine = ds.filter(d => !d.builtin);
  let h = mine.length ? `<h2 class="sec">Мои наборы</h2>${mine.map(deckRow).join('')}` : '';
  for (const [lv, list] of byLevel(ds.filter(d => d.builtin))) {
    const open = levelOpen(lv);
    h += levelHead(lv, list, 'lvl-toggle', open) + (open ? `<div class="lvl-list">${list.map(deckRow).join('')}</div>` : '');
  }
  return h || `<p class="empty">Для этого языка пока нет наборов. Создайте свой или импортируйте список слов.</p>`;
}
screens.decks = c => {
  const q = (c.q || '').toLowerCase();
  const smart = Object.keys(SMART).map(id => `<button class="chip big" data-act="open-deck" data-id="${id}">${SMART[id].emoji} ${SMART[id].title} <small>${smartCards(id).length}</small></button>`).join('');
  return `${hdr('Наборы', { root: true, right: `<button class="ib" data-act="deck-add" aria-label="Добавить">${I.plus}</button>` })}
  <div class="pad">
    ${langBar()}
    <label class="search">${I.search}<input type="search" placeholder="Поиск по наборам и словам" value="${esc(c.q || '')}" data-in="dsearch"></label>
    <div class="chips scroll">${smart}</div>
    <div id="dlist">${decksHtml(q)}</div>
    <button class="btn ghost wide" data-act="deck-add">＋ Новый набор или импорт</button>
  </div>`;
};
function wordSearch(q) {
  const hits = allCards().filter(x => x.t.toLowerCase().includes(q) || x.d.toLowerCase().includes(q)).slice(0, 40);
  if (!hits.length) return '';
  return `<h2 class="sec">Слова</h2>${hits.map(cardRow).join('')}`;
}

const MODES = [
  ['flash', '🃏', 'Карточки', 'Переворачивайте и сортируйте'],
  ['learn', '🎓', 'Заучивание', 'Выбор → письмо, до полного знания'],
  ['review', '🔁', 'Повторение', 'Интервальные повторения'],
  ['test', '📝', 'Тест', 'Проверка с оценкой'],
  ['match', '🧩', 'Подбор', 'Найдите пары на время'],
  ['write', '✍️', 'Письмо', 'Пишите перевод сами'],
  ['listen', '🎧', 'Диктант', 'Слушайте и записывайте'],
  ['scramble', '🔤', 'Собери слово', 'Слово из букв'],
  ['sprint', '⚡', 'Спринт', 'Верно или нет за 60 с'],
  ['lessons', '🗺️', 'Уроки', 'Пошаговый курс по набору'],
];
const DIRS = [['td', 'Слово → перевод'], ['dt', 'Перевод → слово'], ['mix', 'Вперемешку']];
function dirSeg() {
  return `<div class="seg">${DIRS.map(([k, l]) => `<button class="${db.settings.dir === k ? 'on' : ''}" data-act="set" data-k="dir" data-v="${k}">${l}</button>`).join('')}</div>`;
}
function cardRow(c) {
  const { fl } = langsOf(c);
  return `<div class="crow" data-act="edit-card" data-id="${esc(c.id)}">
    ${lvlDot(c.id)}
    <div class="crow-b"><div class="crow-t">${esc(c.t)}</div><div class="crow-d">${esc(c.d)}</div></div>
    ${spkBtn(c.t, fl)}
    <button class="ib star ${c.star ? 'on' : ''}" data-act="star" data-id="${esc(c.id)}" aria-label="Избранное">${I.star}</button>
  </div>`;
}
screens.deck = c => {
  const d = getDeck(c.id);
  if (!d) return hdr('Набор') + '<p class="empty">Набор не найден</p>';
  const cards = d.cards;
  const f = c.f || 'all';
  const filtered = cards.filter(x => f === 'all' || (f === 'star' && x.star) || (f === 'new' && level(x.id) === 0) || (f === 'learn' && level(x.id) > 0 && level(x.id) < 3) || (f === 'known' && level(x.id) >= 3));
  const cnt = [0, 0, 0, 0, 0]; cards.forEach(x => cnt[level(x.id)]++);
  const due = dueCards(cards).length;
  const modes = MODES.filter(m => !(d.smart && m[0] === 'lessons'));
  return `${hdr(`${esc(d.emoji || '📘')} ${esc(d.title)}`, { right: d.smart ? '' : `<button class="ib" data-act="deck-menu" data-id="${esc(d.id)}" aria-label="Меню">${I.more}</button>` })}
  <div class="pad">
    ${d.desc ? `<p class="desc">${esc(d.desc)}</p>` : ''}
    <div class="stats3">
      <div><b>${cards.length}</b><span>${plural(cards.length, 'слово', 'слова', 'слов')}</span></div>
      <div><b>${cnt[3] + cnt[4]}</b><span>знаю</span></div>
      <div><b>${due}</b><span>к повторению</span></div>
    </div>
    ${masteryBar(cards)}
    <div class="legend">${LEVELS.map((l, i) => `<span><i class="lvl-dot l${i}"></i>${l} ${cnt[i]}</span>`).join('')}</div>
    ${cards.length ? `${dirSeg()}
    <div class="modes">${modes.map(([k, e, t, s]) => `<button class="mode" data-act="mode" data-mode="${k}" data-deck="${esc(d.id)}"><b>${e}</b><span>${t}</span><small>${s}</small></button>`).join('')}</div>` : `<p class="empty">${d.smart ? 'Здесь пока пусто.' : 'В наборе пока нет карточек.'}</p>`}
    <div class="list-h"><h2 class="sec">Карточки</h2>${d.smart ? '' : `<button class="btn sm" data-act="card-new" data-deck="${esc(d.id)}">＋ Карточка</button>`}</div>
    <div class="chips">${[['all', 'Все'], ['star', '⭐'], ['new', 'Новые'], ['learn', 'Изучаю'], ['known', 'Знаю']].map(([k, l]) => `<button class="chip ${f === k ? 'on' : ''}" data-act="deck-filter" data-f="${k}">${l}</button>`).join('')}</div>
    ${filtered.map(cardRow).join('') || '<p class="empty">Нет карточек</p>'}
  </div>`;
};

const VOICE_LANGS = [['en-US', 'Английский (США)'], ['en-GB', 'Английский (Брит.)'], ['ru-RU', 'Русский'], ['de-DE', 'Немецкий'], ['fr-FR', 'Французский'], ['es-ES', 'Испанский'], ['it-IT', 'Итальянский'], ['pt-BR', 'Португальский'], ['tr-TR', 'Турецкий'], ['pl-PL', 'Польский'], ['uk-UA', 'Украинский'], ['zh-CN', 'Китайский'], ['ja-JP', 'Японский'], ['ko-KR', 'Корейский'], ['ar-SA', 'Арабский'], ['he-IL', 'Иврит'], ['sr-RS', 'Сербский'], ['cs-CZ', 'Чешский'], ['fi-FI', 'Финский'], ['el-GR', 'Греческий']];
const langSel = (name, v) => `<select name="${name}">${VOICE_LANGS.map(([k, l]) => `<option value="${k}" ${k === v ? 'selected' : ''}>${l}</option>`).join('')}</select>`;
function deckForm(d) {
  d = d || { title: '', emoji: '📘', desc: '', front: LANGS[curLang()].front, back: 'ru-RU' };
  openModal(`<h3>${d.id ? 'Набор' : 'Новый набор'}</h3>
    <form class="form" data-form="deck" data-id="${esc(d.id || '')}">
      <div class="row-e"><input name="emoji" value="${esc(d.emoji || '📘')}" maxlength="4" class="emoji-in"><input name="title" placeholder="Название" value="${esc(d.title)}" required ${d.id ? '' : 'autofocus'}></div>
      <textarea name="desc" rows="2" placeholder="Описание (необязательно)">${esc(d.desc || '')}</textarea>
      <label>Язык слов (лицевая сторона)${langSel('front', d.front)}</label>
      <label>Язык перевода (оборот)${langSel('back', d.back)}</label>
      <div class="row2"><button type="button" class="btn ghost" data-act="modal-close">Отмена</button><button class="btn">${d.id ? 'Сохранить' : 'Создать'}</button></div>
    </form>`);
}
function cardForm(c, deckId) {
  const isNew = !c;
  c = c || { t: '', d: '', ex: '', exT: '', note: '' };
  const p = !isNew && db.prog[c.id];
  openModal(`<h3>${isNew ? 'Новая карточка' : 'Карточка'}</h3>
    <form class="form" data-form="card" data-id="${esc(c.id || '')}" data-deck="${esc(deckId || '')}">
      <input name="t" placeholder="Слово или фраза" value="${esc(c.t)}" required ${isNew ? 'autofocus' : ''}>
      <input name="d" placeholder="Перевод" value="${esc(c.d)}" required>
      <input name="ex" placeholder="Пример (необязательно)" value="${esc(c.ex)}">
      <input name="exT" placeholder="Перевод примера" value="${esc(c.exT)}">
      <textarea name="note" rows="2" placeholder="Подсказка-ассоциация: как запомнить">${esc(c.note || '')}</textarea>
      ${p ? `<p class="hint">Уровень: ${LEVELS[level(c.id)]} · верно ${p.right}, ошибок ${p.wrong}${p.seen ? ` · повтор через ${p.due > now() ? fmtIvl(p.due - now()) : 'сейчас'}` : ''}</p>` : ''}
      <div class="row2">${isNew ? `<button type="button" class="btn ghost" data-act="modal-close">Закрыть</button>` : `<button type="button" class="btn ghost danger-t" data-act="card-del" data-id="${esc(c.id)}">Удалить</button>`}
      <button class="btn">${isNew ? 'Добавить' : 'Сохранить'}</button></div>
      ${isNew ? '<p class="hint">После добавления форма останется открытой для следующей карточки.</p>' : ''}
    </form>`);
}

/* ───────────────────────── Импорт и экспорт ───────────────────────── */
const SEPS = [['auto', 'Авто'], ['tab', 'Табуляция'], ['dash', 'Тире'], ['semi', ';'], ['comma', ','], ['pipe', '|'], ['eq', '=']];
function splitLine(line, sep) {
  const bySep = { tab: '\t', semi: ';', comma: ',', pipe: '|', eq: '=' };
  if (sep === 'dash') { const m = line.match(/\s+[-—–]\s+|[—–]/); return m ? [line.slice(0, m.index), line.slice(m.index + m[0].length)] : null; }
  if (sep !== 'auto') { const p = line.split(bySep[sep]); return p.length > 1 ? p : null; }
  if (line.includes('\t')) return line.split('\t');
  if (line.includes('|')) return line.split('|');
  if (/\s[-—–]\s|[—–]/.test(line)) return splitLine(line, 'dash');
  if (line.includes(';')) return line.split(';');
  if (line.includes('=')) return line.split('=');
  if (line.includes(',')) { const i = line.indexOf(','); return [line.slice(0, i), line.slice(i + 1)]; }
  if (/\s-\s?|-\s/.test(line)) { const i = line.indexOf('-'); return [line.slice(0, i), line.slice(i + 1)]; }
  return null;
}
function parseImport(text, sep, swap) {
  const out = [];
  for (const raw of String(text).split(/\r?\n/)) {
    const line = raw.trim();
    if (!line || line.startsWith('#')) continue;
    const p = splitLine(line, sep);
    if (!p) continue;
    let [t, d, ex, exT, note] = p.map(s => (s || '').trim());
    if (swap) [t, d] = [d, t];
    if (t && d) out.push({ t, d, ex: ex || '', exT: exT || '', note: note || '' });
  }
  return out;
}
screens.import = c => {
  const d = c.deck ? deckById(c.deck) : null;
  return `${hdr(d ? `Импорт в «${esc(d.title)}»` : 'Импорт слов')}
  <div class="pad form">
    <p class="desc">По одной карточке на строку: <b>слово - перевод</b>. Подходит экспорт из Quizlet (табуляция), Excel, заметок. Можно добавить пример и его перевод через «|».</p>
    ${d ? '' : `<input id="imp-title" placeholder="Название нового набора" value="${esc(c.title || '')}">`}
    <textarea id="imp-text" rows="10" placeholder="apple - яблоко&#10;pear - груша&#10;to run — бегать">${esc(c.text || '')}</textarea>
    <label class="file-btn btn ghost">📄 Загрузить из файла (.txt, .csv)<input type="file" accept=".txt,.csv,.tsv,text/*" data-file="import"></label>
    <div class="lbl">Разделитель</div>
    <div class="chips">${SEPS.map(([k, l]) => `<button class="chip ${(c.sep || 'auto') === k ? 'on' : ''}" data-act="imp-sep" data-v="${k}">${l}</button>`).join('')}</div>
    <label class="tgl"><input type="checkbox" id="imp-swap" ${c.swap ? 'checked' : ''}> Поменять стороны местами</label>
    ${d ? '' : `<div class="row2"><label>Язык слов${langSel('imp-front', c.front || LANGS[curLang()].front)}</label><label>Язык перевода${langSel('imp-back', c.back || 'ru-RU')}</label></div>`}
    <div id="imp-prev" class="imp-prev"></div>
    <button class="btn wide" data-act="imp-do">Импортировать</button>
  </div>`;
};
after.import = c => { updateImportPreview(c); };
function readImportForm(c) {
  c.text = $('#imp-text')?.value || '';
  c.title = $('#imp-title')?.value || c.title;
  c.swap = !!$('#imp-swap')?.checked;
  const f = $('select[name="imp-front"]'), b = $('select[name="imp-back"]');
  if (f) c.front = f.value;
  if (b) c.back = b.value;
}
function updateImportPreview(c) {
  readImportForm(c);
  const items = parseImport(c.text, c.sep || 'auto', c.swap);
  const el = $('#imp-prev');
  if (!el) return;
  el.innerHTML = c.text.trim() ? `<div class="lbl">Распознано: ${nCards(items.length)}</div>` +
    items.slice(0, 6).map(x => `<div class="crow sm"><div class="crow-b"><div class="crow-t">${esc(x.t)}</div><div class="crow-d">${esc(x.d)}</div></div></div>`).join('') +
    (items.length > 6 ? `<div class="hint">…и ещё ${items.length - 6}</div>` : '') : '';
}
function exportDeckText(d) {
  return d.cards.map(c => [c.t, c.d, c.ex, c.exT].filter((x, i) => i < 2 || x).join('\t')).join('\n');
}
function shareText(text, title) {
  if (window.Android && Android.share) { Android.share(text, title || 'Мнемо'); return; }
  if (navigator.share) { navigator.share({ title, text }).catch(() => {}); return; }
  copyText(text);
}
function copyText(text) {
  const done = () => toast('Скопировано в буфер обмена');
  if (navigator.clipboard && window.isSecureContext) { navigator.clipboard.writeText(text).then(done, () => fallbackCopy(text, done)); }
  else fallbackCopy(text, done);
}
function fallbackCopy(text, done) {
  const ta = document.createElement('textarea');
  ta.value = text; ta.style.position = 'fixed'; ta.style.opacity = '0';
  document.body.appendChild(ta); ta.select();
  try { document.execCommand('copy'); done(); } catch (e) { toast('Не удалось скопировать'); }
  ta.remove();
}
function saveFile(name, text, mime = 'application/json') {
  if (window.Android && Android.saveFile) {
    const res = Android.saveFile(name, text);
    toast(res ? `Сохранено: ${res}` : 'Не удалось сохранить файл');
    return;
  }
  const a = document.createElement('a');
  a.href = URL.createObjectURL(new Blob([text], { type: mime }));
  a.download = name;
  document.body.appendChild(a); a.click(); a.remove();
  setTimeout(() => URL.revokeObjectURL(a.href), 4000);
  toast('Файл сохранён');
}

/* ───────────────────────── Уроки ───────────────────────── */
function lessonNodes(deck) {
  const L = db.settings.lessonSize;
  const cards = deck.cards;
  const groups = [];
  for (let i = 0; i < cards.length; i += L) groups.push(cards.slice(i, i + L));
  if (groups.length > 1 && groups[groups.length - 1].length < 3) groups[groups.length - 2].push(...groups.pop());
  const nodes = [];
  let unit = [], u = 1;
  groups.forEach((g, gi) => {
    nodes.push({ kind: 'learn', cards: g, unit: u, title: `Урок ${gi + 1}: ${g.slice(0, 3).map(x => x.t).join(', ')}${g.length > 3 ? '…' : ''}` });
    unit.push(...g);
    if ((gi + 1) % 3 === 0 || gi === groups.length - 1) {
      if (groups.length > 1 && unit.length >= 6) nodes.push({ kind: 'review', cards: unit, unit: u, title: `Повторение раздела ${u}` });
      unit = []; u++;
    }
  });
  if (cards.length >= 12) nodes.push({ kind: 'exam', cards, unit: u, title: 'Итоговый экзамен по набору' });
  return nodes;
}
const nodeKey = n => `${n.kind}:${n.cards[0]?.id}:${n.cards.length}`;
const nodeDone = (deck, n) => !!(db.lessons[deck.id] || {})[nodeKey(n)];
const nodeStars = (deck, n) => (db.lessons[deck.id] || {})[nodeKey(n)] || 0;

screens.lessons = () => {
  const deck = activeDeck();
  if (!deck) return `${hdr('Уроки', { root: true })}<div class="pad">${langBar()}<p class="empty">Добавьте набор хотя бы из 3 слов — и из него соберутся уроки.</p></div>`;
  const nodes = lessonNodes(deck);
  const firstOpen = nodes.findIndex(n => !nodeDone(deck, n));
  let html = '', lastUnit = 0;
  nodes.forEach((n, i) => {
    if (n.unit !== lastUnit && n.kind !== 'exam') {
      lastUnit = n.unit;
      const un = nodes.filter(x => x.unit === n.unit);
      const ud = un.filter(x => nodeDone(deck, x)).length;
      html += `<div class="unit"><b>Раздел ${n.unit}</b><span>${ud}/${un.length}</span></div>`;
    }
    const done = nodeDone(deck, n), stars = nodeStars(deck, n);
    const state = done ? 'done' : i === firstOpen ? 'cur' : 'lock';
    const off = Math.round(Math.sin(i * 0.9) * 70);
    const ico = n.kind === 'exam' ? '🏆' : n.kind === 'review' ? '🔁' : done ? '✓' : i === firstOpen ? '▶' : (i + 1);
    html += `<div class="node-wrap ${i === firstOpen ? 'has-lbl' : ''}" style="transform:translateX(${off}px)">
      <button class="node ${state} ${n.kind}" data-act="node" data-n="${i}">${state === 'lock' && n.kind === 'learn' ? `<small>${ico}</small>` : ico}</button>
      <div class="stars">${done ? '★'.repeat(stars) + '☆'.repeat(3 - stars) : ''}</div>
      ${i === firstOpen ? '<div class="node-lbl">Начать</div>' : ''}
    </div>`;
  });
  const doneN = nodes.filter(n => nodeDone(deck, n)).length;
  const nx = doneN === nodes.length ? nextDeckAfter(deck) : null;
  const pos = langDecks().filter(x => x.cards.length >= 3);
  return `${hdr('Уроки', { root: true })}
  <div class="pad">
    ${langBar()}
    <button class="card path-head" data-act="pick-deck"><div class="cta-ico">${esc(deck.emoji || '📘')}</div><div class="cta-body"><div class="cta-t">${deck.level ? `<span class="lvl-tag">${esc(deck.level)}</span> ` : ''}${esc(deck.title)}</div>
      <div class="cta-s">${doneN} из ${nodes.length} уроков · ${nWords(deck.cards.length)} · набор ${pos.indexOf(deck) + 1} из ${pos.length}</div><div class="pbar sm"><i style="width:${doneN / nodes.length * 100}%"></i></div></div><span class="lnk">Сменить</span></button>
    <p class="hint">Курс идёт от A0 к C1: пройдите уроки набора — и переходите к следующему. Каждый урок знакомит с ${db.settings.lessonSize} словами и закрепляет их 7 видами упражнений.</p>
    <div class="path">${html}</div>
    ${nx ? `<div class="card cta"><div class="cta-ico">${esc(nx.emoji || '📘')}</div><div class="cta-body"><div class="cta-t">Набор пройден! Дальше:</div><div class="cta-s">${esc(nx.level)} · ${esc(nx.title)}</div></div><button class="btn" data-act="set-active" data-id="${esc(nx.id)}">Перейти</button></div>` : ''}
  </div>`;
};
function deckPicker() {
  const ad = activeDeck();
  let h = '';
  for (const [lv, list] of byLevel(langDecks().filter(d => d.cards.length >= 3))) {
    h += `<div class="pick-lv">${esc(lv || 'Мои')} · ${LEVEL_NAMES[lv] || ''}</div>` + list.map(d => {
      const nodes = lessonNodes(d), dn = nodes.filter(n => nodeDone(d, n)).length;
      return `<button class="mrow ${ad && d.id === ad.id ? 'on' : ''}" data-act="set-active" data-id="${esc(d.id)}"><b>${esc(d.emoji || '📘')}</b><span>${esc(d.title)}<small>${nWords(d.cards.length)} · уроков ${dn}/${nodes.length}${dn === nodes.length ? ' ✓' : ''}</small></span></button>`;
    }).join('');
  }
  openModal(`<h3>Выберите набор для уроков</h3>${h}`);
  const on = $('#modal .mrow.on'); if (on) on.scrollIntoView({ block: 'center' });
}
function nodeSheet(deck, i) {
  const n = lessonNodes(deck)[i];
  if (!n) return;
  const kindTxt = n.kind === 'learn' ? 'Новые слова: знакомство и закрепление' : n.kind === 'review' ? 'Повторение всех слов раздела' : 'Экзамен: случайные слова из всего набора';
  const shown = n.kind === 'exam' ? '' : `<div class="words">${n.cards.map(c => `<div class="crow sm">${lvlDot(c.id)}<div class="crow-b"><div class="crow-t">${esc(c.t)}</div><div class="crow-d">${esc(c.d)}</div></div></div>`).join('')}</div>`;
  const stars = nodeStars(deck, n);
  openModal(`<h3>${esc(n.title)}</h3><p class="hint">${kindTxt}${stars ? ` · лучший результат ${'★'.repeat(stars)}` : ''}</p>${shown}
    <button class="btn wide" data-act="lesson-start" data-deck="${esc(deck.id)}" data-n="${i}">${nodeDone(deck, n) ? 'Пройти ещё раз' : 'Начать урок'}</button>`);
}

function clozeOf(c) {
  if (!c.ex) return null;
  const t = c.t.trim();
  if (t.length < 2 || /[—/…]/.test(t)) return null;
  const exl = c.ex.toLowerCase(), tl = t.toLowerCase();
  const isL = ch => !!ch && /[\p{L}\p{N}']/u.test(ch);
  if (t.includes(' ')) {
    const i = exl.indexOf(tl);
    if (i >= 0 && !isL(c.ex[i - 1]) && !isL(c.ex[i + t.length])) return { before: c.ex.slice(0, i), word: c.ex.slice(i, i + t.length), after: c.ex.slice(i + t.length) };
    return null;
  }
  const parts = c.ex.split(/([\p{L}\p{N}']+)/u);
  const stem = tl.slice(0, Math.max(3, tl.length - 2));
  for (let k = 1; k < parts.length; k += 2) {
    const w = parts[k].toLowerCase();
    if (w === tl || (tl.length >= 4 && w.startsWith(stem) && w.length <= tl.length + 3)) {
      return { before: parts.slice(0, k).join(''), word: parts[k], after: parts.slice(k + 1).join('') };
    }
  }
  return null;
}
const letterCount = t => [...t].filter(ch => /[\p{L}\p{N}]/u.test(ch)).length;
function variedEx(c, i) {
  const opts = ['mcRev', 'listen', 'tf'];
  if (letterCount(c.t) <= 16) opts.push('scramble');
  if (clozeOf(c)) opts.push('cloze', 'cloze');
  return { type: opts[(i + rnd(opts.length)) % opts.length], c };
}
function buildLesson(n) {
  const W = n.cards;
  const ex = [];
  if (n.kind === 'learn') {
    W.forEach(c => { ex.push({ type: 'intro', c }); ex.push({ type: 'mc', c, dir: 'td' }); });
    if (W.length >= 3) ex.push({ type: 'match', cards: shuffle(W).slice(0, 5) });
    shuffle(W).forEach((c, i) => ex.push(variedEx(c, i)));
    shuffle(W).slice(0, Math.min(4, W.length)).forEach((c, i) => ex.push({ type: i % 3 === 2 && letterCount(c.t) <= 14 ? 'dict' : 'type', c, dir: 'dt' }));
  } else {
    const pool = n.kind === 'exam' ? shuffle(W).slice(0, 20) : shuffle(W);
    for (let i = 0; i + 3 <= Math.min(pool.length, 10); i += 5) ex.push({ type: 'match', cards: pool.slice(i, i + 5) });
    shuffle(pool).slice(0, 8).forEach((c, i) => ex.push(variedEx(c, i)));
    shuffle(pool).slice(0, 6).forEach((c, i) => ex.push({ type: i % 2 ? 'scramble' : 'type', c, dir: 'dt' }));
    for (let i = ex.length - 1; i > 0; i--) if (ex[i].type === 'scramble' && letterCount(ex[i].c.t) > 16) ex[i].type = 'type';
  }
  return ex;
}

/* ───────────────────────── Движок упражнений ───────────────────────── */
let S = null;

function qa(c, dir) {
  const { fl, bl } = langsOf(c);
  if (dir === 'mix') dir = Math.random() < 0.5 ? 'td' : 'dt';
  return dir === 'dt' ? { q: c.d, a: c.t, ql: bl, al: fl, dir } : { q: c.t, a: c.d, ql: fl, al: bl, dir };
}
function distractors(c, n, field, pool) {
  const seen = new Set([norm(c[field])]);
  const out = [];
  const tryFrom = list => {
    const cands = shuffle(list.filter(x => x.id !== c.id));
    const L = c[field].length;
    cands.sort((a, b) => Math.abs(a[field].length - L) - Math.abs(b[field].length - L) + (Math.random() - 0.5) * 8);
    for (const x of cands) {
      if (out.length >= n) break;
      const k = norm(x[field]);
      if (k && !seen.has(k)) { seen.add(k); out.push(x[field]); }
    }
  };
  tryFrom(pool || []);
  if (out.length < n) {
    const { fl } = langsOf(c);
    tryFrom(allCards().filter(x => langsOf(x).fl === fl));
  }
  return out;
}
function prep(ex) {
  if (ex.ready) return ex;
  ex.ready = true;
  const c = ex.c;
  const pool = S.pool;
  const dir = ex.dir || S.dir || db.settings.dir;
  switch (ex.type) {
    case 'mc': case 'mcRev': {
      Object.assign(ex, qa(c, ex.type === 'mcRev' ? 'dt' : dir));
      ex.opts = shuffle([ex.a, ...distractors(c, 3, ex.dir === 'td' ? 'd' : 't', pool)]);
      break;
    }
    case 'listen': {
      Object.assign(ex, qa(c, 'td'));
      ex.opts = shuffle([c.d, ...distractors(c, 3, 'd', pool)]);
      break;
    }
    case 'tf': {
      Object.assign(ex, qa(c, dir));
      const wrong = distractors(c, 1, ex.dir === 'td' ? 'd' : 't', pool)[0];
      ex.truth = !wrong || Math.random() < 0.5;
      ex.shown = ex.truth ? ex.a : wrong;
      break;
    }
    case 'type': Object.assign(ex, qa(c, dir)); ex.hint = 0; break;
    case 'dict': Object.assign(ex, qa(c, 'dt')); ex.hint = 0; break;
    case 'scramble': {
      Object.assign(ex, qa(c, 'dt'));
      const chars = [...c.t];
      ex.chars = chars;
      ex.slots = chars.map((ch, i) => /[\p{L}\p{N}]/u.test(ch) ? i : -1).filter(i => i >= 0);
      let order = shuffle(ex.slots);
      for (let k = 0; k < 5 && ex.slots.length > 1 && order.every((v, i) => chars[v].toLowerCase() === chars[ex.slots[i]].toLowerCase()); k++) order = shuffle(ex.slots);
      ex.tiles = order.map(i => chars[i]);
      ex.fill = [];
      break;
    }
    case 'cloze': {
      const z = clozeOf(c);
      if (!z) { ex.type = 'mcRev'; ex.ready = false; return prep(ex); }
      Object.assign(ex, qa(c, 'dt'), { z, a: z.word });
      ex.opts = shuffle([z.word, ...distractors(c, 3, 't', pool)]);
      break;
    }
    case 'match': {
      ex.left = shuffle(ex.cards).map(x => ({ id: x.id, text: x.t }));
      ex.right = shuffle(ex.cards).map(x => ({ id: x.id, text: x.d }));
      ex.sel = null; ex.done = []; ex.errors = 0; ex.badIds = [];
      break;
    }
  }
  return ex;
}

function startRun(opts) {
  stopTimers();
  S = Object.assign({ ok: 0, bad: 0, xp: 0, tok: 0, start: now(), mist: new Set(), fb: null, finished: false, dir: db.settings.dir }, opts);
  S.cur = S.nextEx();
  if (!S.cur) { toast('Нечего тренировать'); S = null; return; }
  prep(S.cur);
  go('run');
  autoSpeak(S.cur);
}
function autoSpeak(ex) {
  if (!ex) return;
  if (ex.type === 'listen' || ex.type === 'dict') { setTimeout(() => speak(ex.c.t, langsOf(ex.c).fl), 250); return; }
  if (ex.type === 'intro') { setTimeout(() => speak(ex.c.t, langsOf(ex.c).fl), 250); return; }
  if (db.settings.autoplay && ex.q && ex.ql && ex.type !== 'tf') setTimeout(() => speak(ex.q, ex.ql), 250);
}
function nextEx() {
  if (!S) return;
  S.tok++;
  S.fb = null;
  S.cur = S.nextEx();
  if (!S.cur) { finishRun(); return; }
  prep(S.cur);
  render();
  autoSpeak(S.cur);
}
function answer(ok, extra = {}) {
  if (!S || S.fb) return;
  const ex = S.cur;
  S.fb = { ok, ...extra };
  if (ok) { S.ok++; S.xp += 10; addXp(10); } else { S.bad++; if (ex.c) S.mist.add(ex.c.id); }
  if (ex.c && !ex.noGrade) practice(ex.c.id, ok);
  beep(ok);
  S.onAnswer && S.onAnswer(ex, ok);
  render(true);
  if (!ok && ex.c && ex.type !== 'tf') setTimeout(() => speak(ex.c.t, langsOf(ex.c).fl), 200);
  if (ok && ['mc', 'mcRev', 'listen', 'tf', 'cloze', 'match'].includes(ex.type) && !extra.stay) {
    const tok = S.tok;
    setTimeout(() => { if (S && S.tok === tok && S.fb) nextEx(); }, 850);
  }
}
function overrideAnswer() {
  if (!S || !S.fb || S.fb.ok) return;
  const ex = S.cur;
  S.fb.ok = true; S.fb.over = true;
  S.bad--; S.ok++; S.xp += 10; addXp(10);
  if (ex.c) { S.mist.delete(ex.c.id); practice(ex.c.id, true); }
  S.onOverride && S.onOverride(ex);
  render(true);
}

function exPrompt(ex, label) {
  return `<div class="ex-lbl">${label}</div><div class="ex-q ${ex.q.length > 40 ? 'long' : ''}">${esc(ex.q)} ${spkBtn(ex.q, ex.ql)}</div>`;
}
function optBtns(ex, act) {
  return `<div class="opts">${ex.opts.map((o, i) => {
    let cls = '';
    if (S.fb) { if (o === ex.a) cls = 'good'; else if (i === ex.picked) cls = 'bad'; else cls = 'dim'; }
    return `<button class="opt ${cls}" data-act="${act}" data-i="${i}" ${S.fb ? 'disabled' : ''}><kbd>${i + 1}</kbd>${esc(o)}</button>`;
  }).join('')}</div>`;
}
function hintFor(c) { return c.note ? `<div class="note-h">💡 ${esc(c.note)}</div>` : ''; }
const EX = {
  intro(ex) {
    const c = ex.c, { fl, bl } = langsOf(c);
    return `<div class="ex-lbl">Новое слово</div>
      <div class="intro card">
        <div class="intro-t">${esc(c.t)} ${spkBtn(c.t, fl)} ${spkBtn(c.t, fl, 'slow').replace('data-act="speak"', 'data-act="speak-slow"').replace(I.spk, I.turtle)}</div>
        <div class="intro-d">${esc(c.d)}</div>
        ${c.ex ? `<div class="ex-line">${esc(c.ex)} ${spkBtn(c.ex, fl, 'sm')}<br><span>${esc(c.exT)}</span></div>` : ''}
        ${c.note ? `<div class="note-h">💡 ${esc(c.note)}</div>` : `<button class="lnk" data-act="edit-card" data-id="${esc(c.id)}">＋ Придумать ассоциацию</button>`}
      </div>
      <p class="hint center">Произнесите слово вслух вместе с диктором.</p>
      <div class="ex-foot"><button class="btn wide" data-act="ex-next">Запомнил, дальше</button></div>`;
  },
  mc(ex) {
    return `${exPrompt(ex, ex.dir === 'td' ? 'Выберите перевод' : 'Выберите слово')}${optBtns(ex, 'ex-opt')}
      ${S.fb ? '' : `<div class="ex-foot"><button class="lnk" data-act="ex-dunno">Не знаю</button></div>`}`;
  },
  mcRev(ex) { return EX.mc(ex); },
  listen(ex) {
    return `<div class="ex-lbl">Послушайте и выберите перевод</div>
      <div class="listen-btns"><button class="big-spk" data-act="speak" data-text="${esc(ex.c.t)}" data-lang="${esc(ex.ql)}">${I.spk}</button>
      <button class="big-spk sm" data-act="speak-slow" data-text="${esc(ex.c.t)}" data-lang="${esc(ex.ql)}">${I.turtle}</button></div>
      ${S.fb ? `<div class="ex-q center">${esc(ex.c.t)}</div>` : ''}
      ${optBtns(ex, 'ex-opt')}`;
  },
  tf(ex) {
    const btn = (v, l) => {
      let cls = '';
      if (S.fb) cls = v === ex.truth ? 'good' : ex.picked === v ? 'bad' : 'dim';
      return `<button class="opt tf ${cls}" data-act="ex-tf" data-v="${v ? 1 : 0}" ${S.fb ? 'disabled' : ''}>${l}</button>`;
    };
    return `<div class="ex-lbl">Верно ли переведено?</div>
      <div class="tf-card card"><div class="ex-q">${esc(ex.q)} ${spkBtn(ex.q, ex.ql)}</div><div class="tf-eq">=</div><div class="ex-q alt">${esc(ex.shown)}</div></div>
      <div class="opts two">${btn(false, '✗ Неверно')}${btn(true, '✓ Верно')}</div>`;
  },
  type(ex) {
    const hint = ex.hint ? `<div class="hint center">Подсказка: <b>${esc(ex.a.slice(0, ex.hint))}${ex.hint < ex.a.length ? '…' : ''}</b></div>` : '';
    return `${exPrompt(ex, ex.dir === 'td' ? 'Напишите перевод' : 'Напишите слово')}
      ${hint}
      <input class="ans ${S.fb ? (S.fb.ok ? 'good' : 'bad') : ''}" id="ans" autocomplete="off" autocapitalize="off" autocorrect="off" spellcheck="false" placeholder="Ваш ответ" value="${esc(ex.typed || '')}" ${S.fb ? 'readonly' : 'autofocus'} data-enter="ex-check">
      ${S.fb ? '' : `<div class="ex-foot"><button class="lnk" data-act="ex-hint">Подсказка</button><button class="lnk" data-act="ex-dunno">Не знаю</button><button class="btn" data-act="ex-check">Проверить</button></div>`}`;
  },
  dict(ex) {
    const hint = ex.hint ? `<div class="hint center">Подсказка: <b>${esc(ex.a.slice(0, ex.hint))}…</b></div>` : '';
    return `<div class="ex-lbl">Диктант: напишите услышанное</div>
      <div class="listen-btns"><button class="big-spk" data-act="speak" data-text="${esc(ex.c.t)}" data-lang="${esc(ex.al)}">${I.spk}</button>
      <button class="big-spk sm" data-act="speak-slow" data-text="${esc(ex.c.t)}" data-lang="${esc(ex.al)}">${I.turtle}</button></div>
      ${S.fb || ex.showTr ? `<div class="hint center">${esc(ex.c.d)}</div>` : ''}
      ${hint}
      <input class="ans ${S.fb ? (S.fb.ok ? 'good' : 'bad') : ''}" id="ans" autocomplete="off" autocapitalize="off" autocorrect="off" spellcheck="false" placeholder="Что прозвучало?" value="${esc(ex.typed || '')}" ${S.fb ? 'readonly' : 'autofocus'} data-enter="ex-check">
      ${S.fb ? '' : `<div class="ex-foot"><button class="lnk" data-act="ex-tr">Перевод</button><button class="lnk" data-act="ex-hint">Подсказка</button><button class="btn" data-act="ex-check">Проверить</button></div>`}`;
  },
  scramble(ex) {
    let k = 0;
    const slotHtml = ex.chars.map((ch, i) => {
      if (!ex.slots.includes(i)) return ch === ' ' ? '<span class="gap"></span>' : `<span class="fixed">${esc(ch)}</span>`;
      const ti = ex.fill[k++];
      return ti === undefined ? '<span class="slot"></span>' : `<button class="slot full" data-act="scr-del" data-k="${k - 1}">${esc(ex.tiles[ti])}</button>`;
    }).join('');
    const tiles = ex.tiles.map((ch, i) => `<button class="tile ${ex.fill.includes(i) ? 'used' : ''}" data-act="scr-add" data-i="${i}" ${ex.fill.includes(i) || S.fb ? 'disabled' : ''}>${esc(ch)}</button>`).join('');
    return `${exPrompt(ex, 'Соберите слово из букв')}
      <div class="slots ${S.fb ? (S.fb.ok ? 'good' : 'bad') : ''}">${slotHtml}</div>
      <div class="tiles">${tiles}</div>
      ${S.fb ? '' : `<div class="ex-foot"><button class="lnk" data-act="scr-hint">Подсказка</button><button class="lnk" data-act="scr-clear">Стереть</button><button class="lnk" data-act="ex-dunno">Не знаю</button></div>`}`;
  },
  cloze(ex) {
    const blank = S.fb ? `<b class="${S.fb.ok ? 'good-t' : 'bad-t'}">${esc(ex.z.word)}</b>` : '<span class="blank">_____</span>';
    return `<div class="ex-lbl">Вставьте пропущенное слово</div>
      <div class="cloze card"><div class="cloze-s">${esc(ex.z.before)}${blank}${esc(ex.z.after)}</div><div class="hint">${esc(ex.c.exT || ex.c.d)}</div></div>
      ${optBtns(ex, 'ex-opt')}`;
  },
  match(ex) {
    const col = (arr, side) => arr.map((x, i) => {
      const done = ex.done.includes(x.id);
      const sel = ex.sel && ex.sel.side === side && ex.sel.i === i;
      const bad = ex.flash && ex.flash.some(f => f.side === side && f.i === i);
      return `<button class="mt ${done ? 'done' : ''} ${sel ? 'sel' : ''} ${bad ? 'bad' : ''}" data-act="ex-match" data-side="${side}" data-i="${i}" ${done ? 'disabled' : ''}>${esc(x.text)}</button>`;
    }).join('');
    return `<div class="ex-lbl">Соедините пары</div><div class="mgrid"><div>${col(ex.left, 'L')}</div><div>${col(ex.right, 'R')}</div></div>`;
  },
  round(ex) {
    return `<div class="round card">
      <div class="big-emoji">${ex.emoji || '💪'}</div><h2>${esc(ex.title)}</h2><p class="desc">${esc(ex.sub || '')}</p>
      ${ex.html || ''}
      <button class="btn wide" data-act="ex-next">Продолжить</button></div>`;
  },
};
function fbHtml() {
  const ex = S.cur, fb = S.fb;
  if (!fb || ex.type === 'round' || ex.type === 'intro') return '';
  const c = ex.c;
  let body;
  if (fb.ok) {
    body = `<b>${fb.over ? 'Засчитано' : pick(['Верно!', 'Отлично!', 'Так держать!', 'Правильно!', 'Супер!'])}</b>${fb.typo ? `<div>Опечатка — правильно: <b>${esc(ex.a)}</b></div>` : ''}`;
  } else {
    body = `<b>${fb.dunno ? 'Запомните:' : 'Правильный ответ:'}</b><div class="fb-a">${esc(ex.type === 'match' ? 'ошибок: ' + ex.errors : ex.a)}</div>
      ${c && c.ex && ex.type !== 'cloze' ? `<div class="fb-ex">${esc(c.ex)}</div>` : ''}${c ? hintFor(c) : ''}`;
  }
  const canOver = !fb.ok && !fb.dunno && (ex.type === 'type' || ex.type === 'dict') && ex.typed;
  return `<div class="fb ${fb.ok ? 'ok' : 'no'}"><div class="fb-b">${body}</div>
    <div class="fb-btns">${canOver ? '<button class="lnk" data-act="ex-over">Я был прав</button>' : ''}
    <button class="btn" data-act="ex-next" id="fb-next">Дальше</button></div></div>`;
}
screens.run = () => {
  if (!S) return '';
  const ex = S.cur;
  const pct = clamp(S.progress() * 100, 0, 100);
  return `<div class="run ${S.fb ? 'has-fb' : ''}">
    <div class="run-top"><button class="ib" data-act="back" aria-label="Выйти">${I.close}</button>
      <div class="pbar"><i style="width:${pct}%"></i></div><div class="run-cnt">${S.counter ? S.counter() : ''}</div></div>
    <div class="ex ex-${ex.type}">${EX[ex.type](ex)}</div>
    ${fbHtml()}
  </div>`;
};
after.run = (c, keep) => {
  if (S && S.fb) { const b = $('#fb-next'); if (b && !keep) b.focus(); }
};

function finishRun() {
  if (!S) return;
  S.finished = true;
  const res = S.finish ? S.finish() : {};
  const total = S.ok + S.bad;
  const acc = total ? Math.round(S.ok / total * 100) : 100;
  const mist = [...S.mist].map(id => cardIdx.get(id)?.c).filter(Boolean);
  replace('done', { res: { acc, ok: S.ok, bad: S.bad, xp: S.xp + (res.bonus || 0), time: now() - S.start, mist, title: res.title || 'Готово!', emoji: res.emoji || '🎉', stars: res.stars, extra: res.extra || '', again: S.again, next: res.next, deck: S.deckId } });
}
function fmtTime(ms) { const s = Math.round(ms / 1000); return s < 60 ? `${s} с` : `${Math.floor(s / 60)} мин ${s % 60} с`; }
screens.done = c => {
  const r = c.res;
  return `<div class="pad done">
    <div class="big-emoji pop">${r.emoji}</div>
    <h1 class="center">${esc(r.title)}</h1>
    ${r.stars ? `<div class="big-stars">${'★'.repeat(r.stars)}<span>${'★'.repeat(3 - r.stars)}</span></div>` : ''}
    <div class="stats3">
      <div><b>${r.acc}%</b><span>точность</span></div>
      <div><b>+${r.xp}</b><span>XP</span></div>
      <div><b>${fmtTime(r.time)}</b><span>время</span></div>
    </div>
    ${r.extra}
    ${r.mist.length ? `<h2 class="sec">Над этим стоит поработать</h2>${r.mist.map(cardRow).join('')}` : `<p class="center desc">Без единой ошибки — великолепно!</p>`}
    <div class="done-btns">
      ${r.next ? `<button class="btn wide" data-act="done-next">Следующий урок</button>` : ''}
      ${r.mist.length >= 2 ? `<button class="btn ghost wide" data-act="done-mist">Повторить ошибки</button>` : ''}
      ${r.again ? `<button class="btn ghost wide" data-act="done-again">Ещё раз</button>` : ''}
      <button class="btn ghost wide" data-act="back">Готово</button>
    </div>
  </div>`;
};

/* ───────────────────────── Запуск режимов ───────────────────────── */
function sessionCards(deckId) {
  const d = getDeck(deckId);
  return d ? d.cards : [];
}
function startMode(mode, deckId, cardsOverride) {
  const d = getDeck(deckId) || { id: deckId, title: 'Тренировка', cards: [] };
  let cards = cardsOverride || d.cards;
  if (mode === 'lessons') { setActive(deckId); tab('lessons'); return; }
  if (mode === 'review') return startReview(deckId);
  if (!cards.length) { toast('В наборе нет карточек'); return; }
  const again = () => startMode(mode, deckId, cardsOverride);
  switch (mode) {
    case 'flash': return startFlash(d, cards);
    case 'learn': return startLearn(d, cards, again);
    case 'write': return startQueueMode(d, cards, 'type', 'Письмо', again);
    case 'listen': return startQueueMode(d, cards, 'dict', 'Диктант', again);
    case 'scramble': {
      const ok = cards.filter(c => letterCount(c.t) <= 18);
      if (!ok.length) { toast('Слова в наборе слишком длинные для этого режима'); return; }
      return startQueueMode(d, ok, 'scramble', 'Собери слово', again);
    }
    case 'test': return go('testsetup', { deck: deckId, ids: cardsOverride ? cards.map(c => c.id) : null });
    case 'match': return startMatchGame(d, cards);
    case 'sprint': return startSprint(d, cards);
  }
}

// Заучивание: каждое слово проходит «выбор ответа» → «письменный ответ», раундами по 7.
function startLearn(d, cards, again) {
  const list = cards.length > 40 ? [...cards].sort((a, b) => level(a.id) - level(b.id)).slice(0, 40) : cards;
  const st = {};
  list.forEach(c => { st[c.id] = level(c.id) >= 3 ? 1 : 0; });
  let round = [], rpos = 0, rn = 0;
  const byId = new Map(list.map(c => [c.id, c]));
  const newRound = () => {
    const left = list.filter(c => st[c.id] < 2);
    left.sort((a, b) => st[b.id] - st[a.id]);
    round = shuffle(left.slice(0, 7)).map(c => c.id);
    rpos = 0; rn++;
  };
  newRound();
  startRun({
    kind: 'learn', deckId: d.id, pool: list, again,
    nextEx() {
      if (rpos < round.length) {
        const c = byId.get(round[rpos++]);
        return st[c.id] === 0 ? { type: 'mc', c } : { type: 'type', c };
      }
      if (list.every(c => st[c.id] >= 2)) return null;
      const doneN = list.filter(c => st[c.id] >= 2).length;
      const html = `<div class="pbar"><i style="width:${doneN / list.length * 100}%"></i></div><p class="hint center">Освоено ${doneN} из ${list.length}</p>
        <div class="words">${round.map(id => byId.get(id)).map(c => `<div class="crow sm"><span class="lstage s${st[c.id]}">${st[c.id] >= 2 ? '✓' : st[c.id] === 1 ? '½' : '·'}</span><div class="crow-b"><div class="crow-t">${esc(c.t)}</div><div class="crow-d">${esc(c.d)}</div></div></div>`).join('')}</div>`;
      newRound();
      return { type: 'round', title: `Раунд ${rn - 1} пройден`, sub: 'Слова, которые ещё не освоены, вернутся в следующем раунде.', emoji: pick(['💪', '🚀', '🌟', '🔥']), html, noGrade: true };
    },
    onAnswer(ex, ok) { if (ex.c && ok) st[ex.c.id]++; },
    onOverride(ex) { if (ex.c) st[ex.c.id]++; },
    progress() { return list.reduce((s, c) => s + Math.min(2, st[c.id]), 0) / (list.length * 2); },
    counter() { return `${list.filter(c => st[c.id] >= 2).length}/${list.length}`; },
    finish() { return { title: 'Всё выучено!', emoji: '🎓', bonus: 0 }; },
  });
}

// Письмо, диктант, собери слово: проход по всем словам, ошибки — повторно в конце.
function startQueueMode(d, cards, type, title, again) {
  let queue = shuffle(cards).slice(0, 40).map(c => ({ type, c }));
  const total = queue.length;
  let retry = [], done = 0, round = 1;
  startRun({
    kind: type, deckId: d.id, pool: d.cards.length >= 4 ? d.cards : cards, again,
    nextEx() {
      if (queue.length) return queue.shift();
      if (retry.length) {
        queue = shuffle(retry).map(c => ({ type, c }));
        retry = [];
        round++;
        return { type: 'round', title: 'Работа над ошибками', sub: `Осталось ${nWords(queue.length)} — попробуем их ещё раз.`, emoji: '🎯', noGrade: true };
      }
      return null;
    },
    onAnswer(ex, ok) { if (!ex.c) return; ok ? done++ : retry.push(ex.c); },
    onOverride(ex) { const i = retry.lastIndexOf(ex.c); if (i >= 0) retry.splice(i, 1); done++; },
    progress() { return done / total; },
    counter() { return `${done}/${total}`; },
    finish() { return { title: `${title}: готово!`, emoji: type === 'dict' ? '🎧' : type === 'scramble' ? '🔤' : '✍️' }; },
  });
}

function startLesson(deckId, n) {
  const deck = deckById(deckId);
  if (!deck) return;
  const nodes = lessonNodes(deck);
  const node = nodes[n];
  if (!node) return;
  setActive(deckId);
  closeModal();
  const queue = buildLesson(node);
  let pos = 0;
  const retried = new Set();
  startRun({
    kind: 'lesson', deckId, pool: deck.cards, dir: 'td', again: () => startLesson(deckId, n),
    nextEx() { return queue[pos++] || null; },
    onAnswer(ex, ok) {
      if (ok || ex.type === 'match' || !ex.c) return;
      const k = ex.c.id + ex.type;
      if (retried.has(k)) return;
      retried.add(k);
      queue.push({ type: ex.type === 'type' || ex.type === 'dict' ? 'type' : 'mc', c: ex.c, dir: ex.dir === 'dt' ? 'dt' : 'td' });
    },
    progress() { return (pos - 1) / queue.length; },
    counter() { return `${Math.max(0, pos - 1)}/${queue.length}`; },
    finish() {
      const total = S.ok + S.bad;
      const acc = total ? S.ok / total : 1;
      const stars = acc >= 0.95 ? 3 : acc >= 0.8 ? 2 : 1;
      const rec = db.lessons[deckId] || (db.lessons[deckId] = {});
      const key = nodeKey(node);
      rec[key] = Math.max(rec[key] || 0, stars);
      const bonus = node.kind === 'exam' ? 100 : node.kind === 'review' ? 40 : 30;
      addXp(bonus);
      saveSoon();
      return { title: node.kind === 'exam' ? 'Экзамен сдан!' : 'Урок пройден!', emoji: node.kind === 'exam' ? '🏆' : pick(['🎉', '🌟', '🥳', '🎯']), stars, bonus, next: nodes[n + 1] ? () => startLesson(deckId, n + 1) : null };
    },
  });
}

/* ───────────────────────── Повторение (SRS) ───────────────────────── */
let R = null;
function startReview(deckId) {
  const base = deckId === '@due' ? allCards() : sessionCards(deckId);
  let due = dueCards(base).sort((a, b) => db.prog[a.id].due - db.prog[b.id].due);
  const left = Math.max(0, db.settings.newPerDay - dayRec().nw);
  const fresh = deckId === '@due' ? [] : base.filter(c => !db.prog[c.id]?.seen).slice(0, left);
  const queue = [...due.slice(0, 200), ...fresh].map(c => c.id);
  if (!queue.length) {
    const nd = nextDue();
    toast(fresh.length === 0 && left === 0 && deckId !== '@due' ? 'Лимит новых слов на сегодня исчерпан' : `Повторять пока нечего${nd < Infinity ? ' — следующее через ' + fmtIvl(nd - now()) : ''}`);
    return;
  }
  R = { deckId, queue, total: queue.length, done: 0, shown: false, start: now(), cnt: [0, 0, 0, 0], fresh: new Set(fresh.map(c => c.id)), dir: db.settings.dir };
  R.qa = qa(cardIdx.get(queue[0]).c, R.dir);
  go('review');
  if (db.settings.autoplay) speak(R.qa.q, R.qa.ql);
}
screens.review = () => {
  if (!R) return '';
  const id = R.queue[0];
  const c = cardIdx.get(id)?.c;
  if (!c) return '';
  const p = db.prog[id] || { ef: 2.5, ivl: 0, reps: 0 };
  const lbl = q => { const n = nextState(p, q); return q === 0 ? '10 мин' : fmtIvl(n.ivl * DAY); };
  const x = R.qa;
  return `<div class="run review">
    <div class="run-top"><button class="ib" data-act="back" aria-label="Выйти">${I.close}</button>
      <div class="pbar"><i style="width:${R.done / (R.done + R.queue.length) * 100}%"></i></div><div class="run-cnt">${R.queue.length}</div></div>
    <div class="ex">
      <div class="ex-lbl">${R.fresh.has(id) ? '🆕 Новое слово' : `Повторение · ${LEVELS[level(id)]}`}</div>
      <div class="rv card ${R.shown ? 'open' : ''}">
        <div class="ex-q">${esc(x.q)} ${spkBtn(x.q, x.ql)}</div>
        ${R.shown ? `<hr><div class="ex-q alt">${esc(x.a)} ${spkBtn(x.a, x.al)}</div>
          ${c.ex ? `<div class="ex-line">${esc(c.ex)}<br><span>${esc(c.exT)}</span></div>` : ''}${hintFor(c)}` : ''}
      </div>
    </div>
    <div class="rv-foot">${R.shown ? `<div class="grades">
      <button class="g0" data-act="rv-grade" data-q="0"><b>Снова</b><small>${lbl(0)}</small></button>
      <button class="g1" data-act="rv-grade" data-q="1"><b>Трудно</b><small>${lbl(1)}</small></button>
      <button class="g2" data-act="rv-grade" data-q="2"><b>Хорошо</b><small>${lbl(2)}</small></button>
      <button class="g3" data-act="rv-grade" data-q="3"><b>Легко</b><small>${lbl(3)}</small></button></div>`
      : `<button class="btn wide" data-act="rv-show" id="rv-show">Показать ответ</button>`}</div>
  </div>`;
};
function reviewGrade(q) {
  const id = R.queue.shift();
  grade(id, q);
  R.cnt[q]++;
  const d = dayRec();
  q ? d.ok++ : d.bad++;
  if (q > 0) { R.done++; addXp(10); } else { R.queue.splice(Math.min(R.queue.length, 4), 0, id); }
  R.shown = false;
  if (!R.queue.length) {
    const total = R.cnt.reduce((a, b) => a + b, 0);
    replace('done', { res: { acc: total ? Math.round((total - R.cnt[0]) / total * 100) : 100, xp: R.done * 10, time: now() - R.start, mist: [], title: 'Повторение завершено', emoji: '✅',
      extra: `<div class="legend center">Снова ${R.cnt[0]} · Трудно ${R.cnt[1]} · Хорошо ${R.cnt[2]} · Легко ${R.cnt[3]}</div>` } });
    R = null;
    return;
  }
  R.qa = qa(cardIdx.get(R.queue[0]).c, R.dir);
  render();
  if (db.settings.autoplay) speak(R.qa.q, R.qa.ql);
}

/* ───────────────────────── Карточки ───────────────────────── */
let F = null;
function startFlash(d, cards) {
  F = { deckId: d.id, all: cards, cards: [...cards], i: 0, flip: false, know: [], learn: [], hist: [], shuffled: false, auto: false, dir: db.settings.dir };
  F.qa = F.cards.map(c => qa(c, F.dir));
  go('flash');
}
screens.flash = () => {
  if (!F) return '';
  if (F.i >= F.cards.length) {
    return `${hdr('Карточки')}<div class="pad done">
      <div class="big-emoji pop">🃏</div><h1 class="center">Набор пройден</h1>
      <div class="stats3"><div><b class="good-t">${F.know.length}</b><span>знаю</span></div><div><b class="bad-t">${F.learn.length}</b><span>ещё учу</span></div><div><b>${F.cards.length}</b><span>всего</span></div></div>
      <div class="done-btns">
        ${F.learn.length ? `<button class="btn wide" data-act="fl-relearn">Повторить «ещё учу» (${F.learn.length})</button>` : ''}
        <button class="btn ghost wide" data-act="fl-restart">Начать заново</button>
        ${F.learn.length ? `<button class="btn ghost wide" data-act="fl-learnmode">Заучивать трудные</button>` : ''}
        <button class="btn ghost wide" data-act="back">Готово</button></div></div>`;
  }
  const c = F.cards[F.i], x = F.qa[F.i];
  return `<div class="flash">
    <div class="run-top"><button class="ib" data-act="back">${I.close}</button>
      <div class="pbar"><i style="width:${F.i / F.cards.length * 100}%"></i></div><div class="run-cnt">${F.i + 1}/${F.cards.length}</div></div>
    <div class="fl-tools">
      <button class="chip ${F.shuffled ? 'on' : ''}" data-act="fl-shuffle">${I.shuffle} Перемешать</button>
      <button class="chip ${F.auto ? 'on' : ''}" data-act="fl-auto">${F.auto ? I.pause : I.play} Авто</button>
    </div>
    <div class="fc-wrap"><div class="fc ${F.flip ? 'flip' : ''}" id="fc" data-act="fl-flip">
      <div class="fc-face fc-front">
        <button class="ib star ${c.star ? 'on' : ''}" data-act="star" data-id="${esc(c.id)}">${I.star}</button>
        <div class="fc-txt ${x.q.length > 40 ? 'long' : ''}">${esc(x.q)}</div>
        ${spkBtn(x.q, x.ql, 'fc-spk')}
        <div class="fc-hint">нажмите, чтобы перевернуть</div>
      </div>
      <div class="fc-face fc-back">
        <div class="fc-txt ${x.a.length > 40 ? 'long' : ''}">${esc(x.a)}</div>
        ${c.ex ? `<div class="ex-line">${esc(c.ex)}<br><span>${esc(c.exT)}</span></div>` : ''}
        ${hintFor(c)}
        ${spkBtn(x.a, x.al, 'fc-spk')}
      </div>
    </div>
    <div class="swipe-l">Ещё учу</div><div class="swipe-r">Знаю</div></div>
    <div class="fl-btns">
      <button class="fl-no" data-act="fl-mark" data-v="0">✗<small>Ещё учу ${F.learn.length}</small></button>
      <button class="ib" data-act="fl-undo" ${F.hist.length ? '' : 'disabled'} aria-label="Назад">${I.undo}</button>
      <button class="fl-yes" data-act="fl-mark" data-v="1">✓<small>Знаю ${F.know.length}</small></button>
    </div>
    <p class="hint center">Свайп вправо — знаю, влево — ещё учу</p>
  </div>`;
};
after.flash = () => {
  const el = $('#fc');
  if (!el) return;
  let x0 = null, y0 = 0, dx = 0, moved = false;
  el.addEventListener('touchstart', e => { x0 = e.touches[0].clientX; y0 = e.touches[0].clientY; dx = 0; moved = false; el.style.transition = 'none'; }, { passive: true });
  el.addEventListener('touchmove', e => {
    if (x0 === null) return;
    dx = e.touches[0].clientX - x0;
    const dy = e.touches[0].clientY - y0;
    if (Math.abs(dx) > 8 && Math.abs(dx) > Math.abs(dy)) moved = true;
    if (!moved) return;
    el.style.transform = `translateX(${dx}px) rotate(${dx / 25}deg)${F.flip ? ' rotateY(180deg)' : ''}`;
    el.parentElement.classList.toggle('to-r', dx > 60);
    el.parentElement.classList.toggle('to-l', dx < -60);
  }, { passive: true });
  el.addEventListener('touchend', () => {
    el.style.transition = '';
    el.parentElement.classList.remove('to-r', 'to-l');
    if (moved && Math.abs(dx) > 90) { flMark(dx > 0); }
    else el.style.transform = '';
    if (moved) { el.dataset.swiped = '1'; setTimeout(() => { delete el.dataset.swiped; }, 50); }
    x0 = null;
  });
  if (F && F.auto) {
    const c = F.cards[F.i];
    if (!c) { F.auto = false; return; }
    if (!F.flip) speak(F.qa[F.i].q, F.qa[F.i].ql);
    clearTimeout(tickT);
    tickT = setTimeout(() => {
      if (!F || !F.auto || cur().name !== 'flash') return;
      if (!F.flip) { F.flip = true; render(true); speak(F.qa[F.i].a, F.qa[F.i].al); setTimeout(() => { if (F && F.auto && cur().name === 'flash') { F.hist.push({ i: F.i, v: null }); F.i++; F.flip = false; render(true); } }, 2600); }
    }, 2600);
  }
};
function flMark(knowIt) {
  const c = F.cards[F.i];
  if (!c) return;
  (knowIt ? F.know : F.learn).push(c);
  F.hist.push({ i: F.i, v: knowIt });
  practice(c.id, knowIt);
  if (knowIt) addXp(2);
  F.i++; F.flip = false;
  render(true);
}

/* ───────────────────────── Подбор (игра на время) ───────────────────────── */
let MG = null;
function startMatchGame(d, cards) {
  if (cards.length < 2) { toast('Нужно хотя бы 2 карточки'); return; }
  const set = shuffle(cards).slice(0, 6);
  const tiles = shuffle([...set.map(c => ({ id: c.id, text: c.t, side: 't', lang: langsOf(c).fl })), ...set.map(c => ({ id: c.id, text: c.d, side: 'd' }))]);
  MG = { deckId: d.id, cards, tiles, sel: null, done: [], start: now(), pen: 0, errors: 0, end: 0, bad: [] };
  go('matchgame');
}
const mgTime = () => ((MG.end || now()) - MG.start) / 1000 + MG.pen;
screens.matchgame = () => {
  if (!MG) return '';
  const best = db.best['match:' + MG.deckId];
  if (MG.end) {
    const t = mgTime();
    return `${hdr('Подбор')}<div class="pad done"><div class="big-emoji pop">⏱️</div>
      <h1 class="center">${t.toFixed(1)} с</h1>
      <p class="center desc">${MG.newBest ? '🏅 Новый рекорд!' : best ? `Рекорд: ${best.toFixed(1)} с` : ''}${MG.errors ? ` · штраф +${MG.errors} с за ошибки` : ''}</p>
      <div class="done-btns"><button class="btn wide" data-act="mg-again">Играть ещё</button><button class="btn ghost wide" data-act="back">Готово</button></div></div>`;
  }
  return `<div class="mg">
    <div class="run-top"><button class="ib" data-act="back">${I.close}</button><div class="mg-time" id="mg-time">0.0 с</div><div class="run-cnt">${best ? 'рекорд ' + best.toFixed(1) : ''}</div></div>
    <p class="hint center">Соедините слово и его перевод. Ошибка — +1 секунда.</p>
    <div class="mg-grid">${MG.tiles.map((t, i) => `<button class="mg-t ${t.text.length > 16 ? 'long' : ''} ${MG.done.includes(i) ? 'gone' : ''} ${MG.sel === i ? 'sel' : ''} ${MG.bad.includes(i) ? 'bad' : ''}" data-act="mg-tap" data-i="${i}">${esc(t.text)}</button>`).join('')}</div>
  </div>`;
};
after.matchgame = () => {
  if (!MG || MG.end) return;
  const el = $('#mg-time');
  const tick = () => { if (el && MG) el.textContent = mgTime().toFixed(1) + ' с'; };
  tick();
  setTick(tick, 100);
};
function mgTap(i) {
  if (MG.done.includes(i)) return;
  const t = MG.tiles[i];
  if (t.side === 't' && t.lang) speak(t.text, t.lang);
  if (MG.sel === null) { MG.sel = i; render(true); return; }
  if (MG.sel === i) { MG.sel = null; render(true); return; }
  const a = MG.tiles[MG.sel];
  if (a.id === t.id && a.side !== t.side) {
    MG.done.push(MG.sel, i);
    vibrate(15);
    MG.sel = null;
    if (MG.done.length === MG.tiles.length) {
      MG.end = now();
      const k = 'match:' + MG.deckId, tm = mgTime();
      if (!db.best[k] || tm < db.best[k]) { MG.newBest = !!db.best[k]; db.best[k] = tm; }
      if (!db.best.matchMin || tm < db.best.matchMin) db.best.matchMin = tm;
      addXp(20);
      beep(true);
      stopTimers();
    }
  } else {
    MG.pen += 1; MG.errors++;
    MG.bad = [MG.sel, i];
    MG.sel = null;
    beep(false);
    setTimeout(() => { if (MG) { MG.bad = []; if (cur().name === 'matchgame') render(true); } }, 450);
  }
  render(true);
}

/* ───────────────────────── Спринт ───────────────────────── */
let SP = null;
function startSprint(d, cards) {
  if (cards.length < 2) { toast('Нужно хотя бы 2 карточки'); return; }
  SP = { deckId: d.id, cards, end: now() + 60000, score: 0, streak: 0, mult: 1, ok: 0, bad: 0, mist: [], over: false, flash: '' };
  spNext();
  go('sprint');
}
function spNext() {
  const c = pick(SP.cards);
  const x = qa(c, db.settings.dir);
  const wrong = distractors(c, 1, x.dir === 'td' ? 'd' : 't', SP.cards)[0];
  const truth = !wrong || Math.random() < 0.5;
  SP.cur = { c, q: x.q, shown: truth ? x.a : wrong, truth, a: x.a };
}
screens.sprint = () => {
  if (!SP) return '';
  const best = db.best['sprint:' + SP.deckId] || 0;
  if (SP.over) {
    return `${hdr('Спринт')}<div class="pad done"><div class="big-emoji pop">⚡</div><h1 class="center">${SP.score} очков</h1>
      <p class="center desc">${SP.newBest ? '🏅 Новый рекорд!' : best ? `Рекорд: ${best}` : ''}</p>
      <div class="stats3"><div><b class="good-t">${SP.ok}</b><span>верно</span></div><div><b class="bad-t">${SP.bad}</b><span>ошибок</span></div><div><b>${SP.ok + SP.bad ? Math.round(SP.ok / (SP.ok + SP.bad) * 100) : 0}%</b><span>точность</span></div></div>
      ${SP.mist.length ? `<h2 class="sec">Ошибки</h2>${[...new Set(SP.mist)].map(c => cardRow(c)).join('')}` : ''}
      <div class="done-btns"><button class="btn wide" data-act="sp-again">Ещё раз</button><button class="btn ghost wide" data-act="back">Готово</button></div></div>`;
  }
  const x = SP.cur;
  return `<div class="sprint ${SP.flash}">
    <div class="run-top"><button class="ib" data-act="back">${I.close}</button><div class="sp-time" id="sp-time">60</div><div class="run-cnt">${SP.score}</div></div>
    <div class="sp-mult">${'●'.repeat(SP.streak % 4)}${'○'.repeat(3 - SP.streak % 4)} ×${SP.mult}</div>
    <div class="card sp-card"><div class="ex-q">${esc(x.q)}</div><div class="tf-eq">=</div><div class="ex-q alt">${esc(x.shown)}</div></div>
    <div class="opts two sp-btns"><button class="opt tf" data-act="sp-ans" data-v="0">✗ Неверно</button><button class="opt tf" data-act="sp-ans" data-v="1">✓ Верно</button></div>
    <p class="hint center">Серия из 4 верных ответов увеличивает множитель</p>
  </div>`;
};
after.sprint = () => {
  if (!SP || SP.over) return;
  const el = $('#sp-time');
  const tick = () => {
    if (!SP) return;
    const left = Math.max(0, Math.ceil((SP.end - now()) / 1000));
    if (el) { el.textContent = left; el.classList.toggle('low', left <= 10); }
    if (left <= 0) {
      SP.over = true;
      const k = 'sprint:' + SP.deckId;
      if (SP.score > (db.best[k] || 0)) { SP.newBest = !!db.best[k]; db.best[k] = SP.score; }
      db.best.sprintMax = Math.max(db.best.sprintMax || 0, SP.score);
      saveSoon();
      stopTimers();
      render();
    }
  };
  tick();
  setTick(tick, 200);
};
function spAnswer(v) {
  if (!SP || SP.over) return;
  const ok = v === SP.cur.truth;
  if (ok) {
    SP.ok++; SP.streak++;
    SP.score += 10 * SP.mult;
    if (SP.streak % 4 === 0) SP.mult = Math.min(4, SP.mult + 1);
    addXp(2);
  } else {
    SP.bad++; SP.streak = 0; SP.mult = 1;
    SP.mist.push(SP.cur.c);
  }
  practice(SP.cur.c.id, ok);
  beep(ok);
  SP.flash = ok ? 'fl-ok' : 'fl-no';
  spNext();
  render(true);
  setTimeout(() => { if (SP) SP.flash = ''; const el = $('.sprint'); if (el) el.classList.remove('fl-ok', 'fl-no'); }, 250);
}

/* ───────────────────────── Тест ───────────────────────── */
let T = null;
screens.testsetup = c => {
  const cards = c.ids ? c.ids.map(id => cardIdx.get(id)?.c).filter(Boolean) : sessionCards(c.deck);
  c.n = c.n || Math.min(20, cards.length);
  c.types = c.types || { tf: true, mc: true, wr: true, mt: cards.length >= 4 };
  const ty = [['tf', 'Верно / неверно'], ['mc', 'Выбор ответа'], ['wr', 'Письменный ответ'], ['mt', 'Сопоставление']];
  return `${hdr('Настройка теста')}<div class="pad form">
    <div class="lbl">Вопросов: <b id="t-n">${c.n}</b> из ${cards.length}</div>
    <input type="range" min="${Math.min(3, cards.length)}" max="${cards.length}" value="${c.n}" data-in="t-n">
    <div class="lbl">Типы вопросов</div>
    ${ty.map(([k, l]) => `<label class="tgl"><input type="checkbox" data-ch="t-type" data-k="${k}" ${c.types[k] ? 'checked' : ''}> ${l}</label>`).join('')}
    <div class="lbl">Отвечать</div>${dirSeg()}
    <button class="btn wide" data-act="t-start">Начать тест</button>
  </div>`;
};
function buildTest(c) {
  const cards = shuffle(c.ids ? c.ids.map(id => cardIdx.get(id)?.c).filter(Boolean) : sessionCards(c.deck)).slice(0, c.n);
  const types = Object.keys(c.types).filter(k => c.types[k]);
  if (!types.length) types.push('mc');
  const pool = sessionCards(c.deck).length >= 4 ? sessionCards(c.deck) : cards;
  S = { pool, dir: db.settings.dir };
  const groups = { tf: [], mc: [], wr: [], mt: [] };
  cards.forEach((card, i) => groups[types[i % types.length]].push(card));
  if (groups.mt.length && groups.mt.length < 2) { groups.mc.push(...groups.mt); groups.mt = []; }
  const qs = [];
  groups.tf.forEach(card => qs.push(prep({ type: 'tf', c: card })));
  groups.mc.forEach(card => qs.push(prep({ type: 'mc', c: card })));
  groups.wr.forEach(card => qs.push(prep({ type: 'type', c: card })));
  for (let i = 0; i < groups.mt.length; i += 5) {
    const part = groups.mt.slice(i, i + 5);
    if (part.length < 2) { qs.push(prep({ type: 'mc', c: part[0] })); continue; }
    const items = part.map(card => ({ c: card, ...qa(card, db.settings.dir === 'mix' ? 'td' : db.settings.dir) }));
    qs.push({ type: 'mt', items, answers: shuffle(items.map(x => x.a)) });
  }
  S = null;
  T = { deck: c.deck, setup: c, qs, ans: {}, done: false, start: now() };
  replace('test');
}
screens.test = () => {
  if (!T) return '';
  let num = 0;
  const sec = { tf: 'Верно или неверно', mc: 'Выберите ответ', type: 'Напишите ответ', mt: 'Сопоставьте' };
  let lastType = '';
  const html = T.qs.map((q, qi) => {
    let h = '';
    if (q.type !== lastType) { lastType = q.type; h += `<h2 class="sec">${sec[q.type]}</h2>`; }
    const res = T.done ? T.res[qi] : null;
    if (q.type === 'tf') {
      num++;
      const a = T.ans[qi];
      h += `<div class="card tq ${res ? (res.ok ? 'ok' : 'no') : ''}"><div class="tq-n">${num}</div><div class="ex-q sm">${esc(q.q)}</div><div class="tf-eq">=</div><div class="ex-q sm alt">${esc(q.shown)}</div>
        <div class="opts two">${[[0, '✗ Неверно'], [1, '✓ Верно']].map(([v, l]) => `<button class="opt sm ${a === v ? 'chosen' : ''} ${T.done && (v === 1) === q.truth ? 'good' : ''}" data-act="t-ans" data-q="${qi}" data-v="${v}" ${T.done ? 'disabled' : ''}>${l}</button>`).join('')}</div>
        ${res && !res.ok ? `<div class="hint">Правильный перевод: <b>${esc(q.a)}</b></div>` : ''}</div>`;
    } else if (q.type === 'mc') {
      num++;
      const a = T.ans[qi];
      h += `<div class="card tq ${res ? (res.ok ? 'ok' : 'no') : ''}"><div class="tq-n">${num}</div><div class="ex-q sm">${esc(q.q)}</div>
        <div class="opts">${q.opts.map((o, i) => `<button class="opt sm ${a === i ? 'chosen' : ''} ${T.done && o === q.a ? 'good' : ''} ${T.done && a === i && o !== q.a ? 'bad' : ''}" data-act="t-ans" data-q="${qi}" data-v="${i}" ${T.done ? 'disabled' : ''}>${esc(o)}</button>`).join('')}</div></div>`;
    } else if (q.type === 'type') {
      num++;
      h += `<div class="card tq ${res ? (res.ok ? 'ok' : 'no') : ''}"><div class="tq-n">${num}</div><div class="ex-q sm">${esc(q.q)}</div>
        <input class="ans" data-in="t-wr" data-q="${qi}" value="${esc(T.ans[qi] || '')}" placeholder="Ответ" autocomplete="off" autocapitalize="off" autocorrect="off" spellcheck="false" ${T.done ? 'readonly' : ''}>
        ${res && !res.ok ? `<div class="hint">Правильно: <b>${esc(q.a)}</b></div>` : ''}${res && res.typo ? `<div class="hint">Засчитано с опечаткой: <b>${esc(q.a)}</b></div>` : ''}</div>`;
    } else if (q.type === 'mt') {
      const letters = 'АБВГД';
      h += `<div class="card tq"><div class="mt-ans">${q.answers.map((a, i) => `<div><b>${letters[i]}</b> ${esc(a)}</div>`).join('')}</div>
        ${q.items.map((it, k) => {
          num++;
          const v = (T.ans[qi] || {})[k];
          const r = res ? res.items[k] : null;
          return `<div class="mt-row ${r === null ? '' : r ? 'ok' : 'no'}"><span class="tq-n">${num}</span><span class="mt-q">${esc(it.q)}</span>
            <select data-ch="t-mt" data-q="${qi}" data-k="${k}" ${T.done ? 'disabled' : ''}><option value="">—</option>${q.answers.map((a, i) => `<option value="${i}" ${String(v) === String(i) ? 'selected' : ''}>${letters[i]}</option>`).join('')}</select>
            ${res && !res.items[k] ? `<span class="hint">→ ${esc(it.a)}</span>` : ''}</div>`;
        }).join('')}</div>`;
    }
    return h;
  }).join('');
  const total = T.qs.reduce((s, q) => s + (q.type === 'mt' ? q.items.length : 1), 0);
  return `${hdr(T.done ? 'Результаты теста' : 'Тест')}<div class="pad test">
    ${T.done ? `<div class="card score"><div class="goal-ring">${ring(T.score / total, 96, 10)}<span>${Math.round(T.score / total * 100)}%</span></div>
      <div><div class="cta-t">${T.score} из ${total} верно</div><div class="cta-s">${T.score === total ? 'Идеально! 🏆' : T.score / total >= 0.8 ? 'Отличный результат!' : T.score / total >= 0.5 ? 'Неплохо, но есть над чем поработать' : 'Стоит ещё позаниматься'}</div>
      <div class="cta-s">Время: ${fmtTime(T.time)}</div></div></div>` : ''}
    ${html}
    ${T.done ? `<div class="done-btns">${T.wrongIds.length ? `<button class="btn wide" data-act="t-learn">Заучить ошибки (${T.wrongIds.length})</button>` : ''}
      <button class="btn ghost wide" data-act="t-new">Новый тест</button><button class="btn ghost wide" data-act="back">Готово</button></div>`
      : `<button class="btn wide" data-act="t-submit">Проверить тест</button>`}
  </div>`;
};
function submitTest() {
  let score = 0;
  const wrong = [];
  T.res = T.qs.map((q, qi) => {
    const a = T.ans[qi];
    if (q.type === 'tf') { const ok = a !== undefined && (a === 1) === q.truth; ok ? score++ : wrong.push(q.c.id); practice(q.c.id, ok); return { ok }; }
    if (q.type === 'mc') { const ok = a !== undefined && q.opts[a] === q.a; ok ? score++ : wrong.push(q.c.id); practice(q.c.id, ok); return { ok }; }
    if (q.type === 'type') { const r = checkAnswer(a || '', q.a); r.ok ? score++ : wrong.push(q.c.id); practice(q.c.id, r.ok); return r; }
    const items = q.items.map((it, k) => { const v = (a || {})[k]; const ok = v !== undefined && v !== '' && q.answers[v] === it.a; ok ? score++ : wrong.push(it.c.id); practice(it.c.id, ok); return ok; });
    return { items };
  });
  const total = T.qs.reduce((s, q) => s + (q.type === 'mt' ? q.items.length : 1), 0);
  T.done = true; T.score = score; T.wrongIds = wrong; T.time = now() - T.start;
  addXp(score * 5 + (score === total ? 30 : 0));
  if (score === total && total >= 10) db.flags.perfectTest = true;
  db.flags.tests = (db.flags.tests || 0) + 1;
  saveSoon();
  beep(score / total >= 0.6);
  render();
}

/* ───────────────────────── Прогресс ───────────────────────── */
function achievements() {
  const cards = allCards();
  const known = cards.filter(c => level(c.id) >= 2).length;
  const lessonsDone = Object.values(db.lessons).reduce((s, x) => s + Object.keys(x).length, 0);
  const bs = bestStreak(), xp = totalXp();
  return [
    ['🌱', 'Первые шаги', 'Пройти первый урок', lessonsDone >= 1],
    ['📚', 'Ученик', 'Пройти 10 уроков', lessonsDone >= 10],
    ['🎓', 'Магистр', 'Пройти 50 уроков', lessonsDone >= 50],
    ['🔥', 'Неделя силы воли', 'Серия 7 дней', bs >= 7],
    ['💎', 'Месяц без пропусков', 'Серия 30 дней', bs >= 30],
    ['🧠', '50 слов', 'Знать 50 слов', known >= 50],
    ['🏛️', '200 слов', 'Знать 200 слов', known >= 200],
    ['👑', '500 слов', 'Знать 500 слов', known >= 500],
    ['⭐', 'Тысяча XP', 'Набрать 1000 XP', xp >= 1000],
    ['🚀', '10 000 XP', 'Набрать 10 000 XP', xp >= 10000],
    ['⚡', 'Молния', '300 очков в спринте', (db.best.sprintMax || 0) >= 300],
    ['⏱️', 'Скорострел', 'Подбор быстрее 20 с', (db.best.matchMin || 99) < 20],
    ['🏆', 'Отличник', 'Тест из 10+ вопросов на 100%', !!db.flags.perfectTest],
    ['✍️', 'Автор', 'Создать свой набор', db.decks.length > 0],
  ];
}
screens.stats = () => {
  const cards = allCards();
  const cnt = [0, 0, 0, 0, 0];
  cards.forEach(c => cnt[level(c.id)]++);
  const days = Object.values(db.days);
  const ok = days.reduce((s, d) => s + d.ok, 0), bad = days.reduce((s, d) => s + d.bad, 0);
  // Активность за 18 недель
  const goal = db.settings.goal;
  const end = new Date(); end.setHours(12, 0, 0, 0);
  const startD = new Date(end); startD.setDate(startD.getDate() - 7 * 17 - ((startD.getDay() + 6) % 7));
  let cells = '';
  for (let d = new Date(startD); d <= end; d.setDate(d.getDate() + 1)) {
    const x = db.days[dayKey(d)]?.xp || 0;
    const l = x === 0 ? 0 : x < goal / 2 ? 1 : x < goal ? 2 : x < goal * 2 ? 3 : 4;
    cells += `<i class="h${l}" title="${dayKey(d)}: ${x} XP"></i>`;
  }
  // Последние 7 дней
  const week = [];
  for (let i = 6; i >= 0; i--) { const d = new Date(); d.setDate(d.getDate() - i); week.push({ k: dayKey(d), l: ['вс', 'пн', 'вт', 'ср', 'чт', 'пт', 'сб'][d.getDay()], xp: db.days[dayKey(d)]?.xp || 0 }); }
  const maxW = Math.max(goal, ...week.map(w => w.xp));
  const hard = smartCards('@hard').slice(0, 8);
  const ach = achievements();
  const lvRows = byLevel(langDecks().filter(d => d.builtin)).map(([lv, list]) => {
    const cs = list.flatMap(d => d.cards), k = cs.filter(c => level(c.id) >= 2).length;
    return `<div class="lv-row"><span><b class="lvl-tag">${esc(lv)}</b></span><div class="lv-bar"><i class="l3" style="width:${cs.length ? k / cs.length * 100 : 0}%"></i></div><b>${k}/${cs.length}</b></div>`;
  }).join('');
  return `${hdr('Прогресс', { root: true })}<div class="pad">
    ${langBar()}
    <div class="stats3">
      <div><b>🔥 ${streak()}</b><span>серия дней</span></div>
      <div><b>${bestStreak()}</b><span>рекорд серии</span></div>
      <div><b>${totalXp()}</b><span>всего XP</span></div>
    </div>
    <div class="stats3">
      <div><b>${cnt[2] + cnt[3] + cnt[4]}</b><span>слов знаю</span></div>
      <div><b>${dueCards().length}</b><span>к повторению</span></div>
      <div><b>${ok + bad ? Math.round(ok / (ok + bad) * 100) : 0}%</b><span>точность</span></div>
    </div>
    ${lvRows ? `<h2 class="sec">${LANGS[curLang()].flag} Путь к C1</h2><div class="card">${lvRows}<p class="hint">Сколько слов каждого уровня вы уже знаете (интервал повторения от 2 дней).</p></div>` : ''}
    <h2 class="sec">Неделя</h2>
    <div class="card week">${week.map(w => `<div class="wk"><div class="wk-bar"><i style="height:${w.xp / maxW * 100}%" class="${w.xp >= goal ? 'full' : ''}"></i></div><small>${w.l}</small><small class="wk-x">${w.xp || ''}</small></div>`).join('')}
      <div class="wk-goal" style="bottom:calc(${goal / maxW * 100}% * 0.72 + 34px)"></div></div>
    <h2 class="sec">Активность</h2>
    <div class="card heat"><div class="heat-g">${cells}</div><div class="legend">меньше <i class="h0"></i><i class="h1"></i><i class="h2"></i><i class="h3"></i><i class="h4"></i> больше</div></div>
    <h2 class="sec">Слова по уровням</h2>
    <div class="card">${[4, 3, 2, 1, 0].map(l => `<div class="lv-row"><span><i class="lvl-dot l${l}"></i>${LEVELS[l]}</span><div class="lv-bar"><i class="l${l}" style="width:${cards.length ? cnt[l] / cards.length * 100 : 0}%"></i></div><b>${cnt[l]}</b></div>`).join('')}
      <p class="hint">«Знакомо» — интервал повторения от 2 дней, «знаю» — от недели, «выучено» — от 3 недель.</p></div>
    ${hard.length ? `<h2 class="sec">Самые трудные слова</h2>${hard.map(cardRow).join('')}<button class="btn ghost wide" data-act="open-deck" data-id="@hard">Тренировать трудные</button>` : ''}
    <h2 class="sec">Достижения · ${ach.filter(a => a[3]).length}/${ach.length}</h2>
    <div class="ach">${ach.map(([e, t, s, ok]) => `<div class="ach-i ${ok ? 'on' : ''}"><b>${e}</b><span>${t}</span><small>${s}</small></div>`).join('')}</div>
  </div>`;
};

/* ───────────────────────── Ещё: справочник, настройки, копия ───────────────────────── */
screens.more = () => `${hdr('Ещё', { root: true })}<div class="pad">
  <button class="mrow" data-act="go" data-to="guide"><b>🧠</b><span>Как запоминать<small>14 статей о методиках запоминания в приложении</small></span>${I.chev}</button>
  <button class="mrow" data-act="go" data-to="settings"><b>⚙️</b><span>Настройки<small>Тема, цель дня, озвучка, уроки</small></span>${I.chev}</button>
  <button class="mrow" data-act="go" data-to="import"><b>📥</b><span>Импорт слов<small>Из Quizlet, Excel, заметок или файла</small></span>${I.chev}</button>
  <button class="mrow" data-act="go" data-to="backup"><b>💾</b><span>Резервная копия<small>Сохранить и перенести прогресс</small></span>${I.chev}</button>
  <button class="mrow" data-act="go" data-to="install"><b>📱</b><span>Установка на телефон<small>Android и iPhone</small></span>${I.chev}</button>
  <div class="about"><div class="logo">Мнемо</div><p class="hint center">Карточки · Заучивание · Тест · Подбор · Письмо · Диктант · Собери слово · Спринт · Интервальные повторения · Уроки<br>${nWords(cardIdx.size)} в ${DECKS.length} наборах на ${langList().length} языках. Все данные хранятся только на этом устройстве.</p></div>
</div>`;
screens.guide = () => `${hdr('Как запоминать')}<div class="pad">
  <p class="desc">Короткие статьи о том, как работает память, и как этим пользоваться.</p>
  ${(window.MNEMO_GUIDE || []).map(g => `<button class="mrow" data-act="go" data-to="article" data-id="${g.id}"><b>${g.emoji}</b><span>${esc(g.title)}</span>${I.chev}</button>`).join('')}
</div>`;
screens.article = c => {
  const g = (window.MNEMO_GUIDE || []).find(x => x.id === c.id);
  if (!g) return hdr('Статья');
  const list = window.MNEMO_GUIDE;
  const i = list.indexOf(g);
  const nx = list[i + 1];
  return `${hdr(`${g.emoji} ${esc(g.title)}`)}<div class="pad article">${g.body}
    ${nx ? `<button class="mrow" data-act="art-next" data-id="${nx.id}"><b>${nx.emoji}</b><span>Далее: ${esc(nx.title)}</span>${I.chev}</button>` : ''}</div>`;
};
function seg(k, opts) {
  return `<div class="seg">${opts.map(([v, l]) => `<button class="${String(db.settings[k]) === String(v) ? 'on' : ''}" data-act="set" data-k="${k}" data-v="${v}">${l}</button>`).join('')}</div>`;
}
function tgl(k, label, sub) {
  return `<label class="tgl row"><span>${label}${sub ? `<small>${sub}</small>` : ''}</span><input type="checkbox" class="sw" data-ch="set-bool" data-k="${k}" ${db.settings[k] ? 'checked' : ''}></label>`;
}
screens.settings = () => `${hdr('Настройки')}<div class="pad form">
  <div class="lbl">Оформление</div>${seg('theme', [['dark', '🌙 Тёмная'], ['light', '☀️ Светлая'], ['sepia', '📜 Сепия']])}
  <div class="lbl">Цель дня (XP)</div>${seg('goal', [[20, '20'], [50, '50'], [100, '100'], [200, '200'], [400, '400']])}
  <div class="lbl">Направление по умолчанию</div>${dirSeg()}
  <div class="lbl">Слов в одном уроке</div>${seg('lessonSize', [[4, '4'], [5, '5'], [6, '6'], [8, '8'], [10, '10']])}
  <div class="lbl">Новых слов в день в «Повторении»</div>${seg('newPerDay', [[0, '0'], [5, '5'], [10, '10'], [20, '20'], [50, '50']])}
  <div class="lbl">Скорость речи: <b id="rate-v">${db.settings.rate.toFixed(1)}</b></div>
  <input type="range" min="0.5" max="1.3" step="0.1" value="${db.settings.rate}" data-in="rate">
  <button class="btn ghost" data-act="speak" data-text="Hello! Let's learn some new words." data-lang="en-US">🔊 Проверить озвучку</button>
  ${tgl('autoplay', 'Автоозвучка', 'Произносить вопрос автоматически')}
  ${tgl('sound', 'Звуки и вибрация', 'Сигнал при верном и неверном ответе')}
  ${tgl('typos', 'Прощать опечатки', 'Засчитывать ответ с 1–2 ошибками в букве')}
  <div class="lbl">Опасная зона</div>
  <button class="btn ghost danger-t" data-act="reset-progress">Сбросить весь прогресс</button>
  <button class="btn ghost danger-t" data-act="reset-all">Удалить все данные и наборы</button>
</div>`;
screens.backup = () => `${hdr('Резервная копия')}<div class="pad form">
  <p class="desc">Копия содержит все наборы, прогресс, статистику и настройки. Сохраните её, чтобы перенести на другой телефон или не потерять при переустановке.</p>
  <button class="btn wide" data-act="bk-save">💾 Сохранить в файл</button>
  <button class="btn ghost wide" data-act="bk-share">📤 Поделиться копией</button>
  <div class="lbl">Восстановление</div>
  <label class="file-btn btn ghost">📂 Восстановить из файла<input type="file" accept=".json,application/json,text/*" data-file="backup"></label>
  <textarea id="bk-text" rows="4" placeholder="…или вставьте сюда текст копии"></textarea>
  <button class="btn ghost wide" data-act="bk-paste">Восстановить из текста</button>
  <p class="hint">Восстановление заменит текущие данные на устройстве.</p>
</div>`;
screens.install = () => `${hdr('Установка')}<div class="pad article">
  <h3>🤖 Android</h3>
  <p>Скачайте файл <b>Mnemo.apk</b> из раздела Releases репозитория и откройте его — разрешите установку из этого источника.</p>
  <h3>🍏 iPhone</h3>
  <ol><li>Откройте адрес веб-версии <b>в Safari</b> (в других браузерах на iOS установки нет).</li>
  <li>Нажмите «Поделиться» <b>⬆︎</b> → <b>«На экран „Домой“»</b>.</li>
  <li>Мнемо появится на экране как обычное приложение и будет работать без интернета.</li></ol>
  <p class="note">Данные на iPhone и Android хранятся отдельно. Чтобы перенести прогресс или наборы — используйте «Резервную копию» или «Экспорт текстом» набора.</p>
  ${location.protocol.startsWith('http') ? `<p class="hint">Адрес этой страницы: <b>${esc(location.href.split('#')[0])}</b></p><button class="btn ghost" data-act="copy-url">Скопировать адрес</button>` : ''}
</div>`;

function restoreBackup(text) {
  let data;
  try { data = JSON.parse(text); } catch (e) { toast('Это не похоже на резервную копию'); return; }
  if (!data || !Array.isArray(data.decks)) { toast('В файле нет данных Мнемо'); return; }
  confirmBox(`Восстановить копию? ${data.decks.length} наборов заменят текущие данные.`, 'Восстановить', () => {
    localStorage.setItem(KEY, JSON.stringify(data));
    load(); applyTheme(); save();
    toast('Копия восстановлена');
    tab('home');
  });
}

/* ───────────────────────── Тема ───────────────────────── */
const THEME_BG = { dark: '#14122B', light: '#F5F4FB', sepia: '#F3EAD7' };
function applyTheme() {
  const t = db.settings.theme;
  document.documentElement.dataset.theme = t;
  const m = $('meta[name="theme-color"]');
  if (m) m.setAttribute('content', THEME_BG[t]);
  try { if (window.Android && Android.setBars) Android.setBars(THEME_BG[t], t !== 'dark'); } catch (e) {}
}

/* ───────────────────────── События ───────────────────────── */
const A = {
  back: () => back(),
  tab: el => tab(el.dataset.tab),
  go: el => go(el.dataset.to, { id: el.dataset.id }),
  'art-next': el => replace('article', { id: el.dataset.id }),
  'modal-close': () => closeModal(),
  'modal-ok': () => { const cb = modalCb; closeModal(); cb && cb(); },
  speak: el => speak(el.dataset.text, el.dataset.lang),
  'speak-slow': el => speak(el.dataset.text, el.dataset.lang, true),
  star: el => {
    const c = cardIdx.get(el.dataset.id)?.c;
    if (!c) return;
    editCard(c, { star: !c.star });
    if ($('#modal').hidden) render(true);
  },
  set: el => {
    const k = el.dataset.k;
    let v = el.dataset.v;
    if (typeof DEF_SETTINGS[k] === 'number') v = Number(v);
    db.settings[k] = v;
    saveSoon();
    if (k === 'theme') applyTheme();
    if (F && k === 'dir') F.qa = F.cards.map(c => qa(c, v));
    render(true);
  },
  'set-active': el => { setActive(el.dataset.id); closeModal(); render(); },
  lang: el => { db.settings.lang = el.dataset.l; saveSoon(); render(); },
  'lvl-toggle': el => { const k = curLang() + ':' + el.dataset.l; db.settings.open[k] = !levelOpen(el.dataset.l); saveSoon(); render(true); },
  'pick-deck': () => deckPicker(),
  'open-deck': el => go('deck', { id: el.dataset.id }),
  'deck-filter': el => { cur().f = el.dataset.f; render(true); },
  mode: el => startMode(el.dataset.mode, el.dataset.deck),
  'deck-add': () => openModal(`<h3>Добавить</h3>
    <button class="mrow" data-act="deck-new"><b>📘</b><span>Новый набор<small>Пустой, карточки добавите вручную</small></span>${I.chev}</button>
    <button class="mrow" data-act="imp-open"><b>📥</b><span>Импорт текстом<small>Вставить список слов или загрузить файл</small></span>${I.chev}</button>
    <button class="mrow" data-act="restore-builtin"><b>♻️</b><span>Вернуть встроенные наборы<small>Если вы что-то удалили</small></span>${I.chev}</button>`),
  'deck-new': () => deckForm(null),
  'imp-open': () => { closeModal(); go('import', {}); },
  'restore-builtin': () => {
    closeModal();
    db.hidden = {}; db.removed = {};
    rebuild(); save(); render();
    toast('Встроенные наборы и слова восстановлены');
  },
  'deck-menu': el => {
    const d = deckById(el.dataset.id);
    openModal(`<h3>${esc(d.emoji)} ${esc(d.title)}</h3>
      <button class="mrow" data-act="deck-edit" data-id="${esc(d.id)}"><b>✏️</b><span>Название и языки</span>${I.chev}</button>
      <button class="mrow" data-act="card-new" data-deck="${esc(d.id)}"><b>➕</b><span>Добавить карточку</span>${I.chev}</button>
      <button class="mrow" data-act="deck-import" data-id="${esc(d.id)}"><b>📥</b><span>Импорт в этот набор</span>${I.chev}</button>
      <button class="mrow" data-act="deck-export" data-id="${esc(d.id)}"><b>📤</b><span>Экспорт текстом<small>Поделиться набором или перенести в Quizlet</small></span>${I.chev}</button>
      <button class="mrow" data-act="deck-swap" data-id="${esc(d.id)}"><b>🔄</b><span>Поменять стороны всех карточек</span>${I.chev}</button>
      <button class="mrow" data-act="deck-reset" data-id="${esc(d.id)}"><b>↩️</b><span>Сбросить прогресс набора</span>${I.chev}</button>
      <button class="mrow danger-t" data-act="deck-del" data-id="${esc(d.id)}"><b>🗑️</b><span>Удалить набор</span>${I.chev}</button>`);
  },
  'deck-edit': el => deckForm(deckById(el.dataset.id)),
  'deck-import': el => { closeModal(); go('import', { deck: el.dataset.id }); },
  'deck-export': el => { closeModal(); const d = deckById(el.dataset.id); shareText(exportDeckText(d), d.title); },
  'deck-swap': el => {
    const d = deckById(el.dataset.id);
    confirmBox('Поменять местами слово и перевод во всех карточках набора?', 'Поменять', () => {
      d.cards.forEach(c => editCard(c, { t: c.d, d: c.t }));
      editDeck(d, { front: d.back, back: d.front });
      render(true);
    }, false);
  },
  'deck-reset': el => {
    const d = deckById(el.dataset.id);
    confirmBox(`Сбросить прогресс по ${nWords(d.cards.length)} набора «${esc(d.title)}»?`, 'Сбросить', () => {
      d.cards.forEach(c => { delete db.prog[c.id]; });
      delete db.lessons[d.id];
      saveSoon(); render(true);
    });
  },
  'deck-del': el => {
    const d = deckById(el.dataset.id);
    confirmBox(`Удалить набор «${esc(d.title)}» и все его карточки?`, 'Удалить', () => {
      if (d.builtin) db.hidden[d.id] = 1;
      else db.decks = db.decks.filter(x => x !== d);
      d.cards.forEach(c => { delete db.prog[c.id]; });
      delete db.lessons[d.id];
      rebuild(); saveSoon();
      back(true);
    });
  },
  'edit-card': el => { const x = cardIdx.get(el.dataset.id); if (x) cardForm(x.c, x.d.id); },
  'card-new': el => cardForm(null, el.dataset.deck),
  'card-del': el => {
    const x = cardIdx.get(el.dataset.id);
    if (!x) return;
    confirmBox(`Удалить карточку «${esc(x.c.t)}»?`, 'Удалить', () => {
      removeCard(x.d, x.c);
      closeModal(); render(true);
    });
  },
  'imp-sep': el => { const c = cur(); readImportForm(c); c.sep = el.dataset.v; render(true); },
  'imp-do': () => {
    const c = cur();
    readImportForm(c);
    const items = parseImport(c.text, c.sep || 'auto', c.swap);
    if (!items.length) { toast('Не удалось распознать ни одной карточки'); return; }
    let d = c.deck ? deckById(c.deck) : null;
    if (!d) {
      const front = c.front || LANGS[curLang()].front;
      d = { id: 'u' + uid(), title: (c.title || '').trim() || 'Мой набор', emoji: '📘', desc: '', front, back: c.back || 'ru-RU', lang: langOfCode(front), created: now(), cards: [] };
      db.decks.unshift(d);
      rebuild();
      d = deckById(d.id);
    }
    items.forEach(x => addCard(d, { id: 'c' + uid(), ...x }, true));
    reindex(); save();
    toast(`Добавлено: ${nCards(items.length)}`);
    stack.pop();
    if (cur().name === 'deck' && cur().id === d.id) render();
    else go('deck', { id: d.id });
  },
  'node': el => nodeSheet(activeDeck(), Number(el.dataset.n)),
  'lesson-start': el => startLesson(el.dataset.deck, Number(el.dataset.n)),
  // упражнения
  'ex-next': () => nextEx(),
  'ex-opt': el => { if (S.fb) return; const ex = S.cur; ex.picked = Number(el.dataset.i); answer(ex.opts[ex.picked] === ex.a); },
  'ex-tf': el => { if (S.fb) return; const ex = S.cur; ex.picked = el.dataset.v === '1'; answer(ex.picked === ex.truth); },
  'ex-dunno': () => { const ex = S.cur; if (ex.type === 'scramble') ex.fill = []; answer(false, { dunno: true }); },
  'ex-hint': () => { const ex = S.cur; ex.typed = $('#ans')?.value || ''; ex.hint = Math.min(ex.a.length, (ex.hint || 0) + 1); render(true); },
  'ex-tr': () => { const ex = S.cur; ex.typed = $('#ans')?.value || ''; ex.showTr = true; render(true); },
  'ex-check': () => {
    if (!S || S.fb) { if (S && S.fb) nextEx(); return; }
    const ex = S.cur;
    const v = $('#ans')?.value || '';
    if (!v.trim()) { toast('Введите ответ или нажмите «Не знаю»'); return; }
    ex.typed = v;
    const r = checkAnswer(v, ex.a);
    answer(r.ok, { typo: r.typo });
  },
  'ex-over': () => overrideAnswer(),
  'scr-add': el => {
    const ex = S.cur;
    ex.fill.push(Number(el.dataset.i));
    if (ex.fill.length === ex.slots.length) {
      const built = ex.fill.map(i => ex.tiles[i]).join('').toLowerCase();
      const target = ex.slots.map(i => ex.chars[i]).join('').toLowerCase();
      answer(built === target);
    } else render(true);
  },
  'scr-del': el => { const ex = S.cur; if (S.fb) return; ex.fill.splice(Number(el.dataset.k), 1); render(true); },
  'scr-clear': () => { S.cur.fill = []; render(true); },
  'scr-hint': () => {
    const ex = S.cur;
    // Оставляем верное начало, ставим следующую правильную букву.
    let k = 0;
    while (k < ex.fill.length && ex.tiles[ex.fill[k]].toLowerCase() === ex.chars[ex.slots[k]].toLowerCase()) k++;
    ex.fill = ex.fill.slice(0, k);
    const need = ex.chars[ex.slots[k]].toLowerCase();
    const ti = ex.tiles.findIndex((ch, i) => ch.toLowerCase() === need && !ex.fill.includes(i));
    if (ti >= 0) A['scr-add']({ dataset: { i: ti } });
  },
  'ex-match': el => {
    const ex = S.cur;
    if (S.fb) return;
    const side = el.dataset.side, i = Number(el.dataset.i);
    const item = (side === 'L' ? ex.left : ex.right)[i];
    if (side === 'L') speak(item.text, langsOf(cardIdx.get(item.id).c).fl);
    if (!ex.sel || ex.sel.side === side) { ex.sel = { side, i }; render(true); return; }
    const other = (ex.sel.side === 'L' ? ex.left : ex.right)[ex.sel.i];
    if (other.id === item.id) {
      ex.done.push(item.id);
      ex.sel = null;
      vibrate(15);
      if (ex.done.length === ex.cards.length) {
        ex.cards.forEach(c => practice(c.id, !ex.badIds.includes(c.id)));
        ex.noGrade = true;
        answer(ex.errors === 0);
        return;
      }
    } else {
      ex.errors++;
      ex.badIds.push(other.id, item.id);
      ex.flash = [ex.sel, { side, i }];
      ex.sel = null;
      beep(false);
      setTimeout(() => { if (S && S.cur === ex) { ex.flash = null; render(true); } }, 450);
    }
    render(true);
  },
  'done-next': () => { const r = cur().res; if (!r.next) return; stack.pop(); r.next(); },
  'done-again': () => { const r = cur().res; if (!r.again) return; stack.pop(); r.again(); },
  'done-mist': () => { const r = cur().res; stack.pop(); startMode('learn', r.deck || '@all', r.mist); },
  // повторение
  'rv-show': () => { R.shown = true; render(true); if (db.settings.autoplay) speak(R.qa.a, R.qa.al); },
  'rv-grade': el => reviewGrade(Number(el.dataset.q)),
  // карточки
  'fl-flip': el => { if (el.dataset.swiped) return; F.flip = !F.flip; render(true); if (F.flip && db.settings.autoplay) speak(F.qa[F.i].a, F.qa[F.i].al); },
  'fl-mark': el => flMark(el.dataset.v === '1'),
  'fl-undo': () => {
    const h = F.hist.pop();
    if (!h) return;
    F.i = h.i; F.flip = false;
    if (h.v === true) F.know.pop(); else if (h.v === false) F.learn.pop();
    render(true);
  },
  'fl-shuffle': () => {
    F.shuffled = !F.shuffled;
    const rest = F.cards.slice(F.i);
    const ordered = F.shuffled ? shuffle(rest) : F.all.filter(c => rest.includes(c));
    F.cards = [...F.cards.slice(0, F.i), ...ordered];
    F.qa = F.cards.map(c => qa(c, F.dir));
    render(true);
  },
  'fl-auto': () => { F.auto = !F.auto; stopTimers(); render(true); },
  'fl-relearn': () => { const l = F.learn; F.cards = shuffle(l); F.qa = F.cards.map(c => qa(c, F.dir)); F.i = 0; F.know = []; F.learn = []; F.hist = []; F.flip = false; render(); },
  'fl-restart': () => { F.cards = F.shuffled ? shuffle(F.all) : [...F.all]; F.qa = F.cards.map(c => qa(c, F.dir)); F.i = 0; F.know = []; F.learn = []; F.hist = []; F.flip = false; render(); },
  'fl-learnmode': () => { const l = F.learn, id = F.deckId; stack.pop(); startMode('learn', id, l); },
  // подбор и спринт
  'mg-tap': el => mgTap(Number(el.dataset.i)),
  'mg-again': () => { const d = getDeck(MG.deckId) || { id: MG.deckId, cards: MG.cards }; const cards = MG.cards; stack.pop(); startMatchGame(d, cards); },
  'sp-ans': el => spAnswer(el.dataset.v === '1'),
  'sp-again': () => { const d = getDeck(SP.deckId) || { id: SP.deckId }; const cards = SP.cards; stack.pop(); startSprint(d, cards); },
  // тест
  't-start': () => buildTest(cur()),
  't-ans': el => { T.ans[Number(el.dataset.q)] = Number(el.dataset.v); render(true); },
  't-submit': () => {
    const total = T.qs.length;
    const answered = T.qs.filter((q, i) => T.ans[i] !== undefined && T.ans[i] !== '').length;
    if (answered < total) confirmBox(`Отвечено ${answered} из ${total} блоков. Проверить тест?`, 'Проверить', submitTest, false);
    else submitTest();
  },
  't-learn': () => { const ids = T.wrongIds; const deck = T.deck; startMode('learn', deck, [...new Set(ids)].map(id => cardIdx.get(id)?.c).filter(Boolean)); },
  't-new': () => { const s = T.setup; replace('testsetup', { deck: s.deck, ids: s.ids, n: s.n, types: s.types }); },
  // настройки и копия
  'reset-progress': () => confirmBox('Сбросить прогресс всех слов, уроков и статистику? Наборы останутся.', 'Сбросить', () => {
    db.prog = {}; db.lessons = {}; db.days = {}; db.best = {}; db.flags = {};
    save(); tab('home'); toast('Прогресс сброшен');
  }),
  'reset-all': () => confirmBox('Удалить все наборы, прогресс и настройки? Встроенные наборы появятся заново.', 'Удалить всё', () => {
    localStorage.removeItem(KEY); load(); applyTheme(); save(); tab('home'); toast('Данные удалены');
  }),
  'bk-save': () => { save(); saveFile(`mnemo-backup-${dayKey(now())}.json`, JSON.stringify(db)); },
  'bk-share': () => { save(); shareText(JSON.stringify(db), 'Мнемо — резервная копия'); },
  'bk-paste': () => { const t = $('#bk-text').value.trim(); if (!t) { toast('Вставьте текст копии'); return; } restoreBackup(t); },
  'copy-url': () => copyText(location.href.split('#')[0]),
};

document.addEventListener('click', e => {
  const m = $('#modal');
  if (e.target === m) { closeModal(); return; }
  const el = e.target.closest('[data-act]');
  if (!el || el.disabled) return;
  const fn = A[el.dataset.act];
  if (!fn) return;
  e.preventDefault();
  e.stopPropagation();
  try { fn(el, e); } catch (err) { console.error(err); toast('Ошибка: ' + err.message); }
});
document.addEventListener('input', e => {
  const el = e.target;
  const k = el.dataset.in;
  if (!k) return;
  if (k === 'dsearch') {
    const c = cur(); c.q = el.value;
    const q = c.q.toLowerCase();
    $('#dlist').innerHTML = decksHtml(q);
  } else if (k === 't-n') { cur().n = Number(el.value); $('#t-n').textContent = el.value; }
  else if (k === 't-wr') { T.ans[Number(el.dataset.q)] = el.value; }
  else if (k === 'rate') { db.settings.rate = Number(el.value); $('#rate-v').textContent = Number(el.value).toFixed(1); saveSoon(); }
});
let impT = 0;
document.addEventListener('input', e => {
  if (e.target.id === 'imp-text' || e.target.id === 'imp-swap') { clearTimeout(impT); impT = setTimeout(() => updateImportPreview(cur()), 150); }
});
document.addEventListener('change', e => {
  const el = e.target;
  if (el.id === 'imp-swap') { updateImportPreview(cur()); return; }
  if (el.dataset.file) { readFile(el); return; }
  const k = el.dataset.ch;
  if (!k) return;
  if (k === 'set-bool') { db.settings[el.dataset.k] = el.checked; saveSoon(); }
  else if (k === 't-type') { cur().types[el.dataset.k] = el.checked; }
  else if (k === 't-mt') { const qi = Number(el.dataset.q); T.ans[qi] = T.ans[qi] || {}; T.ans[qi][el.dataset.k] = el.value === '' ? '' : Number(el.value); }
});
function readFile(input) {
  const f = input.files && input.files[0];
  if (!f) return;
  const r = new FileReader();
  r.onload = () => {
    const text = String(r.result || '');
    if (input.dataset.file === 'backup') restoreBackup(text);
    else {
      const c = cur();
      readImportForm(c);
      c.text = text;
      if (!c.deck && !c.title) c.title = f.name.replace(/\.[^.]+$/, '');
      render(true);
    }
  };
  r.readAsText(f);
  input.value = '';
}
document.addEventListener('submit', e => {
  const f = e.target;
  e.preventDefault();
  const v = name => (f.elements[name]?.value || '').trim();
  if (f.dataset.form === 'deck') {
    let d = f.dataset.id && deckById(f.dataset.id);
    const isNew = !d;
    const fields = { title: v('title') || 'Мой набор', emoji: v('emoji') || '📘', desc: v('desc'), front: v('front'), back: v('back') };
    if (!d) { d = { id: 'u' + uid(), created: now(), cards: [], ...fields, lang: langOfCode(fields.front) }; db.decks.unshift(d); }
    else { if (!d.builtin) fields.lang = langOfCode(fields.front); editDeck(d, fields); }
    if (d.lang !== curLang()) db.settings.lang = d.lang;
    rebuild(); saveSoon(); closeModal();
    if (isNew) { go('deck', { id: d.id }); cardForm(null, d.id); } else render(true);
  } else if (f.dataset.form === 'card') {
    const t = v('t'), d = v('d');
    if (!t || !d) return;
    const fields = { t, d, ex: v('ex'), exT: v('exT'), note: v('note') };
    if (f.dataset.id) {
      editCard(cardIdx.get(f.dataset.id).c, fields);
      closeModal();
      if (S && S.cur && S.cur.c && S.cur.c.id === f.dataset.id) S.cur.c = cardIdx.get(f.dataset.id).c;
      render(true);
    } else {
      const deck = deckById(f.dataset.deck);
      if (!deck) return;
      addCard(deck, { id: 'c' + uid(), ...fields });
      toast(`Добавлено: ${t}`);
      f.reset();
      f.elements.t.focus();
      render(true);
    }
  }
});
document.addEventListener('keydown', e => {
  const tag = (e.target.tagName || '').toLowerCase();
  if (e.key === 'Enter' && e.target.dataset && e.target.dataset.enter) { e.preventDefault(); A[e.target.dataset.enter](); return; }
  if (!$('#modal').hidden) { if (e.key === 'Escape') closeModal(); return; }
  if (tag === 'input' || tag === 'textarea' || tag === 'select') return;
  const name = cur().name;
  if (e.key === 'Escape') { back(); return; }
  if (name === 'run' && S) {
    if (S.fb && (e.key === 'Enter' || e.key === ' ')) { e.preventDefault(); nextEx(); return; }
    const n = Number(e.key);
    if (!S.fb && n >= 1 && n <= 4 && S.cur.opts) { const b = $$('.opt')[n - 1]; if (b) b.click(); }
    if (!S.fb && S.cur.type === 'intro' && e.key === 'Enter') nextEx();
  } else if (name === 'flash' && F && F.i < F.cards.length) {
    if (e.key === ' ' || e.key === 'ArrowUp' || e.key === 'ArrowDown') { e.preventDefault(); F.flip = !F.flip; render(true); }
    if (e.key === 'ArrowRight') flMark(true);
    if (e.key === 'ArrowLeft') flMark(false);
  } else if (name === 'review' && R) {
    if (!R.shown && (e.key === ' ' || e.key === 'Enter')) { e.preventDefault(); A['rv-show'](); }
    else if (R.shown && ['1', '2', '3', '4'].includes(e.key)) reviewGrade(Number(e.key) - 1);
  } else if (name === 'sprint' && SP && !SP.over) {
    if (e.key === 'ArrowLeft') spAnswer(false);
    if (e.key === 'ArrowRight') spAnswer(true);
  }
});

/* ───────────────────────── Старт ───────────────────────── */
buildBuiltin();
load();
applyTheme();
render();
save();
if ('serviceWorker' in navigator && location.protocol.startsWith('http')) {
  window.addEventListener('load', () => navigator.serviceWorker.register('sw.js').catch(() => {}));
}
window.__mnemo = { get db() { return db; }, get decks() { return DECKS; }, get S() { return S; }, get R() { return R; }, checkAnswer, parseImport, lessonNodes, buildLesson, clozeOf, nextState };
})();
