// ========== STATE ==========
let gameId = null;
let boardData = [];
let selectedMarbles = [];
let currentColor = null;
let gameStatus = null;
let gameData = null;
let timerInterval = null;

// ========== LOG TRACKER (client-side, instantane) ==========
const apiLogs = [];
let logIdCounter = 0;

function addLog(method, url, body, status, duration, response, error) {
    const now = new Date();
    const ts = now.toLocaleTimeString('fr-FR', { hour12: false }) + '.' + String(now.getMilliseconds()).padStart(3, '0');

    apiLogs.unshift({
        id: ++logIdCounter,
        timestamp: ts,
        method,
        url,
        body: body ? JSON.stringify(body, null, 2) : null,
        status,
        duration,
        response: response ? JSON.stringify(response, null, 2) : null,
        error: error || null
    });

    // Garder 200 max
    if (apiLogs.length > 200) apiLogs.pop();

    renderLogs();
}

// ========== ELEMENTS ==========
const welcomeScreen = document.getElementById('welcome-screen');
const gameScreen = document.getElementById('game-screen');
const boardEl = document.getElementById('board');
const messageEl = document.getElementById('message');
const logsListEl = document.getElementById('logs-list');
const logsCountEl = document.getElementById('logs-count');
const selectedMarblesEl = document.getElementById('selected-marbles');

// ========== TABS ==========
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        tab.classList.add('active');
        document.getElementById(tab.dataset.tab).classList.add('active');

        if (tab.dataset.tab === 'tab-players') loadPlayers();
        if (tab.dataset.tab === 'tab-play') { loadPlayersForSelect(); loadActiveGames(); }
        if (tab.dataset.tab === 'tab-hall') loadHallOfFame();
    });
});

// ========== INIT ==========
document.getElementById('btn-create-game').addEventListener('click', createGame);
document.getElementById('btn-clear').addEventListener('click', clearSelection);
document.getElementById('btn-back-menu').addEventListener('click', backToMenu);
document.getElementById('btn-abandon').addEventListener('click', abandonGame);
document.getElementById('btn-add-player').addEventListener('click', addPlayer);

document.querySelectorAll('.btn-dir').forEach(btn => {
    btn.addEventListener('click', () => makeMove(btn.dataset.dir));
});

// Load initial data
loadPlayersForSelect();
loadActiveGames();

// ========== API (avec tracking instantane) ==========
async function apiCall(method, url, body) {
    const opts = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) opts.body = JSON.stringify(body);

    const start = performance.now();
    let res, data, error;

    try {
        res = await fetch(url, opts);
        const duration = Math.round(performance.now() - start);

        if (res.status === 204) {
            addLog(method, url, body, 204, duration, null, null);
            return null;
        }

        data = await res.json();

        if (!res.ok) {
            addLog(method, url, body, res.status, duration, data, data.message);
            throw new Error(data.message || 'Erreur API');
        }

        addLog(method, url, body, res.status, duration, data, null);
        return data;

    } catch (e) {
        if (!res) {
            // Erreur reseau
            const duration = Math.round(performance.now() - start);
            addLog(method, url, body, 0, duration, null, e.message);
        }
        throw e;
    }
}

// ========== PLAYERS CRUD ==========
async function loadPlayers() {
    try {
        const players = await apiCall('GET', '/api/players');
        const list = document.getElementById('players-list');
        if (players.length === 0) {
            list.innerHTML = '<p class="empty-msg">Aucun joueur. Creez-en un !</p>';
            return;
        }
        list.innerHTML = players.map(p => `
            <div class="player-card">
                <div class="player-info">
                    <strong>${esc(p.displayName)}</strong>
                    <span class="player-username">@${esc(p.username)}</span>
                </div>
                <div class="player-actions">
                    <button class="btn-small" onclick="editPlayer(${p.id}, '${esc(p.username)}', '${esc(p.displayName)}')">Modifier</button>
                    <button class="btn-small btn-small-danger" onclick="deletePlayer(${p.id})">Supprimer</button>
                </div>
            </div>
        `).join('');
    } catch (e) { console.error(e); }
}

function esc(str) {
    const el = document.createElement('span');
    el.textContent = str;
    return el.innerHTML;
}

async function addPlayer() {
    const username = document.getElementById('new-username').value.trim();
    const displayName = document.getElementById('new-displayname').value.trim();
    if (!username || !displayName) { alert('Remplissez tous les champs.'); return; }
    try {
        await apiCall('POST', '/api/players', { username, displayName });
        document.getElementById('new-username').value = '';
        document.getElementById('new-displayname').value = '';
        loadPlayers();
        loadPlayersForSelect();
    } catch (e) { alert(e.message); }
}

