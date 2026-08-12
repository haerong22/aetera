"use client";

import { History } from "lucide-react";
import { Badge } from "@/components/ui/Badge";
import { Card, CardHeader } from "@/components/ui/Card";
import { formatTime } from "@/modules/schedule/calendar";
import type { ScheduleEvent } from "@/modules/schedule/api";
import { MOCK_TIMELINE } from "./mock";

interface RecentTimelineCardProps {
  /** 오늘 일정 — 있으면 타임라인 맨 위에 실제 데이터로 한 줄 얹는다. */
  events: ScheduleEvent[] | null;
}

/**
 * 최근 기록 — Aetera 의 장기 핵심 기능인 인생 타임라인의 프리뷰.
 * 백엔드 타임라인 기능이 아직 없어, 오늘 일정 한 줄(실데이터) 외에는 mock 이다.
 */
export function RecentTimelineCard({ events }: RecentTimelineCardProps) {
  const entries = [
    ...(events && events.length > 0
      ? [
          {
            id: "today-schedule",
            when: `오늘 ${events[0].allDay ? "" : formatTime(events[0].startsAt)}`.trim(),
            text: `${events[0].title} 예정`,
          },
        ]
      : []),
    ...MOCK_TIMELINE,
  ];

  return (
    <Card>
      <CardHeader
        icon={<History size={16} />}
        title="최근 기록"
        action={<Badge tone="grey">미리보기</Badge>}
      />

      <ol className="flex flex-col">
        {entries.map((entry, index) => (
          <li key={entry.id} className="flex gap-3">
            {/* 타임라인 축 */}
            <div className="flex flex-col items-center">
              <span className="mt-1.5 size-2 shrink-0 rounded-full bg-primary/60" aria-hidden="true" />
              {index < entries.length - 1 && <span className="w-px flex-1 bg-grey-200" aria-hidden="true" />}
            </div>
            <div className="flex min-w-0 flex-1 items-baseline gap-3 pb-4">
              <span className="w-24 shrink-0 text-[13px] font-medium text-grey-500">{entry.when}</span>
              <span className="min-w-0 truncate text-[14px] text-grey-800">{entry.text}</span>
            </div>
          </li>
        ))}
      </ol>

      <p className="text-[12px] text-grey-400">
        일정·목표·기록이 쌓이면 인생 전체를 돌아보는 타임라인으로 자라나요.
      </p>
    </Card>
  );
}
