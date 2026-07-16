package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private boolean isEnabled = false;

    // Otomatik yay döngüsü için değişkenler
    private boolean isAutoUsing = false;
    private int bowTickCounter = 0;
    
    // Yayın kaç tick boyunca gerileceğini belirler. 
    // 5 tick çok hızlı atış yapar (Anti-cheat ban yememek için idealdir).
    // Tam hasarlı ok atmak istersen bu sayıyı 10 yapabilirsin.
    private final int CHARGE_TICKS = 5; 

    @Override
    public void onInitializeClient() {
        System.out.println("FASTBOW MODU YUKLENDI VE CALISTI!");

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fastbow.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.fastbow"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // M Tuşuna basıldığında modu aç/kapat
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                String status = isEnabled ? "§aAÇIK" : "§cKAPALI";
                client.player.sendMessage(Text.literal("Hızlı Ok Atma: " + status), true);

                // Eğer mod kapatıldıysa yayı bırakıp durumu sıfırla
                if (!isEnabled && isAutoUsing) {
                    stopBow(client);
                }
            }

            // Mod kapalıysa aşağıdaki otomatik işlemleri yapma
            if (!isEnabled) return;

            // Oyuncunun elinde yay var mı kontrol et
            boolean holdingBow = client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.BOW) ||
                                 client.player.getStackInHand(Hand.OFF_HAND).isOf(Items.BOW);

            if (holdingBow) {
                // Eğer yay kullanmaya başlamadıysak, otomatik sağ tık basılmış gibi yayı germeye başla
                if (!isAutoUsing) {
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    isAutoUsing = true;
                    bowTickCounter = 0;
                } else {
                    // Yay geriliyorsa tick sayacını artır
                    bowTickCounter++;

                    // Yeterli kadar (5 tick) gerildiyse yayı bırakıp oku fırlat
                    if (bowTickCounter >= CHARGE_TICKS) {
                        // ÖNEMLİ: Sunucuya "yay bırakıldı" paketini gönderiyoruz ki ok fırlasın
                        client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN,
                            Direction.DOWN
                        ));
                        
                        // İstemcide de yayı bırakma işlemini bitir
                        client.player.stopUsingItem();
                        
                        // Döngüyü başa sarmak için sıfırla
                        isAutoUsing = false;
                    }
                }

                // Güvenlik kontrolü: Oyuncu envanter açarsa veya başka bir şey yaparsa döngüyü sıfırla
                if (isAutoUsing && !client.player.isUsingItem()) {
                    isAutoUsing = false;
                    bowTickCounter = 0;
                }
            } else {
                // Eğer oyuncu elinden yayı bıraktıysa ama mod açıksa, kullanım durumunu sıfırla
                if (isAutoUsing) {
                    stopBow(client);
                }
            }
        });
    }

    // Yayı bırakma ve sıfırlama işlemleri için yardımcı metod
    private void stopBow(net.minecraft.client.MinecraftClient client) {
        client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
            PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
            BlockPos.ORIGIN,
            Direction.DOWN
        ));
        client.player.stopUsingItem();
        isAutoUsing = false;
        bowTickCounter = 0;
    }
}
