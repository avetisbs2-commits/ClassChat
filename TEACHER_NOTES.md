# ClassChat - Teacher Notes

For the teacher only. Remove this file from the students' copy if you prefer.

## Goal

Students already know Java, Spring Boot, Maven and basic Docker. ClassChat shows them **real-time communication**: WebSocket + STOMP with Spring's simple broker. The frontend and the broadcasting are finished, so the students work on Java: validation, the server timestamp, tests. Then they package, containerize and deploy the application.

The starter compiles, starts and supports basic chatting. These parts are **left unfinished on purpose** (and are not solved anywhere in this repository):

| Task | Starter | Where |
|------|---------|-------|
| 1 | Starter title and colors | `index.html`, `style.css` |
| 2 | `validate` does nothing; everything is accepted | `ChatService.validate` |
| 3 | The timestamp is `null`; the page shows `--:--:--` | `ChatService.process` |
| 4 | `updateCounter()` is empty | `app.js` |
| 5 | Only one example test | `ChatServiceTest` |
| 6-9 | Activities, no code | - |

## Before the lesson

- Everybody has **JDK 21** (`java -version`). The Maven Wrapper downloads Maven; it needs the internet the first time. Ask students to run `./mvnw -v` and `./mvnw package` once at home, so the libraries are in the local cache.
- Docker Desktop must work on their computers (`docker info`). The first image build downloads about 500 MB.
- **Two devices** for Task 6: a phone in the same Wi-Fi as the laptop. Some school Wi-Fi networks isolate devices from each other ("client isolation"); then use a phone hotspot or the public URL after the deployment.
- Decide in advance how the deployment (Task 9) is done at your school and tell the students. The assignment text is only: *"Deploy the app, share its public URL, and verify that two users can exchange messages."*
- Spring Boot **4.x** is used (the 3.x line is out of support). The code is simple Spring and should look familiar; the starter names changed (`spring-boot-starter-webmvc`) and Jackson is version 3.

## Suggested lesson schedule

**Option A: two lessons of 90 minutes**

| Time | Lesson 1: understand and build | Lesson 2: test, package, deploy |
|------|--------------------------------|----------------------------------|
| 0-15 | Demo: two tabs chat. Discussion: "How does the server push a message?" (see the questions below) | Review the homework: the validation and the tests |
| 15-30 | Read the code in this order: `index.html` -> `app.js` -> `ChatController` -> `ChatService` -> `WebSocketConfig`. Trace one message (README, section 3) | Task 6 (two tabs, two devices) |
| 30-45 | Task 1 (title, look) | Task 7 (JAR) |
| 45-70 | Task 2 (validation) and Task 3 (timestamp) | Task 8 (Docker) |
| 70-85 | Task 4 (counter), Task 5 starts (tests) | Task 9 (deploy, two users, the public URL) |
| 85-90 | Wrap-up | Demonstrations, wrap-up |

**Option B: three lessons of 45 minutes** - (1) intro + code reading + Task 1; (2) Tasks 2-5; (3) Tasks 6-9 (Task 9 can be homework).

Tasks 6-9 are quick for good students. Give them the optional challenges.

## Discussion questions: WebSocket vs HTTP request-response

1. In HTTP, who always starts the communication? What does that mean for a chat where Ben must see Ana's message "at once"?
2. How could you build a chat with **only HTTP**? (Polling: ask every second; long polling.) What are the costs: requests per minute, delay, server load, battery of a phone?
3. What happens on the network when a WebSocket is opened? (An HTTP request with `Upgrade: websocket`; the answer `101 Switching Protocols`; then the same TCP connection is used in both directions.) Look at it in the browser's **Network** tab.
4. A raw WebSocket carries "messages" with no meaning. What does **STOMP** add? (Destinations, subscribe, frames.) What would you have to invent yourself without it?
5. The server holds an open connection for every user. What does this change for **scaling** (memory, many servers, load balancers)? Why would two server instances not share the simple in-memory broker, so a message sent to server 1 would not reach a user on server 2? (A real broker like RabbitMQ or a shared system is needed.)
6. In ClassChat the **sender also receives** their own message from the broker. Why is that a good design? (One source of truth, and the same server timestamp for everybody.) What is the alternative?
7. Why does the server decide the **timestamp** and do the **validation**, and not the browser? Which parts of the browser code can a user change?
8. Why is the message displayed with `textContent` and not `innerHTML`? Try to send `<b>hi</b>` and a `<script>` tag. What is this attack called? (XSS.)
9. A user closes the laptop lid or loses the Wi-Fi. How does the server know the connection is gone? (Heartbeats, TCP timeouts.) What does the user see? What would an automatic reconnect need?
10. The chat has no history. A user joins late. What do they see? What would you have to change (and which new problems appear: storage, privacy, old messages vs the new rules)?
11. Which tests can you write **without** WebSocket (the `ChatService`)? Which test needs the whole application (`ChatBroadcastTest`)? Why is it good to have both?
12. The page uses `ws://` on `http://` and `wss://` on `https://`. Why? What would a browser do with `ws://` on an HTTPS page? (It blocks it: mixed content.)

