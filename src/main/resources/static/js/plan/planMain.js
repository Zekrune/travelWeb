document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("travelModal"); // 검색 모달
    const openModalBtn = document.getElementById("openModalBtn"); // 모달 열기 버튼
    const closeModalBtn = document.getElementById("closeModalBtn"); // 모달 닫기 버튼
    const searchInput = document.getElementById("searchInput"); // 검색 입력창
    const searchButton = document.getElementById("searchButton"); // 검색 버튼
    const searchResults = document.getElementById("searchResults"); // 검색 결과 목록

    const locationModal = document.getElementById("locationModal"); // 지역 상세 정보 모달
    const closeLocationModalBtn = document.getElementById("closeLocationModalBtn");
    const locationTitle = document.getElementById("location-title");
    const locationImage = document.getElementById("location-image");
    const locationDescription = document.getElementById("location-description");

    openModalBtn.addEventListener("click", function () {
        modal.style.display = "block";
        searchResults.innerHTML = ""; // 이전 검색 결과 초기화
        searchInput.value = ""; // 검색창 초기화
    });

    closeModalBtn.addEventListener("click", function () {
        modal.style.display = "none";
    });

    closeLocationModalBtn.addEventListener("click", function () {
        locationModal.style.display = "none";
    });

    window.addEventListener("click", function (event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
        if (event.target === locationModal) {
            locationModal.style.display = "none";
        }
    });

    searchButton.addEventListener("click", function () {
        searchLocations();
    });

    searchInput.addEventListener("keypress", function (event) {
        if (event.key === "Enter") {
            searchLocations();
        }
    });

    function searchLocations() {
        const query = searchInput.value.trim();

        if (query.length < 2) {
            alert("두 글자 이상 입력하세요.");
            return;
        }

        fetch(`/locations/search?query=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(data => {
                searchResults.innerHTML = ""; // 기존 목록 초기화
                if (data.length === 0) {
                    searchResults.innerHTML = "<p>검색 결과가 없습니다.</p>";
                    return;
                }
                data.forEach(location => {
                    const resultItem = document.createElement("div");
                    resultItem.classList.add("result-item");
                    resultItem.innerHTML = `
                        <div class="result-info">
                            <span class="city-name">${location.city}</span>
                            <span class="country-name">${location.country}</span>
                        </div>
                    `;

                    resultItem.addEventListener("click", function () {
                        showLocationModal(location);
                    });

                    searchResults.appendChild(resultItem);
                });
            })
            .catch(error => console.error("검색 오류 발생:", error));
    }

    function showLocationModal(location) {
        locationTitle.textContent = location.city;
        locationImage.src = location.imageUrl;
        locationImage.alt = location.city;
        locationDescription.textContent = location.description;

        locationModal.style.display = "block";
    }
});
