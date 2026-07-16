
package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private boolean isEnabled = false;

    @Override
    public void onInitializeClient() {
        // "M" Tuşunu oyuna entegre ediyoruz
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fastbow.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M, 
            "category.fastbow"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // M Tuşuna basıldığında açıp kapatma kontrolü
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                String status = isEnabled ? "§aAÇIK" : "§cKAPALI";
                client.player.sendMessage(Text.literal("Hızlı Ok Atma: " + status), true);
            }

            // Eğer mod aktifse ve oyuncu sağ tıka basılı tutuyorsa
            if (isEnabled && client.options.useKey.isPressed()) {
                // Oyuncunun elinde yay olup olmadığını kontrol ediyoruz
                if (client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.BOW) || 
                    client.player.getStackInHand(Hand.OFF_HAND).isOf(Items.BOW)) {
                    
                    // Sunucuya "yay bırakıldı" (Release) paketini gönderiyoruz.
                    // Bu sayede yay gerildiği an beklemeden oku fırlatır.
                    client.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket(
                        net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                        net.minecraft.util.math.BlockPos.ORIGIN,
                        net.minecraft.util.math.Direction.DOWN
                    ));
                    
                    // Yayın kullanımını sıfırlıyoruz ki hemen bir sonraki oku germeye başlasın
                    client.player.stopUsingItem();
                }
            }
        });
    }
}
