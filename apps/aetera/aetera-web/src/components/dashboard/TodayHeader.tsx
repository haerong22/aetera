"use client";

interface TodayHeaderProps {
  nickname?: string;
  /** null 이면 일정 모듈 미사용 — 문구에서 일정 언급을 뺀다. */
  eventCount: number | null;
  priorityCount: number;
}

function greeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "좋은 아침이에요";
  if (hour < 18) return "좋은 오후예요";
  return "좋은 저녁이에요";
}

function summaryLine(eventCount: number | null, priorityCount: number): string {
  const priority = `확인할 우선순위 ${priorityCount}개`;
  if (eventCount === null) return `오늘은 ${priority}가 있어요.`;
  if (eventCount === 0) return `오늘은 예정된 일정 없이 ${priority}가 있어요.`;
  return `오늘은 일정 ${eventCount}개와 ${priority}가 있어요.`;
}

export function TodayHeader({ nickname, eventCount, priorityCount }: TodayHeaderProps) {
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
        <p className="mt-1.5 text-[15px] text-grey-500">{summaryLine(eventCount, priorityCount)}</p>
      </div>
      <time className="text-[13px] font-medium text-grey-500">{today}</time>
    </header>
  );
}
