package oop.search.infrastructure;

import oop.search.application.NewsProvider;
import oop.search.domain.NewsCategory;
import oop.search.domain.NewsResult;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NaverNewsProvider extends AbstractHttpClient implements NewsProvider {

    // 생성자 레벨에서 사용할 상수는 static
    private static final String NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";


    private final String clientId;
    private final String clientSecret;
    private final NewsCategory category;
    private final GeminiSummarizer geminiSummarizer;

    // clientId, clientSecret, category
    public NaverNewsProvider() {
        super(NEWS_API_URL);
        this.clientId = System.getenv("NAVER_CLIENT_ID");
        this.clientSecret = System.getenv("NAVER_CLIENT_SECRET");
        this.geminiSummarizer = new GeminiSummarizer();
        
        if (this.clientId == null || this.clientSecret == null) {
            System.err.println("[경고] API 키가 설정되지 않았습니다. 환경 변수를 확인해주세요.");
        }

        String categoryEnv = System.getenv("NEWS_CATEGORY");
        this.category = categoryEnv != null ? NewsCategory.valueOf(categoryEnv) : NewsCategory.SIM;
        // SIM, DATE -> 변환 (Enum - NewsCategory.SIM, NewsCategory.DATE)
        if (this.clientId != null && this.clientSecret != null) {
            System.out.println("clientId = " + clientId.substring(0, Math.min(clientId.length(), 3)) + "...");
            System.out.println("clientSecret = " + clientSecret.substring(0, Math.min(clientSecret.length(), 3)) + "...");
        }
        System.out.println("category = " + category);
    }

    @Override
    public List<NewsResult> fetchNews(String searchQuery, int limit) {
        String url = endPoint + "?query="
                + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8)
                + "&display" + limit
                + "&sort=" + category.getQueryValue()
                + "&start=1";
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();
        
        List<NewsResult> results = new ArrayList<>();
        
        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            String body = response.body();
            
            // items 
            String items = body.split("items")[1];
            String[] itemArr = items.split("},");
            
            for (String item : itemArr) {
                String title = cutText(item, "\"title\":\"", "\",");
                String link = cutText(item, "\"link\":\"", "\",");
                String description = cutText(item, "\"description\":\"", "\",");
                String pubDate = "";
                
                // JSON에서 URL의 \/ 를 / 로 이스케이프 해제
                String cleanLink = link.replace("\\/", "/");
                
                // 기사 원문에 접속하여 메타데이터(썸네일, 전체 텍스트) 추출
                ArticleMetadata meta = extractMetadata(cleanLink);
                String imageUrl = meta.imageUrl();
                
                // 텍스트를 바탕으로 AI 요약 시도
                String aiSummary = geminiSummarizer.summarize(meta.fullText());
                
                // 요약이 성공했다면 그것을 쓰고, 실패했다면 네이버 기본 요약(description)을 그대로 씁니다.
                String finalDescription = aiSummary.isEmpty() ? description : "✨ **[AI 3줄 요약]**\\n" + aiSummary;

                NewsResult result = new NewsResult(title, finalDescription, cleanLink, pubDate, imageUrl);
                results.add(result);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

//        return List.of();
        return results;
    }

    public String cutText(String original, String prefix, String suffix) {
        return original
                .split(prefix)[1]
                .split(suffix)[0];
    }

    private record ArticleMetadata(String imageUrl, String fullText) {}

    private ArticleMetadata extractMetadata(String articleUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(articleUrl))
                    .header("User-Agent", "Mozilla/5.0") // 봇 차단 방지
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String html = response.body();
            
            String imageUrl = "";
            if (html.contains("og:image")) {
                String ogImagePart = html.split("og:image")[1];
                if (ogImagePart.contains("content=\"")) {
                    imageUrl = ogImagePart.split("content=\"")[1].split("\"")[0];
                }
            }
            
            // 기사 원문 텍스트 추출 (순수 자바 정규식 기반)
            // 1. <style>, <script> 태그 및 내용 완벽 제거 (줄바꿈 포함 매칭을 위해 (?s) 사용)
            String text = html.replaceAll("(?s)<style[^>]*>.*?</style>", "")
                              .replaceAll("(?s)<script[^>]*>.*?</script>", "")
                              // 2. 나머지 모든 HTML 태그를 공백으로 치환
                              .replaceAll("<[^>]*>", " ")
                              // 3. 여러 개의 공백이나 줄바꿈을 하나의 공백으로 압축
                              .replaceAll("\\s+", " ")
                              .trim();
                              
            return new ArticleMetadata(imageUrl, text);
        } catch (Exception e) {
            // 실패 시 빈 값 반환
        }
        return new ArticleMetadata("", "");
    }

    public static void main(String[] args) {
        NewsProvider provider = new NaverNewsProvider();
        List<NewsResult> results = provider.fetchNews("주식", 10);
//        System.out.println("results = " + results);
        for (NewsResult newsItem : results) {
            System.out.println("newsItem = " + newsItem);
        }
    }
}