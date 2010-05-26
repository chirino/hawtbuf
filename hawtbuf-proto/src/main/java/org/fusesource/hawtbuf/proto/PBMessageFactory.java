/**
 *  Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.hawtbuf.proto;

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface PBMessageFactory<Bean extends PBMessage, Buffer extends MessageBuffer> {

   public Bean create();

   public Bean parseUnframed(org.fusesource.hawtbuf.proto.CodedInputStream data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException, java.io.IOException;
   public Bean parseUnframed(java.io.InputStream data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException, java.io.IOException;
   public Buffer parseUnframed(org.fusesource.hawtbuf.Buffer data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException;
   public Buffer parseUnframed(byte[] data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException;

   public Buffer parseFramed(org.fusesource.hawtbuf.proto.CodedInputStream data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException, java.io.IOException;
   public Buffer parseFramed(org.fusesource.hawtbuf.Buffer data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException;
   public Buffer parseFramed(byte[] data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException;
   public Buffer parseFramed(java.io.InputStream data) throws org.fusesource.hawtbuf.proto.InvalidProtocolBufferException, java.io.IOException;

}
