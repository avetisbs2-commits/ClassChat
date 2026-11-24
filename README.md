# ClassChat

ClassChat is a small **real-time chat room**. Many people open the same website, choose a nickname, and every message appears on all screens immediately.

It is built with **Java 21**, **Spring Boot**, **WebSocket + STOMP**, and a plain HTML/CSS/JavaScript page. The Java backend and the website are **one application**, packaged as **one executable JAR** (and one Docker image).

The starter already works: you can chat. A few important parts are **missing on purpose**. These are your tasks in **[TODO.md](TODO.md)**.

```text
 Browser (Ana)                       Java server                        Browser (Ben)
 +--------------+   1. message     +-----------------+   3. message    +--------------+
 |  type, Send  | ---------------> | ChatController  | --------------> | message shows |
 |              |   (WebSocket)    | ChatService     |   (WebSocket)   | up at once    |
 +--------------+                  +-----------------+                 +--------------+
                                    2. check the message,
                                       add the time
```

## 1. Learning goals

After this project you can:

- explain the difference between **HTTP request-response** and a **WebSocket** connection;
- explain what **STOMP**, a **destination**, a **subscription** and a **message broker** are;
- follow one message from a browser, through the Java code, to other browsers;
- write server-side **validation** (the server must never trust the browser);
- write **unit tests** for a service class;
- build and run an **executable JAR** and a **Docker image**, and **deploy** the application.

## 2. What do HTTP, WebSocket and STOMP do here?

| Technology | Job in ClassChat |
|------------|------------------|
| **HTTP** | The browser asks for the page: `GET /` (index.html), `/css/style.css`, `/js/app.js`, and the STOMP library. Each request gets one answer, then it is finished. HTTP is also used for the first step ("handshake") of a WebSocket. |
| **WebSocket** | After the page is loaded, the browser opens **one connection** to `/ws` and keeps it open. Both sides can send data **at any time**. This is why the server can push a message to a browser that did not ask for it. |
| **STOMP** | A small text protocol that runs **on top of** WebSocket. A raw WebSocket only carries "messages" without any meaning. STOMP adds **destinations** (like addresses) and commands: `CONNECT`, `SUBSCRIBE`, `SEND`, `DISCONNECT`. |
| **Message broker** | The part of the server that receives a message for a destination, for example `/topic/messages`, and delivers it to **all clients that subscribed** to it. Here it is Spring's **simple in-memory broker**. No extra program is needed. |

The destinations in ClassChat:

| Destination | Who uses it | Meaning |
|-------------|-------------|---------|
| `/app/chat.send` | browser **sends** here | Goes to the Java method `ChatController.send(...)` |
| `/topic/messages` | everybody **subscribes** here | The chat room: every message sent here reaches all subscribers |
| `/user/queue/errors` | each browser **subscribes** here | Private errors, for example "Message must not be blank." Only the sender receives them |

## 3. How a message travels

Ana types `Hello` and presses **Send**:

1. **Browser (`app.js`)**: `sendMessage()` makes a STOMP `SEND` frame to `/app/chat.send` with the JSON `{"sender":"Ana","content":"Hello"}`. The browser does **not** send a time.
2. **WebSocket** carries the frame to the server.
3. **Spring** sees the prefix `/app` (set in `WebSocketConfig`), removes it, and finds the method with `@MessageMapping("/chat.send")` in `ChatController`. The JSON becomes a `ChatRequest` object.
4. **`ChatController.send`** calls **`ChatService.process`**. The service checks the rules and creates a `ChatMessage` (sender, content, **timestamp**). If a rule is broken, it throws `InvalidMessageException`.
5. **`@SendTo("/topic/messages")`** takes the returned `ChatMessage`, converts it to JSON and gives it to the **broker**.
6. The **broker** delivers the message to every client that subscribed to `/topic/messages`: Ana, Ben, and everybody else who is connected.
7. **Each browser (`app.js`)** receives the frame, and `addMessage()` shows it as **text**.

If the message is invalid (step 4), nothing is broadcast. `ChatController.handleInvalidMessage` sends an `ErrorMessage` only to Ana, on `/user/queue/errors`, and her page shows it in the red box.

**No saved history:** the server keeps no messages. A message is only delivered to the people who are **connected at that moment**. Messages live only in the open web page: if you reload the page or join later, you do not see the old messages. You receive **future** messages only.

## 4. Project structure

