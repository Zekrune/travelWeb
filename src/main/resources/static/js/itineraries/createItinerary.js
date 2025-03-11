/************************************************************
 * 전역 변수 & 스텝 이동 제어
 ************************************************************/
let currentStep = 1;
const totalSteps = 6;
let travelData = {
  departLocation: "", // 출발지
  destination: "", // 목적지
  startDate: "", // 여행 시작일 (YYYY-MM-DD)
  endDate: "", // 여행 종료일 (YYYY-MM-DD)
  transportation: "", // 교통 수단
  purpose: "", // 여행 목적
  budget: "", // 예산
  density: "", // 여행 밀도
  hotelLocation: "", // 숙소 위치
  travelMode: "", // 이동 방식
};

/************************************************************
 * 스텝 이동 함수
 ************************************************************/
function nextStep() {
  document.getElementById(`step${currentStep}`).classList.remove("active");
  currentStep++;
  if (currentStep > totalSteps) currentStep = totalSteps;
  document.getElementById(`step${currentStep}`).classList.add("active");

  // Step 6 도달 시 요약 표시
  if (currentStep === 6) {
    updateSummary();
  }
}
function prevStep() {
  document.getElementById(`step${currentStep}`).classList.remove("active");
  currentStep--;
  if (currentStep < 1) currentStep = 1;
  document.getElementById(`step${currentStep}`).classList.add("active");
}

/************************************************************
 * Step 1: 달력 (2개월)
 ************************************************************/
const today = new Date();
let currentYear = today.getFullYear();
let currentMonth = today.getMonth();
let displayYear, displayMonth;
const MAX_RANGE = 100;
let selectedStart = null;
let selectedEnd = null;

window.onload = function () {
  document.getElementById(`step${currentStep}`).classList.add("active");
  displayYear = currentYear;
  displayMonth = currentMonth;
  renderCalendars(displayYear, displayMonth);
};

document.getElementById("prevBtn").onclick = () => {
  const { year: prevY, month: prevM } = getPrevMonth(displayYear, displayMonth);
  if (prevY < currentYear || (prevY === currentYear && prevM < currentMonth))
    return;
  displayYear = prevY;
  displayMonth = prevM;
  renderCalendars(displayYear, displayMonth);
};
document.getElementById("nextBtn").onclick = () => {
  const { year: nextY, month: nextM } = getNextMonth(displayYear, displayMonth);
  displayYear = nextY;
  displayMonth = nextM;
  renderCalendars(displayYear, displayMonth);
};

function renderCalendars(year, month) {
  const next = getNextMonth(year, month);
  document.getElementById("monthYearLabel").textContent = `${year}년 ${
    month + 1
  }월 / ${next.year}년 ${next.month + 1}월`;
  const { year: py, month: pm } = getPrevMonth(year, month);
  document.getElementById("prevBtn").disabled =
    py < currentYear || (py === currentYear && pm < currentMonth);
  renderSingleCalendar(year, month, "calendar1");
  renderSingleCalendar(next.year, next.month, "calendar2");
}

function renderSingleCalendar(year, month, tableId) {
  const tbody = document.getElementById(tableId).querySelector("tbody");
  tbody.innerHTML = "";
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const totalDays = lastDay.getDate();
  const startDay = firstDay.getDay();
  let rows = [];
  let cells = [];
  for (let i = 0; i < startDay; i++) {
    cells.push("");
  }
  for (let date = 1; date <= totalDays; date++) {
    cells.push(date);
    if (cells.length === 7) {
      rows.push(cells);
      cells = [];
    }
  }
  if (cells.length > 0) {
    while (cells.length < 7) {
      cells.push("");
    }
    rows.push(cells);
  }
  rows.forEach((row) => {
    const tr = document.createElement("tr");
    row.forEach((cell) => {
      const td = document.createElement("td");
      if (cell === "") {
        td.innerHTML = "";
        td.classList.add("disabled");
      } else {
        td.innerHTML = cell;
        const cellDate = new Date(year, month, cell);
        if (isPastDate(cellDate)) {
          td.classList.add("disabled");
        } else {
          td.onclick = () => onDateClick(cellDate);
        }
        styleRangeHighlight(td, cellDate);
      }
      tr.appendChild(td);
    });
    tbody.appendChild(tr);
  });
}

