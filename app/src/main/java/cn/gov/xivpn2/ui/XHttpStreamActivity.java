package cn.gov.xivpn2.ui;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import cn.gov.xivpn2.xrayconfig.Outbound;
import cn.gov.xivpn2.xrayconfig.XHttpStream;

public class XHttpStreamActivity extends ProxyActivity<XHttpStream> {

    @Override
    protected XHttpStream buildProtocolSettings(IProxyEditor adapter) {
        return new XHttpStream();
    }

    @Override
    protected LinkedHashMap<String, String> decodeOutboundConfig(Outbound<XHttpStream> outbound) {
        return super.decodeOutboundConfig(outbound);
    }

    @Override
    protected Type getType() {
        return new TypeToken<Outbound<XHttpStream>>() {
        }.getType();
    }

    @Override
    protected String getProtocolName() {
        return "xhttpstream";
    }

    @Override
    protected void afterInitializeInputs(IProxyEditor adapter) {
        adapter.removeInput("GROUP_PROXY");
        adapter.setValue("NETWORK", "xhttp");
        adapter.notifyValueChanged("NETWORK");
        adapter.setValue("SECURITY", "none");
        adapter.notifyValueChanged("SECURITY");
    }

    @Override
    protected void onInputChanged(IProxyEditor adapter, String key, String value) {
        super.onInputChanged(adapter, key, value);
    }

}
