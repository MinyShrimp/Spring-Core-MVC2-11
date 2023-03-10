# 파일 업로드

## 프로젝트 생성

* 프로젝트 선택
    * Project: Gradle Project
    * Language: Java
    * Spring Boot: 3.0.2
* Project Metadata
    * Group: hello
    * Artifact: spring-core-mvc2-11
    * Name: spring-core-mvc2-11
    * Package name: hello.spring-core-mvc2-11
    * Packaging: Jar
    * Java: 17
* Dependencies
    * Spring Web, Lombok, Thymeleaf

## 파일 업로드 소개

### HTML Form 전송 방식

* `application/x-www-form-urlencoded`
* `multipart/form-data`

### application/x-www-form-urlencoded

![img.png](img.png)

`application/x-www-form-urlencoded`방식은 HTML 폼 데이터를 서버로 전송하는 **가장 기본적인 방법**이다.
Form 태그에 별도의 `enctype`옵션이 없으면 웹 브라우저는 요청 HTTP 메시지의 헤더에 다음 내용을 추가한다.

```
Content-Type: application/x-www-form-urlencoded
```

그리고 폼에 입력한 전송할 항목을 HTTP Body에 문자로 `username=kim&age=20`와 같이 `&`로 구분해서 전송한다.

파일을 업로드 하려면 파일은 문자가 아니라 **바이너리 데이터**를 전송해야 한다.
문자를 전송하는 이 방식으로 파일을 전송하기는 어렵다.
그리고 또 한가지 문제가 더 있는데, 보통 폼을 전송할 때 파일만 전송하는 것이 아니라는 점이다.

이름과 나이는 문자로 전송하고, 첨부파일은 바이너리로 전송해야 한다.
여기에서 문제가 발생한다. **문자와 바이너리를 동시에 전송**해야 하는 상황이다.

이 문제를 해결하기 위해 HTTP는 `multipart/form-data`라는 전송 방식을 제공한다.

### multipart/form-data

![img_1.png](img_1.png)

이 방식을 사용하려면 Form 태그에 별도의 `enctype="multipart/form-data"`를 지정해야 한다.

`multipart/form-data` 방식은 다른 종류의 여러 파일과 폼의 내용 함께 전송할 수 있다. (그래서 이름이 multipart 이다.)

폼의 입력 결과로 생성된 HTTP 메시지를 보면 각각의 전송 항목이 구분이 되어있다.
`Content-Disposition`이라는 항목별 헤더가 추가되어 있고 여기에 부가 정보가 있다.

예제에서는 `username`, `age`, `file`이 각각 분리되어 있고,
폼의 일반 데이터는 각 항목별로 문자가 전송되고,
파일의 경우 파일 이름과 Content-Type이 추가되고 바이너리 데이터가 전송된다.

`multipart/form-data`는 이렇게 각각의 항목을 구분해서, 한번에 전송하는 것이다.

### Part

`multipart/form-data`는 `application/x-www-form-urlencoded`와 비교해서 매우 복잡하고 각각의 부분(`Part`)로 나누어져 있다.
그렇다면 이렇게 복잡한 HTTP 메시지를 서버에서 어떻게 사용할 수 있을까?

### 참고

> 참고<br>
> `multipart/form-data`와 폼 데이터 전송에 대한 더 자세한 내용은
> [모든 개발자를 위한 HTTP 웹 기본 지식 강의](https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC)를
> 참고하자.

## 서블릿과 파일 업로드 1

### 예제

#### ServletUploadController V1

```java

@Slf4j
@Controller
@RequestMapping("/servlet/v1")
public class ServletUploadControllerV1 {
    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(
            HttpServletRequest req
    ) throws ServletException, IOException {
        log.info("request = {}", req);

        String itemName = req.getParameter("itemName");
        log.info("itemName = {}", itemName);

        Collection<Part> parts = req.getParts();
        log.info("parts = {}", parts);

        return "upload-form";
    }
}
```

* `request.getParts()`
    * `multipart/form-data`전송 방식에서 각각 나누어진 부분을 받아서 확인할 수 있다.

#### upload-form.html

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container">
    <div class="py-5 text-center">
        <h2>상품 등록 폼</h2>
    </div>
    <h4 class="mb-3">상품 입력</h4>
    <form enctype="multipart/form-data" method="post" th:action>
        <ul>
            <li>상품명 <input name="itemName" type="text"></li>
            <li>파일<input name="file" type="file"></li>
        </ul>
        <input type="submit"/>
    </form>
