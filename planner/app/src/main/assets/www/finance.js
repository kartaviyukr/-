'use strict';
/* Казна — финансовое планирование Планёра.
   Подключается до app.js; функции используют общие помощники из app.js во время вызова. */

// Шаблон автораспределения дохода (в %), собран по правилу 50/30/20.
const DEF_PCT = {
  c_home: 25, c_food: 13, c_trans: 5, c_health: 3, c_comm: 2, c_debt: 2,
  c_cafe: 7, c_fun: 6, c_cloth: 6, c_gift: 3, c_travel: 5, c_other: 3,
  c_save: 20,
};
// Встроенный словарь для автоопределения категории по комментарию.
const AUTO_KW = [
  ['c_food', ['пятёрочк', 'пятерочк', 'магнит', 'перекрёст', 'перекрест', 'вкусвил', 'лента', 'ашан', 'дикси', 'метро кэш', 'окей', 'спар', 'азбука вкуса', 'самокат', 'продукт', 'супермаркет', 'рынок', 'хлеб', 'молоко', 'мясо', 'овощ', 'фрукт', 'бакалея', 'светофор', 'fix price', 'фикс прайс']],
  ['c_cafe', ['кафе', 'ресторан', 'кофе', 'кофейн', 'бар', 'пицц', 'суши', 'роллы', 'макдон', 'вкусно и точка', 'kfc', 'ростикс', 'бургер', 'шаурм', 'столов', 'обед', 'ужин', 'завтрак', 'доставка еды', 'яндекс еда', 'delivery']],
  ['c_trans', ['такси', 'яндекс go', 'uber', 'метро', 'автобус', 'трамва', 'троллейб', 'электрич', 'проезд', 'транспорт', 'бензин', 'азс', 'заправк', 'лукойл', 'газпромнефть', 'роснефть', 'парковк', 'каршеринг', 'делимобиль', 'ситидрайв', 'тройка', 'мойка']],
  ['c_health', ['аптек', 'лекарств', 'врач', 'клиник', 'стоматол', 'анализ', 'больниц', 'витамин', 'массаж', 'медицин', 'здоров']],
  ['c_comm', ['мтс', 'билайн', 'мегафон', 'теле2', 'tele2', 'йота', 'yota', 'связь', 'интернет', 'ростелеком', 'мобильн', 'телефон счет']],
  ['c_home', ['аренда', 'квартплат', 'жкх', 'коммунал', 'свет', 'электроэнерг', 'газ ', 'вода', 'ипотек', 'ремонт', 'хозтовар', 'леруа', 'икеа', 'ikea', 'мебел', 'управляющ']],
  ['c_fun', ['кино', 'театр', 'концерт', 'музей', 'выставк', 'игр', 'steam', 'боулинг', 'квест', 'книг', 'подписк', 'кинопоиск', 'иви', 'okko', 'netflix', 'spotify', 'музык', 'хобби', 'фитнес', 'спортзал', 'бассейн']],
  ['c_cloth', ['одежд', 'обувь', 'кроссовк', 'куртк', 'джинс', 'плать', 'zara', 'h&m', 'uniqlo', 'спортмастер', 'lamoda', 'ламода', 'gloria', 'глория']],
  ['c_gift', ['подарок', 'подарк', 'цвет', 'букет', 'день рожд', 'сувенир']],
  ['c_travel', ['отель', 'гостиниц', 'авиабилет', 'билет на самол', 'поезд', 'ржд', 'аэрофлот', 'путешеств', 'отпуск', 'тур ', 'booking', 'airbnb', 'туту']],
  ['c_debt', ['кредит', 'займ', 'долг', 'рассрочк', 'кредитк']],
  ['c_other', ['wildberries', 'вайлдберриз', 'озон', 'ozon', 'маркетплейс', 'aliexpress', 'алиэкспресс']],
];
const normNote = t => ' ' + String(t || '').toLowerCase().replace(/ё/g, 'е').replace(/[^a-zа-я0-9&\s]/g, ' ').replace(/\s+/g, ' ').trim() + ' ';
const ruleKey = t => normNote(t).trim().split(' ')[0] || '';
// Угадывает категорию: сначала по выученным правилам, затем по словарю.
function guessCat(note) {
  const n = normNote(note);
  if (n.trim().length < 3) return null;
  const has = id => F().cats.some(c => c.id === id);
  const rules = F().rules || {};
  for (const w of n.trim().split(' ')) if (rules[w] && has(rules[w])) return rules[w];
  for (const [id, kws] of AUTO_KW) {
    if (!has(id)) continue;
    for (const k of kws) if (n.includes(k.replace(/ё/g, 'е'))) return id;
  }
  return null;
}

const FIN_DEF = () => ({
  currency: '₽',
  payself: 10,
  debtMethod: 'snowball',
  cats: [
    { id: 'c_home', icon: '🏠', name: 'Жильё', bucket: 'need', limit: 0 },
    { id: 'c_food', icon: '🛒', name: 'Продукты', bucket: 'need', limit: 0 },
    { id: 'c_trans', icon: '🚌', name: 'Транспорт', bucket: 'need', limit: 0 },
    { id: 'c_health', icon: '💊', name: 'Здоровье', bucket: 'need', limit: 0 },
    { id: 'c_comm', icon: '📱', name: 'Связь', bucket: 'need', limit: 0 },
    { id: 'c_debt', icon: '💳', name: 'Долги', bucket: 'need', limit: 0 },
    { id: 'c_cafe', icon: '🍽️', name: 'Кафе', bucket: 'want', limit: 0 },
    { id: 'c_fun', icon: '🎭', name: 'Развлечения', bucket: 'want', limit: 0 },
    { id: 'c_cloth', icon: '👗', name: 'Одежда', bucket: 'want', limit: 0 },
    { id: 'c_gift', icon: '🎁', name: 'Подарки', bucket: 'want', limit: 0 },
    { id: 'c_travel', icon: '✈️', name: 'Путешествия', bucket: 'want', limit: 0 },
    { id: 'c_other', icon: '📦', name: 'Прочее', bucket: 'want', limit: 0 },
    { id: 'c_save', icon: '💰', name: 'Накопления', bucket: 'save', limit: 0 },
  ],
  tx: [],
  months: {},
  goals: [],
  cushion: { months: 6, base: 0, saved: 0 },
  debts: [],
  bills: [],
  wishes: [],
  autoSplit: true,
  pct: { ...DEF_PCT },
  rules: {},
});

const BUCKETS = {
  need: { name: 'Нужды', pct: 50, color: '#4a6fd1', note: 'жильё, еда, транспорт, обязательные платежи' },
  want: { name: 'Желания', pct: 30, color: '#9a5cc8', note: 'кафе, развлечения, покупки для радости' },
  save: { name: 'Накопления', pct: 20, color: '#d9b45a', note: 'копилки, подушка, инвестиции' },
};
const FIN_COLORS = ['#d9b45a', '#c2385a', '#3aa57d', '#4a6fd1', '#9a5cc8', '#d9783a', '#2bb3b1', '#e07ab0', '#8fae3c', '#b58b63', '#6c7bd8', '#a99fbf', '#f7e3a1', '#7d4e9e'];
const FIN_SECS = [['budget', 'Бюджет'], ['goals', 'Копилки'], ['debts', 'Долги'], ['bills', 'Платежи'], ['fyear', 'Год']];

