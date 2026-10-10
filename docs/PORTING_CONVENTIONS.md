# 포팅 규칙

Thaumcraft 5.2.4 (MC 1.8.9)의 기능을 Minecraft 1.21.1 / NeoForge 21.1 로 새로 구현하는 모드. 모드 ID `alchemia`, 기본 패키지 `me.moonscenty.alchemia`.

## 포팅 원칙

- 원본 코드를 복사하거나 줄 단위로 옮기지 않는다. 디컴파일 소스는 **기능·수치·동작을 확인하는 용도**로만 읽고, 구현은 1.21.1 방식으로 새로 작성한다.
- 원본의 텍스처·모델·사운드·문서 텍스트도 저장소에 넣지 않는다 (Thaumcraft는 All Rights Reserved).
- **원본 에셋은 플레이어의 jar에서 읽어 온다.** 게임 폴더의 `legacy/`에 Thaumcraft 4(1.7.10)와 5(1.8.9) jar를 두면 `client/legacy/`가 실행 중에 필요한 것만 꺼내 내장 리소스팩으로 내준다. 디스크에 풀어 놓지 않는다. 개발 중에는 `run/legacy/`이고 `run/`은 이미 `.gitignore`에 있다.
  - 무엇을 어디서 꺼낼지는 `LegacyAssets`에 단계별로 적는다. 우리 파일 경로 하나에 원본 경로를 TC5, TC4 순으로 적고, 먼저 찾은 쪽을 쓴다.
  - **두 jar는 실행 필수다**(2026-10-10). 클라이언트는 시작할 때(`AlchemiaClient` → `LegacyAssetImporter.requireJars()`) 둘 다 있는지 보고, 하나라도 없으면 폴더 경로와 빠진 jar를 적은 예외로 모드 로딩을 멈춘다. jar는 이름이 아니라 안의 `mcmod.info` 버전으로 가린다. 데이터 생성(`runData`)과 게임 테스트 서버는 그림을 쓰지 않으므로 검사하지 않는다.
  - **저장소에는 대체 그림·모델을 두지 않는다**(2026-10-10 정리). 원본에 있는 것은 전부 jar에서 오고, 렌더러에도 "jar가 없으면" 갈래가 없다. 저장소에 남는 것은 원본에 없는 것(심층암 광석, 노드 배치기 등)과, 원본 그림을 입히는 모양 일부(단지의 액체·라벨, 관 화살표, 작업대)뿐이다.
  - 블록·아이템 모델 파일은 원본 그림 경로(`block/legacy/…`, `item/legacy/…`)를 그대로 쓴다. datagen은 jar 없이 돌므로 `DataGenerators.knowImported`가 `LegacyAssets`의 대상 경로를 "있는 파일"로 알려 준다. 새 원본 그림을 모델에서 쓰려면 `LegacyAssets`에 먼저 적는다.
  - 원본이 회색 그림에 코드로 색을 입혔으면(`getColorFromItemStack`, `colorMultiplier`) `.tinted()`로 색을 구워 넣는다. 색은 jar 안의 클래스를 `javap -c`로 열어 확인한다.
  - **코드로 쓰인 모델**(`ModelBase`를 상속해 생성자에서 상자를 쌓는 것)은 `LegacyModelReader`가 생성자 바이트코드를 데이터로 읽어 모양을 꺼낸다. 원본 코드를 실행하지도, 숫자를 옮겨 적지도 않는다. 꺼낸 것은 `LegacyModelBaker`가 `ModelPart`로 굽는다. 무엇을 언제 보이고 숨길지(렌더 메서드의 판단)는 우리가 새로 짠다.
  - 원본 모델 전용 시트는 `.forModel(키)`로 묶는다. 모델을 못 읽었을 때 쓸모없는 시트까지 들고 오지 않도록.
- **사 온 에셋은 저장소에 넣지 않는다.** 쓰는 것과 공개 저장소에 원본 파일을 두는 것은 다른 일이고, 파는 쪽 약관은 대개 뒤쪽을 막는다. 지금은 그런 에셋이 없다(로브와 요새 갑옷의 사 온 메시는 원본 모델로 바꾸며 걷어냈다).
- **예외**: 원본이 제3자의 공개 라이선스 에셋을 가져다 쓴 경우에는 그 출처에서 직접 가져와 라이선스대로 표기하고 쓸 수 있다. 상 아이콘(game-icons.net, Lorc, CC BY 3.0)이 그렇다. 반드시 출처와 라이선스를 확인한 뒤 [CREDITS.md](../CREDITS.md)에 적는다.
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
