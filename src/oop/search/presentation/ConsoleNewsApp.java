package oop.search.presentation;

import oop.search.application.NewsService;
import oop.search.infrastructure.NaverNewsProvider;

import java.util.Scanner;

public class ConsoleNewsApp {
    private final NewsService newsService;
    public ConsoleNewsApp(NewsService newsService){
        this.newsService = newsService;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.print("검색할 키워드를 입력해주세요 [종료 시 q] : ");
            String keyword = sc.nextLine();
            if ("q".equals(keyword)) {
                break;
            }

            System.out.print("몇 건 검색하시겠습니까? : ");
            int limit = sc.nextInt();
            newsService.search(keyword, limit);
        }
    }

    public static void main(String[] args) {
        NewsService newsService = new NewsService(
                new NaverNewsProvider(),
                new ConsoleNewsPublisher()
        );
        ConsoleNewsApp app = new ConsoleNewsApp(newsService);
        app.run();
    }
}
