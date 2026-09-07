"use client";

import { useState } from "react";
import Link from "next/link";
import { Puzzle, SearchX } from "lucide-react";
import { useMyModules, useToggleModule } from "@/modules/useMyModules";
import { moduleById } from "@/modules/registry";
import type { ModuleCategory, ModuleSummary } from "@/lib/types";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/StatusCard";
import { Switch } from "@/components/ui/Switch";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { cn } from "@/components/ui/cn";

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

function ModuleCard({ module }: { module: ModuleSummary }) {
  const toggle = useToggleModule();
  const definition = moduleById.get(module.id);
  const Icon = definition?.icon ?? Puzzle;

  return (
    <Card className="flex items-start gap-3">
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
  const [filter, setFilter] = useState<Filter>("ALL");

  if (isPending) return <PageSpinner />;
  if (isError) return <ErrorState onRetry={() => void refetch()} />;

  /**
   * 이름순으로 늘어놓는다.
   *
   * 서버가 주는 순서는 **사이드바 메뉴 순서**인데, 그건 설정에서 정하고 여기서는 못 바꾼다.
   * 바꿀 수 없는 순서로 늘어놓으면 사용자에게는 아무 뜻이 없다. 게다가 끈 모듈은 그 순서에서
   * 제자리에 남아 켠 것 사이에 끼어 보인다.
   *
   * 여기는 찾아보는 화면이라 **켜고 끌 때 목록이 움직이지 않는 것**이 가장 중요하다.
   */
  const all = [...(modules ?? [])].sort((a, b) => a.displayName.localeCompare(b.displayName, "ko-KR"));

  const shown = (module: ModuleSummary) => filter === "ALL" || module.category === filter;
  const visible = all.filter(shown);

  const counts: Record<Filter, number> = {
    ALL: all.length,
    TOOL: all.filter((module) => module.category === "TOOL").length,
    GUIDE: all.filter((module) => module.category === "GUIDE").length,
  };

  return (
    <div className="flex max-w-2xl flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-grey-900">모듈 스토어</h1>
        <p className="mt-1 text-[15px] text-grey-500">
          인생의 영역별 도구를 필요한 만큼만 켜고 끄세요. 중지해도 데이터는 안전하게 남아요.
        </p>
        <p className="mt-1 text-[14px] text-grey-500">
          왼쪽 메뉴에 보일 순서는{" "}
          <Link href="/settings" className="font-semibold text-primary hover:underline">
            설정
          </Link>
          에서 정할 수 있어요.
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

      {visible.length === 0 ? (
        <EmptyState
          icon={<SearchX size={22} aria-hidden />}
          title="이 종류의 모듈이 아직 없어요"
          description="다른 종류를 골라 보세요. 새 모듈이 배포되면 여기에 바로 나타납니다."
        />
      ) : (
        <div className="flex flex-col gap-4">
          {visible.map((module) => (
            <ModuleCard key={module.id} module={module} />
          ))}
        </div>
      )}
    </div>
  );
}
