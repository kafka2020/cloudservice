package ru.netology.cloudservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "files",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "filename"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Long size;

    @Column(name = "content_type")
    private String contentType;

    /**
     * Бинарное содержимое файла. Для production-нагрузки лучше хранить
     * данные в S3/файловой системе и держать в БД только метаданные,
     * но в учебных целях BLOB в Postgres достаточен.
     */
    @Lob
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
