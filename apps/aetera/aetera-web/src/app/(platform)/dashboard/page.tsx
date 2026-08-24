"use client";

import { useState } from "react";
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
import { PriorityCard } from "@/components/dashboard/PriorityCard";
import { LifeOverviewCard } from "@/components/dashboard/LifeOverviewCard";
import { WeeklyGoalsCard } from "@/components/dashboard/WeeklyGoalsCard";
import { RecentTimelineCard } from "@/components/dashboard/RecentTimelineCard";
import { MOCK_PRIORITIES } from "@/components/dashboard/mock";

/**
 * 오늘 중심 Life Dashboard.
 *
 * DOM 순서 = 모바일(1열) 표시 순서: 헤더 → 브리핑 → 일정 → 우선순위 → 목표 → 라이프 → 기록.
 * 데스크톱(lg)에서는 12-column grid 로 재배치한다: 일정 6 + 우선순위 6 / 라이프 7 + 목표 5.
 */
export default function DashboardPage() {
  const { user } = useAuth();
  const { data: modules, isPending: modulesPending, isError: modulesFailed, refetch } = useMyModules();
  const [checkedPriorities, setCheckedPriorities] = useState(0);

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
      <div className="lg:order-1 lg:col-span-12">
        <TodayHeader
          nickname={user?.nickname}
          eventCount={events === null || eventsLoading || eventsFailed ? null : events.length}
          priorityCount={MOCK_PRIORITIES.length - checkedPriorities}
        />
      </div>

      <div className="lg:order-2 lg:col-span-12">
        <AeteraBriefingCard events={events} />
      </div>

      <div className="lg:order-3 lg:col-span-6">
        <TodayScheduleCard events={events} isLoading={eventsLoading} isError={eventsFailed} />
      </div>

      <div className="lg:order-4 lg:col-span-6">
        <PriorityCard onCheckedCountChange={setCheckedPriorities} />
      </div>

      {/* 모바일에선 목표가 라이프보다 먼저, 데스크톱에선 라이프(7) + 목표(5) 순서 */}
      <div className="lg:order-6 lg:col-span-5">
        <WeeklyGoalsCard enabled={goalEnabled} />
      </div>

      <div className="lg:order-5 lg:col-span-7">
        <LifeOverviewCard />
      </div>

      <div className="lg:order-7 lg:col-span-12">
        <RecentTimelineCard events={events} />
      </div>
    </div>
  );
}
