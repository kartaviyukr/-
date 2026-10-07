'use strict';
/* Казна — финансовое планирование Планёра.
   Подключается до app.js; функции используют общие помощники из app.js во время вызова. */

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
const fmonth = mk => def(F().months[mk] || (F().months[mk] = {}), { income: [], paid: {} });
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
       <div class="row" style="margin-top:8px"><input class="in" id="fNote" placeholder="Комментарий" autocomplete="off"><input class="in" id="fDate" type="date" value="${defDate}" style="flex:none;width:150px"></div>
       <button class="btn wide" data-act="fAdd">Записать</button>`);

    // Income
    const prev = F().months[prevMk(mk)];
    h += card('Доходы месяца', '👑',
      `${fm.income.map((x, i) => `<div class="task"><span style="color:var(--gold)">◆</span>${inp(`${P}.income.${i}.t`, x.t, 'Источник', 'flat')}${moneyIn(`${P}.income.${i}.a`, x.a, '0', 'flat famt')}<button class="x" data-act="del" data-p="${P}.income" data-i="${i}">×</button></div>`).join('')}
       <div class="addrow"><input class="in" id="iSrc" placeholder="Зарплата, фриланс…" autocomplete="off"><input class="in" id="iAmt" inputmode="decimal" placeholder="Сумма" style="flex:none;width:110px" autocomplete="off"><button class="btn" data-act="incAdd">＋</button></div>
       ${!fm.income.length && prev && prev.income.length ? '<button class="btn ghost wide" data-act="incCopy">Как в прошлом месяце</button>' : ''}`,
      money(st.income));

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
    const env = F().cats.filter(c => c.bucket !== 'save' && (num(c.limit) > 0 || st.byCat[c.id]));
    h += card('Конверты', '✉️',
      `<p class="hint">Метод конвертов: у каждой категории свой лимит на месяц. Конверт пуст — траты в нём заканчиваются.</p>
       ${env.map(c => {
         const sp = st.byCat[c.id] || 0, lim = num(c.limit), over = lim > 0 && sp > lim;
         return `<div class="bk"><div class="bk-t"><span>${esc(c.icon)} ${esc(c.name)}</span><b class="${over ? 'neg' : ''}">${money(sp)}${lim ? ` <small>из ${money(lim)}</small>` : ''}</b></div>
           ${lim ? fprog(sp / lim, over) + `<div class="bk-n">${over ? 'Перерасход ' + money(sp - lim) : 'Осталось ' + money(lim - sp)}</div>` : '<div class="bk-n">Лимит не задан</div>'}</div>`;
       }).join('') || '<div class="empty-note">Задай лимиты категорий ниже</div>'}
       <details class="fdet" ${U.catOpen ? 'open' : ''}><summary data-act="catOpen">Настроить категории и лимиты</summary>
         ${F().cats.map((c, i) => `<div class="catrow"><input class="in ci-in" data-bind="fin.cats.${i}.icon" value="${esc(c.icon)}">${inp(`fin.cats.${i}.name`, c.name, '', 'flat')}
           <select class="in" data-bind="fin.cats.${i}.bucket">${Object.entries(BUCKETS).map(([k, b]) => `<option value="${k}" ${c.bucket === k ? 'selected' : ''}>${b.name}</option>`).join('')}</select>
           ${c.bucket === 'save' ? '<span></span>' : moneyIn(`fin.cats.${i}.limit`, c.limit, 'лимит', 'famt')}
           <button class="x" data-act="catDel" data-i="${i}">×</button></div>`).join('')}
         <div class="addrow"><input class="in" id="cIcon" style="width:56px;flex:none;text-align:center" placeholder="🏷️"><input class="in" id="cName" placeholder="Новая категория" autocomplete="off"><button class="btn" data-act="catAdd">＋</button></div>
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
  fcat(ds) {
    U.fcat = ds.c;
    document.querySelectorAll('.fc').forEach(b => b.classList.toggle('on', b.dataset.c === ds.c));
  },
  fAdd() {
    const t = addTx(val('#fAmt'), U.fcat || 'c_food', val('#fNote'), val('#fDate') || today());
    if (!t) { toast('Введите сумму'); $('#fAmt').focus(); return; }
    vibrate(20); toast(`Записано: ${money(t.amt)} · ${catOf(t.cat).name}`); render();
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
