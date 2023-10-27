package ar.edu.itba.grammar.struct;

import ar.edu.itba.grammar.TempoGraphParser;
import ar.edu.itba.util.Utils;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.HashMap;
import java.util.Map;

public class ProcedureConfiguration {

    private static final String EDGES_LABEL = "edgesLabel";
    private static final String BETWEEN = "between";
    private static final String NODES_LABEL = "nodesLabel";
    private static final String DIRECTION = "direction";
    private static final String DIRECTION_INCOMING = "incoming";
    private static final String DIRECTION_OUTGOING = "outgoing";
    private static final String DIRECTION_BOTH = "both";
    private static final String ATTRIBUTE = "attribute";
    private static final String OPERATOR = "operator";
    private static final String CATEGORY = "category";
    private static final String DELTA = "delta";

    private Map<String, String> args = new HashMap<>();
    private TempoGraphParser.Endpoints_argsContext ctx;
    private Long minLength;
    private Long maxLength;

    public ProcedureConfiguration(TempoGraphParser.Endpoints_argsContext ctx) {
        this.ctx = ctx;
        this.generateMap();
    }

    private void generateMap() {
        getDirectionFromRelationEdge();
        getEdgeType();
        getPathLength();
        getNodesLabel();
    }

    private void getDirectionFromRelationEdge() {
        String direction = DIRECTION_BOTH;
        if (ctx.relation_edge().LEFT() != null) {
            direction = DIRECTION_INCOMING;
        } else if (ctx.relation_edge().RIGHT() != null) {
            direction = DIRECTION_OUTGOING;
        }
        this.addStringParam(DIRECTION, direction);
    }

    private void getEdgeType() {
        TempoGraphParser.Edge_defContext edgeCtx = ctx.relation_edge().edge_def();
        if (edgeCtx.COLON() == null) {
            return;
        }
        String edgeType = edgeCtx.WORD(edgeCtx.WORD().size() - 1).getText();
        this.addStringParam(EDGES_LABEL, edgeType);
    }

    private void getPathLength() {
        TempoGraphParser.Edge_defContext edgeCtx = ctx.relation_edge().edge_def();
        if (edgeCtx.variable_length() == null) {
            minLength = 1L;
            maxLength = 1L;
            return;
        }
        TempoGraphParser.Variable_lengthContext vLength = edgeCtx.variable_length();
        if (vLength.U_INTEGER().isEmpty())
            return;
        if (vLength.DOUBLE_DOT() != null) {
            if (vLength.U_INTEGER().size() == 1) {
                this.minLength = 1L;
                this.maxLength = Long.parseLong(vLength.U_INTEGER(0).getText());
            } else {
                this.minLength = Long.parseLong(vLength.U_INTEGER(0).getText());
                this.maxLength = Long.parseLong(vLength.U_INTEGER(1).getText());
            }
        } else {
            this.maxLength = Long.parseLong(vLength.U_INTEGER(0).getText());
            this.minLength = maxLength;
        }
        if (minLength > maxLength)
            throw new ParseCancellationException(Utils.generateErrorMessage("invalid function variable length range", ctx.relation_edge().edge_def().COLON().getSymbol()));
    }

    private void getNodesLabel() {
        if (ctx.object().size() != 2 ||
                ctx.object().get(1).object_def() == null) {
            return;
        }
        TempoGraphParser.Object_defContext object_defContext =
                ctx.object().get(1).object_def();
        if (object_defContext.WORD().size() != 1) {
            return;
        }
        this.addStringParam(NODES_LABEL, object_defContext.WORD(0).getText());
    }

    private void addStringParam(String key, String value) {
        args.put(key, "'" + value + "'");
    }
    

    public void addTemporalInterval(final String interval) {
        this.addStringParam(BETWEEN, interval);
    }

    public void addAttribute(final String attribute) {
        this.addStringParam(ATTRIBUTE, attribute);
    }

    public void addOperator(final String operator) {
        this.addStringParam(OPERATOR, operator);
    }

    public void addCategory(final String category) {
        this.addStringParam(CATEGORY, category);
    }

    public void addDelta(final String delta) {
        this.addStringParam(DELTA, delta);
    }

    public String getConfigurationString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CharConstants.OPENING_BRACKET);
        boolean appendComma = false;
        for (String key: args.keySet()) {
            if (!appendComma)
                appendComma = true;
            else
                sb.append(CharConstants.COMMA);
            sb.append(key)
                .append(":")
                .append(this.args.get(key));
        }
        sb.append(CharConstants.CLOSING_BRACKET);
        return sb.toString();
    }

    public String getEdgesLabel () {
        return this.args.get(EDGES_LABEL);
    }

    public String getBetweenPeriod () {
        return this.args.get(BETWEEN);
    }

    public Long getMinLength() {
        return minLength;
    }

    public Long getMaxLength() {
        return maxLength;
    }
}
