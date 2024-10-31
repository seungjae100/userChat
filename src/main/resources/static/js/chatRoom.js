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

        // 사용자별로 마지막 시간대의 시간 정보를 저장
        let lastMessageTime = null; // 마지막 시간대의 시간 정보를 저장
        let lastMessageElement = null; // 마지막 시간 표시된 메시지 요소 저장
        let lastSender = null; // 마지막 메시지를 보낸 사용자의 이메일 저장

        function showMessageOutput(message) {
            const messageDiv = document.createElement('div');
            const isSentByMe = message.sender === currentEmail;

            // 메시지 요소 설정
            messageDiv.classList.add('message', isSentByMe ? 'message-sent' : 'message-received');

            const messageContent = document.createElement('div');
            messageContent.classList.add('message-content');
            messageContent.textContent = message.content;

            // 현재 메시지의 시간 추출
            const messageTime = new Date(message.timestamp);
            const currentHoursMinutes = messageTime.getHours() + ':' + messageTime.getMinutes();

            // 시간 표시 로직 수정 - 이전 메시지들의 시간 표시 제거 후 최신 메시지에만 시간 표시
            if (lastMessageElement && lastSender === message.sender && lastMessageTime ===currentHoursMinutes) {
                const previousTimeSpan = lastMessageElement.querySelector('.message-time');
                if (previousTimeSpan) {
                    lastMessageElement.removeChild(previousTimeSpan);
                }
            }

            // 현재 메시지에 시간 추가 (항상 최신 메시지에만 시간 표시)
            const timeSpan = document.createElement('span');
            timeSpan.classList.add('message-time');
            timeSpan.textContent = messageTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            messageDiv.appendChild(timeSpan);

            // 마지막 메시지 요소와 보낸 사용자 업데이트 - 항상 최신 메시지로 업데이트
            lastMessageElement = messageDiv;
            lastSender = message.sender;
            lastMessageTime = currentHoursMinutes;

            // 메시지 요소들을 DOM에 추가
            messageDiv.appendChild(messageContent);
            chatContent.appendChild(messageDiv);

            // 스크롤을 가장 아래로 이동
            scrollToBottom();
        }

    }
});
