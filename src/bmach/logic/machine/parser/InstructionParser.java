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
package bmach.logic.machine.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import util.binary.bitpattern.BitPatternUtils;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class InstructionParser implements IInstructionParser {

    private String hexInstructionPattern = "(\\s*)0x[1-9a-cA-C][0-9a-fA-F]{3}(\\s*//(.*))?";
    private String binaryInstructionPattern = "(\\s*)[01]{16}(\\s*//(.*))?";
    private String commentPattern = "\\s*//(.*)";
    private String emptyLinePattern = "\\s*";
    LineNumberReader r;

    public InstructionParser(InputStream in) {
        r = new LineNumberReader(new InputStreamReader(in));
    }

    public InstructionParser(Reader reader) {
        r = new LineNumberReader(reader);
    }

    public String parseNextInstruction() throws MalformedInstructionException {
        try {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.matches(hexInstructionPattern)) {
                    return BitPatternUtils.hexToBinaryString(line.trim().substring(0, 6), 16);
                } else if (line.matches(binaryInstructionPattern)) {
                    return line.trim().substring(0,16);
                } else if (line.matches(commentPattern) || line.matches(emptyLinePattern)) {
                } else {
                    throw new MalformedInstructionException(r.getLineNumber(), line);
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
