

## 1. 실행 방법 (필수 요구사항)

### 1) 빌드 + 테스트 + 패키징
```bash
mvn clean package
````

* 위 명령 실행 시 **JUnit4 테스트 수행** 후 jar 생성됩니다.

### 2) 실행

```bash
java -jar target/demoWAS-1.0-SNAPSHOT.jar
```

> ※ 과제 요구사항은 `java -jar was.jar` 형태이므로, 제출 시 최종 산출물을 `was.jar`로 rename 하거나 `pom.xml`에서 artifactId/name을 조정해 `was.jar`로 생성되도록 맞출 수 있습니다.

---

## 2. 프로젝트 구조

### Maven 표준 디렉터리

* `src/main/java` : WAS 구현 소스
* `src/main/resources` : 설정 파일/리소스 (권장: config.json 위치)
* `src/test/java` : JUnit4 테스트
* `webroot/` : Host 별 정적 리소스 루트

예시:

```
demoWAS/
 ├─ src/
 │   ├─ main/
 │   │   ├─ java/
 │   │   │   ├─ http/...
 │   │   │   ├─ config/...
 │   │   │   ├─ security/...
 │   │   │   └─ servlet/...
 │   │   └─ resources/
 │   │       └─ config.json
 │   └─ test/
 │       └─ java/
 │           ├─ http/DispatcherTest.java
 │           ├─ http/StaticFileHandlerTest.java
 │           ├─ http/HttpParserTest.java
 │           ├─ security/SecurityRuleTest.java
 │           └─ servlet/HelloServletTest.java
 ├─ webroot/
 │   ├─ localhost/
 │   │   ├─ index.html
 │   │   ├─ 403.html
 │   │   ├─ 404.html
 │   │   └─ 500.html
 │   ├─ a.com/
 │   │   └─ ...
 │   └─ b.com/
 │       └─ ...
 └─ pom.xml
