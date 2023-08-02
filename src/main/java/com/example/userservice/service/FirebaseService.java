package com.example.userservice.service;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class FirebaseService {
    private final String FIREBASE_SDK_JSON = "travelio-8e986-firebase-adminsdk-gtns2-9ca84ca7c0.json";
    private final String FIREBASE_PROJECT_ID = "travelio-8e986";
    private final String FIREBASE_BUCKET = "travelio-8e986.appspot.com";

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String objectName = generateObjectName();

        FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
        File file = convertMultiPartToFile(multipartFile);
        Path filePath = file.toPath();

        Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
        BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();

        storage.create(blobInfo, Files.readAllBytes(filePath));

        return objectName;

    }

    public String uploadBufferedImage(BufferedImage image, String formatName, String contentType) throws IOException {
        String objectName = generateObjectName();

        FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);

        // Convert BufferedImage to byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        byte[] bytes = baos.toByteArray();

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId(FIREBASE_PROJECT_ID)
                .build()
                .getService();
        BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)  // e.g., "image/jpeg"
                .build();

        storage.create(blobInfo, bytes);

        return objectName;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    public String generateObjectName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timestamp = sdf.format(new Date());
        String uuid = UUID.randomUUID().toString();
        return timestamp + "_" + uuid + ".jpg";  // the format should be the same as the image format
    }

    public URL generateURL(String objectName) throws IOException {
        FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
        Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
        BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);


        long expirationTime = 3600;
        return storage.get(blobId).signUrl(expirationTime, TimeUnit.SECONDS);
    }
}
