"use client";

import { useId, useState, type ComponentType } from "react";
import {
  AlertCircle,
  CalendarPlus,
  Calculator,
  ChevronDown,
  ExternalLink,
  MessageSquarePlus,
  Trash2,
} from "lucide-react";
import { Button } from "@/components/ui/Button";
import { cn } from "@/components/ui/cn";
import type { GuideTask, TaskPatch } from "../api";

export function TaskItem({
  task,
  started,
  failed,
  onAddToCalendar,
  Tool,
  onChange,
}: {
  task: GuideTask;
  started: boolean;
  failed: boolean;
  onAddToCalendar?: () => void;
  /** 이 할 일에 딸린 도구. 엔진은 무엇인지 모르고 자리만 내준다. */
  Tool?: ComponentType;
  onChange: (patch: TaskPatch) => void;
}) {
  const checkboxId = useId();
  const toolPanelId = useId();
  const [editingNote, setEditingNote] = useState<string | null>(null);
  // 기본은 접어 둔다 — 계산기가 펼쳐져 있으면 목록을 훑는 흐름이 끊긴다.
  const [toolOpen, setToolOpen] = useState(false);

  function saveNote() {
    const note = editingNote?.trim() ?? "";
    onChange({ done: task.done, note: note.length > 0 ? note : null });
    setEditingNote(null);
  }

  return (
    <li className="flex gap-3 py-3.5">
      <input
        id={checkboxId}
        type="checkbox"
        checked={task.done}
        disabled={!started}
        onChange={(event) => onChange({ done: event.target.checked, note: task.note ?? null })}
        className="mt-0.5 size-[18px] shrink-0 cursor-pointer accent-primary disabled:cursor-not-allowed"
      />

      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-start justify-between gap-x-3 gap-y-1">
          <label
            htmlFor={checkboxId}
            className={cn(
              "cursor-pointer text-[15px] font-semibold",
              task.done ? "text-grey-400 line-through" : "text-grey-900",
            )}
          >
            {task.title}
          </label>

          {!task.required && (
            <span className="shrink-0 rounded-(--radius-chip) bg-grey-100 px-1.5 py-0.5 text-[11px] font-semibold text-grey-500">
              참고
            </span>
          )}
        </div>

        <p className={cn("mt-1 text-[13.5px] leading-relaxed", task.done ? "text-grey-400" : "text-grey-600")}>
          {task.description}
        </p>

        {failed && (
          <p
            role="alert"
            className="mt-2 flex items-center gap-1.5 rounded-xl bg-danger-light px-2.5 py-1.5 text-[13px] font-medium text-danger"
          >
            <AlertCircle size={14} aria-hidden className="shrink-0" />
            저장하지 못해 이전 상태로 되돌렸어요. 다시 눌러 주세요.
          </p>
        )}

        <div className="mt-2 flex flex-wrap items-center gap-3">
          {task.link && (
            <a
              href={task.link.url}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-[13px] font-semibold text-primary hover:underline"
            >
              {task.link.label}
              <ExternalLink size={13} aria-hidden />
              <span className="sr-only">(새 창)</span>
            </a>
          )}

          {started && editingNote === null && !task.note && (
            <button
              type="button"
              onClick={() => setEditingNote("")}
              className="inline-flex items-center gap-1 text-[13px] font-medium text-grey-500 transition-colors hover:text-grey-700"
            >
              <MessageSquarePlus size={14} aria-hidden />
              메모
            </button>
          )}

          {onAddToCalendar && (
            <button
              type="button"
              onClick={onAddToCalendar}
              className="inline-flex items-center gap-1 text-[13px] font-medium text-grey-500 transition-colors hover:text-grey-700"
            >
              <CalendarPlus size={14} aria-hidden />
              캘린더에 추가
            </button>
          )}

          {Tool && (
            <button
              type="button"
              aria-expanded={toolOpen}
              aria-controls={toolPanelId}
              onClick={() => setToolOpen((previous) => !previous)}
              className="inline-flex items-center gap-1 text-[13px] font-semibold text-primary transition-colors hover:text-primary-hover"
            >
              <Calculator size={14} aria-hidden />
              계산해 보기
              <ChevronDown
                size={13}
                aria-hidden
                className={cn("transition-transform duration-200", toolOpen && "rotate-180")}
              />
            </button>
          )}
        </div>

        {Tool && toolOpen && (
          <div id={toolPanelId} className="mt-3">
            <Tool />
          </div>
        )}

        {task.note && editingNote === null && (
          <div className="mt-2 flex items-start gap-2 rounded-xl bg-grey-50 px-3 py-2">
            <p className="min-w-0 flex-1 text-[13px] whitespace-pre-wrap text-grey-700">{task.note}</p>
            <button
              type="button"
              aria-label="메모 수정"
              onClick={() => setEditingNote(task.note ?? "")}
              className="shrink-0 text-[12px] font-semibold text-grey-500 hover:text-grey-700"
            >
              수정
            </button>
          </div>
        )}

        {editingNote !== null && (
          <div className="mt-2 flex flex-col gap-2">
            <textarea
              autoFocus
              value={editingNote}
              maxLength={500}
              onChange={(event) => setEditingNote(event.target.value)}
              placeholder="확인한 내용이나 담당자를 적어 두세요"
              className="min-h-[72px] w-full rounded-(--radius-input) border border-grey-200 bg-white p-3 text-[14px] text-grey-900 outline-none placeholder:text-grey-400 focus:border-primary"
            />
            <div className="flex items-center gap-2">
              <Button size="sm" onClick={saveNote}>
                저장
              </Button>
              <Button size="sm" variant="ghost" onClick={() => setEditingNote(null)}>
                취소
              </Button>
              {task.note && (
                <button
                  type="button"
                  onClick={() => {
                    onChange({ done: task.done, note: null });
                    setEditingNote(null);
                  }}
                  className="ml-auto inline-flex items-center gap-1 text-[13px] font-medium text-grey-400 hover:text-danger"
                >
                  <Trash2 size={14} aria-hidden />
                  메모 삭제
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </li>
  );
}
