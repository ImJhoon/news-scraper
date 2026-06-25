# News Scraper (뉴스 브리핑 자동화 봇)

네이버 검색 API를 활용해 원하는 키워드의 최신 뉴스를 수집하고, 정해진 시간마다 GitHub 이슈(Issue)로 요약해 주는 나만의 자동화 스크래퍼입니다. 무거운 외부 라이브러리 없이 **순수 자바(Vanilla Java)**와 **클린 아키텍처(Clean Architecture)**를 기반으로 견고하게 설계되었습니다.

> **Note**: 현재 프로젝트는 네이버 오픈 API를 기반으로 동작하며, GitHub Actions를 통해 서버 없이 완전 자동화가 가능합니다.

---

## 실행 화면 (Preview)

<!-- TODO: GitHub 저장소에 실행 이미지를 업로드한 후, 아래 주석을 풀고 이미지 링크를 넣어주세요. -->
<!-- ![뉴스 브리핑 이슈 화면](여기에_이미지_주소를_넣어주세요) -->
> 수집된 뉴스는 가독성이 높은 **카드형 인용구 구조**와 **썸네일 이미지**가 포함되어 모바일과 PC 환경 모두에서 깔끔하게 렌더링됩니다.

---

## 목차 (Table of Contents)
1. [주요 기능 (Key Features)](#1-주요-기능-key-features)
2. [기술 스택 (Tech Stack)](#2-기술-스택-tech-stack)
3. [프로젝트 아키텍처 (Architecture)](#3-프로젝트-아키텍처-architecture)
4. [시작하기 (Getting Started)](#4-시작하기-getting-started)

---

## 1. 주요 기능 (Key Features)

* **맞춤형 뉴스 수집**: 원하는 키워드(예: 주식, IT), 수집 개수, 정렬 방식(최신순/정확도순)을 자유롭게 설정할 수 있습니다.
* **스마트 썸네일 추출**: 네이버 API가 기본 제공하지 않는 썸네일 이미지를 기사 원문 HTML 크롤링(OG 태그 파싱)을 통해 자동으로 추출합니다.
* **가독성 높은 레이아웃**: 추출된 데이터를 최적화된 마크다운 썸네일 카드 형태로 가공하여 이슈 본문을 예쁘게 구성합니다.
* **완전 자동화 (CI/CD)**: GitHub Actions 스케줄러와 연동되어 서버를 직접 운영할 필요 없이 매일 자동으로 스크래핑 및 이슈를 등록합니다.

## 2. 기술 스택 (Tech Stack)

* **Language**: Java 17 (Record, Text Blocks, 표준 HttpClient 활용)
* **Architecture**: 객체지향 설계(OOP), 클린 아키텍처(Clean Architecture)
* **CI/CD**: GitHub Actions
* **API**: Naver Search API, GitHub REST API

## 3. 프로젝트 아키텍처 (Architecture)

유지보수와 확장이 쉽도록 도메인(Domain), 애플리케이션(Application), 인프라스트럭처(Infrastructure), 프레젠테이션(Presentation) 4개의 계층으로 완벽하게 분리되어 있습니다. 특정 기능(예: 퍼블리셔)을 교체하더라도 핵심 서비스 로직은 전혀 수정할 필요가 없는 유연한 구조를 가집니다.

* **[📂 한눈에 보는 프로젝트 디렉토리 구조도 (Mermaid)](docs/STRUCTURE.md)**
* **[📄 각 클래스별 역할 및 세부 로직 흐름 상세 문서](docs/FEATURES.md)**

## 4. 시작하기 (Getting Started)

이 프로젝트는 터미널을 이용한 로컬 실행과 GitHub Actions를 이용한 자동화 실행을 모두 지원합니다.

### 4.1 로컬 환경에서 실행 (`ConsoleNewsApp`)
1. 루트 폴더에 있는 `.env.sample`을 참고하여 `.env` 파일을 생성합니다.
2. 발급받은 네이버 API 키(`NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)를 입력합니다.
3. IDE(IntelliJ 등)의 Environment Variables에 해당 값들을 주입하고 `ConsoleNewsApp.java`를 실행하면 터미널에서 즉시 테스트가 가능합니다.

### 4.2 GitHub Actions 자동화 세팅 (`GitHubNewsApp`)
이 저장소를 Fork(포크)하신 뒤, 저장소 설정 메뉴(**Settings > Secrets and variables > Actions**)에 다음 값들을 등록하기만 하면 매일 자동으로 브리핑 이슈가 생성됩니다.
* **Secrets**: `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
* **Variables**: `NEWS_QUERY`(키워드), `NEWS_DISPLAY`(수집 개수), `NEWS_CATEGORY`(정렬 기준)
