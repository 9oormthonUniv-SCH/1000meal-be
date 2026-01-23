INSERT INTO store (id, name, address, phone, description, open_time, close_time, is_open, remain, hours, lat, lng)
VALUES
    (1, '향설 1관', '서울시 강남구', '010-1234-5678', '맛있는 한식 전문점', '08:00:00', '20:00:00', b'0', 50, '08:00 ~ 소진 시', 36.7688, 126.9323),
    (2, '야외 그라찌에', '서울시 서초구', '010-9876-5432', '일본 가정식 맛집', '09:00:00', '21:00:00', b'0', 30, '09:00 ~ 소진 시', 36.7712, 126.9327),
    (3, '베이커리 경', '서울시 송파구', '010-2468-1357', '매콤한 닭갈비 전문점', '10:00:00', '22:00:00', b'0', 20, '10:00 ~ 소진 시', 36.774667, 126.93362)
    ON DUPLICATE KEY UPDATE
                         name=VALUES(name),
                         address=VALUES(address),
                         phone=VALUES(phone),
                         description=VALUES(description),
                         open_time=VALUES(open_time),
                         close_time=VALUES(close_time),
                         is_open=VALUES(is_open),
                         remain=VALUES(remain),
                         hours=VALUES(hours),
                         lat=VALUES(lat),
                         lng=VALUES(lng);


