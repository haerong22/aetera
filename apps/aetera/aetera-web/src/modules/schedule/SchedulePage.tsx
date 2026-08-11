"use client";

import { useState } from "react";
import Link from "next/link";
import { ChevronLeft, ChevronRight, Plus, Puzzle } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { PageSpinner } from "@/components/ui/Spinner";
import { ApiError } from "@/lib/api-client";
import { monthRange } from "./calendar";
import { MonthCalendar } from "./components/MonthCalendar";
import { EventDialog } from "./components/EventDialog";
import { useScheduleEvents, type ScheduleEvent } from "./api";

/** 백엔드 모듈 가드의 403(FORBIDDEN) — 아직 이 모듈을 활성화하지 않았다는 뜻. */
function isModuleDisabled(error: unknown): boolean {
  return error instanceof ApiError && error.status === 403;
}

export function SchedulePage() {
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth());
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<ScheduleEvent | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date | undefined>();

  const { from, to } = monthRange(year, month);
  const { data: events, isPending, error } = useScheduleEvents(from, to);

  function moveMonth(delta: number) {
    const moved = new Date(year, month + delta, 1);
    setYear(moved.getFullYear());
    setMonth(moved.getMonth());
  }

  if (isPending) return <PageSpinner />;

  if (error && isModuleDisabled(error)) {
    return (
      <Card className="mx-auto flex max-w-md flex-col items-center gap-4 py-14 text-center">
        <div className="flex size-14 items-center justify-center rounded-3xl bg-primary-light text-primary">
          <Puzzle size={26} />
        </div>
        <div>
          <p className="text-lg font-bold text-grey-900">일정 모듈을 아직 사용하고 있지 않아요</p>
          <p className="mt-1 text-[14px] text-grey-500">모듈 스토어에서 켜면 바로 쓸 수 있어요.</p>
        </div>
        <Link
          href="/settings/modules"
          className="rounded-(--radius-button) bg-primary px-5 py-3 text-[15px] font-semibold text-white transition-colors hover:bg-primary-hover"
        >
          모듈 스토어로 가기
        </Link>
      </Card>
    );
  }

  if (error) {
    return <p className="py-20 text-center text-grey-500">일정을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.</p>;
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1">
          <h1 className="text-2xl font-bold text-grey-900">
            {year}년 {month + 1}월
          </h1>
          <button
            aria-label="이전 달"
            onClick={() => moveMonth(-1)}
            className="ml-2 rounded-full p-1.5 text-grey-500 transition-colors hover:bg-grey-100"
          >
            <ChevronLeft size={20} />
          </button>
          <button
            aria-label="다음 달"
            onClick={() => moveMonth(1)}
            className="rounded-full p-1.5 text-grey-500 transition-colors hover:bg-grey-100"
          >
            <ChevronRight size={20} />
          </button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setYear(today.getFullYear());
              setMonth(today.getMonth());
            }}
          >
            오늘
          </Button>
        </div>
        <Button
          onClick={() => {
            setEditingEvent(null);
            setSelectedDate(undefined);
            setDialogOpen(true);
          }}
        >
          <Plus size={17} /> 새 일정
        </Button>
      </div>

      <MonthCalendar
        year={year}
        month={month}
        events={events}
        onSelectDate={(date) => {
          setEditingEvent(null);
          setSelectedDate(date);
          setDialogOpen(true);
        }}
        onSelectEvent={(event) => {
          setEditingEvent(event);
          setDialogOpen(true);
        }}
      />

      <EventDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        event={editingEvent}
        initialDate={selectedDate}
      />
    </div>
  );
}
