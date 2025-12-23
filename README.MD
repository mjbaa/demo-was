## 구조

요청 처리 흐름은 다음과 같습니다.

1. **HttpServer**
   - `ServerSocket`을 통해 클라이언트 연결을 수락하고,
     요청 처리를 위한 기본 입구 역할을 수행합니다.

2. **HttpParser**
   - 소켓의 `InputStream`을 읽어 HTTP 요청을 파싱하고,
     이를 내부 표현인 `HttpRequest` 객체로 변환합니다.

3. **Dispatcher**
   - Host 헤더를 기준으로 가상 호스트를 결정하고,
     이후 요청의 전체 흐름을 제어합니다.

4. **Security Chain**
   - 디렉터리 트래버설, 실행 파일 요청 등 보안 규칙을 순차적으로 검사합니다.
   - 규칙에 위배되는 경우 즉시 에러 응답을 반환합니다.

5. **요청 처리 단계**
   - 요청 경로에 해당하는 Servlet이 존재하면 Servlet을 통해 동적 응답을 생성합니다.
   - Servlet이 존재하지 않는 경우 정적 파일 요청으로 간주하여 파일을 서빙합니다.

6. **HttpResponseWriter**
   - 최종적으로 생성된 `HttpResponse` 객체를
     HTTP 프로토콜 형식에 맞게 직렬화하여 클라이언트로 전송합니다.

---

## 주요 패키지 구조

```
src/main/java
 ├─ HttpServer
 ├─ http
 │   ├─ HttpParser
 │   ├─ Dispatcher
 │   ├─ HttpRequest / HttpResponse
 │   ├─ StaticFileHandler
 │   ├─ ErrorResponseBuilder
 │   └─ HttpResponseWriter
 │
 ├─ servlet
 │   ├─ SimpleServlet
 │   ├─ Hello
 │   └─ Time
 │
 ├─ security
 │   ├─ SecurityRule
 │   ├─ AbstractSecurityRule
 │   ├─ DirectoryTraversalRule
 │   └─ ExecutableExtensionRule
 │
 ├─ config
 │   ├─ ConfigLoader
 │   ├─ ServerConfig
 │   └─ HostConfig
 │
 └─ resources
     ├─ config.json
     └─ logback.xml
```

정적 파일은 실행 환경에서 가상 호스트별로 분리된 외부 디렉터리를 사용합니다.

```
webroot/
 ├─ localhost/
 │   ├─ index.html
 │   ├─ hello.html
 │   ├─ 403.html
 │   ├─ 404.html
 │   └─ 500.html
 └─ a.com/
     ├─ index.html
     ├─ hello.html
     ├─ 403.html
     ├─ 404.html
     └─ 500.html
```

---

## 구성 요소 설명

### HttpServer

* `ServerSocket` 기반으로 클라이언트 연결을 수신합니다.
* 요청 처리 중 발생하는 예외는 서버 전체를 종료하지 않고 로그로 기록합니다.
* logback과 slf4j를 이용하여 로그 레벨 분리를 적용했습니다.


---

### HttpParser

* Socket `InputStream`을 파싱하여 `HttpRequest` 객체로 변환합니다.
* Request Line에서 **Path와 Query String을 분리**하여 저장합니다.
* Query Parameter는 Servlet에서 바로 사용할 수 있도록 미리 병합합니다.

> 보안 Rule에서 Path를 처리할 때 Query String이 섞이지 않도록 설계했습니다.

---

### Dispatcher

#### 역할

1. Host 헤더 기반 가상 호스트 선택
2. Security Chain 실행
3. Servlet 처리 시도
4. 정적 파일 처리
5. 에러 발생 시 호스트별 에러 페이지 반환

#### Servlet 매핑 전략

* `/Hello` → `servlet.Hello`
* `/Time` → `servlet.Time`

* 하드코딩된 매핑 테이블 대신 **URL → 클래스명 규칙 기반 로딩**을 사용하였습니다.
* Reflection + 캐시를 통해 서블릿 재사용합니다.

---

### Security Filter

**Chain of Responsibility 패턴**으로 구현했습니다.


* 각 Rule은 자신의 책임만 검사
* 통과 시 다음 Rule로 위임
* 차단 시 즉시 HttpResponse 반환

#### 적용된 보안 규칙

* `../` 디렉터리 접근 차단
* `.exe` 확장자 요청 차단

---

StaticFileHandler

* HostConfig의 `httpRoot` 기준으로 정적 파일을 서빙합니다.
* Path Traversal 공격 방지를 위해 `normalize()` + `startsWith()` 검증을 수행합니다.
* `/` 요청은 자동으로 `/index.html`로 매핑됩니다.

---

### ErrorResponseBuilder

* 상태 코드별로 호스트 전용 에러 페이지를 반환합니다.

---

### Servlet 구현

#### Hello Servlet

`Hello, ` + name 출력

* Query Parameter가 없을 경우에도 NPE 없이 동작하도록 HttpRequest에서 기본값 처리

#### Time Servlet

현재 서버 시각 출력

---

## 설정 파일 (config.json)

```json
{
  "port": 8000,
  "defaultServletPackage": "servlet",
  "hosts": {
    "localhost": { ... },
    "a.com": { ... }
  }
}
```

* 가상 호스트, 정적 파일 루트, 에러 페이지를 외부 설정으로 분리
* 코드 수정 없이 서버 동작 변경 가능

---

## 로깅

* logback 프레임워크 사용
* 로그 파일을 일 단위로 분리


---

## 테스트

### 테스트 원칙

* 실제 파일 시스템 기반 검증
* 문자열 비교 대신 byte 배열 비교
* 가상 호스트별 동작을 명확히 검증

### 주요 테스트

* DispatcherTest
* HostRoutingTest
* HelloServletTest
* SecurityRuleTest


---
## 추가 구현 사항

### 멀티스레드 요청 처리

단일 스레드 방식의 한계를 보완하기 위해 멀티스레드 구조를 적용했습니다.

* `ExecutorService` 기반의 고정 크기 스레드 풀을 사용하여 요청을 처리합니다.
* 스레드 풀 크기는 `config.json`에서 설정하도록 분리하여 환경에 따라 조정할 수 있도록 했습니다.
* 클라이언트 연결 수락(`accept`)과 실제 요청 처리를 분리하여, 서버가 다수의 동시 요청을 안정적으로 처리할 수 있도록 설계했습니다.

### Graceful Shutdown

서버 종료 시 진행 중인 요청이 갑자기 중단되지 않도록 Graceful Shutdown을 구현했습니다.

* JVM 종료 시 ShutdownHook을 등록하여 스레드 풀을 안전하게 종료합니다.
* 일정 시간 동안 기존 작업이 완료되기를 대기한 후, 완료되지 않은 작업이 있을 경우 강제 종료를 수행합니다

### Dispatcher의 Thread-Safety 보장

멀티스레드 환경에서 Dispatcher가 안전하게 동작하도록 내부 구조를 개선했습니다.

* Servlet 캐시는 ConcurrentHashMap을 사용하여 동시 접근 문제를 방지했습니다.
* computeIfAbsent()를 활용해 Servlet 인스턴스 생성과 캐싱을 원자적으로 처리했습니다.