```

---

## 3. 설정 파일(config.json)

과제 요구사항에 따라 **포트 / host별 httpRoot / 에러 페이지 파일명 / 기본 servlet package**를 JSON으로 관리합니다.

예시:

```json
{
  "port": 8000,
  "defaultServletPackage": "servlet",
  "hosts": {
    "localhost": {
      "httpRoot": "webroot/localhost",
      "errorPages": {
        "403": "403.html",
        "404": "404.html",
        "500": "500.html"
      }
    },
    "a.com": {
      "httpRoot": "webroot/a.com",
      "errorPages": {
        "403": "403.html",
        "404": "404.html",
        "500": "500.html"
      }
    }
  }
}
```

### ConfigLoader 경로 처리(중요)

* Maven 구조에서는 `config.json`을 **`src/main/resources/config.json`**에 두고,
* `ClassLoader.getResourceAsStream`으로 읽는 방식이 가장 안전합니다.
* (현재 `new File("config.json")`는 실행 디렉터리(working directory)에 따라 실패할 수 있음)

---

## 4. 처리 흐름(전체 아키텍처 설명)

### 전체 단계

1. **Socket accept** (HttpServer)
2. **HttpParser.parse(InputStream)**

    * Request line 파싱: `method`, `path`, `queryString`
    * Headers 파싱
    * (옵션) Content-Length 기반 body 파싱
3. **Dispatcher.dispatch(HttpRequest)**

    * Host 헤더 검사 / hostConfig 선택 (VirtualHost)
    * SecurityRule Chain 적용 (403)
    * URL → Servlet 클래스 매핑 시도
    * 실패하면 정적 파일 처리(StaticFileHandler)
4. **HttpResponseWriter.write(OutputStream, HttpResponse)**

    * Status line + headers + body 작성

---

## 5. Host 기반 VirtualHost 처리

* 요청의 `Host` 헤더를 읽어 `ServerConfig.hosts`에서 HostConfig를 찾습니다.
* 동일 IP/포트라도 Host에 따라 다른 `httpRoot`를 사용하도록 구현했습니다.

동작 예:

* `Host: localhost` → `webroot/localhost`
* `Host: a.com` → `webroot/a.com`

---

## 6. 정적 파일 처리(StaticFileHandler)

* 요청 path가 `/` 이면 `/index.html`로 매핑 (기본 문서 매핑)
* `hostConfig.httpRoot`를 기준으로 targetPath 생성 후 파일을 읽어 반환
* 파일이 없거나 디렉터리면 404 에러 페이지 반환

---

## 7. 403/404/500 에러 처리(ErrorResponseBuilder)

* 설정 파일의 host별 errorPages 설정을 기반으로 에러 HTML을 반환합니다.
* 예: 404 발생 시 `{httpRoot}/{errorPages.404}` 파일을 읽어 응답

---

## 8. 보안 규칙(SecurityRule, Chain of Responsibility)

과제 요구사항 보안 규칙을 적용합니다.

### 구현 규칙

* Directory Traversal 차단: `..` 등을 통해 httpRoot 밖 접근 시 403
* `.exe` 확장자 요청 시 403
* 규칙은 체인으로 연결되어 있어 추후 규칙 추가가 쉽도록 구성

---

## 9. SimpleServlet 동작 및 URL 매핑 규칙

과제의 매핑 규칙을 만족하도록 구현했습니다.

### 매핑 규칙

* `/Hello` → `{defaultServletPackage}.Hello`
* `/service.Hello` → `service.Hello`

Dispatcher에서 `resolveClassName()`으로 URL을 클래스명으로 변환한 뒤,
`Class.forName(className)`으로 로딩 + 캐싱하여 성능을 확보했습니다.

---

## 10. 현재 시각 출력 Servlet

과제 요구사항에 따라 현재 시각을 출력하는 SimpleServlet 구현체를 추가합니다.

예시:

* `/Time` → servlet.Time (현재 시각 출력)

---

## 11. 테스트(JUnit4)

`mvn clean package` 시 테스트가 자동 수행됩니다.

테스트는 다음 스펙 검증을 목표로 합니다.

* Host 헤더 미존재 → 400
* 존재하지 않는 host → 403
* `/` → index.html 반환
* 정적 파일 존재 → 200 + Content-Type
* 정적 파일 없음 → 404
* 보안 규칙 위반(../, exe) → 403
* `/Hello?name=jun` 요청 시 Hello servlet이 name 출력
* `/service.Hello` 매핑 정상 동작

> 주의: 단위테스트에서 `HttpRequest`를 만들 때 `path`에는 `?name=jun`을 포함하면 안 됩니다.
> 실제 WAS는 HttpParser가 `path`와 `queryParams`를 분리하지만, 테스트에서 `HttpRequest` 생성 시 이를 직접 분리해서 넣어야 합니다.

---

## 12. 테스트용 curl 명령어 모음

### (1) 기본 문서

```bash
curl.exe -H "Host: localhost" http://localhost:8000/
```

### (2) VirtualHost

```bash
curl.exe -H "Host: a.com" http://localhost:8000/
curl.exe -H "Host: b.com" http://localhost:8000/
```

### (3) 정적 파일

```bash
curl.exe -H "Host: localhost" http://localhost:8000/style.css
curl.exe -H "Host: localhost" http://localhost:8000/not-exist.html
```

### (4) 보안 규칙

> Windows curl/powershell 환경에서 `..`가 정규화될 수 있어, 가능하면 URL 인코딩을 사용합니다.

```bash
curl.exe -H "Host: localhost" "http://localhost:8000/%2e%2e/%2e%2e/etc/passwd"
curl.exe -H "Host: localhost" http://localhost:8000/hack.exe
```

### (5) Servlet 매핑

```bash
curl.exe -H "Host: localhost" http://localhost:8000/Hello
curl.exe -H "Host: localhost" "http://localhost:8000/Hello?name=jun"
curl.exe -H "Host: localhost" http://localhost:8000/service.Hello
curl.exe -H "Host: localhost" http://localhost:8000/Time
```

---

## 13. 요구사항 충족 여부 체크리스트

* [x] Host 헤더 기반 VirtualHost 처리
* [x] 설정파일(JSON)로 포트/host별 루트/에러페이지 관리
* [x] 403/404/500 에러 HTML 반환
* [x] 보안 규칙(상위 디렉터리 접근, exe 차단) + 확장 가능한 구조
* [ ] logback 로깅(일 단위 파일 분리/레벨/stacktrace)
* [x] SimpleServlet 동작 + URL→클래스 매핑 규칙
* [x] 현재 시각 출력 SimpleServlet 구현
* [x] JUnit4 테스트 작성 + mvn clean package로 수행

---

## 14. 향후 확장 고려 사항

* URL→Servlet 매핑을 설정파일 기반으로 확장 가능하도록 Dispatcher 설계를 유지
* SecurityRule 체인을 통해 규칙 추가 용이
* ConfigLoader를 resources 기반 로딩으로 변경하면 실행 안정성 향상
* Logback 적용 시 운영 수준 로깅을 갖추게 됨(과제 요구사항)

```

---

# 추가로 지금 상태에서 “가장 중요한 점검 포인트” 3개

1) **ConfigLoader 경로**
- `new File("config.json")` 방식은 “실행 위치”에 따라 깨질 수 있어.
- 제출용으론 `src/main/resources/config.json` + classpath 로딩으로 바꾸는 걸 추천.

2) **logback 요구사항(스펙 5번)**
- 대화 흐름상 아직 logback 설정/적용 코드는 안 보였어.
- 만약 미구현이면 README 체크리스트에서처럼 **미구현 표시**하고, 시간이 되면 `logback.xml` + logger 적용까지 넣는 게 좋아. :contentReference[oaicite:19]{index=19}

3) **테스트에서 queryString 넣는 방식**
- 단위테스트에서 `HttpRequest`를 직접 만들면 `path`에는 `/Hello`만 넣고,
- `queryParams`에 `name=jun`을 넣어야 실제 파서 동작과 일치해.

---

원하면 내가 다음을 바로 이어서 만들어줄게:
- **logback.xml + Logger 적용 코드(요구사항 5 완성)**
- **JUnit4 테스트 세트 전체(스펙별로 분리, 실제로 통과하도록)**  
- **ConfigLoader를 resources 기반으로 수정 + 경로 문제 완전 해결**
```
