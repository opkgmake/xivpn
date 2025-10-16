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
    protected void initializeInputs(IProxyEditor adapter) {
        // The XHTTP stream outbound does not expose any protocol specific fields.
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
        if (adapter.exists("NETWORK_XHTTP_MODE")) {
            adapter.setValue("NETWORK_XHTTP_MODE", "auto");
            adapter.notifyValueChanged("NETWORK_XHTTP_MODE");
        }
    }

    @Override
    protected void onInputChanged(IProxyEditor adapter, String key, String value) {
        super.onInputChanged(adapter, key, value);
    }

}
