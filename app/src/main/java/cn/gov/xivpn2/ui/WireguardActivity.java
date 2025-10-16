package cn.gov.xivpn2.ui;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import cn.gov.xivpn2.R;
import cn.gov.xivpn2.crypto.Key;
import cn.gov.xivpn2.crypto.KeyFormatException;
import cn.gov.xivpn2.xrayconfig.Outbound;
import cn.gov.xivpn2.xrayconfig.WireguardPeer;
import cn.gov.xivpn2.xrayconfig.WireguardSettings;

public class WireguardActivity extends ProxyActivity<WireguardSettings> {

    private static final Pattern RESERVED_PATTERN = Pattern.compile("^\\d{1,3},\\d{1,3},\\d{1,3}$");


    @Override
    protected boolean validateField(String key, String value) {
        switch (key) {
            case "PRIVATE_KEY":
            case "PEER_ENDPOINT":
            case "PEER_PUBLIC_KEY":
            case "ADDRESS1":
                return !value.isEmpty();
            case "RESERVED_PATTERN":
                return RESERVED_PATTERN.matcher(value).find();
            case "PEER_PERSISTENT_KEEPALIVE":
                if (value.isEmpty()) {
                    return true;
                }
                try {
                    int keepAlive = Integer.parseInt(value);
                    return keepAlive >= 0 && keepAlive <= 65535;
                } catch (NumberFormatException ignored) {
                    return false;
                }
        }
        return super.validateField(key, value);
    }

    @Override
    protected Type getType() {
        return new TypeToken<Outbound<WireguardSettings>>() {
        }.getType();
    }

    @Override
    protected WireguardSettings buildProtocolSettings(IProxyEditor adapter) {
        WireguardSettings wireguardSettings = new WireguardSettings();

        String address1 = adapter.getValue("ADDRESS1");
        String address2 = adapter.getValue("ADDRESS2");
        if (!address1.isEmpty()) wireguardSettings.address.add(address1);
        if (!address2.isEmpty()) wireguardSettings.address.add(address2);

        wireguardSettings.secretKey = adapter.getValue("PRIVATE_KEY");
        String[] splits = adapter.getValue("RESERVED").split(",");
        wireguardSettings.reserved[0] = Integer.parseInt(splits[0]);
        wireguardSettings.reserved[1] = Integer.parseInt(splits[1]);
        wireguardSettings.reserved[2] = Integer.parseInt(splits[2]);

        WireguardPeer peer = new WireguardPeer();
        peer.endpoint = adapter.getValue("PEER_ENDPOINT");
        peer.publicKey = adapter.getValue("PEER_PUBLIC_KEY");
        peer.preSharedKey = adapter.getValue("PEER_PRE_SHARED_KEY");
        peer.allowedIPs = adapter.getValue("PEER_ALLOWED_IPS").split(",");
        String keepAliveValue = adapter.getValue("PEER_PERSISTENT_KEEPALIVE");
        if (!keepAliveValue.isEmpty()) {
            peer.keepAlive = Integer.parseInt(keepAliveValue);
        }
        wireguardSettings.peers.add(peer);

        return wireguardSettings;
    }

    @Override
    protected LinkedHashMap<String, String> decodeOutboundConfig(Outbound<WireguardSettings> outbound) {
        // wireguard does not support stream settings
        // so we are not calling super.decodeOutboundConfig
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();

        if (!outbound.settings.address.isEmpty()) {
            hashMap.put("ADDRESS1", outbound.settings.address.get(0));
        }
        if (outbound.settings.address.size() >= 2) {
            hashMap.put("ADDRESS2", outbound.settings.address.get(1));
        }
        hashMap.put("PRIVATE_KEY", outbound.settings.secretKey);
        hashMap.put("RESERVED", outbound.settings.reserved[0] + "," + outbound.settings.reserved[1] + "," + outbound.settings.reserved[2]);
        hashMap.put("PEER_ENDPOINT", outbound.settings.peers.get(0).endpoint);
        hashMap.put("PEER_PUBLIC_KEY", outbound.settings.peers.get(0).publicKey);
        hashMap.put("PEER_PRE_SHARED_KEY", outbound.settings.peers.get(0).preSharedKey);
        hashMap.put("PEER_ALLOWED_IPS", String.join(",", outbound.settings.peers.get(0).allowedIPs));
        hashMap.put("PEER_PERSISTENT_KEEPALIVE", String.valueOf(outbound.settings.peers.get(0).keepAlive));

        return hashMap;
    }

    @Override
    protected String getProtocolName() {
        return "wireguard";
    }

    @Override
    protected void initializeInputs(IProxyEditor adapter) {
        adapter.addInput("ADDRESS1", getString(R.string.wireguard_address1_label), "", getString(R.string.wireguard_address1_helper));
        adapter.addInput("ADDRESS2", getString(R.string.wireguard_address2_label), "", getString(R.string.wireguard_address2_helper));
        adapter.addInput(new ProxyEditTextAdapter.TextInputGen("PRIVATE_KEY", getString(R.string.wireguard_private_key_label), "", (view) -> {
            Key privateKey = Key.generatePrivateKey();
            adapter.setValue("PRIVATE_KEY", privateKey.toBase64());
            adapter.notifyValueChanged("PRIVATE_KEY");

            adapter.setValue("PUBLIC_KEY", Key.generatePublicKey(privateKey).toBase64());
            adapter.notifyValueChanged("PUBLIC_KEY");
        }, () -> {
            String privateKey = adapter.getValue("PRIVATE_KEY");
            try {
                adapter.setValue("PUBLIC_KEY", Key.generatePublicKey(Key.fromBase64(privateKey)).toBase64());
                adapter.notifyValueChanged("PUBLIC_KEY");
            } catch (KeyFormatException ignored) {
            }
        }));
        adapter.addInput(new ProxyEditTextAdapter.TextInput("PUBLIC_KEY", getString(R.string.wireguard_public_key_label), "", true));
        adapter.addInput("RESERVED", getString(R.string.wireguard_reserved_label), "0,0,0", getString(R.string.wireguard_reserved_helper));
        adapter.addInput("PEER_ENDPOINT", getString(R.string.wireguard_peer_endpoint_label), "", getString(R.string.wireguard_peer_endpoint_helper));
        adapter.addInput("PEER_PUBLIC_KEY", getString(R.string.wireguard_peer_public_key_label));
        adapter.addInput("PEER_PRE_SHARED_KEY", getString(R.string.wireguard_peer_pre_shared_key_label));
        adapter.addInput("PEER_ALLOWED_IPS", getString(R.string.wireguard_allowed_ips_label), "0.0.0.0/0,::/0");
        adapter.addInput("PEER_PERSISTENT_KEEPALIVE", getString(R.string.wireguard_persistent_keepalive_label), String.valueOf(new WireguardPeer().keepAlive), getString(R.string.wireguard_persistent_keepalive_helper));
    }

    /**
     * Wireguard does not support stream settings
     */
    @Override
    protected boolean hasStreamSettings() {
        return false;
    }
}
