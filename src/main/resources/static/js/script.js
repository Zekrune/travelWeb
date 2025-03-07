function toggleMenu() {
    document.getElementById("dropdownMenu").classList.toggle("show");
}

// 클릭 외부 감지하여 메뉴 닫기
window.onclick = function(event) {
    // user-icon이 아닌 곳을 클릭하면 메뉴 닫기
    if (!event.target.matches('.user-icon')) {
        let dropdowns = document.getElementsByClassName("dropdown-menu");
        for (let i = 0; i < dropdowns.length; i++) {
            let openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
    }
};
