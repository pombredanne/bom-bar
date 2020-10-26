/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.bombar.core.domain.licenses;

import com.philips.research.bombar.core.domain.Project;
import com.philips.research.bombar.core.domain.Relation;

public class Licenses {
    public static final LicenseRegistry REGISTRY = new LicenseRegistry();
    private static final String PERMISSIVE = "(permissive)";

    private static final String ADVERTISING = "ADVERTISING";
    private static final String PATENTS = "PATENTS";

    static {
        REGISTRY.term(ADVERTISING, "Advertising clause");
        REGISTRY.term(PATENTS, "Patents clause");

        final var permissive = REGISTRY.license(PERMISSIVE)
                .accept(ADVERTISING).accept(PATENTS);
        REGISTRY.license("CC-PDDC", permissive);
        REGISTRY.license("WTFPL", permissive);
        REGISTRY.license("Unlicense", permissive);
        REGISTRY.license("CC0-1.0", permissive);
        REGISTRY.license("MIT", permissive);
        REGISTRY.license("X11", permissive);
        REGISTRY.license("ISC", permissive);
        REGISTRY.license("0BSD", permissive);
        REGISTRY.license("BSD-2-Clause", permissive);
        REGISTRY.license("BSD-3-Clause", permissive);
        REGISTRY.license("BSD-4-Clause", permissive).demand(ADVERTISING);
        REGISTRY.license("Python-2.0", permissive);
        REGISTRY.license("Apache-1.0", permissive);
        REGISTRY.license("Apache-1.1", permissive);
        REGISTRY.license("Apache-2.0", permissive).demand(PATENTS, Relation.Type.MODIFIED_CODE);
        REGISTRY.license("AFL-1.1", permissive);
        REGISTRY.license("AFL-1.2", permissive);
        REGISTRY.license("AFL-2.0", permissive);
        REGISTRY.license("AFL-2.1", permissive);
        REGISTRY.license("AFL-3.0", permissive);
        REGISTRY.license("SAX-PD", permissive);

        REGISTRY.license("CDDL-1.0", permissive).copyleft(Relation.Type.MODIFIED_CODE);
        REGISTRY.license("CDDL-1.1", permissive).copyleft(Relation.Type.MODIFIED_CODE);

        REGISTRY.license("EPL-1.0").copyleft(Relation.Type.MODIFIED_CODE);
        final var epl2 = REGISTRY.license("EPL-2.0").copyleft(Relation.Type.MODIFIED_CODE);

        final var cecill1 = REGISTRY.license("CECILL-1.0").copyleft();
        REGISTRY.license("CECILL-1.1").copyleft(cecill1);
        final var cecill2 = REGISTRY.license("CECILL-2.0").copyleft();
        final var cecill2_1 = REGISTRY.license("CECILL-2.1").copyleft()
                .accept(cecill2);

        final var mpl1_0 = REGISTRY.license("MPL-1.0").copyleft();
        REGISTRY.license("MPL-1.1").copyleft(mpl1_0);
        final var mpl2_0 = REGISTRY.license("MPL-2.0").copyleft();

        REGISTRY.license("EUPL-1.0").copyleft();
        REGISTRY.license("EUPL-1.1").copyleft()
                .accept(cecill2_1);
        final var eupl1_2 = REGISTRY.license("EUPL-1.2").copyleft()
                .accept(cecill2_1);

        final var lgpl2 = REGISTRY.license("LGPL-2.0-only")
                .copyleft(Relation.Type.STATIC_LINK, Project.Distribution.SAAS);
        final var lgpl2plus = REGISTRY.license("LGPL-2.0-or-later")
                .copyleft(Relation.Type.STATIC_LINK, Project.Distribution.SAAS)
                .accept(lgpl2);
        lgpl2.accept(lgpl2plus);
        REGISTRY.license("LGPL-2.1-only")
                .copyleft(lgpl2, Relation.Type.STATIC_LINK, Project.Distribution.SAAS)
                .accept(lgpl2plus)
                .accept(mpl2_0);
        REGISTRY.license("LGPL-2.1-or-later")
                .copyleft(lgpl2plus, Relation.Type.STATIC_LINK, Project.Distribution.SAAS)
                .accept(lgpl2)
                .accept(mpl2_0);
        final var lgpl3 = REGISTRY.license("LGPL-3.0-only")
                .copyleft(Relation.Type.STATIC_LINK, Project.Distribution.SAAS)
                .accept(lgpl2).accept(lgpl2plus)
                .accept(mpl2_0)
                .accept(PATENTS);
        final var lgpl3plus = REGISTRY.license("LGPL-3.0-or-later")
                .copyleft(Relation.Type.STATIC_LINK, Project.Distribution.SAAS)
                .accept(lgpl3)
                .accept(lgpl2).accept(lgpl2plus)
                .accept(mpl2_0)
                .accept(PATENTS);
        lgpl3.accept(lgpl3plus);

        final var gpl1 = REGISTRY.license("GPL-1.0-only")
                .copyleft(Relation.Type.DYNAMIC_LINK)
                .accept(cecill1);
        final var gpl1plus = REGISTRY.license("GPL-1.0-or-later")
                .copyleft(Relation.Type.DYNAMIC_LINK)
                .accept(gpl1)
                .accept(cecill1);
        gpl1.accept(gpl1plus);
        final var gpl2 = REGISTRY.license("GPL-2.0-only")
                .copyleft(Relation.Type.DYNAMIC_LINK)
                .accept(gpl1plus)
                .accept(eupl1_2)
                .accept(epl2)
                .accept(cecill2)
                .accept(cecill2_1)
                .accept(mpl1_0);
        REGISTRY.with("Classpath-exception-2.0", gpl2)
                .copyleft(gpl2, Relation.Type.STATIC_LINK);
        final var gpl2plus = REGISTRY.license("GPL-2.0-or-later")
                .copyleft(Relation.Type.DYNAMIC_LINK)
                .accept(gpl2)
                .accept(lgpl2).accept(lgpl2plus)
                .accept(gpl1plus)
                .accept(eupl1_2)
                .accept(epl2)
                .accept(cecill2).accept(cecill2_1)
                .accept(mpl2_0);
        gpl2.accept(lgpl2plus);
        final var gpl3 = REGISTRY.license("GPL-3.0-only")
                .copyleft(Relation.Type.DYNAMIC_LINK)
                .accept(lgpl3).accept(lgpl3plus)
                .accept(lgpl2plus)
                .accept(eupl1_2)
                .accept(epl2)
                .accept(cecill2).accept(cecill2_1)
                .accept(mpl2_0)
                .accept(PATENTS);
        final var gpl3plus = REGISTRY.license("GPL-3.0-or-later")
                .copyleft(Relation.Type.DYNAMIC_LINK)
                .accept(gpl3)
                .accept(lgpl2plus)
                .accept(lgpl3).accept(lgpl3plus)
                .accept(gpl1plus).accept(gpl2plus)
                .accept(eupl1_2)
                .accept(epl2)
                .accept(cecill2).accept(cecill2_1)
                .accept(mpl2_0)
                .accept(PATENTS);
        gpl3.accept(gpl3plus);

        final var agpl1 = REGISTRY.license("AGPL-1.0-only")
                .copyleft(Relation.Type.INDEPENDENT);
        final var agpl1plus = REGISTRY.license("AGPL-1.0-or-later")
                .copyleft(Relation.Type.INDEPENDENT)
                .accept(agpl1);
        agpl1.accept(agpl1plus);
        final var agpl3 = REGISTRY.license("AGPL-3.0-only")
                .copyleft(Relation.Type.INDEPENDENT)
                .accept(agpl1plus)
                .accept(lgpl3).accept(lgpl3plus)
                .accept(gpl3).accept(gpl3plus)
                .accept(eupl1_2)
                .accept(cecill2_1)
                .accept(mpl2_0)
                .accept(PATENTS);
        final var agpl3plus = REGISTRY.license("AGPL-3.0-or-later")
                .copyleft(Relation.Type.INDEPENDENT)
                .accept(agpl3)
                .accept(agpl1plus)
                .accept(lgpl3).accept(lgpl3plus)
                .accept(gpl3).accept(gpl3plus)
                .accept(eupl1_2)
                .accept(cecill2_1)
                .accept(mpl2_0)
                .accept(PATENTS);
        agpl3.accept(agpl3plus);
    }
}