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

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class ProgramCounterNotificationData {

    private IMemoryAddress address;

    public ProgramCounterNotificationData(IMemoryAddress address) {
        this.address = address;
    }

    public IMemoryAddress getAddress() {
        return address;
    }

    public void setAddress(IMemoryAddress address) {
        this.address = address;
    }
}
