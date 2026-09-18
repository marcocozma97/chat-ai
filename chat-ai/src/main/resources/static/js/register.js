document.getElementById('formRegister').addEventListener('submit', async function (e) {
    e.preventDefault(); // evito che la pagina si ricarichi

    const dati = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
    };

    const box = document.getElementById('messaggio');

    try {
        const res = await fetch('/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dati)
        });

        const testo = await res.text();

        if (res.ok) {
            box.className = 'alert ok';
            box.textContent = 'Registrazione completata! Ti porto al login...';
            box.style.display = 'block';
            setTimeout(() => window.location = '/login', 1500);
        } else {
            // provo a leggere il messaggio d'errore dal server
            let msg = 'Errore nella registrazione.';
            try { const j = JSON.parse(testo); if (j.message) msg = j.message; } catch (ignore) {}
            box.className = 'alert error';
            box.textContent = msg;
            box.style.display = 'block';
        }
    } catch (err) {
        box.className = 'alert error';
        box.textContent = 'Errore di connessione al server.';
        box.style.display = 'block';
    }
});