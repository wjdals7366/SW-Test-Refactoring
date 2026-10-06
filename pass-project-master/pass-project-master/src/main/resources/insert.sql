INSERT INTO `user` (user_id, user_name, status, phone)
VALUES ('A1000000', '하정민', 'ACTIVE', '010-1111-2222'),
('A1000001', '염형섭', 'ACTIVE', '010-3333-4444'),
('A1000002', '이준호', 'INACTIVE', '010-5555-6666'),
('B1000010', '권민우', 'ACTIVE', '010-7777-8888'),
('B1000011', '아무개', 'INACTIVE', '010-8888-9999'),
('B2000000', '한선영', 'ACTIVE', '010-9999-0000'),
('B2000001', '태수미', 'ACTIVE', '010-0000-1111');


INSERT INTO user_group_mapping (user_group_id, user_id, user_group_name, description)
VALUES ('HANBADA', 'A1000000', '한바다', '한바다 임직원 그룹'),
('HANBADA', 'A1000001', '한바다', '한바다 임직원 그룹'),
('HANBADA', 'A1000002', '한바다', '한바다 임직원 그룹'),
('HANBADA', 'B1000010', '한바다', '한바다 임직원 그룹'),
('HANBADA', 'B2000000', '한바다', '한바다 임직원 그룹'),
('TAESAN', 'B2000001', '태산', '태산 임직원 그룹');
--('HANBADA', 'yeomhs420', '한바다', '한바다 임직원 그룹');


INSERT INTO statistics (statistics_at, all_count, attended_count, cancelled_count)
VALUES ('2022-09-01 00:00:00', 5, 3, 2),
('2022-09-02 00:00:00', 6, 2, 1),

('2022-09-04 00:00:00', 4, 1, 1),
('2022-09-05 00:00:00', 8, 3, 1),
('2022-09-10 00:00:00', 6, 4, 3),
('2022-09-01 00:00:00', 2, 5, 4);

INSERT INTO package (package_name, count, period, created_at)
VALUES ('Starter PT 10회', 10, 60, '2022-08-01 00:00:00'),
('Starter PT 20회', 20, 120, '2022-08-01 00:00:00'),
('Starter PT 30회', 30, 180, '2022-08-01 00:00:00'),
('무료 이벤트 필라테스 1회', 1, NULL, '2022-08-01 00:00:00'),
('바디 챌린지 PT 2주', NULL, 28, '2022-08-01 00:00:00'),
('바디 챌린지 PT 4주', NULL, 48, '2022-08-01 00:00:00'),
('인바디 상담', NULL, NULL, '2022-08-01 00:00:00');

--INSERT INTO notification (notificatio_seq, count, period, created_at)
--VALUES ('Starter PT 10회', 10, 60, '2022-08-01 00:00:00');



INSERT INTO instruct (name, gender) values ('염형섭', '남자');
INSERT INTO instruct (name, gender) values ('하정민', '남자');
INSERT INTO instruct (name, gender) values ('홍길동', '남자');
INSERT INTO instruct (name, gender) values ('김남수', '남자');
INSERT INTO instruct (name, gender) values ('김수원', '남자');
INSERT INTO instruct (name, gender) values ('박필두', '남자');
INSERT INTO instruct_date (date, instruct_id) values ('2023-11-25', 1);
INSERT INTO instruct_date (date, instruct_id) values ('2023-11-25', 2);
INSERT INTO instruct_date (date, instruct_id) values ('2023-11-25', 3);
INSERT INTO instruct_date (date, instruct_id) values ('2023-11-26', 4);
INSERT INTO instruct_date (date, instruct_id) values ('2023-11-26', 5);
INSERT INTO instruct_date (date, instruct_id) values ('2023-11-26', 6);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (5, 0, '15:00-16:00', 1);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (3, 0, '11:00-12:00', 1);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (4, 0, '14:00-15:00', 2);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (1, 0, '10:00-11:00', 2);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (2, 0, '16:00-17:00', 3);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (4, 0, '13:00-14:00', 4);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (1, 0, '10:00-11:00', 5);
INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (2, 0, '16:00-17:00', 6);
--INSERT INTO instruct_date_time (limit_number, reserve_number, time, instruct_date_id) values (9, 0, '18:00-19:00', 3);