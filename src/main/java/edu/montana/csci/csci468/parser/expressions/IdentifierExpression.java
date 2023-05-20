package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import org.objectweb.asm.Opcodes;

public class IdentifierExpression extends Expression {
    private final String name;
    private CatscriptType type;

    public IdentifierExpression(String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        CatscriptType type = symbolTable.getSymbolType(getName());
        if (type == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            this.type = type;
        }
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        return runtime.getValue(name);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        //TODO Fix this mess - Fixed?
        Integer slot = code.resolveLocalStorageSlotFor(name);
        //inverse of variable statement code
            //global variable
            //primitave & non primative situation
            //push on a this pointer
            // load field
            //Opcodes.GETFIELD

            // local variable
            // primative and non primative situation
            //Opcodes.ILOAD or Opcodes.ALOAD

        if (slot == null) {
            // global variable
            if (type == CatscriptType.INT || type == CatscriptType.BOOLEAN) {
                code.addVarInstruction(Opcodes.ALOAD, 0);
                code.addFieldInstruction(Opcodes.GETFIELD, name, "I", code.getProgramInternalName());
            } else {
                code.addVarInstruction(Opcodes.ALOAD, 0);
                code.addFieldInstruction(Opcodes.GETFIELD, name, "L" + ByteCodeGenerator.internalNameFor(getType().getJavaType()) + ";", code.getProgramInternalName());
            }
        } else {
            // local variable
            if (type == CatscriptType.INT || type == CatscriptType.BOOLEAN) {
                code.addVarInstruction(Opcodes.ILOAD, slot);
            } else {
                code.addVarInstruction(Opcodes.ALOAD, slot);
            }
        }
//        super.compile(code);
    }


}
