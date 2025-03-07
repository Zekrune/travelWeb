package com.example.travel.plan.service;

import com.example.travel.plan.model.TravelPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GPTService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";

    public String generatePlanContent(TravelPlan plan) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String prompt = generatePrompt(plan);

            ObjectNode requestJson = objectMapper.createObjectNode();
            requestJson.put("model", "gpt-3.5-turbo");

            ObjectNode messageNode = objectMapper.createObjectNode();
            messageNode.put("role", "system");
            messageNode.put("content", prompt);
            requestJson.putArray("messages").add(messageNode);

            String requestBody = objectMapper.writeValueAsString(requestJson);
            System.out.println("GPT API 요청 JSON: " + requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(GPT_API_URL, HttpMethod.POST, entity, String.class);
            if (response.getBody() == null) {
                throw new RuntimeException("GPT 생성 실패: 응답이 없습니다.");
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                throw new RuntimeException("GPT 생성 실패: choices 없음.");
            }
            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new RuntimeException("GPT 생성 실패: message 없음.");
            }
            JsonNode contentNode = message.get("content");
            if (contentNode == null) {
                throw new RuntimeException("GPT 생성 실패: content 없음.");
            }
            return contentNode.asText();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("GPT 생성 실패: " + e.getMessage());
        }
    }

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