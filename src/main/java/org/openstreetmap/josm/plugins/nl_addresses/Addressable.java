package org.openstreetmap.josm.plugins.nl_addresses;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class Addressable {
    private final OsmPrimitive primitive;
    private final Address address;

    public Addressable(OsmPrimitive primitive) {
        this.primitive = primitive;
        this.address = new Address(primitive);
    }

    public OsmPrimitive getPrimitive() {
        return primitive;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        return primitive.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Addressable)) {
            return false;
        }
        return primitive.equals(((Addressable)obj).primitive);
    }
}
