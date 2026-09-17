"use client";

import { useRef } from "react";
import Link from "next/link";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { moduleIcon } from "@/modules/registry";
import { useMyModules, useToggleModule } from "@/modules/useMyModules";
import { SCHEDULE_MODULE_ID } from "@/modules/schedule/id";
import { EXPENSE_MODULE_ID } from "@/modules/expense/id";
import { RENEWAL_MODULE_ID } from "@/modules/renewal/id";
import type { ModuleSummary } from "@/lib/types";

/**
 * 처음 골라 볼 만한 셋.
 *
 * 켜자마자 **대시보드가 달라지는 것**으로 골랐다. 일정은 오늘 할 일을 채우고, 고정지출은
 * 몇 줄만 적어도 "한 달 얼마"가 나오고, 만기는 다가오는 일에 줄이 생긴다.
 * 가이드(퇴사·이사 준비)는 그 상황인 사람에게만 뜻이 있어 첫 제안에서 뺐다.
 */
const STARTERS = [SCHEDULE_MODULE_ID, EXPENSE_MODULE_ID, RENEWAL_MODULE_ID];

/** 아직 안 켠 제안들. 켠 것은 목록에서 빠지므로 남은 것만 권하게 된다. */
function remainingStarters(modules: ModuleSummary[] | undefined): ModuleSummary[] {
  // STARTERS 순서를 지킨다 — 권하는 차례가 정해져 있고, 서버가 주는 순서는 사이드바 것이다.
  return STARTERS.flatMap((id) => {
    const module = modules?.find((candidate) => candidate.id === id);
    return module && !module.enabled ? [module] : [];
  });
}

/**
 * 환영 카드를 보일지.
 *
 * "지금 켠 게 하나도 없나"로만 보면 **하나 켜는 순간 카드가 발밑에서 사라진다** —
 * 셋을 나란히 권해 놓고 하나 고르면 나머지를 치우는 셈이다. 그래서 판정을 둘로 나눈다:
 * 들어올 때 빈손이었는지(한 번 정하면 이 방문 동안 안 바뀐다)와, 아직 권할 것이 남았는지.
 *
 * 다 켜면 비켜 준다. 남은 제안이 없는 카드는 안내가 아니라 빈 상자다.
 */
export function useWelcome(): boolean {
  const { data: modules } = useMyModules();

  // 목록이 도착한 첫 순간의 판정만 붙든다. 그 전(로딩 중)에는 아직 아무것도 정하지 않는다.
  const startedEmpty = useRef<boolean | null>(null);
  if (modules && startedEmpty.current === null) {
    startedEmpty.current = modules.every((module) => !module.enabled);
  }

  return startedEmpty.current === true && remainingStarters(modules).length > 0;
}

function Starter({ module }: { module: ModuleSummary }) {
  const toggle = useToggleModule();
  const Icon = moduleIcon(module.id);

  return (
    <li className="flex flex-wrap items-center gap-x-3 gap-y-1.5 py-3">
      <span className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-primary-light text-primary">
        <Icon size={17} aria-hidden />
      </span>

      <span className="min-w-0 flex-1">
        <span className="block text-[15px] font-semibold text-grey-900">{module.displayName}</span>
        <span className="block truncate text-[12.5px] text-grey-500">{module.description}</span>
      </span>

      <Button
        size="sm"
        variant="secondary"
        disabled={toggle.isPending}
        onClick={() => toggle.mutate({ moduleId: module.id, enable: true })}
      >
        {toggle.isPending ? "켜는 중" : "켜기"}
      </Button>

      {/*
        실패를 말하지 않으면 버튼이 "켜는 중"에서 "켜기"로 돌아올 뿐이라, 누른 사람은
        자기가 잘못 눌렀다고 여긴다. 처음 눌러 보는 버튼이라 더 그렇다.
      */}
      {toggle.isError && (
        <span role="alert" className="w-full text-[12.5px] text-danger">
          켜지 못했어요. 잠시 후 다시 시도해 주세요.
        </span>
      )}
    </li>
  );
}

/**
 * 켠 모듈이 없을 때의 대시보드. 보일지 말지는 [useWelcome] 이 정한다.
 *
 * 이게 없으면 첫 화면이 **"모듈을 켜세요" 카드 넷**이 된다. 같은 말을 네 번 하면서
 * 정작 무엇부터 켜야 하는지는 알려 주지 않는다.
 *
 * 그렇다고 가입할 때 몇 개를 대신 켜 주지는 않는다. 이 제품은 "필요한 만큼만 켠다"가
 * 전제라, 묻지 않고 켜 두면 그 전제부터 어긋난다. 고르는 일은 사용자에게 남기고
 * **첫 선택만 쉽게** 만든다.
 */
export function WelcomeCard() {
  const { data: modules } = useMyModules();
  const starters = remainingStarters(modules);

  return (
    <Card>
      <h2 className="text-[17px] font-bold text-grey-900">어떤 것부터 시작할까요?</h2>
      <p className="mt-1 text-[14px] leading-relaxed text-grey-500">
        아이테라는 인생의 영역별 도구를 필요한 만큼만 켜서 쓰는 곳이에요. 하나만 켜도 이 화면이
        달라지고, 중지해도 데이터는 남아요.
      </p>

      <ul className="mt-3 divide-y divide-grey-100">
        {starters.map((module) => (
          <Starter key={module.id} module={module} />
        ))}
      </ul>

      <Link
        href="/settings/modules"
        className="mt-3 inline-block text-[14px] font-semibold text-primary hover:underline"
      >
        전체 모듈 둘러보기
      </Link>
    </Card>
  );
}
