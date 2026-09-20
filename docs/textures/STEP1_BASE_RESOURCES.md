# 1단계(기반 자원) 텍스처 목록

상태: **A~E 완료 (2026-09-20 확인). 1단계 텍스처 끝.**

- 모든 텍스처는 16×16 PNG. 경로 기준은 `src/main/resources/assets/alchemia/textures/`.
- "틴트" 표시는 회색조 1장을 코드에서 색만 입혀 돌려 쓰는 방식 (원본도 같은 방식). 장수를 줄일 수 있다.
- "제공"은 MoonScenty가 직접 제작해 `Desktop/thaumref/textures/`에 둔 원본 PNG를 뜻한다.

## A. 광석·결정 — 완료 (48개 파일)

생성 스크립트: `Desktop/thaumref/tools/gen_textures_step1a.py`. 색을 바꾸려면 스크립트의 값을 고쳐 다시 실행한다.

| # | 결과 파일 | 제작 방식 (MoonScenty 지정) |
| --- | --- | --- |
| A1 | `block/amber_ore`, `block/deepslate_amber_ore` | 바닐라 `iron_ore` / `deepslate_iron_ore`의 철 부분만 주황색으로 색조 변환 |
| A2 | `block/cinnabar_ore`, `block/deepslate_cinnabar_ore` | 같은 방식, 검붉은색 |
| A3 | `block/crystal/<상>_crystal_stage0~3` (7종 × 4단계 = 28장) | 바닐라 자수정 싹 3종 + 군집을 상별 색으로 색조 변환 |
| A4 | `item/amber` | 제공 `amber.png` 그대로 |
| A5 | `item/quicksilver` | 제공 `quicksilver.png` 그대로 |
| A6 | `item/<상>_shard` (7장) | 제공 `shard.png`를 상별 색으로 색조 변환 |
| A7 | `item/balanced_shard` + `.mcmeta` | `shard.png` 기반 24프레임, 대각선으로 흐르는 밝은 무지개 + 지나가는 반짝임 (frametime 2, interpolate) |
| A8 | `item/raw_cinnabar` | 제공 `raw_cinnabar.png` 그대로 |
| A9 | `item/iron_cluster` | 제공 `gold_cluster.png`의 금색 부분을 철 색으로 |
| A10 | `item/gold_cluster` | 제공 파일 그대로 |
| A11 | `item/cinnabar_cluster` | 금색 부분을 검붉은색으로 |
| — | `item/copper_cluster` | 금색 부분을 구리색으로 |

상별 색: 공기 노랑, 불 주홍, 물 하늘, 땅 초록, 질서 은백, 엔트로피 암회색, 플럭스 보라.
제외: 주석/은/납 군집·조각 (바닐라에 없는 금속).

## B. 금속·기본 재료 — 완료 (13개 파일)

생성 스크립트: `Desktop/thaumref/tools/gen_textures_step1b.py`. 금속 색은 스크립트 상단의 `ALCHEMIUM`, `BRASS`, `IRON` 값으로 조절한다.

| # | 결과 파일 | 제작 방식 (MoonScenty 지정) |
| --- | --- | --- |
| B1 | `block/alchemium_block` | 제공 `thaumium_block.png` 그대로 |
| B2 | `block/brass_block` | 같은 파일을 황동색으로 색조 변환 (원본이 어두워 밝기 범위도 끌어올림) |
| B3~B4 | `item/alchemium_ingot`, `item/brass_ingot` | 제공 `alloy_ingot.png` 색조 변환 |
| B5~B6 | `item/alchemium_nugget`, `item/brass_nugget` | 제공 `alloy_nugget.png` 색조 변환 |
| B7 | `item/quicksilver_drop` | 제공 `quicksilver_drop.png` 그대로 |
| B8~B9 | `item/brass_gear`, `item/alchemium_gear` | 제공 `alloy_gear.png` 색조 변환 |
| B12 | `item/alchemium_plate` | 제공 `alloy_plate.png` 색조 변환 |
| B10~B11 | `item/brass_plate`, `item/iron_plate` | 제공 `alloy_plate2.png` 색조 변환 |
| B13 | `item/salis_mundus` | 제공 `material_dust.png`를 흰 가루 바탕 + 음영 쪽에 은은한 보라~분홍빛으로 변환 (정지 이미지) |

