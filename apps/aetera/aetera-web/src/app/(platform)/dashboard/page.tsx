"use client";

import { useAuth } from "@/lib/auth";
import { useMyModules } from "@/modules/useMyModules";
import { useScheduleEvents } from "@/modules/schedule/api";
import { SCHEDULE_MODULE_ID } from "@/modules/schedule/id";
import { GOAL_MODULE_ID } from "@/modules/goal/id";
import { endOfDay, startOfDay } from "@/modules/schedule/calendar";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { TodayHeader } from "@/components/dashboard/TodayHeader";
import { AeteraBriefingCard } from "@/components/dashboard/AeteraBriefingCard";
import { TodayScheduleCard } from "@/components/dashboard/TodayScheduleCard";
import { WeeklyGoalsCard } from "@/components/dashboard/WeeklyGoalsCard";

/**
 * 오늘 중심 Life Dashboard.
 *
 * **여기 있는 것은 전부 실제 데이터다.** 예전에는 우선순위·라이프 영역·최근 기록 카드가
 * 지어낸 값을 보여 줬는데, 근거 없는 것을 확정된 분석처럼 내놓지 않기로 하고 걷어냈다.
 * 뒤를 받칠 모듈이 생기면 그때 되살린다.
 *
 * DOM 순서 = 모바일(1열) 표시 순서: 헤더 → 브리핑 → 일정 → 목표.
 * 데스크톱(lg)에서는 일정 7 + 목표 5 로 나눈다.
 */
export default function DashboardPage() {
  const { user } = useAuth();
  const { data: modules, isPending: modulesPending, isError: modulesFailed, refetch } = useMyModules();

  const scheduleEnabled = modules?.some((module) => module.id === SCHEDULE_MODULE_ID && module.enabled) ?? false;
  const goalEnabled = modules?.some((module) => module.id === GOAL_MODULE_ID && module.enabled) ?? false;

  // 하루 경계 시각이라 렌더마다 같은 ISO 문자열이 나온다. 쿼리 키가 문자열이므로 메모가 필요 없다.
  const now = new Date();
  const todayRange = { from: startOfDay(now), to: endOfDay(now) };

  const {
    data: todayEvents,
    isLoading: eventsLoading,
    isError: eventsFailed,
  } = useScheduleEvents(todayRange.from, todayRange.to, { enabled: scheduleEnabled });

  if (modulesPending) return <PageSpinner />;

  // 실패를 "모듈을 안 켰다"로 흘려보내면, 멀쩡히 쓰던 사람에게 모듈 스토어로 가라고 안내하게 된다.
  if (modulesFailed) return <ErrorState onRetry={() => void refetch()} />;

  // null = 일정 모듈 미사용 (카드들이 안내 상태로 전환된다)
  const events = scheduleEnabled ? (todayEvents ?? []) : null;

  return (
    <div className="grid grid-cols-1 gap-5 lg:grid-cols-12 lg:gap-6">
      <div className="lg:col-span-12">
        <TodayHeader
          nickname={user?.nickname}
          eventCount={events === null || eventsLoading || eventsFailed ? null : events.length}
        />
      </div>

      {/* 요약할 일정이 없으면 그리지 않는다 — "모듈을 켜세요" 는 아래 카드가 이미 말한다. */}
      {events !== null && (
        <div className="lg:col-span-12">
          <AeteraBriefingCard events={events} />
        </div>
      )}

      <div className="lg:col-span-7">
        <TodayScheduleCard events={events} isLoading={eventsLoading} isError={eventsFailed} />
      </div>

      <div className="lg:col-span-5">
        <WeeklyGoalsCard enabled={goalEnabled} />
      </div>
    </div>
  );
}
