document.addEventListener('DOMContentLoaded', function () {
    const chatLinks = document.querySelectorAll('.chat-link');
    const chatContent = document.getElementById('chat-content');
    const chatUserHeader = document.getElementById('current-chat-user');

    // JWT 토큰에서 현재 사용자 정보 추출
    const accessToken = document.cookie
        .split('; ')
        .find(row => row.startsWith('accessToken='))
        ?.split('=')[1];

    let currentUser; //

    if (accessToken) {
        // JWT 토큰을 디코딩하여 사용자 정보를 추출 (현재는 사용자 이메일)
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        const currentUser = payload.sub;

        // 각 사용자와의 채팅 시작 이벤트 처리
        chatLinks.forEach(link => {
            link.addEventListener('click', function (event) {
                event.preventDefault();
                const targetUser = link.getAttribute('data-username'); // 클릭된 상대방 사용자 이름 가져오기
                const chatRoomId = [currentUser, targetUser].sort().join('_'); // 채팅방 Id 생성 

                // 현재 채팅 대상의 이름을 화면에 업데이트
                chatUserHeader.textContent = targetUser;

                // URL 에 채팅방 ID를 반영하여 동일한 채팅방에 접속할 수 있도록 설정
                history.pushState(null, null, `chatRoom/${currentUser}/${targetUser}`);

                // 이전 채팅 내용 초기화 (이전 대화내용 삭제)
                chatContent.innerHTML = '';

                // 서버에서 해당 유저와의 메시지 목록 가져오기
                fetch(`/api/chat/${chatRoomId}`) // 현재 사용자와 상대방 사이의 대화 불러오기
                    .then(response => response.json())
                    .then(messages => {
                        console.log(messages); // 서버로부터 받은 데이터 확인
                        if(Array.isArray(messages)) { // 반은 메세지가 배열 형태인지 확인
                            messages.forEach(message => {
                                const messageDiv = document.createElement('div');
                                messageDiv.classList.add('message');
                                messageDiv.classList.add(message.sender === currentUser ? 'from-user' : 'from-other'); // 메세지의 발신자에 따라 스타일링 적용

                                const senderSpan = document.createElement('span');
                                senderSpan.classList.add('sender');
                                senderSpan.textContent = message.sender; // 발신자 이름

                                const messageTextDiv = document.createElement('div');
                                messageTextDiv.classList.add('message-text');
                                messageTextDiv.textContent = message.content; // 메세지 내용

                                // 각 메세지 요소들을 조립하여 chatContent 에 추가
                                messageDiv.appendChild(senderSpan);
                                messageDiv.appendChild(messageTextDiv);
                                chatContent.appendChild(messageDiv);
                            });
                        } else {
                            console.error('Expected an array but received:', messages); // 받은 데이터 형식이 올바르지 않을 경우 에러 출력
                        }
                    })
                    .catch(error => console.error('채탕을 로드하지 못했습니다. :', error)); // 메세지 로딩 실패 시 에러 출력
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
                .catch(error => console.error('채팅을 보내는데 실패하였습니다.:', error));
        }
    });

    // 로그아웃 버튼 클릭 시 이벤트 처리
    const logoutButton = document.getElementById('logout-button');
    if (logoutButton) {
        logoutButton.addEventListener('click', function () {
            // 로그아웃 시 쿠키 만료 처리
            document.cookie = 'accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/'; // 쿠키를 만료시킨다.
            window.location.href = '/logout'; // 로그아웃 경로로 리 다이렉트
        })
    }
});