async function editPlayer(id, oldUsername, oldDisplayName) {
    const username = prompt('Nouveau username :', oldUsername);
    if (username === null) return;
    const displayName = prompt('Nouveau nom :', oldDisplayName);
    if (displayName === null) return;
    try {
        await apiCall('PUT', `/api/players/${id}`, { username, displayName });
        loadPlayers();
        loadPlayersForSelect();
    } catch (e) { alert(e.message); }
}

async function deletePlayer(id) {
    if (!confirm('Supprimer ce joueur ?')) return;
    try {
        await apiCall('DELETE', `/api/players/${id}`);
        loadPlayers();
        loadPlayersForSelect();
    } catch (e) { alert(e.message); }
}

async function loadPlayersForSelect() {
    try {
        const players = await apiCall('GET', '/api/players');
        const blackSel = document.getElementById('select-black');
        const whiteSel = document.getElementById('select-white');
        const opts = players.map(p => `<option value="${p.id}">${esc(p.displayName)} (@${esc(p.username)})</option>`).join('');
        blackSel.innerHTML = opts || '<option disabled>Aucun joueur</option>';
        whiteSel.innerHTML = opts || '<option disabled>Aucun joueur</option>';
        if (players.length > 1) whiteSel.selectedIndex = 1;
    } catch (e) { console.error(e); }
}

