import type { ReactNode } from "react";
import { cn } from "./cn";

type Tone = "blue" | "orange" | "grey";

const toneClasses: Record<Tone, string> = {
  blue: "bg-primary-light text-primary",
  orange: "bg-accent-light text-accent",
  grey: "bg-grey-100 text-grey-600",
};

export function Badge({ tone = "grey", children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-(--radius-chip) px-2 py-0.5 text-xs font-semibold",
        toneClasses[tone],
      )}
    >
      {children}
    </span>
  );
}
