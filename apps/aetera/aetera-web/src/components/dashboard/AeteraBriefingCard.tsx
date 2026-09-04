"use client";

import { Sparkles } from "lucide-react";
import { formatTime } from "@/modules/schedule/calendar";
import type { ScheduleEvent } from "@/modules/schedule/api";

interface AeteraBriefingCardProps {
  /** 오늘 일정. 요약할 것이 이것뿐이라 비어 있어도 목록은 필요하다. */
  events: ScheduleEvent[];
}

/**
 * 하루를 먼저 요약해 주는 브리핑 카드.
 *
 * 문장은 **오늘 일정에서 규칙으로 만든다.** AI 는 아직 없고, 없는 걸 있는 것처럼
 * 보이려고 "미리보기" 배지나 눌리지 않는 버튼을 두지 않는다 —
 * 지금 할 수 있는 말만 하고, AI 가 붙으면 그때 문장이 좋아진다.
 *
 * **다른 모듈을 권하지 않는다.** "밀린 목표를 진행해 보세요" 같은 말은 목표 모듈을
 * 켠 사람에게만 성립하는데, 여기서는 그걸 알 방법이 없다. 손에 든 일정만 가지고 말한다.
 *
 * 일정 모듈을 안 켰으면 이 카드는 아예 그리지 않는다(대시보드가 판단한다) —
 * 요약할 것이 없고, 켜라는 안내는 옆의 오늘 일정 카드가 이미 한다.
 */
export function AeteraBriefingCard({ events }: AeteraBriefingCardProps) {
  const lines = buildBriefing(events);

  return (
    <section
      aria-label="오늘 브리핑"
      className="rounded-(--radius-card) border border-[#e3e5fb] bg-gradient-to-br from-[#f4f5ff] to-white p-5 shadow-[0_2px_12px_rgba(15,23,42,0.04)] sm:p-6"
    >
      <div className="mb-3 flex items-center gap-2.5">
        <span className="flex size-8 items-center justify-center rounded-xl bg-primary text-white">
          <Sparkles size={16} />
        </span>
        <h2 className="text-[16px] font-bold text-grey-900 sm:text-[17px]">오늘 브리핑</h2>
      </div>

      <div className="flex flex-col gap-1.5">
        {lines.map((line) => (
          <p key={line} className="text-[15px] leading-relaxed text-grey-700">
            {line}
          </p>
        ))}
      </div>

    </section>
  );
}

/** 종일 일정은 시각이 없다. "00:00 에 있어요" 라고 하면 안 잡은 약속이 생긴다. */
function when(event: ScheduleEvent): string {
  return event.allDay ? "하루 종일" : `${formatTime(event.startsAt)}에`;
}

function buildBriefing(events: ScheduleEvent[]): string[] {
  if (events.length === 0) return ["오늘 예정된 일정이 없어요. 온전히 쓸 수 있는 하루예요."];

  const first = events[0];
  if (events.length === 1) return [`오늘 ${when(first)} 「${first.title}」 일정이 있어요.`];

  const last = events[events.length - 1];
  return [
    `오늘 ${when(first)} 「${first.title}」 를 시작으로 ${events.length}개의 일정이 있어요.`,
    `마지막은 ${when(last)} 「${last.title}」 예요.`,
  ];
}
