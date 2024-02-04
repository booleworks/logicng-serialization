package com.booleworks.logicng.knowledgecompilation.dnnf;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Formulas;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.knowledgecompilation.dnnf.ProtoBufDnnf.PBDnnf;
import com.booleworks.logicng.knowledgecompilation.dnnf.datastructures.Dnnf;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Serialization methods for DNNFs.
 * @version 3.0.0
 * @since 2.5.0
 */
public interface Dnnfs {

    /**
     * Serializes a DNNF to a protocol buffer.
     * @param dnnf the DNNF
     * @return the protocol buffer
     */
    static PBDnnf serialize(final Dnnf dnnf) {
        final PBDnnf.Builder builder = PBDnnf.newBuilder();
        builder.setFormula(Formulas.serialize(dnnf.formula()));
        builder.addAllOriginalVariables(dnnf.getOriginalVariables().stream().map(Variable::name).collect(Collectors.toList()));
        return builder.build();
    }

    /**
     * Deserializes a DNNF from a protocol buffer.
     * @param f   the formula factory to generate the DNNF's formula
     * @param bin the protocol buffer
     * @return the DNNF
     */
    static Dnnf deserialize(final FormulaFactory f, final PBDnnf bin) {
        final SortedSet<Variable> vars = bin.getOriginalVariablesList().stream()
                .map(f::variable)
                .collect(Collectors.toCollection(TreeSet::new));
        return new Dnnf(vars, Formulas.deserialize(f, bin.getFormula()));
    }
}
