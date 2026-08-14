"use client";

import { useState } from "react";
import { ChevronLeft, ChevronRight, Plus } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { PageSpinner } from "@/components/ui/Spinner";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { monthRange } from "./calendar";
import { MonthCalendar } from "./components/MonthCalendar";
import { EventDialog } from "./components/EventDialog";
import { useScheduleEvents, type ScheduleEvent } from "./api";

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

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title="일정" />;

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