const F = () => S.fin;
const num = x => { const v = parseFloat(String(x ?? '').replace(/\s/g, '').replace(',', '.')); return isFinite(v) ? v : 0; };
const money = (n, sign = false) => {
  const v = Math.round(num(n) * 100) / 100;
  const s = Math.abs(v).toLocaleString('ru-RU', { maximumFractionDigits: 2 });
  return `${v < 0 ? '−' : sign && v > 0 ? '+' : ''}${s} ${F().currency}`;
};
const catOf = id => F().cats.find(c => c.id === id) || { id, icon: '❔', name: 'Без категории', bucket: 'want', limit: 0 };
const saveCat = () => F().cats.find(c => c.id === 'c_save') || F().cats.find(c => c.bucket === 'save');
const fmonth = mk => def(F().months[mk] || (F().months[mk] = {}), { income: [], paid: {}, plan: {} });
const monthIncome = mk => { const m = F().months[mk]; return m ? m.income.reduce((s, x) => s + num(x.a), 0) : 0; };
// Сумма, выделенная категории в месяце: ручная правка, иначе доля дохода по шаблону, иначе старый лимит.
function planOf(mk, c) {
  const m = F().months[mk];
  if (m && m.plan && m.plan[c.id] != null && m.plan[c.id] !== '') return num(m.plan[c.id]);
  if (F().autoSplit) return Math.round(monthIncome(mk) * num(F().pct[c.id]) / 100);
  return num(c.limit);
}
const isManual = (mk, id) => { const m = F().months[mk]; return !!(m && m.plan && m.plan[id] != null && m.plan[id] !== ''); };
const prevMk = mk => { const d = pd(mk + '-01'); return monthKey(new Date(d.getFullYear(), d.getMonth() - 1, 1)); };
const moneyIn = (p, v, ph = '0', cls = '') =>
  `<input class="in ${cls}" inputmode="decimal" data-bind="${p}" data-num="1" data-rerender="1" value="${num(v) ? esc(v) : ''}" placeholder="${ph}" autocomplete="off">`;

function addTx(amt, cat, note = '', date = today()) {
  const a = Math.round(num(amt) * 100) / 100;
  if (!(a > 0)) return null;
  const t = { id: 't' + uid(), ts: Date.now(), date, amt: a, cat, note };
  F().tx.push(t);
  return t;
}
function finStats(mk) {
  const fm = F().months[mk];
  const income = fm ? fm.income.reduce((s, x) => s + num(x.a), 0) : 0;
  const byCat = {}, by = { need: 0, want: 0, save: 0 };
  for (const t of F().tx) {
    if (!t.date.startsWith(mk)) continue;
    byCat[t.cat] = (byCat[t.cat] || 0) + t.amt;
    by[catOf(t.cat).bucket] += t.amt;
  }
  const spent = by.need + by.want;
  return { income, byCat, by, spent, saved: by.save, balance: income - spent - by.save };
}

/* ---------------- Charts ---------------- */
function donut(parts, total, label) {
  const r = 70, c = 2 * Math.PI * r;
  let off = 0, segs = '';
  for (const p of parts) {
    const len = total ? c * p.v / total : 0;
    segs += `<circle cx="100" cy="100" r="${r}" fill="none" stroke="${p.color}" stroke-width="22" stroke-dasharray="${Math.max(0, len - 1.5)} ${c}" stroke-dashoffset="${-off}" transform="rotate(-90 100 100)"/>`;
    off += len;
  }
  return `<svg class="donut" viewBox="0 0 200 200"><circle cx="100" cy="100" r="${r}" fill="none" stroke="var(--input)" stroke-width="22"/>${segs}
    <text x="100" y="96" text-anchor="middle" class="dl">${label}</text><text x="100" y="120" text-anchor="middle" class="dv">${money(total)}</text></svg>`;
}
function bars(rows) {
  const max = Math.max(1, ...rows.flatMap(r => [r.a, r.b]));
  const W = 340, H = 150, bw = W / rows.length;
  let s = `<svg class="fbars" viewBox="0 0 ${W} ${H + 18}">`;
  rows.forEach((r, i) => {
    const x = i * bw, ha = H * r.a / max, hb = H * r.b / max;
    s += `<rect x="${x + bw * 0.14}" y="${H - ha}" width="${bw * 0.34}" height="${ha}" rx="2" fill="var(--gold)" opacity="${r.cur ? 1 : 0.75}"/>`;
    s += `<rect x="${x + bw * 0.52}" y="${H - hb}" width="${bw * 0.34}" height="${hb}" rx="2" fill="var(--ruby)" opacity="${r.cur ? 1 : 0.75}"/>`;
    s += `<text x="${x + bw / 2}" y="${H + 13}" text-anchor="middle" font-size="9" fill="${r.cur ? 'var(--gold)' : 'var(--muted)'}" font-family="Montserrat, sans-serif">${r.l}</text>`;
  });
  return s + `<line x1="0" y1="${H}" x2="${W}" y2="${H}" stroke="var(--line-soft)"/></svg>`;
}
const fprog = (v, over = false) => `<div class="prog ${over ? 'over' : ''}"><i style="width:${Math.min(100, Math.round((v || 0) * 100))}%"></i></div>`;

