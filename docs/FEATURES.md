# 뉴스 스크래퍼(News Scraper) 프로젝트 가이드 및 구조

이 문서는 뉴스 스크래퍼 프로젝트의 전반적인 구조와 각 클래스/파일의 역할을 처음 프로젝트를 보는 사람도 이해하기 쉽도록 아주 자세하게 정리한 문서입니다. 

👉 **[한눈에 보는 프로젝트 디렉토리 구조도(Mermaid) 보기](STRUCTURE.md)**

객체지향 프로그래밍(OOP) 원칙과 클린 아키텍처 관점에서 코드를 어떻게 나누었는지, 각 클래스가 어떤 로직 흐름으로 데이터를 처리하는지 상세히 설명합니다.

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
4. 프로그램을 실행하면 터미널 프롬프트에서 검색어를 입력하여 실시간으로 뉴스를 조회할 수 있습니다.

### 1.2 GitHub Actions 자동화 실행 (`GitHubNewsApp`)
이 프로젝트는 자동으로 뉴스를 수집해 GitHub 이슈(Issue)로 발행하도록 설정되어 있습니다.
포크(Fork)한 저장소에서 작동하게 하려면 **Settings > Secrets and variables > Actions**로 이동하여 다음 값을 등록해야 합니다.
* **Secrets**: `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
* **Variables**: `NEWS_QUERY` (검색할 키워드, 예: `주식`), `NEWS_DISPLAY` (검색 건수, 예: `10`), `NEWS_CATEGORY` (정렬 기준, 예: `SIM` 또는 `DATE`)

---

## 2. 도메인 계층 (Domain Layer)
프로젝트의 핵심 비즈니스 개념(데이터 모델, 규칙)을 정의하는 곳입니다. 특정 외부 라이브러리나 기술에 의존하지 않는 가장 순수한 계층입니다.

### 2.1 `NewsResult` (뉴스 데이터 모델)
* **역할**: 검색된 뉴스 기사 한 건의 데이터를 애플리케이션 내에서 전달하기 위한 표준 규격(DTO)입니다.
* **로직 흐름**: 외부 API(네이버)의 복잡한 JSON 응답을 그대로 사용하지 않고, 우리 프로그램이 꼭 필요로 하는 정보만을 담아 각 계층(서비스, 퍼블리셔)으로 넘깁니다.
* **특징**: 자바 16의 `record`를 사용하여 데이터를 읽기만 가능하도록 불변 객체(Immutable)로 설계했습니다. `title`, `description`, `url`, `pubDate`, `imageUrl`(썸네일) 데이터를 안전하게 보관합니다.

### 2.2 `NewsCategory` (검색 정렬 기준)
* **역할**: 뉴스 검색 시 API로 넘길 정렬 기준(정확도순, 최신순)을 캡슐화한 `Enum` 클래스입니다.
* **특징**: `"sim"`, `"date"` 같은 단순 문자열(Magic String)을 코드에 흩뿌려 두면 오타로 인한 버그가 발생하기 쉽습니다. 이를 방지하기 위해 네이버 API 파라미터 값과 한글 설명을 묶어 타입 안정성(Type Safety)을 확보했습니다.

---

## 3. 애플리케이션 계층 (Application Layer)
도메인 모델을 활용하여 '뉴스 검색 -> 처리 -> 발행' 이라는 프로젝트의 핵심 비즈니스 워크플로우를 통제하는 곳입니다.

### 3.1 `NewsProvider` (뉴스 제공자 인터페이스)
* **역할**: "외부 어딘가에서 뉴스를 검색해서 가져온다"는 행위를 추상화한 설계도입니다.
* **로직**: 검색어(`searchQuery`)와 개수(`limit`)를 인자로 받아 표준 규격인 `List<NewsResult>`를 반환하는 책임을 가집니다.

### 3.2 `NewsPublisher` (뉴스 발행자 인터페이스)
* **역할**: "수집된 뉴스를 원하는 매체에 내보낸다"는 행위를 추상화한 설계도입니다.
* **로직**: 검색 주제(`topic`)와 `List<NewsResult>` 데이터를 인자로 받아, 화면에 그리거나 외부 시스템에 전송하는 책임을 가집니다.

### 3.3 `NewsService` (뉴스 서비스 로직)
* **역할**: 도메인 객체와 인프라 객체를 조율하여 전체 흐름을 제어하는 오케스트레이션(Orchestration) 클래스입니다.
* **로직 흐름**:
  1. 사용자(프레젠테이션 계층)로부터 `topic`과 `display`를 전달받아 `NewsProvider`에게 뉴스 수집을 지시합니다.
  2. 수집된 `List<NewsResult>`를 `NewsPublisher`에게 전달하여 출력을 지시합니다.
* **특징 (의존성 주입 및 OCP 준수)**: 서비스는 자신이 어떤 Provider(네이버인지 구글인지)나 Publisher(콘솔인지 깃허브인지)를 사용하는지 전혀 모릅니다. 오직 외부에서 생성자로 주입(DI)받아 사용하므로, 추후 '슬랙(Slack) 퍼블리셔'가 추가되어도 `NewsService`의 코드는 단 한 줄도 수정할 필요가 없습니다.

---

## 4. 인프라스트럭처 계층 (Infrastructure Layer)
인터페이스의 실제 구현체들이 모여있는 곳으로, 외부 API 호출, 파일 쓰기 등 구체적인 기술 스택을 다루는 계층입니다.

### 4.1 `AbstractHttpClient` (HTTP 통신 공통 기반 클래스)
* **역할**: `java.net.http.HttpClient`를 캡슐화하여 네트워크 통신에 필요한 중복 코드를 제거하는 기반 클래스입니다.
* **특징**: 모든 통신 클래스가 이 클래스를 상속받게 함으로써, DRY(Don't Repeat Yourself) 원칙을 지키고 향후 타임아웃이나 공통 헤더 설정이 필요할 때 이곳 한 군데만 수정하면 되도록 유연하게 설계했습니다.

### 4.2 `NaverNewsProvider` (네이버 API 연동 구현체)
* **역할**: `NewsProvider`의 구현체로, 네이버 검색 API와 통신하고 데이터를 가공합니다.
* **로직 흐름**:
  1. **요청 구성**: `URLEncoder`로 한글 검색어를 인코딩하고, 환경 변수에서 꺼낸 `CLIENT_ID`, `CLIENT_SECRET`을 HTTP 헤더에 담아 GET 요청을 보냅니다.
  2. **수동 파싱**: 외부 라이브러리(Gson 등) 없이 `String.split()` 기반의 `cutText` 헬퍼 메서드를 사용해 순수 자바 문법만으로 JSON 텍스트에서 제목, 요약, 링크를 정밀하게 추출해냅니다.
  3. **스마트 썸네일 크롤링**: 네이버 API는 사진을 주지 않기 때문에, 각 기사의 원문 링크(`link`)로 한 번 더 HTTP 접속을 시도합니다. 응답받은 HTML 소스 코드에서 정규식 및 문자열 처리로 `<meta property="og:image">` 태그를 찾아내어 기사 썸네일을 긁어오는 심화 로직을 수행합니다.

### 4.3 `GeminiSummarizer` (구글 AI 요약 연동 체)
* **역할**: 구글의 최신 생성형 AI 모델(`gemini-3.1-flash-lite`)과 통신하여, 길고 복잡한 기사 원문을 3줄로 깔끔하게 요약해주는 특수 헬퍼 클래스입니다.
* **로직 흐름**:
  1. 기사 원문을 입력받으면 API 한도 초과 및 JSON 파싱 에러를 막기 위해 특수기호(`"`, `\n`)를 치환하고 길이를 제한합니다.
  2. "핵심만 3줄로 요약하고 각 줄은 '-' 기호로 시작하라"는 정교한 프롬프트를 JSON으로 조립하여 구글 서버로 `POST` 요청을 쏩니다.
  3. 응답받은 복잡한 JSON 속에서 바닐라 자바의 정규식과 `split`만을 사용하여 정확히 텍스트 추출(`"text": "..."` 부분)만 수행합니다. 만약 API 키가 없거나 에러가 나면 조용히 빈 값을 반환하여 시스템 에러를 방지합니다.

