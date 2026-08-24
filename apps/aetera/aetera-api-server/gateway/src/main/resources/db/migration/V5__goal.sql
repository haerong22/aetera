-- ── 모듈: 목표 ──────────────────────────────────────────────────────────────
-- 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
--
-- 지난 주기의 성적은 남기지 않는다. period_start 가 "지금 재고 있는 창"이고,
-- 주기가 넘어가면 progress 를 0 으로 되돌린다.

create table goals
(
    id           uuid                     primary key,
    user_id      uuid                     not null,
    title        varchar(100)             not null,
    period       varchar(20)              not null,
    target       integer                  not null,
    unit         varchar(10),
    progress     integer                  not null default 0,
    period_start date                     not null,
    created_at   timestamp with time zone not null,
    version      bigint                   not null default 0
);

create index ix_goals_user_created on goals (user_id, created_at);
