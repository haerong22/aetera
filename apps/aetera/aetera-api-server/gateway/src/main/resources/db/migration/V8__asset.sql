-- ── 모듈: 자산 ─────────────────────────────────────────────────────────────
-- 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
--
-- 한 달에 한 번 찍는 스냅샷이다. 한 줄은 그때의 사실이라 쓰고 나면 고치지 않는다 —
-- 한 달을 다시 쓰는 일은 그 달을 지우고 새로 넣는 것이다.
--
-- month 는 언제나 그 달의 1일. 며칠을 보내든 같은 달이면 같은 스냅샷이어야 한다.

create table asset_entries
(
    id          uuid                     primary key,
    user_id     uuid                     not null,
    month       date                     not null,
    name        varchar(100)             not null,
    category    varchar(20)              not null,
    amount      bigint                   not null,
    recorded_at timestamp with time zone not null,
    version     bigint                   not null default 0
);

-- 화면은 한 사용자의 전부를 최근 달부터 읽고, 한 달을 통째로 지운다. 둘 다 이 인덱스를 탄다.
create index ix_asset_entries_user_month on asset_entries (user_id, month desc);
