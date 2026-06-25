package oop.search.infrastructure;

import java.net.http.HttpClient;

public abstract class AbstractHttpClient {
    protected final HttpClient httpClient = HttpClient.newHttpClient();

    protected final String endPoint; // 생성자 주입될 예정

    protected AbstractHttpClient(String endPoint) {
        this.endPoint = endPoint;
    }
}
