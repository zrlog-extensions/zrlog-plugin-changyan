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
相同的固定发布版 [`zrlog-build`](https://github.com/zrlog/zrlog-build)
Action（`v1.0.1`，工作流固定完整 SHA）完成压缩和 SHA-256、文件大小校验；接入与故障处理见该仓库 README。
处理成功后才会生成最终制品的 MD5 并上传；处理失败会停止该平台的发布。
服务接收的版本号来自 `zrlog-build` 构建脚本生成的 `plugin.properties`。

发布前需要配置 Actions Secret `ARTIFACT_SERVICE_TOKEN`，可在仓库中单独设置，
或授权该仓库使用同名组织 Secret。在仓库或组织的 Actions Variables 中配置服务地址 `ARTIFACT_SERVICE_URL`。
