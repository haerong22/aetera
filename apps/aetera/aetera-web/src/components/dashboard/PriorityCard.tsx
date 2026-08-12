"use client";

import { useState } from "react";
import { ListTodo } from "lucide-react";
import { Badge } from "@/components/ui/Badge";
import { Card, CardHeader } from "@/components/ui/Card";
import { cn } from "@/components/ui/cn";
import { MOCK_PRIORITIES } from "./mock";

/**
 * 오늘의 우선순위. 아직 Todo 백엔드가 없으므로 체크 상태는 로컬 UI 상태로만 관리한다.
 */
export function PriorityCard({
  onCheckedCountChange,
}: {
  onCheckedCountChange?: (checked: number) => void;
}) {
  const [checked, setChecked] = useState<ReadonlySet<string>>(new Set());

  function toggle(id: string) {
    const next = new Set(checked);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    setChecked(next);
    onCheckedCountChange?.(next.size);
  }

  return (
    <Card className="h-full">
      <CardHeader icon={<ListTodo size={16} />} title="오늘의 우선순위" />

      <ul className="flex flex-col gap-0.5">
        {MOCK_PRIORITIES.map((priority, index) => {
          const done = checked.has(priority.id);
          return (
            <li key={priority.id}>
              <label
                className={cn(
                  "flex min-h-11 cursor-pointer items-center gap-3 rounded-xl px-2 py-2 transition-colors hover:bg-grey-50",
                )}
              >
                <input
                  type="checkbox"
                  checked={done}
                  onChange={() => toggle(priority.id)}
                  aria-label={`${priority.title} 완료 표시`}
                  className="size-[18px] shrink-0 accent-(--color-primary)"
                />
                <span className="w-4 shrink-0 text-[13px] font-semibold tabular-nums text-grey-400">
                  {index + 1}
                </span>
                <span
                  className={cn(
                    "min-w-0 flex-1 truncate text-[15px] font-medium",
                    done ? "text-grey-400 line-through" : "text-grey-800",
                  )}
                >
                  {priority.title}
                </span>
                {priority.aiSuggested && <Badge tone="blue">AI 추천</Badge>}
              </label>
            </li>
          );
        })}
      </ul>

      <p className="mt-3 px-2 text-[12px] text-grey-400">
        우선순위는 목표·AI 코치 기능과 연결될 예정이에요. 지금은 체크 상태가 저장되지 않아요.
      </p>
    </Card>
  );
}