/* ---------------- Budget allocation ---------------- */
function allocCard(mk, st) {
  const fz = F(), inc = st.income, P = `fin.months.${mk}.plan`;
  const cats = fz.cats;
  const plans = Object.fromEntries(cats.map(c => [c.id, planOf(mk, c)]));
  const alloc = cats.reduce((s, c) => s + plans[c.id], 0);
  const free = inc - alloc;
  const pctSum = cats.reduce((s, c) => s + num(fz.pct[c.id]), 0);
  const groups = Object.entries(BUCKETS).map(([k, b]) => {
    const cs = cats.filter(c => c.bucket === k);
    if (!cs.length) return '';
    const sum = cs.reduce((s, c) => s + plans[c.id], 0), target = inc * b.pct / 100;
    return `<div class="ag"><div class="ag-h"><span><i style="background:${b.color}"></i>${b.name}</span><b>${money(sum)} <small>${inc ? Math.round(sum / inc * 100) + '% · цель ' + b.pct + '%' : ''}</small></b></div>
      ${cs.map(c => {
        const pl = plans[c.id], sp = st.byCat[c.id] || 0, man = isManual(mk, c.id);
        const tag = man ? '✎ вручную' : fz.autoSplit && num(fz.pct[c.id]) ? `⚡ ${num(fz.pct[c.id])}%` : '';
        return `<div class="arow"><span class="txi">${esc(c.icon)}</span>
          <div class="txn">${esc(c.name)}<small>${tag}${tag && sp ? ' · ' : ''}${sp ? 'потрачено ' + money(sp) : ''}</small></div>
          <input class="in famt ${man ? 'man' : ''}" inputmode="decimal" data-bind="${P}.${c.id}" data-num="1" data-rerender="1" value="${pl ? pl : ''}" placeholder="0" autocomplete="off">
          ${man ? `<button class="x" data-act="planAuto" data-c="${c.id}" title="Вернуть авто">↺</button>` : '<span class="x"></span>'}</div>`;
      }).join('')}</div>`;
  }).join('');
  const prevPlan = fz.months[prevMk(mk)];
  return card('Распределение бюджета', '🧮',
    `<p class="hint">Каждому рублю — своя задача: распредели доход месяца по категориям, пока «Свободно» не станет нулём. Суммы сразу становятся лимитами конвертов.</p>
     <div class="fstats">
       <div><span>Доход</span><b>${money(inc)}</b></div>
       <div><span>Распределено</span><b>${money(alloc)}</b></div>
       <div class="${free < 0 ? 'bad' : free > 0 ? 'free' : 'zero'}"><span>${free < 0 ? 'Перебор' : 'Свободно'}</span><b>${money(Math.abs(free))}</b></div>
     </div>
     ${fprog(inc ? alloc / inc : 0, free < 0)}
     <button class="switch ${fz.autoSplit ? 'on' : ''}" data-act="autoSplit"><i></i><span><b>Автораспределение дохода</b><small>${fz.autoSplit ? 'Каждый доход сам делится по шаблону процентов' : 'Выключено — суммы задаются вручную'}</small></span></button>
     <div class="achips">
       <button class="mini" data-act="planTpl">⚡ По шаблону</button>
       <button class="mini" data-act="planHist">📊 По прошлым тратам</button>
       ${prevPlan ? '<button class="mini" data-act="planPrev">↺ Как в прошлом месяце</button>' : ''}
       ${free > 0 && inc ? '<button class="mini" data-act="planRest">💰 Свободное → в накопления</button>' : ''}
       <button class="mini" data-act="planZero">🧹 Обнулить</button>
     </div>
     ${inc ? '' : '<div class="empty-note">Добавь доходы месяца — и бюджет распределится автоматически</div>'}
     ${groups}
     <details class="fdet" ${U.pctOpen ? 'open' : ''}><summary data-act="pctOpen">Шаблон процентов</summary>
       <p class="hint" style="margin-top:8px">Какую долю каждого дохода отдавать категории. Сумма должна быть 100%.</p>
       ${cats.map(c => `<div class="arow"><span class="txi">${esc(c.icon)}</span><div class="txn">${esc(c.name)}</div><input class="in famt" inputmode="decimal" data-bind="fin.pct.${c.id}" data-num="1" data-rerender="1" value="${num(fz.pct[c.id]) || ''}" placeholder="0" autocomplete="off"><span class="pctsign">%</span></div>`).join('')}
       <div class="pcttot ${Math.round(pctSum) === 100 ? 'ok' : 'bad'}">Итого: ${Math.round(pctSum * 10) / 10}% ${Math.round(pctSum) === 100 ? '✓' : pctSum > 100 ? '— больше 100%' : '— не распределено ' + Math.round((100 - pctSum) * 10) / 10 + '%'}</div>
       <div class="achips"><button class="mini" data-act="pctDef">Шаблон 50/30/20</button><button class="mini" data-act="pctHist">По прошлым тратам</button></div>
     </details>`, inc ? `${Math.round(alloc / inc * 100)}%` : '');
}

// Shares of spending per category over the 3 months before mk.
function histShares(mk) {
  const sums = {}; let total = 0;
  let k = mk;
  for (let i = 0; i < 3; i++) {
    k = prevMk(k);
    const st = finStats(k);
    for (const [id, v] of Object.entries(st.byCat)) { sums[id] = (sums[id] || 0) + v; total += v; }
  }
  return total ? Object.fromEntries(Object.entries(sums).map(([id, v]) => [id, v / total])) : null;
}

/* ---------------- Views ---------------- */
function viewFin() {
  const sec = U.fsec || 'budget';
  const segs = `<div class="segs">${FIN_SECS.map(([k, n]) => `<button class="seg ${sec === k ? 'on' : ''}" data-act="fsec" data-s="${k}">${n}</button>`).join('')}</div>`;
  return segs + FIN_VIEWS[sec]();
}

