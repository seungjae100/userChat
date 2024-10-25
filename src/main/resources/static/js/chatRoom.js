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
    let stompClient = null; // 변수를 전역으로 선언

    if (accessToken) {
        // JWT 토큰을 디코딩하여 사용자 정보를 추출 (현재는 사용자 이메일)
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        currentUsername = payload.sub;

        // 각 사용자와의 채팅 시작 이벤트 처리
        chatLinks.forEach(link => {
            link.addEventListener('click', function (event) {
                event.preventDefault();
                const chatRoomId = link.getAttribute('data-chatRoomId');

                // URL 업데이트
                const newUrl = `/chatRoom/${chatRoomId}`;
                history.pushState(null, '', newUrl);

                // 서버에서 해당 유저와의 메세지 목록 가져오기
                fetchMessages(chatRoomId);

                // WebSocket 연결 설정 및 구독 갱신
                setupWebSocket(chatRoomId);
            });
        });

        // 메세지 전송 처리
        sendButton.addEventListener('click', function () {
            const messageContent = messageInput.value.trim();
            if (messageContent) {
                const chatRoomId = window.location.pathname.split("/")[2];

                // 현재 채팅 대상 가져오기
                const targetUser = chatUserHeader.textContent; // 혹은 채팅 상대방 정보를 가진 변수

                stompClient.send(`/api/chat.sendMessage/${chatRoomId}`, {}, JSON.stringify({
                    sender: currentUsername,
                    receiver: targetUser,
                    content: messageContent,
                    chattingRoomId: chatRoomId
                }));

                // 직접 화면에 추가
                showMessageOutput({
                    sender: currentUsername,
                    receiver: targetUser,
                    content: messageContent

                })

                messageInput.value = '';
            }
        });

    }

    // Enter 키로 전송하기
    messageInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            sendButton.click(); // 전송 버튼과 동일하게 처리
        }
    })

    // WebSocket 연결 설정 및 메세지 구독 처리
    function setupWebSocket(chatRoomId) {
        if (stompClient !== null && stompClient.connected) {
            console.log('이미 연결된 WebSocket이 있습니다. 기존 연결을 해제합니다.');
            stompClient.disconnect(() => {
                console.log('기존 WebSocket 연결 해제 완료');
                initializeWebSocket(chatRoomId);
            });
        } else {
            initializeWebSocket(chatRoomId);
        }
    }

    function initializeWebSocket(chatRoomId) {
        const socket = new SockJS('/chat-websocket'); // 서버의 엔드포인트와 동일하게 설정
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
            console.log('WebSocket 연결 성공');

            if (chatRoomId) {
                stompClient.subscribe('/topic/chat/' + chatRoomId, function (messageOutput) {
                    console.log(`메시지 수신: ${messageOutput.body}`);
                    showMessageOutput(JSON.parse(messageOutput.body));
                });
            } else {
                console.error("채팅방 ID가 설정되지 않았습니다.");
            }
        });
    }

    // 서버 메세지 목록 가져오기
    function fetchMessages(chattingRoomId) {
        fetch(`/api/chat/${chattingRoomId}`)
            .then(response => response.json())
            .then(messages => {
                // 기존 채팅 내용 초기화
                chatContent.innerHTML = '';

                // 각 메세지를 화면에 표시
                messages.forEach(message => {
                    showMessageOutput(message);
                });
            })
            .catch(error => console.error('채팅 데이터를 가져오던 중 오류 발생:', error));
    }

    // 채팅방 ID 생성 함수
    function generateRoomId(user1, user2) {
        const users = [user1.trim(), user2.trim()].sort();
        return CryptoJS.SHA256(users.join('_')).toString();
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
    }
});
