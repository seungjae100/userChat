document.addEventListener('DOMContentLoaded', function () {
    const loginLink = document.getElementById('login-link');
    const registerLink = document.getElementById('register-link');
    const logoutButton = document.getElementById('logout-button');
    const chatButton = document.getElementById('chat-button');

    // 토큰 확인 후 요소 상태 업데이트
    updateUserInterface();

    // 로그아웃 버튼 클릭 시 처리
    if (logoutButton) {
        logoutButton.addEventListener('click', handleLogout);
    }

    // 함수: 토큰 확인 후 UI 업데이트
    function updateUserInterface() {
        const accessToken = getAccessToken();

        if (accessToken) {
            showLoggedInState();
        } else {
            showLoggedOutState();
        }
    }

    // 함수: AccessToken 가져오기
    function getAccessToken() {
        const accessTokenCookie = document.cookie.split('; ').find(row => row.startsWith('accessToken='));
        return accessTokenCookie ? accessTokenCookie.split('=')[1] : null;
    }

    // 함수: 로그인 상태 UI 표시
    function showLoggedInState() {
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';
        if (logoutButton) logoutButton.style.display = 'inline';
        if (chatButton) chatButton.style.display = 'inline';
    }

    // 함수: 로그아웃 상태 UI 표시
    function showLoggedOutState() {
        if (loginLink) loginLink.style.display = 'inline';
        if (registerLink) registerLink.style.display = 'inline';
        if (logoutButton) logoutButton.style.display = 'none';
        if (chatButton) chatButton.style.display = 'none';
    }

    // 함수: 로그아웃 처리
    function handleLogout(event) {
        event.preventDefault();

        // 쿠키에서 토큰 삭제
        document.cookie = "accessToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

        // 로그아웃 후 페이지 새로고침
        window.location.href = '/';
    }
});
