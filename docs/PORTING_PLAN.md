# 포팅 계획

대상: Thaumcraft 1.8.9-5.2.4 (클래스 910개) → Alchemia (Minecraft 1.21.1, NeoForge 21.1)

## 참고 자료 준비

원본, 디컴파일 결과, 매핑 도구(`tools/remap_srg.py`)는 모두 저장소 밖(`Desktop/thaumref`)에 둔다.

```bash
cd ~/Desktop/thaumref
# 1. 디컴파일 (Vineflower — NeoForge 빌드를 한 번 돌리면 Gradle 캐시에 들어 있음)
java -Xmx3G -jar <vineflower.jar> --only=thaumcraft Thaumcraft-1.8.9-5.2.4.jar decompiled
# 2. MCP 매핑
git clone --depth 1 https://github.com/Marcelektro/MCP-919
# 3. SRG → MCP 이름 치환
python tools/remap_srg.py MCP-919/conf decompiled/thaumcraft src/thaumcraft
```

결과: 910개 파일, 이름 54,409개 치환, 698개 미매핑(대부분 Forge 추가분·람다 파라미터).

## 원본 기능 목록

| 시스템 | 원본 위치 | 내용 |
| --- | --- | --- |
| 상(Aspect) | `api/aspects` | 기본 6종(aer, terra, ignis, aqua, ordo, perditio) + 합성 상 40여 종, `AspectList`, 아이템·엔티티별 상 부여 (`ConfigAspects`) |
| 오라 / 비스 | `common/lib/aura`, `api/aura` | 청크 단위 오라, 오라 노드 엔티티와 노드 타입 7종, 별도 스레드 시뮬레이션, 플럭스 |
| 연구 | `api/research`, `common/lib/research`, `client/gui` | 타우모노미콘, 연구 카테고리/항목, 스캔(타우모미터), 연구 노트 미니게임, 플레이어 지식 |
| 워프 | `events/WarpEvents`, `lib/potions` | 영구/일반/임시 워프, 워프 이벤트, 포션 효과 7종 |
| 제작 | `api/crafting`, `lib/crafting`, `tiles/crafting` | 신비한 작업대(비스 소모), 도가니, 주입 제단(불안정성), 타우마토리움, 완드 작업대, 패턴 크래프터 |
| 에센시아 | `tiles/essentia`, `blocks/devices` | 제련로(기본/타우미움/공허)+보조장치, 증류기, 관/밸브/필터, 단지, 원심분리기, 결정화기, 에센시아 거울 |
| 완드 | `api/wands`, `items/wands` | 완드/홀/지팡이, 캡·막대 조합, 포커스 13종, 포커스 주머니, 포커스 업그레이드 |
| 골렘 | `api/golems`, `entities/construct/golem` | 재질/머리/팔/다리/부가장치 조합, 봉인(seal) 18종 기반 작업 시스템, 골렘 제작기 |
| 장치 | `blocks/devices`, `tiles/devices` | 신비한 굴착기, 지옥 용광로, 거울, 비전 램프 3종, 공중부양기, 레드스톤 중계기, 신비한 귀, 오라 토템, 스파, 굶주린 상자, 노드 안정기, 터렛 등 |
| 장비 | `items/armor`, `items/tools`, `items/baubles` | 타우미움/공허/요새/진홍 방어구, 로브, 고글, 여행자의 장화, 원소 도구, 원시 분쇄기, 룬 보호막 장신구, 주입 인챈트 |
| 오염 | `blocks/world/taint`, `events/TaintEvents`, `entities/monster/tainted` | 오염 섬유/블록 확산, 오염된 몹 11종, 플럭스 액체 |
| 몹 | `entities/monster` | 페크, 위습, 화염박쥐, 성난 좀비 계열, 엘드리치 수호자/게, 진홍 교단, 보스, 챔피언 모드(mods) 16종 |
| 월드젠 | `common/lib/world` | 광석(호박, 진사), 결정 6+1종, 은빛나무/거대나무, 식물 3종, 마법 숲·으스스한 바이옴, 구조물(언덕, 돌무더기, 교단 포탈), 외부 차원(dim) |
| 클라이언트 | `client/fx`, `client/renderers`, `client/lib` | 파티클/빔 이펙트, 타일·엔티티 렌더러, OBJ 모델, HUD, 셰이더 |

`codechicken/lib`(내장 렌더 유틸), `loader`(의존성 다운로더), `vazkii/botania/api`(연동)는 포팅하지 않는다.

## 포팅 순서

각 단계는 그 자체로 빌드·실행 가능한 상태로 끝낸다.

- [x] **0. 프로젝트 정리** — 템플릿 제거, 패키지 구조, 참고 자료 준비
- [ ] **1. 기반 자원** — datagen 구성 완료 (`./gradlew runData`)
  - [x] A. 광석·결정: 호박/진사 광석(+심층암), 결정 7종(4단계), 조각 7종+균형 조각, 진사 원석, 군집 4종, 제련 레시피, 월드젠
    - 결정의 성장·전파·플럭스 오염은 오라에 의존하므로 5단계에서 구현. 지금은 생성된 크기 그대로 유지된다.
    - 바이옴별 결정 편향(원본 `BiomeHandler`)은 2단계 이후.
    - 군집 입수 경로(원소 곡괭이, 도가니 정제)는 9·7단계에서.
  - [ ] B. 금속·기본 재료
  - [ ] C. 나무 2종
  - [ ] D. 식물 3종
  - [ ] E. 석재·장식
- [ ] **2. 상 시스템** — `Aspect` 레지스트리(커스텀 레지스트리 또는 데이터팩), `AspectList` + Codec/StreamCodec, 아이템→상 데이터맵, 툴팁 표시
- [ ] **3. 플레이어 데이터** — 지식·워프 Data Attachment, 동기화 패킷, 명령어
- [ ] **4. 연구** — 타우모미터 스캔, 연구 데이터(JSON), 타우모노미콘 GUI, 연구 탁자
- [ ] **5. 오라/비스** — 청크 어태치먼트 기반 오라, 노드, 플럭스
- [ ] **6. 완드와 신비한 작업대** — 완드 컴포넌트(캡/막대/비스), 커스텀 RecipeType, 메뉴/스크린
- [ ] **7. 도가니와 에센시아** — 도가니 레시피, 제련로·증류기·관·단지 (에센시아 운송 캐퍼빌리티)
- [ ] **8. 주입 제단** — 멀티블록, 불안정성, 주입 레시피/인챈트
- [ ] **9. 장비·포커스** — 방어구, 도구, 포커스, 장신구
- [ ] **10. 장치류** — 굴착기, 용광로, 거울, 램프 등
- [ ] **11. 몹과 오염** — 몹, 오염 확산, 챔피언
- [ ] **12. 골렘** — 부품 조합, 봉인, 작업 AI
- [ ] **13. 엘드리치** — 외부 차원, 보스, 후반 콘텐츠
- [ ] **14. 마감** — 이펙트 다듬기, JEI/Curios 연동, 밸런스

## 미결 사항

- **에셋**: 원본 텍스처/모델/사운드는 쓸 수 없다. 텍스처가 필요한 항목은 단계마다 목록을 만들어 MoonScenty에게 확인받은 뒤 진행한다.
- **명칭**: 아이템·블록·상의 표시 이름을 원본과 같게 할지, Alchemia 고유 명칭으로 바꿀지.
