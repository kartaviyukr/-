'use strict';
/* Скипетр — королевский личный планер.
   Все данные хранятся локально (localStorage) в объекте S. */

const KEY = 'skipetr.v1';
const $ = (s, r = document) => r.querySelector(s);
const esc = s => String(s ?? '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const uid = () => Math.random().toString(36).slice(2, 9);
const T = (t = '') => ({ t, d: false });

/* ---------------- Constants ---------------- */
const MONTHS = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'];
const MONTHS_GEN = ['января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'];
const MON_S = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];
const WD = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
const WDL = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота', 'Воскресенье'];
const MOODS = ['😣', '😕', '😐', '🙂', '🤩'];
const MOOD_C = ['#c2385a', '#d9783a', '#9a8f7a', '#3aa57d', '#f7e3a1'];
const SPHERES = ['Здоровье', 'Карьера', 'Финансы', 'Отношения', 'Семья', 'Развитие', 'Отдых', 'Духовность'];
const THEMES = [
  { id: 'midnight', name: 'Полночь', bg: '#0e0a1f', g: 'linear-gradient(135deg,#2a1650,#0e0a1f)' },
  { id: 'bordeaux', name: 'Бордо', bg: '#1a0710', g: 'linear-gradient(135deg,#4a0f24,#1a0710)' },
  { id: 'emerald', name: 'Изумруд', bg: '#04140f', g: 'linear-gradient(135deg,#0d3b2c,#04140f)' },
  { id: 'ivory', name: 'Слоновая кость', bg: '#f7f1e3', g: 'linear-gradient(135deg,#efe0bd,#f7f1e3)', light: true },
];
const REVIEW_STEPS = [
  'Разобрать «Входящие» до нуля',
  'Просмотреть прошедшую неделю и календарь',
  'Проверить цели месяца и года',
  'Перенести незавершённое или отпустить',
  'Выбрать большие камни на следующую неделю',
  'Запланировать время для себя и отдыха',
];
const QUOTES = [
  ['Лучший способ предсказать будущее — создать его.', 'Питер Друкер'],
  ['Если тебе нужно съесть лягушку, лучше сделать это с утра.', 'Марк Твен'],
  ['Что важно, редко бывает срочным, а что срочно, редко бывает важным.', 'Дуайт Эйзенхауэр'],
  ['Планы — ничто, планирование — всё.', 'Дуайт Эйзенхауэр'],
  ['Главное — не расставлять приоритеты в расписании, а вносить в расписание свои приоритеты.', 'Стивен Кови'],
  ['Мы — то, что мы делаем постоянно. Совершенство — не действие, а привычка.', 'Аристотель'],
  ['Путь в тысячу ли начинается с первого шага.', 'Лао-цзы'],
  ['Не важно, как медленно ты идёшь, главное — не останавливаться.', 'Конфуций'],
  ['Либо ты управляешь днём, либо день управляет тобой.', 'Джим Рон'],
  ['Дисциплина — мост между целями и достижениями.', 'Джим Рон'],
  ['Мы переоцениваем то, что можем сделать за год, и недооцениваем то, что можем сделать за десять лет.', 'Билл Гейтс'],
  ['Сначала делай необходимое, затем возможное — и вдруг ты делаешь невозможное.', 'Франциск Ассизский'],
  ['Цель без плана — всего лишь желание.', 'Антуан де Сент-Экзюпери'],
  ['Будь собой. Прочие роли уже заняты.', 'Оскар Уайльд'],
  ['Делай что должно, и будь что будет.', 'Девиз рыцарей'],
  ['Корона не делает королём. Королём делают поступки.', 'Королевская мудрость'],
  ['Сделано — лучше, чем идеально.', 'Мудрость мастеров'],
  ['Ваш разум — для того, чтобы рождать идеи, а не хранить их.', 'Дэвид Аллен'],
  ['Вы не поднимаетесь до уровня своих целей. Вы опускаетесь до уровня своих систем.', 'Джеймс Клир'],
  ['Каждое действие — это голос за того человека, которым вы хотите стать.', 'Джеймс Клир'],
  ['Счастье — когда то, что ты думаешь, говоришь и делаешь, в гармонии.', 'Махатма Ганди'],
  ['Утро вечера мудренее.', 'Русская пословица'],
  ['Глаза боятся, а руки делают.', 'Русская пословица'],
  ['Кто рано встаёт, тому Бог подаёт.', 'Русская пословица'],
  ['Время — это то, чего нам больше всего хочется и что мы хуже всего используем.', 'Уильям Пенн'],
  ['Сосредоточься на том, что в твоей власти, и отпусти остальное.', 'Эпиктет'],
  ['Хорошо начатое — наполовину сделано.', 'Аристотель'],
  ['Если вы не спланируете свою жизнь, это сделает за вас кто-то другой.', 'Джим Рон'],
  ['Дорогу осилит идущий.', 'Пословица'],
  ['Скромные дела, сделанные каждый день, создают великие свершения.', 'Мудрость'],
  ['Королевство строится камень за камнем.', 'Королевская мудрость'],
];

const DEF_HABITS = [
  { id: 'h1', icon: '💧', name: 'Вода 2 л' },
  { id: 'h2', icon: '🏃', name: 'Движение' },
  { id: 'h3', icon: '📖', name: 'Чтение' },
  { id: 'h4', icon: '🧘', name: 'Медитация' },
  { id: 'h5', icon: '🌙', name: 'Сон до 23:00' },
];
const DEF_ROLES = [
  { id: 'r1', name: 'Я — личность' },
  { id: 'r2', name: 'Семья' },
  { id: 'r3', name: 'Работа' },
  { id: 'r4', name: 'Друзья' },
  { id: 'r5', name: 'Здоровье' },
];

/* ---------------- State ---------------- */
function fresh() {
  return {
    v: 1, days: {}, weeks: {}, months: {}, years: {},
    habits: JSON.parse(JSON.stringify(DEF_HABITS)),
    roles: JSON.parse(JSON.stringify(DEF_ROLES)),
    inbox: [], matrix: { q1: [], q2: [], q3: [], q4: [] },
    settings: { name: '', theme: 'midnight', dayStart: 6, dayEnd: 23 },
    pomo: { mode: 'focus', running: false, endAt: 0, remain: 25 * 60 },
  };
}
// Fill missing keys of obj from defaults (deep for plain objects).
function def(obj, d) {
  for (const k in d) {
    if (obj[k] === undefined || obj[k] === null) obj[k] = JSON.parse(JSON.stringify(d[k]));
    else if (d[k] && typeof d[k] === 'object' && !Array.isArray(d[k]) && typeof obj[k] === 'object') def(obj[k], d[k]);
  }
  return obj;
}
let S;
try { S = JSON.parse(localStorage.getItem(KEY)); } catch (e) { S = null; }
if (!S || !S.v) S = fresh();
def(S, fresh());

let saveT = 0;
function flush() {
  clearTimeout(saveT);
  try { localStorage.setItem(KEY, JSON.stringify(S)); } catch (e) { toast('Не удалось сохранить'); }
}
function save() { clearTimeout(saveT); saveT = setTimeout(flush, 300); }
window.appFlush = flush;
document.addEventListener('visibilitychange', () => { if (document.hidden) flush(); });
window.addEventListener('pagehide', flush);

const getP = p => p.split('.').reduce((o, k) => (o == null ? undefined : o[k]), S);
function setP(p, v) {
  const ks = p.split('.'); const last = ks.pop();
  let o = S;
  for (const k of ks) { if (o[k] == null) o[k] = {}; o = o[k]; }
  o[last] = v;
}

