/*
 *  Copyright 2010 Georgios Migdos <cyberpython@gmail.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package bmach.logic.processor;

import util.binary.bitpattern.BitPatternUtils;
import util.binary.bitpattern.IBitPattern;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class ProcessorUtils {
    
    private static final String hexInstructionPattern = "0x[1-9a-cA-C][0-9a-fA-F]{3}";

    public static String instructionToString(IBitPattern instruction) throws MalformedProcessorInstructionException{
        if(instruction.toHexString().matches(hexInstructionPattern)){
            return instructionToString(instruction.toBinaryString());
        }else{
            throw new MalformedProcessorInstructionException(instruction);
        }
    }

    public static String instructionToString(String instruction){
        String byte1 = instruction.substring(0,8);
        String byte2 = instruction.substring(8);

        String opCode = byte1.substring(0, 4);
        String register = byte1.substring(4, 8);

        if (opCode.equals("0001") || opCode.equals("0010") || opCode.equals("0011") || opCode.equals("1011")) {
            if (opCode.equals("0001")) {
                return "LOAD register "+BitPatternUtils.binaryToHexString(register)+" from memory address "+BitPatternUtils.binaryToHexString(byte2);
            } else if (opCode.equals("0010")) {// 0x2 : LOAD bit pattern
                return "LOAD register "+BitPatternUtils.binaryToHexString(register)+" with the value "+BitPatternUtils.binaryToHexString(byte2);
            } else if (opCode.equals("0011")) {// 0x3 : STORE to memory
                return "STORE from register "+BitPatternUtils.binaryToHexString(register)+" to memory address "+BitPatternUtils.binaryToHexString(byte2);
            } else {// 0xB : JUMP to target if address contents == 0
                return "JUMP to memory address "+BitPatternUtils.binaryToHexString(byte2)+" if register "+BitPatternUtils.binaryToHexString(register)+" contents equal zero";
            }

        } else {
            String op1 = byte2.substring(0, 4);
            String op2 = byte2.substring(4, 8);

            if (opCode.equals("0100")) {// 0x4 : MOVE from R to S
                return "MOVE from register "+BitPatternUtils.binaryToHexString(op1)+" to register "+BitPatternUtils.binaryToHexString(op2);
            } else if (opCode.equals("0101")) {// 0x5 : ADD S and T ans save to R
                return "ADD the values in registers "+BitPatternUtils.binaryToHexString(op1)+" and "+BitPatternUtils.binaryToHexString(op2)+" and leave result in "+BitPatternUtils.binaryToHexString(register);
            } else if (opCode.equals("0110")) {// 0x6 : ADD floats in S and T ans save to R
                return "ADD as floating-point numbers values in registers "+BitPatternUtils.binaryToHexString(op1)+" and "+BitPatternUtils.binaryToHexString(op2)+" and leave result in "+BitPatternUtils.binaryToHexString(register);
            } else if (opCode.equals("0111")) {// 0x7 : OR S and T ans save to R
                return "OR the values in registers "+BitPatternUtils.binaryToHexString(op1)+" and "+BitPatternUtils.binaryToHexString(op2)+" and leave result in "+BitPatternUtils.binaryToHexString(register);
            } else if (opCode.equals("1000")) {// 0x8 : AND S and T ans save to R
                return "AND the values in registers "+BitPatternUtils.binaryToHexString(op1)+" and "+BitPatternUtils.binaryToHexString(op2)+" and leave result in "+BitPatternUtils.binaryToHexString(register);
            } else if (opCode.equals("1001")) {// 0x9 : XOR S and T ans save to R
                return "XOR the values in registers "+BitPatternUtils.binaryToHexString(op1)+" and "+BitPatternUtils.binaryToHexString(op2)+" and leave result in "+BitPatternUtils.binaryToHexString(register);
            } else if (opCode.equals("1010")) {// 0xA : ROTATE R right X times
                int times = Integer.parseInt(op2, 2);
                return "ROTATE the value in register "+BitPatternUtils.binaryToHexString(register)+" "+times+" times";
            } else if (opCode.equals("1100")) {// 0xC : HALT
                return "HALT";
            } else {
                return "UNKNOWN INSTRUCTION";
            }
        }
    }

}
