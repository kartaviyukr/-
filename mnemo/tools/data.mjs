// Проверка и обслуживание словарей Мнемо.
//   node mnemo/tools/data.mjs          — статистика и проверка (код выхода 1 при ошибках)
//   node mnemo/tools/data.mjs --fix    — удалить повторы слов и переписать список скриптов в index.html
import { readFileSync, writeFileSync, readdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import vm from 'node:vm';

const WWW = join(dirname(fileURLToPath(import.meta.url)), '../app/src/main/assets/www');
const DATA = join(WWW, 'data');
const fix = process.argv.includes('--fix');
const LANG_ORDER = ['en', 'it', 'uk'];
const LEVELS = ['a0', 'a1', 'a2', 'b1', 'b2', 'c1', 'c2'];

// Порядок загрузки: en-core, затем по языку, уровню и номеру части.
const rank = f => {
  const m = /^([a-z]+)-([a-z0-9]+)(?:-(\d+))?\.js$/.exec(f);
  if (!m) return [99, 99, 0, f];
  const li = LANG_ORDER.indexOf(m[1]);
  const lv = m[2] === 'core' ? -1 : LEVELS.indexOf(m[2]);
  return [li < 0 ? 98 : li, lv, Number(m[3] || 0), f];
};
const cmp = (a, b) => { const x = rank(a), y = rank(b); for (let i = 0; i < 4; i++) if (x[i] !== y[i]) return x[i] < y[i] ? -1 : 1; return 0; };
const files = readdirSync(DATA).filter(f => f.endsWith('.js')).sort(cmp);

let errors = 0;
const seen = new Map();
const stats = {};
for (const f of files) {
  const path = join(DATA, f);
  let text = readFileSync(path, 'utf8');
  const ctx = { window: {} };
  vm.runInNewContext(text, ctx, { filename: f });
  const decks = ctx.window.MNEMO_DECKS || [];
  const removed = new Set();
  for (const d of decks) {
    if (!d.lang || !d.id || !d.title) { console.log(`✗ ${f}: у набора нет lang/id/title`, d.id); errors++; }
    let n = 0;
    for (const raw of String(d.cards).split('\n')) {
      const line = raw.trim();
      if (!line) continue;
      const p = line.split('|').map(s => s.trim());
      if (p.length < 2 || !p[0] || !p[1]) { console.log(`✗ ${f} [${d.id}]: плохая строка: ${line}`); errors++; continue; }
      const key = `${d.lang}:${p[0]}`;
      if (seen.has(key)) {
        if (fix) removed.add(raw);
        else console.log(`  повтор ${key} в ${d.id} (уже в ${seen.get(key)})`);
        continue;
      }
      seen.set(key, d.id);
      n++;
    }
    const s = (stats[d.lang] ||= {});
    s[d.level || '—'] = (s[d.level || '—'] || 0) + n;
  }
  if (fix && removed.size) {
    text = text.split('\n').filter(l => !removed.has(l.trim()) || !l.includes('|')).join('\n');
    writeFileSync(path, text);
    console.log(`${f}: удалено повторов ${removed.size}`);
  }
}

// Список <script> в index.html
const idxPath = join(WWW, 'index.html');
let html = readFileSync(idxPath, 'utf8');
const block = files.map(f => `<script src="data/${f}"></script>`).join('\n');
const re = /<!-- data:start -->[\s\S]*?<!-- data:end -->/;
if (!re.test(html)) { console.log('✗ в index.html нет маркеров data:start/data:end'); errors++; }
else if (fix) { html = html.replace(re, `<!-- data:start -->\n${block}\n<!-- data:end -->`); writeFileSync(idxPath, html); }
else for (const f of files) if (!html.includes(`data/${f}"`)) { console.log(`✗ index.html не подключает data/${f} (запустите с --fix)`); errors++; }

let total = 0;
for (const [lang, s] of Object.entries(stats)) {
  const sum = Object.values(s).reduce((a, b) => a + b, 0);
  total += sum;
  console.log(`${lang}: ${sum} слов — ` + Object.entries(s).sort().map(([k, v]) => `${k} ${v}`).join(', '));
}
console.log(`Всего: ${total} карточек в ${files.length} файлах`);
process.exit(errors ? 1 : 0);
