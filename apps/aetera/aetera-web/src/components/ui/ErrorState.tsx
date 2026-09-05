"use client";

import { AlertCircle } from "lucide-react";
import { Button } from "./Button";
import { StatusCard } from "./StatusCard";

/**
 * 데이터를 못 불러왔을 때 쓰는 공통 상태.
 *
 * 오류를 빈 화면이나 "아직 설정하지 않았어요" 같은 안내로 바꿔 보여주면 안 된다 —
 * 사용자가 자기 잘못이라고 오해하고 엉뚱한 곳을 헤매게 된다. 그래서 색이 다르다.
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
    <StatusCard
      tone="danger"
      icon={<AlertCircle size={22} aria-hidden />}
      title={title}
      description={description}
      action={
        onRetry && (
          <Button size="sm" variant="secondary" onClick={onRetry}>
            다시 시도
          </Button>
        )
      }
    />
  );
}
