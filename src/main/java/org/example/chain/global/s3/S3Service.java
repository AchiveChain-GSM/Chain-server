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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.url-prefix}")
    private String urlPrefix;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    public String upload(MultipartFile file, String dirName) {
        String originalFilename = file.getOriginalFilename();

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

            return urlPrefix + "/" + key;
        } catch (IOException e) {
            throw new PostImageUploadFailedException("파일 삭제 실패");
        } catch (S3Exception e) {
            throw new PostImageUploadFailedException("S3 업로드 실패");
        }
    }

    public void delete(String imageUrl) {
        try {
            String key = imageUrl.substring(urlPrefix.length() + 1);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new PostImageDeleteFailedException("이미지 삭제 실패");
        }
    }
}
