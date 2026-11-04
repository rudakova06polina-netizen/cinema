const API = '/api/seances';
let editingId = null;
let current = [];

const $ = id => document.getElementById(id);

async function load() {
  const q = $('search').value.trim();
  const r = await fetch(API + (q ? '?q=' + encodeURIComponent(q) : ''));
  if (r.redirected || !r.ok) { location.href = '/login.html'; return; }
  current = await r.json();
  render();
  loadHistogram();
}

function render() {
  const tbody = $('rows');
  tbody.innerHTML = '';
  for (const s of current) {
    const tr = document.createElement('tr');
    const cells = [s.id, s.filmTitle, s['киностудия'],
      s.sessionDateTime.replace('T', ' ').slice(0, 16), s.ticketsTotal, s.ticketsSold];
    for (const c of cells) {
      const td = document.createElement('td');
      td.textContent = c;
      tr.appendChild(td);
    }
    const act = document.createElement('td');
    const e = document.createElement('button');
    e.textContent = 'Изменить';
    e.onclick = () => edit(s);
    const d = document.createElement('button');
    d.textContent = 'Удалить';
    d.className = 'danger';
    d.onclick = () => remove(s.id);
    act.append(e, d);
    tr.appendChild(act);
    tbody.appendChild(tr);
  }
  $('count').textContent = current.length;
}

function edit(s) {
  editingId = s.id;
  $('formTitle').textContent = 'Редактирование сеанса #' + s.id;
  $('title').value = s.filmTitle;
  $('studio').value = s['киностудия'];
  $('dt').value = s.sessionDateTime.slice(0, 16);
  $('total').value = s.ticketsTotal;
  $('sold').value = s.ticketsSold;
  window.scrollTo(0, 0);
}

function resetForm() {
  editingId = null;
  $('formTitle').textContent = 'Новый сеанс';
  for (const id of ['title', 'studio', 'dt', 'total', 'sold']) $(id).value = '';
  $('err').textContent = '';
}

async function save() {
  const body = {
    filmTitle: $('title').value.trim(),
    'киностудия': $('studio').value.trim(),
    sessionDateTime: $('dt').value ? $('dt').value + ':00' : null,
    ticketsTotal: parseInt($('total').value || '0', 10),
    ticketsSold: parseInt($('sold').value || '0', 10)
  };
  const r = await fetch(editingId ? API + '/' + editingId : API, {
    method: editingId ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  if (!r.ok) {
    let m = 'Ошибка сохранения';
    try { const j = await r.json(); m = j.message || j.error || m; } catch (e) {}
    $('err').textContent = m;
    return;
  }
  resetForm();
  load();
}

async function remove(id) {
  if (!confirm('Удалить сеанс #' + id + '?')) return;
  await fetch(API + '/' + id, { method: 'DELETE' });
  load();
}

async function loadHistogram() {
  const r = await fetch(API + '/histogram');
  if (!r.ok) return;
  drawHistogram(await r.json());
}

function drawHistogram(data) {
  const canvas = $('hist');
  const ctx = canvas.getContext('2d');
  const W = canvas.width, H = canvas.height;
  const padL = 40, padB = 40, padT = 20, padR = 10;
  ctx.clearRect(0, 0, W, H);
  const max = Math.max(1, ...data.map(d => d.count));
  const plotW = W - padL - padR, plotH = H - padT - padB;
  const barW = plotW / data.length;

  ctx.fillStyle = '#222'; ctx.strokeStyle = '#999'; ctx.font = '12px Arial';
  ctx.beginPath(); ctx.moveTo(padL, padT); ctx.lineTo(padL, H - padB); ctx.lineTo(W - padR, H - padB); ctx.stroke();

  for (let i = 0; i <= max; i++) {
    const y = H - padB - (plotH * i) / max;
    ctx.fillText(i, 12, y + 4);
    ctx.strokeStyle = '#eee';
    ctx.beginPath(); ctx.moveTo(padL, y); ctx.lineTo(W - padR, y); ctx.stroke();
  }
  data.forEach((d, i) => {
    const h = (plotH * d.count) / max;
    const x = padL + i * barW;
    ctx.fillStyle = '#2c3e50';
    ctx.fillRect(x + 2, H - padB - h, barW - 4, h);
    if (i % 3 === 0) {
      ctx.fillStyle = '#222';
      ctx.save();
      ctx.translate(x + barW / 2, H - padB + 12);
      ctx.rotate(Math.PI / 4);
      ctx.fillText(d.date.slice(8, 10) + '.' + d.date.slice(5, 7), 0, 0);
      ctx.restore();
    }
  });
}

load();
