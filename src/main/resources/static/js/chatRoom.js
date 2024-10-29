document.addEventListener('DOMContentLoaded', function () {
    const chatLinks = document.querySelectorAll('.chat-link');
    const chatContent = document.getElementById('chat-content');
    const chatUserHeader = document.getElementById('current-chat-user');
    const sendButton = document.getElementById('send-button');
    const messageInput = document.getElementById('message-input');

    // JWT 토큰에서 현재 사용자 정보 추출
    const accessToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('accessToken='))
        ?.split('=')[1];

    let currentEmail;
    let stompClient = null; // STOMP 클라이언트 선언

    if (accessToken) {
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        currentEmail = payload.sub;

        chatLinks.forEach(link => {
            link.addEventListener('click', async function (event) {
                event.preventDefault();
                const chatUserEmail = link.getAttribute('data-user-email');

                if (!chatUserEmail) {
                    console.error("선택된 유저 정보를 찾을 수 없습니다.");
                    return;
                }

                // 사용자 이메일을 알파벳순으로 정렬하고 공백을 제거하고 소문자로 변환
                const normalizedCurrentEmail = currentEmail.trim().toLowerCase();
                const normalizedChatUserEmail = chatUserEmail.trim().toLowerCase();

                const sortedUsers = normalizedCurrentEmail.localeCompare(normalizedChatUserEmail) < 0
                    ? `${normalizedCurrentEmail}_${normalizedChatUserEmail}`
                    : `${normalizedChatUserEmail}_${normalizedCurrentEmail}`;

                try {

                    const response = await fetch(`/api/chatRoom/getId?user1Email=${encodeURIComponent(normalizedCurrentEmail)}&user2Email=${encodeURIComponent(normalizedChatUserEmail)}`);
                    if (response.ok) {
                        const chatRoomId = await response.text();

                        console.log(`Received chatRoomId: ${chatRoomId}`); // 디버깅용 로그 추가

                        const newUrl = `/chatRoom/${chatRoomId}`;
                        history.pushState(null, '', newUrl);

                        fetchMessages(chatRoomId);
                        setupStompClient(chatRoomId);

                        chatUserHeader.textContent = chatUserEmail;
                    } else {
                        console.error("채팅방 Id를 가져오는 도중 에러 발생");
                    }
                } catch (error) {
                    console.error("서버에 채팅방 ID 요청 중 오류 발생", error);
                }
            });
        });

        sendButton.addEventListener('click', function () {
            sendMessage();
        });

        messageInput.addEventListener('keypress', function (event) {
            if (event.key === 'Enter') {
                sendMessage();
            }
        });

        function sendMessage() {
            const messageContent = messageInput.value.trim();
            if (messageContent && stompClient) {
                const chatRoomId = window.location.pathname.split("/")[2];
                const targetUser = chatUserHeader.textContent;

                const chatMessage = {
                    type: 'CHAT',
                    sender: currentEmail,
                    receiver: targetUser,
                    content: messageContent,
                    chattingRoomId: chatRoomId,
                    timestamp: new Date().toISOString()
                };

                stompClient.send(`/app/chat/${chatRoomId}`, {}, JSON.stringify(chatMessage));
                messageInput.value = '';
                scrollToBottom();
            } else {
                alert('채팅 연결이 끊어졌습니다. 페이지를 새로고침해주세요.');
            }
        }

        function scrollToBottom() {
            chatContent.scrollTop = chatContent.scrollHeight;
        }

        function setupStompClient(chatRoomId) {
            const socket = new SockJS('/chat');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, function (frame) {
                console.log('Connected: ' + frame);
                stompClient.subscribe(`/topic/chat/${chatRoomId}`, function (message) {
                    const parsedMessage = JSON.parse(message.body);
                    if (parsedMessage.type === 'CHAT') {
                        showMessageOutput(parsedMessage);
                        scrollToBottom();
                    }
                });

                const subscribeMessage = {
                    type: 'SUBSCRIBE',
                    roomId: chatRoomId
                };
                stompClient.send(`/app/chat.subscribe/${chatRoomId}`, {}, JSON.stringify(subscribeMessage));
            });
        }

        function fetchMessages(chattingRoomId) {
            fetch(`/api/chatRoom/${chattingRoomId}/messages`, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            })
                .then(response => response.json())
                .then(messages => {
                    chatContent.innerHTML = '';
                    messages.forEach(message => {
                        showMessageOutput(message);
                    });
                })
                .catch(error => console.error('채팅 데이터를 가져오던 중 오류 발생:', error));
        }

        function showMessageOutput(message) {
            const messageDiv = document.createElement('div');
            const isSentByMe = message.sender === currentEmail;

            messageDiv.classList.add('message', isSentByMe ? 'message-sent' : 'message-received');

            const messageContent = document.createElement('div');
            messageContent.classList.add('message-content');

            const textSpan = document.createElement('span');
            textSpan.classList.add('message-text');
            textSpan.textContent = message.content;

            const timeSpan = document.createElement('span');
            timeSpan.classList.add('message-time');
            const messageTime = new Date(message.timestamp);
            timeSpan.textContent = messageTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

            messageContent.appendChild(textSpan);
            messageContent.appendChild(timeSpan);
            messageDiv.appendChild(messageContent);

            chatContent.appendChild(messageDiv);
        }
    }
});
