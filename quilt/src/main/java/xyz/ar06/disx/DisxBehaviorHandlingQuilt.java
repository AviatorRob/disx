package xyz.ar06.disx;

import net.minecraft.network.chat.Component;
import xyz.ar06.disx.config.DisxConfigHandler;

public class DisxBehaviorHandlingQuilt {
    static String debugChatKeyFallback = "vegan vegan vegan";
    static String undebugChatKeyFallback = "steak steak steak";
    public static boolean chatListener(String s) {
        String debugChatKey = Component.translatableWithFallback("key.disx.debug", debugChatKeyFallback).getString();
        String undebugChatKey = Component.translatableWithFallback("key.disx.undebug", undebugChatKeyFallback).getString();
        if (s.equalsIgnoreCase(debugChatKey)){
            boolean currentValue = Boolean.parseBoolean(DisxConfigHandler.CLIENT.getProperty("debug_mode"));
            if (!currentValue){
                DisxConfigHandler.CLIENT.updateProperty("debug_mode", "true");
                DisxSystemMessages.debugStatus(true);
                return false;
            }
        }
        if (s.equalsIgnoreCase(undebugChatKey)){
            boolean currentValue = Boolean.parseBoolean(DisxConfigHandler.CLIENT.getProperty("debug_mode"));
            if (currentValue){
                DisxConfigHandler.CLIENT.updateProperty("debug_mode", "false");
                DisxSystemMessages.debugStatus(false);
                return false;
            }
        }
        return true;
    }
}
