"use client";

import { useState, type FormEvent } from "react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { errorMessage } from "@/lib/api-client";
import { useAuth } from "@/lib/auth";
import { NewPasswordFields, useNewPassword } from "@/components/auth/NewPasswordFields";
import { useChangeNickname, useChangePassword } from "@/lib/profile";
import type { User } from "@/lib/types";

/**
 * 내 계정.
 *
 * 이메일은 보여만 준다 — 로그인 아이디이자 알림이 가는 곳이라, 바꾸려면 새 주소가 진짜
 * 그 사람 것인지 확인하는 절차가 먼저 있어야 한다. 그 절차가 없는 동안 입력칸을 두면
 * 바꿀 수 있는 척하는 셈이다.
 */
export function ProfileSection() {
  const { user, applyProfile } = useAuth();

  return (
    <section className="flex flex-col gap-3">
      <div>
        <h2 className="text-[17px] font-bold text-grey-900">내 계정</h2>
        <p className="mt-1 text-[14px] text-grey-500">이름과 비밀번호를 바꿀 수 있어요.</p>
      </div>

      <Card className="flex flex-col gap-4">
        <div>
          <p className="text-[12.5px] text-grey-500">이메일</p>
          <p className="mt-0.5 text-[15px] text-grey-900">{user?.email ?? "—"}</p>
        </div>

        <NicknameForm nickname={user?.nickname ?? ""} onSaved={applyProfile} />
      </Card>

      <PasswordCard />
    </section>
  );
}

function NicknameForm({ nickname, onSaved }: { nickname: string; onSaved: (user: User) => void }) {
  const [value, setValue] = useState(nickname);
  const change = useChangeNickname();

  /*
   * 아직 프로필이 안 왔을 때는 빈 칸으로 시작했다가, 도착하면 그 값으로 맞춘다.
   * 렌더 중에 상태를 맞추는 쪽이 `useEffect` 보다 한 번 덜 그린다 — 옛 값이 잠깐
   * 비치는 일이 없다.
   */
  const [seeded, setSeeded] = useState(nickname);
  if (nickname !== seeded) {
    setSeeded(nickname);
    setValue(nickname);
  }

  // 공백만 다른 것은 서버가 다듬어 같은 값이 되므로, 보내 봐야 "저장됨"만 깜빡인다.
  const changed = value.trim() !== nickname && value.trim() !== "";

  /**
   * 입력이 바뀌면 지난 결과를 버린다.
   *
   * `isSuccess`/`isError` 는 한 번 켜지면 다음 요청까지 안 꺼진다. 그대로 두면
   * 저장한 뒤 이름을 고쳤다가 되돌렸을 때 **저장한 적 없는데 "저장했어요"가 뜬다.**
   * 실패 문구도 마찬가지로, 이미 고쳐 적은 값 위에 옛 이유가 남는다.
   */
  function edit(next: string) {
    setValue(next);
    if (change.isSuccess || change.isError) change.reset();
  }

  function save(event: FormEvent) {
    event.preventDefault();
    if (!changed) return;
    change.mutate(value, { onSuccess: onSaved });
  }

  return (
    <form onSubmit={save} className="flex flex-col gap-2">
      <Input
        label="이름"
        value={value}
        maxLength={30}
        disabled={change.isPending}
        onChange={(event) => edit(event.target.value)}
      />

      {change.isError && (
        <p role="alert" className="text-[13px] text-danger">
          {errorMessage(change.error, "이름을 바꾸지 못했어요.")}
        </p>
      )}
      {change.isSuccess && (
        <p role="status" className="text-[13px] text-grey-500">
          저장했어요.
        </p>
      )}

      <div className="flex justify-end">
        <Button type="submit" variant="secondary" disabled={!changed || change.isPending}>
          {change.isPending ? "저장 중" : "이름 바꾸기"}
        </Button>
      </div>
    </form>
  );
}

function PasswordCard() {
  const [open, setOpen] = useState(false);
  const [current, setCurrent] = useState("");
  const password = useNewPassword();
  const [done, setDone] = useState(false);
  const change = useChangePassword();

  function close() {
    if (change.isPending) return;
    setOpen(false);
    setCurrent("");
    password.clear();
    change.reset();
  }

  // 지금 비밀번호까지 있어야 보낼 수 있다. 새 비밀번호 두 칸은 [NewPasswordFields] 가 본다.
  const ready = current !== "" && password.ready;

  function submit(event: FormEvent) {
    event.preventDefault();
    if (!ready) return;
    change.mutate(
      { currentPassword: current, newPassword: password.value },
      {
        onSuccess: () => {
          setDone(true);
          setOpen(false);
          setCurrent("");
          password.clear();
        },
      },
    );
  }

  return (
    <>
      <Card className="flex flex-wrap items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="text-[15px] font-semibold text-grey-900">비밀번호</p>
          <p className="mt-0.5 text-[12.5px] text-grey-500">
            바꾸면 다른 기기는 모두 로그아웃돼요. 이 기기만 그대로예요.
          </p>
        </div>
        <Button
          variant="secondary"
          onClick={() => {
            setDone(false);
            setOpen(true);
          }}
        >
          비밀번호 바꾸기
        </Button>
      </Card>

      {done && (
        <p role="status" className="text-[13px] text-grey-500">
          비밀번호를 바꿨어요. 다른 기기는 로그아웃됐어요.
        </p>
      )}

      <Dialog open={open} onClose={close} title="비밀번호 바꾸기">
        <form onSubmit={submit} className="flex flex-col gap-3">
          <Input
            label="지금 비밀번호"
            type="password"
            autoComplete="current-password"
            autoFocus
            value={current}
            disabled={change.isPending}
            onChange={(event) => setCurrent(event.target.value)}
          />
          <NewPasswordFields password={password} disabled={change.isPending} />

          {change.isError && (
            <p role="alert" className="text-[13px] text-danger">
              {errorMessage(change.error, "비밀번호를 바꾸지 못했어요.")}
            </p>
          )}

          <div className="mt-2 flex items-center gap-2">
            <Button
              type="button"
              variant="ghost"
              className="flex-1"
              disabled={change.isPending}
              onClick={close}
            >
              취소
            </Button>
            <Button type="submit" className="flex-1" disabled={!ready || change.isPending}>
              {change.isPending ? "바꾸는 중" : "바꾸기"}
            </Button>
          </div>
        </form>
      </Dialog>
    </>
  );
}

