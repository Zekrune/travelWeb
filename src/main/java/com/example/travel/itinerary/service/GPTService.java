package com.example.travel.itinerary.service;

import com.example.travel.exception.ApiException;
import com.example.travel.itinerary.model.TravelPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GPTService {

  @Value("${openai.api.key}")
  private String apiKey;

  private final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
  private final int MAX_RETRIES = 3;
  private final long RETRY_DELAY_MS = 1000;

  /**
   * GPT API를 호출하여 여행 계획 내용을 생성합니다.
   * 
   * @param plan 여행 계획 정보
   * @return GPT가 생성한 여행 계획 JSON 문자열
   * @throws ApiException API 호출 중 오류 발생 시
   */
  public String generatePlanContent(TravelPlan plan) {
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();

    int retries = 0;
    while (retries < MAX_RETRIES) {
      try {
        String prompt = generatePrompt(plan);
        log.debug("GPT 프롬프트: {}", prompt);

        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put("model", "gpt-3.5-turbo");
        requestJson.put("temperature", 0.7);
        requestJson.put("max_tokens", 4000);

        ObjectNode messageNode = objectMapper.createObjectNode();
        messageNode.put("role", "system");
        messageNode.put("content", prompt);
        requestJson.putArray("messages").add(messageNode);

        String requestBody = objectMapper.writeValueAsString(requestJson);
        log.info("GPT API 요청 JSON: {}", requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            GPT_API_URL,
            HttpMethod.POST,
            entity,
            String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
          log.error("GPT API 응답 오류: {}", response.getStatusCode());
          throw new ApiException(HttpStatus.valueOf(response.getStatusCode().value()), "GPT API 응답 오류: " + response.getStatusCode());
        }

        if (response.getBody() == null) {
          throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 생성 실패: 응답이 없습니다.");
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode choices = root.get("choices");

        if (choices == null || !choices.isArray() || choices.size() == 0) {
          throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 생성 실패: choices 없음.");
        }

        JsonNode message = choices.get(0).get("message");
        if (message == null) {
          throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 생성 실패: message 없음.");
        }

        JsonNode contentNode = message.get("content");
        if (contentNode == null) {
          throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 생성 실패: content 없음.");
        }

        String content = contentNode.asText();
        validateJsonResponse(content);

        return content;

      } catch (RestClientException e) {
        log.error("GPT API 호출 오류 (시도 {}/{}): {}", retries + 1, MAX_RETRIES, e.getMessage());
        retries++;

        if (retries >= MAX_RETRIES) {
          throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
              "GPT API 호출 실패 (최대 재시도 횟수 초과): " + e.getMessage());
        }

        try {
          TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS * retries);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      } catch (Exception e) {
        log.error("GPT 생성 중 오류 발생: {}", e.getMessage());
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 생성 실패: " + e.getMessage());
      }
    }

    // 이 코드는 실행되지 않아야 함 (위에서 예외 발생)
    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "GPT 생성 실패: 알 수 없는 오류");
  }

  /**
   * GPT 응답이 유효한 JSON 형식인지 검증합니다.
   */
  private void validateJsonResponse(String content) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      // JSON 파싱 시도
      JsonNode jsonNode = objectMapper.readTree(content);

      // 필수 필드 확인
      if (!jsonNode.has("travelPlan")) {
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
            "GPT 응답이 올바른 형식이 아닙니다: 'travelPlan' 필드가 없습니다.");
      }

      JsonNode travelPlan = jsonNode.get("travelPlan");
      if (!travelPlan.has("itinerary")) {
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
            "GPT 응답이 올바른 형식이 아닙니다: 'itinerary' 필드가 없습니다.");
      }

    } catch (Exception e) {
      log.error("GPT 응답 JSON 검증 실패: {}", e.getMessage());
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
          "GPT 응답이 올바른 JSON 형식이 아닙니다: " + e.getMessage());
    }
  }

  /**
   * 여행 계획 정보를 바탕으로 GPT 프롬프트를 생성합니다.
   */
  private String generatePrompt(TravelPlan plan) {
    return """
        당신은 경험이 풍부한 여행 플래너 AI입니다.
        사용자의 여행 스타일과 예산, 선호도를 바탕으로 창의적이고 현실적인 여행 일정을 구성하세요.
        아래 입력 정보를 참고하여, 반드시 아래 JSON 형식에 맞추되 내용은 전혀 다른, 새롭고 창의적인 여행 계획을 생성해 주세요.

        **입력 정보**
        - 출발 장소: %s
        - 출발 날짜: %s
        - 종료 날짜: %s
        - 교통 수단: %s
        - 도착 장소: %s
        - 여행 목적: %s
        - 여행 예산: %d
        - 여행 밀도: %s
        - 숙소 위치: %s
        - 이동 방식: %s

        **출력 JSON 형식** (예시일 뿐이며, 실제 내용은 창의적으로 생성할 것)
        {
          "travelPlan": {
            "plan_Id": "UUID",
            "departLocation": "출발 장소",
            "startDate": "출발 날짜",
            "endDate": "종료 날짜",
            "transportation": "교통 수단",
            "destination": "도착 장소",
            "purpose": "여행 목적",
            "budget": 여행 예산,
            "density": "여행 밀도",
            "hotelLocation": "숙소 위치",
            "travelMode": "이동 방식",
            "itinerary": [
              {
                "date": "일자",
                "weather": "날씨 정보",
                "totalCost": "총 비용",
                "schedule": [
                  {
                    "departureTime": "출발 시간",
                    "category": "카테고리",
                    "location": "장소 이동 정보",
                    "duration": "소요 시간",
                    "cost": "비용 (설명)"
                  }
                  // 추가 스케줄 항목 가능
                ]
              }
              // 추가 일자 항목 가능
            ]
          }
        }

        위 형식을 참고하되, 입력 정보를 기반으로 창의적이고 현실적인 여행 계획을 새롭게 만들어 주세요.
        반드시 유효한 JSON 형식으로 응답해야 합니다.
        """.formatted(
        plan.getDepartLocation(),
        plan.getStartDate(),
        plan.getEndDate(),
        plan.getTransportation(),
        (plan.getDestination() != null ? plan.getDestination() : "N/A"),
        plan.getPurpose(),
        plan.getBudget(),
        plan.getDensity(),
        plan.getHotelLocation(),
        plan.getTravelMode());
  }
}