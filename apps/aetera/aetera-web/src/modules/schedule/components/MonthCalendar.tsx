"use client";

import { cn } from "@/components/ui/cn";
import { endOfDay, monthGrid, startOfDay } from "../calendar";
import { DEFAULT_EVENT_COLOR } from "../colors";
import type { ScheduleEvent } from "../api";

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];
const MAX_CHIPS_PER_DAY = 3;

interface MonthCalendarProps {
  year: number;
  month: number;
  events: ScheduleEvent[];
  onSelectDate: (date: Date) => void;
  onSelectEvent: (event: ScheduleEvent) => void;
}

export function MonthCalendar({ year, month, events, onSelectDate, onSelectEvent }: MonthCalendarProps) {
  const cells = monthGrid(year, month);

  function eventsOn(date: Date): ScheduleEvent[] {
    const dayStart = startOfDay(date).getTime();
    const dayEnd = endOfDay(date).getTime();
    return events.filter((event) => {
      const startsAt = new Date(event.startsAt).getTime();
      const endsAt = new Date(event.endsAt).getTime();
      return startsAt <= dayEnd && endsAt >= dayStart;
    });
  }

  return (
    <div className="overflow-hidden rounded-(--radius-card) bg-white shadow-[0_1px_3px_rgba(25,31,40,0.06)]">
      <div className="grid grid-cols-7 border-b border-grey-100">
        {WEEKDAYS.map((weekday, index) => (
          <div
            key={weekday}
            className={cn(
              "py-2.5 text-center text-[12px] font-semibold",
              index === 0 ? "text-danger" : index === 6 ? "text-primary" : "text-grey-500",
            )}
          >
            {weekday}
          </div>
        ))}
      </div>
      <div className="grid grid-cols-7">
        {cells.map((cell) => {
          const dayEvents = eventsOn(cell.date);
          const overflow = dayEvents.length - MAX_CHIPS_PER_DAY;
          return (
            <button
              key={cell.date.toISOString()}
              onClick={() => onSelectDate(cell.date)}
              className={cn(
                "flex min-h-24 flex-col items-stretch gap-1 border-b border-r border-grey-100 p-1.5 text-left align-top",
                "transition-colors hover:bg-grey-50 [&:nth-child(7n)]:border-r-0",
                !cell.inCurrentMonth && "bg-grey-50/60",
              )}
            >
              <span
                className={cn(
                  "flex size-6 items-center justify-center rounded-full text-[12px] font-semibold",
                  cell.isToday
                    ? "bg-primary text-white"
                    : cell.inCurrentMonth
                      ? "text-grey-800"
                      : "text-grey-300",
                )}
              >
                {cell.date.getDate()}
              </span>
              {dayEvents.slice(0, MAX_CHIPS_PER_DAY).map((event) => (
                <span
                  key={event.id}
                  role="button"
                  tabIndex={0}
                  onClick={(clickEvent) => {
                    clickEvent.stopPropagation();
                    onSelectEvent(event);
                  }}
                  onKeyDown={(keyEvent) => {
                    if (keyEvent.key === "Enter") {
                      keyEvent.stopPropagation();
                      onSelectEvent(event);
                    }
                  }}
                  className="truncate rounded-md px-1.5 py-0.5 text-[11px] font-medium text-white"
                  style={{ backgroundColor: event.color ?? DEFAULT_EVENT_COLOR }}
                >
                  {event.title}
                </span>
              ))}
              {overflow > 0 && (
                <span className="px-1 text-[11px] font-medium text-grey-400">+{overflow}개 더보기</span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}
