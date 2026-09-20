# 포팅 규칙

Thaumcraft 5.2.4 (MC 1.8.9)의 기능을 Minecraft 1.21.1 / NeoForge 21.1 로 새로 구현하는 모드. 모드 ID `alchemia`, 기본 패키지 `me.moonscenty.alchemia`.

## 포팅 원칙

- 원본 코드를 복사하거나 줄 단위로 옮기지 않는다. 디컴파일 소스는 **기능·수치·동작을 확인하는 용도**로만 읽고, 구현은 1.21.1 방식으로 새로 작성한다.
- 원본의 텍스처·모델·사운드·문서 텍스트도 저장소에 넣지 않는다 (Thaumcraft는 All Rights Reserved).
  - 예외: 원본이 제3자의 공개 라이선스 에셋을 가져다 쓴 경우에는 그 출처에서 직접 가져와 라이선스대로 표기하고 쓸 수 있다. 상 아이콘(game-icons.net, Lorc, CC BY 3.0)이 그렇다. 반드시 출처와 라이선스를 확인한 뒤 [CREDITS.md](../CREDITS.md)에 적는다.
- **텍스처가 필요한 작업은 진행 전에 MoonScenty에게 확인받는다.** 필요한 텍스처 목록(파일 경로, 크기, 용도)을 정리해 먼저 보고하고, 임의로 텍스처를 만들거나 가져오지 않는다.
- 클래스/레지스트리 이름에 `thaumcraft`, `TC` 등을 쓰지 않는다.
- **원본 이름에 들어간 `thaum`은 `alche`로 바꾼다.** 레지스트리 ID, 클래스·상수 이름, 태그, 번역 모두 해당한다.
  - thaumium → alchemium (알케미움), thaumometer → alchemometer (알케모미터), thaumonomicon → alchemonomicon (알케모노미콘), thaumatorium → alchematorium (알케마토리움), thaumostatic → alchemostatic, thaumaturge → alchemist
  - 원본 자체를 가리킬 때(Thaumcraft, `thaumref` 경로, 원본 클래스명)와 MoonScenty가 제공한 원본 PNG 파일명은 그대로 둔다.
- 1.8.9 관용구를 그대로 가져오지 말고 현재 대응물을 쓴다:
  - 메타데이터/NBT 아이템 변형 → 개별 아이템 + Data Components
  - `IExtendedEntityProperties`, 플레이어 NBT → Data Attachments
  - TileEntity + `ITickable` → BlockEntity + `BlockEntityTicker`
  - 하드코딩된 레시피·상 부여·연구 → 데이터팩 (커스텀 RecipeType, 데이터맵/리로드 리스너) + datagen
  - `SimpleNetworkWrapper` 패킷 → `CustomPacketPayload` + `StreamCodec`
  - OreDictionary → 태그
  - TESR/GL 직접 호출 → BlockEntityRenderer + RenderType/VertexConsumer
  - Baubles 의존 → 자체 슬롯 없이 시작, 필요 시 Curios 선택적 연동
- 포팅 순서와 진행 상황은 [PORTING_PLAN.md](PORTING_PLAN.md)에 기록한다.

## 참고 자료 (저장소 밖)

- `C:\Users\im\Desktop\thaumref\Thaumcraft-1.8.9-5.2.4.jar` — 원본
- `C:\Users\im\Desktop\thaumref\src\thaumcraft\` — 디컴파일 + MCP 이름 적용본. **여기를 읽는다.**
- `C:\Users\im\Desktop\thaumref\decompiled\` — Vineflower 원출력 (SRG 이름, assets 포함)
- `C:\Users\im\Desktop\thaumref\tools\remap_srg.py` — SRG → MCP 이름 치환 스크립트. 매핑·분석용 도구는 모드 동작에 필요한 코드가 아니므로 저장소가 아닌 이 폴더에 둔다.
- `C:\Users\im\Desktop\thaumref\MCP-919\` — MCP 1.8.9 매핑(`conf/*.csv`)과 바닐라 1.8.9 소스(`src/`)
- 주요 진입점: `api/blocks/BlocksTC`, `api/items/ItemsTC`, `common/config/Config*.java` (블록·아이템·레시피·연구·상 등록)

## 코드 규칙

- 레지스트리는 `registry/Mod*.java`의 `DeferredRegister`에 모은다.
- 클라이언트 전용 코드는 `client/` 패키지 아래에만 둔다.
- `ResourceLocation`은 `Alchemia.id("path")`로 만든다.
- 모델·블록스테이트·루트테이블·레시피·태그·lang은 가능하면 datagen(`./gradlew runData`)으로 생성한다.
- 번역은 `datagen/ModLanguageProvider`에서 `pick(영어, 한국어)`로 두 언어를 한 줄에 같이 적는다. lang JSON을 직접 만들지 않는다.
- `src/generated/resources`는 커밋한다. 블록·아이템을 추가하면 `runData`를 다시 돌린다.
- 텍스처 생성 스크립트와 원본 PNG는 `Desktop/thaumref/tools`, `Desktop/thaumref/textures`에 둔다 (저장소에는 결과 PNG만).

## 명령

- `./gradlew build` / `runClient` / `runServer` / `runData`
- 플레이어 데이터는 `player/` 패키지의 어태치먼트에 넣고, 동기화는 `sync(StreamCodec)`에 맡긴다. 커스텀 패킷을 만들지 않는다.
- GameTest는 `makeMockServerPlayerInLevel()`로 어태치먼트를 건드리면 동기화 패킷이 막혀 크래시한다. 판정 로직을 순수 함수로 분리해서 플레이어 없이 검증한다.
- 상 값은 `datagen/ModDataMapProvider`에 원재료만 적는다. 만들어지는 물건은 `aspect/Aspects`가 레시피에서 계산하므로 적지 않는다.
- `./gradlew runGameTestServer` — `gametest/` 패키지의 GameTest 실행. 월드젠처럼 눈으로 확인하기 어려운 로직은 여기에 검사를 추가한다. 빈 테스트 구조물은 `Desktop/thaumref/tools/make_empty_structure.py`로 만든다.