```text
class-chat/
├── pom.xml                      Maven build (Java 21, Spring Boot, WebSocket, STOMP library)
├── mvnw, mvnw.cmd, .mvn/        Maven Wrapper: the same Maven on every computer
├── Dockerfile, .dockerignore    multi-stage Docker build
├── README.md, TODO.md, TEACHER_NOTES.md
└── src/
    ├── main/java/com/classchat/
    │   ├── ClassChatApplication.java       starts the application
    │   ├── config/
    │   │   ├── WebSocketConfig.java        turns on WebSocket + STOMP, defines the destinations
    │   │   └── ClockConfig.java            provides the Clock (so tests can use a fixed time)
    │   ├── controller/
    │   │   └── ChatController.java         receives chat messages, sends the answers and errors
    │   ├── service/
    │   │   ├── ChatService.java            the rules: validation + timestamp   <- TODO 2, TODO 3
    │   │   └── InvalidMessageException.java  "this message breaks a rule"
    │   └── dto/                            plain data carried as JSON
    │       ├── ChatRequest.java            browser -> server (sender, content)
    │       ├── ChatMessage.java            server -> browsers (sender, content, timestamp)
    │       └── ErrorMessage.java           server -> one browser (message)
    ├── main/resources/
    │   ├── application.properties          the port (environment variable PORT, default 8080)
    │   └── static/                         the website, served by Spring Boot itself
    │       ├── index.html                  the page                                <- TODO 1
    │       ├── css/style.css               colors and layout (phone + desktop)      <- TODO 1
    │       └── js/app.js                   connects, sends, shows messages          <- TODO 4
    └── test/java/com/classchat/
        ├── ClassChatApplicationTests.java  can the application start?
        ├── ChatBroadcastTest.java          two Java clients chat with each other (read it!)
        └── service/ChatServiceTest.java    unit tests of ChatService               <- TODO 5
```

| Package | Purpose |
|---------|---------|
| `config` | Settings and beans: how WebSocket and STOMP work. |
| `controller` | The "door" of the application: receives messages and returns answers. No business rules here. |
| `service` | The business rules. It knows nothing about WebSocket, so it is easy to test. |
| `dto` | **D**ata **T**ransfer **O**bjects: simple `record`s that are converted to and from JSON. |

The browser's STOMP library (`stomp.umd.min.js`) comes from the Maven dependency `stomp__stompjs`, so it is inside the JAR. ClassChat works without internet.

## 5. What you need

- **JDK 21** (`java -version` must show 21).
- Nothing else for Maven: the **Maven Wrapper** (`mvnw`) downloads Maven by itself the first time.
- **Docker** (Docker Desktop), only for the Docker task.
- Two browser tabs, and for Task 6 a second device (a phone) in the same Wi-Fi network.

## 6. Run locally (Maven Wrapper)

macOS / Linux:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

Open <http://localhost:8080>. Stop the application with `Ctrl+C`.

The port comes from the environment variable `PORT`; if it is missing, the port is `8080`:

```bash
PORT=9090 ./mvnw spring-boot:run          # macOS / Linux
```

```bat
set PORT=9090
mvnw.cmd spring-boot:run
```

On macOS/Linux, if you get `Permission denied`, run `chmod +x mvnw` once.

Run all tests:

```bash
./mvnw test
```

## 7. Build and run the executable JAR

```bash
./mvnw package                     # compiles, runs the tests, builds the JAR
java -jar target/classchat.jar     # runs it
```

The JAR is **`target/classchat.jar`**. It contains the Java code, the website (`static/`), the libraries and the embedded Tomcat server. It runs on any computer with Java 21, without Maven. Open <http://localhost:8080>. Another port:

```bash
PORT=9090 java -jar target/classchat.jar
```

## 8. Build and run the Docker image

Start Docker Desktop first (`docker info` must work). Then, in the project folder:

```bash
docker build -t classchat .
docker run --rm -p 8080:8080 classchat
```

Open <http://localhost:8080>. Stop with `Ctrl+C`.

- `-p 8080:8080` means: port 8080 of your computer -> port 8080 in the container.
- Another port: `docker run --rm -e PORT=9090 -p 9090:9090 classchat`.

The `Dockerfile` has **two stages**: stage 1 (`maven:3.9-eclipse-temurin-21`) builds the JAR; stage 2 (`eclipse-temurin:21-jre`) contains only a Java runtime and the JAR, and runs as a normal user (not `root`). The tests are skipped in the image build; run them with `./mvnw test`.

## 9. Test with two browser tabs and two devices

**Two tabs:**

1. Start the application and open <http://localhost:8080> in a tab. Nickname `Ana`, press **Connect**. The status shows **Connected**.
2. Open a **second tab** with the same address. Nickname `Ben`, press **Connect**.
3. Write a message in one tab. It appears in **both** tabs at once.
4. Press **Disconnect** in one tab. The status shows **Disconnected** and **Send** is disabled. The other tab still works.
5. Reload a tab and connect again: the old messages are gone (no history).

