"use client";

import { useState } from "react";
import { Input } from "@/components/ui/Input";

/**
 * 새 비밀번호를 두 번 받는 자리.
 *
 * **서버는 확인 칸을 모른다** — 새 비밀번호를 한 번만 받으므로 오타를 걸러 줄 곳이 화면뿐이다.
 * 놓치면 오타난 비밀번호가 그대로 저장되고, 본인도 다음 로그인에서야 안다. 비밀번호 찾기
 * 쪽은 더 나쁘다 — 링크는 이미 죽어서 **다시 요청하는 수밖에 없다.**
 *
 * 그래서 규칙을 두 화면에 따로 적지 않는다. 갈리면 한쪽만 느슨해지는데, 느슨해진 쪽이
 * 어디인지는 아무도 모른다.
 */
export interface NewPassword {
  value: string;
  again: string;
  setValue: (next: string) => void;
  setAgain: (next: string) => void;
  /** 두 칸이 다르다. 두 번째 칸에 무언가 적힌 뒤에만 참이다 — 적는 도중에 빨개지면 성가시다. */
  mismatched: boolean;
  /** 보낼 수 있는 상태인가. */
  ready: boolean;
  clear: () => void;
}

export function useNewPassword(): NewPassword {
  const [value, setValue] = useState("");
  const [again, setAgain] = useState("");

  return {
    value,
    again,
    setValue,
    setAgain,
    mismatched: again !== "" && value !== again,
    ready: value !== "" && value === again,
    clear: () => {
      setValue("");
      setAgain("");
    },
  };
}

export function NewPasswordFields({
  password,
  disabled = false,
  autoFocus = false,
}: {
  password: NewPassword;
  disabled?: boolean;
  autoFocus?: boolean;
}) {
  return (
    <>
      <Input
        label="새 비밀번호"
        type="password"
        autoComplete="new-password"
        autoFocus={autoFocus}
        value={password.value}
        disabled={disabled}
        onChange={(event) => password.setValue(event.target.value)}
      />
      <Input
        label="새 비밀번호 확인"
        type="password"
        autoComplete="new-password"
        value={password.again}
        disabled={disabled}
        onChange={(event) => password.setAgain(event.target.value)}
      />

      <p className="text-[12.5px] text-grey-500">영문과 숫자를 섞어 8자 이상이어야 해요.</p>

      {password.mismatched && (
        <p role="alert" className="text-[13px] text-danger">
          새 비밀번호가 서로 달라요.
        </p>
      )}
    </>
  );
}