### 4.4 `ConsoleNewsPublisher` (콘솔 출력 구현체)
* **역할**: `NewsPublisher`의 구현체로, 수집된 뉴스를 터미널 환경에서 예쁘게 보여줍니다.
* **로직 흐름**: 전달받은 `NewsResult` 리스트를 순회하며, Java 15의 **텍스트 블록(`"""..."""`)**과 `String.formatted()`를 사용하여 가독성 높은 템플릿에 맞춰 한 건씩 화면에 출력합니다.

### 4.5 `GitHubNewsPublisher` (GitHub Issue 발행 구현체)
* **역할**: `NewsPublisher`의 구현체로, 뉴스를 GitHub 저장소의 새로운 이슈(Issue)로 등록합니다.
* **로직 흐름**:
  1. **마크다운 빌드**: `StringBuilder`를 사용하여 모바일/PC 모두에서 보기 좋은 디자인의 마크다운 텍스트를 조립합니다. 추출된 썸네일 이미지는 HTML `<img>` 태그를 활용해 `width=300`으로 렌더링을 최적화합니다.
  2. **문자열 정제**: 제목이나 AI 요약 내용에 큰따옴표(`"`)가 포함되거나 줄바꿈 형식이 어긋나면 GitHub API 통신 시 JSON 형식이 깨질 수 있습니다. 이를 방어하기 위해 내부 헬퍼 메서드(`cleanTextForJson`)로 줄바꿈 문자를 `\\n`으로 이스케이프 처리하여 목록형 리스트가 잘 출력되도록 합니다.
  3. **이슈 생성**: 완성된 마크다운을 JSON 페이로드에 담아 `GITHUB_TOKEN` 인증 헤더와 함께 POST 요청으로 전송합니다.

