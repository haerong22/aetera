"use client";

interface TodayHeaderProps {
  nickname?: string;
  /** null 이면 일정 모듈 미사용이거나 아직 못 읽음 — 그럴 땐 숫자를 말하지 않는다. */
  eventCount: number | null;
}

function greeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "좋은 아침이에요";
  if (hour < 18) return "좋은 오후예요";
  return "좋은 저녁이에요";
}

/** 셀 수 있는 것만 센다. 숫자를 못 아는 상태에서는 아는 척하지 않는다. */
function summaryLine(eventCount: number | null): string {
  if (eventCount === null) return "오늘 하루를 시작해 볼까요?";
  if (eventCount === 0) return "오늘은 예정된 일정이 없어요.";
  return `오늘은 일정이 ${eventCount}개 있어요.`;
}

export function TodayHeader({ nickname, eventCount }: TodayHeaderProps) {
  const today = new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long",
  }).format(new Date());

  return (
    <header className="flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="text-[24px] font-bold leading-snug text-grey-900 sm:text-[26px]">
          {greeting()}, {nickname ?? ""}님
        </h1>
        <p className="mt-1.5 text-[15px] text-grey-500">{summaryLine(eventCount)}</p>
      </div>
      <time className="text-[13px] font-medium text-grey-500">{today}</time>
    </header>
  );
}
