package cn.gov.xivpn2.ui;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;

import cn.gov.xivpn2.Utils;
import cn.gov.xivpn2.xrayconfig.Outbound;
import cn.gov.xivpn2.xrayconfig.VlessServerSettings;
import cn.gov.xivpn2.xrayconfig.VlessSettings;
import cn.gov.xivpn2.xrayconfig.VlessUser;

public class VlessActivity extends ProxyActivity<VlessSettings> {

    @Override
    protected boolean validateField(String key, String value) {
        switch (key) {
            case "ADDRESS":
            case "UUID":
                return !value.isEmpty();
            case "PORT":
                return Utils.isValidPort(value);
            case "LEVEL":
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return false;
                }
                return true;
            case "ENCRYPTION":
                return !value.isEmpty();
        }
        return super.validateField(key, value);
    }

    @Override
    protected Type getType() {
        return new TypeToken<Outbound<VlessSettings>>() {
        }.getType();
    }

    @Override
    protected VlessSettings buildProtocolSettings(IProxyEditor adapter) {
        VlessSettings vlessSettings = new VlessSettings();

        VlessServerSettings vnext = new VlessServerSettings();
        vnext.address = adapter.getValue("ADDRESS");
        vnext.port = Integer.parseInt(adapter.getValue("PORT"));

        VlessUser user = new VlessUser();
        user.id = adapter.getValue("UUID");
        String flow = adapter.getValue("FLOW");
        if (!flow.equals("none")) {
            user.flow = flow;
        } else {
            user.flow = null;
        }
        String encryptionValue = adapter.getValue("ENCRYPTION");
        if (encryptionValue == null || encryptionValue.isEmpty()) {
            user.encryption = "none";
        } else {
            user.encryption = encryptionValue;
        }
        user.level = Integer.parseInt(adapter.getValue("LEVEL"));
        vnext.users.add(user);

        vlessSettings.vnext.add(vnext);

        return vlessSettings;
    }

    @Override
    protected LinkedHashMap<String, String> decodeOutboundConfig(Outbound<VlessSettings> outbound) {
        LinkedHashMap<String, String> hashMap = super.decodeOutboundConfig(outbound);
        VlessServerSettings vnext = outbound.settings.vnext.get(0);
        hashMap.put("ADDRESS", vnext.address);
        hashMap.put("PORT", String.valueOf(vnext.port));
        String flow = vnext.users.get(0).flow;
        if (flow == null || flow.isEmpty()) {
            hashMap.put("FLOW", "none");
        } else {
            hashMap.put("FLOW", flow);
        }
        hashMap.put("UUID", vnext.users.get(0).id);
        String encryption = vnext.users.get(0).encryption;
        if (encryption == null || encryption.isEmpty()) {
            hashMap.put("ENCRYPTION", "none");
        } else {
            hashMap.put("ENCRYPTION", encryption);
        }
        hashMap.put("LEVEL", String.valueOf(vnext.users.get(0).level));
        return hashMap;
    }

    @Override
    protected String getProtocolName() {
        return "vless";
    }

    @Override
    protected void initializeInputs(IProxyEditor adapter) {
        adapter.addInput("ADDRESS", "Address");
        adapter.addInput("PORT", "Port");
        adapter.addInput("FLOW", "Flow", List.of("none", "xtls-rprx-vision", "xtls-rprx-vision-udp443", "xtls-rprx-vision-udp443-uplink", "xtls-rprx-vision-udp443-downlink"));
        adapter.addInput("UUID", "UUID");
        adapter.addInput("ENCRYPTION", "Encryption", List.of("none", "aes-128-gcm", "chacha20-poly1305", "zero"));
        adapter.addInput("LEVEL", "User Level", "0");
    }
}
