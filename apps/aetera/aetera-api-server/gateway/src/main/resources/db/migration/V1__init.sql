-- ── 코어: 회원 ──────────────────────────────────────────────────────────────

create table users
(
    id            uuid                     primary key,
    email         varchar(320)             not null,
    nickname      varchar(30)              not null,
    timezone      varchar(50)              not null,
    status        varchar(20)              not null,
    registered_at timestamp with time zone not null,
    withdrawn_at  timestamp with time zone,
    version       bigint                   not null default 0
);

create unique index ux_users_email on users (email);

-- ── 코어: 인증 ──────────────────────────────────────────────────────────────
-- 인증 수단을 프로필과 분리해 둔다. 카카오 로그인 추가 = provider 행 추가.

create table auth_credentials
(
    id               uuid                     primary key,
    user_id          uuid                     not null,
    provider         varchar(20)              not null,
    provider_user_id varchar(100),
    password_hash    varchar(300),
    created_at       timestamp with time zone not null,
    version          bigint                   not null default 0,
    constraint fk_auth_credentials_user foreign key (user_id) references users (id)
);

create unique index ux_auth_credentials_user_provider on auth_credentials (user_id, provider);
create unique index ux_auth_credentials_provider_user
    on auth_credentials (provider, provider_user_id)
    where provider_user_id is not null;

-- 원문은 저장하지 않는다. token_hash 는 SHA-256(base64url).
create table refresh_tokens
(
    id         uuid                     primary key,
    user_id    uuid                     not null,
    token_hash varchar(100)             not null,
    issued_at  timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    revoked_at timestamp with time zone,
    version    bigint                   not null default 0,
    constraint fk_refresh_tokens_user foreign key (user_id) references users (id)
);

create unique index ux_refresh_tokens_token_hash on refresh_tokens (token_hash);
create index ix_refresh_tokens_user_id on refresh_tokens (user_id);

-- ── 코어: 모듈 사용 상태 ────────────────────────────────────────────────────

create table module_enrollments
(
    id          uuid                     primary key,
    user_id     uuid                     not null,
    module_id   varchar(50)              not null,
    status      varchar(20)              not null,
    enabled_at  timestamp with time zone not null,
    disabled_at timestamp with time zone,
    version     bigint                   not null default 0,
    constraint fk_module_enrollments_user foreign key (user_id) references users (id)
);

create unique index ux_module_enrollments_user_module on module_enrollments (user_id, module_id);

-- ── 모듈: 일정 ──────────────────────────────────────────────────────────────
-- 모듈 데이터는 사용자 테이블에 FK 를 걸지 않는다(플랫폼 규약).
-- 모듈을 떼어 내거나 데이터를 지울 때 코어와 얽히지 않기 위해서다.

create table schedule_events
(
    id          uuid                     primary key,
    user_id     uuid                     not null,
    title       varchar(200)             not null,
    description text,
    starts_at   timestamp with time zone not null,
    ends_at     timestamp with time zone not null,
    all_day     boolean                  not null,
    color       varchar(7),
    created_at  timestamp with time zone not null,
    version     bigint                   not null default 0
);

create index ix_schedule_events_user_starts on schedule_events (user_id, starts_at);
