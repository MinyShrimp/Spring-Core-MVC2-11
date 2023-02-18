package hello.springcoremvc211.form;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
