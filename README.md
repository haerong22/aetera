# Aetera

**태어날 때부터 죽을 때까지 — 인생의 모든 순간을 설계하는 모듈형 라이프 플랫폼.**

일정, 가계부, 퇴사·결혼 같은 인생 이벤트 가이드까지, 모든 기능이 **모듈**로 만들어져 있고
사용자는 필요한 모듈만 골라서 켠다. 인생 관리의 앱스토어라고 생각하면 된다.

## 빠른 시작

로컬 실행에 필요한 명령은 세 개뿐이다.

```bash
# 1. 로컬 인프라 (PostgreSQL 18) — Docker 가 떠 있으면 bootRun 이 자동으로 띄우기도 한다
docker compose up -d postgres

# 2. 백엔드 (http://localhost:8080, Swagger: /swagger-ui.html)
./gradlew :aetera-app:bootRun

# 3. 프론트엔드 (http://localhost:3000)
cd apps/aetera/aetera-web && npm install && npm run dev
```

```bash
./gradlew build            # 포맷 검사 + 컴파일 + 단위 테스트 + ArchUnit + 에러코드 검증 (Docker 불필요)
./gradlew integrationTest  # Testcontainers 통합 테스트 (Docker 필요)
./gradlew spotlessApply    # 포맷 자동 정리
```

### Nx 오케스트레이션

**빌드는 여전히 Gradle/Next 가 한다.** Nx 는 그 위에서 "무엇을 돌릴지"만 정한다 —
변경 파일이 도달할 수 있는 프로젝트만 빌드하는 `nx affected` 와 언어를 가리지 않는
캐시가 목적이다. 백엔드(Gradle 모듈)와 프론트엔드(Next.js)가 한 그래프에서 함께 돈다.

```bash
npm ci                                  # 최초 1회 (루트: nx)
(cd apps/aetera/aetera-web && npm ci)   # 최초 1회 (프론트엔드 의존성)

npx nx run-many -t build   # 전체 빌드 (백엔드 gradle build + 프론트 next build)
npx nx affected -t build   # 변경 영향 범위만
npx nx run aetera-web:dev  # 프론트 개발 서버
npx nx graph               # 의존 그래프 시각화
```

**Gradle 모듈에는 project.json 이 없다 — `tools/nx-gradle-lite.js` 가 추론한다.**
이 로컬 플러그인(nx.json 의 `plugins` 에 등록)이 nx 실행 시점에 빌드 파일의 선언만 읽어
프로젝트·타깃·의존 간선을 만들고, 실제 명령은 `nx.json` 의 targetDefaults 가
`./gradlew :{projectName}:build` 로 위임한다:

- `settings.gradle.kts` 의 `includeService`/`includeSharedLib` → 모듈 목록과 이름
- 각 모듈 `build.gradle.kts` 의 `project(":이름")` → 의존 간선 (affected 계산 근거)
- `src/test` 존재 → `test` 타깃, `@Tag("integration")` 존재 → `integrationTest` 타깃

그래서 **모듈을 추가하거나 의존을 바꿔도 Nx 쪽은 아무것도 손대지 않는다** —
Gradle 빌드 파일이 곧 진실이고 그래프는 자동으로 따라온다. 그래프가 이상해 보이면
`npx nx reset` 으로 캐시를 비운다.

공식 `@nx/gradle` 플러그인을 쓰지 않는 이유: 그 플러그인의 그래프 스캔은 Gradle 태스크를
실행해 태스크 의존을 전수 순회하다 `build-logic`(included build) 경계에서 Gradle
라이프사이클 락과 충돌해 데드락이 난다. Nx 에 필요한 건 모듈 수준 의존뿐이라,
우리 플러그인은 Gradle 을 실행하지 않고 선언만 읽는다 — 같은 자동화, 지뢰 없이.

`project.json` 이 남아 있는 곳은 Gradle 모듈이 아닌 둘뿐이다:
`aetera-api-server`(서비스 단위 docker-build 타깃), `aetera-web`(Next.js 앱).

## 저장소 구조

여러 서비스가 한 저장소에 사는 모노레포다. 백엔드는 클린 아키텍처 멀티모듈, 프론트엔드는 Next.js.

```
aetera
├── build-logic/               ← Gradle 컨벤션 플러그인 (included build)
├── gradle/libs.versions.toml  ← 의존성 버전 단일 출처
│
├── apps/aetera/
│   ├── aetera-api-server/     ← 백엔드 서비스 (Kotlin + Spring Boot 4)
│   │   ├── app                ← application 정의. 모듈을 임포트만 함
│   │   ├── controller         ← inbound adapter (REST API, 인증/모듈 가드 인터셉터)
│   │   ├── gateway            ← outbound adapter (JPA, Flyway 마이그레이션)
│   │   ├── infrastructure     ← 인프라 상세 구현 (PBKDF2 암호화, JWT 서명)
│   │   ├── usecase            ← Application Service + 모듈 레지스트리
│   │   ├── model              ← 도메인 모델 (user / auth / module / schedule)
│   │   └── config             ← 설정 클래스와 application yml
│   │
│   └── aetera-web/            ← 프론트엔드 (Next.js 15 + Tailwind 4 + TanStack Query)
│       └── src/modules/       ← 백엔드 모듈 구조의 미러 (registry.ts)
│
├── libs/shared/shared-core    ← 서비스 경계를 넘는 공유 타입 (에러 규격, Page/Slice)
├── compose.yaml               ← 로컬 개발 인프라
└── nx.json / package.json     ← Nx 오케스트레이션 (빌드는 Gradle 이 함)
```

