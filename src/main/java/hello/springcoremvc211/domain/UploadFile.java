package hello.springcoremvc211.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