// ========== HALL OF FAME ==========
async function loadHallOfFame() {
    try {
        const entries = await apiCall('GET', '/api/scores/hall-of-fame');
        const el = document.getElementById('hall-of-fame');
        if (entries.length === 0) {
            el.innerHTML = '<p class="empty-msg">Aucun score. Jouez une partie !</p>';
            return;
        }
        el.innerHTML = `
            <table class="hall-table">
                <thead>
                    <tr><th>#</th><th>Joueur</th><th>Parties</th><th>V</th><th>D</th><th>Ejectees</th><th>Score</th></tr>
                </thead>
                <tbody>
                    ${entries.map((e, i) => `
                        <tr>
                            <td>${i + 1}</td>
                            <td>${esc(e.playerName)}</td>
                            <td>${e.totalGames}</td>
                            <td class="win">${e.wins}</td>
                            <td class="loss">${e.losses}</td>
                            <td>${e.totalMarblesEjected}</td>
                            <td class="score-pts">${e.totalScorePoints}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (e) { console.error(e); }
}

// ========== ACTIVE GAMES ==========
async function loadActiveGames() {
    try {
        const games = await apiCall('GET', '/api/games?status=IN_PROGRESS');
        const el = document.getElementById('active-games-list');
        if (games.length === 0) {
            el.innerHTML = '<p class="empty-msg">Aucune partie en cours.</p>';
            return;
        }
        el.innerHTML = games.map(g => `
            <div class="game-card" onclick="resumeGame(${g.id})">
                <span>${esc(g.playerBlack.displayName)} vs ${esc(g.playerWhite.displayName)}</span>
                <span class="game-card-info">Tour ${g.turnNumber} - ${g.currentColor}</span>
            </div>
        `).join('');
    } catch (e) { console.error(e); }
}

async function resumeGame(id) {
    try {
        const game = await apiCall('GET', `/api/games/${id}`);
        gameId = game.id;
        updateGameInfo(game);
        welcomeScreen.classList.add('hidden');
        gameScreen.classList.remove('hidden');
        await loadBoard();
        startTimer();
    } catch (e) { alert(e.message); }
}

// ========== CREATE GAME ==========
async function createGame() {
    const blackId = document.getElementById('select-black').value;
    const whiteId = document.getElementById('select-white').value;
    const timeLimit = parseInt(document.getElementById('time-limit').value) || 0;

    if (!blackId || !whiteId) { alert('Selectionnez deux joueurs.'); return; }
    if (blackId === whiteId) { alert('Les deux joueurs doivent etre differents.'); return; }

    try {
        const game = await apiCall('POST', '/api/games', {
            playerBlackId: parseInt(blackId),
            playerWhiteId: parseInt(whiteId),
            turnTimeLimitSeconds: timeLimit
        });
        gameId = game.id;
        updateGameInfo(game);
        welcomeScreen.classList.add('hidden');
        gameScreen.classList.remove('hidden');
        await loadBoard();
        startTimer();
        showMessage('Partie creee !', 'info');
    } catch (e) { alert(e.message); }
}

// ========== BOARD ==========
const ROW_SIZES = [5, 6, 7, 8, 9, 8, 7, 6, 5];

async function loadBoard() {
    try {
        const data = await apiCall('GET', `/api/games/${gameId}/board`);
        boardData = data.cells;
        renderBoard();
    } catch (e) { showMessage('Erreur chargement plateau : ' + e.message, 'error'); }
}

function renderBoard() {
    boardEl.innerHTML = '';
    selectedMarbles = [];
    updateSelectionDisplay();

    for (let row = 0; row < 9; row++) {
        const rowEl = document.createElement('div');
        rowEl.className = 'hex-row';
        const size = ROW_SIZES[row];
        for (let col = 0; col < size; col++) {
            const cell = boardData.find(c => c.row === row && c.col === col);
            const state = cell ? cell.state : 'EMPTY';
            const cellEl = document.createElement('div');
            cellEl.className = `hex-cell ${state.toLowerCase()}`;
            cellEl.dataset.row = row;
            cellEl.dataset.col = col;
            cellEl.addEventListener('click', () => onCellClick(row, col, state, cellEl));
            rowEl.appendChild(cellEl);
        }
        boardEl.appendChild(rowEl);
    }
}

function onCellClick(row, col, state, cellEl) {
    if (gameStatus !== 'IN_PROGRESS') return;
    if (state !== currentColor) return;

    const idx = selectedMarbles.findIndex(m => m[0] === row && m[1] === col);
    if (idx >= 0) {
        selectedMarbles.splice(idx, 1);
        cellEl.classList.remove('selected');
    } else {
        if (selectedMarbles.length >= 3) { showMessage('Maximum 3 billes.', 'error'); return; }
        selectedMarbles.push([row, col]);
        cellEl.classList.add('selected');
    }
    updateSelectionDisplay();
}

function updateSelectionDisplay() {
    selectedMarblesEl.textContent = selectedMarbles.length === 0
        ? 'Aucune bille selectionnee'
        : selectedMarbles.map(m => `[${m[0]}, ${m[1]}]`).join('  ');
}

// ========== MOVE ==========
async function makeMove(direction) {
    if (gameStatus !== 'IN_PROGRESS') { showMessage('La partie est terminee !', 'error'); return; }
    if (selectedMarbles.length === 0) { showMessage('Selectionnez au moins 1 bille.', 'error'); return; }

    try {
        const game = await apiCall('POST', `/api/games/${gameId}/move`, {
            marbles: selectedMarbles,
            direction: direction
        });
        updateGameInfo(game);
        await loadBoard();

        if (game.status === 'FINISHED') {
            showWinner(game.winner.displayName);
        } else {
            const name = game.currentColor === 'BLACK'
                ? game.playerBlack.displayName : game.playerWhite.displayName;
            showMessage(`Au tour de ${name} (${game.currentColor})`, 'success');
        }
    } catch (e) { showMessage(e.message, 'error'); }
}

// ========== ABANDON ==========
async function abandonGame() {
    if (gameStatus !== 'IN_PROGRESS') return;

    const activeColor = currentColor;
    const activePlayer = activeColor === 'BLACK' ? gameData.playerBlack : gameData.playerWhite;

    if (!confirm(`${activePlayer.displayName} veut abandonner ?`)) return;

    try {
        const game = await apiCall('POST', `/api/games/${gameId}/abandon`, {
            playerId: activePlayer.id
        });
        updateGameInfo(game);
        showMessage(`${activePlayer.displayName} a abandonne. ${game.winner.displayName} gagne !`, 'info');
    } catch (e) { showMessage(e.message, 'error'); }
}

// ========== GAME INFO ==========
function updateGameInfo(game) {
    gameData = game;
    currentColor = game.currentColor;
    gameStatus = game.status;

    document.getElementById('game-id').textContent = game.id;
    document.getElementById('game-status').textContent = game.status;
    document.getElementById('turn-number').textContent = game.turnNumber;

    const turnEl = document.getElementById('current-turn');
    turnEl.textContent = game.currentColor || '-';
    turnEl.className = 'info-value turn-indicator ' + (game.currentColor || '').toLowerCase();

    document.getElementById('black-player-name').textContent = game.playerBlack.displayName;
    document.getElementById('white-player-name').textContent = game.playerWhite.displayName;
    document.getElementById('black-score').textContent = game.whiteOut;
    document.getElementById('white-score').textContent = game.blackOut;

    document.getElementById('btn-abandon').style.display =
        game.status === 'IN_PROGRESS' ? 'block' : 'none';

    if (game.status === 'IN_PROGRESS') startTimer();
    else stopTimer();
}

// ========== TIMER ==========
function startTimer() {
    stopTimer();
    if (!gameData || gameData.turnTimeLimitSeconds <= 0) {
        document.getElementById('timer-display').textContent = 'Illimite';
        return;
    }
    updateTimerDisplay();
    timerInterval = setInterval(updateTimerDisplay, 1000);
}

function stopTimer() {
    if (timerInterval) { clearInterval(timerInterval); timerInterval = null; }
}

function updateTimerDisplay() {
    if (!gameData || !gameData.lastMoveAt || gameData.turnTimeLimitSeconds <= 0) return;
    const lastMove = new Date(gameData.lastMoveAt);
    const now = new Date();
    const elapsed = Math.floor((now - lastMove) / 1000);
    const remaining = gameData.turnTimeLimitSeconds - elapsed;
    const el = document.getElementById('timer-display');

    if (remaining <= 0) {
        el.textContent = 'TEMPS ECOULE';
        el.style.color = '#e94560';
        stopTimer();
    } else {
        const min = Math.floor(remaining / 60);
        const sec = remaining % 60;
        el.textContent = `${min}:${sec.toString().padStart(2, '0')}`;
        el.style.color = remaining <= 10 ? '#e94560' : '#e0e0e0';
    }
}

// ========== BACK TO MENU ==========
function backToMenu() {
    stopTimer();
    welcomeScreen.classList.remove('hidden');
    gameScreen.classList.add('hidden');
    loadPlayersForSelect();
    loadActiveGames();
}

// ========== CLEAR SELECTION ==========
function clearSelection() {
    selectedMarbles = [];
    document.querySelectorAll('.hex-cell.selected').forEach(el => el.classList.remove('selected'));
    updateSelectionDisplay();
}

// ========== MESSAGES ==========
function showMessage(text, type) {
    messageEl.textContent = text;
    messageEl.className = 'message ' + type;
    setTimeout(() => {
        if (messageEl.textContent === text) { messageEl.textContent = ''; messageEl.className = 'message'; }
    }, 5000);
}

function showWinner(name) {
    const overlay = document.createElement('div');
    overlay.className = 'winner-overlay';
    overlay.innerHTML = `
        <div class="winner-card">
            <h2>Victoire !</h2>
            <p><strong>${esc(name)}</strong> a gagne la partie !</p>
            <button class="btn btn-primary" onclick="this.closest('.winner-overlay').remove()">Fermer</button>
        </div>
    `;
    document.body.appendChild(overlay);
}

// ========== LOGS RENDERING ==========
function renderLogs() {
    logsCountEl.textContent = apiLogs.length;

    logsListEl.innerHTML = '';
    for (const log of apiLogs) {
        let statusClass = 'status-2xx';
        if (log.status >= 400 && log.status < 500) statusClass = 'status-4xx';
        if (log.status >= 500 || log.status === 0) statusClass = 'status-5xx';

        const entry = document.createElement('div');
        entry.className = `log-entry ${statusClass}`;
        entry.innerHTML = `
            <div class="log-top">
                <span class="log-method ${log.method}">${log.method}</span>
                <span class="log-url">${esc(log.url)}</span>
            </div>
            <div class="log-bottom">
                <span class="log-status ${log.status < 400 && log.status > 0 ? 'ok' : 'err'}">${log.status || 'ERR'}</span>
                <span class="log-duration">${log.duration}ms</span>
                <span class="log-time">${log.timestamp}</span>
            </div>
            ${log.body ? `<div class="log-detail log-body-detail hidden"><div class="log-detail-label">Request body</div><pre>${esc(log.body)}</pre></div>` : ''}
            ${log.response ? `<div class="log-detail log-response-detail hidden"><div class="log-detail-label">Response</div><pre>${esc(truncateJson(log.response))}</pre></div>` : ''}
            ${log.error ? `<div class="log-detail log-error-detail"><div class="log-detail-label">Erreur</div><pre>${esc(log.error)}</pre></div>` : ''}
        `;

        // Toggle details on click
        entry.addEventListener('click', () => {
            entry.querySelectorAll('.log-body-detail, .log-response-detail').forEach(d => {
                d.classList.toggle('hidden');
            });
        });

        logsListEl.appendChild(entry);
    }
}

function truncateJson(json) {
    if (json.length > 500) return json.substring(0, 500) + '\n... (tronque)';
    return json;
}
