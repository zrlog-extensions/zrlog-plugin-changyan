import {Button, Col, Divider, Form, Input, Row, Space, Switch, Tag, Typography, message, theme} from "antd";
import {SaveOutlined} from "@ant-design/icons";
import axios from "axios";
import {FunctionComponent, useEffect, useMemo, useState} from "react";
import {PluginInfoResponse, StandardResponse} from "../index";

type PluginSettingsProps = {
    data: PluginInfoResponse;
}

type ChangyanFormValues = {
    appId?: string;
    appKey?: string;
    callbackUrl?: string;
    status?: boolean;
    commentEmailNotify?: boolean;
}

const enabled = (value?: string) => value === "on" || value === "true" || value === "1";
const switchValue = (value?: boolean) => value ? "on" : "off";

const PluginSettings: FunctionComponent<PluginSettingsProps> = ({data}) => {
    const {token} = theme.useToken();
    const [form] = Form.useForm<ChangyanFormValues>();
    const [loading, setLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();
    const version = data.config.version || data.plugin.version;

    useEffect(() => {
        form.setFieldsValue({
            appId: data.config.appId || "",
            appKey: data.config.appKey || "",
            callbackUrl: data.config.callbackUrl || "",
            status: enabled(data.config.status),
            commentEmailNotify: enabled(data.config.commentEmailNotify),
        });
    }, [data.config, form]);

    const shellStyle = useMemo(() => ({
        maxWidth: 980,
        margin: "0 auto",
        padding: 24,
        color: token.colorText,
        background: token.colorBgLayout,
        minHeight: "100vh",
        boxSizing: "border-box" as const,
    }), [token]);

    const panelStyle = useMemo(() => ({
        padding: 24,
        border: `1px solid ${token.colorBorderSecondary}`,
        borderRadius: 8,
        background: token.colorBgContainer,
    }), [token]);

    const submit = async (values: ChangyanFormValues) => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                appId: values.appId || "",
                appKey: values.appKey || "",
                callbackUrl: values.callbackUrl || "",
                status: switchValue(values.status),
                commentEmailNotify: switchValue(values.commentEmailNotify),
            });
            const {data: response} = await axios.post<StandardResponse<unknown>>("update", params, {
                headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"},
            });
            if (!response.success) {
                throw new Error(response.message || "保存失败");
            }
            messageApi.success("已保存");
        } catch (e) {
            messageApi.error(e instanceof Error ? e.message : "保存失败");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={shellStyle}>
            {contextHolder}
            <Space direction="vertical" size={20} style={{width: "100%"}}>
                <div style={{display: "flex", justifyContent: "space-between", gap: 16, flexWrap: "wrap"}}>
                    <Space direction="vertical" size={4}>
                        <Space wrap>
                            <Typography.Title level={3} style={{margin: 0}}>{data.plugin.name}</Typography.Title>
                            <Tag>v{version}</Tag>
                        </Space>
                        <Typography.Text type="secondary">{data.plugin.desc}</Typography.Text>
                    </Space>
                </div>

                <div style={panelStyle}>
                    <Form form={form} layout="vertical" onFinish={submit} requiredMark={false}>
                        <Row gutter={16}>
                            <Col xs={24} md={12}>
                                <Form.Item label="APP ID" name="appId">
                                    <Input autoComplete="off"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24} md={12}>
                                <Form.Item label="APP KEY" name="appKey">
                                    <Input.Password autoComplete="new-password"/>
                                </Form.Item>
                            </Col>
                            <Col xs={24}>
                                <Form.Item label="评论回推地址" name="callbackUrl">
                                    <Input autoComplete="off"/>
                                </Form.Item>
                            </Col>
                        </Row>

                        <Divider/>

                        <Space direction="vertical" size={16} style={{width: "100%"}}>
                            <Form.Item label="启用畅言评论框" name="status" valuePropName="checked" style={{marginBottom: 0}}>
                                <Switch checkedChildren="启用" unCheckedChildren="停用"/>
                            </Form.Item>
                            <Form.Item label="新评论邮件通知" name="commentEmailNotify" valuePropName="checked" style={{marginBottom: 0}}>
                                <Switch checkedChildren="通知" unCheckedChildren="关闭"/>
                            </Form.Item>
                        </Space>

                        <Divider/>

                        <Button type="primary" htmlType="submit" icon={<SaveOutlined/>} loading={loading}>
                            保存
                        </Button>
                    </Form>
                </div>
            </Space>
        </div>
    );
};

export default PluginSettings;
