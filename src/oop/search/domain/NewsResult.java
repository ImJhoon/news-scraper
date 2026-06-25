package oop.search.domain;

// Value Object
// Immutable (수정 불가능)
// Java에서 변화를 인지하려면 Heap 메모리의 변화를 통해 대부분 감지 한다
// 기본 자바 객체는 불면 객체로 만드는 과정을 거쳐야 함 -> Record는 원래 immutable하고 setter x
// 멤버 변수를 한번 생성자로 만들면 수정 x

// 1. 불러오는 API를 바탕
// 2. 개념적 설계
// record 레코드명(멤버변수를 생성자의 패러미터처럼 넣는다)
public record NewsResult(
        String title,
        String description,
        String url,
        String pubDate
) {
    public static void main(String[] args) {
        NewsResult result = new NewsResult("환율 1600원 돌파 가능성?", "현재 1550원 간당간당...", "https://naver.com", "2026.06.25");
        System.out.println("result = " + result);
    }
}