/* ---------------- Dates ---------------- */
const pad = n => String(n).padStart(2, '0');
const ymd = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const pd = s => { const [a, b, c] = s.split('-').map(Number); return new Date(a, b - 1, c || 1); };
const addD = (d, n) => { const x = new Date(d.getFullYear(), d.getMonth(), d.getDate()); x.setDate(x.getDate() + n); return x; };
const wdi = d => (d.getDay() + 6) % 7;
const monday = d => addD(d, -wdi(d));
const today = () => ymd(new Date());
const daysIn = (y, m) => new Date(y, m + 1, 0).getDate();
function isoWeek(d) {
  const t = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
  const dn = t.getUTCDay() || 7; t.setUTCDate(t.getUTCDate() + 4 - dn);
  const y0 = new Date(Date.UTC(t.getUTCFullYear(), 0, 1));
  return Math.ceil(((t - y0) / 864e5 + 1) / 7);
}
const weekKey = d => 'W' + ymd(monday(d));
const monthKey = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}`;
const dayOfYear = d => Math.floor((d - new Date(d.getFullYear(), 0, 0)) / 864e5);

/* ---------------- Data accessors ---------------- */
const DAY_DEF = () => ({
  intention: '', energy: 0, mood: 0, frog: T(),
  tasks: { big: [T()], mid: [T(), T(), T()], small: [T(), T(), T(), T(), T()] },
  blocks: {}, habits: {}, grat: ['', '', ''], wins: '', improve: '', rating: 0, pomos: 0,
});
function day(k) { return def(S.days[k] || (S.days[k] = {}), DAY_DEF()); }
function week(k) {
  return def(S.weeks[k] || (S.weeks[k] = {}), { focus: '', motto: '', rocks: {}, goals: [], review: { checks: {}, wins: '', lessons: '', next: '', rating: 0 } });
}
function month(k) {
  return def(S.months[k] || (S.months[k] = {}), { theme: '', goals: [], events: [], review: { wins: '', lessons: '', rating: 0 } });
}
function year(k) {
  return def(S.years[k] || (S.years[k] = {}), { word: '', vision: '', wheel: {}, okrs: [], quarters: ['', '', '', ''], review: '', dreams: [] });
}
function dayItems(d) {
  if (!d) return [];
  const t = d.tasks || {};
  return [d.frog, ...(t.big || []), ...(t.mid || []), ...(t.small || [])].filter(x => x && x.t && x.t.trim());
}
function dayProgress(d) {
  const it = dayItems(d);
  if (!it.length) return null;
  return it.filter(x => x.d).length / it.length;
}
function listProgress(arr) {
  const it = (arr || []).filter(x => x.t && x.t.trim());
  return it.length ? it.filter(x => x.d).length / it.length : null;
}
function streak(hid) {
  let n = 0; let d = new Date();
  if (!(S.days[ymd(d)] && S.days[ymd(d)].habits && S.days[ymd(d)].habits[hid])) d = addD(d, -1);
  for (;;) {
    const x = S.days[ymd(d)];
    if (x && x.habits && x.habits[hid]) { n++; d = addD(d, -1); } else break;
  }
  return n;
}

/* ---------------- UI state ---------------- */
const U = { tab: 'day', cur: today(), sub: null };

/* ---------------- HTML helpers ---------------- */
const card = (title, icon, body, tag = '') =>
  `<section class="card"><h2 class="card-h"><span class="ci">${icon}</span>${title}${tag ? `<span class="tag">${tag}</span>` : ''}</h2>${body}</section>`;
const inp = (p, v, ph = '', cls = '') =>
  `<input class="in ${cls}" data-bind="${p}" value="${esc(v)}" placeholder="${esc(ph)}" autocomplete="off">`;
const ta = (p, v, ph = '', cls = '') =>
  `<textarea class="ta ${cls}" data-bind="${p}" placeholder="${esc(ph)}">${esc(v)}</textarea>`;
const chk = (p, on, round = false) =>
  `<button class="chk ${round ? 'round' : ''} ${on ? 'on' : ''}" data-act="tog" data-p="${p}" aria-label="Готово"></button>`;
const prog = v => `<div class="prog"><i style="width:${Math.round((v || 0) * 100)}%"></i></div>`;
function pills(p, val, labels, cls = '') {
  return `<div class="pills ${cls}">${labels.map((l, i) =>
    `<button class="pill ${cls.includes('emo') ? 'emo' : ''} ${val === i + 1 ? 'on' : ''}" data-act="set" data-p="${p}" data-v="${i + 1}">${l}</button>`).join('')}</div>`;
}
function taskRow(p, it, ph = 'Задача…') {
  return `<div class="task ${it.d ? 'done' : ''}">${chk(p + '.d', it.d)}${inp(p + '.t', it.t, ph, 'flat')}</div>`;
}
// Editable list of {t,d} items stored at path p.
function dlist(p, ph = 'Добавить…', withCheck = true) {
  const arr = getP(p) || [];
  const rows = arr.map((it, i) =>
    `<div class="task ${it.d ? 'done' : ''}">${withCheck ? chk(`${p}.${i}.d`, it.d) : '<span style="color:var(--gold)">◆</span>'}${inp(`${p}.${i}.t`, it.t, '', 'flat')}<button class="x" data-act="del" data-p="${p}" data-i="${i}">×</button></div>`).join('');
  return `${rows}<div class="addrow"><input class="in add-in" data-addp="${p}" placeholder="${esc(ph)}" autocomplete="off"><button class="btn" data-act="add" data-p="${p}">＋</button></div>`;
}
function ring(v, size = 100, color = 'var(--gold)') {
  const r = 46, c = 2 * Math.PI * r;
  return `<svg viewBox="0 0 ${size} ${size}"><circle cx="50" cy="50" r="${r}" fill="none" stroke="${color}" stroke-width="5" stroke-dasharray="${c}" stroke-dashoffset="${c * (1 - v)}" stroke-linecap="round" opacity=".85"/></svg>`;
}

/* ---------------- Header ---------------- */
function header() {
  const d = pd(U.cur);
  let title = 'Скипетр', sub = '', nav = '';
  const arrows = isToday => `<button class="ib" data-act="shift" data-n="-1">‹</button>${isToday ? '' : '<button class="chip" data-act="today">Сегодня</button>'}<button class="ib" data-act="shift" data-n="1">›</button>`;
  if (U.tab === 'day') {
    title = WDL[wdi(d)];
    sub = `${d.getDate()} ${MONTHS_GEN[d.getMonth()]} ${d.getFullYear()}`;
    nav = arrows(U.cur === today());
  } else if (U.tab === 'week') {
    const m = monday(d), e = addD(m, 6);
    title = `Неделя ${isoWeek(d)}`;
    sub = `${m.getDate()} ${MONTHS_GEN[m.getMonth()]} — ${e.getDate()} ${MONTHS_GEN[e.getMonth()]}`;
    nav = arrows(weekKey(d) === weekKey(new Date()));
  } else if (U.tab === 'month') {
    title = MONTHS[d.getMonth()];
    sub = `${d.getFullYear()} · месяц`;
    nav = arrows(monthKey(d) === monthKey(new Date()));
  } else if (U.tab === 'year') {
    title = `${d.getFullYear()} год`;
    sub = 'Королевский замысел';
    nav = arrows(d.getFullYear() === new Date().getFullYear());
  } else {
    const subs = { matrix: 'Матрица Эйзенхауэра', inbox: 'Входящие', pomo: 'Помодоро', habits: 'Привычки', roles: 'Роли', guide: 'Методики', settings: 'Настройки' };
    title = U.sub ? subs[U.sub] : 'Сокровищница';
    sub = U.sub ? 'Инструменты' : 'Инструменты и методики';
    nav = U.sub ? '<button class="ib" data-act="back">‹</button>' : '';
  }
  $('#title').textContent = title;
  $('#subtitle').textContent = sub;
  $('#tnav').innerHTML = nav;
  document.querySelectorAll('#tabs button').forEach(b => b.classList.toggle('on', b.dataset.tab === U.tab));
}

/* ---------------- DAY ---------------- */
function greeting() {
  const h = new Date().getHours();
  const n = S.settings.name ? `, ${esc(S.settings.name)}` : '';
  if (h < 5) return `Доброй ночи${n}`;
  if (h < 12) return `Доброе утро${n}`;
  if (h < 18) return `Добрый день${n}`;
  return `Добрый вечер${n}`;
}
function viewDay() {
  const k = U.cur, d = day(k), P = `days.${k}`;
  const dt = pd(k);
  const q = QUOTES[(dayOfYear(dt) + dt.getFullYear()) % QUOTES.length];
  const p = dayProgress(d);
  const isToday = k === today();
  let h = `<div class="hero fade"><h1>${isToday ? greeting() : WDL[wdi(dt)]}</h1>
    <div class="quote">«${esc(q[0])}»</div><div class="who">— ${esc(q[1])}</div></div>`;

  h += card('Утро монарха', '☀️',
    `<div class="lbl">Намерение дня</div>${inp(P + '.intention', d.intention, 'Каким будет этот день?')}
     <div class="lbl">Энергия</div>${pills(P + '.energy', d.energy, ['⚡1', '⚡2', '⚡3', '⚡4', '⚡5'], 'n10')}
     <div class="lbl">Настроение</div>${pills(P + '.mood', d.mood, MOODS, 'emo')}`);

  h += card('Съешь лягушку', '🐸',
    `<p class="hint">Самая важная и неприятная задача дня. Сделай её первой — до почты и соцсетей (Брайан Трейси).</p>
     <div class="frog ${d.frog.d ? 'done' : ''}">${chk(P + '.frog.d', d.frog.d, true)}${inp(P + '.frog.t', d.frog.t, 'Главная задача дня', 'flat big')}</div>`);

  const grp = (key, name, note) =>
    `<div class="group-t"><b>${name}</b><span>${note}</span></div>` +
    d.tasks[key].map((it, i) => taskRow(`${P}.tasks.${key}.${i}`, it)).join('');
  h += card('Правило 1 · 3 · 5', '📜',
    `<div class="progline">${prog(p)}<span>${p == null ? '—' : Math.round(p * 100) + '%'}</span></div>
     ${grp('big', '1 крупная', 'двигает к цели')}${grp('mid', '3 средних', 'важные дела')}${grp('small', '5 мелких', 'быстрые дела')}
     <button class="btn ghost wide" data-act="carry">Перенести невыполненное на завтра →</button>`, 'план дня');

  const hs = Math.max(0, Math.min(23, +S.settings.dayStart || 6)), he = Math.max(hs, Math.min(23, +S.settings.dayEnd || 23));
  const nowH = new Date().getHours();
  let tb = '';
  for (let i = hs; i <= he; i++) {
    tb += `<div class="tb ${isToday && i === nowH ? 'now' : ''}"><div class="h">${pad(i)}:00</div>${inp(`${P}.blocks.${i}`, d.blocks[i] || '', '', 'flat')}</div>`;
  }
  h += card('Тайм-блокинг', '⏳', `<p class="hint">Назначь каждому часу дело — у каждой задачи своё место в дне (Кэл Ньюпорт).</p>${tb}`);

  if (S.habits.length) {
    h += card('Привычки', '💎', `<div class="habits">${S.habits.map(x => {
      const on = !!d.habits[x.id]; const s = streak(x.id);
      return `<button class="hab ${on ? 'on' : ''}" data-act="hab" data-k="${k}" data-id="${x.id}">${esc(x.icon)} ${esc(x.name)}${s ? ` <small>🔥${s}</small>` : ''}</button>`;
    }).join('')}</div>`);
  }

  h += card('Помодоро', '🍅',
    `<div class="row"><div style="flex:1;font-size:20px;letter-spacing:2px">${d.pomos ? '🍅'.repeat(Math.min(d.pomos, 12)) + (d.pomos > 12 ? ' ×' + d.pomos : '') : '<span class="hint" style="margin:0;letter-spacing:0">Сегодня ещё нет помидоров</span>'}</div>
     <button class="btn sm" data-act="open" data-s="pomo">Фокус 25 мин</button></div>`);

  h += card('Вечерняя рефлексия', '🌙',
    `<div class="lbl">Я благодарен(на) за…</div>${d.grat.map((g, i) => `<div class="row" style="margin-bottom:6px"><span style="color:var(--gold)">${i + 1}.</span>${inp(`${P}.grat.${i}`, g, 'Благодарность')}</div>`).join('')}
     <div class="lbl">Победы дня</div>${ta(P + '.wins', d.wins, 'Что получилось сегодня?')}
     <div class="lbl">Что улучшить завтра</div>${ta(P + '.improve', d.improve, 'Один урок дня')}
     <div class="lbl">Оценка дня</div>${pills(P + '.rating', d.rating, [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 'n10')}`);
  return h;
}

/* ---------------- WEEK ---------------- */
function viewWeek() {
  const d = pd(U.cur), m = monday(d), k = weekKey(d), w = week(k), P = `weeks.${k}`;
  let h = '';
  h += card('Фокус недели', '🎯',
    `${inp(P + '.focus', w.focus, 'Главное на этой неделе', 'big')}
     <div class="lbl">Девиз недели</div>${inp(P + '.motto', w.motto, 'Например: «Меньше, но лучше»')}`);

  h += card('Роли и большие камни', '🪨',
    `<p class="hint">Метод Стивена Кови: для каждой роли выбери один «большой камень» — его кладут в неделю первым, а мелочи заполнят пустоты.</p>
     ${S.roles.map(r => {
       const rk = w.rocks[r.id] || (w.rocks[r.id] = T());
       return `<div class="lbl">${esc(r.name)}</div><div class="task ${rk.d ? 'done' : ''}">${chk(`${P}.rocks.${r.id}.d`, rk.d)}${inp(`${P}.rocks.${r.id}.t`, rk.t, 'Большой камень', 'flat')}</div>`;
     }).join('')}
     <button class="btn ghost wide" data-act="open" data-s="roles">Настроить роли</button>`);

  const gp = listProgress(w.goals);
  h += card('Цели недели', '🏹', `<div class="progline">${prog(gp)}<span>${gp == null ? '—' : Math.round(gp * 100) + '%'}</span></div>${dlist(P + '.goals', 'Новая цель недели…')}`);

  let rows = '';
  for (let i = 0; i < 7; i++) {
    const dd = addD(m, i), dk = ymd(dd), x = S.days[dk];
    const pr = dayProgress(x);
    const fr = x && x.frog && x.frog.t ? (x.frog.d ? '✅ ' : '🐸 ') + esc(x.frog.t) : (x && x.intention ? esc(x.intention) : 'Не запланировано');
    const empty = !(x && ((x.frog && x.frog.t) || x.intention));
    rows += `<div class="dayrow ${dk === today() ? 'today' : ''}" data-act="goDay" data-k="${dk}">
      <div class="dn"><b>${dd.getDate()}</b><span>${WD[i]}</span></div>
      <div class="info"><div class="f ${empty ? 'empty' : ''}">${fr}</div>${pr == null ? '' : prog(pr)}</div>
      <div class="m">${x && x.mood ? MOODS[x.mood - 1] : ''}</div></div>`;
  }
  h += card('Дни недели', '🗓️', rows, 'нажми на день');

  if (S.habits.length) {
    let g = `<div class="hgrid" style="grid-template-columns:1fr repeat(7, 30px)"><div></div>${WD.map(x => `<div class="hd">${x}</div>`).join('')}`;
    for (const hb of S.habits) {
      g += `<div class="hn">${esc(hb.icon)} ${esc(hb.name)}</div>`;
      for (let i = 0; i < 7; i++) {
        const dk = ymd(addD(m, i)), x = S.days[dk];
        g += `<button class="cell ${x && x.habits && x.habits[hb.id] ? 'on' : ''} ${dk === today() ? 'today' : ''}" data-act="hab" data-k="${dk}" data-id="${hb.id}"></button>`;
      }
    }
    h += card('Трекер привычек', '💎', g + '</div>');
  }

  const R = w.review;
  h += card('Еженедельный обзор', '🔍',
    `<p class="hint">Ритуал GTD (Дэвид Аллен): раз в неделю — обычно в воскресенье — наводим порядок в королевстве.</p>
     ${REVIEW_STEPS.map((s, i) => `<div class="task ${R.checks[i] ? 'done' : ''}">${chk(`${P}.review.checks.${i}`, R.checks[i])}<div class="in flat" style="border:0">${s}</div></div>`).join('')}
     <div class="lbl">Победы недели</div>${ta(P + '.review.wins', R.wins, 'Чем я горжусь?')}
     <div class="lbl">Уроки</div>${ta(P + '.review.lessons', R.lessons, 'Что понял(а), что не сработало?')}
     <div class="lbl">Главное на следующую неделю</div>${ta(P + '.review.next', R.next, '')}
     <div class="lbl">Оценка недели</div>${pills(P + '.review.rating', R.rating, [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 'n10')}`);
  return h;
}

/* ---------------- MONTH ---------------- */
function viewMonth() {
  const d = pd(U.cur), y = d.getFullYear(), mo = d.getMonth(), k = monthKey(d), M = month(k), P = `months.${k}`;
  const n = daysIn(y, mo), first = wdi(new Date(y, mo, 1));
  let h = '';
  h += card('Тема месяца', '👑', `${inp(P + '.theme', M.theme, 'Например: «Месяц здоровья»', 'big')}`);

  let cal = `<div class="cal">${WD.map(x => `<div class="wd">${x}</div>`).join('')}`;
  for (let i = 0; i < first; i++) cal += '<div class="cd out"></div>';
  for (let i = 1; i <= n; i++) {
    const dk = `${y}-${pad(mo + 1)}-${pad(i)}`, x = S.days[dk], pr = dayProgress(x);
    const we = wdi(new Date(y, mo, i)) >= 5;
    cal += `<button class="cd ${dk === today() ? 'today' : ''} ${we ? 'we' : ''}" data-act="goDay" data-k="${dk}">
      ${pr != null ? ring(pr) : ''}${i}
      ${x && x.mood ? `<span class="dot" style="background:${MOOD_C[x.mood - 1]}"></span>` : ''}
      ${x && x.frog && x.frog.d ? '<span class="fr">🐸</span>' : ''}</button>`;
  }
  cal += '</div><div class="legend"><span>◯ кольцо — выполнение плана</span><span>🐸 лягушка съедена</span>' +
    MOODS.map((e, i) => `<span><i style="background:${MOOD_C[i]}"></i>${e}</span>`).join('') + '</div>';
  h += card('Календарь', '📅', cal);

  const gp = listProgress(M.goals);
  h += card('Цели месяца', '🏹', `<div class="progline">${prog(gp)}<span>${gp == null ? '—' : Math.round(gp * 100) + '%'}</span></div>${dlist(P + '.goals', 'Цель месяца…')}`);
  h += card('Важные даты и события', '🔔', dlist(P + '.events', 'Например: 12 — день рождения мамы', false));

  if (S.habits.length) {
    let hh = '';
    for (const hb of S.habits) {
      let cnt = 0, cells = '';
      for (let i = 1; i <= n; i++) {
        const x = S.days[`${y}-${pad(mo + 1)}-${pad(i)}`];
        const on = x && x.habits && x.habits[hb.id]; if (on) cnt++;
        cells += `<i class="${on ? 'on' : ''}"></i>`;
      }
      hh += `<div class="heat"><div class="ht">${esc(hb.icon)} ${esc(hb.name)}<span>${cnt}/${n}</span></div><div class="hr" style="grid-template-columns:repeat(${n},1fr)">${cells}</div></div>`;
    }
    h += card('Привычки за месяц', '💎', hh);
  }

  h += card('Итоги месяца', '📖',
    `<div class="lbl">Достижения</div>${ta(P + '.review.wins', M.review.wins, 'Чем запомнился месяц?')}
     <div class="lbl">Уроки и выводы</div>${ta(P + '.review.lessons', M.review.lessons, '')}
     <div class="lbl">Оценка месяца</div>${pills(P + '.review.rating', M.review.rating, [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 'n10')}`);
  return h;
}

/* ---------------- YEAR ---------------- */
function radar(wheel) {
  const cx = 150, cy = 150, R = 110, n = SPHERES.length;
  const pt = (i, v) => { const a = -Math.PI / 2 + i * 2 * Math.PI / n; return [cx + Math.cos(a) * R * v / 10, cy + Math.sin(a) * R * v / 10]; };
  let s = `<svg id="radar" viewBox="-36 -6 372 312"><defs><radialGradient id="rg"><stop offset="0" stop-color="#f7e3a1" stop-opacity=".55"/><stop offset="1" stop-color="#d9b45a" stop-opacity=".25"/></radialGradient></defs>`;
  for (const lv of [2, 4, 6, 8, 10]) s += `<polygon points="${SPHERES.map((_, i) => pt(i, lv).join(',')).join(' ')}" fill="none" stroke="var(--line-soft)" stroke-width="1"/>`;
  SPHERES.forEach((name, i) => {
    const [x, y] = pt(i, 10), [lx, ly] = pt(i, 12.4);
    s += `<line x1="${cx}" y1="${cy}" x2="${x}" y2="${y}" stroke="var(--line-soft)"/>`;
    s += `<text x="${lx}" y="${ly + 4}" text-anchor="middle" font-size="11" fill="var(--muted)" font-family="Montserrat, sans-serif">${name}</text>`;
  });
  const pts = SPHERES.map((_, i) => pt(i, +wheel[i] || 0));
  s += `<polygon points="${pts.map(p => p.join(',')).join(' ')}" fill="url(#rg)" stroke="var(--gold)" stroke-width="2"/>`;
  pts.forEach(p => { s += `<circle cx="${p[0]}" cy="${p[1]}" r="3.5" fill="var(--gold-hi)"/>`; });
  return s + '</svg>';
}
function okrPct(o) {
  const krs = o.krs || [];
  if (!krs.length) return 0;
  return krs.reduce((a, x) => a + (+x.p || 0), 0) / krs.length;
}
function viewYear() {
  const d = pd(U.cur), y = d.getFullYear(), k = String(y), Y = year(k), P = `years.${k}`;
  let h = '';
  h += card('Слово года', '✨',
    `<p class="hint">Одно слово, которое задаёт тон всему году: «Сила», «Свобода», «Глубина»…</p>
     ${inp(P + '.word', Y.word, 'Моё слово года', 'big')}
     <div class="lbl">Видение</div>${ta(P + '.vision', Y.vision, 'Представь, что год закончился блестяще. Как выглядит твоя жизнь 31 декабря?')}`);

  h += card('Колесо баланса', '☸️',
    `<p class="hint">Оцени каждую сферу жизни от 1 до 10. Слабые сферы — подсказка, где поставить цели года (Пол Майер).</p>
     <div id="radarBox">${radar(Y.wheel)}</div>
     <div class="wheel">${SPHERES.map((s, i) => `<div class="sl"><span>${s}</span><input type="range" min="0" max="10" step="1" value="${+Y.wheel[i] || 0}" data-bind="${P}.wheel.${i}" data-live="wheel"><b>${+Y.wheel[i] || 0}</b></div>`).join('')}</div>`);

  const okrs = Y.okrs.map((o, oi) => {
    const pct = Math.round(okrPct(o));
    return `<div class="okr">
      <div class="oh">${inp(`${P}.okrs.${oi}.o`, o.o, 'Цель (Objective)', 'flat')}<button class="x" data-act="okrDel" data-i="${oi}">×</button></div>
      <div class="meta"><select class="in" data-bind="${P}.okrs.${oi}.sphere" data-rerender="1">
        <option value="">— сфера —</option>${SPHERES.map(s => `<option ${o.sphere === s ? 'selected' : ''}>${s}</option>`).join('')}</select>
        <span class="pct">${pct}%</span></div>
      ${prog(pct / 100)}
      ${(o.krs || []).map((kr, ki) => `<div class="kr">${inp(`${P}.okrs.${oi}.krs.${ki}.t`, kr.t, 'Ключевой результат', 'flat')}<button class="x" data-act="krDel" data-i="${oi}" data-j="${ki}">×</button>
        <div class="krs"><input type="range" min="0" max="100" step="5" value="${+kr.p || 0}" data-bind="${P}.okrs.${oi}.krs.${ki}.p" data-live="pct"><b>${+kr.p || 0}%</b></div></div>`).join('')}
      <button class="mini" style="margin-top:8px" data-act="krAdd" data-i="${oi}">＋ ключевой результат</button>
    </div>`;
  }).join('');
  h += card('Цели года · OKR', '🏰',
    `<p class="hint">Objectives & Key Results: вдохновляющая цель + 2–4 измеримых результата. Отмечай прогресс ползунком.</p>
     ${okrs || '<div class="empty-note">Пока нет целей. Начни с 3–5 главных.</div>'}
     <button class="btn wide" data-act="okrAdd">＋ Новая цель года</button>`);

  const curQ = new Date().getFullYear() === y ? Math.floor(new Date().getMonth() / 3) : -1;
  h += card('12-недельные кварталы', '⚔️',
    `<p class="hint">«12-недельный год» (Брайан Моран): каждый квартал — отдельный забег с 1–3 главными целями.</p>
     <div class="quarters">${Y.quarters.map((q, i) => `<div class="q ${i === curQ ? 'cur' : ''}"><b>${['I', 'II', 'III', 'IV'][i]} квартал</b>${ta(`${P}.quarters.${i}`, q, 'Фокус квартала')}</div>`).join('')}</div>`);

  let mt = '';
  for (let i = 0; i < 12; i++) {
    const mk = `${y}-${pad(i + 1)}`, M = S.months[mk], gp = M ? listProgress(M.goals) : null;
    const cur = monthKey(new Date()) === mk;
    mt += `<button class="mt ${cur ? 'cur' : ''}" data-act="goMonth" data-k="${mk}"><b>${MONTHS[i]}</b><span>${M && M.theme ? esc(M.theme) : '—'}</span>${prog(gp)}</button>`;
  }
  h += card('Месяцы года', '🗺️', `<div class="months">${mt}</div>`);

  let px = '<div class="pixels"><div></div>';
  for (let i = 1; i <= 31; i++) px += `<div class="pm" style="text-align:center">${i % 5 === 0 || i === 1 ? i : ''}</div>`;
  for (let mo = 0; mo < 12; mo++) {
    px += `<div class="pm">${MON_S[mo]}</div>`;
    const n = daysIn(y, mo);
    for (let i = 1; i <= 31; i++) {
      if (i > n) { px += '<i class="px na"></i>'; continue; }
      const dk = `${y}-${pad(mo + 1)}-${pad(i)}`, x = S.days[dk];
      const c = x && x.mood ? MOOD_C[x.mood - 1] : '';
      px += `<button class="px ${dk === today() ? 'today' : ''}" ${c ? `style="background:${c}"` : ''} data-act="goDay" data-k="${dk}"></button>`;
    }
  }
  px += '</div><div class="legend">' + MOODS.map((e, i) => `<span><i style="background:${MOOD_C[i]}"></i>${e}</span>`).join('') + '</div>';
  h += card('Год в пикселях', '🎨', `<p class="hint">Каждый день окрашивается настроением, которое ты отметил(а) в «Утре монарха».</p>${px}`);

  h += card('Мечты и желания', '🌠', dlist(P + '.dreams', 'Чего я хочу в этом году…'));
  h += card('Итоги года', '📜', ta(P + '.review', Y.review, 'Главные победы, уроки и благодарности года', ''));
  return h;
}

/* ---------------- MORE ---------------- */
function viewMore() {
  if (U.sub) return SUBS[U.sub]();
  const mcount = Object.values(S.matrix).reduce((a, l) => a + l.filter(x => !x.d).length, 0);
  const tile = (s, i, b, sp, badge = 0) =>
    `<button class="tile" data-act="open" data-s="${s}">${badge ? `<span class="badge">${badge}</span>` : ''}<div class="ti">${i}</div><b>${b}</b><span>${sp}</span></button>`;
  return `<div class="hero fade"><h1>Сокровищница</h1><div class="quote">«Королевство строится камень за камнем.»</div><div class="who">— инструменты для управления своим временем</div></div>
    <div class="tiles">
      ${tile('inbox', '📥', 'Входящие', 'GTD: выгрузи всё из головы', S.inbox.length)}
      ${tile('matrix', '⚖️', 'Матрица Эйзенхауэра', 'Важное vs срочное', mcount)}
      ${tile('pomo', '🍅', 'Помодоро', 'Глубокий фокус 25/5')}
      ${tile('habits', '💎', 'Привычки', 'Атомные привычки')}
      ${tile('roles', '🎭', 'Роли', 'Для больших камней недели')}
      ${tile('guide', '📚', 'Методики', 'Как пользоваться')}
      ${tile('settings', '⚙️', 'Настройки', 'Темы, копия данных')}
    </div>`;
}
const SUBS = {
  inbox() {
    return card('Входящие', '📥',
      `<p class="hint">Записывай сюда всё, что приходит в голову: идеи, дела, обещания. Потом разбирай: в день, в матрицу или удали.</p>
       <div class="addrow" style="margin:0 0 10px"><input class="in add-in" data-addp="inbox" placeholder="Что у тебя на уме?" autocomplete="off"><button class="btn" data-act="add" data-p="inbox">＋</button></div>
       ${S.inbox.map((it, i) => `<div class="task">${inp(`inbox.${i}.t`, it.t, '', 'flat')}
         <button class="mini" data-act="inToday" data-i="${i}">→ День</button><button class="mini" data-act="inMatrix" data-i="${i}">→ ⚖️</button><button class="x" data-act="del" data-p="inbox" data-i="${i}">×</button></div>`).join('') ||
         '<div class="empty-note">Пусто. Чистый разум — как вода 🌊</div>'}`);
  },
  matrix() {
    const Q = [
      ['q1', 'Сделать сейчас', 'Важно и срочно'],
      ['q2', 'Запланировать', 'Важно, не срочно — зона роста'],
      ['q3', 'Делегировать', 'Срочно, не важно'],
      ['q4', 'Удалить', 'Не важно и не срочно'],
    ];
    return `<p class="hint" style="margin:0 0 12px">Президент Эйзенхауэр делил дела по двум осям. Секрет успеха — проводить больше времени во втором квадранте.</p>
      <div class="matrix">${Q.map(([q, t, s]) => `<div class="quad ${q}"><h3 class="serif">${t}</h3><div class="qs">${s}</div>
        ${S.matrix[q].map((it, i) => `<div class="task ${it.d ? 'done' : ''}">${chk(`matrix.${q}.${i}.d`, it.d)}${inp(`matrix.${q}.${i}.t`, it.t, '', 'flat')}${q !== 'q4' ? `<button class="mini" data-act="mToday" data-q="${q}" data-i="${i}">→ День</button>` : ''}<button class="x" data-act="del" data-p="matrix.${q}" data-i="${i}">×</button></div>`).join('')}
        <div class="addrow"><input class="in add-in" data-addp="matrix.${q}" placeholder="Добавить…" autocomplete="off"><button class="btn" data-act="add" data-p="matrix.${q}">＋</button></div></div>`).join('')}</div>`;
  },
  pomo() {
    const P = S.pomo, dur = POMO_MODES[P.mode].min * 60;
    const rem = pomoRemain();
    const c = 2 * Math.PI * 100;
    const td = S.days[today()];
    const cnt = td ? td.pomos || 0 : 0;
    return card('Помодоро', '🍅',
      `<div class="pills" style="justify-content:center">${Object.entries(POMO_MODES).map(([k, m]) => `<button class="pill ${P.mode === k ? 'on' : ''}" style="padding:0 12px" data-act="pmode" data-m="${k}">${m.name}</button>`).join('')}</div>
       <div class="pomo"><svg viewBox="0 0 240 240"><defs><linearGradient id="pg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#f7e3a1"/><stop offset="1" stop-color="#a97a22"/></linearGradient></defs>
         <circle class="ring-bg" cx="120" cy="120" r="100"/><circle id="pring" class="ring" cx="120" cy="120" r="100" stroke-dasharray="${c}" stroke-dashoffset="${c * (1 - rem / dur)}"/>
         <text id="ptime" class="tm" x="120" y="132" text-anchor="middle">${fmt(rem)}</text>
         <text class="tl" x="120" y="162" text-anchor="middle">${POMO_MODES[P.mode].name.toUpperCase()}</text></svg>
         <div class="ctrl">${P.running ? '<button class="btn" data-act="ppause">❚❚ Пауза</button>' : '<button class="btn" data-act="pstart">▶ Старт</button>'}<button class="btn ghost" data-act="preset">↺ Сброс</button></div>
         <div class="tomatoes">${cnt ? '🍅'.repeat(Math.min(cnt, 16)) : ''}</div>
         <div class="hint" style="margin-top:4px">Сегодня: ${cnt} × 25 мин фокуса</div></div>
       <p class="hint">Техника Франческо Чирилло: 25 минут работы без отвлечений, 5 минут отдыха. После четырёх помидоров — длинный перерыв.</p>`);
  },
  habits() {
    return card('Мои привычки', '💎',
      `<p class="hint">«Атомные привычки» (Джеймс Клир): маленькие ежедневные действия дают огромный результат. Не пропускай дважды подряд.</p>
       ${S.habits.map((hb, i) => `<div class="task"><input class="in" style="width:52px;text-align:center;padding:8px 4px" data-bind="habits.${i}.icon" value="${esc(hb.icon)}">${inp(`habits.${i}.name`, hb.name, '', 'flat')}<small style="color:var(--gold)">🔥${streak(hb.id)}</small><button class="x" data-act="habDel" data-i="${i}">×</button></div>`).join('')}
       <div class="addrow"><input class="in" id="hIcon" style="width:56px;flex:none;text-align:center" placeholder="⭐" maxlength="4"><input class="in add-in" id="hName" data-addp="__habit" placeholder="Новая привычка…"><button class="btn" data-act="habAdd">＋</button></div>`);
  },
  roles() {
    return card('Мои роли', '🎭',
      `<p class="hint">Кто ты в жизни? Сын или дочь, родитель, специалист, друг… Каждую неделю у каждой роли — свой большой камень.</p>
       ${S.roles.map((r, i) => `<div class="task"><span style="color:var(--gold)">◆</span>${inp(`roles.${i}.name`, r.name, '', 'flat')}<button class="x" data-act="roleDel" data-i="${i}">×</button></div>`).join('')}
       <div class="addrow"><input class="in add-in" id="rName" data-addp="__role" placeholder="Новая роль…"><button class="btn" data-act="roleAdd">＋</button></div>`);
  },
  guide() {
    const G = [
      ['🐸', 'Съешь лягушку', 'Брайан Трейси', 'Каждый день выбирай <b>одну</b> самую важную и трудную задачу и делай её первой, пока свежи силы. Всё остальное день пойдёт легче.'],
      ['📜', 'Правило 1-3-5', 'Планирование дня', 'Реалистичный план на день: <b>1</b> крупная задача, <b>3</b> средних и <b>5</b> мелких. Больше в день просто не помещается — и это нормально.'],
      ['⏳', 'Тайм-блокинг', 'Кэл Ньюпорт, «В работу с головой»', 'Расписывай день по часам: каждому делу — отрезок времени. Так задачи из списка превращаются в реальные действия, а не висят бесконечно.'],
      ['🍅', 'Помодоро', 'Франческо Чирилло', '25 минут глубокой работы без телефона → 5 минут отдыха. Четыре «помидора» → перерыв 15 минут. Отлично лечит прокрастинацию.'],
      ['⚖️', 'Матрица Эйзенхауэра', 'Важное и срочное', 'Дела делятся на 4 квадранта. <b>Срочное и важное</b> — сделать, <b>важное не срочное</b> — запланировать (именно здесь рост), <b>срочное не важное</b> — делегировать, остальное — удалить.'],
      ['📥', 'GTD — «Входящие»', 'Дэвид Аллен', 'Голова — для идей, а не для хранения. Записывай всё во «Входящие», затем регулярно разбирай: сделать, запланировать, делегировать или удалить.'],
      ['🪨', 'Роли и большие камни', 'Стивен Кови, «7 навыков»', 'Если сначала насыпать в банку песок, камни не влезут. Планируя неделю, сначала поставь большие камни для каждой своей роли.'],
      ['🔍', 'Еженедельный обзор', 'GTD', 'Раз в неделю 30–60 минут: разбор входящих, оценка недели, сверка с целями месяца и года, выбор камней на следующую неделю.'],
      ['💎', 'Трекер привычек', 'Джеймс Клир, «Атомные привычки»', 'Отмечай привычки каждый день — серия 🔥 мотивирует не прерывать цепочку. Правило: никогда не пропускай дважды подряд.'],
      ['👑', 'Тема месяца', 'Фокус', 'Дай каждому месяцу тему («Месяц спорта», «Месяц порядка»). Цели месяца — ступени к целям года.'],
      ['✨', 'Слово года', 'Намерение', 'Одно слово-ориентир, к которому можно вернуться в любой момент года, когда нужно принять решение.'],
      ['☸️', 'Колесо баланса', 'Пол Майер', 'Оцени 8 сфер жизни от 1 до 10. Колесо с провалами «не едет» — поставь цели там, где оценка ниже всего.'],
      ['🏰', 'OKR', 'Энди Гроув, Джон Дорр', 'Objective — вдохновляющая цель, Key Results — 2–4 измеримых результата. 70% выполнения амбициозной цели — уже успех.'],
      ['⚔️', '12-недельный год', 'Брайан Моран', 'Год слишком длинный — мы расслабляемся. Думай кварталами: 12 недель — достаточно, чтобы достичь многого, и достаточно мало, чтобы не терять темп.'],
      ['🎨', 'Год в пикселях', 'Дневник настроения', 'Каждый день окрашивается своим настроением. В конце года — живая картина твоей жизни и подсказки, что делает тебя счастливее.'],
      ['🌙', 'Вечерняя рефлексия', 'Дневник благодарности', 'Три благодарности, победы дня и один урок. 5 минут вечером повышают осознанность и настроение.'],
    ];
    return `<div class="card guide">${G.map(([i, t, s, p]) => `<details><summary><span style="font-size:22px">${i}</span><div>${t}<small>${s}</small></div></summary><p>${p}</p></details>`).join('')}</div>
      ${card('Как строится система', '🧭', `<p class="hint" style="margin:0;line-height:1.55">Год → <b>Слово, колесо баланса, OKR</b><br>Квартал → <b>12-недельный фокус</b><br>Месяц → <b>тема и цели месяца</b><br>Неделя → <b>роли, большие камни, обзор</b><br>День → <b>лягушка, 1-3-5, тайм-блоки, привычки, рефлексия</b></p>`)}`;
  },
  settings() {
    const st = S.settings;
    return card('Монарх', '🤴',
      `<div class="lbl">Как к тебе обращаться?</div>${inp('settings.name', st.name, 'Имя')}
       <div class="lbl">Часы дня для тайм-блокинга</div>
       <div class="row"><select class="in" data-bind="settings.dayStart" data-num="1">${[...Array(13).keys()].map(i => `<option value="${i}" ${+st.dayStart === i ? 'selected' : ''}>с ${pad(i)}:00</option>`).join('')}</select>
       <select class="in" data-bind="settings.dayEnd" data-num="1">${[...Array(12).keys()].map(i => i + 12).map(i => `<option value="${i}" ${+st.dayEnd === i ? 'selected' : ''}>до ${pad(i)}:00</option>`).join('')}</select></div>`) +
      card('Королевская палитра', '🎨',
        `<div class="themes">${THEMES.map(t => `<button class="th ${st.theme === t.id ? 'on' : ''}" style="background:${t.g};${t.light ? 'color:#2b1d2e' : ''}" data-act="theme" data-t="${t.id}">${t.name}<i></i></button>`).join('')}</div>`) +
      card('Сокровища (резервная копия)', '🗝️',
        `<p class="hint">Все данные хранятся только на этом телефоне. Время от времени сохраняй копию — например, отправь её себе в Telegram или на почту.</p>
         <button class="btn wide" data-act="export">Сохранить копию</button>
         <div class="lbl" style="margin-top:16px">Восстановить из копии</div>
         <textarea class="ta" id="impTa" placeholder="Вставь сюда текст копии"></textarea>
         <button class="btn ghost wide" data-act="import">Восстановить</button>
         <button class="btn danger wide" data-act="reset">Стереть все данные</button>`) +
      '<div class="divider">❖ ❖ ❖</div><p class="hint" style="text-align:center">Скипетр 1.0 · личный королевский планер</p>';
  },
};

/* ---------------- Pomodoro ---------------- */
const POMO_MODES = { focus: { name: 'Фокус', min: 25 }, short: { name: 'Перерыв', min: 5 }, long: { name: 'Отдых', min: 15 } };
const fmt = s => `${pad(Math.floor(s / 60))}:${pad(Math.floor(s % 60))}`;
function pomoRemain() {
  const P = S.pomo;
  return P.running ? Math.max(0, Math.round((P.endAt - Date.now()) / 1000)) : P.remain;
}
function pomoFinish() {
  const P = S.pomo;
  P.running = false;
  if (P.mode === 'focus') {
    const d = day(today()); d.pomos = (d.pomos || 0) + 1;
    P.mode = d.pomos % 4 === 0 ? 'long' : 'short';
    toast('🍅 Помидор завершён! Время отдохнуть');
  } else {
    P.mode = 'focus';
    toast('👑 Отдых окончен. Вперёд, к победам!');
  }
  P.remain = POMO_MODES[P.mode].min * 60;
  vibrate(600); beep();
  flush();
  if (U.sub === 'pomo' || U.tab === 'day') render();
}
setInterval(() => {
  const P = S.pomo;
  if (!P.running) return;
  const rem = pomoRemain();
  const t = $('#ptime'), r = $('#pring');
  if (t) t.textContent = fmt(rem);
  if (r) { const c = 2 * Math.PI * 100; r.style.strokeDashoffset = c * (1 - rem / (POMO_MODES[P.mode].min * 60)); }
  if (rem <= 0) pomoFinish();
}, 1000);
function beep() {
  try {
    const ac = new (window.AudioContext || window.webkitAudioContext)();
    [0, 0.25, 0.5].forEach((t, i) => {
      const o = ac.createOscillator(), g = ac.createGain();
      o.frequency.value = [880, 1046.5, 1318.5][i]; o.type = 'sine';
      g.gain.setValueAtTime(0.0001, ac.currentTime + t);
      g.gain.exponentialRampToValueAtTime(0.3, ac.currentTime + t + 0.02);
      g.gain.exponentialRampToValueAtTime(0.0001, ac.currentTime + t + 0.4);
      o.connect(g); g.connect(ac.destination); o.start(ac.currentTime + t); o.stop(ac.currentTime + t + 0.45);
    });
  } catch (e) { /* no audio */ }
}

/* ---------------- Platform helpers ---------------- */
function vibrate(ms) { try { if (window.Android) Android.vibrate(ms); else if (navigator.vibrate) navigator.vibrate(ms); } catch (e) { /* ignore */ } }
let toastT = 0;
function toast(msg) {
  const t = $('#toast'); t.textContent = msg; t.classList.add('show');
  clearTimeout(toastT); toastT = setTimeout(() => t.classList.remove('show'), 2400);
}
function ask(title, text, okText = 'Да') {
  return new Promise(res => {
    const m = $('#modal');
    m.innerHTML = `<div class="dlg fade"><h3 class="serif">${title}</h3><p>${text}</p><div class="row"><button class="btn ghost" id="mNo">Отмена</button><button class="btn" id="mOk">${okText}</button></div></div>`;
    m.hidden = false;
    const done = v => { m.hidden = true; m.innerHTML = ''; res(v); };
    $('#mOk').onclick = () => done(true);
    $('#mNo').onclick = () => done(false);
    m.onclick = e => { if (e.target === m) done(false); };
  });
}
function applyTheme() {
  const t = THEMES.find(x => x.id === S.settings.theme) || THEMES[0];
  document.documentElement.dataset.theme = t.id;
  try { if (window.Android) Android.setBars(t.bg, !!t.light); } catch (e) { /* ignore */ }
}

/* ---------------- Render ---------------- */
const VIEWS = { day: viewDay, week: viewWeek, month: viewMonth, year: viewYear, more: viewMore };
function render(resetScroll = false) {
  const v = $('#view'), st = v.scrollTop;
  header();
  v.innerHTML = VIEWS[U.tab]();
  v.scrollTop = resetScroll ? 0 : st;
  if (resetScroll) v.classList.remove('fade'), void v.offsetWidth, v.classList.add('fade');
  save();
}
function go(patch) { Object.assign(U, patch); render(true); }

/* ---------------- Actions ---------------- */
function nextFree(arr) { return arr.findIndex(x => !x.t || !x.t.trim()); }
function pushToDay(k, text) {
  const d = day(k);
  for (const g of ['small', 'mid', 'big']) {
    const i = nextFree(d.tasks[g]);
    if (i >= 0) { d.tasks[g][i] = T(text); return true; }
  }
  d.tasks.small.push(T(text));
  return true;
}
const A = {
  nav: ds => go({ tab: ds.tab, sub: null }),
  shift(ds) {
    const n = +ds.n, d = pd(U.cur);
    let x;
    if (U.tab === 'day') x = addD(d, n);
    else if (U.tab === 'week') x = addD(d, 7 * n);
    else if (U.tab === 'month') x = new Date(d.getFullYear(), d.getMonth() + n, 1);
    else x = new Date(d.getFullYear() + n, 0, 1);
    go({ cur: ymd(x) });
  },
  today: () => go({ cur: today() }),
  goDay: ds => go({ tab: 'day', cur: ds.k }),
  goMonth: ds => go({ tab: 'month', cur: ds.k + '-01' }),
  open: ds => go({ tab: 'more', sub: ds.s }),
  back: () => go({ sub: null }),
  tog(ds) { setP(ds.p, !getP(ds.p)); if (getP(ds.p)) vibrate(15); render(); },
  set(ds) { const v = +ds.v; setP(ds.p, getP(ds.p) === v ? 0 : v); render(); },
  add(ds, el) {
    const box = el.closest('.addrow') || el.parentElement;
    const input = box.querySelector('.add-in') || el;
    const t = input.value.trim();
    if (!t) return;
    const arr = getP(ds.p);
    arr.push(T(t));
    render();
    const ni = document.querySelector(`.add-in[data-addp="${ds.p}"]`);
    if (ni) ni.focus();
  },
  async del(ds) {
    const arr = getP(ds.p), it = arr[+ds.i];
    if (it && it.t && it.t.trim() && !(await ask('Удалить?', esc(it.t), 'Удалить'))) return;
    arr.splice(+ds.i, 1); render();
  },
  hab(ds) { const d = day(ds.k); d.habits[ds.id] = !d.habits[ds.id]; if (d.habits[ds.id]) vibrate(15); render(); },
  carry() {
    const k = U.cur, d = day(k), nk = ymd(addD(pd(k), 1)), n = day(nk);
    let moved = 0;
    if (d.frog.t.trim() && !d.frog.d) {
      if (!n.frog.t.trim()) n.frog = T(d.frog.t); else pushToDay(nk, d.frog.t);
      moved++;
    }
    for (const g of ['big', 'mid', 'small']) {
      for (const it of d.tasks[g]) {
        if (!it.t.trim() || it.d) continue;
        const i = nextFree(n.tasks[g]);
        if (i >= 0) n.tasks[g][i] = T(it.t); else pushToDay(nk, it.t);
        moved++;
      }
    }
    toast(moved ? `Перенесено на завтра: ${moved}` : 'Всё выполнено — нечего переносить 👑');
  },
  inToday(ds) { const it = S.inbox.splice(+ds.i, 1)[0]; pushToDay(today(), it.t); toast('Добавлено в план на сегодня'); render(); },
  inMatrix(ds) { const it = S.inbox.splice(+ds.i, 1)[0]; S.matrix.q2.push(T(it.t)); toast('Перемещено в матрицу → «Запланировать»'); render(); },
  mToday(ds) { const it = S.matrix[ds.q][+ds.i]; pushToDay(today(), it.t); it.d = true; toast('Добавлено в план на сегодня'); render(); },
  okrAdd() { year(String(pd(U.cur).getFullYear())).okrs.push({ o: '', sphere: '', krs: [{ t: '', p: 0 }] }); render(); },
  async okrDel(ds) {
    const Y = year(String(pd(U.cur).getFullYear()));
    if (!(await ask('Удалить цель?', esc(Y.okrs[+ds.i].o || 'Цель без названия'), 'Удалить'))) return;
    Y.okrs.splice(+ds.i, 1); render();
  },
  krAdd(ds) { year(String(pd(U.cur).getFullYear())).okrs[+ds.i].krs.push({ t: '', p: 0 }); render(); },
  krDel(ds) { year(String(pd(U.cur).getFullYear())).okrs[+ds.i].krs.splice(+ds.j, 1); render(); },
  habAdd() {
    const n = $('#hName').value.trim(); if (!n) return;
    S.habits.push({ id: 'h' + uid(), icon: $('#hIcon').value.trim() || '⭐', name: n }); render();
    const ni = $('#hName'); if (ni) ni.focus();
  },
  async habDel(ds) {
    if (!(await ask('Удалить привычку?', esc(S.habits[+ds.i].name), 'Удалить'))) return;
    S.habits.splice(+ds.i, 1); render();
  },
  roleAdd() {
    const n = $('#rName').value.trim(); if (!n) return;
    S.roles.push({ id: 'r' + uid(), name: n }); render();
    const ni = $('#rName'); if (ni) ni.focus();
  },
  async roleDel(ds) {
    if (!(await ask('Удалить роль?', esc(S.roles[+ds.i].name), 'Удалить'))) return;
    S.roles.splice(+ds.i, 1); render();
  },
  pmode(ds) { S.pomo = { mode: ds.m, running: false, endAt: 0, remain: POMO_MODES[ds.m].min * 60 }; render(); },
  pstart() { const P = S.pomo; if (P.remain <= 0) P.remain = POMO_MODES[P.mode].min * 60; P.endAt = Date.now() + P.remain * 1000; P.running = true; render(); },
  ppause() { const P = S.pomo; P.remain = pomoRemain(); P.running = false; render(); },
  preset() { const P = S.pomo; P.running = false; P.remain = POMO_MODES[P.mode].min * 60; render(); },
  theme(ds) { S.settings.theme = ds.t; applyTheme(); render(); },
  export() {
    flush();
    const json = JSON.stringify(S);
    if (window.Android && Android.share) { Android.share(json); return; }
    try { navigator.clipboard.writeText(json); toast('Копия скопирована в буфер'); } catch (e) { $('#impTa').value = json; }
  },
  async import() {
    let data;
    try { data = JSON.parse($('#impTa').value.trim()); } catch (e) { toast('Не получилось прочитать копию'); return; }
    if (!data || !data.v || !data.days) { toast('Это не копия Скипетра'); return; }
    if (!(await ask('Восстановить?', 'Текущие данные будут заменены данными из копии.', 'Восстановить'))) return;
    S = def(data, fresh()); flush(); applyTheme(); toast('Данные восстановлены 👑'); go({ tab: 'day', sub: null, cur: today() });
  },
  async reset() {
    if (!(await ask('Стереть всё?', 'Все планы, цели и привычки будут удалены безвозвратно.', 'Стереть'))) return;
    S = fresh(); flush(); applyTheme(); go({ tab: 'day', sub: null, cur: today() });
  },
};

/* ---------------- Events ---------------- */
document.addEventListener('click', e => {
  const b = e.target.closest('[data-act]');
  if (!b) return;
  const f = A[b.dataset.act];
  if (f) f(b.dataset, b, e);
});
document.addEventListener('input', e => {
  const el = e.target, p = el.dataset.bind;
  if (!p) return;
  let v = el.value;
  if (el.type === 'range' || el.dataset.num) v = +v;
  setP(p, v);
  save();
  if (el.dataset.live === 'wheel') {
    el.nextElementSibling.textContent = v;
    $('#radarBox').innerHTML = radar(getP(p.split('.').slice(0, -1).join('.')));
  } else if (el.dataset.live === 'pct') {
    el.nextElementSibling.textContent = v + '%';
  }
});
document.addEventListener('change', e => {
  const el = e.target;
  if (el.dataset.live === 'pct' || el.dataset.rerender || el.tagName === 'SELECT') render();
});
document.addEventListener('keydown', e => {
  if (e.key !== 'Enter') return;
  const el = e.target;
  if (el.classList.contains('add-in')) {
    e.preventDefault();
    if (el.dataset.addp === '__habit') A.habAdd();
    else if (el.dataset.addp === '__role') A.roleAdd();
    else A.add({ p: el.dataset.addp }, el);
  } else if (el.tagName === 'INPUT') {
    el.blur();
  }
});

// Android back button: returns true if handled inside the app.
window.appBack = () => {
  if (!$('#modal').hidden) { $('#modal').hidden = true; return true; }
  if (U.tab === 'more' && U.sub) { go({ sub: null }); return true; }
  if (U.tab !== 'day' || U.cur !== today()) { go({ tab: 'day', sub: null, cur: today() }); return true; }
  return false;
};

// Midnight rollover: if the app stays open, keep "today" fresh.
let lastToday = today();
setInterval(() => {
  const t = today();
  if (t !== lastToday) { if (U.cur === lastToday) U.cur = t; lastToday = t; if (!document.activeElement || document.activeElement === document.body) render(); }
}, 60000);

applyTheme();
render(true);
