"use client";

import { useState, type DragEvent } from "react";
import { ChevronDown, ChevronUp, GripVertical, Puzzle } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { LinkButton } from "@/components/ui/LinkButton";
import { EmptyState } from "@/components/ui/StatusCard";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { NotificationSection } from "@/components/settings/NotificationSection";
import { cn } from "@/components/ui/cn";
import { sortByIdOrder } from "@/lib/order";
import { moduleIcon } from "@/modules/registry";
import { useMyModules, useReorderModules } from "@/modules/useMyModules";
import type { ModuleSummary } from "@/lib/types";

function OrderRow({
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
  const Icon = moduleIcon(module.id);

  return (
    <li
      onDragOver={onDragOver}
      // 놓는 순간 브라우저가 기본 동작으로 넘어가지 않게만 막는다.
      // 순서를 확정하는 건 항상 dragend 다 — 허공에 놓아도 반드시 불린다.
      onDrop={(event) => event.preventDefault()}
      onDragEnd={onDragEnd}
      className={cn("flex items-center gap-3 py-3", dragging && "opacity-50")}
    >
      <div className="flex shrink-0 flex-col items-center gap-0.5">
        <button
          type="button"
          aria-label={`${module.displayName} 위로`}
          disabled={position === 0}
          onClick={() => onMove(-1)}
          className="flex size-6 items-center justify-center rounded-md text-grey-400 transition-colors hover:bg-grey-100 hover:text-grey-700 disabled:text-grey-200 disabled:hover:bg-transparent"
        >
          <ChevronUp size={16} aria-hidden />
        </button>
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

      <span
        draggable
        onDragStart={onDragStart}
        aria-hidden
        className="cursor-grab text-grey-300 active:cursor-grabbing"
      >
        <GripVertical size={16} />
      </span>

      <span className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-primary-light text-primary">
        <Icon size={18} />
      </span>

      <span className="min-w-0 flex-1 truncate text-[15px] font-semibold text-grey-900">
        {module.displayName}
      </span>

      <span className="shrink-0 text-[12.5px] text-grey-400">
        {module.category === "TOOL" ? "도구" : "가이드"}
      </span>
    </li>
  );
}

/**
 * 설정 — 지금은 왼쪽 메뉴 순서 하나뿐이다.
 *
 * 순서 변경을 모듈 스토어에서 떼어 왔다. 저쪽은 종류로 걸러 보는 화면이라 **일부만 보이는데**,
 * 보이는 것끼리 자리를 바꾸면 안 보이는 것들 때문에 전체 순서가 사용자가 본 것과 달라진다.
 * 여기서는 언제나 켠 모듈 전부를 늘어놓으므로 그 어긋남이 생길 수 없다.
 *
 * 끈 모듈은 보여주지 않는다. 이 화면의 목적이 "왼쪽 메뉴를 내 손으로 배치하는 것"이라
 * 메뉴에 없는 것까지 늘어놓으면 무엇을 옮기는지 헷갈린다.
 */
export default function SettingsPage() {
  const { data: modules, isPending, isError, refetch } = useMyModules();
  const reorder = useReorderModules();
  const [draggingId, setDraggingId] = useState<string | null>(null);
  /**
   * 끄는 동안의 임시 순서. 카드 위를 지날 때마다 저장하면 한 번 끌 때 요청이 모듈 수만큼 나가고,
   * 응답이 뒤바뀌어 도착하면 순서가 튄다. 놓을 때 한 번만 저장한다.
   */
  const [dragOrder, setDragOrder] = useState<string[] | null>(null);

  if (isPending) return <PageSpinner />;
  if (isError) return <ErrorState onRetry={() => void refetch()} />;

  const enabled = (modules ?? []).filter((module) => module.enabled);
  const ordered = dragOrder ? sortByIdOrder(enabled, dragOrder, (module) => module.id) : enabled;

  /** 켠 모듈만 보내고 끈 모듈은 건드리지 않는다 — 서버가 목록에 없는 모듈의 자리를 그대로 둔다. */
  function reorderTo(from: number, to: number): string[] | null {
    if (to < 0 || to >= ordered.length || from === to) return null;
    const ids = ordered.map((module) => module.id);
    const [moved] = ids.splice(from, 1);
    ids.splice(to, 0, moved);
    return ids;
  }

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
   * 끌고 있는 줄이 대상의 **세로 중앙선을 넘었을 때만** 자리를 바꾼다.
   * 닿자마자 바꾸면 두 줄이 자리를 맞바꾸고, 커서가 여전히 대상 위에 남으면 곧바로 되바꾼다.
   */
  function dragOverRow(index: number, event: DragEvent<HTMLElement>) {
    event.preventDefault();
    if (!draggingId) return;

    const from = ordered.findIndex((item) => item.id === draggingId);
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
    const before = enabled.map((item) => item.id).join();
    if (dragOrder && dragOrder.join() !== before) reorder.mutate(dragOrder);
    setDraggingId(null);
    setDragOrder(null);
  }

  return (
    <div className="flex max-w-2xl flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-grey-900">설정</h1>
      </div>

      <NotificationSection />

      <section className="flex flex-col gap-3">
        <div>
          <h2 className="text-[17px] font-bold text-grey-900">메뉴 순서</h2>
          <p className="mt-1 text-[14px] text-grey-500">
            켠 모듈이 왼쪽 메뉴에 보일 순서예요. 화살표나 손잡이로 바꾸면 바로 적용돼요.
          </p>
        </div>

        {reorder.isError && (
          <p role="alert" className="text-[13px] text-danger">
            순서를 저장하지 못해 되돌렸어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}

        {ordered.length === 0 ? (
          <EmptyState
            icon={<Puzzle size={22} aria-hidden />}
            title="아직 켠 모듈이 없어요"
            description="모듈 스토어에서 필요한 것을 켜면 여기서 순서를 정할 수 있어요."
            action={
<LinkButton href="/settings/modules">모듈 스토어로 가기</LinkButton>
            }
          />
        ) : (
          <Card className="p-0 sm:p-0">
            <ul className="divide-y divide-grey-100 px-5 sm:px-6">
              {ordered.map((module, index) => (
                <OrderRow
                  key={module.id}
                  module={module}
                  position={index}
                  total={ordered.length}
                  onMove={(delta) => moveBy(index, delta)}
                  dragging={draggingId === module.id}
                  onDragStart={(event) => startDrag(module.id, event)}
                  onDragOver={(event) => dragOverRow(index, event)}
                  onDragEnd={endDrag}
                />
              ))}
            </ul>
          </Card>
        )}
      </section>
    </div>
  );
}
