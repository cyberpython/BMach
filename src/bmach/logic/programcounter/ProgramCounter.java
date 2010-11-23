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

package bmach.logic.programcounter;

import bmach.logic.memory.IMemoryAddress;
import bmach.logic.memory.MemoryAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class ProgramCounter implements IProgramCounter{

    private IMemoryAddress address;
    private List<IObserver> observers;

    public ProgramCounter() {
        this.address = new MemoryAddress("0x00");
        this.observers = new ArrayList<IObserver>();
    }

    public IMemoryAddress get() {
        return this.address;
    }

    public void set(IMemoryAddress address) {
        this.address = address;
        notifyObservers(new ProgramCounterNotificationData(address));
    }

    public void inc() {
        this.set(new MemoryAddress("0x"+Integer.toHexString( Integer.parseInt(this.address.toHexString().substring(2), 16) + 2 )));
        notifyObservers(new ProgramCounterNotificationData(address));
    }

    public void reset() {
        this.address = new MemoryAddress("0x00");
        notifyObservers(new ProgramCounterNotificationData(address));
    }

    public void addObserver(IObserver observer) {
        this.observers.add(observer);
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
}
