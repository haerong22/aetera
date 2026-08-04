/**
 * Gradle 모듈을 Nx 프로젝트로 추론하는 로컬 플러그인 — @nx/gradle 의 경량 대체물.
 *
 * 공식 플러그인은 그래프를 얻으려고 Gradle 태스크(nxProjectGraph)를 실행하고, 그 태스크가
 * 태스크 수준 의존을 전수 순회하다 build-logic(included build) 경계에서 Gradle 라이프사이클
 * 락과 충돌해 데드락이 난다. Nx 그래프에 필요한 건 모듈 수준 의존뿐이므로, 이 플러그인은
 * Gradle 을 아예 실행하지 않고 빌드 파일의 "선언"만 읽는다:
 *
 *   - settings.gradle.kts  : includeService / includeSharedLib 호출 → 모듈 디렉터리 ↔ 이름 매핑
 *   - build.gradle.kts     : project(":이름") 참조 → 모듈 간 의존 간선
 *   - src/test 존재 여부   : test 타깃 노출
 *   - @Tag("integration")  : integrationTest 타깃 노출
 *
 * 실행 명령은 nx.json 의 targetDefaults 가 `./gradlew :{projectName}:build` 로 위임하므로
 * 빌드는 여전히 Gradle 이 한다. 이 파일은 "지도 제작"만 담당한다.
 *
 * 한계: 그래프는 빌드 파일이 바뀔 때 다시 계산된다. 드물게 캐시가 어긋나 보이면
 * `npx nx reset` 으로 초기화하면 된다.
 */
const { existsSync, readFileSync, readdirSync } = require("fs");
const { dirname, join } = require("path");

/**
 * settings.gradle.kts 를 읽어 { 모듈 디렉터리(상대경로) → Gradle 프로젝트 이름 } 매핑을 만든다.
 *
 * 노드 생성과 의존 간선 생성이 같은 매핑을 쓰므로 파일 내용으로 캐시한다.
 * (Nx 는 두 훅을 따로 부른다 — 캐시가 없으면 그래프 계산마다 같은 파일을 두 번 파싱한다.)
 */
let settingsCache = { text: null, map: null };

function parseSettings(workspaceRoot) {
  const text = readFileSync(join(workspaceRoot, "settings.gradle.kts"), "utf8");
  if (settingsCache.text === text) return settingsCache.map;

  const nameByRoot = new Map();

  // includeService("apps/aetera/aetera-api-server", "aetera", "model", "usecase", ...)
  for (const match of text.matchAll(/includeService\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,([^)]*)\)/g)) {
    const [, servicePath, prefix, moduleList] = match;
    for (const moduleMatch of moduleList.matchAll(/"([^"]+)"/g)) {
      const module = moduleMatch[1];
      nameByRoot.set(`${servicePath}/${module}`, `${prefix}-${module}`);
    }
  }

  // includeSharedLib("shared-core", ...)
  for (const match of text.matchAll(/includeSharedLib\(([^)]*)\)/g)) {
    for (const nameMatch of match[1].matchAll(/"([^"]+)"/g)) {
      nameByRoot.set(`libs/shared/${nameMatch[1]}`, nameMatch[1]);
    }
  }

  settingsCache = { text, map: nameByRoot };
  return nameByRoot;
}

/** src/test 아래 .kt 파일 중 @Tag("integration") 을 쓰는 게 있는지. */
function hasIntegrationTests(dir) {
  if (!existsSync(dir)) return false;
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (hasIntegrationTests(path)) return true;
    } else if (entry.name.endsWith(".kt") && readFileSync(path, "utf8").includes('@Tag("integration")')) {
      return true;
    }
  }
  return false;
}

/** build.gradle.kts 하나 → Nx 프로젝트 하나. */
const createNodesV2 = [
  "{apps,libs}/**/build.gradle.kts",
  (configFiles, _options, context) => {
    const nameByRoot = parseSettings(context.workspaceRoot);

    return configFiles.map((configFile) => {
      const root = dirname(configFile);
      const name = nameByRoot.get(root);
      // settings.gradle.kts 에 등록되지 않은 빌드 파일은 Gradle 빌드에 속하지 않는다 — 무시.
      if (!name) return [configFile, {}];

      const buildFile = readFileSync(join(context.workspaceRoot, configFile), "utf8");
      const testDir = join(context.workspaceRoot, root, "src/test");

      const targets = { build: {} };
      if (existsSync(testDir)) targets.test = {};
      if (hasIntegrationTests(testDir)) targets.integrationTest = {};

      return [
        configFile,
        {
          projects: {
            [root]: {
              name,
              projectType: buildFile.includes("convention.spring-boot-app") ? "application" : "library",
              tags: ["scope:backend"],
              targets,
            },
          },
        },
      ];
    });
  },
];

/** build.gradle.kts 의 project(":이름") 선언 → Nx 의존 간선. */
const createDependencies = (_options, context) => {
  const nameByRoot = parseSettings(context.workspaceRoot);
  const dependencies = [];

  for (const [root, source] of nameByRoot) {
    if (!(source in context.projects)) continue;
    const buildFilePath = `${root}/build.gradle.kts`;
    const absolutePath = join(context.workspaceRoot, buildFilePath);
    if (!existsSync(absolutePath)) continue;

    const text = readFileSync(absolutePath, "utf8");
    for (const match of text.matchAll(/project\(":([^"]+)"\)/g)) {
      const target = match[1];
      if (target !== source && target in context.projects) {
        dependencies.push({ source, target, type: "static", sourceFile: buildFilePath });
      }
    }
  }

  return dependencies;
};

module.exports = { createNodesV2, createDependencies };
