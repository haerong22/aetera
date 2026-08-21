-- ── 모듈: 만기 관리 ────────────────────────────────────────────────────────
-- 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
--
-- 만기일은 시각이 아니라 날짜다 — 사용자가 달력에서 고른 그 날이 곧 값이라
-- 타임존 변환이 끼어들 여지를 두지 않는다.

create table renewals
(
    id           uuid                     primary key,
    user_id      uuid                     not null,
    title        varchar(100)             not null,
    category     varchar(20)              not null,
    expires_at   date                     not null,
    cycle        varchar(20)              not null,
    notice_days  integer                  not null,
    memo         text,
    created_at   timestamp with time zone not null,
    version      bigint                   not null default 0
);

-- 화면이 만기 이른 순으로 그리므로 그 순서를 인덱스가 그대로 준다.
create index ix_renewals_user_expires on renewals (user_id, expires_at);
