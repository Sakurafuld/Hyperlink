# Hyperlink
MinecraftForge

ハイパー桁外れなアイテムとボスを追加するMod

# ↓Config(すべて1.20.1でのデフォルトの値)

- Config warning = true
  - バージョン2.0でコンフィグファイルが統合されたことをログイン時に警告するか

## Muteki

- Enable Muteki = true
  - ムテキスターを機能させるのか
- Hyper Muteki = false
  - ムテキスターがノベルカリバーの攻撃を完全に弾くか
- Muteki command = true
  - ムテキスターがコマンドの対象にならないようにするのか

## Novel

- Enable Novel = true
  - ノベルカリバーを機能させるのか
- Ignore entities = [ "minecraft:item", "minecraft:experience_orb", "hyperdaimc:fumetsu_skull", "hyperdaimc:fumetsu_storm", "hyperdaimc:fumetsu_storm_skull" ]
  - ノベルカリバーを無視するエンティティのリスト
- Special entities = [ "minecraft:ender_dragon", "draconicevolution:draconic_guardian", "cataclysm:ender_guardian", "cataclysm:netherite_monstrosity", "cataclysm:ignis", "cataclysm:the_harbinger", "cataclysm:the_prowler", "cataclysm:coralssus", "cataclysm:amethyst_crab", "cataclysm:ancient_remnant", "cataclysm:wadjet", "cataclysm:maledictus", "cataclysm:aptrgangr" ]
  - 死亡処理を中断しないエンティティのリスト(撃破演出を持っているなど)
- Invert Novel control = false
  - スニーク時の操作挙動を反転させるか(デフォルトではスニークすると単体攻撃)

## Chronicle

- Enable Chronicle = true
  - クロニクロックを機能させるのか
- Hyper Chronicle = false
  - クロニクロックがピックドクスの破壊を完全に弾くか
- Pause owner = false
  - 保護した本人が影響を受けるか
- Pause interaction = false
  - アドベンチャーモードのようにブロックへのインタラクトを防ぐか
- Selection size = 16384
  - 一度に設定できる保護の最大範囲
- Invert Chronicle control = false
  - スニーク時の操作挙動を反転させるか(デフォルトではスニークすると面でオフセットされた座標を選ぶ)

## Paradox

- Enable Paradox = true
  - ピックドクスを機能させるのか
- Fluid Paradox = true
  - 液体を破壊できるか
- Invert Paradox control = false
  - スニーク時の操作挙動を反転させるか(デフォルトではスニークすると1ブロックずつ破壊)

## VRX

- Enable VRX = true
  - V.R.X.を機能させるのか
- Keep VRX = true
  - 自身を対象としたV.R.X.が死亡後も保たれるか
- Create for others = false
  - 他のプレイヤーへV.R.X.を設定できるか
- Just Enough VRX = true
  - V.R.X.のセットアップをJEIから行えるか
- Seal Hyperlink = true
  - V.R.X.にHyperlinkのアイテムを設定できなくするか

## Fumetsu

- Enable recipes = true
  - ケミカルMAXとフメツドクロの特殊な醸造レシピを有効化するのか
- Enable summoning = true
  - ブロックを組み立ててフメツウザーを召喚できるか
- Max health = 20
  - 最大体力
- Search range = 128
  - ターゲットを探す範囲
- Search underground = false
  - プレイヤー以外のMobでも地下にいる相手をターゲットするのか

## Materializer

- Process time = 6000
  - 加工時間
- Stack same ingredients = false
  - レシピに同じ材料がある時それをスタックさせるのか(デフォルトでは全ての材料は一つづつ抽出される)
- Materializer recipe types = [ "minecraft:crafting", "minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking", "minecraft:smithing", "hyperdaimc:desk", "avaritia:crafting_table_recipe", "avaritia:compressor_recipe", "avaritia:extreme_smithing_recipe" ]
  - Z-MAX マテリアライザーが材料を遡るレシピタイプのリスト
- Materializer fuels = [ "hyperdaimc:god_sigil=64" ]
  - Z-MAX マテリアライザーの燃料として使えるアイテムとその使用回数のリスト
- Recipe blacklist = [ "hyperdaimc:nether_star", "hyperdaimc:desk", "hyperdaimc:game_orb", "hyperdaimc:hyper/muteki", "hyperdaimc:hyper/novel", "hyperdaimc:hyper/chronicle", "hyperdaimc:hyper/paradox", "hyperdaimc:hyper/vrx" ]
  - 材料を遡る時に無視する個別のレシピのリスト
- Tag blacklist = [ "forge:ingots", "forge:gems", "forge:storage_blocks", "forge:nuggets", "hyperdaimc:essences", "hyperdaimc:cores", "hyperdaimc:gists", "tconstruct:anvil_metal" ]
  - 材料を遡る時に無視するアイテムタグのリスト
