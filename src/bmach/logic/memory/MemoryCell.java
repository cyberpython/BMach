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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import util.binary.bitpattern.ByteBitPattern;
import util.binary.bitpattern.IBitPattern;
import util.patterns.observer.IObserver;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class MemoryCell implements IMemoryCell {

    private IMemoryAddress address;
    private IBitPattern content;
    private List<IObserver> observers;

    public MemoryCell(IMemoryAddress address) {
        this.address = address;
        this.content = new ByteBitPattern();
        this.observers = new ArrayList<IObserver>();
    }

    public IMemoryAddress getAddress() {
        return this.address;
    }

    public void setAddress(IMemoryAddress address) {
        this.address = address;
        notifyObservers(new MemoryCellNotificationData(this));
    }

    public IBitPattern getContent() {
        return this.content;
    }

    public void setContent(IBitPattern content) {
        if(content.length() == 8){
            this.content = content;
            notifyObservers(new MemoryCellNotificationData(this));
        }else{
            throw new ArithmeticException("Cannot assign a bit pattern that is not 8 bits long to a byte.");
        }
    }

    public void setContentValue(String value) {
        this.content.setValue(value);
        notifyObservers(new MemoryCellNotificationData(this));
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
