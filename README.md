# 📰 News Scraper (자동 뉴스 브리핑 봇)

네이버 뉴스를 검색하여 GitHub 이슈(Issue)로 매일 자동 발행해주는 나만의 맞춤형 뉴스 브리핑 스크래퍼입니다. 
외부 JSON 파싱 라이브러리(Gson, Jackson 등)에 의존하지 않고, **순수 자바(Vanilla Java)** 와 **클린 아키텍처(Clean Architecture)** 를 기반으로 견고하게 설계되었습니다.

---

## ✨ 주요 기능 (Key Features)

* **맞춤형 뉴스 검색**: 네이버 검색 API를 활용하여 원하는 키워드(예: `주식`, `IT`)의 최신 뉴스를 수집합니다.
* **마크다운(Markdown) 리포트 자동 생성**: 수집된 뉴스를 읽기 편한 표(Table) 형태로 예쁘게 가공하여 보여줍니다.
* **완전 자동화 (CI/CD)**: GitHub Actions 스케줄러와 연동되어 지정된 시간마다 자동으로 스크래핑을 수행하고 저장소에 이슈(Issue)를 등록합니다.
* **콘솔 지원**: 로컬 환경(터미널)에서도 사용자로부터 검색어를 직접 입력받아 테스트해볼 수 있습니다.

---

## 🛠 기술 스택 (Tech Stack)

* **언어**: Java 17 (Record, Text Blocks, 표준 HttpClient 활용)
* **아키텍처**: 객체지향 설계(OOP) 및 클린 아키텍처
* **자동화**: GitHub Actions (CI/CD)
* **외부 통신**: Naver Search API, GitHub REST API

---

## 🚀 시작하기 (Getting Started)

이 프로젝트는 로컬에서 직접 실행하거나, GitHub Actions를 통해 24시간 자동화할 수 있습니다. 

### 1. 로컬에서 직접 실행하기
루트 폴더에 있는 `.env.sample`을 참고하여, 환경 변수를 설정하고 `ConsoleNewsApp.java`를 실행하면 됩니다.
👉 **[상세 로컬 실행 가이드 보기](docs/FEATURES.md#11-로컬-환경에서-실행-consolenewsapp)**

### 2. GitHub Actions로 내 저장소에 자동화하기
이 저장소를 Fork(포크)하신 뒤, 저장소 설정(`Settings > Secrets and variables > Actions`)에 네이버 API 키와 검색 키워드만 등록하시면 매일 자동으로 이슈가 생성됩니다!
👉 **[상세 GitHub Actions 설정 가이드 보기](docs/FEATURES.md#12-github-actions-자동화-실행-githubnewsapp)**

---

## 📂 프로젝트 구조 (Architecture)

프로젝트는 유지보수와 확장이 용이하도록 클린 아키텍처 관점에서 4개의 계층(`Domain`, `Application`, `Infrastructure`, `Presentation`)으로 완벽하게 분리되어 있습니다.

* **👉 [한눈에 보는 다이어그램(구조도) 확인하기](docs/STRUCTURE.md)**
* **👉 [클래스별 상세 역할(명세서) 확인하기](docs/FEATURES.md)**

---

## 💡 개발 포인트

1. **외부 라이브러리 최소화**: 학습과 성능을 위해 무거운 외부 라이브러리를 배제하고, `String.split()`을 활용한 수동 JSON 파싱 및 Java 11+ 내장 `HttpClient`를 활용하여 가볍게 구현했습니다.
2. **다형성(Polymorphism) 활용**: `NewsPublisher` 인터페이스를 통해 콘솔 화면 출력(`ConsoleNewsPublisher`)과 깃허브 이슈 생성(`GitHubNewsPublisher`)을 자유자재로 교체할 수 있도록 설계했습니다.
