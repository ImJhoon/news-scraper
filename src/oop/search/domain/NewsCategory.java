package oop.search.domain;

// https://developers.naver.com/docs/serviceapi/search/news/news.md#%EB%89%B4%EC%8A%A4
public enum NewsCategory {
    SIM("sim", "정확도순"), DATE("date", "최신순");

    private final String queryValue;
    private final String description;

    NewsCategory(String queryValue, String description) {
        this.queryValue = queryValue;
        this.description = description;
    }

    public static void main(String[] args) {
        System.out.println(NewsCategory.SIM);
        System.out.println(NewsCategory.DATE);
        NewsCategory nc = NewsCategory.SIM;
        System.out.println("nc.getQueryValue() = " + nc.getQueryValue());
        System.out.println("nc.getDescription() = " + nc.getDescription());
        useCategory(NewsCategory.SIM);

    }

    public static void useCategory(NewsCategory category) {
        System.out.println(category);
    }

    public String getQueryValue() {
        return queryValue;
    }

    public String getDescription() {
        return description;
    }
}
