document.addEventListener("DOMContentLoaded", function() {
    const loginBtn = document.getElementById("loginBtn");
    const loginContainer = document.getElementById("loginContainer");
    const closeBtn = document.getElementById("closeBtn");

    // 로그인 버튼 클릭 시 로그인 창 열기
    loginBtn.addEventListener("click", function() {
        loginContainer.classList.add("active");
    });

    // 닫기 버튼 클릭 시 로그인 창 닫기
    closeBtn.addEventListener("click", function() {
        loginContainer.classList.remove("active");
    });
});