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
package util.binary.bitpattern;

/**
 * An interface that provides the basic operations for bit patterns
 * representing binary values in two's complement.
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public interface IBitPattern extends Comparable<IBitPattern>{

    /**
     * Returns this IBitPattern's length (number of bits).
     * @return this IBitPattern's length
     */
    public int length();

    /**
     * Returns this IBitPattern's bit value at index 'bitIndex'.
     * @param bitIndex the index of the bit to be returned
     * @return the bit value at index 'bitIndex'
     * @throws IndexOutOfBoundsException if bitIndex is less than 0 or greater than this IBitPattern's length-1
     */
    public boolean get(int bitIndex);

    /**
     * Sets this IBitPattern's bit value at index 'bitIndex'.
     * @param bitIndex the index of the bit to be set
     * @param value the bit's new value
     * @throws IndexOutOfBoundsException if bitIndex is less than 0 or greater than this IBitPattern's length-1
     */
    public void set(int bitIndex, boolean value);

    /**
     * Returns this IBitPattern's value as an integer
     * @return this IBitPattern's value
     */
    public int intValue();

    /**
     * Sets this IBitPattern's value using the String 'pattern' as the source.
     * The 'pattern' parameter can be either a two's complement hexadecimal value matched by the
     * regular expression "0x[0-9a-fA-F]+" or a binary value in two's complement representation.
     * The value in binary form must have the same length as this IBitPattern.
     * @param pattern a String representing the new value in two's complement hex or binary representation
     * @throws NumberFormatException if pattern is not in binary or hex form, or the value in binary representation
     * does not have the same length with this IBitPattern.
     */
    public void setValue(String pattern);

    /**
     * Sets this IBitPattern's value using the integer 'value' as the source
     * The 'value' parameter must be an integer in the range 
     * [-2<sup>length()-1</sup>, 2<sup>length()-1</sup>-1]
     * @param value an integer representing the new value
     * @throws ArithmeticException if value is not in the range [-2<sup>length()-1</sup>, 2<sup>length()-1</sup>-1]
     */
    public void setValue(int value)throws NumberFormatException, BitPatternOverflowException;

    /**
     * Adds an IBitPattern's value to this IBitPattern's value
     * @param iBitPattern the IBitPattern whose value will be added to this IBitPattern's value
     * @throws ArithmeticException if the two operands have different lengths or the result is not in the
     * range [-2<sup>length()-1</sup>, 2<sup>length()-1</sup>-1]
     */
    public void add(IBitPattern iBitPattern) throws BitPatternOverflowException;

    /**
     * Applies the bitwise NOT operator to this IBitPattern's bits.
     */
    public void not();

    /**
     * Performs bitwise AND between this IBitPattern's bits and the bits of the
     * second operand.
     * @param iBitPattern the second AND operand
     * @throws ArithmeticException if the two operands have different lengths
     */
    public void and(IBitPattern iBitPattern);

    /**
     * Performs bitwise OR between this IBitPattern's bits and the bits of the
     * second operand
     * @param iBitPattern the second OR operand
     * @throws ArithmeticException if the two operands have different lengths
     */
    public void or(IBitPattern iBitPattern);

    /**
     * Performs bitwise XOR between this IBitPattern's bits and the bits of the
     * second operand
     * @param iBitPattern the second XOR operand
     * @throws ArithmeticException if the two operands have different lengths
     */
    public void xor(IBitPattern iBitPattern);

    /**
     * Shifts (arithmetic shift) this IBitPattern's bits one place to the left.
     */
    public void shiftLeft();

    /**
     * Shifts (arithmetic shift) this IBitPattern's bits one place to the right.
     */
    public void shiftRight();

    /**
     * Rotates this IBitPattern's bits to the left.
     */
    public void rotateLeft();

    /**
     * Rotates this IBitPattern's bits to the right.
     */
    public void rotateRight();

    /**
     * Returns this IBitPattern's value in binary representation as a String.
     * @return this IBitPattern's value
     */
    public String toBinaryString();

    /**
     * Returns this IBitPattern's value in hexadecimal representation as a String.
     * @return this IBitPattern's value
     */
    public String toHexString();
}
