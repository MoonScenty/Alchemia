# Alchemia

Thaumcraft 5.2.4 (Minecraft 1.8.9)의 게임플레이를 Minecraft 1.21.1 / NeoForge 환경에서 새로 구현하는 마법 모드입니다.

원본 코드를 옮겨 적지 않습니다. 원본의 **기능과 동작**을 분석한 뒤 1.21.1의 방식(데이터 컴포넌트, 데이터팩, 어태치먼트, 캐퍼빌리티 등)에 맞게 처음부터 다시 작성합니다.

## 개발 환경

| 항목 | 버전 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.251 |
| Java | 21 |
| 매핑 | Mojang official + Parchment 2024.11.17 |

```bash
./gradlew build        # 빌드
./gradlew runClient    # 클라이언트 실행
./gradlew runServer    # 서버 실행
./gradlew runData      # 데이터 생성 (src/generated/resources)
./gradlew runGameTestServer  # GameTest 실행
```

## 구조

```
src/main/java/me/moonscenty/alchemia/
  Alchemia.java          모드 진입점
  AlchemiaConfig.java    설정
  client/                클라이언트 전용 코드
  registry/              DeferredRegister 모음 (ModBlocks, ModItems, ...)
docs/PORTING_PLAN.md         원본 기능 목록과 포팅 순서
docs/PORTING_CONVENTIONS.md  포팅 원칙과 코드 규칙
```

## 참고 자료

원본 jar와 디컴파일 결과물은 저작권 문제로 **이 저장소에 포함하지 않습니다.** 준비 방법은 [docs/PORTING_PLAN.md](docs/PORTING_PLAN.md)를 참고하세요.

## 라이선스

코드는 [MIT](LICENSE.md). 상 아이콘은 [game-icons.net](https://game-icons.net)의 Lorc가 만든 CC BY 3.0 에셋입니다. 자세한 내용은 [CREDITS.md](CREDITS.md)를 보세요.