</div> <!-- /container -->
</body>
</html>
```

#### application.properties

```properties
logging.level.org.apache.coyote.http11 = debug
```

이 옵션을 사용하면 HTTP 요청 메시지를 확인할 수 있다.

#### 결과 로그

```
POST /servlet/v1/upload HTTP/1.1
Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryktKDOG6JAa7BiJ9K

------WebKitFormBoundaryktKDOG6JAa7BiJ9K
Content-Disposition: form-data; name="itemName"

testC
------WebKitFormBoundaryktKDOG6JAa7BiJ9K
Content-Disposition: form-data; name="file"; filename="test.jpg"
Content-Type: image/jpeg

qweojqbefoasdgfoxzncvponawper...
------WebKitFormBoundaryktKDOG6JAa7BiJ9K--
```

```
request  = org.springframework.web.multipart.support.StandardMultipartHttpServletRequest@45a8cc01
itemName = testC
parts    = [org.apache.catalina.core.ApplicationPart@6cdbfe7f, org.apache.catalina.core.ApplicationPart@6d251b04]
```

### multipart 사용 옵션

#### 업로드 사이즈 제한

```properties
spring.servlet.multipart.max-file-size = 1MB
spring.servlet.multipart.max-request-size = 10MB
```

큰 파일을 무제한 업로드하게 둘 수는 없으므로 업로드 사이즈를 제한할 수 있다.

사이즈를 넘으면 예외(`SizeLimitExceededException`)가 발생한다.

* `max-file-size`
    * 파일 하나의 최대 사이즈
    * 기본 1MB
* `max-request-size`
    * 멀티파트 요청 하나에 여러 파일을 업로드 할 수 있는데, 그 전체 합이다.
    * 기본 10MB

#### multipart 끄기

```properties
# default: true
spring.servlet.multipart.enabled = false
```

#### 결과

```
request  = org.apache.catalina.connector.RequestFacade@xxx
itemName = null
parts    = []
```

* `HttpServletRequest` 객체가 `RequestFacade`이다.
* 멀티파트는 일반적인 폼 요청인 `application/x-www-form-urlencoded` 보다 훨씬 복잡하다.
* `spring.servlet.multipart.enabled` 옵션을 끄면 서블릿 컨테이너는 멀티파트와 관련된 처리를 하지 않는다.
* 그래서 결과 로그를 보면 `request.getParameter("itemName")`, `request.getParts()`의 결과가 비어있다.

#### multipart 사용(기본)

```properties
# default: true
spring.servlet.multipart.enabled = true
```

#### 결과

```
request  = org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
itemName = Spring
parts    = [ApplicationPart1, ApplicationPart2]
```

* `HttpServletRequest` 객체가 `StandardMultipartHttpServletRequest`이다.

### 참고

* `DispatcherServlet`에서 멀티파트 리졸버(`MultipartResolver`)를 실행한다.
* 멀티파트 요청인 경우, `HttpServletRequest`를 `MultipartHttpServletRequest`로 변환해서 반환한다.
* `MultipartHttpServletRequest`는 `HttpServletRequest`의 자식 인터페이스이고, 멀티파트와 관련된 추가 기능을 제공한다.
* 기본 구현 객체는 `StandardMultipartHttpServletRequest`이다.
* 이것을 사용하면 멀티파트와 관련된 여러가지 처리를 편리하게 할 수 있다.
* 그런데 이후 강의에서 설명할 `MultipartFile`를 사용하는 것이 더 편하다.

## 서블릿과 파일 업로드 2

### 파일 저장 경로 설정

#### application.properties

```properties
file.dir = /Users/gimhoemin/Desktop/project/java/temp/file/
```

> **주의**<br>
> 1. 꼭 해당 경로에 실제 폴더를 미리 만들어두자.
> 2. application.properties 에서 설정할 때 마지막에 `/` (슬래시)가 포함된 것에 주의하자.
> 3. `~` 가 포함된 주소가 아닌, "full path"를 입력해야한다.

### 예제

#### ServletUploadController V2

```java

