# 9단계 텍스처 목록 — 장비와 포커스

9단계에 들어가기 전에 확인받아야 할 그림 목록이다 (`PORTING_CONVENTIONS.md` 규칙).
**원본 에셋은 쓸 수 없다.** 여기 적힌 "원본" 파일명은 무엇을 옮기는 것인지 가리키는 이름일 뿐이고,
그림은 직접 그리거나 우리 것에서 파생시킨다.

수는 **총 68장**이고, 그중 **직접 그려야 할 것이 13장**, 나머지는 절차적으로 뽑을 수 있다.
한 번에 다 할 일이 아니라 아래 묶음 단위로 끊어 가는 것이 낫다.

---

## A. 방어구 — 아이템 그림 (16×16)

한 벌이 네 장(투구·가슴·다리·신발)이다. 로브류는 가슴·다리에 겹침 그림이 하나씩 더 붙는다.

| # | 파일 | 원본 | 만드는 법 |
|---|---|---|---|
| A1–A4 | `alchemium_helm/chest/legs/boots.png` | `thaumium_*` | **파생** — 우리 알케미움 주괴 색으로 바닐라 철 방어구에서 |
| A5–A8 | `void_helm/chest/legs/boots.png` | `void_*` | **파생** — 공허 색(`cap_void_mat`)으로 A1–A4에서 |
| A9–A11 | `fortress_helm/chest/legs.png` | `fortress_*` | **직접** — 요새 방어구는 생김새가 따로다 (신발 없음) |
| A12–A14 | `robe_helm/chest/legs.png` | `cloth_*` | **직접** — 천 로브 |
| A15–A16 | `robe_chest_over/legs_over.png` | `cloth_*_over` | **직접** — 로브 겹침 층 |
| A17–A19 | `void_robe_helm/chest/legs.png` | `void_robe_*` | **파생** — A12–A14를 공허 색으로 |
| A20–A21 | `void_robe_chest_over/legs_over.png` | `void_robe_*_over` | **파생** |
| A22 | `goggles_revealing.png` | 〃 | **직접** |
| A23 | `traveller_boots.png` | 〃 | **직접** |

진홍(crimson) 방어구 세 벌은 **11단계 몹과 함께** 오는 것이라 여기서 뺀다.

## B. 방어구 — 입은 모습 (갑옷 레이어)

`models/armor/<이름>_layer_1.png`(64×32)과 `_layer_2.png`(64×32) 두 장이 한 벌이다.

| # | 파일 | 만드는 법 |
|---|---|---|
| B1–B2 | `alchemium_layer_1/2.png` | **파생** — 바닐라 철 레이어에서 색만 |
| B3–B4 | `void_layer_1/2.png` | **파생** |
| B5–B6 | `fortress_layer_1/2.png` | **직접** |
| B7–B8 | `robes_layer_1/2.png` | **직접** |
| B9–B10 | `robes_overlay_layer_1/2.png` | **직접** |
| B11–B12 | `void_robe_layer_1/2.png` | **파생** — B7–B8에서 |
| B13 | `goggles_layer_1.png` | **직접** |
| B14 | `traveller_boots_layer_1.png` | **직접** |

## C. 도구 (16×16)

| # | 파일 | 만드는 법 |
|---|---|---|
| C1–C5 | `alchemium_pick/axe/shovel/sword/hoe.png` | **파생** — 바닐라 철 도구에서 알케미움 색으로 |
| C6–C10 | `void_pick/axe/shovel/sword/hoe.png` | **파생** |
| C11–C15 | `elemental_pick/axe/shovel/sword/hoe.png` | **직접** — 원소 도구는 생김새가 따로다 |
| C16 | `primal_crusher.png` | **직접** — 원본은 애니메이션이나 정지로 간다 |

## D. 포커스 (16×16)

**앞서 적었던 "밑바탕 한 장에 색을 입힌다"는 틀렸다.** 원본을 재어 보니 `focus.png`는 8×8짜리
빈 자리 표시이고, 열둘은 저마다 독립된 16×16 그림이며 거의 모든 화소가 칠해져 있다.
`ItemFocusBasic`의 `renderColor`는 그림에 입히는 색이 아니라 **완드에서 나가는 주문의 색**이다.
곧 밑바탕을 깔아 색만 바꾸는 수법은 여기서 쓸 수 없다.

| # | 파일 | 만드는 법 |
|---|---|---|
| D1–D12 | `wand/focus_fire/frost/shock/excavation/grapple/hellbat/pech/hole/primal/shard/trade/builder.png` | **절차적** — `tools/gen_foci.py`. **완료** |
| D13 | `item/focus_pouch.png` | **직접** — 직접 그렸다. **완료** |
| D14 | `gui/focus_pouch.png` | **절차적** — `tools/gen_pouch_gui.py`. **완료** |

