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

function toggleChat() {
    const chatContainer = document.getElementById("chat-container");
    const chatIcon = document.querySelector('.chat-icon');
    const isOpening = chatContainer.style.display === "none";

    if (isOpening) {
        // Hide chat icon
        chatIcon.style.display = "none";

        // Show and animate container
        chatContainer.style.display = "flex";
        chatContainer.classList.add("animating");

        // Remove animation class after animation completes
        setTimeout(() => {
            chatContainer.classList.remove("animating");
        }, 300);

        // Connect if needed
        if (!socket || socket.readyState !== WebSocket.OPEN) {
            connect();
        }

        // Focus input
        setTimeout(() => {
            document.getElementById('message-input').focus();
        }, 300);
    } else {
        // Start closing animation
        chatContainer.classList.add("closing");

        // Hide after animation
        setTimeout(() => {
            chatContainer.style.display = "none";
            chatContainer.classList.remove("closing");
        }, 300);

        // Show chat icon with delay for smooth transition
        setTimeout(() => {
            chatIcon.style.display = "flex";
            chatIcon.style.animation = "fadeIn 0.2s cubic-bezier(0.34, 1.56, 0.64, 1)";
            setTimeout(() => chatIcon.style.animation = "", 200);
        }, 200);
    }
}

function toggleFullScreen() {
    const chatContainer = document.getElementById("chat-container");
    const chatIcon = document.querySelector('.chat-icon');
    const fullscreenButton = document.querySelector('.chat-fullscreen i');

    // Add transition class
    chatContainer.classList.add('transitioning');
    chatContainer.classList.toggle("fullscreen");

    if (chatContainer.classList.contains("fullscreen")) {
        fullscreenButton.classList.remove("fa-expand");
        fullscreenButton.classList.add("fa-compress");
        chatIcon.style.display = 'none';

        // Focus input when going fullscreen
        setTimeout(() => {
            document.getElementById('message-input').focus();
        }, 300);
    } else {
        fullscreenButton.classList.remove("fa-compress");
        fullscreenButton.classList.add("fa-expand");
        chatIcon.style.display = 'flex';
    }

    // Handle layout changes
    setTimeout(() => {
        chatContainer.classList.remove('transitioning');
        const chatWindow = document.getElementById('chat-window');
        chatWindow.scrollTop = chatWindow.scrollHeight;
    }, 300);
}

function loadPersonalities() {
    const personalitySelect = document.getElementById("personality-select");
    const contextPath = getApplicationContext();

    // Clear existing options
    personalitySelect.innerHTML = '';

    // Fetch available personalities
    fetch(`${contextPath}/rest-api/personalities`)
        .then(response => {
            console.log('Personalities response:', response);
            return response.json();
        })
        .then(personalities => {
            console.log('Available personalities:', personalities);
            if (Array.isArray(personalities)) {
                personalities.forEach(personality => {
                    console.log('Creating option for personality:', personality);
                    const option = document.createElement('option');
                    option.value = personality.name;
                    option.textContent = personality.displayName;
                    personalitySelect.appendChild(option);
                });
            } else {
                console.error('Expected array of personalities but got:', personalities);
            }

            // Get and set current personality
            return fetch(`${contextPath}/rest-api/personalities/current`);
        })
        .then(response => {
            console.log('Current personality response:', response);
            return response.json();
        })
        .then(currentPersonality => {
            console.log('Current personality:', currentPersonality);
            if (currentPersonality && currentPersonality.name) {
                personalitySelect.value = currentPersonality.name;
            } else {
                console.error('Invalid current personality response:', currentPersonality);
            }
        })
        .catch(error => {
            console.error('Error loading personalities:', error);
        });
}

function loadModels() {
    const modelSelect = document.getElementById("model-select");
    const contextPath = getApplicationContext();

    // Clear existing options
    modelSelect.innerHTML = '';

    // Fetch available models
    fetch(`${contextPath}/rest-api/models`)
        .then(response => {
            console.log('Models response:', response);
            return response.json();
        })
        .then(models => {
            console.log('Available models:', models);
            if (Array.isArray(models)) {
                models.forEach(model => {
                    console.log('Creating option for model:', model);
                    const option = document.createElement('option');
                    option.value = model.modelName;
                    option.textContent = model.displayName;
                    modelSelect.appendChild(option);
                });
            } else {
                console.error('Expected array of models but got:', models);
            }

            // Get and set current model
            return fetch(`${contextPath}/rest-api/models/current`);
        })
        .then(response => {
            console.log('Current model response:', response);
            return response.json();
        })
        .then(currentModel => {
            console.log('Current model:', currentModel);
            if (currentModel && currentModel.modelName) {
                modelSelect.value = currentModel.modelName;
            } else {
                console.error('Invalid current model response:', currentModel);
            }
        })
        .catch(error => {
            console.error('Error loading models:', error);
        });
}

function switchModel(modelType) {
    const contextPath = getApplicationContext();
    const loadingIndicator = document.getElementById("loading-indicator");
    loadingIndicator.style.display = "flex";

    fetch(`${contextPath}/rest-api/models/switch/${modelType}`, {
        method: 'POST'
    })
    .then(response => {
        console.log('Switch model response:', response);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(model => {
        console.log('Switched to model:', model);
        if (model && model.displayName) {
            addMessage(`Switched to ${model.displayName} model`, "bot");
        } else {
            console.error('Invalid model response:', model);
            throw new Error('Invalid model response');
        }
    })
    .catch(error => {
        console.error('Error switching model:', error);
        showErrorBubble("Failed to switch model. Please try again.");
    })
    .finally(() => {
        loadingIndicator.style.display = "none";
    });
}

function switchPersonality(personality) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        const loadingIndicator = document.getElementById("loading-indicator");
        loadingIndicator.style.display = "flex";

        // Send personality switch command
        socket.send(JSON.stringify({
            type: "SWITCH_PERSONALITY",
            personality: personality
        }));

        // Show switching message
        const personalityName = personality.toLowerCase().replace(/_/g, " ");
        addMessage("Switching to " + personalityName + " mode...", "bot");

        // Hide loading after a short delay
        setTimeout(() => {
            loadingIndicator.style.display = "none";
        }, 500);
    } else {
        showErrorBubble("Cannot switch personality: connection lost. Please refresh the page.");
    }
}

function toggleSystemPrompt() {
    const systemPrompt = document.getElementById("system-prompt");
    const isVisible = systemPrompt.style.display === "block";

    if (!isVisible) {
        const personality = document.getElementById("personality-select").value;
        const personalityName = personality.toLowerCase().replace(/_/g, " ");
        const promptText = document.getElementById("system-prompt-text");

        // Show loading state
        promptText.textContent = "Loading system prompt...";
        systemPrompt.style.display = "block";

        // Fetch actual system prompt
        const contextPath = getApplicationContext();
        const fullPath = contextPath + "/rest-api/system-prompt";
        fetch(fullPath)
            .then(response => response.json())
            .then(prompt => {
                const { name, systemMessage } = prompt;
                document.getElementById("current-mode").textContent = name;
                document.getElementById("system-prompt-text").innerHTML = marked.parse(systemMessage);
            })
            .catch(error => {
                console.error('Error fetching system prompt:', error);
                promptText.innerHTML = marked.parse(`### Error\n\nFailed to load system prompt: ${error.message}`);
            });
    } else {
        systemPrompt.style.display = "none";
    }
}

