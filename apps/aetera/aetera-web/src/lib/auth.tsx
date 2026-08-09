"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import {
  apiFetch,
  setAccessToken,
  setCurrentUserId,
  setSessionExpiredHandler,
  tryRefreshSession,
} from "@/lib/api-client";
import type { AuthSession, User } from "@/lib/types";

type AuthStatus = "loading" | "authenticated" | "guest";

interface AuthContextValue {
  status: AuthStatus;
  user: User | null;
  signup: (input: { email: string; nickname: string; password: string }) => Promise<void>;
  login: (input: { email: string; password: string }) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>("loading");
  const [user, setUser] = useState<User | null>(null);
  const queryClient = useQueryClient();

  /**
   * 세션이 바뀌면 캐시를 통째로 버린다.
   *
   * 쿼리 키에 사용자 식별자가 없기 때문에, 지우지 않으면 같은 브라우저에서 다음 사람이
   * 로그인했을 때 이전 사용자의 일정·모듈 데이터가 그대로 화면에 뜬다.
   */
  const resetSession = useCallback(() => {
    setAccessToken(null);
    setCurrentUserId(null);
    setUser(null);
    setStatus("guest");
    queryClient.clear();
  }, [queryClient]);

  // 새로고침하면 메모리의 액세스 토큰은 사라진다.
  // httpOnly 쿠키의 리프레시 토큰으로 세션을 조용히 복원한다.
  useEffect(() => {
    let cancelled = false;
    (async () => {
      const outcome = await tryRefreshSession();
      if (cancelled) return;
      if (outcome.ok) {
        const restored = outcome.session.user as unknown as User;
        setCurrentUserId(restored.id);
        setUser(restored);
        setStatus("authenticated");
      } else {
        setStatus("guest");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  // 재발급이 최종 실패하면(쿠키 만료·서버측 폐기) 앱을 guest 로 되돌린다.
  // 이게 없으면 세션이 죽어도 화면은 로그인 상태로 남아 빈 카드만 보여준다.
  useEffect(() => {
    setSessionExpiredHandler(() => resetSession());
    return () => setSessionExpiredHandler(null);
  }, [resetSession]);

  const acceptSession = useCallback(
    (session: AuthSession) => {
      queryClient.clear();
      setAccessToken(session.accessToken);
      setCurrentUserId(session.user.id);
      setUser(session.user);
      setStatus("authenticated");
    },
    [queryClient],
  );

  const signup = useCallback(
    async (input: { email: string; nickname: string; password: string }) => {
      const session = await apiFetch<AuthSession>("/api/v1/auth/signup", {
        method: "POST",
        body: JSON.stringify(input),
      });
      acceptSession(session);
    },
    [acceptSession],
  );

  const login = useCallback(
    async (input: { email: string; password: string }) => {
      const session = await apiFetch<AuthSession>("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify(input),
      });
      acceptSession(session);
    },
    [acceptSession],
  );

  const logout = useCallback(async () => {
    try {
      await apiFetch<void>("/api/v1/auth/logout", { method: "POST" });
    } catch {
      // 로그아웃은 실패할 이유가 없다. 서버가 못 받았어도 로컬 세션은 끝낸다.
    } finally {
      resetSession();
    }
  }, [resetSession]);

  const value = useMemo(
    () => ({ status, user, signup, login, logout }),
    [status, user, signup, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth 는 AuthProvider 안에서만 쓸 수 있어요.");
  return context;
}
