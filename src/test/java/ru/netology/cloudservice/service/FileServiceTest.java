package ru.netology.cloudservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.netology.cloudservice.entity.FileEntity;
import ru.netology.cloudservice.entity.UserEntity;
import ru.netology.cloudservice.exception.ApiException;
import ru.netology.cloudservice.repository.FileRepository;
import ru.netology.cloudservice.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock UserRepository userRepository;
    @Mock FileRepository fileRepository;

    @InjectMocks FileService fileService;

    UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder().id(1L).login("user1").password("hash").build();
    }

    @Test
    void upload_savesFile() throws Exception {
        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));
        when(fileRepository.existsByUserAndFilename(user, "a.txt")).thenReturn(false);

        MockMultipartFile mp = new MockMultipartFile(
                "file", "a.txt", "text/plain", "hello".getBytes());

        fileService.upload("user1", "a.txt", mp);

        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void upload_duplicate_throws400() {
        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));
        when(fileRepository.existsByUserAndFilename(user, "a.txt")).thenReturn(true);

        MockMultipartFile mp = new MockMultipartFile(
                "file", "a.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> fileService.upload("user1", "a.txt", mp))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void delete_existingFile() {
        FileEntity fe = FileEntity.builder().id(10L).user(user).filename("a.txt").size(5L).build();
        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));
        when(fileRepository.findByUserAndFilename(user, "a.txt")).thenReturn(Optional.of(fe));

        fileService.delete("user1", "a.txt");

        verify(fileRepository).delete(fe);
    }

    @Test
    void rename_changesFilename() {
        FileEntity fe = FileEntity.builder().id(10L).user(user).filename("a.txt").size(5L).build();
        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));
        when(fileRepository.findByUserAndFilename(user, "a.txt")).thenReturn(Optional.of(fe));
        when(fileRepository.existsByUserAndFilename(user, "b.txt")).thenReturn(false);

        fileService.rename("user1", "a.txt", "b.txt");

        assertThat(fe.getFilename()).isEqualTo("b.txt");
    }

    @Test
    void list_returnsLimited() {
        FileEntity f1 = FileEntity.builder().filename("a").size(1L).createdAt(Instant.now()).build();
        FileEntity f2 = FileEntity.builder().filename("b").size(2L).createdAt(Instant.now()).build();
        FileEntity f3 = FileEntity.builder().filename("c").size(3L).createdAt(Instant.now()).build();
        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));
        when(fileRepository.findAllByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(f1, f2, f3));

        var result = fileService.list("user1", 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).filename()).isEqualTo("a");
    }
}
