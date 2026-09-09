"use client";

import { useState } from "react";
import { ChevronLeft, ChevronRight, History } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/StatusCard";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { LinkButton } from "@/components/ui/LinkButton";
import { cn } from "@/components/ui/cn";
import { localToday, toLocalDateIso } from "@/lib/date";
import { moduleIcon } from "../registry";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { useTimeline, type TimelineEntry } from "./api";

/** `"2026-09-30"` → `"9월 30일"`. 연도는 왼쪽 제목이 이미 말한다. */
function formatDay(iso: string): string {
  const [, month, day] = iso.split("-").map(Number);
  return `${month}월 ${day}일`;
}

function EntryRow({ entry, today }: { entry: TimelineEntry; today: string }) {
  const Icon = moduleIcon(entry.moduleId);
  const future = entry.on > today;

  return (
    <li className="flex gap-3">
      <div className="flex flex-col items-center">
        {/* 아직 오지 않은 일은 속을 비운다 — 지나온 것과 앞으로 올 것을 한 줄에서 갈라 보여준다. */}
        <span
          aria-hidden
          className={
            future
              ? "mt-1.5 size-2.5 shrink-0 rounded-full border-2 border-primary/50 bg-white"
              : "mt-1.5 size-2.5 shrink-0 rounded-full bg-primary/60"
          }
        />
        <span aria-hidden className="w-px flex-1 bg-grey-200" />
      </div>

      <div className="flex min-w-0 flex-1 items-baseline gap-3 pb-5">
        <span className="w-20 shrink-0 text-[13px] font-medium text-grey-500 tabular-nums">
          {formatDay(entry.on)}
        </span>
        <span className="flex size-6 shrink-0 items-center justify-center rounded-lg bg-primary-light text-primary">
          <Icon size={13} />
        </span>
        <span className="min-w-0">
          <span className="block text-[14.5px] font-semibold text-grey-900">{entry.title}</span>
          {entry.detail && <span className="block text-[12.5px] text-grey-500">{entry.detail}</span>}
        </span>
      </div>
    </li>
  );
}

/**
 * 켠 모듈들이 남긴 이정표를 **한 해씩** 모아 본다. 무한 스크롤이 아니라 연 단위 창인 이유는
 * 서버 쪽(`TimelineController`)에 적어 뒀다.
 */
export function TimelinePage() {
  const today = localToday();
  const [year, setYear] = useState(today.getFullYear());

  const { data: entries, error, refetch, isPlaceholderData } = useTimeline(`${year}-01-01`, `${year}-12-31`);
  const todayIso = toLocalDateIso(today);

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title="타임라인" />;
  if (!entries) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div>
        <h1 className="text-2xl font-bold text-grey-900">타임라인</h1>
        <p className="mt-1 text-[15px] text-grey-500">
          켠 모듈들이 남긴 기록을 시간순으로 모아요. 쓸수록 촘촘해집니다.
        </p>
      </div>

      <div className="flex items-center justify-between">
        <Button size="sm" variant="ghost" onClick={() => setYear(year - 1)}>
          <ChevronLeft size={16} aria-hidden />
          {year - 1}년
        </Button>
        <span className="text-[17px] font-bold text-grey-900 tabular-nums">{year}년</span>
        <Button
          size="sm"
          variant="ghost"
          disabled={year >= today.getFullYear() + 1}
          onClick={() => setYear(year + 1)}
        >
          {year + 1}년
          <ChevronRight size={16} aria-hidden />
        </Button>
      </div>

      {/* 지난 해를 받아오는 동안에는 이전 해의 줄이 남아 있다 — 흐리게 해서 아직 도착 전임을 알린다. */}
      <div className={cn("transition-opacity", isPlaceholderData && "opacity-50")}>
        {entries.length === 0 ? (
          <EmptyState
            icon={<History size={22} aria-hidden />}
            title={`${year}년에는 아직 기록이 없어요`}
            description="자산을 기록하거나 가이드를 시작하면 여기에 쌓여요. 만기가 다가와도 한 줄이 생깁니다."
            action={<LinkButton href="/settings/modules">모듈 스토어로 가기</LinkButton>}
          />
        ) : (
          <Card>
            <ol className="flex flex-col">
              {entries.map((entry, index) => (
                <EntryRow key={`${entry.moduleId}-${entry.on}-${index}`} entry={entry} today={todayIso} />
              ))}
            </ol>
          </Card>
        )}
      </div>
    </div>
  );
}
