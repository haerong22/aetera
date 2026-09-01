"use client";

import { useState, type DragEvent } from "react";
import { ChevronDown, ChevronUp, GripVertical, Puzzle } from "lucide-react";
import { useMyModules, useReorderModules, useToggleModule } from "@/modules/useMyModules";
import { moduleById } from "@/modules/registry";
import type { ModuleCategory, ModuleSummary } from "@/lib/types";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { Switch } from "@/components/ui/Switch";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { cn } from "@/components/ui/cn";
import { sortByIdOrder } from "@/lib/order";

type Filter = ModuleCategory | "ALL";

const FILTERS: { key: Filter; label: string }[] = [
  { key: "ALL", label: "전체" },
  { key: "TOOL", label: "도구" },
  { key: "GUIDE", label: "가이드" },
];

/**
 * 종류로 걸러 보는 버튼 묶음.
 *
 * `role="tab"` 을 쓰지 않는다. 탭이라고 선언하면 스크린리더와 키보드 사용자가 탭 위젯의 규약을
 * 기대한다 — 좌우 화살표로 이동하고, Tab 키로는 묶음 전체를 한 번에 지나가고, 고른 탭에 대응하는
 * `tabpanel` 이 있어야 한다. 여기서 하는 일은 목록을 거르는 것뿐이라 그 규약을 지킬 이유가 없고,
 * 지키지 않으면서 이름만 빌리면 안 지켜지는 약속이 된다.
 *
 * 눌린 상태는 [aria-pressed] 로 말한다. 지금 동작(각각 Tab 으로 닿고, 눌러서 고름)과 정확히 맞는다.
 */
function CategoryFilter({
  filter,
  counts,
  onChange,
}: {
  filter: Filter;
  counts: Record<Filter, number>;
  onChange: (filter: Filter) => void;
}) {
  return (
    <div role="group" aria-label="모듈 종류" className="flex gap-1 rounded-(--radius-chip) bg-grey-100 p-1">
      {FILTERS.map((option) => {
        const selected = option.key === filter;
        return (
          <button
            key={option.key}
            type="button"
            aria-pressed={selected}
            onClick={() => onChange(option.key)}
            className={cn(
              "flex-1 rounded-(--radius-chip) px-3 py-1.5 text-[13.5px] font-semibold transition-colors",
              selected ? "bg-white text-grey-900 shadow-sm" : "text-grey-500 hover:text-grey-700",
            )}
          >
            {option.label}
            <span className="ml-1.5 text-[12px] font-medium text-grey-400 tabular-nums">
              {counts[option.key]}
            </span>
          </button>
        );
      })}
    </div>
  );
}

