import type { HTMLAttributes } from "react";
import { cn } from "./cn";

export function Card({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        "rounded-(--radius-card) border border-grey-200 bg-white p-5 sm:p-6",
        "shadow-[0_2px_12px_rgba(15,23,42,0.04)]",
        className,
      )}
      {...props}
    />
  );
}

/** 카드 상단 공통 헤더: 아이콘 + 제목 + (오른쪽 액션). */
export function CardHeader({
  icon,
  title,
  action,
}: {
  icon?: React.ReactNode;
  title: React.ReactNode;
  action?: React.ReactNode;
}) {
  return (
    <div className="mb-4 flex items-center justify-between gap-2">
      <div className="flex min-w-0 items-center gap-2.5">
        {icon && (
          <span className="flex size-8 shrink-0 items-center justify-center rounded-xl bg-primary-light text-primary">
            {icon}
          </span>
        )}
        <h2 className="truncate text-[16px] font-bold text-grey-900 sm:text-[17px]">{title}</h2>
      </div>
      {action}
    </div>
  );
}
