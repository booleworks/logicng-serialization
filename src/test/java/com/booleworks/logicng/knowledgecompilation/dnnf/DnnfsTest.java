package com.booleworks.logicng.knowledgecompilation.dnnf;

import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.knowledgecompilation.dnnf.datastructures.Dnnf;
import com.booleworks.logicng.util.FormulaRandomizer;
import com.booleworks.logicng.util.FormulaRandomizerConfig;
import org.junit.jupiter.api.Test;

public class DnnfsTest {

    @Test
    public void testRandomizedDnnfs() {
        final FormulaFactory f = FormulaFactory.caching();
        final FormulaRandomizer randomizer = new FormulaRandomizer(f, FormulaRandomizerConfig.builder().seed(42).build());
        for (int i = 0; i < 100; i++) {
            final Formula formula = randomizer.formula(4);
            final Dnnf dnnf = new DnnfFactory().compile(f, formula);
            final Dnnf deserialized = Dnnfs.deserialize(f, Dnnfs.serialize(dnnf));
            assertThat(deserialized).isEqualTo(dnnf);
        }
    }
}
