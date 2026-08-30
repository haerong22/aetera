-- ── 모듈: 고정지출 ─────────────────────────────────────────────────────────
-- 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
--
-- 금액은 원 단위 정수다. 부동소수를 쓰면 합계에서 원이 흔들린다.
-- 결제일은 두지 않는다 — "언제"는 만기 관리와 일정의 몫이고, 여기는 "얼마"만 묻는다.

create table fixed_expenses
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

-- 화면은 한 사용자의 전부를 한 번에 읽는다. 보이는 순서(부담 큰 순)는 파생값이라 정렬은 응용에서 한다.
create index ix_fixed_expenses_user on fixed_expenses (user_id, created_at);
