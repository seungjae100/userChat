document.addEventListener('DOMContentLoaded', function () {
    const chatLinks = document.querySelectorAll('.chat-link');
    const chatContent = document.getElementById('chat-content');
    const chatUserHeader = document.getElementById('current-chat-user');
    const sendButton = document.getElementById('send-button');
    const messageInput = document.getElementById('message-input');
    const exitChatButton = document.getElementById('exit-chat-button');

    // 모달 관련 요소
    const leaveModal = document.getElementById('leaveModal');
    const confirmLeaveBtn = document.getElementById('confirmLeaveBtn');
    const cancelLeaveBtn = document.getElementById('cancelLeaveBtn');
    const modalTitle = document.getElementById('modalTitle');

    // JWT 토큰에서 현재 사용자 정보 추출
    const accessToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('accessToken='))
        ?.split('=')[1];

    let currentEmail;
    let stompClient = null; // STOMP 클라이언트 선언
    let lastDisplayedDate = null; // 마지막 표시된 날짜 저장

    if (accessToken) {
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        currentEmail = payload.sub;
        chatLinks.forEach(link => {
            link.addEventListener('click', async function (event) {
                event.preventDefault();

                // URL 생성을 위한 이메일
                const chatUserEmail = link.getAttribute('data-user-email');

                // 채팅방 이름을 위한 username
                const chatUsername = link.getAttribute('data-user-username')

                if (!chatUsername || !chatUserEmail) {
                    console.error("선택된 유저 정보를 찾을 수 없습니다.");
                    return;
                }
                // 채팅방 이름 설정을 위한 username
                chatUserHeader.textContent = chatUsername;
                // 채팅방 입장 시 나가기 버튼 표시
                showExitButton();

                // 사용자 이메일을 알파벳순으로 정렬하고 공백을 제거하고 소문자로 변환
                const normalizedCurrentEmail = currentEmail.trim().toLowerCase();
                const normalizedChatUserEmail = chatUserEmail.trim().toLowerCase();

                try {
                    const response = await fetch(`/api/chatRoom/getId?user1Email=${encodeURIComponent(normalizedCurrentEmail)}&user2Email=${encodeURIComponent(normalizedChatUserEmail)}`);
                    if (response.ok) {
                        const chatRoomId = await response.text();

                        const newUrl = `/chatRoom/${chatRoomId}`;
                        history.pushState(null, '', newUrl);

                        await joinRoom(chatRoomId);


                    } else {
                        console.error("채팅방 Id를 가져오는 도중 에러 발생");
                    }
                } catch (error) {
                    console.error("서버에 채팅방 ID 요청 중 오류 발생", error);
                }
            });
        });

        function showExitButton() {
            exitChatButton.style.display = "block"; // 나가기 버튼 표시
        }

        function hideExitChatButton() {
            exitChatButton.style.display = "none"; // 나가기 버튼 숨김
        }

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

        async function joinRoom(chatRoomId) {
            try {
                const response = await fetch(`/chat/enter?chatRoomId=${encodeURIComponent(chatRoomId)}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${accessToken}`
                    }
                });
                const result = await response.json();

                // 서버로부터 받은 재입장 여부를 세션 스토리지에 저장
                sessionStorage.setItem(`isReturningUser_${chatRoomId}`, result.isReturningUser);

                // 채팅창 초기화 및 WebSocket 설정
                setupChatRoom(chatRoomId);

            } catch (error) {
                console.error("채팅방 입장 중 오류 발생: ", error);
            }
        }

        function setupChatRoom(chatRoomId) {
            // 세션 스토리지에서 재입장 여부를 확인
            const isReturningUser = sessionStorage.getItem(`isReturningUser_${chatRoomId}`) === 'true';

            // **홈화면 이동 후 복귀는 기존 메시지 유지**
            if (isReturningUser && sessionStorage.getItem(`chatContent_${chatRoomId}`)) {
                // 세션 스토리지에서 이전 채팅 내용을 복구
                chatContent.innerHTML = sessionStorage.getItem(`chatContent_${chatRoomId}`);
                lastDisplayedDate = sessionStorage.getItem(`lastDisplayedDate_${chatRoomId}`);
            } else if (!isReturningUser) {
                // 첫 입장인 경우 서버에서 메시지 로드
                fetchMessages(chatRoomId);
            }
            // Websocket 설정
            setupStompClient(chatRoomId);
        }

        // WebSocket 연결 전에 이전 연결을 정리하는 함수
        function cleanupPreviousConnection() {
            if (stompClient !== null) {
                try {
                    stompClient.disconnect();
                } catch (e) {
                    console.error("Error during disconnect:", e);
                }
                stompClient = null;
            }
        }

        function setupStompClient(chatRoomId) {
            cleanupPreviousConnection();

            const socket = new SockJS('/chat');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, function (frame) {
                console.log('Connected: ' + frame);
                stompClient.subscribe(`/topic/chat/${chatRoomId}`, function (message) {
                    const parsedMessage = JSON.parse(message.body);
                    console.log("Received message:", parsedMessage); // 수신된 메시지 확인
                    showMessageOutput(parsedMessage);
                    scrollToBottom();
                });

                const subscribeMessage = {
                    type: 'SUBSCRIBE',
                    roomId: chatRoomId
                };
                stompClient.send(`/app/chat.subscribe/${chatRoomId}`, {}, JSON.stringify(subscribeMessage));
            });
        }

        // 채팅방 내옹을 초기화하는 함수
        function clearChatWindow() {
            console.log("Clearing chat window - Previous content:", chatContent.innerHTML); // 초기화 전 내용 확인
            chatContent.innerHTML = '';
            console.log("Chat window cleared - Current content:", chatContent.innerHTML); // 초기화 후 내용 확인
            lastDisplayedDate = null;
            lastMessageTime = null;
            lastMessageElement = null;
            lastSender = null;
        }

        function fetchMessages(chatRoomId) {
            fetch(`/api/chatRoom/${chatRoomId}/messages`, {
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
                    scrollToBottom();
                })
                .catch(error => console.error('채팅 데이터를 가져오던 중 오류 발생:', error));
        }

        // 사용자별로 마지막 시간대의 시간 정보를 저장
        let lastMessageTime = null; // 마지막 시간대의 시간 정보를 저장
        let lastMessageElement = null; // 마지막 시간 표시된 메시지 요소 저장
        let lastSender = null; // 마지막 메시지를 보낸 사용자의 이메일 저장

        function showMessageOutput(message) {
            const isSentByMe = message.sender === currentEmail || message.sender === currentEmail.split('@')[0];
            const messageDate = new Date(message.timestamp);
            const formattedDate = messageDate.toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                weekday: 'long'
            });

            // 날짜가 변경될 경우, 중앙에 날짜를 시스템 메시지로 표시
            if (lastDisplayedDate !== formattedDate) {
                const dateMessageDiv = document.createElement('div');
                dateMessageDiv.classList.add('date-system-message');
                dateMessageDiv.textContent = formattedDate;
                chatContent.appendChild(dateMessageDiv);
                lastDisplayedDate = formattedDate;
            }

            // 시스템 메시지 처리
            if (message.type === 'SYSTEM') {
                const systemMessageDiv = document.createElement('div');
                systemMessageDiv.classList.add('system-message');
                systemMessageDiv.textContent = message.content;
                chatContent.appendChild(systemMessageDiv);
                scrollToBottom();
                return;
            }

            // 채팅 메시지 DOM 생성
            const messageDiv = document.createElement('div');
            messageDiv.classList.add('message', isSentByMe ? 'message-sent' : 'message-received');
            const messageContent = document.createElement('div');
            messageContent.classList.add('message-content');
            messageContent.textContent = message.content;

            // 메시지 요소 추가
            messageDiv.appendChild(messageContent);

            // 메시지 그룹화: 이전 메시지와 동일 그룹인지 확인
            let messageGroup = document.querySelector('.message-group:last-child');
            if (!messageGroup || lastSender !== message.sender || lastMessageTime !== messageDate.toTimeString().slice(0, 5)) {
                messageGroup = document.createElement('div');
                messageGroup.classList.add('message-group', isSentByMe ? 'message-sent-group' : 'message-received-group');
                chatContent.appendChild(messageGroup);
            }

            messageGroup.appendChild(messageDiv);

            // 메시지 시간 표시
            const timeSpan = document.createElement('span');
            timeSpan.classList.add('message-time');
            timeSpan.textContent = messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            timeSpan.classList.add(isSentByMe ? 'time-right' : 'time-left');

            // 이전 시간 표시를 대체
            const existingTimeSpan = messageGroup.querySelector('.message-time');
            if (existingTimeSpan) {
                messageGroup.removeChild(existingTimeSpan);
            }
            messageGroup.appendChild(timeSpan);

            // 마지막 메시지 정보 갱신
            lastSender = message.sender;
            lastMessageTime = messageDate.toTimeString().slice(0, 5);

            // 스크롤을 가장 아래로 이동
            scrollToBottom();

            // **채팅 내용을 세션 스토리지에 저장**
            const chatRoomId = window.location.pathname.split("/")[2]; // 현재 채팅방 ID
            sessionStorage.setItem(`chatContent_${chatRoomId}`, chatContent.innerHTML);
            sessionStorage.setItem(`lastDisplayedDate_${chatRoomId}`, lastDisplayedDate);
        }


        // 모달을 열고 닫는 함수
        function openModal() {
            const chatUsername = chatUserHeader.textContent.trim();
            modalTitle.textContent = `${chatUsername}님의 채팅방을 나가시겠습니까?`; // 모달 제목 표시
            leaveModal.style.display = 'flex';
        }

        function closeModal() {
            leaveModal.style.display = 'none';
        }

        // 나가기 버튼 클릭 시 모달 표시
        exitChatButton.addEventListener('click', function (event) {
            event.preventDefault();
            openModal();
        });

        // 모달에서 예 버튼 클릭 시 나가기 요청
        confirmLeaveBtn.addEventListener('click', function () {
            const chatRoomId = window.location.pathname.split("/")[2];

            // 채팅방 데이터를 세션 스토리지에서 삭제
            sessionStorage.removeItem(`chatContent_${chatRoomId}`);
            sessionStorage.removeItem(`lastDisplayedDate_${chatRoomId}`);
            sessionStorage.removeItem(`isReturningUser_${chatRoomId}`);

            fetch(`/chat/leave?chatRoomId=${encodeURIComponent(chatRoomId)}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            })
                .then(response => {
                    if (response.ok) {
                        alert('채팅방에서 나갔습니다.');
                        window.location.href = '/chatRoom'; // 채팅방 목록 페이지로 리다이렉트
                        hideExitChatButton();
                    } else {
                        alert('채팅방 나가기에 실패했습니다.');
                    }
                })
                .catch(error => console.error('채팅방 나가기 중 오류 발생:', error));

            closeModal(); // 모달 닫기
        });

        // 모달에서 아니오 버튼 클릭 시 모달 닫기
        cancelLeaveBtn.addEventListener('click', function () {
            closeModal();
        });
        // 초기화 시 모달을 숨김
        closeModal();
    }
});
