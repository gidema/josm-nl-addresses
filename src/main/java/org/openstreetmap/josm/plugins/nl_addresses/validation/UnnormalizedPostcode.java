package org.openstreetmap.josm.plugins.nl_addresses.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.nl_addresses.Address;
import org.openstreetmap.josm.tools.I18n;

public class UnnormalizedPostcode extends Test {
    public static final int UNNORMALIZED_POSTCODE = 13702;
    public List<Node> nodes = new ArrayList<>();

    public UnnormalizedPostcode() {
        super(tr("Unnormalized postcode"), tr("Checks for unnormalized postcodes."));
    }

    @Override
    public void startTest(ProgressMonitor monitor) {
        super.startTest(monitor);
        nodes.clear();
    }

    @Override
    public void visit(Node n) {
        String postcode = n.get("addr:postcode");
        if (postcode == null) return;
        String normalizedPostcode = Address.normalizePostcode(postcode);
        if (!postcode.equals(normalizedPostcode)) {
            nodes.add(n);
        }
    }

    @Override
    public void endTest() {
        nodes.forEach(n -> {
            Builder builder = TestError
                    .builder(this, Severity.WARNING, UNNORMALIZED_POSTCODE)
                    .message("Unnormalized postcode",
                            I18n.tr("Unnormalized postcode"))
                    .primitives(Collections.singletonList(n))
                    .fix(new UnnormalizedPostcodeFixer(n));
            errors.add(builder.build());

        });
        nodes.clear();
    }

    @Override
    public boolean isPrimitiveUsable(OsmPrimitive p) {
        return p.hasKey("addr:postcode");
    }

    static class UnnormalizedPostcodeFixer implements Supplier<Command> {
        private final Node node;

        public UnnormalizedPostcodeFixer(Node node) {
            this.node = node;
        }

        @Override
        public Command get() {
            String normalizedPostcode = Address.normalizePostcode(node.get("addr:postcode"));
            return new ChangePropertyCommand(node, "addr:postcode", normalizedPostcode);
        }
    }
}
