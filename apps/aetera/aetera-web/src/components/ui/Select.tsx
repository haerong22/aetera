"use client";

import { forwardRef, useId, type SelectHTMLAttributes } from "react";
import { cn } from "./cn";

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  /** 표시 순서를 코드가 정한 대로 유지하려고 객체가 아니라 배열로 받는다. */
  options: ReadonlyArray<{ value: string; label: string }>;
}

/**
 * `{ MONTHLY: "매월", YEARLY: "매년" }` 같은 라벨 표를 [Select] 가 받는 배열로 바꾼다.
 *
 * 모듈마다 이 한 줄을 다시 적고 있었다. 값-라벨 표는 모듈이 갖되, 그걸 배열로 펴는 방식은
 * Select 를 쓰는 모두가 같아야 순서 규칙이 한곳에 남는다.
 */
export function optionsFrom(labels: Record<string, string>): ReadonlyArray<{ value: string; label: string }> {
  return Object.entries(labels).map(([value, label]) => ({ value, label }));
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { label, options, className, id: idProp, ...props },
  ref,
) {
  const generatedId = useId();
  const id = idProp ?? generatedId;

  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label htmlFor={id} className="text-[13px] font-medium text-grey-600">
          {label}
        </label>
      )}
      <select
        ref={ref}
        id={id}
        className={cn(
          "h-[52px] rounded-(--radius-input) border bg-white px-3.5 text-[15px] text-grey-900",
          "transition-colors duration-150 outline-none",
          "border-grey-200 hover:border-grey-300 focus:border-primary",
          className,
        )}
        {...props}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
});
