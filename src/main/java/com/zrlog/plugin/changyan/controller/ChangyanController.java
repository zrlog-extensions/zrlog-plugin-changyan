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
        session.sendMsg(new MsgPacket(requestConfig(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(),
                ActionType.SET_WEBSITE.name()), msgPacket -> {
            response(ChangyanApiResponse.success());
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
        session.sendJsonMsg(WebsiteKeyRequest.of("appId"), ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            ChangyanConfig config = gson.fromJson(msgPacket.getDataStr(), ChangyanConfig.class);
            String articleId = paramValue("articleId");
            if (Objects.isNull(articleId)) {
                articleId = "-1";
            }
            Map<String, Object> map = new HashMap<>();
            map.put("appId", config == null ? "" : config.getAppId());
            map.put("articleId", articleId);
            session.responseHtml("/widget", map, requestPacket.getMethodStr(), requestPacket.getMsgId());
        });

    }

    /**
     * 反向同步接口
     */
    public void sync() {
        session.sendJsonMsg(WebsiteKeyRequest.of("short_name,secret,status,commentEmailNotify,callbackUrl"),
                ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
            ChangyanConfig changyan = gson.fromJson(msgPacket.getDataStr(), ChangyanConfig.class);
            String callbackUrl = changyan == null ? null : changyan.getCallbackUrl();
            String ignoreChar = "/p" + "/" + session.getPlugin().getShortName();
            try {
                if (callbackUrl != null && new URL(callbackUrl).getPath().replace(ignoreChar, "").equals(requestInfo.getUri().replace(".action", ""))) {
                    String commentJsonStr = requestInfo.getParam().get("data")[0];
                    LOGGER.info(commentJsonStr);
                    final ChangyanComment changyanComment = gson.fromJson(commentJsonStr, ChangyanComment.class);
                    dealSyncRequest(new ChangyanSyncResponse(), changyanComment,
                            changyan != null && changyan.isCommentEmailNotifyEnabled());
                } else {
                    session.sendMsg(ContentType.HTML, ClientActionHandler.ACTION_NOT_FOUND_PAGE, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
                session.sendMsg(ContentType.HTML, "Exception", requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_ERROR);
            }
        });

    }

    private ChangyanApiResponse<ChangyanPageData> pageData() {
        ChangyanPageData data = new ChangyanPageData();
        data.setDark(isDarkMode());
        data.setColorPrimary(getAdminColorPrimary());
        data.setPlugin(session.getPlugin());
        data.setConfig(loadConfig());
        return ChangyanApiResponse.success(data);
    }

    private ChangyanConfig loadConfig() {
        ChangyanConfig config = session.getResponseSync(ContentType.JSON, WebsiteKeyRequest.of(CONFIG_KEYS), ActionType.GET_WEBSITE,
                ChangyanConfig.class);
        if (config == null) {
            config = new ChangyanConfig();
        }
        config.normalize(requestInfo.getAccessUrl() + "/p/" + session.getPlugin().getShortName() + "/sync/"
                + UUID.randomUUID().toString().replace("-", ""), session.getPlugin().getVersion());
        return config;
    }

    private ChangyanConfig requestConfig() {
        ChangyanConfig config = new ChangyanConfig();
        config.setAppId(paramValue("appId"));
        config.setAppKey(paramValue("appKey"));
        config.setCallbackUrl(paramValue("callbackUrl"));
        config.setStatus(paramValue("status"));
        config.setCommentEmailNotify(paramValue("commentEmailNotify"));
        return config;
    }

    private String paramValue(String key) {
        if (requestInfo.getParam() == null || requestInfo.getParam().get(key) == null || requestInfo.getParam().get(key).length == 0) {
            return null;
        }
        return requestInfo.getParam().get(key)[0];
    }

    private void response(Object data) {
        session.sendMsg(ContentType.JSON, data, requestPacket.getMethodStr(), requestPacket.getMsgId(), MsgPacketStatus.RESPONSE_SUCCESS);
    }

    private boolean isDarkMode() {
        return requestInfo.isDarkMode();
    }

    private String getAdminColorPrimary() {
        return requestInfo.getAdminColorPrimary();
    }

    private void dealSyncRequest(final ChangyanSyncResponse response, final ChangyanComment changyanComment, final boolean emailNotify) {
        if (changyanComment != null) {
            LOGGER.info("sync action " + changyanComment);
            for (CommentsEntry commentsEntry : changyanComment.getComments()) {
                final Comment comment = getComment(changyanComment, commentsEntry);

                LOGGER.log(Level.INFO, "changyan call " + gson.toJson(comment));
                session.sendMsg(ContentType.JSON, comment, ActionType.ADD_COMMENT.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, msgPacket -> {
                    response.setStatus(msgPacket.getStatus() == MsgPacketStatus.RESPONSE_SUCCESS ? 200 : 500);
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
