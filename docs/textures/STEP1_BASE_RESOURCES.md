# 1단계(기반 자원) 텍스처 목록

상태: **A 완료 (2026-09-20 확인)**, B~E 확인 대기 — MoonScenty 확인 전에는 텍스처를 추가하지 않는다.

- 모든 텍스처는 16×16 PNG. 경로 기준은 `src/main/resources/assets/alchemia/textures/`.
- "틴트" 표시는 회색조 1장을 코드에서 색만 입혀 돌려 쓰는 방식 (원본도 같은 방식). 장수를 줄일 수 있다.
- 레지스트리 이름은 제안이며 명칭 결정에 따라 바뀔 수 있다.

## A. 광석·결정 — 완료 (48개 파일)

생성 스크립트: `Desktop/thaumref/tools/gen_textures_step1a.py` (원본 PNG는 `Desktop/thaumref/textures/`). 색을 바꾸려면 스크립트의 값을 고쳐 다시 실행한다.

| # | 결과 파일 | 제작 방식 (MoonScenty 지정) |
| --- | --- | --- |
| A1 | `block/amber_ore`, `block/deepslate_amber_ore` | 바닐라 `iron_ore` / `deepslate_iron_ore`의 철 부분만 주황색으로 색조 변환 |
| A2 | `block/cinnabar_ore`, `block/deepslate_cinnabar_ore` | 같은 방식, 검붉은색 |
| A3 | `block/crystal/<상>_crystal_stage0~3` (7종 × 4단계 = 28장) | 바닐라 자수정 싹 3종 + 군집을 상별 색으로 색조 변환 |
| A4 | `item/amber` | 제공 파일 그대로 |
| A5 | `item/quicksilver` | 제공 파일 그대로 |
| A6 | `item/<상>_shard` (7장) | 제공 `shard.png`를 상별 색으로 색조 변환 |
| A7 | `item/balanced_shard` + `.mcmeta` | `shard.png` 기반 24프레임, 대각선으로 흐르는 밝은 무지개 + 지나가는 반짝임 (frametime 2, interpolate) |
| A8 | `item/raw_cinnabar` | 제공 파일 그대로 |
| A9 | `item/iron_cluster` | 제공 `gold_cluster.png`의 금색 부분을 철 색으로 |
| A10 | `item/gold_cluster` | 제공 파일 그대로 |
| A11 | `item/cinnabar_cluster` | 금색 부분을 검붉은색으로 |
| — | `item/copper_cluster` | 금색 부분을 구리색으로 |

상별 색: 공기 노랑, 불 주홍, 물 하늘, 땅 초록, 질서 은백, 엔트로피 암회색, 플럭스 보라.
제외: 주석/은/납 군집·조각 (바닐라에 없는 금속).

## B. 금속·기본 재료 (블록 2장 + 아이템 11장)

| # | 경로 | 용도 |
| --- | --- | --- |
| B1 | `block/thaumium_block.png` | 타우미움 블록 |
| B2 | `block/brass_block.png` | 황동 블록 |
| B3 | `item/thaumium_ingot.png` | 타우미움 주괴 |
| B4 | `item/brass_ingot.png` | 황동 주괴 |
| B5 | `item/thaumium_nugget.png` | 타우미움 조각 |
| B6 | `item/brass_nugget.png` | 황동 조각 |
| B7 | `item/quicksilver_drop.png` | 수은 방울 (원본 nugget_quicksilver) |
| B8 | `item/brass_gear.png` | 황동 톱니 |
| B9 | `item/thaumium_gear.png` | 타우미움 톱니 |
| B10 | `item/brass_plate.png` | 황동 판 |
| B11 | `item/iron_plate.png` | 철 판 |
| B12 | `item/thaumium_plate.png` | 타우미움 판 |
| B13 | `item/salis_mundus.png` | 살리스 문두스 (원본은 애니메이션) |

뒤 단계로 미룸: 공허 금속 계열(주괴/조각/톱니/판/블록, 13단계 전후), 수지(tallow)·마법 천(fabric)은 각각 양초·로브 단계에서.

## C. 나무 2종 (블록 12장 + 아이템 없음)

| # | 경로 | 용도 |
| --- | --- | --- |
| C1 | `block/greatwood_log.png` | 거대나무 원목 옆면 |
| C2 | `block/greatwood_log_top.png` | 거대나무 원목 윗면 |
| C3 | `block/greatwood_planks.png` | 거대나무 판자 (계단·반블록 공용) |
| C4 | `block/greatwood_leaves.png` | 거대나무 잎 |
| C5 | `block/greatwood_sapling.png` | 거대나무 묘목 (아이템 공용) |
| C6 | `block/silverwood_log.png` | 은빛나무 원목 옆면 |
| C7 | `block/silverwood_log_top.png` | 은빛나무 원목 윗면 |
| C8 | `block/silverwood_planks.png` | 은빛나무 판자 |
| C9 | `block/silverwood_leaves.png` | 은빛나무 잎 |
| C10 | `block/silverwood_sapling.png` | 은빛나무 묘목 |

선택 (1.21 나무 세트 관례, 원본에는 없음): 껍질 벗긴 원목 (+4장), 문/다락문/울타리/버튼/표지판 등은 판자 텍스처 재사용 가능하나 문·다락문은 전용 텍스처 필요 (+6장). 기본 제안은 **원목·판자·잎·묘목·계단·반블록만**.

## D. 식물 3종 (블록 3장)

| # | 경로 | 용도 |
| --- | --- | --- |
| D1 | `block/shimmerleaf.png` | 반짝잎 (마법 숲, 발광) |
| D2 | `block/cinderpearl.png` | 재진주 (사막, 불꽃 파티클) |
| D3 | `block/vishroom.png` | 비스버섯 (마법 숲) |

아이템 아이콘은 블록 텍스처 공용.

## E. 석재·장식 (블록 4장)

| # | 경로 | 용도 |
| --- | --- | --- |
| E1 | `block/arcane_stone.png` | 신비한 돌 (원본은 무작위 3종 변형 → 1장으로 시작, 원하면 3장) |
| E2 | `block/arcane_stone_bricks.png` | 신비한 돌 벽돌 (계단·반블록 공용) |
| E3 | `block/amber_block.png` | 호박 블록 (원본은 옆/위 2장) |
| E4 | `block/amber_bricks.png` | 호박 벽돌 |

뒤 단계로 미룸: 고대 석재·엘드리치 석재·글리프 석재 등(13단계), 포장석·기둥·탁자·양초·깃발(각 기능 단계).

## 확인이 필요한 것 (B~E)

1. B~E 텍스처 조달 방식 (A처럼 항목별 지정)
2. 선택 항목(껍질 벗긴 원목·문 세트, 신비한 돌 변형) 포함 여부
3. 공허 금속을 뒤 단계로 미루는 데 동의하는지
