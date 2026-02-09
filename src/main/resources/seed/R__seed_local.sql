-- generated seed (local)
SET FOREIGN_KEY_CHECKS=0;

-- SECTION: store
INSERT INTO `store`
(`id`, `image_url`, `name`, `address`, `phone`, `description`, `open_time`, `close_time`, `is_open`, `remain`, `hours`, `lat`, `lng`)
VALUES
    (1,'https://1000mealsql.s3.ap-northeast-2.amazonaws.com/66ce7785-c1fb-4a38-a475-2c43d28dc856.png','향설 1관','충청남도 아산시 신창면','010-0000-0000','향설 생활관 학식','08:00:00.000000','20:00:00.000000',b'1',50,'08:00 ~ 소진 시',36.7688,126.9323),
    (2,'https://1000mealsql.s3.ap-northeast-2.amazonaws.com/dc0a5daa-4ed3-4df8-a1f0-f3d338c3c991.png','야외 그라찌에','충청남도 아산시 신창면','010-0000-0000','교내 카페 브랜드','08:00:00.000000','21:00:00.000000',b'1',30,'08:00 ~ 소진 시',36.7712,126.9327),
    (3,'https://1000mealsql.s3.ap-northeast-2.amazonaws.com/d10cf7b4-ef7d-4719-b22a-9d785e6aefc2.png','베이커리 경','충청남도 아산시 신창면','041-531-6930','수제 베이커리','08:00:00.000000','22:00:00.000000',b'1',20,'08:00 ~ 소진 시',36.774667,126.93362),
    (4,'https://1000mealsql.s3.ap-northeast-2.amazonaws.com/0d3044c2-f29f-4a62-bf32-578d491765ae.png','향설 2관','충청남도 아산시 신창면','010-0000-0000','향설 생활관 학식','08:00:00.000000','20:00:00.000000',b'1',0,'08:00 ~ 소진 시',36.76810630469366,126.93359748523937)
    ON DUPLICATE KEY UPDATE
                         `image_url`=VALUES(`image_url`),
                         `name`=VALUES(`name`),
                         `address`=VALUES(`address`),
                         `phone`=VALUES(`phone`),
                         `description`=VALUES(`description`),
                         `open_time`=VALUES(`open_time`),
                         `close_time`=VALUES(`close_time`),
                         `is_open`=VALUES(`is_open`),
                         `remain`=VALUES(`remain`),
                         `hours`=VALUES(`hours`),
                         `lat`=VALUES(`lat`),
                         `lng`=VALUES(`lng`);


-- SECTION: accounts (ADMIN)
INSERT INTO `accounts` (`id`, `user_id`, `email`, `password_hash`, `role`, `status`) VALUES
                                                                                         (14,'council1','testemail1@sch.ac.kr','$2a$10$Gt7.NiZHLEnWZ49pHyFbhO.BnvnLA.N5TUHDv78/MjpQvz/m30AvO','ADMIN','ACTIVE'),
                                                                                         (15,'council2','testemail2@sch.ac.kr','$2a$10$bR0U2VVgO36DMhGEcRD2fuuoA0Ih9nZHfVsv.E6qQe816pl5ezaV.','ADMIN','ACTIVE'),
                                                                                         (16,'council3','testemail3@sch.ac.kr','$2a$10$XB78LfyS38dH1.6k.GRGu.uA19x5b/jd/oVKvak62.GkuIOYfx4Ny','ADMIN','ACTIVE'),
                                                                                         (17,'council4','testemail4@sch.ac.kr','$2a$10$xRHdyDCMJEGEtnwfMI8BTuKJJSP3G.vAmZs3LFOjUypc/ECdM05Ki','ADMIN','ACTIVE'),
                                                                                         (18,'admin1','storeemail1@sch.ac.kr','$2a$10$gHk/oqyT/jpAiJWqdAsNJ./NHgrrQ9n8xYbfZ4FaGfF7CwsRFw1du','ADMIN','ACTIVE'),
                                                                                         (19,'admin2','storeemail2@sch.ac.kr','$2a$10$uDDGIJO.tMDurBx4Ev3LGuljRNlTevW2Z254QhYQmwnBK3dyWuZH.','ADMIN','ACTIVE'),
                                                                                         (20,'admin3','storeemail3@sch.ac.kr','$2a$10$shzwTXzXD23Uph5mwVtrJuPbifT62/yd99zZdX5IYEbOdGd/j4Zw.','ADMIN','ACTIVE'),
                                                                                         (21,'admin4','storeemail4@sch.ac.kr','$2a$10$wVEYAdQH7LVQ1qZkMTFbq.iwXaSWydUE4N.M35LhpQ75scixfG3E6','ADMIN','ACTIVE')
    ON DUPLICATE KEY UPDATE
                         `user_id`=VALUES(`user_id`),
                         `email`=VALUES(`email`),
                         `password_hash`=VALUES(`password_hash`),
                         `role`=VALUES(`role`),
                         `status`=VALUES(`status`);


-- SECTION: accounts (STUDENT)
INSERT INTO `accounts`
(`id`, `user_id`, `email`, `password_hash`, `role`, `status`)
VALUES
    (23,'student1','student1@sch.ac.kr','$2a$10$QpK0N8RzQ7Yq9Qm4cH0JEu2Z9Rr9c4WZkzYc8m3dU9sRrZyKk1U2a','STUDENT','ACTIVE')
    ON DUPLICATE KEY UPDATE
                         `user_id`=VALUES(`user_id`),
                         `email`=VALUES(`email`),
                         `password_hash`=VALUES(`password_hash`),
                         `role`=VALUES(`role`),
                         `status`=VALUES(`status`);


-- SECTION: admin_profiles
INSERT INTO `admin_profiles` (`id`, `account_id`, `store_id`, `admin_level`, `display_name`) VALUES
                                                                                                 (1,18,1,1,'admin1'),
                                                                                                 (2,19,2,1,'admin2'),
                                                                                                 (3,20,3,1,'admin3'),
                                                                                                 (4,21,4,1,'admin4'),
                                                                                                 (5,14,1,1,'향설 1관'),
                                                                                                 (6,15,2,1,'야외 그라찌에'),
                                                                                                 (7,16,3,1,'베이커리 경'),
                                                                                                 (8,17,4,1,'향설 2관')
    ON DUPLICATE KEY UPDATE
                         `account_id`=VALUES(`account_id`),
                         `store_id`=VALUES(`store_id`),
                         `admin_level`=VALUES(`admin_level`),
                         `display_name`=VALUES(`display_name`);


-- SECTION: user_profiles (STUDENT)
INSERT INTO `user_profiles`
(`id`, `account_id`, `department`, `name`, `phone`)
VALUES
    (2,23,'컴퓨터소프트웨어공학과','학생1','010-1111-2222')
    ON DUPLICATE KEY UPDATE
                         `department`=VALUES(`department`),
                         `name`=VALUES(`name`),
                         `phone`=VALUES(`phone`);


SET FOREIGN_KEY_CHECKS=1;