@Slf4j
@Controller
@RequestMapping("/servlet/v2")
public class ServletUploadControllerV2 {
    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(
            HttpServletRequest req
    ) throws ServletException, IOException {
        log.info("request = {}", req);

        String itemName = req.getParameter("itemName");
        log.info("itemName = {}", itemName);

        Collection<Part> parts = req.getParts();
        log.info("parts = {}", parts);

        for (Part part : parts) {
            log.info("==== PART ====");
            log.info("name = {}", part.getName());

            Collection<String> headerNames = part.getHeaderNames();
            for (String headerName : headerNames) {
                log.info("header {}: {}", headerName, part.getHeader(headerName));
            }

            // 편의 메서드
            // content-disposition; filename
            log.info("submittedFileName = {}", part.getSubmittedFileName());
            // part body size
            log.info("size = {}", part.getSize());

            // 데이터 읽기
            InputStream inputStream = part.getInputStream();
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            log.info("body = {}", body);

            // 파일에 저장하기
            if (StringUtils.hasText(part.getSubmittedFileName())) {
                String fullPath = fileDir + part.getSubmittedFileName();
                log.info("파일 저장 fullPath = {}", fullPath);
                part.write(fullPath);
            }
        }

        return "upload-form";
    }
}
```

#### @Value

```java
@Value("${file.dir}")
private String fileDir;
```

* `application`에서 설정한 `file.dir`의 값을 주입한다.
* 위 방법은 `final` 키워드를 사용할 수 없으니, 아래 방법(생성자)을 사용하자.

```java

@Slf4j
@Controller
@RequestMapping("/servlet/v2")
public class ServletUploadControllerV2 {

    private final String fileDir;

    public ServletUploadControllerV2(
            @Value("${file.dir}") String fileDir
    ) {
        this.fileDir = fileDir;
    }
}
```

#### Part 주요 메서드

* `Collection<Part> parts = request.getParts()`
    * 모든 파트들을 가져온다.
* `part.getName()`
    * 해당 파트의 이름을 가져온다.
* `part.getHeaderNames()`
    * 해당 파트의 모든 해더 이름을 가져온다.
* `part.getHeader(헤더 이름)`
    * 해당 파트의 해더 정보를 가져온다.
* `part.getSubmittedFileName()`
    * 해당 파트의 파일 명을 가져온다.
* `part.getInputStream()`
    * 해당 파트의 전송 데이터(body)를 읽을 수 있다.
    * `StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);` 변환 작업
* `part.write(...)`
    * 해당 파트를 통해 전송된 데이터를 저장할 수 있다.

### 결과 로그

```
request = org.springframework.web.multipart.support.StandardMultipartHttpServletRequest@7a8e14dc
itemName = testC
parts = [org.apache.catalina.core.ApplicationPart@73e5365a, org.apache.catalina.core.ApplicationPart@5425912f]

==== PART ====
name = itemName
header content-disposition: form-data; name="itemName"
submittedFileName = null
size = 5
body = testC
==== PART ====
name = file
header content-disposition: form-data; name="file"; filename="증명사진.jpg"
header content-type: image/jpeg
submittedFileName = 증명사진.jpg
size = 14737
body = ...

파일 저장 fullPath = /Users/gimhoemin/Desktop/project/java/temp/file/증명사진.jpg
```

### 정리

서블릿이 제공하는 `Part`는 편하기는 하지만, `HttpServletRequest`를 사용해야 하고, 추가로 파일 부분만 구분하려면 여러가지 코드를 넣어야 한다.

이번에는 스프링이 이 부분을 얼마나 편리하게 제공하는지 확인해보자.

## 스프링과 파일 업로드

스프링은 MultipartFile 이라는 인터페이스로 멀티파트 파일을 매우 편리하게 지원한다.

### SpringUploadController

```java

@Slf4j
@Controller
@RequestMapping("/spring")
public class SpringUploadController {
    private final String fileDir;

    public SpringUploadController(
            @Value("${file.dir}") String fileDir
    ) {
        this.fileDir = fileDir;
    }

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(
            @RequestParam String itemName,
            @RequestParam MultipartFile file,
            HttpServletRequest request
    ) throws ServletException, IOException {
        log.info("request = {}", request);
        log.info("itemName = {}", itemName);
        log.info("multipartFile = {}", file);

        if (!file.isEmpty()) {
            String fullPath = fileDir + file.getOriginalFilename();
            log.info("파일 저장 fullPath = {}", fullPath);
            file.transferTo(new File(fullPath));
        }

        return "upload-form";
    }
}
```

* `@RequestParam MultipartFile file`
    * 업로드하는 HTML Form의 name에 맞추어 `@RequestParam`을 적용하면 된다.
    * 추가로 `@ModelAttribute`에서도 `MultipartFile`을 동일하게 사용할 수 있다.

### MultipartFile 의 주요 메서드

* `file.getOriginalFilename()`: 업로드 파일 명
* `file.transferTo(...)`: 파일 저장

### 실행 로그

```
request = org.springframework.web.multipart.support.StandardMultipartHttpServletRequest@7d4a9cdb
itemName = testC
multipartFile = org.springframework.web.multipart.support.StandardMultipartHttpServletRequest$StandardMultipartFile@f677be5
파일 저장 fullPath = /Users/gimhoemin/Desktop/project/java/temp/file/김회민.pdf
```

## 예제로 구현하는 파일 업로드, 다운로드

실제 파일이나 이미지를 업로드, 다운로드 할 때는 몇가지 고려할 점이 있는데, 구체적인 예제로 알아보자.

### 요구 사항

* 상품을 관리
    * 상품 이름
    * 첨부파일 하나
    * 이미지 파일 여러개
* 첨부파일을 업로드 다운로드 할 수 있다.
* 업로드한 이미지를 웹 브라우저에서 확인할 수 있다.

### 예제

#### Item - 상품 도메인

```java
/**
 * 상품 도메인
 */
