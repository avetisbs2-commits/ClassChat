# ClassChat - Your Tasks

ClassChat already starts and you can chat. Nine things are missing or unfinished. Do them **in order**.

In the code you will find comments `// TODO 1`, `// TODO 2` ... that match these numbers. In IntelliJ IDEA open the **TODO** tool window (`View > Tool Windows > TODO`) to jump to them.

| # | Task | File(s) | Type |
|---|------|---------|------|
| 1 | Title and appearance | `index.html`, `style.css` | HTML / CSS |
| 2 | Server-side validation | `ChatService.java` | **Java** |
| 3 | Server timestamp | `ChatService.java` | **Java** |
| 4 | Character counter | `app.js` | JavaScript |
| 5 | Tests | `ChatServiceTest.java` | **Java** |
| 6 | Two tabs and two devices | browser | Testing |
| 7 | Build and run the JAR | terminal | Maven |
| 8 | Build and run the Docker image | terminal | Docker |
| 9 | Deploy and demonstrate | - | Deployment |

First start the application (`./mvnw spring-boot:run`, then open <http://localhost:8080>) and chat with yourself in two tabs, so you see what "works" looks like. Restart the application after changes in Java files. After changes in `static/` files, reload the page (`Ctrl+F5` or `Cmd+Shift+R`); with `spring-boot:run` the files are served from `target/classes`, so run `./mvnw compile` or restart if you do not see a change.

---

## Task 1 - Personalize the title and the appearance

**Files:** `src/main/resources/static/index.html`, `src/main/resources/static/css/style.css`

- **Starter:** the title is "ClassChat" (browser tab and heading), blue colors.
- **Do:** write your own title in `<title>` and in `<h1 id="app-title">`. At the top of `style.css`, change the color variables (`--brand-color`, ...) and the font. Change at least one more thing you like.
- **Expected:** the browser tab and the page show your title. The colors are clearly yours. The page still looks good on a phone (try the browser's responsive mode, `F12`).

## Task 2 - Server-side validation

**File:** `src/main/java/com/classchat/service/ChatService.java`, method `validate`

- **Starter:** `validate` does nothing. Everything is accepted: blank messages, blank nicknames, a nickname with 31 characters, a message with 501 characters.
- **Do:** in `validate`, throw `new InvalidMessageException("...")` with a helpful text when:
  1. the nickname is `null` or blank,
  2. the nickname is longer than **30** characters (`MAX_NICKNAME_LENGTH`),
  3. the message is `null` or blank,
  4. the message is longer than **500** characters (`MAX_MESSAGE_LENGTH`).
- **Why on the server?** The web page does not check anything on purpose. Anybody can use another program or the browser console to send any data. The server must protect the chat.
- **Expected:** a bad message is **not** shown to the other users. Only the sender sees the text of your exception in the red box above the messages. A good message works as before.
- **Try it:** connect with an empty nickname and send a message. Send a blank message (spaces only). Paste 501 characters (for example from `"x".repeat(501)` in the browser console).

> You do not need to change `ChatController`: it already catches `InvalidMessageException` and sends its text to the sender.

## Task 3 - Timestamp from the server

**File:** `src/main/java/com/classchat/service/ChatService.java`, method `process`

- **Starter:** `Instant timestamp = null;`. Every message shows `--:--:--`.
- **Do:** create the timestamp on the server with the injected `clock` (`clock.instant()`).
- **Expected:** every message shows the time (for example `9:32:39 PM`). Messages that you send quickly after each other have times that grow. Two devices with different clocks show the same moment, because the time comes from the server.
- **Why not from the browser?** A browser clock can be wrong or changed.

## Task 4 - Character counter

**File:** `src/main/resources/static/js/app.js` (the paragraph `<small id="char-counter">` already exists in `index.html`)

- **Starter:** nothing is shown under the message box.
- **Do:** finish `updateCounter()`, and call it when the user types (the `input` event).
- **Expected:** under the message box you see `0 / 500`. While you type it changes: `12 / 500`. When the text is longer than 500 characters the counter is **red** (the CSS class `counter--over` already exists). After sending a message it goes back to `0 / 500`.
- Note: the counter only **helps** the user. It does not replace the server check of Task 2.

## Task 5 - Tests for valid and invalid messages

**File:** `src/test/java/com/classchat/service/ChatServiceTest.java`

- **Starter:** one example test (`validMessageKeepsSenderAndContent`).
- **Do:** add your own tests. The list is in the TODO comment in the file: valid messages (timestamp from the fixed clock, 30-character nickname, 500-character message) and invalid messages (blank, `null`, too long, 31 and 501 characters). Use `assertThrows` for the invalid ones.
- **Expected:** `./mvnw test` is green. Good tests have clear names, test **one thing** each, and also test the **limits** (30 and 31, 500 and 501).
- Tip: write the test of an invalid message **first**, watch it fail, then finish Task 2 and watch it turn green.
- Read `ChatBroadcastTest.java` too. It starts the real application and lets two Java clients chat, with no browser. It already passes.

## Task 6 - Test with two tabs and two devices

Follow "Test with two browser tabs and two devices" in `README.md`.

- **Expected:** you can chat between two tabs, and between your computer and a phone. Connection status, **Disconnect**, the error box and the disabled **Send** button work.
- **Hand in:** a short list of what you tested (for example 8 lines, "I sent a blank message -> I saw the error", ...) and a screenshot of each device.

## Task 7 - Build and run the executable JAR

```bash
./mvnw package
java -jar target/classchat.jar
```

- **Expected:** the build ends with `BUILD SUCCESS` (the tests run as part of it). `target/classchat.jar` exists. The application runs from the JAR and you can chat. Also try `PORT=9090 java -jar target/classchat.jar`.
- **Hand in:** the last lines of the build output and a screenshot of the running application.

## Task 8 - Build and run the Docker image

```bash
docker build -t classchat .
docker run --rm -p 8080:8080 classchat
```

- **Expected:** the image builds, the container runs, and you can chat at <http://localhost:8080>.
- **Hand in:** the output of `docker images classchat` and a screenshot of the running application.
- Think about it: why does the `Dockerfile` have **two** stages?

## Task 9 - Deploy and demonstrate

Deploy the app, share its public URL, and verify that two users can exchange messages.

- **Hand in:** the public URL and a short demonstration (live or a screen recording) of two users, on two different devices, exchanging messages through that URL.

---

## What to hand in

1. Your source code (the project folder as a ZIP **without** the `target` folder).
2. Your test results (`./mvnw test` output) and the Task 6-8 evidence.
3. The public URL and the demonstration of Task 9.

---

## Optional challenges

Only after the nine tasks. Ask your teacher if you are stuck.

| Challenge | Idea |
|-----------|------|
| **Multiple chat rooms** | The browser chooses a room. Send to `/app/chat.send/{room}` (`@MessageMapping("/chat.send/{room}")` with `@DestinationVariable`) and subscribe to `/topic/rooms/{room}`. Use `SimpMessagingTemplate` to broadcast to the right topic. |
| **Typing indicators** | Show "Ben is typing...". The browser sends a small message on every key press (not too often!). The server broadcasts it to `/topic/typing`. The page hides it after a few seconds. |
| **Persistent message history** | Save messages (a file, or a database with Spring Data JPA). When a user connects, send the last 50 messages. Where do you do this: in `ChatService` or in the controller? What about old messages and the 500-character rule? |