의존 방향과 에러 코드 체계(HTTP status 3자리 + 일련번호 4자리, 도메인별 대역)는
`app/src/test/kotlin/io/aetera/app/` 의 **ArchitectureTest / ErrorCodeTest 가 빌드에서 강제**한다.

## 모듈 시스템 — 이 플랫폼의 핵심 계약

```
사용자  ──enable/disable──▶  core (ModuleRegistry + ModuleEnrollment + 가드 인터셉터)
                                   ▲ List<AeteraModule> 주입으로 발견
모듈(schedule, budget, ...)  ──descriptor 빈 1개 등록──┘
```

- **등록**: 모듈은 `AeteraModule` 인터페이스를 구현한 빈 하나로 플랫폼에 편입된다.
  코어는 `List<AeteraModule>` 를 주입받아 발견하므로 **모듈 추가에 코어 수정이 0줄**이다.
- **활성화 강제**: 모듈 API 는 전부 `/api/v1/modules/{module-id}/` 아래에 둔다.
  활성화하지 않은 사용자의 요청은 코어의 `ModuleGuardInterceptor` 가 일괄 403 처리한다 —
  모듈 컨트롤러에는 검사 코드가 한 줄도 없다.
- **소프트 비활성화**: 모듈을 꺼도 데이터는 남고 접근만 막힌다. 다시 켜면 그대로 돌아온다.
- **데이터 격리**: 모듈 테이블은 `user_id` 를 평범한 컬럼으로만 갖고 사용자 테이블에 FK 를 걸지 않는다.
- **모듈 간 참조 금지**: ArchUnit 이 모듈 → 다른 모듈 의존을 빌드에서 깨뜨린다.

### 새 모듈 추가하기 (예: 가계부 `budget`)

백엔드 — 템플릿의 "새 도메인 추가" 절차와 같다:

1. `model/budget/` — 도메인 모델, Repository 인터페이스, `BudgetErrorCode` (`shared/error/ErrorCode.kt` 에 `BUDGET_BAND` 등록 후)
2. `usecase/budget/` — 서비스들 + **`BudgetModule : AeteraModule` 빈 1개** (이게 플랫폼 등록의 전부)
3. `gateway/budget/` — JpaEntity/JpaRepository/Adapter + Flyway 마이그레이션 (user FK 금지)
4. `controller/budget/` — `/api/v1/modules/budget/` 아래 API (활성화 검사 불필요 — 가드가 대신함)
5. `app/.../ArchitectureTest.kt` 의 모듈 간 참조 금지 목록에 `..budget..` 한 줄

프론트엔드:

6. `src/modules/budget/` 에 모듈 구현, `src/modules/registry.ts` 에 한 줄 등록

### 인증 설계

- 이메일+비밀번호 (PBKDF2). `users`(프로필) 와 `auth_credentials`(인증 수단) 를 분리해 뒀으므로
  **카카오 로그인 추가 = provider 행 추가** 다. `User` 는 바뀌지 않는다.
- 액세스 토큰: 15분짜리 JWT(HS256), 프론트는 메모리에만 보관.
- 리프레시 토큰: 원문은 httpOnly 쿠키(`/api/v1/auth` 경로 한정), 서버는 SHA-256 해시만 저장.
  재발급마다 회전(rotation)하고, 폐기된 토큰이 재사용되면 탈취로 보고 그 사용자의 세션을 전부 끊는다.

## 스택

| 영역 | 선택 |
|---|---|
| 백엔드 | Kotlin 2.3 / Spring Boot 4.1 / Java 21 / PostgreSQL 18 / Flyway / JPA |
| 프론트 | Next.js 15 (App Router, 클라이언트 렌더링 중심) / React 19 / Tailwind 4 / TanStack Query 5 |
| 테스트 | Kotest + MockK (단위) / Testcontainers (통합) / ArchUnit (아키텍처) |
| 빌드 | Gradle 9 (컨벤션 플러그인) + Nx 오케스트레이션 (`npx nx affected -t build`) |
| 배포 | `./gradlew :aetera-app:bootBuildImage` 또는 `Dockerfile` / 프론트는 Vercel 권장 |

## 로드맵

- [x] 코어: 회원/인증(JWT+리프레시 회전), 모듈 레지스트리·활성화·가드
- [x] 일정 모듈 — 계약 검증용 1호 모듈
- [ ] 가이드/체크리스트 모듈 ("퇴사 준비" 첫 콘텐츠) — 콘텐츠형 모듈로 계약 2차 검증
- [ ] 카카오 로그인 (`auth_credentials` 에 provider 행 추가)
- [ ] 타임라인 피드(모듈 이벤트 구독), 알림
- [ ] 가계부 모듈