@Getter
@Setter
@RequiredArgsConstructor
public class Item {
    /**
     * 상품 이름
     */
    private final String itemName;

    /**
     * 첨부 파일 ( 1개 )
     */
    private final UploadFile attachFile;

    /**
     * 이미지 파일 ( N개 )
     */
    private final List<UploadFile> imageFiles;

    private Long id;
}
```

#### ItemRepository - 상품 레포지토리

```java
/**
 * 상품 저장소 - InMemory( HashMap )
 */
@Repository
public class ItemRepository {
    private final Map<Long, Item> store = new HashMap<>();
    private long sequence = 0L;

    /**
     * 상품 저장
     * 상품이 저장될 때마다 sequence field를 이용해 index를 1개씩 증가시킴
     *
     * @param item 저장할 상품 인스턴스
     * @return 저장된 상품 인스턴스
     */
    public Item save(
            Item item
    ) {
        item.setId(++sequence);
        store.put(item.getId(), item);
        return item;
    }

    /**
     * index를 이용해 상품 찾기
     *
     * @param id 찾고 싶은 상품의 index 값
     * @return 찾은 상품
     */
    @Nullable
    public Item findById(
            Long id
    ) {
        return store.get(id);
    }
}
```

#### UploadFile - 업로드 파일 정보 보관

```java
/**
 * 업로드 파일 정보
 */
@Getter
@RequiredArgsConstructor
public class UploadFile {
    /**
     * 사용자가 업로드한 실제 파일명
     */
    private final String uploadFileName;

    /**
     * 서버에서 관리하기 위해 사용하는 파일명
     */
    private final String storeFileName;
}
```

* `uploadFileName`: 고객이 업로드한 파일명
* `storeFileName`: 서버 내부에서 관리하는 파일명

* 고객이 업로드한 파일명으로 서버 내부에 파일을 저장하면 안된다.
* 왜냐하면 서로 다른 고객이 같은 파일이름을 업로드 하는 경우 기존 파일 이름과 충돌이 날 수 있다. (덮어버린다.)
* 서버에서는 저장할 파일명이 겹치지 않도록 내부에서 관리하는 별도의 파일명이 필요하다.

#### FileStore - 파일 저장과 관련된 업무 처리

```java
/**
 * 파일 저장과 관련된 업무 처리
 * - MultipartFile 을 UploadFile 객체로 변환
 * - MultipartFile 을 서버에 저장
 */
@Component
public class FileStore {
    /**
     * application.properties 에 저장된 file.dir 값
     */
    private final String fileDir;

    /**
     * application.properties 에 저장된 file.dir 값을 가져오기 위해 사용.
     */
    public FileStore(
            @Value("${file.dir}") String fileDir
    ) {
        this.fileDir = fileDir;
    }

    /**
     * fileDir + filename
     *
     * @param filename 상대 경로 + 파일 이름
     * @return FullPath
     */
    public String getFullPath(
            String filename
    ) {
        return fileDir + filename;
    }