function isPastDate(dateObj) {
  const todayMidnight = new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate()
  );
  return dateObj < todayMidnight;
}

function onDateClick(dateObj) {
  if (!selectedStart && !selectedEnd) {
    selectedStart = dateObj;
  } else if (selectedStart && !selectedEnd) {
    if (dateObj < selectedStart) {
      [selectedStart, dateObj] = [dateObj, selectedStart];
    }
    const diff = dateDiffInDays(selectedStart, dateObj);
    if (diff > MAX_RANGE) {
      alert(`최대 ${MAX_RANGE}일까지만 선택할 수 있습니다.`);
      return;
    }
    selectedEnd = dateObj;
  } else {
    selectedStart = dateObj;
    selectedEnd = null;
  }
  renderCalendars(displayYear, displayMonth);
}

function dateDiffInDays(d1, d2) {
  const t1 = new Date(d1.getFullYear(), d1.getMonth(), d1.getDate()).getTime();
  const t2 = new Date(d2.getFullYear(), d2.getMonth(), d2.getDate()).getTime();
  return Math.round((t2 - t1) / (1000 * 60 * 60 * 24));
}

function styleRangeHighlight(td, dateObj) {
  if (!selectedStart) return;
  const time = dateObj.getTime();
  const startTime = selectedStart.getTime();
  const endTime = selectedEnd ? selectedEnd.getTime() : null;
  if (time === startTime) td.classList.add("selected-start");
  if (selectedEnd && time === endTime) td.classList.add("selected-end");
  if (selectedEnd && time > startTime && time < endTime)
    td.classList.add("in-range");
}

function getPrevMonth(year, month) {
  if (month === 0) {
    return { year: year - 1, month: 11 };
  } else {
    return { year, month: month - 1 };
  }
}
function getNextMonth(year, month) {
  if (month === 11) {
    return { year: year + 1, month: 0 };
  } else {
    return { year, month: month + 1 };
  }
}

// Step 1 "다음" 버튼
function goNextFromStep1() {
  if (!selectedStart || !selectedEnd) {
    alert("시작일과 종료일을 모두 선택해주세요.");
    return;
  }
  travelData.startDate = formatDate(selectedStart);
  travelData.endDate = formatDate(selectedEnd);
  travelData.departLocation = document
    .getElementById("departLocation")
    .value.trim();
  travelData.destination = document.getElementById("destination").value.trim();
  if (!travelData.departLocation || !travelData.destination) {
    alert("출발지와 목적지를 입력해주세요.");
    return;
  }
  nextStep();
}

function formatDate(dateObj) {
  const y = dateObj.getFullYear();
  const m = ("0" + (dateObj.getMonth() + 1)).slice(-2);
  const d = ("0" + dateObj.getDate()).slice(-2);
  return `${y}-${m}-${d}`;
}

/************************************************************
 * Step 2: 교통 수단 선택
 ************************************************************/
function selectTransportation(elem) {
  document
    .querySelectorAll(".transport-box")
    .forEach((box) => box.classList.remove("selected"));
  elem.classList.add("selected");
  travelData.transportation = elem.getAttribute("data-value");
}

/************************************************************
 * Step 3: 여행 목적 선택
 ************************************************************/
function selectPurpose(elem) {
  document
    .querySelectorAll(".purpose-box")
    .forEach((box) => box.classList.remove("selected"));
  elem.classList.add("selected");
  travelData.purpose = elem.getAttribute("data-value");
}

/************************************************************
 * Step 4: 예산 입력 및 여행 밀도 선택
 ************************************************************/
function selectDensity(elem) {
  document
    .querySelectorAll(".density-box")
    .forEach((box) => box.classList.remove("selected"));
  elem.classList.add("selected");
  travelData.density = elem.getAttribute("data-value");
}
// Step4 "다음" 버튼 - budget 처리
function goNextFromStep4() {
  let budgetValue = document.getElementById("budget").value.trim();
  if (!budgetValue) {
    // 빈 값이면 기본값 "0" 할당
    budgetValue = "0";
  }
  travelData.budget = budgetValue;
  nextStep();
}

