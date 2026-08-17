"use client";

import { useEffect, useState, type FormEvent } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Dialog } from "@/components/ui/Dialog";
import { Input } from "@/components/ui/Input";
import { cn } from "@/components/ui/cn";
import { ApiError } from "@/lib/api-client";
import { toDateTimeLocal } from "../calendar";
import { DEFAULT_EVENT_COLOR, EVENT_COLORS } from "../colors";
import {
  useCreateEvent,
  useDeleteEvent,
  useUpdateEvent,
  type ScheduleEvent,
} from "../api";

interface EventDialogProps {
  open: boolean;
  onClose: () => void;
  /** 수정 대상. 없으면 생성 모드. */
  event?: ScheduleEvent | null;
  /** 생성 모드에서 미리 선택된 날짜. */
  initialDate?: Date;
  initial?: { title?: string; description?: string; allDay?: boolean };
}

export function EventDialog({ open, onClose, event, initialDate, initial }: EventDialogProps) {
  const create = useCreateEvent();
  const update = useUpdateEvent();
  const remove = useDeleteEvent();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [startsAt, setStartsAt] = useState("");
  const [endsAt, setEndsAt] = useState("");
  const [allDay, setAllDay] = useState(false);
  const [color, setColor] = useState(DEFAULT_EVENT_COLOR);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    setError(null);
    if (event) {
      setTitle(event.title);
      setDescription(event.description ?? "");
      setStartsAt(toDateTimeLocal(new Date(event.startsAt)));
      setEndsAt(toDateTimeLocal(new Date(event.endsAt)));
      setAllDay(event.allDay);
      setColor(event.color ?? DEFAULT_EVENT_COLOR);
    } else {
      const base = initialDate ?? new Date();
      const start = new Date(base);
      start.setHours(9, 0, 0, 0);
      const end = new Date(base);
      end.setHours(10, 0, 0, 0);
      setTitle(initial?.title ?? "");
      setDescription(initial?.description ?? "");
      setStartsAt(toDateTimeLocal(start));
      setEndsAt(toDateTimeLocal(end));
      setAllDay(initial?.allDay ?? false);
      setColor(DEFAULT_EVENT_COLOR);
    }
  }, [open, event, initialDate]);

  const busy = create.isPending || update.isPending || remove.isPending;

  async function onSubmit(formEvent: FormEvent) {
    formEvent.preventDefault();
    setError(null);

    // datetime-local 이 지원되지 않는 환경에서는 임의 문자열이 들어올 수 있다.
    // 변환을 try 밖에 두면 RangeError 가 그대로 새어 나가 화면이 멈춘 것처럼 보인다.
    const start = new Date(startsAt);
    const end = new Date(endsAt);
    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
      setError("날짜 형식이 올바르지 않아요.");
      return;
    }
    if (end < start) {
      setError("종료 시각이 시작 시각보다 빠를 수 없어요.");
      return;
    }

    const input = {
      title,
      description: description || undefined,
      startsAt: start.toISOString(),
      endsAt: end.toISOString(),
      allDay,
      color,
    };
    try {
      if (event) await update.mutateAsync({ id: event.id, input });
      else await create.mutateAsync(input);
      onClose();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "저장에 실패했어요.");
    }
  }

  async function onDelete() {
    if (!event) return;
    try {
      await remove.mutateAsync(event.id);
      onClose();
    } catch {
      setError("삭제에 실패했어요.");
    }
  }

  return (
    <Dialog open={open} onClose={onClose} title={event ? "일정 수정" : "새 일정"}>
      <form onSubmit={onSubmit} className="flex flex-col gap-4">
        <Input
          label="제목"
          placeholder="무슨 일정인가요?"
          value={title}
          onChange={(changeEvent) => setTitle(changeEvent.target.value)}
          maxLength={200}
          required
          autoFocus
        />
        <Input
          label="메모"
          placeholder="(선택)"
          value={description}
          onChange={(changeEvent) => setDescription(changeEvent.target.value)}
          maxLength={2000}
        />
        <div className="grid grid-cols-2 gap-3">
          <Input
            label="시작"
            type="datetime-local"
            value={startsAt}
            onChange={(changeEvent) => setStartsAt(changeEvent.target.value)}
            required
          />
          <Input
            label="종료"
            type="datetime-local"
            value={endsAt}
            onChange={(changeEvent) => setEndsAt(changeEvent.target.value)}
            required
          />
        </div>

        <div className="flex items-center justify-between">
          <label className="flex cursor-pointer items-center gap-2 text-[14px] font-medium text-grey-700">
            <input
              type="checkbox"
              checked={allDay}
              onChange={(changeEvent) => setAllDay(changeEvent.target.checked)}
              className="size-4 accent-(--color-primary)"
            />
            하루 종일
          </label>
          <div className="flex items-center gap-2">
            {EVENT_COLORS.map((candidate) => (
              <button
                key={candidate}
                type="button"
                aria-label={`색상 ${candidate}`}
                onClick={() => setColor(candidate)}
                className={cn(
                  "size-6 rounded-full transition-transform",
                  color === candidate && "scale-110 ring-2 ring-grey-900/20 ring-offset-2",
                )}
                style={{ backgroundColor: candidate }}
              />
            ))}
          </div>
        </div>

        {error && (
          <p className="rounded-xl bg-danger-light px-4 py-3 text-[13px] font-medium text-danger">{error}</p>
        )}

        <div className="mt-2 flex gap-2">
          {event && (
            <Button variant="danger" disabled={busy} onClick={onDelete} className="shrink-0">
              <Trash2 size={16} /> 삭제
            </Button>
          )}
          <Button type="submit" disabled={busy} className="flex-1">
            {event ? "저장" : "추가"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
