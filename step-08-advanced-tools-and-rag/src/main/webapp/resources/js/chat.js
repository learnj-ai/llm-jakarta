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
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'userId') {
            return value;
        }
    }
    return null;
}

function updateCartCount(cartCountElement) {
    const userId = getUserId();
    const contextPath = getApplicationContext();
    const path = `${contextPath}/rest-api/session/count/${userId}`;
    console.log(path);

    fetch(path)
        .then(response => response.json())
        .then(data => {
            cartCountElement.textContent = data;
        })
        .catch(error => console.error("Error fetching cart count:", error));
}


function connectWebSocket() {

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const host = window.location.host;
    const contextPath = getApplicationContext();
    const path = `${contextPath}/bookUpdates`;
    const wsUrl = `${protocol}//${host}${path}`;

   let socketUpdate = new WebSocket(wsUrl);

    socketUpdate.onmessage = function(event) {
        console.log("Received message: ", event.data);
        if (event.data === 'update') {
            document.getElementById('refreshForm:refreshButton').click();
        }
    };

    socketUpdate.onopen = function () {
        console.log("Connected to Update WebSocket");
    };

    socketUpdate.onclose = function() {
        // Reconnect after a delay if connection is lost
        setTimeout(connectWebSocket, 1000);
    };
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
        // Parse markdown and wrap in markdown-content div
        currentStreamingMessage.innerHTML = `<div class="markdown-content">${marked.parse(markdownBuffer)}</div>`;

        // Find and enhance code blocks
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

        // Process code blocks in bot messages
        const codeBlocks = messageElement.querySelectorAll('pre code');
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
    } else {
        messageElement.textContent = text;
    }
    chatWindow.appendChild(messageElement);

    // Smooth scroll to the new message with a slight delay to ensure proper rendering
    setTimeout(() => {
        messageElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }, 50);
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

    // Add warning icon if not present in the message
    if (!message.includes('⚠️')) {
        errorBubble.textContent = '⚠️ ' + message;
    }

    // Click to dismiss
    errorBubble.onclick = function () {
        hideErrorBubble();
    };

    // Auto dismiss after 5 seconds
    setTimeout(hideErrorBubble, 5000);
}

function hideErrorBubble() {
    const errorBubble = document.getElementById("error-bubble");
    errorBubble.style.opacity = '0';
    setTimeout(() => {
        errorBubble.style.display = 'none';
        errorBubble.style.opacity = '1';
    }, 300);
}
