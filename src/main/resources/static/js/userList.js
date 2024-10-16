document.addEventListener('DOMContentLoaded', function () {
    const chatLinks = document.querySelectorAll('.chat-link');

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

                // 채팅방으로 이동하는 URL 생성
                const chatUrl = `/chat/${currentUser}/${targetUser}`;
                window.location.href = chatUrl;
            });
        });
    } else {
        console.error('AccessToken 을 발견하지 못했습니다.');
    }
});