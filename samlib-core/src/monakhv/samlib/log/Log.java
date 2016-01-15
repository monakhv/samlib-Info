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
public class Log {
    protected static AbstractLogger logger;



    public static void  forceInit(AbstractLogger log){
       logger=log;
    }
    public static void v (String tag, String msg) {
        logger.verbose(tag,msg);

    }
    public static void d (String tag, String msg) {
        logger.debug(tag, msg);

    }
    public static void i (String tag, String msg) {
        logger.info(tag, msg);

    }
    public static void w (String tag, String msg) {
        logger.warn(tag, msg);

    }
    public static void e (String tag, String msg) {
        logger.error(tag, msg);

    }

    public static void v (String tag, String msg,Throwable ex) {
        logger.verbose(tag,msg,ex);
    }
    public static void d (String tag, String msg,Throwable ex) {
        logger.debug(tag,msg,ex);
    }
    public static void i (String tag, String msg,Throwable ex) {
        logger.info(tag,msg,ex);
    }
    public static void w (String tag, String msg,Throwable ex) {
        logger.warn(tag,msg,ex);
    }
    public static void e (String tag, String msg,Throwable ex) {
        logger.error(tag,msg,ex);
    }
}
