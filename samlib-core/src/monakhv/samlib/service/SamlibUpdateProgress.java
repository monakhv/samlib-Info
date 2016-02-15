/*
 *  Copyright 2016 Dmitry Monakhov.
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
 *
 *  12.02.16 17:45
 *
 */

package monakhv.samlib.service;

/**
 * Created by monakhv on 12.02.16.
 */
public class SamlibUpdateProgress {
    int total;
    int current;
    String name;

    public SamlibUpdateProgress(int total, int current, String name) {
        this.total = total;
        this.current = current;
        this.name = name;
    }

    public int getTotal() {
        return total;
    }

    public int getCurrent() {
        return current;
    }

    public String getName() {
        return name;
    }
}
