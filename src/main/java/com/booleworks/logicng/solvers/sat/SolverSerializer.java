package com.booleworks.logicng.solvers.sat;

import com.booleworks.logicng.collections.Collections;
import com.booleworks.logicng.collections.LNGVector;
import com.booleworks.logicng.collections.ProtoBufCollections;
import com.booleworks.logicng.datastructures.Tristate;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.propositions.PropositionType;
import com.booleworks.logicng.propositions.Propositions;
import com.booleworks.logicng.propositions.SerializablePropositionBackpack;
import com.booleworks.logicng.solvers.MiniSat;
import com.booleworks.logicng.solvers.ProtoBufSatSolver;
import com.booleworks.logicng.solvers.ProtoBufSatSolver.PBGlucose;
import com.booleworks.logicng.solvers.ProtoBufSatSolver.PBMiniSat2;
import com.booleworks.logicng.solvers.SolverWrapperState;
import com.booleworks.logicng.solvers.datastructures.MSClause;
import com.booleworks.logicng.solvers.datastructures.MSVariable;
import com.booleworks.logicng.solvers.datastructures.MSWatcher;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBProofInformation;
import com.booleworks.logicng.solvers.datastructures.SolverDatastructures;
import com.booleworks.logicng.solvers.sat.MiniSatStyleSolver.ProofInformation;
import com.booleworks.logicng.util.Pair;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SolverSerializer {
    private final Map<Integer, Function<ByteString, SerializablePropositionBackpack>> deserializers = new HashMap<>();
    private final FormulaFactory f;

    public SolverSerializer(final FormulaFactory f) {
        this.f = f;
    }

    public void registerDeserializer(final PropositionType propositionType, final Function<ByteString, SerializablePropositionBackpack> deserializer) {
        deserializers.put(propositionType.ordinal(), deserializer);
    }

    public void serializeSolverToFile(final MiniSat miniSat, final Path path, final boolean compress) throws IOException {
        try (final OutputStream outputStream = compress ? new GZIPOutputStream(Files.newOutputStream(path)) : Files.newOutputStream(path)) {
            serializeSolverToStream(miniSat, outputStream);
        }
    }

    public void serializeSolverToStream(final MiniSat miniSat, final OutputStream stream) throws IOException {
        if (miniSat.getStyle() != MiniSat.SolverStyle.GLUCOSE) {
            serializeMiniSat(miniSat).writeTo(stream);
        } else {
            serializeGlucose(miniSat).writeTo(stream);
        }
    }

    public PBMiniSat2 serializeMiniSat(final MiniSat miniSat) {
        if (miniSat.getStyle() == MiniSat.SolverStyle.GLUCOSE) {
            throw new IllegalArgumentException("Cannot serialize a " + miniSat.getStyle() + " solver as MiniSat");
        }
        if (miniSat.underlyingSolver() instanceof MiniSat2Solver) {
            return serialize((MiniSat2Solver) miniSat.underlyingSolver(), new SolverWrapperState(miniSat));
        } else if (miniSat.underlyingSolver() instanceof MiniCard) {
            return serialize((MiniCard) miniSat.underlyingSolver(), new SolverWrapperState(miniSat));
        }
        throw new IllegalArgumentException("Unknown solver type " + miniSat.underlyingSolver());
    }

    public PBGlucose serializeGlucose(final MiniSat miniSat) {
        if (miniSat.getStyle() != MiniSat.SolverStyle.GLUCOSE) {
            throw new IllegalArgumentException("Cannot serialize a " + miniSat.getStyle() + " solver as Glucose");
        }
        return serialize((GlucoseSyrup) miniSat.underlyingSolver(), new SolverWrapperState(miniSat));
    }

    public MiniSat deserializeMiniSatFromFile(final Path path, final boolean compress) throws IOException {
        try (final InputStream inputStream = compress ? new GZIPInputStream(Files.newInputStream(path)) : Files.newInputStream(path)) {
            return deserializeMiniSatFromStream(inputStream);
        }
    }

    public MiniSat deserializeMiniSatFromStream(final InputStream stream) throws IOException {
        return deserializeMiniSat(ProtoBufSatSolver.PBMiniSat2.newBuilder().mergeFrom(stream).build());
    }

    public MiniSat deserializeMiniSat(final ProtoBufSatSolver.PBMiniSat2 bin) {
        final MiniSat miniSat = new MiniSat(f, deserialize(bin));
        SolverWrapperState.setWrapperState(miniSat, bin.getWrapper());
        return miniSat;
    }

    public MiniSat deserializeGlucoseFromFile(final Path path, final boolean compress) throws IOException {
        try (final InputStream inputStream = compress ? new GZIPInputStream(Files.newInputStream(path)) : Files.newInputStream(path)) {
            return deserializeGlucoseFromStream(inputStream);
        }
    }

    public MiniSat deserializeGlucoseFromStream(final InputStream stream) throws IOException {
        return deserializeGlucose(ProtoBufSatSolver.PBGlucose.newBuilder().mergeFrom(stream).build());
    }

    public MiniSat deserializeGlucose(final ProtoBufSatSolver.PBGlucose bin) {
        final MiniSat miniSat = new MiniSat(f, deserialize(bin));
        SolverWrapperState.setWrapperState(miniSat, bin.getWrapper());
        return miniSat;
    }

    Pair<ProtoBufSolverCommons.PBMiniSatStyleSolver, IdentityHashMap<MSClause, Integer>> serializeCommon(final MiniSatStyleSolver solver) {
        final IdentityHashMap<MSClause, Integer> clauseMap = generateClauseMap(solver.clauses, solver.learnts);
        final ProtoBufSolverCommons.PBMiniSatStyleSolver.Builder builder = ProtoBufSolverCommons.PBMiniSatStyleSolver.newBuilder();
        builder.setConfig(SatSolverConfigs.serialize(solver.config));
        builder.setOk(solver.ok);
        builder.setQhead(solver.qhead);
        builder.setClauses(serializeClauseVec(solver.clauses, clauseMap));
        builder.setLearnts(serializeClauseVec(solver.learnts, clauseMap));
        builder.setWatches(serializeWatches(solver.watches, clauseMap));
        builder.setVars(serializeVarVec(solver.vars, clauseMap));
        builder.setOrderHeap(SolverDatastructures.serialize(solver.orderHeap));
        builder.setTrail(Collections.serialize(solver.trail));
        builder.setTrailLim(Collections.serialize(solver.trailLim));
        builder.setModel(Collections.serialize(solver.model));
        builder.setConflict(Collections.serialize(solver.conflict));
        builder.setAssumptions(Collections.serialize(solver.assumptions));
        builder.setSeen(Collections.serialize(solver.seen));
        builder.setAnalyzeBtLevel(solver.analyzeBtLevel);
        builder.setClaInc(solver.claInc);
        builder.setSimpDBAssigns(solver.simpDBAssigns);
        builder.setSimpDBProps(solver.simpDBProps);
        builder.setClausesLiterals(solver.clausesLiterals);
        builder.setLearntsLiterals(solver.learntsLiterals);

        builder.setVarDecay(solver.varDecay);
        builder.setVarInc(solver.varInc);
        builder.setCcminMode(SatSolverConfigs.serialize(solver.ccminMode));
        builder.setRestartFirst(solver.restartFirst);
        builder.setRestartInc(solver.restartInc);
        builder.setClauseDecay(solver.clauseDecay);
        builder.setShouldRemoveSatsisfied(solver.shouldRemoveSatsisfied);
        builder.setLearntsizeInc(solver.learntsizeInc);
        builder.setIncremental(solver.incremental);

        builder.putAllName2Idx(solver.name2idx);

        if (solver.pgProof != null) {
            builder.setPgProof(Collections.serialize(solver.pgProof));
        }
        if (solver.pgOriginalClauses != null) {
            for (final ProofInformation oc : solver.pgOriginalClauses) {
                builder.addPgOriginalClauses(serialize(oc));
            }
        }

        if (solver.backboneCandidates != null) {
            builder.setBackboneCandidates(serializeStack(solver.backboneCandidates));
        }
        if (solver.backboneAssumptions != null) {
            builder.setBackboneAssumptions(Collections.serialize(solver.backboneAssumptions));
        }
        if (solver.backboneMap != null) {
            builder.putAllBackboneMap(serializeBbMap(solver.backboneMap));
        }
        builder.setComputingBackbone(solver.computingBackbone);

        builder.setSelectionOrder(Collections.serialize(solver.selectionOrder));
        builder.setSelectionOrderIdx(solver.selectionOrderIdx);

        builder.setLearntsizeAdjustConfl(solver.learntsizeAdjustConfl);
        builder.setLearntsizeAdjustCnt(solver.learntsizeAdjustCnt);
        builder.setLearntsizeAdjustStartConfl(solver.learntsizeAdjustStartConfl);
        builder.setLearntsizeAdjustInc(solver.learntsizeAdjustInc);
        builder.setMaxLearnts(solver.maxLearnts);

        return new Pair<>(builder.build(), clauseMap);
    }

    private Map<Integer, MSClause> deserializeCommon(final ProtoBufSolverCommons.PBMiniSatStyleSolver bin, final MiniSatStyleSolver solver) {
        final Map<Integer, MSClause> clauseMap = new TreeMap<>();
        solver.config = SatSolverConfigs.deserialize(bin.getConfig());
        solver.ok = bin.getOk();
        solver.qhead = bin.getQhead();
        solver.clauses = deserializeClauseVec(bin.getClauses(), clauseMap);
        solver.learnts = deserializeClauseVec(bin.getLearnts(), clauseMap);
        solver.watches = deserializeWatches(bin.getWatches(), clauseMap);
        solver.vars = deserializeVarVec(bin.getVars(), clauseMap);
        solver.orderHeap = SolverDatastructures.deserialize(bin.getOrderHeap(), solver);
        solver.trail = Collections.deserialize(bin.getTrail());
        solver.trailLim = Collections.deserialize(bin.getTrailLim());
        solver.model = Collections.deserialize(bin.getModel());
        solver.conflict = Collections.deserialize(bin.getConflict());
        solver.assumptions = Collections.deserialize(bin.getAssumptions());
        solver.seen = Collections.deserialize(bin.getSeen());
        solver.analyzeBtLevel = bin.getAnalyzeBtLevel();
        solver.claInc = bin.getClaInc();
        solver.simpDBAssigns = bin.getSimpDBAssigns();
        solver.simpDBProps = bin.getSimpDBProps();
        solver.clausesLiterals = bin.getClausesLiterals();
        solver.learntsLiterals = bin.getLearntsLiterals();

        solver.varDecay = bin.getVarDecay();
        solver.varInc = bin.getVarInc();
        solver.ccminMode = SatSolverConfigs.deserialize(bin.getCcminMode());
        solver.restartFirst = bin.getRestartFirst();
        solver.restartInc = bin.getRestartInc();
        solver.clauseDecay = bin.getClauseDecay();
        solver.shouldRemoveSatsisfied = bin.getShouldRemoveSatsisfied();
        solver.learntsizeInc = bin.getLearntsizeInc();
        solver.incremental = bin.getIncremental();

        solver.name2idx = new TreeMap<>(bin.getName2IdxMap());
        solver.idx2name = new TreeMap<>();
        solver.name2idx.forEach((k, v) -> solver.idx2name.put(v, k));

        if (bin.hasPgProof()) {
            solver.pgProof = Collections.deserialize(bin.getPgProof());
        }
        if (bin.getPgOriginalClausesCount() > 0) {
            solver.pgOriginalClauses = new LNGVector<>(bin.getPgOriginalClausesCount());
            for (final PBProofInformation pi : bin.getPgOriginalClausesList()) {
                solver.pgOriginalClauses.push(deserialize(pi));
            }
        }

        if (bin.hasBackboneCandidates()) {
            solver.backboneCandidates = deserializeStack(bin.getBackboneCandidates());
        }
        if (bin.hasBackboneAssumptions()) {
            solver.backboneAssumptions = Collections.deserialize(bin.getBackboneAssumptions());
        }
        solver.backboneMap = deserializeBbMap(bin.getBackboneMapMap());
        solver.computingBackbone = bin.getComputingBackbone();

        solver.selectionOrder = Collections.deserialize(bin.getSelectionOrder());
        solver.selectionOrderIdx = bin.getSelectionOrderIdx();

        solver.learntsizeAdjustConfl = bin.getLearntsizeAdjustConfl();
        solver.learntsizeAdjustCnt = bin.getLearntsizeAdjustCnt();
        solver.learntsizeAdjustStartConfl = bin.getLearntsizeAdjustStartConfl();
        solver.learntsizeAdjustInc = bin.getLearntsizeAdjustInc();
        solver.maxLearnts = bin.getMaxLearnts();

        return clauseMap;
    }

    PBMiniSat2 serialize(final MiniSat2Solver solver, final SolverWrapperState wrapperState) {
        return PBMiniSat2.newBuilder()
                .setCommon(serializeCommon(solver).first())
                .setWrapper(serializeWrapperState(wrapperState))
                .setUnitClauses(Collections.serialize(solver.unitClauses))
                .build();
    }

    PBMiniSat2 serialize(final MiniCard solver, final SolverWrapperState wrapperState) {
        return PBMiniSat2.newBuilder()
                .setCommon(serializeCommon(solver).first())
                .setWrapper(serializeWrapperState(wrapperState))
                .setUnitClauses(Collections.serialize(solver.unitClauses))
                .build();
    }

    MiniSat2Solver deserialize(final PBMiniSat2 bin) {
        final MiniSat2Solver solver = new MiniSat2Solver(SatSolverConfigs.deserialize(bin.getCommon().getConfig()));
        deserializeCommon(bin.getCommon(), solver);
        solver.unitClauses = Collections.deserialize(bin.getUnitClauses());
        return solver;
    }

    PBGlucose serialize(final GlucoseSyrup solver, final SolverWrapperState wrapperState) {
        final Pair<ProtoBufSolverCommons.PBMiniSatStyleSolver, IdentityHashMap<MSClause, Integer>> common = serializeCommon(solver);
        return PBGlucose.newBuilder()
                .setGlucoseConfig(SatSolverConfigs.serialize(solver.glucoseConfig))
                .setCommon(common.first())
                .setWrapper(serializeWrapperState(wrapperState))
                .setWatchesBin(serializeWatches(solver.watchesBin, common.second()))
                .setPermDiff(Collections.serialize(solver.permDiff))
                .setLastDecisionLevel(Collections.serialize(solver.lastDecisionLevel))
                .setLbdQueue(SolverDatastructures.serialize(solver.lbdQueue))
                .setTrailQueue(SolverDatastructures.serialize(solver.trailQueue))
                .setAssump(Collections.serialize(solver.assump))
                .setMyflag(solver.myflag)
                .setAnalyzeLBD(solver.analyzeLBD)
                .setAnalyzeSzWithoutSelectors(solver.analyzeSzWithoutSelectors)
                .setNbclausesbeforereduce(solver.nbclausesbeforereduce)
                .setConflicts(solver.conflicts)
                .setConflictsRestarts(solver.conflictsRestarts)
                .setSumLBD(solver.sumLBD)
                .setCurRestart(solver.curRestart)
                .build();
    }

    private GlucoseSyrup deserialize(final PBGlucose bin) {
        final GlucoseSyrup solver = new GlucoseSyrup(SatSolverConfigs.deserialize(bin.getCommon().getConfig()), SatSolverConfigs.deserialize(bin.getGlucoseConfig()));
        final Map<Integer, MSClause> clauseMap = deserializeCommon(bin.getCommon(), solver);
        solver.watchesBin = deserializeWatches(bin.getWatchesBin(), clauseMap);
        solver.permDiff = Collections.deserialize(bin.getPermDiff());
        solver.lastDecisionLevel = Collections.deserialize(bin.getLastDecisionLevel());
        solver.lbdQueue = SolverDatastructures.deserialize(bin.getLbdQueue());
        solver.trailQueue = SolverDatastructures.deserialize(bin.getTrailQueue());
        solver.assump = Collections.deserialize(bin.getAssump());
        solver.myflag = bin.getMyflag();
        solver.analyzeLBD = bin.getAnalyzeLBD();
        solver.analyzeSzWithoutSelectors = bin.getAnalyzeSzWithoutSelectors();
        solver.nbclausesbeforereduce = bin.getNbclausesbeforereduce();
        solver.conflicts = bin.getConflicts();
        solver.conflictsRestarts = bin.getConflictsRestarts();
        solver.sumLBD = bin.getSumLBD();
        solver.curRestart = bin.getCurRestart();
        return solver;
    }

    private static IdentityHashMap<MSClause, Integer> generateClauseMap(final LNGVector<MSClause> clauses, final LNGVector<MSClause> learnts) {
        final IdentityHashMap<MSClause, Integer> clauseMap = new IdentityHashMap<>();
        for (final MSClause clause : clauses) {
            clauseMap.put(clause, clauseMap.size());
        }
        for (final MSClause learnt : learnts) {
            clauseMap.put(learnt, clauseMap.size());
        }
        return clauseMap;
    }

    private static ProtoBufSolverDatastructures.PBMsClauseVector serializeClauseVec(final LNGVector<MSClause> vec, final IdentityHashMap<MSClause, Integer> clauseMap) {
        final ProtoBufSolverDatastructures.PBMsClauseVector.Builder builder = ProtoBufSolverDatastructures.PBMsClauseVector.newBuilder();
        for (final MSClause clause : vec) {
            builder.addElement(SolverDatastructures.serialize(clause, clauseMap.get(clause)));
        }
        return builder.build();
    }

    private static LNGVector<MSClause> deserializeClauseVec(final ProtoBufSolverDatastructures.PBMsClauseVector bin, final Map<Integer, MSClause> clauseMap) {
        final LNGVector<MSClause> vec = new LNGVector<>(bin.getElementCount());
        for (int i = 0; i < bin.getElementCount(); i++) {
            final ProtoBufSolverDatastructures.PBMsClause binClause = bin.getElement(i);
            final MSClause clause = SolverDatastructures.deserialize(binClause);
            clauseMap.put(binClause.getId(), clause);
            vec.push(clause);
        }
        return vec;
    }

    private static ProtoBufSolverDatastructures.PBMsWatcherVectorVector serializeWatches(final LNGVector<LNGVector<MSWatcher>> vec, final IdentityHashMap<MSClause, Integer> clauseMap) {
        final ProtoBufSolverDatastructures.PBMsWatcherVectorVector.Builder builder = ProtoBufSolverDatastructures.PBMsWatcherVectorVector.newBuilder();
        for (final LNGVector<MSWatcher> watchList : vec) {
            final ProtoBufSolverDatastructures.PBMsWatcherVector.Builder watchBuilder = ProtoBufSolverDatastructures.PBMsWatcherVector.newBuilder();
            for (final MSWatcher watch : watchList) {
                watchBuilder.addElement(SolverDatastructures.serialize(watch, clauseMap));
            }
            builder.addElement(watchBuilder.build());
        }
        return builder.build();
    }

    private static LNGVector<LNGVector<MSWatcher>> deserializeWatches(final ProtoBufSolverDatastructures.PBMsWatcherVectorVector bin, final Map<Integer, MSClause> clauseMap) {
        final LNGVector<LNGVector<MSWatcher>> vec = new LNGVector<>(bin.getElementCount());
        for (int i = 0; i < bin.getElementCount(); i++) {
            final ProtoBufSolverDatastructures.PBMsWatcherVector binWatch = bin.getElement(i);
            final LNGVector<MSWatcher> watch = new LNGVector<>(binWatch.getElementCount());
            for (int j = 0; j < binWatch.getElementCount(); j++) {
                watch.push(SolverDatastructures.deserialize(binWatch.getElement(j), clauseMap));
            }
            vec.push(watch);
        }
        return vec;
    }

    private static ProtoBufSolverDatastructures.PBMsVariableVector serializeVarVec(final LNGVector<MSVariable> vec, final IdentityHashMap<MSClause, Integer> clauseMap) {
        final ProtoBufSolverDatastructures.PBMsVariableVector.Builder builder = ProtoBufSolverDatastructures.PBMsVariableVector.newBuilder();
        for (final MSVariable var : vec) {
            builder.addElement(SolverDatastructures.serialize(var, clauseMap));
        }
        return builder.build();
    }

    private static LNGVector<MSVariable> deserializeVarVec(final ProtoBufSolverDatastructures.PBMsVariableVector bin, final Map<Integer, MSClause> clauseMap) {
        final LNGVector<MSVariable> vec = new LNGVector<>(bin.getElementCount());
        for (int i = 0; i < bin.getElementCount(); i++) {
            vec.push(SolverDatastructures.deserialize(bin.getElement(i), clauseMap));
        }
        return vec;
    }

    static ProtoBufCollections.PBIntVector serializeStack(final Stack<Integer> stack) {
        if (stack == null) {
            return null;
        }
        final ProtoBufCollections.PBIntVector.Builder vec = ProtoBufCollections.PBIntVector.newBuilder();
        for (final Integer integer : stack) {
            vec.addElement(integer);
        }
        vec.setSize(stack.size());
        return vec.build();
    }

    static Stack<Integer> deserializeStack(final ProtoBufCollections.PBIntVector vec) {
        final Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < vec.getSize(); i++) {
            stack.push(vec.getElement(i));
        }
        return stack;
    }

    private static HashMap<Integer, ProtoBufSolverDatastructures.PBTristate> serializeBbMap(final Map<Integer, Tristate> map) {
        final HashMap<Integer, ProtoBufSolverDatastructures.PBTristate> ser = new HashMap<>();
        map.forEach((k, v) -> ser.put(k, SolverDatastructures.serialize(v)));
        return ser;
    }

    private static HashMap<Integer, Tristate> deserializeBbMap(final Map<Integer, ProtoBufSolverDatastructures.PBTristate> map) {
        if (map.isEmpty()) {
            return null;
        }
        final HashMap<Integer, Tristate> ser = new HashMap<>();
        map.forEach((k, v) -> ser.put(k, SolverDatastructures.deserialize(v)));
        return ser;
    }

    private static ProtoBufSatSolver.PBWrapperState serializeWrapperState(final SolverWrapperState state) {
        return ProtoBufSatSolver.PBWrapperState.newBuilder()
                .setResult(SolverDatastructures.serialize(state.getResult()))
                .setValidStates(Collections.serialize(state.getValidStates()))
                .setNextStateId(state.getNextStateId())
                .setLastComputationWithAssumptions(state.isLastComputationWithAssumptions())
                .setSolverStyle(SolverWrapperState.serialize(state.getSolverStyle()))
                .build();
    }

    private static PBProofInformation serialize(final ProofInformation pi) {
        final var builder = PBProofInformation.newBuilder().setClause(Collections.serialize(pi.clause));
        if (pi.proposition != null) {
            builder.setProposition(Propositions.serialize(pi.proposition));
        }
        return builder.build();
    }

    private ProofInformation deserialize(final PBProofInformation bin) {
        final var prop = bin.hasProposition() ? Propositions.deserialize(f, bin.getProposition(), deserializers) : null;
        return new ProofInformation(Collections.deserialize(bin.getClause()), prop);
    }
}
