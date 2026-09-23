/**
 * 시험에서 서버 흉내를 낼 때 쓰는 응답 만들기.
 *
 * **타입을 받는다.** 손으로 `JSON.stringify({ ... })` 를 쓰면 없는 필드를 넣거나 있는 필드를
 * 빠뜨려도 아무도 말해 주지 않는다 — `stringify` 는 무엇이든 받기 때문이다. 그러면 가짜 서버가
 * 진짜 서버와 다른 모양을 내놓고, 그 위에서 통과한 시험은 아무것도 지키지 못한다.
 */
export function jsonResponse<T>(body: T, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

/** 서버가 거절했을 때의 모양. 코드와 메시지가 오는 것은 `ApiError` 가 읽는 규약이다. */
export function errorResponse(status: number, code: number, message = "시험용 오류"): Response {
  return jsonResponse({ code, message }, status);
}
