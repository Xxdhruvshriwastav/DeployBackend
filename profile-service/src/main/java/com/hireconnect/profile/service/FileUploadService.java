package com.hireconnect.profile.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        boolean isPdf = contentType != null && contentType.equals("application/pdf");


        String resourceType = isPdf ? "raw" : "image";
        String publicId = isPdf
                ? UUID.randomUUID().toString() + ".pdf"  // raw needs explicit extension
                : UUID.randomUUID().toString();           // image: Cloudinary adds format

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", resourceType
        ));
        
        return uploadResult.get("secure_url").toString();
    }
}
