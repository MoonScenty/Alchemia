# 크레딧

Alchemia의 **코드**는 [MIT](LICENSE.md)입니다. 아래 에셋은 각자의 라이선스를 따릅니다.

## 상 아이콘 — CC BY 3.0

`assets/alchemia/textures/aspect/`의 상 아이콘 35종은 [game-icons.net](https://game-icons.net)의 **Lorc**가 만든 아이콘입니다.

- 라이선스: [Creative Commons Attribution 3.0 Unported (CC BY 3.0)](https://creativecommons.org/licenses/by/3.0/)
- 변경 사항: 32×32로 축소하고 흰색/투명으로 단색화했습니다.

> Icons made by Lorc. Available on https://game-icons.net

같은 아이콘이 Thaumcraft에도 쓰였습니다. 원작자가 CC BY 3.0으로 공개한 것을 각자 가져다 쓴 것이며, Thaumcraft에서 가져온 것이 아닙니다.

`background.png`와 `unknown.png`는 이 저장소에서 직접 만든 것으로 MIT입니다.

## 연구 노드 판 — 바닐라 마인크래프트

`assets/alchemia/textures/gui/sprites/research/`의 여섯 장은 바닐라 마인크래프트 1.21.1의 업적 프레임 스프라이트를 그대로 추출한 것입니다.

| 파일 | 원본 |
|---|---|
| `node_plain.png` / `node_plain_done.png` | `advancements/task_frame_unobtained` / `_obtained` |
| `node_special.png` / `node_special_done.png` | `advancements/goal_frame_unobtained` / `_obtained` |
| `node_major.png` / `node_major_done.png` | `advancements/challenge_frame_unobtained` / `_obtained` |

`textures/gui/research_table.png`의 아래쪽 인벤토리 격자(0,166 기준 184×88)도 바닐라 `gui/container/inventory.png`의 (0,76) 영역입니다. 같은 자리에 원작도 바닐라 격자를 썼습니다. 그 위의 본체는 MoonScenty가 직접 그린 것입니다.

Mojang의 에셋이므로 이 저장소의 MIT 라이선스가 적용되지 않습니다. Minecraft EULA를 따릅니다.

## 연구 화면 배경

`textures/gui/research_background/`의 여섯 장은 MoonScenty가 준비한 이미지를 1024×1024로 리사이즈한 것입니다. 분기별 색은 원작이 쓰던 색을 따라갔습니다 — 기초 청록, 아르카나 자홍, 연금술 녹색·호박, 장치 짙은 파랑, 골레마니 진홍, 엘드리치 보라.

`textures/gui/research_overlay.png`는 이 저장소에서 값 노이즈와 별점으로 생성한 것으로 MIT입니다.

## 노드

`textures/entity/node_core.png`(2048×64, 64px 프레임 32장)와 `node_halo.png`(1024×64, 16장)는 이 저장소에서 절차 생성한 것으로 MIT입니다. 생성기는 `thaumref/tools/gen_nodes.py`에 있습니다.

둘 다 회색조이고, 색은 노드가 품은 상의 색으로 코드에서 입힙니다. 그래서 상 35종과 노드 7종에 대해 프레임을 따로 만들 필요가 없습니다.

`textures/entity/node_bubble.png`도 이 저장소에서 절차 생성한 것입니다 — 구면 조명과 비눗막 간섭색을 계산해 그렸습니다.

노드 안정기의 모델(`models/block/node_stabilizer/*.obj`)과 텍스처(`block/node_stabilizer_top.png`, `block/node_stabilizer_piston.png`)는 MoonScenty가 Blockbench로 제작했습니다.

## 그 밖의 텍스처

나머지 텍스처는 MoonScenty가 직접 제작했거나, 바닐라 마인크래프트 텍스처를 색조 변환해 만든 것입니다. 제작 방식은 [docs/textures/](docs/textures/)에 기록돼 있습니다.

Thaumcraft의 에셋은 이 저장소에 포함되어 있지 않습니다.
