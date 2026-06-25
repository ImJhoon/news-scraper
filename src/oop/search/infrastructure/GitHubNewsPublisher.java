package oop.search.infrastructure;

import oop.search.application.NewsPublisher;
import oop.search.domain.NewsResult;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class GitHubNewsPublisher extends AbstractHttpClient implements NewsPublisher {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/issues";
    // %s : GITHUB_REPOSITORY <- GitHub Actions가 주는 것을 그대로 쓸 예정
    private final String token; // 환경변수로 주어질 것. GitHub Actions가 주는 것을 그대로 쓸 예정

    public GitHubNewsPublisher() {
        super(GITHUB_API_URL
                .formatted(System.getenv("GITHUB_REPOSITORY"))
        );
        this.token = System.getenv("GITHUB_TOKEN");
    }

    @Override
    public void publish(String topic, List<NewsResult> newsResults) {
        String url = endPoint;
        
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("## \uD83D\uDCF0 ").append(topic).append(" 뉴스 검색 결과\\n\\n");
        
        int index = 1;
        for (NewsResult result : newsResults) {
            String title = cleanTextForJson(result.title());
            String desc = cleanTextForJson(result.description());
            String imgUrl = result.imageUrl();
            
            bodyBuilder.append(String.format("### %d. %s\\n", index++, title));
            if (imgUrl != null && !imgUrl.isEmpty()) {
                // 마크다운 문법 대신 HTML img 태그를 사용하여 너비를 300픽셀로 제한합니다.
                bodyBuilder.append(String.format("> <img src=\"%s\" width=\"300\">\\n> \\n", imgUrl));
            }
            bodyBuilder.append(String.format("> %s\\n> \\n", desc));
            bodyBuilder.append(String.format("> 👉 **[기사 원문 보러가기](%s)**\\n\\n", result.url()));
            bodyBuilder.append("---\\n\\n");
        }

        String payload = """
                {
                "title": "%s",
                "body": "%s"
                }
                """.formatted(
                // %s -> topic. %s -> 한국기준 현재 시간 (일자까지만)
                "%s (%s)".formatted(topic, java.time.LocalDate.now(ZoneId.of("Asia/Seoul"))),
                bodyBuilder.toString()
        ).trim();
        HttpRequest request = HttpRequest.newBuilder()
//                .GET()
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .uri(URI.create(url))
//                .header("X-Naver-Client-Id", clientId)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        try {
            httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cleanTextForJson(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "") // HTML 태그 제거 (<b> 등)
                   .replace("&quot;", "'")    // HTML 엔티티 변경
                   .replace("\"", "\\\"")     // JSON 큰따옴표 이스케이프
                   .replace("\n", " ")        // 줄바꿈 제거 (한 줄로)
                   .replace("\r", "");
    }
}