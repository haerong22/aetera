"use client";

import { MessageCircle, Plus, Sparkles } from "lucide-react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { formatTime } from "@/modules/schedule/calendar";
import type { ScheduleEvent } from "@/modules/schedule/api";

interface AeteraBriefingCardProps {
  /** 오늘 일정. null 이면 일정 모듈 미사용. */
  events: ScheduleEvent[] | null;
}

/**
 * AI 가 하루를 먼저 요약해 주는 브리핑 카드.
 * 실제 AI 는 아직 연결되지 않았다 — 문장은 오늘 일정 데이터에서 규칙 기반으로 만들고,
 * 코칭 문구는 프리뷰 고정 문구다. 버튼은 UI 상태만 구현한다.
 */
export function AeteraBriefingCard({ events }: AeteraBriefingCardProps) {
  const lines = buildBriefing(events);

  return (
    <section
      aria-label="Aetera Briefing"
      className="rounded-(--radius-card) border border-[#e3e5fb] bg-gradient-to-br from-[#f4f5ff] to-white p-5 shadow-[0_2px_12px_rgba(15,23,42,0.04)] sm:p-6"
    >
      <div className="mb-3 flex items-center gap-2.5">
        <span className="flex size-8 items-center justify-center rounded-xl bg-primary text-white">
          <Sparkles size={16} />
        </span>
        <h2 className="text-[16px] font-bold text-grey-900 sm:text-[17px]">Aetera Briefing</h2>
        <Badge tone="grey">미리보기</Badge>
      </div>

      <div className="flex flex-col gap-1.5">
        {lines.map((line) => (
          <p key={line} className="text-[15px] leading-relaxed text-grey-700">
            {line}
          </p>
        ))}
      </div>

      <div className="mt-5 flex flex-wrap gap-2">
        <Button size="sm" disabled title="AI 코치 기능과 함께 제공될 예정이에요">
          <Plus size={15} /> 오늘 계획에 추가
        </Button>
        <Button size="sm" variant="secondary" disabled title="AI 코치 기능과 함께 제공될 예정이에요">
          <MessageCircle size={15} /> AI와 대화
        </Button>
        <span className="self-center text-[12px] text-grey-400">AI 코치 기능과 함께 제공될 예정이에요</span>
      </div>
    </section>
  );
}

function buildBriefing(events: ScheduleEvent[] | null): string[] {
  if (events === null) {
    return [
      "일정 모듈을 켜면 하루 브리핑이 더 정확해져요.",
      "지금은 우선순위와 목표를 중심으로 하루를 계획해보세요.",
    ];
  }
  if (events.length === 0) {
    return [
      "오늘 예정된 일정이 없어요. 온전히 쓸 수 있는 하루예요.",
      "밀린 개인 목표를 진행하거나, 여유를 계획에 넣어보는 건 어때요?",
    ];
  }

  const first = events[0];
  const firstLine =
    events.length === 1
      ? `오늘 ${formatTime(first.startsAt)}에 「${first.title}」 일정이 있어요.`
      : `오늘 ${formatTime(first.startsAt)} 「${first.title}」 를 시작으로 ${events.length}개의 일정이 있어요.`;

  return [
    firstLine,
    "첫 일정 전에 우선순위를 10분 정도 정리해보세요.",
    "비어 있는 시간대는 밀린 개인 목표를 진행하기 좋아요.",
  ];
}