주머니 화면은 앞서 목록에서 빠져 있던 것이다. 아이템 그림 한 장으로는 주머니를 열 수 없다.
`gen_pouch_gui.py`가 바닐라의 세 줄짜리 상자 화면(셜커 상자)에서 만든다. 틀도 회색도 아래쪽 소지품
칸도 그대로 쓰고, 아홉 줄짜리 칸 덩이만 지우고 여섯 개짜리로 다시 찍는다. 찍는 칸 자체가 같은
화면에서 떼어 온 것이라 아래 소지품 칸과 화소 단위로 맞는다. 칸 자리는 `x = 35 + 열 * 18`,
`y = 18 + 행 * 18`이고 열여덟 칸이 여섯 곱하기 셋으로 들어간다.

D1–D12는 `gen_foci.py`가 그린다. 열둘이 한 벌로 보이도록 **몸은 한 번만 그리고**(놋쇠 소켓에 물린
깎은 보석, 면을 평평하게 끊어 화소 그림답게) 종류마다 색과 표식만 달리한다. 표식은 9×9 격자에
선으로 그려 밝게 박아 넣고 아랫변에 그림자를 넣는다.

색은 원본이 각 포커스를 등록하며 넘긴 값을 그대로 쓴다. 불 `0xE55104`, 서리 `0x4F69CC`,
충격 `0x9FB3BF`, 발굴 `0x064006`, 갈고리 `0x1515FF`, 지옥박쥐 `0xDC3602`, 펙 `0x229944`,
구멍 `0x091429`, 원시 `0xA5A1C1`, 파편 `0x9929BD`, 등가교환 `0x857B93`, 건설 `0x85EB93`.
다만 **밝기는 가져오지 않는다.** 그대로 두면 구멍은 검댕, 건설은 흰 얼룩이 되어 표식이 묻힌다.
색상과 최소 채도만 가져와 열둘을 같은 밝기 띠에 맞췄다. 같은 밝기의 보석 열둘은 색상과 표식으로
서로를 구분하며, 한 줄로 늘어놓고 보는 물건에는 그편이 맞다.

원본에는 넷이 애니메이션이고(`focus_pech` 16컷, `focus_portablehole` 16컷, `focus_shard` 16컷,
`focus_primal` 30컷) `focus_hellbat_orn`은 덧그림 층이지만, 여기서는 정지 한 장으로 간다.
`focus_warding`은 5.2.4에 대응하는 아이템이 없어 뺀다.

## E. 포커스 업그레이드 아이콘 (16×16)

포커스 화면에 뜨는 작은 표식들이다. 원본에 23장이 있다.

| # | 파일 | 만드는 법 |
|---|---|---|
| E1–E23 | `foci/potency, frugal, treasure, enlarge, extend, silktouch, architect, alchemistsfire, alchemistsfrost, persistant, scattershot, seeker, sticky, dowsing, nightshade, fireball, firebeam, iceboulder, chainlightning, earthshock, batbombs, devilbats, vampirebats` | **직접**이 원칙이나, 대부분 단순 기호라 절차적으로 짤 수 있는 것도 있다 |

이 묶음은 **포커스 업그레이드를 실제로 구현할 때까지 미뤄도 된다.** 포커스 자체가 먼저다.

## F. 장신구 (16×16)

| # | 파일 | 만드는 법 |
|---|---|---|
| F1–F3 | `amulet_mundane/fancy/runic.png` | **직접** |
| F4–F6 | `ring_mundane/fancy/runic.png` | **직접** |
| F7–F9 | `girdle_mundane/fancy/runic.png` | **직접** |
| F10 | `ring_verdant.png` | **파생** — F5에서 |
| F11 | `girdle_hover.png` | **파생** — F8에서 |
| F12 | `thaumostatic_harness.png` | **직접** |

장신구는 **Curios 연동**이 필요하다(14단계에 적혀 있다). 그림보다 그쪽이 먼저 막힌다.

---

## 끊어 가는 순서 제안

1. **C1–C10 (알케미움·공허 도구 10장)** — 전부 파생이라 그릴 것이 없고, 9단계에서 가장 먼저 쓰인다
2. **A1–A8 + B1–B4 (알케미움·공허 방어구 12장)** — 마찬가지로 파생
3. **A22–A23, B13–B14 (고글과 여행자의 장화 4장)** — 직접 넷. 고글은 5단계 노드 읽기가 이미 태그로 자리를 비워 두었다
4. ~~**D1–D14 (포커스 열둘과 주머니 둘)**~~ — 마쳤다
5. **A9–A21, B5–B12 (요새·로브 방어구)** — 직접 여덟에 파생 여럿
6. **C11–C16, E, F** — 나중

1~4번은 마쳤다. 남은 것은 5·6번이다.
