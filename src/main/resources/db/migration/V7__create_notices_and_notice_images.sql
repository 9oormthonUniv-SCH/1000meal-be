-- 1) notices (Notice)
CREATE TABLE `notices` (
                           `id`           BIGINT NOT NULL AUTO_INCREMENT,
                           `title`        VARCHAR(200) NOT NULL,
                           `content`      LONGTEXT NOT NULL,
                           `is_published` TINYINT(1) NOT NULL DEFAULT 0,
                           `is_pinned`    TINYINT(1) NOT NULL DEFAULT 0,
                           `created_at`   DATETIME(6) NOT NULL,
                           `updated_at`   DATETIME(6) NOT NULL,
                           `deleted_at`   DATETIME(6) NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 2) notice_images (NoticeImage)
CREATE TABLE `notice_images` (
                                 `id`            BIGINT NOT NULL AUTO_INCREMENT,
                                 `notice_id`     BIGINT NOT NULL,
                                 `s3key`        VARCHAR(600) NOT NULL,
                                 `url`           VARCHAR(1200) NOT NULL,
                                 `original_name` VARCHAR(255) NULL,
                                 `content_type`  VARCHAR(100) NULL,
                                 `size`          BIGINT NOT NULL DEFAULT 0,
                                 `created_at`    DATETIME(6) NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `idx_notice_images_notice_id` (`notice_id`),
                                 CONSTRAINT `fk_notice_images_notice`
                                     FOREIGN KEY (`notice_id`)
                                         REFERENCES `notices` (`id`)
                                         ON DELETE CASCADE
                                         ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
