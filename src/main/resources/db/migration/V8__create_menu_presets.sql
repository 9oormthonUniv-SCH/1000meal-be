-- 1) menu_presets
-- 1) menu_presets
CREATE TABLE IF NOT EXISTS `menu_presets` (
                                              `id` BIGINT NOT NULL AUTO_INCREMENT,
                                              `store_id` BIGINT NOT NULL,
                                              `group_id` BIGINT NOT NULL DEFAULT 0,
                                              `created_by_account_id` BIGINT NOT NULL,
                                              `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_menu_presets_store_group` (`store_id`, `group_id`),
    CONSTRAINT `fk_menu_presets_store`
    FOREIGN KEY (`store_id`)
    REFERENCES `store` (`id`)
    ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) menu_preset_items (ElementCollection)
CREATE TABLE IF NOT EXISTS `menu_preset_items` (
                                    `preset_id` BIGINT NOT NULL,
                                    `sort_order` INT NOT NULL,
                                    `name` VARCHAR(200) NOT NULL,
                                    PRIMARY KEY (`preset_id`, `sort_order`),
                                    CONSTRAINT `fk_menu_preset_items_preset`
                                        FOREIGN KEY (`preset_id`)
                                            REFERENCES `menu_presets` (`id`)
                                            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
