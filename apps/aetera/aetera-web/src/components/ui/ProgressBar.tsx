import { cn } from "./cn";

/**
 * 진행률 막대.
 *
 * 접근성 속성(role·aria-value*)을 손으로 적으면 한 곳에서 빠뜨려도 화면은 멀쩡해 보인다.
 * 그래서 값만 받고 나머지는 컴포넌트가 채운다.
 */
export function ProgressBar({
  percent,
  label,
  done = false,
  className,
}: {
  /** 0~100. 범위를 벗어난 값은 잘라 낸다. */
  percent: number;
  /** 스크린 리더가 읽을 이름. 막대만으로는 무엇의 진행인지 알 수 없다. */
  label: string;
  done?: boolean;
  className?: string;
}) {
  const clamped = Math.max(0, Math.min(100, Math.round(percent)));

  return (
    <div
      role="progressbar"
      aria-valuenow={clamped}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-label={`${label} 진행률 ${clamped}%`}
      className={cn("h-1.5 overflow-hidden rounded-full bg-grey-100", className)}
    >
      <div
        className={cn(
          "h-full rounded-full transition-[width] duration-300",
          done ? "bg-success" : "bg-primary",
        )}
        style={{ width: `${clamped}%` }}
      />
    </div>
  );
}
