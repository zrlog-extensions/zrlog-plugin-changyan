package com.zrlog.plugin.changyan;

import com.zrlog.plugin.changyan.controller.ChangyanController;
import com.zrlog.plugin.changyan.response.ChangyanComment;
import com.zrlog.plugin.changyan.response.CommentsEntry;
import com.zrlog.plugin.changyan.response.User;
import com.zrlog.plugin.common.PluginNativeImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class GraalvmAgentApplication {


    public static void main(String[] args) throws IOException {
        PluginNativeImageUtils.usedGsonObject();
        PluginNativeImageUtils.gsonNativeAgentByClazz(Arrays.asList(User.class, ChangyanComment.class, CommentsEntry.class));
        String basePath = System.getProperty("user.dir").replace("\\target","").replace("/target", "");
        //PathKit.setRootPath(basePath);
        File file = new File(basePath + "/src/main/resources");
        PluginNativeImageUtils.doLoopResourceLoad(file.listFiles(), file.getPath()  + "/", "/");
        //Application.nativeAgent = true;
        PluginNativeImageUtils.exposeController(Collections.singletonList(ChangyanController.class));
        Application.main(args);

    }
}