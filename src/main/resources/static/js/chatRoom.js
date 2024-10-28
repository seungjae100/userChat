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

    let currentUsername;
    let webSocket = null; // WebSocket 변수 선언

    if (accessToken) {
        // JWT 토큰을 디코딩하여 사용자 정보를 추출 (현재는 사용자 이메일)
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        currentUsername = payload.sub;

        // SHA256 해시를 생성하는 비동기 함수
        async function generateSHA256(input) {
            const encoder = new TextEncoder();
            const data = encoder.encode(input);
            const hasBuffer = await crypto.subtle.digest('SHA-256', data);
            const hashArray = Array.from(new Uint8Array(hasBuffer));
            const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
            return hashHex;
        }

        // 각 사용자와의 채팅 시작 이벤트 처리
        chatLinks.forEach(link => {
            link.addEventListener('click', async function (event) { // async 추가
                event.preventDefault();
                const chatUserName = link.getAttribute('data-username');

                if (!chatUserName) {
                    console.error("선택된 유저 정보를 찾을 수 없습니다.");
                    return;
                }

                // URL 업데이트
                const sortedUsers = [currentUsername, chatUserName].sort().join('_'); // 사용자를 정렬하여 일관성 유지
                try {
                    const hashedChatRoomId = await generateSHA256(sortedUsers); // 채팅방 ID 해시 생성
                    const newUrl = `/chatRoom/${hashedChatRoomId}`;
                    history.pushState(null, '', newUrl);

                    // 서버에서 해당 유저와의 메세지 목록 가져오기
                    fetchMessages(hashedChatRoomId);

                    // WebSocket 연결 설정 및 구독 갱신
                    setupWebSocket(hashedChatRoomId);

                    // 현재 채팅 대상 표시
                    chatUserHeader.textContent = chatUserName;
                } catch (error) {
                    console.error("채팅방 ID를 생성하거나 서버 요청 중 오류 발생: ", error);
                }
            });
        });

        // 메세지 전송 처리
        sendButton.addEventListener('click', function () {
            const messageContent = messageInput.value.trim();
            if (messageContent && webSocket && webSocket.readyState === WebSocket.OPEN) {
                const chatRoomId = window.location.pathname.split("/")[2];

                // 현재 채팅 대상 가져오기
                const targetUser = chatUserHeader.textContent || '알려지지 않은 유저입니다.';

                const chatMessage = {
                    sender: currentUsername,
                    receiver: targetUser,
                    content: messageContent,
                    chattingRoomId: chatRoomId
                };
                webSocket.send(JSON.stringify(chatMessage));

                // 직접 화면에 추가
                showMessageOutput(chatMessage);
                messageInput.value = '';
            } else {
                console.error('WebSocket 이 연결되지 않았습니다.');
            }
        });

        // WebSocket 메시지 처리
        function setupWebSocket(chatRoomId) {
            if (webSocket !== null && webSocket.readyState === WebSocket.OPEN) {
                console.log('이미 연결된 WebSocket이 있습니다. 기존 연결을 해제합니다.');
                webSocket.close();
            }
            initializeWebSocket(chatRoomId);
        }

        function initializeWebSocket(chatRoomId) {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const socketUrl = `${protocol}//${window.location.host}/chat?roomId=${chatRoomId}`;
            webSocket = new WebSocket(socketUrl);

            webSocket.onopen = function () {
                console.log('WebSocket 연결 성공');
            };

            webSocket.onmessage = function (event) {
                console.log(`메시지 수신: ${event.data}`);
                try {
                    const parsedMessage = JSON.parse(event.data);
                    showMessageOutput(parsedMessage);
                } catch (e) {
                    console.error('메세지 파싱 중 오류 발생', e);
                }
            };

            webSocket.onclose = function () {
                console.log('WebSocket 연결이 닫혔습니다.');
                webSocket = null;
            };

            webSocket.onerror = function (error) {
                console.error('WebSocket 오류 발생: ', error);
            };
        }

        // 서버 메세지 목록 가져오기
        function fetchMessages(chattingRoomId) {
            fetch(`/api/chatRoom/${chattingRoomId}/messages`, {
                headers: {
                    'Authorization': `Bearer ${accessToken}` // JWT 토큰을 Authorization 헤더에 추가
                }
            })
                .then(response => response.json())
                .then(messages => {
                    chatContent.innerHTML = ''; // 기존 채팅 내용 초기화
                    messages.forEach(message => {
                        showMessageOutput(message); // 각 메세지를 화면에 표시
                    });
                })
                .catch(error => console.error('채팅 데이터를 가져오던 중 오류 발생:', error));
        }

        // 받은 메세지를 화면에 표시하는 함수
        function showMessageOutput(message) {
            const messageDiv = document.createElement('div');
            messageDiv.classList.add('message', message.sender === currentUsername ? 'from-user' : 'from-other');

            const senderSpan = document.createElement('span');
            senderSpan.classList.add('sender');
            senderSpan.textContent = `${message.sender} → ${message.receiver}`;

            const messageTextDiv = document.createElement('div');
            messageTextDiv.classList.add('message-text');
            messageTextDiv.textContent = message.content;

            messageDiv.appendChild(senderSpan);
            messageDiv.appendChild(messageTextDiv);
            chatContent.appendChild(messageDiv);
            chatContent.scrollTop = chatContent.scrollHeight;
        }
    }

    // Enter 키로 전송하기
    messageInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            sendButton.click(); // 전송 버튼과 동일하게 처리
        }
    });
});
