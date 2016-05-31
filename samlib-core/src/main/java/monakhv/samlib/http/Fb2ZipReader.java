package monakhv.samlib.http;



import monakhv.samlib.log.Log;
import rx.subjects.Subject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * Copyright 2014  Dmitry Monakhov
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
 * 4/1/14.
 */


/**
 *
 *  Y = A*X +B
 *  A =0.56
 *  B=4.55
 */
class Fb2ZipReader implements HttpClientController.PageReader {
    private static final String DEBUG_TAG="Fb2ZipReader";
    private File file;
    final private Subject<Integer, Integer> mSubject;
    private final long mSize;

    Fb2ZipReader(File file, long size, Subject<Integer, Integer> subject) {
        this.file = file;
        mSize=size;
        mSubject=subject;
    }


    @Override
    public String doReadPage(InputStream in) throws IOException {
        ZipInputStream zipInp = new ZipInputStream(in);
        ZipEntry ze;
        double size=0.;
        double report;
        while ((ze = zipInp.getNextEntry()) != null) {
            Log.d(DEBUG_TAG, "Unzipping " + ze.getName() + " into " + file.getAbsolutePath());

            if (!ze.isDirectory()) {
                FileOutputStream fOut = new FileOutputStream(file);
                final int BUFFER_SIZE = 2048;
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fOut, BUFFER_SIZE);
                int count ;

                byte[] buffer = new byte[BUFFER_SIZE];
                while ((count = zipInp.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    bufferedOutputStream.write(buffer, 0, count);
                    size+=count;
                    report=(0.56*size/1024.+4.55);
                    mSubject.onNext((int) report);
                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();

                zipInp.closeEntry();
                fOut.close();
            }
        }
        zipInp.close();
        size=size/1024.;
        Log.d(DEBUG_TAG,"total size: "+size);
        Log.d(DEBUG_TAG,"size: "+ 0.56*size+4.55 +"   book size "+mSize);
        return null;
    }
}
