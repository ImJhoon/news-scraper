package oop.search.infrastructure;

import oop.search.application.NewsProvider;

import java.net.http.HttpClient;

public abstract class AbstractHttpScraper implements NewsProvider {
    protected final HttpClient httpClient = HttpClient.newHttpClient();

    protected final String endPoint; // 생성자 주입될 예정

    protected  AbstractHttpScraper(String endPoint) {
        this.endPoint = endPoint;
    }
}
