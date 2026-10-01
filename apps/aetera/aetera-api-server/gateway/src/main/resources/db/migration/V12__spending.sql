-- 변동지출. 달에 한 줄, 총액 하나.
--
-- 고정지출(fixed_expenses)과 표를 나눈다 — 저쪽은 "등록해 두면 매달 나가는 항목"이고
-- 여기는 "그 달이 끝난 뒤 적는 총액"이라 모양도 수명도 다르다.
create table spending_records
(
    id          uuid                     primary key,
    user_id     uuid                     not null,
    -- 언제나 그 달의 1일. 화면이 며칠을 보내도 같은 달이면 같은 줄이어야 한다.
    month       date                     not null,
    amount      bigint                   not null,
    note        varchar(200),
    recorded_at timestamp with time zone not null,
    version     bigint                   not null default 0,
    constraint fk_spending_records_user foreign key (user_id) references users (id)
);

-- 한 달에 하나뿐이다. 저장이 덮어쓰기이므로 이 인덱스가 **같은 달 두 줄**을 막는다 —
-- 동시에 두 번 저장해도 뒤에 온 쪽이 충돌로 걸리고, 조용히 둘이 남지 않는다.
create unique index ux_spending_records_user_month on spending_records (user_id, month);
