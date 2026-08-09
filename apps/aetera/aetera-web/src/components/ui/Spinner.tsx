import { cn } from "./cn";

export function Spinner({ className }: { className?: string }) {
  return (
    <span
      role="status"
      aria-label="불러오는 중"
      className={cn(
        "inline-block size-5 animate-spin rounded-full border-2 border-grey-200 border-t-primary",
        className,
      )}
    />
  );
}

export function PageSpinner() {
  return (
    <div className="flex h-[60vh] items-center justify-center">
      <Spinner className="size-7" />
    </div>
  );
}
