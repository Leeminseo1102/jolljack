package kopo.poly.jolljack.service.impl;

import kopo.poly.jolljack.service.IS3Service;
import kopo.poly.jolljack.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service implements IS3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.upload-prefix:diag/}")
    private String uploadPrefix;

    @Value("${aws.s3.presigned-url-expiration-minutes:10}")
    private long presignedUrlExpirationMinutes;

    @Override
    public String uploadImage(MultipartFile image) throws Exception {

        String mimeType = CmmUtil.nvl(image.getContentType());
        String s3Key = createS3Key(mimeType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(mimeType)
                .contentLength(image.getSize())
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(image.getInputStream(), image.getSize())
        );

        log.info("S3 이미지 업로드 완료 : {}", s3Key);

        return s3Key;
    }

    @Override
    public String createPresignedUrl(String s3Key) throws Exception {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    private String createS3Key(String mimeType) {

        String extension = getExtensionByMimeType(mimeType);

        String today = LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String prefix = CmmUtil.nvl(uploadPrefix);

        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        return prefix + today + "/" + UUID.randomUUID() + extension;
    }

    private String getExtensionByMimeType(String mimeType) {

        mimeType = CmmUtil.nvl(mimeType);

        if (mimeType.equals("image/jpeg")) {
            return ".jpg";

        } else if (mimeType.equals("image/png")) {
            return ".png";

        } else if (mimeType.equals("image/webp")) {
            return ".webp";

        } else {
            return ".img";
        }
    }
}