const FIN_VIEWS = {
  budget() {
    const d = pd(U.cur), mk = monthKey(d), fm = fmonth(mk), st = finStats(mk), P = `fin.months.${mk}`;
    const rate = st.income ? Math.round(st.saved / st.income * 100) : 0;
    let h = `<div class="hero fade fin-hero">
      <div class="fh-l">Остаток на ${MONTHS[d.getMonth()].toLowerCase()}</div>
      <div class="fh-v ${st.balance < 0 ? 'neg' : ''}">${money(st.balance)}</div>
      <div class="fstats">
        <div><span>Доходы</span><b>${money(st.income)}</b></div>
        <div><span>Расходы</span><b>${money(st.spent)}</b></div>
        <div><span>Отложено</span><b>${money(st.saved)}</b></div>
      </div>
      <div class="hint" style="margin:10px 0 0">Норма сбережений: <b style="color:var(--gold)">${rate}%</b>${st.income ? '' : ' · добавь доходы месяца ниже'}</div>
    </div>`;

    // Quick expense entry
    const curMonth = mk === monthKey(new Date());
    const defDate = curMonth ? today() : (U.cur.startsWith(mk) ? U.cur : mk + '-01');
    const sel = U.fcat || 'c_food';
    h += card('Записать расход', '🪙',
      `<input class="in fin-amt add-in" id="fAmt" data-addp="__fin" data-fact="fAdd" inputmode="decimal" placeholder="0 ${esc(F().currency)}" autocomplete="off">
       <div class="fcats">${F().cats.map(c => `<button class="fc ${c.id === sel ? 'on' : ''}" data-act="fcat" data-c="${c.id}">${esc(c.icon)}<span>${esc(c.name)}</span></button>`).join('')}</div>
       <div class="row" style="margin-top:8px"><input class="in" id="fNote" placeholder="Комментарий: «Пятёрочка», «такси»…" autocomplete="off"><input class="in" id="fDate" type="date" value="${defDate}" style="flex:none;width:150px"></div>
       <div id="fGuess" class="fguess"></div>
       <button class="btn wide" data-act="fAdd">Записать</button>`);

    // Income
    const prev = F().months[prevMk(mk)];
    h += card('Доходы месяца', '👑',
      `${fm.income.map((x, i) => `<div class="task"><span style="color:var(--gold)">◆</span>${inp(`${P}.income.${i}.t`, x.t, 'Источник', 'flat')}${moneyIn(`${P}.income.${i}.a`, x.a, '0', 'flat famt')}<button class="x" data-act="del" data-p="${P}.income" data-i="${i}">×</button></div>`).join('')}
       <div class="addrow"><input class="in" id="iSrc" placeholder="Зарплата, фриланс…" autocomplete="off"><input class="in" id="iAmt" inputmode="decimal" placeholder="Сумма" style="flex:none;width:110px" autocomplete="off"><button class="btn" data-act="incAdd">＋</button></div>
       ${!fm.income.length && prev && prev.income.length ? '<button class="btn ghost wide" data-act="incCopy">Как в прошлом месяце</button>' : ''}`,
      money(st.income));

    // Allocation (zero-based budget)
    h += allocCard(mk, st);

    // 50/30/20
    h += card('Правило 50 / 30 / 20', '⚖️',
      `<p class="hint">Элизабет Уоррен: 50% дохода — на нужды, 30% — на желания, 20% — на накопления и досрочное погашение долгов.</p>
       ${Object.entries(BUCKETS).map(([k, b]) => {
         const plan = st.income * b.pct / 100, fact = st.by[k];
         const over = k !== 'save' && plan > 0 && fact > plan;
         return `<div class="bk"><div class="bk-t"><span><i style="background:${b.color}"></i>${b.name} · ${b.pct}%</span><b class="${over ? 'neg' : ''}">${money(fact)} <small>из ${money(plan)}</small></b></div>
           ${fprog(plan ? fact / plan : 0, over)}<div class="bk-n">${b.note}</div></div>`;
       }).join('')}`);

    // Pay yourself first
    const target = st.income * F().payself / 100;
    h += card('Заплати сначала себе', '💎',
      `<p class="hint">Джордж Клейсон, «Самый богатый человек в Вавилоне»: в день дохода сразу откладывай часть — живи на остаток.</p>
       ${pills('fin.payself', [5, 10, 15, 20, 30].indexOf(F().payself) + 1, ['5%', '10%', '15%', '20%', '30%'], 'n10').replace(/data-act="set" data-p="fin.payself" data-v="(\d)"/g, (m, i) => `data-act="fPay" data-v="${[5, 10, 15, 20, 30][i - 1]}"`)}
       <div class="progline" style="margin-top:10px">${fprog(target ? st.saved / target : 0)}<span>${money(st.saved)} / ${money(target)}</span></div>
       ${target > st.saved ? `<button class="btn ghost wide" data-act="fPayDo" data-v="${Math.round((target - st.saved) * 100) / 100}">Отложить ${money(target - st.saved)}</button>` : st.income ? '<div class="empty-note">👑 Себе заплачено. Так держать!</div>' : ''}`);

    // Envelopes
    const env = F().cats.filter(c => c.bucket !== 'save' && (planOf(mk, c) > 0 || st.byCat[c.id]));
    h += card('Конверты', '✉️',
      `<p class="hint">Метод конвертов: в каждом конверте — сумма из распределения бюджета. Конверт пуст — траты в нём заканчиваются.</p>
       ${env.map(c => {
         const sp = st.byCat[c.id] || 0, lim = planOf(mk, c), over = lim > 0 && sp > lim;
         return `<div class="bk"><div class="bk-t"><span>${esc(c.icon)} ${esc(c.name)}</span><b class="${over ? 'neg' : ''}">${money(sp)}${lim ? ` <small>из ${money(lim)}</small>` : ''}</b></div>
           ${lim ? fprog(sp / lim, over) + `<div class="bk-n">${over ? 'Перерасход ' + money(sp - lim) : 'Осталось ' + money(lim - sp)}</div>` : '<div class="bk-n">Лимит не задан</div>'}</div>`;
       }).join('') || '<div class="empty-note">Распредели бюджет по категориям выше</div>'}
       <details class="fdet" ${U.catOpen ? 'open' : ''}><summary data-act="catOpen">Настроить категории</summary>
         ${F().cats.map((c, i) => `<div class="catrow"><input class="in ci-in" data-bind="fin.cats.${i}.icon" value="${esc(c.icon)}">${inp(`fin.cats.${i}.name`, c.name, '', 'flat')}
           <select class="in" data-bind="fin.cats.${i}.bucket">${Object.entries(BUCKETS).map(([k, b]) => `<option value="${k}" ${c.bucket === k ? 'selected' : ''}>${b.name}</option>`).join('')}</select>
           <button class="x" data-act="catDel" data-i="${i}">×</button></div>`).join('')}
         <div class="addrow"><input class="in" id="cIcon" style="width:56px;flex:none;text-align:center" placeholder="🏷️"><input class="in" id="cName" placeholder="Новая категория" autocomplete="off"><button class="btn" data-act="catAdd">＋</button></div>
       </details>
       <details class="fdet" ${U.rulesOpen ? 'open' : ''}><summary data-act="rulesOpen">Автокатегории 🪄</summary>
         <p class="hint" style="margin-top:8px">Категория расхода выбирается сама по комментарию: «Пятёрочка» → Продукты, «такси» → Транспорт. Встроено более ${Math.floor(AUTO_KW.reduce((s, x) => s + x[1].length, 0) / 10) * 10} слов, а твои выборы Планёр запоминает.</p>
         ${Object.entries(F().rules).map(([w, id]) => `<div class="tx"><span class="txi">${esc(catOf(id).icon)}</span><div class="txn">«${esc(w)}»<small>→ ${esc(catOf(id).name)}</small></div><button class="x" data-act="ruleDel" data-w="${esc(w)}">×</button></div>`).join('') || '<div class="empty-note">Своих правил пока нет — они появятся сами</div>'}
         <div class="row" style="margin-top:8px"><input class="in" id="rWord" placeholder="Слово: «лента», «спортзал»" autocomplete="off"><select class="in" id="rCat" style="flex:none;width:130px">${F().cats.map(c => `<option value="${c.id}">${esc(c.icon)} ${esc(c.name)}</option>`).join('')}</select><button class="btn" data-act="ruleAdd">＋</button></div>
       </details>`);

    // Structure donut
    const parts = Object.entries(st.byCat).filter(([id]) => catOf(id).bucket !== 'save').sort((a, b) => b[1] - a[1])
      .map(([id, v], i) => ({ id, v, color: FIN_COLORS[i % FIN_COLORS.length] }));
    if (parts.length) {
      h += card('Куда уходят деньги', '🥧',
        `<div class="donut-box">${donut(parts, st.spent, 'расходы')}</div>
         <div class="flegend">${parts.map(p => `<div><i style="background:${p.color}"></i>${esc(catOf(p.id).icon)} ${esc(catOf(p.id).name)}<b>${Math.round(p.v / st.spent * 100)}%</b></div>`).join('')}</div>`);
    }

    // Transactions
    const txs = F().tx.filter(t => t.date.startsWith(mk)).sort((a, b) => b.date.localeCompare(a.date) || (b.ts || 0) - (a.ts || 0));
    let list = '', lastD = '';
    for (const t of txs) {
      if (t.date !== lastD) {
        lastD = t.date; const dd = pd(t.date);
        list += `<div class="txd">${dd.getDate()} ${MONTHS_GEN[dd.getMonth()]}, ${WD[wdi(dd)]}</div>`;
      }
      const c = catOf(t.cat);
      list += `<div class="tx"><span class="txi">${esc(c.icon)}</span><div class="txn">${esc(t.note || c.name)}${t.note ? `<small>${esc(c.name)}</small>` : ''}</div><b class="${c.bucket === 'save' ? 'pos' : ''}">${money(t.amt)}</b><button class="x" data-act="txDel" data-id="${t.id}">×</button></div>`;
    }
    h += card('Операции', '📒', list || '<div class="empty-note">В этом месяце ещё нет записей</div>', txs.length ? String(txs.length) : '');
    return h;
  },

  goals() {
    const fz = F();
    const now = new Date();
    // Average of essential spending over the last 3 months with data.
    let essSum = 0, essN = 0;
    for (let i = 1; i <= 3; i++) {
      const s = finStats(monthKey(new Date(now.getFullYear(), now.getMonth() - i, 1)));
      if (s.by.need) { essSum += s.by.need; essN++; }
    }
    const essAvg = essN ? Math.round(essSum / essN) : 0;
    const base = num(fz.cushion.base) || essAvg;
    const ctarget = base * fz.cushion.months;
    let h = card('Подушка безопасности', '🛡️',
      `<p class="hint">Запас на 3–6 месяцев обязательных расходов — защита королевства от любых бурь. Хранить отдельно и не трогать.</p>
       <div class="lbl">Обязательные расходы в месяц</div>${moneyIn('fin.cushion.base', fz.cushion.base, essAvg ? `≈ ${essAvg} (по факту)` : 'Сумма')}
       <div class="lbl">На сколько месяцев</div>${[3, 6, 9, 12].map(m => `<button class="pill ${fz.cushion.months === m ? 'on' : ''}" style="margin-right:6px" data-act="cushM" data-v="${m}">${m}</button>`).join('')}
       <div class="goal-sum"><span>${money(fz.cushion.saved)}</span><small>из ${money(ctarget)}</small></div>
       ${fprog(ctarget ? fz.cushion.saved / ctarget : 0)}
       <div class="addrow"><input class="in" id="cushAmt" inputmode="decimal" placeholder="Сумма" autocomplete="off"><button class="btn" data-act="cushDep">Пополнить</button><button class="btn ghost" data-act="cushWd">Снять</button></div>`);

    h += card('Цели-копилки', '🏺',
      `<p class="hint">Каждая мечта — отдельная копилка с суммой и сроком. Планёр посчитает, сколько откладывать в месяц.</p>
       ${fz.goals.map((g, i) => {
         const left = Math.max(0, num(g.target) - num(g.saved));
         let per = '';
         if (g.deadline && left > 0) {
           const [y, m] = g.deadline.split('-').map(Number);
           const months = Math.max(1, (y - now.getFullYear()) * 12 + (m - 1 - now.getMonth()) + 1);
           per = `${money(left / months)} в месяц · ${months} мес.`;
         }
         const done = num(g.target) > 0 && left === 0;
         return `<div class="goal ${done ? 'done' : ''}">
           <div class="oh"><input class="in ci-in" data-bind="fin.goals.${i}.icon" value="${esc(g.icon)}">${inp(`fin.goals.${i}.name`, g.name, 'Цель', 'flat gname')}<button class="x" data-act="goalDel" data-i="${i}">×</button></div>
           <div class="goal-sum"><span>${money(g.saved)}</span><small>из</small>${moneyIn(`fin.goals.${i}.target`, g.target, 'сумма', 'flat famt')}</div>
           ${fprog(num(g.target) ? num(g.saved) / num(g.target) : 0)}
           <div class="row" style="margin-top:6px"><span class="hint" style="margin:0;flex:1">${done ? '👑 Цель достигнута!' : per || 'Укажи срок →'}</span><input class="in" type="month" data-bind="fin.goals.${i}.deadline" data-rerender="1" value="${esc(g.deadline || '')}" style="flex:none;width:150px;padding:6px 8px;font-size:13px"></div>
           <div class="addrow"><input class="in g-amt" inputmode="decimal" placeholder="Сумма" autocomplete="off"><button class="btn" data-act="goalDep" data-i="${i}">Пополнить</button></div></div>`;
       }).join('')}
       <div class="addrow"><input class="in" id="gName" placeholder="Новая цель: машина, отпуск…" autocomplete="off"><input class="in" id="gAmt" inputmode="decimal" placeholder="Сумма" style="flex:none;width:100px" autocomplete="off"><button class="btn" data-act="goalAdd">＋</button></div>`);

    const nowMs = Date.now();
    h += card('Список желаний · 30 дней', '⏳',
      `<p class="hint">Правило 30 дней против импульсивных покупок: запиши желание и подожди месяц. Если всё ещё хочется — заведи под него копилку.</p>
       ${fz.wishes.map((w, i) => {
         const ready = nowMs - w.added >= 30 * 864e5;
         const dl = new Date(w.added + 30 * 864e5);
         return `<div class="tx"><span class="txi">${ready ? '✅' : '⏳'}</span><div class="txn">${esc(w.t)}<small>${ready ? 'Прошло 30 дней — всё ещё хочешь?' : `Решение после ${dl.getDate()} ${MONTHS_GEN[dl.getMonth()]}`}</small></div><b>${num(w.price) ? money(w.price) : ''}</b>
           ${ready ? `<button class="mini" data-act="wishGoal" data-i="${i}">→ 🏺</button>` : ''}<button class="x" data-act="wishDel" data-i="${i}">×</button></div>`;
       }).join('') || '<div class="empty-note">Пусто — отличная финансовая дисциплина</div>'}
       <div class="addrow"><input class="in" id="wName" placeholder="Чего хочется купить?" autocomplete="off"><input class="in" id="wAmt" inputmode="decimal" placeholder="Цена" style="flex:none;width:100px" autocomplete="off"><button class="btn" data-act="wishAdd">＋</button></div>`);
    return h;
  },

  debts() {
    const fz = F(), m = fz.debtMethod;
    const list = fz.debts.map((x, i) => ({ x, i })).filter(o => num(o.x.bal) > 0)
      .sort((a, b) => m === 'snowball' ? num(a.x.bal) - num(b.x.bal) : num(b.x.rate) - num(a.x.rate));
    const closed = fz.debts.map((x, i) => ({ x, i })).filter(o => !(num(o.x.bal) > 0));
    const total = list.reduce((s, o) => s + num(o.x.bal), 0), mins = list.reduce((s, o) => s + num(o.x.min), 0);
    const orig = fz.debts.reduce((s, x) => s + Math.max(num(x.orig), num(x.bal)), 0);
    let h = `<div class="hero fade fin-hero"><div class="fh-l">Общий долг</div><div class="fh-v ${total ? 'neg' : ''}">${money(total)}</div>
      <div class="fstats"><div><span>Минимум в месяц</span><b>${money(mins)}</b></div><div><span>Погашено</span><b>${orig ? Math.round((1 - total / orig) * 100) : 0}%</b></div></div>
      ${orig ? fprog(1 - total / orig) : ''}</div>`;
    const row = (o, first) => {
      const x = o.x, i = o.i, P = `fin.debts.${i}`;
      const pr = num(x.orig) ? 1 - num(x.bal) / num(x.orig) : 0;
      return `<div class="goal ${first ? 'target' : ''} ${num(x.bal) > 0 ? '' : 'done'}">
        ${first ? '<div class="tgt">🎯 Атакуем первым</div>' : ''}
        <div class="oh">${inp(`${P}.name`, x.name, 'Кредит, карта…', 'flat gname')}<button class="x" data-act="debtDel" data-i="${i}">×</button></div>
        <div class="dgrid"><label>Остаток${moneyIn(`${P}.bal`, x.bal)}</label><label>Ставка, %${moneyIn(`${P}.rate`, x.rate)}</label><label>Минимум${moneyIn(`${P}.min`, x.min)}</label></div>
        ${fprog(pr)}
        ${num(x.bal) > 0 ? `<div class="addrow"><input class="in d-amt" inputmode="decimal" placeholder="Платёж" autocomplete="off"><button class="btn" data-act="debtPay" data-i="${i}">Внести</button></div>` : '<div class="empty-note">🎉 Долг закрыт!</div>'}</div>`;
    };
    h += card('Стратегия погашения', '⚔️',
      `<div class="pills" style="margin-bottom:8px"><button class="pill ${m === 'snowball' ? 'on' : ''}" style="flex:1" data-act="debtM" data-v="snowball">☃️ Снежный ком</button><button class="pill ${m === 'avalanche' ? 'on' : ''}" style="flex:1" data-act="debtM" data-v="avalanche">🏔️ Лавина</button></div>
       <p class="hint" style="margin:0">${m === 'snowball'
         ? '<b>Снежный ком</b> (Дэйв Рэмси): по всем долгам — минимум, а все свободные деньги — в самый маленький долг. Быстрые победы дают мотивацию.'
         : '<b>Лавина</b>: по всем долгам — минимум, а всё свободное — в долг с самой высокой ставкой. Математически самый выгодный путь.'}</p>`);
    h += card('Мои долги', '💳',
      `${list.map((o, k) => row(o, k === 0)).join('')}${closed.map(o => row(o, false)).join('')}
       ${fz.debts.length ? '' : '<div class="empty-note">Долгов нет — королевство свободно 👑</div>'}
       <div class="addrow"><input class="in" id="dName" placeholder="Название долга" autocomplete="off"><input class="in" id="dAmt" inputmode="decimal" placeholder="Сумма" style="flex:none;width:100px" autocomplete="off"><button class="btn" data-act="debtAdd">＋</button></div>`);
    return h;
  },

  bills() {
    const d = pd(U.cur), mk = monthKey(d), fm = fmonth(mk), fz = F();
    const isCur = mk === monthKey(new Date()), td = new Date().getDate();
    const list = fz.bills.map((b, i) => ({ b, i })).sort((a, b) => (+a.b.day || 0) - (+b.b.day || 0));
    const total = fz.bills.reduce((s, b) => s + num(b.amt), 0);
    const paid = fz.bills.filter(b => fm.paid[b.id]).reduce((s, b) => s + num(b.amt), 0);
    let h = `<div class="hero fade fin-hero"><div class="fh-l">Обязательные платежи · ${MONTHS[d.getMonth()].toLowerCase()}</div><div class="fh-v">${money(total)}</div>
      <div class="fstats"><div><span>Оплачено</span><b>${money(paid)}</b></div><div><span>Осталось</span><b>${money(total - paid)}</b></div></div>${fprog(total ? paid / total : 0)}</div>`;
    h += card('Календарь платежей', '📆',
      `<p class="hint">Аренда, коммуналка, связь, подписки. Отметка «оплачено» сама запишет расход в бюджет месяца.</p>
       ${list.map(({ b, i }) => {
         const on = !!fm.paid[b.id];
         const late = isCur && !on && +b.day < td, soon = isCur && !on && +b.day >= td && +b.day - td <= 3;
         return `<div class="bill ${on ? 'done' : ''} ${late ? 'late' : ''} ${soon ? 'soon' : ''}">
           <button class="chk ${on ? 'on' : ''}" data-act="billPay" data-i="${i}"></button>
           <div class="bday"><b>${+b.day || '—'}</b><span>число</span></div>
           <div class="txn">${inp(`fin.bills.${i}.name`, b.name, 'Платёж', 'flat')}<small>${late ? '⚠️ Просрочен' : soon ? '⏰ Скоро' : esc(catOf(b.cat).icon + ' ' + catOf(b.cat).name)}</small></div>
           ${moneyIn(`fin.bills.${i}.amt`, b.amt, '0', 'flat famt')}<button class="x" data-act="billDel" data-i="${i}">×</button></div>`;
       }).join('') || '<div class="empty-note">Добавь регулярные платежи</div>'}
       <div class="lbl" style="margin-top:12px">Новый платёж</div>
       <div class="row"><input class="in" id="bName" placeholder="Название" autocomplete="off"><input class="in" id="bAmt" inputmode="decimal" placeholder="Сумма" style="flex:none;width:96px" autocomplete="off"></div>
       <div class="row" style="margin-top:8px"><select class="in" id="bDay">${[...Array(31).keys()].map(i => `<option value="${i + 1}">${i + 1} число</option>`).join('')}</select>
         <select class="in" id="bCat">${fz.cats.filter(c => c.bucket !== 'save').map(c => `<option value="${c.id}" ${c.id === 'c_home' ? 'selected' : ''}>${esc(c.icon)} ${esc(c.name)}</option>`).join('')}</select></div>
       <button class="btn wide" data-act="billAdd">Добавить платёж</button>`);
    return h;
  },

  fyear() {
    const y = pd(U.cur).getFullYear(), curMk = monthKey(new Date());
    const rows = [], tot = { income: 0, spent: 0, saved: 0 }, byCat = {};
    for (let m = 0; m < 12; m++) {
      const mk = `${y}-${pad(m + 1)}`, st = finStats(mk);
      rows.push({ l: MON_S[m], a: st.income, b: st.spent, cur: mk === curMk });
      tot.income += st.income; tot.spent += st.spent; tot.saved += st.saved;
      for (const [k, v] of Object.entries(st.byCat)) byCat[k] = (byCat[k] || 0) + v;
    }
    const rate = tot.income ? Math.round(tot.saved / tot.income * 100) : 0;
    let h = `<div class="hero fade fin-hero"><div class="fh-l">Казна за ${y} год</div><div class="fh-v ${tot.income - tot.spent - tot.saved < 0 ? 'neg' : ''}">${money(tot.income - tot.spent - tot.saved)}</div>
      <div class="fstats"><div><span>Доходы</span><b>${money(tot.income)}</b></div><div><span>Расходы</span><b>${money(tot.spent)}</b></div><div><span>Отложено</span><b>${money(tot.saved)}</b></div></div>
      <div class="hint" style="margin:10px 0 0">Норма сбережений за год: <b style="color:var(--gold)">${rate}%</b></div></div>`;
    h += card('Доходы и расходы по месяцам', '📊',
      `${bars(rows)}<div class="legend"><span><i style="background:var(--gold)"></i>доходы</span><span><i style="background:var(--ruby)"></i>расходы</span></div>
       <div class="months" style="margin-top:12px">${rows.map((r, m) => `<button class="mt ${r.cur ? 'cur' : ''}" data-act="fGoMonth" data-k="${y}-${pad(m + 1)}"><b>${MONTHS[m]}</b><span>${r.a || r.b ? money(r.a - r.b) : '—'}</span></button>`).join('')}</div>`);
    const top = Object.entries(byCat).filter(([id]) => catOf(id).bucket !== 'save').sort((a, b) => b[1] - a[1]);
    if (top.length) {
      const mx = top[0][1];
      h += card('Главные статьи расходов', '🏆', top.map(([id, v]) => `<div class="bk"><div class="bk-t"><span>${esc(catOf(id).icon)} ${esc(catOf(id).name)}</span><b>${money(v)}</b></div>${fprog(v / mx)}</div>`).join(''));
    }
    const Y = year(String(y));
    if (!Y.fgoals) Y.fgoals = [];
    h += card('Финансовые цели года', '🏰', `<p class="hint">Например: «Подушка на 6 месяцев», «Закрыть кредитку», «Инвестировать 10% дохода». Их же удобно вести как OKR во вкладке «Год».</p>${dlist(`years.${y}.fgoals`, 'Финансовая цель года…')}`);
    return h;
  },
};

