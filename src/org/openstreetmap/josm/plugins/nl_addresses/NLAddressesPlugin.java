package org.openstreetmap.josm.plugins.nl_addresses;

import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.nl_addresses.validation.DuplicateAddress;

public class NLAddressesPlugin extends Plugin {

    public NLAddressesPlugin(PluginInformation info) {
        super(info);
        OsmValidator.addTest(DuplicateAddress.class);
    }
}
