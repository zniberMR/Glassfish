// WARNING: This file is autogenerated. DO NOT EDIT!
// Generated Sat May 02 18:23:33 +1000 2009
package com.kenai.constantine.platform.fake;
public enum OpenFlags implements com.kenai.constantine.Constant {
O_RDONLY(0x1),
O_WRONLY(0x2),
O_RDWR(0x4),
O_ACCMODE(0x8),
O_NONBLOCK(0x10),
O_APPEND(0x20),
O_SYNC(0x40),
O_SHLOCK(0x80),
O_EXLOCK(0x100),
O_ASYNC(0x200),
O_FSYNC(0x400),
O_NOFOLLOW(0x800),
O_CREAT(0x1000),
O_TRUNC(0x2000),
O_EXCL(0x4000),
O_EVTONLY(0x8000),
O_DIRECTORY(0x10000),
O_SYMLINK(0x20000),
O_BINARY(0x40000),
O_NOCTTY(0x80000);
private final int value;
private OpenFlags(int value) { this.value = value; }
public static final long MIN_VALUE = 1;
public static final long MAX_VALUE = 0x80000;
public final int value() { return value; }
}
