package ru.netology.cloudservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.dto.FileInfoResponse;
import ru.netology.cloudservice.dto.RenameRequest;
import ru.netology.cloudservice.entity.FileEntity;
import ru.netology.cloudservice.service.FileService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<Void> upload(@AuthenticationPrincipal UserDetails user,
                                       @RequestParam("filename") String filename,
                                       @RequestPart("file") MultipartFile file) {
        fileService.upload(user.getUsername(), filename, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user,
                                       @RequestParam("filename") String filename) {
        fileService.delete(user.getUsername(), filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal UserDetails user,
                                             @RequestParam("filename") String filename) {
        FileEntity entity = fileService.download(user.getUsername(), filename);
        ByteArrayResource resource = new ByteArrayResource(entity.getData());
        MediaType mediaType = entity.getContentType() != null
                ? MediaType.parseMediaType(entity.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(entity.getSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + entity.getFilename() + "\"")
                .body(resource);
    }

    @PutMapping("/file")
    public ResponseEntity<Void> rename(@AuthenticationPrincipal UserDetails user,
                                       @RequestParam("filename") String filename,
                                       @Valid @RequestBody RenameRequest request) {
        fileService.rename(user.getUsername(), filename, request.name());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public List<FileInfoResponse> list(@AuthenticationPrincipal UserDetails user,
                                       @RequestParam(value = "limit", required = false) Integer limit) {
        return fileService.list(user.getUsername(), limit);
    }
}
