package org.example.chain.global.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public S3Client s3Client() {

        String cleanAccessKey = accessKey.trim();
        String cleanSecretKey = secretKey.trim();
        String cleanRegion = region.trim();

        System.out.println("DEBUG S3 - Region: [" + cleanRegion + "]");
        System.out.println("DEBUG S3 - AccessKey: [" + cleanAccessKey.substring(0, 4) + "...]");

        AwsBasicCredentials credentials = AwsBasicCredentials.create(cleanAccessKey, cleanSecretKey);

        return S3Client.builder()
                .region(Region.of(cleanRegion))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .serviceConfiguration(s3Configuration ->
                        s3Configuration
                                .pathStyleAccessEnabled(true) // 점(.)이 있는 버킷은 이게 필수입니다
                                .checksumValidationEnabled(false) // 서명 불일치 시 체크섬 검증을 잠시 꺼봅니다
                )
                .build();
    }

    @Bean
    S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }



}