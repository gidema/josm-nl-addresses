package org.openstreetmap.josm.plugins.nl_addresses.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.nl_addresses.Address;
import org.openstreetmap.josm.plugins.nl_addresses.AddressNode;
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
        AddressNode addressNode = new AddressNode(n);
        data.add(addressNode);
    }

    @Override
    public void endTest() {
        for (Entry<PcHnrKey, Set<AddressNode>> entry :
            data.getDuplicatePcHnrNodes().entrySet()) {
            errors.addAll(buildTestErrorsPcHnr(this, entry));
        }
        for (Entry<StreetHnrKey, Set<AddressNode>> entry :
            data.getDuplicateStreetHnrNodes().entrySet()) {
            if (!falsePositive(entry.getValue())) {
                errors.addAll(buildTestErrorsStreetHnr(this, entry));
            }
        }
        super.endTest();
        data.clear();
    }

    private static boolean falsePositive(Set<AddressNode> addressNodes) {
        return falsePositive(addressNodes.toArray(new AddressNode[addressNodes.size()]));
    }

    private static boolean falsePositive(AddressNode[] nodes) {
        if (nodes.length == 2) {
            return falsePositive(nodes[0], nodes[1]);
        }
        return false;
    }

    private static boolean falsePositive(AddressNode an1, AddressNode an2) {
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

    private static Collection<? extends TestError> buildTestErrorsPcHnr(
            DuplicateAddress tester, Entry<PcHnrKey, Set<AddressNode>> entry) {
        List<Node> nodes = new ArrayList<>(entry.getValue().size());
        PcHnrKey key = entry.getKey();
        for (AddressNode n : entry.getValue()) {
            nodes.add(n.getNode());
        }
        Builder builder = TestError
                .builder(tester, Severity.WARNING, DUPLICATE_ADDRESS)
                .message("Duplicate Address",
                        I18n.tr("Duplicate address node for {0}-{1}",
                                key.getPostcode(), key.getFullHouseNumber()))
                .primitives(nodes)
                .fix(new DuplicateAddressNodeFixer(nodes));
        return Collections.singletonList(builder.build());
    }

    private static Collection<? extends TestError> buildTestErrorsStreetHnr(
            DuplicateAddress tester, Entry<StreetHnrKey, Set<AddressNode>> entry) {
        List<Node> nodes = new ArrayList<>(entry.getValue().size());
        StreetHnrKey key = entry.getKey();
        for (AddressNode n : entry.getValue()) {
            nodes.add(n.getNode());
        }

        Builder builder = TestError
                .builder(tester, Severity.WARNING, DUPLICATE_ADDRESS)
                .message("Duplicate Address",
                        I18n.tr("Duplicate address node for {0} {1}",
                                key.getStreet(), key.getFullHouseNumber()))
                .primitives(nodes)
                .fix(new DuplicateAddressNodeFixer(nodes));
        return Collections.singletonList(builder.build());
    }

    static class DuplicateAddressNodeFixer implements Supplier<Command> {
        private final Collection<Node> nodes;

        public DuplicateAddressNodeFixer(Collection<Node> nodes) {
            this.nodes = nodes;
        }

        @Override
        public Command get() {
            if (nodes.size() != 2)
                return null;
            Iterator<? extends Node> it = nodes.iterator();
            Node n1 = it.next();
            Node n2 = it.next();
            if (n1.isDeleted() || n1.isDeleted())
                return null;
            double distance = n1.getCoor().distance(n2.getCoor());
            AddressNode a1 = new AddressNode(n1);
            AddressNode a2 = new AddressNode(n2);
            return null;
        }
    }

    @Override
    public boolean isPrimitiveUsable(OsmPrimitive p) {
        return super.isPrimitiveUsable(p) && p.hasKey("addr:housenumber");
    }
}
