package cn.aethli.redisSucker.subscribe;

import cn.aethli.redisSucker.model.SSEMessage;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author 93162*
 */
public class SSEManager {
    public static synchronized Map<String, Queue<SSEMessage>> addTheme(String themeName) {
        Map<String, Queue<SSEMessage>> idMessageMap = THEME_ID_MESSAGE.get(themeName);
        if (idMessageMap != null) {
            return idMessageMap;
        } else {
            Map<String, Queue<SSEMessage>> themeMap = new ConcurrentHashMap<>();
            THEME_ID_MESSAGE.put(themeName, themeMap);
            return themeMap;
        }

    }

    public static synchronized Queue<SSEMessage> addId(String themeName, String id) {
        Map<String, Queue<SSEMessage>> idMessageMap = addTheme(themeName);
        Queue<SSEMessage> idMessageQueue = idMessageMap.get(id);
        if (idMessageQueue != null) {
            return idMessageQueue;
        } else {
            Queue<SSEMessage> sseMessageQueue = new ConcurrentLinkedDeque<>();
            idMessageMap.put(id, sseMessageQueue);
            return sseMessageQueue;
        }

    }

    public static void releaseMessageById(String themeName, String id, SSEMessage message) {
        Queue<SSEMessage> messageList = addId(themeName, id);
        messageList.offer(message);
    }

    public static Queue<SSEMessage> readMessageByThemeId(String themeName, String id) {
        return addId(themeName, id);
    }

    private static final Map<String, Map<String, Queue<SSEMessage>>> THEME_ID_MESSAGE = new ConcurrentHashMap<>();
}
