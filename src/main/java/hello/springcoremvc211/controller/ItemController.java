package hello.springcoremvc211.controller;

import hello.springcoremvc211.domain.Item;
import hello.springcoremvc211.domain.UploadFile;
import hello.springcoremvc211.file.FileStore;
import hello.springcoremvc211.form.ItemForm;
import hello.springcoremvc211.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
