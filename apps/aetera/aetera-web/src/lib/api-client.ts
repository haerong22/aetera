/**
 * 백엔드 API 클라이언트.
 *
 * - 액세스 토큰은 메모리에만 둔다(XSS 로 훔칠 수 있는 localStorage 금지).
 * - 리프레시 토큰은 httpOnly 쿠키라 JS 가 만질 수 없다 — `credentials: "include"` 로만 실린다.
 * - 401 이 오면 조용히 한 번 재발급을 시도하고 원래 요청을 재시도한다.
 */

export const API_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

let accessToken: string | null = null;

export function setAccessToken(token: string | null) {
  accessToken = token;
}

/**
 * 재발급이 최종 실패해 세션이 끝났을 때 부를 콜백. AuthProvider 가 등록해서
 * 앱 상태를 guest 로 되돌린다 — 이게 없으면 세션이 죽어도 화면은 로그인 상태로 남는다.
 */
let sessionExpiredHandler: (() => void) | null = null;

export function setSessionExpiredHandler(handler: (() => void) | null) {
  sessionExpiredHandler = handler;
}

/**
 * 응답을 기다리는 최대 시간.
 *
 * 이게 없으면 서버가 응답을 영영 안 주는 상황(예외 처리 도중 죽어 커넥션만 열려 있는 경우)에
 * fetch 가 끝나지 않는다. 그러면 낙관적으로 반영해 둔 화면이 "저장됨"인 채로 굳고
 * 실패 안내도 뜨지 않아서, 사용자는 저장된 줄 알고 넘어간다. 끊어야 알릴 수 있다.
 */
const REQUEST_TIMEOUT_MS = 15_000;

/** 부르는 쪽이 준 취소 신호가 있으면 함께 묶는다 — 타임아웃이 그걸 덮어쓰면 안 된다. */
function timeoutSignal(callerSignal: AbortSignal | null | undefined): AbortSignal {
  const timeout = AbortSignal.timeout(REQUEST_TIMEOUT_MS);
  return callerSignal ? AbortSignal.any([callerSignal, timeout]) : timeout;
}

async function rawFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers);
  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }
  // 신호는 호출마다 새로 만든다. 재시도가 첫 시도에서 이미 흘러간 시간을 물려받으면 안 된다.
  return fetch(`${API_URL}${path}`, {
    ...init,
    headers,
    credentials: "include",
    signal: timeoutSignal(init.signal),
  });
}

async function parseError(response: Response): Promise<ApiError> {
  try {
    const body = await response.json();
    return new ApiError(response.status, body.code ?? 0, body.message ?? "요청에 실패했어요.");
  } catch {
    return new ApiError(response.status, 0, "요청에 실패했어요.");
  }
}

/** 세션 응답 형태 (refresh 전용으로 api-client 가 직접 아는 유일한 API). */
interface SessionBody {
  accessToken: string;
  accessTokenExpiresInSeconds: number;
  user: { id: string } & Record<string, unknown>;
}

/**
 * 재발급 결과. 실패를 한 덩어리로 뭉뚱그리지 않는다 —
 * "토큰이 죽었다(401)"와 "잠깐 통신이 안 됐다(오프라인·5xx·409)"는 대응이 달라야 한다.
 */
type RefreshOutcome =
  | { ok: true; session: SessionBody }
  | { ok: false; sessionEnded: boolean };

/**
 * 이 탭이 알고 있는 사용자. 재발급 응답의 주인이 이 값과 다르면 쿠키가 다른 사람 것으로
 * 바뀐 것이므로(공용 PC 에서 다른 탭이 재로그인한 경우) 이 탭의 세션을 끝내야 한다.
 */
let currentUserId: string | null = null;

export function setCurrentUserId(userId: string | null) {
  currentUserId = userId;
}

let refreshPromise: Promise<RefreshOutcome> | null = null;

/** 동시에 여러 요청이 401 을 맞아도 재발급은 한 번만 나간다. */
export async function tryRefreshSession(): Promise<RefreshOutcome> {
  if (!refreshPromise) {
    const pending = (async (): Promise<RefreshOutcome> => {
      let response: Response;
      try {
        response = await fetch(`${API_URL}/api/v1/auth/refresh`, {
          method: "POST",
          credentials: "include",
          signal: timeoutSignal(null),
        });
      } catch {
        // 네트워크 오류이거나 시간 초과. 토큰이 죽었다는 근거가 없으므로 세션을 끝내지 않는다.
        return { ok: false, sessionEnded: false };
      }
      if (!response.ok) {
        return { ok: false, sessionEnded: response.status === 401 || response.status === 403 };
      }
      const body = (await response.json()) as SessionBody;
      // 쿠키의 주인이 바뀌었다면 이 탭이 남의 데이터를 이어받게 된다. 세션을 끝내고 다시 로그인시킨다.
      if (currentUserId && body.user.id !== currentUserId) {
        return { ok: false, sessionEnded: true };
      }
      setAccessToken(body.accessToken);
      return { ok: true, session: body };
    })();
    refreshPromise = pending;
    // 정리는 저장한 참조를 기준으로 한다. 안에서 finally 로 지우면, 본문이 동기적으로
    // 끝나는 경우 대입보다 먼저 실행돼 "실패로 굳은 promise" 가 영구히 캐시된다.
    void pending.finally(() => {
      if (refreshPromise === pending) refreshPromise = null;
    });
  }
  return refreshPromise;
}

/** 재발급이 실패했다 = 세션이 끝났다. 죽은 토큰을 버리고 앱에 알린다. */
function endSession() {
  setAccessToken(null);
  sessionExpiredHandler?.();
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response = await rawFetch(path, init);

  if (response.status === 401 && !path.startsWith("/api/v1/auth/")) {
    const outcome = await tryRefreshSession();
    if (!outcome.ok) {
      // 통신 실패로 재발급을 못 한 것뿐이라면 세션을 끝내지 않는다 — 지하철에서 잠깐 끊겼다고
      // 로그아웃시키면 안 된다. 토큰이 실제로 죽었을 때만 로그인 화면으로 보낸다.
      if (outcome.sessionEnded) endSession();
      throw await parseError(response);
    }
    response = await rawFetch(path, init);
  }

  if (!response.ok) throw await parseError(response);
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}
