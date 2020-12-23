package com.dfsek.terra.api.structures.parser;

import com.dfsek.terra.api.structures.parser.exceptions.ParseException;
import com.dfsek.terra.api.structures.parser.lang.Block;
import com.dfsek.terra.api.structures.parser.lang.Item;
import com.dfsek.terra.api.structures.parser.lang.Keyword;
import com.dfsek.terra.api.structures.parser.lang.Returnable;
import com.dfsek.terra.api.structures.parser.lang.constants.BooleanConstant;
import com.dfsek.terra.api.structures.parser.lang.constants.ConstantExpression;
import com.dfsek.terra.api.structures.parser.lang.constants.NumericConstant;
import com.dfsek.terra.api.structures.parser.lang.constants.StringConstant;
import com.dfsek.terra.api.structures.parser.lang.functions.Function;
import com.dfsek.terra.api.structures.parser.lang.functions.FunctionBuilder;
import com.dfsek.terra.api.structures.parser.lang.keywords.BreakKeyword;
import com.dfsek.terra.api.structures.parser.lang.keywords.ContinueKeyword;
import com.dfsek.terra.api.structures.parser.lang.keywords.IfKeyword;
import com.dfsek.terra.api.structures.parser.lang.keywords.ReturnKeyword;
import com.dfsek.terra.api.structures.parser.lang.keywords.WhileKeyword;
import com.dfsek.terra.api.structures.parser.lang.operations.BinaryOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.BooleanAndOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.BooleanNotOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.BooleanOrOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.ConcatenationOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.DivisionOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.MultiplicationOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.NumberAdditionOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.SubtractionOperation;
import com.dfsek.terra.api.structures.parser.lang.operations.statements.EqualsStatement;
import com.dfsek.terra.api.structures.parser.lang.operations.statements.GreaterOrEqualsThanStatement;
import com.dfsek.terra.api.structures.parser.lang.operations.statements.GreaterThanStatement;
import com.dfsek.terra.api.structures.parser.lang.operations.statements.LessThanOrEqualsStatement;
import com.dfsek.terra.api.structures.parser.lang.operations.statements.LessThanStatement;
import com.dfsek.terra.api.structures.parser.lang.operations.statements.NotEqualsStatement;
import com.dfsek.terra.api.structures.parser.lang.variables.Assignment;
import com.dfsek.terra.api.structures.parser.lang.variables.BooleanVariable;
import com.dfsek.terra.api.structures.parser.lang.variables.Getter;
import com.dfsek.terra.api.structures.parser.lang.variables.NumberVariable;
import com.dfsek.terra.api.structures.parser.lang.variables.StringVariable;
import com.dfsek.terra.api.structures.parser.lang.variables.Variable;
import com.dfsek.terra.api.structures.tokenizer.Position;
import com.dfsek.terra.api.structures.tokenizer.Token;
import com.dfsek.terra.api.structures.tokenizer.Tokenizer;
import com.dfsek.terra.api.structures.tokenizer.exceptions.TokenizerException;
import com.dfsek.terra.api.util.GlueList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final String data;
    private final Map<String, FunctionBuilder<? extends Function<?>>> functions = new HashMap<>();
    private String id;

    public Parser(String data) {
        this.data = data;
    }

    public Parser addFunction(String name, FunctionBuilder<? extends Function<?>> functionBuilder) {
        functions.put(name, functionBuilder);
        return this;
    }

    public String getID() {
        return id;
    }

    /**
     * Parse input
     *
     * @return executable {@link Block}
     * @throws ParseException If parsing fails.
     */
    public Block parse() throws ParseException {
        Tokenizer tokenizer = new Tokenizer(data);

        List<Token> tokens = new GlueList<>();
        try {
            while(tokenizer.hasNext()) tokens.add(tokenizer.fetch());
        } catch(TokenizerException e) {
            throw new ParseException("Failed to tokenize input", e);
        }

        // Parse ID
        ParserUtil.checkType(tokens.remove(0), Token.Type.ID); // First token must be ID
        Token idToken = tokens.get(0);
        ParserUtil.checkType(tokens.remove(0), Token.Type.STRING); // Second token must be string literal containing ID
        ParserUtil.checkType(tokens.remove(0), Token.Type.STATEMENT_END);
        this.id = idToken.getContent();

        // Check for dangling brackets
        int blockLevel = 0;
        for(Token t : tokens) {
            if(t.getType().equals(Token.Type.BLOCK_BEGIN)) blockLevel++;
            else if(t.getType().equals(Token.Type.BLOCK_END)) blockLevel--;
            if(blockLevel < 0) throw new ParseException("Dangling closing brace: " + t.getPosition());
        }
        if(blockLevel != 0) throw new ParseException("Dangling opening brace");

        return parseBlock(tokens, new HashMap<>());
    }


    @SuppressWarnings("unchecked")
    private Keyword<?> parseLoopLike(List<Token> tokens, Map<String, Variable<?>> variableMap) throws ParseException {

        Token identifier = tokens.remove(0);
        ParserUtil.checkType(identifier, Token.Type.IF_STATEMENT, Token.Type.WHILE_LOOP);

        ParserUtil.checkType(tokens.remove(0), Token.Type.GROUP_BEGIN);

        Returnable<?> comparator = parseExpression(tokens, true, variableMap);
        ParserUtil.checkReturnType(comparator, Returnable.ReturnType.BOOLEAN);

        ParserUtil.checkType(tokens.remove(0), Token.Type.GROUP_END);

        ParserUtil.checkType(tokens.remove(0), Token.Type.BLOCK_BEGIN);

        if(identifier.getType().equals(Token.Type.IF_STATEMENT))
            return new IfKeyword(parseBlock(tokens, variableMap), (Returnable<Boolean>) comparator, identifier.getPosition()); // If statement
        else if(identifier.getType().equals(Token.Type.WHILE_LOOP))
            return new WhileKeyword(parseBlock(tokens, variableMap), (Returnable<Boolean>) comparator, identifier.getPosition()); // While loop
        else throw new UnsupportedOperationException("Unknown keyword " + identifier.getContent() + ": " + identifier.getPosition());
    }

    @SuppressWarnings("unchecked")
    private Returnable<?> parseExpression(List<Token> tokens, boolean full, Map<String, Variable<?>> variableMap) throws ParseException {
        boolean booleanInverted = false; // Check for boolean not operator
        if(tokens.get(0).getType().equals(Token.Type.BOOLEAN_NOT)) {
            booleanInverted = true;
            tokens.remove(0);
        }

        Token id = tokens.get(0);

        ParserUtil.checkType(id, Token.Type.IDENTIFIER, Token.Type.BOOLEAN, Token.Type.STRING, Token.Type.NUMBER, Token.Type.GROUP_BEGIN);

        Returnable<?> expression;
        if(id.isConstant()) {
            expression = parseConstantExpression(tokens);
        } else if(id.getType().equals(Token.Type.GROUP_BEGIN)) { // Parse grouped expression
            expression = parseGroup(tokens, variableMap);
        } else {
            if(functions.containsKey(id.getContent())) expression = parseFunction(tokens, false, variableMap);
            else if(variableMap.containsKey(id.getContent())) {
                ParserUtil.checkType(tokens.remove(0), Token.Type.IDENTIFIER);
                expression = new Getter(variableMap.get(id.getContent()));
            } else throw new ParseException("Unexpected token: " + id.getContent() + " at " + id.getPosition());
        }

        if(booleanInverted) { // Invert operation if boolean not detected
            ParserUtil.checkReturnType(expression, Returnable.ReturnType.BOOLEAN);
            expression = new BooleanNotOperation((Returnable<Boolean>) expression, expression.getPosition());
        }

        if(full && tokens.get(0).isBinaryOperator()) { // Parse binary operations
            return parseBinaryOperation(expression, tokens, variableMap);
        }
        return expression;
    }

    private ConstantExpression<?> parseConstantExpression(List<Token> tokens) {
        Token constantToken = tokens.remove(0);
        Position position = constantToken.getPosition();
        switch(constantToken.getType()) {
            case NUMBER:
                String content = constantToken.getContent();
                return new NumericConstant(content.contains(".") ? Double.parseDouble(content) : Integer.parseInt(content), position);
            case STRING:
                return new StringConstant(constantToken.getContent(), position);
            case BOOLEAN:
                return new BooleanConstant(Boolean.parseBoolean(constantToken.getContent()), position);
            default:
                throw new UnsupportedOperationException("Unsupported constant token: " + constantToken.getType() + " at position: " + position);
        }
    }

    private Returnable<?> parseGroup(List<Token> tokens, Map<String, Variable<?>> variableMap) throws ParseException {
        ParserUtil.checkType(tokens.remove(0), Token.Type.GROUP_BEGIN);
        Returnable<?> expression = parseExpression(tokens, true, variableMap); // Parse inside of group as a separate expression
        ParserUtil.checkType(tokens.remove(0), Token.Type.GROUP_END);
        return expression;
    }


    private BinaryOperation<?, ?> parseBinaryOperation(Returnable<?> left, List<Token> tokens, Map<String, Variable<?>> variableMap) throws ParseException {
        Token binaryOperator = tokens.remove(0);
        ParserUtil.checkBinaryOperator(binaryOperator);

        Returnable<?> right = parseExpression(tokens, false, variableMap);

        Token other = tokens.get(0);
        if(other.isBinaryOperator() && (other.getType().equals(Token.Type.MULTIPLICATION_OPERATOR) || other.getType().equals(Token.Type.DIVISION_OPERATOR))) {
            return assemble(left, parseBinaryOperation(right, tokens, variableMap), binaryOperator);
        } else if(other.isBinaryOperator()) {
            return parseBinaryOperation(assemble(left, right, binaryOperator), tokens, variableMap);
        }
        return assemble(left, right, binaryOperator);
    }

    @SuppressWarnings("unchecked")
    private BinaryOperation<?, ?> assemble(Returnable<?> left, Returnable<?> right, Token binaryOperator) throws ParseException {
        if(binaryOperator.isStrictNumericOperator())
            ParserUtil.checkArithmeticOperation(left, right, binaryOperator); // Numeric type checking
        if(binaryOperator.isStrictBooleanOperator()) ParserUtil.checkBooleanOperation(left, right, binaryOperator); // Boolean type checking
        switch(binaryOperator.getType()) {
            case ADDITION_OPERATOR:
                if(left.returnType().equals(Returnable.ReturnType.NUMBER) && right.returnType().equals(Returnable.ReturnType.NUMBER)) {
                    return new NumberAdditionOperation((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
                }
                return new ConcatenationOperation((Returnable<Object>) left, (Returnable<Object>) right, binaryOperator.getPosition());
            case SUBTRACTION_OPERATOR:
                return new SubtractionOperation((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
            case MULTIPLICATION_OPERATOR:
                return new MultiplicationOperation((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
            case DIVISION_OPERATOR:
                return new DivisionOperation((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
            case EQUALS_OPERATOR:
                return new EqualsStatement((Returnable<Object>) left, (Returnable<Object>) right, binaryOperator.getPosition());
            case NOT_EQUALS_OPERATOR:
                return new NotEqualsStatement((Returnable<Object>) left, (Returnable<Object>) right, binaryOperator.getPosition());
            case GREATER_THAN_OPERATOR:
                return new GreaterThanStatement((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
            case LESS_THAN_OPERATOR:
                return new LessThanStatement((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
            case GREATER_THAN_OR_EQUALS_OPERATOR:
                return new GreaterOrEqualsThanStatement((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
            case LESS_THAN_OR_EQUALS_OPERATOR:
                return new LessThanOrEqualsStatement((Returnable<Number>) left, (Returnable<Number>) right, binaryOperator.getPosition());
            case BOOLEAN_AND:
                return new BooleanAndOperation((Returnable<Boolean>) left, (Returnable<Boolean>) right, binaryOperator.getPosition());
            case BOOLEAN_OR:
                return new BooleanOrOperation((Returnable<Boolean>) left, (Returnable<Boolean>) right, binaryOperator.getPosition());
            default:
                throw new UnsupportedOperationException("Unsupported binary operator: " + binaryOperator.getType());
        }
    }

    private Variable<?> parseVariableDeclaration(List<Token> tokens, Returnable.ReturnType type) throws ParseException {
        ParserUtil.checkVarType(tokens.get(0), type); // Check for type mismatch
        switch(type) {
            case NUMBER:
                return new NumberVariable(0d, tokens.get(0).getPosition());
            case STRING:
                return new StringVariable("", tokens.get(0).getPosition());
            case BOOLEAN:
                return new BooleanVariable(false, tokens.get(0).getPosition());
        }
        throw new UnsupportedOperationException("Unsupported variable type: " + type);
    }

    private Block parseBlock(List<Token> tokens, Map<String, Variable<?>> superVars) throws ParseException {
        List<Item<?>> parsedItems = new GlueList<>();

        Map<String, Variable<?>> parsedVariables = new HashMap<>(superVars); // New hashmap as to not mutate parent scope's declarations.

        Token first = tokens.get(0);

        while(tokens.size() > 0) {
            Token token = tokens.get(0);
            if(token.getType().equals(Token.Type.BLOCK_END)) break; // Stop parsing at block end.

            ParserUtil.checkType(token, Token.Type.IDENTIFIER, Token.Type.IF_STATEMENT, Token.Type.WHILE_LOOP, Token.Type.NUMBER_VARIABLE, Token.Type.STRING_VARIABLE, Token.Type.BOOLEAN_VARIABLE, Token.Type.RETURN, Token.Type.BREAK, Token.Type.CONTINUE);

            if(token.isLoopLike()) { // Parse loop-like tokens (if, while, etc)
                parsedItems.add(parseLoopLike(tokens, parsedVariables));
            } else if(token.isIdentifier()) { // Parse identifiers
                if(parsedVariables.containsKey(token.getContent())) { // Assume variable assignment
                    Variable<?> variable = parsedVariables.get(token.getContent());
                    parsedItems.add(parseAssignment(variable, tokens, parsedVariables));
                } else parsedItems.add(parseFunction(tokens, true, parsedVariables));
            } else if(token.isVariableDeclaration()) {
                Variable<?> temp;
                if(token.getType().equals(Token.Type.NUMBER_VARIABLE))
                    temp = parseVariableDeclaration(tokens, Returnable.ReturnType.NUMBER);
                else if(token.getType().equals(Token.Type.STRING_VARIABLE))
                    temp = parseVariableDeclaration(tokens, Returnable.ReturnType.STRING);
                else temp = parseVariableDeclaration(tokens, Returnable.ReturnType.BOOLEAN);
                Token name = tokens.get(1);

                ParserUtil.checkType(name, Token.Type.IDENTIFIER); // Name must be an identifier.

                if(functions.containsKey(name.getContent()) || parsedVariables.containsKey(name.getContent()))
                    throw new ParseException(name.getContent() + " is already defined in this scope: " + name.getPosition());

                parsedVariables.put(name.getContent(), temp);

                ParserUtil.checkType(tokens.remove(0), Token.Type.STRING_VARIABLE, Token.Type.BOOLEAN_VARIABLE, Token.Type.NUMBER_VARIABLE);

                parsedItems.add(parseAssignment(temp, tokens, parsedVariables));
            } else if(token.getType().equals(Token.Type.RETURN)) parsedItems.add(new ReturnKeyword(tokens.remove(0).getPosition()));
            else if(token.getType().equals(Token.Type.BREAK)) parsedItems.add(new BreakKeyword(tokens.remove(0).getPosition()));
            else if(token.getType().equals(Token.Type.CONTINUE)) parsedItems.add(new ContinueKeyword(tokens.remove(0).getPosition()));
            else throw new UnsupportedOperationException("Unexpected token " + token.getType() + ": " + token.getPosition());

            if(!tokens.isEmpty()) ParserUtil.checkType(tokens.remove(0), Token.Type.STATEMENT_END, Token.Type.BLOCK_END);
        }
        return new Block(parsedItems, first.getPosition());
    }

    @SuppressWarnings("unchecked")
    private Assignment<?> parseAssignment(Variable<?> variable, List<Token> tokens, Map<String, Variable<?>> variableMap) throws ParseException {
        Token name = tokens.get(0);

        ParserUtil.checkType(tokens.remove(0), Token.Type.IDENTIFIER);

        ParserUtil.checkType(tokens.remove(0), Token.Type.ASSIGNMENT);

        Returnable<?> expression = parseExpression(tokens, true, variableMap);

        ParserUtil.checkReturnType(expression, variable.getType());

        return new Assignment<>((Variable<Object>) variable, (Returnable<Object>) expression, name.getPosition());
    }

    private Function<?> parseFunction(List<Token> tokens, boolean fullStatement, Map<String, Variable<?>> variableMap) throws ParseException {
        Token identifier = tokens.remove(0);
        ParserUtil.checkType(identifier, Token.Type.IDENTIFIER); // First token must be identifier

        if(!functions.containsKey(identifier.getContent()))
            throw new ParseException("No such function " + identifier.getContent() + ": " + identifier.getPosition());

        ParserUtil.checkType(tokens.remove(0), Token.Type.GROUP_BEGIN); // Second is body begin


        List<Returnable<?>> args = getArgs(tokens, variableMap); // Extract arguments, consume the rest.

        tokens.remove(0); // Remove body end

        if(fullStatement) ParserUtil.checkType(tokens.get(0), Token.Type.STATEMENT_END);

        FunctionBuilder<?> builder = functions.get(identifier.getContent());

        if(builder.argNumber() != -1 && args.size() != builder.argNumber())
            throw new ParseException("Expected " + builder.argNumber() + " arguments, found " + args.size() + ": " + identifier.getPosition());

        for(int i = 0; i < args.size(); i++) {
            Returnable<?> argument = args.get(i);
            if(builder.getArgument(i) == null)
                throw new ParseException("Unexpected argument at position " + i + " in function " + identifier.getContent() + ": " + identifier.getPosition());
            ParserUtil.checkReturnType(argument, builder.getArgument(i));
        }
        return builder.build(args, identifier.getPosition());
    }


    private List<Returnable<?>> getArgs(List<Token> tokens, Map<String, Variable<?>> variableMap) throws ParseException {
        List<Returnable<?>> args = new GlueList<>();

        while(!tokens.get(0).getType().equals(Token.Type.GROUP_END)) {
            args.add(parseExpression(tokens, true, variableMap));
            ParserUtil.checkType(tokens.get(0), Token.Type.SEPARATOR, Token.Type.GROUP_END);
            if(tokens.get(0).getType().equals(Token.Type.SEPARATOR)) tokens.remove(0);
        }
        return args;
    }
}
