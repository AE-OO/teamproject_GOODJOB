package com.goodjob.post.fileupload;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileService implements WebMvcConfigurer {
    @Value("${file.url}")
    private String fileDir;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles)
            throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        Path path = Paths.get(getFullPath(storeFileName)).toAbsolutePath();
        multipartFile.transferTo(path.toFile());
        return new UploadFile(originalFilename, storeFileName);
    }

    public String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
    public List<String> getFiles(List<UploadFile> uploadFileList) throws IOException {
        Path fileStorageLocation = Paths.get(fileDir).toAbsolutePath().normalize();
        List<String> fileList = new ArrayList<>();
        for( UploadFile uploadFile: uploadFileList){
            Path filePath = fileStorageLocation.resolve(uploadFile.getStoreFileName()).normalize();
            File file = new File(filePath.toAbsolutePath().toString());
            fileList.add(file.getName());
        }

        return fileList;
    }
    public ResponseEntity<byte[]> getFile(String fileName) throws IOException {
        ResponseEntity<byte[]> result = null;
        File file = new File(fileDir+fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", Files.probeContentType(file.toPath()));
        result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file),headers, HttpStatus.OK);
        return result;

    }
}