/************************************************************
 * Step 5: 숙소 및 이동 방식 선택
 ************************************************************/
function selectTravelMode(elem) {
  document
    .querySelectorAll(".travel-mode-box")
    .forEach((box) => box.classList.remove("selected"));
  elem.classList.add("selected");
  travelData.travelMode = elem.getAttribute("data-value");
}

/************************************************************
 * Step 6: 요약 및 제출
 ************************************************************/
function updateSummary() {
  travelData.hotelLocation = document
    .getElementById("hotelLocation")
    .value.trim();

  // 이동 방식이 선택되지 않았을 경우 기본값 설정
  if (!travelData.travelMode) {
    const selectedTravelMode = document.querySelector(
      ".travel-mode-box.selected"
    );
    if (selectedTravelMode) {
      travelData.travelMode = selectedTravelMode.getAttribute("data-value");
    } else {
      travelData.travelMode = "도보"; // 기본값
    }
  }

  // 기존 요약 내용 제거
  const summaryElement = document.getElementById("summary");
  summaryElement.innerHTML = "";

  // 새로운 요약 컨테이너 생성
  const summaryContainer = document.createElement("div");
  summaryContainer.className = "summary-container";

  // 헤더 추가
  const summaryHeader = document.createElement("div");
  summaryHeader.className = "summary-header";
  summaryHeader.textContent = "여행 계획 요약";
  summaryContainer.appendChild(summaryHeader);

  // 내용 컨테이너 추가
  const summaryContent = document.createElement("div");
  summaryContent.className = "summary-content";

  // 여행 정보 항목 추가
  const items = [
    { label: "출발지", value: travelData.departLocation },
    { label: "목적지", value: travelData.destination },
    { label: "출발일", value: formatDateForDisplay(travelData.startDate) },
    { label: "도착일", value: formatDateForDisplay(travelData.endDate) },
    { label: "교통 수단", value: travelData.transportation },
    { label: "여행 목적", value: travelData.purpose },
    { label: "예산", value: formatBudget(travelData.budget) },
    { label: "여행 밀도", value: travelData.density },
    { label: "숙소 위치", value: travelData.hotelLocation || "미지정" },
    { label: "이동 방식", value: travelData.travelMode },
  ];

  // 각 항목을 추가
  items.forEach((item) => {
    const itemElement = document.createElement("div");
    itemElement.className = "summary-item";

    const labelElement = document.createElement("div");
    labelElement.className = "summary-label";
    labelElement.textContent = item.label;

    const valueElement = document.createElement("div");
    valueElement.className = "summary-value";
    valueElement.textContent = item.value;

    itemElement.appendChild(labelElement);
    itemElement.appendChild(valueElement);
    summaryContent.appendChild(itemElement);
  });

  summaryContainer.appendChild(summaryContent);
  summaryElement.appendChild(summaryContainer);
}

// 날짜 표시 형식 변환 (YYYY-MM-DD -> YYYY년 MM월 DD일)
function formatDateForDisplay(dateString) {
  if (!dateString) return "미지정";

  const parts = dateString.split("-");
  if (parts.length !== 3) return dateString;

  return `${parts[0]}년 ${parts[1]}월 ${parts[2]}일`;
}

// 예산 표시 형식 변환 (숫자 -> 천 단위 구분 + 원)
function formatBudget(budget) {
  if (!budget || budget === "0") return "미지정";

  // 숫자로 변환 후 천 단위 구분 기호 추가
  return parseInt(budget).toLocaleString() + "원";
}

function submitPlan() {
  console.log("최종 전송 데이터:", travelData);
  const formData = new URLSearchParams();
  for (const key in travelData) {
    formData.append(key, travelData[key]);
  }
  fetch("/itineraries/createItinerary", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formData.toString(),
  })
    .then((response) => {
      if (!response.ok) throw new Error("서버 요청 실패");
      return response.text();
    })
    .then((html) => {
      document.open();
      document.write(html);
      document.close();
    })
    .catch((error) => {
      console.error("에러 발생:", error);
      alert("일정 생성 중 오류가 발생했습니다.");
    });
}
