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

    // clientId, clientSecret, category
    public NaverNewsProvider() {
        super(NEWS_API_URL);
        this.clientId = System.getenv("NAVER_CLIENT_ID");
        this.clientSecret = System.getenv("NAVER_CLIENT_SECRET");
        
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
            System.out.println("body = " + body);
            
            // items 
            String items = body.split("items")[1];
            // <- items -> 
            System.out.println("items = " + items);
            String[] itemArr = items.split("},");
            
            for (String item : itemArr) {
//                String title = item
//                        .split("\"title\":\"")[1]
//                        .split("\",")[0];
                String title = cutText(item, "\"title\":\"", "\",");
                String link = cutText(item, "\"link\":\"", "\",");
                String description = cutText(item, "\"description\":\"", "\",");
                String pubDate = "";
//                System.out.println("title = " + title);
//                System.out.println("description = " + description);
                NewsResult result = new NewsResult(title, description, link, pubDate);
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

    public static void main(String[] args) {
        NewsProvider provider = new NaverNewsProvider();
        List<NewsResult> results = provider.fetchNews("주식", 10);
//        System.out.println("results = " + results);
        for (NewsResult newsItem : results) {
            System.out.println("newsItem = " + newsItem);
        }
    }
}