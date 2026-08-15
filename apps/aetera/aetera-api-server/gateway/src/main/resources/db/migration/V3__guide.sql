-- ── 모듈: 가이드(여정형) ────────────────────────────────────────────────────
-- 콘텐츠(단계·할 일·마감 오프셋)는 테이블에 없다. 코드에 배포된 GuideTemplate 이 콘텐츠이고,
-- 여기에는 개인화되는 것만 남긴다: 기준일 하나와, 사용자가 실제로 손댄 항목의 체크·메모.
--
-- 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
-- 반대로 같은 모듈 안에서는 FK 를 건다 — 진행 행이 사라진 여정을 가리키면 안 된다.

create table guide_journeys
(
    id          uuid                     primary key,
    user_id     uuid                     not null,
    guide_id    varchar(50)              not null,
    anchor_date date                     not null,
    started_at  timestamp with time zone not null,
    version     bigint                   not null default 0
);

-- 한 사람이 같은 가이드를 동시에 두 번 밟지는 않는다.
create unique index ux_guide_journeys_user_guide on guide_journeys (user_id, guide_id);

create table guide_task_progresses
(
    id         uuid                     primary key,
    journey_id uuid                     not null,
    task_key   varchar(80)              not null,
    done       boolean                  not null,
    note       text,
    updated_at timestamp with time zone not null,
    version    bigint                   not null default 0,
    constraint fk_guide_task_progresses_journey foreign key (journey_id) references guide_journeys (id)
);

-- 조회(여정 단위 전체 읽기)와 중복 방지를 한 인덱스로 겸한다.
create unique index ux_guide_task_progresses_journey_task on guide_task_progresses (journey_id, task_key);
