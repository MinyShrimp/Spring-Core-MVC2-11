package hello.springcoremvc211.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

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
