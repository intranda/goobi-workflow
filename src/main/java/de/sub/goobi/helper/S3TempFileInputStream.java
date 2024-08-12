//package de.sub.goobi.helper;
//
///**
// * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
// *
// * Visit the websites for more information.
// *          - https://goobi.io
// *          - https://www.intranda.com
// *          - https://github.com/intranda/goobi-workflow
// *
// * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
// * Software Foundation; either version 2 of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
// * Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
// * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
// * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
// * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
// * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
// * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
// * exception statement from your version.
// */
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import software.amazon.awssdk.services.s3.model.S3ObjectInputStream;
//import com.amazonaws.util.IOUtils;
//
//import lombok.extern.log4j.Log4j2;
//
///**
// * This class stores the the {@link S3ObjectInputStream} into a temporary file and provides a {@link FileInputStream} to it. It is used to avoid
// * FileDescriptor leaks in the S3Object.
// *
// * When the Stream is closed, the temporary file gets removed.
// */
//@Log4j2
//public class S3TempFileInputStream extends InputStream {
//
//    private Path tempFile = null;
//    private FileInputStream inputStream;
//
//    public S3TempFileInputStream(S3ObjectInputStream stream) {
//        try {
//            tempFile = Files.createTempFile("s3file", ""); //NOSONAR, using temporary file is save here
//        } catch (IOException exception) {
//            log.warn(exception);
//        }
//        try (OutputStream fos = Files.newOutputStream(tempFile)) {
//            IOUtils.copy(stream, fos);
//            inputStream = new FileInputStream(tempFile.toFile());
//        } catch (IOException exception) {
//            log.warn(exception);
//        }
//    }
//
//    @Override
//    public int read() throws IOException {
//        return inputStream.read();
//    }
//
//    @Override
//    public int read(byte[] b, int off, int len) throws IOException {
//        return inputStream.read(b, off, len);
//    }
//
//    @Override
//    public void close() throws IOException {
//        inputStream.close();
//        if (tempFile != null && Files.exists(tempFile)) {
//            Files.delete(tempFile);
//        }
//    }
//}
