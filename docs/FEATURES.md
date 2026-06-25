# 뉴스 스크래퍼(News Scraper) 프로젝트 가이드 및 구조

이 문서는 뉴스 스크래퍼 프로젝트의 전반적인 구조와 각 클래스/파일의 역할을 처음 프로젝트를 보는 사람도 이해하기 쉽도록 아주 자세하게 정리한 문서입니다. 

👉 **[한눈에 보는 프로젝트 디렉토리 구조도(Mermaid) 보기](STRUCTURE.md)**

객체지향 프로그래밍(OOP) 원칙과 클린 아키텍처 관점에서 코드를 어떻게 나누었는지, 계층별로 어떤 역할을 하는지 설명합니다.

---

## 📑 목차 (Table of Contents)
1. [실행 가이드 (Getting Started)](#1-실행-가이드-getting-started)
2. [도메인 계층 (Domain Layer)](#2-도메인-계층-domain-layer)
3. [애플리케이션 계층 (Application Layer)](#3-애플리케이션-계층-application-layer)
4. [인프라스트럭처 계층 (Infrastructure Layer)](#4-인프라스트럭처-계층-infrastructure-layer)
5. [프레젠테이션 계층 (Presentation Layer)](#5-프레젠테이션-계층-presentation-layer)
6. [환경 및 설정 (Configuration & CI/CD)](#6-환경-및-설정-configuration--cicd)

---

## 1. 🚀 실행 가이드 (Getting Started)

이 프로젝트는 두 가지 환경에서 실행할 수 있습니다.

### 1.1 로컬 환경에서 실행 (`ConsoleNewsApp`)
1. 프로젝트 루트에 있는 `.env.sample` 파일을 복사하여 `.env` 파일을 생성합니다.
2. 네이버 개발자 센터에서 발급받은 `NAVER_CLIENT_ID`와 `NAVER_CLIENT_SECRET`을 `.env` 파일에 작성합니다.
3. IDE(IntelliJ 등)에서 `ConsoleNewsApp.java`를 찾아 **Run Configuration**의 **Environment Variables**에 위 설정값과 `NEWS_CATEGORY`(예: `SIM`) 값을 주입합니다.
4. 프로그램을 실행하면 콘솔에서 검색어를 입력하고 뉴스를 조회할 수 있습니다.

### 1.2 GitHub Actions 자동화 실행 (`GitHubNewsApp`)
이 프로젝트는 자동으로 뉴스를 수집해 GitHub 이슈(Issue)로 발행하도록 설정되어 있습니다.
포크(Fork)한 저장소에서 작동하게 하려면 **Settings > Secrets and variables > Actions**로 이동하여 다음 값을 등록해야 합니다.
* **Secrets**: `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
* **Variables**: `NEWS_QUERY` (검색할 키워드, 예: `주식`), `NEWS_DISPLAY` (검색 건수, 예: `10`), `NEWS_CATEGORY` (정렬 기준, 예: `SIM` 또는 `DATE`)

---

## 2. 도메인 계층 (Domain Layer)
프로젝트의 핵심 비즈니스 개념(데이터 모델, 규칙)을 정의하는 곳입니다.

### 2.1 `NewsResult` (뉴스 데이터 모델)
* **어떤 파일인가요?**: 검색된 뉴스 기사 한 건의 데이터를 담아 전달하는 그릇(DTO)입니다.
* **특징**: 자바 16의 `record`를 사용하여 불변 객체(Immutable)로 설계되었으며, `title`, `description`, `url`, `pubDate`, `imageUrl`(썸네일) 데이터를 가집니다.

### 2.2 `NewsCategory` (검색 정렬 기준)
* **어떤 파일인가요?**: 뉴스 정렬 기준(정확도순, 최신순)을 미리 정의해 둔 `Enum` 클래스입니다.
* **특징**: 네이버 API 파라미터 값(`sim`, `date`)과 한글 설명("정확도순", "최신순")을 매칭하여 오타를 방지하고 안전하게 사용합니다.

---

## 3. 애플리케이션 계층 (Application Layer)
도메인 데이터를 활용하여 '뉴스 검색 -> 처리 -> 발행(출력)'이라는 워크플로우를 통제하는 곳입니다.

### 3.1 `NewsProvider` (뉴스 제공자 인터페이스)
* **어떤 파일인가요?**: "뉴스를 검색해서 가져온다"는 행동을 정의해 둔 인터페이스입니다. `fetchNews` 메서드를 가집니다.

### 3.2 `NewsPublisher` (뉴스 발행자 인터페이스)
* **어떤 파일인가요?**: "수집된 뉴스를 어딘가로 내보낸다"는 행동을 정의해 둔 인터페이스입니다. `publish` 메서드를 가집니다.

### 3.3 `NewsService` (뉴스 서비스 로직)
* **어떤 파일인가요?**: 실제로 일을 지휘하는 '오케스트라 지휘자' 역할의 클래스입니다.
* **특징 (의존성 주입)**: 직접 API를 호출하지 않고 생성자로 주입받은 Provider와 Publisher 부품을 활용하여 작업을 수행합니다.

---

## 4. 인프라스트럭처 계층 (Infrastructure Layer)
외부 API, 네트워크 등과의 통신을 직접 담당하는 구체적인 기술 계층입니다.

### 4.1 `AbstractHttpClient` (HTTP 통신 공통 클래스)
* **어떤 파일인가요?**: 웹(HTTP) 통신에 필요한 `HttpClient` 인스턴스와 공통 도구들을 모아둔 기반(Base) 추상 클래스입니다.

### 4.2 `NaverNewsProvider` (네이버 뉴스 연동 구현체)
* **어떤 파일인가요?**: `NewsProvider` 인터페이스를 따르고 네이버 오픈 API와 직접 소통하는 구체적 구현 클래스입니다.
* **데이터 파싱 (수동)**: 응답받은 순수 텍스트(JSON) 문자열을 `String.split()`을 활용한 `cutText` 헬퍼 메서드로 직접 잘라내어 `NewsResult` 객체로 변환합니다. (외부 라이브러리 미사용)
* **스마트 썸네일 추출**: 네이버 검색 API는 기본적으로 사진을 제공하지 않으므로, 이 클래스에서 각 기사 원문 링크에 추가로 HTTP 접속하여 HTML 소스 코드 내의 OG 태그(`og:image`)를 긁어오는(크롤링) 똑똑한 기능이 내장되어 있습니다.

### 4.3 `ConsoleNewsPublisher` (콘솔 출력 구현체)
* **어떤 파일인가요?**: `NewsPublisher` 인터페이스를 구현하여, 수집된 뉴스를 콘솔 화면에 예쁘게 출력합니다.
* **특징**: Java 15의 **텍스트 블록(`"""..."""`)**과 `String.formatted()`를 사용하여 가독성 좋게 포맷팅합니다.

### 4.4 `GitHubNewsPublisher` (GitHub Issue 발행 구현체)
* **어떤 파일인가요?**: 수집된 뉴스 데이터를 GitHub 저장소의 새로운 이슈(Issue)로 등록해주는 기능입니다.
* **특징**: 가독성이 뛰어난 카드형 인용구(Blockquote) 구조를 지원하며, 내부 헬퍼 메서드(`cleanTextForJson`)를 통해 HTML 태그 제거 및 JSON 문법 에러(이스케이프 처리)를 방어합니다. 또한, 추출된 썸네일 이미지는 HTML `<img>` 태그를 활용해 크기를 300px로 제한하여 레이아웃을 예쁘게 유지합니다.

---

## 5. 프레젠테이션 계층 (Presentation Layer)
사용자 인터페이스 및 어플리케이션의 진입점(Entry Point) 역할을 담당합니다.

### 5.1 `ConsoleNewsApp` (로컬 실행 진입점)
* **어떤 파일인가요?**: 개발자나 사용자가 로컬 터미널에서 스크래퍼를 실행할 때 사용하는 메인(`main`) 클래스입니다.
* **특징**: 스캐너(`Scanner`)를 통해 사용자로부터 검색 키워드와 개수를 직접 입력받아 `NewsService`를 호출합니다.

### 5.2 `GitHubNewsApp` (자동화 실행 진입점)
* **어떤 파일인가요?**: GitHub Actions 스크립트 상에서 자동으로 실행될 때 호출되는 메인(`main`) 클래스입니다.
* **특징**: 사용자 입력 대신 환경변수(`NEWS_QUERY`, `NEWS_DISPLAY`)에 설정된 값을 읽어들여 묵묵히 스크래핑과 이슈 발행을 수행합니다.

---

## 6. 환경 및 설정 (Configuration & CI/CD)

### 6.1 `.env` & `.env.sample` (API 키 및 설정 관리)
* **특징**: 보안이 중요한 API 키를 보호하기 위해 `.gitignore`에 등록하여 절대 업로드되지 않도록 막고, 대신 뼈대만 있는 `.env.sample` 파일을 제공합니다.

### 6.2 `.github/workflows/news-scraper.yml` (GitHub Actions)
* **특징**: 클라우드 상에서 정해진 시간이나 이벤트(Push 등)에 맞춰 자동으로 `GitHubNewsApp`을 실행(컴파일 및 구동)시켜주는 자동화 스크립트 파일입니다. 이를 통해 24시간 내내 PC를 켜두지 않아도 뉴스가 수집됩니다.
