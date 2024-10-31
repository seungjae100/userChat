document.addEventListener('DOMContentLoaded', function () {
    const socket = new SockJS('/chat-websocket');
    const stompClient = Stomp.over(socket);
    const chatRoomId = document.querySelector('h1').getAttribute('data-room-id');

    // JWT 토큰에서 현재 사용자 정보 추출
    const accessToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('accessToken'))
        ?.split('=')[1];

    if (accessToken) {
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        const currentUser = payload.sub;

        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/' + chatRoomId, function (messageOutput) {
                showMessageOutput(JSON.parse(messageOutput.body));
            });
        });

        document.getElementById('sendMessageButton').addEventListener('click', function () {
            const messageInput = document.getElementById('messageInput');
            const messageContent = messageInput.value.trim();

            if (messageContent && stompClient) {
                const chatMessage = {
                    sender: currentUser,
                    content: messageContent,
                    chatRoomId: chatRoomId
                };
                stompClient.send("/app/chat.sendMessage/" + chatRoomId, {}, JSON.stringify(chatMessage));
                messageInput.value = '';
            }
        });

        function showMessageOutput(message) {
            const chatMessageDiv = document.getElementById('chatMessage');
            const messageElement = document.createElement('p');
            messageElement.innerHTML = `<strong>${message.sender}</strong>: ${message.content}`;
            chatMessageDiv.appendChild(messageElement);
        }
    } else {
        console.error('AccessToken 을 발견하지 못했습니다.');
    }
});