    /**
     * Converting List<MultipartFile> To List<UploadFile>
     * 서버로 전송된 이미지 파일들을 서버 관리용 객체들로 변환
     *
     * @param multipartFiles 이미지 파일들
     * @return 서버 관리용 객체들
     */
    public List<UploadFile> storeFiles(
            List<MultipartFile> multipartFiles
    ) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    /**
     * Converting MultipartFile To UploadFile
     * - 서버로 전송된 이미지 파일을 서버 관리용 객체로 변환
     * - 이미지 파일을 서버에 저장 (transferTo)
     *
     * @param multipartFile 이미지 파일
     * @return 서버 관리용 객체
     */
    public UploadFile storeFile(
            MultipartFile multipartFile
    ) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFilename = createStoreFileName(originalFilename);
        multipartFile.transferTo(new File(getFullPath(storeFilename)));
        return new UploadFile(originalFilename, storeFilename);
    }

    /**
     * Converting Original Name To UUID Name
     *
     * @param originalFilename 파일의 원래 이름
     * @return UUID + "." + ext
     */
    private String createStoreFileName(
            String originalFilename
    ) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /**
     * 이름에서 확장자만 분리
     *
     * @param originalFilename 파일의 원래 이름
     * @return 확장자
     */
    private String extractExt(
            String originalFilename
    ) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
```

멀티파트 파일을 서버에 저장하는 역할을 담당한다.

* `createStoreFileName()`
    * 서버 내부에서 관리하는 파일명은 유일한 이름을 생성하는 `UUID`를 사용해서 충돌하지 않도록 한다.
* `extractExt()`
    * 확장자를 별도로 추출해서 서버 내부에서 관리하는 파일명에도 붙여준다.
    * 예를 들어서 고객이 `a.png` 라는 이름으로 업로드 하면 `51041c62-86e4-4274-801d-614a7d994edb.png` 와 같이 저장한다.

#### ItemForm

```java
/**
 * 클라이언트에서 넘어오는 상품 Form 데이터
 */
@Getter
@RequiredArgsConstructor
public class ItemForm {
    /**
     * 상품 이름
     */
    private final String itemName;

    /**
     * 첨부 파일 ( 1개 )
     */
    private final MultipartFile attachFile;

    /**
     * 이미지 파일 ( N개 )
     */
    private final List<MultipartFile> imageFiles;
}
```

* `List<MultipartFile> imageFiles`
    * 이미지를 다중 업로드 하기 위해 `MultipartFile`를 `List`로 사용했다.
* `MultipartFile attachFile`
    * 멀티파트는 `@ModelAttribute` 에서 사용할 수 있다.

#### ItemController

```java
/**
 * 상품 Controller
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    /**
     * GET /items/new<br>
     * - 단순히 item/form.html 을 뿌려주는 역할
     */
    @GetMapping("/items/new")
    public String newItem(
            @ModelAttribute ItemForm form
    ) {
        return "item/form";
    }

    /**
     * Post /items/new<br>
     * - item/form.html 의 form 에서 submit 하면 여기로 온다.<br>
     * - 클라이언트에서 넘어온 파일들을 서버에 저장한다.<br>
     * - 저장된 파일들의 정보를 DB에 저장한다.<br>
     * - 위의 과정을 모두 완료하면, 업로드한 정보를 확인할 수 있도록 상품 index 와 함께 상세페이지로 Redirect 한다.
     *
     * @return {@link #items(Long, Model)}
     */
    @PostMapping("/items/new")
    public String saveItem(
            @ModelAttribute ItemForm form,
            RedirectAttributes redirectAttributes
    ) throws IOException {
        // 서버에 파일 저장
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        // 데이터베이스에 저장
        Item item = new Item(
                form.getItemName(),
                attachFile,
                storeImageFiles
        );
        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        return "redirect:/items/{itemId}";
    }

    /**
     * GET /items/{id}<br>
     * - POST /items/new 에서 Redirect 하면 여기로 온다.<br>
     * - Redirect 하기 전에 넘겨준 상품 index 정보를 이용해 DB 에서 상품 정보를 찾는다.<br>
     * - 찾은 상품 정보를 Thymeleaf 에서 사용할 수 있도록 model 에 담아준다.<br>
     * - item/view.html 을 뿌려준다.
     */
    @GetMapping("/items/{id}")
    public String items(
            @PathVariable Long id,
            Model model
    ) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item/view";
    }

    /**
     * GET /images/{filename}<br>
     * - item/view.html 의 th:src="|/images/${imageFile.getStoreFileName()}|" 에서 요청.<br>
     * - UrlResource 를 이용해 서버에 저장된 파일을 불러온다.
     *
     * @param filename {@link UploadFile#storeFileName}
     * @return 서버에 저장된 파일 (인라인)
     * @throws MalformedURLException URL을 찾을 수 없음
     */
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(
            @PathVariable String filename
    ) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    /**
     * GET /attach/{itemId}<br>
     * - 다운로드를 위한 API
     * - item/view.html 의 th:href="|/attach/${item.id}|" 에서 요청.<br>
     * - Content-Disposition 헤더에 attachment; 를 추가한다.
     *
     * @param itemId {@link Item#id}
     * @return 서버에 저장된 파일 (다운로드)
     * @throws MalformedURLException URL을 찾을 수 없음
     */
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(
            @PathVariable Long itemId
    ) throws MalformedURLException {
        // 상품 찾기
        Item item = itemRepository.findById(itemId);

        // 상품의 첨부 파일의 정보를 가져온다.
        String storeFilename = item.getAttachFile().getStoreFileName();
        String uploadFilename = item.getAttachFile().getUploadFileName();
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFilename));
        log.info("download File = {}", uploadFilename);

        // 파일의 이름이 한글인 경우를 대비해 UTF-8 인코딩
        // 업로드 당시의 실제 파일 이름을 가져온다.
        String encodedUploadFilename = UriUtils.encode(uploadFilename, StandardCharsets.UTF_8);

        // Content-Disposition Header 에 넣을 정보
        // attachment; filename="실제 파일 이름"
        String contentDisposition = "attachment; filename=\"" + encodedUploadFilename + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
