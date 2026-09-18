# Chat con suggerimento AI

Applicazione web di **chat tra utenti registrati**, con messaggi consegnati
in **tempo reale via WebSocket**. L'intelligenza artificiale non partecipa alla
conversazione: è disponibile solo come **aiuto**, tramite un pulsante
**"Suggerisci"** che propone una possibile risposta al messaggio ricevuto (la
proposta è modificabile prima dell'invio e **non viene salvata** nel database).
L'utente può inoltre ricevere via **email** un riepilogo delle proprie statistiche.

---

## Stack tecnologico

- **Linguaggio:** Java 21
- **Framework:** Spring Boot
    - Spring Web
    - Spring Data JPA
    - Spring Security
    - Spring WebSocket (STOMP + SockJS)
    - Thymeleaf
    - Java Mail Sender
    - Validation
    - Lombok
- **Database:** PostgreSQL
- **LLM (solo per i suggerimenti):** OpenRouter (endpoint compatibile OpenAI)
- **Email:** Gmail SMTP + template Thymeleaf
- **Frontend:** pagine Thymeleaf + JavaScript (`fetch` + SockJS/STOMP)

---

## Funzionalità

- Registrazione e login reale degli utenti (password cifrate con BCrypt)
- Protezione delle pagine e degli endpoint tramite Spring Security (login con form)
- Chat **1-a-1** tra due utenti loggati:
    - apertura di una conversazione indicando l'username dell'altro utente
      (se esiste già una chat tra i due, viene riutilizzata)
    - elenco delle proprie conversazioni, ordinate dalla più recente
    - eliminazione della chat (soft delete)
- **Messaggi in tempo reale** via WebSocket: ogni messaggio inviato compare
  istantaneamente a entrambi i partecipanti, senza ricaricare la pagina
- Lettura dello storico messaggi di una chat **paginata a 50**, dal più recente
- Pulsante **"Suggerisci"**: chiede all'IA una possibile risposta al messaggio
  ricevuto e la scrive nella casella di testo, modificabile prima dell'invio
  (la risposta dell'IA non viene salvata)
- Invio via **email** delle statistiche personali (messaggi inviati, messaggi
  ricevuti, chat aperte) con template HTML

---

## Struttura del progetto

```
com.epicode.chatai
├── ChatAiApplication.java        Avvio dell'applicazione
├── entity/                       Entità JPA (tabelle del database)
│   ├── Utente.java
│   ├── Chat.java                 collega due utenti (utenteUno, utenteDue)
│   └── Messaggio.java            mittente = un Utente
├── repository/                   Accesso al database
│   ├── UtenteRepository.java
│   ├── ChatRepository.java
│   └── MessaggioRepository.java
├── dto/                          Oggetti di input/output
│   ├── NuovaChatRequest.java     usernameDestinatario
│   ├── ChatResponse.java         altroUtente, tokens, date
│   ├── NuovoMessaggioRequest.java
│   ├── MessaggioResponse.java    mittenteUsername, testo, data
│   ├── SuggerimentoRequest.java  idChat + messaggioRicevuto
│   ├── SuggerimentoResponse.java
│   ├── RegistrazioneRequest.java
│   ├── StatisticheResponse.java
│   ├── OpenRouterMessage.java    DTO per la comunicazione con OpenRouter
│   ├── OpenRouterRequest.java
│   ├── OpenRouterResponse.java
│   ├── Choice.java
│   ├── Usage.java
│   └── RispostaLLM.java
├── service/                      Logica applicativa
│   ├── ChatService.java
│   ├── MessaggioService.java     salva il messaggio e lo spinge via WebSocket
│   ├── OpenRouterService.java    usato solo dai suggerimenti
│   ├── UtenteService.java
│   ├── UtenteDetailsService.java
│   ├── EmailService.java
│   └── StatisticheService.java
├── controller/                   Endpoint HTTP e pagine
│   ├── ChatController.java
│   ├── MessaggioController.java
│   ├── AuthController.java
│   ├── StatisticheController.java
│   └── PageController.java
└── config/
    ├── SecurityConfig.java
    └── WebSocketConfig.java       configurazione STOMP/SockJS

src/main/resources
├── application.properties        Configurazione (DB, OpenRouter, email)
├── templates/                    Pagine Thymeleaf
│   ├── login.html
│   ├── register.html
│   ├── chat.html
│   └── email-statistiche.html    Template dell'email statistiche
└── static/
    ├── css/style.css
    └── js/
        ├── app.js                Logica della chat + WebSocket
        └── register.js           Logica della registrazione
```

---

## Modello dati

### Utente
| Campo    | Tipo   | Note                          |
|----------|--------|-------------------------------|
| id       | UUID   | chiave primaria               |
| username | String | univoco                       |
| password | String | cifrata con BCrypt            |
| email    | String | univoca                       |
| ruolo    | String | default `ROLE_USER`           |

### Chat (conversazione tra due utenti)
| Campo           | Tipo    | Note                                              |
|-----------------|---------|---------------------------------------------------|
| id              | UUID    | chiave primaria                                   |
| utenteUno       | Utente  | primo partecipante (ManyToOne)                    |
| utenteDue       | Utente  | secondo partecipante (ManyToOne)                  |
| tokens          | Integer | token consumati dai suggerimenti IA in questa chat|
| createdAt       | Instant | impostato alla creazione                          |
| lastMessageSent | Instant | aggiornato a ogni messaggio                       |
| isDeleted       | Boolean | default false (soft delete)                       |

### Messaggio
| Campo     | Tipo    | Note                                          |
|-----------|---------|-----------------------------------------------|
| id        | UUID    | chiave primaria                               |
| mittente  | Utente  | utente che ha scritto il messaggio (ManyToOne)|
| testo     | Text    |                                               |
| createdAt | Instant |                                               |
| chat      | Chat    | relazione ManyToOne (colonna `chat_id`)       |

> Nota: le risposte proposte dall'IA **non** sono entità del database. L'IA è
> usata solo dal pulsante "Suggerisci" e la sua proposta non viene salvata.

---

## Requisiti

- **JDK 21** (o superiore)
- **PostgreSQL** installato e in esecuzione (con pgAdmin per comodità)
- Un account **OpenRouter** (openrouter.ai) per la chiave API usata dai suggerimenti
- Un account **Gmail** con **verifica in due passaggi** attiva, per generare una
  *App Password* (necessaria all'invio delle email)

---

## Configurazione e avvio

### 1. Crea il database

In pgAdmin (o da `psql`) crea un database vuoto:

```sql
CREATE DATABASE chat_ai_db;
```

### 2. Configura `application.properties`

Nel file `src/main/resources/application.properties` inserisci i tuoi valori
(sostituisci i segnaposto):

```properties
spring.application.name=chat-ai

# --- PostgreSQL ---
spring.datasource.url=jdbc:postgresql://localhost:5432/chat_ai_db
spring.datasource.username=postgres
spring.datasource.password=LA_TUA_PASSWORD_POSTGRES

# --- JPA / Hibernate ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# --- Server ---
server.port=8080

# --- OpenRouter (LLM, solo per i suggerimenti) ---
openrouter.api.url=https://openrouter.ai/api/v1/chat/completions
openrouter.api.key=LA_TUA_CHIAVE_OPENROUTER
openrouter.api.model=openrouter/free

# --- Email (Gmail SMTP) ---
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=LA_TUA_EMAIL@gmail.com
spring.mail.password=LA_TUA_APP_PASSWORD_SENZA_SPAZI
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Come ottenere la chiave OpenRouter:** registrati su openrouter.ai → sezione
> *Keys* → *Create Key* → copia la chiave (`sk-or-v1-...`). Il modello
> `openrouter/free` seleziona automaticamente un modello gratuito disponibile.
>
> **Come ottenere la App Password di Gmail:** attiva la verifica in due passaggi
> sull'account Google, poi vai su *myaccount.google.com/apppasswords*, genera una
> password di 16 caratteri e inseriscila **senza spazi**.

> **Sicurezza:** `application.properties` contiene dati sensibili (password DB,
> chiave API, password email). È inserito in `.gitignore` e **non va pubblicato**.

### 3. Avvia l'applicazione

Esegui `ChatAiApplication` da IntelliJ (freccia verde sul metodo `main`), oppure
da terminale:

```bash
./mvnw spring-boot:run
```

Al primo avvio Hibernate crea automaticamente le tabelle. L'app è disponibile su
**http://localhost:8080**.

### 4. Usa l'applicazione

1. Apri `http://localhost:8080` → vieni portato alla pagina di login.
2. Clicca **Registrati** e crea un account (servono almeno **due utenti** per
   poter chattare in due).
3. Accedi con le tue credenziali.
4. Clicca **+ Nuova chat**, indica l'username dell'altro utente e inizia a
   scrivere. I messaggi arrivano in tempo reale a entrambi.
5. Usa **💡 Suggerisci** per farti proporre una risposta dall'IA.

> Per provare la chat in tempo reale con due utenti diversi sullo stesso computer,
> usa una finestra normale e una in **incognito** (due sessioni di login separate).

---

## Endpoint principali

### Pagine (browser)
| Metodo | URL         | Descrizione                          |
|--------|-------------|--------------------------------------|
| GET    | `/`         | Pagina della chat (home)             |
| GET    | `/login`    | Pagina di login                      |
| GET    | `/register` | Pagina di registrazione              |
| POST   | `/login`    | Login (gestito da Spring Security)   |
| POST   | `/logout`   | Logout                               |

### WebSocket
| Endpoint                 | Descrizione                                             |
|--------------------------|---------------------------------------------------------|
| `/ws`                    | Punto di connessione WebSocket (SockJS/STOMP)           |
| `/topic/chat/{idChat}`   | Canale su cui il server invia i messaggi di una chat    |

### Autenticazione
| Metodo | URL              | Descrizione                | Auth |
|--------|------------------|----------------------------|------|
| POST   | `/auth/register` | Registra un nuovo utente   | No   |

Body:
```json
{ "username": "marco", "email": "marco@example.com", "password": "password123" }
```

### Chat
| Metodo | URL          | Descrizione                                                   | Auth |
|--------|--------------|--------------------------------------------------------------|------|
| POST   | `/chat`      | Apre (o riusa) una chat con un altro utente                  | Sì   |
| GET    | `/chat`      | Elenco delle proprie chat, dalla più recente                | Sì   |
| PATCH  | `/chat/{id}` | Elimina (soft delete) una chat                               | Sì   |

Body creazione chat:
```json
{ "usernameDestinatario": "giulia" }
```

### Messaggi
| Metodo | URL                          | Descrizione                                              | Auth |
|--------|------------------------------|----------------------------------------------------------|------|
| POST   | `/message`                   | Invia un messaggio (salvato e trasmesso via WebSocket)   | Sì   |
| GET    | `/message/{chatId}?pagina=0` | Storico messaggi della chat, paginati a 50, dal più recente | Sì |
| POST   | `/message/suggerisci`        | Chiede all'IA una proposta di risposta (non salvata)     | Sì   |

Body invio messaggio:
```json
{ "idChat": "UUID_DELLA_CHAT", "testo": "Ciao!" }
```

Body suggerimento:
```json
{ "idChat": "UUID_DELLA_CHAT", "messaggioRicevuto": "Ci vediamo domani?" }
```

### Statistiche
| Metodo | URL                   | Descrizione                                             | Auth |
|--------|-----------------------|---------------------------------------------------------|------|
| POST   | `/statistiche/invia`  | Calcola le statistiche personali e le invia via email   | Sì   |

Risposta:
```json
{ "messaggiInviati": 12, "messaggiRicevuti": 9, "chatAperte": 3 }
```

---

## Flusso di invio di un messaggio (`POST /message`)

1. Si cerca la chat indicata (errore 404 se non esiste).
2. Si verifica che il mittente sia uno dei due partecipanti (errore 403 altrimenti).
3. Si salva il messaggio (con il mittente e il riferimento alla chat).
4. Si aggiorna `lastMessageSent` della chat.
5. Il messaggio viene **spinto in tempo reale** sul canale `/topic/chat/{idChat}`:
   entrambi i partecipanti iscritti lo ricevono all'istante.

## Flusso del suggerimento IA (`POST /message/suggerisci`)

1. Si costruisce un prompt a partire dal messaggio ricevuto.
2. Si chiama l'API OpenRouter e si ottiene una proposta di risposta.
3. I token consumati vengono sommati al campo `tokens` della chat (`AddTokenToChat`).
4. La proposta viene restituita al frontend, che la scrive nella casella di testo
   (modificabile prima dell'invio). **Non viene salvata** nel database.

---

## Scelte progettuali

- **Chat 1-a-1:** una `Chat` collega esattamente due utenti; se una conversazione
  tra i due esiste già, viene riutilizzata invece di crearne una nuova.
- **Tempo reale con WebSocket:** i messaggi si inviano via REST e vengono
  ritrasmessi a entrambi i partecipanti tramite STOMP su `/topic/chat/{idChat}`.
  Il mittente riceve il proprio messaggio dallo stesso canale, così la
  visualizzazione è coerente e non compaiono duplicati.
- **IA come suggeritore:** l'intelligenza artificiale non è un partecipante e le
  sue proposte non vengono salvate; è usata solo su richiesta esplicita.
- **Riuso del campo `tokens`:** poiché nella chat tra utenti non ci sono token di
  conversazione, il campo `tokens` della chat viene usato per tenere traccia dei
  token consumati dai suggerimenti IA in quella conversazione.
- **Soft delete delle chat:** l'eliminazione imposta `isDeleted = true`; la chat
  scompare dagli elenchi ma resta nel database.
- **Statistiche personali:** messaggi inviati (scritti dall'utente), messaggi
  ricevuti (scritti dall'altro nelle proprie chat) e chat aperte, calcolati per
  l'utente loggato e recapitati alla sua email.
- **Sicurezza:** password cifrate con BCrypt; l'accesso alle pagine e agli
  endpoint (esclusi login, registrazione, risorse statiche e connessione
  WebSocket) richiede l'autenticazione tramite login con form.

---
