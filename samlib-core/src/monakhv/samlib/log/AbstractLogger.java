package monakhv.samlib.log;

/*
 * Copyright 2015  Dmitry Monakhov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 2/12/15.
 */
public interface AbstractLogger {
    public void verbose(String tag, String mes);
    public void debug(String tag, String mes);
    public void info(String tag, String mes);
    public void warn(String tag, String mes);
    public void error(String tag, String mes);


    public void verbose(String tag, String mes,Throwable ex);
    public void debug(String tag, String mes,Throwable ex);
    public void info(String tag, String mes,Throwable ex);
    public void warn(String tag, String mes,Throwable ex);
    public void error(String tag, String mes,Throwable ex);



}
