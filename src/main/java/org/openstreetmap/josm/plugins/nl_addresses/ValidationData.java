package org.openstreetmap.josm.plugins.nl_addresses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ValidationData {
    private final Map<PcHnrKey, Addressable> pcHnrIndex = new HashMap<>();
    private final Map<PcHnrKey, Set<Addressable>> duplicatesByPcHnr = new HashMap<>();
    private final Map<StreetHnrKey, Addressable> streetHnrIndex = new HashMap<>();
    private final Map<StreetHnrKey, Set<Addressable>> duplicatesByStreetHnr = new HashMap<>();

    public void clear() {
        pcHnrIndex.clear();
        duplicatesByPcHnr.clear();
        streetHnrIndex.clear();
        duplicatesByStreetHnr.clear();
    }

    public void add(Addressable addressable) {
        PcHnrKey pcHnrKey = new PcHnrKey(addressable);
        if (addressable.getAddress().getPostCode() != null) {
            Addressable existing = pcHnrIndex.put(pcHnrKey, addressable);
            if (existing != null && existing != addressable) {
                Set<Addressable> duplicates = duplicatesByPcHnr.get(pcHnrKey);
                if (duplicates == null) {
                    duplicates = new HashSet<>();
                    duplicates.add(existing);
                    duplicatesByPcHnr.put(pcHnrKey, duplicates);
                }
                duplicates.add(addressable);
            }
        }
        StreetHnrKey streetHnrKey = new StreetHnrKey(addressable);
        Addressable existing = streetHnrIndex.put(streetHnrKey, addressable);
        if (existing != null && existing != addressable &&
                !Objects.equals(existing.getAddress().getPostCode(), pcHnrKey.getPostcode())) {
            Set<Addressable> duplicates = duplicatesByStreetHnr.get(streetHnrKey);
            if (duplicates == null) {
                duplicates = new HashSet<>();
                duplicates.add(existing);
                duplicatesByStreetHnr.put(streetHnrKey, duplicates);
            }
            duplicates.add(addressable);
        }

    }

    public Map<PcHnrKey, Set<Addressable>> getDuplicatePcHnrNodes() {
        return duplicatesByPcHnr;
    }

    public Map<StreetHnrKey, Set<Addressable>> getDuplicateStreetHnrNodes() {
        return duplicatesByStreetHnr;
    }
}
