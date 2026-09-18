-- ── 알림 설정 ─────────────────────────────────────────────────────────────
-- 모듈이 아니라 코어 기능이다. 만기·가이드처럼 날짜가 걸린 모듈들이 여기로 모여 나간다.
--
-- send_hour 는 사용자의 시간대 기준 시각이다. 잡이 한 시간마다 돌면서
-- "지금 그 사람에게 몇 시인가"를 보고 고른다.
--
-- last_sent_on 은 하루 한 통을 넘지 않게 막는 유일한 장치다. 메일은 되돌릴 수 없어서
-- 잡이 두 번 돌거나 서버가 여러 대일 때 "아마 안 보냈겠지"로 넘길 수 없다.

create table notification_preferences
(
    id           uuid    primary key,
    user_id      uuid    not null,
    enabled      boolean not null default true,
    send_hour    int     not null default 8,
    last_sent_on date,
    version      bigint  not null default 0
);

-- 한 사람에 하나. 잡이 같은 사람의 설정을 두 벌 보면 메일이 두 번 간다.
create unique index ux_notification_preferences_user on notification_preferences (user_id);

-- 잡이 매시 "켠 사람"만 훑는다.
create index ix_notification_preferences_enabled on notification_preferences (enabled) where enabled;
