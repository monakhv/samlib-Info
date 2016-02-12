package monakhv.samlib.http;



import monakhv.samlib.log.Log;

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
public class Fb2ZipReader implements HttpClientController.PageReader {
    private File file;
    private long length;


    public Fb2ZipReader(File file) {
        this.file = file;
    }

    @Override
    public void setContentLength(long s) {
        length=s;
    }

    @Override
    public String doReadPage(InputStream in) throws IOException {
        ZipInputStream zipInp = new ZipInputStream(in);
        ZipEntry ze;
        while ((ze = zipInp.getNextEntry()) != null) {
            Log.v("Fb2ZipReader", "Unzipping " + ze.getName() + " into " + file.getParent());

            if (!ze.isDirectory()) {
                FileOutputStream fout = new FileOutputStream(file.getParent() + "/" + ze.getName());
                final int BUFFER_SIZE = 2048;
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fout, BUFFER_SIZE);
                int count = 0;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((count = zipInp.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    bufferedOutputStream.write(buffer, 0, count);
                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();

                zipInp.closeEntry();
                fout.close();
            }
        }
        zipInp.close();
        return null;
    }
}
