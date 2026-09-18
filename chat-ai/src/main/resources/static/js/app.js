let chatAttivaId = null;
let ultimoMessaggioAltro = null;
let stompClient = null;
let sottoscrizione = null;

// ===== WebSocket =====
function connettiWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function () {
        if (chatAttivaId) sottoscriviChat(chatAttivaId);
    });
}

function sottoscriviChat(id) {
    if (!stompClient || !stompClient.connected) return;
    if (sottoscrizione) sottoscrizione.unsubscribe();
    sottoscrizione = stompClient.subscribe('/topic/chat/' + id, function (msg) {
        mostraMessaggio(JSON.parse(msg.body));
        scrollInFondo();
    });
}

// ===== Elenco chat =====
async function caricaChat() {
    const res = await fetch('/chat');
    if (!controllaAuth(res)) return;

    const chats = await res.json();
    const lista = document.getElementById('listaChat');
    lista.innerHTML = '';

    chats.forEach(chat => {
        const li = document.createElement('li');
        li.className = 'chat-item';
        if (chat.id === chatAttivaId) li.classList.add('attiva');

        const nome = document.createElement('span');
        nome.textContent = chat.altroUtente;
        nome.onclick = () => apriChat(chat.id, chat.altroUtente);

        const cestino = document.createElement('button');
        cestino.className = 'btn-elimina';
        cestino.textContent = '🗑';
        cestino.onclick = (e) => { e.stopPropagation(); eliminaChat(chat.id); };

        li.appendChild(nome);
        li.appendChild(cestino);
        lista.appendChild(li);
    });
}

async function creaChat() {
    const username = prompt("Username dell'utente con cui vuoi chattare:");
    if (!username) return;

    const res = await fetch('/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usernameDestinatario: username })
    });
    if (!controllaAuth(res)) return;

    if (res.ok) {
        const chat = await res.json();
        await caricaChat();
        apriChat(chat.id, chat.altroUtente);
    } else {
        alert('Utente non trovato (o errore nella creazione della chat).');
    }
}

// ===== Apertura chat =====
async function apriChat(id, altroUtente) {
    chatAttivaId = id;
    ultimoMessaggioAltro = null;
    document.getElementById('titoloChat').textContent = altroUtente;
    document.getElementById('composer').style.display = 'flex';

    await caricaMessaggi(id);
    sottoscriviChat(id);
    caricaChat();
}

async function caricaMessaggi(id) {
    const res = await fetch('/message/' + id);
    if (!controllaAuth(res)) return;

    const pagina = await res.json();
    const messaggi = pagina.content.slice().reverse();

    document.getElementById('messaggi').innerHTML = '';
    messaggi.forEach(mostraMessaggio);
    scrollInFondo();
}

function mostraMessaggio(m) {
    const mio = (m.mittenteUsername === window.UTENTE_CORRENTE);

    const div = document.createElement('div');
    div.className = 'messaggio ' + (mio ? 'mio' : 'altro');
    div.textContent = m.testo;
    document.getElementById('messaggi').appendChild(div);
    if (!mio) ultimoMessaggioAltro = m.testo;
}

// ===== Invio =====
async function inviaMessaggio() {
    const area = document.getElementById('testoMessaggio');
    const testo = area.value.trim();
    if (!testo || !chatAttivaId) return;

    const btn = document.getElementById('btnInvia');
    btn.disabled = true;

    const res = await fetch('/message', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idChat: chatAttivaId, testo: testo })
    });

    btn.disabled = false;
    if (!controllaAuth(res)) return;

    if (res.ok) {
        area.value = '';
    } else {
        alert("Errore nell'invio del messaggio.");
    }
}

// ===== Suggerimento IA =====
async function suggerisci() {
    if (!chatAttivaId) return;
    if (!ultimoMessaggioAltro) {
        alert("Non c'è ancora un messaggio dell'altro utente a cui rispondere.");
        return;
    }

    const btn = document.getElementById('btnSuggerisci');
    btn.disabled = true;
    btn.textContent = '💭 ...';

    const res = await fetch('/message/suggerisci', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idChat: chatAttivaId, messaggioRicevuto: ultimoMessaggioAltro })
    });

    btn.disabled = false;
    btn.textContent = '💡 Suggerisci';
    if (!controllaAuth(res)) return;

    if (res.ok) {
        const dati = await res.json();
        const area = document.getElementById('testoMessaggio');
        area.value = dati.rispostaProposta;
        area.focus();
    } else {
        alert('Errore nella generazione del suggerimento.');
    }
}

// ===== Eliminazione =====
async function eliminaChat(id) {
    if (!confirm('Vuoi eliminare questa chat?')) return;

    const res = await fetch('/chat/' + id, { method: 'PATCH' });
    if (!controllaAuth(res)) return;

    if (id === chatAttivaId) {
        chatAttivaId = null;
        if (sottoscrizione) { sottoscrizione.unsubscribe(); sottoscrizione = null; }
        document.getElementById('titoloChat').textContent = 'Seleziona o crea una chat';
        document.getElementById('messaggi').innerHTML = '';
        document.getElementById('composer').style.display = 'none';
    }
    await caricaChat();
}

// ===== Utility =====
function controllaAuth(res) {
    if (res.status === 401 || res.redirected) {
        window.location = '/login';
        return false;
    }
    return true;
}

function scrollInFondo() {
    const box = document.getElementById('messaggi');
    box.scrollTop = box.scrollHeight;
}

// ===== Avvio =====
document.getElementById('btnNuovaChat').onclick = creaChat;
document.getElementById('btnInvia').onclick = inviaMessaggio;
document.getElementById('btnSuggerisci').onclick = suggerisci;
document.getElementById('testoMessaggio').addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey) {   // Invio = invia, Shift+Invio = a capo
        e.preventDefault();
        inviaMessaggio();
    }
});

connettiWebSocket();
caricaChat();