document.addEventListener('DOMContentLoaded', function () {
    const chatLinks = document.querySelectorAll('.chat-link');
    const chatContent = document.getElementById('chat-content');
    const chatUserHeader = document.getElementById('current-chat-user');

    // JWT 토큰에서 현재 사용자 정보 추출
    const accessToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('accessToken='))
        ?.split('=')[1];

    if (accessToken) {
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        const currentUser = payload.sub;

        chatLinks.forEach(link => {
            link.addEventListener('click', function (event) {
                event.preventDefault();
                const targetUser = link.getAttribute('data-username');

                // 채팅방 사용자 업데이트
                chatUserHeader.textContent = targetUser;

                // 이전 채팅 내용 초기화
                chatContent.innerHTML = '';

                // 서버에서 해당 유저와의 메시지 목록 가져오기
                fetch(`/api/chat/${currentUser}/${targetUser}`)
                    .then(response => response.json())
                    .then(messages => {
                        messages.forEach(message => {
                            const messageDiv = document.createElement('div');
                            messageDiv.classList.add('message');
                            messageDiv.classList.add(message.sender === currentUser ? 'from-user' : 'from-other');

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
                    .catch(error => console.error('Failed to load chat messages:', error));
            });
        });
    } else {
        console.error('AccessToken 을 발견하지 못했습니다.');
    }

    // 메시지 전송 버튼 클릭 이벤트 처리
    const sendButton = document.getElementById('send-button');
    const messageInput = document.getElementById('message-input');

    sendButton.addEventListener('click', function () {
        const messageContent = messageInput.value.trim();
        if (messageContent) {
            // 현재 채팅 상대 가져오기
            const targetUser = chatUserHeader.textContent;

            // 서버로 메시지 전송
            fetch(`/api/chat/send`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${accessToken}`
                },
                body: JSON.stringify({
                    sender: currentUser,
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
                .catch(error => console.error('Failed to send message:', error));
        }
    });
});
