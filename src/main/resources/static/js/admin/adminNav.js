// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
    // 참고: 이전에 initializeCharts 함수를 호출했으나 해당 함수는 대시보드 페이지에서만 필요하므로 제거했습니다.
    // 만약 차트 기능이 필요하다면 별도의 JS 파일로 분리하는 것이 좋습니다.
    
    // 드롭다운 메뉴 토글
    document.addEventListener('click', function (event) {
        const dropdown = document.getElementById('dropdownMenu');
        if (!dropdown) return; // 드롭다운 메뉴가 없는 경우 종료
        
        const userMenu = document.querySelector('.user-menu');
        if (!userMenu) return; // 유저 메뉴가 없는 경우 종료

        if (!userMenu.contains(event.target) && dropdown.classList.contains('show')) {
            dropdown.classList.remove('show');
        }
    });
});

// 드롭다운 메뉴 토글 함수
function toggleMenu() {
    const dropdown = document.getElementById('dropdownMenu');
    if (dropdown) {
        dropdown.classList.toggle('show');
    } else {
        console.error("드롭다운 메뉴를 찾을 수 없습니다.");
    }
}