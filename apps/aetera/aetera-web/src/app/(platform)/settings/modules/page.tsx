"use client";

import { Puzzle } from "lucide-react";
import { useMyModules, useToggleModule } from "@/modules/useMyModules";
import { moduleById } from "@/modules/registry";
import type { ModuleSummary } from "@/lib/types";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";

function ModuleCard({ module }: { module: ModuleSummary }) {
  const toggle = useToggleModule();
  const definition = moduleById.get(module.id);
  const Icon = definition?.icon ?? Puzzle;

  return (
    <Card className="flex items-start gap-4">
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
      <div className="flex shrink-0 flex-col items-end gap-1">
        <Button
          size="sm"
          variant={module.enabled ? "ghost" : "primary"}
          disabled={toggle.isPending}
          onClick={() => toggle.mutate({ moduleId: module.id, enable: !module.enabled })}
        >
          {module.enabled ? "사용 중지" : "사용하기"}
        </Button>
        {toggle.isError && <span className="text-[12px] text-danger">변경하지 못했어요</span>}
      </div>
    </Card>
  );
}

export default function ModuleStorePage() {
  const { data: modules, isPending, isError, refetch } = useMyModules();

  if (isPending) return <PageSpinner />;
  if (isError) return <ErrorState onRetry={() => void refetch()} />;

  return (
    <div className="flex max-w-2xl flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold text-grey-900">모듈 스토어</h1>
        <p className="mt-1 text-[15px] text-grey-500">
          인생의 영역별 도구를 필요한 만큼만 켜고 끄세요. 중지해도 데이터는 안전하게 남아요.
        </p>
      </div>
      <div className="flex flex-col gap-4">
        {modules?.map((module) => <ModuleCard key={module.id} module={module} />)}
      </div>
    </div>
  );
}
