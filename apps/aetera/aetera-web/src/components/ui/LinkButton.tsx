import Link from "next/link";
import type { ReactNode } from "react";
import { buttonClasses, type ButtonSize, type ButtonVariant } from "./Button";

/**
 * 버튼처럼 보이지만 실제로는 링크인 자리.
 *
 * [Button] 은 `<button>` 만 그린다. 화면을 옮기는 일에 `<button onClick={router.push}>` 을 쓰면
 * 새 탭으로 열기·주소 복사가 안 되고 스크린 리더도 버튼이라고 읽는다. 생김새만 빌려 온다.
 */
export function LinkButton({
  href,
  variant,
  size,
  className,
  children,
}: {
  href: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
  className?: string;
  children: ReactNode;
}) {
  return (
    <Link href={href} className={buttonClasses(variant, size, className)}>
      {children}
    </Link>
  );
}
