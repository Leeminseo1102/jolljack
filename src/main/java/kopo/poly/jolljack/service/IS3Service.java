package kopo.poly.jolljack.service;

import org.springframework.web.multipart.MultipartFile;

public interface IS3Service {

    String uploadImage(MultipartFile image) throws Exception;

    String createPresignedUrl(String s3Key) throws Exception;

    String uploadTradeImage(MultipartFile image) throws Exception;

    void deleteTradeImage(String s3Key) throws Exception;

    String createTradePresignedUrl(String s3Key) throws Exception;
}