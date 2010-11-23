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

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class MalformedInstructionException extends Exception{

    private int line;
    private String instruction;

    public MalformedInstructionException(int line, String instruction){
        super();
        this.line = line;
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String getMessage() {
        return "Malformed instruction at line "+line+": "+instruction==null?"":instruction;
    }



}
