let socket;
let typingIndicator;
let currentStreamingMessage = null;
let markdownBuffer = "";

marked.use({
    pedantic: false,
    gfm: true,
    breaks: false,
    highlight: function(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
            try {
                return hljs.highlight(code, { language: lang }).value;
            } catch (err) {
                console.error('Failed to highlight:', err);
            }
        }
        try {
            // Attempt to auto-detect language if not specified
            return hljs.highlightAuto(code).value;
        } catch (err) {
            console.error('Failed to auto-highlight:', err);
        }
        return code;
    }
});

// Initialize highlight.js
hljs.configure({
    ignoreUnescapedHTML: true,
    languages: ['java', 'xml', 'html', 'javascript', 'css']
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
            loadingIndicator.style.display = "none";

            if (data === "[END]") {
                finalizeStreamingMessage();
            } else {
                if (!currentStreamingMessage) {
                    createNewBotBubble();
                }
                appendToStreamingBuffer(data);
            }
        };

        socket.onopen = function () {
            console.log("Connected to WebSocket");
            hideErrorBubble();
        };

        socket.onclose = function () {
            console.log("Disconnected from WebSocket");
            showErrorBubble("The connection to the chatbot has been closed. Please refresh the page to reconnect.");
        };

        socket.onerror = function (error) {
            console.error("WebSocket error:", error);
            showErrorBubble("Unable to connect to the chatbot. Please try again later.");
        };
    } catch (e) {
        console.error("WebSocket connection failed:", e);
        showErrorBubble("Unable to connect to the chatbot. Please try again later.");
    }
}

function createNewBotBubble() {
    const chatWindow = document.getElementById("chat-window");
    currentStreamingMessage = document.createElement("div");
    currentStreamingMessage.classList.add("message-bubble", "bot");
    chatWindow.appendChild(currentStreamingMessage);
    currentStreamingMessage.scrollIntoView({ behavior: 'smooth', block: 'end' });
}

function appendToStreamingBuffer(textFragment) {
    if (currentStreamingMessage) {
        markdownBuffer += textFragment;
        currentStreamingMessage.innerHTML = `<div class="markdown-content">${marked.parse(markdownBuffer)}</div>`;

        // Highlight any code blocks
        const codeBlocks = currentStreamingMessage.querySelectorAll('pre code');
        codeBlocks.forEach(block => {
            if (!block.className) {
                const content = block.textContent.toLowerCase();
                if (content.includes('class ') || content.includes('public ') || 
                    content.includes('private ') || content.includes('protected ') ||
                    content.includes('import ') || content.includes('@')) {
                    block.className = 'language-java';
                }
            }
            hljs.highlightElement(block);
        });
    }
}

function finalizeStreamingMessage() {
    if (currentStreamingMessage) {
        currentStreamingMessage.innerHTML = `<div class="markdown-content">${marked.parse(markdownBuffer)}</div>`;

        // Final highlighting of code blocks
        const codeBlocks = currentStreamingMessage.querySelectorAll('pre code');
        codeBlocks.forEach(block => {

            // Auto-detect language if not specified
            if (!block.className) {
                const content = block.textContent.toLowerCase();
                let detectedLang = '';

                if (content.includes('class ') || content.includes('public ') ||
                    content.includes('private ') || content.includes('protected ') ||
                    content.includes('import ') || content.includes('@')) {
                    detectedLang = 'java';
                } else if (content.includes('<!doctype html') || content.includes('<html')) {
                    detectedLang = 'html';
                } else if (content.includes('<?xml') || content.includes('xmlns:')) {
                    detectedLang = 'xml';
                } else if (content.includes('function ') || content.includes('const ') ||
                    content.includes('let ') || content.includes('=>')) {
                    detectedLang = 'javascript';
                } else if (content.includes('{') && content.includes('}') &&
                    (content.includes(':') || content.includes('@media'))) {
                    detectedLang = 'css';
                }

                if (detectedLang) {
                    block.className = `language-${detectedLang}`;
                    block.parentElement.className = `language-${detectedLang}`;
                }
            } else {
                // Copy the language class to the pre element
                const lang = block.className.replace('language-', '');
                block.parentElement.className = `language-${lang}`;
            }

            // Add copy button to code blocks
            const pre = block.parentElement;
            const copyButton = document.createElement('button');
            copyButton.className = 'copy-code-button';
            copyButton.innerHTML = '<i class="fas fa-copy"></i>';
            copyButton.onclick = function() {
                navigator.clipboard.writeText(block.textContent).then(() => {
                    copyButton.innerHTML = '<i class="fas fa-check"></i>';
                    setTimeout(() => {
                        copyButton.innerHTML = '<i class="fas fa-copy"></i>';
                    }, 2000);
                });
            };
            pre.appendChild(copyButton);

            hljs.highlightElement(block);
        });

        currentStreamingMessage = null;
        markdownBuffer = "";
    }
}

function addMessage(text, type) {
    const chatWindow = document.getElementById("chat-window");
    const messageElement = document.createElement("div");

    messageElement.classList.add("message-bubble", type);
    if (type === "bot") {
        messageElement.innerHTML = `<div class="markdown-content">${marked.parse(text)}</div>`;

        // Highlight code blocks in bot messages
        const codeBlocks = messageElement.querySelectorAll('pre code');
        codeBlocks.forEach(block => {
            hljs.highlightElement(block);
        });
    } else {
        messageElement.textContent = text;
    }
    chatWindow.appendChild(messageElement);
    messageElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
}

function sendMessage() {
    const input = document.getElementById("message-input");
    const message = input.value.trim();
    const loadingIndicator = document.getElementById("loading-indicator");

    if (message) {
        addMessage(message, "user");
        if (socket.readyState === WebSocket.OPEN) {
            loadingIndicator.style.display = "flex";
            socket.send(JSON.stringify({
                type: "TEXT",
                content: message
            }));
        } else {
            console.error("Failed to send message: WebSocket is not open");
            showErrorBubble("Failed to send message. Please try again.");
        }
        input.value = "";
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
    setTimeout(() => {
        hideErrorBubble();
    }, 5000);
}

function hideErrorBubble() {
    const errorBubble = document.getElementById("error-bubble");
    errorBubble.style.display = "none";
}
