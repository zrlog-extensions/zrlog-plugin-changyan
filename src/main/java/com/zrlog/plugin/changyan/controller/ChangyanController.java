package com.zrlog.plugin.changyan.controller;

import com.google.gson.Gson;
import com.zrlog.plugin.IOSession;
import com.zrlog.plugin.changyan.response.ChangyanComment;
import com.zrlog.plugin.changyan.response.CommentsEntry;
import com.zrlog.plugin.client.ClientActionHandler;
import com.zrlog.plugin.common.IdUtil;
import com.zrlog.plugin.common.LoggerUtil;
import com.zrlog.plugin.common.model.Comment;
import com.zrlog.plugin.common.model.PublicInfo;
import com.zrlog.plugin.data.codec.ContentType;
import com.zrlog.plugin.data.codec.HttpRequestInfo;
import com.zrlog.plugin.data.codec.MsgPacket;
import com.zrlog.plugin.data.codec.MsgPacketStatus;
import com.zrlog.plugin.render.SimpleTemplateRender;
import com.zrlog.plugin.type.ActionType;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChangyanController {

    private static final Logger LOGGER = LoggerUtil.getLogger(ChangyanController.class);
    private static final String CONFIG_KEYS = "appId,appKey,status,commentEmailNotify,callbackUrl";

    private final IOSession session;
    private final MsgPacket requestPacket;
    private final HttpRequestInfo requestInfo;
    private final Gson gson = new Gson();

    public ChangyanController(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        session.sendMsg(new MsgPacket(requestInfo.simpleParam(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(),
                ActionType.SET_WEBSITE.name()), msgPacket -> {
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            session.sendMsg(new MsgPacket(map, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
        });
    }

    public void info() {
        response(loadConfig());
    }

    public void index() {
        Map<String, Object> data = new HashMap<>();
        data.put("theme", isDarkMode() ? "dark" : "light");
        data.put("data", gson.toJson(pageData()));
        session.responseHtml("/templates/index", data, requestPacket.getMethodStr(), requestPacket.getMsgId());
    }

    public void json() {
        response(pageData());
    }

    public void widget() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "appId");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map map = gson.fromJson(msgPacket.getDataStr(), Map.class);
            String articleId = (String) requestInfo.simpleParam().get("articleId");
            if (Objects.isNull(articleId)) {
                articleId = "-1";
            }
            map.put("articleId", articleId);
            session.responseHtml("/widget", map, requestPacket.getMethodStr(), requestPacket.getMsgId());
        });

    }

    /**
     * 反向同步接口
     */
    public void sync() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "short_name,secret,status,commentEmailNotify,callbackUrl");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            Map<String, Object> changyan = gson.fromJson(msgPacket.getDataStr(), Map.class);
            String callbackUrl = (String) changyan.get("callbackUrl");
            String ignoreChar = "/p" + "/" + session.getPlugin().getShortName();
            try {
                if (callbackUrl != null && new URL(callbackUrl).getPath().replace(ignoreChar, "").equals(requestInfo.getUri().replace(".action", ""))) {
                    String commentJsonStr = requestInfo.getParam().get("data")[0];
                    LOGGER.info(commentJsonStr);
                    final ChangyanComment changyanComment = gson.fromJson(commentJsonStr, ChangyanComment.class);
                    Map<String, Object> response = new HashMap<>();
                    dealSyncRequest(response, changyanComment, "on".equals(changyan.get("commentEmailNotify")));
                } else {
                    session.sendMsg(ContentType.HTML, ClientActionHandler.ACTION_NOT_FOUND_PAGE, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
                session.sendMsg(ContentType.HTML, "Exception", requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        });

    }

    private Map<String, Object> pageData() {
        Map<String, Object> data = new HashMap<>();
        data.put("dark", isDarkMode());
        data.put("colorPrimary", getAdminColorPrimary());
        data.put("plugin", session.getPlugin());
        data.put("config", loadConfig());
        return successMap(data);
    }

    private Map<String, Object> loadConfig() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", CONFIG_KEYS);
        Map response = session.getResponseSync(ContentType.JSON, keyMap, ActionType.GET_WEBSITE, Map.class);
        Map<String, Object> config = response == null ? new HashMap<>() : new HashMap<>(response);
        if (isBlank(config.get("callbackUrl"))) {
            config.put("callbackUrl", requestInfo.getAccessUrl() + "/p/" + session.getPlugin().getShortName() + "/sync/" + UUID.randomUUID().toString().replace("-", ""));
        }
        normalizeSwitch(config, "status");
        normalizeSwitch(config, "commentEmailNotify");
        config.put("version", session.getPlugin().getVersion());
        return config;
    }

    private boolean isBlank(Object value) {
        return value == null || "".equals(value);
    }

    private void normalizeSwitch(Map<String, Object> config, String key) {
        if (!Objects.equals(config.get(key), "on")) {
            config.put(key, "off");
        }
    }

    private Map<String, Object> successMap(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("data", data);
        return map;
    }

    private void response(Map<String, Object> map) {
        session.sendMsg(ContentType.JSON, map, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    private boolean isDarkMode() {
        return requestInfo.getHeader() != null && Objects.equals(requestInfo.getHeader().get("Dark-Mode"), "true");
    }

    private String getAdminColorPrimary() {
        if (requestInfo.getHeader() == null) {
            return null;
        }
        String color = requestInfo.getHeader().get("Admin-Color-Primary");
        if (color == null) {
            color = requestInfo.getHeader().get("admin-color-primary");
        }
        if (color == null) {
            for (Map.Entry<String, String> entry : requestInfo.getHeader().entrySet()) {
                if ("admin-color-primary".equalsIgnoreCase(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return color;
    }

    private void dealSyncRequest(final Map<String, Object> response, final ChangyanComment changyanComment, final boolean emailNotify) {
        if (changyanComment != null) {
            LOGGER.info("sync action " + changyanComment);
            for (CommentsEntry commentsEntry : changyanComment.getComments()) {
                final Comment comment = getComment(changyanComment, commentsEntry);

                LOGGER.log(Level.INFO, "changyan call " + gson.toJson(comment));
                session.sendMsg(ContentType.JSON, comment, ActionType.ADD_COMMENT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
                    response.put("status", msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS ? 200 : 500);
                    session.sendMsg(ContentType.JSON, response, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
                });
                if (emailNotify) {
                    session.sendMsg(ContentType.JSON, new HashMap<>(), ActionType.LOAD_PUBLIC_INFO.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
                        PublicInfo publicInfo = msgPacket.convertToClass(PublicInfo.class);
                        Map<String, String> map = new HashMap<>();
                        Map<String, Object> moduleMap = new HashMap<>();
                        moduleMap.put("content", comment.getContent());
                        moduleMap.put("title", changyanComment.getTitle());
                        moduleMap.put("titleUrl", changyanComment.getUrl());
                        moduleMap.put("username", comment.getName());
                        moduleMap.put("version", session.getPlugin().getVersion());
                        map.put("content", new SimpleTemplateRender().render("/email/notify-email.html", session.getPlugin(), moduleMap));
                        map.put("title", publicInfo.getTitle() + " 有了新的评论");
                        session.requestService("emailService", map);
                    });
                }
            }
        }
    }

    private static Comment getComment(ChangyanComment changyanComment, CommentsEntry commentsEntry) {
        final Comment comment = new Comment();
        comment.setName(commentsEntry.getUser().getNickname());
        comment.setHeadPortrait(commentsEntry.getUser().getUsericon());
        comment.setLogId(changyanComment.getSourceid());
        comment.setIp(commentsEntry.getIp());
        comment.setContent(commentsEntry.getContent());
        comment.setCreatedTime(new Date(commentsEntry.getCtime()));
        comment.setPostId(commentsEntry.getCmtid());
        return comment;
    }
}
