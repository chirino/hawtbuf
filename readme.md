HawtBuf
===============

This library implements a simple interface with working with byte arrays. It
is a shame that the Java SDK did not come with a built in class that was just
simply a `byte[]`, `int offset`, `int length` class which provided a rich
interface similar to what the String class does for `char` arrays. This
library fills in that void by providing a Buffer class which does provide
that rich interface.

It also provides a java protobuf code generator to make it easy to encoded and decode objects to buffers and back.


