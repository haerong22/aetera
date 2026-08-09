/** 백엔드 DTO 와 1:1 로 맞춘 타입들. */

export interface User {
  id: string;
  email: string;
  nickname: string;
  timezone: string;
  registeredAt: string;
}

export interface AuthSession {
  accessToken: string;
  accessTokenExpiresInSeconds: number;
  user: User;
}

export type ModuleCategory = "TOOL" | "GUIDE";

export interface ModuleSummary {
  id: string;
  displayName: string;
  description: string;
  category: ModuleCategory;
  version: string;
  enabled: boolean;
  enabledAt?: string;
}
