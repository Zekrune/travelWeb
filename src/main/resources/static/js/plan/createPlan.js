/************************************************************
 * 0) 전역 변수 & 스텝 이동 제어
 ************************************************************/
let currentStep = 1;
const totalSteps = 5;

// 최종 전송할 데이터
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

// Step 이동
function nextStep() {
  document.getElementById(`step${currentStep}`).classList.remove("active");
  currentStep++;
  if (currentStep > totalSteps) currentStep = totalSteps;
  document.getElementById(`step${currentStep}`).classList.add("active");

  // Step5 도달 시 요약 표시
  if (currentStep === 5) {
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
 * 1) Step 1: 달력 (2개월)
 ************************************************************/
const today = new Date(); // 오늘 날짜
let currentYear = today.getFullYear();
let currentMonth = today.getMonth(); // 0~11 (3월이면 2)
let displayYear; // 달력 '왼쪽' 기준 연도
let displayMonth; // 달력 '왼쪽' 기준 월(0~11)

// 최대 10일 제한
const MAX_RANGE = 100;

// 선택된 시작/종료 (Date 객체)
let selectedStart = null;
let selectedEnd = null;

window.onload = function () {
  // Step1 초기 설정
  document.getElementById(`step${currentStep}`).classList.add("active");
  displayYear = currentYear;
  displayMonth = currentMonth;
  renderCalendars(displayYear, displayMonth);
};

// 달력 이전/다음 화살표
document.getElementById("prevBtn").onclick = () => {
  const { year: prevY, month: prevM } = getPrevMonth(displayYear, displayMonth);
  // 오늘보다 이전 달로는 이동 불가
  if (prevY < currentYear || (prevY === currentYear && prevM < currentMonth)) {
    return; // 막기
  }
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

// 달력 2개월 렌더링
function renderCalendars(year, month) {
  const next = getNextMonth(year, month);
  document.getElementById("monthYearLabel").textContent = `${year}년 ${
    month + 1
  }월 / ${next.year}년 ${next.month + 1}월`;

  // prev 버튼 활성/비활성
  const { year: py, month: pm } = getPrevMonth(year, month);
  if (py < currentYear || (py === currentYear && pm < currentMonth)) {
    document.getElementById("prevBtn").disabled = true;
  } else {
    document.getElementById("prevBtn").disabled = false;
  }

  renderSingleCalendar(year, month, "calendar1");
  renderSingleCalendar(next.year, next.month, "calendar2");
}

// 단일 달력 렌더링
function renderSingleCalendar(year, month, tableId) {
  const tbody = document.getElementById(tableId).querySelector("tbody");
  tbody.innerHTML = "";

  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const totalDays = lastDay.getDate();
  const startDay = firstDay.getDay(); // 0=일,1=월,...

  let rows = [];
  let cells = [];

  // 첫 주 앞 공백
  for (let i = 0; i < startDay; i++) {
    cells.push("");
  }

  // 날짜 채우기
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

  // 테이블에 행 추가
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

        // 과거 날짜는 disabled
        if (isPastDate(cellDate)) {
          td.classList.add("disabled");
        } else {
          td.onclick = () => onDateClick(cellDate);
        }
        // 범위 하이라이트
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
    // 처음 선택
    selectedStart = dateObj;
  } else if (selectedStart && !selectedEnd) {
    // 두 번째 선택
    if (dateObj < selectedStart) {
      // 만약 종료일이 시작일보다 과거면 swap
      [selectedStart, dateObj] = [dateObj, selectedStart];
    }
    const diff = dateDiffInDays(selectedStart, dateObj);
    if (diff > MAX_RANGE) {
      alert(`최대 ${MAX_RANGE}일까지만 선택할 수 있습니다.`);
      return;
    }
    selectedEnd = dateObj;
  } else {
    // 이미 start/end가 있음 -> 다시 처음부터
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

  if (time === startTime) {
    td.classList.add("selected-start");
  }
  if (selectedEnd && time === endTime) {
    td.classList.add("selected-end");
  }
  if (selectedEnd && time > startTime && time < endTime) {
    td.classList.add("in-range");
  }
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

// Step1 "다음" 버튼 클릭 시
function goNextFromStep1() {
  // 날짜 검사
  if (!selectedStart || !selectedEnd) {
    alert("시작일과 종료일을 모두 선택해주세요.");
    return;
  }
  // travelData에 저장 (YYYY-MM-DD)
  travelData.startDate = formatDate(selectedStart);
  travelData.endDate = formatDate(selectedEnd);

  // 출발지/목적지
  travelData.departLocation = document
    .getElementById("departLocation")
    .value.trim();
  travelData.destination = document.getElementById("destination").value.trim();

  if (!travelData.departLocation || !travelData.destination) {
    alert("출발지와 목적지를 입력해주세요.");
    return;
  }

  // 다음 스텝
  nextStep();
}

function formatDate(dateObj) {
  const y = dateObj.getFullYear();
  const m = ("0" + (dateObj.getMonth() + 1)).slice(-2);
  const d = ("0" + dateObj.getDate()).slice(-2);
  return `${y}-${m}-${d}`;
}

/************************************************************
 * 2) Step 2: 교통수단 선택
 ************************************************************/
function selectTransportation(elem) {
  const boxes = document.querySelectorAll(".transport-box");
  boxes.forEach((box) => box.classList.remove("selected"));
  elem.classList.add("selected");
  travelData.transportation = elem.getAttribute("data-value");
}

/************************************************************
 * 3) Step 5: 요약 & 제출
 ************************************************************/
function updateSummary() {
  // Step 3 값
  travelData.purpose = document.getElementById("purpose").value.trim();
  travelData.budget = document.getElementById("budget").value.trim();
  travelData.density = document.getElementById("density").value.trim();

  // Step 4 값
  travelData.hotelLocation = document
    .getElementById("hotelLocation")
    .value.trim();
  travelData.travelMode = document.getElementById("travelMode").value;

  // 요약 표시
  document.getElementById("summary").innerText = JSON.stringify(
    travelData,
    null,
    2
  );
}

function submitPlan() {
  console.log("최종 전송 데이터:", travelData);

  const formData = new URLSearchParams();
  for (const key in travelData) {
    formData.append(key, travelData[key]);
  }

  fetch("/plans/create", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formData.toString(),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("서버 요청 실패");
      }
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
