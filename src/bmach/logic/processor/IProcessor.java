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

import bmach.logic.machine.IMachine;
import bmach.logic.memory.IMemoryAddress;
import bmach.logic.programcounter.IProgramCounter;
import bmach.logic.registers.IRegister;
import bmach.logic.registers.IRegisterAddress;
import java.io.PrintStream;
import util.binary.bitpattern.BitPatternOverflowException;
import util.binary.bitpattern.IBitPattern;
import util.patterns.observer.ISubject;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public interface IProcessor extends ISubject {

    public void init(IMachine machine);

    public void load(IMemoryAddress src, IRegisterAddress dest);

    public void load(IBitPattern value, IRegisterAddress dest);

    public void store(IRegisterAddress src, IMemoryAddress dest);

    public void move(IRegisterAddress src, IRegisterAddress dest);

    public void add(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException;

    public void addFloat(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException;

    public void and(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException;

    public void or(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException;

    public void xor(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException;

    public void rotate(IRegisterAddress address, int times);

    public void jump(IRegisterAddress address, IMemoryAddress target);

    public void execNext() throws MalformedProcessorInstructionException, BitPatternOverflowException;

    public void halt();

    public boolean hasReachedEnd();

    public void printRegistersHex(PrintStream out);

    public void printRegistersBinary(PrintStream out);

    public IRegister getRegister(int registerIndex);

    public int getNumberOfRegisters();

    public IProgramCounter getProgramCounter();
}
