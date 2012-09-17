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
import bmach.logic.memory.IMainMemory;
import bmach.logic.memory.IMemoryAddress;
import bmach.logic.memory.MemoryAddress;
import bmach.logic.programcounter.IProgramCounter;
import bmach.logic.programcounter.ProgramCounter;
import bmach.logic.registers.IRegister;
import bmach.logic.registers.IRegisterAddress;
import bmach.logic.registers.Register;
import bmach.logic.registers.RegisterAddress;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import util.binary.bitpattern.BitPattern;
import util.binary.bitpattern.BitPatternOverflowException;
import util.binary.bitpattern.BitPatternUtils;
import util.binary.bitpattern.ByteBitPattern;
import util.binary.bitpattern.IBitPattern;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class Processor implements IProcessor {

    private IMachine machine;
    private IProgramCounter programCounter;
    private HashMap<IRegisterAddress, IRegister> registers;
    private List<IObserver> observers;
    private ByteBitPattern zero;
    private IRegisterAddress zeroRegisterAddress;
    private boolean hasReachedEnd;

    public Processor() {
        this.machine = null;
        this.programCounter = new ProgramCounter();
        this.registers = new HashMap<IRegisterAddress, IRegister>(16, 1.0f);
        this.observers = new ArrayList<IObserver>();
        this.hasReachedEnd = false;
        this.zero = new ByteBitPattern();
        this.zeroRegisterAddress = new RegisterAddress("0x" + Integer.toHexString(0));
        try{
            zero.setValue(0);
        }catch(Exception e){
        }
        generateRegisters();
    }

    private void generateRegisters() {
        for (int i = 0; i < 16; i++) {
            IRegisterAddress address = new RegisterAddress("0x" + Integer.toHexString(i));
            registers.put(address, new Register(address));
        }
    }

    public IRegister getRegister(int registerIndex) {
        return registers.get(new RegisterAddress("0x" + Integer.toHexString(registerIndex)));
    }

    public int getNumberOfRegisters() {
        return this.registers.size();
    }

    public IProgramCounter getProgramCounter() {
        return this.programCounter;
    }

    public boolean hasReachedEnd() {
        return this.hasReachedEnd;
    }

    public void init(IMachine machine) {
        this.machine = machine;
        this.observers.add(machine);
        this.programCounter.reset();
        this.hasReachedEnd = false;
        for (Iterator<IRegisterAddress> it = registers.keySet().iterator(); it.hasNext();) {
            IRegisterAddress address = it.next();
            IRegister register = registers.get(address);
            register.setContentValue("00000000");
        }
        notifyObservers(new ProcessorNotificationData("init"));
    }

    public void execNext() throws MalformedProcessorInstructionException, BitPatternOverflowException {
        IMainMemory memory = this.machine.getMemory();
        IMemoryAddress instructionAddress = programCounter.get();
        IMemoryAddress instructionAddressPlusOne = new MemoryAddress("0x" + Integer.toHexString(Integer.parseInt(instructionAddress.toHexString().substring(2), 16) + 1));

        IBitPattern byte1 = memory.get(instructionAddress).getContent();
        IBitPattern byte2 = memory.get(instructionAddressPlusOne).getContent();

        String opCode = byte1.toBinaryString().substring(0, 4);
        String register = byte1.toBinaryString().substring(4, 8);

        IMemoryAddress pcVal = programCounter.get();

        if (opCode.equals("0001") || opCode.equals("0010") || opCode.equals("0011") || opCode.equals("1011")) {
            if (opCode.equals("0001")) {// 0x1 : LOAD from memory
                IMemoryAddress src = new MemoryAddress(byte2.toBinaryString());
                IRegisterAddress dest = new RegisterAddress(register);
                load(src, dest);
            } else if (opCode.equals("0010")) {// 0x2 : LOAD bit pattern
                IRegisterAddress dest = new RegisterAddress(register);
                load(byte2, dest);
            } else if (opCode.equals("0011")) {// 0x3 : STORE to memory
                IRegisterAddress src = new RegisterAddress(register);
                IMemoryAddress dest = new MemoryAddress(byte2.toBinaryString());
                store(src, dest);
            } else {// 0xB : JUMP to target if address contents == contents at register 0
                IRegisterAddress address = new RegisterAddress(register);
                IMemoryAddress target = new MemoryAddress(byte2.toBinaryString());
                jump(address, target);
            }

        } else {
            String op1 = byte2.toBinaryString().substring(0, 4);
            String op2 = byte2.toBinaryString().substring(4, 8);

            if (opCode.equals("0100")) {// 0x4 : MOVE from R to S
                IRegisterAddress src = new RegisterAddress(op1);
                IRegisterAddress dest = new RegisterAddress(op2);
                move(src, dest);
            } else if (opCode.equals("0101")) {// 0x5 : ADD S and T ans save to R
                try {
                    IRegisterAddress src1 = new RegisterAddress(op1);
                    IRegisterAddress src2 = new RegisterAddress(op2);
                    IRegisterAddress dest = new RegisterAddress(register);
                    add(src1, src2, dest);
                } catch (BitPatternOverflowException boe) {
                    notifyObservers(new ProcessorNotificationData(boe));
                    throw boe;
                }
            } else if (opCode.equals("0110")) {// 0x6 : ADD floats in S and T ans save to R
                try {
                    IRegisterAddress src1 = new RegisterAddress(op1);
                    IRegisterAddress src2 = new RegisterAddress(op2);
                    IRegisterAddress dest = new RegisterAddress(register);
                    addFloat(src1, src2, dest);
                } catch (BitPatternOverflowException boe) {
                    notifyObservers(new ProcessorNotificationData(boe));
                    throw boe;
                }
            } else if (opCode.equals("0111")) {// 0x7 : OR S and T ans save to R
                try {
                    IRegisterAddress src1 = new RegisterAddress(op1);
                    IRegisterAddress src2 = new RegisterAddress(op2);
                    IRegisterAddress dest = new RegisterAddress(register);
                    or(src1, src2, dest);
                } catch (BitPatternOverflowException boe) {
                    notifyObservers(new ProcessorNotificationData(boe));
                    throw boe;
                }
            } else if (opCode.equals("1000")) {// 0x8 : AND S and T ans save to R
                try {
                    IRegisterAddress src1 = new RegisterAddress(op1);
                    IRegisterAddress src2 = new RegisterAddress(op2);
                    IRegisterAddress dest = new RegisterAddress(register);
                    and(src1, src2, dest);
                } catch (BitPatternOverflowException boe) {
                    notifyObservers(new ProcessorNotificationData(boe));
                    throw boe;
                }
            } else if (opCode.equals("1001")) {// 0x9 : XOR S and T ans save to R
                try {
                    IRegisterAddress src1 = new RegisterAddress(op1);
                    IRegisterAddress src2 = new RegisterAddress(op2);
                    IRegisterAddress dest = new RegisterAddress(register);
                    xor(src1, src2, dest);
                } catch (BitPatternOverflowException boe) {
                    notifyObservers(new ProcessorNotificationData(boe));
                    throw boe;
                }
            } else if (opCode.equals("1010")) {// 0xA : ROTATE R right X times
                IRegisterAddress address = new RegisterAddress(register);
                int times = Integer.parseInt(op2, 2);
                rotate(address, times);

            } else if (opCode.equals("1100")) {// 0xC : HALT
                hasReachedEnd = true;
                halt();
            } else {
                MalformedProcessorInstructionException mpie = new MalformedProcessorInstructionException(new BitPattern(16, byte1.toBinaryString() + byte2.toBinaryString()));
                notifyObservers(new ProcessorNotificationData(mpie));
                throw mpie;
            }
        }

        notifyObservers(new ProcessorNotificationData(new BitPattern(24, pcVal.toBinaryString() + byte1.toBinaryString() + byte2.toBinaryString())));

    }

    public void halt() {
        machine.halt();
        notifyObservers(new ProcessorNotificationData("halt"));
    }

    public void load(IMemoryAddress src, IRegisterAddress dest) {
        String newValue = machine.getMemory().get(src).getContent().toBinaryString();
        registers.get(dest).setContentValue(newValue);
        programCounter.inc();
    }

    public void load(IBitPattern value, IRegisterAddress dest) {
        registers.get(dest).setContentValue(value.toBinaryString());
        programCounter.inc();
    }

    public void store(IRegisterAddress src, IMemoryAddress dest) {
        String newValue = registers.get(src).getContent().toBinaryString();
        machine.getMemory().get(dest).setContentValue(newValue);
        programCounter.inc();
    }

    public void move(IRegisterAddress src, IRegisterAddress dest) {
        String newValue = registers.get(src).getContent().toBinaryString();
        registers.get(dest).setContentValue(newValue);
        programCounter.inc();
    }

    public void add(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException {
        IBitPattern value1 = registers.get(src1).getContent();
        IBitPattern value2 = registers.get(src2).getContent();
        registers.get(dest).setContentValue(BitPattern.add(value1, value2).toBinaryString());
        programCounter.inc();
    }

    public void addFloat(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException {
        IBitPattern value1 = registers.get(src1).getContent();
        IBitPattern value2 = registers.get(src2).getContent();
        registers.get(dest).setContentValue(BitPatternUtils.addFloats(value1, value2).toBinaryString());
        programCounter.inc();
    }

    public void and(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException {
        IBitPattern value1 = registers.get(src1).getContent();
        IBitPattern value2 = registers.get(src2).getContent();
        registers.get(dest).setContentValue(BitPattern.and(value1, value2).toBinaryString());
        programCounter.inc();
    }

    public void or(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException {
        IBitPattern value1 = registers.get(src1).getContent();
        IBitPattern value2 = registers.get(src2).getContent();
        registers.get(dest).setContentValue(BitPattern.or(value1, value2).toBinaryString());
        programCounter.inc();
    }

    public void xor(IRegisterAddress src1, IRegisterAddress src2, IRegisterAddress dest) throws BitPatternOverflowException {
        IBitPattern value1 = registers.get(src1).getContent();
        IBitPattern value2 = registers.get(src2).getContent();
        registers.get(dest).setContentValue(BitPattern.xor(value1, value2).toBinaryString());
        programCounter.inc();
    }

    public void rotate(IRegisterAddress address, int times) {
        IBitPattern registerContent = registers.get(address).getContent();
        for (int i = 0; i < times; i++) {
            registerContent.rotateRight();
        }
        registers.get(address).setContentValue(registerContent.toBinaryString());
        programCounter.inc();
    }

    public void jump(IRegisterAddress address, IMemoryAddress target) {
        IBitPattern op1 = registers.get(address).getContent();
        IBitPattern op2 = registers.get(zeroRegisterAddress).getContent();
        if (op2.compareTo(op1) == 0) {
            programCounter.set(target);
        } else {
            programCounter.inc();
        }
    }

    public void addObserver(IObserver observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    public void removeObserver(IObserver observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers(Object notificationData) {
        for (Iterator<IObserver> it = observers.iterator(); it.hasNext();) {
            IObserver iObserver = it.next();
            iObserver.notifyObserver(notificationData);
        }
    }

    public void printRegistersHex(PrintStream out) {
        for (int i = 0; i < 16; i++) {
            out.println(String.format("Register[%1$2d]: %2$s", i, registers.get(new RegisterAddress("0x" + Integer.toHexString(i))).getContent().toHexString()));
        }
    }

    public void printRegistersBinary(PrintStream out) {
        for (int i = 0; i < 16; i++) {
            out.println(String.format("Register[%1$2d]: %2$s", i, registers.get(new RegisterAddress("0x" + Integer.toHexString(i))).getContent().toBinaryString()));
        }
    }
}
