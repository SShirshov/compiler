package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import java.lang.reflect.Array;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.ArrayList;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch (RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================
    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }
    private Statement parseForStatement() {
        if (tokens.match(FOR)) {
            //parse for loop
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            //require identifier
//            String varName = require(IDENTIFIER, forStatement).toString();
//            forStatement.setVariableName(varName);
            forStatement.setVariableName(tokens.getCurrentToken().getStringValue());
            tokens.consumeToken();
//            System.out.println(forStatement.getVariableName());
            //require in for(x in ...)
            require(IN, forStatement);
            forStatement.setExpression(parseExpression());
            System.out.println(forStatement.getExpression());
            require(RIGHT_PAREN, forStatement);
            ArrayList <Statement> statements = new ArrayList<>();
            require(LEFT_BRACE, forStatement);
            //carson
            while(tokens.hasMoreTokens() && !tokens.match(RIGHT_BRACE)){
//                if (!tokens.match(EOF)){
//                    break;
//                }
                Statement Statement = parseStatement();
                statements.add(Statement);
            }
            //carson
            forStatement.setBody(statements);
            forStatement.setEnd(require(RIGHT_BRACE,forStatement));
            return forStatement;
        } else {
            return null;
        }
    }
    private Statement parseIfStatement() {
        if (tokens.match(IF)) {

            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, ifStatement);
            ifStatement.setExpression(parseExpression());
//            tokens.consumeToken();
//            System.out.println(ifStatement.getExpression());
            require(RIGHT_PAREN, ifStatement);
            ArrayList <Statement> statements = new ArrayList<>();
            require(LEFT_BRACE, ifStatement);
            while(!tokens.match(EOF) && !tokens.match(RIGHT_BRACE)){
                Statement newStatement = parseStatement();
                statements.add(newStatement);
            }
            ifStatement.setTrueStatements(statements);
            Token brace = require(RIGHT_BRACE,ifStatement);
//            require(LEFT_BRACKET,ifStatement);

            if(tokens.match(ELSE)) {
//                if (tokens.match(FOR)){
//                    parseForStatement();
//                } else if (tokens.match(IF)) {
//                    parseIfStatement()
//                } else if (tokens.match(PRINT)) {
//                    parsePrintStatement()
//                } else if () {
//
//                }
                require(ELSE, ifStatement);
                ArrayList <Statement> newStatements = new ArrayList<>();
                require(LEFT_BRACE, ifStatement);
                while(!tokens.match(EOF) && !tokens.match(RIGHT_BRACE)){
                    Statement newStatement = parseStatement();
                    newStatements.add(newStatement);
                }
                ifStatement.setElseStatements(newStatements);
                ifStatement.setEnd(require(RIGHT_BRACE,ifStatement));
            }
            ifStatement.setEnd(brace);
            return ifStatement;
        } else {
            return null;
        }
    }
    private Statement parseVariableStatement() {
        if(tokens.match(VAR)){
            VariableStatement varStatement = new VariableStatement();
            varStatement.setStart(tokens.consumeToken());
            varStatement.setVariableName(tokens.getCurrentToken().getStringValue());
            tokens.consumeToken();
            if(tokens.match(COLON)){
                require(COLON,varStatement);
                varStatement.setExplicitType(parseType().getType());
//                System.out.println(varStatement.getExplicitType());
            }
//            System.out.println(varStatement.getExplicitType());
//            varStatement.setExplicitType(parseType().getType());
//            System.out.println(parseType().getType());
            require(EQUAL,varStatement);

//            tokens.consumeToken();
            varStatement.setExpression(parseExpression());
//            System.out.println(varStatement.getExpression());
            varStatement.setEnd(varStatement.getExpression().getEnd());
//            if (tokens.hasMoreTokens()){ //missing consume somewhere in expressions bandaid fix doesn't pass boolean test w/o
//                tokens.consumeToken();
//            }

            return varStatement;
        } else {
            return null;
        }
    }
    private Statement parseAssignmentFunctionCallStatement() {
        if (tokens.match(IDENTIFIER)) {
            Token startToken = tokens.consumeToken();
            if (tokens.match(EQUAL)) {
                AssignmentStatement assignmentStatement = new AssignmentStatement();
                assignmentStatement.setStart(startToken);
                assignmentStatement.setVariableName(startToken.getStringValue());
                tokens.consumeToken();
                assignmentStatement.setExpression(parseExpression());
                assignmentStatement.setEnd(assignmentStatement.getExpression().getEnd());
                return assignmentStatement;
            } else if (tokens.match(LEFT_PAREN)) {

                return parseFunctionCallStatement(startToken);
            }
        } else {
            return null;
        }
        return null;
    }
    private Statement parseFunctionCallStatement(Token startToken) {
        tokens.consumeToken();
        ArrayList<Expression> expressionsList = new ArrayList<>();
        while(tokens.hasMoreTokens() && !tokens.match(RIGHT_PAREN)){
            if (tokens.match(COMMA)){
                tokens.consumeToken();
            }
            Expression newExpression = parseExpression();
            expressionsList.add(newExpression);

        }
        FunctionCallExpression functionExpression = new FunctionCallExpression(startToken.getStringValue(), expressionsList);
        FunctionCallStatement functionStatement = new FunctionCallStatement(functionExpression);
        functionStatement.setEnd(require(RIGHT_PAREN,functionStatement));
        functionStatement.setStart(startToken);
        return functionStatement;
    }
    private Statement parseFunctionDefinitionStatement() {
        if(tokens.match(FUNCTION)){
            Token start = tokens.consumeToken();
            FunctionDefinitionStatement funcDefStatement = new FunctionDefinitionStatement();
            funcDefStatement.setStart(start);

            Token functionName = require(IDENTIFIER,funcDefStatement);
            funcDefStatement.setName(functionName.getStringValue());
            require(LEFT_PAREN,funcDefStatement);
            while(!tokens.match(EOF) && !tokens.match(RIGHT_PAREN)){
                String parameter = require(IDENTIFIER,funcDefStatement).getStringValue();
//                System.out.println(parameter);
                TypeLiteral type = null;
                if(tokens.matchAndConsume(COLON)){
                    type = parseType();
                }

                if (tokens.match(COMMA)){
                    tokens.consumeToken();
//                    tokens.consumeToken();
//                    System.out.println("hits break");
                    funcDefStatement.addParameter(parameter,type);
                    continue;
                }
                funcDefStatement.addParameter(parameter,type);
//                funcDefStatement.addParameter(parameter,type);
            }
//            System.out.println(funcDefStatement.getParameterCount());
//            System.out.println(funcDefStatement.getParameterName(0));
//            System.out.println(funcDefStatement.getParameterName(1));
            require(RIGHT_PAREN,funcDefStatement);
            TypeLiteral type = null;
            if (tokens.matchAndConsume(COLON)){
                type = parseType();
            }
            funcDefStatement.setType(type);
            currentFunctionDefinition = funcDefStatement;

            require(LEFT_BRACE,funcDefStatement);
            ArrayList<Statement> statements = new ArrayList<>();
            while(!tokens.match(EOF) && !tokens.match(RIGHT_BRACE)){
                statements.add(parseStatement());
            }
//            if (!tokens.hasMoreTokens()){
//                tokens.consumeToken();
//            }
            require(RIGHT_BRACE,funcDefStatement);
            funcDefStatement.setBody(statements);

            return funcDefStatement;
        } else {
            return null;
        }
    }
    private Statement parseReturnStatement() {
        if(tokens.match(RETURN)){
            //carson
            ReturnStatement returnStatement = new ReturnStatement();
            returnStatement.setStart(tokens.consumeToken());
            returnStatement.setFunctionDefinition(currentFunctionDefinition);
//            require(LEFT_BRACE,returnStatement);
            if(!tokens.match(RIGHT_BRACE)){
                Expression expression = parseExpression();
                returnStatement.setExpression(expression);
            }
            return returnStatement;
        }else {
            return null;
        }
    }
    private Statement parseStatement() {
        return parseProgramStatement();
    }
    private Statement parseProgramStatement() {
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        Statement forStmt = parseForStatement();
        if (forStmt != null) {
            return forStmt;
        }
        Statement ifStmt = parseIfStatement();
        if (ifStmt != null) {
            return ifStmt;
        }
        Statement varStmt = parseVariableStatement();
        if (varStmt != null) {
            return varStmt;
        }
        Statement assignmentStmt = parseAssignmentFunctionCallStatement();
        if (assignmentStmt != null) {
            return assignmentStmt;
        }
        Statement functionDefStmt = parseFunctionDefinitionStatement();
        if (functionDefStmt != null){
            return functionDefStmt;
        }
        if (currentFunctionDefinition != null) {
            Statement returnStmt = parseReturnStatement();
            if (returnStmt != null) {
                return returnStmt;
            }
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    //============================================================
    //  Expressions
    //============================================================
    private TypeLiteral parseType() {
        TypeLiteral type = new TypeLiteral();
        Token token = tokens.consumeToken();
        if (token.getStringValue().equals("int")){
            type.setType(CatscriptType.INT);
            return type;
        } else if (token.getStringValue().equals("string")) {
            type.setType(CatscriptType.STRING);
            return type;
        } else if (token.getStringValue().equals("bool")) {
            type.setType(CatscriptType.BOOLEAN);
            type.setToken(token);
            return type;
        }else if (token.getStringValue().equals("list")) {
            type.setType(CatscriptType.getListType(CatscriptType.OBJECT));
            if(tokens.match(LESS)){
                tokens.consumeToken();
                TypeLiteral innerType = parseType();
                type.setType(CatscriptType.getListType(innerType.getType()));
                require(GREATER,type);
            }
            return type;
        }else if (token.getStringValue().equals("object")) {
            type.setType(CatscriptType.OBJECT);
            return type;
        } else {
            type.setType(CatscriptType.VOID);
        }
        return type;
    }
    private Expression parseExpression() {
        return parseEqualityExpression();
//        return parseAdditiveExpression();//to test
    }

    //--------new>

    private Expression parseEqualityExpression() {
        Expression expression = parseComparisonExpression();
        while (tokens.match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private Expression parseComparisonExpression() {
        Expression expression = parseAdditiveExpression();
        while (tokens.match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;
        }
        return expression;
    }

    //-------new<
    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }

    //    --new>
    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression();
        while (tokens.match(STAR, SLASH)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }
//    --new<

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if (tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringExpression.setToken(stringToken);
            return stringExpression;
        } else if (tokens.matchAndConsume(TRUE)) {
            return new BooleanLiteralExpression(true);
        } else if (tokens.matchAndConsume(FALSE)) {
            return new BooleanLiteralExpression(false);
        } else if (tokens.matchAndConsume(NULL)) {
            return new NullLiteralExpression();
        } else if (tokens.match(IDENTIFIER)) {
            return parseFunctionCall();
        } else if (tokens.match(LEFT_BRACKET)) {
            return parseListLiteral();
        }
//        else if (tokens.match(LEFT_PAREN)) {
//            tokens.consumeToken();
//            return new ParenthesizedExpression(parseExpression());
//        }
        else if (tokens.matchAndConsume(LEFT_PAREN)){
            ParenthesizedExpression pExpression = new ParenthesizedExpression(parseExpression());
            if (tokens.matchAndConsume(RIGHT_PAREN)){
                return pExpression;
            }
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        } else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    private  Expression parseListLiteral(){
        ArrayList<Expression> items = new ArrayList<>();
        tokens.consumeToken();
        if (tokens.match(LEFT_BRACKET)) {
            tokens.consumeToken();
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(items);
            return listLiteralExpression;
        }
        if (!tokens.match(RIGHT_BRACKET)){
            items.add(parseExpression());
        }
        while (tokens.match(COMMA)) {
            tokens.consumeToken();
            items.add(parseExpression());
        }
        ListLiteralExpression listLiteralExpression = new ListLiteralExpression(items);
        if (tokens.match(RIGHT_BRACKET)) {
            tokens.consumeToken();
        } else {
            listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
        }
        return listLiteralExpression;
    }
    private Expression parseFunctionCall() {
        Token identifierToken = tokens.consumeToken();
        IdentifierExpression identifierExpression = new IdentifierExpression(identifierToken.getStringValue());
        identifierExpression.setToken(identifierToken);
        if (tokens.match(LEFT_PAREN)) {
            ArrayList<Expression> arguments = new ArrayList<>();
            tokens.consumeToken();
            if (tokens.match(RIGHT_PAREN)) {
                tokens.consumeToken();
                FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), arguments);
                return functionCallExpression;
            }
            arguments.add(parseExpression());
            while (tokens.match(COMMA)) {
                tokens.consumeToken();
                arguments.add(parseExpression());
            }
            FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), arguments);
            if (tokens.match(RIGHT_PAREN)) {
                tokens.consumeToken();
                return functionCallExpression;
            } else {
                functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
            }
            return functionCallExpression;
        }
        return identifierExpression;
    }


    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
