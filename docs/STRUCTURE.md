# 뉴스 스크래퍼(News Scraper) 프로젝트 구조

아래는 Mermaid를 활용하여 현재 `news-scraper` 프로젝트의 디렉토리와 주요 파일 구조를 시각화한 다이어그램입니다. 각 패키지(클린 아키텍처 계층)별로 파일들이 어떻게 분류되어 있는지 한눈에 파악할 수 있습니다.

```mermaid
graph TD
    Root["news-scraper/ (프로젝트 루트)"]
    
    %% 최상위 설정 파일
    Root --> EnvSample[".env.sample (환경변수 템플릿)"]
    Root --> GitIgnore[".gitignore"]
    
    %% 문서
    Root --> Docs["docs/ (문서 폴더)"]
    Docs --> FeaturesMD["FEATURES.md"]
    Docs --> StructureMD["STRUCTURE.md (현재 파일)"]
    
    %% 소스 코드 루트
    Root --> Src["src/"]
    Src --> OOP["oop/search/"]
    
    %% 도메인 계층
    OOP --> Domain["domain/ (핵심 비즈니스 모델)"]
    Domain --> NewsResult["NewsResult.java"]
    Domain --> NewsCategory["NewsCategory.java"]
    
    %% 애플리케이션 계층
    OOP --> App["application/ (비즈니스 워크플로우)"]
    App --> NewsProvider["NewsProvider.java"]
    App --> NewsPublisher["NewsPublisher.java"]
    App --> NewsService["NewsService.java"]
    
    %% 인프라스트럭처 계층
    OOP --> Infra["infrastructure/ (외부 통신 및 기술)"]
    Infra --> AbstractHttpScraper["AbstractHttpScraper.java"]
    Infra --> NaverNewsProvider["NaverNewsProvider.java"]
    
    %% 프레젠테이션 계층
    OOP --> Presentation["presentation/ (사용자 인터페이스)"]
    Presentation --> ConsoleNewsApp["ConsoleNewsApp.java"]
    Presentation --> ConsoleNewsPublisher["ConsoleNewsPublisher.java"]
    
    %% 스타일링
    style Root fill:#f9f,stroke:#333,stroke-width:2px
    style Domain fill:#bbf,stroke:#333,stroke-width:2px
    style App fill:#bbf,stroke:#333,stroke-width:2px
    style Infra fill:#bbf,stroke:#333,stroke-width:2px
    style Presentation fill:#bbf,stroke:#333,stroke-width:2px
    style Docs fill:#bfb,stroke:#333,stroke-width:2px
```

## 계층(Layer) 요약 설명
- **`domain`**: 뉴스 데이터 자체의 규격과 기준(`NewsResult`, `NewsCategory`)을 정의합니다. 외부 환경에 영향을 받지 않는 가장 순수한 계층입니다.
- **`application`**: 도메인을 활용하여 뉴스를 검색하고 발행하는 핵심적인 작업 흐름(인터페이스 및 `NewsService`)을 정의합니다.
- **`infrastructure`**: 외부 네이버 API와의 실제 네트워크 통신 등 구체적인 기술 구현(`NaverNewsProvider` 등)을 담당합니다.
- **`presentation`**: 사용자와 직접 맞닿는 콘솔 입출력(`ConsoleNewsApp`, `ConsoleNewsPublisher`)을 담당합니다.
