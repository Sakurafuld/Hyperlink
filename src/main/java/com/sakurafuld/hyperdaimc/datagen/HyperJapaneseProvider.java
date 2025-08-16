package com.sakurafuld.hyperdaimc.datagen;

import com.google.common.collect.Maps;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullWallBlock;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class HyperJapaneseProvider extends LanguageProvider {
    private static final Map<String, String> MAP = Util.make(Maps.newHashMap(), map -> {
        map.put("god", "神");
        map.put("sigil", "のしるし");
        map.put("bug", "バグ");
        map.put("star", "スター");
        map.put("zwei", "II");
        map.put("drei", "III");
        map.put("essence", "のエッセンス");
        map.put("ground", "地面");
        map.put("crust", "地殻");
        map.put("mineral", "ミネラル");
        map.put("herb", "ハーブ");
        map.put("tree", "木");
        map.put("marine", "海");
        map.put("food", "食べ物");
        map.put("motion", "移動");
        map.put("partition", "隔壁");
        map.put("light", "光");
        map.put("shadow", "闇");
        map.put("battle", "戦い");
        map.put("sound", "音");
        map.put("work", "作業");
        map.put("drawing", "描画");
        map.put("core", "のコア");
        map.put("land", "大地");
        map.put("cave", "洞窟");
        map.put("forest", "樹海");
        map.put("garden", "お花畑");
        map.put("wind", "嵐");
        map.put("thunder", "雷");
        map.put("treasure", "お宝");
        map.put("flame", "火炎");
        map.put("frost", "氷雪");
        map.put("animal", "ケモノ");
        map.put("monster", "バケモノ");
        map.put("amusement", "遊び");
        map.put("order", "秩序");
        map.put("healing", "癒やし");
        map.put("echo", "反響");
        map.put("death", "死");
        map.put("wonder", "不思議");
        map.put("gist", "のジスト");
        map.put("contraption", "からくり");
        map.put("sky", "天");
        map.put("love", "愛");
        map.put("fear", "恐怖");
        map.put("adventure", "大冒険");
        map.put("taint", "穢れ");
        map.put("destruction", "破壊");
        map.put("leaping", "飛躍");
        map.put("fairy", "妖");
        map.put("quintessence", "のクインテッセンス");
        map.put("game", "ゲーム");
        map.put("orb", "オーブ");

        map.put("fumetsu", "フメツ");
        map.put("storm", "ストーム");
        map.put("squall", "デカガイコツ");
    });

    public HyperJapaneseProvider(DataGenerator gen) {
        super(gen, HYPERDAIMC, "ja_jp");
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.hyperdaimc.main", "Hyperlink");
        this.add("itemGroup.hyperdaimc.crafting", "Hyperlink-クラフト");

        HyperItems.REGISTRY.getEntries().stream()
                .filter(item -> !(item.get() instanceof BlockItem))
                .forEach(item -> this.addItem(item, defaultName(item.get())));
        HyperBlocks.REGISTRY.getEntries().stream()
                .filter(block -> !(block.get() instanceof FumetsuSkullWallBlock))
                .forEach(block -> this.addBlock(block, defaultName(block.get())));
        HyperEntities.REGISTRY.getEntries()
                .forEach(entity -> this.addEntityType(entity, defaultName(entity.get())));

        this.add("subtitles.hyperdaimc.muteki_equip", "パッカーーン！！！");
        this.add("subtitles.hyperdaimc.novelize", "クリティカルディスティニー！！");
        this.add("subtitles.hyperdaimc.chronicle_select", "ポーズセレクト");
        this.add("subtitles.hyperdaimc.chronicle_pause", "ポーズ！！");
        this.add("subtitles.hyperdaimc.chronicle_restart", "リスタート！！");
        this.add("subtitles.hyperdaimc.perfect_knockout", "コンボ！！");
        this.add("subtitles.hyperdaimc.vrx_open", "V.R.X.を開く");
        this.add("subtitles.hyperdaimc.vrx_create", "V!R!X!");
        this.add("subtitles.hyperdaimc.vrx_erase", "X!R!V!");
        this.add("subtitles.hyperdaimc.fumetsu_ambient", "フメツウィザーが怒る");
        this.add("subtitles.hyperdaimc.fumetsu_hurt", "フメツウィザーが攻撃される");
        this.add("subtitles.hyperdaimc.fumetsu_shoot", "フメツウィザーがガイコツを放つ");
        this.add("subtitles.hyperdaimc.fumetsu_storm", "フメツストームが炸裂する");
        this.add("subtitles.hyperdaimc.chemical_maximization", "ソウルが凝固していく");
        this.add("subtitles.hyperdaimc.soul", "ソウルが脈動する");

        this.add("tooltip.hyperdaimc.muteki", "流星のごとく輝け！");
        this.add("tooltip.hyperdaimc.novel", "俺の言う通りのストーリー！");
        this.add("tooltip.hyperdaimc.chronicle", "時は今こそ極まれり！");
        this.add("tooltip.hyperdaimc.paradox", "交差する強さ連鎖！");
        this.add("tooltip.hyperdaimc.vrx", "天地創造ゲットメイク！");
        this.add("tooltip.hyperdaimc.vrx.face", "%s方向の内容");
        this.add("tooltip.hyperdaimc.vrx.face.empty", " -無いようです");
        this.add("tooltip.hyperdaimc.vrx.indexes", "%3$s %1$s / %2$s %4$s");
        this.add("tooltip.hyperdaimc.vrx.left", "←左クリック");
        this.add("tooltip.hyperdaimc.vrx.right", "右クリック→");
        this.add("tooltip.hyperdaimc.face.null", "なし");
        this.add("tooltip.hyperdaimc.face.down", "下");
        this.add("tooltip.hyperdaimc.face.up", "上");
        this.add("tooltip.hyperdaimc.face.north", "北");
        this.add("tooltip.hyperdaimc.face.south", "南");
        this.add("tooltip.hyperdaimc.face.west", "西");
        this.add("tooltip.hyperdaimc.face.east", "東");
        this.add("tooltip.hyperdaimc.vrx.player", "現在のV.R.X.の内容");
        this.add("tooltip.hyperdaimc.vrx.energy", "底なしのエネルギー");
        this.add("tooltip.hyperdaimc.vrx.energy.description", "ForgeEnergy, RedstoneFlux, Jouleなど");
        this.add("tooltip.hyperdaimc.desk.minecrafting", "マインしてクラフトだ！");
        this.add("tooltip.hyperdaimc.desk.lock", "クリックでレシピをロック");
        this.add("tooltip.hyperdaimc.desk.unlock", "シフトクリックでレシピをクリア");
        this.add("tooltip.hyperdaimc.desk.animation", "Ctrl+Altを押している間はアニメーションが再生されません");
        this.add("tooltip.hyperdaimc.god_sigil", "神の恵みを受け取れぇ！");
        this.add("tooltip.hyperdaimc.chemical_max", "ゾンビに投げよう");

        this.add("container.hyperdaimc.vrx.face", "方向: %s");

        this.add("chat.hyperdaimc.config_warning", "バージョン2.0になり、hyperdaimc-server.tomlコンフィグファイルはhyperdaimc-common.tomlに統合されました\nこの警告はhyperdaimc-common.toml内で無効化することができます");
        this.add("chat.hyperdaimc.chronicle.conflict", "選択範囲は既に存在しています");
        this.add("chat.hyperdaimc.chronicle.too_large", "選択範囲が大きすぎます");

        this.add("death.attack.novel.0", "%sは無くなった");
        this.add("death.attack.novel.1", "%sはアリになって踏み潰された");
        this.add("death.attack.novel.2", "%sは塵になって吹き飛ばされた");
        this.add("death.attack.novel.3", "%sは葉っぱになって引き裂かれた");
        this.add("death.attack.novel.4", "%sはガラスになって砕け散った");
        this.add("death.attack.novel.5", "%sは炎となって燃え尽きた");
        this.add("death.attack.novel.6", "%sはからっぽになった");

        this.add("argument.muteki.notfound", "§6ムテキ§e状態§cで無いエンティティが見つかりませんでした");

        this.add("permissions.requires.muteki", "このコマンドを実行するためには§6ムテキ§e状態§cで無いエンティティが必要です");

        // 連携.
        this.add("curios.identifier.maximum", "マキシマム");

        this.add("recipe.hyperdaimc.desk", "ゲーマクラフト");
        this.add("recipe.hyperdaimc.materializer", "マテリアライズ");

        this.add("information.hyperdaimc.fumetsu_wither.0", """
                %2$s、%3$s、%4$s、%5$sをいい感じに配置し、%1$sを持ちながらシフト右クリックすることで%7$sを召喚することが出来る
                %7$sは基本的に中立であり、ダメージを受けることが無ければ敵対することはない (そして絶対に敵対してはならない)
                そのままあなたが、ゲームからログアウトする・ディメンションを移動する・死ぬなどすれば、%7$sはその場に%6$sを残してこのワールドからいなくなる""");

        this.add("tooltip.hyperdaimc.materializer.fuel", "使用回数: %s");

        this.add("modifier.hyperdaimc.novel", "マイティノベル X");
        this.add("modifier.hyperdaimc.novel.flavor", "なぜ君がぁ、、、！");
        this.add("modifier.hyperdaimc.novel.description", "全ての攻撃が相手を即死させる");
    }

    private String specialize(IForgeRegistryEntry<?> entry, String name) {
        if (entry == HyperItems.MUTEKI.get()) {
            return "ムテキスター";
        }
        if (entry == HyperItems.NOVEL.get()) {
            return "ノベルカリバー";
        }
        if (entry == HyperItems.CHRONICLE.get()) {
            return "クロニクロック";
        }
        if (entry == HyperItems.PARADOX.get()) {
            return "ピックドクス";
        }
        if (entry == HyperItems.VRX.get()) {
            return "V.R.X.";
        }
        if (entry == HyperItems.FUMETSU.get()) {
            return "フメツウィザーのスポーンエッグ";
        }
        if (entry == HyperBlocks.DESK.get()) {
            return "ゲーマクラフター";
        }
        if (entry == HyperBlocks.SOUL.get()) {
            return "ボーンソウル";
        }
        if (entry == HyperBlocks.MATERIALIZER.get()) {
            return "Z-MAX マテリアライザー";
        }
        if (entry == HyperEntities.FUMETSU.get()) {
            return "フメツウィザー";
        }
        if (name.contains("chemical_max")) {
            name = name.replaceAll("chemical_max", "ケミカルMAX");
        }
        if (name.contains("storm_skull")) {
            name = name.replaceAll("storm_skull", "デカガイコツ");
        }
        if (name.contains("skull")) {
            if (entry instanceof Block) {
                name = name.replaceAll("skull", "ドクロ");
            } else {
                name = name.replaceAll("skull", "ガイコツ");
            }
        }
        if (name.contains("fumetsu_right")) {
            name = name.replaceAll("fumetsu_right", "右フメツ");
        }
        if (name.contains("fumetsu_left")) {
            name = name.replaceAll("fumetsu_left", "左フメツ");
        }
        return name;
    }

    private String defaultName(IForgeRegistryEntry<?> entry) {
        if (entry.getRegistryName() == null) {
            return "";
        }

        String special = specialize(entry, entry.getRegistryName().getPath());

        return Arrays.stream(special.split("_")).map(s -> {
            if (MAP.containsKey(s)) {
                return MAP.get(s);
            }
            char splinter = Character.toUpperCase(s.charAt(0));
            return splinter + s.substring(1);
        }).collect(Collectors.joining());
    }
}
