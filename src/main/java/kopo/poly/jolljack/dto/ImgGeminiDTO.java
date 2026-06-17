package kopo.poly.jolljack.dto;

public record ImgGeminiDTO(

        // Gemini 요청에 넣을 이미지 MIME 타입
        // 예: image/jpeg, image/png, image/webp
        String mimeType,

        // S3 presigned URL
        // Gemini가 이미지를 읽기 위한 임시 접근 URL
        String fileUri

) {
}