/* Small card on the Day screen. */
function finDayCard(k) {
  const fz = F(), txs = fz.tx.filter(t => t.date === k && catOf(t.cat).bucket !== 'save');
  const sum = txs.reduce((s, t) => s + t.amt, 0);
  const dd = pd(k), mk = monthKey(dd), fm = fz.months[mk];
  const due = fz.bills.filter(b => (+b.day === dd.getDate() || +b.day === dd.getDate() + 1) && !(fm && fm.paid[b.id]));
  return card('Казна дня', '🪙',
    `<div class="row"><div style="flex:1"><div class="hint" style="margin:0">Потрачено</div><div class="fday">${money(sum)}</div></div><button class="btn sm" data-act="fQuick">＋ Расход</button></div>
     ${txs.slice(-3).reverse().map(t => `<div class="tx"><span class="txi">${esc(catOf(t.cat).icon)}</span><div class="txn">${esc(t.note || catOf(t.cat).name)}</div><b>${money(t.amt)}</b></div>`).join('')}
     ${due.map(b => `<div class="tx"><span class="txi">⏰</span><div class="txn">${esc(b.name)}<small>платёж ${+b.day === dd.getDate() ? 'сегодня' : 'завтра'}</small></div><b>${money(b.amt)}</b></div>`).join('')}`);
}

