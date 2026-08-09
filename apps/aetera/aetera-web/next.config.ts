import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  // 모노레포 루트에 다른 lockfile 이 있어도 워크스페이스 루트를 이 앱으로 고정한다.
  outputFileTracingRoot: import.meta.dirname,
  // dev 서버(.next)와 프로덕션 빌드(.next-build)의 산출물을 분리한다.
  // 분리하지 않으면 `nx build aetera-web`(next build)이 떠 있는 dev 서버의 .next 를
  // 덮어써서 dev 서버가 404 청크를 내놓는다 (흰 화면).
  distDir: process.env.NODE_ENV === "production" ? ".next-build" : ".next",
};

export default nextConfig;
