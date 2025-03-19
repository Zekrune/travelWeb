// 드롭다운 메뉴 토글 함수
function toggleMenu() {
  const dropdownMenu = document.getElementById("dropdownMenu");
  dropdownMenu.style.display =
    dropdownMenu.style.display === "block" ? "none" : "block";
}

document.addEventListener("DOMContentLoaded", function () {
  // 현재 페이지에 해당하는 네비게이션 링크에 active 클래스 추가
  const currentPath = window.location.pathname;
  const navLinks = document.querySelectorAll(".nav-item");

  navLinks.forEach((link) => {
    const href = link.getAttribute("href");
    if (href && currentPath.includes(href.replace("@{", "").replace("}", ""))) {
      link.classList.add("active");
    }
  });

  // 드롭다운 메뉴 처리
  const userIcon = document.querySelector(".user-icon");
  const dropdownMenu = document.getElementById("dropdownMenu");
  const notificationIcon = document.querySelector(".notification-icon");
  const notificationDropdown = document.getElementById("notificationDropdown");

  if (userIcon && dropdownMenu) {
    let hideTimeout;

    userIcon.addEventListener("mouseenter", function () {
      clearTimeout(hideTimeout); // 숨기는 타이머 제거
      dropdownMenu.style.display = "block"; // 드롭다운 표시
    });

    userIcon.addEventListener("mouseleave", function () {
      hideTimeout = setTimeout(function () {
        if (!dropdownMenu.matches(":hover")) {
          dropdownMenu.style.display = "none";
        }
      }, 300); // 0.3초의 지연 후 숨김 처리
    });

    dropdownMenu.addEventListener("mouseenter", function () {
      clearTimeout(hideTimeout); // 마우스가 메뉴 안으로 들어오면 숨김 방지
    });

    dropdownMenu.addEventListener("mouseleave", function () {
      hideTimeout = setTimeout(function () {
        dropdownMenu.style.display = "none";
      }, 300); // 0.3초 후 숨김 처리
    });
  }

  // 알림 메뉴 처리 - 클릭 방식으로 변경
  if (notificationIcon && notificationDropdown) {
    // 클릭 이벤트 리스너 추가
    notificationIcon.addEventListener("click", function (e) {
      e.preventDefault(); // 기본 동작 방지
      toggleNotifications(); // 알림 토글 함수 호출
    });
  }

  // 알림 삭제 버튼 처리
  const dismissButtons = document.querySelectorAll(".notification-dismiss");
  dismissButtons.forEach((button) => {
    button.addEventListener("click", function (e) {
      e.preventDefault();
      const notificationItem = this.closest(".notification-item");
      notificationItem.style.height = "0";
      notificationItem.style.padding = "0";
      notificationItem.style.opacity = "0";

      setTimeout(() => {
        notificationItem.remove();

        // 알림이 없을 경우 빈 메시지 표시
        const notificationItems =
          document.querySelectorAll(".notification-item");
        if (notificationItems.length === 0) {
          const notificationItemsContainer = document.querySelector(
            ".notification-items"
          );
          if (notificationItemsContainer) {
            notificationItemsContainer.innerHTML = `
              <div class="empty-notifications">
                <p>새로운 알림이 없습니다</p>
              </div>
            `;
          }
        }
      }, 300);
    });
  });

  // 모두 읽음 표시 버튼 처리
  const markAllReadBtn = document.querySelector(".mark-all-read");
  if (markAllReadBtn) {
    markAllReadBtn.addEventListener("click", function (e) {
      e.preventDefault();
      const notificationItems = document.querySelectorAll(".notification-item");

      if (notificationItems.length > 0) {
        const notificationItemsContainer = document.querySelector(
          ".notification-items"
        );
        notificationItemsContainer.innerHTML = `
          <div class="empty-notifications">
            <p>새로운 알림이 없습니다</p>
          </div>
        `;

        // 알림 배지 제거
        const notificationBadge = document.querySelector(".notification-badge");
        if (notificationBadge) {
          notificationBadge.style.display = "none";
        }
      }
    });
  }

  // 드롭다운 메뉴 외부 클릭 시 닫기
  document.addEventListener("click", function (event) {
    if (
      userIcon &&
      dropdownMenu &&
      !userIcon.contains(event.target) &&
      !dropdownMenu.contains(event.target)
    ) {
      dropdownMenu.style.display = "none";
    }

    if (
      notificationIcon &&
      notificationDropdown &&
      !notificationIcon.contains(event.target) &&
      !notificationDropdown.contains(event.target)
    ) {
      notificationDropdown.style.display = "none";
    }
  });
});

// 드롭다운 메뉴 토글 함수
function toggleMenu() {
  const dropdownMenu = document.getElementById("dropdownMenu");
  dropdownMenu.style.display =
    dropdownMenu.style.display === "block" ? "none" : "block";
}

// 알림 메뉴 토글 함수
function toggleNotifications() {
  const notificationDropdown = document.getElementById("notificationDropdown");
  notificationDropdown.style.display =
    notificationDropdown.style.display === "block" ? "none" : "block";
}
