package oop.search.infrastructure;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiSummarizer extends AbstractHttpClient {
    private final String apiKey;

    public GeminiSummarizer() {
        // Gemini 1.5 Flash 모델 엔드포인트
        super("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent");
        this.apiKey = System.getenv("GEMINI_API_KEY");
    }

    public String summarize(String articleText) {
        if (apiKey == null || apiKey.isEmpty() || articleText == null || articleText.isBlank()) {
            return ""; // 키가 없거나 텍스트가 없으면 작동 안 함 (기존 로직 유지)
        }

        try {
            // 텍스트가 너무 길면 API 토큰 초과를 방지하기 위해 앞부분만 자름
            String safeText = articleText.length() > 3000 ? articleText.substring(0, 3000) : articleText;
            
            // JSON 파싱 에러를 막기 위해 특수기호 안전하게 이스케이프
            safeText = safeText.replace("\"", "'").replace("\n", " ").replace("\r", " ").replace("\\", "/");

            // 프롬프트 작성 (JSON Payload)
            // 응답 파싱을 쉽게 하기 위해 큰따옴표 사용을 금지시킴
            String payload = """
                    {
                      "contents": [{
                        "parts": [{"text": "다음 기사 원문을 읽고 핵심만 3줄로 매우 짧고 명확하게 요약해줘. 응답 텍스트 안에 큰따옴표(\\")는 절대 쓰지 말고, 각 줄은 '- ' 기호로 시작해줘:\\n\\n%s"}]
                      }]
                    }
                    """.formatted(safeText);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .uri(URI.create(endPoint + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            if (response.statusCode() != 200) {
                System.err.println("[Gemini API 에러] 상태 코드: " + response.statusCode());
                System.err.println("[Gemini API 응답]: " + body);
                return "";
            }

            // 순수 자바(Vanilla Java) 환경이므로 외부 JSON 라이브러리(Gson 등) 없이 직접 문자열 파싱
            // "text": " 또는 "text":" 와 같이 공백이 다를 수 있으므로 정규식 split 사용
            String[] splitByText = body.split("\"text\"\\s*:\\s*\"");
            if (splitByText.length > 1) {
                String afterText = splitByText[1];
                // 값이 끝나는 닫는 큰따옴표 찾기 (단, \" 처럼 이스케이프된 따옴표는 무시)
                String[] textParts = afterText.split("(?<!\\\\)\"");
                if (textParts.length > 0) {
                    String result = textParts[0];
                    // JSON에서 전달된 이스케이프 줄바꿈(\\n)과 이스케이프 따옴표(\\\")를 실제 문자로 복구
                    return result.replace("\\n", "\n").replace("\\\"", "\"").replace("\\'", "'").trim();
                }
            } else {
                System.err.println("[Gemini 파싱 실패] 예상된 JSON 구조가 아닙니다. 응답 바디: " + body);
            }
        } catch (Exception e) {
            System.err.println("Gemini AI 요약 중 에러 발생: " + e.getMessage());
        }
        
        return ""; // 실패 시 빈 문자열 반환 (원본 요약으로 Fallback)
    }
}
