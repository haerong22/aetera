"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";
import { cn } from "./cn";

type Variant = "primary" | "secondary" | "ghost" | "danger";
type Size = "sm" | "md" | "lg";

const variantClasses: Record<Variant, string> = {
  primary:
    "bg-primary text-white hover:bg-primary-hover active:scale-[0.98] disabled:bg-grey-200 disabled:text-grey-400",
  secondary:
    "bg-primary-light text-primary hover:bg-[#e0e3fc] active:scale-[0.98] disabled:bg-grey-100 disabled:text-grey-400",
  ghost:
    "bg-transparent text-grey-700 hover:bg-grey-100 active:scale-[0.98] disabled:text-grey-300",
  danger:
    "bg-danger-light text-danger hover:bg-[#fcd9dd] active:scale-[0.98] disabled:bg-grey-100 disabled:text-grey-400",
};

const sizeClasses: Record<Size, string> = {
  sm: "h-9 px-3.5 text-[13px]",
  md: "h-11 px-5 text-[15px]",
  lg: "h-[52px] px-6 text-base w-full",
};

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  children: ReactNode;
}

export function Button({
  variant = "primary",
  size = "md",
  className,
  type = "button",
  children,
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        "inline-flex items-center justify-center gap-1.5 rounded-(--radius-button) font-semibold",
        "transition-all duration-150 select-none disabled:cursor-not-allowed",
        variantClasses[variant],
        sizeClasses[size],
        className,
      )}
      {...props}
    >
      {children}
    </button>
  );
}
