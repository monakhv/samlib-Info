package monakhv.samlib.http;

import monakhv.samlib.log.Log;
import rx.subjects.Subject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/*
 * Copyright 2014  Dmitry Monakhov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this mFile except in compliance with the License.
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
 * 3/31/14.
 */

/**
 * Read web page and put data into text mFile
 *
 *  Y = A*X +B
 *  A = 0.55
 *  B=4.59
 *
 *
 *
 */
public class TextFileReader implements HttpClientController.PageReader {
    final private File mFile;
    final private Subject<Integer, Integer> mSubject;
    final long mSize;
    public TextFileReader(File file,long size,Subject<Integer, Integer> subject){
        mSubject=subject;
        this.mFile = file;
        mSize=size;
    }


    @Override
    public String doReadPage(InputStream content) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(content, HttpClientController.ENCODING));
        BufferedWriter bw = new BufferedWriter(new FileWriter(mFile));
        String inputLine = in.readLine();

        double size=0;
        double report;
        while (inputLine != null) {
            size+=inputLine.getBytes("UTF-8").length;
            report=(0.55*size/1024+4.59);
            mSubject.onNext((int) report);
            bw.write(inputLine);
            bw.newLine();
            inputLine = in.readLine();
        }
        size=size/1024.;
        Log.d("TextFileReader","size: "+size);
        Log.d("TextFileReader","size: "+ 0.55*size+4.59 +"   book size "+mSize);
        bw.flush();
        bw.close();
        return null;
    }
}
