import type { ReactNode } from "react";
import { Card } from "./Card";
import { cn } from "./cn";

/** 아이콘 배지의 색. 무슨 상태인지에 따라 갈린다. */
const TONE = {
  neutral: "bg-grey-100 text-grey-400",
  primary: "bg-primary-light text-primary",
  danger: "bg-danger-light text-danger",
} as const;

/**
 * 목록 대신 보여 주는 한 장짜리 상태 카드.
 *
 * 비었을 때·못 불러왔을 때·모듈을 안 켰을 때가 **같은 모양**이어야 한다.
 * 세 곳이 각자 그리던 시절엔 아이콘 크기가 11·12·14, 세로 여백이 12·14·10 이었는데,
 * 나란히 놓고 보지 않으면 아무도 알아채지 못한다.
 *
 * 뜻은 이름으로 남긴다 — 부르는 쪽은 [EmptyState] 처럼 "무슨 상태인지"를 쓴다.
 */
export function StatusCard({
  icon,
  tone,
  title,
  description,
  action,
}: {
  icon: ReactNode;
  tone: keyof typeof TONE;
  title: string;
  description: string;
  /** 여기서 바로 할 수 있는 일. 사용자가 할 수 있는 게 없으면 비운다. */
  action?: ReactNode;
}) {
  return (
    <Card className="flex flex-col items-center gap-3 py-12 text-center">
      <span className={cn("flex size-12 items-center justify-center rounded-2xl", TONE[tone])}>
        {icon}
      </span>
      <div>
        <p className="text-[15px] font-semibold text-grey-800">{title}</p>
        <p className="mx-auto mt-1 max-w-sm text-[13.5px] leading-relaxed text-grey-500">
          {description}
        </p>
      </div>
      {action}
    </Card>
  );
}

/**
 * 아직 아무것도 없는 목록 자리.
 *
 * **무엇을 넣으면 되는지까지 말한다.** "항목이 없습니다"만 띄우면 사용자는 여기가
 * 고장 난 건지 자기가 뭘 안 한 건지 알 수 없다.
 */
export function EmptyState(props: Omit<Parameters<typeof StatusCard>[0], "tone">) {
  return <StatusCard tone="neutral" {...props} />;
}
