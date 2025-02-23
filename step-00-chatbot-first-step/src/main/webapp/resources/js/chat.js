let socket;
let typingIndicator;
let currentStreamingMessage = null; // Track the current bot message bubble
let markdownBuffer = ""; // Buffer to hold Markdown fragments during streaming

marked.use({
    pedantic: false,
    gfm: true,
    breaks: false,
    highlight: function(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
            try {
                return hljs.highlight(code, { language: lang }).value;
            } catch (err) {}
        }
        return code;
    }
});

function getUserId() {
    let userId = localStorage.getItem("userId");
    if (!userId) {
        userId = `user-${Date.now()}-${Math.random().toString(36).substring(2, 15)}`;
        localStorage.setItem("userId", userId);
    }
    return userId;
}

function connect() {
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const host = window.location.host;
    const userId = getUserId();
    const contextPath = getApplicationContext();
    const path = `${contextPath}/chat?userId=${userId}`;
    const wsUrl = `${protocol}//${host}${path}`;

    try {
        socket = new WebSocket(wsUrl);

        socket.onmessage = function (event) {
            const data = event.data;
            const loadingIndicator = document.getElementById("loading-indicator");

            if (data === "[END]") {
                finalizeStreamingMessage();
                loadingIndicator.style.display = "none";

                // Highlight any code blocks in the message
                if (currentStreamingMessage) {
                    const codeBlocks = currentStreamingMessage.querySelectorAll('pre code');
                    codeBlocks.forEach(block => {
                        hljs.highlightElement(block);
                    });
                }
            } else {
                if (!currentStreamingMessage) {
                    createNewBotBubble();
                }
                appendToStreamingBuffer(data);
            }
        };

        socket.onopen = function () {
            console.log("Connected to WebSocket");
        };

        socket.onclose = function () {
            console.log("Disconnected from WebSocket");
            hideTypingIndicator();
            showErrorBubble("The connection to the chatbot has been closed. Please refresh the page to reconnect.");
        };

        socket.onerror = function (error) {
            console.error("WebSocket error:", error);
            hideTypingIndicator();
            showErrorBubble("Unable to connect to the chatbot. Please try again later.");
        };
    } catch (e) {
        console.error("WebSocket connection failed:", e);
        showErrorBubble("Unable to connect to the chatbot. Please try again later.");
    }
}

function sendMessage() {
    const input = document.getElementById("message-input");
    const message = input.value.trim();
    const sendButton = document.querySelector('.chat-send-button');

    if (message) {
        // Disable input and button while sending
        input.disabled = true;
        sendButton.disabled = true;
        sendButton.style.opacity = '0.7';

        addMessage(message, "user");
        if (socket.readyState === WebSocket.OPEN) {
            const loadingIndicator = document.getElementById("loading-indicator");
            loadingIndicator.style.display = "flex";
            socket.send(message);
        } else {
            console.error("Failed to send message: WebSocket is not open");
            showErrorBubble("Failed to send message. Please try again.");
        }
        input.value = "";

        // Re-enable input and button
        setTimeout(() => {
            input.disabled = false;
            sendButton.disabled = false;
            sendButton.style.opacity = '1';
            input.focus();
        }, 500);
    }
}

function createNewBotBubble() {
    const chatWindow = document.getElementById("chat-window");
    currentStreamingMessage = document.createElement("div");
    currentStreamingMessage.classList.add("message-bubble", "bot");
    chatWindow.appendChild(currentStreamingMessage);

    // Smooth scroll to the new message
    currentStreamingMessage.scrollIntoView({ behavior: 'smooth', block: 'end' });
}

function appendToStreamingBuffer(textFragment) {
    if (currentStreamingMessage) {
        markdownBuffer += textFragment;

        currentStreamingMessage.innerHTML = marked.parse(markdownBuffer);
    }
}

function finalizeStreamingMessage() {
    if (currentStreamingMessage) {
        currentStreamingMessage.innerHTML = `<div class="markdown-content">${marked.parse(markdownBuffer)}</div>`;
        currentStreamingMessage = null; // Reset for the next bot message
        markdownBuffer = ""; // Clear the buffer
    }
}

function addMessage(text, type) {
    const chatWindow = document.getElementById("chat-window");
    const messageElement = document.createElement("div");

    messageElement.classList.add("message-bubble", type);
    if (type === "bot") {
        messageElement.innerHTML = `<div class="markdown-content">${marked.parse(text)}</div>`;
    } else {
        messageElement.textContent = text;
    }
    chatWindow.appendChild(messageElement);

    // Smooth scroll to the new message with a slight delay to ensure proper rendering
    setTimeout(() => {
        messageElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }, 50);

    // Highlight any code blocks in bot messages
    if (type === "bot") {
        const codeBlocks = messageElement.querySelectorAll('pre code');
        codeBlocks.forEach(block => {
            hljs.highlightElement(block);
        });
    }
}

function showTypingIndicator() {
    if (!typingIndicator) {
        const chatWindow = document.getElementById("chat-window");
        typingIndicator = document.createElement("div");
        typingIndicator.classList.add("message-bubble", "bot", "typing");
        typingIndicator.innerHTML = `
            <div class="typing-indicator">
                <span></span><span></span><span></span>
            </div>
        `;
        chatWindow.appendChild(typingIndicator);

        chatWindow.scrollTop = chatWindow.scrollHeight;
    }
}

function hideTypingIndicator() {
    if (typingIndicator) {
        typingIndicator.parentNode.removeChild(typingIndicator);
        typingIndicator = null;
    }
}

function getApplicationContext() {
    const pathname = window.location.pathname;
    const context = pathname.split("/")[1];
    return context ? `/${context}` : "";
}

function showErrorBubble(message) {
    const errorBubble = document.getElementById("error-bubble");
    errorBubble.textContent = message;
    errorBubble.style.display = "flex";

    errorBubble.onclick = function () {
        errorBubble.style.display = "none";
    };
}
