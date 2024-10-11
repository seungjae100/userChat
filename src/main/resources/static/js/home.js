document.addEventListener("DOMContentLoaded", () => {
    const chatButton = document.getElementById("chat-button");

    // 로그아웃 버튼 클릭 시 토큰 처리
    const logoutButton = document.getElementById("logout-button");
    if (logoutButton) {
        logoutButton.addEventListener("click", () => {
            deleteCookie("accessToken");
            deleteCookie("refreshToken");
        });
    }

    // 쿠키에서 값을 가져오는 함수
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    }

    // 쿠키를 삭제하는 함수
    function deleteCookie(name) {
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
    }

    // 채팅 버튼 클릭 시 처리
    if (chatButton) {
        chatButton.addEventListener("click", () => {
            window.location.href = "/chat";
        });
    }
});