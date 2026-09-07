"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";
import { cn } from "./cn";

export type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";
export type ButtonSize = "sm" | "md" | "lg";

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    "bg-primary text-white hover:bg-primary-hover active:scale-[0.98] disabled:bg-grey-200 disabled:text-grey-400",
  secondary:
    "bg-primary-light text-primary hover:bg-[#e0e3fc] active:scale-[0.98] disabled:bg-grey-100 disabled:text-grey-400",
  ghost:
    "bg-transparent text-grey-700 hover:bg-grey-100 active:scale-[0.98] disabled:text-grey-300",
  danger:
    "bg-danger-light text-danger hover:bg-[#fcd9dd] active:scale-[0.98] disabled:bg-grey-100 disabled:text-grey-400",
};

const sizeClasses: Record<ButtonSize, string> = {
  sm: "h-9 px-3.5 text-[13px]",
  md: "h-11 px-5 text-[15px]",
  lg: "h-[52px] px-6 text-base w-full",
};

/**
 * 버튼의 생김새만 문자열로 뽑아 둔다.
 *
 * 링크를 버튼처럼 보이게 해야 하는 자리가 있는데([LinkButton]) `<button>` 을 쓸 수 없다.
 * 그때 클래스를 손으로 베끼면 여기 색이 바뀔 때 그쪽만 남는다.
 */
export function buttonClasses(
  variant: ButtonVariant = "primary",
  size: ButtonSize = "md",
  className?: string,
): string {
  return cn(
    "inline-flex items-center justify-center gap-1.5 rounded-(--radius-button) font-semibold",
    "transition-all duration-150 select-none disabled:cursor-not-allowed",
    variantClasses[variant],
    sizeClasses[size],
    className,
  );
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
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
      className={buttonClasses(variant, size, className)}
      {...props}
    >
      {children}
    </button>
  );
}
