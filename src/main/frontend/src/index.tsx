import {legacyLogicalPropertiesTransformer, StyleProvider} from "@ant-design/cssinjs";
import {App, ConfigProvider, theme} from "antd";
import zhCN from "antd/es/locale/zh_CN";
import axios from "axios";
import {useEffect, useState} from "react";
import {createRoot} from "react-dom/client";
import AppBase from "./AppBase";

const {darkAlgorithm, defaultAlgorithm} = theme;

export interface Plugin {
    id?: string;
    version: string;
    name: string;
    paths?: string[];
    actions?: string[];
    desc: string;
    author: string;
    shortName: string;
    indexPage: string;
    previewImageBase64?: string;
    services?: string[];
    dependentService?: string[];
}

export interface PluginConfig {
    appId?: string;
    appKey?: string;
    callbackUrl?: string;
    status?: string;
    commentEmailNotify?: string;
    uriPath?: string;
    sitemapText?: string;
    version?: string;
}

export interface PluginInfoResponse {
    dark: boolean;
    colorPrimary?: string;
    plugin: Plugin;
    config: PluginConfig;
}

export interface StandardResponse<T> {
    success: boolean;
    message?: string;
    data: T;
}

const loadFromDocument = () => {
    try {
        const node = document.getElementById("pluginInfo");
        if (node === null || node.textContent === null || node.textContent.length === 0) {
            return null;
        }
        return JSON.parse(node.textContent) as StandardResponse<PluginInfoResponse>;
    } catch (e) {
        return null;
    }
};

const Index = () => {
    const [response, setResponse] = useState<StandardResponse<PluginInfoResponse> | null>(loadFromDocument);

    useEffect(() => {
        if (response === null) {
            axios.get<StandardResponse<PluginInfoResponse>>("json").then(({data}) => {
                setResponse(data);
            });
        }
    }, [response]);

    if (response === null || !response.success) {
        return <></>;
    }

    return (
        <ConfigProvider
            locale={zhCN}
            theme={{
                algorithm: response.data.dark ? darkAlgorithm : defaultAlgorithm,
                token: {
                    colorPrimary: response.data.colorPrimary || "#1677ff",
                },
            }}
        >
            <StyleProvider transformers={[legacyLogicalPropertiesTransformer]}>
                <App>
                    <AppBase pluginInfo={response.data}/>
                </App>
            </StyleProvider>
        </ConfigProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!);
root.render(<Index/>);
