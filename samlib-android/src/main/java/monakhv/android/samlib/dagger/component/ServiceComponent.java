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
 *  19.01.16 18:02
 *
 */

package monakhv.android.samlib.dagger.component;

import dagger.Subcomponent;
import monakhv.android.samlib.dagger.UpdateScope;
import monakhv.android.samlib.dagger.module.ServiceModule;

/**
 * Created by monakhv on 15.01.16.
 */
@UpdateScope
@Subcomponent(modules = {ServiceModule.class})
public interface ServiceComponent {



}
