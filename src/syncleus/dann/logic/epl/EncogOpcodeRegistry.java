/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core

 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information on Heaton Research copyrights, licenses
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
package syncleus.dann.logic.epl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all known EPL opcodes. Extension programs should add new opcodes here.
 * The FunctionFactory selects a subset of opcodes from here that will be run.
 * <p/>
 * An opcode is identified by its name, and the number of parameters it accepts.
 * It is okay to add an opcode multiple times, the new opcode replaces the
 * previous.
 */
public enum EncogOpcodeRegistry {
    INSTANCE;

    /**
     * Construct a lookup key for the hash map.
     *
     * @param functionName The name of the opcode.
     * @param argCount     The number of parameters this opcode accepts.
     * @return Return the string key.
     */
    public static String createKey(final String functionName, final int argCount) {
        return functionName + '`' + argCount;
    }

    /**
     * A lookup for all of the opcodes.
     */
    private final Map<String, ProgramExtensionTemplate> registry = new HashMap<>();

    /**
     * Construct the opcode registry with all known opcodes. User programs can
     * always add additional opcodes later.
     */
    private EncogOpcodeRegistry() {
        add(StandardExtensions.EXTENSION_NOT_EQUAL);
        add(StandardExtensions.EXTENSION_NOT);
        add(StandardExtensions.EXTENSION_VAR_SUPPORT);
        add(StandardExtensions.EXTENSION_CONST_SUPPORT);
        add(StandardExtensions.EXTENSION_NEG);
        add(StandardExtensions.EXTENSION_ADD);
        add(StandardExtensions.EXTENSION_SUB);
        add(StandardExtensions.EXTENSION_MUL);
        add(StandardExtensions.EXTENSION_DIV);
        add(StandardExtensions.EXTENSION_POWER);
        add(StandardExtensions.EXTENSION_AND);
        add(StandardExtensions.EXTENSION_OR);
        add(StandardExtensions.EXTENSION_EQUAL);
        add(StandardExtensions.EXTENSION_GT);
        add(StandardExtensions.EXTENSION_LT);
        add(StandardExtensions.EXTENSION_GTE);
        add(StandardExtensions.EXTENSION_LTE);
        add(StandardExtensions.EXTENSION_ABS);
        add(StandardExtensions.EXTENSION_ACOS);
        add(StandardExtensions.EXTENSION_ASIN);
        add(StandardExtensions.EXTENSION_ATAN);
        add(StandardExtensions.EXTENSION_ATAN2);
        add(StandardExtensions.EXTENSION_CEIL);
        add(StandardExtensions.EXTENSION_COS);
        add(StandardExtensions.EXTENSION_COSH);
        add(StandardExtensions.EXTENSION_EXP);
        add(StandardExtensions.EXTENSION_FLOOR);
        add(StandardExtensions.EXTENSION_LOG);
        add(StandardExtensions.EXTENSION_LOG10);
        add(StandardExtensions.EXTENSION_MAX);
        add(StandardExtensions.EXTENSION_MIN);
        add(StandardExtensions.EXTENSION_POWFN);
        add(StandardExtensions.EXTENSION_RANDOM);
        add(StandardExtensions.EXTENSION_ROUND);
        add(StandardExtensions.EXTENSION_SIN);
        add(StandardExtensions.EXTENSION_SINH);
        add(StandardExtensions.EXTENSION_SQRT);
        add(StandardExtensions.EXTENSION_TAN);
        add(StandardExtensions.EXTENSION_TANH);
        add(StandardExtensions.EXTENSION_TODEG);
        add(StandardExtensions.EXTENSION_TORAD);
        add(StandardExtensions.EXTENSION_LENGTH);
        add(StandardExtensions.EXTENSION_FORMAT);
        add(StandardExtensions.EXTENSION_LEFT);
        add(StandardExtensions.EXTENSION_RIGHT);
        add(StandardExtensions.EXTENSION_CINT);
        add(StandardExtensions.EXTENSION_CFLOAT);
        add(StandardExtensions.EXTENSION_CSTR);
        add(StandardExtensions.EXTENSION_CBOOL);
        add(StandardExtensions.EXTENSION_IFF);
        add(StandardExtensions.EXTENSION_CLAMP);
        
        
        /*
        //http://jgap.sourceforge.net/javadoc/3.6/
        
CommandGene:        
-----------
AddAndStoreTerminal, ADF, AntCommand, AntCommand, Argument, CharacterProvider, CommandDynamicArity, CountMatrix, CountStones, EvaluateBoard, ExchangeMemory, ForLoop, ForXLoop, If, IfColor, IfElse, IfIsFree, IfIsOccupied, IfLessThanOrEqual, IfLessThanZero, IsOwnColor, Loop, LoopUntil, MathCommand, Push, PutStone, PutStone1, ReadBoard, ReadFromMatrix, ReadTerminal, ReadTerminalIndexed, ReplaceInMatrix, ResetMatrix, StoreTerminal, StoreTerminalIndexed, SubProgram, Terminal, TransferBoardToMemory, TransferMemory, Tupel, Variable, WriteToMatrix
        
MathCommand:
    Abs 	Returns the absolute value of a number.
    Add 	The add operation.
    Add3 	The add operation with three parameters (X + Y + Z).
    Add4 	The add operation with four parameters (W + X + Y + Z).
    AddAndStore 	The add operation that stores the result in internal memory afterwards.
    AddAndStoreTerminal 	Stores a value in the internal memory but adds the value already stored in the target memory cell before storing it.
    ADF 	Automatically Defined Function (ADF).
    And 	The boolean and operation.
    ArcCosine 	The arc cosine command.
    ArcSine 	The arc sine command.
    ArcTangent 	The arc tangent command.
    Ceil 	Returns the smallest (closest to negative infinity) double/float value that is not less than the argument and is equal to a mathematical integer.
    CharacterProvider 	Returns a single character out of a set of given characters.
    Cosine 	The cosine command.
    CountMatrix 	Counts either the elements in a row, in a column or in a diagonal of a two-dimensional matrix in internal memory.
    Divide 	The divide operation.
    Equals 	The equals operation.
    ExchangeMemory 	Exchanges the values of two memory cells.
    Exp 	The exponential operation.
    Floor 	Returns the largest (closest to positive infinity) double/float value that is not greater than the argument and is equal to a mathematical integer.
    ForLoop 	The for-loop.
    ForXLoop 	The for-loop loop from 0 to X-1.
    GreaterThan 	The Greater Than (x > y) operation.
    If 	The if-then construct.
    IfDyn 	The if-then construct with a dynamic number of children.
    IfElse 	The if-then-else construct.
    Increment 	The increment operation.
    IncrementMemory 	Increments the value of a memory cell and returns the incremented value.
    LesserThan 	The Lesser Than (x < y) operation.
    Log 	Returns the natural logarithm (base e) of a double value.
    Loop 	A loop that executes a given number of times.
    Max 	Returns the bigger of two values.
    Mean 	This class calculates the Mean of N numbers.
    Min 	Returns the smaller of two values.
    Modulo 	The modulo operation.
    Multiply 	The multiply operation.
    Multiply3 	The multiply operation with three argument (X * Y * Z).
    Not 	The boolean not operation.
    Or 	The boolean or operation.
    Pop 	Pops a value from the stack after it has been pushed onto it (PushCommand).
    Pow 	The power operation.
    Push 	Pushes a value onto the stack.
    RandomGenerator 	Returns a double/float value with a positive sign, greater than or equal to 0.0 and less than 1.0.
    ReadFromMatrix 	Reads a value from a two-dimensional matrix in internal memory.
    ReadTerminal 	Reads a value from the internal memory.
    ReadTerminalIndexed 	Reads a value from the internal indexed memory.
    ReplaceInMatrix 	Replaces occurrences of specified characters in a two-dimensional matrix with a given other character.
    ResetMatrix 	Resets a two-dimensional matrix in internal memory by setting each cell to an initial value.
    Round 	Returns the closest value to the argument.
    Sine 	The sine operation.
    StoreTerminal 	Stores a value in the internal memory.
    StoreTerminalIndexed 	Stores a value in the internal indexed memory.
    SubProgram 	A connector for independent subprograms (subtrees).
    Subtract 	The subtract operation.
    Switch 	The switch construct: if then return else return
    Tangent 	The tangent command.
    TransferMemory 	Transfers a memory value to another memory cell.
    Tupel 	Holds a tupel of n values of arbitrary type.
    WriteToMatrix 	Stores a value to a two-dimensional matrix in internal memory.
    Xor 	The boolean xor operation.      
        */
    }

    /**
     * Add an opcode. User programs should add opcodes here.
     *
     * @param ext The opcode to add.
     */
    public void add(final ProgramExtensionTemplate ext) {
        this.registry.put(
                EncogOpcodeRegistry.createKey(ext.getName(),
                        ext.getChildNodeCount()), ext);
    }

    public Collection<ProgramExtensionTemplate> findAllOpcodes() {
        return this.registry.values();
    }

    /**
     * Find the specified opcode.
     *
     * @param name The name of the opcode.
     * @param args The number of arguments.
     * @return The opcode if found, null otherwise.
     */
    public ProgramExtensionTemplate findOpcode(final String name, final int args) {
        final String key = EncogOpcodeRegistry.createKey(name, args);
        if (this.registry.containsKey(key)) {
            return this.registry.get(key);
        } else {
            return null;
        }
    }

}
