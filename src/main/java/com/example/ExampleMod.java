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

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fastbow.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.fastbow"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                String status = isEnabled ? "§aAÇIK" : "§cKAPALI";
                client.player.sendMessage(Text.literal("Hızlı Ok Atma: " + status), true);
            }

            if (!isEnabled) return;

            boolean holdingBow = client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.BOW) ||
                                 client.player.getStackInHand(Hand.OFF_HAND).isOf(Items.BOW);

            if (holdingBow && client.options.useKey.isPressed()) {
                // Yay kullanım süresini zorla 72000 tick'te (tam gerilmiş) kabul ettir
                // Önce use başlat, hemen release paketi gönder
                if (client.player.getItemUseTime() <= 0 && !client.player.isUsingItem()) {
                    // Sağ tık başlatma paketini gönder (Minecraft bunu normalde yapar ama emin olmak için)
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                } else if (client.player.getItemUseTime() > 0) {
                    // Yay bırakma paketini gönder, bu oku fırlatır
                    client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        Direction.DOWN
                    ));
                    client.player.stopUsingItem();
                }
            }
        });
    }
}
