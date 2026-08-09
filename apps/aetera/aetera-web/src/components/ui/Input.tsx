"use client";

import { forwardRef, useId, type InputHTMLAttributes } from "react";
import { cn } from "./cn";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, className, id: idProp, ...props },
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
      <input
        ref={ref}
        id={id}
        className={cn(
          "h-[52px] rounded-(--radius-input) border bg-white px-4 text-[15px] text-grey-900",
          "placeholder:text-grey-400 transition-colors duration-150 outline-none",
          "border-grey-200 hover:border-grey-300 focus:border-primary",
          className,
        )}
        {...props}
      />
    </div>
  );
});