function ModuleCard({
  module,
  position,
  total,
  onMove,
  dragging,
  onDragStart,
  onDragOver,
  onDragEnd,
}: {
  module: ModuleSummary;
  position: number;
  total: number;
  onMove: (delta: number) => void;
  dragging: boolean;
  onDragStart: (event: DragEvent<HTMLElement>) => void;
  onDragOver: (event: DragEvent<HTMLElement>) => void;
  onDragEnd: () => void;
}) {
  const toggle = useToggleModule();
  const definition = moduleById.get(module.id);
  const Icon = definition?.icon ?? Puzzle;

  return (
    <Card
      onDragOver={onDragOver}
      // 놓는 순간 브라우저가 기본 동작(주소 열기 등)으로 넘어가지 않게만 막는다.
      // 순서를 확정하는 건 항상 dragend 다 — 허공에 놓아도 반드시 불린다.
      onDrop={(event) => event.preventDefault()}
      onDragEnd={onDragEnd}
      className={cn("flex items-start gap-3", dragging && "opacity-50")}
    >
      <div className="flex shrink-0 flex-col items-center gap-0.5 pt-0.5">
        <button
          type="button"
          aria-label={`${module.displayName} 위로`}
          disabled={position === 0}
          onClick={() => onMove(-1)}
          className="flex size-6 items-center justify-center rounded-md text-grey-400 transition-colors hover:bg-grey-100 hover:text-grey-700 disabled:text-grey-200 disabled:hover:bg-transparent"
        >
          <ChevronUp size={16} aria-hidden />
        </button>
        <span
          draggable
          onDragStart={onDragStart}
          aria-hidden
          className="cursor-grab text-grey-300 active:cursor-grabbing"
        >
          <GripVertical size={14} />
        </span>
        <button
          type="button"
          aria-label={`${module.displayName} 아래로`}
          disabled={position === total - 1}
          onClick={() => onMove(1)}
          className="flex size-6 items-center justify-center rounded-md text-grey-400 transition-colors hover:bg-grey-100 hover:text-grey-700 disabled:text-grey-200 disabled:hover:bg-transparent"
        >
          <ChevronDown size={16} aria-hidden />
        </button>
      </div>

      <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-primary-light text-primary">
        <Icon size={22} />
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <h2 className="text-[17px] font-bold text-grey-900">{module.displayName}</h2>
          <Badge tone={module.category === "TOOL" ? "blue" : "orange"}>
            {module.category === "TOOL" ? "도구" : "가이드"}
          </Badge>
        </div>
        <p className="mt-1 text-[14px] leading-relaxed text-grey-500">{module.description}</p>
        {module.enabled && module.enabledAt && (
          <p className="mt-2 text-[12px] text-grey-400">
            {new Date(module.enabledAt).toLocaleDateString("ko-KR")}부터 사용 중
          </p>
        )}
      </div>

      <div className="flex shrink-0 flex-col items-end gap-1.5">
        <Switch
          checked={module.enabled}
          disabled={toggle.isPending}
          label={`${module.displayName} 사용`}
          onChange={(enable) => toggle.mutate({ moduleId: module.id, enable })}
        />
        {toggle.isError && (
          <span role="alert" className="text-[12px] text-danger">
            변경하지 못했어요
          </span>
        )}
      </div>
    </Card>
  );
}