금속 색: 알케미움 = 블록과 같은 짙은 보라, 황동 = 따뜻한 황금색, 철 = 거의 무채색.
뒤 단계로 미룸: 공허 금속 계열(13단계 전후), 수지(tallow)·마법 천(fabric)은 각각 양초·로브 단계에서.

## C. 나무 2종 — 완료 (10개 파일)

생성 스크립트: `Desktop/thaumref/tools/gen_textures_step1c.py`. 잎 색은 텍스처에 직접 입혔으므로 바이옴 색조(tint)를 받지 않는다.

| # | 결과 파일 | 제작 방식 (MoonScenty 지정) |
| --- | --- | --- |
| C1~C3 | `block/greatwood_log`, `_log_top`, `_planks` | 제공 `gwood_side.png`, `gwood_top.png`, `gwood_plank.png` 그대로 |
| C4 | `block/greatwood_leaves` | 바닐라 짙은 참나무 잎을 더 어두운 녹색으로 |
| C5 | `block/greatwood_sapling` | 짙은 참나무 묘목의 잎 부분만 더 어두운 녹색으로 (줄기는 그대로) |
| C6~C8 | `block/silverwood_log`, `_log_top`, `_planks` | 제공 `swood_side.png`, `swood_top.png`, `swood_plank.png` 그대로 |
| C9 | `block/silverwood_leaves` | 짙은 참나무 잎을 `#1f416c` 계열로 |
| C10 | `block/silverwood_sapling` | 짙은 참나무 묘목의 줄기는 `#cac4b2` 계열, 잎은 `#1f416c` 계열로 |

계단·반 블록은 판자 텍스처를 그대로 쓴다. 껍질 벗긴 원목, 문·다락문 세트는 만들지 않았다 (필요해지면 추가).

## D. 식물 3종 — 완료 (3개 파일)

MoonScenty가 직접 제작해 전달. 그대로 사용한다.

| # | 결과 파일 | 비고 |
| --- | --- | --- |
| D1 | `block/shimmerleaf` | 반짝잎. 밝기 6 |
| D2 | `block/cinderpearl` | 재진주. 밝기 7 |
| D3 | `block/vishroom` | 비스버섯. 밝기 6 |

아이템 아이콘은 블록 텍스처를 그대로 쓴다.

## E. 석재·장식 — 완료 (4개 파일)

MoonScenty가 직접 제작해 전달. 계단·반 블록은 바탕 블록과 같은 텍스처를 쓴다.

| # | 결과 파일 | 제공 파일명 |
| --- | --- | --- |
| E1 | `block/arcane_stone` | `arcane_stone.png` |
| E2 | `block/arcane_stone_bricks` | `arcane_stone_bricks.png` |
| E3 | `block/amber_block` | `amber_block.png` |
| E4 | `block/amber_bricks` | `amber_block_bricks.png` |

뒤 단계로 미룸: 고대 석재·엘드리치 석재·글리프 석재 등(13단계), 포장석·기둥·탁자·양초·깃발(각 기능 단계).

## 합계

| 구분 | 파일 수 |
| --- | --- |
| A. 광석·결정 | 48 |
| B. 금속·재료 | 13 |
| C. 나무 | 10 |
| D. 식물 | 3 |
| E. 석재 | 4 |
| **계** | **78** (`.mcmeta` 1개 포함) |

## 남은 항목

1단계 텍스처는 모두 끝났다. 다음 단계에서 새로 필요한 텍스처는 그때 목록을 만들어 확인받는다.
