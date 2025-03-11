document.addEventListener("DOMContentLoaded", function () {
  const itineraryContainer = document.getElementById("itineraryContainer");
  const scheduleJsonElement = document.getElementById("scheduleJsonData");

  // AJAX 대신 hidden div에서 데이터 가져오기 (실제 AJAX 구현 시 이 부분을 변경)
  fetchItineraryData();

  /**
   * 일정 데이터 가져오기 (AJAX 시뮬레이션)
   */
  function fetchItineraryData() {
    // 로딩 스피너 표시
    itineraryContainer.innerHTML = `
                    <div class="loading-spinner">
                        <i class="fas fa-spinner"></i>
                    </div>
                `;

    // 실제 AJAX 요청 대신 setTimeout으로 지연 시뮬레이션
    setTimeout(() => {
      try {
        const jsonData = scheduleJsonElement.textContent;
        const itineraryData = JSON.parse(jsonData);
        console.log("Loaded itinerary data:", itineraryData);

        // 일정 표시
        renderItinerary(itineraryData);
      } catch (error) {
        console.error("Failed to parse itinerary JSON:", error);
        showErrorMessage("일정 데이터를 불러오는데 실패했습니다.");
      }
    }, 500); // 0.5초 지연 (로딩 효과를 위해)
  }

  /**
   * 일정 데이터를 화면에 표시
   */
  function renderItinerary(data) {
    // 컨테이너 초기화
    itineraryContainer.innerHTML = "";

    // 데이터 형식 확인 및 변환
    let days = [];

    // GPT가 생성한 JSON 형식 처리 (travelPlan 객체 내부에 itinerary 배열이 있는 경우)
    if (data.travelPlan && data.travelPlan.itinerary) {
      days = data.travelPlan.itinerary;
    } else if (data.days) {
      days = data.days;
    } else {
      showErrorMessage("지원되지 않는 일정 데이터 형식입니다.");
      return;
    }

    // 일정이 없는 경우
    if (!days || days.length === 0) {
      showErrorMessage("등록된 일정이 없습니다.");
      return;
    }

    // 각 날짜별 일정 표시
    days.forEach((day, dayIndex) => {
      const dayElement = createDayElement(day, dayIndex);
      itineraryContainer.appendChild(dayElement);
    });
  }

  /**
   * 날짜 요소 생성
   */
  function createDayElement(day, dayIndex) {
    const dayContainer = document.createElement("div");
    dayContainer.className = "day-container";

    // 날짜 헤더
    const dayHeader = document.createElement("div");
    dayHeader.className = "day-header";

    const dayTitle = document.createElement("h3");
    dayTitle.className = "day-title";
    dayTitle.innerHTML = `Day ${dayIndex + 1} <span class="day-date">${
      day.date || "날짜 미지정"
    }</span>`;

    // 날씨와 총 비용 정보
    const dayInfo = document.createElement("div");
    dayInfo.className = "day-info";

    if (day.weather) {
      const weatherIcon = getWeatherIcon(day.weather);
      dayInfo.innerHTML += `
                        <div class="day-weather">
                            <i class="${weatherIcon}"></i> ${day.weather}
                        </div>
                    `;
    }

    if (day.totalCost) {
      dayInfo.innerHTML += `
                        <div class="day-cost">
                            <i class="fas fa-coins"></i> ${day.totalCost}
                        </div>
                    `;
    }

    dayHeader.appendChild(dayTitle);
    dayHeader.appendChild(dayInfo);
    dayContainer.appendChild(dayHeader);

    // 일정 컨테이너
    const scheduleContainer = document.createElement("div");
    scheduleContainer.className = "schedule-container";

    // 각 일정 항목 추가
    const schedules = day.schedule || day.schedules || [];

    if (schedules.length > 0) {
      schedules.forEach((schedule) => {
        const scheduleElement = createScheduleElement(schedule);
        scheduleContainer.appendChild(scheduleElement);
      });
    } else {
      // 일정이 없는 경우 메시지 표시
      const emptySchedule = document.createElement("div");
      emptySchedule.className = "empty-schedule";
      emptySchedule.textContent = "이 날의 일정이 없습니다.";
      scheduleContainer.appendChild(emptySchedule);
    }

    dayContainer.appendChild(scheduleContainer);
    return dayContainer;
  }

  /**
   * 일정 항목 요소 생성
   */
  function createScheduleElement(schedule) {
    const scheduleItem = document.createElement("div");
    scheduleItem.className = "schedule-item";

    // 시간
    const scheduleTime = document.createElement("div");
    scheduleTime.className = "schedule-time";
    scheduleTime.textContent =
      schedule.departureTime || schedule.time || "시간 미지정";

    // 내용
    const scheduleContent = document.createElement("div");
    scheduleContent.className = "schedule-content";

    const locationText = document.createElement("div");
    locationText.className = "schedule-location";
    locationText.textContent =
      schedule.location || schedule.content || "내용 미지정";

    // 카테고리 배지
    if (schedule.category) {
      const categoryBadge = document.createElement("span");
      categoryBadge.className = "category-badge";
      categoryBadge.textContent = schedule.category;
      locationText.appendChild(categoryBadge);
    }

    scheduleContent.appendChild(locationText);

    // 상세 정보 (소요 시간, 비용)
    const scheduleDetails = document.createElement("div");
    scheduleDetails.className = "schedule-details";

    if (schedule.duration) {
      const durationInfo = document.createElement("div");
      durationInfo.className = "duration-info";
      durationInfo.innerHTML = `<i class="fas fa-clock"></i> ${schedule.duration}`;
      scheduleDetails.appendChild(durationInfo);
    }

    if (schedule.cost) {
      const costInfo = document.createElement("div");
      costInfo.className = "cost-info";
      costInfo.innerHTML = `<i class="fas fa-won-sign"></i> ${schedule.cost}`;
      scheduleDetails.appendChild(costInfo);
    }

    if (scheduleDetails.children.length > 0) {
      scheduleContent.appendChild(scheduleDetails);
    }

    scheduleItem.appendChild(scheduleTime);
    scheduleItem.appendChild(scheduleContent);

    return scheduleItem;
  }

  /**
   * 날씨에 따른 아이콘 반환
   */
  function getWeatherIcon(weather) {
    switch (weather) {
      case "맑음":
        return "fas fa-sun";
      case "흐림":
        return "fas fa-cloud";
      case "비":
        return "fas fa-cloud-rain";
      case "눈":
        return "fas fa-snowflake";
      default:
        return "fas fa-cloud-sun";
    }
  }

  /**
   * 오류 메시지 표시
   */
  function showErrorMessage(message) {
    itineraryContainer.innerHTML = `
                    <div class="error-message">
                        <i class="fas fa-exclamation-circle"></i>
                        <p>${message}</p>
                    </div>
                `;
  }
});
