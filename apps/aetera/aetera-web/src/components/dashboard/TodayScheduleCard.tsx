"use client";

import Link from "next/link";
import { ArrowRight, CalendarDays, Puzzle } from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { cn } from "@/components/ui/cn";
import { formatTime } from "@/modules/schedule/calendar";
import { DEFAULT_EVENT_COLOR } from "@/modules/schedule/colors";
import { SCHEDULE_MODULE_ID } from "@/modules/schedule/id";
import { modulePath } from "@/modules/types";
import type { ScheduleEvent } from "@/modules/schedule/api";

interface TodayScheduleCardProps {
  /** null 이면 일정 모듈 미사용. */
  events: ScheduleEvent[] | null;
  isLoading: boolean;
  /** 조회 실패. 이걸 빈 목록으로 흘려보내면 일정이 가득한 사용자에게 "일정 없음"이라고 말하게 된다. */
  isError?: boolean;
}

export function TodayScheduleCard({ events, isLoading, isError }: TodayScheduleCardProps) {
  if (events === null) {
    return (
      <Card className="flex h-full flex-col items-center justify-center gap-3 py-10 text-center">
        <span className="flex size-11 items-center justify-center rounded-2xl bg-primary-light text-primary">
          <Puzzle size={20} />
        </span>
        <p className="text-[15px] font-semibold text-grey-800">일정 모듈을 켜면 오늘 일정이 여기 표시돼요</p>
        <Link href="/settings/modules" className="text-[14px] font-semibold text-primary hover:underline">
          모듈 살펴보기
        </Link>
      </Card>
    );
  }

  // 현재 시점 기준으로 아직 끝나지 않은 가장 가까운 일정을 강조한다.
  const now = Date.now();
  const nearestId = events.find((event) => new Date(event.endsAt).getTime() >= now)?.id;

  return (
    <Card className="h-full">
      <CardHeader
        icon={<CalendarDays size={16} />}
        title={isLoading || isError ? "오늘 할 일" : `오늘 할 일 · ${events.length}`}
        action={
          <Link
            href={modulePath(SCHEDULE_MODULE_ID)}
            className="flex items-center gap-0.5 text-[13px] font-semibold text-primary hover:underline"
          >
            전체 일정 보기 <ArrowRight size={14} />
          </Link>
        }
      />

      {isLoading ? (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      ) : isError ? (
        <div className="py-7 text-center">
          <p className="text-[15px] font-medium text-grey-700">일정을 불러오지 못했어요.</p>
          <p className="mt-1 text-[13px] text-grey-500">잠시 후 다시 시도해 주세요.</p>
        </div>
      ) : events.length === 0 ? (
        <div className="py-7 text-center">
          <p className="text-[15px] font-medium text-grey-700">오늘 예정된 일이 없어요.</p>
          <p className="mt-1 text-[13px] text-grey-500">여유 시간을 목표나 휴식에 활용해보세요.</p>
        </div>
      ) : (
        <ul className="flex flex-col">
          {events.map((event) => {
            const nearest = event.id === nearestId;
            return (
              <li
                key={event.id}
                className={cn(
                  "flex items-center gap-3 rounded-xl px-2 py-2.5",
                  nearest && "bg-primary-light/60",
                )}
              >
                <span
                  className="h-9 w-1 shrink-0 rounded-full"
                  style={{ backgroundColor: event.color ?? DEFAULT_EVENT_COLOR }}
                />
                <span className="w-16 shrink-0 text-[13px] font-semibold tabular-nums text-grey-600">
                  {event.allDay ? "하루 종일" : formatTime(event.startsAt)}
                </span>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-[15px] font-semibold text-grey-900">{event.title}</p>
                  {event.description && (
                    <p className="truncate text-[12px] text-grey-500">{event.description}</p>
                  )}
                </div>
                {nearest && (
                  <span className="shrink-0 rounded-(--radius-chip) bg-primary px-1.5 py-0.5 text-[10px] font-semibold text-white">
                    다음 일정
                  </span>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </Card>
  );
}