---

## 5. 프레젠테이션 계층 (Presentation Layer)
프로그램의 진입점(Entry Point)이자, 최종 사용자의 입력을 받아 애플리케이션 계층(서비스)에 전달하는 역할을 합니다.

### 5.1 `ConsoleNewsApp` (로컬 터미널 진입점)
* **로직 흐름**: 
  1. `Scanner` 객체를 생성하여 사용자에게 검색 키워드(`topic`)와 수집할 뉴스 개수(`display`)를 묻습니다.
  2. `NaverNewsProvider`와 `ConsoleNewsPublisher` 객체를 직접 생성(new)하여 `NewsService`의 생성자에 주입합니다(의존성 조립).
  3. 조립된 `NewsService`를 호출하여 전체 워크플로우를 가동합니다.

### 5.2 `GitHubNewsApp` (자동화 스크립트 진입점)
* **로직 흐름**: 
  1. 사용자 입력이 없는 자동화 환경이므로, `System.getenv()`를 통해 운영체제(또는 GitHub Actions) 환경 변수에 등록된 `NEWS_QUERY`, `NEWS_DISPLAY` 값을 조용히 읽어옵니다.
  2. `NaverNewsProvider`와 `GitHubNewsPublisher` 인스턴스를 생성하고 `NewsService`에 주입합니다.
  3. `NewsService`를 실행하여 묵묵히 스크래핑을 수행하고 이슈 생성을 마친 뒤 프로그램을 종료합니다.

---

## 6. 환경 및 설정 (Configuration & CI/CD)

### 6.1 `.env` & `.env.sample` (보안 및 설정 관리)
* **역할**: 하드코딩하면 위험한 민감 정보(API Key)를 소스 코드로부터 분리하여 안전하게 관리합니다.
* **특징**: 실제 키가 들어있는 `.env`는 `.gitignore`에 의해 버전 관리에서 제외되며, 동료 개발자를 위해 어떤 환경 변수가 필요한지 뼈대만 남긴 `.env.sample`을 공유합니다.

### 6.2 `.github/workflows/news-scraper.yml` (CI/CD 자동화)
* **역할**: 지정된 스케줄(매일 특정 시간) 또는 이벤트(Push)에 따라 클라우드 환경(Ubuntu)에서 `GitHubNewsApp`을 자동 실행해 주는 GitHub Actions 명세서입니다.
* **로직 흐름**: 소스 코드를 내려받고(Checkout) -> Java 17 버전을 세팅한 뒤 -> `javac`로 소스를 컴파일하고 -> 등록해둔 Secrets와 Variables를 환경변수로 주입하여 -> `java` 명령어로 최종 구동시키는 파이프라인을 자동화합니다.
