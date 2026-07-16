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
    private int tickCounter = 0;
    private boolean wasForcedPressed = false;

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

            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                String status = isEnabled ? "§aAÇIK" : "§cKAPALI";
                client.player.sendMessage(Text.literal("Hızlı Ok Atma: " + status), true);
                
                if (!isEnabled && wasForcedPressed) {
                    client.options.useKey.setPressed(false);
                    wasForcedPressed = false;
                }
            }

            if (isEnabled) {
                if (client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.BOW)) {
                    if (!client.player.isUsingItem()) {
                        client.options.useKey.setPressed(true);
                        wasForcedPressed = true;
                        tickCounter = 0;
                    } else {
                        tickCounter++;
                        if (tickCounter >= 3) {
                            client.options.useKey.setPressed(false);
                            wasForcedPressed = false;
                            tickCounter = 0;
                        }
                    }
                } else if (wasForcedPressed) {
                    client.options.useKey.setPressed(false);
                    wasForcedPressed = false;
                }
            }
        });
    }
}
