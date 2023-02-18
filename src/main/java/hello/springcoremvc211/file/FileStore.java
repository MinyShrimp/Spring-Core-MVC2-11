package hello.springcoremvc211.file;

import hello.springcoremvc211.domain.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
