# 뉴스 스크래퍼(News Scraper) 구현 기능 현황

지금까지 커밋된 내역(`Initial commit` ~ `feat: NewsPublisher 인터페이스 및 NewsService 핵심 로직 구현`)을 바탕으로 현재까지 구현된 도메인 모델과 비즈니스 로직(애플리케이션 계층)의 기능을 상세히 정리한 문서입니다.

## 1. 도메인 계층 (Domain Layer)
뉴스 데이터를 표현하고 제어하기 위한 핵심 비즈니스 개념 및 규격들을 정의했습니다.

### 1.1 `NewsResult` (뉴스 결과 모델)
- **위치**: `src/oop/search/domain/NewsResult.java`
- **특징**: Java 16+의 **Record** 클래스를 활용하여 구현된 **불변 객체(Value Object)**입니다. 데이터의 변경을 방지하여 안정성을 높였습니다.
- **포함 데이터 (필드)**:
  - `title` (String): 기사 제목
  - `description` (String): 기사 요약 및 내용
  - `url` (String): 기사 원문 링크
  - `pubDate` (String): 기사 발행일

### 1.2 `NewsCategory` (검색 정렬 카테고리)
- **위치**: `src/oop/search/domain/NewsCategory.java`
- **특징**: 검색 시 사용할 정렬 조건들을 정의한 **Enum 클래스**입니다. (네이버 뉴스 API 사양을 참고하여 설계)
- **정의된 항목**:
  - `SIM("sim", "정확도순")`: 검색어와 가장 관련성이 높은 순서로 정렬합니다.
  - `DATE("date", "최신순")`: 뉴스가 발행된 최신 날짜 순서로 정렬합니다.
- **메서드**: 
  - `getQueryValue()`: API 호출 시 사용할 쿼리 파라미터 값(`sim` 또는 `date`)을 반환합니다.
  - `getDescription()`: 카테고리에 대한 한글 설명을 반환합니다.

---

## 2. 애플리케이션 계층 (Application Layer)
도메인 객체를 활용하여 실제 뉴스 검색과 관련된 흐름(Flow)을 제어하는 인터페이스와 서비스 로직을 정의했습니다.

### 2.1 `NewsProvider` (뉴스 제공자 인터페이스)
- **위치**: `src/oop/search/application/NewsProvider.java`
- **특징**: 외부 API(예: 네이버 Open API) 등으로부터 뉴스를 실질적으로 검색하여 가져오는 역할을 추상화한 **인터페이스**입니다.
- **주요 기능**:
  - `fetchNews(String searchQuery, int limit)`: 검색어(`searchQuery`)와 가져올 뉴스의 최대 개수(`limit`)를 전달받아 `NewsResult`의 리스트로 반환합니다.

### 2.2 `NewsPublisher` (뉴스 발행자 인터페이스)
- **위치**: `src/oop/search/application/NewsPublisher.java`
- **특징**: 수집된 뉴스 데이터를 외부 시스템(콘솔 출력, 파일 저장, 슬랙 메신저 전송, 이슈 등록 등)으로 내보내는 역할을 추상화한 **인터페이스**입니다.
- **주요 기능**:
  - `publish(String topic, List<NewsResult> newsResults)`: 검색 주제(`topic`)와 앞서 검색된 뉴스 데이터 리스트(`newsResults`)를 받아 외부로 발행합니다.

### 2.3 `NewsService` (뉴스 서비스 로직)
- **위치**: `src/oop/search/application/NewsService.java`
- **특징**: `NewsProvider`와 `NewsPublisher`를 조합하여 핵심 비즈니스 워크플로우를 관장하는 **서비스 클래스**입니다.
- **작동 방식 (의존성 주입)**: 
  - 생성자를 통해 `NewsProvider`와 `NewsPublisher`의 구현체를 외부에서 주입받아 사용함으로써 결합도를 낮추었습니다.
- **핵심 메서드**:
  - `search(String searchQuery, int limit)`: 
    1. 주입받은 `newsProvider`를 통해 뉴스를 검색(`fetchNews`)해 옵니다.
    2. 수집된 결과 목록을 `newsPublisher`를 통해 발행(`publish`)합니다.
    3. 결과물을 호출자에게 반환합니다.

---

## 3. 인프라스트럭처 계층 (Infrastructure Layer)
외부 시스템(HTTP 네트워크, 파일 시스템 등)과의 기술적 통신 및 세부 구현을 담당하는 계층입니다.

### 3.1 `AbstractHttpScraper` (HTTP 통신 기반 추상 클래스)
- **위치**: `src/oop/search/infrastructure/AbstractHttpScraper.java`
- **특징**: `NewsProvider` 인터페이스를 구현하기 위한 기반이 되며, HTTP 클라이언트를 사용하여 외부 웹/API와 통신하는 데 필요한 공통 기능을 제공하는 **추상 클래스**입니다.
- **주요 구성**:
  - `httpClient`: 기본 제공되는 `java.net.http.HttpClient`를 활용하여 네트워크 요청을 관리합니다.
  - `endPoint`: 뉴스 수집을 요청할 타겟 URL(엔드포인트)을 생성자를 통해 주입받아 사용합니다.

### 3.2 `NaverNewsProvider` (네이버 API 연동 구현체)
- **위치**: `src/oop/search/infrastructure/NaverNewsProvider.java`
- **특징**: `AbstractHttpScraper`를 상속받아 네이버 Open API를 통해 뉴스를 검색하는 **구체 클래스(Concrete Class)**입니다.
- **주요 구현 방식**:
  - `NEWS_API_URL` 상수를 통해 네이버 뉴스 검색 API 엔드포인트를 지정합니다.
  - 보안 정보(`clientId`, `clientSecret`)와 기본 검색 설정(`category`)을 하드코딩하지 않고 **환경 변수(`System.getenv`)**에서 읽어와 주입하도록 구현하여 보안성과 유연성을 높였습니다.

## 4. 환경 및 설정 파일 (Configuration)
API 키와 같은 민감한 정보 및 개인별 설정 환경을 관리하기 위한 구조를 추가했습니다.

### 4.1 `.env` & `.env.sample` (환경 변수 파일)
- **`.env.sample`**: 프로젝트 실행에 필요한 환경 변수(예: `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `NEWS_CATEGORY`)의 양식을 정의한 템플릿입니다.
- **`.gitignore` 설정**: 실제 비밀키가 담긴 `.env` 파일이 Git 저장소에 커밋되는 것을 방지하기 위해 `.gitignore`에 `.env`를 추가했습니다.
