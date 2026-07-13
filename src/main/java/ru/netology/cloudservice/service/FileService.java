package ru.netology.cloudservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudservice.dto.FileInfoResponse;
import ru.netology.cloudservice.entity.FileEntity;
import ru.netology.cloudservice.entity.UserEntity;
import ru.netology.cloudservice.exception.ApiException;
import ru.netology.cloudservice.repository.FileRepository;
import ru.netology.cloudservice.repository.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Transactional
    public void upload(String login, String filename, MultipartFile file) {
        if (filename == null || filename.isBlank() || file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, 400, "Error input data");
        }
        UserEntity user = requireUser(login);
        if (fileRepository.existsByUserAndFilename(user, filename)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, 400, "File with the same name already exists");
        }
        try {
            FileEntity entity = FileEntity.builder()
                    .user(user)
                    .filename(filename)
                    .size(file.getSize())
                    .contentType(file.getContentType())
                    .data(file.getBytes())
                    .createdAt(Instant.now())
                    .build();
            fileRepository.save(entity);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Error upload file");
        }
    }

    @Transactional
    public void delete(String login, String filename) {
        UserEntity user = requireUser(login);
        FileEntity file = fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, 400, "File not found"));
        fileRepository.delete(file);
    }

    @Transactional(readOnly = true)
    public FileEntity download(String login, String filename) {
        UserEntity user = requireUser(login);
        return fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, 400, "File not found"));
    }

    @Transactional
    public void rename(String login, String filename, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, 400, "Error input data");
        }
        UserEntity user = requireUser(login);
        FileEntity file = fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, 400, "File not found"));
        if (fileRepository.existsByUserAndFilename(user, newName)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, 400, "File with the same name already exists");
        }
        file.setFilename(newName);
    }

    @Transactional(readOnly = true)
    public List<FileInfoResponse> list(String login, Integer limit) {
        UserEntity user = requireUser(login);
        var stream = fileRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(f -> new FileInfoResponse(f.getFilename(), f.getSize()));
        if (limit != null && limit > 0) {
            stream = stream.limit(limit);
        }
        return stream.toList();
    }

    private UserEntity requireUser(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, 401, "Unauthorized"));
    }
}
