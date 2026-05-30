import {FunctionComponent} from "react";
import {PluginInfoResponse} from "./index";
import PluginSettings from "./components/PluginSettings";

export type AppBaseProps = {
    pluginInfo: PluginInfoResponse;
}

const AppBase: FunctionComponent<AppBaseProps> = ({pluginInfo}) => {
    return <PluginSettings data={pluginInfo}/>;
}

export default AppBase;