**Two devices:** a laptop and a phone in the **same Wi-Fi**:

1. Find the IP address of your computer, for example `192.168.1.25` (macOS: `ipconfig getifaddr en0`; Windows: `ipconfig`; Linux: `hostname -I`).
2. On the phone open `http://192.168.1.25:8080`. If it does not open, check the computer's firewall.
3. Chat between the laptop and the phone.

## 10. The deployment assignment

Deploy the app, share its public URL, and verify that two users can exchange messages.

## 11. Starter behavior and expected behavior

| Task | Starter (what you see now) | Expected after you finish it |
|------|----------------------------|------------------------------|
| 1. Title and look | Title and colors of the starter | Your title, your colors |
| 2. Validation | A blank message, a blank nickname, 31-character nickname and 501-character message are **all accepted** and broadcast | Each of them is **rejected**. Nothing is broadcast. Only the sender sees a red error text |
| 3. Timestamp | Every message shows `--:--:--` (the server sends no time) | Every message shows the time that the **server** created |
| 4. Counter | Nothing under the message box | `12 / 500`, red when over 500 |
| 5. Tests | 1 example test | Your tests for valid and invalid messages, all green |
| 6.-9. | - | Tested on two tabs and two devices, JAR runs, Docker image runs, deployed |

## 12. Troubleshooting

| Problem | What to check |
|---------|---------------|
| `Port 8080 was already in use` | Another program uses the port. Stop it, or use another port: `PORT=9090 ...` |
| `mvnw: Permission denied` | `chmod +x mvnw` |
| `release version 21 not supported` / `invalid target release` | Your JDK is older than 21. Check `java -version` and `JAVA_HOME` |
| The page opens, but the status is **Disconnected** and the red box says "Cannot reach the chat server" | The server is not running or has stopped. Look at the terminal. Open the browser console (F12) for details |
| The status stays on **Connecting...** | Something between the browser and the server blocks WebSocket (a proxy, an antivirus, a school network). Try another network. Look at the **Network** tab in the browser: `ws` -> the request to `/ws` |
| HTTP **403** for `/ws` in the browser console | Only the **same origin** is allowed. Open the page from the same address the server is on, and use the same host name for the page and the connection. Behind a proxy: the proxy must send the `Host` and `X-Forwarded-*` headers |
| The page works locally but not through a public HTTPS address | The page uses `wss://` automatically on `https://`. The place that serves the app must allow WebSocket (the `Upgrade` header) |
| "Connection lost. Press Connect to join again." | The connection broke (server stopped, network changed, phone went to sleep). Press **Connect** again |
| I see my message, but my friend does not | Both must be **connected** at the same time, and open the **same** server address. Messages are not saved |
| The phone cannot open `http://192.168...` | Not the same Wi-Fi, or the firewall blocks the port, or the IP address is wrong |
| `stomp.umd.min.js` gives 404 | The version in `pom.xml` (`stompjs.version`) and the path in `static/index.html` must match |
| All times show `--:--:--` | That is the starter. Do Task 3 |
| Empty and very long messages appear in the chat | That is the starter. Do Task 2 |
| Tests fail after Task 2 or 3 | Read the message of the failed test. Your tests and `ChatService` must agree |
| `docker: Cannot connect to the Docker daemon` | Start Docker Desktop and wait until it says it is running |
| The Docker build is slow the first time | It downloads the base images and the libraries once. The next builds are fast |

## 13. Versions

Chosen on 2026-11-24 from the official sources (Maven Central metadata, the Spring Initializr at start.spring.io, and the Spring Boot support dates).

| Part | Version | Why |
|------|---------|-----|
| Java | **21** | LTS, as required |
| Spring Boot | **4.2.0** | The current stable release and the default of the Spring Initializr. It brings Spring Framework 7.1, Tomcat 11 and Jackson 3. The 3.5 line is not supported any more (ended June 2026) |
| Maven (wrapper) | **3.9.16** | Used by `mvnw` |
| STOMP JavaScript client | **@stomp/stompjs 7.3.0** | From the WebJar `org.webjars.npm:stomp__stompjs` |
| Docker images | `maven:3.9-eclipse-temurin-21`, `eclipse-temurin:21-jre` | Build stage and run stage |

Note for students who know Spring Boot 3: Boot 4 has new starter names (`spring-boot-starter-webmvc` instead of `spring-boot-starter-web`) and uses Jackson 3. The ideas (`@Controller`, `@Service`, constructor injection) are the same.

## 14. Optional challenges

See the end of [TODO.md](TODO.md): multiple chat rooms, typing indicators, persistent message history.
