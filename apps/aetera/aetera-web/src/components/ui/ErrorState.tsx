"use client";

import { AlertCircle } from "lucide-react";
import { Button } from "./Button";
import { Card } from "./Card";

/**
 * 데이터를 못 불러왔을 때 쓰는 공통 상태.
 *
 * 오류를 빈 화면이나 "아직 설정하지 않았어요" 같은 안내로 바꿔 보여주면 안 된다 —
 * 사용자가 자기 잘못이라고 오해하고 엉뚱한 곳을 헤매게 된다.
 */
export function ErrorState({
  title = "잠시 문제가 생겼어요",
  description = "네트워크 상태를 확인하고 다시 시도해 주세요.",
  onRetry,
}: {
  title?: string;
  description?: string;
  onRetry?: () => void;
}) {
  return (
    <Card className="flex flex-col items-center gap-3 py-12 text-center">
      <span className="flex size-11 items-center justify-center rounded-2xl bg-danger-light text-danger">
        <AlertCircle size={20} />
      </span>
      <div>
        <p className="text-[15px] font-semibold text-grey-800">{title}</p>
        <p className="mt-1 text-[13px] text-grey-500">{description}</p>
      </div>
      {onRetry && (
        <Button size="sm" variant="secondary" onClick={onRetry}>
          다시 시도
        </Button>
      )}
    </Card>
  );
}