```

* `@GetMapping("/items/new")`
    * 단순히 item/form.html 을 뿌려주는 역할
* `@PostMapping("/items/new")`
    * item/form.html 의 form 에서 submit 하면 여기로 온다.
    * 클라이언트에서 넘어온 파일들을 서버에 저장한다.
    * 저장된 파일들의 정보를 DB에 저장한다.
    * 위의 과정을 모두 완료하면, 업로드한 정보를 확인할 수 있도록 상품 index 와 함께 상세페이지로 Redirect 한다.
* `@GetMapping("/items/{id}")`
    * POST /items/new 에서 Redirect 하면 여기로 온다.
    * Redirect 하기 전에 넘겨준 상품 index 정보를 이용해 DB 에서 상품 정보를 찾는다.
    * 찾은 상품 정보를 Thymeleaf 에서 사용할 수 있도록 model 에 담아준다.
    * `item/view.html` 을 뿌려준다.
* `@GetMapping("/images/{filename}")`
    * item/view.html 의 `th:src="|/images/${imageFile.getStoreFileName()}|"` 에서 요청.
    * `UrlResource`로 이미지 파일을 읽어서 `@ResponseBody`로 이미지 바이너리를 반환한다.
* `@GetMapping("/attach/{itemId}")`
    * 다운로드를 위한 API
    * item/view.html 의 `th:href="|/attach/${item.id}|"` 에서 요청.
    * `Content-Disposition` 헤더에 `attachment;`를 추가한다.

> 참고<br>
> Content-Disposition Header 는 3가지 형식이 있다.
> - `inline`: 브라우저에 표시, 기본값
> - `attachment`: 로컬 파일로 다운로드
> - `attachment; filename=""`: 파일 이름 지정

#### item/form.html

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container">
    <div class="py-5 text-center">
        <h2>상품 등록</h2>
    </div>
    <form enctype="multipart/form-data" method="post" th:action>
        <ul>
            <li>
                상품명 <input name="itemName" type="text">
            </li>
            <li>
                첨부파일<input name="attachFile" type="file">
            </li>
            <li>
                이미지 파일들
                <input multiple="multiple" name="imageFiles" type="file">
            </li>
        </ul>
        <input type="submit"/>
    </form>
</div> <!-- /container -->
</body>
</html>
```

* `<input multiple="multiple" name="imageFiles" type="file">`
    * 다중 파일 업로드를 하려면 `multiple="multiple"` 옵션을 주면 된다.
    * `ItemForm`의 다음 코드에서 여러 이미지 파일을 받을 수 있다.
    * `private List<MultipartFile> imageFiles;`

#### item/view.html

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container">
    <div class="py-5 text-center">
        <h2>상품 조회</h2>
    </div>

    상품명: <span th:text="${item.itemName}">상품명</span>
    <br/>

    첨부파일:
    <a th:href="|/attach/${item.id}|" th:if="${item.attachFile}"
       th:text="${item.getAttachFile().getUploadFileName()}"/>
    <br/>

    <img height="300"
         th:each="imageFile : ${item.imageFiles}"
         th:src="|/images/${imageFile.getStoreFileName()}|"
         width="300"/>
</div> <!-- /container -->
</body>
</html>
```

첨부 파일은 링크로 걸어두고, 이미지는 <img> 태그를 반복해서 출력한다