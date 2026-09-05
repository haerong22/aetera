import type { ReactNode } from "react";
import { Card } from "./Card";
import { cn } from "./cn";

/**
 * 화면의 핵심 숫자를 얹는 카드. 왼쪽에 큰 값, 오른쪽에 곁들이는 값.
 *
 * 색과 배치를 화면마다 적어 두면 셋이 미묘하게 어긋나는데, 나란히 놓고 보지 않으면
 * 아무도 눈치채지 못한다. 한 달 고정지출·순자산·버틸 개월 수가 같은 옷을 입는다.
 */
export function SummaryCard({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <Card
      className={cn(
        "flex flex-wrap items-baseline justify-between gap-x-6 gap-y-2",
        "border-primary/20 bg-primary-light/40",
        className,
      )}
    >
      {children}
    </Card>
  );
}
