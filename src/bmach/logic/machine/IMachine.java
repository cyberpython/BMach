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

import bmach.logic.machine.parser.MalformedInstructionException;
import bmach.logic.memory.IMainMemory;
import bmach.logic.processor.IProcessor;
import java.io.InputStream;
import java.io.Reader;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public interface IMachine extends Runnable, IObserver {

    public void reset();

    public IProcessor getProcessor();

    public IMainMemory getMemory();

    public void loadInstructions(Reader r) throws MalformedInstructionException;

    public void loadInstructions(InputStream is) throws MalformedInstructionException;

    public void halt();

    public void setStepByStep(boolean stepByStep);

    public boolean getStepByStep();

    public void nextStep();
}
