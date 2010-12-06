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
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class BitPatternUtils {

    public static String hexToBinaryString(String hexValue, int minLength) {
        if (hexValue.matches("0x[0-9a-fA-F]+")) {
            StringBuilder sb = new StringBuilder(Integer.toBinaryString(Integer.parseInt(hexValue.substring(2), 16)));
            while (sb.length() < minLength) {
                sb.insert(0, '0');
            }
            return sb.toString();
        } else {
            throw new NumberFormatException("Invalid hex value: " + hexValue);
        }
    }

    public static String binaryToHexString(String binaryValue) {
        return "0x"+Integer.toHexString(Integer.parseInt(binaryValue,2)).toUpperCase();
    }

    public static String binaryToHexString(String binaryValue, int minLength) {
        StringBuilder sb = new StringBuilder(Integer.toHexString(Integer.parseInt(binaryValue,2)).toUpperCase());
        while (sb.length() < minLength) {
            sb.insert(0, '0');
        }
        return "0x"+sb.toString();
    }

    public static String toBinaryString(int value, int minLength) {
        StringBuilder sb = new StringBuilder(Integer.toBinaryString(value));
        while (sb.length() < minLength) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    public static String toHexString(int value, int minLength) {
        StringBuilder sb = new StringBuilder(Integer.toHexString(value).toUpperCase());
        while (sb.length() < minLength) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    private static int getExcessThreeValue(String bits) {
        return Integer.parseInt(bits, 2) - 4;
    }

    private static String toExcessThreeValue(int value) {
        return BitPatternUtils.toBinaryString(value + 4, 3);
    }

    public static IBitPattern addFloats(IBitPattern a, IBitPattern b) throws BitPatternOverflowException {
        float fA = byteToFloatValue(a);
        float fB = byteToFloatValue(b);
        float f = fA + fB;
        if( (f < -7.5f) || (f > 7.5f) ){
            throw new BitPatternOverflowException("Overflow caused by value: "+f);
        }else{
            return floatToBytePattern(f);
        }
    }

    public static IBitPattern floatToBytePattern(float f) {
        return new BitPattern(8, getByteBinaryStringForFloat(f));
    }

    public static float byteToFloatValue(IBitPattern bytePattern) {
        return getFloatValueForByteBinaryString(bytePattern.toBinaryString());
    }

    private static String getByteBinaryStringForFloat(float f) {
        String signBit = "0";
        if (f < 0) {
            signBit = "1";
            f = -f;
        }
        int integerPart = (int) f;

        float fractionalPart = f - integerPart;
        fractionalPart *= 16;

        float tmp = fractionalPart;
        int factor = 16;
        int count = 0;
        while ((tmp - (int) tmp > 0) && (count < 4)) {
            factor = factor * 2;
            tmp = (f - integerPart) * factor;
            count++;
        }
        String fractionalBits = Integer.toBinaryString((int) tmp);
        count = count + 4 - fractionalBits.length();
        while (count > 4) {
            fractionalBits = "0" + fractionalBits;
            count--;
        }
        int moveLeft = -count;
        String integerBits = integerPart == 0 ? "" : Integer.toBinaryString(integerPart);
        String exp = integerBits.length() == 0 ? toExcessThreeValue(moveLeft) : toExcessThreeValue(integerBits.length());
        String result = signBit + exp + integerBits + fractionalBits;
        while (result.length() < 8) {
            result = result + "0";
        }
        return result.substring(0, 8);
    }

    private static float getFloatValueForByteBinaryString(String bits) {
        if (bits.matches("[01]{8}")) {
            int sign = 1;
            if (bits.charAt(0) == '1') {
                sign = -1;
            }
            float result = 0f;

            int exponent = getExcessThreeValue(bits.substring(1, 4));
            StringBuilder mantissaBitsBuilder = new StringBuilder(bits.substring(4));

            if (exponent >= 0) {
                if (exponent > 4) {
                    for (int i = 4; i < exponent; i++) {
                        mantissaBitsBuilder.append('0');
                    }
                }
            } else {
                for (int i = 0; i > exponent; i--) {
                    mantissaBitsBuilder.insert(0, '0');
                }
            }
            String mantissaBits = mantissaBitsBuilder.toString();

            int integerPart = 0;
            if (exponent > 0) {
                integerPart = Integer.parseInt(mantissaBits.substring(0, exponent), 2);
                mantissaBits = mantissaBits.substring(exponent);
            }

            float fractionalPart = Integer.parseInt(mantissaBits, 2) / (float) (Math.pow(2, mantissaBits.length()));
            result = integerPart + fractionalPart;
            return sign * result;
        } else {
            throw new NumberFormatException("Invalid binary representation of a byte containing a float value: " + bits);
        }
    }

    public static void main(String[] args) {
        /*try{
        System.out.println(getFloatValueForByteBinaryString("01101011"));//2.75
        //01101011
        System.out.println(getFloatValueForByteBinaryString("00111100"));//0.375
        //00111100
        System.out.println(getFloatValueForByteBinaryString("01001100"));//0.75
        //01001100
        System.out.println(getFloatValueForByteBinaryString("11101011"));//-2.75
        //11101011
        System.out.println(getFloatValueForByteBinaryString("00001000"));//0.03125
        //00001000
        System.out.println(getFloatValueForByteBinaryString("00000010"));//0.0078125
        //00000010
        System.out.println(getFloatValueForByteBinaryString("00000001"));//0.00390625
        //00000001

        System.out.println(getFloatValueForByteBinaryString("10101101"));//-0.203125
        //10101101
        System.out.println(getFloatValueForByteBinaryString("01101010"));//2.5

        
        System.out.println(getByteBinaryStringForFloat(2.75f));
        System.out.println(getByteBinaryStringForFloat(0.375f));
        System.out.println(getByteBinaryStringForFloat(0.75f));
        System.out.println(getByteBinaryStringForFloat(-2.75f));
        System.out.println(getByteBinaryStringForFloat(0.03125f));
        System.out.println(getByteBinaryStringForFloat(0.0078125f));
        System.out.println(getByteBinaryStringForFloat(0.00390625f));
        System.out.println(getByteBinaryStringForFloat(-0.203125f));
        System.out.println(getByteBinaryStringForFloat(2.0625f)); // truncation error: will return the value for 2.5

        System.out.println("2.5 + 2 = "+byteToFloatValue(addFloats(new BitPattern(8, "01101010"), new BitPattern(8, "01101000"))));
        }catch(BitPatternOverflowException boe){
            System.err.println(boe.getMessage());
        }*/

        System.out.println(getFloatValueForByteBinaryString("11111111"));
    }
}
