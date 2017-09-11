package org.openstreetmap.josm.plugins.nl_addresses.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.nl_addresses.Address;
import org.openstreetmap.josm.plugins.nl_addresses.Addressable;
import org.openstreetmap.josm.plugins.nl_addresses.PcHnrKey;
import org.openstreetmap.josm.plugins.nl_addresses.StreetHnrKey;
import org.openstreetmap.josm.plugins.nl_addresses.ValidationData;
import org.openstreetmap.josm.tools.I18n;

public class DuplicateAddress extends Test {
    public static final int DUPLICATE_ADDRESS = 13701;

    private final ValidationData data;

    public DuplicateAddress() {
        super(tr("Duplicate addresses"), tr("Checks for duplicate addresses."));
        data = new ValidationData();
    }

    public ValidationData getValidationData() {
        return data;
    }

    @Override
    public void startTest(ProgressMonitor monitor) {
        super.startTest(monitor);
        data.clear();
    }

    @Override
    public void visit(Node n) {
        visit((OsmPrimitive)n);
    }

    @Override
    public void visit(Way w) {
        visit((OsmPrimitive)w);
    }

    @Override
    public void visit(Relation r) {
        visit((OsmPrimitive)r);
    }

    public void visit(OsmPrimitive n) {
        Addressable addressable = new Addressable(n);
        data.add(addressable);
    }

    @Override
    public void endTest() {
        for (Entry<PcHnrKey, Set<Addressable>> entry :
            data.getDuplicatePcHnrNodes().entrySet()) {
            errors.add(buildTestErrorPcHnr(this, entry));
        }
        for (Entry<StreetHnrKey, Set<Addressable>> entry :
            data.getDuplicateStreetHnrNodes().entrySet()) {
            if (!falsePositive(entry.getValue())) {
                errors.add(buildTestErrorStreetHnr(this, entry));
            }
        }
        super.endTest();
        data.clear();
    }

    private static boolean falsePositive(Set<Addressable> addressables) {
        return falsePositive(addressables.toArray(new Addressable[addressables.size()]));
    }

    private static boolean falsePositive(Addressable[] primitives) {
        if (primitives.length == 2) {
            return falsePositive(primitives[0], primitives[1]);
        }
        return false;
    }

    private static boolean falsePositive(Addressable an1, Addressable an2) {
        Address a1 = an1.getAddress();
        Address a2 = an2.getAddress();
        if (a1.getPostCode() != null && !Objects.equals(a1.getPostCode(), a2.getPostCode())) {
            return true;
        }
        if (a1.getCity() != null && !Objects.equals(a1.getCity(), a2.getCity())) {
            return true;
        }
        return false;
    }

    private static TestError buildTestErrorPcHnr(
            DuplicateAddress tester, Entry<PcHnrKey, Set<Addressable>> entry) {
        List<OsmPrimitive> primitives = new ArrayList<>(entry.getValue().size());
        PcHnrKey key = entry.getKey();
        for (Addressable n : entry.getValue()) {
            primitives.add(n.getPrimitive());
        }
        Builder builder = TestError
                .builder(tester, Severity.WARNING, DUPLICATE_ADDRESS)
                .message("Duplicate Address",
                        I18n.tr("Duplicate address primitive for {0}-{1}",
                                key.getPostcode(), key.getFullHouseNumber()))
                .primitives(primitives)
                .fix(new DuplicateAddressableFixer(primitives));
        return builder.build();
    }

    private static TestError buildTestErrorStreetHnr(
            DuplicateAddress tester, Entry<StreetHnrKey, Set<Addressable>> entry) {
        List<OsmPrimitive> primitives = new ArrayList<>(entry.getValue().size());
        StreetHnrKey key = entry.getKey();
        for (Addressable n : entry.getValue()) {
            primitives.add(n.getPrimitive());
        }

        Builder builder = TestError
                .builder(tester, Severity.WARNING, DUPLICATE_ADDRESS)
                .message("Duplicate Address",
                        I18n.tr("Duplicate address primitive for {0} {1}",
                                key.getStreet(), key.getFullHouseNumber()))
                .primitives(primitives);
        //                .fix(new DuplicateAddressableFixer(primitives));
        return builder.build();
    }

    static class DuplicateAddressableFixer implements Supplier<Command> {
        private final Collection<OsmPrimitive> primitives;

        public DuplicateAddressableFixer(Collection<OsmPrimitive> primitives) {
            this.primitives = primitives;
        }

        @Override
        public Command get() {
            if (primitives.size() != 2)
                return null;
            Iterator<? extends OsmPrimitive> it = primitives.iterator();
            OsmPrimitive n1 = it.next();
            OsmPrimitive n2 = it.next();
            if (n1.isDeleted() || n2.isDeleted())
                return null;
            //            double distance = n1.getCoor().distance(n2.getCoor());
            //            Addressable a1 = new Addressable(n1);
            //            Addressable a2 = new Addressable(n2);
            return null;
        }
    }

    @Override
    public boolean isPrimitiveUsable(OsmPrimitive p) {
        return super.isPrimitiveUsable(p) && p.hasKey("addr:housenumber");
    }
}
