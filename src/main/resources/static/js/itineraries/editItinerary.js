document.addEventListener("DOMContentLoaded", function () {
  const itineraryArea = document.getElementById("itineraryArea");
  let itineraryData = { days: [] };

  // Parse the JSON itinerary; initialize if invalid
  try {
    const rawData = JSON.parse(itineraryArea.value);

    // GPT가 생성한 JSON 형식 처리 (travelPlan 객체 내부에 itinerary 배열이 있는 경우)
    if (rawData.travelPlan && rawData.travelPlan.itinerary) {
      // 일정 데이터를 days 배열로 변환
      itineraryData.days = rawData.travelPlan.itinerary.map((day) => {
        return {
          date: day.date,
          weather: day.weather,
          totalCost: day.totalCost,
          schedules: day.schedule.map((item) => {
            return {
              departureTime: item.departureTime,
              category: item.category,
              location: item.location,
              duration: item.duration,
              cost: item.cost,
            };
          }),
        };
      });

      // 기존 데이터 보존
      itineraryData.departLocation = rawData.travelPlan.departLocation;
      itineraryData.destination = rawData.travelPlan.destination;
      itineraryData.startDate = rawData.travelPlan.startDate;
      itineraryData.endDate = rawData.travelPlan.endDate;
      itineraryData.transportation = rawData.travelPlan.transportation;
      itineraryData.purpose = rawData.travelPlan.purpose;
      itineraryData.budget = rawData.travelPlan.budget;
      itineraryData.density = rawData.travelPlan.density;
      itineraryData.hotelLocation = rawData.travelPlan.hotelLocation;
      itineraryData.travelMode = rawData.travelPlan.travelMode;
    } else if (rawData.days) {
      // 이미 days 형식으로 저장된 경우
      itineraryData = rawData;
    } else {
      console.warn("지원되지 않는 JSON 형식입니다. 기본 구조로 초기화합니다.");
    }
  } catch (e) {
    console.error("유효하지 않은 itinerary JSON입니다:", e);
  }

  const itineraryContainer = document.getElementById("itineraryContainer");
  const editForm = document.getElementById("editForm");
  const modal = document.getElementById("confirmModal");
  const btnOpenModal = document.getElementById("btnOpenModal");
  const btnCloseModal = document.getElementById("btnCloseModal");
  const btnCancelModal = document.getElementById("btnCancelModal");
  const btnSubmitModal = document.getElementById("btnSubmitModal");
  const btnSelectPhoto = document.getElementById("btnSelectPhoto");
  const planPhotoUpload = document.getElementById("planPhotoUpload");
  const photoPreview = document.getElementById("photoPreview");

  // 전역 변수로 모달 상태 관리
  let activeModal = null;

  // 사진 선택 버튼 이벤트
  btnSelectPhoto.addEventListener("click", function () {
    planPhotoUpload.click();
  });

  // 사진 업로드 이벤트
  planPhotoUpload.addEventListener("change", handlePhotoUpload);

  // Render all days and their schedules
  function renderItinerary() {
    itineraryContainer.innerHTML = "";

    if (itineraryData.days.length === 0) {
      const emptyMessage = document.createElement("div");
      emptyMessage.className = "empty-message";
      emptyMessage.textContent = "일정이 없습니다. 새로운 일정을 추가해보세요.";
      itineraryContainer.appendChild(emptyMessage);
      return;
    }

    // 여행 요약 정보 표시
    if (itineraryData.departLocation && itineraryData.destination) {
      const summaryElement = document.createElement("div");
      summaryElement.className = "itinerary-summary";
      summaryElement.innerHTML = `
        <div class="summary-header">
          <h3>여행 요약</h3>
        </div>
        <div class="summary-content">
          <div class="summary-item">
            <span class="label">출발지:</span>
            <span class="value">${itineraryData.departLocation || "-"}</span>
          </div>
          <div class="summary-item">
            <span class="label">목적지:</span>
            <span class="value">${itineraryData.destination || "-"}</span>
          </div>
          <div class="summary-item">
            <span class="label">기간:</span>
            <span class="value">${itineraryData.startDate || "-"} ~ ${
        itineraryData.endDate || "-"
      }</span>
          </div>
          <div class="summary-item">
            <span class="label">교통수단:</span>
            <span class="value">${itineraryData.transportation || "-"}</span>
          </div>
          <div class="summary-item">
            <span class="label">숙소:</span>
            <span class="value">${itineraryData.hotelLocation || "-"}</span>
          </div>
        </div>
      `;
      itineraryContainer.appendChild(summaryElement);
    }

    // 각 날짜별 일정 표시
    itineraryData.days.forEach((day, dayIndex) => {
      const dayElement = createDayElement(day, dayIndex);
      itineraryContainer.appendChild(dayElement);
    });

    // 새 날짜 추가 버튼
    const addDayBtn = document.createElement("button");
    addDayBtn.className = "btn";
    addDayBtn.innerHTML = '<i class="fas fa-plus"></i> 새 날짜 추가';
    addDayBtn.addEventListener("click", () => addNewDay());
    itineraryContainer.appendChild(addDayBtn);

    // 드래그 앤 드롭 초기화
    initializeDragAndDrop();
  }

  // 날짜 요소 생성
  function createDayElement(day, dayIndex) {
    const template = document.getElementById("dayTemplate");
    const dayElement = template.content
      .cloneNode(true)
      .querySelector(".day-container");

    // 데이터 속성 설정
    dayElement.dataset.index = dayIndex;

    // 날짜 정보 설정
    const dayNumber = dayElement.querySelector(".day-number");
    const dayDate = dayElement.querySelector(".day-date");
    dayNumber.textContent = dayIndex + 1;
    dayDate.textContent = day.date || "날짜 미지정";

    // 날씨와 총 비용 정보 추가
    if (day.weather || day.totalCost) {
      const dayInfo = document.createElement("div");
      dayInfo.className = "day-info";
      dayInfo.innerHTML = `
        ${
          day.weather
            ? `<span class="day-weather"><i class="fas fa-cloud-sun"></i> ${day.weather}</span>`
            : ""
        }
        ${
          day.totalCost
            ? `<span class="day-cost"><i class="fas fa-coins"></i> ${day.totalCost}</span>`
            : ""
        }
      `;
      dayElement.querySelector(".day-header").appendChild(dayInfo);
    }

    // 일정 컨테이너
    const scheduleContainer = dayElement.querySelector(".schedule-container");

    // 각 일정 항목 추가
    if (day.schedules && day.schedules.length > 0) {
      day.schedules.forEach((schedule, scheduleIndex) => {
        const scheduleElement = createScheduleElement(
          schedule,
          dayIndex,
          scheduleIndex
        );
        scheduleContainer.appendChild(scheduleElement);
      });
    } else {
      const emptySchedule = document.createElement("div");
      emptySchedule.className = "empty-schedule";
      emptySchedule.textContent = "이 날의 일정이 없습니다.";
      scheduleContainer.appendChild(emptySchedule);
    }

    // 일정 추가 버튼 추가
    const addScheduleBtn = createAddScheduleButton(dayIndex);
    scheduleContainer.appendChild(addScheduleBtn);

    // 이벤트 리스너 추가
    const deleteDayBtn = dayElement.querySelector(".delete-day");

    // 날짜 복사 버튼 추가
    const copyDayBtn = document.createElement("button");
    copyDayBtn.type = "button";
    copyDayBtn.className = "btn-icon copy-day";
    copyDayBtn.innerHTML = '<i class="fas fa-copy"></i>';
    copyDayBtn.title = "날짜 복사";

    copyDayBtn.addEventListener("click", () => copyDay(dayIndex));

    // 날짜 편집 버튼 추가
    const editDayBtn = document.createElement("button");
    editDayBtn.type = "button";
    editDayBtn.className = "btn-icon edit-day";
    editDayBtn.innerHTML = '<i class="fas fa-edit"></i>';
    editDayBtn.title = "날짜 정보 편집";

    editDayBtn.addEventListener("click", () => showDayEditModal(dayIndex));

    // 버튼 컨테이너에 버튼 추가
    const actionsContainer = dayElement.querySelector(".day-actions");
    actionsContainer.insertBefore(copyDayBtn, deleteDayBtn);
    actionsContainer.insertBefore(editDayBtn, copyDayBtn);

    deleteDayBtn.addEventListener("click", () => deleteDay(dayIndex));

    return dayElement;
  }

  // 일정 항목 요소 생성
  function createScheduleElement(schedule, dayIndex, scheduleIndex) {
    const template = document.getElementById("scheduleItemTemplate");
    const scheduleElement = template.content
      .cloneNode(true)
      .querySelector(".schedule-item");

    // 데이터 속성 설정
    scheduleElement.dataset.dayIndex = dayIndex;
    scheduleElement.dataset.scheduleIndex = scheduleIndex;

    // 시간 및 내용 설정
    const timeInput = scheduleElement.querySelector(".time-input");
    const contentInput = scheduleElement.querySelector(".content-input");

    timeInput.value = schedule.departureTime || "09:00";
    contentInput.value = schedule.location || "";

    // 추가 정보 컨테이너 생성
    const scheduleDetails = document.createElement("div");
    scheduleDetails.className = "schedule-details";

    // 카테고리 선택 드롭다운
    const categoryGroup = document.createElement("div");
    categoryGroup.className = "detail-group";

    const categoryLabel = document.createElement("label");
    categoryLabel.textContent = "카테고리:";

    const categorySelect = document.createElement("select");
    categorySelect.className = "category-select";

    const categories = ["관람", "음식", "쇼핑", "휴식", "체험", "이동", "기타"];
    categories.forEach((cat) => {
      const option = document.createElement("option");
      option.value = cat;
      option.textContent = cat;
      if (schedule.category === cat) {
        option.selected = true;
      }
      categorySelect.appendChild(option);
    });

    categorySelect.addEventListener("change", () => {
      updateScheduleData(
        dayIndex,
        scheduleIndex,
        "category",
        categorySelect.value
      );

      // 카테고리 배지 업데이트
      const categoryBadge = scheduleElement.querySelector(".category-badge");
      if (categoryBadge) {
        categoryBadge.textContent = categorySelect.value;
      }
    });

    categoryGroup.appendChild(categoryLabel);
    categoryGroup.appendChild(categorySelect);
    scheduleDetails.appendChild(categoryGroup);

    // 소요 시간 입력
    const durationGroup = document.createElement("div");
    durationGroup.className = "detail-group";

    const durationLabel = document.createElement("label");
    durationLabel.textContent = "소요 시간:";

    const durationInput = document.createElement("input");
    durationInput.type = "text";
    durationInput.className = "duration-input";
    durationInput.value = schedule.duration || "1시간";

    durationInput.addEventListener("change", () => {
      updateScheduleData(
        dayIndex,
        scheduleIndex,
        "duration",
        durationInput.value
      );

      // 소요 시간 정보 업데이트
      const durationInfo = scheduleElement.querySelector(".duration-info");
      if (durationInfo) {
        durationInfo.innerHTML = `<i class="fas fa-clock"></i> ${durationInput.value}`;
      }
    });

    durationGroup.appendChild(durationLabel);
    durationGroup.appendChild(durationInput);
    scheduleDetails.appendChild(durationGroup);

    // 비용 입력
    const costGroup = document.createElement("div");
    costGroup.className = "detail-group";

    const costLabel = document.createElement("label");
    costLabel.textContent = "비용:";

    const costInput = document.createElement("input");
    costInput.type = "text";
    costInput.className = "cost-input";
    costInput.value = schedule.cost || "0원";

    costInput.addEventListener("change", () => {
      updateScheduleData(dayIndex, scheduleIndex, "cost", costInput.value);

      // 비용 정보 업데이트
      const costInfo = scheduleElement.querySelector(".cost-info");
      if (costInfo) {
        costInfo.innerHTML = `<i class="fas fa-won-sign"></i> ${costInput.value}`;
      }
    });

    costGroup.appendChild(costLabel);
    costGroup.appendChild(costInput);
    scheduleDetails.appendChild(costGroup);

    // 상세 정보 토글 버튼
    const detailsToggle = document.createElement("button");
    detailsToggle.type = "button";
    detailsToggle.className = "btn-icon details-toggle";
    detailsToggle.innerHTML = '<i class="fas fa-cog"></i>';
    detailsToggle.title = "상세 정보 편집";

    detailsToggle.addEventListener("click", function () {
      const detailsSection = scheduleElement.querySelector(".schedule-details");
      if (
        detailsSection.style.display === "none" ||
        !detailsSection.style.display
      ) {
        detailsSection.style.display = "flex";
        this.innerHTML = '<i class="fas fa-times"></i>';
        this.title = "상세 정보 닫기";
      } else {
        detailsSection.style.display = "none";
        this.innerHTML = '<i class="fas fa-cog"></i>';
        this.title = "상세 정보 편집";
      }
    });

    // 상세 정보 섹션 기본적으로 숨김
    scheduleDetails.style.display = "none";

    // 일정 복사 버튼 추가
    const copyBtn = document.createElement("button");
    copyBtn.type = "button";
    copyBtn.className = "btn-icon copy-schedule";
    copyBtn.innerHTML = '<i class="fas fa-copy"></i>';
    copyBtn.title = "일정 복사";

    copyBtn.addEventListener("click", () => {
      copySchedule(dayIndex, scheduleIndex);
    });

    // 입력 변경 이벤트
    timeInput.addEventListener("change", () =>
      updateScheduleData(
        dayIndex,
        scheduleIndex,
        "departureTime",
        timeInput.value
      )
    );
    contentInput.addEventListener("change", () =>
      updateScheduleData(
        dayIndex,
        scheduleIndex,
        "location",
        contentInput.value
      )
    );

    // 삭제 버튼 이벤트
    const deleteBtn = scheduleElement.querySelector(".delete-schedule");
    deleteBtn.addEventListener("click", () =>
      deleteSchedule(dayIndex, scheduleIndex)
    );

    // 버튼 컨테이너에 버튼 추가
    const actionsContainer = scheduleElement.querySelector(".schedule-actions");
    actionsContainer.insertBefore(copyBtn, deleteBtn);
    actionsContainer.insertBefore(detailsToggle, copyBtn);

    // 상세 정보 섹션 추가
    scheduleElement.appendChild(scheduleDetails);

    return scheduleElement;
  }

  // Update hidden textarea with current itinerary JSON
  function updateItineraryJson() {
    // 원본 형식 유지를 위해 travelPlan 구조로 다시 변환
    const outputData = {
      travelPlan: {
        departLocation: itineraryData.departLocation,
        startDate: itineraryData.startDate,
        endDate: itineraryData.endDate,
        transportation: itineraryData.transportation,
        destination: itineraryData.destination,
        purpose: itineraryData.purpose,
        budget: itineraryData.budget,
        density: itineraryData.density,
        hotelLocation: itineraryData.hotelLocation,
        travelMode: itineraryData.travelMode,
        itinerary: itineraryData.days.map((day) => {
          return {
            date: day.date,
            weather: day.weather,
            totalCost: day.totalCost,
            schedule: day.schedules.map((schedule) => {
              return {
                departureTime: schedule.departureTime,
                category: schedule.category,
                location: schedule.location,
                duration: schedule.duration,
                cost: schedule.cost,
              };
            }),
          };
        }),
      },
    };

    itineraryArea.value = JSON.stringify(outputData);
    console.log("Updated itinerary data:", outputData);
  }

  // Initialize Sortable for day reordering
  function initializeDragAndDrop() {
    // 각 날짜의 일정 컨테이너에 Sortable 적용
    document
      .querySelectorAll(".schedule-container")
      .forEach((container, dayIndex) => {
        new Sortable(container, {
          animation: 150,
          ghostClass: "dragging",
          onEnd: function (evt) {
            const fromIndex = evt.oldIndex;
            const toIndex = evt.newIndex;

            if (fromIndex !== toIndex) {
              // 일정 순서 변경
              const day = itineraryData.days[dayIndex];
              const movedSchedule = day.schedules.splice(fromIndex, 1)[0];
              day.schedules.splice(toIndex, 0, movedSchedule);

              // 데이터 속성 업데이트
              updateScheduleIndices(container);

              // 변경사항 저장
              updateItineraryJson();
            }
          },
        });
      });

    // 날짜 컨테이너에 Sortable 적용
    new Sortable(itineraryContainer, {
      animation: 150,
      ghostClass: "dragging",
      handle: ".day-header",
      filter: ".btn, .itinerary-summary", // 버튼과 요약 정보는 드래그 대상에서 제외
      onEnd: function (evt) {
        // 버튼이나 요약 정보면 무시
        if (
          evt.item.classList.contains("btn") ||
          evt.item.classList.contains("itinerary-summary")
        ) {
          return;
        }

        const fromIndex = evt.oldIndex;
        const toIndex = evt.newIndex;

        if (fromIndex !== toIndex) {
          // 날짜 순서 변경
          const movedDay = itineraryData.days.splice(fromIndex, 1)[0];
          itineraryData.days.splice(toIndex, 0, movedDay);

          // 변경사항 저장
          updateItineraryJson();

          // 날짜 번호 업데이트
          updateDayNumbers();
        }
      },
    });
  }

  // 날짜 번호 업데이트
  function updateDayNumbers() {
    const dayContainers = itineraryContainer.querySelectorAll(".day-container");
    dayContainers.forEach((container, index) => {
      const dayNumber = container.querySelector(".day-number");
      if (dayNumber) {
        dayNumber.textContent = index + 1;
      }
    });
  }

  // 일정 인덱스 업데이트
  function updateScheduleIndices(container) {
    const scheduleItems = container.querySelectorAll(".schedule-item");
    scheduleItems.forEach((item, index) => {
      item.dataset.scheduleIndex = index;
    });
  }

  // 새 날짜 추가
  function addNewDay() {
    // 새 날짜 객체 생성
    const today = new Date();
    const dateString = today.toISOString().split("T")[0]; // YYYY-MM-DD 형식

    const newDay = {
      date: dateString,
      weather: "맑음",
      totalCost: "0원",
      schedules: [],
    };

    // 데이터에 추가
    itineraryData.days.push(newDay);

    // UI 갱신
    renderItinerary();

    // 변경사항 저장
    updateItineraryJson();
  }

  // 날짜 삭제
  function deleteDay(dayIndex) {
    if (confirm("이 날짜의 모든 일정이 삭제됩니다. 계속하시겠습니까?")) {
      // 데이터에서 삭제
      itineraryData.days.splice(dayIndex, 1);

      // UI 갱신
      renderItinerary();

      // 변경사항 저장
      updateItineraryJson();
    }
  }

  // 새 일정 추가
  function addNewSchedule(dayIndex) {
    // 새 일정 객체 생성
    const newSchedule = {
      departureTime: "09:00",
      category: "일정",
      location: "",
      duration: "1시간",
      cost: "0원",
    };

    // 데이터에 추가
    if (!itineraryData.days[dayIndex].schedules) {
      itineraryData.days[dayIndex].schedules = [];
    }

    const scheduleIndex = itineraryData.days[dayIndex].schedules.length;
    itineraryData.days[dayIndex].schedules.push(newSchedule);

    // UI에 추가
    const dayContainer = document.querySelector(
      `.day-container[data-index="${dayIndex}"]`
    );
    if (!dayContainer) return;

    const scheduleContainer = dayContainer.querySelector(".schedule-container");

    // 빈 메시지 제거
    const emptyMessage = scheduleContainer.querySelector(".empty-schedule");
    if (emptyMessage) {
      scheduleContainer.removeChild(emptyMessage);
    }

    const scheduleElement = createScheduleElement(
      newSchedule,
      dayIndex,
      scheduleIndex
    );
    scheduleContainer.appendChild(scheduleElement);

    // 변경사항 저장
    updateItineraryJson();
  }

  // 일정 삭제
  function deleteSchedule(dayIndex, scheduleIndex) {
    if (confirm("이 일정을 삭제하시겠습니까?")) {
      // 데이터에서 삭제
      itineraryData.days[dayIndex].schedules.splice(scheduleIndex, 1);

      // UI에서 삭제
      const dayContainer = document.querySelector(
        `.day-container[data-index="${dayIndex}"]`
      );
      if (!dayContainer) return;

      const scheduleContainer = dayContainer.querySelector(
        ".schedule-container"
      );
      const scheduleItems =
        scheduleContainer.querySelectorAll(".schedule-item");

      if (scheduleItems.length > 0 && scheduleIndex < scheduleItems.length) {
        scheduleContainer.removeChild(scheduleItems[scheduleIndex]);

        // 남은 일정 인덱스 업데이트
        updateScheduleIndices(scheduleContainer);

        // 일정이 없는 경우 메시지 표시
        if (itineraryData.days[dayIndex].schedules.length === 0) {
          const emptySchedule = document.createElement("div");
          emptySchedule.className = "empty-schedule";
          emptySchedule.textContent = "이 날의 일정이 없습니다.";
          scheduleContainer.appendChild(emptySchedule);
        }
      }

      // 변경사항 저장
      updateItineraryJson();
    }
  }

  // 일정 데이터 업데이트
  function updateScheduleData(dayIndex, scheduleIndex, field, value) {
    if (
      itineraryData.days[dayIndex] &&
      itineraryData.days[dayIndex].schedules[scheduleIndex]
    ) {
      itineraryData.days[dayIndex].schedules[scheduleIndex][field] = value;
      updateItineraryJson();
    }
  }

  // 사진 업로드 처리
  function handlePhotoUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    // 파일 크기 제한 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert("파일 크기는 5MB 이하여야 합니다.");
      return;
    }

    // 이미지 파일 확인
    if (!file.type.startsWith("image/")) {
      alert("이미지 파일만 업로드 가능합니다.");
      return;
    }

    // 파일 미리보기
    const reader = new FileReader();
    reader.onload = function (e) {
      photoPreview.style.backgroundImage = `url(${e.target.result})`;
      photoPreview.classList.add("has-image");
      photoPreview.innerHTML = ""; // 기존 내용 제거

      // 히든 필드에 Base64 이미지 데이터 저장
      document.getElementById("planPhoto").value = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  // 모달 표시 함수
  function showModal(modalId, modalContent, onSave) {
    // 이미 열린 모달이 있으면 닫기
    if (activeModal) {
      closeModal(activeModal);
    }

    // 기존 모달이 있으면 제거
    let existingModal = document.getElementById(modalId);
    if (existingModal) {
      document.body.removeChild(existingModal);
    }

    // 새 모달 생성
    const modal = document.createElement("div");
    modal.id = modalId;
    modal.className = "modal";
    modal.innerHTML = modalContent;

    // body에 모달 추가
    document.body.appendChild(modal);

    // 모달 표시
    setTimeout(() => {
      modal.style.display = "block";
    }, 10);

    // 현재 활성 모달 설정
    activeModal = modal;

    // 모달 닫기 함수
    function closeCurrentModal() {
      modal.style.display = "none";
      setTimeout(() => {
        if (modal.parentNode) {
          document.body.removeChild(modal);
        }
        if (activeModal === modal) {
          activeModal = null;
        }
      }, 300);
    }

    // 이벤트 리스너 추가
    const closeBtn = modal.querySelector(".close-btn");
    const cancelBtn = modal.querySelector(".back-btn");
    const saveBtn = modal.querySelector(".submit-btn");

    if (closeBtn) {
      closeBtn.addEventListener("click", closeCurrentModal);
    }

    if (cancelBtn) {
      cancelBtn.addEventListener("click", closeCurrentModal);
    }

    if (saveBtn && onSave) {
      saveBtn.addEventListener("click", () => {
        const result = onSave();
        if (result !== false) {
          closeCurrentModal();
        }
      });
    }

    // 모달 외부 클릭 시 닫기
    modal.addEventListener("click", function (event) {
      if (event.target === modal) {
        closeCurrentModal();
      }
    });

    // 모달 내부 클릭은 전파 중지
    const modalContent = modal.querySelector(".modal-content");
    if (modalContent) {
      modalContent.addEventListener("click", function (event) {
        event.stopPropagation();
      });
    }

    // ESC 키 누르면 모달 닫기
    const escKeyHandler = function (event) {
      if (event.key === "Escape" && activeModal === modal) {
        closeCurrentModal();
        document.removeEventListener("keydown", escKeyHandler);
      }
    };
    document.addEventListener("keydown", escKeyHandler);

    return { modal, close: closeCurrentModal };
  }

  // 모달 닫기 함수
  function closeModal(modal) {
    if (!modal) return;

    modal.style.display = "none";
    setTimeout(() => {
      if (modal.parentNode) {
        document.body.removeChild(modal);
      }
      if (activeModal === modal) {
        activeModal = null;
      }
    }, 300);
  }

  // 일정 복사 기능
  function copySchedule(dayIndex, scheduleIndex) {
    // 복사할 일정 가져오기
    const originalSchedule =
      itineraryData.days[dayIndex].schedules[scheduleIndex];

    // 깊은 복사를 위해 JSON 변환 사용
    const newSchedule = JSON.parse(JSON.stringify(originalSchedule));

    // 새 일정 추가
    itineraryData.days[dayIndex].schedules.push(newSchedule);

    // UI 갱신
    renderItinerary();

    // 변경사항 저장
    updateItineraryJson();

    // 성공 메시지
    alert("일정이 복사되었습니다.");
  }

  // 날짜 복사 기능 추가
  function copyDay(dayIndex) {
    // 복사할 날짜 가져오기
    const originalDay = itineraryData.days[dayIndex];

    // 깊은 복사를 위해 JSON 변환 사용
    const newDay = JSON.parse(JSON.stringify(originalDay));

    // 새 날짜 추가
    itineraryData.days.push(newDay);

    // UI 갱신
    renderItinerary();

    // 변경사항 저장
    updateItineraryJson();

    // 성공 메시지
    alert("날짜가 복사되었습니다.");
  }

  // 일정 추가 모달 표시 함수
  function showAddScheduleModal(dayIndex) {
    const modalContent = `
      <div class="modal-content">
        <div class="modal-header">
          <h2><i class="fas fa-plus-circle"></i> 새 일정 추가</h2>
          <span class="close-btn">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="scheduleTime">시간</label>
            <input type="time" id="scheduleTime" value="09:00">
          </div>
          <div class="form-group">
            <label for="scheduleLocation">장소</label>
            <input type="text" id="scheduleLocation" placeholder="방문할 장소를 입력하세요">
          </div>
          <div class="form-group">
            <label for="scheduleCategory">카테고리</label>
            <select id="scheduleCategory">
              <option value="관람">관람</option>
              <option value="음식">음식</option>
              <option value="쇼핑">쇼핑</option>
              <option value="휴식">휴식</option>
              <option value="체험">체험</option>
              <option value="이동">이동</option>
              <option value="기타">기타</option>
            </select>
          </div>
          <div class="form-group">
            <label for="scheduleDuration">소요 시간</label>
            <input type="text" id="scheduleDuration" placeholder="예: 1시간" value="1시간">
          </div>
          <div class="form-group">
            <label for="scheduleCost">비용</label>
            <input type="text" id="scheduleCost" placeholder="예: 10000원" value="0원">
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn back-btn">취소</button>
          <button type="button" class="btn submit-btn">추가하기</button>
        </div>
      </div>
    `;

    const onSave = function () {
      // 입력값 가져오기
      const time = document.getElementById("scheduleTime").value;
      const location = document.getElementById("scheduleLocation").value;
      const category = document.getElementById("scheduleCategory").value;
      const duration = document.getElementById("scheduleDuration").value;
      const cost = document.getElementById("scheduleCost").value;

      // 유효성 검사
      if (!location) {
        alert("장소를 입력해주세요.");
        return false;
      }

      try {
        // 새 일정 객체 생성
        const newSchedule = {
          departureTime: time,
          category: category,
          location: location,
          duration: duration,
          cost: cost,
        };

        // 데이터에 추가
        if (!itineraryData.days[dayIndex].schedules) {
          itineraryData.days[dayIndex].schedules = [];
        }

        itineraryData.days[dayIndex].schedules.push(newSchedule);

        // UI 갱신
        renderItinerary();

        // 변경사항 저장
        updateItineraryJson();

        // 성공 메시지
        alert("일정이 추가되었습니다.");
        return true;
      } catch (error) {
        console.error("일정 추가 중 오류 발생:", error);
        alert("일정 추가 중 오류가 발생했습니다.");
        return false;
      }
    };

    showModal("addScheduleModal", modalContent, onSave);
  }

  // 일정 추가 버튼 생성 함수
  function createAddScheduleButton(dayIndex) {
    const addScheduleBtn = document.createElement("button");
    addScheduleBtn.type = "button";
    addScheduleBtn.className = "btn add-schedule-btn";
    addScheduleBtn.innerHTML =
      '<i class="fas fa-plus"></i> 이 날짜에 일정 추가하기';

    // 이벤트 리스너 추가
    addScheduleBtn.addEventListener("click", function (event) {
      event.preventDefault();
      event.stopPropagation();
      showAddScheduleModal(dayIndex);
    });

    return addScheduleBtn;
  }

  // 날짜 정보 편집 모달 표시
  function showDayEditModal(dayIndex) {
    // 현재 날짜 데이터
    const day = itineraryData.days[dayIndex];

    const modalContent = `
      <div class="modal-content day-edit-modal">
        <div class="modal-header">
          <h2><i class="fas fa-calendar-alt"></i> 날짜 정보 편집</h2>
          <span class="close-btn">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="dayDate">날짜</label>
            <input type="date" id="dayDate" value="${day.date || ""}">
          </div>
          <div class="form-group">
            <label for="dayWeather">날씨</label>
            <select id="dayWeather">
              <option value="맑음" ${
                day.weather === "맑음" ? "selected" : ""
              }>맑음</option>
              <option value="흐림" ${
                day.weather === "흐림" ? "selected" : ""
              }>흐림</option>
              <option value="비" ${
                day.weather === "비" ? "selected" : ""
              }>비</option>
              <option value="눈" ${
                day.weather === "눈" ? "selected" : ""
              }>눈</option>
            </select>
          </div>
          <div class="form-group">
            <label for="dayTotalCost">총 비용</label>
            <input type="text" id="dayTotalCost" value="${
              day.totalCost || "0원"
            }">
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn back-btn">취소</button>
          <button type="button" class="btn submit-btn">저장</button>
        </div>
      </div>
    `;

    const onSave = function () {
      // 입력값 가져오기
      const date = document.getElementById("dayDate").value;
      const weather = document.getElementById("dayWeather").value;
      const totalCost = document.getElementById("dayTotalCost").value;

      // 데이터 업데이트
      itineraryData.days[dayIndex].date = date;
      itineraryData.days[dayIndex].weather = weather;
      itineraryData.days[dayIndex].totalCost = totalCost;

      // UI 갱신
      renderItinerary();

      // 변경사항 저장
      updateItineraryJson();

      return true;
    };

    showModal("dayEditModal", modalContent, onSave);
  }

  // 초기 렌더링
  renderItinerary();

  // CSS 스타일 추가
  function addCustomStyles() {
    // 이미 추가된 스타일이 있는지 확인
    if (document.getElementById("customModalStyles")) return;

    const styleElement = document.createElement("style");
    styleElement.id = "customModalStyles";
    styleElement.textContent = `
        /* 일정 추가 버튼 스타일 */
        .add-schedule-btn {
            display: block;
            width: 100%;
            padding: 10px;
            margin: 15px 0 5px;
            background-color: #e9f2ff;
            color: #4a6da7;
            border: 1px dashed #4a6da7;
            border-radius: 6px;
            cursor: pointer;
            text-align: center;
            transition: all 0.2s;
        }

        .add-schedule-btn:hover {
            background-color: #d0e3ff;
            color: #3a5a8f;
        }

        .add-schedule-btn i {
            margin-right: 5px;
        }

        /* 일정 추가 모달 스타일 */
        #addScheduleModal .modal-content {
            max-width: 500px;
        }

        #addScheduleModal .form-group {
            margin-bottom: 15px;
        }

        #addScheduleModal label {
            display: block;
            margin-bottom: 5px;
            font-weight: 500;
        }

        #addScheduleModal input,
        #addScheduleModal select {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
    `;

    document.head.appendChild(styleElement);
  }

  // 문서 로드 시 스타일 추가
  document.addEventListener("DOMContentLoaded", function () {
    // 기존 DOMContentLoaded 이벤트 핸들러 내용

    // 커스텀 스타일 추가
    addCustomStyles();
  });
});
