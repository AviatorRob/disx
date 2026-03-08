package xyz.ar06.disx.fabric;

import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.ar06.disx.DisxModInfo;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.config.DisxConfigHandler;

public class DisxBehaviorHandlingFabric {

    static String[] debugKeys = DisxModInfo.getDebugKeys();
    public static boolean chatListener(String s) {
        if (s.equalsIgnoreCase(debugKeys[0])){
            boolean currentValue = Boolean.parseBoolean(DisxConfigHandler.CLIENT.getProperty("debug_mode"));
            if (!currentValue){
                DisxConfigHandler.CLIENT.updateProperty("debug_mode", "true");
                DisxSystemMessages.debugStatus(true);
                return false;
            }
        }
        if (s.equalsIgnoreCase(debugKeys[1])){
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