export default function ModuleStorePage() {
  const { data: modules, isPending, isError, refetch } = useMyModules();
  const reorder = useReorderModules();
  const [draggingId, setDraggingId] = useState<string | null>(null);
  /**
   * 끄는 동안의 임시 순서. 카드 위를 지날 때마다 저장하면 한 번 끌 때 요청이 모듈 수만큼 나가고,
   * 응답이 뒤바뀌어 도착하면 순서가 튄다. 놓을 때 한 번만 저장한다.
   */
  const [dragOrder, setDragOrder] = useState<string[] | null>(null);
  const [filter, setFilter] = useState<Filter>("ALL");

  if (isPending) return <PageSpinner />;
  if (isError) return <ErrorState onRetry={() => void refetch()} />;

  const ordered = dragOrder
    ? sortByIdOrder(modules ?? [], dragOrder, (module) => module.id)
    : (modules ?? []);

  const shown = (module: ModuleSummary) => filter === "ALL" || module.category === filter;
  const visible = ordered.filter(shown);

  const counts: Record<Filter, number> = {
    ALL: ordered.length,
    TOOL: ordered.filter((module) => module.category === "TOOL").length,
    GUIDE: ordered.filter((module) => module.category === "GUIDE").length,
  };

  /**
   * 보이는 것끼리만 자리를 바꾸고, 전체 순서로 되돌려 놓는다.
   *
   * 가이드 탭에서 두 번째 가이드를 올리면 그 사이에 낀 도구는 제자리에 있어야 한다.
   * 그래서 보이는 항목이 원래 차지하던 자리(slots)에 순서만 바꿔 다시 끼운다.
   * 전체 탭이면 slots 가 곧 전체라 같은 코드가 그대로 동작한다.
   */
  function reorderTo(from: number, to: number): string[] | null {
    if (to < 0 || to >= visible.length || from === to) return null;

    const movedIds = visible.map((module) => module.id);
    const [moved] = movedIds.splice(from, 1);
    movedIds.splice(to, 0, moved);

    const slots = ordered.flatMap((module, index) => (shown(module) ? [index] : []));
    const ids = ordered.map((module) => module.id);
    slots.forEach((slot, position) => {
      ids[slot] = movedIds[position];
    });
    return ids;
  }

  /** 화살표: 한 칸 옮기고 바로 저장한다. */
  function moveBy(index: number, delta: number) {
    const ids = reorderTo(index, index + delta);
    if (ids) reorder.mutate(ids);
  }

  function startDrag(moduleId: string, event: DragEvent<HTMLElement>) {
    // 데이터가 비어 있으면 Firefox 는 드래그를 시작하지 않는다.
    event.dataTransfer.setData("text/plain", moduleId);
    event.dataTransfer.effectAllowed = "move";
    setDraggingId(moduleId);
    setDragOrder(ordered.map((item) => item.id));
  }

  /**
   * 끌고 있는 카드가 대상의 **세로 중앙선을 넘었을 때만** 자리를 바꾼다.
   *
   * 닿자마자 바꾸면 두 카드가 자리를 맞바꾸고, 높이가 서로 달라 커서가 여전히 대상 위에 남으면
   * 곧바로 되바꾼다 — 목록이 떨린다. 중앙선을 기준으로 두면 한 번 지나간 자리로 돌아오지 않는다.
   */
  function dragOverCard(index: number, event: DragEvent<HTMLElement>) {
    event.preventDefault();
    if (!draggingId) return;

    const from = visible.findIndex((item) => item.id === draggingId);
    if (from === -1 || from === index) return;

    const box = event.currentTarget.getBoundingClientRect();
    const middle = box.top + box.height / 2;
    if (from < index && event.clientY < middle) return;
    if (from > index && event.clientY > middle) return;

    const ids = reorderTo(from, index);
    if (ids) setDragOrder(ids);
  }

  /** 놓았을 때 한 번만, 그것도 실제로 자리가 바뀌었을 때만 저장한다. */
  function endDrag() {
    const before = (modules ?? []).map((item) => item.id).join();
    if (dragOrder && dragOrder.join() !== before) reorder.mutate(dragOrder);
    setDraggingId(null);
    setDragOrder(null);
  }

  return (
    <div className="flex max-w-2xl flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-grey-900">모듈 스토어</h1>
        <p className="mt-1 text-[15px] text-grey-500">
          인생의 영역별 도구를 필요한 만큼만 켜고 끄세요. 중지해도 데이터는 안전하게 남아요.
        </p>
        <p className="mt-1 text-[14px] text-grey-500">
          화살표나 손잡이로 순서를 바꾸면 왼쪽 메뉴에도 그대로 적용돼요.
        </p>
      </div>

      <CategoryFilter filter={filter} counts={counts} onChange={setFilter} />

      {/*
        거르기가 먹혔다는 걸 눈으로 보지 못하는 사람에게 알린다. 목록 자체를 live 로 두면
        카드 여덟 장을 통째로 다시 읽어 준다 — 바뀐 사실만 한 줄로 말한다.
      */}
      <p role="status" className="sr-only">
        {FILTERS.find((option) => option.key === filter)?.label} 모듈 {visible.length}개
      </p>

      {reorder.isError && (
        <p role="alert" className="text-[13px] text-danger">
          순서를 저장하지 못해 되돌렸어요. 잠시 후 다시 시도해 주세요.
        </p>
      )}

      <div className="flex flex-col gap-4">
        {visible.map((module, index) => (
          <ModuleCard
            key={module.id}
            module={module}
            position={index}
            total={visible.length}
            onMove={(delta) => moveBy(index, delta)}
            dragging={draggingId === module.id}
            onDragStart={(event) => startDrag(module.id, event)}
            onDragOver={(event) => dragOverCard(index, event)}
            onDragEnd={endDrag}
          />
        ))}
      </div>
    </div>
  );
}
