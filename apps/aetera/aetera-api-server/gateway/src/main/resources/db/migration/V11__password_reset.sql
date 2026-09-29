-- 비밀번호 재설정 토큰.
--
-- 원문은 메일에만 있고 여기에는 SHA-256 해시만 들어간다 — 이 표가 유출돼도 남의
-- 비밀번호를 바꿀 수 없다. 평문으로 두면 표 한 벌이 곧 전 계정의 마스터키가 된다.
create table password_reset_tokens
(
    id         uuid                     primary key,
    user_id    uuid                     not null,
    token_hash varchar(100)             not null,
    issued_at  timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    -- 한 번 쓰면 시각이 박힌다. 메일은 지워지지 않고 남으므로 다 쓴 링크는 죽어야 한다.
    used_at    timestamp with time zone,
    version    bigint                   not null default 0,
    constraint fk_password_reset_tokens_user foreign key (user_id) references users (id)
);

-- 해시로 찾는다. 유니크라 같은 해시가 두 줄 생기면 그 자리에서 막힌다.
create unique index ux_password_reset_tokens_token_hash on password_reset_tokens (token_hash);
-- 비밀번호가 바뀔 때 남은 토큰을 한꺼번에 죽이고, 탈퇴할 때 한꺼번에 지운다.
create index ix_password_reset_tokens_user_id on password_reset_tokens (user_id);
