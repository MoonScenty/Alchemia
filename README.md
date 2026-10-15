# Alchemia

**Thaumcraft를 Minecraft 1.21.1에서 돌아가게 해 주는 애드온입니다.**

Alchemia는 홀로 서는 모드가 아니라 Thaumcraft의 애드온입니다. 플레이어가 가진 **Thaumcraft 4와 Thaumcraft 5**가 있어야 하고, 그 둘을 Minecraft 1.21.1 / NeoForge에서 정상적으로 구동되도록 이어 줍니다.

- **게임플레이**는 Thaumcraft 5.2.4(Minecraft 1.8.9)를 따릅니다. 원본의 기능과 동작을 분석해 1.21.1의 방식(데이터 컴포넌트, 데이터팩, 어태치먼트, 캐퍼빌리티 등)으로 다시 작성했습니다. 원본 코드를 옮겨 적지 않습니다.
- **그림·모델·소리·파티클**은 원본 Thaumcraft 4·5 jar에서 실행할 때 읽어 옵니다. 이 저장소에는 원본 에셋이 들어 있지 않습니다.

## 실행에 필요한 것

| 항목 | 버전 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.251 이상 |
| TerraBlender | 1.21.1용 (마법의 숲 생성) |
| Curios | 1.21.1용 9 이상 (장신구 칸) |
| **Thaumcraft 4** | Minecraft 1.7.10용, 예: `Thaumcraft-1.7.10-4.2.3.5.jar` |
| **Thaumcraft 5** | Minecraft 1.8.9용, 예: `Thaumcraft-1.8.9-5.2.4.jar` |

두 Thaumcraft jar를 게임 폴더의 `legacy/`(개발 중에는 `run/legacy/`)에 넣습니다. 파일 이름은 상관없습니다. **하나라도 없으면 타이틀 화면 대신 안내 화면이 뜹니다.** 빠진 jar를 작성자의 CurseForge 페이지([Thaumcraft 4](https://www.curseforge.com/minecraft/mc-mods/thaumcraft/files/2227552), [Thaumcraft 5](https://www.curseforge.com/minecraft/mc-mods/thaumcraft/files/2287496))에서 받는 버튼과 `legacy` 폴더를 여는 버튼이 있습니다. 원본 라이선스는 작성자만 배포할 수 있게 하므로, 이 저장소나 배포본에 포함하지 않고 자동으로 내려받지도 않습니다.

## 진행 상황

[포팅 계획](docs/PORTING_PLAN.md)의 열네 단계가 **모두** 들어왔습니다. 남은 것은 직접 플레이한 뒤의 숫자 밸런스입니다.

| 단계 | 내용 | 상태 |
| --- | --- | --- |
| 0~4 | 기반 자원, 상 35종, 플레이어 데이터·뒤틀림, 연구(스캔·알케모노미콘·연구 탁자) | 완료 |
| 5 | 청크 오라, 노드, 플럭스와 오염 | 완료 |
| 6 | 완드와 비전 작업대 | 완료 |
| 7 | 도가니, 제련로, 관과 단지, 공허 금속 | 완료 |
| 8 | 주입 제단 | 완료 |
| 9 | 장비와 포커스 열둘 | 완료 |
| 10 | 장치류(램프, 굴착기, 지옥 용광로, 거울, 타우마토리움 등) | 완료 |
| 11 | 몹과 오염: 성난 좀비, 챔피언, 위습, 페크, 오염 몹, 오염된 동물, 연금 슬라임 | 완료 |
| 12 | 골렘: 몸, 봉인과 작업, 전투 | 완료 |
| 13 | 엘드리치: 바이옴, 엘드리치 몹·진홍 교단, 엘드리치 블록과 진주, 바깥 땅과 보스 | 완료 |
| 14 | 마감: 장신구와 룬 보호막(Curios), JEI, 남은 것들, 소리·뒤틀림·빠졌던 것 다듬기 | 완료 (밸런스는 플레이 뒤에) |

## 개발

| 항목 | 버전 |
| --- | --- |
| Java | 21 |
| 매핑 | Mojang official + Parchment 2024.11.17 |

```bash
./gradlew build              # 빌드
./gradlew runClient          # 클라이언트 실행
./gradlew runServer          # 서버 실행
./gradlew runData            # 데이터 생성 (src/generated/resources)
./gradlew runGameTestServer  # GameTest 실행
```

```
src/main/java/me/moonscenty/alchemia/
  Alchemia.java          모드 진입점
  AlchemiaConfig.java    설정
  client/                클라이언트 전용 코드
  client/legacy/         원본 jar에서 에셋을 읽어 오는 곳
  registry/              DeferredRegister 모음 (ModBlocks, ModItems, ...)
docs/PORTING_PLAN.md         원본 기능 목록과 포팅 순서, 단계별 기록
docs/PORTING_CONVENTIONS.md  포팅 원칙과 코드 규칙
```

## 라이선스

코드는 [MIT](LICENSE.md). Thaumcraft와 그 에셋의 권리는 원작자(Azanor)에게 있습니다. 자세한 것은 [CREDITS.md](CREDITS.md)를 보세요.
