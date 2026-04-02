CREATE TABLE holiday (
    id BIGINT NOT NULL AUTO_INCREMENT,
    date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_holiday_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