/* ---------------- Actions ---------------- */
const val = sel => { const e = typeof sel === 'string' ? $(sel) : sel; return e ? e.value.trim() : ''; };
const FA = {
  fsec: ds => { U.fsec = ds.s; render(true); },
  catOpen: () => { U.catOpen = !U.catOpen; },
  fQuick: () => go({ tab: 'fin', fsec: 'budget', cur: today() }),
  fGoMonth: ds => go({ fsec: 'budget', cur: ds.k + '-01' }),
  pctOpen: () => { U.pctOpen = !U.pctOpen; },
  rulesOpen: () => { U.rulesOpen = !U.rulesOpen; },
  autoSplit() { F().autoSplit = !F().autoSplit; render(); },
  planAuto(ds) { const m = fmonth(monthKey(pd(U.cur))); delete m.plan[ds.c]; render(); },
  planTpl() { fmonth(monthKey(pd(U.cur))).plan = {}; F().autoSplit = true; toast('⚡ Доход распределён по шаблону'); render(); },
  planHist() {
    const mk = monthKey(pd(U.cur)), sh = histShares(mk), inc = monthIncome(mk);
    if (!sh) { toast('Нет трат за прошлые 3 месяца'); return; }
    if (!inc) { toast('Сначала добавь доходы месяца'); return; }
    // Savings keep their template share; the rest follows past spending.
    const plan = {}, savePct = F().cats.filter(c => c.bucket === 'save').reduce((s, c) => s + num(F().pct[c.id]), 0);
    const spendShare = Object.entries(sh).filter(([id]) => catOf(id).bucket !== 'save').reduce((s, [, v]) => s + v, 0) || 1;
    const rest = inc * (1 - Math.min(100, savePct) / 100);
    for (const c of F().cats) plan[c.id] = c.bucket === 'save' ? Math.round(inc * num(F().pct[c.id]) / 100) : Math.round(rest * (sh[c.id] || 0) / spendShare);
    fmonth(mk).plan = plan; toast('📊 Распределено по прошлым тратам'); render();
  },
  planPrev() {
    const mk = monthKey(pd(U.cur)), pk = prevMk(mk), plan = {};
    for (const c of F().cats) plan[c.id] = planOf(pk, c);
    fmonth(mk).plan = plan; toast('Распределение как в прошлом месяце'); render();
  },
  planRest() {
    const mk = monthKey(pd(U.cur)), c = saveCat(); if (!c) return;
    const free = monthIncome(mk) - F().cats.reduce((s, x) => s + planOf(mk, x), 0);
    if (free <= 0) return;
    fmonth(mk).plan[c.id] = planOf(mk, c) + Math.round(free);
    toast(`💰 В накопления: +${money(free)}`); render();
  },
  async planZero() {
    if (!(await ask('Обнулить распределение?', 'Все суммы этого месяца станут нулевыми, чтобы распределить заново вручную.', 'Обнулить'))) return;
    const plan = {}; for (const c of F().cats) plan[c.id] = 0;
    fmonth(monthKey(pd(U.cur))).plan = plan; render();
  },
  pctDef() {
    const p = {};
    for (const c of F().cats) p[c.id] = DEF_PCT[c.id] || 0;
    F().pct = p; toast('Шаблон 50/30/20'); render();
  },
  pctHist() {
    const sh = histShares(monthKey(pd(U.cur)));
    if (!sh) { toast('Нет трат за прошлые 3 месяца'); return; }
    // Spending shares fill 80%; 20% stays reserved for savings.
    const p = {}, sc = saveCat();
    for (const c of F().cats) p[c.id] = c.bucket === 'save' ? 0 : Math.round((sh[c.id] || 0) * 800) / 10;
    if (sc) p[sc.id] = 20;
    F().pct = p; toast('Шаблон по прошлым тратам'); render();
  },
  ruleAdd() {
    const w = ruleKey(val('#rWord')); if (w.length < 2) return;
    F().rules[w] = val('#rCat'); render();
  },
  ruleDel(ds) { delete F().rules[ds.w]; render(); },
  fGuess() {
    const g = guessCat(val('#fNote')), box = $('#fGuess');
    if (U.fcatManual || !g) { if (box && !U.fcatManual) box.textContent = ''; return; }
    U.fcat = g; U.fcatAuto = true;
    document.querySelectorAll('.fc').forEach(b => b.classList.toggle('on', b.dataset.c === g));
    if (box) box.textContent = `🪄 Категория определена автоматически: ${catOf(g).icon} ${catOf(g).name}`;
  },
  fcat(ds) {
    U.fcatManual = true; U.fcatAuto = false;
    const box = $('#fGuess'); if (box) box.textContent = '';
    U.fcat = ds.c;
    document.querySelectorAll('.fc').forEach(b => b.classList.toggle('on', b.dataset.c === ds.c));
  },
  fAdd() {
    const t = addTx(val('#fAmt'), U.fcat || 'c_food', val('#fNote'), val('#fDate') || today());
    if (!t) { toast('Введите сумму'); $('#fAmt').focus(); return; }
    // Learn: a manually chosen category for this word wins next time.
    const w = ruleKey(t.note);
    if (w.length >= 3 && U.fcatManual && guessCat(t.note) !== t.cat) F().rules[w] = t.cat;
    const auto = U.fcatAuto;
    U.fcatManual = false; U.fcatAuto = false;
    vibrate(20); toast(`${auto ? '🪄 ' : ''}Записано: ${money(t.amt)} · ${catOf(t.cat).name}`); render();
  },
  txDel(ds) {
    const fz = F(), i = fz.tx.findIndex(t => t.id === ds.id);
    if (i < 0) return;
    fz.tx.splice(i, 1);
    for (const m of Object.values(fz.months)) for (const k in m.paid) if (m.paid[k] === ds.id) delete m.paid[k];
    render();
  },
  incAdd() {
    const a = num(val('#iAmt')), t = val('#iSrc') || 'Доход';
    if (!(a > 0)) { toast('Введите сумму'); return; }
    fmonth(monthKey(pd(U.cur))).income.push({ t, a }); render();
  },
  incCopy() {
    const mk = monthKey(pd(U.cur));
    fmonth(mk).income = JSON.parse(JSON.stringify(F().months[prevMk(mk)].income)); render();
  },
  fPay(ds) { F().payself = +ds.v; render(); },
  fPayDo(ds) {
    const c = saveCat(); if (!c) return;
    const mk = monthKey(pd(U.cur)), date = mk === monthKey(new Date()) ? today() : mk + '-01';
    addTx(ds.v, c.id, 'Заплатил себе', date); toast('💎 Отложено'); render();
  },
  catAdd() {
    const n = val('#cName'); if (!n) return;
    F().cats.push({ id: 'c' + uid(), icon: val('#cIcon') || '🏷️', name: n, bucket: 'want', limit: 0 }); render();
  },
  async catDel(ds) {
    const c = F().cats[+ds.i];
    if (!(await ask('Удалить категорию?', `${esc(c.icon)} ${esc(c.name)}. Записанные операции останутся без категории.`, 'Удалить'))) return;
    F().cats.splice(+ds.i, 1); render();
  },
  cushM(ds) { F().cushion.months = +ds.v; render(); },
  cushDep() {
    const a = num(val('#cushAmt')); if (!(a > 0)) return;
    F().cushion.saved = num(F().cushion.saved) + a;
    const c = saveCat(); if (c) addTx(a, c.id, 'Подушка безопасности');
    toast('🛡️ Подушка пополнена'); render();
  },
  cushWd() {
    const a = num(val('#cushAmt')); if (!(a > 0)) return;
    F().cushion.saved = Math.max(0, num(F().cushion.saved) - a); render();
  },
  goalAdd() {
    const n = val('#gName'); if (!n) return;
    F().goals.push({ id: 'g' + uid(), icon: '🏺', name: n, target: num(val('#gAmt')), saved: 0, deadline: '' }); render();
  },
  goalDep(ds, el) {
    const a = num(val(el.closest('.addrow').querySelector('.g-amt'))); if (!(a > 0)) return;
    const g = F().goals[+ds.i]; g.saved = num(g.saved) + a;
    const c = saveCat(); if (c) addTx(a, c.id, `Копилка: ${g.name}`);
    vibrate(20); toast(num(g.target) && g.saved >= num(g.target) ? '👑 Цель достигнута!' : `${esc(g.icon)} +${money(a)}`); render();
  },
  async goalDel(ds) {
    const g = F().goals[+ds.i];
    if (!(await ask('Удалить копилку?', esc(g.name || 'Без названия'), 'Удалить'))) return;
    F().goals.splice(+ds.i, 1); render();
  },
  wishAdd() {
    const n = val('#wName'); if (!n) return;
    F().wishes.push({ id: 'w' + uid(), t: n, price: num(val('#wAmt')), added: Date.now() }); render();
  },
  wishDel(ds) { F().wishes.splice(+ds.i, 1); render(); },
  wishGoal(ds) {
    const w = F().wishes.splice(+ds.i, 1)[0];
    F().goals.push({ id: 'g' + uid(), icon: '🎁', name: w.t, target: w.price, saved: 0, deadline: '' });
    toast('Желание стало копилкой 🏺'); render();
  },
  debtM(ds) { F().debtMethod = ds.v; render(); },
  debtAdd() {
    const n = val('#dName'), a = num(val('#dAmt')); if (!n || !(a > 0)) { toast('Название и сумма долга'); return; }
    F().debts.push({ id: 'd' + uid(), name: n, bal: a, orig: a, rate: 0, min: 0 }); render();
  },
  debtPay(ds, el) {
    const a = num(val(el.closest('.addrow').querySelector('.d-amt'))); if (!(a > 0)) return;
    const x = F().debts[+ds.i];
    if (!num(x.orig) || num(x.orig) < num(x.bal)) x.orig = num(x.bal);
    x.bal = Math.max(0, Math.round((num(x.bal) - a) * 100) / 100);
    const c = F().cats.find(c => c.id === 'c_debt'); addTx(a, c ? c.id : 'c_debt', `Платёж: ${x.name}`);
    vibrate(20); toast(x.bal ? `⚔️ Минус ${money(a)}` : '🎉 Долг закрыт!'); render();
  },
  async debtDel(ds) {
    const x = F().debts[+ds.i];
    if (!(await ask('Удалить долг?', esc(x.name), 'Удалить'))) return;
    F().debts.splice(+ds.i, 1); render();
  },
  billAdd() {
    const n = val('#bName'); if (!n) { toast('Введите название'); return; }
    F().bills.push({ id: 'b' + uid(), name: n, amt: num(val('#bAmt')), day: +val('#bDay') || 1, cat: val('#bCat') || 'c_home' }); render();
  },
  billPay(ds) {
    const b = F().bills[+ds.i], d = pd(U.cur), mk = monthKey(d), fm = fmonth(mk);
    if (fm.paid[b.id]) {
      const i = F().tx.findIndex(t => t.id === fm.paid[b.id]);
      if (i >= 0) F().tx.splice(i, 1);
      delete fm.paid[b.id];
    } else {
      const day = Math.min(+b.day || 1, daysIn(d.getFullYear(), d.getMonth()));
      const date = mk === monthKey(new Date()) ? today() : `${mk}-${pad(day)}`;
      const t = addTx(b.amt, b.cat, b.name, date);
      fm.paid[b.id] = t ? t.id : true;
      vibrate(20);
    }
    render();
  },
  async billDel(ds) {
    const b = F().bills[+ds.i];
    if (!(await ask('Удалить платёж?', esc(b.name), 'Удалить'))) return;
    F().bills.splice(+ds.i, 1); render();
  },
};

document.addEventListener('input', e => { if (e.target.id === 'fNote') FA.fGuess(); });
