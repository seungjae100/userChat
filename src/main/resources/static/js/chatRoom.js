document.addEventListener('DOMContentLoaded', function () {
    const chatLinks = document.querySelectorAll('.chat-link');
    const chatContent = document.getElementById('chat-content');
    const chatUserHeader = document.getElementById('current-chat-user');

    // JWT 토큰에서 현재 사용자 정보 추출
    const accessToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('accessToken='))
        ?.split('=')[1];

    let currentUsername; //

    if (accessToken) {
        // JWT 토큰을 디코딩하여 사용자 정보를 추출 (현재는 사용자 이메일)
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        currentUsername = payload.sub;

        // 각 사용자와의 채팅 시작 이벤트 처리
        chatLinks.forEach(link => {
            link.addEventListener('click', function (event) {
                event.preventDefault();
                const targetUser = link.getAttribute('data-username'); // 클릭된 상대방 사용자 이름 가져오기


                // 현재 채팅 대상의 이름을 화면에 업데이트
                chatUserHeader.textContent = targetUser;

                // 채팅방 Id 생성
                const chattingRoomId = generateRoomId(currentUsername, targetUser);

                // URL 업데이트
                const newUrl = `/chatRoom/${chattingRoomId}`;
                history.pushState(null, '', newUrl);

                // 서버에서 해당 유저와의 메시지 목록 가져오기
                fetch(`/api/chat/${chattingRoomId}`)
                    .then(response => response.json())
                    .then(messages => {
                        chatContent.innerHTML = '';
                        messages.forEach(message => {
                            const messageDiv = document.createElement('div');
                            messageDiv.classList.add('message', message.sender === currentUsername ? 'from-user' : 'from-other');

                            const senderSpan = document.createElement('span');
                            senderSpan.classList.add('sender');
                            senderSpan.textContent = message.sender;

                            const messageTextDiv = document.createElement('div');
                            messageTextDiv.classList.add('message-text');
                            messageTextDiv.textContent = message.content;

                            messageDiv.appendChild(senderSpan);
                            messageDiv.appendChild(messageTextDiv);
                            chatContent.appendChild(messageDiv);
                        });
                    })
                    .catch(error => console.error('채팅 데이터를 가져오는 중 오류 발생:', error));
            });
        });

    // 메시지 전송 버튼 클릭 이벤트 처리
    const sendButton = document.getElementById('send-button');
    const messageInput = document.getElementById('message-input');

    sendButton.addEventListener('click', function () {
        const messageContent = messageInput.value.trim();
        if (messageContent) {
            // 현재 채팅 상대 가져오기
            const targetUser = chatUserHeader.textContent;
            const chattingRoomId = generateRoomId(currentUsername, targetUser);

            // 서버로 메시지 전송
            fetch(`/api/chat/send`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${accessToken}`
                },
                body: JSON.stringify({
                    sender: currentUsername,
                    receiver: targetUser,
                    content: messageContent
                })
            })
                .then(response => response.json())
                .then(sentMessage => {
                    // 메시지 화면에 추가
                    const messageDiv = document.createElement('div');
                    messageDiv.classList.add('message', 'from-user');

                    const senderSpan = document.createElement('span');
                    senderSpan.classList.add('sender');
                    senderSpan.textContent = sentMessage.sender;

                    const messageTextDiv = document.createElement('div');
                    messageTextDiv.classList.add('message-text');
                    messageTextDiv.textContent = sentMessage.content;

                    messageDiv.appendChild(senderSpan);
                    messageDiv.appendChild(messageTextDiv);
                    chatContent.appendChild(messageDiv);

                    // 메시지 입력창 초기화
                    messageInput.value = '';
                })
                .catch(error => console.error('채팅을 보내는데 실패하였습니다.:', error));
        }
    });

        // 채팅방 ID 생성 함수
        function generateRoomId(user1, user2) {
            const users = [user1, user2].sort();
            return CryptoJS.SHA256(users.join('_')).toString();
        }
}});
