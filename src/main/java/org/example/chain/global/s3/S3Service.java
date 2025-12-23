package org.example.chain.global.s3;

import lombok.RequiredArgsConstructor;
import org.example.chain.global.error.exception.PostImageDeleteFailedException;
import org.example.chain.global.error.exception.PostImageUploadFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.url-prefix}")
    private String urlPrefix;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    public String upload(MultipartFile file, String dirName) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.lastIndexOf('.') == -1) {
            throw new PostImageUploadFailedException("잘못된 파일 이름입니다. 확장자가 필요합니다.");
        }
        String extension =
                originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        String key = dirName + "/" + UUID.randomUUID() + "." + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return key;
        } catch (IOException e) {
            throw new PostImageUploadFailedException("파일 삭제 실패");
        } catch (S3Exception e) {
            throw new PostImageUploadFailedException("S3 업로드 실패");
        }
    }

    public String generateGetUrl(String imageKey) {

        //S3에서 파일 가져오기
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(imageKey)
                .build();

        //Presigned URL 요청 생성 (유효기간 설정)
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15)) // ⏱ 15분
                        .getObjectRequest(getObjectRequest)
                        .build();

        //실제 client가 접속할 Presigned URL 생성
        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        //URL 문자열 반환
            return presignedRequest.url().toString();
        }


    public void delete(String imageKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(imageKey)
                    .build());
        } catch (Exception e) {
            throw new PostImageDeleteFailedException("이미지 삭제 실패");
        }
    }
}
