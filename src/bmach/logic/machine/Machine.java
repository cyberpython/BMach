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
package bmach.logic.machine;

import bmach.logic.machine.parser.IInstructionParser;
import bmach.logic.machine.parser.InstructionParser;
import bmach.logic.machine.parser.MalformedInstructionException;
import bmach.logic.memory.IMainMemory;
import bmach.logic.memory.IMemoryAddress;
import bmach.logic.memory.MainMemory;
import bmach.logic.memory.MemoryAddress;
import bmach.logic.processor.IProcessor;
import bmach.logic.processor.MalformedProcessorInstructionException;
import bmach.logic.processor.Processor;
import bmach.logic.processor.ProcessorNotificationData;
import bmach.logic.processor.ProcessorUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import util.binary.bitpattern.BitPattern;
import util.binary.bitpattern.BitPatternOverflowException;
import util.binary.bitpattern.IBitPattern;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class Machine implements IMachine {

    private final static long DEFAULT_SLEEP_TIME;
    public final static int STATUS_OK;
    public final static int STATUS_UNKNOWN_INSTRUCTION;
    public final static int STATUS_UNKNOWN_ERROR;

    static {
        DEFAULT_SLEEP_TIME = 500;
        STATUS_OK = 0;
        STATUS_UNKNOWN_INSTRUCTION = 1;
        STATUS_UNKNOWN_ERROR = 2;
    }
    private IProcessor processor;
    private IMainMemory memory;
    private boolean stop;
    private long sleepTime;
    private int statusCode;
    private String statusMsg;
    private boolean stepByStep;

    public Machine() {
        processor = new Processor();
        memory = new MainMemory();
        stop = false;
        sleepTime = DEFAULT_SLEEP_TIME;
        statusCode = STATUS_OK;
        statusMsg = "";
        stepByStep = false;
    }

    public IMainMemory getMemory() {
        return memory;
    }

    public IProcessor getProcessor() {
        return processor;
    }

    public void loadInstructions(InputStream is) throws MalformedInstructionException {
        load(is);
    }

    public void loadInstructions(Reader r) throws MalformedInstructionException {
        load(r);
    }

    public void reset() {
        processor.init(this);
        memory.clear();
    }

    public void setStepByStep(boolean stepByStep) {
        this.stepByStep = stepByStep;
    }

    public boolean getStepByStep() {
        return stepByStep;
    }

    public void nextStep() {
        proceed();
    }

    private void load(Object o) throws MalformedInstructionException {
        int i = 0;
        IInstructionParser p;
        if (o instanceof InputStream) {
            p = new InstructionParser((InputStream) o);
        } else if (o instanceof Reader) {
            p = new InstructionParser((Reader) o);
        } else {
            return;
        }
        String inst;

        while ((inst = p.parseNextInstruction()) != null) {
            IMemoryAddress address1 = new MemoryAddress("0x" + Integer.toHexString(i++));
            IMemoryAddress address2 = new MemoryAddress("0x" + Integer.toHexString(i++));

            memory.get(address1).setContentValue(inst.substring(0, 8));
            memory.get(address2).setContentValue(inst.substring(8, 16));
        }
    }

    public void halt() {
        stop = true;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void run() {
        stop = false;
        statusCode = STATUS_OK;
        statusMsg = "";
        processor.init(this);
        proceed();
        while ((!stop) && (!processor.hasReachedEnd())) {
            consumeTime(1000);
        }
    }

    public void notifyObserver(Object notificationData) {
        if(!stepByStep){
            if (notificationData instanceof ProcessorNotificationData) {
                Object data = ((ProcessorNotificationData) notificationData).getData();
                if ((data instanceof IBitPattern) && (!stop) && (!processor.hasReachedEnd())) {
                    consumeTime(sleepTime);
                    proceed();
                }
            }
        }
    }

    private void proceed() {
        try {
            processor.execNext();
        } catch (MalformedProcessorInstructionException mpie) {
            stop = true;
            statusCode = STATUS_UNKNOWN_INSTRUCTION;
            statusMsg = mpie.getMessage();
        } catch(BitPatternOverflowException boe) {
            stop = true;
            statusCode = STATUS_UNKNOWN_INSTRUCTION;
            statusMsg = boe.getMessage();
        }
    }

    private void consumeTime(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
        }
    }

    public static void main(String[] args) {


        IMachine machine = new Machine();
        IProcessor p = machine.getProcessor();
        p.addObserver(new IObserver() {

            public void notifyObserver(Object notificationData) {
                if (notificationData instanceof ProcessorNotificationData) {
                    Object data = ((ProcessorNotificationData) notificationData).getData();
                    if (data instanceof IBitPattern) {
                        //System.out.println("Command executed: " + ((IBitPattern) data).toHexString());
                        try {
                            IBitPattern inst = new BitPattern(16,((IBitPattern) data).toBinaryString().substring(8));
                            System.out.println("Command executed: " + ProcessorUtils.instructionToString(inst));
                        } catch (MalformedProcessorInstructionException mpie) {
                        }
                    }
                }
            }
        });

        try {
            machine.loadInstructions(new FileReader("/home/cyberpython/Desktop/test2.bma"));
            Thread t = new Thread(machine);
            t.start();
            try {
                t.join();
            } catch (InterruptedException ie) {
            }
            System.out.println();
            p.printRegistersHex(System.out);
            System.out.println();
            machine.getMemory().printHex(System.out);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (MalformedInstructionException mie) {
            mie.printStackTrace();
        }
    }
}
