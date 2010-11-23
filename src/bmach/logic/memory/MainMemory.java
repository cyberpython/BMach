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
package bmach.logic.memory;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import util.binary.bitpattern.IBitPattern;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class MainMemory implements IMainMemory {

    private HashMap<IMemoryAddress, IMemoryCell> cells;

    public MainMemory() {
        this.cells = new HashMap<IMemoryAddress, IMemoryCell>(256, 1.0f);
        generateCells();
    }

    private void generateCells() {
        for (int i = 0; i < 256; i++) {
            IMemoryAddress address = new MemoryAddress("0x" + Integer.toHexString(i));
            cells.put(address, new MemoryCell(address));
        }
    }

    public void clear() {
        for (Iterator<IMemoryAddress> it = cells.keySet().iterator(); it.hasNext();) {
            IMemoryAddress address = it.next();
            IMemoryCell cell = cells.get(address);
            cell.getContent().setValue("00000000");
        }
    }

    public IMemoryCell get(IMemoryAddress address) {
        return this.cells.get(address);
    }

    public IMemoryCell get(int index) {
        return this.cells.get(new MemoryAddress("0x" + Integer.toHexString(index)));
    }

    public int getNumberOfCells() {
        return this.cells.size();
    }

    public void set(IMemoryAddress address, IBitPattern content) {
        this.cells.get(address).setContent(content);
    }

    public void printBinary(PrintStream out) {
        for(int i=0; i<255; i++){
            out.println(String.format("Memory[%1$3d]: %2$s",i,get(new MemoryAddress("0x"+Integer.toHexString(i))).getContent().toBinaryString()));
        }
    }

    public void printHex(PrintStream out) {
        for(int i=0; i<255; i++){
            out.println(String.format("Memory[%1$3d]: %2$s",i,get(new MemoryAddress("0x"+Integer.toHexString(i))).getContent().toHexString()));
        }
    }

}