## Assessment criteria (100 points)

| Task | Points | Full marks when |
|------|--------|-----------------|
| 1. Title and appearance | 5 | Own title (tab + heading), clearly changed colors, still readable on a phone |
| 2. Server-side validation | 20 | All four rules work on the **server**. Null-safe (no `NullPointerException` for a missing nickname or message). Helpful text in each exception. Bad messages are not broadcast. Limits are exact: 30 and 500 are accepted, 31 and 501 are rejected. Uses the existing constants |
| 3. Server timestamp | 10 | Created in `ChatService` with the injected `Clock`. Not `Instant.now()` (then it cannot be tested with a fixed clock), and not sent by the browser. Shown in the page |
| 4. Character counter | 10 | Shows `n / 500`, updates while typing, red over the limit, resets after sending |
| 5. Tests | 20 | At least the cases of the TODO list in `ChatServiceTest`. Clear names, one idea in each test, limits are tested, `assertThrows` for invalid input. A fixed clock is used for the timestamp |
| 6. Two tabs and two devices | 10 | The list of what was tested and screenshots from two devices are credible |
| 7. JAR | 5 | `BUILD SUCCESS`, JAR runs, chat works from the JAR |
| 8. Docker | 5 | Image builds and runs; the student can explain the two stages |
| 9. Deployment | 15 | The public URL works; the demonstration shows two users on two different devices exchanging messages through that URL |
| **Total** | **100** | |

Grade bands you can use: 90-100 excellent, 75-89 good, 60-74 satisfactory, below 60 repeat.
Optional challenges: up to +10 bonus points.

### Quick checks for the teacher

- Task 2: open `ChatService`. Look for `null` handling and `isBlank()`. Test the limits in the browser: paste exactly 500 and 501 characters, nickname 30 and 31 characters.
- Task 3: `grep -n "Instant.now" -r src/main` should find nothing; `clock.instant()` should be there.
- Does `./mvnw test` run green? Did the students keep the given `ChatBroadcastTest` (it must still pass)?
- The text of the errors must come from the server (change the text in the page's JavaScript: nothing changes).
- The deployed URL: open it on your own phone and write to a student's laptop.

### Common mistakes

- Checking only `isEmpty()` (a message of spaces passes) or forgetting `null`.
- Using `>=` instead of `>` (off by one at 30 and 500).
- Putting the validation in the JavaScript only.
- Adding `@Valid` / Bean Validation annotations to the DTOs and thinking STOMP shows the error: needs extra work, and the task asks for the explicit checks in the service. Accept it if it works and the user sees the error.
- Using `LocalDateTime.now()`: no time zone. Show the difference between a point in time (`Instant`) and a local time.
- The Docker container works but the page does not: forgotten `-p 8080:8080`.
- Different port in the container: `PORT` and `-p` must agree.

## Notes about the implementation

- One shared room: `/topic/messages`. Private errors: `/user/queue/errors`. There is no login. Each WebSocket connection gets a random internal user name (`WebSocketConfig.UniqueUserHandshakeHandler`) so that errors reach only the sender.
- The connection is **same-origin** by default (no `setAllowedOrigins`). `server.forward-headers-strategy=framework` makes the origin check work behind a proxy that terminates HTTPS.
- Messages are never stored on the server; a late user only receives future messages.
- The STOMP JavaScript library is served from the JAR (WebJar), so the application needs no internet at run time.
- `ChatBroadcastTest` is complete and is meant to stay green in the starter and after every task. It checks only valid messages.
- The full solutions are not in this repository. In words: Task 2 is four `if` statements with `isBlank()`, `length()` and `throw new InvalidMessageException(...)`; Task 3 is `clock.instant()`; Task 4 is `textContent` plus `classList.toggle("counter--over", ...)` and an `input` listener.
