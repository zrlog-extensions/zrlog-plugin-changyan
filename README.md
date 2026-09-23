# zrlog-plugin-changyan

ZrLog 畅言评论插件。接入畅言评论框，处理畅言回推的评论同步，并可在新评论同步后通过邮件服务发送通知。

## 功能

- 配置畅言 App ID 和 App Key
- 开关畅言评论框
- 提供评论回推入口，用于同步畅言评论
- 可开启新评论邮件通知

## 构建

```shell
export JAVA_HOME=${HOME}/dev/graalvm-jdk-latest
export PATH=${JAVA_HOME}/bin:$PATH
```

## 原生制品发布

Linux amd64/arm64 制品在上传前会调用 `zrlog-artifact-service`，通过与 `plugin-core`
相同的固定版本 `process-artifact` Action 完成压缩和 SHA-256、文件大小校验。
处理成功后才会生成最终制品的 MD5 并上传；处理失败会停止该平台的发布。
服务接收的版本号使用 `bin/build-info.sh` 生成的实际插件版本。

发布前需要配置 Actions Secret `ARTIFACT_SERVICE_TOKEN`，可在仓库中单独设置，
或授权该仓库使用同名组织 Secret。服务地址为 `https://webdav.zrlog.com/artifact`。
