-- ── 모듈: 소득 ────────────────────────────────────────────────────────────
-- 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
--
-- 고정지출과 표를 나눈다. 한 표에 부호만 바꿔 담으면 "한 달 고정지출 250만원"이 뜻을 잃는다.
-- 금액은 원 단위 정수이고 실수령액을 적는다 — 세전을 적으면 "몇 달 버티나"가 그만큼 넉넉해진다.
-- 입금일은 두지 않는다. "언제"는 일정의 몫이고, 여기는 "얼마"만 묻는다.

create table income_sources
(
    id         uuid                     primary key,
    user_id    uuid                     not null,
    title      varchar(100)             not null,
    category   varchar(20)              not null,
    amount     bigint                   not null,
    cycle      varchar(20)              not null,
    memo       text,
    created_at timestamp with time zone not null,
    version    bigint                   not null default 0
);

-- 화면은 한 사용자의 전부를 한 번에 읽는다. 보이는 순서(큰 것부터)는 파생값이라 정렬은 응용에서 한다.
create index ix_income_sources_user on income_sources (user_id, created_at);
