"use client";

import { forwardRef, useId, type SelectHTMLAttributes } from "react";
import { cn } from "./cn";

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  /** 표시 순서를 코드가 정한 대로 유지하려고 객체가 아니라 배열로 받는다. */
  options: ReadonlyArray<{ value: string; label: string }>;
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
