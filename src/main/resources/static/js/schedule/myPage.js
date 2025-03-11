// 탭 전환 기능
document.addEventListener("DOMContentLoaded", function () {
  const tabs = document.querySelectorAll(".tab");
  const tabContents = document.querySelectorAll(".tab-content");

  tabs.forEach((tab) => {
    tab.addEventListener("click", function () {
      // 모든 탭에서 active 클래스 제거
      tabs.forEach((t) => t.classList.remove("active"));
      // 클릭한 탭에 active 클래스 추가
      this.classList.add("active");

      // 모든 탭 컨텐츠 숨기기
      tabContents.forEach((content) => content.classList.remove("active"));

      // 클릭한 탭에 해당하는 컨텐츠 표시
      const targetId = this.id.replace("tab-", "content-");
      document.getElementById(targetId).classList.add("active");
    });
  });

  // 정렬 기능
  const sortNewest = document.getElementById("sort-newest");
  const sortOldest = document.getElementById("sort-oldest");
  const cardsContainer = document.getElementById("travel-cards-container");

  // 최신순 정렬 (기본)
  function sortCardsByNewest() {
    const cards = Array.from(cardsContainer.querySelectorAll(".travel-card"));
    cards.sort((a, b) => {
      const dateA = new Date(a.getAttribute("data-date"));
      const dateB = new Date(b.getAttribute("data-date"));
      return dateB - dateA; // 내림차순 (최신순)
    });

    // 정렬된 카드를 컨테이너에 다시 추가
    cards.forEach((card) => cardsContainer.appendChild(card));

    // 정렬 버튼 활성화 상태 변경
    sortNewest.classList.add("active");
    sortOldest.classList.remove("active");
  }

  // 오래된순 정렬
  function sortCardsByOldest() {
    const cards = Array.from(cardsContainer.querySelectorAll(".travel-card"));
    cards.sort((a, b) => {
      const dateA = new Date(a.getAttribute("data-date"));
      const dateB = new Date(b.getAttribute("data-date"));
      return dateA - dateB; // 오름차순 (오래된순)
    });

    // 정렬된 카드를 컨테이너에 다시 추가
    cards.forEach((card) => cardsContainer.appendChild(card));

    // 정렬 버튼 활성화 상태 변경
    sortNewest.classList.remove("active");
    sortOldest.classList.add("active");
  }

  // 정렬 버튼 이벤트 리스너
  sortNewest.addEventListener("click", sortCardsByNewest);
  sortOldest.addEventListener("click", sortCardsByOldest);

  // 초기 정렬 (최신순)
  sortCardsByNewest();
});

// 네비게이션 메뉴 토글 함수
function toggleMenu() {
  document.getElementById("dropdownMenu").classList.toggle("show");
}

// 다른 곳을 클릭하면 메뉴 닫기
window.onclick = function (event) {
  if (!event.target.matches(".user-icon")) {
    var dropdowns = document.getElementsByClassName("dropdown-menu");
    for (var i = 0; i < dropdowns.length; i++) {
      var openDropdown = dropdowns[i];
      if (openDropdown.classList.contains("show")) {
        openDropdown.classList.remove("show");
      }
    }
  }
};
