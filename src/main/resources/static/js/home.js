document.addEventListener('DOMContentLoaded', function() {
    const loginLink = document.getElementById('login-link');
    const registerLink = document.getElementById('register-link');
    const logoutForm = document.getElementById('logout-button');
    const chatButton = document.getElementById('chat-button');

    // 쿠키에서 accessToken 확인
    const accessTokenCookie = document.cookie.split('; ').find(row => row.startsWith('accessToken='));

    if (accessTokenCookie) {
        const accessToken = accessTokenCookie.split('=')[1];
        // 토큰이 존재하면 로그인/회원가입 링크를 숨기고 로그아웃 버튼과 채팅 참여 버튼을 보여줌
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';
        if (logoutForm) logoutForm.style.display = 'inline';
        if (chatButton) chatButton.style.display = 'inline';
        console.log("AccessToken found : ", accessToken);
    } else {
        // 토큰이 존재하지 않으면 로그아웃 버튼과 채팅 참여 버튼을 숨기고 로그인/회원가입 링크를 보여줌
        if (loginLink) loginLink.style.display = 'inline';
        if (registerLink) registerLink.style.display = 'inline';
        if (logoutForm) logoutForm.style.display = 'none';
        if (chatButton) chatButton.style.display = 'none';
        console.log("Access token not found.");
    }

    // 로그아웃 버튼 클릭 시 처리
    if (logoutForm) {
        logoutForm.addEventListener('click', function (event) {
            event.preventDefault(); // 기본 제출 동작 방지

            // 쿠키에서 토큰 삭제
            document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
            document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

            // 로그아웃 후 페이지 새로고침
            window.location.href = '/';
        });
    }
});
