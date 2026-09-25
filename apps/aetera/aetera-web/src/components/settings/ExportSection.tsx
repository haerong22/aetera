"use client";

import { useState } from "react";
import { Download } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { apiFetchRaw } from "@/lib/api-client";

/**
 * 내 데이터 내보내기.
 *
 * `apiFetch` 가 아니라 `apiFetchRaw` 를 쓴다 — 저쪽은 JSON 을 파싱해 객체로 돌려주는데,
 * 여기서 필요한 것은 **파일 그 자체**다. 파싱했다가 다시 문자열로 만들면 서버가 정한
 * 파일 이름(헤더)도, 줄 모양도 잃는다. 인증과 재발급은 둘이 똑같이 한다.
 */
export function ExportSection() {
  const [busy, setBusy] = useState(false);
  const [failed, setFailed] = useState(false);

  async function download() {
    setBusy(true);
    setFailed(false);
    try {
      const response = await apiFetchRaw("/api/v1/me/export");
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = fileNameFrom(response) ?? "aetera.json";
      link.click();
      // 브라우저가 blob 을 붙들고 있지 않도록 놓아 준다.
      URL.revokeObjectURL(url);
    } catch {
      setFailed(true);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="flex flex-col gap-3">
      <div>
        <h2 className="text-[17px] font-bold text-grey-900">내 데이터</h2>
        <p className="mt-1 text-[14px] text-grey-500">
          적어 둔 것을 언제든 파일로 가져갈 수 있어요. 중지한 모듈의 기록도 함께 담겨요.
        </p>
      </div>

      <Card className="flex flex-wrap items-center justify-between gap-3">
        <div className="min-w-0">
          <p className="text-[15px] font-semibold text-grey-900">전체 내려받기</p>
          <p className="mt-0.5 text-[12.5px] text-grey-500">
            자산·소득·고정지출·목표·만기·일정·가이드가 JSON 한 파일로 나와요.
          </p>
        </div>
        <Button variant="secondary" disabled={busy} onClick={() => void download()}>
          <Download size={16} aria-hidden />
          {busy ? "준비 중" : "내려받기"}
        </Button>
      </Card>

      {failed && (
        <p role="alert" className="text-[13px] text-danger">
          내려받지 못했어요. 잠시 후 다시 시도해 주세요.
        </p>
      )}
    </section>
  );
}

/** 서버가 정해 준 파일 이름(`aetera-20260925.json`). 못 읽으면 `null`. */
function fileNameFrom(response: Response): string | null {
  const header = response.headers.get("Content-Disposition");
  return header?.match(/filename="([^"]+)"/)?.[1] ?? null;
}
