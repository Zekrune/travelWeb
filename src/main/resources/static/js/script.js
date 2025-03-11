function toggleMenu() {
    document.getElementById("dropdownMenu").classList.toggle("show");
}

window.onclick = function(event) {
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
